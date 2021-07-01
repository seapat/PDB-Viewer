package CoV2StructureExplorer.view;

import CoV2StructureExplorer.model.Structure;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;

import java.util.AbstractMap;
import java.util.Map;

public class Balls extends Group {

    //sphere.setdrawMode(DrawMode.LINE) to see mesh used for drawing

    private final static Map<Character, Color> colorMap = Map.ofEntries(
            // single bonded radii found on wikipedia "covalent radius"
            new AbstractMap.SimpleEntry<>('O', Color.RED),
            new AbstractMap.SimpleEntry<>('C', Color.DIMGRAY),
            new AbstractMap.SimpleEntry<>('N', Color.BLUE),
            new AbstractMap.SimpleEntry<>('S', Color.YELLOW),
            new AbstractMap.SimpleEntry<>('P', Color.ORANGE),
            new AbstractMap.SimpleEntry<>('H', Color.WHITE)
    );

    public Balls(Structure pdb, ReadOnlyDoubleProperty radiusScale, Integer modelChoice){

//        final int opacity = 1;

        for (var chain: pdb.get(modelChoice -1 )){
            for (var residue: chain) {
                for (var atom: residue) {

                    final Sphere sphere = new Sphere(100, 32);
                    final Color color = colorMap.getOrDefault(atom.getSimpleType(), Color.PLUM);

                    sphere.setTranslateX(100 * atom.getPosition().x());
                    sphere.setTranslateY(100 * atom.getPosition().y());
                    sphere.setTranslateZ(100 * atom.getPosition().z());

                    PhongMaterial material = new PhongMaterial();
                    material.setSpecularColor(Color.WHITE);
                    material.setSpecularPower(20);
                    material.setDiffuseColor(color);
                    sphere.setMaterial(material);

                    sphere.radiusProperty().bind(radiusScale.multiply(atom.getRadius()));
                    getChildren().add(sphere);
                }
            }
        }

//        AmbientLight ambient = new AmbientLight();
//        getChildren().add(ambient);
        System.out.println("balls: " + this.getChildren().size());

    }

}
