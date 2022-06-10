package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import mara.mybox.data.StringTable;
import mara.mybox.data2d.Data2D;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WebViewTools;
import mara.mybox.fxml.style.HtmlStyles;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.tools.TextTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-10-18
 * @License Apache License Version 2.0
 *
 */
public class ControlData2DView extends BaseController {

    protected ControlData2D dataController;
    protected String displayDelimiterName;
    protected Data2D data2D;
    protected ChangeListener<Boolean> delimiterListener;

    @FXML
    protected Tab htmlTab, textTab;
    @FXML
    protected CheckBox formCheck, titleCheck, columnCheck, rowCheck, wrapCheck;
    @FXML
    protected TextArea textArea;
    @FXML
    protected ControlWebView htmlController;
    @FXML
    protected HBox textButtonsBox;

    protected void setParameters(ControlData2D dataController) {
        try {
            this.dataController = dataController;

            htmlController.setParent(parentController);

            tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
                @Override
                public void changed(ObservableValue ov, Tab oldValue, Tab newValue) {
                    textButtonsBox.setVisible(newValue == textTab);
                }
            });
            textButtonsBox.setVisible(textTab.isSelected());

            titleCheck.setSelected(UserConfig.getBoolean(baseName + "DisplayTitle", true));
            titleCheck.selectedProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
                UserConfig.setBoolean(baseName + "DisplayTitle", newValue);
                updateHtml();
                updateText();
            });
            columnCheck.setSelected(UserConfig.getBoolean(baseName + "DisplayColumn", false));
            columnCheck.selectedProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
                UserConfig.setBoolean(baseName + "DisplayColumn", newValue);
                updateHtml();
                updateText();
            });
            rowCheck.setSelected(UserConfig.getBoolean(baseName + "DisplayRow", false));
            rowCheck.selectedProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
                UserConfig.setBoolean(baseName + "DisplayRow", newValue);
                updateHtml();
                updateText();
            });

            formCheck.setSelected(UserConfig.getBoolean(baseName + "DisplayForm", false));
            formCheck.selectedProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
                UserConfig.setBoolean(baseName + "DisplayForm", newValue);
                updateHtml();
                updateText();
            });

            wrapCheck.setSelected(UserConfig.getBoolean(baseName + "DisplayTextWrap", true));
            wrapCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "DisplayTextWrap", newValue);
                    textArea.setWrapText(newValue);
                }
            });
            textArea.setWrapText(wrapCheck.isSelected());

            displayDelimiterName = UserConfig.getString(baseName + "DisplayDelimiter", ",");

        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
    }

    public void setData(Data2D data) {
        try {
            data2D = data;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void loadData() {
        updateHtml();
        updateText();
    }

    @FXML
    public void editAction() {
        if (htmlTab.isSelected()) {
            editHtml();
        } else {
            editText();
        }
    }

    @FXML
    public void refreshAction() {
        if (htmlTab.isSelected()) {
            updateHtml();
        } else if (textTab.isSelected()) {
            updateText();
        }
    }

    @FXML
    @Override
    public boolean popAction() {
        if (htmlTab.isSelected()) {
            HtmlPopController.openWebView(this, htmlController.webView);
            return true;
        } else if (textTab.isSelected()) {
            TextPopController.openInput(this, textArea);
            return true;
        }
        return false;
    }

    @FXML
    @Override
    public boolean menuAction() {
        closePopup();
        if (htmlTab.isSelected()) {
            Point2D localToScreen = htmlController.webView.localToScreen(htmlController.webView.getWidth() - 80, 80);
            MenuWebviewController.pop(htmlController, null, localToScreen.getX(), localToScreen.getY());
            return true;
        } else if (textTab.isSelected()) {
            Point2D localToScreen = textArea.localToScreen(textArea.getWidth() - 80, 80);
            MenuTextEditController.open(myController, textArea, localToScreen.getX(), localToScreen.getY());
            return true;
        }
        return false;
    }

    @FXML
    public void delimiterActon() {
        TextDelimiterController controller = TextDelimiterController.open(this, displayDelimiterName, false);
        controller.okNotify.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                displayDelimiterName = controller.delimiterName;
                UserConfig.setString(baseName + "DisplayDelimiter", displayDelimiterName);
                if (!formCheck.isSelected()) {
                    textInTable();
                }
            }
        });
    }

    /*
        Display Html
     */
    protected void updateHtml() {
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
                htmlController.loadContents("");
                return;
            }
            List<String> names;
            if (columnCheck.isSelected()) {
                names = new ArrayList<>();
                if (rowCheck.isSelected()) {
                    names.add(message("RowNumber"));
                }
                for (int i = 0; i < cNumber; i++) {
                    names.add(data2D.colName(i));
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
                    }
                    String style = data2D.cellStyle(i, data2D.colName(col));
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
                htmlController.loadContents("");
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
                        htmlRow.add(data2D.colName(col));
                    }
                    String value = dataRow.get(col + 1);
                    if (value == null) {
                        value = "";
                    }
                    String style = data2D.cellStyle(r, data2D.colName(col));
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
    public void editHtml() {
        HtmlEditorController controller = (HtmlEditorController) openStage(Fxmls.HtmlEditorFxml);
        controller.loadContents(WebViewTools.getHtml(htmlController.webEngine));
    }

    /*
        Display Text
     */
    protected void updateText() {
        if (formCheck.isSelected()) {
            textInForm();
        } else {
            textInTable();
        }
    }

    protected void textInTable() {
        String title = null;
        if (titleCheck.isSelected()) {
            title = data2D.titleName();
        }
        String text = TextTools.dataPage(data2D, displayDelimiterName,
                rowCheck.isSelected(), columnCheck.isSelected());
        if (title != null && !title.isBlank()) {
            textArea.setText(title + "\n\n" + text);
        } else {
            textArea.setText(text);
        }
    }

    protected void textInForm() {
        StringBuilder s = new StringBuilder();
        if (titleCheck.isSelected()) {
            s.append(data2D.titleName()).append("\n\n");
        }
        for (int r = 0; r < data2D.tableRowsNumber(); r++) {
            if (rowCheck.isSelected()) {
                s.append(data2D.rowName(r)).append("\n");
            }
            List<String> drow = data2D.tableRowWithoutNumber(r);
            if (drow == null) {
                continue;
            }
            for (int col = 0; col < data2D.columnsNumber(); col++) {
                if (columnCheck.isSelected()) {
                    s.append(data2D.colName(col)).append(": ");
                }
                String v = drow.get(col);
                if (v == null) {
                    continue;
                }
                s.append(v).append("\n");
            }
            s.append("\n");
            textArea.setText(s.toString());
        }
    }

    @FXML
    public void editText() {
        TextEditorController controller = (TextEditorController) openStage(Fxmls.TextEditorFxml);
        controller.loadContents(textArea.getText());
    }

    @Override
    public void cleanPane() {
        try {
            delimiterListener = null;
            data2D = null;
            dataController = null;
            displayDelimiterName = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
