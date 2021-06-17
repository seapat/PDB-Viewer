package CoV2StructureExplorer.view;

import javafx.beans.property.Property;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.Camera;
import javafx.scene.Cursor;
import javafx.scene.SubScene;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;

public class MouseInteraction {

        private static double x ;
        private static double y ;
        public static void installRotate (Pane pane , Camera camera, Property<Transform> figureTransformProperty ) {
            pane.setOnMousePressed(e -> {
                    x = e.getSceneX();
                    y = e.getSceneY();
            });

            pane.setOnMouseDragged ( e -> {
                    pane.setCursor(Cursor.CLOSED_HAND);
                    var delta = new Point2D((float)(e.getSceneX() - x) , (float)(e.getSceneY() - y) ) ;
                    var dragOrthogonalAxis = new Point3D( delta.getY(), -delta.getX() , 0) ;
                    var rotate = new Rotate(0.5 * delta.magnitude(), dragOrthogonalAxis);
                    figureTransformProperty.setValue(rotate.createConcatenation(figureTransformProperty.getValue()));
                    x = e.getSceneX();
                    y = e.getSceneY();
                    e.consume();
            });
            pane.setOnMouseReleased(e ->pane.setCursor(Cursor.DEFAULT));

        }



    }
