import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        var view = new view.Window();
        var model = new model.PDBFile("6ZOJ");

        WindowPresenter.setup(primaryStage, view.getController(), model);

        primaryStage.setScene(new Scene(view.getRoot()));
        primaryStage.setTitle("CoV2Structure Explorer");
        primaryStage.show();

        //TEST
    }


    public static void main(String[] args) {
        launch(args);
    }
}
