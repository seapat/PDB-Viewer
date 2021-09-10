module CoV2StructureExplorer {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.json;

    // fxml file is located in resources now, needs access
    opens PDBViewer.view to javafx.fxml;

    exports PDBViewer.model;
    exports PDBViewer.view;
    exports PDBViewer.selection;
    exports PDBViewer.presenter;
    exports PDBViewer;
}