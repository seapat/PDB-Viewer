package CoV2StructureExplorer.model;

import java.util.Map;

public class Atom {

    String type;
    Residue residue;
    Map<Character, Double> coords;
    char chain;
    Integer id;
    static Integer globalID = 0;

    Atom(String atomType, Map<Character, Double> coords, char chain, Residue residue) {
        this.id = ++globalID; //TODO: check if incrementing like this works
        this.residue = residue;
        this.type = atomType;
        this.coords = coords;
        this.chain = chain;
    }

    public int getId() {
        return id;
    }


    private enum StructureType {
        HELIX,
        STRAND,
        COIL
    }

    // FIXME: good idea?
    public record coords(float x, float y, float z) {
    }

}
