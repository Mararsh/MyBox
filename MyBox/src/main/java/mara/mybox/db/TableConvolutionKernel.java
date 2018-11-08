package mara.mybox.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import static mara.mybox.db.DerbyBase.protocol;
import mara.mybox.objects.ConvolutionKernel;
import mara.mybox.tools.DateTools;

/**
 * @Author Mara
 * @CreateDate 2018-11-6 20:54:43
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class TableConvolutionKernel extends DerbyBase {

    public TableConvolutionKernel() {
        Table_Name = "Convolution_Kernel";
        Keys = new ArrayList() {
            {
                add("name");
            }
        };
        Create_Table_Statement
                = " CREATE TABLE Convolution_Kernel ( "
                + "  name  VARCHAR(1024) NOT NULL PRIMARY KEY, "
                + "  width  INT  NOT NULL,  "
                + "  height  INT  NOT NULL,  "
                + "  type SMALLINT, "
                + "  gray SMALLINT, "
                + "  modify_time TIMESTAMP, "
                + "  create_time TIMESTAMP, "
                + "  description VARCHAR(1024)  "
                + " )";
    }

    public static List<ConvolutionKernel> read() {
        List<ConvolutionKernel> records = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(protocol + dbName + parameters);
                Statement statement = conn.createStatement()) {
            String sql = " SELECT * FROM Convolution_Kernel ORDER BY name";
            ResultSet kResult = statement.executeQuery(sql);
            while (kResult.next()) {
                ConvolutionKernel record = new ConvolutionKernel();
                String name = kResult.getString("name");
                int w = kResult.getInt("width");
                int h = kResult.getInt("height");
                record.setName(name);
                record.setWidth(w);
                record.setHeight(h);
                record.setType(kResult.getInt("type"));
                record.setGray(kResult.getInt("gray"));
                Date t = kResult.getTimestamp("create_time");
                if (t != null) {
                    record.setCreateTime(DateTools.datetimeToString(t));
                }
                t = kResult.getTimestamp("modify_time");
                if (t != null) {
                    record.setModifyTime(DateTools.datetimeToString(t));
                }
                record.setDescription(kResult.getString("description"));
                records.add(record);
            }
            for (ConvolutionKernel k : records) {
                int w = k.getWidth();
                int h = k.getHeight();
                float[][] matrix = new float[h][w];
                for (int j = 0; j < h; j++) {
                    for (int i = 0; i < w; i++) {
                        sql = " SELECT * FROM Float_Matrix WHERE name='" + k.getName()
                                + "' AND row=" + j + " AND col=" + i;
                        ResultSet mResult = statement.executeQuery(sql);
                        if (mResult.next()) {
                            matrix[j][i] = mResult.getFloat("value");
                        }
                    }
                }
                k.setMatrix(matrix);
            }

        } catch (Exception e) {
            logger.debug(e.toString());
        }
        return records;
    }

    public static ConvolutionKernel read(String name) {
        ConvolutionKernel record = null;
        if (name == null) {
            return record;
        }
        try (Connection conn = DriverManager.getConnection(protocol + dbName + parameters);
                Statement statement = conn.createStatement()) {
            String sql = " SELECT width FROM Convolution_Kernel WHERE name='" + name + "'";
            ResultSet kResult = statement.executeQuery(sql);
            if (kResult.next()) {
                record = new ConvolutionKernel();
                int w = kResult.getInt("width");
                int h = kResult.getInt("height");
                record.setName(name);
                record.setWidth(w);
                record.setHeight(h);
                record.setType(kResult.getInt("type"));
                record.setGray(kResult.getInt("gray"));
                Date t = kResult.getTimestamp("create_time");
                if (t != null) {
                    record.setCreateTime(DateTools.datetimeToString(t));
                }
                t = kResult.getTimestamp("modify_time");
                if (t != null) {
                    record.setModifyTime(DateTools.datetimeToString(t));
                }
                float[][] matrix = new float[h][w];
                for (int j = 0; j < h; j++) {
                    for (int i = 0; i < w; i++) {
                        sql = " SELECT * FROM Float_Matrix WHERE name='" + name
                                + "' AND row=" + j + " AND col=" + i;
                        ResultSet mResult = statement.executeQuery(sql);
                        if (mResult.next()) {
                            matrix[j][i] = mResult.getFloat("value");
                        }
                    }
                }
                record.setMatrix(matrix);
                record.setDescription(kResult.getString("description"));
            }
            if (record != null) {
                int w = record.getWidth();
                int h = record.getHeight();
                float[][] matrix = new float[h][w];
                for (int j = 0; j < h; j++) {
                    for (int i = 0; i < w; i++) {
                        sql = " SELECT * FROM Float_Matrix WHERE name='" + name
                                + "' AND row=" + j + " AND col=" + i;
                        ResultSet mResult = statement.executeQuery(sql);
                        if (mResult.next()) {
                            matrix[j][i] = mResult.getFloat("value");
                        }
                    }
                }
                record.setMatrix(matrix);
            }
            return record;
        } catch (Exception e) {
            logger.debug(e.toString());
            return record;
        }
    }

    public static boolean write(ConvolutionKernel record) {
        if (record == null || record.getName() == null
                || record.getWidth() < 3 || record.getWidth() % 2 == 0
                || record.getHeight() < 3 || record.getHeight() % 2 == 0) {
            return false;
        }
        try (Connection conn = DriverManager.getConnection(protocol + dbName + parameters);
                Statement statement = conn.createStatement()) {
            String sql = " SELECT width FROM Convolution_Kernel WHERE name='" + record.getName() + "'";
            if (statement.executeQuery(sql).next()) {
                sql = "UPDATE Convolution_Kernel "
                        + " SET width=" + record.getWidth()
                        + " , height=" + record.getHeight()
                        + " , type=" + record.getType()
                        + " , gray=" + record.getGray()
                        + " , create_time='" + record.getCreateTime() + "'"
                        + " , modify_time='" + record.getModifyTime() + "'"
                        + " , description='" + record.getDescription() + "'"
                        + " WHERE name='" + record.getName() + "'";
            } else {
                sql = "INSERT INTO Convolution_Kernel(name, width , height, type, gray, create_time, modify_time, description) VALUES('"
                        + record.getName() + "', " + record.getWidth() + ", " + record.getHeight() + ", "
                        + record.getType() + ", " + record.getGray() + ", '"
                        + record.getCreateTime() + "',  '" + record.getModifyTime() + "', '"
                        + record.getDescription() + "')";
            }
            statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            logger.debug(e.toString());
            return false;
        }
    }

    public static boolean writeExamples() {
        ConvolutionKernel.makeExample();
        try (Connection conn = DriverManager.getConnection(protocol + dbName + parameters);
                Statement statement = conn.createStatement()) {
            String sql;
            for (ConvolutionKernel k : ConvolutionKernel.ExampleKernels) {
                sql = " SELECT width FROM Convolution_Kernel WHERE name='" + k.getName() + "'";
                if (!statement.executeQuery(sql).next()) {
                    sql = "INSERT INTO Convolution_Kernel(name, width , height, type, gray, create_time, modify_time, description) VALUES('"
                            + k.getName() + "', " + k.getWidth() + ", " + k.getHeight() + ", "
                            + k.getType() + ", " + k.getGray() + ", '"
                            + k.getCreateTime() + "',  '" + k.getModifyTime() + "', '"
                            + k.getDescription() + "')";
                    statement.executeUpdate(sql);
                }
            }
            return true;
        } catch (Exception e) {
            logger.debug(e.toString());
            return false;
        }
    }

    public static boolean deleteRecords(List<ConvolutionKernel> records) {
        if (records == null || records.isEmpty()) {
            return false;
        }
        try (Connection conn = DriverManager.getConnection(protocol + dbName + parameters);
                Statement statement = conn.createStatement()) {
            String sql;
            for (ConvolutionKernel a : records) {
                sql = "DELETE FROM Convolution_Kernel WHERE name='" + a.getName() + "'";
                statement.executeUpdate(sql);
            }
            return true;
        } catch (Exception e) {
            logger.debug(e.toString());
            return false;
        }
    }

    public static boolean delete(List<String> names) {
        if (names == null || names.isEmpty()) {
            return false;
        }
        try (Connection conn = DriverManager.getConnection(protocol + dbName + parameters);
                Statement statement = conn.createStatement()) {
            String sql;
            for (String name : names) {
                sql = "DELETE FROM Convolution_Kernel WHERE name='" + name + "'";
                statement.executeUpdate(sql);
            }
            return true;
        } catch (Exception e) {
            logger.debug(e.toString());
            return false;
        }
    }

    public static float[][] readMatrix(String name) {
        float[][] matrix = null;
        if (name == null) {
            return matrix;
        }
        ConvolutionKernel k = read(name);
        if (k == null) {
            return matrix;
        }
        return TableFloatMatrix.read(name, k.getWidth(), k.getHeight());
    }

}
