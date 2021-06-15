package CoV2StructureExplorer.model;

import java.util.ArrayList;

public class Model extends ArrayList<Chain> {

    int idx = 0;

    public int getIdx() {
        return idx;
    }

    public Model(){
        this.idx = ++idx;
    }

}
