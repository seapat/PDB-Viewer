package CoV2StructureExplorer.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;

public class PDBParser {
    /*
    Reads the content of .pdb files.
    A single reader object is created for the instance of the class, lines are progressed from various methods.

    From The Biopython Structural Bioinformatics FAQ:
        The Structure object follows the so-called SMCRA (Structure/Model/Chain/Residue/Atom) architecture:
            - A structure consists of models
            - A CoV2StructureExplore.CoV2StructureExplorer.model consists of chains
            - A chain consists of residues
            - A residue consists of atoms

    This Class (and all corresponding ones) try to mimic this architecture.
    */

    static final double BOND_TOLERANCE = 1.1;

    private final Structure structure;
    private final ArrayList<Helix> helices = new ArrayList<>();
    private final ArrayList<Sheet> sheets = new ArrayList<>();
    private final HashMap<String, Map<String, Integer>> aaCompositionPerChain;
    private BufferedReader reader;
    private String currLine;

    PDBParser(String pdbID, String pdbFile) {

        this.aaCompositionPerChain = new HashMap<>();
        this.aaCompositionPerChain.put("Total", new HashMap<>());
        this.structure = new Structure(pdbID);
        int modelID = 0;

        try {
            reader = new BufferedReader(new StringReader(pdbFile));
            progressLine();

            while (currLine != null && currLine.trim().length() > 0) {

                if (currLine.startsWith("HELIX")) {
                    this.helices.add(new Helix(
                            parseInt(currLine.substring(7, 10).strip()),
                            parseInt(currLine.substring(21, 25).strip()),
                            parseInt(currLine.substring(33, 37).strip()),
                            currLine.charAt(19)
                    ));
                }
                if (currLine.startsWith("SHEET")) {
                    this.sheets.add(new Sheet(
                            parseInt(currLine.substring(7, 10).strip()),
                            currLine.substring(11, 14).strip(),
                            parseInt(currLine.substring(22, 26).strip()),
                            parseInt(currLine.substring(33, 37).strip()),
                            currLine.charAt(21)
                    ));
                }
                if (currLine.startsWith("ATOM")) {
                    var model = parseModel(structure, modelID++);
                    model.setCoordExtrema();
                    structure.add(model);
                }
                progressLine();
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error: Target File Cannot Be Read");
        }
        createBonds();
    }

    private static void createBondsHelper(Residue residue1, Residue residue2) {
        for (var atom1 : residue1.values()) {
            for (var atom2 : residue2.values()) {
                double distanceThreshold = (atom1.getRadius() + atom2.getRadius()) * BOND_TOLERANCE;
                if (!atom1.equals(atom2) && calcDistance(atom1, atom2) < distanceThreshold && !atom1.equals(atom2)) {
                    atom1.addBond(atom2);
                }
            }
        }
    }

    private static double calcDistance(Atom atom1, Atom atom2) {
        var coordsAtom1 = atom1.getPosition();
        var coordsAtom2 = atom2.getPosition();
        return Math.sqrt(Math.pow(coordsAtom1.x() - coordsAtom2.x(), 2)
                + Math.pow(coordsAtom1.y() - coordsAtom2.y(), 2)
                + Math.pow(coordsAtom1.z() - coordsAtom2.z(), 2)
        );
    }

    public HashMap<String, Map<String, Integer>> getAaCompositionPerChain() {
        return aaCompositionPerChain;
    }

    private Model parseModel(Structure structure, int modelID) {

        var model = new Model(structure, modelID);

        while (currLine != null && currLine.trim().length() > 0) {

            if (currLine.startsWith("ENDMDL")) {
//                progressLine();
                return model;
            }
            if (currLine.startsWith("ATOM")) {
                var chain = parseChain(model);
                chain.setExtrema();
                model.add(chain);
            }
            progressLine();
        }
        return model;
    }

    private Chain parseChain(Model model) {
        var chain = new Chain(currLine.charAt(21), model);
        aaCompositionPerChain.putIfAbsent(String.valueOf(chain.getChainID()), new HashMap<>());

        while (currLine != null && currLine.trim().length() > 0) {

            if (currLine.startsWith("ATOM")) {
                var residue = parseResidue(chain);
                residue.setCoordExtrema();
                chain.add(residue);
            } else {
                progressLine();
            }
            if (currLine.startsWith("TER")) {
                return chain;
            }
        }
        return chain;
    }

    private Residue parseResidue(Chain chain) {

        // some amino acids have multiple versions, the first char is used to differentiate them.
        char res_version = currLine.charAt(16);

        int resID = parseInt(currLine.substring(22, 26).strip());
        String resType = currLine.substring(17, 20).strip();
        var residue = new Residue(resID, resType, chain);

        // parse ATOM lines to Atom objects
        while (currLine != null && currLine.trim().length() > 0) {

            if (currLine.startsWith("TER") || !(resID == parseInt(currLine.substring(22, 26).strip()))) {
                if (chain.getModel().getId() == 1) {
                    aaCompositionPerChain.get(String.valueOf(chain.getChainID()))
                            .computeIfPresent(residue.getThreeLetter(), (k, v) -> v + 1);
                    aaCompositionPerChain.get(String.valueOf(chain.getChainID()))
                            .putIfAbsent(residue.getThreeLetter(), 1);
                    aaCompositionPerChain.get("Total")
                            .computeIfPresent(residue.getThreeLetter(), (k, v) -> v + 1);
                    aaCompositionPerChain.get("Total")
                            .putIfAbsent(residue.getThreeLetter(), 1);
                }
                attachSecStructure(residue);
                return residue;
            }

            if (currLine.startsWith("ATOM")) {
                if (!(currLine.charAt(16) == ' ') || !(currLine.charAt(16) == res_version)) {
                    progressLine();
                }
                var atom = parseAtom(residue);
                residue.putIfAbsent(atom.getComplexType(), atom);
            }

            progressLine();
        }
        attachSecStructure(residue);
        return residue;
    }

    private Atom parseAtom(Residue residue) {

        String complexType = currLine.substring(12, 16).strip();
        char simpleType = currLine.charAt(77);
        int id = parseInt(currLine.substring(6, 11).strip());

        var position = new Atom.Position(
                parseDouble(currLine.substring(30, 38)),
                parseDouble(currLine.substring(38, 46)),
                parseDouble(currLine.substring(46, 55))
        );
        return new Atom(id, complexType, simpleType, residue, position);
    }

    private void attachSecStructure(Residue residue) {
        for (var helix : helices) {
            if (residue.getChain().getChainID() == helix.chain() && residue.getId() >= helix.start() && residue.getId() <= helix.end()) {
                residue.setStructure(StructureType.HELIX);
                residue.values().forEach(atom -> atom.setStructureType(StructureType.HELIX));
                break;
            }
        }
        if (residue.getStructureType() != StructureType.HELIX) {
            for (var sheet : sheets) {
                if (residue.getChain().getChainID() == sheet.chain() && residue.getId() >= sheet.start() && residue.getId() <= sheet.end()) {
                    residue.setStructure(StructureType.SHEET);
                    residue.values().forEach(atom -> atom.setStructureType(StructureType.SHEET));
                    break;
                }
            }
        }
        if (residue.getStructureType() == StructureType.COIL && residue.getOneLetter() == null) {
            residue.setStructure(StructureType.NUCLEOTIDE);
            residue.values().forEach(atom -> atom.setStructureType(StructureType.NUCLEOTIDE));
        }
    }

    private void createBonds() {
        Residue prevResidue = null;

        for (var model : structure) {
            for (var chain : model) {
                for (var residue : chain) {

                    // add bonds WITHIN residues
                    createBondsHelper(residue, residue);
                    // add bonds BETWEEN residues
                    if (prevResidue != null && !residue.equals(chain.get(0))) {
                        createBondsHelper(residue, prevResidue);
                    }
                    prevResidue = residue;
                }
            }
        }
    }

    private void progressLine() {
        /*
        central Place to manage Exception caused by reader
         */
        try {
            currLine = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Structure getStructure() {
        return structure;
    }

    record Helix(int id, int start, int end, char chain) {
    }

    record Sheet(int counter, String id, int start, int end, char chain) {
    }

}
