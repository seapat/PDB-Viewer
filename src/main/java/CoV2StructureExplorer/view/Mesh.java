package CoV2StructureExplorer.view;

import CoV2StructureExplorer.model.Residue;
import CoV2StructureExplorer.model.Structure;
import CoV2StructureExplorer.model.StructureType;
import javafx.geometry.Point3D;
import javafx.scene.Group;

public class Mesh extends Group {

    public Mesh (Structure pdb, Integer modelChoice) {

//        var meshMaker = new Shape3DUtils();

        for (var chain : pdb.get(modelChoice - 1)) {
            Residue prevRes = null;
            for (var residue : chain) {

                // skip first iteration, then make sure both residues are not DNA/RNA and neither is glycine
                if (prevRes != null &&
                    prevRes.getStructureType() != StructureType.NUCLEOTIDE &&
                    residue.getStructureType() != StructureType.NUCLEOTIDE &&
                    residue.containsKey("CB") && prevRes.containsKey("CB")){

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
                    this.getChildren().add(mesh);
                }
                prevRes = residue;
            }
        }
        System.out.println("Meshes: " + this.getChildren().size());
    }
}

