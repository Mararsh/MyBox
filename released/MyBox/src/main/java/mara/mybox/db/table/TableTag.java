package mara.mybox.db.table;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.Tag;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-3-3
 * @License Apache License Version 2.0
 */
public class TableTag extends BaseTable<Tag> {

    public TableTag() {
        tableName = "Tag";
        defineColumns();
    }

    public TableTag(boolean defineColumns) {
        tableName = "Tag";
        if (defineColumns) {
            defineColumns();
        }
    }

    public final TableTag defineColumns() {
        addColumn(new ColumnDefinition("tgid", ColumnType.Long, true, true).setAuto(true));
        addColumn(new ColumnDefinition("category", ColumnType.String, true).setLength(StringMaxLength).setDefaultValue("Root"));
        addColumn(new ColumnDefinition("tag", ColumnType.String, true).setLength(StringMaxLength));
        addColumn(new ColumnDefinition("color", ColumnType.Color, true));
        orderColumns = "tgid ASC";
        return this;
    }

    public static final String Create_Unique_Index
            = "CREATE UNIQUE INDEX Tag_unique_index on Tag (  category, tag )";

    public static final String QueryTag
            = "SELECT * FROM Tag WHERE category=? AND tag=?";

    public static final String QueryCategoryTags
            = "SELECT tag FROM Tag WHERE category=? ";

    public Tag queryTag(Connection conn, String category, String tag) {
        if (conn == null || tag == null) {
            return null;
        }
        try ( PreparedStatement statement = conn.prepareStatement(TableTag.QueryTag)) {
            statement.setString(1, category);
            statement.setString(2, tag);
            ResultSet results = statement.executeQuery();
            if (results.next()) {
                return readData(results);
            } else {
                return null;
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public List<String> categoryTags(String category) {
        try ( Connection conn = DerbyBase.getConnection()) {
            return categoryTags(conn, category);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public List<String> categoryTags(Connection conn, String category) {
        List<String> tags = new ArrayList<>();
        try ( PreparedStatement statement = conn.prepareStatement(QueryCategoryTags)) {
            statement.setString(1, category);
            ResultSet results = statement.executeQuery();
            while (results.next()) {
                tags.add(results.getString("tag"));
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return tags;
    }

    public Tag findAndCreate(Connection conn, String category, String name) {
        if (conn == null || category == null || name == null) {
            return null;
        }
        try {
            Tag tag = queryTag(conn, category, name);
            if (tag == null) {
                tag = new Tag(category, name);
                tag = insertData(conn, tag);
                conn.commit();
            }
            return tag;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
