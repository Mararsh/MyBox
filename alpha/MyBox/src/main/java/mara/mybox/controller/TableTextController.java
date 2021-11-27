package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
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

/**
 * @Author Mara
 * @CreateDate 2021-11-27
 * @License Apache License Version 2.0
 */
public class TableTextController extends TextDelimiterController {

    protected ControlData2D dataController;
    protected DataFileText dataFileText;
    protected List<List<String>> data;
    protected List<String> columnNames;

    @FXML
    protected TextArea textArea;
    @FXML
    protected CheckBox nameCheck;
    @FXML
    protected ControlWebView htmlController;

    public TableTextController() {
    }

    public void setParameters(ControlData2D parent, String text) {
        try {
            super.setParameters(parent, ",", true);
            dataController = parent;
            htmlController.setParent(parent);

            dataFileText = new DataFileText();
            textArea.setText(text);
            if (text != null && !text.isBlank()) {
                analyseAction();
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void analyseAction() {
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
                    dataFileText.setDelimiter(delimiterName);
                    dataFileText.setPageSize(Integer.MAX_VALUE);
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
                    if (validateTable != null && !validateTable.isEmpty()) {
                        validateTable.htmlTable();
                    }
                }

            };
            start(task);
        }
    }


    /*
        static
     */
    public static TableTextController open(ControlData2D parent, String initName) {
        try {
            TableTextController controller = (TableTextController) WindowTools.openChildStage(
                    parent.getMyWindow(), Fxmls.TableTextFxml);
            controller.setParameters(parent, initName);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
