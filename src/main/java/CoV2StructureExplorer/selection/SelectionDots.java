package CoV2StructureExplorer.selection;

import CoV2StructureExplorer.model.Atom;
import CoV2StructureExplorer.view.Balls;
import CoV2StructureExplorer.view.WindowController;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.DrawMode;

import javafx.collections.SetChangeListener;
import javafx.scene.shape.Sphere;


import java.util.Map;
import java.util.Set;

import java.util.function.Function;

// SELECTION
public class SelectionDots<T> {

    public static<T> void setup(Balls balls , SetSelectionModel<T> selectionModel , //SetSelectionModel<T> selectionModel
                                Function<T, Set<Map.Entry<Atom, Sphere>>> itemToDots, WindowController controller) {

        selectionModel.getSelectedItems().addListener((SetChangeListener<T>) c -> {

            if (c.wasRemoved()) {
                Platform.runLater ( ( ) -> {
                    for ( var shape : itemToDots.apply (c.getElementRemoved())) {
//                        shape.getValue().setDrawMode(DrawMode.FILL);
                        shape.getValue().setMaterial( new PhongMaterial(balls.getColor(controller.getColorChoice().getValue(),
                                shape.getKey())));
                    }
                } ) ;
            }
            if (c.wasAdded()) {
                var items = itemToDots.apply(c.getElementAdded());
                Platform.runLater ( ( ) -> {

                    for ( var shape : items) {
//                        balls.getModelToView().values().forEach();
//                        for (var chain : balls.getModelToView().entrySet()) {
//                            for (var residue : chain.getValue().entrySet()){
//                                for (var atom: residue.getValue().entrySet()){
//                                    if (!items.contains(atom)){
//                                        atom.getValue().setDrawMode(DrawMode.LINE);
//                                    }
//                                }
//                            }
//                        }//);
                        var selectedImage = new PhongMaterial();
                        selectedImage.setDiffuseMap(new Image("/pinkblack2.png", true));
                        shape.getValue().setMaterial(selectedImage);
                    }

                });

            }

        } ) ;
    }

    /*
    Changes opacity of not selected atoms

    var color = ((PhongMaterial) shape.getMaterial()).getDiffuseColor();
                                color = new Color(color.getRed(), color.getGreen(), color.getBlue(), 0.3);
                                ((PhongMaterial) shape.getMaterial()).setDiffuseColor(color);
                                ((PhongMaterial) shape.getMaterial()).setSpecularColor(color.brighter());

     */

}
