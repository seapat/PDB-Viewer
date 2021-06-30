package CoV2StructureExplorer.model;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


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

    // get via code from pdb
    public PDBFile(String pdbID) {
        this.pdbID = pdbID;

        //FIXME: this method is protected instead of private to allow access from here, good idea?
        this.content = PDBWeb.getPDBString(this.pdbID);

        this.structure = new PDBParser(pdbID, this.content).getStructure();
    }

    // load locally
    public PDBFile(Path path) {
        String filename = path.getFileName().toString();
        this.pdbID = filename.substring(0, filename.lastIndexOf('.'));
        this.content = getPDBString(path);
        this.structure = new PDBParser(pdbID,this.content).getStructure();
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
            Files.writeString( Paths.get( path.toString() ,this.pdbID + ".pdb"), this.content);
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

}
