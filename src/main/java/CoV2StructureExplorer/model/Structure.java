package CoV2StructureExplorer.model;

import java.util.ArrayList;

public class Structure extends ArrayList<Model> {

    private int idx = 0;

    // This might be redundant since PDBFile already holds the ID, this is for completeness
    private final String pdbID;

    public int getIdx() {
        return idx;
    }

    public Structure(String pdbID){
        this.idx = ++idx;
        this.pdbID = pdbID;
    }



    public String getPdbID() {
        return pdbID;
    }
}
