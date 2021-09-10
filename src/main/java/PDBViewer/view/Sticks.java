package PDBViewer.view;

import PDBViewer.model.Atom;
import PDBViewer.model.Chain;
import PDBViewer.model.Residue;
import PDBViewer.model.Structure;
import PDBViewer.selection.SetSelectionModel;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

public class Sticks extends ArrayList<Cylinder> {

    public Sticks(Structure pdb, HashMap<Chain, Group> chainGroups, WindowController controller,
                  StringProperty selectedResiduesProp, SetSelectionModel<Residue> selectedResidues) {

        ReadOnlyDoubleProperty diameterScale = controller.getDiameterScale().valueProperty();
        Integer modelChoice = controller.getModelChoice().getValue();

        for (var chain : pdb.get(modelChoice - 1)) {
            HashSet<MyPair<Atom, Atom>> bonds = new HashSet<>();

            chainGroups.putIfAbsent(chain, new Group());
            var currSticks = new Group();

            // parse all bonds per chain...
            for (var residue : chain) {
                for (var atom : residue.values()) {
                    for (var bond : atom.getBonds()) {
                        bonds.add(new MyPair<>(atom, bond));
                    }
                }
            }
            // ...then create cylinders from that
            for (MyPair<Atom, Atom> bond : bonds) {

                var first = new Point3D(bond.getRight().getPosition().x(),
                        bond.getRight().getPosition().y(),
                        bond.getRight().getPosition().z());
                var second = new Point3D(bond.getLeft().getPosition().x(),
                        bond.getLeft().getPosition().y(),
                        bond.getLeft().getPosition().z());

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
                material.setDiffuseColor(Color.GREY);
                material.setSpecularPower(0);
                cylinder.setMaterial(material);

                cylinder.radiusProperty().bind(diameterScale.multiply(cylinder.getRadius()));

                cylinder.setId(bond.getLeft().getResidue().getChain().getChainID() +
                        ":" + bond.getLeft().getResidue().getId() +
                        ":" + bond.getRight().getResidue().getId());

                cylinder.setOnMouseClicked(e -> {

                    if (!e.isShiftDown()) {
                        selectedResidues.clearSelection();
                    }
                    if (bond.getRight().getResidue().equals(bond.getLeft().getResidue())) {
                        selectedResidues.select(bond.getRight().getResidue());
                        selectedResiduesProp.setValue(String.join("",
                                selectedResidues.getSelectedItems().stream()
                                        .map(Residue::getThreeLetter)
                                        .map(code -> code += " ")
                                        .sorted(Comparator.comparingInt(String::length))
                                        .toList()));
                    }
                });

                this.add(cylinder);

                currSticks.getChildren().add(cylinder);
            }
            currSticks.setVisible(false);
            currSticks.visibleProperty().bind(controller.getBondsChecked().selectedProperty());
            chainGroups.get(chain).getChildren().add(currSticks);
        }
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
            if (!(o instanceof MyPair pairObj)) return false;
            return this.left.equals(pairObj.getLeft()) && this.right.equals(pairObj.getRight())
                    ||
                    this.right.equals(pairObj.getLeft()) && this.left.equals(pairObj.getRight());
        }

    }

}
