package CoV2StructureExplorer.model;

import java.util.ArrayList;

public class Residue extends ArrayList<Atom> {

    private int id = 0;
    private String type;

    public Residue(int id, String type) {
        this.id = id;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
