package mara.mybox.db.data;

import java.io.File;
import java.util.Date;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2020-9-7
 * @License Apache License Version 2.0
 */
public class FileBackup extends BaseData {

    public static final int Default_Max_Backups = 20;

    protected long fbid;
    protected File file, backup;
    protected Date recordTime;

    private void init() {
        fbid = -1;
        file = null;
        backup = null;
        recordTime = null;
    }

    public FileBackup() {
        init();
    }

    public FileBackup(File file, File backup) {
        fbid = -1;
        this.file = file;
        this.backup = backup;
        recordTime = new Date();
    }

    public String getName() {
        return backup != null ? backup.getAbsolutePath() : null;
    }

    public long getSize() {
        return backup != null ? backup.length() : 0;
    }

    /*
        Static methods
     */
    public static FileBackup create() {
        return new FileBackup();
    }

    public static boolean setValue(FileBackup data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        try {
            switch (column) {
                case "fbid":
                    data.setFbid(value == null ? -1 : (long) value);
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
                case "backup":
                    data.setBackup(null);
                    if (value != null) {
                        File f = new File((String) value);
                        if (f.exists()) {
                            data.setBackup(f);
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

    public static Object getValue(FileBackup data, String column) {
        if (data == null || column == null) {
            return null;
        }
        switch (column) {
            case "fbid":
                return data.getFbid();
            case "file":
                return data.getFile() != null ? data.getFile().getAbsolutePath() : null;
            case "backup":
                return data.getBackup() != null ? data.getBackup().getAbsolutePath() : null;
            case "record_time":
                return data.getRecordTime();
        }
        return null;
    }

    public static boolean valid(FileBackup data) {
        return data != null
                && data.getFile() != null && data.getFile().exists()
                && data.getBackup() != null && data.getBackup().exists()
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
    public long getFbid() {
        return fbid;
    }

    public void setFbid(long fbid) {
        this.fbid = fbid;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public File getBackup() {
        return backup;
    }

    public void setBackup(File backup) {
        this.backup = backup;
    }

    public Date getRecordTime() {
        return recordTime;
    }

    public void setRecordTime(Date recordTime) {
        this.recordTime = recordTime;
    }

}
