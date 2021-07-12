package CoV2StructureExplorer.model;

import javax.json.Json;
import javax.json.JsonValue;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class PDBWeb {
    /*
    Class that handles communication with rcsb.org, preferably everything is static/only exists once
     */

    // No instances allowed / needed
    private PDBWeb(){}

    private static final ArrayList<String> pdbEntries = fillEntryList();
    private static ArrayList<String> fillEntryList() {
        try {
            var url = new URL("https://data.rcsb.org/rest/v1/holdings/current/entry_ids");
            var reader = Json.createReader(getFromURL(url));
            return new ArrayList<>(reader.readArray().stream()
                    .map(JsonValue::toString)
                    // strings are enclosed by "-signs twice because Json
                    .map(s -> s.replace("\"", ""))
                    .sorted()
                    .toList());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }

    }

    // fetch pdb from url
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

    // use to populate pdbCodeList based on content of entryField
    public static ArrayList<String> getPDBEntries(String query) {

        if (query.isEmpty()) {
            // Corona-related pdb codes if selection is empty, If you just want to see any file, these are enough
            // You don't want to scroll through all codes if you are "just testing", else you have a code at hand
            return new ArrayList<>( // commented ones don't work, can't fetch from server
                    List.of(// "6ZMO",
                            "6ZOJ",//"6ZPE",
                            "6ZP5",//"6ZP4",
                            "6ZP7", "6ZOX", "6ZOW", "6ZOZ", "6ZOY", "6ZOK",//"6ZON",
                            "6ZP1", "6ZP0", "6ZP2", "5R84", "5R83", "5R7Y", "5R80", "5R82", "5R81",//"5R8T",
                            "5R7Z", "5REA", "5REC", "MULTIPLE MODELS:", "5jxv"));
        }
        try {
            var hits = pdbEntries.stream().filter(s -> s.startsWith(query.toUpperCase())).toList();
            if (hits.isEmpty()) {
                hits = new ArrayList<>() {{
                    add("Nothing Found");
                }};
            }
            return new ArrayList<>(hits);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>(List.of("Error", e.toString()));
        }
    }

    protected static String getPDBString(String pdbID) {
        try {
            var code = pdbID.toLowerCase();
            var url = new URL("https://files.rcsb.org/download/" + code + ".pdb"); //"https://data.rcsb.org/rest/v1/core/polymer_entity/"+ code + "/1"
            return new String(PDBWeb.getFromURL(url).readAllBytes());
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Program requires valid pdb code found at rcsb.org");
            return "";
        }
    }

    protected static String getAbstract(String pdbID) {
        try {
            var code = pdbID.toLowerCase();
            var url = new URL("https://data.rcsb.org/rest/v1/core/pubmed/" + code); //"https://data.rcsb.org/rest/v1/core/polymer_entity/"+ code + "/1"

            var content = Json.createReader(getFromURL(url)).read();
//            var content = reader.read();
            var text = content.getValue("/rcsb_pubmed_abstract_text").toString();
            var address = content.getValue("/rcsb_pubmed_affiliation_info").toString()
                    .replaceAll("([a-zA-Z0-9+._-]+@[a-zA-Z0-9._-]+\\.[a-zA-Z0-9_-]+)", "")
                    .replace("[", "").replace("]", "")
                    ;
            var doi = content.getValue("/rcsb_pubmed_doi").toString();

            return ("Abstract: \n" + text + "\n\nAddress: \t" + address  + "\nDOI: \t" + doi).replace("\"", "");

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("No Abstract Available");
            return "No Abstract Available";
        }
    }

    public static ArrayList<String> getPDBEntries() {
        return pdbEntries;
    }

}
