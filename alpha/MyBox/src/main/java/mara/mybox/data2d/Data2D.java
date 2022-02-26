package mara.mybox.data2d;

import mara.mybox.dev.MyBoxLog;

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
            MyBoxLog.debug(e.toString());
            return null;
        }
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

}
