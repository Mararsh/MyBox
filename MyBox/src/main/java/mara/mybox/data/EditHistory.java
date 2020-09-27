package mara.mybox.data;

import java.io.File;
import mara.mybox.db.TableBase;
import mara.mybox.db.TableEditHistory;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.logger;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2020-9-7
 * @License Apache License Version 2.0
 */
public class EditHistory extends TableData {

    protected long ehid;
    protected File history, file;
    protected long createTime;

    private void init() {
        createTime = CommonValues.InvalidLong;
        history = null;
        file = null;
    }

    public EditHistory() {
        init();
    }

    public EditHistory(File file, File history, long createTime) {
        this.createTime = createTime;
        this.history = history;
        this.file = file;
    }

    @Override
    public TableBase getTable() {
        if (table == null) {
            table = new TableEditHistory();
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
                case "create_time":
                    createTime = value == null ? CommonValues.InvalidLong : (long) value;
                    return true;
            }
        } catch (Exception e) {
            logger.debug(e.toString());
        }
        return false;
    }

    @Override
    public Object getValue(String column) {
        if (column == null) {
            return null;
        }
        switch (column) {
            case "history":
                return history;
            case "file":
                return file;
            case "create_time":
                return createTime;
        }
        return null;
    }

    @Override
    public boolean valid() {
        return history != null && history.exists()
                && file != null && file.exists()
                && createTime > 0;
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
    public long getEhid() {
        return ehid;
    }

    public void setEhid(long ehid) {
        this.ehid = ehid;
    }

    public File getHistory() {
        return history;
    }

    public void setHistory(File history) {
        this.history = history;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

}
