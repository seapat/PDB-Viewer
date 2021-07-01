package CoV2StructureExplorer.model;

import java.util.ArrayList;

public class Model extends ArrayList<Chain> {

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
