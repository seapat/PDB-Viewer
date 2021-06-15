package CoV2StructureExplorer;

import CoV2StructureExplorer.model.PDBFile;
import CoV2StructureExplorer.view.Window;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;



public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        var view = new Window();
        var model = new PDBFile("3NIR"); //TODO: make this not required to parse at start

        WindowPresenter.setup(primaryStage, view.getController(), model);

        primaryStage.setScene(new Scene(view.getRoot()));
        primaryStage.setTitle("CoV2Structure Explorer");
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
