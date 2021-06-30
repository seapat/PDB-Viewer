package CoV2StructureExplorer;

import CoV2StructureExplorer.model.PDBFile;
import CoV2StructureExplorer.model.PDBWeb;
import CoV2StructureExplorer.view.Visualization;
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

    // non-static allows easy reassignment of model
    private static PDBFile model;
    static void setup(Stage stage, WindowController controller){ //, PDBFile model

        // set items to be displayed in choicebox for visualisation style
        controller.getViewChoice().getItems().addAll("Spheres", "Spheres + Ribbon", "Ribbon", "Pseudo-Cartoon");
        controller.getViewChoice().setValue("Spheres");

        // show/hide modelSelection in visualisation tab, SimpleIntegerProperty updated via parse button
        var sizeModelChoiceSize = new SimpleIntegerProperty(controller.getModelChoice().getItems().size(), "sizeModelChoiceSize");
        controller.getModelChoice().visibleProperty().bind(sizeModelChoiceSize.greaterThan(1));
        controller.getModelLabel().visibleProperty().bind(sizeModelChoiceSize.greaterThan(1));
        controller.getModelChoice().managedProperty().bind(sizeModelChoiceSize.greaterThan(1));
        controller.getModelLabel().managedProperty().bind(sizeModelChoiceSize.greaterThan(1));


        // service to parse pdb file in background
        var service = new Service<ArrayList<String>>() {
            @Override
            protected Task<ArrayList<String>> createTask() {
                return new pdbTextTask();
            }
        };
        service.setOnScheduled( v ->
            clearAll(controller, model)
        );
        service.setOnSucceeded(v -> {
            controller.getPdbText().setItems(FXCollections.observableArrayList(service.getValue()));
            controller.getPdbText().scrollTo(0);
        });

        // load new visualisation, clear only visualisation tab
        //TODO: disable if nothing new selected to draw, alternatively: prevent unnecessary redraw (eg: no new speheres if already drawn and we want to add sticks)
        controller.getDrawButton().setOnAction(e-> {
            controller.getCenterPane().getChildren().clear();
            Visualization.setupMoleculeVisualization(controller, model);
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

//            clearAll(controller, model);
            model = new PDBFile(pdbCode);

            // populate model choice
            controller.getModelChoice().getItems().clear();
            for (var item : model.getProtein()){
                controller.getModelChoice().getItems().add(item.getId());
            }
            try {
                controller.getModelChoice().setValue(controller.getModelChoice().getItems().get(0));
            } catch (IndexOutOfBoundsException idxException){

            }
//            writePDB(controller, model);
            sizeModelChoiceSize.setValue(controller.getModelChoice().getItems().size());
            service.restart();
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
        controller.getClearMenu().setOnAction(e -> clearAll(controller,model));
        controller.getSaveMenu().setOnAction(e -> savePDB(stage, controller, model));
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
            clearAll(controller, model);
            model = new PDBFile(Path.of(selectedFile.getPath())); //Files.readString(Path.of(selectedFile.getPath()));
            writePDB(controller, model); //TODO: call service here
        }
    }

    private static void clearAll(WindowController controller, PDBFile model) {
        controller.getPdbText().getItems().clear();
        controller.getCenterPane().getChildren().clear();
    }

    // FIXME: either this or WritePDB
    private static class pdbTextTask extends Task<ArrayList<String>> {
        @Override
        public ArrayList<String> call() throws IOException {

            var reader = new BufferedReader(new StringReader(model.getContent()));
            long size = model.getContent().lines().count();//reader.lines().count(); // model.getContent().lines().split("\r\n|\r|\n").length;
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

//            Scanner scanner = new Scanner(model.getContent());
//            long size =  model.getContent().lines().count(); //.split("\r\n|\r|\n").length;
//            long count = 0;
//            ArrayList<String> lines = new ArrayList<>();
//
//            while (scanner.hasNextLine()) {
//                String line = scanner.nextLine();
//                lines.add(line);
//                count++;
//                if (isCancelled()) return null;
//                 updateProgress(count/size, size);
//            }
//            scanner.close();

            return lines;
        }
    }

    private static void writePDB(WindowController controller, PDBFile model){

        //TODO: better way? this hangs for long pdb files (eg '6ZP5')
        //TODO: monospace currently defined in fxml, move to css once created
            /*
            .list-cell
            {
                -fx-font-family: "monospace";
            }
             */

        var reader = new BufferedReader(new StringReader(model.getContent()));

        long size = reader.lines().count(); // model.getContent().lines().split("\r\n|\r|\n").length;
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

}
