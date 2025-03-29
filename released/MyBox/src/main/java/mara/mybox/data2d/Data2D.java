package mara.mybox.data2d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mara.mybox.data.StringTable;
import mara.mybox.data2d.tools.Data2DDefinitionTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.StringTools;
import mara.mybox.tools.TextTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-10-18
 * @License Apache License Version 2.0
 */
public abstract class Data2D extends Data2D_Operations {

    @Override
    public Data2D cloneAll() {
        try {
            Data2D newData = (Data2D) super.clone();
            newData.cloneData(this);
            return newData;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    @Override
    public String info() {
        return Data2DDefinitionTools.dataInfo(this);
    }

    public String pageInfo() {
        StringTable infoTable = new StringTable();
        List<String> row = new ArrayList<>();
        row.addAll(Arrays.asList(message("Type"), getTypeName()));
        infoTable.add(row);
        if (file != null) {
            row = new ArrayList<>();
            row.addAll(Arrays.asList(message("File"), file.getAbsolutePath()));
            infoTable.add(row);
            row = new ArrayList<>();
            row.addAll(Arrays.asList(message("FileSize"), FileTools.showFileSize(file.length()) + ""));
            infoTable.add(row);
            row = new ArrayList<>();
            row.addAll(Arrays.asList(message("FileModifyTime"), DateTools.datetimeToString(file.lastModified())));
            infoTable.add(row);
            if (isExcel()) {
                DataFileExcel e = (DataFileExcel) this;
                row = new ArrayList<>();
                row.addAll(Arrays.asList(message("CurrentSheet"), (sheet == null ? "" : sheet)
                        + (e.getSheetNames() == null ? "" : " / " + e.getSheetNames().size())));
                infoTable.add(row);
            } else {
                row = new ArrayList<>();
                row.addAll(Arrays.asList(message("Charset"), charset.name()));
                infoTable.add(row);
                row = new ArrayList<>();
                row.addAll(Arrays.asList(message("Delimiter"), TextTools.delimiterMessage(delimiter)));
                infoTable.add(row);
            }
            row = new ArrayList<>();
            row.addAll(Arrays.asList(message("FirstLineAsNames"), (hasHeader ? message("Yes") : message("No"))));
            infoTable.add(row);
        }
        int tableRowsNumber = tableRowsNumber();
        if (isMutiplePages()) {
            row = new ArrayList<>();
            row.addAll(Arrays.asList(message("RowsNumberInFile"), pagination.rowsNumber + ""));
            infoTable.add(row);
        } else {
            row = new ArrayList<>();
            row.addAll(Arrays.asList(message("RowsNumber"), tableRowsNumber + ""));
            infoTable.add(row);
        }
        row = new ArrayList<>();
        row.addAll(Arrays.asList(message("ColumnsNumber"), columnsNumber() + ""));
        infoTable.add(row);
        row = new ArrayList<>();
        row.addAll(Arrays.asList(message("CurrentPage"),
                StringTools.format(pagination.currentPage + 1)
                + " / " + StringTools.format(pagination.pagesNumber)));
        infoTable.add(row);
        if (isMutiplePages()) {
            row = new ArrayList<>();
            row.addAll(Arrays.asList(message("RowsRangeInPage"),
                    StringTools.format(pagination.startRowOfCurrentPage + 1) + " - "
                    + StringTools.format(pagination.startRowOfCurrentPage + tableRowsNumber)
                    + " ( " + StringTools.format(tableRowsNumber) + " )"));
            infoTable.add(row);
        }
        row = new ArrayList<>();
        row.addAll(Arrays.asList(message("PageModifyTime"), DateTools.nowString()));
        infoTable.add(row);
        return infoTable.div();
    }

    public String dataInfo() {
        return pageInfo() + "<BR>" + Data2DDefinitionTools.toHtml(this);
    }

    /*
        static
     */
    public static Data2D create(DataType type) {
        if (type == null) {
            return null;
        }
        Data2D data;
        switch (type) {
            case CSV:
                data = new DataFileCSV();
                break;
            case Excel:
                data = new DataFileExcel();
                break;
            case Texts:
                data = new DataFileText();
                break;
            case Matrix:
                data = new DataMatrix();
                break;
            case MyBoxClipboard:
                data = new DataClipboard();
                break;
            case DatabaseTable:
                data = new DataTable();
                break;
            case InternalTable:
                data = new DataInternalTable();
                break;
            default:
                return null;
        }
        data.setType(type);
        return data;
    }

    public int drop() {
        if (!FileDeleteTools.delete(file)) {
            return -1;
        }
        return tableData2DDefinition.deleteData(this);
    }
}
