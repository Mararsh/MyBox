package mara.mybox.data2d;

import java.sql.Connection;
import java.util.List;
import mara.mybox.controller.BaseController;
import mara.mybox.controller.DataFileCSVController;
import mara.mybox.controller.DataFileExcelController;
import mara.mybox.controller.DataFileTextController;
import mara.mybox.controller.DataInMyBoxClipboardController;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.tools.TextFileTools;

/**
 * @Author Mara
 * @CreateDate 2021-10-18
 * @License Apache License Version 2.0
 */
public abstract class DataFile extends Data2D {

    @Override
    public Data2DDefinition queryDefinition(Connection conn) {
        if (conn == null || type == null || file == null) {
            return null;
        }
        return tableData2DDefinition.queryFile(conn, type, file);
    }

    @Override
    public List<String> readColumnNames() {
        Data2DReader reader = Data2DReader.create(this)
                .setReaderTask(task).start(Data2DReader.Operation.ReadColumnNames);
        if (reader == null) {
            hasHeader = false;
            return null;
        }
        return reader.getNames();
    }

    public DataFile convert(BaseController controller, DataFileCSV dataSource, String format) {
        try {
            if (controller == null || dataSource == null || format == null) {
                return null;
            }
            DataFile dataFile;
            switch (format) {
                case "csv":
                    dataFile = dataSource;
                    controller.recordFileWritten(dataFile.getFile(), VisitHistory.FileType.CSV);
                    break;
                case "excel":
                    dataFile = DataFileExcel.toExcel(controller.getTask(), dataSource);
                    if (dataFile == null) {
                        return null;
                    }
                    controller.recordFileWritten(dataFile.getFile(), VisitHistory.FileType.Excel);
                    break;
                case "texts":
                    dataFile = DataFileText.toText(dataSource);
                    if (dataFile == null) {
                        return null;
                    }
                    controller.recordFileWritten(dataFile.getFile(), VisitHistory.FileType.Text);
                    break;
                case "systemClipboard":
                    dataFile = dataSource;
                    break;
                case "myBoxClipboard":
                    dataFile = DataClipboard.create(controller.getTask(), dataSource.getColumns(), dataSource);
                    if (dataFile == null) {
                        return null;
                    }
                    break;
                default:
                    return null;
            }
            dataFile.setD2did(-1);
            Data2D.saveAttributes(dataSource, dataFile);
            return dataFile;
        } catch (Exception e) {
            if (controller.getTask() != null) {
                controller.getTask().setError(e.toString());
            }
            return null;
        }
    }

    public void output(BaseController controller, DataFile dataFile, String format) {
        try {
            if (controller == null || dataFile == null || format == null) {
                return;
            }
            switch (format) {
                case "csv":
                    DataFileCSVController.open(dataFile.getFile(), dataFile.getCharset(),
                            dataFile.isHasHeader(), dataFile.getDelimiter().charAt(0));
                    break;
                case "excel":
                    DataFileExcelController.open(dataFile.getFile(), dataFile.isHasHeader());
                    break;
                case "texts":
                    DataFileTextController.open(dataFile.getFile(), dataFile.getCharset(),
                            dataFile.isHasHeader(), dataFile.getDelimiter());
                    break;
                case "systemClipboard":
                    TextClipboardTools.copyToSystemClipboard(controller, TextFileTools.readTexts(dataFile.getFile()));
                    break;
                case "myBoxClipboard":
                    DataInMyBoxClipboardController.open(dataFile);
                    break;
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
