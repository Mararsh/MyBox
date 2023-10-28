package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tab;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import mara.mybox.data.DoublePoint;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2019-8-13
 * @License Apache License Version 2.0
 */
public class ImageManufactureOperationController extends BaseController {

    protected ImageManufactureController editor;
    protected ImageManufactureScopeController scopeController;
    protected ImageManufactureOperationsController operationsController;
    protected ImageView imageView, maskView;
    protected AnchorPane maskPane;
    protected boolean isTabbing;

    @FXML
    protected CheckBox scopeCheck;

    public ImageManufactureOperationController() {
        baseTitle = Languages.message("ImageManufacture");
    }

    protected void initPane() {
        try {

            if (scopeCheck != null) {
                scopeCheck.setSelected(editor.isScopeTabSelected());
                scopeCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue v, Boolean ov, Boolean nv) {
                        if (isTabbing) {
                            return;
                        }
                        if (scopeCheck.isSelected()) {
                            editor.scopeTab();
                        } else {
                            editor.imageTab();
                        }
                    }
                });
                editor.tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
                    @Override
                    public void changed(ObservableValue v, Tab ov, Tab nv) {
                        isTabbing = true;
                        scopeCheck.setSelected(editor.isScopeTabSelected());
                        isTabbing = false;
                    }
                });
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void paneExpanded() {

    }

    protected void paneUnexpanded() {
    }

    protected void resetOperationPane() {

    }

    public void initOperation() {
        resetOperationPane();
        editor.resetImagePane();
    }

    public boolean isCurrentOperation() {
        return operationsController.currentController == this;
    }

    @Override
    public BaseImageController refreshInterfaceAndFile() {
        return null;  //Bypass since this is part of frame
    }

    public void showScope(boolean noWhole) {

        isTabbing = true;
        if (scopeCheck != null) {
            scopeCheck.setDisable(false);
            scopeCheck.setSelected(true);
        }
        editor.scopeTab();
        isTabbing = false;
        if (noWhole && (scopeController.scopeWhole())) {
            scopeController.scopeRectangleRadio.setSelected(true);
        }
    }

    /*
        events passed from image pane
     */
    public void paneClicked(MouseEvent event, DoublePoint p) {

    }

    public void mousePressed(MouseEvent event) {
    }

    public void mouseDragged(MouseEvent event) {
    }

    public void mouseReleased(MouseEvent event) {
    }

    public void quitPane() {
    }

}
