package mara.mybox.db.data;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.paint.Color;
import mara.mybox.controller.BaseController;
import static mara.mybox.db.data.DataNode.TagsSeparater;
import mara.mybox.db.table.BaseNodeTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.image.FxColorTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.JsonTools;
import static mara.mybox.value.AppValues.Indent;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2024-11-1
 * @License Apache License Version 2.0
 */
public class DataNodeTools {

    public static String toHtml(FxTask fxTask, Connection conn,
            BaseController controller, BaseNodeTable dataTable,
            String parentName, String hierarchyNumber,
            DataNode node, List<DataNodeTag> tags,
            boolean withId, boolean withTime, boolean withOrder, boolean withData) {
        try {
            StringBuilder s = new StringBuilder();
            String indent2 = Indent + Indent;
            String indent3 = indent2 + Indent;
            s.append(indent2).append("<DIV id=\"").append(node.getNodeid()).append("\">\n");
            if (withId) {
                if (node.getNodeid() >= 0) {
                    s.append(indent3).append("<H5>")
                            .append(message("ID")).append(": ").append(node.getNodeid())
                            .append("</H5>\n");
                }
                if (node.getParentid() >= 0) {
                    s.append(indent3).append("<H5>")
                            .append(message("ParentID")).append(": ").append(node.getParentid())
                            .append("</H5>\n");
                }
            }
            if (parentName != null) {
                s.append(indent3).append("<H5>")
                        .append(message("Parent")).append(": <CODE>").append(parentName)
                        .append("</CODE></H5>\n");
            }
            if (withOrder) {
                s.append(indent3).append("<H5>")
                        .append(message("OrderNumber")).append(": ").append(node.getOrderNumber())
                        .append("</CODE></PRE></H5>\n");
            }
            if (withTime && node.getUpdateTime() != null) {
                s.append(indent3).append("<H5>")
                        .append(message("UpdateTime")).append(": ")
                        .append(DateTools.datetimeToString(node.getUpdateTime()))
                        .append("</H5>\n");
            }
            if (tags != null && !tags.isEmpty()) {
                s.append(indent3).append("<H4>");
                for (DataNodeTag nodeTag : tags) {
                    Color color = nodeTag.getTag().getColor();
                    if (color == null) {
                        color = FxColorTools.randomColor();
                    }
                    s.append("<SPAN style=\"border-radius:4px; padding: 2px; font-size:0.8em;  background-color: ")
                            .append(FxColorTools.color2rgb(color))
                            .append("; color: ").append(FxColorTools.color2rgb(FxColorTools.foreColor(color)))
                            .append(";\">").append(nodeTag.getTag().getTag())
                            .append("</SPAN>\n");
                }
                s.append("</H4>\n");
            }

            s.append(indent3).append("<H5>").append(message("Title")).append(": ");
            if (hierarchyNumber != null) {
                s.append(hierarchyNumber).append("&nbsp;&nbsp;");
            }
            s.append(node.getTitle()).append("</H5>\n");
            if (withData) {
                String valuesHtml = dataTable.valuesHtml(fxTask, conn, controller, node);
                if (valuesHtml != null && !valuesHtml.isBlank()) {
                    s.append(indent3).append("<DIV>").append(valuesHtml).append("</DIV>").append("\n");
                }
            }
            s.append(indent2).append("</DIV><HR>\n\n");
            return s.toString();
        } catch (Exception e) {
            if (fxTask != null) {
                fxTask.setError(e.toString());
            } else {
                MyBoxLog.error(e.toString());
            }
            return null;
        }
    }

    public static String treeHtml(FxTask fxTask, Connection conn,
            BaseController controller, BaseNodeTable nodeTable,
            DataNode node, List<DataNodeTag> tags,
            String nodePageid, int indent, String serialNumber) {
        try {
            StringBuilder s = new StringBuilder();
            String indentNode = " ".repeat(indent);
            String spaceNode = "&nbsp;".repeat(indent);
            String nodeName = node.getTitle();
            String displayName = "<SPAN class=\"SerialNumber\">" + serialNumber + "&nbsp;&nbsp;</SPAN>" + nodeName;
            boolean hasChildren = nodeTable.hasChildren(conn, node);
            if (hasChildren) {
                displayName = "<a href=\"javascript:nodeClicked('" + nodePageid + "')\">" + displayName + "</a>";
            }
            s.append(indentNode).append("<DIV style=\"padding: 2px;\">")
                    .append(spaceNode).append(displayName).append("\n");
            if (tags != null && !tags.isEmpty()) {
                String indentTag = " ".repeat(indent + 8);
                String spaceTag = "&nbsp;".repeat(2);
                s.append(indentTag).append("<SPAN class=\"NodeTag\">\n");
                for (DataNodeTag nodeTag : tags) {
                    Color color = nodeTag.getTag().getColor();
                    if (color == null) {
                        color = FxColorTools.randomColor();
                    }
                    s.append(indentTag).append(spaceTag)
                            .append("<SPAN style=\"border-radius:4px; padding: 2px; font-size:0.8em;  background-color: ")
                            .append(FxColorTools.color2rgb(color))
                            .append("; color: ").append(FxColorTools.color2rgb(FxColorTools.foreColor(color)))
                            .append(";\">").append(nodeTag.getTag().getTag()).append("</SPAN>\n");
                }
                s.append(indentTag).append("</SPAN>\n");
            }
            s.append(indentNode).append("</DIV>\n");

            String dataHtml = nodeTable.valuesHtml(fxTask, conn, controller, node);
            if (dataHtml != null && !dataHtml.isBlank()) {
                s.append(indentNode).append("<DIV class=\"nodeValue\"><DIV style=\"padding: 0 0 0 ")
                        .append((indent + 4) * 6).append("px;\"><DIV class=\"valueBox\">\n");
                s.append(indentNode).append(dataHtml).append("\n");
                s.append(indentNode).append("</DIV></DIV></DIV>\n");
            }

            return s.toString();
        } catch (Exception e) {
            if (fxTask != null) {
                fxTask.setError(e.toString());
            } else {
                MyBoxLog.error(e.toString());
            }
            return null;
        }
    }

    public static String toXML(FxTask fxTask, Connection conn,
            BaseController controller, BaseNodeTable dataTable,
            String prefix, String parentName, String hierarchyNumber,
            DataNode node, List<DataNodeTag> tags,
            boolean withId, boolean withTime, boolean withOrder,
            boolean withData, boolean formatData) {
        try {
            StringBuilder s = new StringBuilder();
            String prefix2 = prefix + Indent;
            String prefix3 = prefix2 + Indent;
            s.append(prefix).append("<NodeAttributes>\n");
            if (withId) {
                if (node.getNodeid() >= 0) {
                    s.append(prefix2).append("<nodeid>").append(node.getNodeid()).append("</nodeid>\n");
                }
                if (node.getParentid() >= 0) {
                    s.append(prefix2).append("<parentid>").append(node.getParentid()).append("</parentid>\n");
                }
            }
            if (parentName != null) {
                s.append(prefix2).append("<parent_name>\n");
                s.append(prefix3).append("<![CDATA[").append(parentName).append("]]>\n");
                s.append(prefix2).append("</parent_name>\n");
            }
            if (hierarchyNumber != null) {
                s.append(prefix2).append("<hierarchy_number>").append(hierarchyNumber).append("</hierarchy_number>\n");
            }
            if (node.getTitle() != null) {
                s.append(prefix2).append("<title>\n");
                s.append(prefix3).append("<![CDATA[").append(node.getTitle()).append("]]>\n");
                s.append(prefix2).append("</title>\n");
            }
            if (withOrder) {
                s.append(prefix2).append("<order_number>").append(node.getOrderNumber()).append("</order_number>\n");
            }
            if (withTime && node.getUpdateTime() != null) {
                s.append(prefix2).append("<update_time>")
                        .append(DateTools.datetimeToString(node.getUpdateTime()))
                        .append("</update_time>\n");
            }
            if (withData) {
                String valuesXml = dataTable.valuesXml(prefix2, node, formatData);
                if (valuesXml != null && !valuesXml.isBlank()) {
                    s.append(valuesXml);
                }
            }
            s.append(prefix).append("</NodeAttributes>\n");
            if (tags != null && !tags.isEmpty()) {
                for (DataNodeTag tag : tags) {
                    s.append(prefix).append("<NodeTag>\n");
                    s.append(prefix2).append("<![CDATA[").append(tag.getTag().getTag()).append("]]>\n");
                    s.append(prefix).append("</NodeTag>\n");
                }
            }
            return s.toString();
        } catch (Exception e) {
            if (fxTask != null) {
                fxTask.setError(e.toString());
            } else {
                MyBoxLog.error(e.toString());
            }
            return null;
        }
    }

    public static String toJson(FxTask fxTask, Connection conn,
            BaseController controller, BaseNodeTable dataTable,
            String prefix, String parentName, String hierarchyNumber,
            DataNode node, List<DataNodeTag> tags,
            boolean withId, boolean withTime, boolean withOrder,
            boolean withData, boolean formatData) {
        try {
            StringBuilder s = new StringBuilder();
            if (withId) {
                if (node.getNodeid() >= 0) {
                    s.append(prefix)
                            .append("\"").append(message("ID")).append("\": ")
                            .append(node.getNodeid());
                }
                if (node.getParentid() >= 0) {
                    if (!s.isEmpty()) {
                        s.append(",\n");
                    }
                    s.append(prefix)
                            .append("\"").append(message("ParentID")).append("\": ")
                            .append(node.getParentid());
                }
            }
            if (parentName != null) {
                if (!s.isEmpty()) {
                    s.append(",\n");
                }
                s.append(prefix)
                        .append("\"").append(message("Parent")).append("\": ")
                        .append(JsonTools.encode(parentName));
            }
            if (hierarchyNumber != null) {
                if (!s.isEmpty()) {
                    s.append(",\n");
                }
                s.append(prefix)
                        .append("\"").append(message("HierarchyNumber")).append("\": \"")
                        .append(hierarchyNumber).append("\"");
            }
            if (!s.isEmpty()) {
                s.append(",\n");
            }
            s.append(prefix)
                    .append("\"").append(message("Title")).append("\": ")
                    .append(JsonTools.encode(node.getTitle()));
            if (withOrder) {
                s.append(",\n");
                s.append(prefix)
                        .append("\"").append(message("OrderNumber")).append("\": \"")
                        .append(node.getOrderNumber()).append("\"");
            }
            if (withTime && node.getUpdateTime() != null) {
                s.append(",\n");
                s.append(prefix)
                        .append("\"").append(message("UpdateTime")).append("\": \"")
                        .append(node.getUpdateTime()).append("\"");
            }
            if (tags != null && !tags.isEmpty()) {
                String t = null;
                for (DataNodeTag tag : tags) {
                    String v = tag.getTag().getTag();
                    v = JsonTools.encode(v);
                    if (t == null) {
                        t = v;
                    } else {
                        t += "," + v;
                    }
                }
                s.append(",\n");
                s.append(prefix)
                        .append("\"").append(message("Tags")).append("\": ")
                        .append("[").append(t).append("]");
            }
            if (withData) {
                String valuesJson = dataTable.valuesJson(prefix, node, formatData);
                if (valuesJson != null && !valuesJson.isBlank()) {
                    s.append(",\n").append(valuesJson);
                }
            }
            return s.toString();
        } catch (Exception e) {
            if (fxTask != null) {
                fxTask.setError(e.toString());
            } else {
                MyBoxLog.error(e.toString());
            }
            return null;
        }
    }

    public static List<String> toCsv(FxTask fxTask, Connection conn,
            BaseController controller, BaseNodeTable dataTable,
            String parentName, String hierarchyNumber,
            DataNode node, List<DataNodeTag> tags,
            boolean withId, boolean withTime, boolean withOrder,
            boolean withData, boolean formatData) {
        try {
            List<String> row = new ArrayList<>();
            if (withId) {
                row.add(node.getNodeid() + "");
                row.add(node.getParentid() + "");
            }
            if (parentName != null) {
                row.add(parentName);
            }
            if (hierarchyNumber != null) {
                row.add(hierarchyNumber);
            }
            row.add(node.getTitle());

            if (withOrder) {
                row.add(node.getOrderNumber() + "");
            }
            if (withTime && node.getUpdateTime() != null) {
                row.add(node.getUpdateTime() + "");
            }
            if (tags != null) {
                String t = null;
                for (DataNodeTag tag : tags) {
                    String v = tag.getTag().getTag();
                    if (t == null) {
                        t = v;
                    } else {
                        t += TagsSeparater + v;
                    }
                }
                row.add(t);
            }
            if (withData) {
                for (String name : dataTable.dataColumnNames()) {
                    Object value = node.getValue(name);
                    ColumnDefinition column = dataTable.column(name);
                    String sValue = dataTable.exportValue(column, value, formatData);
                    row.add(sValue);
                }
            }
            return row;
        } catch (Exception e) {
            if (fxTask != null) {
                fxTask.setError(e.toString());
            } else {
                MyBoxLog.error(e.toString());
            }
            return null;
        }
    }

}
