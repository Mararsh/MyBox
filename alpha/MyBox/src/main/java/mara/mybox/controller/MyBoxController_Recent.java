package mara.mybox.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.MouseEvent;
import mara.mybox.db.data.VisitHistoryTools;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2021-7-29
 * @License Apache License Version 2.0
 */
public abstract class MyBoxController_Recent extends MyBoxController_Media {

    @FXML
    protected void showRecentMenu(MouseEvent event) {
        hideMenu(event);

        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        popMenu.getItems().addAll(VisitHistoryTools.getRecentMenu(this));

        popMenu.getItems().add(new SeparatorMenuItem());
        MenuItem closeMenu = new MenuItem(Languages.message("PopupClose"));
        closeMenu.setStyle("-fx-text-fill: #2e598a;");
        closeMenu.setOnAction((ActionEvent cevent) -> {
            popMenu.hide();
            popMenu = null;
        });
        popMenu.getItems().add(closeMenu);

        showMenu(recentBox, event);

    }

}
