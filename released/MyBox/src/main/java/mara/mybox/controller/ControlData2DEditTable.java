package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.input.Clipboard;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.DataClipboard;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.tools.TextTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-10-18
 * @License Apache License Version 2.0
 */
public class ControlData2DEditTable extends ControlData2DLoad {

    protected SimpleBooleanProperty columnChangedNotify;

    @FXML
    protected Button headerButton;

    public ControlData2DEditTable() {
        readOnly = false;
        columnChangedNotify = new SimpleBooleanProperty(false);
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(headerButton, new Tooltip(message("FirstLineDefineNames")));
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected void setParameters(ControlData2DEdit editController) {
        try {
            dataController = editController.dataController;
            baseTitle = dataController.baseTitle;
            baseName = dataController.baseName;

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

            initPagination();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void setData(Data2D data) {
        try {
            super.setData(data);

            headerButton.setVisible(data2D.isTmpData() || data2D.isDataFile() || data.isClipboard());

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void notifyColumnChanged() {
        columnChangedNotify.set(!columnChangedNotify.get());
    }

    public void dataSaved() {
        try {
            popInformation(message("Saved"));
            if (data2D.getFile() != null) {
                recordFileWritten(data2D.getFile());
            }
            notifySaved();
            readDefinition();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void headerAction() {
        try {
            if (data2D == null || tableData.isEmpty()) {
                popError(message("NoData"));
                return;
            }
            List<String> row = tableData.get(0);
            if (row == null || row.size() < 2) {
                popError(message("InvalidData"));
                return;
            }
            List<String> names = new ArrayList<>();
            for (int i = 1; i < row.size(); i++) {
                String name = row.get(i);
                if (name == null || name.isBlank()) {
                    name = message("Column") + i;
                }
                while (names.contains(name)) {
                    name += "m";
                }
                names.add(name);
            }
            dataController.columnsController.setNames(names);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    /*
        table
     */
    @Override
    public synchronized void tableChanged(boolean changed) {
        if (isSettingValues || data2D == null) {
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
        return checkBeforeNextAction();
    }

    @Override
    public boolean checkBeforeNextAction() {
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
    public void clearAction() {
        if (data2D.isTmpData()) {
            deleteAllRows();
        } else {
            super.clearAction();
        }
    }

    @Override
    protected long clearData() {
        return data2D.clearData();
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
            toMyBoxClipboard(null, data2D.toColumns(names), data);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void toMyBoxClipboard(String name, List<Data2DColumn> cols, List<List<String>> data) {
        try {
            if (data == null || data.isEmpty()) {
                popError(message("NoData"));
                return;
            }
            SingletonTask copyTask = new SingletonTask<Void>(this) {

                private DataClipboard clip;

                @Override
                protected boolean handle() {
                    clip = DataClipboard.create(task, name, cols, data);
                    return clip != null;
                }

                @Override
                protected void whenSucceeded() {
                    DataInMyBoxClipboardController controller = DataInMyBoxClipboardController.oneOpen();
                    controller.loadDef(clip);
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

    @Override
    public void cleanPane() {
        try {
            columnChangedNotify = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
