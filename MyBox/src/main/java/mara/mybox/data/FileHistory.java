package mara.mybox.data;

import java.io.File;
import java.util.Date;
import mara.mybox.db.TableBase;
import mara.mybox.db.TableMyBoxLog;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2020-9-7
 * @License Apache License Version 2.0
 */
public class FileHistory extends TableData {

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

    @Override
    public TableBase getTable() {
        if (table == null) {
            table = new TableMyBoxLog();
        }
        return table;
    }

    @Override
    public boolean setValue(String column, Object value) {
        if (column == null) {
            return false;
        }
        try {
            switch (column) {
                case "fhid":
                    fhid = value == null ? -1 : (long) value;
                    return true;
                case "category":
                    category = value == null ? null : (String) value;
                    return true;
                case "history":
                    history = null;
                    if (value != null) {
                        File f = new File((String) value);
                        if (f.exists()) {
                            history = f;
                        }
                    }
                    return true;
                case "file":
                    file = null;
                    if (value != null) {
                        File f = new File((String) value);
                        if (f.exists()) {
                            file = f;
                        }
                    }
                    return true;
                case "record_time":
                    recordTime = value == null ? null : (Date) value;
                    return true;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return false;
    }

    @Override
    public Object getValue(String column) {
        if (column == null) {
            return null;
        }
        switch (column) {
            case "fhid":
                return fhid;
            case "category":
                return category;
            case "history":
                return history != null ? history.getAbsolutePath() : null;
            case "file":
                return file != null ? file.getAbsolutePath() : null;
            case "record_time":
                return recordTime;
        }
        return null;
    }

    @Override
    public boolean valid() {
        return category != null
                && history != null && history.exists()
                && file != null && file.exists()
                && recordTime != null;
    }

    public void setFile(String string) {
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
