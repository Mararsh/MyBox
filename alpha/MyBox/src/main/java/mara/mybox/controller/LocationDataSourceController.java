package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.CheckBoxTreeItem;
import mara.mybox.db.data.Dataset;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ConditionNode;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2020-7-7
 * @License Apache License Version 2.0
 */
public class LocationDataSourceController extends ControlConditionTree {

    protected List<Dataset> datasets;

    public LocationDataSourceController() {
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            List<String> s = new ArrayList();
            s.add(Languages.message("Dataset"));
//            treeView.setSelectedTitles(s);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void loadTree(List<Dataset> datasets) {
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

            for (Dataset dataset : datasets) {
                String name = dataset.getDataSet();
                CheckBoxTreeItem<ConditionNode> datasetItem = new CheckBoxTreeItem(
                        ConditionNode.create(name)
                                .setTitle(name)
                                .setCondition(" datasetid=" + dataset.getId())
                );
                datasetItem.setExpanded(true);
                allItem.getChildren().add(datasetItem);
            }
            treeView.setSelection();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

}
