package CoV2StructureExplorer.view;

import CoV2StructureExplorer.model.Structure;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;

public class Balls extends Group {

    //sphere.setdrawMode(DrawMode.LINE) to see mesh used for drawing

    public Balls(Structure pdb, ReadOnlyDoubleProperty radiusScale, Integer modelChoice){

        final int opacity = 1;

        for (var chain: pdb.get(modelChoice -1 )){
            for (var residue: chain) {
                for (var atom: residue) {

                    final Sphere sphere = new Sphere(radiusScale.getValue(), 32); //32
                    final Color color;

                    // TODO: Maybe make this a separate method?
                    switch (atom.getSimpleType()) {
                        case 'O' -> {
                            color = Color.RED.deriveColor(1,1,1, opacity);
                        }
                        case 'C' -> {
                            color = Color.DARKGREY.deriveColor(1,1,1, opacity);
                        }
                        case 'N' -> {
                            color = Color.BLUE.deriveColor(1,1,1, opacity);
                        }
                        case 'S' -> {
                            color = Color.YELLOW.deriveColor(1,1,1, opacity);
                        }
                        case 'P' -> {
                            color = Color.ORANGE.deriveColor(1,1,1, opacity);
                        }
                        case 'H' -> {
                            color = Color.WHITE.deriveColor(1,1,1, opacity);
                        }
                        default -> {
                            color = Color.PLUM.deriveColor(1,1,1, opacity);
                        }
                    }

                    sphere.setTranslateX(100 * atom.getPosition().x());
                    sphere.setTranslateY(100 * atom.getPosition().y());
                    sphere.setTranslateZ(100 * atom.getPosition().z());
                    sphere.setMaterial(new PhongMaterial(color));
                    sphere.radiusProperty().bind(radiusScale.multiply(atom.getRadius()));
                    getChildren().add(sphere);
                }
            }

        }

    }

}
