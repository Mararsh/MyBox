package mara.mybox.db.table;

import mara.mybox.db.DerbyBase;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.data.BrowserHistory;
import mara.mybox.tools.DateTools;
import mara.mybox.dev.MyBoxLog;

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
            try ( PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setMaxRows(MaxBrowserURLs);
                try ( ResultSet results = statement.executeQuery()) {
                    while (results.next()) {
                        recent.add(results.getString("address"));
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
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
            try ( PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setMaxRows(max);
                try ( ResultSet results = statement.executeQuery()) {
                    while (results.next()) {
                        BrowserHistory h = new BrowserHistory();
                        h.setAddress(results.getString("address"));
                        h.setVisitTime(results.getTimestamp("visit_time").getTime());
                        h.setTitle(results.getString("title"));
                        h.setIcon(results.getString("icon"));
                        his.add(h);
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return his;
    }

    public static boolean write(BrowserHistory his) {
        if (his == null) {
            return false;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            boolean exist = false;
            try ( PreparedStatement query = conn.prepareStatement("select * from Browser_History WHERE address=? AND visit_time=?")) {
                query.setString(1, his.getAddress());
                query.setString(2, DateTools.datetimeToString(his.getVisitTime()));
                query.setMaxRows(1);
                try ( ResultSet results = query.executeQuery()) {
                    if (results.next()) {
                        exist = true;
                    }
                }
            }
            if (exist) {
                try ( PreparedStatement update = conn.prepareStatement("UPDATE Browser_History SET title=?, icon=?  WHERE address=? AND visit_time=?")) {
                    if (his.getTitle() != null) {
                        update.setString(1, his.getTitle());
                    } else {
                        update.setString(1, "");
                    }
                    if (his.getIcon() != null) {
                        update.setString(2, his.getIcon());
                    } else {
                        update.setString(2, "");
                    }
                    update.setString(3, his.getAddress());
                    update.setString(4, DateTools.datetimeToString(his.getVisitTime()));
                    update.executeUpdate();
                }
            } else {
                try ( PreparedStatement insert = conn.prepareStatement("INSERT INTO Browser_History(address, visit_time , title, icon) VALUES(?,?,?,?)")) {
                    insert.setString(1, his.getAddress());
                    insert.setString(2, DateTools.datetimeToString(his.getVisitTime()));
                    if (his.getTitle() != null) {
                        insert.setString(3, his.getTitle());
                    } else {
                        insert.setString(3, "");
                    }
                    if (his.getIcon() != null) {
                        insert.setString(4, his.getIcon());
                    } else {
                        insert.setString(4, "");
                    }
                    insert.executeUpdate();
                }
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
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
            MyBoxLog.error(e);
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
            MyBoxLog.error(e);
        }
        return count;
    }

}
