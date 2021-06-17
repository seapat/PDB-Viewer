package CoV2StructureExplorer.model;

import java.util.ArrayList;

// name could be longer than one letter (technically)
public class Chain extends ArrayList<Residue> {

    private final char chainID;
    private final Model model;
    private int idx = 0;


    public Chain(char code, Model model){
        this.chainID = code;
        this.model = model;
        this.idx = ++idx;
    }

    public char getChainID() {
        return chainID;
    }

    public int getIdx() {
        return idx;
    }

    public Model getModel() {
        return model;
    }
}
