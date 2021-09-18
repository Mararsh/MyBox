package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import mara.mybox.data.StringTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WebViewTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-12-26
 * @License Apache License Version 2.0
 */
public abstract class ControlSheet_Html extends ControlSheet_ColMenu {

    public void initHtmlControls() {
        try {
            htmlViewController.setParent(parentController);

            htmlTitleCheck.setSelected(UserConfig.getBoolean(baseName + "HtmlTitle", true));
            htmlTitleCheck.selectedProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
                updateHtml();
                UserConfig.setBoolean(baseName + "HtmlTitle", newValue);
            });
            htmlColumnCheck.setSelected(UserConfig.getBoolean(baseName + "HtmlColumn", false));
            htmlColumnCheck.selectedProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
                updateHtml();
                UserConfig.setBoolean(baseName + "HtmlColumn", newValue);
            });
            htmlRowCheck.setSelected(UserConfig.getBoolean(baseName + "HtmlRow", false));
            htmlRowCheck.selectedProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
                updateHtml();
                UserConfig.setBoolean(baseName + "HtmlRow", newValue);
            });
            if (htmlAllCheck != null) {
//            htmlAllCheck.setSelected(UserConfig.getBoolean(baseName + "HtmlAll", false));
                htmlAllCheck.selectedProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
                    updateHtml();
//                UserConfig.setBoolean(baseName + "HtmlAll", newValue);
                });
            }
        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
    }

    protected void updateHtml() {
        if (pagesNumber > 1 && htmlAllCheck != null && htmlAllCheck.isSelected()) {
            displayAllHtml();
        } else {
            displayPageHtml();
        }
    }

    protected void displayPageHtml() {
        try {
            if (pageData == null || pageData.length == 0) {
                htmlViewController.webEngine.loadContent("");
                return;
            }
            int rNumber = pageData.length;
            int cNumber = pageData[0].length;
            if (cNumber == 0) {
                htmlViewController.webEngine.loadContent("");
                return;
            }
            List<String> names;
            if (htmlColumnCheck.isSelected()) {
                names = new ArrayList<>();
                if (htmlRowCheck.isSelected()) {
                    names.add("");
                }
                for (int i = 0; i < cNumber; i++) {
                    names.add(colName(i));
                }
            } else {
                names = null;
            }
            String title = null;
            if (htmlTitleCheck.isSelected()) {
                title = titleName();
            }
            StringTable table = new StringTable(names, title);
            for (int i = 0; i < rNumber; i++) {
                List<String> row = new ArrayList<>();
                if (htmlRowCheck.isSelected()) {
                    row.add(rowName(i));
                }
                for (int j = 0; j < cNumber; j++) {
                    row.add(pageData[i][j]);
                }
                table.add(row);
            }
            htmlViewController.webEngine.loadContent(table.html());
        } catch (Exception e) {
            MyBoxLog.console(e);
        }
    }

    protected void displayAllHtml() {
        displayPageHtml();
    }

    protected int pageHtml(StringTable table, int inIndex) {
        int index = inIndex;
        try {
            if (sheetInputs != null) {
                for (int r = 0; r < sheetInputs.length; r++) {
                    List<String> values = new ArrayList<>();
                    if (htmlRowCheck.isSelected()) {
                        values.add(message("Row") + (index + 1));
                    }
                    values.addAll(row(r));
                    table.add(values);
                    index++;
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return index;
    }

    @FXML
    public void editHtml() {
        HtmlEditorController controller = (HtmlEditorController) openStage(Fxmls.HtmlEditorFxml);
        controller.loadContents(WebViewTools.getHtml(htmlViewController.webEngine));
    }

}
