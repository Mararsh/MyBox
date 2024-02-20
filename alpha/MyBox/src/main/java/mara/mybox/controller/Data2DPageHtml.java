package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import mara.mybox.data.StringTable;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.DataFilter;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WebViewTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.HtmlStyles;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-10-18
 * @License Apache License Version 2.0
 */
public class Data2DPageHtml extends BaseChildController {

    protected ControlData2DLoad dataController;
    protected Data2D data2D;
    protected DataFilter styleFilter;

    @FXML
    protected CheckBox formCheck, titleCheck, columnCheck, rowCheck;
    @FXML
    protected ControlWebView htmlController;
    @FXML
    protected Label nameLabel;

    public Data2DPageHtml() {
        baseTitle = message("ViewPageDataInHtml");
        styleFilter = new DataFilter();
    }

    protected void setParameters(ControlData2DLoad controller) {
        try {
            dataController = controller;

            titleCheck.setSelected(UserConfig.getBoolean(baseName + "DisplayTitle", true));
            titleCheck.selectedProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
                UserConfig.setBoolean(baseName + "DisplayTitle", newValue);
                updatePage();
            });

            columnCheck.setSelected(UserConfig.getBoolean(baseName + "DisplayColumn", false));
            columnCheck.selectedProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
                UserConfig.setBoolean(baseName + "DisplayColumn", newValue);
                updatePage();
            });

            rowCheck.setSelected(UserConfig.getBoolean(baseName + "DisplayRow", false));
            rowCheck.selectedProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
                UserConfig.setBoolean(baseName + "DisplayRow", newValue);
                updatePage();
            });

            formCheck.setSelected(UserConfig.getBoolean(baseName + "DisplayForm", false));
            formCheck.selectedProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
                UserConfig.setBoolean(baseName + "DisplayForm", newValue);
                updatePage();
            });

            initMore();

            loadData();

        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
    }

    protected void initMore() {
        htmlController.setParent(parentController);
    }

    public void loadData() {
        if (dataController == null || !dataController.isShowing()) {
            close();
            return;
        }
        data2D = dataController.data2D;
        nameLabel.setText(message("Data") + ": " + data2D.displayName());
        updatePage();
    }

    protected void updatePage() {
        if (formCheck.isSelected()) {
            htmlInForm();
        } else {
            htmlInTable();
        }
    }

    protected void htmlInTable() {
        try {
            int rNumber = data2D.tableRowsNumber();
            int cNumber = data2D.tableColsNumber();
            if (rNumber <= 0 || cNumber <= 0) {
                htmlController.clear();
                return;
            }
            List<String> names;
            if (columnCheck.isSelected()) {
                names = new ArrayList<>();
                if (rowCheck.isSelected()) {
                    names.add(message("RowNumber"));
                }
                for (int i = 0; i < cNumber; i++) {
                    names.add(data2D.columnName(i));
                }
            } else {
                names = null;
            }
            String title = null;
            if (titleCheck.isSelected()) {
                title = data2D.titleName();
            }
            StringTable table = new StringTable(names, title);

            for (int i = 0; i < rNumber; i++) {
                List<String> htmlRow = new ArrayList<>();
                if (rowCheck.isSelected()) {
                    htmlRow.add(data2D.rowName(i));
                }
                List<String> dataRow = data2D.tableData().get(i);
                for (int col = 0; col < cNumber; col++) {
                    String value = dataRow.get(col + 1);
                    if (value == null) {
                        value = "";
                    } else {
                        value = data2D.column(col).format(value);
                    }
                    value = StringTools.replaceHtmlLineBreak(value);
                    String style = data2D.cellStyle(styleFilter, i, data2D.columnName(col));
                    if (style != null && !style.isBlank()) {
                        style = style.replace("-fx-font-size:", "font-size:")
                                .replace("-fx-text-fill:", "color:")
                                .replace("-fx-background-color:", "background-color:")
                                .replace("-fx-font-weight: bolder", "font-weight:bold");
                        value = "<SPAN style=\"" + style + "\">" + value + "</SPAN>";
                    }
                    htmlRow.add(value);
                }
                table.add(htmlRow);
            }
            htmlController.loadContents(table.html());
        } catch (Exception e) {
            MyBoxLog.console(e);
        }
    }

    protected void htmlInForm() {
        try {
            int rNumber = data2D.tableRowsNumber();
            int cNumber = data2D.tableColsNumber();
            if (rNumber <= 0 || cNumber <= 0) {
                htmlController.clear();
                return;
            }
            StringBuilder s = new StringBuilder();
            if (titleCheck.isSelected()) {
                s.append("<H2>").append(data2D.titleName()).append("</H2>\n");
            }
            for (int r = 0; r < rNumber; r++) {
                StringTable table = new StringTable();
                if (rowCheck.isSelected()) {
                    List<String> row = new ArrayList<>();
                    row.add(data2D.rowName(r));
                    if (columnCheck.isSelected()) {
                        row.add(null);
                    }
                    table.add(row);
                }
                List<String> dataRow = data2D.tableData().get(r);
                for (int col = 0; col < cNumber; col++) {
                    List<String> htmlRow = new ArrayList<>();
                    if (columnCheck.isSelected()) {
                        htmlRow.add(data2D.columnName(col));
                    }
                    String value = dataRow.get(col + 1);
                    if (value == null) {
                        value = "";
                    } else {
                        value = data2D.column(col).format(value);
                    }
                    value = StringTools.replaceHtmlLineBreak(value);
                    String style = data2D.cellStyle(styleFilter, r, data2D.columnName(col));
                    if (style != null && !style.isBlank()) {
                        style = style.replace("-fx-font-size:", "font-size:")
                                .replace("-fx-text-fill:", "color:")
                                .replace("-fx-background-color:", "background-color:")
                                .replace("-fx-font-weight: bolder", "font-weight:bold");
                        value = "<SPAN style=\"" + style + "\">" + value + "</SPAN>";
                    }
                    htmlRow.add(value);
                    table.add(htmlRow);
                }
                s.append(table.div()).append("\n<BR><BR>\n");
            }
            htmlController.loadContents(HtmlWriteTools.html(data2D.titleName(),
                    "utf-8", HtmlStyles.DefaultStyle, s.toString()));
        } catch (Exception e) {
            MyBoxLog.console(e);
        }
    }

    @FXML
    public void editAction() {
        HtmlEditorController.openHtml(WebViewTools.getHtml(htmlController.webEngine));
    }

    @FXML
    @Override
    public void refreshAction() {
        loadData();
    }

    @FXML
    @Override
    public boolean popAction() {
        HtmlPopController.openWebView(this, htmlController.webView);
        return true;
    }

    @FXML
    @Override
    public boolean menuAction() {
        closePopup();
        Point2D localToScreen = htmlController.webView.localToScreen(htmlController.webView.getWidth() - 80, 80);
        MenuWebviewController.webviewMenu(htmlController, null, localToScreen.getX(), localToScreen.getY());
        return true;
    }

    @Override
    public void cleanPane() {
        try {
            data2D = null;
            dataController = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

    /*
        static
     */
    public static Data2DPageHtml open(ControlData2DLoad tableController) {
        try {
            Data2DPageHtml controller = (Data2DPageHtml) WindowTools.branchStage(
                    tableController, Fxmls.Data2DPageHtmlFxml);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
