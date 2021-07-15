package CoV2StructureExplorer.model;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class PDBFile {

    // get via code from pdb
    public PDBFile(String pdbID) {
        this.pdbID = pdbID;
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
        this.content = PDBFile.getPDBString(path);
        var parser = new PDBParser(pdbID, this.content);
        this.structure = parser.getStructure();
        this.aaComposition = parser.getAaCompositionPerChain();
        this.abstractText = "Load via pdb code to see this";
        this.chainSequences = parseSequences();
    }

    private final String pdbID;
    private final String content;
    private final Structure structure;
    private final String abstractText;

    public static void savePDB(Stage stage, PDBFile model) {
        if (model == null) {
            var info = new Alert(Alert.AlertType.ERROR, "No pdb entry loaded!");
            info.showAndWait();
        } else {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Please choose saving location");
            File selectedDirectory = chooser.showDialog(stage);
            var path = selectedDirectory.toPath();

            // only save if user confirms chosen path
            var alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("");
            alert.setHeaderText("Do you want to save the file?");
            alert.setContentText("directory: " + path);
            alert.showAndWait()
                    .filter(response -> response == ButtonType.OK)
                    .ifPresent(response -> {
                        model.savePDBFile(path);
                        var info = new Alert(Alert.AlertType.INFORMATION, "Saved!");
                        info.showAndWait();
                    });
        }
    }

    public Structure getStructure() {
        return structure;
    }

    public HashMap<String, Map<String, Integer>> getAaComposition() {
        return aaComposition;
    }

    private final HashMap<String, Map<String, Integer>> aaComposition;
    private final HashMap<Chain, String> chainSequences;

    private HashMap<Chain, String> parseSequences(){

        var sequenceMap = new HashMap<Chain, String>();

        for (var chain: this.structure.get(0)) {
            var sequence = new StringBuilder();
            for (var residue : chain) {
                    sequence.append(residue.getOneLetter() == null ? residue.getThreeLetter() : residue.getOneLetter() );
            }
            sequenceMap.putIfAbsent(chain, sequence.toString());
        }
        return sequenceMap;
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
