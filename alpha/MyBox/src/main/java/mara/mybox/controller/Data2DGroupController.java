package mara.mybox.controller;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.data2d.DataTable;
import mara.mybox.data2d.reader.DataTableGroup;
import mara.mybox.data2d.reader.DataTableGroup.TargetType;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import org.apache.commons.csv.CSVPrinter;

/**
 * @Author Mara
 * @CreateDate 2022-9-23
 * @License Apache License Version 2.0
 */
public class Data2DGroupController extends BaseData2DHandleController {

    protected DataFileCSV resultsFile;
    protected List<DataFileCSV> files;
    protected File currentFile;
    protected CSVPrinter csvPrinter;
    protected long rowIndex, startIndex, currentSize;
    protected String prefix;

    @FXML
    protected RadioButton fileRadio, filesRadio, tableRadio;

    public Data2DGroupController() {
        baseTitle = message("SplitGroup");
    }

    @Override
    protected void startOperation() {
        if (task != null) {
            task.cancel();
        }
        resultsFile = null;
        task = new SingletonTask<Void>(this) {

            private DataTableGroup group;

            @Override
            protected boolean handle() {
                try {
                    TargetType targetType;
                    if (fileRadio.isSelected()) {
                        targetType = TargetType.SingleFile;
                    } else if (filesRadio.isSelected()) {
                        targetType = TargetType.MultipleFiles;
                    } else {
                        targetType = TargetType.Table;
                    }
                    List<String> dataNames = new ArrayList<>();
                    if (showRowNumber()) {
                        dataNames.add(message("SourceRowNumber"));
                    }
                    dataNames.addAll(checkedColsNames);
                    group = groupData(targetType, dataNames, orders, maxData, -1);
                    return group.run();
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                if (fileRadio.isSelected()) {
                    DataFileCSV targetFile = group.getTargetFile();
                    if (targetFile != null) {
                        DataFileCSVController.loadCSV(targetFile);
                        popInformation(message("GroupNumber") + ": " + group.groupsNumber());
                    }
                } else if (filesRadio.isSelected()) {
                    List<File> files = group.getCsvFiles();
                    if (files != null && !files.isEmpty()) {
                        browse(files.get(0).getParentFile());
                        popInformation(MessageFormat.format(message("FilesGenerated"), files.size()));
                    }
                } else {
                    DataTable targetData = group.getTargetData();
                    if (targetData != null) {
                        DataTablesController.loadTable(targetData);
                        popInformation(message("GroupNumber") + ": " + group.groupsNumber());
                    }
                }

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
    public static Data2DGroupController open(ControlData2DLoad tableController) {
        try {
            Data2DGroupController controller = (Data2DGroupController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DGroupFxml, false);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
