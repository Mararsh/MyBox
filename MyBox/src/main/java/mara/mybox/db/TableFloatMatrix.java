package mara.mybox.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.data.ConvolutionKernel;
import static mara.mybox.db.DerbyBase.protocol;
import static mara.mybox.value.AppVariables.logger;

/**
 * @Author Mara
 * @CreateDate 2018-11-7
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class TableFloatMatrix extends DerbyBase {

    public TableFloatMatrix() {
        Table_Name = "Float_Matrix";
        Keys = new ArrayList<>() {
            {
                add("name");
                add("row");
                add("col");
            }
        };
        Create_Table_Statement
                = " CREATE TABLE Float_Matrix ( "
                + "  name  VARCHAR(1024) NOT NULL, "
                + "  row  INT  NOT NULL,  "
                + "  col INT  NOT NULL,  "
                + "  value FLOAT  NOT NULL, "
                + "  PRIMARY KEY (name, row, col)"
                + " )";
    }

    public static float[][] read(String name, int width, int height) {
        float[][] matrix = new float[height][width];
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            conn.setReadOnly(true);
            try ( PreparedStatement statement = conn.prepareStatement(" SELECT * FROM Float_Matrix WHERE name=? AND row=? AND col=?")) {
                for (int j = 0; j < height; ++j) {
                    for (int i = 0; i < width; ++i) {
                        statement.setString(1, name);
                        statement.setInt(2, j);
                        statement.setInt(3, i);
                        try ( ResultSet result = statement.executeQuery()) {
                            if (result.next()) {
                                matrix[j][i] = result.getFloat("value");
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
        }
        return matrix;
    }

    public static boolean write(String name, float[][] values) {
        if (name == null || values == null) {
            return false;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            try ( PreparedStatement statement = conn.prepareStatement(
                    "DELETE FROM Float_Matrix WHERE name=?")) {
                statement.setString(1, name);
                statement.executeUpdate();
            }
            conn.setAutoCommit(false);
            try ( PreparedStatement insert = conn.prepareStatement(
                    "INSERT INTO Float_Matrix(name, row , col, value) VALUES(?,?,?,?)")) {
                for (int j = 0; j < values.length; ++j) {
                    for (int i = 0; i < values[j].length; ++i) {
                        float v = values[j][i];
                        insert.setString(1, name);
                        insert.setInt(2, j);
                        insert.setInt(3, i);
                        insert.setFloat(4, v);
                        insert.executeUpdate();
                    }
                }
            }
            conn.commit();
            return true;
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
            return false;
        }
    }

    public static boolean write(String name, int row, int col, float value) {
        if (name == null || row < 0 || col < 0) {
            return false;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 PreparedStatement statement = conn.prepareStatement(" SELECT * FROM Float_Matrix WHERE name=? AND row=? AND col=?")) {
            statement.setString(1, name);
            statement.setInt(2, row);
            statement.setInt(3, col);
            try ( ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    try ( PreparedStatement update = conn.prepareStatement("UPDATE Float_Matrix SET value=? WHERE name=? AND row=? AND col=?")) {
                        update.setFloat(1, value);
                        update.setString(2, name);
                        update.setInt(3, row);
                        update.setInt(4, col);
                        update.executeUpdate();
                    }
                } else {
                    try ( PreparedStatement insert = conn.prepareStatement("INSERT INTO Float_Matrix(name, row , col, value) VALUES(?,?,?,?)")) {
                        insert.setString(1, name);
                        insert.setInt(2, row);
                        insert.setInt(3, col);
                        insert.setFloat(4, value);
                        insert.executeUpdate();
                    }
                }
            }
            return true;
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
            return false;
        }
    }

    public static boolean delete(String name, int row, int col) {
        if (name == null || row < 0 || col < 0) {
            return false;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            try ( PreparedStatement statement = conn.prepareStatement(
                    "DELETE FROM Float_Matrix WHERE name=? AND row=? AND col=?")) {
                statement.setString(1, name);
                statement.setInt(2, row);
                statement.setInt(3, col);
                statement.executeUpdate();
            }
            return true;
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
            return false;
        }
    }

    public static boolean delete(String name) {
        if (name == null) {
            return false;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            try ( PreparedStatement statement = conn.prepareStatement(
                    "DELETE FROM Float_Matrix WHERE name=?")) {
                statement.setString(1, name);
                statement.executeUpdate();
            }
            return true;
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
            return false;
        }
    }

    public static boolean delete(List<String> names) {
        if (names == null || names.isEmpty()) {
            return false;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            conn.setAutoCommit(false);
            try ( PreparedStatement statement = conn.prepareStatement(
                    "DELETE FROM Float_Matrix WHERE name=?")) {
                for (int i = 0; i < names.size(); ++i) {
                    statement.setString(1, names.get(i));
                    statement.executeUpdate();
                }
            }
            conn.commit();
            return true;
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
            return false;
        }
    }

    public static boolean writeExamples() {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 PreparedStatement query = conn.prepareStatement(" SELECT row FROM Float_Matrix WHERE name=?");
                 PreparedStatement insert = conn.prepareStatement("INSERT INTO Float_Matrix(name, row , col, value) VALUES(?,?,?,?)")) {
            for (ConvolutionKernel k : ConvolutionKernel.makeExample()) {
                String name = k.getName();
                query.setString(1, name);
                try ( ResultSet result = query.executeQuery()) {
                    if (!result.next()) {
                        float[][] m = k.getMatrix();
                        for (int j = 0; j < m.length; ++j) {
                            for (int i = 0; i < m[j].length; ++i) {
                                float v = m[j][i];
                                insert.setString(1, name);
                                insert.setInt(2, j);
                                insert.setInt(3, i);
                                insert.setFloat(4, v);
                                insert.executeUpdate();
                            }
                        }
                    }
                }
            }
            return true;
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
            return false;
        }
    }

}
