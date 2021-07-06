module CoV2StructureExplorer {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.json;

    // fxml file is located in resources now, needs access
    opens CoV2StructureExplorer.view to javafx.fxml;

    exports CoV2StructureExplorer.model;
    exports CoV2StructureExplorer.view;
    exports CoV2StructureExplorer;
//    opens CoV2StructureExplorer to javafx.fxml;
}