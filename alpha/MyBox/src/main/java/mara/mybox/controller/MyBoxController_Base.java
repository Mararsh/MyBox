package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Popup;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.NodeTools;

/**
 * @Author Mara
 * @CreateDate 2021-7-29
 * @License Apache License Version 2.0
 */
public abstract class MyBoxController_Base extends BaseController {

    protected Popup imagePop;
    protected ImageView view;
    protected Text text;

    @FXML
    protected VBox menuBox, imageBox, documentBox, fileBox, recentBox, networkBox, dataBox,
            settingsBox, aboutBox, mediaBox;
    @FXML
    protected CheckBox imageCheck;

    protected void showMenu(Region box, MouseEvent event) {
        if (popMenu == null || popMenu.isShowing()) {
            return;
        }
        LocateTools.locateCenter(box, popMenu);
    }

    protected void locateImage(Node region, boolean right) {
        if (!imageCheck.isSelected()) {
            imagePop.hide();
            return;
        }
        Bounds bounds = region.localToScreen(region.getBoundsInLocal());
        double x = right ? (bounds.getMaxX() + 200) : (bounds.getMinX() - 550);
        imagePop.show(region, x, bounds.getMinY() - 50);
        refreshStyle(imagePop.getOwnerNode().getParent());
    }

    @FXML
    protected void hideMenu(MouseEvent event) {
        if (popMenu != null) {
            popMenu.hide();
            popMenu = null;
        }
        imagePop.hide();
    }

}
