package mara.mybox.controller;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.util.List;
import javafx.beans.binding.Bindings;
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
import mara.mybox.db.data.TreeNode;
import mara.mybox.db.data.WebFavorite;
import mara.mybox.db.table.TableTree;
import mara.mybox.db.table.TableWebFavorite;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeTools;
import static mara.mybox.fxml.NodeTools.badStyle;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.TextTools;
import static mara.mybox.value.AppValues.Indent;
import mara.mybox.value.Fxmls;
import mara.mybox.value.HtmlStyles;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-4-30
 * @License Apache License Version 2.0
 */
public class WebFavoritesExportController extends BaseTaskController {

    protected TreeView<TreeNode> treeView;
    protected WebFavoritesController favoriteController;
    protected TableTree tableTree;
    protected TableWebFavorite tableFavoriteAddress;
    protected TreeItem<TreeNode> selectedNode;
    protected File textsFile, xmlFile, jsonFile, htmlFile, framesetFile, framesetNavFile;
    protected FileWriter textsWriter, htmlWriter, xmlWriter, jsonWriter, framesetNavWriter;
    protected final String indent = "    ";
    protected int count, level;
    protected Charset charset;
    protected boolean firstRow;

    @FXML
    protected ControlWebFavoriateNodes treeController;
    @FXML
    protected CheckBox iconCheck, textsCheck, htmlCheck, xmlCheck, jsonCheck, framesetCheck;
    @FXML
    protected ComboBox<String> charsetSelector;
    @FXML
    protected TextArea styleInput;

    public WebFavoritesExportController() {
        baseTitle = Languages.message("FavoritesExport");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            treeView = treeController.treeView;

            iconCheck.setSelected(UserConfig.getUserConfigBoolean(baseName + "Icon", false));
            iconCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) {
                    UserConfig.setUserConfigBoolean(baseName + "Icon", iconCheck.isSelected());
                }
            });

            textsCheck.setSelected(UserConfig.getUserConfigBoolean(baseName + "Texts", true));
            textsCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) {
                    UserConfig.setUserConfigBoolean(baseName + "Texts", textsCheck.isSelected());
                }
            });

            htmlCheck.setSelected(UserConfig.getUserConfigBoolean(baseName + "Html", false));
            htmlCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) {
                    UserConfig.setUserConfigBoolean(baseName + "Html", htmlCheck.isSelected());
                }
            });

            framesetCheck.setSelected(UserConfig.getUserConfigBoolean(baseName + "Frameset", false));
            framesetCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) {
                    UserConfig.setUserConfigBoolean(baseName + "Frameset", framesetCheck.isSelected());
                }
            });

            xmlCheck.setSelected(UserConfig.getUserConfigBoolean(baseName + "Xml", false));
            xmlCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) {
                    UserConfig.setUserConfigBoolean(baseName + "Xml", xmlCheck.isSelected());
                }
            });

            jsonCheck.setSelected(UserConfig.getUserConfigBoolean(baseName + "Json", false));
            jsonCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) {
                    UserConfig.setUserConfigBoolean(baseName + "Json", jsonCheck.isSelected());
                }
            });

            List<String> setNames = TextTools.getCharsetNames();
            charsetSelector.getItems().addAll(setNames);
            try {
                charset = Charset.forName(UserConfig.getUserConfigString(baseName + "Charset", Charset.defaultCharset().name()));
            } catch (Exception e) {
                charset = Charset.defaultCharset();
            }
            charsetSelector.setValue(charset.name());
            charsetSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    charset = Charset.forName(charsetSelector.getSelectionModel().getSelectedItem());
                    UserConfig.setUserConfigString(baseName + "Charset", charset.name());
                }
            });

            styleInput.setText(UserConfig.getUserConfigString(baseName + "Style", null));

            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(
                    Bindings.isEmpty(targetPathInput.textProperty())
                            .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                            .or(treeView.getSelectionModel().selectedItemProperty().isNull())
            );

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setController(WebFavoritesController favoriteController) {
        this.favoriteController = favoriteController;
        this.tableTree = favoriteController.tableTree;
        this.tableFavoriteAddress = favoriteController.tableWebFavorite;

        treeController.setCaller(favoriteController.treeController);
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
            popError(Languages.message("NothingSave"));
            return false;
        }
        selectedNode = treeView.getSelectionModel().getSelectedItem();
        if (selectedNode == null) {
            selectedNode = treeView.getRoot();
            if (selectedNode == null) {
                popError(Languages.message("NoData"));
                return false;
            }
        }
        TreeItem<TreeNode> node = selectedNode;
        if (node.getValue() == null) {
            popError(Languages.message("NoData"));
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
                menu = new MenuItem(Languages.message(style.name()));
                menu.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        styleInput.setText(HtmlStyles.styleValue(style));
                        UserConfig.setUserConfigString(baseName + "Style", styleInput.getText());
                    }
                });
                popMenu.getItems().add(menu);
            }

            popMenu.getItems().add(new SeparatorMenuItem());
            menu = new MenuItem(Languages.message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    popMenu.hide();
                }
            });
            popMenu.getItems().add(menu);

            NodeTools.locateCenter((Region) mouseEvent.getSource(), popMenu);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void clearStyle() {
        styleInput.clear();
    }

    @Override
    protected boolean doTask() {
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
            exportNode(conn, selectedNode.getValue(), treeController.chainName(selectedNode.getParent()));
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
            String prefix = nodeName.replaceAll(TreeNode.NodeSeparater, "-") + "_" + DateTools.nowFileString();

            if (textsCheck.isSelected()) {
                textsFile = makeTargetFile(prefix, ".txt", targetPath);
                if (textsFile != null) {
                    updateLogs(Languages.message("Writing") + " " + textsFile.getAbsolutePath());
                    textsWriter = new FileWriter(textsFile, charset);
                } else if (targetExistType == TargetExistType.Skip) {
                    updateLogs(Languages.message("Skipped"));
                }
            }
            if (htmlCheck.isSelected()) {
                htmlFile = makeTargetFile(prefix, ".html", targetPath);
                if (htmlFile != null) {
                    updateLogs(Languages.message("Writing") + " " + htmlFile.getAbsolutePath());
                    htmlWriter = new FileWriter(htmlFile, charset);
                    writeHtmlHead(htmlWriter, nodeName);
                    htmlWriter.write(indent + "<BODY>\n" + indent + indent + "<H2>" + nodeName + "</H2>\n");
                } else if (targetExistType == TargetExistType.Skip) {
                    updateLogs(Languages.message("Skipped"));
                }
            }
            if (framesetCheck.isSelected()) {
                framesetFile = makeTargetFile(prefix, "-frameset.html", targetPath);
                if (framesetFile != null) {
                    updateLogs(Languages.message("Writing") + " " + framesetFile.getAbsolutePath());
                    StringBuilder s;
                    String subPath = FileNameTools.filenameFilter(prefix) + "-frameset";
                    File path = new File(targetPath + File.separator + subPath + File.separator);
                    path.mkdirs();
                    framesetNavFile = new File(path.getAbsolutePath() + File.separator + "nav.html");
                    File coverFile = new File(path.getAbsolutePath() + File.separator + "cover.html");
                    try ( FileWriter coverWriter = new FileWriter(coverFile, charset)) {
                        writeHtmlHead(coverWriter, nodeName);
                        coverWriter.write("<BODY>\n<BR><BR><BR><BR><H1>" + Languages.message("Notes") + "</H1>\n</BODY></HTML>");
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
                } else if (targetExistType == TargetExistType.Skip) {
                    updateLogs(Languages.message("Skipped"));
                }
            }
            if (xmlCheck.isSelected()) {
                xmlFile = makeTargetFile(prefix, ".xml", targetPath);
                if (xmlFile != null) {
                    updateLogs(Languages.message("Writing") + " " + xmlFile.getAbsolutePath());
                    xmlWriter = new FileWriter(xmlFile, charset);
                    StringBuilder s = new StringBuilder();
                    s.append("<?xml version=\"1.0\" encoding=\"")
                            .append(charset.name()).append("\"?>\n").append("<favorites>\n");
                    xmlWriter.write(s.toString());
                } else if (targetExistType == TargetExistType.Skip) {
                    updateLogs(Languages.message("Skipped"));
                }
            }
            if (jsonCheck.isSelected()) {
                jsonFile = makeTargetFile(prefix, ".json", targetPath);
                if (jsonFile != null) {
                    updateLogs(Languages.message("Writing") + " " + jsonFile.getAbsolutePath());
                    jsonWriter = new FileWriter(jsonFile, Charset.forName("UTF-8"));
                    StringBuilder s = new StringBuilder();
                    s.append("{\"Favorites\": [\n");
                    jsonWriter.write(s.toString());
                } else if (targetExistType == TargetExistType.Skip) {
                    updateLogs(Languages.message("Skipped"));
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
                xmlWriter.write("</favorites>\n");
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
                File bookNavFile = new File(framesetNavFile.getParent() + File.separator + FileNameTools.filenameFilter(title) + "_nav.html");
                bookNavWriter = new FileWriter(bookNavFile, charset);
                writeHtmlHead(bookNavWriter, title);
                bookNavWriter.write(indent + "<BODY>\n");

                nodeFile = new File(framesetNavFile.getParent() + File.separator + FileNameTools.filenameFilter(title) + ".html");
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
            List<WebFavorite> favorites = tableFavoriteAddress.addresses(conn, node.getNodeid());
            if (favorites != null) {
                for (WebFavorite favorite : favorites) {
                    count++;
                    if (textsWriter != null) {
                        writeTexts(conn, title, favorite);
                    }
                    if (htmlWriter != null) {
                        writeHtml(conn, title, favorite, htmlWriter);
                    }
                    if (xmlWriter != null) {
                        writeXml(conn, title, favorite);
                    }
                    if (jsonWriter != null) {
                        writeJson(conn, title, favorite);
                    }
                    if (bookNavWriter != null && nodeFile != null) {
                        bookNavWriter.write("<A href=\"" + nodeFile.getName()
                                + "#" + favorite.getFaid() + "\"  target=main>" + favorite.getTitle() + "</A><BR>\n");
                    }
                    if (bookWriter != null) {
                        writeHtml(conn, title, favorite, bookWriter);
                    }
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

    protected void writeTexts(Connection conn, String bookName, WebFavorite favorite) {
        try {
            textsWriter.write(bookName + "\n");
            textsWriter.write(favorite.getTitle() + "\n");
            textsWriter.write(favorite.getAddress() + "\n");
            if (iconCheck.isSelected()) {
                textsWriter.write(favorite.getIcon() + "\n\n");
            }
        } catch (Exception e) {
            updateLogs(e.toString());
        }
    }

    protected void writeHtml(Connection conn, String bookName, WebFavorite favorite, FileWriter writer) {
        try {
            writer.write(indent + indent + "<div id=\"" + favorite.getFaid() + "\">\n"
                    + indent + indent + indent + "<H3><PRE><CODE>" + bookName + "</CODE></PRE></H3>\n");
            writer.write(indent + indent + indent + "<H4>");
            if (iconCheck.isSelected() && favorite.getIcon() != null && !favorite.getIcon().isBlank()) {
                writer.write("<IMG src=\"" + new File(favorite.getIcon()).toURI().toString() + "\">");
            }
            writer.write("<A href=\"" + favorite.getAddress() + "\">" + favorite.getTitle() + "</A></H4>\n");
            writer.write(indent + indent + "</div><HR>\n\n");
        } catch (Exception e) {
            updateLogs(e.toString());
        }
    }

    protected void writeXml(Connection conn, String bookName, WebFavorite favorite) {
        try {
            xmlWriter.write(indent + indent + "<favorite>\n"
                    + indent + indent + indent + "<node><![CDATA[" + bookName + "]]></node>\n");
            xmlWriter.write(indent + indent + indent + "<title><![CDATA[" + favorite.getTitle() + "]]></title>\n");
            xmlWriter.write(indent + indent + indent + "<address><![CDATA[" + favorite.getAddress() + "]]></address>\n");
            if (iconCheck.isSelected() && favorite.getIcon() != null && !favorite.getIcon().isBlank()) {
                xmlWriter.write(indent + indent + indent + "<icon><![CDATA[" + favorite.getIcon() + "]]></icon>\n");
            }
            xmlWriter.write(indent + indent + "</favorite>\n\n");
        } catch (Exception e) {
            updateLogs(e.toString());
        }
    }

    protected void writeJson(Connection conn, String bookName, WebFavorite favorite) {
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
                    .append(bookName).append("\",\n");
            s.append(indent).append(indent)
                    .append("\"title\": \"")
                    .append(favorite.getTitle()).append("\",\n");
            s.append(indent).append(indent)
                    .append("\"address\": \"")
                    .append(favorite.getAddress()).append("\"");
            if (iconCheck.isSelected() && favorite.getIcon() != null && !favorite.getIcon().isBlank()) {
                s.append(",\n")
                        .append(indent).append(indent)
                        .append("\"icon\": \"")
                        .append(new File(favorite.getIcon()).toURI().toString()).append("\"\n");
            } else {
                s.append("\n");
            }
            s.append(indent).append("}").append("\n");
            jsonWriter.write(s.toString());
        } catch (Exception e) {
            updateLogs(e.toString());
        }
    }

    @Override
    protected void afterSuccess() {
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
            TextEditerController controller = (TextEditerController) openStage(Fxmls.TextEditerFxml);
            controller.sourceFileChanged(textsFile);
        }
        popInformation(Languages.message("Count") + ": " + count);
    }

}
