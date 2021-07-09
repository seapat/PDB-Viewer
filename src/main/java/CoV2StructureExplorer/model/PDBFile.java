package CoV2StructureExplorer.model;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class PDBFile {
    /*
    Right now, handles IO for pdb files, also serves as container for Protein structure and file content

    FIXME: might want to separate IO from Structure, could move file content to structure calls,
     everything else is redundant already
     */

    // FIXME: Error below shows up when clicking on "draw" before parsing a model, just catch the exception
    //  Exception in thread "JavaFX Application Thread" java.lang.NullPointerException: Cannot invoke "CoV2StructureExplorer.model.PDBFile.getProtein()" because "model" is null

    private final String pdbID;
    private final String content;
    private final Structure structure;
    private final String abstractText;

    public Structure getStructure() {
        return structure;
    }

    public HashMap<String, Map<String, Integer>> getAaComposition() {
        return aaComposition;
    }

    private final HashMap<String, Map<String, Integer>> aaComposition;
    private HashMap<Chain, String> chainSequences;

    private HashMap<Chain, String> parseSequences(){

        var sequenceMap = new HashMap<Chain, String>();

        for (var chain: this.structure.get(0)) {
            var sequence = new StringBuilder();
            for (var residue : chain) {
                    sequence.append(residue.getOneLetter());
            }
            sequenceMap.putIfAbsent(chain, sequence.toString());
        }
        return sequenceMap;
    }

    // get via code from pdb
    public PDBFile(String pdbID) {
        this.pdbID = pdbID;

        //FIXME: this method is protected instead of private to allow access from here, good idea?
        this.content = PDBWeb.getPDBString(this.pdbID);
        this.abstractText = PDBWeb.getAbstract(this.pdbID);

        var parser = new PDBParser(pdbID, this.content);
        this.structure = parser.getStructure();
        this.aaComposition = parser.getAaCompositionPerChain();

        this.chainSequences = parseSequences();
    }

    // load locally
    public PDBFile(Path path) {
        String filename = path.getFileName().toString();
        this.pdbID = filename.substring(0, filename.lastIndexOf('.'));
        this.content = getPDBString(path);
        var parser = new PDBParser(pdbID, this.content);
        this.structure = parser.getStructure();
        this.aaComposition = parser.getAaCompositionPerChain();
        this.abstractText = "Load via pdb code to see this";
    }

    private static String getPDBString(Path path) {
        try {
            return Files.readString(path); //Files.newInputStream(path); //
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("File not found at " + path);
            return "";//InputStream.nullInputStream();
        }
    }
    
    // FIXME: use this.content and have one less arguments to hand over?
    public void savePDBFile(Path path) {
        try {
            Files.writeString( Paths.get( path.toString() ,this.pdbID + ".pdb"), this.content.toUpperCase());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getPdbID() {
        return pdbID;
    }

    public  String getContent() {
        return content;
    }

    public Structure getProtein() {
        return this.structure;
    }

    public String getAbstractText() {
        return abstractText;
    }

    public HashMap<Chain, String> getChainSequences() {
        return chainSequences;
    }
}
