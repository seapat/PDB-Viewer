package view;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class WindowController {

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
    private Button searchButton;

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
}
