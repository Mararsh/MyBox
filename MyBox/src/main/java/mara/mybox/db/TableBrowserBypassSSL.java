package mara.mybox.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import mara.mybox.data.CertificateBypass;
import mara.mybox.tools.DateTools;
import static mara.mybox.value.AppVariables.logger;

/**
 * @Author Mara
 * @CreateDate 2018-10-15 9:31:28
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class TableBrowserBypassSSL extends DerbyBase {

    public TableBrowserBypassSSL() {
        Table_Name = "Browser_Bypass_SSL";
        Keys = new ArrayList<>() {
            {
                add("host");
            }
        };
        Create_Table_Statement
                = " CREATE TABLE Browser_Bypass_SSL ( "
                + "  host  VARCHAR(1024) NOT NULL PRIMARY KEY, "
                + "  create_time TIMESTAMP  NOT NULL "
                + " )";
    }

    public static boolean bypass(String host) {
        if (host == null || host.trim().isBlank()) {
            return false;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            conn.setReadOnly(true);
            String sql = "SELECT host FROM Browser_Bypass_SSL WHERE host=?";
            try ( PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setMaxRows(1);
                statement.setString(1, host.trim());
                boolean exist = false;
                try ( ResultSet results = statement.executeQuery()) {
                    if (results.next()) {
                        exist = true;
                    }
                }
                return exist;
            }
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
        }
        return false;
    }

    public static List<CertificateBypass> read() {
        List<CertificateBypass> bypass = new ArrayList<>();
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            conn.setReadOnly(true);
            String sql = "SELECT * FROM Browser_Bypass_SSL ORDER BY create_time DESC";
            try ( PreparedStatement statement = conn.prepareStatement(sql);
                     ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    CertificateBypass b = new CertificateBypass();
                    b.setHost(results.getString("host"));
                    b.setCreateTime(results.getTimestamp("create_time").getTime());
                    bypass.add(b);
                }
            }
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
        }
        return bypass;
    }

    public static CertificateBypass read(String host) {
        if (host == null || host.trim().isEmpty()) {
            return null;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            conn.setReadOnly(true);
            return read(conn, host);
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
        }
        return null;
    }

    public static CertificateBypass read(Connection conn, String host) {
        if (conn == null || host.trim().isEmpty()) {
            return null;
        }
        final String sql = "SELECT * FROM Browser_Bypass_SSL  WHERE host=?";
        try ( PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setMaxRows(1);
            statement.setString(1, host.trim());
            try ( ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    CertificateBypass b = new CertificateBypass();
                    b.setHost(results.getString("host"));
                    b.setCreateTime(results.getTimestamp("create_time").getTime());
                    return b;
                }
            }
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
        }
        return null;
    }

    public static boolean write(String host) {
        if (host == null || host.trim().isEmpty()) {
            return false;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            CertificateBypass exist = read(conn, host);
            if (exist != null) {
                return false;
            } else {
                final String insertSql = "INSERT INTO Browser_Bypass_SSL(host, create_time) VALUES(?,?)";
                try ( PreparedStatement insert = conn.prepareStatement(insertSql)) {
                    insert.setString(1, host.trim());
                    insert.setString(2, DateTools.datetimeToString(new Date()));
                    return insert.executeUpdate() > 0;
                }
            }
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
            return false;
        }
    }

    public static boolean delete(String host) {
        if (host == null || host.trim().isEmpty()) {
            return false;
        }
        final String sql = "DELETE FROM Browser_Bypass_SSL WHERE host=?";
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, host.trim());
            return statement.executeUpdate() > 0;
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
            return false;
        }
    }

    public static boolean delete(PreparedStatement statement, String host) {
        if (statement == null || host == null || host.trim().isEmpty()) {
            return false;
        }
        try {
            statement.setString(1, host.trim());
            statement.executeUpdate();
            return true;
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
            return false;
        }
    }

    public static boolean delete(List<CertificateBypass> hosts) {
        if (hosts == null || hosts.isEmpty()) {
            return false;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            conn.setAutoCommit(false);
            final String sql = "DELETE FROM Browser_Bypass_SSL WHERE host=?";
            try ( PreparedStatement statement = conn.prepareStatement(sql)) {
                for (int i = 0; i < hosts.size(); ++i) {
                    statement.setString(1, hosts.get(i).getHost());
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

}
