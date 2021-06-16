package CoV2StructureExplorer.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;

import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;

public class PDBParser {
    /*
    From The Biopython Structural Bioinformatics FAQ:
        The Structure object follows the so-called SMCRA (Structure/Model/Chain/Residue/Atom) architecture:
            - A structure consists of models
            - A CoV2StructureExplore.CoV2StructureExplorer.model consists of chains
            - A chain consists of residues
            - A residue consists of atoms

    This Class (and all corresponding ones) try to mimic this architecture.
    */

    private final Structure structure;
    private BufferedReader reader;
    private String currLine;

    PDBParser(String pdbFile) {

        structure = new Structure();

        try {
            reader = new BufferedReader(new StringReader(pdbFile));

            progressLine();

            while ( currLine != null && currLine.trim().length() > 0 ) {
                if (currLine.startsWith("ATOM")) {
                    structure.add(
                            parseModel()
                    );
                }
                progressLine();
            }

            reader.close();
        } catch (Exception e) {
            System.err.println("Error: Target File Cannot Be Read");
        }

    }

    private Model parseModel() {
        var model = new Model();

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

    private Chain parseChain() {
        var chain = new Chain(currLine.charAt(21));

        while ( currLine != null && currLine.trim().length() > 0 ) {

            if (currLine.startsWith("TER")) {
                return chain;
            }

            if (currLine.startsWith("ATOM")) {
                chain.add(
                        parseResidue()
                );
            }
            progressLine();

        }
        return chain;
    }



    private Residue parseResidue() { //BufferedReader reader, String firstLine


        int resID = parseInt(currLine.substring(23, 27).strip());
        String resType = currLine.substring(17, 20).strip();
        var residue = new Residue(resID, resType);

        //read ATOM lines
//        for (String line = null; null != (line = reader.readLine()); /*until no more line left*/) {
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

                var coords = new HashMap<Character, Double>(3);
                coords.put('x', parseDouble(currLine.substring(30, 38)));
                coords.put('y', parseDouble(currLine.substring(38, 46)));
                coords.put('z', parseDouble(currLine.substring(46, 55)));

                residue.add(new Atom(id,atomType, coords,chainID, residue));

                // TODO: remove if everything works
//                if (residue.get(idx-1).id == idx)
//                    System.err.println("Somethings fucked with the atom id's, PDBParser.java");
            }

            progressLine();
        }

        return residue;
    }

    private void progressLine(){
        try {
            currLine = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Structure getStructure() {
        return structure;
    }

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

idea: does .readline() automatically progress the file, ie advances reader to the next line on call?
    - could allow global progress through the pdb file
 */
}
