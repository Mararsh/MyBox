package mara.mybox.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.data.ConvolutionKernel;
import static mara.mybox.db.DerbyBase.protocol;

/**
 * @Author Mara
 * @CreateDate 2018-11-7
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class TableDoubleMatrix extends DerbyBase {

    public TableDoubleMatrix() {
        Table_Name = "Double_Matrix";
        Keys = new ArrayList<>() {
            {
                add("name");
                add("row");
                add("col");
            }
        };
        Create_Table_Statement
                = " CREATE TABLE Double_Matrix ( "
                + "  name  VARCHAR(1024) NOT NULL, "
                + "  row  INT  NOT NULL,  "
                + "  col INT  NOT NULL,  "
                + "  value DOUBLE  NOT NULL, "
                + "  PRIMARY KEY (name, row, col)"
                + " )";
    }

    public static float[][] read(String name, int width, int height) {
        float[][] matrix = new float[height][width];
        try ( Connection conn = DriverManager.getConnection(protocol + dbName() + login);
                 Statement statement = conn.createStatement()) {
            for (int j = 0; j < height; j++) {
                for (int i = 0; i < width; i++) {
                    String sql = " SELECT * FROM Double_Matrix WHERE name='" + name
                            + "' AND row=" + j + " AND col=" + i;
                    ResultSet result = statement.executeQuery(sql);
                    if (result.next()) {
                        matrix[j][i] = result.getFloat("value");
                    }
                }
            }
        } catch (Exception e) {  failed(e);
            // logger.debug(e.toString());
        }
        return matrix;
    }

    public static boolean write(String name, float[][] values) {
        if (name == null || values == null) {
            return false;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbName() + login);
                 Statement statement = conn.createStatement()) {
            String sql = "DELETE FROM Double_Matrix WHERE name='" + name + "'";
            statement.executeUpdate(sql);
            for (int j = 0; j < values.length; j++) {
                for (int i = 0; i < values[j].length; i++) {
                    float v = values[j][i];
                    sql = "INSERT INTO Double_Matrix(name, row , col, value) VALUES('"
                            + name + "', " + j + ", " + i + ", " + v + ")";
                    statement.executeUpdate(sql);
                }
            }
            return true;
        } catch (Exception e) {  failed(e);
            // logger.debug(e.toString());
            return false;
        }
    }

    public static boolean write(String name, int row, int col, float value) {
        if (name == null || row < 0 || col < 0) {
            return false;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbName() + login);
                 Statement statement = conn.createStatement()) {
            String sql = " SELECT * FROM Double_Matrix WHERE name='" + name
                    + "' AND row=" + row + " AND col=" + col;
            if (statement.executeQuery(sql).next()) {
                sql = "UPDATE Double_Matrix "
                        + " SET value=" + value
                        + " WHERE name='" + name + "' AND row=" + row + " AND col=" + col;
            } else {
                sql = "INSERT INTO Double_Matrix(name, row , col, value) VALUES('"
                        + name + "', " + row + ", " + col + ", " + value + ")";
            }
            statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {  failed(e);
            // logger.debug(e.toString());
            return false;
        }
    }

    public static boolean delete(String name, int row, int col) {
        if (name == null || row < 0 || col < 0) {
            return false;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbName() + login);
                 Statement statement = conn.createStatement()) {
            String sql = "DELETE FROM Double_Matrix WHERE name='" + name
                    + "' AND row=" + row + " AND col=" + col;
            statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {  failed(e);
            // logger.debug(e.toString());
            return false;
        }
    }

    public static boolean delete(String name) {
        if (name == null) {
            return false;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbName() + login);
                 Statement statement = conn.createStatement()) {
            String sql = "DELETE FROM Double_Matrix WHERE name='" + name + "'";
            statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {  failed(e);
            // logger.debug(e.toString());
            return false;
        }
    }

    public static boolean delete(List<String> names) {
        if (names == null || names.isEmpty()) {
            return false;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbName() + login);
                 Statement statement = conn.createStatement()) {
            conn.setAutoCommit(false);
            for (String name : names) {
                String sql = "DELETE FROM Double_Matrix WHERE name='" + name + "'";
                statement.executeUpdate(sql);
            }
            conn.commit();
            return true;
        } catch (Exception e) {  failed(e);
            // logger.debug(e.toString());
            return false;
        }
    }

    public static boolean writeExamples() {
        ConvolutionKernel.makeExample();
        try ( Connection conn = DriverManager.getConnection(protocol + dbName() + login);
                 Statement statement = conn.createStatement()) {
            String sql;
            for (ConvolutionKernel k : ConvolutionKernel.ExampleKernels) {
                String name = k.getName();
                sql = " SELECT row FROM Double_Matrix WHERE name='" + name + "'";
                if (!statement.executeQuery(sql).next()) {
                    float[][] m = k.getMatrix();
                    conn.setAutoCommit(false);
                    for (int j = 0; j < m.length; j++) {
                        for (int i = 0; i < m[j].length; i++) {
                            float v = m[j][i];
                            sql = "INSERT INTO Double_Matrix(name, row , col, value) VALUES('"
                                    + name + "', " + j + ", " + i + ", " + v + ")";
                            statement.executeUpdate(sql);
                        }
                    }
                    conn.commit();
                }
            }
            return true;
        } catch (Exception e) {  failed(e);
            // logger.debug(e.toString());
            return false;
        }
    }

}
