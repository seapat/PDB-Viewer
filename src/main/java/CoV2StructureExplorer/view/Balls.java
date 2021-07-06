package CoV2StructureExplorer.view;

import CoV2StructureExplorer.model.Atom;
import CoV2StructureExplorer.model.Structure;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.util.Pair;

import java.util.*;

public class Balls extends Group {

    //sphere.setdrawMode(DrawMode.LINE) to see mesh used for drawing

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
            new AbstractMap.SimpleEntry<>("HELIX", Color.RED)
    );

    // create list of colors, iterator is renewed once the list ends
    private final static List<Color> chainColors = Collections.synchronizedList(new ArrayList<>(Arrays.asList(
            Color.AQUA, Color.DARKMAGENTA, Color.AQUAMARINE, Color.HOTPINK, Color.INDIANRED, Color.INDIGO,
            Color.MEDIUMPURPLE, Color.MEDIUMTURQUOISE, Color.MEDIUMVIOLETRED, Color.ORANGE, Color.YELLOW
    )));
    private static Iterator<Color> iterateChainColors = chainColors.iterator();

    public Balls(Structure pdb, ReadOnlyDoubleProperty radiusScale, Integer modelChoice, String colorChoice){

        createSpheres(pdb, radiusScale, modelChoice, colorChoice);

        System.out.println("balls: " + this.getChildren().size());
    }

    private void createSpheres (Structure pdb, ReadOnlyDoubleProperty radiusScale, Integer modelChoice, String colorChoice){

        for (var chain: pdb.get(modelChoice -1 )){
            if (!iterateChainColors.hasNext()) {
                iterateChainColors = chainColors.iterator();
            }

            for (var residue: chain) {

                for (var atom: residue) {
                    final Sphere sphere = new Sphere(100, 32);

                    sphere.setTranslateX(100 * atom.getPosition().x());
                    sphere.setTranslateY(100 * atom.getPosition().y());
                    sphere.setTranslateZ(100 * atom.getPosition().z());

                    sphere.radiusProperty().bind(radiusScale.multiply(atom.getRadius()));

                    changeColor(colorChoice);

                    // Add twice, once to scene graph, other time to the hashmap to access subsets of spheres
                    getChildren().add(sphere);
                    atomSpheres.add(new Pair<>(atom, sphere));
                }
            }
        }

    }

    public void changeColor(String colorChoice){

        Character lastChain = null;
        Color chainColor = iterateChainColors.next();
        for (var atomSphere : atomSpheres){
            var atom = atomSphere.getKey();
            var sphere = atomSphere.getValue();

            // reset colors if all used up
            if (!iterateChainColors.hasNext()) {
                iterateChainColors = chainColors.iterator();
            }

            // go to next chain
            if (lastChain != null &&  lastChain !=  atom.getChain()) {
                chainColor = iterateChainColors.next();
            }

            Color color;
            switch (colorChoice){
                case "Structure" -> color = secStrucColors.getOrDefault(atom.getStructureType().toString(), Color.PLUM);
                case "Chains" -> color = chainColor;
                default -> color = atomColors.getOrDefault(atom.getSimpleType(), Color.PLUM);
            }
            sphere.setMaterial(new PhongMaterial(color));

            lastChain = atom.getChain();
        }
        // reset color choices, make coloring reproducible
        iterateChainColors = chainColors.iterator();
    }

}
