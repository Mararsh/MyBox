package mara.mybox.data2d;

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
            newData.cloneAll(this);
            return newData;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    public String info() {
        String info = message("Type") + ": " + message(type.name());
        if (file != null) {
            info = message("File") + ": " + file + "\n"
                    + message("FileSize") + ": " + FileTools.showFileSize(file.length()) + "\n"
                    + message("FileModifyTime") + ": " + DateTools.datetimeToString(file.lastModified()) + "\n";
            if (isExcel()) {
                DataFileExcel e = (DataFileExcel) this;
                info += message("CurrentSheet") + ": " + (sheet == null ? "" : sheet)
                        + (e.getSheetNames() == null ? "" : " / " + e.getSheetNames().size()) + "\n";
            } else {
                info += message("Charset") + ": " + charset + "\n"
                        + message("Delimiter") + ": " + TextTools.delimiterMessage(delimiter) + "\n";
            }
            info += message("FirstLineAsNames") + ": " + (hasHeader ? message("Yes") : message("No")) + "\n";
        }
        int tableRowsNumber = tableRowsNumber();
        if (isMutiplePages()) {
            info += message("RowsNumberInFile") + ": " + dataSize + "\n";
        } else {
            info += message("RowsNumber") + ": " + tableRowsNumber + "\n";
        }
        info += message("ColumnsNumber") + ": " + columnsNumber() + "\n"
                + message("CurrentPage") + ": " + StringTools.format(currentPage + 1)
                + " / " + StringTools.format(pagesNumber) + "\n";
        if (isMutiplePages() && hasData()) {
            info += message("RowsRangeInPage")
                    + ": " + StringTools.format(startRowOfCurrentPage + 1) + " - "
                    + StringTools.format(startRowOfCurrentPage + tableRowsNumber)
                    + " ( " + StringTools.format(tableRowsNumber) + " )\n";
        }
        info += message("PageModifyTime") + ": " + DateTools.nowString();
        return info;
    }


    /*
        static
     */
    public static Data2D create(Type type) {
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
