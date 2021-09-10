package PDBViewer.presenter;

import PDBViewer.model.PDBFile;
import PDBViewer.model.PDBWeb;
import PDBViewer.redoundo.PropertyCommand;
import PDBViewer.redoundo.UndoRedoManager;
import PDBViewer.view.WindowController;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.ArrayList;

public class WindowPresenter {

    private final WindowController controller;
    private final Stage stage;
    private PDBFile model;
    private String pdbCode;
    private final UndoRedoManager undoManager;
    private final Service<ArrayList<String>> textService = new Service<>() {
        @Override
        protected Task<ArrayList<String>> createTask() {
            return new pdbTextTask(model);
        }
    };
    private final Service<PDBFile> modelService = new Service<>() {
        @Override
        protected Task<PDBFile> createTask() {
            return new changeModelTask(pdbCode, controller);
        }
    };

    public WindowPresenter(Stage stage, WindowController controller) {
        this.controller = controller;
        this.stage = stage;
        this.undoManager = new UndoRedoManager();

        setupUndoRedoInteraction(controller);

        // show/hide modelSelection in visualisation tab, SimpleIntegerProperty updated via parse button
        var sizeModelChoiceSize = new SimpleIntegerProperty(controller.getModelChoice().getItems().size(), "sizeModelChoiceSize");
        controller.getModelChoice().disableProperty().bind(sizeModelChoiceSize.greaterThan(1).not());
        controller.getModelChoice().setValue(1);

        setupServices(controller, sizeModelChoiceSize);

        setupButtons(controller);
        setupPDBSearch(controller);

        controller.getModelChoice().valueProperty().addListener((v, o, n) -> {
            if (n != null && o != null && !n.equals(o)) {
                controller.getFigurePane().getChildren().clear();
                System.out.println("model choice listener triggered");
                new ViewPresenter(controller, model);
            }
        });
        this.menuButtons();
    }

    private void setupServices(WindowController controller, SimpleIntegerProperty sizeModelChoiceSize) {
        // service to parse pdb file in background
        textService.setOnScheduled(v -> clearAll(controller));
        textService.setOnSucceeded(v -> {
            controller.getPdbText().setItems(FXCollections.observableArrayList(textService.getValue()));
            controller.getPdbText().scrollTo(0);
        });

        modelService.setOnSucceeded(v -> {
            this.model = modelService.getValue();

            populateFocusChoice(controller);
            populateModelChoice(controller, sizeModelChoiceSize);

            textService.restart();
            new ViewPresenter(controller, model);

            controller.getAbstractContent().setText(fillReportTab());
            ChartPresenter.setupChartTab(this.model, controller);
        });
    }

    private void setupPDBSearch(WindowController controller) {
        // get default value for List of pdb codes
        controller.getPdbCodeList().setItems(
                FXCollections.observableArrayList(
                        PDBWeb.getPDBEntries(controller.getEntryField().getText())));
        controller.getEntryField().textProperty().addListener(e ->
                controller.getPdbCodeList().setItems(
                        FXCollections.observableArrayList(
                                PDBWeb.getPDBEntries(controller.getEntryField().getText())))
        );
    }

    private void setupButtons(WindowController controller) {
        // Only let user parse if pdb code is selected and listview in focus (no unnecessary re-parsing of already parsed code)
        controller.getParseButton().disableProperty().bind(
                textService.runningProperty().or(
                        controller.getEntryField().textProperty().length().isEqualTo(4).or(
                                controller.getPdbCodeList().getSelectionModel().selectedItemProperty().isNotNull()))
                        .not()
        );
        controller.getParseButton().setOnAction(e -> {
            controller.getModelChoice().getItems().clear();
            controller.getFocusChoice().getItems().clear();

            var enteredQuery = controller.getEntryField().getText();
            //String pdbCode;
            if (enteredQuery.length() == 4) {
                pdbCode = enteredQuery;
            } else {
                pdbCode = controller.getPdbCodeList().getSelectionModel().getSelectedItem();
            }
            modelService.restart();
        });

        // Simple Button Listeners
        controller.getClearSearchButton().disableProperty().bind(controller.getEntryField().textProperty().isEmpty());
        controller.getClearSearchButton().setOnAction(e -> controller.getEntryField().clear());
    }

    private void populateFocusChoice(WindowController controller) {
        // populate focus choice
        controller.getFocusChoice().getItems().removeAll();
        controller.getFocusChoice().getItems().add("All");
        model.getProtein().get(0).forEach(chain -> controller.getFocusChoice().getItems().add(String.valueOf(chain.getChainID())));
        controller.getFocusChoice().setValue("All");
    }

    private void populateModelChoice(WindowController controller, SimpleIntegerProperty sizeModelChoiceSize) {
        // populate model choice
        controller.getModelChoice().getItems().removeAll();
        for (var item : this.model.getProtein()) {
            controller.getModelChoice().getItems().add(item.getId());
        }
        controller.getModelChoice().setValue(controller.getModelChoice().getItems().get(0));
        sizeModelChoiceSize.setValue(controller.getModelChoice().getItems().size());
    }

    private static void clearAll(WindowController controller) {
        controller.getFigurePane().getChildren().clear();
        controller.getPdbText().getItems().clear();

        if (controller.getChartTab().getChildren().size() > 1)
            controller.getChartTab().getChildren().remove(1);
    }

    public static void savePDB(Stage stage, PDBFile model) {
        if (model == null) {
            var info = new Alert(Alert.AlertType.ERROR, "No pdb entry loaded!");
            info.showAndWait();
        } else {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Please choose saving location");
            File selectedDirectory = chooser.showDialog(stage);
            var path = selectedDirectory.toPath();

            // only save if user confirms chosen path
            var alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("");
            alert.setHeaderText("Do you want to save the file?");
            alert.setContentText("directory: " + path);
            alert.showAndWait()
                    .filter(response -> response == ButtonType.OK)
                    .ifPresent(response -> {
                        model.savePDBFile(path);
                        var info = new Alert(Alert.AlertType.INFORMATION, "Saved!");
                        info.showAndWait();
                    });
        }
    }

    private void setupUndoRedoInteraction(WindowController controller) {
        controller.getUndoMenu().setOnAction(e -> undoManager.undo());
        controller.getUndoMenu().textProperty().bind(undoManager.undoLabelProperty());
        controller.getUndoMenu().disableProperty().bind(undoManager.canUndoProperty().not());
        controller.getRedoMenu().setOnAction(e -> undoManager.redo());
        controller.getRedoMenu().textProperty().bind(undoManager.redoLabelProperty());
        controller.getRedoMenu().disableProperty().bind(undoManager.canRedoProperty().not());

        controller.getRadiusScale().valueProperty().addListener((v, o, n) ->
                undoManager.add(new PropertyCommand<>("Atom Radius", (DoubleProperty) v, o, n)));

        controller.getDiameterScale().valueProperty().addListener((v, o, n) ->
                undoManager.add(new PropertyCommand<>("Bond Thickness", (DoubleProperty) v, o, n)));

        controller.getAtomsChecked().selectedProperty().addListener((v, o, n) ->
                undoManager.add(new PropertyCommand<>("show Atoms", (BooleanProperty) v, o, n)));

        controller.getRibbonChecked().selectedProperty().addListener((v, o, n) ->
                undoManager.add(new PropertyCommand<>("show Bonds", (BooleanProperty) v, o, n)));

        controller.getBondsChecked().selectedProperty().addListener((v, o, n) ->
                undoManager.add(new PropertyCommand<>("show Ribbon", (BooleanProperty) v, o, n)));

        controller.getColorChoice().valueProperty().addListener((v, o, n) ->
                undoManager.add(new PropertyCommand<>("changed color", (ObjectProperty<String>) v, o, n)));

        controller.getFocusChoice().valueProperty().addListener((v, o, n) ->
                undoManager.add(new PropertyCommand<>("changed highlight", (ObjectProperty<String>) v, o, n)));

        controller.getModelChoice().valueProperty().addListener((v, o, n) ->
                undoManager.add(new PropertyCommand<>("changed model", (ObjectProperty<Integer>) v, o, n)));
    }

    private void menuButtons() {

        controller.getCopyImageMenuItem().setOnAction(e -> {

            var content = new ClipboardContent();
            content.putImage(
                    controller.getFigurePane().snapshot(null, null));
            Clipboard.getSystemClipboard().setContent(content);
        });

        var darkThemeToggledProperty = new SimpleBooleanProperty(controller.getDarkThemeMenu(), "Dark Theme Toggle", false);
        controller.getDarkThemeMenu().setOnAction(e -> {
            darkThemeToggledProperty.setValue(!darkThemeToggledProperty.getValue());
            if (darkThemeToggledProperty.getValue()) {
                stage.getScene().getStylesheets().add("/mondena_dark.css");
            } else {
                stage.getScene().getStylesheets().remove("/mondena_dark.css");
            }
        });

        controller.getAboutMenu().setOnAction(e -> {
            var alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("About this thing!");
            alert.setHeaderText("About this program");
            alert.setContentText("""
                    This application fetches and visualizes pdb Files from rcsb.org
                                    
                    Author: Sean Klein
                    """);
            alert.showAndWait();
        });
        controller.getOpenMenu().setOnAction(e -> openPDB(stage, this.controller));
        controller.getClearMenu().setOnAction(e -> clearAll(this.controller));
        controller.getSaveMenu().setOnAction(e -> savePDB(stage, model));
        controller.getExitMenu().setOnAction(e -> System.exit(0));
    }

    private String fillReportTab() {
        var content = new StringBuilder(this.model.getAbstractText());
        content.append("\n\nChains:\n");
        this.model.getChainSequences().forEach((chain, seq) ->
                content.append(chain.getChainID())
                        .append(": \n")
                        .append(seq)
                        .append("\n")
        );
        return content.toString();
    }

    private void openPDB(Stage stage, WindowController controller) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open PDB File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("pdb files", "*.pdb"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            clearAll(controller);
            this.model = new PDBFile(Path.of(selectedFile.getPath()));
            this.textService.restart();
            new ViewPresenter(controller, this.model);
        }
    }

    private static class pdbTextTask extends Task<ArrayList<String>> {

        BufferedReader reader;
        long size;

        pdbTextTask(PDBFile model) {
            this.reader = new BufferedReader(new StringReader(model.getContent()));
            this.size = model.getContent().lines().count();
        }

        @Override
        public ArrayList<String> call() throws IOException {

            long count = 0;
            ArrayList<String> lines = new ArrayList<>();

            var currLine = reader.readLine();
            while (currLine != null && currLine.trim().length() > 0) {

                lines.add(currLine);
                count++;
                if (isCancelled()) return null;
                updateProgress(count / size, size);

                currLine = reader.readLine();
            }
            reader.close();

            return lines;
        }
    }

    private static class changeModelTask extends Task<PDBFile> {

        PDBFile model;
        WindowController controller;

        changeModelTask(String pdbID, WindowController controller) {
            this.model = new PDBFile(pdbID);
            this.controller = controller;
        }

        @Override
        public PDBFile call() {

            return this.model;
        }
    }

}
