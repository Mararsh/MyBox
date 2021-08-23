package mara.mybox.controller;

import java.util.List;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.Tab;
import mara.mybox.db.table.ColumnDefinition;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2020-12-26
 * @License Apache License Version 2.0
 */
public class ControlSheetDisplay extends ControlSheetDisplay_Calculation {

    // Should always run this after scene loaded and before input data
    public void initDisplay(BaseSheetController parent) {
        try {
            sheetController = parent;
            this.parentController = parent;
            this.baseName = parent.baseName;
            this.baseTitle = parent.baseTitle;

            this.dataName = parent.dataName;
            this.colPrefix = parent.colPrefix;
            this.defaultColumnType = parent.defaultColumnType;
            this.defaultColValue = parent.defaultColValue;
            this.defaultColNotNull = parent.defaultColNotNull;
            if (saveButton == null) {
                this.saveButton = parent.saveButton;
            }
            if (htmlTitleCheck == null) {
                return;
            }

            initHtmlControls();
            initTextControls();
            initCalculationControls();

        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
    }

    protected void setDisplayData(String[][] sheet, List<ColumnDefinition> columns) {
        try {
            this.sheet = sheet;
            this.columns = columns;
            pickData();
            makeDefintion();
            validate();
            updateHtml();
            updateText();
            updateCalculation();
        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
    }

    @FXML
    @Override
    public boolean popAction() {
        try {
            Tab tab = tabPane.getSelectionModel().getSelectedItem();
            if (tab == htmlTab) {
                HtmlPopController.openWebView(this, htmlViewController.webView);
                return true;

            } else if (tab == textsTab) {
                TextPopController.openInput(this, textArea);
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

            } else if (tab == textsTab) {
                Point2D localToScreen = textArea.localToScreen(textArea.getWidth() - 80, 80);
                MenuTextEditController.open(this, textArea, localToScreen.getX(), localToScreen.getY());
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

    @Override
    public void cleanPane() {
        try {
            sheet = null;
            columns = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
