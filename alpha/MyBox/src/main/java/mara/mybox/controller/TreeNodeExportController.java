package mara.mybox.controller;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.TreeNode;
import mara.mybox.db.data.TreeNodeTag;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.table.TableTreeNode;
import mara.mybox.db.table.TableTreeNodeTag;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.style.HtmlStyles;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.JsonTools;
import mara.mybox.tools.TextTools;
import mara.mybox.value.AppValues;
import static mara.mybox.value.AppValues.Indent;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-4-30
 * @License Apache License Version 2.0
 */
public class TreeNodeExportController extends BaseTaskController {

    protected TreeManageController treeController;
    protected TableTreeNode tableTreeNode;
    protected TableTreeNodeTag tableTreeNodeTag;
    protected TreeView<TreeNode> treeView;
    protected TreeItem<TreeNode> selectedNode;
    protected File textsFile, xmlFile, jsonFile, htmlFile, framesetFile, framesetNavFile;
    protected FileWriter textsWriter, htmlWriter, xmlWriter, jsonWriter, framesetNavWriter;
    protected final String indent = "    ";
    protected int count, level;
    protected Charset charset;
    protected boolean firstRow;

    @FXML
    protected TreeNodesController nodesController;
    @FXML
    protected CheckBox timeCheck, tagsCheck, iconCheck,
            textsCheck, htmlCheck, xmlCheck, jsonCheck, framesetCheck;
    @FXML
    protected ComboBox<String> charsetSelector;
    @FXML
    protected TextArea styleInput;

    public TreeNodeExportController() {
        baseTitle = message("Export");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            treeView = nodesController.treeView;

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

            iconCheck.setSelected(UserConfig.getBoolean(baseName + "Icons", true));
            iconCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) {
                    UserConfig.setBoolean(baseName + "Icons", iconCheck.isSelected());
                }
            });

            textsCheck.setSelected(UserConfig.getBoolean(baseName + "Texts", true));
            textsCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) {
                    UserConfig.setBoolean(baseName + "Texts", textsCheck.isSelected());
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

            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(targetPathController.valid.not()
                    .or(treeView.getSelectionModel().selectedItemProperty().isNull())
            );

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setParamters(TreeManageController treeController, TreeItem<TreeNode> item) {
        this.treeController = treeController;
        this.tableTreeNode = treeController.tableTreeNode;
        this.tableTreeNodeTag = treeController.tableTreeNodeTag;
        if (treeController instanceof WebFavoritesController) {
            iconCheck.setVisible(true);
            iconCheck.setSelected(UserConfig.getBoolean(baseName + "Icon", false));
            iconCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) {
                    UserConfig.setBoolean(baseName + "Icon", iconCheck.isSelected());
                }
            });
        } else {
            if (treeController instanceof NotesController) {
                jsonCheck.setSelected(false);
                jsonCheck.setVisible(false);
            }
            iconCheck.setSelected(false);
            iconCheck.setVisible(false);
        }
        nodesController.setCaller(treeController.nodesController);
        if (item == null) {
            treeView.getSelectionModel().select(treeView.getRoot());
        } else {
            treeView.getSelectionModel().select(item);
        }
    }

    @Override
    public boolean checkOptions() {
        textsFile = null;
        xmlFile = null;
        htmlFile = null;
        framesetFile = null;
        textsWriter = null;
        htmlWriter = null;
        xmlWriter = null;
        framesetNavWriter = null;
        level = count = 0;
        if (!textsCheck.isSelected() && !htmlCheck.isSelected()
                && !framesetCheck.isSelected() && !xmlCheck.isSelected()) {
            if (treeController instanceof NotesController || !jsonCheck.isSelected()) {
                popError(message("NothingSave"));
                return false;
            }
        }
        selectedNode = treeView.getSelectionModel().getSelectedItem();
        if (selectedNode == null) {
            selectedNode = treeView.getRoot();
            if (selectedNode == null) {
                popError(message("SelectToHandle"));
                return false;
            }
        }
        if (selectedNode.getValue() == null) {
            popError(message("SelectToHandle"));
            return false;
        }
        return true;
    }

    @FXML
    public void popDefaultStyle(MouseEvent mouseEvent) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

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
                popMenu.getItems().add(menu);
            }

            popMenu.getItems().add(new SeparatorMenuItem());
            menu = new MenuItem(message("PopupClose"), StyleTools.getIconImageView("iconCancel.png"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    popMenu.hide();
                }
            });
            popMenu.getItems().add(menu);

            LocateTools.locateCenter((Region) mouseEvent.getSource(), popMenu);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void clearStyle() {
        styleInput.clear();
    }

    @Override
    public boolean doTask() {
        if (selectedNode == null || targetPath == null) {
            return false;
        }
        if (!openWriters()) {
            closeWriters();
            return false;
        }
        count = level = 0;
        firstRow = true;
        try ( Connection conn = DerbyBase.getConnection()) {
            exportNode(conn, selectedNode.getValue(), nodesController.chainName(selectedNode.getParent()));
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
            String nodeName = nodesController.chainName(selectedNode);
            String prefix = nodeName.replaceAll(TreeNode.NodeSeparater, "-") + "_" + DateTools.nowFileString();

            if (textsCheck.isSelected()) {
                textsFile = makeTargetFile(prefix, ".txt", targetPath);
                if (textsFile != null) {
                    updateLogs(message("Writing") + " " + textsFile.getAbsolutePath());
                    textsWriter = new FileWriter(textsFile, charset);
                } else if (targetPathController.isSkip()) {
                    updateLogs(message("Skipped"));
                }
            }
            if (htmlCheck.isSelected()) {
                htmlFile = makeTargetFile(prefix, ".html", targetPath);
                if (htmlFile != null) {
                    updateLogs(message("Writing") + " " + htmlFile.getAbsolutePath());
                    htmlWriter = new FileWriter(htmlFile, charset);
                    writeHtmlHead(htmlWriter, nodeName);
                    htmlWriter.write(indent + "<BODY>\n" + indent + indent + "<H2>" + nodeName + "</H2>\n");
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
                    try ( FileWriter coverWriter = new FileWriter(coverFile, charset)) {
                        writeHtmlHead(coverWriter, nodeName);
                        coverWriter.write("<BODY>\n<BR><BR><BR><BR><H1>" + message("Notes") + "</H1>\n</BODY></HTML>");
                        coverWriter.flush();
                    }
                    try ( FileWriter framesetWriter = new FileWriter(framesetFile, charset)) {
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
                    s.append(indent).append("<BODY>\n");
                    s.append(indent).append(indent).append("<H2>").append(nodeName).append("</H2>\n");
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
                            .append("<").append(treeController.category).append(">\n");
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
                    s.append("{\"").append(treeController.category).append("\": [\n");
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
            s.append("<!DOCTYPE html><HTML>\n").append(indent).append("<HEAD>\n")
                    .append(indent).append(indent).append("<title>").append(title).append("</title>\n")
                    .append(indent).append(indent)
                    .append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=")
                    .append(charset.name()).append("\" />\n");
            String style = styleInput.getText();
            if (style != null && !style.isBlank()) {
                s.append(Indent).append(Indent).append("<style type=\"text/css\">\n");
                s.append(Indent).append(Indent).append(Indent).append(style).append("\n");
                s.append(Indent).append(Indent).append("</style>\n");
            }
            s.append(indent).append("</HEAD>\n");
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
                htmlWriter.write(indent + "</BODY>\n</HTML>\n");
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
                framesetNavWriter.write(indent + "</BODY>\n</HTML>\n");
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
                xmlWriter.write("</" + treeController.category + ">\n");
                xmlWriter.flush();
                xmlWriter.close();
                xmlWriter = null;
                targetFileGenerated(xmlFile, VisitHistory.FileType.Xml);
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
                targetFileGenerated(jsonFile, VisitHistory.FileType.Text);
            } catch (Exception e) {
                updateLogs(e.toString());
                well = false;
            }
        }
        return well;
    }

    public void exportNode(Connection conn, TreeNode node, String parentChainName) {
        level++;
        if (conn == null || node == null) {
            return;
        }
        try {
            count++;
            List<TreeNodeTag> tags = null;
            if (tagsCheck.isSelected()) {
                tags = tableTreeNodeTag.nodeTags(conn, node.getNodeid());
            }
            if (textsWriter != null) {
                writeTexts(conn, parentChainName, node, tags);
            }
            if (htmlWriter != null) {
                writeHtml(conn, parentChainName, node, htmlWriter, tags);
            }
            if (xmlWriter != null) {
                writeXml(conn, parentChainName, node, tags);
            }
            if (jsonWriter != null) {
                writeJson(conn, parentChainName, node, tags);
            }
            String nodeChainName;
            if (parentChainName != null && !parentChainName.isBlank()) {
                nodeChainName = parentChainName + TreeNode.NodeSeparater + node.getTitle();
            } else {
                nodeChainName = node.getTitle();
            }
            List<TreeNode> children = tableTreeNode.children(conn, node.getNodeid());

            if (framesetNavWriter != null) {
                String nodeTitle = node.getTitle() + "_" + node.getNodeid();
                File bookNavFile = new File(framesetNavFile.getParent() + File.separator + FileNameTools.filter(nodeTitle) + "_nav.html");
                FileWriter bookNavWriter = new FileWriter(bookNavFile, charset);
                writeHtmlHead(bookNavWriter, nodeTitle);
                bookNavWriter.write(indent + "<BODY>\n");

                File nodeFile = new File(framesetNavFile.getParent() + File.separator + FileNameTools.filter(nodeTitle) + ".html");
                FileWriter bookWriter = new FileWriter(nodeFile, charset);
                writeHtmlHead(bookWriter, nodeTitle);
                bookWriter.write(indent + "<BODY>\n");

                String prefix = "";
                for (int i = 1; i < level; i++) {
                    prefix += "&nbsp;&nbsp;&nbsp;&nbsp;";
                }
                framesetNavWriter.write(prefix + "<A href=\"" + bookNavFile.getName() + "\"  target=booknav>" + node.getTitle() + "</A><BR>\n");

                writeHtml(conn, nodeChainName, node, bookWriter, tags);
                try {
                    bookWriter.write(indent + "\n</BODY>\n</HTML>");
                    bookWriter.flush();
                    bookWriter.close();
                } catch (Exception e) {
                    updateLogs(e.toString());
                }

                if (children != null && !children.isEmpty()) {
                    for (TreeNode child : children) {
                        File childFile = new File(framesetNavFile.getParent() + File.separator
                                + FileNameTools.filter(child.getTitle() + "_" + child.getNodeid()) + ".html");
                        bookNavWriter.write("<A href=\"" + childFile.getName() + "\"  target=main>" + child.getTitle() + "</A><BR>\n");
                    }
                } else {
                    bookNavWriter.write("<A href=\"" + nodeFile.getName() + "\"  target=main>" + node.getTitle() + "</A><BR>\n");
                }
                try {
                    bookNavWriter.write(indent + "\n</BODY>\n</HTML>");
                    bookNavWriter.flush();
                    bookNavWriter.close();
                } catch (Exception e) {
                    updateLogs(e.toString());
                }
            }

            if (children != null) {
                for (TreeNode child : children) {
                    exportNode(conn, child, nodeChainName);
                }
            }
        } catch (Exception e) {
            updateLogs(e.toString());
        }
        level--;
    }

    protected void writeTexts(Connection conn, String parentName, TreeNode node, List<TreeNodeTag> tags) {
        try {
            if (!(treeController instanceof WebFavoritesController)) {
                textsWriter.write(AppValues.MyBoxSeparator + "\n");
            }
            textsWriter.write((parentName == null ? TreeNode.RootIdentify : parentName) + "\n");
            textsWriter.write(node.getTitle() + "\n");
            if (timeCheck.isSelected() && node.getUpdateTime() != null) {
                textsWriter.write(TreeNode.TimePrefix + DateTools.datetimeToString(node.getUpdateTime()) + "\n");
            }
            if (tags != null && !tags.isEmpty()) {
                String s = null;
                for (TreeNodeTag tag : tags) {
                    Color color = tag.getTag().getColor();
                    if (color == null) {
                        color = FxColorTools.randomColor();
                    }
                    String v = tag.getTag().getTag() + TreeNode.TagsSeparater + color.toString();
                    if (s == null) {
                        s = v;
                    } else {
                        s += TreeNode.TagsSeparater + v;
                    }
                }
                textsWriter.write(TreeNode.TagsPrefix + s + "\n");
            }
            if (node.getValue() != null) {
                textsWriter.write(node.getValue() + "\n");
            }
            if (node.getMore() != null && !node.getMore().isBlank()) {
                if (!(treeController instanceof WebFavoritesController) || iconCheck.isSelected()) {
                    textsWriter.write(node.getMore() + "\n");
                }
            }

            textsWriter.write("\n");
        } catch (Exception e) {
            updateLogs(e.toString());
        }
    }

    protected void writeHtml(Connection conn, String parentName, TreeNode node, FileWriter writer, List<TreeNodeTag> tags) {
        try {
            writer.write(indent + indent + "<div id=\"" + node.getNodeid() + "\">\n");
            if (parentName != null) {
                writer.write(indent + indent + indent + "<H3><PRE><CODE>" + parentName + "</CODE></PRE></H3>\n");
            }
            if (timeCheck.isSelected() && node.getUpdateTime() != null) {
                writer.write(indent + indent + indent + "<H5>" + DateTools.datetimeToString(node.getUpdateTime()) + "</H5>\n");
            }
            if (tags != null && !tags.isEmpty()) {
                writer.write(indent + indent + indent + "<H4>");
                for (TreeNodeTag nodeTag : tags) {
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
            if (treeController instanceof WebFavoritesController) {
                writer.write(indent + indent + indent + "<H4>");
                if (iconCheck.isSelected() && node.getMore() != null && !node.getMore().isBlank()) {
                    writer.write("<IMG src=\"" + new File(node.getMore()).toURI().toString() + "\" width=40/>");
                }
                writer.write("<A href=\"" + node.getValue() + "\">" + node.getTitle() + "</A></H4>\n");
            } else {
                writer.write(indent + indent + indent + "<H4><PRE><CODE>" + node.getTitle() + "</CODE></PRE></H4>\n");
                if (node.getValue() != null) {
                    if (treeController instanceof NotesController) {
                        writer.write(indent + indent + indent + node.getValue() + "\n"
                                + indent + indent + "</div>\n\n");
                    } else {
                        writer.write(indent + indent + indent + "<PRE><CODE>" + node.getValue() + "</CODE></PRE>\n"
                                + indent + indent + "</div>\n\n");
                    }
                }
                if (node.getMore() != null && !node.getMore().isBlank()) {
                    writer.write(indent + indent + indent + "<PRE><CODE>" + node.getMore() + "</CODE></PRE>\n"
                            + indent + indent + "</div>\n\n");
                }

            }
            writer.write(indent + indent + "</div><HR>\n\n");
        } catch (Exception e) {
            updateLogs(e.toString());
        }
    }

    protected void writeXml(Connection conn, String parentName, TreeNode node, List<TreeNodeTag> tags) {
        try {
            xmlWriter.write(indent + indent + "<" + treeController.category + ">\n");
            if (parentName != null) {
                xmlWriter.write(indent + indent + indent + "<" + message("Node")
                        + "><![CDATA[" + parentName + "]]></" + message("Node") + ">\n");
            }
            xmlWriter.write(indent + indent + indent + "<" + treeController.nameMsg
                    + "><![CDATA[" + node.getTitle() + "]]></" + treeController.nameMsg + ">\n");
            if (timeCheck.isSelected() && node.getUpdateTime() != null) {
                xmlWriter.write(indent + indent + indent + "<" + treeController.timeMsg + ">"
                        + DateTools.datetimeToString(node.getUpdateTime())
                        + "</" + treeController.timeMsg + ">\n");
            }
            if (tags != null && !tags.isEmpty()) {
                String s = null;
                for (TreeNodeTag tag : tags) {
                    String v = tag.getTag().getTag();
                    if (s == null) {
                        s = v;
                    } else {
                        s += TreeNode.TagsSeparater + v;
                    }
                }
                xmlWriter.write(indent + indent + indent + "<" + message("Tags")
                        + "><![CDATA[" + s + "]]></" + message("Tags") + ">\n");
            }
            if (node.getMore() != null && !node.getMore().isBlank()) {
                if (!(treeController instanceof WebFavoritesController) || iconCheck.isSelected()) {
                    xmlWriter.write(indent + indent + indent + "<" + treeController.moreMsg
                            + "><![CDATA[" + node.getMore()
                            + "]]></" + treeController.moreMsg + ">\n");
                }
            }
            if (node.getValue() != null) {
                xmlWriter.write(indent + indent + indent + "<" + treeController.valueMsg + ">\n"
                        + "<![CDATA[" + node.getValue() + "]]>\n"
                        + indent + indent + indent + "</" + treeController.valueMsg + ">\n");
            }
            xmlWriter.write(indent + indent + "</" + treeController.category + ">\n\n");

        } catch (Exception e) {
            updateLogs(e.toString());
        }
    }

    protected void writeJson(Connection conn, String parentName, TreeNode node, List<TreeNodeTag> tags) {
        try {
            StringBuilder s = new StringBuilder();
            if (!firstRow) {
                s.append(",\n");
            } else {
                firstRow = false;
            }
            s.append(indent).append("{").append("\n");
            if (parentName != null) {
                s.append(indent).append(indent)
                        .append("\"node\": \"")
                        .append(parentName).append("\",\n");
            }
            s.append(indent).append(indent)
                    .append("\"").append(treeController.nameMsg).append("\": \"")
                    .append(node.getTitle()).append("\"");
            if (timeCheck.isSelected() && node.getUpdateTime() != null) {
                s.append(",\n");
                s.append(indent).append(indent)
                        .append("\"").append(treeController.timeMsg).append("\": \"")
                        .append(node.getUpdateTime()).append("\"");
            }
            if (tags != null && !tags.isEmpty()) {
                String t = null;
                for (TreeNodeTag tag : tags) {
                    String v = tag.getTag().getTag();
                    if (t == null) {
                        t = v;
                    } else {
                        t += TreeNode.TagsSeparater + v;
                    }
                }
                t = t.replaceAll("\\[|\\]", "");
                s.append(",\n");
                s.append(indent).append(indent)
                        .append("\"").append(message("Tags")).append("\": \"")
                        .append(JsonTools.replaceSpecialChars(t)).append("\"");
            }
            if (node.getValue() != null) {
                s.append(",\n");
                s.append(indent).append(indent)
                        .append("\"").append(treeController.valueMsg).append("\": \"")
                        .append(JsonTools.replaceSpecialChars(node.getValue())).append("\"");
            }
            if (node.getMore() != null && !node.getMore().isBlank()) {
                if (!(treeController instanceof WebFavoritesController) || iconCheck.isSelected()) {
                    s.append(",\n");
                    s.append(indent).append(indent)
                            .append("\"").append(treeController.moreMsg).append("\": \"")
                            .append(node.getMore().replaceAll("\\\\", "/")).append("\"");
                }
            }

            s.append("\n");
            s.append(indent).append("}").append("\n");
            jsonWriter.write(s.toString());
        } catch (Exception e) {
            updateLogs(e.toString());
        }
    }

    @Override
    public void afterSuccess() {
        try {
            openTarget(null);
            if (framesetFile != null && framesetFile.exists()) {
                WebBrowserController.openFile(framesetFile);
                return;
            }
            if (htmlFile != null && htmlFile.exists()) {
                WebBrowserController.openFile(htmlFile);
                return;
            }
            if (xmlFile != null && xmlFile.exists()) {
                browseURI(xmlFile.toURI());
                return;
            }
            if (jsonFile != null && jsonFile.exists()) {
                browseURI(jsonFile.toURI());
                return;
            }
            if (textsFile != null && textsFile.exists()) {
                TextEditorController controller = (TextEditorController) openStage(Fxmls.TextEditorFxml);
                controller.sourceFileChanged(textsFile);
            }
            popInformation(message("Count") + ": " + count);
        } catch (Exception e) {
            updateLogs(e.toString());
        }
    }

    @Override
    public void openTarget(ActionEvent event) {
        browseURI(targetPath.toURI());
    }

}
