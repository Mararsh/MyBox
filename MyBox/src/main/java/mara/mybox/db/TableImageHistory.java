package mara.mybox.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import mara.mybox.image.ImageHistory;
import mara.mybox.image.ImageScope;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;

/**
 * @Author Mara
 * @CreateDate 2018-10-15 9:31:28
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class TableImageHistory extends DerbyBase {

    private static final int Default_Max_Histories = 20;

    public TableImageHistory() {
        Table_Name = "image_history";
        Keys = new ArrayList<>() {
            {
                add("image_location");
                add("history_location");
            }
        };
        Create_Table_Statement
                = " CREATE TABLE image_history ( "
                + "  image_location  VARCHAR(1024) NOT NULL, "
                + "  operation_time TIMESTAMP NOT NULL, "
                + "  history_location  VARCHAR(1024) NOT NULL, "
                + "  update_type  VARCHAR(128), "
                + "  object_type  VARCHAR(128), "
                + "  op_type  VARCHAR(128), "
                + "  scope_type  VARCHAR(128), "
                + "  scope_name  VARCHAR(1024), "
                + "  PRIMARY KEY (image_location, history_location)"
                + " )";
    }

    public static List<ImageHistory> read(String image) {
        List<ImageHistory> records = new ArrayList<>();
        if (image == null || image.trim().isEmpty()) {
            return records;
        }
        int max = AppVariables.getUserConfigInt("MaxImageHistories", Default_Max_Histories);
        try ( Connection conn = DriverManager.getConnection(protocol + dbName() + login);
                 Statement statement = conn.createStatement()) {
            String sql = " SELECT * FROM image_history WHERE image_location='" + image + "' ORDER BY operation_time DESC";
            ResultSet results = statement.executeQuery(sql);
            while (results.next()) {
                ImageHistory his = new ImageHistory();
                his.setImage(image);
                his.setHistoryLocation(results.getString("history_location"));
                his.setUpdateType(results.getString("update_type"));
                his.setObjectType(results.getString("object_type"));
                his.setOpType(results.getString("op_type"));
                his.setScopeType(results.getString("scope_type"));
                his.setScopeName(results.getString("scope_name"));
                his.setOperationTime(results.getTimestamp("operation_time"));
                records.add(his);
            }

            List<ImageHistory> valid = new ArrayList<>();
            for (int i = 0; i < records.size(); i++) {
                String hisname = records.get(i).getHistoryLocation();
                File hisFile = new File(hisname);
                if (!hisFile.exists()) {
                    deleteRecord(statement, image, hisname);
                    continue;
                }
                valid.add(records.get(i));
            }
            if (valid.size() > max) {
                for (int i = max; i < valid.size(); i++) {
                    deleteRecord(statement, image, valid.get(i).getHistoryLocation());
                }
                return valid.subList(0, max);
            }

        } catch (Exception e) {  failed(e);
            // logger.debug(e.toString());
        }
        return records;
    }

    public static void deleteRecord(Statement statement, String image, String hisname) {
        try {
            String sql = "DELETE FROM image_history WHERE image_location='" + image
                    + "' AND history_location='" + hisname + "'";
            statement.executeUpdate(sql);
            File hisFile = new File(hisname);
            if (hisFile.exists()) {
                hisFile.delete();
            }
            File thumbFile = new File(FileTools.appendName(hisname, "_thumbnail"));
            if (thumbFile.exists()) {
                thumbFile.delete();
            }
        } catch (Exception e) {  failed(e);
            // logger.debug(e.toString());
        }
    }

    public static List<ImageHistory> add(String image, String his_location, ImageScope scope) {
        return add(image, his_location, null, null, null, scope);
    }

    public static List<ImageHistory> add(String image, String his_location, String update_type,
            String object_type, String op_type, ImageScope scope) {
        if (image == null || image.trim().isEmpty()
                || his_location == null || his_location.trim().isEmpty()) {
            return read(image);
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbName() + login);
                 Statement statement = conn.createStatement()) {
            String fields = "image_location, history_location ,operation_time ";
            String values = " '" + image + "', '" + his_location + "', '" + DateTools.datetimeToString(new Date()) + "' ";
            if (update_type != null) {
                fields += ", update_type";
                values += ", '" + update_type + "' ";
            }
            if (object_type != null) {
                fields += ", object_type";
                values += ", '" + object_type + "' ";
            }
            if (op_type != null) {
                fields += ", op_type";
                values += ", '" + op_type + "' ";
            }
            if (scope != null) {
                if (scope.getScopeType() != null) {
                    fields += ", scope_type";
                    values += ", '" + scope.getScopeType().name() + "' ";
                }
                if (scope.getName() != null) {
                    fields += ", scope_name";
                    values += ", '" + scope.getName() + "' ";
                }
            }
            String sql = "INSERT INTO image_history(" + fields + ") VALUES(" + values + ")";
//            logger.debug(sql);
            statement.executeUpdate(sql);
            return read(image);
        } catch (Exception e) {  failed(e);
            // logger.debug(e.toString());
            return read(image);
        }
    }

    public static boolean clearImage(String image) {
        List<ImageHistory> records = read(image);
        try ( Connection conn = DriverManager.getConnection(protocol + dbName() + login);
                 Statement statement = conn.createStatement()) {
            conn.setAutoCommit(false);
            for (int i = 0; i < records.size(); i++) {
                deleteRecord(statement, image, records.get(i).getHistoryLocation());
            }
            conn.commit();
            return true;
        } catch (Exception e) {  failed(e);
            // logger.debug(e.toString());
            return false;
        }
    }

    public static boolean deleteHistory(String image, String hisname) {
        try ( Connection conn = DriverManager.getConnection(protocol + dbName() + login);
                 Statement statement = conn.createStatement()) {
            deleteRecord(statement, image, hisname);
            return true;
        } catch (Exception e) {  failed(e);
            // logger.debug(e.toString());
            return false;
        }
    }

    @Override
    public boolean clear() {
        try ( Connection conn = DriverManager.getConnection(protocol + dbName() + login);
                 Statement statement = conn.createStatement()) {
            String sql = " SELECT history_location FROM image_history";
            ResultSet results = statement.executeQuery(sql);
            while (results.next()) {
                try {
                    new File(results.getString("history_location")).delete();
                } catch (Exception e) {  failed(e);
                }
            }
            String imageHistoriesPath = AppVariables.getImageHisPath();
            File path = new File(imageHistoriesPath);
            if (path.exists()) {
                File[] files = path.listFiles();
                for (File f : files) {
                    f.delete();
                }
            }
            sql = "DELETE FROM image_history";
            statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {  failed(e);
            // logger.debug(e.toString());
            return false;
        }
    }

}
