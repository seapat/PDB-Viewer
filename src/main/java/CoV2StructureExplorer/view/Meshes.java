package CoV2StructureExplorer.view;

import CoV2StructureExplorer.model.Chain;
import CoV2StructureExplorer.model.Residue;
import CoV2StructureExplorer.model.Structure;
import CoV2StructureExplorer.model.StructureType;
import CoV2StructureExplorer.selection.SetSelectionModel;
import javafx.beans.property.StringProperty;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.shape.MeshView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class Meshes extends ArrayList<MeshView> {

    public Meshes(Structure pdb, HashMap<Chain, Group> chainGroups, WindowController controller,
                  StringProperty selectedResiduesProp, SetSelectionModel<Residue> selectedResidues) {

        Integer modelChoice = controller.getModelChoice().getValue();

        for (var chain : pdb.get(modelChoice - 1)) {
            Residue prevRes = null;

            chainGroups.putIfAbsent(chain, new Group());
            var currMeshes = new Group();

            for (var residue : chain) {

                // skip first iteration, then make sure both residues are not DNA/RNA and neither is glycine
                if (prevRes != null &&
                        prevRes.getStructureType() != StructureType.NUCLEOTIDE &&
                        residue.getStructureType() != StructureType.NUCLEOTIDE &&
                        residue.containsKey("CB") && prevRes.containsKey("CB")) {

                    var ca1Atom = prevRes.get("CA").getPosition();
                    var ca1Point = new Point3D(ca1Atom.x(), ca1Atom.y(), ca1Atom.z());
                    var cb1Atom = prevRes.get("CB").getPosition();
                    var cb1Point = new Point3D(cb1Atom.x(), cb1Atom.y(), cb1Atom.z());
                    var op1 = Shape3DUtils.computeOpposite(ca1Point, cb1Point);

                    var ca2Atom = residue.get("CA").getPosition();
                    var ca2Point = new Point3D(ca2Atom.x(), ca2Atom.y(), ca2Atom.z());
                    var cb2Atom = residue.get("CB").getPosition();
                    var cb2Point = new Point3D(cb2Atom.x(), cb2Atom.y(), cb2Atom.z());
                    var op2 = Shape3DUtils.computeOpposite(ca2Point, cb2Point);

                    var mesh = Shape3DUtils.createRibbonPart(ca1Point, cb1Point, op1,
                            ca2Point, cb2Point, op2);

                    mesh.setOnMouseClicked(e -> {

                        if (!e.isShiftDown()) {
                            selectedResidues.clearSelection();
                        }
                        selectedResidues.select(residue);
                        selectedResiduesProp.setValue(String.join("",
                                selectedResidues.getSelectedItems().stream()
                                        .map(Residue::getThreeLetter)
                                        .map(code -> code += " ")
                                        .sorted(Comparator.comparingInt(String::length).thenComparing(Comparator.naturalOrder()))
                                        .toList()));
                    });

                    mesh.setId(residue.getChain().getChainID() + ":" + residue.getId() + ": dummy");
                    this.add(mesh);
                    currMeshes.getChildren().add(mesh);
                }
                prevRes = residue;
            }
            currMeshes.setVisible(false);
            currMeshes.visibleProperty().bind(controller.getRibbonChecked().selectedProperty());
            chainGroups.get(chain).getChildren().add(currMeshes);
        }
    }
}

