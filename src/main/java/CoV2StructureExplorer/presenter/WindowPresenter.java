package CoV2StructureExplorer.presenter;

import CoV2StructureExplorer.model.PDBFile;
import CoV2StructureExplorer.model.PDBWeb;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import CoV2StructureExplorer.view.WindowController;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Scanner;

public class WindowPresenter {

private WindowPresenter() {}

    private static PDBFile model;
    private static ViewPresenter view;
    public static void setup(Stage stage, WindowController controller){

        // setup ChoiceBox
        controller.getColorChoice().getItems().addAll("Atoms", "Structure", "Chains", "Residue");
        controller.getColorChoice().setValue("Atoms");

        // show/hide modelSelection in visualisation tab, SimpleIntegerProperty updated via parse button
        var sizeModelChoiceSize = new SimpleIntegerProperty(controller.getModelChoice().getItems().size(), "sizeModelChoiceSize");
        controller.getModelChoice().visibleProperty().bind(sizeModelChoiceSize.greaterThan(1));
        controller.getModelLabel().visibleProperty().bind(sizeModelChoiceSize.greaterThan(1));
        controller.getModelChoice().managedProperty().bind(sizeModelChoiceSize.greaterThan(1));
        controller.getModelLabel().managedProperty().bind(sizeModelChoiceSize.greaterThan(1));
        controller.getLoadModel().visibleProperty().bind(sizeModelChoiceSize.greaterThan(1));
        controller.getLoadModel().managedProperty().bind(sizeModelChoiceSize.greaterThan(1));

        // service to parse pdb file in background
        var service = new Service<ArrayList<String>>() {
            @Override
            protected Task<ArrayList<String>> createTask() {
                return new pdbTextTask();
            }
        };
        service.setOnScheduled( v ->
            clearAll(controller)
        );
        service.setOnSucceeded(v -> {
            controller.getPdbText().setItems(FXCollections.observableArrayList(service.getValue()));
            controller.getPdbText().scrollTo(0);
        });

        // Only let user parse if pdb code is selected and listview in focus (no unnecessary re-parsing of already parsed code)
        controller.getParseButton().disableProperty().bind(
                // TODO: check if parse button is disabled correctly when service is running
                service.runningProperty().or(
                        controller.getEntryField().textProperty().length().isEqualTo(4).or(
                                controller.getPdbCodeList().getSelectionModel().selectedItemProperty().isNotNull()))
                        .not()
        );
        controller.getParseButton().setOnAction(e -> {
            var selection = controller.getPdbCodeList().getSelectionModel().getSelectedItem();
            var enteredQuery = controller.getEntryField().getText();
            String pdbCode;
            if (enteredQuery.length() == 4 ) {
                pdbCode = enteredQuery;
            } else {
                pdbCode = selection;
            }

            clearAll(controller);
            model = new PDBFile(pdbCode);

            // populate model choice
            controller.getModelChoice().getItems().clear();
            for (var item : model.getProtein()){
                controller.getModelChoice().getItems().add(item.getId());
            }
            try {
                controller.getModelChoice().setValue(controller.getModelChoice().getItems().get(0));
            } catch (Exception idxException){
                //do nothing
            }

            sizeModelChoiceSize.setValue(controller.getModelChoice().getItems().size());

//            writePDB(controller, model);
            service.restart();

            view = new ViewPresenter(controller, model);
//            controller.getModelChoice().valueProperty().addListener(e2 -> {
//                controller.getCenterPane().getChildren().clear();
//                view.setupView(controller, model);
//            });
            controller.getInfoLabel().setText(model.getProtein().size() + " models found.");

            controller.getAbstractContent().setText(fillReportTab());
            ChartPresenter.setupChartTab(model, controller);
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
        controller.getLoadModel().setOnAction(e -> {
            controller.getFigurePane().getChildren().clear();
            // TODO: set up in a way that does not reset camera
            view = new ViewPresenter(controller, model);
        });


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
        controller.getOpenMenu().setOnAction(e -> openPDB(stage,controller,model));
        controller.getClearMenu().setOnAction(e -> clearAll(controller));
        controller.getSaveMenu().setOnAction(e -> savePDB(stage, controller, model));
        controller.getExitMenu().setOnAction(e-> System.exit(0));


    }

    private static String fillReportTab() {
        var content = new StringBuilder(model.getAbstractText());
        content.append("\n\nChains:\n");
        model.getChainSequences().forEach((chain, seq) ->
                content.append(chain.getChainID())
                        .append(": \n")
                        .append(seq)
                        .append("\n")
        );
        return content.toString();
    }

    private static void savePDB(Stage stage, WindowController controller, PDBFile model){
        if (model == null){
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

    private static void openPDB(Stage stage, WindowController controller, PDBFile model) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open PDB File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("pdb files", "*.pdb"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            clearAll(controller);
            model = new PDBFile(Path.of(selectedFile.getPath())); //Files.readString(Path.of(selectedFile.getPath()));
            writePDB(controller, model); //TODO: call service here
        }
    }

    private static void clearAll(WindowController controller) {
        controller.getPdbText().getItems().clear();
        controller.getFigurePane().getChildren().clear();
    }

    private static void writePDB(WindowController controller, PDBFile model){

        var reader = new BufferedReader(new StringReader(model.getContent()));

        long size = reader.lines().count();
        long count = 0;
        ObservableList<String> lines = FXCollections.observableArrayList();

        Scanner scanner = new Scanner(model.getContent());

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            lines.add(line);
            controller.getPdbText().getItems().add(line);
            count++;
        }
        scanner.close();
    }

    // FIXME: either this or WritePDB
    private static class pdbTextTask extends Task<ArrayList<String>> {
        @Override
        public ArrayList<String> call() throws IOException {

            var reader = new BufferedReader(new StringReader(model.getContent()));
            long size = model.getContent().lines().count();
            long count = 0;
            ArrayList<String> lines = new ArrayList<>();

            var currLine = reader.readLine();
            while ( currLine != null && currLine.trim().length() > 0 ) {

                lines.add(currLine);
                count++;
                if (isCancelled()) return null;
                updateProgress(count/size, size);

                currLine = reader.readLine();
            }
            reader.close();

            return lines;
        }
    }



}
