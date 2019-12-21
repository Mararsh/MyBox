package mara.mybox.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import static mara.mybox.controller.MediaPlayerController.MiaoGuaiGuaiBenBen;
import mara.mybox.data.MediaInformation;
import mara.mybox.data.MediaList;
import mara.mybox.tools.DateTools;

/**
 * @Author Mara
 * @CreateDate 2019-8-21
 * @License Apache License Version 2.0
 */
public class TableMediaList extends DerbyBase {

    public TableMediaList() {
        Table_Name = "media_list";
        Keys = new ArrayList<>() {
            {
                add("list_name");
                add("address_index");
                add("address");
            }
        };
        Create_Table_Statement
                = " CREATE TABLE media_list ( "
                + "  list_name  VARCHAR(32672) NOT NULL, "
                + "  address_index INTEGER, "
                + "  address VARCHAR(32672)  NOT NULL, "
                + "  modify_time TIMESTAMP NOT NULL, "
                + "  PRIMARY KEY (list_name, address_index, address)"
                + " )";
    }

    public static List<MediaList> read() {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            List<String> names = new ArrayList();
            String sql = " SELECT DISTINCT list_name FROM media_list";
            ResultSet results = statement.executeQuery(sql);
            while (results.next()) {
                names.add(results.getString("list_name"));
            }
            names.remove(MiaoGuaiGuaiBenBen);
            List<MediaList> mediaLists = new ArrayList();
            for (String name : names) {
                sql = " SELECT * FROM media_list WHERE list_name='" + name + "' ORDER BY address_index";
                results = statement.executeQuery(sql);
                List<String> addresses = new ArrayList();
                while (results.next()) {
                    addresses.add(results.getString("address"));
                }
                List<MediaInformation> medias = TableMedia.read(statement, addresses);
                MediaList mediaList = MediaList.create().setName(name).setMedias(medias);
                mediaLists.add(mediaList);
            }
            return mediaLists;
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
            return null;
        }

    }

    public static List<String> names() {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            List<String> names = new ArrayList();
            String sql = " SELECT DISTINCT list_name FROM media_list";
            ResultSet results = statement.executeQuery(sql);
            while (results.next()) {
                names.add(results.getString("list_name"));
            }
            names.remove(MiaoGuaiGuaiBenBen);
            return names;
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
            return null;
        }

    }

    public static MediaList read(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String sql = " SELECT * FROM media_list WHERE list_name='" + name + "' ORDER BY address_index";
            ResultSet results = statement.executeQuery(sql);
            List<String> addresses = new ArrayList();
            while (results.next()) {
                addresses.add(results.getString("address"));
            }
            if (addresses.isEmpty()) {
                return null;
            }
            MediaList list = new MediaList(name);
            list.setMedias(TableMedia.read(statement, addresses));
            return list;
        } catch (Exception e) {
            failed(e);
//            // logger.debug(e.toString());
            return null;
        }

    }

    public static boolean set(String name, List<MediaInformation> medias) {
        if (medias == null || medias.isEmpty()) {
            return false;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
//            TableMedia.write(statement, medias);

            String sql = "DELETE FROM media_list WHERE list_name='" + name + "'";
            statement.executeUpdate(sql);

            int index = 0;
            for (MediaInformation media : medias) {
                try {
                    sql = "INSERT INTO media_list(list_name, address_index , address, modify_time) VALUES('"
                            + name + "', " + index + ", '" + media.getAddress() + "', '"
                            + DateTools.datetimeToString(new Date()) + "')";
                    statement.executeUpdate(sql);
                    index++;
                } catch (Exception e) {
                    failed(e);
//                    logger.debug(e.toString());
                }
            }

            return true;
        } catch (Exception e) {
            failed(e);
//            logger.debug(e.toString());
            return false;
        }
    }

    public static boolean delete(String name) {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String sql = " SELECT * FROM media_list WHERE list_name='" + name + "'";
            ResultSet results = statement.executeQuery(sql);
            List<String> addresses = new ArrayList();
            while (results.next()) {
                addresses.add(results.getString("address"));
            }
            TableMedia.delete(statement, addresses);
            sql = "DELETE FROM media_list WHERE list_name='" + name + "'";
            statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            failed(e);
//            logger.debug(e.toString());
            return false;
        }
    }

}
