package mara.mybox.controller;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.data2d.DataTable;
import mara.mybox.data2d.reader.DataTableGroup;
import mara.mybox.data2d.reader.DataTableGroup.TargetType;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
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

    protected List<Integer> dataColsIndices;
    protected DataFileCSV resultsFile;
    protected List<DataFileCSV> files;
    protected File currentFile;
    protected CSVPrinter csvPrinter;
    protected long rowIndex, startIndex, currentSize;
    protected String prefix;

    @FXML
    protected RadioButton fileRadio, filesRadio;

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
                    Data2D tmp2D = data2D.cloneAll();
                    List<Data2DColumn> tmpColumns = new ArrayList<>();
                    for (Data2DColumn column : data2D.columns) {
                        Data2DColumn tmpColumn = column.cloneAll();
                        String name = tmpColumn.getColumnName();
                        if (groupController.groupName != null && groupController.groupName.equals(name)) {
                            tmpColumn.setType(ColumnDefinition.ColumnType.Double);
                        }
                        tmpColumns.add(tmpColumn);
                    }
                    tmp2D.setColumns(tmpColumns);
                    tmp2D.startTask(task, filterController.filter);
                    DataTable tmpTable;
                    List<Integer> colIndices = data2D.columnIndices();
                    if (isAllPages()) {
                        tmpTable = tmp2D.toTmpTable(task, colIndices, false, false, invalidAs);
                    } else {
                        outputData = filtered(colIndices, false);
                        if (outputData == null || outputData.isEmpty()) {
                            error = message("NoData");
                            return false;
                        }
                        tmpTable = tmp2D.toTmpTable(task, colIndices, outputData, false, false, invalidAs);
                        outputData = null;
                    }
                    tmp2D.stopFilter();
                    List<String> tnames = new ArrayList<>();
                    if (groupController.groupName != null) {
                        tnames.add(groupController.groupName);
                    } else if (groupController.groupNames != null) {
                        tnames.addAll(groupController.groupNames);
                    }
                    for (String name : checkedColsNames) {
                        if (!tnames.contains(name)) {
                            tnames.add(name);
                        }
                    }
                    group = new DataTableGroup(tmpTable)
                            .setOriginalData(data2D)
                            .setType(groupController.groupType())
                            .setGroupNames(groupController.groupNames)
                            .setGroupName(groupController.groupName)
                            .setSplitInterval(groupController.splitInterval())
                            .setSplitNumber(groupController.splitNumber())
                            .setSplitList(groupController.splitList())
                            .setConditions(groupController.groupConditions)
                            .setCopyNames(checkedColsNames)
                            .setSorts(sortController.selectedNames())
                            .setMax(maxData).setScale(scale)
                            .setInvalidAs(invalidAs).setTask(task)
                            .setTargetType(fileRadio.isSelected() ? TargetType.SingleFile : TargetType.MultipleFiles)
                            .setTargetNames(tnames);
                    return group.run();
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                if (fileRadio.isSelected()) {
                    if (group.getTargetFile() != null) {
                        DataFileCSVController.loadCSV(group.getTargetFile());
                        popInformation(message("GroupNumber") + ": " + group.groupNumber());
                    }
                } else {
                    List<File> files = group.getCsvFiles();
                    if (files != null && !files.isEmpty()) {
                        browse(files.get(0).getParentFile());
                        popInformation(MessageFormat.format(message("FilesGenerated"), files.size()));
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
