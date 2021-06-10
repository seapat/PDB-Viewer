import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.PDBFile;
import view.WindowController;

import java.io.File;
import java.nio.file.Path;
import java.util.Scanner;

public class WindowPresenter {

    public static void setup(Stage stage, WindowController controller, PDBFile model) {

        var protein = model.getProtein();

        // only allow valid pdb codes (len = 4)
        var pdbCodeSize = new SimpleBooleanProperty(controller.getEntryField(), "pdbCodeSize", false);
        controller.getEntryField().textProperty().addListener( e -> {
            pdbCodeSize.set(controller.getEntryField().getText().length() == 4);
            controller.getParseButton().setDisable(!pdbCodeSize.get());
        });

        // parse pdb code
        // FIXME: load new model on parse, currently id's not changing
        //  (check assignment 4, you did something similar there)
        controller.getParseButton().setOnAction(e -> {
            var pdbCode = controller.getEntryField().getText();
            clearAll(controller, model);

            // FIXME: How to Handle path? alternative: separate button / put parse into menu (alert window)
//            model = new PDBFile(pdbCode);
            model.setContent(pdbCode);
            writePDB(controller, model);
        });

        // save pdb to file
//        controller.getSaveButton().setOnAction(e -> savePDB(stage, controller, model));
        controller.getSaveMenu().setOnAction(e -> savePDB(stage, controller, model));

        // Menu items
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

    }

    private static void savePDB(Stage stage, WindowController controller, PDBFile model){
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


//        controller.getPdbText().setText("");
        controller.getPdbText().getItems().clear();

        // TODO: clear visualisation
//        controller.getTreePane().getChildren().clear();
//        model.setRoot("");

        // TODO: disable save button if no model loaded
//        controller.getDrawButton().setDisable(true);
//        controller.getChooseEdgeStyle().setDisable(true);
//        controller.getIsScaled().setDisable(true);
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
