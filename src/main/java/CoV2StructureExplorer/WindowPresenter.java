package CoV2StructureExplorer;

import CoV2StructureExplorer.model.PDBFile;
import CoV2StructureExplorer.model.PDBUrl;
import CoV2StructureExplorer.view.BallsOnly;
import CoV2StructureExplorer.view.MouseInteraction;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Point3D;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.input.ScrollEvent;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
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
            setupMoleculeVisualization(controller);
        });


        // Button Listeners
        // Only let user parse if pdb code is selected and listview in focus (no unnecessary re-parsing of already parsed code)
        controller.getParseButton().disableProperty().bind(
                // TODO: check if parse button is disabled correctly when service is running
                service.runningProperty().or(
                        controller.getEntryField().textProperty().length().isEqualTo(4).or(
                                controller.getPdbCodeList().focusedProperty()))
                        .not()
        );
        controller.getParseButton().setOnAction(e -> {
            var selection = controller.getPdbCodeList().getSelectionModel().getSelectedItem();
            var enteredQuery = controller.getEntryField().getText();
            String pdbCode;
            if (enteredQuery.length() != 4 && controller.getPdbCodeList().isFocused()) {
                pdbCode = selection;
            } else {
                pdbCode = enteredQuery;
            }

//            clearAll(controller, model);
            model = new PDBFile(pdbCode);
//            writePDB(controller, model);
            service.restart();

        });

        // get default value for List of pdb codes
        controller.getPdbCodeList().setItems(
                FXCollections.observableArrayList(
                        PDBUrl.getPDBEntries(controller.getEntryField().getText())));
        controller.getEntryField().textProperty().addListener(e ->
                controller.getPdbCodeList().setItems(
                        FXCollections.observableArrayList(
                                PDBUrl.getPDBEntries(controller.getEntryField().getText())))
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


    private static void setupMoleculeVisualization(WindowController controller) {

        // TODO: adjust range of radiusScale slider
        var figure = new BallsOnly(model.getProtein(), controller.getRadiusScale().valueProperty());
        var maxX = figure.getChildren().stream().map(x -> x.translateXProperty().getValue()).max(Double::compare).orElse(0d);
        var maxY = figure.getChildren().stream().map(y -> y.translateYProperty().getValue()).max(Double::compare).orElse(0d);
        var minX = figure.getChildren().stream().map(x -> x.translateXProperty().getValue()).min(Double::compare).orElse(0d);
        var minY = figure.getChildren().stream().map(y -> y.translateYProperty().getValue()).min(Double::compare).orElse(0d);
        var minZ = figure.getChildren().stream().map(z -> z.translateZProperty().getValue()).min(Double::compare).orElse(0d);
        var maxZ = figure.getChildren().stream().map(z -> z.translateZProperty().getValue()).max(Double::compare).orElse(0d);

        var avgX = figure.getChildren().stream().map(x -> x.translateXProperty().getValue()).mapToDouble(Double::doubleValue).average().orElse(0d);
        var avgY = figure.getChildren().stream().map(y -> y.translateYProperty().getValue()).mapToDouble(Double::doubleValue).average().orElse(0d);
        var avgZ = figure.getChildren().stream().map(z -> z.translateZProperty().getValue()).mapToDouble(Double::doubleValue).average().orElse(0d);

        // camera
        var camera = new PerspectiveCamera(true);
        camera.setFarClip(100000);
        camera.setNearClip(0.1);
        camera.setTranslateX((maxX + minX) / 2);
        camera.setTranslateY((maxY + minY) / 2);
        camera.setTranslateZ( Math.abs(minZ) * -2 - 1000);

//        var rotateDummy = new Sphere(300 );
//        rotateDummy.setVisible(false);
//        rotateDummy.setTranslateX((maxX + minX) / 2 );
//        rotateDummy.setTranslateY((maxY + minY) / 2 );
//        rotateDummy.setTranslateZ((maxZ + minZ) / 2);
//        figure.getChildren().add(0, rotateDummy);



        controller.getCenterPane().setOnScroll((ScrollEvent event) -> {
            var curr = camera.getTranslateZ();
            camera.setTranslateZ(curr + (event.getDeltaY() * 2)); //FIXME: try to scale zoomspeed to figure size
        });


        Property<Transform> figureTransformProperty = new SimpleObjectProperty<>(new Rotate());
        figureTransformProperty.addListener((v, o, n) -> figure.getTransforms().setAll(n));
        MouseInteraction.installRotate(controller.getCenterPane(), camera,figure , figureTransformProperty);

        // separate method in order to keep camera persistent
//        drawBalls(figure, subScene, controller);
//        drawSticks(figure, subScene, controller, model);

        var subScene = new SubScene(figure, 600, 600, true, SceneAntialiasing.BALANCED);
        subScene.widthProperty().bind(controller.getCenterPane().widthProperty());
        subScene.heightProperty().bind(controller.getCenterPane().heightProperty());
        subScene.setCamera(camera);
        controller.getCenterPane().getChildren().add(subScene);

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

        // TODO: clear visualisation
        controller.getCenterPane().getChildren().clear();
    }

    // FIXME: feither this or WritePDB
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
            //if (isCancelled()) return null;

            // updateProgress(count/size, size)
        }
        scanner.close();
    }

}
