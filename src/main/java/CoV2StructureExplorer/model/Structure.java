package CoV2StructureExplorer.model;

import java.util.ArrayList;

public class Structure extends ArrayList<Model> {

    int idx = 0;

    public int getIdx() {
        return idx;
    }

    public Structure(){
        this.idx = ++idx;
    }
}
