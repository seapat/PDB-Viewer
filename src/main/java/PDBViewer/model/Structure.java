package PDBViewer.model;

import java.util.ArrayList;

public class Structure extends ArrayList<Model> {

    // This might be redundant since PDBFile already holds the ID, this is for completeness
    private final String pdbID;
    private int idx = 0;

    public Structure(String pdbID) {
        this.idx = ++idx;
        this.pdbID = pdbID;
    }

    public int getIdx() {
        return idx;
    }

    public String getPdbID() {
        return pdbID;
    }
}
