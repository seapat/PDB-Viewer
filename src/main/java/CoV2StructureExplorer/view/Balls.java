package CoV2StructureExplorer.view;

import CoV2StructureExplorer.model.Atom;
import CoV2StructureExplorer.model.Chain;
import CoV2StructureExplorer.model.Residue;
import CoV2StructureExplorer.model.Structure;
import CoV2StructureExplorer.selection.SetSelectionModel;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

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
//            Color.DARKMAGENTA,
            Color.AQUAMARINE, Color.HOTPINK, Color.INDIANRED, Color.INDIGO, Color.LIGHTGREEN, Color.YELLOW,
            Color.MEDIUMPURPLE, Color.ORANGE, Color.YELLOW, Color.PALEVIOLETRED, Color.LIGHTGREEN, Color.CORNFLOWERBLUE
    ));
//    private final ObservableSet<Residue> selectedResidueNames = FXCollections.observableSet(new LinkedHashSet<>());
    private final StringProperty selectedResiduesProp;
    private final HashMap<Chain, HashMap<Residue, HashMap<Atom, Sphere>>> modelToView = new HashMap<>();
    private Iterator<Color> iterateChainColors = chainColors.iterator();
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
                        System.out.println(selectedResidues.getSelectedItems());
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

        var colorChoice = controller.getColorChoice().getValue();

        Chain lastChain = null;
        Residue lastResidue = null;
        Color color;
        // reset color choices, make coloring reproducible
        iterateChainColors = chainColors.iterator();

        for (var chain : modelToView.entrySet()) {
            for (var residues : chain.getValue().entrySet()) {
                for (var atomSphere : residues.getValue().entrySet()) {
                    var atom = atomSphere.getKey();
                    var sphere = atomSphere.getValue();

                    // reset colors if all are used up
                    progressIterator(colorChoice, lastChain, lastResidue, atom);
                    color = getColor(colorChoice, atom);
                    sphere.setMaterial(new PhongMaterial(color));
                    lastChain = atom.getResidue().getChain();
                    lastResidue = atom.getResidue();
                }
            }
        }
        // show or remove corresponding legend
        switch (colorChoice) {
            case "Structure" -> changeLegend(controller, secStrucColors);
            case "Chains", "Residue" -> controller.getLegendPane().setVisible(false);
            default -> changeLegend(controller, atomColors);
        }


    }

    public Color getColor(String colorChoice, Atom atom) {

        Color color;
        switch (colorChoice) {
            case "Structure" -> color = secStrucColors.getOrDefault(atom.getStructureType().toString(), Color.PLUM);
            case "Chains", "Residue" -> color = currIterColor;
            default -> color = atomColors.getOrDefault(atom.getSimpleType(), Color.PLUM);
        }

        return color;
    }

    //TODO maybe move this to the vie presenter?
    private void changeLegend(WindowController controller, Map<Object, Color> colorMap) {
        // create legends for atoms and sec. structure
        var recSize = 20;
        var spacer = 5;

        controller.getLegendPane().setVisible(true);
        controller.getLegendPane().getChildren().clear();

        var legendItems = new LinkedHashMap<Rectangle, Text>();
        // TreeMap + LinkedHashMap allow deterministic ordering of keys
        new TreeMap<>(colorMap).forEach((key, value) -> legendItems.put(
                new Rectangle(recSize, recSize, value),
                new Text(key.toString().substring(0, 1).toUpperCase() + key.toString().substring(1).toLowerCase())
        ));

        AtomicReference<Integer> idx = new AtomicReference<>(0);
        legendItems.forEach((key, value) -> {
            key.setY(idx.get() * (recSize + spacer));
            key.setStroke(Color.BLACK);

            value.setStyle("-fx-inner-fill: white");
            value.setStroke(Color.BLACK);
            value.setX(recSize + spacer);
            value.setY(key.getY() + spacer * 3);
            value.setFont(Font.font("calibri light", FontWeight.EXTRA_LIGHT, FontPosture.REGULAR, 13));
            controller.getLegendPane().getChildren().add(key);
            controller.getLegendPane().getChildren().add(value);

            idx.getAndSet(idx.get() + 1);
        });
    }

    public void highlightChain(WindowController controller) {

        var colorChoice = controller.getColorChoice().getValue();
        var chainChoice = controller.getFocusChoice().getValue();

        Chain lastChain = null;
        Residue lastResidue = null;
        iterateChainColors = chainColors.iterator();

        for (var chain : modelToView.entrySet()) {
            for (var residues : chain.getValue().entrySet()) {
                for (var atomSphere : residues.getValue().entrySet()) {
                    var atom = atomSphere.getKey();
                    var sphere = atomSphere.getValue();
                    var color = getColor(colorChoice, atom);

                    progressIterator(colorChoice, lastChain, lastResidue, atom);

                    if (chain.getKey().getChainID() != chainChoice.charAt(0) && !chainChoice.equals("All")) {
                        sphere.setMaterial(new PhongMaterial(Color.hsb(0.5, 0, 0.1, 0.1))); // color.deriveColor(1, 0, 0.5, 0.5)

                    } else {
                        sphere.setMaterial(new PhongMaterial(color));
                    }
                    lastChain = atom.getResidue().getChain();
                    lastResidue = atom.getResidue();
                }
            }
        }
    }

    private void progressIterator(String colorChoice, Chain lastChain, Residue lastResidue, Atom atom) {
        if (!iterateChainColors.hasNext()) {
            iterateChainColors = chainColors.iterator();
        }

        if (colorChoice.equals("Chains") && lastChain != null && !lastChain.equals(atom.getResidue().getChain())
                ||
                colorChoice.equals("Residue") && lastResidue != null && !lastResidue.equals(atom.getResidue())) {
            currIterColor = iterateChainColors.next();
        }
    }
}
