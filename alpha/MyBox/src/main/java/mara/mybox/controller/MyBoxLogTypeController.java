package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.CheckBoxTreeItem;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ConditionNode;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2020-11-26
 * @License Apache License Version 2.0
 */
public class MyBoxLogTypeController extends ControlConditionTree {

    CheckBoxTreeItem<ConditionNode> allItem, errorItem;

    public MyBoxLogTypeController() {
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            List<String> s = new ArrayList();
            s.add(Languages.message("Type"));
            treeView.setSelectedTitles(s);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void loadTree() {
        try {
            allItem = new CheckBoxTreeItem(
                    ConditionNode.create(Languages.message("Type"))
                            .setTitle(Languages.message("Type"))
                            .setCondition("")
            );
            allItem.setExpanded(true);
            treeView.setRoot(allItem);

            errorItem = new CheckBoxTreeItem(
                    ConditionNode.create(Languages.message("Error"))
                            .setTitle(Languages.message("Error"))
                            .setCondition(" log_type=1 ")
            );
            errorItem.setExpanded(true);

            CheckBoxTreeItem<ConditionNode> debugItem = new CheckBoxTreeItem(
                    ConditionNode.create(Languages.message("Debug"))
                            .setTitle(Languages.message("Debug"))
                            .setCondition(" log_type=2 ")
            );
            debugItem.setExpanded(true);

            CheckBoxTreeItem<ConditionNode> infoItem = new CheckBoxTreeItem(
                    ConditionNode.create(Languages.message("Info"))
                            .setTitle(Languages.message("Info"))
                            .setCondition(" log_type=3 ")
            );
            infoItem.setExpanded(true);

            allItem.getChildren().addAll(errorItem, debugItem, infoItem);

            treeView.setSelection();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

}
