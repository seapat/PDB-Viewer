package CoV2StructureExplorer.presenter;

import CoV2StructureExplorer.model.PDBFile;
import CoV2StructureExplorer.view.AminoAcidComposition;
import CoV2StructureExplorer.view.WindowController;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.HashMap;
import java.util.Map;

public class ChartPresenter {

    public static void setupChartTab(PDBFile model, WindowController controller) {

        HashMap<String, Map<String, Integer>> data = model.getAaComposition();

        var dataFlipped = new SimpleBooleanProperty(controller.getFlipChartButton(), "data flipped", true);
        controller.getChartTab().getChildren().add(AminoAcidComposition.setupStackedBarChart(
                data, controller, dataFlipped.getValue()));

        controller.getFlipChartButton().setOnAction(e -> {
            dataFlipped.setValue(!dataFlipped.getValue());
            controller.getChartTab().getChildren().set(1, AminoAcidComposition.setupStackedBarChart(
                    data, controller, dataFlipped.getValue()));
        });

        controller.getChartTotalButton().setOnAction(e -> controller.getChartTab().getChildren()
                .set(1, AminoAcidComposition.setupStackedBarChart(
                data, controller, dataFlipped.getValue())));
    }

}
