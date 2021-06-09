package model;

import java.util.SortedMap;

public class Residue {

    private SortedMap<Integer, Atom> atoms;
    private int id;

    public SortedMap<Integer, Atom> getAtoms() {
        return this.atoms;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
