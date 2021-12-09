package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import mara.mybox.data.Data2D;
import mara.mybox.data.DataFileText;
import mara.mybox.data.StringTable;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.tools.TmpFileTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-11-27
 * @License Apache License Version 2.0
 */
public class Data2DPasteController extends BaseController {

    protected ControlData2DEditTable tableController;
    protected Data2D data2D;
    protected DataFileText dataFileText;
    protected List<List<String>> data;
    protected List<String> columnNames;
    protected String delimiterName;
    protected SimpleBooleanProperty okNotify;
    protected boolean forPaste;

    @FXML
    protected TextArea textArea;
    @FXML
    protected CheckBox nameCheck;
    @FXML
    protected ControlWebView htmlController;
    @FXML
    protected ControlTextDelimiter delimiterController;
    @FXML
    protected HBox pasteBox;
    @FXML
    protected ComboBox<String> rowSelector, colSelector;
    @FXML
    protected RadioButton replaceRadio, insertRadio, appendRadio;

    public Data2DPasteController() {
        okNotify = new SimpleBooleanProperty();
    }

    @Override
    public void setStageStatus() {
        setAsPop(baseName);
    }

    public void setParameters(ControlData2DEditTable parent, String text, boolean forPaste) {
        try {
            tableController = parent;
            data2D = tableController.data2D;

            this.forPaste = forPaste;
            if (!forPaste) {
                thisPane.getChildren().remove(pasteBox);
            }

            htmlController.setParent(parent);

            delimiterController.setControls(baseName, true);
            delimiterController.changedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    delimiterName = delimiterController.delimiterName;
                }
            });
            delimiterName = delimiterController.delimiterName;

            nameCheck.setSelected(UserConfig.getBoolean(baseName + "WithNames", false));
            nameCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "WithNames", nameCheck.isSelected());
                }
            });

            dataFileText = new DataFileText();
            textArea.setText(text);
            if (text != null && !text.isBlank()) {
                delimiterName = null;  // guess at first 
                goAction();
            }

            makeControls(0, 0);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void makeControls(int row, int col) {
        try {
            if (!forPaste) {
                return;
            }
            List<String> rows = new ArrayList<>();
            for (long i = 0; i < tableController.tableData.size(); i++) {
                rows.add("" + (i + 1));
            }
            rowSelector.getItems().setAll(rows);
            rowSelector.getSelectionModel().select(row);

            colSelector.getItems().setAll(data2D.columnNames());
            colSelector.getSelectionModel().select(col);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void goAction() {
        dataFileText.initFile(null);
        htmlController.loadContents("");
        data = null;
        columnNames = null;
        String text = textArea.getText();
        if (text == null || text.isBlank()) {
            return;
        }
        synchronized (this) {
            if (task != null) {
                task.cancel();
            }
            task = new SingletonTask<Void>(this) {

                private StringTable validateTable;

                @Override
                protected boolean handle() {
                    File tmpFile = TmpFileTools.getTempFile();
                    TextFileTools.writeFile(tmpFile, text, Charset.forName("UTF-8"));
                    dataFileText.initFile(tmpFile);
                    dataFileText.setHasHeader(nameCheck.isSelected());
                    dataFileText.setCharset(Charset.forName("UTF-8"));
                    dataFileText.setPageSize(Integer.MAX_VALUE);
                    if (delimiterName == null || delimiterName.isEmpty()) {
                        delimiterName = dataFileText.guessDelimiter();
                    }
                    if (delimiterName == null || delimiterName.isEmpty()) {
                        delimiterName = ",";
                    }
                    dataFileText.setDelimiter(delimiterName);
                    dataFileText.setTask(task);
                    List<String> names = dataFileText.readColumns();
                    if (isCancelled()) {
                        return false;
                    }
                    if (names != null && !names.isEmpty()) {
                        List<Data2DColumn> columns = new ArrayList<>();
                        for (int i = 0; i < names.size(); i++) {
                            Data2DColumn column = new Data2DColumn(names.get(i), dataFileText.defaultColumnType());
                            column.setIndex(i);
                            columns.add(column);
                        }
                        dataFileText.setColumns(columns);
                        validateTable = Data2DColumn.validate(columns);
                    }
                    data = dataFileText.readPageData();
                    return data != null && !data.isEmpty();
                }

                @Override
                protected void whenSucceeded() {
                    try {
                        List<String> tcols = null;
                        if (dataFileText.isColumnsValid()) {
                            columnNames = new ArrayList<>();
                            for (int i = 0; i < dataFileText.columnsNumber(); i++) {
                                columnNames.add(dataFileText.colName(i));
                            }
                            tcols = new ArrayList<>();
                            tcols.add(message("RowNumber"));
                            tcols.addAll(columnNames);
                        }
                        StringTable table = new StringTable(tcols);
                        for (int i = 0; i < data.size(); i++) {
                            List<String> row = new ArrayList<>();
                            row.add(dataFileText.rowName(i));
                            List<String> drow = data.get(i);
                            drow.remove(0);
                            row.addAll(drow);
                            table.add(row);
                        }
                        htmlController.loadContents(table.html());
                    } catch (Exception e) {
                        MyBoxLog.console(e);
                    }
                }

                @Override
                protected void whenFailed() {
                    if (isCancelled()) {
                        return;
                    }
                    if (error != null) {
                        popError(message(error));
                    } else {
                        popFailed();
                    }
                }

                @Override
                protected void finalAction() {
                    dataFileText.setTask(null);
                    task = null;
                    delimiterController.setDelimiter(dataFileText.getDelimiter());
                    if (validateTable != null && !validateTable.isEmpty()) {
                        validateTable.htmlTable();
                    }
                }

            };
            start(task);
        }
    }

    @FXML
    @Override
    public void okAction() {
        if (data == null || data.isEmpty()) {
            popError(message("NoData"));
            return;
        }
        if (forPaste) {
            pasteData();
        } else {
            tableController.dataController.loadTmpData(columnNames, data);
        }
        popDone();
    }

    public void pasteData() {
        try {
            int row = rowSelector.getSelectionModel().getSelectedIndex();
            int col = colSelector.getSelectionModel().getSelectedIndex();
            int rowsNumber = data2D.tableRowsNumber();
            int colsNumber = data2D.tableColsNumber();
            if (row < 0 || row >= rowsNumber || col < 0 || col >= colsNumber) {
                popError(message("InvalidParameters"));
                return;
            }
            tableController.isSettingValues = true;
            if (replaceRadio.isSelected()) {
                for (int r = row; r < Math.min(row + data.size(), rowsNumber); r++) {
                    List<String> tableRow = tableController.tableData.get(r);
                    List<String> dataRow = data.get(r - row);
                    for (int c = col; c < Math.min(col + dataRow.size(), colsNumber); c++) {
                        tableRow.set(c + 1, dataRow.get(c - col));
                    }
                    tableController.tableData.set(r, tableRow);
                }
            } else {
                List<List<String>> newRows = new ArrayList<>();
                for (int r = 0; r < data.size(); r++) {
                    List<String> newRow = tableController.data2D.newRow();
                    List<String> dataRow = data.get(r);
                    for (int c = col; c < Math.min(col + dataRow.size(), colsNumber); c++) {
                        newRow.set(c + 1, dataRow.get(c - col));
                    }
                    newRows.add(newRow);
                }
                tableController.tableData.addAll(insertRadio.isSelected() ? row : row + 1, newRows);
            }
            tableController.isSettingValues = false;
            tableController.tableChanged(true);

            makeControls(row, col);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void cancelAction() {
        close();
    }

    @Override
    public boolean keyESC() {
        close();
        return false;
    }

    @Override
    public boolean keyF6() {
        close();
        return false;
    }

    /*
        static
     */
    public static Data2DPasteController open(ControlData2DEditTable parent, String text, boolean forPaste) {
        try {
            Data2DPasteController controller = (Data2DPasteController) WindowTools.openChildStage(
                    parent.getMyWindow(), Fxmls.Data2DPasteFxml);
            controller.setParameters(parent, text, forPaste);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
