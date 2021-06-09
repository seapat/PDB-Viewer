package model;

import javax.json.Json;
import javax.json.JsonString;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PDBFile {
    private final String id;
    private String content;
    private Structure structure;

    // get via code from pdb
    public PDBFile(String id) {
        this.id = id;
        this.content = getPDBString(this.id);
        this.structure = new PDBParser(this.content).getStructure();
    }

    // load locally
    public PDBFile(Path path) {
        String filename = path.getFileName().toString();
        this.id = filename.substring(0, filename.lastIndexOf('.'));
        this.content = getPDBString(path);
    }

    public static void showPDBEntries() throws MalformedURLException {
        var url = new URL("https://data.rcsb.org/rest/v1/holdings/current/entryids");
        try (var reader = Json.createReader(getFromURL(url))) {
            reader.readArray().stream().map(v -> ((JsonString) v).getString()).forEach(System.out::println);
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

            // FIXME: How to download a real pdb file???
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
        // TODO: handle Exception somehow in Presenter and show AlertWindow, check for null inputStream?
        catch (IOException e) {
            e.printStackTrace();
            System.err.println("\n Couldn't retrieve the PDB code...");
            return InputStream.nullInputStream();
        }
    }

    // FIXME: use this.content and have one less arguments to hand over?
    public void savePDBFile(Path path) {
        try {
            Files.writeString(Paths.get(path.toString(), this.id + ".pdb" ), this.content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getId() {
        return id;
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
