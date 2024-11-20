package mara.mybox.db.data;

import java.sql.Connection;
import java.util.List;
import javafx.scene.paint.Color;
import mara.mybox.controller.BaseController;
import mara.mybox.db.table.BaseNodeTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.FxTask;
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
            String parentName, DataNode node, List<DataNodeTag> tags,
            boolean withId, boolean withTime, boolean withOrder) {
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
            s.append(indent3).append("<H5>")
                    .append(message("Title")).append(": ").append(node.getTitle())
                    .append("</H5>\n");
            String valuesHtml = dataTable.valuesHtml(fxTask, conn, controller, node);
            if (valuesHtml != null && !valuesHtml.isBlank()) {
                s.append(indent3).append("<DIV>").append(valuesHtml).append("</DIV>").append("\n");
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

    public static String toXML(FxTask fxTask, Connection conn,
            BaseController controller, BaseNodeTable dataTable,
            String prefix, String parentName, DataNode node, List<DataNodeTag> tags,
            boolean withTitle, boolean withId, boolean withTime, boolean withOrder) {
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
            if (withTitle && node.getTitle() != null) {
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
            String valuesXml = dataTable.valuesXml(fxTask, conn, controller, prefix2, node);
            if (valuesXml != null && !valuesXml.isBlank()) {
                s.append(valuesXml);
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
            String parentName, DataNode node, List<DataNodeTag> tags,
            boolean withId, boolean withTime, boolean withOrder) {
        try {
            StringBuilder s = new StringBuilder();
            String indent2 = Indent + Indent;
            s.append(Indent).append("{").append("\n");
            if (withId) {
                if (node.getNodeid() >= 0) {
                    s.append(indent2)
                            .append("\"").append(message("ID")).append("\": ")
                            .append(node.getNodeid());
                }
                if (node.getParentid() >= 0) {
                    s.append(",\n");
                    s.append(indent2)
                            .append("\"").append(message("ParentID")).append("\": ")
                            .append(node.getParentid());
                }
            }
            if (parentName != null) {
                s.append(",\n");
                s.append(indent2)
                        .append("\"").append(message("Parent")).append("\": \"")
                        .append(parentName).append("\"");
            }
            if (node.getTitle() != null) {
                s.append(",\n");
                s.append(indent2)
                        .append("\"").append(message("Title")).append("\": \"")
                        .append(node.getTitle()).append("\"");
            }
            if (withOrder) {
                s.append(",\n");
                s.append(indent2)
                        .append("\"").append(message("OrderNumber")).append("\": \"")
                        .append(node.getOrderNumber()).append("\"");
            }
            if (withTime && node.getUpdateTime() != null) {
                s.append(",\n");
                s.append(indent2)
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
                s.append(indent2)
                        .append("\"").append(message("Tags")).append("\": ")
                        .append("[").append(t).append("]");
            }
            String valuesJson = dataTable.valuesJson(fxTask, conn, controller, indent2, node);
            if (valuesJson != null && !valuesJson.isBlank()) {
                s.append(valuesJson);
            }
            s.append("\n");
            s.append(Indent).append("}").append("\n");
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

}
