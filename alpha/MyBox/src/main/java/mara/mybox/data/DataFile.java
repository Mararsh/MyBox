package mara.mybox.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.Charset;
import java.util.List;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-10-17
 * @License Apache License Version 2.0
 */
public abstract class DataFile extends Data2D {

    protected File file;
    protected Charset sourceCharset, targetCharset;
    protected boolean userSavedDataDefinition,
            sourceWithNames, autoDetermineSourceCharset, targetWithNames;
    protected List<ColumnDefinition> savedColumns;

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
        if (file == null || sourceCharset == null) {
            return null;
        }
        try (
                 BufferedReader reader = new BufferedReader(new FileReader(file, sourceCharset))) {
            String line1 = reader.readLine();
            if (line1 == null) {
                return null;
            }
            String[] delimiter = {",", " ", "    ", "        ", "\t", "|", "@",
                "#", ";", ":", "*", ".", "%", "$", "_", "&"};
            int[] count1 = new int[delimiter.length];
            int maxCount1 = 0, maxCountIndex1 = -1;
            for (int i = 0; i < delimiter.length; i++) {
                count1[i] = FindReplaceString.count(line1, delimiter[i]);
                MyBoxLog.console(">>" + delimiter[i] + "<<<   " + count1[i]);
                if (count1[i] > maxCount1) {
                    maxCount1 = count1[i];
                    maxCountIndex1 = i;
                }
            }
            MyBoxLog.console(maxCount1);
            String line2 = reader.readLine();
            if (line2 == null) {
                if (maxCountIndex1 >= 0) {
                    return delimiter[maxCountIndex1];
                } else {
                    return null;
                }
            }
            int[] count2 = new int[delimiter.length];
            int maxCount2 = 0, maxCountIndex2 = -1;
            for (int i = 0; i < delimiter.length; i++) {
                count2[i] = FindReplaceString.count(line2, delimiter[i]);
                MyBoxLog.console(">>" + delimiter[i] + "<<<   " + count1[i]);
                if (count1[i] == count2[i] && count2[i] > maxCount2) {
                    maxCount2 = count2[i];
                    maxCountIndex2 = i;
                }
            }
            MyBoxLog.console(maxCount2);
            if (maxCountIndex2 >= 0) {
                return delimiter[maxCountIndex2];
            } else {
                if (maxCountIndex1 >= 0) {
                    return delimiter[maxCountIndex1];
                } else {
                    return null;
                }
            }
        } catch (Exception e) {
        }
        return null;
    }

    @Override
    public void loadPageData(List<List<String>> data, List<ColumnDefinition> dataColumns) {
        file = null;
        super.loadPageData(data, dataColumns);
    }

    public void initFile() {
        totalRead = false;
        savedColumns = null;
        initData();
    }

    /*
        get/set
     */
    @Override
    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public Charset getSourceCharset() {
        return sourceCharset;
    }

    public void setSourceCharset(Charset sourceCharset) {
        this.sourceCharset = sourceCharset;
    }

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

    public boolean isSourceWithNames() {
        return sourceWithNames;
    }

    public void setSourceWithNames(boolean sourceWithNames) {
        this.sourceWithNames = sourceWithNames;
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

    public List<ColumnDefinition> getSavedColumns() {
        return savedColumns;
    }

    public void setSavedColumns(List<ColumnDefinition> savedColumns) {
        this.savedColumns = savedColumns;
    }

}
