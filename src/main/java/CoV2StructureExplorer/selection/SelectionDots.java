package CoV2StructureExplorer.selection;

// TODO: Should only contain model items, no javaFX

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.Property;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape3D;

import javafx.collections.SetChangeListener;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

// SELECTION
public class SelectionDots<T> {

    public static<T> void setup(Pane selectionPane , SetSelectionModel<T> selectionModel ,
                                 Function<T, List<? extends Shape3D>> itemToDots, Property ...properties ) {
        var shapeDots = new HashMap<Shape3D, Rectangle>( );

        // FIXME: it seems that this listener is never triggered according to printouts, clicking is recognized and the Set of selected items is updated properly
        selectionModel.getSelectedItems().addListener((SetChangeListener<T>) c -> {
            System.out.println("Listener is triggered");
            if (c.wasRemoved()) {
                Platform.runLater ( ( ) -> {
                    for ( var shape : itemToDots.apply (c.getElementRemoved())) {
                        selectionPane.getChildren( ).remove ( shapeDots.get( shape ) ) ;
                        shapeDots.remove(shape);
                    }
                } ) ;
            }
            if (c.wasAdded()) {
                Platform.runLater ( ( ) -> {
                    for ( var shape : itemToDots.apply(c.getElementAdded())) {
                        var dot = createDotWithBinding(selectionPane , shape , properties ) ;
                        selectionPane.getChildren( ).add(dot) ;
                        shapeDots.put ( shape ,dot ) ;
                    }
                } ) ;
            }
        } ) ;
    }


    private static Rectangle createDotWithBinding ( Pane selectionPane, Shape3D shape, final Property ...properties ) {
        final var dot = new Rectangle( );
        InvalidationListener listener = a -> updateDot(dot , selectionPane, shape ) ;

        // attach listener to properties, then call it once (triggers updateDot()), then invalidate it
        for ( var property: properties) {
            property.addListener(new WeakInvalidationListener( listener ) ) ;
        }
        dot.setUserData(listener);
        listener.invalidated( null) ;
        return dot;
    }

    private static void updateDot(Rectangle dot, Pane pane, Shape3D shape) {
        var boundsScreen = shape.localToScreen(shape.getBoundsInLocal());
        var paneBoundsScreen = pane .localToScreen(pane.getBoundsInLocal());
        var xInScene = boundsScreen.getMinX() - paneBoundsScreen.getMinX();
        var yInScene = boundsScreen.getMinY() - paneBoundsScreen.getMinY();
        dot.setX( xInScene ) ;
        dot.setY( yInScene ) ;
        dot.setWidth(boundsScreen.getWidth());
        dot.setHeight(boundsScreen.getHeight());
    }

    /*
    Changes opacity of not selected atoms

    var color = ((PhongMaterial) shape.getMaterial()).getDiffuseColor();
                                color = new Color(color.getRed(), color.getGreen(), color.getBlue(), 0.3);
                                ((PhongMaterial) shape.getMaterial()).setDiffuseColor(color);
                                ((PhongMaterial) shape.getMaterial()).setSpecularColor(color.brighter());

     */

}
