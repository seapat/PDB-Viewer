package CoV2StructureExplorer.view;

import CoV2StructureExplorer.model.Atom;
import CoV2StructureExplorer.model.Chain;
import CoV2StructureExplorer.model.Residue;
import CoV2StructureExplorer.model.Structure;
import CoV2StructureExplorer.presenter.ViewPresenter;
import CoV2StructureExplorer.selection.SetSelectionModel;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;

import java.util.*;

public class Balls extends Group {

    //sphere.setDrawMode(DrawMode.LINE) to see mesh used for drawing

    private static final Map<Object, Color> atomColors = Map.ofEntries(
            // single bonded radii found on wikipedia "covalent radius"
            new AbstractMap.SimpleEntry<>('O', Color.RED),
            new AbstractMap.SimpleEntry<>('C', Color.DIMGRAY),
            new AbstractMap.SimpleEntry<>('N', Color.BLUE),
            new AbstractMap.SimpleEntry<>('S', Color.YELLOW),
            new AbstractMap.SimpleEntry<>('P', Color.ORANGE),
            new AbstractMap.SimpleEntry<>('H', Color.WHITE)
    );
    private static final Map<Object, Color> secStrucColors = Map.ofEntries(
            // is it ok to check for Enums here which are declared in the model? maybe
            new AbstractMap.SimpleEntry<>("COIL", Color.LIGHTGREEN),
            new AbstractMap.SimpleEntry<>("SHEET", Color.YELLOW),
            new AbstractMap.SimpleEntry<>("HELIX", Color.RED),
            new AbstractMap.SimpleEntry<>("NUCLEOTIDE", Color.CORNFLOWERBLUE)
    );

    // create list of colors, iterator is renewed once the list ends
    private static final ArrayList<Color> chainColors = new ArrayList<>(Arrays.asList(
            Color.DARKMAGENTA,
            Color.AQUAMARINE,
            Color.HOTPINK,
            Color.INDIANRED,
            Color.INDIGO,
            Color.LIGHTGREEN,
            Color.YELLOW,
            Color.MEDIUMPURPLE,
            Color.ORANGE,
            Color.PALEVIOLETRED,
            Color.CORNFLOWERBLUE
    ));


    private final StringProperty selectedResiduesProp;
    private final HashMap<Chain, HashMap<Residue, HashMap<Atom, Sphere>>> modelToView = new HashMap<>();
    private final CyclicIterator<Color, ArrayList<Color>> iterateChainColors = new CyclicIterator<>(chainColors); //chainColors.iterator();
    private Color currIterColor = iterateChainColors.next();

    public Balls(Structure pdb, ReadOnlyDoubleProperty radiusScale, Integer modelChoice, SetSelectionModel<Residue> selectedResidues) { //<Sphere>

        selectedResiduesProp = new SimpleStringProperty(selectedResidues, "StringBuilder");

        for (var chain : pdb.get(modelChoice - 1)) {
            modelToView.put(chain, new HashMap<>());
            var currChain = modelToView.get(chain);
            for (var residue : chain) {
                currChain.put(residue, new HashMap<>());
                var curRes = currChain.get(residue);

                for (var atom : residue.values()) {
                    final Sphere sphere = new Sphere(100, 32);

                    sphere.setTranslateX((int) (100 * atom.getPosition().x()));
                    sphere.setTranslateY((int) (100 * atom.getPosition().y()));
                    sphere.setTranslateZ((int) (100 * atom.getPosition().z()));

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
                                        .toList()));
                        System.out.println("clicking is recognized");
//                        System.out.println(selectedResidues.getSelectedItems());
                    });

                    // Add twice, once to Group, other time to the hashmap to access subsets of spheres
                    this.getChildren().add(sphere);
                    curRes.put(atom, sphere);
                }
            }
        }
        System.out.println("balls: " + this.getChildren().size());
    }

    public ObservableValue<? extends String> getSelectedResiduesProp() {
        return selectedResiduesProp;
    }

    public StringProperty selectedResiduesPropProperty() {
        return selectedResiduesProp;
    }

    public HashMap<Chain, HashMap<Residue, HashMap<Atom, Sphere>>> getModelToView() {
        return modelToView;
    }

    public void changeColor(WindowController controller) {

        iterateChainColors.reset();
        currIterColor = iterateChainColors.next();
        var colorChoice = controller.getColorChoice().getValue();

        Color color;
        Atom prevAtom = null;

        for (var chain : modelToView.entrySet()) {
            for (var residues : chain.getValue().entrySet()) {
                for (var atomSphere : residues.getValue().entrySet()) {
                    var atom = atomSphere.getKey();
                    var sphere = atomSphere.getValue();

                    color = getColor(colorChoice, atom, prevAtom);

                    sphere.setMaterial(new PhongMaterial(color));
                    prevAtom = atom;
                }
            }
        }
        // show or remove corresponding legend
        switch (colorChoice) {
            case "Structure" -> ViewPresenter.changeLegend(controller, secStrucColors);
            case "Chains", "Residue" -> controller.getLegendPane().setVisible(false);
            default -> ViewPresenter.changeLegend(controller, atomColors);
        }
    }

    public Color getColor(String colorChoice, Atom atom, Atom prev) {

        Color color;
        switch (colorChoice) {
            case "Structure" -> color = secStrucColors.getOrDefault(atom.getStructureType().toString(), Color.PLUM);
            case "Chains" -> {
                if (prev != null && !atom.getChain().equals(prev.getChain())) {
                    currIterColor = iterateChainColors.next();
                }
                color = currIterColor;
            }
            case "Residue" -> {
                if (prev != null && !atom.getResidue().equals(prev.getResidue())) {
                    currIterColor = iterateChainColors.next();
                }
                color = currIterColor;
            }
            default -> color = atomColors.getOrDefault(atom.getSimpleType(), Color.PLUM);
        }
        return color;
    }

    public Group highlightChain(WindowController controller) {
        var selection = new Group();

        iterateChainColors.reset();
        currIterColor = iterateChainColors.next();

        var colorChoice = controller.getColorChoice().getValue();
        var chainChoice = controller.getFocusChoice().getValue();

        Atom prevAtom = null;

        for (var chain : modelToView.entrySet()) {
            for (var residues : chain.getValue().entrySet()) {
                for (var atomSphere : residues.getValue().entrySet()) {
                    var atom = atomSphere.getKey();
                    var sphere = atomSphere.getValue();
                    var color = getColor(colorChoice, atom, prevAtom);

                    if (chain.getKey().getChainID() != chainChoice.charAt(0) && !chainChoice.equals("All")) {

                        sphere.setMaterial(new PhongMaterial(Color.hsb(0.5, 0, 0.1, 0.1))); // color.deriveColor(1, 0, 0.5, 0.5)
//                        sphere.setVisible(false);
                    } else {
//                        sphere.setVisible(true);
                        sphere.setMaterial(new PhongMaterial(color));
                    }
                    prevAtom = atom;
                }
            }
        }
        return selection;
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
