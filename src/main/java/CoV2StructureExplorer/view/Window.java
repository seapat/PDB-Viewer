package CoV2StructureExplorer.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;
import java.util.Objects;

public class Window {
    private final WindowController controller;
    private final Parent root;

    public Window() throws IOException {
        try (var ins = Objects.requireNonNull(getClass().getResource("/Window.fxml")).openStream()) {
            var fxmlLoader = new FXMLLoader();
            root = fxmlLoader.load(ins);
            controller = fxmlLoader.getController();

            // setup ChoiceBox; this is here because it never changes, could be in fxml as well
            controller.getColorChoice().getItems().addAll("Atoms", "Structure", "Chains", "Residue");
            controller.getColorChoice().setValue("Atoms");
        }
    }

    public WindowController getController() {
        return controller;
    }

    public Parent getRoot() {
        return root;
    }
}
