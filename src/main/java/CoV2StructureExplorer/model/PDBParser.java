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

    private Structure structure;

    public Structure getStructure() {
        return structure;
    }

    // TODO: implement me
    PDBParser(String pdbFile) {

//        Model[] models = new Model[]{};
        structure = new Structure(new Model[]{}, 0);

    try{
        BufferedReader reader = new BufferedReader(new StringReader(pdbFile));

        for ( String line = null; null != (line = reader.readLine()); /*until no more line left*/) {
//           System.out.println(line);
        }

        reader.close();
    }
    catch(Exception e){
        System.err.println("Error: Target File Cannot Be Read");
    }


//        reader.readLine();
    }

    private static Model parseModel(BufferedReader reader, String firstLine){
        var model = new Model(new Chain[]{}, 0);

        return model;
    }

    private static Residue parseResidue(BufferedReader reader, String firstLine) throws IOException {


        var residue = new Residue();

        residue.setId(parseInt(firstLine.substring(22, 35)));

        //read ATOM lines
        for ( String line = null; null != (line = reader.readLine()); /*until no more line left*/) {
            if (line.startsWith("ATOM")){
                String atomType = "Test"; //line.substring(7, 11)
                var coords = new HashMap<Character, Double> (3);
                coords.put('x', parseDouble(line.substring(30, 37)));
                coords.put('y', parseDouble(line.substring(38, 45)));
                coords.put('z', parseDouble(line.substring(46, 55)));

                residue.getAtoms().put(parseInt(line.substring(7, 11)), new Atom(atomType, coords, line.charAt(26)));

                // TODO: remove if everything works
                if (residue.getAtoms().get(parseInt(line.substring(7, 11))).id == parseInt(line.substring(7, 11)))
                System.err.println("Somethings fucked with the atom id's, PDBParser.java");
            }
        }

        return residue;
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
