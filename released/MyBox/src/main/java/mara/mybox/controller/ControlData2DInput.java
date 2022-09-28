package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import mara.mybox.data.StringTable;
import mara.mybox.data2d.DataFileText;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.tools.TextFileTools;
import mara.mybox.tools.TmpFileTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-11-27
 * @License Apache License Version 2.0
 */
public class ControlData2DInput extends BaseController {

    protected ControlData2DLoad loadController;
    protected DataFileText dataFileText;
    protected List<List<String>> data;
    protected List<String> columnNames;
    protected String delimiterName;
    protected SimpleBooleanProperty statusNotify;
    protected ChangeListener<Boolean> delimiterListener;

    @FXML
    protected TextArea textArea;
    @FXML
    protected CheckBox nameCheck;
    @FXML
    protected ControlWebView htmlController;

    public ControlData2DInput() {
        statusNotify = new SimpleBooleanProperty(false);
    }

    @Override
    public void setStageStatus() {
        setAsPop(baseName);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            htmlController.setParent(this);

            dataFileText = new DataFileText();

            delimiterName = UserConfig.getString(baseName + "InputDelimiter", ",");

            nameCheck.setSelected(UserConfig.getBoolean(baseName + "WithNames", false));
            nameCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "WithNames", nameCheck.isSelected());
                    goAction();
                }
            });

            goButton.disableProperty().bind(textArea.textProperty().isNull()
                    .or(textArea.textProperty().isEmpty()));
            editButton.setDisable(true);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setParameters(ControlData2DLoad parent, String text) {
        try {
            loadController = parent;
            load(text);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void load(String text) {
        try {
            if (text == null || text.isBlank()) {
                popError(message("InputOrPasteText"));
                return;
            }
            textArea.setText(text);
            delimiterName = null;  // guess at first 
            goAction();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void delimiterActon() {
        TextDelimiterController controller = TextDelimiterController.open(this, delimiterName, true);
        controller.okNotify.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                delimiterName = controller.delimiterName;
                UserConfig.setString(baseName + "InputDelimiter", delimiterName);
                goAction();
                popDone();
            }
        });
    }

    @FXML
    @Override
    public void loadContentInSystemClipboard() {
        try {
            String text = Clipboard.getSystemClipboard().getString();
            if (text == null || text.isBlank()) {
                popError(message("NoTextInClipboard"));
            }
            load(text);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void goAction() {
        dataFileText.initFile(null);
        htmlController.loadContents("");
        editButton.setDisable(true);
        data = null;
        columnNames = null;
        String text = textArea.getText();
        if (text == null || text.isBlank()) {
            popError(message("InputOrPasteText"));
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
                    try {
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
                        dataFileText.startTask(task, null);
                        List<String> names = dataFileText.readColumnNames();
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
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    try ( Connection conn = DerbyBase.getConnection()) {
                        data = dataFileText.readPageData(conn);
                    } catch (Exception e) {
                        MyBoxLog.error(e);
                        return false;
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    try {
                        List<String> tcols = null;
                        if (dataFileText.isColumnsValid()) {
                            columnNames = new ArrayList<>();
                            for (int i = 0; i < dataFileText.columnsNumber(); i++) {
                                columnNames.add(dataFileText.columnName(i));
                            }
                            tcols = new ArrayList<>();
                            tcols.add(message("SourceRowNumber"));
                            tcols.addAll(columnNames);
                        }
                        StringTable table = new StringTable(tcols);
                        if (data != null) {
                            for (int i = 0; i < data.size(); i++) {
                                List<String> row = new ArrayList<>();
                                row.add(dataFileText.rowName(i));
                                List<String> drow = data.get(i);
                                drow.remove(0);
                                row.addAll(drow);
                                table.add(row);
                            }
                        }
                        htmlController.loadContents(table.html());
                        editButton.setDisable(false);
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
                    dataFileText.stopTask();
                    task = null;
                    if (validateTable != null && !validateTable.isEmpty()) {
                        validateTable.htmlTable();
                    }
                    statusNotify.set(statusNotify.get());
                }

            };
            start(task);
        }
    }

    @FXML
    public void editAction() {
        if (dataFileText.getColumns() == null || data == null || data.isEmpty()) {
            popError(message("NoData"));
            return;
        }
        DataFileTextController.open(null, dataFileText.getColumns(), data);
    }

    public boolean hasData() {
        return dataFileText != null && dataFileText.hasData();
    }

    @Override
    public void cleanPane() {
        try {
            statusNotify = null;
            delimiterListener = null;
            loadController = null;
            dataFileText = null;
            data = null;
            columnNames = null;
            delimiterName = null;
            columnNames = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
