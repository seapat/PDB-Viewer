package CoV2StructureExplorer.model;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Residue extends HashMap<String, Atom> {

    private static final Map<String, Character> lookUpAminoAcids = Map.ofEntries(
            new AbstractMap.SimpleEntry<>("CYS", 'C'),
            new AbstractMap.SimpleEntry<>("ASP", 'D'),
            new AbstractMap.SimpleEntry<>("SER", 'S'),
            new AbstractMap.SimpleEntry<>("GLN", 'Q'),
            new AbstractMap.SimpleEntry<>("LYS", 'K'),
            new AbstractMap.SimpleEntry<>("ILE", 'I'),
            new AbstractMap.SimpleEntry<>("PRO", 'P'),
            new AbstractMap.SimpleEntry<>("THR", 'T'),
            new AbstractMap.SimpleEntry<>("PHE", 'F'),
            new AbstractMap.SimpleEntry<>("ASN", 'N'),
            new AbstractMap.SimpleEntry<>("GLY", 'G'),
            new AbstractMap.SimpleEntry<>("HIS", 'H'),
            new AbstractMap.SimpleEntry<>("LEU", 'L'),
            new AbstractMap.SimpleEntry<>("ARG", 'R'),
            new AbstractMap.SimpleEntry<>("TRP", 'W'),
            new AbstractMap.SimpleEntry<>("ALA", 'A'),
            new AbstractMap.SimpleEntry<>("VAL",'V'),
            new AbstractMap.SimpleEntry<>("GLU", 'E'),
            new AbstractMap.SimpleEntry<>("TYR", 'Y'),
            new AbstractMap.SimpleEntry<>("MET", 'M'));

    private final int id;
    private final String threeLetter;
    private final Character oneLetter;
    private final Chain chain;
    private StructureType secStructure;


    public Chain getChain() {
        return chain;
    }

    public Residue(int id, String threeLetter, Chain chain) {
        this.id = id;
        this.threeLetter = threeLetter;
        this.chain = chain;
        this.secStructure = StructureType.COIL;
        this.oneLetter = lookUpAminoAcids.getOrDefault(this.threeLetter, this.getThreeLetter().charAt(0));
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
