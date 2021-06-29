package CoV2StructureExplorer.model;

import java.util.ArrayList;

public class Residue extends ArrayList<Atom> {

    private final int id;
    private final String type;
    private final Chain chain;
    private StructureType secStructure;

    public Chain getChain() {
        return chain;
    }

    public Residue(int id, String type, Chain chain) {
        this.id = id;
        this.type = type;
        this.chain = chain;
        this.secStructure = StructureType.COIL;
    }

    public String getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    public StructureType getStructureType() {
        return secStructure;
    }

    public void setStructure(StructureType secStructure) {
        this.secStructure = secStructure;
    }



}
