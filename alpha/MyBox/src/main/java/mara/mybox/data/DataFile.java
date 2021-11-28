package mara.mybox.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.charset.Charset;
import java.sql.Connection;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.tools.TextFileTools;

/**
 * @Author Mara
 * @CreateDate 2021-10-17
 * @License Apache License Version 2.0
 */
public abstract class DataFile extends Data2D {

    @Override
    public Data2DDefinition queryDefinition(Connection conn) {
        return tableData2DDefinition.queryFile(conn, type, file);
    }

    @Override
    public String titleName() {
        if (file == null) {
            return "";
        }
        return file.getAbsolutePath();
    }

    public String guessDelimiter(String[] values) {
        if (file == null || values == null) {
            return null;
        }
        if (charset == null) {
            charset = TextFileTools.charset(file);
        }
        if (charset == null) {
            charset = Charset.forName("UTF-8");
        }
        try ( BufferedReader reader = new BufferedReader(new FileReader(file, charset))) {
            String line1 = reader.readLine();
            if (line1 == null) {
                return null;
            }
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
                    return values[maxCountIndex1];
                } else {
                    hasHeader = false;
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
                return values[maxCountIndex2];
            } else {
                if (maxCountIndex1 >= 0) {
                    return values[maxCountIndex1];
                } else {
                    hasHeader = false;
                    return null;
                }
            }
        } catch (Exception e) {
        }
        hasHeader = false;
        return null;
    }

    public void asTmpFile() {
        file = tmpFile();
        hasHeader = false;
        userSavedDataDefinition = false;
    }

}
