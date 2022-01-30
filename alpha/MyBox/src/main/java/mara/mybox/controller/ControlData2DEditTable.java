package mara.mybox.controller;

import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.input.Clipboard;
import mara.mybox.data.DataClipboard;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.tools.TextTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-10-18
 * @License Apache License Version 2.0
 */
public class ControlData2DEditTable extends ControlData2DLoad {

    public ControlData2DEditTable() {
        forEdit = true;
    }

    protected void setParameters(ControlData2DEdit editController) {
        try {
            dataController = editController.dataController;
            baseTitle = dataController.baseTitle;

            paginationPane = dataController.paginationPane;
            pageSizeSelector = dataController.pageSizeSelector;
            pageSelector = dataController.pageSelector;
            pageLabel = dataController.pageLabel;
            dataSizeLabel = dataController.dataSizeLabel;
            selectedLabel = dataController.selectedLabel;
            pagePreviousButton = dataController.pagePreviousButton;
            pageNextButton = dataController.pageNextButton;
            pageFirstButton = dataController.pageFirstButton;
            pageLastButton = dataController.pageLastButton;
            saveButton = dataController.saveButton;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    /*
        table
     */
    @Override
    public synchronized void tableChanged(boolean changed) {
        if (isSettingValues) {
            return;
        }
        data2D.setTableChanged(changed);
        validateData();
        notifyStatus();

        dataController.textController.loadData();
        dataController.viewController.loadData();
    }

    @Override
    public boolean checkBeforeLoadingTableData() {
        return dataController.checkBeforeNextAction();
    }

    @Override
    public List<String> newData() {
        return data2D.newRow();
    }

    @Override
    public List<String> dataCopy(List<String> data) {
        return data2D.copyRow(data);
    }

    /*
        action
     */
    @FXML
    @Override
    public void addAction() {
        if (!validateData()) {
            return;
        }
        addRowsAction();
    }

    @FXML
    @Override
    public void deleteAction() {
        deleteRowsAction();
    }

    @FXML
    @Override
    public void copyAction() {
        if (!validateData()) {
            return;
        }
        Data2DCopyController.open(this);
    }

    public void copyToSystemClipboard(List<String> names, List<List<String>> data) {
        try {
            if (data == null || data.isEmpty()) {
                popError(message("NoData"));
                return;
            }
            String text = TextTools.dataText(data, ",", names, null);
            TextClipboardTools.copyToSystemClipboard(this, text);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void copyToMyBoxClipboard(List<String> names, List<List<String>> data) {
        try {
            if (data == null || data.isEmpty()) {
                popError(message("NoData"));
                return;
            }
            copyToMyBoxClipboard2(data2D.toColumns(names), data);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void copyToMyBoxClipboard2(List<Data2DColumn> cols, List<List<String>> data) {
        try {
            if (data == null || data.isEmpty()) {
                popError(message("NoData"));
                return;
            }
            SingletonTask copyTask = new SingletonTask<Void>(this) {

                private DataClipboard clip;

                @Override
                protected boolean handle() {
                    clip = DataClipboard.create(task, cols, data);
                    return clip != null;
                }

                @Override
                protected void whenSucceeded() {
                    DataInMyBoxClipboardController controller = DataInMyBoxClipboardController.oneOpen();
                    controller.load(clip);
                    popDone();
                }

            };
            start(copyTask, false);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void pasteContentInSystemClipboard() {
        try {
            if (data2D == null) {
                return;
            }
            String text = Clipboard.getSystemClipboard().getString();
            if (text == null || text.isBlank()) {
                popError(message("NoTextInClipboard"));
            }
            Data2DPasteContentInSystemClipboardController.open(this, text);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void pasteContentInMyboxClipboard() {
        try {
            if (data2D == null) {
                return;
            }
            Data2DPasteContentInMyBoxClipboardController.open(this);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
