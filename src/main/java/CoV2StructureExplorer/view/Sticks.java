package CoV2StructureExplorer.view;

import CoV2StructureExplorer.model.Structure;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.SubScene;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.DrawMode;
import javafx.util.Pair;

public class Sticks extends Group {

    // TODO: loop over all atoms and create set of all bonds where ever pair of atoms only occurs once or not at all
    //  draw sticks from that

    public Sticks(Structure pdb, ReadOnlyDoubleProperty radiusScale, Integer modelChoice){
        for (var chain: pdb.get(modelChoice -1 )) {
            for (var residue : chain) {
                for (var atom : residue) {

                }
            }
        }

    }



//        for (Pair<Integer, Integer> bond : model.bonds()) {
//            var a = model.getLocation(bond.getKey());
//            var b = model.getLocation(bond.getValue());
//
//            final Point3D YAXIS = new Point3D(0, 100, 0);
//            var midpoint = a.midpoint(b);
//            var direction = b.subtract(a);
//            var perpendicularAxis = YAXIS.crossProduct(direction);
//            var angle = YAXIS.angle(direction);
//            var cylinder = new Cylinder(5, 100);
//            cylinder.setRotationAxis(perpendicularAxis);
//            cylinder.setRotate(angle);
//            cylinder.setTranslateX(midpoint.getX());
//            cylinder.setTranslateY(midpoint.getY());
//            cylinder.setTranslateZ(midpoint.getZ());
//            cylinder.setScaleY(a.distance(b) / cylinder.getHeight());
//
//            cylinder.setCullFace(CullFace.BACK);
//            cylinder.setDrawMode(DrawMode.FILL);
//            PhongMaterial material = new PhongMaterial();
//            material.setDiffuseColor(Color.GREY);
//            cylinder.setMaterial(material);
//
//            figure.getChildren().add(cylinder);
//        }


}
