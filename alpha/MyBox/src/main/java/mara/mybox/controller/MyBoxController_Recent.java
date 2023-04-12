package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import mara.mybox.db.data.VisitHistoryTools;

/**
 * @Author Mara
 * @CreateDate 2021-7-29
 * @License Apache License Version 2.0
 */
public abstract class MyBoxController_Recent extends MyBoxController_Media {

    @FXML
    protected void showRecentMenu(MouseEvent event) {
        List<MenuItem> items = new ArrayList<>();
        items.addAll(VisitHistoryTools.getRecentMenu(this));

        popCenterMenu(recentBox, items);

    }

}
