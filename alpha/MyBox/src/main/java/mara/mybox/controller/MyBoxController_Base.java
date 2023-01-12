package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import mara.mybox.fxml.LocateTools;

/**
 * @Author Mara
 * @CreateDate 2021-7-29
 * @License Apache License Version 2.0
 */
public abstract class MyBoxController_Base extends BaseController {

    @FXML
    protected VBox menuBox, imageBox, documentBox, fileBox, recentBox, networkBox, dataBox,
            settingsBox, aboutBox, mediaBox;

    protected void showMenu(Region box, MouseEvent event) {
        if (popMenu == null || popMenu.isShowing()) {
            return;
        }
        LocateTools.locateCenter(box, popMenu);
    }

    @FXML
    protected void hideMenu(MouseEvent event) {
        if (popMenu != null) {
            popMenu.hide();
            popMenu = null;
        }
    }

}
