package mara.mybox.data2d;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mara.mybox.db.DerbyBase;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2022-2-12
 * @License Apache License Version 2.0
 */
public class DataInternalTable extends DataTable {

    public static List<String> InternalTables = new ArrayList<String>() {
        {
            addAll(Arrays.asList("ALARM_CLOCK",
                    "COLOR", "COLOR_PALETTE", "COLOR_PALETTE_NAME",
                    "CONVOLUTION_KERNEL",
                    "DATA2D_CELL", "DATA2D_COLUMN", "DATA2D_DEFINITION", "DATA2D_STYLE",
                    "FILE_BACKUP", "FLOAT_MATRIX", "GEOGRAPHY_CODE",
                    "IMAGE_CLIPBOARD", "IMAGE_EDIT_HISTORY",
                    "MEDIA", "MEDIA_LIST", "MYBOX_LOG", "NAMED_VALUES",
                    "NODE_DATA2D_DEFINITION", "NODE_DATA2D_DEFINITION_NODE_TAG", "NODE_DATA2D_DEFINITION_TAG",
                    "NODE_HTML", "NODE_HTML_NODE_TAG", "NODE_HTML_TAG",
                    "NODE_IMAGE_SCOPE", "NODE_IMAGE_SCOPE_NODE_TAG", "NODE_IMAGE_SCOPE_TAG",
                    "NODE_JAVASCRIPT", "NODE_JAVASCRIPT_NODE_TAG", "NODE_JAVASCRIPT_TAG",
                    "NODE_JEXL", "NODE_JEXL_NODE_TAG", "NODE_JEXL_TAG",
                    "NODE_JSHELL", "NODE_JSHELL_NODE_TAG", "NODE_JSHELL_TAG",
                    "NODE_MATH_FUNCTION", "NODE_MATH_FUNCTION_NODE_TAG", "NODE_MATH_FUNCTION_TAG",
                    "NODE_ROW_FILTER", "NODE_ROW_FILTER_NODE_TAG", "NODE_ROW_FILTER_TAG",
                    "NODE_SQL", "NODE_SQL_NODE_TAG", "NODE_SQL_TAG",
                    "NODE_TEXT", "NODE_TEXT_NODE_TAG", "NODE_TEXT_TAG",
                    "NODE_WEB_ADDRESSES", "NODE_WEB_ADDRESSES_NODE_TAG", "NODE_WEB_ADDRESSES_TAG",
                    "PATH_CONNECTION", "QUERY_CONDITION", "STRING_VALUE", "STRING_VALUES",
                    "SYSTEM_CONF", "TAG", "TEXT_CLIPBOARD",
                    "TREE_NODE", "TREE_NODE_TAG", "USER_CONF", "VISIT_HISTORY", "WEB_HISTORY"
            ));
        }
    };

    public DataInternalTable() {
        dataType = DataType.InternalTable;
    }

    @Override
    public int type() {
        return type(DataType.InternalTable);
    }

    public static String allTableNames() {
        try (Connection conn = DerbyBase.getConnection()) {
            List<String> tables = DerbyBase.allTables(conn);
            StringBuilder s = new StringBuilder();
            for (String referredName : tables) {
                if (!s.isEmpty()) {
                    s.append(", ");
                }
                s.append("\"").append(referredName.toUpperCase()).append("\"");
            }
            return s.toString();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
