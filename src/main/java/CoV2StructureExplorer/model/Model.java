package CoV2StructureExplorer.model;

import java.util.ArrayList;

public class Model extends ArrayList<Chain> {

    // TODO: Model id's start from 1, change here and verify that modelChoice still works correctly
//    private static int globalID = 0;
    private final int id;
    private final Structure structure;

    public int getId() {
        return id;
    }

    public Structure getStructure() {
        return structure;
    }

    public Model(Structure structure, int id){
        this.id = ++id;
        this.structure = structure;
    }

}
