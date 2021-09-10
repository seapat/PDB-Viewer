package PDBViewer.model;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class Residue extends HashMap<String, Atom> {

    private static final Map<String, Character> lookUpAminoAcids = new HashMap<>() {{
            put("CYS", 'C');
            put("ASP", 'D');
            put("SER", 'S');
            put("GLN", 'Q');
            put("LYS", 'K');
            put("ILE", 'I');
            put("PRO", 'P');
            put("THR", 'T');
            put("PHE", 'F');
            put("ASN", 'N');
            put("GLY", 'G');
            put("HIS", 'H');
            put("LEU", 'L');
            put("ARG", 'R');
            put("TRP", 'W');
            put("ALA", 'A');
            put("VAL", 'V');
            put("GLU", 'E');
            put("TYR", 'Y');
            put("MET", 'M');
    }};

    private final int id;
    private final String threeLetter;
    private final Character oneLetter;
    private final Chain chain;
    private StructureType secStructure;

    public CoordExtrema getCoordExtrema() {
        return coordExtrema;
    }

    private CoordExtrema coordExtrema;

    public Residue(int id, String threeLetter, Chain chain) {
        this.id = id;
        this.threeLetter = threeLetter;
        this.chain = chain;
        this.secStructure = StructureType.COIL;
        this.oneLetter = lookUpAminoAcids.getOrDefault(this.threeLetter, null); // this.getThreeLetter().charAt(0)
    }

    public void setCoordExtrema() {
        this.coordExtrema = new CoordExtrema(
                this.values().stream().map(atom -> atom.getPosition().x())
                        .max(Comparator.comparing(Double::valueOf)).orElse(Double.MIN_VALUE),
                this.values().stream().map(atom -> atom.getPosition().x())
                        .min(Comparator.comparing(Double::valueOf)).orElse(Double.MAX_VALUE),
                this.values().stream().map(atom -> atom.getPosition().y())
                        .max(Comparator.comparing(Double::valueOf)).orElse(Double.MIN_VALUE),
                this.values().stream().map(atom -> atom.getPosition().y())
                        .min(Comparator.comparing(Double::valueOf)).orElse(Double.MAX_VALUE),
                this.values().stream().map(atom -> atom.getPosition().z())
                        .max(Comparator.comparing(Double::valueOf)).orElse(Double.MIN_VALUE),
                this.values().stream().map(atom -> atom.getPosition().z())
                        .min(Comparator.comparing(Double::valueOf)).orElse(Double.MAX_VALUE)
        );
    }

    public Chain getChain() {
        return chain;
    }

    public String getThreeLetter() {
        return threeLetter;
    }

    public int getId() {
        return id;
    }

    public StructureType getStructureType() {
        return secStructure;
    }

    public void setStructure(StructureType secStructure) {
        this.secStructure = secStructure;
    }

    public Character getOneLetter() {
        return oneLetter;
    }
}
