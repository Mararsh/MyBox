package mara.mybox.fxml.chart;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import mara.mybox.fxml.NodeTools;

/**
 * Reference:
 * https://stackoverflow.com/questions/34286062/how-to-clear-text-added-in-a-javafx-barchart/41494789#41494789
 * By Roland
 *
 * @Author Mara
 * @CreateDate 2022-4-27
 * @License Apache License Version 2.0
 */
public class LabeledBoxWhiskerChart<X, Y> extends LabeledLineChart<X, Y> {

    public LabeledBoxWhiskerChart(Axis xAxis, Axis yAxis) {
        super(xAxis, yAxis);
        init();
    }

    public void plotNodes() {
        NodeTools.children(1, this);
    }

    public ObservableList<Node> getPlotNodes() {
        return getPlotChildren();
    }

    public boolean addPlotNodes(Node node) {
        return getPlotChildren().add(node);
    }

}
