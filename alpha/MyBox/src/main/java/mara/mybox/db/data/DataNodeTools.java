package mara.mybox.db.data;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.paint.Color;
import mara.mybox.controller.BaseController;
import static mara.mybox.db.data.DataNode.TagsSeparater;
import mara.mybox.db.table.BaseNodeTable;
import mara.mybox.db.table.TableDataNodeTag;
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
    public static String htmlControls(boolean isTree) {
        String codes = " <script>\n"
                + "    function changeVisible(id) {\n"
                + "      var obj = document.getElementById(id);\n"
                + "      var objv = obj.style.display;\n"
                + "      if (objv == 'none') {\n"
                + "        obj.style.display = 'block';\n"
                + "      } else {\n"
                + "        obj.style.display = 'none';\n"
                + "      }\n"
                + "    }\n"
                + "    function setVisible(className, show) {\n"
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
        codes += "<DIV>\n<DIV>\n";
        if (isTree) {
            codes += "    <INPUT type=\"checkbox\" checked onclick=\"setVisible('Children', this.checked);\">"
                    + message("Unfold") + "</INPUT>\n";
        }
        codes += "    <INPUT type=\"checkbox\" checked onclick=\"setVisible('HierarchyNumber', this.checked);\">"
                + message("HierarchyNumber") + "</INPUT>\n"
                + "    <INPUT type=\"checkbox\" checked onclick=\"setVisible('NodeTag', this.checked);\">"
                + message("Tags") + "</INPUT>\n"
                + "    <INPUT type=\"checkbox\" checked onclick=\"setVisible('NodeValues', this.checked);\">"
                + message("Values") + "</INPUT>\n"
                + "    <INPUT type=\"checkbox\" checked onclick=\"setVisible('NodeAttributes', this.checked);\">"
                + message("Attributes") + "</INPUT>\n"
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
        return prefix + "<DIV class=\"NodeValues\"><DIV style=\"padding: 0 0 0 "
                + indent * 6 + "px;\"><DIV class=\"valueBox\">\n"
                + prefix + dataHtml + "\n"
                + prefix + "</DIV></DIV></DIV>\n";
    }

    public static String titleHtml(DataNode node, List<DataNodeTag> tags,
            String childrenid, String prefixIndent, String spaceIndent) {
        if (node == null) {
            return "";
        }
        String title = node.getHierarchyNumber();
        if (title != null && !title.isBlank()) {
            title = "<SPAN class=\"HierarchyNumber\">" + title + "&nbsp;&nbsp;</SPAN>";
        }
        if (childrenid != null) {
            title += "<a href=\"javascript:changeVisible('" + childrenid + "')\">" + node.getTitle() + "</a>";
        } else {
            title += node.getTitle();
        }
        title = prefixIndent + "<DIV style=\"padding: 2px;\">" + spaceIndent + title + "\n";
        title += DataNodeTools.tagsHtml(tags, 4);
        title += prefixIndent + "</DIV>\n";
        return title;
    }

    public static String attributesHtml(DataNode node,
            boolean withId, boolean withTime, boolean withOrder,
            String spaceIndent) {
        if (node == null) {
            return "";
        }
        String attributes = "";
        if (withId) {
            if (node.getNodeid() >= 0) {
                attributes += message("ID") + ":" + node.getNodeid() + " ";
            }
            if (node.getParentid() >= 0) {
                attributes += message("ParentID") + ":" + node.getParentid() + " ";
            }
        }
        if (withOrder) {
            attributes += message("OrderNumber") + ":" + node.getOrderNumber() + " ";
        }
        if (withTime && node.getUpdateTime() != null) {
            attributes += message("UpdateTime") + ":" + DateTools.datetimeToString(node.getUpdateTime()) + " ";
        }
        if (!attributes.isBlank()) {
            attributes = "<SPAN>" + spaceIndent + "</SPAN>"
                    + "<SPAN style=\"font-size: 0.8em;\">" + attributes.trim() + "</SPAN>\n";
        }
        String cname = node.getChainName();
        if (cname != null && !cname.isBlank()) {
            if (!attributes.isBlank()) {
                attributes += "<BR>";
            }
            attributes += "<SPAN>" + spaceIndent + "</SPAN>"
                    + "<SPAN style=\"font-size: 0.8em;\">" + message("Parent") + ":" + cname + "</SPAN>\n";
        }
        if (!attributes.isBlank()) {
            attributes = "<DIV class=\"NodeAttributes\">" + attributes + "</DIV>\n";
        }
        return attributes;
    }

    public static String nodeHtml(FxTask task, Connection conn,
            BaseController controller, BaseNodeTable dataTable,
            DataNode node) {
        try {
            if (conn == null || node == null) {
                return null;
            }
            long nodeid = node.getNodeid();
            String indentNode = " ".repeat(4);
            List<DataNodeTag> tags = new TableDataNodeTag(dataTable).nodeTags(conn, nodeid);
            String html = titleHtml(node, tags, null, Indent, "");
            html += DataNodeTools.valuesBox(dataTable.valuesHtml(task, conn, controller, node),
                    indentNode, 4);
            html += attributesHtml(node, true, true, true, "");
            return html;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static String listNodeHtml(FxTask fxTask, Connection conn,
            BaseController controller, BaseNodeTable nodeTable,
            DataNode node, List<DataNodeTag> tags,
            boolean withId,
            boolean withTime, boolean withOrder, boolean withData) {
        try {
            StringBuilder s = new StringBuilder();
            String indent2 = Indent + Indent;
            String spaceNode = "&nbsp;".repeat(4);
            s.append(indent2).append("<DIV id=\"").append(node.getNodeid()).append("\">\n");
            s.append(titleHtml(node, tags, null, indent2, ""));
            if (withData) {
                String values = valuesBox(nodeTable.valuesHtml(fxTask, conn, controller, node),
                        indent2, 4);
                if (!values.isBlank()) {
                    s.append(values);
                }
            }
            String attrs = attributesHtml(node, withId, withTime, withOrder, spaceNode);
            if (!attrs.isBlank()) {
                s.append(attrs);
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
            int indent,
            boolean withId,
            boolean withTime, boolean withOrder, boolean withData) {
        try {
            StringBuilder s = new StringBuilder();
            String indentNode = " ".repeat(indent);
            String spaceNode = "&nbsp;".repeat(indent);
            s.append(titleHtml(node, tags,
                    nodeTable.hasChildren(conn, node) ? "children" + node.getNodeid() : null,
                    indentNode, spaceNode));
            if (withData) {
                String values = valuesBox(nodeTable.valuesHtml(fxTask, conn, controller, node),
                        indentNode, indent + 4);
                if (!values.isBlank()) {
                    s.append(values);
                }
            }
            String attrs = attributesHtml(node, withId, withTime, withOrder,
                    spaceNode + "&nbsp;".repeat(4));
            if (!attrs.isBlank()) {
                s.append(indentNode).append(Indent).append(attrs);
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
            DataNode node, List<DataNodeTag> tags,
            String prefix,
            boolean withId,
            boolean withTime, boolean withOrder,
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
            String cname = node.getChainName();
            if (cname != null && !cname.isBlank()) {
                s.append(prefix2).append("<parent_name>\n");
                s.append(prefix3).append("<![CDATA[").append(cname).append("]]>\n");
                s.append(prefix2).append("</parent_name>\n");
            }
            String hierarchyNumber = node.getHierarchyNumber();
            if (hierarchyNumber != null && !hierarchyNumber.isBlank()) {
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
            DataNode node, List<DataNodeTag> tags,
            String prefix,
            boolean withId,
            boolean withTime, boolean withOrder,
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
            String cname = node.getChainName();
            if (cname != null && !cname.isBlank()) {
                if (!s.isEmpty()) {
                    s.append(",\n");
                }
                s.append(prefix)
                        .append("\"").append(message("Parent")).append("\": ")
                        .append(JsonTools.encode(cname));
            }
            String hierarchyNumber = node.getHierarchyNumber();
            if (hierarchyNumber != null && !hierarchyNumber.isBlank()) {
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
            DataNode node, List<DataNodeTag> tags,
            boolean withId,
            boolean withTime, boolean withOrder,
            boolean withData, boolean formatData) {
        try {
            List<String> row = new ArrayList<>();
            if (withId) {
                row.add(node.getNodeid() + "");
                row.add(node.getParentid() + "");
            }
            String cname = node.getChainName();
            if (cname != null) {
                row.add(cname);
            }
            String hie = node.getHierarchyNumber();
            if (hie != null) {
                row.add(hie);
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
