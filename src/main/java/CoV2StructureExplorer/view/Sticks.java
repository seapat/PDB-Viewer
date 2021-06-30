package CoV2StructureExplorer.view;

import CoV2StructureExplorer.model.Atom;
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

import java.util.HashSet;

public class Sticks extends Group {


    // FIXME: in the 5jxv (NMR) there is something weird going on with one specific bond

    public Sticks(Structure pdb, ReadOnlyDoubleProperty radiusScale, Integer modelChoice){

        var bonds = new HashSet<Pair<Atom.Position, Atom.Position>>();

        for (var chain: pdb.get(modelChoice -1 )) {
            for (var residue : chain) {
                for (var atom : residue) {
                    for (var bond : atom.getBonds()) {
                        bonds.add(new Pair<>(atom.getPosition(), bond.getPosition()));
                    }
                }
            }
        }

        for (var bond : bonds){

            var first = new Point3D(bond.getValue().x(),bond.getValue().y(), bond.getValue().z() );
            var second = new Point3D(bond.getKey().x(),bond.getKey().y(), bond.getKey().z() );

            final Point3D YAXIS = new Point3D(0, 100, 0);
            var midpoint = first.midpoint(second);
            var direction = second.subtract(first);
            var perpendicularAxis = YAXIS.crossProduct(direction);
            var angle = YAXIS.angle(direction);
            var cylinder = new Cylinder(0.1, 1, 32);
            cylinder.setRotationAxis(perpendicularAxis);
            cylinder.setRotate(angle);
            cylinder.setTranslateX(midpoint.getX());
            cylinder.setTranslateY(midpoint.getY());
            cylinder.setTranslateZ(midpoint.getZ());
            cylinder.setScaleY(first.distance(second) / cylinder.getHeight());

            cylinder.setCullFace(CullFace.BACK);
            cylinder.setDrawMode(DrawMode.FILL);
            PhongMaterial material = new PhongMaterial();
            material.setDiffuseColor(Color.GREY);
            cylinder.setMaterial(material);

            getChildren().add(cylinder);


        }
        var debug = "";
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
