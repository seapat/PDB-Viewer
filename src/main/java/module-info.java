module CoV2StructureExplorer {
    requires javafx.controls;
    requires javafx.fxml;

    // parse pdb entries
//    requires biojava.core;
//    requires biojava.structure;

    //javax.json
    requires java.json;

    // fxml file is located in resources now, needs access
    opens CoV2StructureExplorer.view to javafx.fxml;

    exports CoV2StructureExplorer;
}