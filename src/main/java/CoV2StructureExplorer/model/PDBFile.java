package CoV2StructureExplorer.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class PDBFile {
    /*
    Right now, handles IO for pdb files, also serves as container for Protein structure and file content

    FIXME: might want to separate IO from Structure, could move file content to structure calls,
     everything else is redundant already
     */

    private final String pdbID;
    private final String content;
    private final Structure structure;

    // get via code from pdb
    public PDBFile(String pdbID) {
        this.pdbID = pdbID;

        //FIXME: this method is protected instead of private to allow access from here, good idea?
        this.content = PDBUrl.getPDBString(this.pdbID);

        this.structure = new PDBParser(pdbID, this.content).getStructure();
    }

    // load l ocally
    public PDBFile(Path path) {
        String filename = path.getFileName().toString();
        this.pdbID = filename.substring(0, filename.lastIndexOf('.'));
        this.content = getPDBString(path);
        this.structure = new PDBParser(pdbID,this.content).getStructure();
    }



    private static String getPDBString(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("File not found at " + path);
            return "";
        }
    }



    // FIXME: use this.content and have one less arguments to hand over?
    public void savePDBFile(Path path) {
        try {
            Files.writeString(Paths.get(path.toString(), this.pdbID + ".pdb" ), this.content);
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    public String getPdbID() {
        return pdbID;
    }

    public String getContent() {
        return content;
    }

    public Structure getProtein() {
        return this.structure;
    }

    //    private static String getPDBString(String pdbID) {
//        try {
//            var code = pdbID.toLowerCase();
//            var url = new URL("https://files.rcsb.org/download/" + code + ".pdb"); //"https://data.rcsb.org/rest/v1/core/polymer_entity/"+ code + "/1"
//            return new String(PDBUrl.getFromURL(url).readAllBytes());
//        } catch (IOException e) {
//            e.printStackTrace();
//            System.err.println("Program requires valid pdb code found at rcsb.org");
//            return "";
//        }
//    }

    // use to populate pdbCodeList based on content of entryField,
//    public static ObservableList<String> getPDBEntries(String query) {
//
//        if (query.isEmpty()) {
//            // Corona-related pdb codes if selection is empty, If you just want to see any file, these are enough
//            // You don't want to scroll through all codes if you are "just testing", else you have a code at hand
//            return FXCollections.observableArrayList(
//                    "6ZMO", "6ZOJ", "6ZPE", "6ZP5", "6ZP4", "6ZP7", "6ZOX", "6ZOW", "6ZOZ", "6ZOY", "6ZOK", "6ZON",
//                    "6ZP1", "6ZP0", "6ZP2", "5R84", "5R83", "5R7Y", "5R80", "5R82", "5R81", "5R8T", "5R7Z", "5REA", "5REC"
//            );
//        }
//        try {
//            var hits = PDBUrl.getPDBEntries().stream().filter(s -> ((String)s).startsWith(query.toUpperCase())).toList();
//            if (hits.isEmpty()) {
//                hits = new ArrayList<>() {{
//                    add("Nothing Found");
//                }};
//            }
//            return FXCollections.observableArrayList(hits);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return FXCollections.observableArrayList("Error", e.toString());
//        }
//    }

}
