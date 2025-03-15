package mara.mybox.db.table;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import mara.mybox.db.DerbyBase;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2018-10-15 9:31:28
 * @License Apache License Version 2.0
 */
public class BaseTableTools {

    public static Map<String, BaseTable> internalTables() {
        Map<String, BaseTable> tables = new LinkedHashMap<>() {
            {
                put("ALARM_CLOCK", new TableAlarmClock());
                put("COLOR", new TableColor());
                put("COLOR_PALETTE_NAME", new TableColorPaletteName());
                put("COLOR_PALETTE", new TableColorPalette());
                put("CONVOLUTION_KERNEL", new TableConvolutionKernel());
                put("DATA2D_DEFINITION", new TableData2DDefinition());
                put("DATA2D_COLUMN", new TableData2DColumn());
                put("DATA2D_STYLE", new TableData2DStyle());
                put("FILE_BACKUP", new TableFileBackup());
                put("FLOAT_MATRIX", new TableFloatMatrix());
                put("IMAGE_CLIPBOARD", new TableImageClipboard());
                put("IMAGE_EDIT_HISTORY", new TableImageEditHistory());
                put("MATRIX_CELL", new TableMatrixCell());
                put("MEDIA", new TableMedia());
                put("MEDIA_LIST", new TableMediaList());
                put("MYBOX_LOG", new TableMyBoxLog());
                put("NAMED_VALUES", new TableNamedValues());
                put("NODE_DATA_COLUMN", new TableNodeDataColumn());
                put("NODE_DATA_COLUMN_NODE_TAG", new TableDataNodeTag(new TableNodeDataColumn()));
                put("NODE_DATA_COLUMN_TAG", new TableDataTag(new TableNodeDataColumn()));
                put("NODE_GEOGRAPHY_CODE", new TableNodeGeographyCode());
                put("NODE_GEOGRAPHY_CODE_NODE_TAG", new TableDataNodeTag(new TableNodeGeographyCode()));
                put("NODE_GEOGRAPHY_CODE_TAG", new TableDataTag(new TableNodeGeographyCode()));
                put("NODE_HTML", new TableNodeHtml());
                put("NODE_HTML_NODE_TAG", new TableDataNodeTag(new TableNodeHtml()));
                put("NODE_HTML_TAG", new TableDataTag(new TableNodeHtml()));
                put("NODE_IMAGE_SCOPE", new TableNodeImageScope());
                put("NODE_IMAGE_SCOPE_NODE_TAG", new TableDataNodeTag(new TableNodeImageScope()));
                put("NODE_IMAGE_SCOPE_TAG", new TableDataTag(new TableNodeImageScope()));
                put("NODE_JEXL", new TableNodeJEXL());
                put("NODE_JEXL_NODE_TAG", new TableDataNodeTag(new TableNodeJEXL()));
                put("NODE_JEXL_TAG", new TableDataTag(new TableNodeJEXL()));
                put("NODE_JSHELL", new TableNodeJShell());
                put("NODE_JSHELL_NODE_TAG", new TableDataNodeTag(new TableNodeJShell()));
                put("NODE_JSHELL_TAG", new TableDataTag(new TableNodeJShell()));
                put("NODE_JAVASCRIPT", new TableNodeJavaScript());
                put("NODE_JAVASCRIPT_NODE_TAG", new TableDataNodeTag(new TableNodeJavaScript()));
                put("NODE_JAVASCRIPT_TAG", new TableDataTag(new TableNodeJavaScript()));
                put("NODE_MATH_FUNCTION", new TableNodeMathFunction());
                put("NODE_MATH_FUNCTION_NODE_TAG", new TableDataNodeTag(new TableNodeMathFunction()));
                put("NODE_MATH_FUNCTION_TAG", new TableDataTag(new TableNodeMathFunction()));
                put("NODE_ROW_EXPRESSION", new TableNodeRowExpression());
                put("NODE_ROW_EXPRESSION_NODE_TAG", new TableDataNodeTag(new TableNodeRowExpression()));
                put("NODE_ROW_EXPRESSION_TAG", new TableDataTag(new TableNodeRowExpression()));
                put("NODE_SQL", new TableNodeSQL());
                put("NODE_SQL_NODE_TAG", new TableDataNodeTag(new TableNodeSQL()));
                put("NODE_SQL_TAG", new TableDataTag(new TableNodeSQL()));
                put("NODE_TEXT", new TableNodeText());
                put("NODE_TEXT_NODE_TAG", new TableDataNodeTag(new TableNodeText()));
                put("NODE_TEXT_TAG", new TableDataTag(new TableNodeText()));
                put("NODE_WEB_FAVORITE", new TableNodeWebFavorite());
                put("NODE_WEB_FAVORITE_NODE_TAG", new TableDataNodeTag(new TableNodeWebFavorite()));
                put("NODE_WEB_FAVORITE_TAG", new TableDataTag(new TableNodeWebFavorite()));
                put("PATH_CONNECTION", new TablePathConnection());
                put("QUERY_CONDITION", new TableQueryCondition());
                put("STRING_VALUE", new TableStringValue());
                put("STRING_VALUES", new TableStringValues());
                put("SYSTEM_CONF", new TableSystemConf());
                put("TEXT_CLIPBOARD", new TableTextClipboard());
                put("USER_CONF", new TableUserConf());
                put("VISIT_HISTORY", new TableVisitHistory());
                put("WEB_HISTORY", new TableWebHistory());
            }
        };
        return tables;
    }

    public static List<String> internalTableNames() {
        List<String> names = new ArrayList<>();
        for (String name : internalTables().keySet()) {
            names.add(name);
        }
        return names;
    }

    public static boolean isInternalTable(String name) {
        if (name == null) {
            return false;
        }
        return internalTables().containsKey(name.toUpperCase());
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

    public static List<String> userTables() {
        List<String> userTables = new ArrayList<>();
        try (Connection conn = DerbyBase.getConnection()) {
            List<String> allTables = DerbyBase.allTables(conn);
            for (String name : allTables) {
                if (!isInternalTable(name)) {
                    userTables.add(name);
                }
            }
        } catch (Exception e) {
            MyBoxLog.console(e);
        }
        return userTables;
    }

}
