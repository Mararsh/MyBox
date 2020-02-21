package mara.mybox.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.data.Member;
import static mara.mybox.db.DerbyBase.dbHome;
import static mara.mybox.db.DerbyBase.failed;
import static mara.mybox.db.DerbyBase.login;
import static mara.mybox.db.DerbyBase.protocol;
import static mara.mybox.value.AppVariables.logger;

/**
 * @Author Mara
 * @CreateDate 2020-2-13
 * @License Apache License Version 2.0
 */
public class TableMember extends DerbyBase {

    public TableMember() {
        Table_Name = "Member";
        Keys = new ArrayList<>() {
            {
                add("data_type");
                add("data_object");
                add("data_member");
            }
        };
        Create_Table_Statement
                = " CREATE TABLE Member ( "
                + "  data_type VARCHAR(2048) NOT NULL, "
                + "  data_object VARCHAR(2048) NOT NULL, "
                + "  data_member VARCHAR(2048) NOT NULL, "
                + "  PRIMARY KEY (data_type, data_object, data_member)"
                + " )";
    }

    public static List<String> read(String type, String object) {
        List<String> members = new ArrayList<>();
        if (type == null || object == null) {
            return members;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            String sql = "SELECT * FROM Member WHERE data_type='" + type
                    + "' AND data_object='" + object + "'";
            ResultSet results = statement.executeQuery(sql);
            while (results.next()) {
                members.add(results.getString("data_member"));
            }
        } catch (Exception e) {
            failed(e);
            // logger.debug(e.toString());
        }
        return members;
    }

    public static boolean add(List<Member> members) {
        if (members == null || members.isEmpty()) {
            return false;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            for (Member member : members) {
                try {
                    String sql = "INSERT INTO Member (data_type, data_object, data_member) VALUES(";
                    sql += "'" + member.getType() + "', '" + member.getObject() + "', '" + member.getMember() + "')";
                    statement.executeUpdate(sql);
                } catch (Exception e) {
                }
            }
            return true;
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
            return false;
        }
    }

    public static boolean delete(List<Member> members) {
        if (members == null || members.isEmpty()) {
            return false;
        }
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 Statement statement = conn.createStatement()) {
            conn.setAutoCommit(false);
            for (Member member : members) {
                try {
                    String sql = "DELETE FROM Member WHERE data_type='" + member.getType()
                            + "' AND data_object='" + member.getObject()
                            + "' AND data_member='" + member.getMember() + "'";
                    statement.executeUpdate(sql);
                } catch (Exception e) {
                }
            }
            conn.commit();
            return true;
        } catch (Exception e) {
            failed(e);
            logger.debug(e.toString());
            return false;
        }
    }

}
