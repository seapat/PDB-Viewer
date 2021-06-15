package CoV2StructureExplorer.model;

import java.util.ArrayList;

// name could be longer than one letter (technically)
public class Chain extends ArrayList<Residue> {

    char iCode;
    int idx = 0;

    public Chain(char icode){
        this.iCode = icode;
        this.idx = ++idx;
    }

}
