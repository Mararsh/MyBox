package mara.mybox.controller;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.DataNode;
import mara.mybox.db.data.DataNodeTag;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.table.BaseDataTable;
import mara.mybox.db.table.TableDataNode;
import mara.mybox.db.table.TableDataNodeTag;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.style.HtmlStyles;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.JsonTools;
import mara.mybox.tools.TextTools;
import mara.mybox.tools.XmlTools;
import static mara.mybox.value.AppValues.Indent;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-4-30
 * @License Apache License Version 2.0
 */
public class DataTreeExportController extends BaseTaskController {

    protected DataTreeController treeController;
    protected BaseDataTable dataTable;
    protected TableDataNode nodeTable;
    protected TableDataNodeTag nodeTagsTable;
    protected TreeItem<DataNode> selectedNode;
    protected File textsFile, xmlFile, jsonFile, htmlFile, framesetFile, framesetNavFile;
    protected FileWriter textsWriter, htmlWriter, xmlWriter, jsonWriter, framesetNavWriter;
    protected String dataName;
    protected int count, level;
    protected Charset charset;
    protected boolean firstRow;

    @FXML
    protected CheckBox idCheck, timeCheck, tagsCheck,
            htmlCheck, xmlCheck, jsonCheck, framesetCheck;
    @FXML
    protected ComboBox<String> charsetSelector;
    @FXML
    protected TextArea styleInput;

    public DataTreeExportController() {
        baseTitle = message("Export");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            idCheck.setSelected(UserConfig.getBoolean(baseName + "ID", false));
            idCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) {
                    UserConfig.setBoolean(baseName + "ID", idCheck.isSelected());
                }
            });

            timeCheck.setSelected(UserConfig.getBoolean(baseName + "Time", false));
            timeCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) {
                    UserConfig.setBoolean(baseName + "Time", timeCheck.isSelected());
                }
            });

            tagsCheck.setSelected(UserConfig.getBoolean(baseName + "Tags", true));
            tagsCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) {
                    UserConfig.setBoolean(baseName + "Tags", tagsCheck.isSelected());
                }
            });

            htmlCheck.setSelected(UserConfig.getBoolean(baseName + "Html", false));
            htmlCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) {
                    UserConfig.setBoolean(baseName + "Html", htmlCheck.isSelected());
                }
            });

            framesetCheck.setSelected(UserConfig.getBoolean(baseName + "Frameset", false));
            framesetCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) {
                    UserConfig.setBoolean(baseName + "Frameset", framesetCheck.isSelected());
                }
            });

            xmlCheck.setSelected(UserConfig.getBoolean(baseName + "Xml", false));
            xmlCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) {
                    UserConfig.setBoolean(baseName + "Xml", xmlCheck.isSelected());
                }
            });

            jsonCheck.setSelected(UserConfig.getBoolean(baseName + "Json", false));
            jsonCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) {
                    UserConfig.setBoolean(baseName + "Json", jsonCheck.isSelected());
                }
            });

            List<String> setNames = TextTools.getCharsetNames();
            charsetSelector.getItems().addAll(setNames);
            try {
                charset = Charset.forName(UserConfig.getString(baseName + "Charset", Charset.defaultCharset().name()));
            } catch (Exception e) {
                charset = Charset.defaultCharset();
            }
            charsetSelector.setValue(charset.name());
            charsetSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    charset = Charset.forName(charsetSelector.getSelectionModel().getSelectedItem());
                    UserConfig.setString(baseName + "Charset", charset.name());
                }
            });

            styleInput.setText(UserConfig.getString(baseName + "Style", HtmlStyles.styleValue("Default")));

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setParamters(DataTreeController controller, TreeItem<DataNode> item) {
        try {
            if (controller == null || item == null || item.getValue() == null) {
                close();
                return;
            }
            this.treeController = controller;
            this.dataTable = controller.dataTable;
            this.nodeTable = controller.nodeTable;
            this.nodeTagsTable = controller.nodeTagsTable;
            this.dataName = controller.dataName;
            selectedNode = item;

            baseTitle = treeController.baseTitle + " - " + message("Export") + " : " + item.getValue().getTitle();
            setTitle(baseTitle);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean checkOptions() {
        if (treeController == null || selectedNode == null || selectedNode.getValue() == null) {
            close();
            return false;
        }
        textsFile = null;
        xmlFile = null;
        htmlFile = null;
        jsonFile = null;
        framesetFile = null;
        textsWriter = null;
        htmlWriter = null;
        xmlWriter = null;
        jsonWriter = null;
        framesetNavWriter = null;
        level = count = 0;
        if (!htmlCheck.isSelected() && !framesetCheck.isSelected()
                && !xmlCheck.isSelected() && !jsonCheck.isSelected()) {
            popError(message("NothingSave"));
            return false;
        }

        targetPath = targetPathController.pickFile();
        if (targetPath == null) {
            popError(message("InvalidParameters") + ": " + message("TargetPath"));
            return false;
        }
        return true;
    }

    @FXML
    public void popDefaultStyle(MouseEvent mouseEvent) {
        try {
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;
            for (HtmlStyles.HtmlStyle style : HtmlStyles.HtmlStyle.values()) {
                menu = new MenuItem(message(style.name()));
                menu.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        styleInput.setText(HtmlStyles.styleValue(style));
                        UserConfig.setString(baseName + "Style", styleInput.getText());
                    }
                });
                items.add(menu);
            }

            items.add(new SeparatorMenuItem());

            popEventMenu(mouseEvent, items);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void clearStyle() {
        styleInput.clear();
    }

    @Override
    public boolean doTask(FxTask currentTask) {
        if (selectedNode == null || targetPath == null) {
            return false;
        }
        if (!openWriters()) {
            closeWriters();
            return false;
        }
        count = level = 0;
        firstRow = true;
        try (Connection conn = DerbyBase.getConnection()) {
            exportNode(currentTask, conn, selectedNode.getValue(),
                    treeController.chainName(selectedNode.getParent()));
        } catch (Exception e) {
            updateLogs(e.toString());
        }
        return closeWriters();
    }

    protected boolean openWriters() {
        if (selectedNode == null || targetPath == null) {
            return false;
        }
        try {
            String nodeName = treeController.chainName(selectedNode);
            String prefix = nodeName.replaceAll(DataNode.TitleSeparater, "-");

            if (htmlCheck.isSelected()) {
                htmlFile = makeTargetFile(prefix, ".html", targetPath);
                if (htmlFile != null) {
                    updateLogs(message("Writing") + " " + htmlFile.getAbsolutePath());
                    htmlWriter = new FileWriter(htmlFile, charset);
                    writeHtmlHead(htmlWriter, nodeName);
                    htmlWriter.write(Indent + "<BODY>\n" + Indent + Indent + "<H2>" + nodeName + "</H2>\n");
                } else if (targetPathController.isSkip()) {
                    updateLogs(message("Skipped"));
                }
            }
            if (framesetCheck.isSelected()) {
                framesetFile = makeTargetFile(prefix, "-frameset.html", targetPath);
                if (framesetFile != null) {
                    updateLogs(message("Writing") + " " + framesetFile.getAbsolutePath());
                    StringBuilder s;
                    String subPath = FileNameTools.filter(prefix) + "-frameset";
                    File path = new File(targetPath + File.separator + subPath + File.separator);
                    path.mkdirs();
                    framesetNavFile = new File(path.getAbsolutePath() + File.separator + "nav.html");
                    File coverFile = new File(path.getAbsolutePath() + File.separator + "cover.html");
                    try (FileWriter coverWriter = new FileWriter(coverFile, charset)) {
                        writeHtmlHead(coverWriter, nodeName);
                        coverWriter.write("<BODY>\n<BR><BR><BR><BR><H1>" + message("Notes") + "</H1>\n</BODY></HTML>");
                        coverWriter.flush();
                    }
                    try (FileWriter framesetWriter = new FileWriter(framesetFile, charset)) {
                        writeHtmlHead(framesetWriter, nodeName);
                        s = new StringBuilder();
                        s.append("<FRAMESET border=2 cols=240,240,*>\n")
                                .append("<FRAME name=nav src=\"").append(subPath).append("/").append(framesetNavFile.getName()).append("\" />\n")
                                .append("<FRAME name=booknav />\n")
                                .append("<FRAME name=main src=\"").append(subPath).append("/cover.html\" />\n</HTML>\n");
                        framesetWriter.write(s.toString());
                        framesetWriter.flush();
                    }
                    framesetNavWriter = new FileWriter(framesetNavFile, charset);
                    writeHtmlHead(framesetNavWriter, nodeName);
                    s = new StringBuilder();
                    s.append(Indent).append("<BODY>\n");
                    s.append(Indent).append(Indent).append("<H2>").append(nodeName).append("</H2>\n");
                    framesetNavWriter.write(s.toString());
                } else if (targetPathController.isSkip()) {
                    updateLogs(message("Skipped"));
                }
            }
            if (xmlCheck.isSelected()) {
                xmlFile = makeTargetFile(prefix, ".xml", targetPath);
                if (xmlFile != null) {
                    updateLogs(message("Writing") + " " + xmlFile.getAbsolutePath());
                    xmlWriter = new FileWriter(xmlFile, charset);
                    StringBuilder s = new StringBuilder();
                    s.append("<?xml version=\"1.0\" encoding=\"")
                            .append(charset.name()).append("\"?>\n")
                            .append("<").append(XmlTools.xmlTag(dataName)).append(">\n");
                    xmlWriter.write(s.toString());
                } else if (targetPathController.isSkip()) {
                    updateLogs(message("Skipped"));
                }
            }
            if (jsonCheck.isSelected()) {
                jsonFile = makeTargetFile(prefix, ".json", targetPath);
                if (jsonFile != null) {
                    updateLogs(message("Writing") + " " + jsonFile.getAbsolutePath());
                    jsonWriter = new FileWriter(jsonFile, Charset.forName("UTF-8"));
                    StringBuilder s = new StringBuilder();
                    s.append("{\"").append(message(dataName)).append("\": [\n");
                    jsonWriter.write(s.toString());
                } else if (targetPathController.isSkip()) {
                    updateLogs(message("Skipped"));
                }
            }
        } catch (Exception e) {
            updateLogs(e.toString());
            return false;
        }
        return true;
    }

    protected void writeHtmlHead(FileWriter writer, String title) {
        try {
            StringBuilder s = new StringBuilder();
            s.append("<HTML>\n").append(Indent).append("<HEAD>\n")
                    .append(Indent).append(Indent).append("<title>").append(title).append("</title>\n")
                    .append(Indent).append(Indent)
                    .append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=")
                    .append(charset.name()).append("\" />\n");
            String style = styleInput.getText();
            if (style != null && !style.isBlank()) {
                s.append(Indent).append(Indent).append("<style type=\"text/css\">\n");
                s.append(Indent).append(Indent).append(Indent).append(style).append("\n");
                s.append(Indent).append(Indent).append("</style>\n");
            }
            s.append(Indent).append("</HEAD>\n");
            writer.write(s.toString());
        } catch (Exception e) {
            updateLogs(e.toString());
        }
    }

    protected boolean closeWriters() {
        if (selectedNode == null || targetPath == null) {
            return false;
        }
        boolean well = true;
        if (textsWriter != null) {
            try {
                textsWriter.flush();
                textsWriter.close();
                textsWriter = null;
                targetFileGenerated(textsFile, VisitHistory.FileType.Text);
            } catch (Exception e) {
                updateLogs(e.toString());
                well = false;
            }
        }
        if (htmlWriter != null) {
            try {
                htmlWriter.write(Indent + "</BODY>\n</HTML>\n");
                htmlWriter.flush();
                htmlWriter.close();
                htmlWriter = null;
                targetFileGenerated(htmlFile, VisitHistory.FileType.Html);
            } catch (Exception e) {
                updateLogs(e.toString());
                well = false;
            }
        }
        if (framesetNavWriter != null) {
            try {
                framesetNavWriter.write(Indent + "</BODY>\n</HTML>\n");
                framesetNavWriter.flush();
                framesetNavWriter.close();
                framesetNavWriter = null;
                targetFileGenerated(framesetFile, VisitHistory.FileType.Html);
            } catch (Exception e) {
                updateLogs(e.toString());
                well = false;
            }
        }
        if (xmlWriter != null) {
            try {
                xmlWriter.write("</" + XmlTools.xmlTag(dataTable.getTableName()) + ">\n");
                xmlWriter.flush();
                xmlWriter.close();
                xmlWriter = null;
                targetFileGenerated(xmlFile, VisitHistory.FileType.XML);
            } catch (Exception e) {
                updateLogs(e.toString());
                well = false;
            }
        }
        if (jsonWriter != null) {
            try {
                jsonWriter.write("\n]}\n");
                jsonWriter.flush();
                jsonWriter.close();
                jsonWriter = null;
                targetFileGenerated(jsonFile, VisitHistory.FileType.JSON);
            } catch (Exception e) {
                updateLogs(e.toString());
                well = false;
            }
        }
        return well;
    }

    public void exportNode(FxTask currentTask, Connection conn, DataNode node, String parentChainName) {
        level++;
        if (conn == null || node == null) {
            return;
        }
        try {
            count++;
            String xmlPrefix = "";
            for (int i = 1; i < level; i++) {
                xmlPrefix += Indent;
            }
            List<DataNodeTag> tags = null;
            if (tagsCheck.isSelected()) {
                tags = nodeTagsTable.nodeTags(conn, node.getNodeid());
            }
            if (htmlWriter != null) {
                writeHtml(currentTask, conn, parentChainName, node, htmlWriter, tags);
            }
            if (xmlWriter != null) {
                xmlWriter.write(xmlPrefix + "<TreeNode>\n");
                writeXML(currentTask, conn, parentChainName, xmlPrefix, node, tags);
            }
            if (jsonWriter != null) {
                writeJson(currentTask, conn, parentChainName, node, tags);
            }
            if (currentTask == null || !currentTask.isWorking()) {
                return;
            }
            String nodeChainName;
            if (parentChainName != null && !parentChainName.isBlank()) {
                nodeChainName = parentChainName + DataNode.TitleSeparater + node.getTitle();
            } else {
                nodeChainName = node.getTitle();
            }
            List<DataNode> children = nodeTable.children(conn, node.getNodeid());

            if (framesetNavWriter != null) {
                String nodeTitle = node.getTitle() + "_" + node.getNodeid();
                File bookNavFile = new File(framesetNavFile.getParent() + File.separator + FileNameTools.filter(nodeTitle) + "_nav.html");
                FileWriter bookNavWriter = new FileWriter(bookNavFile, charset);
                writeHtmlHead(bookNavWriter, nodeTitle);
                bookNavWriter.write(Indent + "<BODY>\n");

                File nodeFile = new File(framesetNavFile.getParent() + File.separator + FileNameTools.filter(nodeTitle) + ".html");
                FileWriter bookWriter = new FileWriter(nodeFile, charset);
                writeHtmlHead(bookWriter, nodeTitle);
                bookWriter.write(Indent + "<BODY>\n");
                String prefix = "";
                for (int i = 1; i < level; i++) {
                    prefix += "&nbsp;&nbsp;&nbsp;&nbsp;";
                }
                framesetNavWriter.write(prefix + "<A href=\"" + bookNavFile.getName() + "\"  target=booknav>" + node.getTitle() + "</A><BR>\n");

                writeHtml(currentTask, conn, nodeChainName, node, bookWriter, tags);
                try {
                    bookWriter.write(Indent + "\n</BODY>\n</HTML>");
                    bookWriter.flush();
                    bookWriter.close();
                } catch (Exception e) {
                    updateLogs(e.toString());
                }

                if (children != null && !children.isEmpty()) {
                    for (DataNode child : children) {
                        File childFile = new File(framesetNavFile.getParent() + File.separator
                                + FileNameTools.filter(child.getTitle() + "_" + child.getNodeid()) + ".html");
                        bookNavWriter.write("<A href=\"" + childFile.getName() + "\"  target=main>" + child.getTitle() + "</A><BR>\n");
                    }
                } else {
                    bookNavWriter.write("<A href=\"" + nodeFile.getName() + "\"  target=main>" + node.getTitle() + "</A><BR>\n");
                }
                try {
                    bookNavWriter.write(Indent + "\n</BODY>\n</HTML>");
                    bookNavWriter.flush();
                    bookNavWriter.close();
                } catch (Exception e) {
                    updateLogs(e.toString());
                }
            }
            if (currentTask == null || !currentTask.isWorking()) {
                return;
            }

            if (children != null) {
                for (DataNode child : children) {
                    if (currentTask == null || !currentTask.isWorking()) {
                        return;
                    }
                    exportNode(currentTask, conn, child, nodeChainName);
                }
            }
            if (xmlWriter != null) {
                xmlWriter.write(xmlPrefix + "</TreeNode>\n");
            }
        } catch (Exception e) {
            updateLogs(e.toString());
        }
        level--;
    }

    protected void writeXML(FxTask currentTask, Connection conn, String prefix,
            String parentName, DataNode node, List<DataNodeTag> tags) {
        try {
            String xml = prefix + "<Attributes>\n";
            if (idCheck.isSelected()) {
                if (node.getNodeid() >= 0) {
                    xml += prefix + Indent + "<nodeid>" + node.getNodeid() + "</nodeid>\n";
                }
                if (node.getParentid() >= 0) {
                    xml += prefix + Indent + "<parentid>" + node.getParentid() + "</parentid>\n";
                }
            }
            if (parentName != null) {
                xml += prefix + Indent + "<parentName>" + parentName + "</parentName>\n";
            }
            if (node.getTitle() != null) {
                xml += prefix + Indent + "<title>\n"
                        + prefix + Indent + "<![CDATA[" + node.getTitle() + "]]>\n"
                        + prefix + Indent + "</title>\n";
            }
            if (node.getUpdateTime() != null) {
                xml += prefix + "<updateTime>" + DateTools.datetimeToString(node.getUpdateTime()) + "</updateTime>\n";
            }
            xml += prefix + "</Attributes>\n";
            xml += prefix + "<Data>\n";
            List<ColumnDefinition> columns = dataTable.getColumns();
            for (ColumnDefinition column : columns) {
                String name = column.getColumnName();
                Object value = node.getValue(name);
                if (value == null) {
                    continue;
                }
                String tname = XmlTools.xmlTag(name);
                xml += prefix + Indent + "<" + tname + ">\n";
                xml += prefix + Indent + Indent + "<![CDATA[" + column.toString(value) + "]]>\n";
                xml += prefix + Indent + "</" + tname + ">\n";
            }
            xml += prefix + "</Data>\n";
            if (tags != null && !tags.isEmpty()) {
                xml += prefix + "<Tags>\n";
                for (DataNodeTag tag : tags) {
                    xml += prefix + Indent + "<Tag>\n";
                    xml += prefix + Indent + Indent + "<![CDATA[" + tag.getTag().getTag() + "]]>\n";
                    xml += prefix + Indent + "</Tag>\n";
                }
                xml += prefix + "</Tags>\n";
            }
            xmlWriter.write(xml);
        } catch (Exception e) {
            updateLogs(e.toString());
        }
    }

    protected void writeHtml(FxTask currentTask, Connection conn,
            String parentName, DataNode node, FileWriter writer, List<DataNodeTag> tags) {
        try {
            writer.write(Indent + Indent + "<div id=\"" + node.getNodeid() + "\">\n");
            if (idCheck.isSelected()) {
                if (node.getNodeid() >= 0) {
                    writer.write(Indent + Indent + Indent + "<H3>" + message("ID") + ": " + node.getNodeid() + "</H3>\n");
                }
                if (node.getParentid() >= 0) {
                    writer.write(Indent + Indent + Indent + "<H3>" + message("ParentID") + ": " + node.getParentid() + "</H3>\n");
                }
            }
            if (parentName != null) {
                writer.write(Indent + Indent + Indent + "<H3>" + message("Parent") + ": "
                        + "<PRE><CODE>" + parentName + "</CODE></PRE></H3>\n");
            }
            if (timeCheck.isSelected() && node.getUpdateTime() != null) {
                writer.write(Indent + Indent + Indent + "<H5>" + DateTools.datetimeToString(node.getUpdateTime()) + "</H5>\n");
            }
            if (tags != null && !tags.isEmpty()) {
                writer.write(Indent + Indent + Indent + "<H4>");
                for (DataNodeTag nodeTag : tags) {
                    Color color = nodeTag.getTag().getColor();
                    if (color == null) {
                        color = FxColorTools.randomColor();
                    }
                    writer.write("<SPAN style=\"border-radius:4px; padding: 2px; font-size:0.8em;  background-color: "
                            + FxColorTools.color2rgb(color) + "; color: "
                            + FxColorTools.color2rgb(FxColorTools.foreColor(color)) + ";\">"
                            + nodeTag.getTag().getTag() + "</SPAN>\n");
                }
                writer.write("</H4>\n");
            }
            writer.write(Indent + Indent + Indent + "<H4><PRE><CODE>" + node.getTitle() + "</CODE></PRE></H4>\n");
            String dataHtml = dataTable.valuesHtml(currentTask, conn, myController, node);
            if (dataHtml != null && !dataHtml.isBlank()) {
                writer.write(Indent + Indent + Indent + dataHtml + "\n");
            }
            writer.write(Indent + Indent + "</div><HR>\n\n");
        } catch (Exception e) {
            updateLogs(e.toString());
        }
    }

    protected void writeJson(FxTask currentTask, Connection conn,
            String parentName, DataNode node, List<DataNodeTag> tags) {
        try {
            StringBuilder s = new StringBuilder();
            if (!firstRow) {
                s.append(",\n");
            } else {
                firstRow = false;
            }
            s.append(Indent).append("{").append("\n");
            if (idCheck.isSelected()) {
                if (node.getNodeid() >= 0) {
                    s.append(Indent).append(Indent)
                            .append("\"").append(message("ID")).append("\": ")
                            .append(node.getNodeid()).append(",\n");
                }
                if (node.getParentid() >= 0) {
                    s.append(Indent).append(Indent)
                            .append("\"").append(message("ParentID")).append("\": ")
                            .append(node.getParentid()).append(",\n");
                }
            }
            if (parentName != null) {
                s.append(Indent).append(Indent)
                        .append("\"").append(message("Parent")).append("\": \"")
                        .append(parentName).append("\",\n");
            }
            s.append(Indent).append(Indent)
                    .append("\"").append(message("Title")).append("\": \"")
                    .append(node.getTitle()).append("\"");
            if (timeCheck.isSelected() && node.getUpdateTime() != null) {
                s.append(",\n");
                s.append(Indent).append(Indent)
                        .append("\"").append("UpdateTime").append("\": \"")
                        .append(node.getUpdateTime()).append("\"");
            }
            if (tags != null && !tags.isEmpty()) {
                String t = null;
                for (DataNodeTag tag : tags) {
                    String v = tag.getTag().getTag();
                    if (t == null) {
                        t = v;
                    } else {
                        t += DataNode.TagSeparater + v;
                    }
                }
                s.append(",\n");
                s.append(Indent).append(Indent)
                        .append("\"").append(message("Tags")).append("\": ")
                        .append(JsonTools.encode(t));
            }
//            String infoJson = DataNode.infoJson(currentTask, this, node.getCategory(), node.getInfo(), Indent + Indent);
//            if (infoJson != null && !infoJson.isBlank()) {
//                s.append(infoJson);
//            }
            s.append("\n");
            s.append(Indent).append("}").append("\n");
            jsonWriter.write(s.toString());
        } catch (Exception e) {
            updateLogs(e.toString());
        }
    }

    @Override
    public void afterSuccess() {
        try {
            openTarget();
            if (openCheck.isSelected()) {
                if (framesetFile != null && framesetFile.exists()) {
                    WebBrowserController.openFile(framesetFile);
                }
                if (htmlFile != null && htmlFile.exists()) {
                    WebBrowserController.openFile(htmlFile);
                }
                if (xmlFile != null && xmlFile.exists()) {
                    XmlEditorController.open(xmlFile);
                }
                if (jsonFile != null && jsonFile.exists()) {
                    JsonEditorController.open(jsonFile);
                }
                if (textsFile != null && textsFile.exists()) {
                    TextEditorController.open(textsFile);
                }
            }
            popInformation(message("Count") + ": " + count);
        } catch (Exception e) {
            updateLogs(e.toString());
        }
    }

    @Override
    public void openTarget() {
        browseURI(targetPath.toURI());
    }

}
