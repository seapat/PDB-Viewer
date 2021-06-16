package CoV2StructureExplorer.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.json.Json;
import javax.json.JsonValue;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;


public class PDBFile {
    private final String pdbID;
    private String content;
    private Structure structure;
    private ArrayList<String> pdbEntries;

    // load url once instead of every function call
    {
        try {
            var url = new URL("https://data.rcsb.org/rest/v1/holdings/current/entry_ids");
            var reader = Json.createReader(getFromURL(url));
            pdbEntries = new ArrayList<String>(reader.readArray().stream()
                    .map(JsonValue::toString)
                    // strings are enclosed by "-signs twice because Json
                    .map(s -> s.replace("\"", ""))
                    .sorted()
                    .toList());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    // get via code from pdb
    public PDBFile(String pdbID) {
        this.pdbID = pdbID;
        this.content = getPDBString(this.pdbID);
        this.structure = new PDBParser(this.content).getStructure();
    }

    // load locally
    public PDBFile(Path path) {
        String filename = path.getFileName().toString();
        this.pdbID = filename.substring(0, filename.lastIndexOf('.'));
        this.content = getPDBString(path);
        this.structure = new PDBParser(this.content).getStructure();
    }

    // use to populate pdbCodeList based on content of entryField,
    public ObservableList<String> getPDBEntries(String query) {

        if (query.isEmpty()) {
            // Corona-related pdb codes if selection is empty, If you just want to see any file, these are enough
            // You don't want to scroll through all codes if you are "just testing", else you have a code at hand
            return FXCollections.observableArrayList(
                    "6ZMO", "6ZOJ", "6ZPE", "6ZP5", "6ZP4", "6ZP7", "6ZOX", "6ZOW", "6ZOZ", "6ZOY", "6ZOK", "6ZON",
                    "6ZP1", "6ZP0", "6ZP2", "5R84", "5R83", "5R7Y", "5R80", "5R82", "5R81", "5R8T", "5R7Z", "5REA", "5REC"
            );
        }
        try {
            var hits = pdbEntries.stream().filter(s -> ((String)s).startsWith(query.toUpperCase())).toList();
            if (hits.isEmpty()) {
                hits = new ArrayList<String>() {{
                    add("Nothing Found");
                }};
            }
            return FXCollections.observableArrayList(hits);
        } catch (Exception e) {
            e.printStackTrace();
            return FXCollections.observableArrayList("Error", e.toString());
        }
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

    private static String getPDBString(String pdbID) {
        try {
            var code = pdbID.toLowerCase();
            var url = new URL("https://files.rcsb.org/download/" + code + ".pdb"); //"https://data.rcsb.org/rest/v1/core/polymer_entity/"+ code + "/1"
            return new String(getFromURL(url).readAllBytes());
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Program requires valid pdb code found at rcsb.org");
            return "";
        }
    }

    public static InputStream getFromURL(URL url){

        try {
            var connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            return connection.getInputStream();
        }
        catch (IOException e) {
            e.printStackTrace();
            var errorString = """
                    Couldn't retrieve the PDB code.
                    There is probably no regular .pdb file available.
                    Or the code does not exist at all.
                   
                    Error Message:
                    """ + e;
            System.err.println("\n" + errorString);
            return new ByteArrayInputStream(errorString.getBytes(StandardCharsets.UTF_8));
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

    public void setContent(String code) {
        this.content = getPDBString(code);
    }

    public void setContent(Path path) {
        this.content = getPDBString(path);
    }

    public Structure getProtein() {
        return this.structure;
    }

}
