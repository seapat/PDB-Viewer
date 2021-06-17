package CoV2StructureExplorer.view;

import javafx.beans.property.Property;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.Camera;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.SubScene;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;

public class MouseInteraction {

        private static double x ;
        private static double y ;
        public static void installRotate (Pane pane , Camera camera, Group figure, Property<Transform> figureTransformProperty ) {
            pane.setOnMousePressed(e -> {
                    x = e.getSceneX();
                    y = e.getSceneY();
            });

            pane.setOnMouseDragged ( e -> {
                pane.setCursor(Cursor.CLOSED_HAND);
                var delta = new Point2D((float)(e.getSceneX() - x) , (float)(e.getSceneY() - y) ) ;
                var dragOrthogonalAxis = new Point3D( delta.getY(), -delta.getX() , 0) ;
                var rotate = new Rotate(0.5 * delta.magnitude(), dragOrthogonalAxis);

                var maxX = figure.getChildren().stream().map(x -> x.translateXProperty().getValue()).max(Double::compare).orElse(0d);
                var maxY = figure.getChildren().stream().map(y -> y.translateYProperty().getValue()).max(Double::compare).orElse(0d);
                var minX = figure.getChildren().stream().map(x -> x.translateXProperty().getValue()).min(Double::compare).orElse(0d);
                var minY = figure.getChildren().stream().map(y -> y.translateYProperty().getValue()).min(Double::compare).orElse(0d);
                var minZ = figure.getChildren().stream().map(z -> z.translateZProperty().getValue()).min(Double::compare).orElse(0d);
                var maxZ = figure.getChildren().stream().map(z -> z.translateZProperty().getValue()).max(Double::compare).orElse(0d);


                  // FIXME: somehow this does not work
//                    rotate.setPivotX(camera.getTranslateX());
//                    rotate.setPivotY(camera.getTranslateY());
//                    rotate.setPivotZ(camera.getTranslateZ());

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
