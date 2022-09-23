package mara.mybox.controller;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-9-23
 * @License Apache License Version 2.0
 */
public class Data2DSplitController extends BaseData2DHandleController {

    protected List<DataFileCSV> files;

    @FXML
    protected ControlSplit splitController;

    public Data2DSplitController() {
        baseTitle = message("Split");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            notSelectColumnsInTable(true);

            splitController.setParameters(this);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean initData() {
        try {
            if (!splitController.valid.get()) {
                popError(message("InvalidParameters") + ": " + message("Split"));
                return false;
            }
            files = null;
            return super.initData();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public boolean handleRows() {
        try {
            outputData = filtered(showRowNumber());
            if (outputData == null) {
                return false;
            }
            switch (splitController.splitType) {
                case Size:
                    return handleRowsBySize(splitController.size);
                case Number:
                    return handleRowsBySize(splitController.size(outputData.size(), splitController.number));
                case List:
                    return handleRowsByList();
            }
            return false;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            return false;
        }
    }

    public boolean handleRowsBySize(int size) {
        try {
            String prefix = data2D.dataName();
            int total = outputData.size();
            int start = 0, end = size;
            files = new ArrayList<>();
            while (start < total) {
                if (end > total) {
                    end = total;
                }
                DataFileCSV file = DataFileCSV.save(prefix + "_" + (start + 1) + "-" + end, task,
                        outputColumns, outputData.subList(start, end));
                files.add(file);
                start = end;
                end = start + size;
            }
            return true;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
            return false;
        }
    }

    public boolean handleRowsByList() {
        try {
            String prefix = data2D.dataName();
            files = new ArrayList<>();
            int total = outputData.size();
            for (int i = 0; i < splitController.list.size();) {
                int start = splitController.list.get(i++);
                int end = splitController.list.get(i++);
                if (start <= 0) {
                    start = 1;
                }
                if (end > total) {
                    end = total;
                }
                DataFileCSV file = DataFileCSV.save(prefix + "_" + start + "-" + end, task,
                        outputColumns, outputData.subList(start - 1, end));
                files.add(file);
            }
            return true;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
            return false;
        }
    }

    @Override
    public void ouputRows() {
        ouputFiles();
    }

    public void ouputFiles() {
        if (files == null || files.isEmpty()) {
            popError(message("NoFileGenerated"));
            return;
        }
        browse(files.get(0).getFile().getParentFile());
        popInformation(MessageFormat.format(message("FilesGenerated"), files.size()));
    }

    @Override
    public void handleAllTask() {
        if (task != null) {
            task.cancel();
        }
        files = null;
        task = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    data2D.startTask(task, filterController.filter);
                    switch (splitController.splitType) {
                        case Size:
                            files = data2D.splitBySize(checkedColsIndices, showRowNumber(), splitController.size);
                            break;
                        case Number:
                            files = data2D.splitBySize(checkedColsIndices, showRowNumber(),
                                    splitController.size(data2D.dataSize, splitController.number));
                            break;
                        case List:
                            files = data2D.splitByList(checkedColsIndices, showRowNumber(), splitController.list);
                            break;
                    }
                    data2D.stopFilter();
                    return files != null;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                ouputFiles();
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                data2D.stopTask();
                task = null;
            }

        };
        start(task);
    }

    /*
        static
     */
    public static Data2DSplitController open(ControlData2DLoad tableController) {
        try {
            Data2DSplitController controller = (Data2DSplitController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DSplitFxml, false);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
