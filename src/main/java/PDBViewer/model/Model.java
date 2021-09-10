package PDBViewer.model;

import java.util.ArrayList;
import java.util.Comparator;

public class Model extends ArrayList<Chain> {

    private final int id;
    private final Structure structure;
    private CoordExtrema coordExtrema;

    public Model(Structure structure, int id) {
        this.id = ++id;
        this.structure = structure;
    }

    public int getId() {
        return id;
    }

    public CoordExtrema getCoordExtrema() {
        return coordExtrema;
    }

    public void setCoordExtrema() {
        this.coordExtrema = new CoordExtrema(
                this.stream().map(Chain::getCoordExtrema).map(CoordExtrema::maxX)
                        .max(Comparator.comparing(Double::valueOf)).orElse(Double.MIN_VALUE),
                this.stream().map(Chain::getCoordExtrema).map(CoordExtrema::minX)
                        .min(Comparator.comparing(Double::valueOf)).orElse(Double.MAX_VALUE),
                this.stream().map(Chain::getCoordExtrema).map(CoordExtrema::maxY)
                        .max(Comparator.comparing(Double::valueOf)).orElse(Double.MIN_VALUE),
                this.stream().map(Chain::getCoordExtrema).map(CoordExtrema::minY)
                        .min(Comparator.comparing(Double::valueOf)).orElse(Double.MAX_VALUE),
                this.stream().map(Chain::getCoordExtrema).map(CoordExtrema::maxZ)
                        .max(Comparator.comparing(Double::valueOf)).orElse(Double.MIN_VALUE),
                this.stream().map(Chain::getCoordExtrema).map(CoordExtrema::minZ)
                        .min(Comparator.comparing(Double::valueOf)).orElse(Double.MAX_VALUE)
        );
    }

    public Structure getStructure() {
        return structure;
    }

}
