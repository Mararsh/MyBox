package mara.mybox.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
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
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            statement.setMaxRows(1);
            String sql = "SELECT host FROM Browser_Bypass_SSL WHERE host='" + host.trim() + "'";
            ResultSet results = statement.executeQuery(sql);
            return results.next();
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
        }
        return false;
    }

    public static List<CertificateBypass> read() {
        List<CertificateBypass> bypass = new ArrayList<>();
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String sql = "SELECT * FROM Browser_Bypass_SSL ORDER BY create_time DESC";
            ResultSet results = statement.executeQuery(sql);
            while (results.next()) {
                CertificateBypass b = new CertificateBypass();
                b.setHost(results.getString("host"));
                b.setCreateTime(results.getTimestamp("create_time").getTime());
                bypass.add(b);
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
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            statement.setMaxRows(1);
            String sql = "SELECT * FROM Browser_Bypass_SSL  WHERE host='" + host.trim() + "'";
            ResultSet results = statement.executeQuery(sql);
            if (results.next()) {
                CertificateBypass b = new CertificateBypass();
                b.setHost(results.getString("host"));
                b.setCreateTime(results.getTimestamp("create_time").getTime());
                return b;
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
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String d = host.trim();
            String sql = " SELECT host FROM Browser_Bypass_SSL WHERE host='" + d + "'";
            ResultSet results = statement.executeQuery(sql);
            if (results.next()) {
                return false;
            } else {
                sql = "INSERT INTO Browser_Bypass_SSL(host, create_time) VALUES('" + d + "', '"
                        + DateTools.datetimeToString(new Date()) + "')";
                statement.executeUpdate(sql);
                return true;
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
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String d = host.trim();
            String sql = "DELETE FROM Browser_Bypass_SSL WHERE host='" + d + "'";
            statement.executeUpdate(sql);
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
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String inStr = "( '" + hosts.get(0).getHost() + "'";
            for (int i = 1; i < hosts.size(); ++i) {
                inStr += ", '" + hosts.get(i).getHost() + "'";
            }
            inStr += " )";
            String sql = "DELETE FROM Browser_Bypass_SSL WHERE key_value IN " + inStr;
            statement.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
            return false;
        }
    }

}
