package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import mara.mybox.data.StringTable;
import mara.mybox.db.table.ColumnDefinition;
import mara.mybox.db.table.ColumnDefinition.ColumnType;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.TextClipboardTools;
import static mara.mybox.fxml.NodeTools.badStyle;
import mara.mybox.fxml.WebViewTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;

import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-12-26
 * @License Apache License Version 2.0
 */
public class ControlSheetData extends BaseController {

    protected List<ColumnDefinition> columns;
    protected ColumnType defaultColumnType;
    protected String dataName, defaultColValue, colPrefix, inputStyle;
    protected boolean defaultColNotNull, dataChanged;
    protected String[][] sheet;
    protected int colsNumber, rowsNumber;
    protected TextArea textArea;

    @FXML
    protected ControlDataText textController;
    @FXML
    protected WebView webView;
    @FXML
    protected CheckBox htmlColumnCheck, htmlRowCheck;
    @FXML
    protected VBox defBox;
    @FXML
    protected WebView validationView;

    public ControlSheetData() {
        baseTitle = Languages.message("Data");
        dataName = "sheet";
        colPrefix = "Field";
        defaultColumnType = ColumnDefinition.ColumnType.String;
        defaultColValue = "";
        defaultColNotNull = false;
    }

    // Should always run this after scene loaded and before input data
    public void setParameters(BaseController parent) {
        try {
            this.parentController = parent;
            if (parent != null) {
                this.baseName = parent.baseName;
                this.baseTitle = parent.baseTitle;
                if (parent instanceof ControlSheetData) {
                    ControlSheetData pSheet = (ControlSheetData) parent;
                    this.dataName = pSheet.dataName;
                    this.colPrefix = pSheet.colPrefix;
                    this.defaultColumnType = pSheet.defaultColumnType;
                    this.defaultColValue = pSheet.defaultColValue;
                    this.defaultColNotNull = pSheet.defaultColNotNull;
                }
            }
            if (textController != null) {
                textController.setParameters(parent == null ? this : parent);
                textArea = textController.textArea;
            }
            if (htmlColumnCheck != null) {
                htmlColumnCheck.setSelected(UserConfig.getUserConfigBoolean(baseName + "HtmlColumn", true));
                htmlColumnCheck.selectedProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
                    updateDataHtml(pickData());
                    UserConfig.setUserConfigBoolean(baseName + "HtmlColumn", newValue);
                });
            }
            if (htmlColumnCheck != null) {
                htmlRowCheck.setSelected(UserConfig.getUserConfigBoolean(baseName + "HtmlRow", true));
                htmlRowCheck.selectedProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
                    updateDataHtml(pickData());
                    UserConfig.setUserConfigBoolean(baseName + "HtmlRow", newValue);
                });
            }

        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
    }

    protected String titleName() {
        return sourceFile == null ? "" : sourceFile.getAbsolutePath();
    }

    /*
        Sheet data
     */
    protected void setData(String[][] sheet, List<ColumnDefinition> columns) {
        try {
            this.sheet = sheet;
            this.columns = columns;
            pickData();
            makeDataDefintion();
            updateDataHtml(sheet);
            updateDataText(sheet);
            dataChanged(true);
        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
    }

    protected void dataChanged(boolean dataChanged) {
        this.dataChanged = dataChanged;
        if (getMyStage() != null) {
            String title = baseTitle + " " + titleName();
            if (dataChanged) {
                title += " *";
            }
            myStage.setTitle(title);
        }
        if (!checkInvalid().isEmpty()) {
//            popError(message("InvalidData"));
        }
    }

    protected String[][] pickData() {
        if (sheet == null || sheet.length == 0) {
            rowsNumber = colsNumber = 0;
        } else {
            rowsNumber = sheet.length;
            colsNumber = sheet[0].length;
        }
        return sheet;
    }

    /*
        columns
     */
    protected List<ColumnDefinition> checkColumns() {
        if (columns == null && sheet != null && sheet.length > 0) {
            makeColumns(sheet[0].length);
        }
        return columns;
    }

    protected List<String> columnNames() {
        try {
            if (checkColumns() == null) {
                return null;
            }
            List<String> names = new ArrayList<>();
            for (ColumnDefinition column : columns) {
                names.add(column.getName());
            }
            return names;
        } catch (Exception e) {
            return null;
        }
    }

    public void makeColumns() {
        columns = null;
        checkColumns();
    }

    public void makeColumns(int number) {
        columns = new ArrayList<>();
        for (int i = 1; i <= number; i++) {
            ColumnDefinition column = new ColumnDefinition(Languages.message(colPrefix) + i, defaultColumnType, defaultColNotNull);
            columns.add(column);
        }
    }

    // start: 0-based
    public void makeColumns(int start, int number) {
        if (columns == null) {
            makeColumns(start);
        }
        List<String> columnNames = columnNames();
        List<ColumnDefinition> newColumns = new ArrayList<>();
        for (int i = 1; i <= number; i++) {
            String name = Languages.message(colPrefix) + (start + i);
            while (columnNames.contains(name)) {
                name += "m";
            }
            ColumnDefinition column = new ColumnDefinition(name, defaultColumnType, defaultColNotNull);
            newColumns.add(column);
            columnNames.add(name);
        }
        columns.addAll(start, newColumns);
    }

    protected String rowName(int row) {
        return Languages.message("Row") + (row + 1);
    }

    protected String colName(int col) {
        try {
            if (checkColumns() == null || columns.size() <= col) {
                return null;
            }
            return columns.get(col).getName();
        } catch (Exception e) {
            return null;
        }

    }

    protected boolean dataValid(int col, String value) {
        try {
            if (checkColumns() == null) {
                return false;
            }
            ColumnDefinition column = columns.get(col);
            return column.valid(value);
        } catch (Exception e) {
        }
        return false;
    }

    protected StringTable checkInvalid() {
        List<String> names = new ArrayList<>();
        names.addAll(Arrays.asList(Languages.message("Row"), Languages.message("Column"), Languages.message("Reason")));
        StringTable table = new StringTable(names, Languages.message("InvalidData"));
        if (sheet != null) {
            for (int i = 0; i < sheet.length; i++) {
                for (int j = 0; j < sheet[i].length; j++) {
                    String value = sheet[i][j];
                    if (!dataValid(j, value)) {
                        List<String> row = new ArrayList<>();
                        row.addAll(Arrays.asList((i + 1) + "", (j + 1) + "",
                                (value == null || value.isBlank() ? Languages.message("Null") : Languages.message("InvalidValue"))));
                        table.add(row);
                    }
                }
            }
        }
        if (validationView != null) {
            if (table.isEmpty()) {
                validationView.getEngine().loadContent("");
            } else {
                validationView.getEngine().loadContent(table.html());
            }
        }
        if (saveButton != null) {
            saveButton.setDisable(!table.isEmpty());
        }
        return table;
    }

    /*
        Text tab
     */
    protected void updateDataText(String[][] data) {
        if (textController != null) {
            textController.update(data);
        }
    }

    protected String textDelimiter() {
        if (textController == null) {
            return ",";
        }
        return textController.delimiter;
    }

    @FXML
    public void copyText() {
        if (textController != null) {
            TextClipboardTools.copyToSystemClipboard(myController, textController.textArea.getText());
        }
    }

    @FXML
    public void editText() {
        if (textController == null) {
            return;
        }
        TextEditerController controller = (TextEditerController) openStage(Fxmls.TextEditerFxml);
        controller.loadContexts(textController.textArea.getText());
    }

    /*
        html tab
     */
    protected void updateDataHtml(String[][] data) {
        if (webView != null) {
            webView.getEngine().loadContent(html(data));
        }
    }

    public String html(String[][] data) {
        try {
            if (data == null || data.length == 0) {
                return "";
            }
            int rNumber = data.length;
            int cNumber = data[0].length;
            if (cNumber == 0) {
                return "";
            }
            List<String> names;
            if (htmlColumnCheck != null && htmlColumnCheck.isSelected()) {
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
            StringTable table = new StringTable(names, titleName());
            for (int i = 0; i < rNumber; i++) {
                List<String> row = new ArrayList<>();
                if (htmlRowCheck != null && htmlRowCheck.isSelected()) {
                    row.add(rowName(i));
                }
                for (int j = 0; j < cNumber; j++) {
                    row.add(data[i][j]);
                }
                table.add(row);
            }
            return table.html();
        } catch (Exception e) {
            MyBoxLog.console(e);
            return "";
        }
    }

    @FXML
    public void copyHtml() {
        TextClipboardTools.copyToSystemClipboard(myController, WebViewTools.getHtml(webView));
    }

    @FXML
    public void editHtml() {
        HtmlEditorController controller = (HtmlEditorController) openStage(Fxmls.HtmlEditorFxml);
        controller.loadContents(WebViewTools.getHtml(webView));
    }

    /*
        definition
     */
    public void makeDataDefintion() {
        if (defBox == null) {
            return;
        }
        defBox.getChildren().clear();
        if (columns == null && sheet != null && sheet.length > 0) {
            makeColumns(sheet[0].length);
        }
        if (columns == null) {
            return;
        }
        int index = 1;
        for (ColumnDefinition column : columns) {
            HBox line = new HBox();
            line.setAlignment(Pos.CENTER_LEFT);
            line.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            line.setSpacing(5);

            Label indexLabel = new Label("" + index++);
            indexLabel.setPrefWidth(50);
            indexLabel.setMinWidth(Region.USE_PREF_SIZE);
            indexLabel.setAlignment(Pos.CENTER_RIGHT);

            TextField nameInput = new TextField(column.getName());
            nameInput.setPrefWidth(200);
            nameInput.setMinWidth(Region.USE_PREF_SIZE);

            TextField widthInput = new TextField(column.getWidth() + "");
            widthInput.setPrefWidth(80);
            widthInput.setMinWidth(Region.USE_PREF_SIZE);

            CheckBox notNull = new CheckBox(Languages.message("Yes"));
            notNull.setPrefWidth(80);
            notNull.setMinWidth(Region.USE_PREF_SIZE);
            notNull.setSelected(column.isNotNull());

            ComboBox<String> typeSelector = new ComboBox<>();
            for (ColumnDefinition.ColumnType type : ColumnDefinition.editTypes()) {
                typeSelector.getItems().add(Languages.message(type.name()));
            }
            typeSelector.setValue(Languages.message(column.getType().name()));

            line.getChildren().addAll(indexLabel, nameInput, widthInput, notNull, typeSelector);
            defBox.getChildren().add(line);

        }
        refreshStyle(defBox);
    }

    @FXML
    public void okDefAction() {
        if (defBox == null) {
            return;
        }
        List<ColumnDefinition> newValues = new ArrayList<>();
        boolean ok = true;
        for (Node node : defBox.getChildren()) {
            ColumnDefinition column = new ColumnDefinition();
            HBox line = (HBox) node;

            TextField nameInput = (TextField) (line.getChildren().get(1));
            if (nameInput.getText().isBlank()) {
                nameInput.setStyle(badStyle);
                ok = false;
            } else {
                column.setName(nameInput.getText().trim());
                nameInput.setStyle(null);
            }

            TextField widthInput = (TextField) (line.getChildren().get(2));
            try {
                double v = Double.parseDouble(widthInput.getText());
                if (v > 10) {
                    column.setWidth((int) v);
                    widthInput.setStyle(null);
                } else {
                    ok = false;
                    widthInput.setStyle(badStyle);
                }
            } catch (Exception e) {
                ok = false;
                widthInput.setStyle(badStyle);
            }

            if (ok) {
                CheckBox notNull = (CheckBox) (line.getChildren().get(3));
                column.setNotNull(notNull.isSelected());
            }

            ComboBox<String> typeSelector = (ComboBox) (line.getChildren().get(4));
            String ctype = typeSelector.getValue();
            if (ctype == null) {
                ok = false;
            } else if (ok) {
                for (ColumnDefinition.ColumnType type : ColumnDefinition.ColumnType.values()) {
                    if (ctype.equals(type.name()) || ctype.equals(Languages.message(type.name()))) {
                        column.setType(type);
                        break;
                    }
                }
            }

            if (ok) {
                newValues.add(column);
                if (!ColumnDefinition.valid(this, newValues)) {
                    return;
                }
            }
        }
        if (ok) {
            columns = newValues;
            popSaved();
            afterDefChanged();
        } else {
            popError(Languages.message("InvalidData"));
        }
    }

    public void afterDefChanged() {
        dataChanged(true);
        if (parentController != null && parentController instanceof ControlSheetData) {
            ControlSheetData pSheet = (ControlSheetData) parentController;
            pSheet.columns = columns;
            pSheet.afterDefChanged();
        }
    }

    @FXML
    public void recoverDefAction() {
        makeDataDefintion();
    }

    @FXML
    public void clearDefAction() {
        makeColumns();
        makeDataDefintion();
    }

}
