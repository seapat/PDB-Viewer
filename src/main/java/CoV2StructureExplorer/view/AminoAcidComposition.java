package CoV2StructureExplorer.view;

import CoV2StructureExplorer.model.PDBFile;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class AminoAcidComposition {

    // HORIZONTAL STACKED AR CHART ... MAYBE BUBBLE CHART?

    // get data structure from ChartPresenter
    // loop over data structure: each chain one bar, each chains value: one series
    // NOTE: sort series by size

    //TODO draw single barchart instead of stacked if only one chain

    public static Node setup(PDBFile model, WindowController controller){

        var data = flip( model.getAaComposition());

        var yAxis = new CategoryAxis();
        yAxis.setLabel("Chain");
        var xAxis = new NumberAxis();
        xAxis.setLabel("Count");
        xAxis.setTickLabelRotation(90);

        var chart = new StackedBarChart<>(yAxis, xAxis);

        chart.setLegendSide(Side.BOTTOM);
        chart.setLegendVisible(true);

        for (var chain : data.entrySet()) {
            var series = new XYChart.Series<String, Number>();
            series.setName(chain.getKey());
            for (var residue : chain.getValue().entrySet()) {
                series.getData().add(new XYChart.Data<>(residue.getKey(), residue.getValue()));
            }
            series.getData().sort(Comparator.comparingDouble(d -> d.getYValue().doubleValue()));
            chart.getData().add(series);
        }

        if (chart.getData().size() == 2) chart.getData().remove(1);

        return chart;
    }

    public static Map<String, HashMap<String, Integer>> flip(HashMap<String, Map<String, Integer>> map){
        Map<String, HashMap<String, Integer>> result = new HashMap<>();
        for (var key : map.keySet()){
            for (var key2 : map.get(key).keySet()){
                if (!result.containsKey(key2)){
                    result.put(key2, new HashMap<>());
                }

                result.get(key2).put(key, map.get(key).get(key2));
            }
        }
        return result;
    }

}
