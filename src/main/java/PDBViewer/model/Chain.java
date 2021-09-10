package PDBViewer.model;

import java.util.ArrayList;
import java.util.Comparator;

// name could be longer than one letter (technically)
public class Chain extends ArrayList<Residue> {

    private final Character chainID;
    private final Model model;
    private CoordExtrema coordExtrema;

    public Chain(char code, Model model) {
        this.chainID = code;
        this.model = model;

    }

    public CoordExtrema getCoordExtrema() {
        return coordExtrema;
    }

    public void setExtrema() {
        this.coordExtrema = new CoordExtrema(
                this.stream().map(Residue::getCoordExtrema).map(CoordExtrema::maxX)
                        .max(Comparator.comparing(Double::valueOf)).orElse(Double.MIN_VALUE),
                this.stream().map(Residue::getCoordExtrema).map(CoordExtrema::minX)
                        .min(Comparator.comparing(Double::valueOf)).orElse(Double.MAX_VALUE),
                this.stream().map(Residue::getCoordExtrema).map(CoordExtrema::maxY)
                        .max(Comparator.comparing(Double::valueOf)).orElse(Double.MIN_VALUE),
                this.stream().map(Residue::getCoordExtrema).map(CoordExtrema::minY)
                        .min(Comparator.comparing(Double::valueOf)).orElse(Double.MAX_VALUE),
                this.stream().map(Residue::getCoordExtrema).map(CoordExtrema::maxZ)
                        .max(Comparator.comparing(Double::valueOf)).orElse(Double.MIN_VALUE),
                this.stream().map(Residue::getCoordExtrema).map(CoordExtrema::minZ)
                        .min(Comparator.comparing(Double::valueOf)).orElse(Double.MAX_VALUE)
        );
    }

    public Character getChainID() {
        return chainID;
    }

    public Model getModel() {
        return model;
    }
}
