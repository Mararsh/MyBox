package mara.mybox.controller;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.Tab;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeStyleTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-8-24
 * @License Apache License Version 2.0
 */
public abstract class ControlSheetFile extends ControlSheetFile_Sheet {

    @Override
    public void initValues() {
        try {
            super.initValues();

            initCurrentPage();
            fileLoadedNotify = new SimpleBooleanProperty(false);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            initPagination();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();

            NodeStyleTools.setTooltip(trimColumnsButton, message("RenameAllColumns"));
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void setParent(BaseDataFileController fileController) {
        this.fileController = fileController;
        this.backupController = fileController.backupController;
        this.baseName = fileController.baseName;
    }

    @FXML
    @Override
    public boolean popAction() {
        try {
            Tab tab = tabPane.getSelectionModel().getSelectedItem();
            if (tab == htmlTab) {
                HtmlPopController.openWebView(this, htmlViewController.webView);
                return true;

            } else if (tab == textsDisplayTab) {
                TextPopController.openInput(this, textsDisplayArea);
                return true;

            } else if (tab == reportTab) {
                HtmlPopController.openWebView(this, reportViewController.webView);
                return true;

            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return true;
    }

    @FXML
    @Override
    public boolean menuAction() {
        try {
            closePopup();

            Tab tab = tabPane.getSelectionModel().getSelectedItem();
            if (tab == htmlTab) {
                Point2D localToScreen = htmlViewController.webView.localToScreen(htmlViewController.webView.getWidth() - 80, 80);
                MenuWebviewController.pop(htmlViewController, null, localToScreen.getX(), localToScreen.getY());
                return true;

            } else if (tab == textsDisplayTab) {
                Point2D localToScreen = textsDisplayArea.localToScreen(textsDisplayArea.getWidth() - 80, 80);
                MenuTextEditController.open(this, textsDisplayArea, localToScreen.getX(), localToScreen.getY());
                return true;

            } else if (tab == reportTab) {
                Point2D localToScreen = reportViewController.webView.localToScreen(reportViewController.webView.getWidth() - 80, 80);
                MenuWebviewController.pop(reportViewController, null, localToScreen.getX(), localToScreen.getY());
                return true;

            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return false;
    }

}
