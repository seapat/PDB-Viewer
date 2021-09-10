package PDBViewer.view;

import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class AminoAcidComposition {

    public static Node setupStackedBarChart(HashMap<String, Map<String, Integer>> data, WindowController controller, boolean flipBool) {

        var dataCopy = new HashMap<>(data);

        if (!controller.getChartTotalButton().isSelected()){
            dataCopy.remove("Total");
        }

        if (flipBool) {
            dataCopy = flip(dataCopy);
        }

        var yAxis = new CategoryAxis();
        yAxis.setLabel("Chain");
        var xAxis = new NumberAxis();
        xAxis.setLabel("Count");

        var chart = new StackedBarChart<>(yAxis, xAxis);

        chart.setLegendSide(Side.BOTTOM);
        chart.setLegendVisible(true);

        for (var chain : dataCopy.entrySet()) {
            var series = new XYChart.Series<String, Number>();
            series.setName(chain.getKey());
            for (var residue : chain.getValue().entrySet()) {
                series.getData().add(new XYChart.Data<>(residue.getKey(), residue.getValue()));
            }
            series.getData().sort(Comparator.comparingDouble(d -> d.getYValue().doubleValue()));
            chart.getData().add(series);
        }

        VBox.setVgrow(chart, Priority.ALWAYS);

        return chart;
    }

    public static HashMap<String, Map<String, Integer>> flip(HashMap<String, Map<String, Integer>> map) {
        HashMap<String, Map<String, Integer>> result = new HashMap<>();
        for (var key : map.keySet()) {
            for (var key2 : map.get(key).keySet()) {
                if (!result.containsKey(key2)) {
                    result.put(key2, new HashMap<>());
                }

                result.get(key2).put(key, map.get(key).get(key2));
            }
        }
        return result;
    }


}
