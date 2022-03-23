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
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.TreeLeaf;
import mara.mybox.db.data.TreeNode;
import mara.mybox.db.table.TableTree;
import mara.mybox.db.table.TableTreeLeaf;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.style.HtmlStyles;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileNameTools;
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
    protected TableTree tableTree;
    protected TableTreeLeaf tableTreeLeaf;
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
    protected CheckBox timeCheck, iconCheck, textsCheck, htmlCheck, xmlCheck, jsonCheck, framesetCheck;
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

    public void setController(TreeManageController treeController) {
        this.treeController = treeController;
        this.tableTree = treeController.tableTree;
        this.tableTreeLeaf = treeController.tableTreeLeaf;
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
            iconCheck.setSelected(false);
            iconCheck.setVisible(false);
        }

        nodesController.setCaller(treeController.nodesController);
        if (treeView.getSelectionModel().getSelectedItem() == null) {
            treeView.getSelectionModel().select(treeView.getRoot());
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
            popError(message("NothingSave"));
            return false;
        }
        selectedNode = treeView.getSelectionModel().getSelectedItem();
        if (selectedNode == null) {
            selectedNode = treeView.getRoot();
            if (selectedNode == null) {
                popError(message("NoData"));
                return false;
            }
        }
        TreeItem<TreeNode> node = selectedNode;
        if (node.getValue() == null) {
            popError(message("NoData"));
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
            menu = new MenuItem(message("PopupClose"));
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
                targetFileGenerated(textsFile);
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
                targetFileGenerated(htmlFile);
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
                targetFileGenerated(framesetFile);
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
                targetFileGenerated(xmlFile);
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
                targetFileGenerated(jsonFile);
            } catch (Exception e) {
                updateLogs(e.toString());
                well = false;
            }
        }
        return well;
    }

    public void exportNode(Connection conn, TreeNode node, String baseName) {
        level++;
        if (conn == null || node == null) {
            return;
        }
        try {
            String title = node.getTitle() + "_" + node.getNodeid();
            FileWriter bookWriter = null, bookNavWriter = null;
            File nodeFile = null;
            if (framesetNavWriter != null) {
                File bookNavFile = new File(framesetNavFile.getParent() + File.separator + FileNameTools.filter(title) + "_nav.html");
                bookNavWriter = new FileWriter(bookNavFile, charset);
                writeHtmlHead(bookNavWriter, title);
                bookNavWriter.write(indent + "<BODY>\n");

                nodeFile = new File(framesetNavFile.getParent() + File.separator + FileNameTools.filter(title) + ".html");
                bookWriter = new FileWriter(nodeFile, charset);
                writeHtmlHead(bookWriter, title);
                bookWriter.write(indent + "<BODY>\n");

                String prefix = "";
                for (int i = 1; i < level; i++) {
                    prefix += "&nbsp;&nbsp;&nbsp;&nbsp;";
                }
                framesetNavWriter.write(prefix + "<A href=\"" + bookNavFile.getName() + "\"  target=booknav>" + node.getTitle() + "</A><BR>\n");
            }
            if (baseName != null) {
                title = baseName + TreeNode.NodeSeparater + node.getTitle();
            } else {
                title = node.getTitle();
            }
            List<TreeLeaf> leaves = tableTreeLeaf.leaves(conn, node.getNodeid());
            if (leaves != null && !leaves.isEmpty()) {
                for (TreeLeaf leaf : leaves) {
                    count++;
                    if (textsWriter != null) {
                        writeTexts(conn, title, leaf);
                    }
                    if (htmlWriter != null) {
                        writeHtml(conn, title, leaf, htmlWriter);
                    }
                    if (xmlWriter != null) {
                        writeXml(conn, title, leaf);
                    }
                    if (jsonWriter != null) {
                        writeJson(conn, title, leaf);
                    }
                    if (bookNavWriter != null && nodeFile != null) {
                        bookNavWriter.write("<A href=\"" + nodeFile.getName()
                                + "#" + leaf.getLeafid() + "\"  target=main>" + leaf.getName() + "</A><BR>\n");
                    }
                    if (bookWriter != null) {
                        writeHtml(conn, title, leaf, bookWriter);
                    }
                }
            } else {
                count++;
                if (textsWriter != null) {
                    writeTexts(conn, title, null);
                }
                if (htmlWriter != null) {
                    writeHtml(conn, title, null, htmlWriter);
                }
                if (xmlWriter != null) {
                    writeXml(conn, title, null);
                }
                if (jsonWriter != null) {
                    writeJson(conn, title, null);
                }
            }
            if (bookNavWriter != null) {
                try {
                    bookNavWriter.write(indent + "\n</BODY>\n</HTML>");
                    bookNavWriter.flush();
                    bookNavWriter.close();
                } catch (Exception e) {
                    updateLogs(e.toString());
                }
            }
            if (bookWriter != null) {
                try {
                    bookWriter.write(indent + "\n</BODY>\n</HTML>");
                    bookWriter.flush();
                    bookWriter.close();
                } catch (Exception e) {
                    updateLogs(e.toString());
                }
            }
            List<TreeNode> children = tableTree.children(conn, node.getNodeid());
            if (children != null) {
                for (TreeNode child : children) {
                    exportNode(conn, child, title);
                }
            }
        } catch (Exception e) {
            updateLogs(e.toString());
        }
        level--;
    }

    protected void writeTexts(Connection conn, String nodeName, TreeLeaf leaf) {
        try {
            if (!(treeController instanceof WebFavoritesController)) {
                textsWriter.write(AppValues.MyBoxSeparator + "\n");
            }
            textsWriter.write(nodeName + "\n");
            if (leaf != null) {
                textsWriter.write(leaf.getName() + "\n");
                if (timeCheck.isSelected() && leaf.getTime() != null) {
                    textsWriter.write(TreeLeaf.TimePrefix + DateTools.datetimeToString(leaf.getTime()) + "\n");
                }
                if (leaf.getValue() != null) {
                    textsWriter.write(leaf.getValue() + "\n");
                }
                if ((treeController instanceof WebFavoritesController)
                        && iconCheck.isSelected() && leaf.getMore() != null && !leaf.getMore().isBlank()) {
                    textsWriter.write(leaf.getMore() + "\n");
                }
            }
            textsWriter.write("\n");
        } catch (Exception e) {
            updateLogs(e.toString());
        }
    }

    protected void writeHtml(Connection conn, String nodeName, TreeLeaf leaf, FileWriter writer) {
        try {
            if (leaf == null) {
                writer.write(indent + indent + "<div>\n"
                        + indent + indent + indent + "<H3><PRE><CODE>" + nodeName + "</CODE></PRE></H3>\n");
                return;
            }
            writer.write(indent + indent + "<div id=\"" + leaf.getLeafid() + "\">\n"
                    + indent + indent + indent + "<H3><PRE><CODE>" + nodeName + "</CODE></PRE></H3>\n");
            if (treeController instanceof WebFavoritesController) {
                writer.write(indent + indent + indent + "<H4>");
                if (iconCheck.isSelected() && leaf.getMore() != null && !leaf.getMore().isBlank()) {
                    writer.write("<IMG src=\"" + new File(leaf.getMore()).toURI().toString() + "\" />");
                }
                writer.write("<A href=\"" + leaf.getValue() + "\">" + leaf.getName() + "</A></H4>\n");
            } else {
                writer.write(indent + indent + indent + "<H4><PRE><CODE>" + leaf.getName() + "</CODE></PRE></H4>\n");
                if (timeCheck.isSelected()) {
                    writer.write(indent + indent + indent + "<H5>" + DateTools.datetimeToString(leaf.getTime()) + "</H5>\n");
                }
                if (leaf.getValue() != null) {
                    if (treeController instanceof NotesController) {
                        writer.write(indent + indent + indent + leaf.getValue() + "\n"
                                + indent + indent + "</div><HR>\n\n");
                    } else {
                        writer.write(indent + indent + indent + "<PRE><CODE>" + leaf.getValue() + "</CODE></PRE>\n"
                                + indent + indent + "</div><HR>\n\n");
                    }
                }
            }
            writer.write(indent + indent + "</div><HR>\n\n");
        } catch (Exception e) {
            updateLogs(e.toString());
        }
    }

    protected void writeXml(Connection conn, String nodeName, TreeLeaf leaf) {
        try {
            xmlWriter.write(indent + indent + "<" + treeController.category + ">\n"
                    + indent + indent + indent + "<" + message("Node")
                    + "><![CDATA[" + nodeName + "]]></" + message("Node") + ">\n");
            if (leaf != null) {
                xmlWriter.write(indent + indent + indent + "<" + treeController.nameMsg
                        + "><![CDATA[" + leaf.getName() + "]]></" + treeController.nameMsg + ">\n");
                if (timeCheck.isSelected() && leaf.getTime() != null) {
                    xmlWriter.write(indent + indent + indent + "<" + treeController.timeMsg + ">"
                            + DateTools.datetimeToString(leaf.getTime())
                            + "</" + treeController.timeMsg + ">\n");
                }
                if (treeController instanceof WebFavoritesController) {
                    if (iconCheck.isSelected() && leaf.getMore() != null && !leaf.getMore().isBlank()) {
                        xmlWriter.write(indent + indent + indent + "<" + treeController.moreMsg
                                + "><![CDATA[" + leaf.getMore()
                                + "]]></" + treeController.moreMsg + ">\n");
                    }

                }
                if (leaf.getValue() != null) {
                    xmlWriter.write(indent + indent + indent + "<" + treeController.valueMsg + ">\n"
                            + "<![CDATA[" + leaf.getValue() + "]]>\n"
                            + indent + indent + indent + "</" + treeController.valueMsg + ">\n");
                }
            }
            xmlWriter.write(indent + indent + "</" + treeController.category + ">\n\n");

        } catch (Exception e) {
            updateLogs(e.toString());
        }
    }

    protected void writeJson(Connection conn, String bookName, TreeLeaf leaf) {
        try {
            StringBuilder s = new StringBuilder();
            if (!firstRow) {
                s.append(",\n");
            } else {
                firstRow = false;
            }
            s.append(indent).append("{").append("\n");
            s.append(indent).append(indent)
                    .append("\"node\": \"")
                    .append(bookName).append("\"");
            if (leaf == null) {
            } else {
                s.append(",\n");
                s.append(indent).append(indent)
                        .append("\"").append(treeController.nameMsg).append("\": \"")
                        .append(leaf.getName()).append("\"");
                if (timeCheck.isSelected() && leaf.getTime() != null) {
                    s.append(",\n");
                    s.append(indent).append(indent)
                            .append("\"").append(treeController.timeMsg).append("\": \"")
                            .append(leaf.getTime()).append("\"");
                }
                if (leaf.getValue() != null) {
                    s.append(",\n");
                    s.append(indent).append(indent)
                            .append("\"").append(treeController.valueMsg).append("\": \"")
                            .append(leaf.getValue()).append("\"");
                }
                if ((treeController instanceof WebFavoritesController)
                        && iconCheck.isSelected() && leaf.getMore() != null && !leaf.getMore().isBlank()) {
                    s.append(",\n");
                    s.append(indent).append(indent)
                            .append("\"").append(treeController.moreMsg).append("\": \"")
                            .append(leaf.getValue()).append("\"");
                }
                s.append("\n");
            }
            s.append(indent).append("}").append("\n");
            jsonWriter.write(s.toString());
        } catch (Exception e) {
            updateLogs(e.toString());
        }
    }

    @Override
    public void afterSuccess() {
        browseURI(targetPath.toURI());
        if (framesetFile != null && framesetFile.exists()) {
            WebBrowserController.oneOpen(framesetFile);
            return;
        }
        if (htmlFile != null && htmlFile.exists()) {
            WebBrowserController.oneOpen(htmlFile);
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
    }

    @Override
    public void openTarget(ActionEvent event) {
        browseURI(targetPath.toURI());
    }

}
