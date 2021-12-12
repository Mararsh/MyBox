package mara.mybox.db.table;

import mara.mybox.db.DerbyBase;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import mara.mybox.db.data.ConvolutionKernel;
import mara.mybox.tools.DateTools;
import mara.mybox.dev.MyBoxLog;

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
        Keys = new ArrayList<>() {
            {
                add("name");
            }
        };
        Create_Table_Statement
                = " CREATE TABLE Convolution_Kernel ( "
                + "  name  VARCHAR(32672) NOT NULL PRIMARY KEY, "
                + "  width  INT  NOT NULL,  "
                + "  height  INT  NOT NULL,  "
                + "  type SMALLINT, "
                + "  edge SMALLINT, "
                + "  is_gray BOOLEAN, "
                + "  is_invert BOOLEAN, "
                + "  modify_time TIMESTAMP, "
                + "  create_time TIMESTAMP, "
                + "  description VARCHAR(32672)  "
                + " )";
    }

    public static List<ConvolutionKernel> read() {
        List<ConvolutionKernel> records = new ArrayList<>();
        try ( Connection conn = DerbyBase.getConnection();
                 PreparedStatement query = conn.prepareStatement("SELECT * FROM Convolution_Kernel ORDER BY name")) {
            conn.setReadOnly(true);
            try ( ResultSet kResult = query.executeQuery()) {
                while (kResult.next()) {
                    ConvolutionKernel record = read(conn, kResult);
                    if (record != null) {
                        records.add(record);
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return records;
    }

    public static ConvolutionKernel read(String name) {
        if (name == null) {
            return null;
        }
        try ( Connection conn = DerbyBase.getConnection();
                 PreparedStatement kernelQuery = conn.prepareStatement(" SELECT * FROM Convolution_Kernel WHERE name=?");) {
            conn.setReadOnly(true);
            kernelQuery.setString(1, name);
            try ( ResultSet kResult = kernelQuery.executeQuery()) {
                if (kResult.next()) {
                    return read(conn, kResult);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return null;
    }

    public static ConvolutionKernel read(Connection conn, ResultSet kResult) {
        if (kResult == null) {
            return null;
        }
        try {
            ConvolutionKernel record = new ConvolutionKernel();
            int w = kResult.getInt("width");
            int h = kResult.getInt("height");
            record.setName(kResult.getString("name"));
            record.setWidth(w);
            record.setHeight(h);
            record.setType(kResult.getInt("type"));
            record.setGray(kResult.getBoolean("is_gray"));
            record.setInvert(kResult.getBoolean("is_invert"));
            record.setEdge(kResult.getInt("edge"));
            Date t = kResult.getTimestamp("create_time");
            if (t != null) {
                record.setCreateTime(DateTools.datetimeToString(t));
            }
            t = kResult.getTimestamp("modify_time");
            if (t != null) {
                record.setModifyTime(DateTools.datetimeToString(t));
            }
            record.setDescription(kResult.getString("description"));
            try ( PreparedStatement matrixQuery
                    = conn.prepareStatement(" SELECT * FROM Float_Matrix WHERE name=? AND row=? AND col=?")) {
                float[][] matrix = new float[h][w];
                for (int j = 0; j < h; ++j) {
                    for (int i = 0; i < w; ++i) {
                        matrixQuery.setString(1, record.getName());
                        matrixQuery.setInt(2, j);
                        matrixQuery.setInt(3, i);
                        try ( ResultSet mResult = matrixQuery.executeQuery()) {
                            if (mResult.next()) {
                                matrix[j][i] = mResult.getFloat("value");
                            }
                        }
                    }
                }
                record.setMatrix(matrix);
            }
            return record;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static boolean exist(String name) {
        if (name == null) {
            return false;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            conn.setReadOnly(true);
            return exist(conn, name);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean exist(Connection conn, String name) {
        if (conn == null || name == null) {
            return false;
        }
        try ( PreparedStatement kernelQuery = conn.prepareStatement(" SELECT width FROM Convolution_Kernel WHERE name=?")) {
            kernelQuery.setString(1, name);
            try ( ResultSet kResult = kernelQuery.executeQuery()) {
                return (kResult.next());
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean insert(Connection conn, ConvolutionKernel record) {
        String sql = "INSERT INTO Convolution_Kernel "
                + "(name, width , height, type, edge, is_gray, is_invert, create_time, modify_time, description) "
                + " VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";
        try ( PreparedStatement update = conn.prepareStatement(sql)) {
            update.setString(1, record.getName());
            update.setInt(2, record.getWidth());
            update.setInt(3, record.getHeight());
            update.setInt(4, record.getType());
            update.setInt(5, record.getEdge());
            update.setBoolean(6, record.isGray());
            update.setBoolean(7, record.isInvert());
            update.setString(8, record.getCreateTime());
            update.setString(9, record.getModifyTime());
            update.setString(10, record.getDescription());
            update.executeUpdate();
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean update(Connection conn, ConvolutionKernel record) {
        String sql = "UPDATE Convolution_Kernel SET "
                + "  width=?, height=?, type=?, edge=?, is_gray=?, is_invert=?, create_time=?, "
                + " modify_time=?, description=?"
                + " WHERE name=?";
        try ( PreparedStatement update = conn.prepareStatement(sql)) {
            update.setInt(1, record.getWidth());
            update.setInt(2, record.getHeight());
            update.setInt(3, record.getType());
            update.setInt(4, record.getEdge());
            update.setBoolean(5, record.isGray());
            update.setBoolean(6, record.isInvert());
            update.setString(7, record.getCreateTime());
            update.setString(8, record.getModifyTime());
            update.setString(9, record.getDescription());
            update.setString(10, record.getName());
            update.executeUpdate();
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean write(ConvolutionKernel record) {
        if (record == null || record.getName() == null
                || record.getWidth() < 3 || record.getWidth() % 2 == 0
                || record.getHeight() < 3 || record.getHeight() % 2 == 0) {
            return false;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            if (exist(conn, record.getName())) {
                return update(conn, record);
            } else {
                return insert(conn, record);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean writeExamples() {
        return write(ConvolutionKernel.makeExample());
    }

    public static boolean write(List<ConvolutionKernel> records) {
        if (records == null || records.isEmpty()) {
            return false;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            for (ConvolutionKernel k : records) {
                if (exist(conn, k.getName())) {
                    update(conn, k);
                } else {
                    insert(conn, k);
                }
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean deleteRecords(List<ConvolutionKernel> records) {
        if (records == null || records.isEmpty()) {
            return false;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            conn.setAutoCommit(false);
            try ( PreparedStatement statement = conn.prepareStatement(
                    "DELETE FROM Convolution_Kernel WHERE name=?")) {
                for (int i = 0; i < records.size(); ++i) {
                    statement.setString(1, records.get(i).getName());
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

    public static boolean delete(List<String> names) {
        if (names == null || names.isEmpty()) {
            return false;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            conn.setAutoCommit(false);
            try ( PreparedStatement statement = conn.prepareStatement(
                    "DELETE FROM Convolution_Kernel WHERE name=?")) {
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
