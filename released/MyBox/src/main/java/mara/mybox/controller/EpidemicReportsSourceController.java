package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.CheckBoxTreeItem;
import mara.mybox.db.data.EpidemicReport;
import mara.mybox.fxml.ConditionNode;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2020-04-19
 * @License Apache License Version 2.0
 */
public class EpidemicReportsSourceController extends ControlConditionTree {

    protected List<String> datasets;

    public EpidemicReportsSourceController() {
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            List<String> s = new ArrayList();
            s.add(Languages.message("Dataset"));
            treeView.setSelectedTitles(s);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void loadTree(List<String> datasets) {
        this.datasets = datasets;
        loadTree();
    }

    @Override
    public void loadTree() {
        if (datasets == null || datasets.isEmpty()) {
            return;
        }
        try {
            CheckBoxTreeItem<ConditionNode> allItem = new CheckBoxTreeItem(
                    ConditionNode.create(Languages.message("Dataset"))
                            .setTitle(Languages.message("Dataset"))
                            .setCondition("")
            );
            allItem.setExpanded(true);
            treeView.setRoot(allItem);

            for (String dataset : datasets) {
                CheckBoxTreeItem<ConditionNode> datasetItem = new CheckBoxTreeItem(
                        ConditionNode.create(dataset)
                                .setTitle(dataset)
                                .setCondition(" data_set='" + dataset.replaceAll("'", "''") + "' ")
                );
                datasetItem.setExpanded(true);
                allItem.getChildren().add(datasetItem);

                CheckBoxTreeItem<ConditionNode> pItem = new CheckBoxTreeItem(
                        ConditionNode.create(Languages.message("PredefinedData"))
                                .setTitle(dataset + " - " + Languages.message("PredefinedData"))
                                .setCondition(" data_set='" + dataset.replaceAll("'", "''")
                                        + "' AND source=" + EpidemicReport.PredefinedData() + " ")
                );
                datasetItem.getChildren().add(pItem);

                CheckBoxTreeItem<ConditionNode> iItem = new CheckBoxTreeItem(
                        ConditionNode.create(Languages.message("InputtedData"))
                                .setTitle(dataset + " - " + Languages.message("InputtedData"))
                                .setCondition(" data_set='" + dataset.replaceAll("'", "''")
                                        + "' AND source=" + EpidemicReport.InputtedData() + " ")
                );
                datasetItem.getChildren().add(iItem);

                CheckBoxTreeItem<ConditionNode> fItem = new CheckBoxTreeItem(
                        ConditionNode.create(Languages.message("FilledData"))
                                .setTitle(dataset + " - " + Languages.message("FilledData"))
                                .setCondition(" data_set='" + dataset.replaceAll("'", "''")
                                        + "' AND source=" + EpidemicReport.FilledData() + " ")
                );
                datasetItem.getChildren().add(fItem);

                CheckBoxTreeItem<ConditionNode> sItem = new CheckBoxTreeItem(
                        ConditionNode.create(Languages.message("StatisticData"))
                                .setTitle(dataset + " - " + Languages.message("StatisticData"))
                                .setCondition(" data_set='" + dataset.replaceAll("'", "''")
                                        + "' AND source=" + EpidemicReport.StatisticData() + " ")
                );
                datasetItem.getChildren().add(sItem);
            }
            treeView.setSelection();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

}
