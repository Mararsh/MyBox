package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tab;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import mara.mybox.data.DoublePoint;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2019-8-13
 * @License Apache License Version 2.0
 */
public class ImageManufactureOperationController extends ImageViewerController {

    protected ImageManufactureController imageController;
    protected ImageManufactureScopeController scopeController;
    protected ImageManufactureOperationsController operationsController;
    protected ImageView maskView;
    protected boolean isTabbing;

    @FXML
    protected CheckBox scopeCheck;

    public ImageManufactureOperationController() {
        baseTitle = Languages.message("ImageManufacture");
    }

    protected void initPane() {
        try {
            if (scopeCheck != null) {
                scopeCheck.setSelected(imageController.isScopeTabSelected());
                scopeCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue v, Boolean ov, Boolean nv) {
                        if (isTabbing) {
                            return;
                        }
                        if (scopeCheck.isSelected()) {
                            imageController.scopeTab();
                        } else {
                            imageController.imageTab();
                        }
                    }
                });
                imageController.tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
                    @Override
                    public void changed(ObservableValue v, Tab ov, Tab nv) {
                        isTabbing = true;
                        scopeCheck.setSelected(imageController.isScopeTabSelected());
                        isTabbing = false;
                    }
                });
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void paneExpanded() {
    }

    protected void resetOperationPane() {

    }

    public void initOperation() {
        resetOperationPane();
        imageController.resetImagePane();
    }

    @Override
    public BaseImageController refreshInterfaceAndFile() {
        return null;  //Bypass since this is part of frame
    }

    /*
        events passed from image pane
     */
    @Override
    public void imageClicked(MouseEvent event, DoublePoint p) {

    }

    public void quitPane() {
    }

}
