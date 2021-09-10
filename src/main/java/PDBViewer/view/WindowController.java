package PDBViewer.view;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class WindowController {

    @FXML
    private ToggleButton chartTotalButton;
    @FXML
    private Button flipChartButton;
    @FXML
    private MenuItem undoMenu;
    @FXML
    private MenuItem redoMenu;
    @FXML
    private Pane legendPane;
    @FXML
    private VBox chartTab;
    @FXML
    private Pane selectionPane;
    @FXML
    private ToggleButton rotateProteinButton;
    @FXML
    private TextArea abstractContent;
    @FXML
    private CheckBox ribbonChecked;
    @FXML
    private CheckBox atomsChecked;
    @FXML
    private Button scaleChainsButton;
    @FXML
    private CheckBox bondsChecked;
    @FXML
    private MenuItem allSelectionMenu;
    @FXML
    private MenuItem copyImageMenuItem;
    @FXML
    private MenuItem clearSelectionMenu;
    @FXML
    private MenuItem darkThemeMenu;
    @FXML
    private ToggleButton explodeButton;
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
    @FXML
    private Label focusLabel;
    @FXML
    private ChoiceBox<String> focusChoice;

    public VBox getChartTab() {
        return chartTab;
    }

    public ToggleButton getChartTotalButton() {
        return chartTotalButton;
    }

    public Button getFlipChartButton() {
        return flipChartButton;
    }

    public ToggleButton getExplodeButton() {
        return explodeButton;
    }

    public MenuItem getDarkThemeMenu() {
        return darkThemeMenu;
    }

    public MenuItem getClearSelectionMenu() {
        return clearSelectionMenu;
    }

    public MenuItem getCopyImageMenuItem() {
        return copyImageMenuItem;
    }

    public MenuItem getAllSelectionMenu() {
        return allSelectionMenu;
    }

    public ChoiceBox<String> getFocusChoice() {
        return focusChoice;
    }

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

    public Pane getSelectionPane() {
        return selectionPane;
    }

    public Pane getLegendPane() {
        return legendPane;
    }

    public Label getFocusLabel() {
        return focusLabel;
    }

    public MenuItem getUndoMenu() {
        return undoMenu;
    }

    public MenuItem getRedoMenu() {
        return redoMenu;
    }

    public Button getScaleChainsButton() {
        return scaleChainsButton;
    }

    public ToggleButton getRotateProteinButton() {
        return rotateProteinButton;
    }
}
