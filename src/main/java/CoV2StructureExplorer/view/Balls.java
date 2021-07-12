package CoV2StructureExplorer.view;

import CoV2StructureExplorer.selection.SetSelectionModel;
import CoV2StructureExplorer.model.Atom;
import CoV2StructureExplorer.model.Residue;
import CoV2StructureExplorer.model.Structure;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.util.Pair;

import java.util.*;

public class Balls extends Group {

    // TODO: Anchorpane to set up legend for colors

    //sphere.setDrawMode(DrawMode.LINE) to see mesh used for drawing

    public ArrayList<Pair<Atom, Sphere>> getAtomSpheres() {
        return atomSpheres;
    }

    public HashMap<Residue, ArrayList<Sphere>> getResidueSpheres() {
        return residueSpheres;
    }

    private final HashMap<Residue, ArrayList<Sphere>> residueSpheres = new HashMap<>();
    private final ArrayList<Pair<Atom, Sphere>> atomSpheres = new ArrayList<>();
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

    public Balls(Structure pdb, ReadOnlyDoubleProperty radiusScale, Integer modelChoice, String colorChoice, SetSelectionModel<Atom> selectedAtoms){

        for (var chain: pdb.get(modelChoice -1 )){
            if (!iterateChainColors.hasNext()) {
                iterateChainColors = chainColors.iterator();
            }

            for (var residue: chain) {
                var spheres = new ArrayList<Sphere>();
                for (var atom: residue.values()) {
                    final Sphere sphere = new Sphere(100, 32);

                    sphere.setTranslateX((int)(100 * atom.getPosition().x()));
                    sphere.setTranslateY((int)(100 * atom.getPosition().y()));
                    sphere.setTranslateZ((int)(100 * atom.getPosition().z()));

                    sphere.radiusProperty().bind(radiusScale.multiply(atom.getRadius()));

                    // SELECTION
                    sphere.setOnMouseClicked(e -> {
                        if (!e.isShiftDown()) { selectedAtoms.clearSelection(); }
                        residue.values().forEach(selectedAtoms::select);
                        System.out.println("clicking is recognized");
                        System.out.println(selectedAtoms.getSelectedItems());
                    });

                    // Add twice, once to Group, other time to the hashmap to access subsets of spheres
                    this.getChildren().add(sphere);
                    atomSpheres.add(new Pair<>(atom, sphere));
                    spheres.add(sphere);
                }
                //System.out.println(spheres);
                residueSpheres.putIfAbsent(residue, spheres);
            }
        }
//        this.changeColor(colorChoice);

        System.out.println("balls: " + this.getChildren().size());
    }

    public void changeColor(String colorChoice, WindowController controller){

        Character lastChain = null;
        Residue lastResidue = null;
        Color currIterColor = iterateChainColors.next();
        for (var atomSphere : atomSpheres){
            var atom = atomSphere.getKey();
            var sphere = atomSphere.getValue();

            // reset colors if all used up
            if (!iterateChainColors.hasNext()) {
                iterateChainColors = chainColors.iterator();
            }

            // go to next chain
            if ((colorChoice.equals("Chains") && lastChain != null && lastChain != atom.getChain()) ||
                    (colorChoice.equals("Residue") && lastResidue != null && !lastResidue.equals(atom.getResidue()))) {
                currIterColor = iterateChainColors.next();
            }

            Color color;
            switch (colorChoice){
                case "Structure" -> {
                    color = secStrucColors.getOrDefault(atom.getStructureType().toString(), Color.PLUM);
                }
                case "Chains", "Residue" -> color = currIterColor;
                default -> {
                    color = atomColors.getOrDefault(atom.getSimpleType(), Color.PLUM);

                }
            }
            sphere.setMaterial(new PhongMaterial(color));

            lastChain = atom.getChain();
            lastResidue = atom.getResidue();
        }
        // reset color choices, make coloring reproducible
        iterateChainColors = chainColors.iterator();
    }
}
