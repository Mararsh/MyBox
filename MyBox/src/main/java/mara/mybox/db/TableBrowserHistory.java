package mara.mybox.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            conn.setReadOnly(true);
            String sql = "select address from Browser_History  group by address  order by max(visit_time) desc";
            ResultSet results;
            try ( PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setMaxRows(MaxBrowserURLs);
                results = statement.executeQuery();
            }
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
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            conn.setReadOnly(true);
            String sql = "SELECT * FROM Browser_History ORDER BY visit_time DESC";
            ResultSet results;
            try ( PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setMaxRows(max);
                results = statement.executeQuery();
            }
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
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {

            PreparedStatement statement = conn.prepareStatement(
                    "select * from Browser_History WHERE address=? AND visit_time=?"
            );
            statement.setString(1, his.getAddress());
            statement.setString(2, DateTools.datetimeToString(his.getVisitTime()));
            statement.setMaxRows(1);
            ResultSet results = statement.executeQuery();
            if (results.next()) {
                statement = conn.prepareStatement(
                        "UPDATE Browser_History SET title=?, icon=? "
                        + "WHERE address=? AND visit_time=?"
                );
                if (his.getTitle() != null) {
                    statement.setString(1, his.getTitle());
                } else {
                    statement.setString(1, "");
                }
                if (his.getIcon() != null) {
                    statement.setString(2, his.getIcon());
                } else {
                    statement.setString(2, "");
                }
                statement.setString(3, his.getAddress());
                statement.setString(4, DateTools.datetimeToString(his.getVisitTime()));
            } else {
                statement = conn.prepareStatement(
                        "INSERT INTO Browser_History(address, visit_time , title, icon) VALUES(?,?,?,?)"
                );
                statement.setString(1, his.getAddress());
                statement.setString(2, DateTools.datetimeToString(his.getVisitTime()));
                if (his.getTitle() != null) {
                    statement.setString(3, his.getTitle());
                } else {
                    statement.setString(3, "");
                }
                if (his.getIcon() != null) {
                    statement.setString(4, his.getIcon());
                } else {
                    statement.setString(4, "");
                }
            }
            statement.executeUpdate();
            statement.close();
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
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            String sql = "DELETE FROM Browser_History WHERE address='" + address.trim() + "'　"
                    + " AND　visit_time='" + DateTools.datetimeToString(time) + "' ";
            conn.createStatement().executeUpdate(sql);
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

    public static int delete(List<BrowserHistory> his) {
        if (his == null || his.isEmpty()) {
            return 0;
        }
        int count = 0;
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            conn.setAutoCommit(false);
            for (BrowserHistory h : his) {
                String sql = "DELETE FROM Browser_History WHERE address='" + h.getAddress() + "'　"
                        + " AND　visit_time='" + DateTools.datetimeToString(h.getVisitTime()) + "' ";
                count += conn.createStatement().executeUpdate(sql);
            }
            conn.commit();
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
        }
        return count;
    }

}
