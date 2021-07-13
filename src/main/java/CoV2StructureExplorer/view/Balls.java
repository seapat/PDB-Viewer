package CoV2StructureExplorer.view;

import CoV2StructureExplorer.model.Atom;
import CoV2StructureExplorer.model.Chain;
import CoV2StructureExplorer.model.Residue;
import CoV2StructureExplorer.model.Structure;
import CoV2StructureExplorer.selection.SetSelectionModel;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
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

    private final static Map<Object, Color> atomColors = Map.ofEntries(
            // single bonded radii found on wikipedia "covalent radius"
            new AbstractMap.SimpleEntry<>('O', Color.RED),
            new AbstractMap.SimpleEntry<>('C', Color.DIMGRAY),
            new AbstractMap.SimpleEntry<>('N', Color.BLUE),
            new AbstractMap.SimpleEntry<>('S', Color.YELLOW),
            new AbstractMap.SimpleEntry<>('P', Color.ORANGE),
            new AbstractMap.SimpleEntry<>('H', Color.WHITE)
    );
    private final static Map<Object, Color> secStrucColors = Map.ofEntries(
            // is it ok to check for Enums here which are declared in the model? maybe
            new AbstractMap.SimpleEntry<>("COIL", Color.LIGHTGREEN),
            new AbstractMap.SimpleEntry<>("SHEET", Color.YELLOW),
            new AbstractMap.SimpleEntry<>("HELIX", Color.RED),
            new AbstractMap.SimpleEntry<>("NUCLEOTIDE", Color.CORNFLOWERBLUE)
    );

    // create list of colors, iterator is renewed once the list ends
    private final static List<Color> chainColors = Collections.synchronizedList(new ArrayList<>(Arrays.asList(
            Color.DARKMAGENTA, Color.AQUAMARINE, Color.HOTPINK, Color.INDIANRED, Color.INDIGO, Color.LIGHTGREEN, Color.YELLOW,
            Color.MEDIUMPURPLE, Color.ORANGE, Color.YELLOW, Color.PALEVIOLETRED, Color.LIGHTGREEN, Color.CORNFLOWERBLUE
    )));
    private static Iterator<Color> iterateChainColors = chainColors.iterator();
    private static Color currIterColor = iterateChainColors.next();
    private final HashMap<Chain, HashMap<Residue, HashMap<Atom, Sphere>>> modelToView = new HashMap<>();

    public Balls(Structure pdb, ReadOnlyDoubleProperty radiusScale, Integer modelChoice, SetSelectionModel<Atom> selectedAtoms) {

        for (var chain : pdb.get(modelChoice - 1)) {
            if (!iterateChainColors.hasNext()) {
                iterateChainColors = chainColors.iterator();
            }
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
                            selectedAtoms.clearSelection();
                        }
                        residue.values().forEach(selectedAtoms::select);
                        System.out.println("clicking is recognized");
                        System.out.println(selectedAtoms.getSelectedItems());
                    });

                    // Add twice, once to Group, other time to the hashmap to access subsets of spheres
                    this.getChildren().add(sphere);
                    curRes.put(atom, sphere);
                }

            }
        }
        System.out.println("balls: " + this.getChildren().size());
    }

    public HashMap<Chain, HashMap<Residue, HashMap<Atom, Sphere>>> getModelToView() {
        return modelToView;
    }

    public void changeColor(String colorChoice, WindowController controller) {

        Character lastChain = null;
        Residue lastResidue = null;

        for (var chain : modelToView.entrySet()) {
            for (var residues : chain.getValue().entrySet()) {
                for (var atomSphere : residues.getValue().entrySet()) {
                    var atom = atomSphere.getKey();
                    var sphere = atomSphere.getValue();

                    // reset colors if all are used up
                    if (!iterateChainColors.hasNext()) {
                        iterateChainColors = chainColors.iterator();
                    }

                    // go to next chain
                    if ((colorChoice.equals("Chains") && lastChain != null && lastChain != atom.getChain()) ||
                            (colorChoice.equals("Residue") && lastResidue != null && !lastResidue.equals(atom.getResidue()))) {
                        currIterColor = iterateChainColors.next();
                    }

                    Color color;
                    color = this.getColor(colorChoice, atomSphere);

                    sphere.setMaterial(new PhongMaterial(color));

                    lastChain = atom.getChain();
                    lastResidue = atom.getResidue();
                }
            }
        }

//        for (var atomSphere : atomSpheres.entrySet()) {
//            var atom = atomSphere.getKey();
//            var sphere = atomSphere.getValue();
//
//            // reset colors if all are used up
//            if (!iterateChainColors.hasNext()) {
//                iterateChainColors = chainColors.iterator();
//            }
//
//            // go to next chain
//            if ((colorChoice.equals("Chains") && lastChain != null && lastChain != atom.getChain()) ||
//                    (colorChoice.equals("Residue") && lastResidue != null && !lastResidue.equals(atom.getResidue())))
//            {
//                currIterColor = iterateChainColors.next();
//            }
//
//            Color color;
//            color = this.getColor(colorChoice, currIterColor, atomSphere);
//
//            sphere.setMaterial(new PhongMaterial(color));
//
//            lastChain = atom.getChain();
//            lastResidue = atom.getResidue();
//        }

        // show or remove corresponding legend
        switch (colorChoice) {
            case "Structure" -> changeLegend(controller, secStrucColors);
            case "Chains", "Residue" -> controller.getLegendPane().setVisible(false);
            default -> changeLegend(controller, atomColors);
        }

        // reset color choices, make coloring reproducible
        iterateChainColors = chainColors.iterator();
    }

    private Color getColor(String colorChoice, Map.Entry<Atom, Sphere> atomSphere) {

        Color color;
        switch (colorChoice) {
            case "Structure" -> color = secStrucColors.getOrDefault(atomSphere.getKey().getStructureType().toString(), Color.PLUM);
            case "Chains", "Residue" -> color = currIterColor;
            default -> color = atomColors.getOrDefault(atomSphere.getKey().getSimpleType(), Color.PLUM);
        }

        return color;
    }

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
            value.setX(value.getX() + recSize + spacer);
            value.setY(key.getY() + spacer * 3);
            value.setFont(Font.font("calibri light", FontWeight.EXTRA_LIGHT, FontPosture.REGULAR, 13));
            controller.getLegendPane().getChildren().add(key);
            controller.getLegendPane().getChildren().add(value);

            idx.getAndSet(idx.get() + 1);
        });
    }

    public void highlightChain(String chainChoice, String colorChoice) {

        for (var chain : modelToView.entrySet()) {
            for (var residues : chain.getValue().entrySet()) {
                for (var atomSphere : residues.getValue().entrySet()) {
                    var atom = atomSphere.getKey();
                    var sphere = atomSphere.getValue();
                    var color = getColor(colorChoice, atomSphere);

                    if (chain.getKey().getChainID() != chainChoice.charAt(0) && !chainChoice.equals("All")) {
                        sphere.setMaterial(new PhongMaterial(color.deriveColor(1, 0, 0.5, 0.5)));
                    } else {
                        sphere.setMaterial(new PhongMaterial(color));
                    }
                }
            }
        }
    }
}
