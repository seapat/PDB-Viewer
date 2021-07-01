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

import java.util.HashSet;

public class Sticks extends Group {

    public Sticks(Structure pdb, ReadOnlyDoubleProperty diameterScale, Integer modelChoice){

        var bonds = new HashSet<MyPair<Atom.Position, Atom.Position>>();
        for (var chain: pdb.get(modelChoice -1 )) {
            for (var residue : chain) {
                for (var atom : residue) {
                    for (var bond : atom.getBonds()) {
                        bonds.add(new MyPair<>(atom.getPosition(), bond.getPosition()));
                    }
                }
            }
        }
        System.out.println("bonds: " + bonds.size());

        for (var bond : bonds){

            var first = new Point3D(bond.getRight().x() * 100,bond.getRight().y() * 100, bond.getRight().z() * 100);
            var second = new Point3D(bond.getLeft().x() * 100,bond.getLeft().y() * 100, bond.getLeft().z() * 100);

            final Point3D yAxis = new Point3D(0, 100, 0);
            var midpoint = first.midpoint(second);
            var direction = first.subtract(second);
            var perpendicularAxis = yAxis.crossProduct(direction);
            var angle = yAxis.angle(direction);
            var cylinder = new Cylinder(1, 100, 32);
            cylinder.setRotationAxis(perpendicularAxis);
            cylinder.setRotate(angle);
            cylinder.setTranslateX(midpoint.getX());
            cylinder.setTranslateY(midpoint.getY());
            cylinder.setTranslateZ(midpoint.getZ());
            cylinder.setScaleY(first.distance(second) / cylinder.getHeight());

            PhongMaterial material = new PhongMaterial();
            material.setDiffuseColor(Color.BLACK);
            material.setSpecularPower(0);
            cylinder.setMaterial(material);

            cylinder.radiusProperty().bind(diameterScale.multiply(cylinder.getRadius()));

            getChildren().add(cylinder);

        }
        System.out.println("sticks: " + this.getChildren().size());
    }

    private record MyPair<L, R>(L left, R right) {

        private MyPair {
            assert left != null;
            assert right != null;
        }

        public L getLeft() {
            return left;
        }

        public R getRight() {
            return right;
        }

        @Override
        public int hashCode() {
            return left.hashCode() ^ right.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof MyPair pairo)) return false;
            return this.left.equals(pairo.getLeft()) && this.right.equals(pairo.getRight())
                    ||
                    this.right.equals(pairo.getLeft()) && this.left.equals(pairo.getRight());
        }

    }


}
