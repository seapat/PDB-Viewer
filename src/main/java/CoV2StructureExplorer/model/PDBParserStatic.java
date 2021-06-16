package CoV2StructureExplorer.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;

public class PDBParserStatic {
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

    private static BufferedReader reader;
    private static String currLine;

    private PDBParserStatic() {}

    public static Structure createStructure(String pdbID, String pdbFile) {

        // better safe than sorry
        reader = null;
        currLine = null;

        var structure = new Structure(pdbID);

        try {
            reader = new BufferedReader(new StringReader(pdbFile));

            progressLine();

            while ( currLine != null && currLine.trim().length() > 0 ) {
                if (currLine.startsWith("ATOM")) {
                    structure.add(
                            parseModel(structure)
                    );
                }
                progressLine();
            }

            reader.close();
        } catch (Exception e) {
            System.err.println("Error: Target File Cannot Be Read");
        }
        return structure;
    }

     private static Model parseModel(Structure structure) {
        var model = new Model(structure);

        while ( currLine != null && currLine.trim().length() > 0 ) {

            if (currLine.startsWith("ENDMDL")) {
                return model;
            }
            if (currLine.startsWith("ATOM")) {
                model.add(
                        parseChain()
                );
            }
            progressLine();
        }
        return model;
    }

     private static Chain parseChain() {
        var chain = new Chain(currLine.charAt(21));

        while ( currLine != null && currLine.trim().length() > 0 ) {

            if (currLine.startsWith("TER")) {
                return chain;
            }

            if (currLine.startsWith("ATOM")) {
                chain.add(
                        parseResidue(chain)
                );
            }
            progressLine();

        }
        return chain;
    }



     private static Residue parseResidue(Chain chain) {


        int resID = parseInt(currLine.substring(23, 27).strip());
        String resType = currLine.substring(17, 20).strip();
        var residue = new Residue(resID, resType, chain);

        // parse ATOM lines to Atom objects
        while ( currLine != null && currLine.trim().length() > 0 ) {

            if (!resType.equals(currLine.substring(17, 20).strip())) {
                return residue;
            }else {
                resType = currLine.substring(17,20).strip();

            }

            if (currLine.startsWith("ATOM")) {
                String atomType = currLine.substring(12, 16).strip();
                int id = parseInt(currLine.substring(6, 11).strip());
                char chainID = currLine.charAt(21);

                var position = new Atom.Position(
                        parseDouble(currLine.substring(30, 38)),
                        parseDouble(currLine.substring(38, 46)),
                        parseDouble(currLine.substring(46, 55))
                );

                residue.add(new Atom(id,atomType,chainID, residue, position));
            }

            progressLine();
        }
        return residue;
    }


     private static void progressLine(){
        /*
        central Place to manage Exception caused by reader
         */
        try {
            currLine = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//     public static Structure getStructure() {
//        return structure;
//    }

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
