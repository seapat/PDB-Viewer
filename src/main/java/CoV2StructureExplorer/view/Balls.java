package CoV2StructureExplorer.view;

import CoV2StructureExplorer.model.Structure;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;

public class Balls extends Group {

    //sphere.setdrawMode(DrawMode.LINE) to see mesh used for drawing
    // use constructor of sphere to reduce mesh-lines -> better performance sphere(x, y) x = radius, y = subdivisions

    public Balls(Structure pdb, ReadOnlyDoubleProperty radiusScale, Integer modelChoice){

        final int opacity = 1;
        double max = 0;
        // TODO: bonds belong to the model!

        for (var chain: pdb.get(modelChoice -1 )){
            for (var residue: chain) {
                for (var atom: residue) {

//                    var x = atom.getPosition().x();
//                    if (x > max) {
//                        max = x;
//                    }

                    final Sphere sphere = new Sphere(1, 32); //32
                    final Color color;
                    final double radius;

                    switch (atom.getSimpleType()) {
                        case 'O' -> {
                            color = Color.RED.deriveColor(1,1,1, opacity);
                            radius = 160;
                        }
                        case 'C' -> {
                            color = Color.DARKGREY.deriveColor(1,1,1, opacity);
                            radius = 120;
                        }
                        case 'N' -> {
                            color = Color.BLUE.deriveColor(1,1,1, opacity);
                            radius = 140;
                        }
                        case 'S' -> {
                            color = Color.YELLOW.deriveColor(1,1,1, opacity);
                            radius = 320;
                        }
                        case 'P' -> {
                            color = Color.ORANGE.deriveColor(1,1,1, opacity);
                            radius = 310;
                        }
                        case 'H' -> {
                            color = Color.WHITE.deriveColor(1,1,1, opacity);
                            radius = 50;
                        }
                        default -> {
                            color = Color.PLUM.deriveColor(1,1,1, opacity);
                            radius = 100;
                        }
                    }

                    sphere.setTranslateX(15 * atom.getPosition().x());
                    sphere.setTranslateY(15 * atom.getPosition().y());
                    sphere.setTranslateZ(15 * atom.getPosition().z());
                    sphere.setMaterial(new PhongMaterial(color));
                    sphere.radiusProperty().bind(radiusScale.multiply(radius));
                    getChildren().add(sphere);
                }
            }

        }


//        System.out.println(max);
    }

}
