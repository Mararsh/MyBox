package mara.mybox.data;

import mara.mybox.db.data.BaseData;
import java.io.File;
import java.util.Date;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2020-9-7
 * @License Apache License Version 2.0
 */
public class FileHistory extends BaseData {

    protected long fhid;
    protected String category;
    protected File history, file;
    protected Date recordTime;

    private void init() {
        fhid = -1;
        category = null;
        recordTime = null;
        history = null;
        file = null;
    }

    public FileHistory() {
        init();
    }

    /*
        customized get/set
     */
    public static boolean setValue(FileHistory data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        try {
            switch (column) {
                case "fhid":
                    data.setFhid(value == null ? -1 : (long) value);
                    return true;
                case "category":
                    data.setCategory(value == null ? null : (String) value);
                    return true;
                case "history":
                    data.setHistory(null);
                    if (value != null) {
                        File f = new File((String) value);
                        if (f.exists()) {
                            data.setHistory(f);
                        }
                    }
                    return true;
                case "file":
                    data.setFile(null);
                    if (value != null) {
                        File f = new File((String) value);
                        if (f.exists()) {
                            data.setFile(f);
                        }
                    }
                    return true;
                case "record_time":
                    data.setRecordTime(value == null ? null : (Date) value);
                    return true;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return false;
    }

    public static Object getValue(FileHistory data, String column) {
        if (data == null || column == null) {
            return null;
        }
        switch (column) {
            case "fhid":
                return data.getFhid();
            case "category":
                return data.getCategory();
            case "history":
                return data.getHistory() != null ? data.getHistory().getAbsolutePath() : null;
            case "file":
                return data.getFile() != null ? data.getFile().getAbsolutePath() : null;
            case "record_time":
                return data.getRecordTime();
        }
        return null;
    }

    public static boolean valid(FileHistory data) {
        return data != null
                && data.getHistory() != null && data.getHistory().exists()
                && data.getFile() != null && data.getFile().exists()
                && data.getRecordTime() != null;
    }

    /*
        customized get/set
     */
    public void setFilename(String string) {
        if (string != null) {
            File f = new File((String) string);
            if (f.exists()) {
                file = f;
            }
        }
    }

    /*
        get/set
     */
    public long getFhid() {
        return fhid;
    }

    public FileHistory setFhid(long fhid) {
        this.fhid = fhid;
        return this;
    }

    public File getHistory() {
        return history;
    }

    public FileHistory setHistory(File history) {
        this.history = history;
        return this;
    }

    public File getFile() {
        return file;
    }

    public FileHistory setFile(File file) {
        this.file = file;
        return this;
    }

    public Date getRecordTime() {
        return recordTime;
    }

    public FileHistory setRecordTime(Date recordTime) {
        this.recordTime = recordTime;
        return this;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

}
