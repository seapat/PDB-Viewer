package model;

import java.util.ArrayList;
import java.util.SortedMap;

public class Residue extends ArrayList<Atom> {

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
