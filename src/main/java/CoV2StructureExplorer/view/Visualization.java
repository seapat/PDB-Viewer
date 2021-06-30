package CoV2StructureExplorer.view;

import CoV2StructureExplorer.model.PDBFile;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;

public class Visualization {

        private Visualization() {}

    // TODO: Don't reset camera on drawing, important when we want to add sticks etc.

    // FIXME: same initial distance when drawing sticks or balls
    // FIXME: somethings wrong with mouse-scrolling (zoom) behaves differently depending on view loaded, too sensitive for sticks

    public static void setupMoleculeVisualization(WindowController controller, PDBFile model) {

        final Group figure;
        String choice = controller.getViewChoice().getValue();
        switch (choice) {
//            case "Spheres" ->  figure = new Balls(model.getProtein(), controller.getRadiusScale().valueProperty());
            case "Ribbon" -> figure = new Sticks(model.getProtein(),
                    controller.getDiameterScale().valueProperty(), //TODO: change to slide for sticks
                    controller.getModelChoice().getValue());
            case "Spheres + Ribbon" -> figure = new Sticks(model.getProtein(),
                    controller.getRadiusScale().valueProperty(),
                    controller.getModelChoice().getValue());
            case "Pseudo-Cartoon" -> figure =new Mesh();
            default -> figure = new Balls(model.getProtein(),
                    controller.getRadiusScale().valueProperty(),
                    controller.getModelChoice().getValue());
        }


        // camera
        var camera = new PerspectiveCamera(true);

        controller.getCenterPane().setOnScroll((ScrollEvent event) -> {
            var curr = camera.getTranslateZ();
            camera.setTranslateZ(curr + (event.getDeltaY() * 2)); //FIXME: try to scale zoom speed to figure size
        });


        Property<Transform> figureTransformProperty = new SimpleObjectProperty<>(new Rotate());
        figureTransformProperty.addListener((v, o, n) -> figure.getTransforms().setAll(n));
        Visualization.installRotate(controller.getCenterPane(), camera,figure , figureTransformProperty);

        // separate method in order to keep camera persistent
//        drawBalls(figure, subScene, controller);
//        drawSticks(figure, subScene, controller, model);

        var subScene = new SubScene(figure, 600, 600, true, SceneAntialiasing.BALANCED);
        subScene.widthProperty().bind(controller.getCenterPane().widthProperty());
        subScene.heightProperty().bind(controller.getCenterPane().heightProperty());
        subScene.setCamera(camera);
        controller.getCenterPane().getChildren().add(subScene);

    }

        private static double x ;
        private static double y ;
        private static void installRotate (Pane pane , Camera camera, Group figure, Property<Transform> figureTransformProperty ) {

            // TODO: maybe extract parts here ? figure.getChildren().stream() perhaps?
            var maxX = figure.getChildren().stream().map(x -> x.translateXProperty().getValue()).max(Double::compare).orElse(0d);
            var maxY = figure.getChildren().stream().map(y -> y.translateYProperty().getValue()).max(Double::compare).orElse(0d);
            var maxZ = figure.getChildren().stream().map(z -> z.translateZProperty().getValue()).max(Double::compare).orElse(0d);
            var minX = figure.getChildren().stream().map(x -> x.translateXProperty().getValue()).min(Double::compare).orElse(0d);
            var minY = figure.getChildren().stream().map(y -> y.translateYProperty().getValue()).min(Double::compare).orElse(0d);
            var minZ = figure.getChildren().stream().map(z -> z.translateZProperty().getValue()).min(Double::compare).orElse(0d);

            var avgX = figure.getChildren().stream().map(x -> x.translateXProperty().getValue()).mapToDouble(Double::doubleValue).average().orElse(0d);
            var avgY = figure.getChildren().stream().map(y -> y.translateYProperty().getValue()).mapToDouble(Double::doubleValue).average().orElse(0d);
            var avgZ = figure.getChildren().stream().map(z -> z.translateZProperty().getValue()).mapToDouble(Double::doubleValue).average().orElse(0d);

            camera.setFarClip(100000);
            camera.setNearClip(0.1);
            camera.setTranslateX((maxX + minX) / 2);
            camera.setTranslateY((maxY + minY) / 2);
            camera.setTranslateZ( Math.abs(minZ) * -2 - 1000);

//            camera.setTranslateX(avgX);
//            camera.setTranslateY(avgY);


            pane.setOnMousePressed(e -> {
                    x = e.getSceneX();
                    y = e.getSceneY();
            });

            pane.setOnMouseDragged ( e -> {
                pane.setCursor(Cursor.CLOSED_HAND);
                var delta = new Point2D((float)(e.getSceneX() - x) , (float)(e.getSceneY() - y) ) ;
                var dragOrthogonalAxis = new Point3D( delta.getY(), -delta.getX() , 0) ;
                var rotate = new Rotate(0.5 * delta.magnitude(), dragOrthogonalAxis);


//                    rotate.setPivotX(camera.getTranslateX());
//                    rotate.setPivotY(camera.getTranslateY());
                    // cant use camera's z position


                rotate.setPivotX((maxX + minX) / 2);
                rotate.setPivotY((maxY + minY) / 2);
                rotate.setPivotZ((maxZ + minZ) / 2);
//                rotate.setPivotX(avgX);
//                rotate.setPivotY(avgY);
//                rotate.setPivotZ(avgZ);

                figureTransformProperty.setValue(rotate.createConcatenation(figureTransformProperty.getValue()));
                x = e.getSceneX();
                y = e.getSceneY();
                e.consume();
            });
            pane.setOnMouseReleased(e ->pane.setCursor(Cursor.DEFAULT));

        }



    }
