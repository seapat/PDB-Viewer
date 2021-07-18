package CoV2StructureExplorer;

import CoV2StructureExplorer.presenter.WindowPresenter;
import CoV2StructureExplorer.view.Window;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        var view = new Window();
        new WindowPresenter(primaryStage, view.getController());

        primaryStage.setScene(new Scene(view.getRoot()));
        primaryStage.setTitle("CoV2Structure Explorer");
        primaryStage.show();
    }
}
