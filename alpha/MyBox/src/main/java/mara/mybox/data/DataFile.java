package mara.mybox.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.Charset;
import java.util.List;
import mara.mybox.controller.BaseData2DFileController;
import mara.mybox.db.data.Data2DColumn;

/**
 * @Author Mara
 * @CreateDate 2021-10-17
 * @License Apache License Version 2.0
 */
public abstract class DataFile extends Data2D {

    protected BaseData2DFileController fileController;
    protected Charset targetCharset;
    protected boolean userSavedDataDefinition, autoDetermineSourceCharset, targetWithNames;
    protected List<Data2DColumn> savedColumns;

    @Override
    public String titleName() {
        if (file == null) {
            return "";
        }
        return file.getAbsolutePath();
    }

    @Override
    public void newData() {
        file = null;
        super.newData();
    }

    public String guessDelimiter() {
        hasHeader = false;
        if (file == null || charset == null) {
            return null;
        }
        try ( BufferedReader reader = new BufferedReader(new FileReader(file, charset))) {
            String line1 = reader.readLine();
            if (line1 == null) {
                return null;
            }
            String[] values = {",", " ", "    ", "        ", "\t", "|", "@",
                "#", ";", ":", "*", ".", "%", "$", "_", "&"};
            int[] count1 = new int[values.length];
            int maxCount1 = 0, maxCountIndex1 = -1;
            for (int i = 0; i < values.length; i++) {
                count1[i] = FindReplaceString.count(line1, values[i]);
//                MyBoxLog.console(">>" + values[i] + "<<<   " + count1[i]);
                if (count1[i] > maxCount1) {
                    maxCount1 = count1[i];
                    maxCountIndex1 = i;
                }
            }
//            MyBoxLog.console(maxCount1);
            String line2 = reader.readLine();
            if (line2 == null) {
                if (maxCountIndex1 >= 0) {
                    hasHeader = true;
                    return values[maxCountIndex1];
                } else {
                    return null;
                }
            }
            int[] count2 = new int[values.length];
            int maxCount2 = 0, maxCountIndex2 = -1;
            for (int i = 0; i < values.length; i++) {
                count2[i] = FindReplaceString.count(line2, values[i]);
//                MyBoxLog.console(">>" + values[i] + "<<<   " + count1[i]);
                if (count1[i] == count2[i] && count2[i] > maxCount2) {
                    maxCount2 = count2[i];
                    maxCountIndex2 = i;
                }
            }
//            MyBoxLog.console(maxCount2);
            if (maxCountIndex2 >= 0) {
                hasHeader = true;
                return values[maxCountIndex2];
            } else {
                if (maxCountIndex1 >= 0) {
                    hasHeader = true;
                    return values[maxCountIndex1];
                } else {
                    return null;
                }
            }
        } catch (Exception e) {
        }
        return null;
    }

    public void initFile(File file) {
        resetData();
        this.file = file;
        savedColumns = null;
    }

    @Override
    public void savePageData() {
        if (fileController != null) {
            fileController.saveAction();
        }
    }

    /*
        get/set
     */
    public Charset getTargetCharset() {
        return targetCharset;
    }

    public void setTargetCharset(Charset targetCharset) {
        this.targetCharset = targetCharset;
    }

    public boolean isUserSavedDataDefinition() {
        return userSavedDataDefinition;
    }

    public void setUserSavedDataDefinition(boolean userSavedDataDefinition) {
        this.userSavedDataDefinition = userSavedDataDefinition;
    }

    public boolean isAutoDetermineSourceCharset() {
        return autoDetermineSourceCharset;
    }

    public void setAutoDetermineSourceCharset(boolean autoDetermineSourceCharset) {
        this.autoDetermineSourceCharset = autoDetermineSourceCharset;
    }

    public boolean isTargetWithNames() {
        return targetWithNames;
    }

    public void setTargetWithNames(boolean targetWithNames) {
        this.targetWithNames = targetWithNames;
    }

    public List<Data2DColumn> getSavedColumns() {
        return savedColumns;
    }

    public void setSavedColumns(List<Data2DColumn> savedColumns) {
        this.savedColumns = savedColumns;
    }

    public BaseData2DFileController getFileController() {
        return fileController;
    }

    public void setFileController(BaseData2DFileController fileController) {
        this.fileController = fileController;
    }

}
