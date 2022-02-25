package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.data.StringTable;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.value.AppPaths;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2020-12-24
 * @License Apache License Version 2.0
 */
public class DataFileCSVController extends BaseData2DFileController {

    protected DataFileCSV dataFileCSV;

    @FXML
    protected ControlCsvOptions csvReadController, csvWriteController;
    @FXML
    protected VBox mainBox;

    public DataFileCSVController() {
        baseTitle = message("EditCSV");
    }

    @Override
    public void initData() {
        try {
            setDataType(Data2D.Type.CSV);
            dataFileCSV = (DataFileCSV) dataController.data2D;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.CSV);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            csvReadController.setControls(baseName + "Read");
            csvWriteController.setControls(baseName + "Write");

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void pickRefreshOptions() {
        Charset charset;
        if (csvReadController.autoDetermine) {
            charset = TextFileTools.charset(dataFileCSV.getFile());
        } else {
            charset = csvReadController.charset;
        }
        dataFileCSV.setOptions(csvReadController.withNamesCheck.isSelected(),
                charset, csvReadController.delimiter + "");
    }

    @Override
    public Data2D saveAsTarget() {
        File file = chooseSaveFile();
        if (file == null) {
            return null;
        }
        DataFileCSV targetData = new DataFileCSV();
        targetData.initFile(file)
                .setCharset(csvWriteController.charset)
                .setDelimiter(csvWriteController.delimiter + "")
                .setHasHeader(csvWriteController.withNamesCheck.isSelected());
        return targetData;
    }

    public void setFile(File file, Charset charset, boolean withName, char delimiter) {
        if (file == null || !checkBeforeNextAction()) {
            return;
        }
        csvReadController.withNamesCheck.setSelected(withName);
        csvReadController.setDelimiter(delimiter);
        csvReadController.setCharset(charset);
        dataFileCSV.initFile(file);
        dataFileCSV.setOptions(withName, charset, delimiter + "");
        dataController.readDefinition();
    }

    public void loadData(List<StringTable> tables) {
        if (tables == null || tables.isEmpty()) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {

                private File filePath;
                private LinkedHashMap<File, Boolean> files;
                private int count;

                @Override
                protected boolean handle() {
                    filePath = new File(AppPaths.getGeneratedPath());
                    files = DataFileCSV.save(filePath, "tmp", tables);
                    count = files != null ? files.size() : 0;
                    return count > 0;
                }

                @Override
                protected void whenSucceeded() {
                    Iterator<File> iterator = files.keySet().iterator();
                    File csvFile = iterator.next();
                    setFile(csvFile, Charset.forName("UTF-8"), files.get(csvFile), ',');
                    if (count > 1) {
                        browseURI(filePath.toURI());
                        String info = MessageFormat.format(message("GeneratedFilesResult"),
                                count, "\"" + filePath + "\"");
                        int num = 1;
                        info += "\n    " + csvFile.getName();
                        while (iterator.hasNext()) {
                            info += "\n    " + iterator.next().getName();
                            if (++num > 10) {
                                info += "\n    ......";
                                break;
                            }
                        }
                        alertInformation(info);
                    }
                }

            };
            start(task);
        }
    }

    /*
        static
     */
    public static DataFileCSVController open(File file, Charset charset, boolean withNames, char delimiter) {
        DataFileCSVController controller = (DataFileCSVController) WindowTools.openStage(Fxmls.DataFileCSVFxml);
        controller.setFile(file, charset, withNames, delimiter);
        controller.requestMouse();
        return controller;
    }

    public static DataFileCSVController open(List<Data2DColumn> cols, List<List<String>> data) {
        DataFileCSVController controller = (DataFileCSVController) WindowTools.openStage(Fxmls.DataFileCSVFxml);
        controller.dataController.loadTmpData(cols, data);
        controller.requestMouse();
        return controller;
    }

    public static DataFileCSVController openFile(File file) {
        DataFileCSVController controller = (DataFileCSVController) WindowTools.openStage(Fxmls.DataFileCSVFxml);
        controller.sourceFileChanged(file);
        controller.requestMouse();
        return controller;
    }

    public static DataFileCSVController open(Data2DDefinition def) {
        DataFileCSVController controller = (DataFileCSVController) WindowTools.openStage(Fxmls.DataFileCSVFxml);
        controller.loadDef(def);
        controller.requestMouse();
        return controller;
    }

}
