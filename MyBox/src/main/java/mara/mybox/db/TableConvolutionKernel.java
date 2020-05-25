package mara.mybox.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import mara.mybox.data.ConvolutionKernel;
import static mara.mybox.db.DerbyBase.protocol;
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
        Keys = new ArrayList<>() {
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
                + "  edge SMALLINT, "
                + "  modify_time TIMESTAMP, "
                + "  create_time TIMESTAMP, "
                + "  description VARCHAR(1024)  "
                + " )";
    }

    public static List<ConvolutionKernel> read() {
        List<ConvolutionKernel> records = new ArrayList<>();
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            conn.setReadOnly(true);
            String sql = " SELECT * FROM Convolution_Kernel ORDER BY name";
            ResultSet kResult = conn.createStatement().executeQuery(sql);
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
                records.add(record);
            }
            try ( PreparedStatement statement = conn.prepareStatement(
                    " SELECT * FROM Float_Matrix WHERE name=? AND row=? AND col=?")) {
                for (ConvolutionKernel k : records) {
                    int w = k.getWidth();
                    int h = k.getHeight();
                    float[][] matrix = new float[h][w];
                    for (int j = 0; j < h; ++j) {
                        for (int i = 0; i < w; ++i) {
                            statement.setString(1, k.getName());
                            statement.setInt(2, j);
                            statement.setInt(3, i);
                            ResultSet mResult = statement.executeQuery();
                            if (mResult.next()) {
                                matrix[j][i] = mResult.getFloat("value");
                            }
                        }
                    }
                    k.setMatrix(matrix);
                }
            }

        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
        }
        return records;
    }

    public static ConvolutionKernel read(String name) {
        ConvolutionKernel record = null;
        if (name == null) {
            return record;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            conn.setReadOnly(true);
            PreparedStatement statement = conn.prepareStatement(
                    " SELECT width FROM Convolution_Kernel WHERE name=?"
            );
            statement.setString(1, name);
            ResultSet kResult = statement.executeQuery();
            if (kResult.next()) {
                statement = conn.prepareStatement(
                        " SELECT * FROM Float_Matrix WHERE name=? AND row=? AND col=?"
                );
                record = new ConvolutionKernel();
                int w = kResult.getInt("width");
                int h = kResult.getInt("height");
                record.setName(name);
                record.setWidth(w);
                record.setHeight(h);
                record.setType(kResult.getInt("type"));
                record.setGray(kResult.getInt("gray"));
                record.setEdge(kResult.getInt("edge"));
                Date t = kResult.getTimestamp("create_time");
                if (t != null) {
                    record.setCreateTime(DateTools.datetimeToString(t));
                }
                t = kResult.getTimestamp("modify_time");
                if (t != null) {
                    record.setModifyTime(DateTools.datetimeToString(t));
                }
                float[][] matrix = new float[h][w];
                for (int j = 0; j < h; ++j) {
                    for (int i = 0; i < w; ++i) {
                        statement.setString(1, name);
                        statement.setInt(2, j);
                        statement.setInt(3, i);
                        ResultSet mResult = statement.executeQuery();
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
                for (int j = 0; j < h; ++j) {
                    for (int i = 0; i < w; ++i) {
                        statement.setString(1, name);
                        statement.setInt(2, j);
                        statement.setInt(3, i);
                        ResultSet mResult = statement.executeQuery();
                        if (mResult.next()) {
                            matrix[j][i] = mResult.getFloat("value");
                        }
                    }
                }
                record.setMatrix(matrix);
            }
            statement.close();
            return record;
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
            return record;
        }
    }

    public static boolean write(ConvolutionKernel record) {
        if (record == null || record.getName() == null
                || record.getWidth() < 3 || record.getWidth() % 2 == 0
                || record.getHeight() < 3 || record.getHeight() % 2 == 0) {
            return false;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            ResultSet result;
            try ( PreparedStatement statement = conn.prepareStatement(
                    " SELECT width FROM Convolution_Kernel WHERE name=?"
            )) {
                statement.setString(1, record.getName());
                result = statement.executeQuery();
            }
            if (result.next()) {
                try ( PreparedStatement update = conn.prepareStatement(
                        "UPDATE Convolution_Kernel SET "
                        + "  width=?, height=？, type=？, gray=？, edge=？, create_time=,？"
                        + " modify_time=？, description=？"
                        + " WHERE name=?"
                )) {
                    update.setInt(1, record.getWidth());
                    update.setInt(2, record.getHeight());
                    update.setInt(3, record.getType());
                    update.setInt(4, record.getGray());
                    update.setInt(5, record.getEdge());
                    update.setString(6, record.getCreateTime());
                    update.setString(7, record.getModifyTime());
                    update.setString(8, record.getDescription());
                    update.setString(9, record.getName());
                    update.executeUpdate();
                }

            } else {
                try ( PreparedStatement update = conn.prepareStatement(
                        "INSERT INTO Convolution_Kernel "
                        + "(name, width , height, type, gray, edge, create_time, modify_time, description) "
                        + " VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?) "
                )) {
                    update.setString(1, record.getName());
                    update.setInt(2, record.getWidth());
                    update.setInt(3, record.getHeight());
                    update.setInt(4, record.getType());
                    update.setInt(5, record.getGray());
                    update.setInt(6, record.getEdge());
                    update.setString(7, record.getCreateTime());
                    update.setString(8, record.getModifyTime());
                    update.setString(9, record.getDescription());
                    update.executeUpdate();
                }
            }

            return true;
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
            return false;
        }
    }

    public static boolean writeExamples() {
        ConvolutionKernel.makeExample();
        return write(ConvolutionKernel.ExampleKernels);
    }

    public static boolean write(List<ConvolutionKernel> records) {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            conn.setAutoCommit(false);
            for (ConvolutionKernel k : records) {
                boolean exist;
                try ( PreparedStatement statement = conn.prepareStatement(
                        " SELECT width FROM Convolution_Kernel WHERE name=?"
                )) {
                    statement.setString(1, k.getName());
                    try ( ResultSet results = statement.executeQuery()) {
                        exist = results.next();
                    }
                }
                if (!exist) {
                    try ( PreparedStatement update = conn.prepareStatement(
                            "INSERT INTO Convolution_Kernel "
                            + "(name, width , height, type, gray, edge, create_time, modify_time, description) "
                            + " VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?) "
                    )) {
                        update.setString(1, k.getName());
                        update.setInt(2, k.getWidth());
                        update.setInt(3, k.getHeight());
                        update.setInt(4, k.getType());
                        update.setInt(5, k.getGray());
                        update.setInt(6, k.getEdge());
                        update.setString(7, k.getCreateTime());
                        update.setString(8, k.getModifyTime());
                        update.setString(9, k.getDescription());
                        update.executeUpdate();
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

    public static boolean deleteRecords(List<ConvolutionKernel> records) {
        if (records == null || records.isEmpty()) {
            return false;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
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
                    "DELETE FROM Convolution_Kernel WHERE name=?")) {
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
