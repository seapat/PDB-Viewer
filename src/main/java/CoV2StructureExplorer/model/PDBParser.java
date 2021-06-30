package CoV2StructureExplorer.model;

import java.io.*;
import java.util.ArrayList;

import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;

public class PDBParser {

    static final double BOND_TOLERANCE = 1.1;
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

    /*TODO: perhaps make this static? see pdbURL on how to create structure without instantiation

        // private constructor
        private PDBParserStatic() {}

        // start of new method, replaces current constructor
        public static Structure createStructure(String pdbID, String pdbFileContent) {

            // better safe than sorry
            reader = null;
            currLine = null;

            var structure = new Structure(pdbID);
            ...
     */

    // TODO: add residues, chain etc. to class scope to be able to change them from other methods
    //  (e.g. add stats (count, avg ...) or add coord of CA/midpoint to residues

    //TODO: next to progressLine() method: method to count atoms etc for statistics MAYBE USE MASTER RECORD

    // TODO: übrigens: falls ihr einen distance threshold haben wollt, der bisschen mehr sophisticated ist als einfach 2 anzunehmen,
    //  ich habe meinen nach ner funktion von PerlMol definiert:
    //  double distanceThreshold = Math.max(atom1.getRadius(), atom2.getRadius())*2*TOLERANCE; (die radien sind die covalent radius werte von wikipedia)

    private final Structure structure;
    private BufferedReader reader;
    private String currLine;
    private final ArrayList<Helix> helices = new ArrayList<>();
    private final ArrayList<Sheet> sheets = new ArrayList<>();

    PDBParser(String pdbID, String pdbFile) {

        structure = new Structure(pdbID);
        int modelID = 0;

        try {
            reader = new BufferedReader(new StringReader(pdbFile));

            progressLine();

            while ( currLine != null && currLine.trim().length() > 0 ) {

                if (currLine.startsWith("HELIX")) {
                    helices.add(new Helix(
                            parseInt(currLine.substring(7, 10).strip()),
                            parseInt(currLine.substring(21, 25).strip()),
                            parseInt(currLine.substring(33, 37).strip()),
                            currLine.charAt(19)
                    ));
                }
                if (currLine.startsWith("SHEET")) {
                    sheets.add(new Sheet(
                            parseInt(currLine.substring(7, 10).strip()),
                            currLine.substring(11, 14).strip(),
                            parseInt(currLine.substring(22, 26).strip()),
                            parseInt(currLine.substring(33, 37).strip()),
                            currLine.charAt(21)
                    ));
                }

                if (currLine.startsWith("ATOM")) {
                    structure.add(parseModel(structure, modelID++));
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

    private Model parseModel(Structure structure, int modelID) {


        var model = new Model(structure, modelID);

        while ( currLine != null && currLine.trim().length() > 0 ) {

            if (currLine.startsWith("ENDMDL") ) { // || currLine.!startsWith("ATOM")
//                progressLine();
                return model;
            }

            if (currLine.startsWith("ATOM")) {
                model.add(parseChain(model));
            }
            progressLine();
        }
        return model;
    }

    private Chain parseChain(Model model) {
        var chain = new Chain(currLine.charAt(21), model);

        while ( currLine != null && currLine.trim().length() > 0 ) {

            if (currLine.startsWith("ATOM")) {
                chain.add(parseResidue(chain));
                //continue; //This line is IMPORTANT, we don't want to progress the line after adding a residue
            } else {
                progressLine();
            }

            if (currLine.startsWith("TER")) { //currLine.startsWith("ENDMDL") ||
                return chain;
            }

        }
        return chain;
    }

    private Residue parseResidue(Chain chain) {

        int resID = parseInt(currLine.substring(22, 26).strip());
        String resType = currLine.substring(17, 20).strip();
        var residue = new Residue(resID, resType, chain);

        // parse ATOM lines to Atom objects
        while ( currLine != null && currLine.trim().length() > 0 ) {

            if (currLine.startsWith("TER") || !(resID == parseInt(currLine.substring(22, 26).strip()))) {
                return residue;
            }

            if (currLine.startsWith("ATOM")) {
                String complexType = currLine.substring(12, 16).strip();
                char simpleType = currLine.charAt(77);
                int id = parseInt(currLine.substring(6, 11).strip());
                char chainID = currLine.charAt(21);


                var position = new Atom.Position(
                        parseDouble(currLine.substring(30, 38)),
                        parseDouble(currLine.substring(38, 46)),
                        parseDouble(currLine.substring(46, 55))
                );

                residue.add(new Atom(id,complexType, simpleType,chainID, residue, position));
            }


            // COIL is default for all residues
            for (var helix : helices){
                if (residue.getChain().getChainID() == helix.chain() && residue.getId() >= helix.start() && residue.getId() <= helix.end()){
                    residue.setStructure(StructureType.HELIX);
                    residue.forEach(atom -> atom.setStructureType(StructureType.HELIX));
                    break;
                }
            }
            // only enter if not already helix
            if (residue.getStructureType() != StructureType.HELIX){
                for (var sheet : sheets){
                    if (residue.getChain().getChainID() == sheet.chain() && residue.getId() >= sheet.start() && residue.getId() <= sheet.end()){
                        residue.setStructure(StructureType.SHEET);
                        residue.forEach(atom -> atom.setStructureType(StructureType.SHEET));
                        break;
                    }
                }

            }

            progressLine();
        }
        return residue;
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

    private void createBondsHelper(Residue residue1, Residue residue2){
        for (var atom1 : residue1) {
            for (var atom2 : residue2) {
                // FIXME: distance does not work for some cases (eg in 6ZOJ) -> flaoting residues
                double distanceThreshold = ((double)atom1.getRadius() / 100 + (double)atom2.getRadius() / 100 ) * BOND_TOLERANCE;
                if (!atom1.equals(atom2) && calcDistance(atom1, atom2) < distanceThreshold && !atom1.equals(atom2)){
                    atom1.addBond(atom2);
                }
            }
        }
    }

    private double calcDistance(Atom atom1, Atom atom2){
        var coordsAtom1 = atom1.getPosition();
        var coordsAtom2 = atom2.getPosition();
        return Math.sqrt(Math.pow(coordsAtom1.x()  - coordsAtom2.x(), 2)
                        + Math.pow(coordsAtom1.y()  - coordsAtom2.y(), 2)
                        + Math.pow(coordsAtom1.z()  - coordsAtom2.z(), 2)
        );
    }

    private void progressLine(){
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

    record Helix (int id, int start, int end, char chain){}

    record Sheet (int counter, String id, int start, int end, char chain){}

/* PDB format
- 1 file == 1 Structure
- MODEL record separates atoms of different models (coords of models differ)
    - not present when file only contains one CoV2StructureExplore.CoV2StructureExplorer.model !!!
    - if line.startswith("MODEL"): append current CoV2StructureExplore.CoV2StructureExplorer.model and create new one
    - ENDMDL denotes end of a CoV2StructureExplore.CoV2StructureExplorer.model
- TER records denotes <END> of a chain
    - if line.startswith("TER") return chain;
- ATOM denotes single Atom of a residue
    - 13 - 16 denotes Atom code
    - 7-11 denotes id
    - 18-20 corresponding residue
    - 31-38 x coordinate
    - 39-46 y coord
    - 47-54 z coord
    - 27 contains chain id
- Residues are runs of ATOMS with same 3-letter Residue code
    - set type to the one of first atom
    - ATOM 23 - 26 contains id of residue
    - terminate once residue of ATOM changes (look ahead somehow without skipping the line!!!)
- MASTER denotes prev-to-last line of file
    - MASTER contains info about how many records are in there
    - use to check if everything went smoothly
- END denotes last line of file
 */
}
