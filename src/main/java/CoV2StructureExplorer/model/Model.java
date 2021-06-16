package CoV2StructureExplorer.model;

import java.util.ArrayList;

public class Model extends ArrayList<Chain> {

    private int id = 0;
    private final Structure structure;

    public int getId() {
        return id;
    }

    public Structure getStructure() {
        return structure;
    }

    public Model(Structure structure){
        this.id = ++id;
        this.structure = structure;
    }

}
