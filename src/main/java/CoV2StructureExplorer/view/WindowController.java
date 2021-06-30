package CoV2StructureExplorer.view;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;

public class WindowController {


    @FXML
    private Label modelLabel;

    @FXML
    private Button drawButton;

    @FXML
    private ChoiceBox<Integer> modelChoice;

    @FXML
    private ChoiceBox<String> viewChoice;

    @FXML
    private Button clearSearchButton;

    @FXML
    private Slider radiusScale;

    @FXML
    private Slider diameterScale;

    @FXML
    private Label infoLabel;

    @FXML
    private Pane centerPane;

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

    public void setPdbCodeList(ListView<String> pdbCodeList) {
        this.pdbCodeList = pdbCodeList;
    }

    public MenuItem getOpenMenu() {
        return openMenu;
    }

    public void setOpenMenu(MenuItem openMenu) {
        this.openMenu = openMenu;
    }

    public MenuItem getSaveMenu() {
        return saveMenu;
    }

    public void setSaveMenu(MenuItem saveMenu) {
        this.saveMenu = saveMenu;
    }

    public MenuItem getClearMenu() {
        return clearMenu;
    }

    public void setClearMenu(MenuItem clearMenu) {
        this.clearMenu = clearMenu;
    }

    public MenuItem getExitMenu() {
        return exitMenu;
    }

    public void setExitMenu(MenuItem closeMenu) {
        this.exitMenu = closeMenu;
    }

    public MenuItem getAboutMenu() {
        return aboutMenu;
    }

    public void setAboutMenu(MenuItem aboutMenu) {
        this.aboutMenu = aboutMenu;
    }

    public TextField getEntryField() {
        return entryField;
    }

    public void setEntryField(TextField entryField) {
        this.entryField = entryField;
    }

    public Button getParseButton() {
        return parseButton;
    }

    public void setParseButton(Button parseButton) {
        this.parseButton = parseButton;
    }

    public ListView<String> getPdbText() {
        return pdbText;
    }

    public void setPdbText(ListView<String> pdbText) {
        this.pdbText = pdbText;
    }

    public Label getInfoLabel() {
        return infoLabel;
    }

    public void setInfoLabel(Label infoLabel) {
        this.infoLabel = infoLabel;
    }

    public Pane getCenterPane() {
        return centerPane;
    }

    public void setCenterPane(Pane centerPane) {
        this.centerPane = centerPane;
    }

    public Slider getRadiusScale() {
        return radiusScale;
    }

    public void setRadiusScale(Slider radiusScale) {
        this.radiusScale = radiusScale;
    }

    public Button getClearSearchButton() {
        return clearSearchButton;
    }

    public void setClearSearchButton(Button clearSearchButton) {
        this.clearSearchButton = clearSearchButton;
    }

    public ChoiceBox<String> getViewChoice() {
        return viewChoice;
    }

    public void setViewChoice(ChoiceBox<String> viewChoice) {
        this.viewChoice = viewChoice;
    }

    public ChoiceBox<Integer> getModelChoice() {
        return modelChoice;
    }

    public void setModelChoice(ChoiceBox<Integer> modelChoice) {
        this.modelChoice = modelChoice;
    }

    public Button getDrawButton() {
        return drawButton;
    }

    public void setDrawButton(Button drawButton) {
        this.drawButton = drawButton;
    }

    public Label getModelLabel() {
        return modelLabel;
    }

    public void setModelLabel(Label modelLabel) {
        this.modelLabel = modelLabel;
    }

    public Slider getDiameterScale() {
        return diameterScale;
    }

    public void setDiameterScale(Slider diameterScale) {
        this.diameterScale = diameterScale;
    }
}
