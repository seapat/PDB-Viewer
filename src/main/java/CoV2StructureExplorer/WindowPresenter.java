package CoV2StructureExplorer;

import CoV2StructureExplorer.model.PDBFile;
import CoV2StructureExplorer.model.PDBUrl;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import CoV2StructureExplorer.view.WindowController;

import java.io.File;
import java.nio.file.Path;
import java.util.Scanner;

public class WindowPresenter {


    /*
    TODO: make stage and controller instance variables and update all related methods
     Could alternatively create static version with variables for stage, controller, model and
     methods that are called on variable creation (see PDBURL)
     */


    // non-static allows easy reassignment of model
    private PDBFile model;
    WindowPresenter(Stage stage, WindowController controller){ //, PDBFile model


        // Button Listeners
        // Only let user parse if pdb code is selected and listview in focus (no unnecessary re-parsing of already parsed code)
        controller.getParseButton().disableProperty().bind(
                Bindings.or(
                        //TODO: disable button if already parsing (concurrency)
                        controller.getEntryField().textProperty().length().isEqualTo(4),
                        controller.getPdbCodeList().focusedProperty())
                        .not()
        );
        controller.getParseButton().setOnAction(e -> {
            var selection = controller.getPdbCodeList().getSelectionModel().getSelectedItem();
            var enteredQuery = controller.getEntryField().getText();
            String pdbCode;
            if (enteredQuery.length() != 4) {
                pdbCode = selection;
            } else {
                pdbCode = enteredQuery;
            }

            clearAll(controller, model);
            this.model = new PDBFile(pdbCode);
            writePDB(controller, model);
            controller.getPdbText().scrollTo(0);
        });

        // get default value for List of pdb codes
        controller.getPdbCodeList().setItems(PDBUrl.getPDBEntries(controller.getEntryField().getText()));
        controller.getEntryField().textProperty().addListener(e ->
                controller.getPdbCodeList().setItems(
                        PDBUrl.getPDBEntries(controller.getEntryField().getText()))
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
            writePDB(controller, model);
        }
    }

    private static void clearAll(WindowController controller, PDBFile model) {

        controller.getPdbText().getItems().clear();

        // TODO: clear visualisation
//        controller.getTreePane().getChildren().clear();
//        CoV2StructureExplore.CoV2StructureExplorer.model.setRoot("");
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
        ObservableList<String> lines = FXCollections.observableArrayList();
        Scanner scanner = new Scanner(model.getContent());
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            lines.add(line);
            controller.getPdbText().getItems().add(line);
        }
        scanner.close();
    }

}
