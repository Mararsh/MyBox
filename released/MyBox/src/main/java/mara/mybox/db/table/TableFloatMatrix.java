package mara.mybox.db.table;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.BaseData;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ConvolutionKernel;
import static mara.mybox.db.table.BaseTable.StringMaxLength;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2018-11-7
 * @License Apache License Version 2.0
 */
public class TableFloatMatrix extends BaseTable<BaseData> {

    public TableFloatMatrix() {
        tableName = "Float_Matrix";
        defineColumns();
    }

    public TableFloatMatrix(boolean defineColumns) {
        tableName = "Float_Matrix";
        if (defineColumns) {
            defineColumns();
        }
    }

    public final TableFloatMatrix defineColumns() {
        addColumn(new ColumnDefinition("name", ColumnDefinition.ColumnType.String, true, true).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("row", ColumnDefinition.ColumnType.Integer, true, true));
        addColumn(new ColumnDefinition("col", ColumnDefinition.ColumnType.Integer, true, true));
        addColumn(new ColumnDefinition("value", ColumnDefinition.ColumnType.Float, true));
        return this;
    }

    public static float[][] read(String name, int width, int height) {
        float[][] matrix = new float[height][width];
        try ( Connection conn = DerbyBase.getConnection()) {
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
            MyBoxLog.error(e);
        }
        return matrix;
    }

    public static boolean write(String name, float[][] values) {
        if (name == null || values == null) {
            return false;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
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
            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean write(String name, int row, int col, float value) {
        if (name == null || row < 0 || col < 0) {
            return false;
        }
        try ( Connection conn = DerbyBase.getConnection();
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
            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean delete(String name, int row, int col) {
        if (name == null || row < 0 || col < 0) {
            return false;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            try ( PreparedStatement statement = conn.prepareStatement(
                    "DELETE FROM Float_Matrix WHERE name=? AND row=? AND col=?")) {
                statement.setString(1, name);
                statement.setInt(2, row);
                statement.setInt(3, col);
                statement.executeUpdate();
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean delete(String name) {
        if (name == null) {
            return false;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            try ( PreparedStatement statement = conn.prepareStatement(
                    "DELETE FROM Float_Matrix WHERE name=?")) {
                statement.setString(1, name);
                statement.executeUpdate();
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean delete(List<String> names) {
        if (names == null || names.isEmpty()) {
            return false;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
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
            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean writeExamples() {
        try ( Connection conn = DerbyBase.getConnection();
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
            MyBoxLog.error(e);
            return false;
        }
    }

}
