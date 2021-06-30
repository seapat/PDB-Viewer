package CoV2StructureExplorer.view;

import CoV2StructureExplorer.model.Atom;
import CoV2StructureExplorer.model.Structure;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.DrawMode;
import javafx.util.Pair;

import java.util.HashSet;

public class Sticks extends Group {

    public Sticks(Structure pdb, ReadOnlyDoubleProperty diameterScale, Integer modelChoice){

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

            var first = new Point3D(bond.getValue().x() * 100,bond.getValue().y() * 100, bond.getValue().z() * 100);
            var second = new Point3D(bond.getKey().x() * 100,bond.getKey().y() * 100, bond.getKey().z() * 100);

            final Point3D YAXIS = new Point3D(0, 100, 0);
            var midpoint = first.midpoint(second);
            var direction = second.subtract(first);
            var perpendicularAxis = YAXIS.crossProduct(direction);
            var angle = YAXIS.angle(direction);
            var cylinder = new Cylinder(diameterScale.getValue(), 1, 32);
            cylinder.setRotationAxis(perpendicularAxis);
            cylinder.setRotate(angle);
            cylinder.setTranslateX(midpoint.getX());
            cylinder.setTranslateY(midpoint.getY());
            cylinder.setTranslateZ(midpoint.getZ());
            cylinder.setScaleY(first.distance(second) / cylinder.getHeight());

            // TODO: get rid of shades when coloring
            cylinder.setCullFace(CullFace.BACK);
            cylinder.setDrawMode(DrawMode.FILL);
            PhongMaterial material = new PhongMaterial();
            material.setDiffuseColor(Color.GREY);
            cylinder.setMaterial(material);

            cylinder.radiusProperty().bind(diameterScale.multiply(cylinder.getRadius()));

            getChildren().add(cylinder);


        }
        var debug = "";
    }



}
