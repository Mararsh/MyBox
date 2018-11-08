package mara.mybox.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import mara.mybox.tools.DateTools;

/**
 * @Author Mara
 * @CreateDate 2018-10-15 9:31:28
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class TableBrowserUrls extends DerbyBase {

    private static final int Max_Browser_URLs = 10;

    public TableBrowserUrls() {
        Table_Name = "Browser_URLs";
        Keys = new ArrayList() {
            {
                add("address");
            }
        };
        Create_Table_Statement
                = " CREATE TABLE Browser_URLs ( "
                + "  address  VARCHAR(1024) NOT NULL PRIMARY KEY, "
                + "  last_visit TIMESTAMP  NOT NULL "
                + " )";
    }

    @Override
    public boolean init(Statement statement) {
        try {
            if (statement == null) {
                return false;
            }
            statement.executeUpdate(Create_Table_Statement);
            ResultSet resultSet = statement.executeQuery("SELECT string_Value FROM User_Conf WHERE key_Name='HtmlLastUrlsKey'");
            if (resultSet.next()) {
                String[] savedUrls = resultSet.getString(1).split("####");
                if (savedUrls.length > 0) {
                    String sql;
                    for (int i = 0; i < savedUrls.length && i < 10; i++) {
                        if (!savedUrls[i].trim().isEmpty()) {
                            sql = "INSERT INTO Browser_URLs(address, last_visit) VALUES('";
                            sql += savedUrls[i].trim() + "', '" + DateTools.datetimeToString(new Date()) + "')";
                            statement.executeUpdate(sql);
                        }
                    }
                }
            }
            return true;
        } catch (Exception e) {
//            logger.debug(e.toString());
            return false;
        }
    }

    public static List<String> read() {
        List<String> urls = new ArrayList();
        try (Connection conn = DriverManager.getConnection(protocol + dbName + parameters);
                Statement statement = conn.createStatement()) {
            String sql = "SELECT address FROM Browser_URLs ORDER BY last_visit DESC";
            ResultSet results = statement.executeQuery(sql);
            while (results.next()) {
                urls.add(results.getString("address"));
            }
            if (urls.size() > Max_Browser_URLs) {
                for (int i = Max_Browser_URLs; i < urls.size(); i++) {
                    sql = "DELETE FROM Browser_URLs WHERE address='" + urls.get(i) + "'";
                    statement.executeUpdate(sql);
                }
                return urls.subList(0, Max_Browser_URLs);
            }
        } catch (Exception e) {
            logger.debug(e.toString());
        }
        return urls;
    }

    public static List<String> write(String address) {
        List<String> urls = read();
        if (address == null || address.trim().isEmpty()) {
            return urls;
        }
        try {
            String d = address.trim();
            if (!d.toLowerCase().startsWith("http")) {
                return urls;
            }
            try (Connection conn = DriverManager.getConnection(protocol + dbName + parameters);
                    Statement statement = conn.createStatement()) {
                String sql;
                if (urls.contains(d)) {
                    sql = "UPDATE Browser_URLs SET last_visit='" + DateTools.datetimeToString(new Date())
                            + "' WHERE address='" + d + "'";
                    statement.executeUpdate(sql);
                } else {
                    sql = "INSERT INTO Browser_URLs(address, last_visit) VALUES('" + d + "', '"
                            + DateTools.datetimeToString(new Date()) + "')";
                    statement.executeUpdate(sql);
                }
                urls = read();
            } catch (Exception e) {
                logger.debug(e.toString());
            }

        } catch (Exception e) {
            logger.debug(e.toString());
        }

        return urls;
    }

}
