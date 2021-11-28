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
import javafx.scene.control.TextArea;
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

/**
 * @Author Mara
 * @CreateDate 2021-11-27
 * @License Apache License Version 2.0
 */
public class DataTextController extends BaseController {

    protected ControlData2D dataController;
    protected DataFileText dataFileText;
    protected List<List<String>> data;
    protected List<String> columnNames;
    protected String delimiterName;
    protected SimpleBooleanProperty okNotify;

    @FXML
    protected TextArea textArea;
    @FXML
    protected CheckBox nameCheck;
    @FXML
    protected ControlWebView htmlController;
    @FXML
    protected ControlTextDelimiter delimiterController;

    public DataTextController() {
        okNotify = new SimpleBooleanProperty();
    }

    public void setParameters(ControlData2D parent, String text) {
        try {
            dataController = parent;
            baseName = parent.baseName;

            htmlController.setParent(parent);

            delimiterController.setControls(baseName, true);
            delimiterController.changedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    delimiterName = delimiterController.delimiterName;
                }
            });
            delimiterName = delimiterController.delimiterName;

            dataFileText = new DataFileText();
            textArea.setText(text);
            if (text != null && !text.isBlank()) {
                delimiterName = null;  // guess at first 
                goAction();
            }
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
        synchronized (this) {
            if (task != null) {
                task.cancel();
            }
            task = new SingletonTask<Void>(this) {

                private Data2D data2D;

                @Override
                protected boolean handle() {
                    data2D = dataController.data2D;
                    data2D.setTask(task);
                    File tmpFile = data2D.tmpFile(columnNames, data);
                    data2D.initFile(tmpFile);
                    data2D.setHasHeader(columnNames != null);
                    data2D.setCharset(Charset.forName("UTF-8"));
                    data2D.setDelimiter(",");
                    data2D.setUserSavedDataDefinition(false);
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    close();
                    dataController.loadDefinition();
                }

                @Override
                protected void finalAction() {
                    data2D.setTask(null);
                    task = null;
                }

            };
            start(task);
        }
    }

    @FXML
    @Override
    public void cancelAction() {
        close();
    }


    /*
        static
     */
    public static DataTextController open(ControlData2D parent, String text) {
        try {
            DataTextController controller = (DataTextController) WindowTools.openChildStage(
                    parent.getMyWindow(), Fxmls.DataTextFxml);
            controller.setParameters(parent, text);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
