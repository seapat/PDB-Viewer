package PDBViewer.view;

import PDBViewer.model.*;
import PDBViewer.presenter.ViewPresenter;
import PDBViewer.selection.SetSelectionModel;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;

import java.util.*;

public class Balls extends ArrayList<Sphere> {

    private static final Map<Object, Color> atomColors = new TreeMap<>() {{
        // single bonded radii found on wikipedia "covalent radius"
        put('O', Color.RED);
        put('C', Color.DIMGRAY);
        put('N', Color.BLUE);
        put('S', Color.YELLOW);
        put('P', Color.ORANGE);
        put('H', Color.WHITE);
    }};
    private static final Map<Object, Color> secStrucColors = new TreeMap<>() {{
        // is it ok to check for Enums here which are declared in the model? maybe
        put("COIL", Color.LIGHTGREEN);
        put("SHEET", Color.YELLOW);
        put("HELIX", Color.RED);
        put("NUCLEOTIDE", Color.CORNFLOWERBLUE);
    }};
    private static final ArrayList<Color> chainColorsChoices = new ArrayList<>() {{
        add(Color.DARKMAGENTA);
        add(Color.AQUAMARINE);
        add(Color.HOTPINK);
        add(Color.INDIANRED);
        add(Color.INDIGO);
        add(Color.LIGHTGREEN);
        add(Color.YELLOW);
        add(Color.MEDIUMPURPLE);
        add(Color.ORANGE);
        add(Color.PALEVIOLETRED);
        add(Color.CORNFLOWERBLUE);
    }};
    private static final Map<Object, Color> residueColorLookUp = new HashMap<>() {{
        put("CYS", Color.CHOCOLATE);
        put("ASP", Color.BLUE);
        put("SER", Color.YELLOW);
        put("GLN", Color.YELLOW);
        put("LYS", Color.RED);
        put("ILE", Color.GREEN);
        put("PRO", Color.DARKGOLDENROD);
        put("THR", Color.YELLOW);
        put("PHE", Color.GREEN);
        put("ASN", Color.YELLOW);
        put("GLY", Color.ROSYBROWN);
        put("HIS", Color.RED);
        put("LEU", Color.GREEN);
        put("ARG", Color.RED);
        put("TRP", Color.GREEN);
        put("ALA", Color.GREEN);
        put("VAL", Color.GREEN);
        put("GLU", Color.BLUE);
        put("TYR", Color.GREEN);
        put("MET", Color.GREEN);
        put("A", Color.PALEVIOLETRED);
        put("T", Color.DARKSALMON);
        put("G", Color.AZURE);
        put("C", Color.LIGHTGREEN);
        put("U", Color.ORANGE);
    }};
    private static final Map<Object, Color> residueColorLegend = new LinkedHashMap<>() {{
        put("CYS", Color.LEMONCHIFFON);
        put("PRO", Color.DARKGOLDENROD);
        put("GLY", Color.PINK);
        put("Pos. charged SC", Color.RED);
        put("Neg. charged SC", Color.BLUE);
        put("Hydrophobic SC", Color.GREEN);
        put("Uncharged SC", Color.YELLOW);
        put("A", Color.PALEVIOLETRED);
        put("T", Color.DARKSALMON);
        put("G", Color.AZURE);
        put("C", Color.LIGHTGREEN);
        put("U", Color.ORANGE);
    }};
    private final HashMap<Chain, HashMap<Residue, HashMap<Atom, Sphere>>> modelToView = new HashMap<>();
    private final HashMap<Character, Color> chainColorsLookUp = new HashMap<>();

    public Balls(Structure pdb, SetSelectionModel<Residue> selectedResidues, HashMap<Chain, Group> chainGroups,
                 WindowController controller, StringProperty selectedResiduesProp) {

        ReadOnlyDoubleProperty radiusScale = controller.getRadiusScale().valueProperty();
        Integer modelChoice = controller.getModelChoice().getValue();

        CyclicIterator<Color, ArrayList<Color>> iterateChainColors = new CyclicIterator<>(chainColorsChoices);

        for (var chain : pdb.get(modelChoice - 1)) {

            chainColorsLookUp.put(chain.getChainID(), iterateChainColors.next());
            modelToView.put(chain, new HashMap<>());

            chainGroups.putIfAbsent(chain, new Group());
            var currBalls = new Group();

            var currChain = modelToView.get(chain);
            for (var residue : chain) {

                currChain.put(residue, new HashMap<>());
                var curRes = currChain.get(residue);

                for (var atom : residue.values()) {
                    final Sphere sphere = new Sphere(100, 32);

                    sphere.setTranslateX(atom.getPosition().x());
                    sphere.setTranslateY(atom.getPosition().y());
                    sphere.setTranslateZ(atom.getPosition().z());

                    sphere.radiusProperty().bind(radiusScale.multiply(atom.getRadius()));

                    // SELECTION
                    sphere.setOnMouseClicked(e -> {
                        if (!e.isShiftDown()) {
                            selectedResidues.clearSelection();
                        }

                        selectedResidues.select(residue);
                        selectedResiduesProp.setValue(String.join("",
                                selectedResidues.getSelectedItems().stream()
                                        .map(Residue::getThreeLetter)
                                        .map(code -> code += " ")
                                        .sorted(Comparator.comparingInt(String::length))
                                        .toList()));
                    });
                    sphere.setId(atom.getChain().getChainID() +
                            ":" + atom.getResidue().getId() +
                            ":" + atom.getId()
                    );
                    this.add(sphere);
                    curRes.put(atom, sphere);
                    currBalls.getChildren().add(sphere);
                }
            }
            currBalls.setVisible(false);
            currBalls.visibleProperty().bind(controller.getAtomsChecked().selectedProperty());
            chainGroups.get(chain).getChildren().add(currBalls);
        }
    }

    public HashMap<Chain, HashMap<Residue, HashMap<Atom, Sphere>>> getModelToView() {
        return modelToView;
    }

    public void changeColor(WindowController controller) {

        var colorChoice = controller.getColorChoice().getValue();

        Color color;
        for (var chain : modelToView.entrySet()) {
            for (var residues : chain.getValue().entrySet()) {
                for (var atomSphere : residues.getValue().entrySet()) {
                    color = getColor(colorChoice, atomSphere.getKey());
                    atomSphere.getValue().setMaterial(new PhongMaterial(color));
                }
            }
        }
        // show or remove corresponding legend
        switch (colorChoice) {
            case "Structure" -> ViewPresenter.changeLegend(controller, secStrucColors);
            case "Residue" -> ViewPresenter.changeLegend(controller, residueColorLegend);
            case "Chains" -> controller.getLegendPane().setVisible(false);
            default -> ViewPresenter.changeLegend(controller, atomColors);
        }
    }

    public Color getColor(String colorChoice, Atom atom) {

        Color color;
        switch (colorChoice) {
            case "Structure" -> color = secStrucColors.getOrDefault(atom.getStructureType().toString(), Color.PLUM);
            case "Chains" -> color = chainColorsLookUp.get(atom.getChain().getChainID());
            case "Residue" -> color = residueColorLookUp.get(atom.getResidue().getThreeLetter());
            default -> color = atomColors.getOrDefault(atom.getSimpleType(), Color.PLUM);
        }
        return color;
    }

    public void highlightChain(WindowController controller) {

        var colorChoice = controller.getColorChoice().getValue();
        var chainChoice = controller.getFocusChoice().getValue();

        for (var chain : modelToView.entrySet()) {
            for (var residues : chain.getValue().entrySet()) {
                for (var atomSphere : residues.getValue().entrySet()) {
                    var atom = atomSphere.getKey();
                    var sphere = atomSphere.getValue();
                    var color = getColor(colorChoice, atom);
                    if (chain.getKey().getChainID() != chainChoice.charAt(0) && !chainChoice.equals("All")) {
                        sphere.setMaterial(new PhongMaterial(Color.hsb(0.5, 0, 0.1, 0.2)));
                    } else {
                        sphere.setMaterial(new PhongMaterial(color));
                    }
                }
            }
        }
    }

    public record LayoutBonds(Double maxX, Double minX, Double maxY, Double minY, Double maxZ, Double minZ) {
    }

    public static class CyclicIterator<E, C extends Collection<E>> implements Iterator<E> {
        final private C mElements;
        private Iterator<E> mIterator;

        public CyclicIterator(C elements) {
            mElements = elements;
            mIterator = elements.iterator();
        }

        public void reset() {
            mIterator = mElements.iterator();
        }

        @Override
        public boolean hasNext() {
            if (!mIterator.hasNext()) {
                mIterator = mElements.iterator();
            }
            return mIterator.hasNext();
        }

        @Override
        public E next() {
            if (!mIterator.hasNext()) {
                mIterator = mElements.iterator();
            }
            return mIterator.next();
        }
    }
}
