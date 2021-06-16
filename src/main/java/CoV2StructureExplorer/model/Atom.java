package CoV2StructureExplorer.model;

public class Atom {

    private static Integer globalID = 0;

    private final Integer id;
    private final String type;
    private final Residue residue;
    private final char chain;
    private final Position position;

    Atom(int id, String atomType, char chain, Residue residue, Position position) {
        this.id = id;
        this.residue = residue;
        this.type = atomType;
        this.chain = chain;
        this.position = position;
    }

    // incrementing id automatically should work, parsing it still feels 'safer'
    Atom(String atomType, char chain, Residue residue, Position position) {
        this.id = ++globalID;
        this.residue = residue;
        this.type = atomType;
        this.chain = chain;
        this.position = position;
    }

    public String getType() {
        return type;
    }

    public Residue getResidue() {
        return residue;
    }

    public char getChain() {
        return chain;
    }

    public Position getPosition() {
        return position;
    }

    public int getId() {
        return id;
    }

    // TODO: implement me
    private enum StructureType {
        HELIX,
        STRAND,
        COIL
    }

    // save as record instead of map, list etc. because predefined static length (always 3 coordinates)
    public record Position(double x, double y, double z) {
    }

}
