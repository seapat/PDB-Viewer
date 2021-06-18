package CoV2StructureExplorer.model;

import java.util.ArrayList;

// name could be longer than one letter (technically)
public class Chain extends ArrayList<Residue> {

    private final char chainID;
    private final Model model;

    public Chain(char code, Model model){
        this.chainID = code;
        this.model = model;
    }

    public char getChainID() {
        return chainID;
    }


    public Model getModel() {
        return model;
    }
}
