package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import mara.mybox.data.StringTable;
import mara.mybox.db.table.ColumnDefinition;
import mara.mybox.db.table.ColumnDefinition.ColumnType;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.fxml.WebViewTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-12-26
 * @License Apache License Version 2.0
 */
public class ControlSheetData extends BaseController {

    protected BaseSheetController sheetController;
    protected List<ColumnDefinition> columns;
    protected ColumnType defaultColumnType;
    protected String dataName, defaultColValue, colPrefix, inputStyle;
    protected boolean defaultColNotNull, dataChanged, dataInvalid;
    protected String[][] sheet;
    protected int colsNumber, rowsNumber;

    @FXML
    protected TabPane tabPane, validationPane;
    @FXML
    protected Tab defTab, htmlTab, textsTab, reportTab;
    @FXML
    protected WebView webView, reportView;
    @FXML
    protected ControlDataText textController;
    @FXML
    protected CheckBox htmlTitleCheck, htmlColumnCheck, htmlRowCheck, textTitleCheck, textColumnCheck, textRowCheck;
    @FXML
    protected VBox defBox;

    public ControlSheetData() {
        baseTitle = message("Data");
        dataName = "sheet";
        colPrefix = "Field";
        defaultColumnType = ColumnDefinition.ColumnType.String;
        defaultColValue = "";
        defaultColNotNull = false;
    }

    // Should always run this after scene loaded and before input data
    public void initSheetController(BaseSheetController parent) {
        try {
            sheetController = parent;
            this.parentController = parent;
            this.baseName = parent.baseName;
            this.baseTitle = parent.baseTitle;

            this.dataName = sheetController.dataName;
            this.colPrefix = sheetController.colPrefix;
            this.defaultColumnType = sheetController.defaultColumnType;
            this.defaultColValue = sheetController.defaultColValue;
            this.defaultColNotNull = sheetController.defaultColNotNull;
            if (saveButton == null) {
                this.saveButton = sheetController.saveButton;
            }
            if (textController != null) {
                textController.setDataController(this);
            }
            if (htmlTitleCheck != null) {
                htmlTitleCheck.setSelected(UserConfig.getBoolean(baseName + "HtmlTitle", true));
                htmlTitleCheck.selectedProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
                    updateDataHtml(pickData());
                    UserConfig.setBoolean(baseName + "HtmlTitle", newValue);
                });
            }
            if (htmlColumnCheck != null) {
                htmlColumnCheck.setSelected(UserConfig.getBoolean(baseName + "HtmlColumn", false));
                htmlColumnCheck.selectedProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
                    updateDataHtml(pickData());
                    UserConfig.setBoolean(baseName + "HtmlColumn", newValue);
                });
            }
            if (htmlRowCheck != null) {
                htmlRowCheck.setSelected(UserConfig.getBoolean(baseName + "HtmlRow", false));
                htmlRowCheck.selectedProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
                    updateDataHtml(pickData());
                    UserConfig.setBoolean(baseName + "HtmlRow", newValue);
                });
            }
            if (textTitleCheck != null) {
                textTitleCheck.setSelected(UserConfig.getBoolean(baseName + "TextTitle", true));
                textTitleCheck.selectedProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
                    updateDataText(pickData());
                    UserConfig.setBoolean(baseName + "TextTitle", newValue);
                });
            }
            if (textColumnCheck != null) {
                textColumnCheck.setSelected(UserConfig.getBoolean(baseName + "TextColumn", false));
                textColumnCheck.selectedProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
                    updateDataText(pickData());
                    UserConfig.setBoolean(baseName + "TextColumn", newValue);
                });
            }
            if (textRowCheck != null) {
                textRowCheck.setSelected(UserConfig.getBoolean(baseName + "TextRow", false));
                textRowCheck.selectedProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
                    updateDataText(pickData());
                    UserConfig.setBoolean(baseName + "TextRow", newValue);
                });
            }

        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
    }

    protected String titleName() {
        if (sheetController == null || sheetController.sourceFile == null) {
            return "";
        }
        return sheetController.sourceFile.getAbsolutePath();
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
        try {
            this.dataChanged = dataChanged;
            if (getMyStage() != null) {
                String title = baseTitle + " " + titleName();
                if (dataChanged) {
                    title += " *";
                }
                myStage.setTitle(title);
            }
            validate();
        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
    }

    protected String[][] pickData() {
        try {
            if (sheet == null || sheet.length == 0) {
                rowsNumber = colsNumber = 0;
            } else {
                rowsNumber = sheet.length;
                colsNumber = sheet[0].length;
            }
        } catch (Exception e) {
            MyBoxLog.console(e.toString());
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
            for (int i = 0; i < columns.size(); i++) {
                names.add(colName(i));
            }
            return names;
        } catch (Exception e) {
            return null;
        }
    }

    protected List<String> rowNames(int rows) {
        try {
            List<String> names = new ArrayList<>();
            for (int i = 0; i < rows; i++) {
                names.add(rowName(i));
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
            ColumnDefinition column = new ColumnDefinition(message(colPrefix) + i, defaultColumnType, defaultColNotNull);
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
            String name = message(colPrefix) + (start + i);
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
        return message("Row") + (row + 1);
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

    protected void validate() {
        try {
            dataInvalid = false;
            List<String> names = new ArrayList<>();
            names.addAll(Arrays.asList(message("Row"), message("Column"), message("Reason")));
            StringTable table = new StringTable(names, message("InvalidData"));
            if (sheet != null) {
                for (int i = 0; i < sheet.length; i++) {
                    for (int j = 0; j < sheet[i].length; j++) {
                        String value = sheet[i][j];
                        if (!dataValid(j, value)) {
                            List<String> row = new ArrayList<>();
                            row.addAll(Arrays.asList((i + 1) + "", (j + 1) + "",
                                    (value == null || value.isBlank() ? message("Null") : message("InvalidValue"))));
                            table.add(row);
                        }
                    }
                }
            }
            dataInvalid = !table.isEmpty();
            if (saveButton != null) {
                saveButton.setDisable(dataInvalid);
            }
            if (reportView == null) {
                return;
            }
            reportView.getEngine().getLoadWorker().cancel();
            if (dataInvalid) {
                reportView.getEngine().loadContent(table.html());
                validationPane.getSelectionModel().select(reportTab);
            } else {
                reportView.getEngine().loadContent("<H2 align=\"center\">" + message("DataAreValid") + "</H2>");
            }
        } catch (Exception e) {
        }
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
        TextEditorController controller = (TextEditorController) openStage(Fxmls.TextEditorFxml);
        controller.loadContents(textController.textArea.getText());
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
            String title = null;
            if (htmlTitleCheck != null && htmlTitleCheck.isSelected()) {
                title = titleName();
            }
            StringTable table = new StringTable(names, title);
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

            CheckBox notNull = new CheckBox(message("Yes"));
            notNull.setPrefWidth(80);
            notNull.setMinWidth(Region.USE_PREF_SIZE);
            notNull.setSelected(column.isNotNull());

            ComboBox<String> typeSelector = new ComboBox<>();
            for (ColumnDefinition.ColumnType type : ColumnDefinition.editTypes()) {
                typeSelector.getItems().add(message(type.name()));
            }
            typeSelector.setValue(message(column.getType().name()));

            line.getChildren().addAll(indexLabel, nameInput, typeSelector, notNull, widthInput);
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
                nameInput.setStyle(NodeStyleTools.badStyle);
                ok = false;
            } else {
                column.setName(nameInput.getText().trim());
                nameInput.setStyle(null);
            }

            TextField widthInput = (TextField) (line.getChildren().get(4));
            try {
                double v = Double.parseDouble(widthInput.getText());
                if (v > 10) {
                    column.setWidth((int) v);
                    widthInput.setStyle(null);
                } else {
                    ok = false;
                    widthInput.setStyle(NodeStyleTools.badStyle);
                }
            } catch (Exception e) {
                ok = false;
                widthInput.setStyle(NodeStyleTools.badStyle);
            }

            if (ok) {
                CheckBox notNull = (CheckBox) (line.getChildren().get(3));
                column.setNotNull(notNull.isSelected());
            }

            ComboBox<String> typeSelector = (ComboBox) (line.getChildren().get(2));
            String ctype = typeSelector.getValue();
            if (ctype == null) {
                ok = false;
            } else if (ok) {
                for (ColumnDefinition.ColumnType type : ColumnDefinition.ColumnType.values()) {
                    if (ctype.equals(type.name()) || ctype.equals(message(type.name()))) {
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
            popError(message("InvalidData"));
        }
    }

    public void afterDefChanged() {
        dataChanged(true);
        if (sheetController != this) {
            sheetController.afterDefChanged(columns);
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

    /*
        buttons
     */
    @FXML
    @Override
    public void popAction() {
        try {
            Tab tab = tabPane.getSelectionModel().getSelectedItem();
            if (tab == htmlTab) {
                HtmlPopController.html(this, webView);

            } else if (tab == textsTab) {
                TextPopController.open(this, textController.textArea.getText());

            } else if (tab == reportTab) {
                HtmlPopController.html(this, reportView);

            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @FXML
    @Override
    public void menuAction() {
        try {
            closePopup();

            Tab tab = tabPane.getSelectionModel().getSelectedItem();
            if (tab == htmlTab) {
                Point2D localToScreen = webView.localToScreen(webView.getWidth() - 80, 80);
                MenuWebviewController.pop((BaseWebViewController) (webView.getUserData()), webView, null, localToScreen.getX(), localToScreen.getY());

            } else if (tab == textsTab) {
                Point2D localToScreen = textController.textArea.localToScreen(textController.textArea.getWidth() - 80, 80);
                MenuTextEditController.open(this, textController.textArea, localToScreen.getX(), localToScreen.getY());

            } else if (tab == reportTab) {
                Point2D localToScreen = reportView.localToScreen(reportView.getWidth() - 80, 80);
                MenuWebviewController.pop((BaseWebViewController) (reportView.getUserData()), reportView, null, localToScreen.getX(), localToScreen.getY());

            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void cleanPane() {
        try {
            sheet = null;
            columns = null;
            sheetController = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
