package mara.mybox.data2d.tools;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import mara.mybox.data.StringTable;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.Data2D_Attributes;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.data2d.DataTable;
import mara.mybox.data2d.TmpTable;
import mara.mybox.data2d.operate.Data2DOperate;
import mara.mybox.data2d.operate.Data2DSingleColumn;
import mara.mybox.data2d.writer.DataFileCSVWriter;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.tools.CsvTools;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.TextFileTools;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 * @Author Mara
 * @CreateDate 2023-9-12
 * @License Apache License Version 2.0
 */
public class Data2DConvertTools {

    public static File targetFile(String prefix, Data2D_Attributes.TargetType type) {
        if (type == null) {
            return null;
        }
        String ext;
        switch (type) {
            case Excel:
                ext = "xlsx";
                break;
            case Text:
            case Matrix:
                ext = "txt";
                break;
            default:
                ext = type.name().toLowerCase();
                break;
        }
        return FileTmpTools.generateFile(prefix, ext);
    }

    public static DataFileCSV write(FxTask task, ResultSet results) {
        try {
            DataFileCSVWriter writer = new DataFileCSVWriter();
            writer.setPrintFile(FileTmpTools.getTempFile(".csv"));
            if (!Data2DTableTools.write(task, null, writer, results, null, 8, ColumnDefinition.InvalidAs.Empty)) {
                return null;
            }
            return (DataFileCSV) writer.getTargetData();
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
            return null;
        }
    }

    public static DataTable singleColumn(FxTask task, Data2D sourceData, List<Integer> cols) {
        if (sourceData == null) {
            return null;
        }
        try (Connection conn = DerbyBase.getConnection()) {
            List<Data2DColumn> columns = sourceData.getColumns();
            if (columns == null || columns.isEmpty()) {
                sourceData.loadColumns(conn);
            }
            if (columns == null || columns.isEmpty()) {
                return null;
            }
            List<Data2DColumn> referColumns = new ArrayList<>();
            referColumns.add(new Data2DColumn("data", ColumnDefinition.ColumnType.Double));
            DataTable dataTable = Data2DTableTools.createTable(task, conn,
                    TmpTable.tmpTableName(), referColumns, null, sourceData.getComments(), null, true);
            dataTable.setDataName(sourceData.getName());
            dataTable.copyDataAttributes(sourceData);
            if (cols == null || cols.isEmpty()) {
                cols = new ArrayList<>();
                for (int i = 0; i < columns.size(); i++) {
                    cols.add(i);
                }
            }
            Data2DOperate reader = Data2DSingleColumn.create(sourceData)
                    .setConn(conn).setWriterTable(dataTable)
                    .setCols(cols).setTask(task).start();
            if (reader != null && !reader.isFailed()) {
                conn.commit();
                return dataTable;
            } else {
                return null;
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
            return null;
        }
    }

    public static String toHtml(FxTask task, DataFileCSV data) {
        StringTable table = new StringTable(data.getDataName());
        File file = FileTools.removeBOM(task, data.getFile());
        if (file == null) {
            return null;
        }
        try (CSVParser parser = CSVParser.parse(file, data.getCharset(),
                CsvTools.csvFormat(data.getDelimiter(), data.isHasHeader()))) {
            if (data.isHasHeader()) {
                List<String> names = parser.getHeaderNames();
                table.setNames(names);
            }
            Iterator<CSVRecord> iterator = parser.iterator();
            while (iterator.hasNext() && (task == null || task.isWorking())) {
                CSVRecord csvRecord = iterator.next();
                if (csvRecord == null) {
                    continue;
                }
                List<String> htmlRow = new ArrayList<>();
                for (String v : csvRecord) {
                    htmlRow.add(v != null ? "<PRE><CODE>" + v + "</PRE></CODE>" : null);
                }
                table.add(htmlRow);
            }
            table.setComments(data.getComments());
            return table.html();
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
            return null;
        }
    }

    public static File toHtmlFile(FxTask task, DataFileCSV data, File htmlFile) {
        String html = toHtml(task, data);
        if (html == null) {
            return null;
        }
        return TextFileTools.writeFile(htmlFile, html);
    }

}
