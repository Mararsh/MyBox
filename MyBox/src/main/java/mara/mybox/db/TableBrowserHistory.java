package mara.mybox.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.data.BrowserHistory;
import mara.mybox.tools.DateTools;
import static mara.mybox.value.AppVariables.logger;

/**
 * @Author Mara
 * @CreateDate 2018-10-15 9:31:28
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class TableBrowserHistory extends DerbyBase {

    private static final int RecentNumber = 50;
    private static final int MaxBrowserURLs = 20;

    public TableBrowserHistory() {
        Table_Name = "Browser_History";
        Keys = new ArrayList<>() {
            {
                add("address");
                add("visit_time");
            }
        };
        Create_Table_Statement
                = " CREATE TABLE Browser_History ( "
                + "  address  VARCHAR(1024) NOT NULL, "
                + "  visit_time TIMESTAMP  NOT NULL, "
                + "  title  VARCHAR(4096) , "
                + "  icon   VARCHAR(4096) , "
                + "  PRIMARY KEY (address, visit_time)"
                + " )";
    }

    public static List<BrowserHistory> recentHistory() {
        return read(RecentNumber);
    }

    public static List<String> recentBrowse() {
        List<String> recent = new ArrayList<>();
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            statement.setMaxRows(MaxBrowserURLs);
            String sql = "select address from Browser_History  group by address  order by max(visit_time) desc";
            ResultSet results = statement.executeQuery(sql);
            while (results.next()) {
                recent.add(results.getString("address"));
            }
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
        }
        return recent;
    }

    public static List<BrowserHistory> read() {
        return read(0);
    }

    public static List<BrowserHistory> read(int max) {
        List<BrowserHistory> his = new ArrayList<>();
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            statement.setMaxRows(max);
            String sql = "SELECT * FROM Browser_History ORDER BY visit_time DESC";
            ResultSet results = statement.executeQuery(sql);
            while (results.next()) {
                BrowserHistory h = new BrowserHistory();
                h.setAddress(results.getString("address"));
                h.setVisitTime(results.getTimestamp("visit_time").getTime());
                h.setTitle(results.getString("title"));
                h.setIcon(results.getString("icon"));
                his.add(h);
            }
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
        }
        return his;
    }

    public static boolean write(BrowserHistory his) {
        if (his == null) {
            return false;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            statement.setMaxRows(1);
            String sql = "select * from Browser_History WHERE address='" + his.getAddress()
                    + "' AND visit_time='" + DateTools.datetimeToString(his.getVisitTime()) + "' ";
            if (statement.executeQuery(sql).next()) {
                sql = "UPDATE Browser_History SET title='";
                if (his.getTitle() != null) {
                    sql += his.getTitle() + "' ";
                } else {
                    sql += "' ";
                }
                if (his.getIcon() != null) {
                    sql += ", icon='" + his.getIcon() + "' ";
                } else {
                    sql += ", icon='' ";
                }
                sql += " WHERE address='" + his.getAddress() + "'　"
                        + " AND　visit_time='" + DateTools.datetimeToString(his.getVisitTime()) + "' ";
            } else {
                sql = "INSERT INTO Browser_History(address, visit_time , title, icon) VALUES('";
                sql += his.getAddress() + "', '";
                sql += DateTools.datetimeToString(his.getVisitTime()) + "' ";
                if (his.getTitle() != null) {
                    sql += ", '" + his.getTitle() + "' ";
                } else {
                    sql += ", '' ";
                }
                if (his.getIcon() != null) {
                    sql += ", '" + his.getIcon() + "' ";
                } else {
                    sql += ", '' ";
                }
                sql += " )";
            }
            statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
            return false;
        }
    }

    public static boolean delete(String address, long time) {
        if (address == null || address.trim().isEmpty()) {
            return false;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String sql = "DELETE FROM Browser_History WHERE address='" + address.trim() + "'　"
                    + " AND　visit_time='" + DateTools.datetimeToString(time) + "' ";
            statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
            return false;
        }
    }

    public static boolean delete(BrowserHistory his) {
        if (his == null) {
            return false;
        }
        return delete(his.getAddress(), his.getVisitTime());
    }

    public static boolean delete(List<BrowserHistory> his) {
        if (his == null || his.isEmpty()) {
            return false;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            conn.setAutoCommit(false);
            for (BrowserHistory h : his) {
                String sql = "DELETE FROM Browser_History WHERE address='" + h.getAddress() + "'　"
                        + " AND　visit_time='" + DateTools.datetimeToString(h.getVisitTime()) + "' ";
                statement.executeUpdate(sql);
            }
            conn.commit();
            return true;
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
            return false;
        }
    }

}
