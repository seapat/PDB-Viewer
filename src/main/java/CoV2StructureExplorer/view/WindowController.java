package CoV2StructureExplorer.view;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;

public class WindowController {

    @FXML
    private Pane selectionPane;

    @FXML
    private Button loadModel;

    @FXML
    private TextArea abstractContent;

    @FXML
    private CheckBox ribbonChecked;

    @FXML
    private CheckBox atomsChecked;

    @FXML
    private CheckBox bondsChecked;

    @FXML
    private ChoiceBox<String> colorChoice;

    @FXML
    private Label modelLabel;

    @FXML
    private ChoiceBox<Integer> modelChoice;

    @FXML
    private Button clearSearchButton;

    @FXML
    private Slider radiusScale;

    @FXML
    private Slider diameterScale;

    @FXML
    private Label infoLabel;

    @FXML
    private Pane figurePane;

    @FXML
    private ListView<String> pdbCodeList;

    @FXML
    private MenuItem openMenu;

    @FXML
    private MenuItem saveMenu;

    @FXML
    private MenuItem clearMenu;

    @FXML
    private MenuItem exitMenu;

    @FXML
    private MenuItem aboutMenu;

    @FXML
    private ListView<String> pdbText;

    @FXML
    private TextField entryField;

    @FXML
    private Button parseButton;

    public ListView<String> getPdbCodeList() {
        return pdbCodeList;
    }

    public MenuItem getOpenMenu() {
        return openMenu;
    }

    public MenuItem getSaveMenu() {
        return saveMenu;
    }

    public MenuItem getClearMenu() {
        return clearMenu;
    }

    public MenuItem getExitMenu() {
        return exitMenu;
    }

    public MenuItem getAboutMenu() {
        return aboutMenu;
    }

    public TextField getEntryField() {
        return entryField;
    }

    public Button getParseButton() {
        return parseButton;
    }

    public ListView<String> getPdbText() {
        return pdbText;
    }

    public Label getInfoLabel() {
        return infoLabel;
    }

    public Pane getFigurePane() {
        return figurePane;
    }

    public Slider getRadiusScale() {
        return radiusScale;
    }

    public Button getClearSearchButton() {
        return clearSearchButton;
    }

    public ChoiceBox<Integer> getModelChoice() {
        return modelChoice;
    }

    public Label getModelLabel() {
        return modelLabel;
    }

    public Slider getDiameterScale() {
        return diameterScale;
    }

    public ChoiceBox<String> getColorChoice() {
        return colorChoice;
    }

    public CheckBox getAtomsChecked() {
        return atomsChecked;
    }

    public CheckBox getBondsChecked() {
        return bondsChecked;
    }

    public CheckBox getRibbonChecked() {
        return ribbonChecked;
    }

    public TextArea getAbstractContent() {
        return abstractContent;
    }

    public Button getLoadModel() {
        return loadModel;
    }

    public Pane getSelectionPane() {
        return selectionPane;
    }
}
