package mara.mybox.controller;

import javafx.event.Event;
import mara.mybox.db.data.DataNode;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-3-14
 * @License Apache License Version 2.0
 */
public class BaseDataSelectController extends BaseDataTreeController {

    @Override
    public String initTitle() {
        return nodeTable.getTreeName() + " - " + message("SelectNode");
    }

    @Override
    public void doubleClicked(Event event, DataNode node) {
        okAction();
    }
}
