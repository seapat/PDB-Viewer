package CoV2StructureExplorer.presenter;

import CoV2StructureExplorer.model.PDBFile;
import CoV2StructureExplorer.model.PDBWeb;
import CoV2StructureExplorer.view.WindowController;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
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

    private String pdbCode;
    private PDBFile model;
    private final Service<ArrayList<String>> textService = new Service<>() {
        @Override
        protected Task<ArrayList<String>> createTask() {
            return new pdbTextTask(model);
        }
    };
    private final WindowController controller;
    private final Service<PDBFile> modelService = new Service<>() {
        @Override
        protected Task<PDBFile> createTask() {
            return new changeModelTask(pdbCode, controller);
        }
    };
    private final Stage stage;
    private ViewPresenter view;

    public WindowPresenter(Stage stage, WindowController controller) {
        this.controller = controller;
        this.stage = stage;

        // setup ChoiceBox
        controller.getColorChoice().getItems().addAll("Atoms", "Structure", "Chains", "Residue");
        controller.getColorChoice().setValue("Atoms");

        // show/hide modelSelection in visualisation tab, SimpleIntegerProperty updated via parse button
        var sizeModelChoiceSize = new SimpleIntegerProperty(controller.getModelChoice().getItems().size(), "sizeModelChoiceSize");
        controller.getModelChoice().visibleProperty().bind(sizeModelChoiceSize.greaterThan(1));
        controller.getModelLabel().visibleProperty().bind(sizeModelChoiceSize.greaterThan(1));
        controller.getModelChoice().managedProperty().bind(sizeModelChoiceSize.greaterThan(1));
        controller.getModelLabel().managedProperty().bind(sizeModelChoiceSize.greaterThan(1));
        controller.getModelChoice().setValue(1);

        var sizeChainChoiceSize = new SimpleIntegerProperty(controller.getFocusChoice().getItems().size(), "sizeChainChoiceSize");
        controller.getFocusChoice().visibleProperty().bind(sizeChainChoiceSize.greaterThan(2));
        controller.getFocusLabel().visibleProperty().bind(sizeChainChoiceSize.greaterThan(2));
        controller.getFocusChoice().managedProperty().bind(sizeChainChoiceSize.greaterThan(2));
        controller.getFocusLabel().managedProperty().bind(sizeChainChoiceSize.greaterThan(2));
        controller.getFocusChoice().setValue("All");

        // service to parse pdb file in background
        textService.setOnScheduled(v -> clearAll(controller));
        textService.setOnSucceeded(v -> {
            controller.getPdbText().setItems(FXCollections.observableArrayList(textService.getValue()));
            controller.getPdbText().scrollTo(0);
        });

//        modelService.setOnSucceeded(v -> clearAll(controller));
        modelService.setOnSucceeded(v -> {
            this.model = modelService.getValue();

            // populate focus choice
            controller.getFocusChoice().getItems().removeAll();
            controller.getFocusChoice().getItems().add("All");
            model.getProtein().get(0).forEach( chain ->  controller.getFocusChoice().getItems().add(String.valueOf(chain.getChainID())));
            controller.getFocusChoice().setValue("All");

            // populate model choice
            controller.getModelChoice().getItems().removeAll();
            for (var item : this.model.getProtein()) {
                controller.getModelChoice().getItems().add(item.getId());
            }
            controller.getModelChoice().setValue(controller.getModelChoice().getItems().get(0));
            sizeModelChoiceSize.setValue(controller.getModelChoice().getItems().size());
            sizeChainChoiceSize.setValue(controller.getFocusChoice().getItems().size());


            textService.restart();

            this.view = new ViewPresenter(controller, model);

            controller.getAbstractContent().setText(fillReportTab());
            ChartPresenter.setupChartTab(this.model, controller);
        });

        // Only let user parse if pdb code is selected and listview in focus (no unnecessary re-parsing of already parsed code)
        controller.getParseButton().disableProperty().bind(
                // TODO: check if parse button is disabled correctly when service is running
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
//            this.model = new PDBFile(pdbCode);
            modelService.restart();
        });


        // Simple Button Listeners
        controller.getClearSearchButton().disableProperty().bind(controller.getEntryField().textProperty().isEmpty());
        controller.getClearSearchButton().setOnAction(e -> controller.getEntryField().clear());

        // get default value for List of pdb codes
        controller.getPdbCodeList().setItems(
                FXCollections.observableArrayList(
                        PDBWeb.getPDBEntries(controller.getEntryField().getText())));
        controller.getEntryField().textProperty().addListener(e ->
                controller.getPdbCodeList().setItems(
                        FXCollections.observableArrayList(
                                PDBWeb.getPDBEntries(controller.getEntryField().getText())))
        );
        controller.getModelChoice().valueProperty().addListener((v, o, n) -> {
            if (n != null && o != null && !n.equals(o)) {
                controller.getFigurePane().getChildren().clear();
                System.out.println("model choice listener triggered");
                // TODO: set up in a way that does not reset camera
                view = new ViewPresenter(controller, model);
            }
        });

        this.menuButtons();
    }

    // TODO: move to model
    private static void savePDB(Stage stage, WindowController controller, PDBFile model) {
        if (model == null) {
            var info = new Alert(Alert.AlertType.ERROR, "No pdb entry loaded!");
            info.showAndWait();
        } else {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Please choose saving location");
            File selectedDirectory = chooser.showDialog(stage);
            var path = selectedDirectory.toPath(); //selectedDirectory.getAbsolutePath();

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

    private static void clearAll(WindowController controller) {
        controller.getFigurePane().getChildren().clear();
        controller.getChartTab().setContent(null);
        controller.getPdbText().getItems().clear();
    }

    private void menuButtons() {
        // Menu item Listeners
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
        controller.getSaveMenu().setOnAction(e -> savePDB(stage, controller, model));
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
            this.view = new ViewPresenter(controller, this.model);
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

        changeModelTask(Path pdbID, WindowController controller) {
            this.model = new PDBFile(pdbID);
            this.controller = controller;
        }

        @Override
        public PDBFile call() {

            return this.model;
        }
    }

    // TODO: How to set this up.
    private static class changeViewTask extends Task<ViewPresenter> {

        ViewPresenter view;
        WindowController controller;

        changeViewTask(PDBFile model, WindowController controller) {

            this.view = new ViewPresenter(controller, model);
            this.controller = controller;
        }

        @Override
        public ViewPresenter call() {
            return this.view;
        }
    }


}
