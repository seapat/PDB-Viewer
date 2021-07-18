package CoV2StructureExplorer.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Atom {

    private final static Map<Character, Double> atomRadii = new HashMap<>(){{
            // single bonded radii found on wikipedia "covalent radius" in Angstrom
            put('O', 0.63);
            put('C', 0.75);
            put('N', 0.71);
            put('S', 1.03);
            put('P', 1.11);
            put('H', 0.32);
    }};
    private final Integer id;
    private final String complexType;
    private final char simpleType;
    private final Residue residue;
    private final Chain chain;
    private final Position position;
    private final ArrayList<Atom> bonds;
    private final Double radius;
    private StructureType secStructure;

    Atom(int id, String complexType, char simpleType, Residue residue, Position position) {
        this.id = id;
        this.residue = residue;
        this.complexType = complexType;
        this.simpleType = simpleType;
        this.chain = residue.getChain();
        this.position = position;
        this.bonds = new ArrayList<>();
        this.secStructure = StructureType.COIL;
        this.radius = atomRadii.getOrDefault(simpleType, 1d);
    }

    public ArrayList<Atom> getBonds() {
        return bonds;
    }

    public Double getRadius() {
        return radius;
    }

    public String getComplexType() {
        return complexType;
    }

    public Residue getResidue() {
        return residue;
    }

    public Chain getChain() {
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
        if (this.getId() != atom.getId()) {
            this.bonds.add(atom);
        }
    }

    public StructureType getStructureType() {
        return secStructure;
    }

    public void setStructureType(StructureType structureType) {
        this.secStructure = structureType;
    }

    // save as record instead of map, list etc. because predefined static length (always 3 coordinates)
    public record Position(double x, double y, double z) {
    }

}
