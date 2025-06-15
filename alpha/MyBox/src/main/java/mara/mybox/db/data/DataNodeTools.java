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

    /*
        static
     */
    public static String htmlControls() {
        String codes = " <script>\n"
                + "    function showNode(id) {\n"
                + "      var obj = document.getElementById(id);\n"
                + "      var objv = obj.style.display;\n"
                + "      if (objv == 'none') {\n"
                + "        obj.style.display = 'block';\n"
                + "      } else {\n"
                + "        obj.style.display = 'none';\n"
                + "      }\n"
                + "    }\n"
                + "    function showClass(className, show) {\n"
                + "      var nodes = document.getElementsByClassName(className);  　\n"
                + "      if ( show) {\n"
                + "           for (var i = 0 ; i < nodes.length; i++) {\n"
                + "              nodes[i].style.display = '';\n"
                + "           }\n"
                + "       } else {\n"
                + "           for (var i = 0 ; i < nodes.length; i++) {\n"
                + "              nodes[i].style.display = 'none';\n"
                + "           }\n"
                + "       }\n"
                + "    }\n"
                + "  </script>\n\n";
        codes += "<DIV>\n<DIV>\n"
                + "    <INPUT type=\"checkbox\" checked onclick=\"showClass('TreeNode', this.checked);\">"
                + message("Unfold") + "</INPUT>\n"
                + "    <INPUT type=\"checkbox\" checked onclick=\"showClass('HierarchyNumber', this.checked);\">"
                + message("HierarchyNumber") + "</INPUT>\n"
                + "    <INPUT type=\"checkbox\" checked onclick=\"showClass('NodeTag', this.checked);\">"
                + message("Tags") + "</INPUT>\n"
                + "    <INPUT type=\"checkbox\" checked onclick=\"showClass('nodeValue', this.checked);\">"
                + message("Values") + "</INPUT>\n"
                + "</DIV>\n<HR>\n";
        return codes;
    }

    public static String tagsHtml(List<DataNodeTag> tags, int indent) {
        if (tags == null || tags.isEmpty()) {
            return "";
        }
        String indentTag = " ".repeat(indent + 8);
        String spaceTag = "&nbsp;".repeat(2);
        String s = indentTag + "<SPAN class=\"NodeTag\">\n";
        for (DataNodeTag nodeTag : tags) {
            s += indentTag + spaceTag + tagHtml(nodeTag);
        }
        s += indentTag + "</SPAN>\n";
        return s;
    }

    public static String tagHtml(DataNodeTag nodeTag) {
        if (nodeTag == null) {
            return null;
        }
        Color color = nodeTag.getTag().getColor();
        if (color == null) {
            color = FxColorTools.randomColor();
        }
        return "<SPAN style=\"border-radius:4px; padding: 2px; font-size:0.8em;  background-color: "
                + FxColorTools.color2rgb(color) + "; color: "
                + FxColorTools.color2rgb(FxColorTools.foreColor(color))
                + ";\">" + nodeTag.getTag().getTag() + "</SPAN>\n";
    }

    public static String valuesBox(String dataHtml, String prefix, int indent) {
        if (dataHtml == null || dataHtml.isBlank()) {
            return "";
        }
        return prefix + "<DIV class=\"nodeValue\"><DIV style=\"padding: 0 0 0 "
                + (indent + 4) * 6 + "px;\"><DIV class=\"valueBox\">\n"
                + prefix + dataHtml + "\n"
                + prefix + "</DIV></DIV></DIV>\n";
    }

    public static String listNodeHtml(FxTask fxTask, Connection conn,
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
                    s.append(tagHtml(nodeTag));
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

    public static String treeNodeHtml(FxTask fxTask, Connection conn,
            BaseController controller, BaseNodeTable nodeTable,
            DataNode node, List<DataNodeTag> tags,
            String nodePageid, int indent,
            String hierarchyNumber) {
        try {
            StringBuilder s = new StringBuilder();
            String indentNode = " ".repeat(indent);
            String spaceNode = "&nbsp;".repeat(indent);
            String spaceTag = "&nbsp;".repeat(2);
            String displayName = hierarchyNumber != null && !hierarchyNumber.isBlank() ? hierarchyNumber : "0";
            displayName = "<SPAN class=\"HierarchyNumber\">" + displayName + "</SPAN>" + spaceTag + node.getTitle();
            boolean hasChildren = nodeTable.hasChildren(conn, node);
            if (hasChildren) {
                displayName = "<a href=\"javascript:showNode('" + nodePageid + "')\">" + displayName + "</a>";
            }
            s.append(indentNode).append("<DIV style=\"padding: 2px;\">")
                    .append(spaceNode).append(displayName);

            s.append(tagsHtml(tags, indent));
            s.append(indentNode).append("</DIV>\n");

            s.append(valuesBox(nodeTable.valuesHtml(fxTask, conn, controller, node),
                    indentNode, indent));
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
