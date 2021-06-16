package CoV2StructureExplorer.model;

import java.util.ArrayList;

public class Residue extends ArrayList<Atom> {

    private final int id;
    private final String type;
    private final Chain chain;

    public Chain getChain() {
        return chain;
    }

    public Residue(int id, String type, Chain chain) {
        this.id = id;
        this.type = type;
        this.chain = chain;
    }

    public String getType() {
        return type;
    }

    public int getId() {
        return id;
    }

}
