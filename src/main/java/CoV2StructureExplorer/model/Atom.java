package CoV2StructureExplorer.model;

import java.util.ArrayList;

public class Atom {


    private final Integer id;
    private final String complexType;
    private final char simpleType;
    private final Residue residue;
    private final char chain;
    private final Position position;
    private ArrayList<Atom> bonds;

    Atom(int id, String complexType, char simpleType, char chain, Residue residue, Position position) {
        this.id = id;
        this.residue = residue;
        this.complexType = complexType;
        this.simpleType = simpleType;
        this.chain = chain;
        this.position = position;
        this.bonds = new ArrayList<>();
    }


    public String getComplexType() {
        return complexType;
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

    public char getSimpleType() {
        return simpleType;
    }

    public void addBond(Atom atom) {
        if (this.getId() != atom.getId()){
            this.bonds.add(atom);
        }
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
