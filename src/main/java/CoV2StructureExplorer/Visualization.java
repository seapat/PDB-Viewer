package CoV2StructureExplorer;

import CoV2StructureExplorer.model.PDBFile;
import CoV2StructureExplorer.view.Balls;
import CoV2StructureExplorer.view.Mesh;
import CoV2StructureExplorer.view.Sticks;
import CoV2StructureExplorer.view.WindowController;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class Visualization {

    // TODO make this a task so it can run in the background?

    private static SubScene subScene;
    private static WindowController controller;
    private static PDBFile model;
    private static Group figure;
    private static double maxX;
    private static double maxY;
    private static double maxZ;
    private static double minX;
    private static double minY;
    private static double minZ;
    private static Pane pane;
    private static Camera camera;
    private static double x ;
    private static double y ;

        private Visualization(){}

        // TODO: best would be to rewrite this as fully static with no class variable, rather return stuff to main presenter
        //  also save min and max vars in PDBFile and access them via getters

        public static void setupVisualization(WindowController controller, PDBFile model) {
            Visualization.controller = controller;
            Visualization.model = model;
            Visualization.figure = loadView();
            Visualization.subScene = new SubScene(figure, 600, 600, true, SceneAntialiasing.DISABLED);
            subScene.widthProperty().bind(controller.getCenterPane().widthProperty());
            subScene.heightProperty().bind(controller.getCenterPane().heightProperty());
            var t1 = System.nanoTime();

            // FIXME: MAYBE write this as a sinlge loop over all atoms? might be faster overall
            Visualization.maxX = figure.getChildren().stream().map(x -> x.translateXProperty().getValue()).max(Double::compare).orElse(0d);
            Visualization.maxY = figure.getChildren().stream().map(y -> y.translateYProperty().getValue()).max(Double::compare).orElse(0d);
            Visualization.maxZ = figure.getChildren().stream().map(z -> z.translateZProperty().getValue()).max(Double::compare).orElse(0d);
            Visualization.minX = figure.getChildren().stream().map(x -> x.translateXProperty().getValue()).min(Double::compare).orElse(0d);
            Visualization.minY = figure.getChildren().stream().map(y -> y.translateYProperty().getValue()).min(Double::compare).orElse(0d);
            Visualization.minZ = figure.getChildren().stream().map(z -> z.translateZProperty().getValue()).min(Double::compare).orElse(0d);
            Visualization.pane = controller.getCenterPane();
            Visualization.camera = setupCamera();
            subScene.setCamera(camera);
            setupRotation();
            pane.getChildren().add(subScene);
        }

    // TODO: Don't reset camera on drawing, important when we want to add sticks etc.

    // TODO: instead of center camera and rotation, translate atom by x y z


    public static Group loadView(){
        final Group figure;
        String choice = controller.getViewChoice().getValue();
        switch (choice) {
            case "Ribbon" -> figure = new Sticks(model.getProtein(),
                    controller.getDiameterScale().valueProperty(),
                    controller.getModelChoice().getValue());
            case "Spheres + Ribbon" -> {
                // FIXME: maybe add these separately to the subscene? void method no return, would need to deal with camera
                var balls = new Balls(model.getProtein(),
                        controller.getRadiusScale().valueProperty(),
                        controller.getModelChoice().getValue(),
                        controller.getColorChoice().getValue());
                controller.getColorChoice().valueProperty().addListener( e ->
                        balls.changeColor( controller.getColorChoice().getValue())
                );
                var sticks = new Sticks(model.getProtein(),
                        controller.getDiameterScale().valueProperty(),
                        controller.getModelChoice().getValue());
                balls.getChildren().addAll(sticks.getChildren()); //FIXME: this seems to take a lot of time
                figure = balls;
            }
            case "Pseudo-Cartoon" -> figure = new Mesh();
            default -> {
                var balls = new Balls(model.getProtein(),
                        controller.getRadiusScale().valueProperty(),
                        controller.getModelChoice().getValue(),
                        controller.getColorChoice().getValue());
                controller.getColorChoice().valueProperty().addListener( e ->
                        balls.changeColor( controller.getColorChoice().getValue())
                );
                figure = balls;
            }

        }

        return figure;
    }

    public static Camera setupCamera(){
        var camera = new PerspectiveCamera(true);


        var maxFromCenter = Math.abs(Collections.max(Arrays.asList(minX, minZ, minY, maxX, maxY, maxZ), Comparator.comparingDouble(Math::abs)));
        System.out.println("maxFromCenter " + maxFromCenter);
        camera.setFarClip(10 * maxFromCenter);
        camera.setNearClip(0.1);
        camera.setTranslateX((maxX + minX) / 2);
        camera.setTranslateY(( maxY + minY) / 2);
        camera.setTranslateZ( -2000 * Math.log(maxFromCenter));

        System.out.println("maxX " + maxX);
        System.out.println("minX " + minX);
        System.out.println("maxY " + maxY);
        System.out.println("minY " + minY);
        System.out.println("maxZ " + maxZ);
        System.out.println("minZ " + minZ);

        controller.getCenterPane().setOnScroll((ScrollEvent e) -> {
            var curr = camera.getTranslateZ();

            // TODO: maybe remove the e.delta so that is always scrolls the same, no matter how aggressive you tread your mouse
            camera.setTranslateZ(curr +  (Math.abs(Math.max(minZ, maxZ)*0.005) + 1)); // (e.getDeltaY() * )
        });

        return camera;
    }

    public static void setupRotation(){

        Property<Transform> figureTransformProperty = new SimpleObjectProperty<>(new Rotate());
        figureTransformProperty.addListener((v, o, n) -> figure.getTransforms().setAll(n));

        pane.setOnMousePressed(e -> {
            x = e.getSceneX();
            y = e.getSceneY();
        });

        pane.setOnMouseDragged ( e -> {
            pane.setCursor(Cursor.CLOSED_HAND);
            var delta = new Point2D((e.getSceneX() - x) , (e.getSceneY() - y) ) ;
            var dragOrthogonalAxis = new Point3D( delta.getY(), -delta.getX() , 0) ;
            var rotate = new Rotate(0.5 * delta.magnitude(), dragOrthogonalAxis);

            rotate.setPivotX((maxX + minX) / 2);
            rotate.setPivotY((maxY + minY) / 2);
            rotate.setPivotZ((maxZ + minZ) / 2);

            figureTransformProperty.setValue(rotate.createConcatenation(figureTransformProperty.getValue()));
            x = e.getSceneX();
            y = e.getSceneY();
            e.consume();
        });

        pane.setOnMouseReleased(e ->pane.setCursor(Cursor.DEFAULT));
    }

    }
