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
import mara.mybox.db.data.Note;
import mara.mybox.db.data.Notebook;
import static mara.mybox.db.data.Notebook.NotebookNameSeparater;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.table.TableNote;
import mara.mybox.db.table.TableNotebook;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.style.HtmlStyles;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.StringTools;
import mara.mybox.tools.TextTools;
import mara.mybox.value.AppValues;
import static mara.mybox.value.AppValues.Indent;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-3-16
 * @License Apache License Version 2.0
 */
public class NotesExportController extends BaseTaskController {

    protected TreeView<Notebook> treeView;
    protected NotesController notesController;
    protected TableNotebook tableNotebook;
    protected TableNote tableNote;
    protected TreeItem<Notebook> selectedNode;
    protected File textsFile, xmlFile, htmlFile, framesetFile, framesetNavFile;
    protected FileWriter textsWriter, htmlWriter, xmlWriter, framesetNavWriter;
    protected final String indent = "    ";
    protected int count, level;
    protected Charset charset;

    @FXML
    protected ControlNotebookSelector notebooksController;
    @FXML
    protected CheckBox timeCheck, textsCheck, htmlCheck, xmlCheck, framesetCheck;
    @FXML
    protected ComboBox<String> charsetSelector;
    @FXML
    protected TextArea styleInput;

    public NotesExportController() {
        baseTitle = message("NotesExport");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            treeView = notebooksController.treeView;

            timeCheck.setSelected(UserConfig.getBoolean(baseName + "Time", false));
            timeCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) {
                    UserConfig.setBoolean(baseName + "Time", timeCheck.isSelected());
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
            startButton.disableProperty().bind(
                    targetPathController.valid.not()
                            .or(treeView.getSelectionModel().selectedItemProperty().isNull())
            );

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setParameters(NotesController notesController) {
        this.notesController = notesController;
        this.tableNotebook = notesController.tableNotebook;
        this.tableNote = notesController.tableNote;

        notebooksController.setCaller(notesController);
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
        selectedNode = notebooksController.treeView.getSelectionModel().getSelectedItem();
        if (selectedNode == null) {
            selectedNode = notebooksController.treeView.getRoot();
            if (selectedNode == null) {
                popError(message("NoData"));
                return false;
            }
        }
        TreeItem<Notebook> node = selectedNode;
        if (node.getValue() == null) {
            popError(message("NoData"));
            return false;
        }
        if (targetPath == null || !targetPath.exists()) {
            popError(message("InvalidParameters") + ": " + message("TargetPath"));
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
        try ( Connection conn = DerbyBase.getConnection()) {
            exportNotes(conn, selectedNode.getValue(),
                    notebooksController.chainName(selectedNode.getParent()));
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
            String nodeName = notebooksController.chainName(selectedNode);
            String prefix = nodeName.replaceAll(Notebook.NotebookNameSeparater, "-") + "_" + DateTools.nowFileString();
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
                            .append(charset.name()).append("\"?>\n").append("<notes>\n");
                    xmlWriter.write(s.toString());
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
                targetFileGenerated(framesetFile, VisitHistory.FileType.Html);
            } catch (Exception e) {
                updateLogs(e.toString());
                well = false;
            }
        }
        if (xmlWriter != null) {
            try {
                xmlWriter.write("</notes>\n");
                xmlWriter.flush();
                xmlWriter.close();
                targetFileGenerated(xmlFile, VisitHistory.FileType.Xml);
            } catch (Exception e) {
                updateLogs(e.toString());
                well = false;
            }
        }
        return well;
    }

    public void exportNotes(Connection conn, Notebook book, String baseName) {
        level++;
        if (conn == null || book == null) {
            return;
        }
        try {
            String bookName = book.getName() + "_" + book.getNbid();
            FileWriter bookWriter = null, bookNavWriter = null;
            File bookFile = null;
            if (framesetNavWriter != null) {
                File bookNavFile = new File(framesetNavFile.getParent() + File.separator + FileNameTools.filter(bookName) + "_nav.html");
                bookNavWriter = new FileWriter(bookNavFile, charset);
                writeHtmlHead(bookNavWriter, bookName);
                bookNavWriter.write(indent + "<BODY>\n");

                bookFile = new File(framesetNavFile.getParent() + File.separator + FileNameTools.filter(bookName) + ".html");
                bookWriter = new FileWriter(bookFile, charset);
                writeHtmlHead(bookWriter, bookName);
                bookWriter.write(indent + "<BODY>\n");

                String prefix = "";
                for (int i = 1; i < level; i++) {
                    prefix += "&nbsp;&nbsp;&nbsp;&nbsp;";
                }
                framesetNavWriter.write(prefix + "<A href=\"" + bookNavFile.getName() + "\"  target=booknav>" + book.getName() + "</A><BR>\n");
            }
            if (baseName != null) {
                bookName = baseName + NotebookNameSeparater + book.getName();
            } else {
                bookName = book.getName();
            }
            List<Note> notes = tableNote.notes(conn, book.getNbid());
            if (notes != null) {
                for (Note note : notes) {
                    count++;
                    note.setHtml(StringTools.discardBlankLines(note.getHtml()));
                    if (textsWriter != null) {
                        writeTexts(conn, bookName, note);
                    }
                    if (htmlWriter != null) {
                        writeHtml(conn, bookName, note, htmlWriter);
                    }
                    if (xmlWriter != null) {
                        writeXml(conn, bookName, note);
                    }
                    if (bookNavWriter != null && bookFile != null) {
                        bookNavWriter.write("<A href=\"" + bookFile.getName()
                                + "#" + note.getNtid() + "\"  target=main>" + note.getTitle() + "</A><BR>\n");
                    }
                    if (bookWriter != null) {
                        writeHtml(conn, bookName, note, bookWriter);
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
            List<Notebook> children = tableNotebook.children(conn, book.getNbid());
            if (children != null) {
                for (Notebook child : children) {
                    exportNotes(conn, child, bookName);
                }
            }
        } catch (Exception e) {
            updateLogs(e.toString());
        }
        level--;
    }

    protected void writeTexts(Connection conn, String bookName, Note note) {
        try {
            textsWriter.write(AppValues.MyBoxSeparator + "\n");
            textsWriter.write(bookName + "\n");
            textsWriter.write(note.getTitle() + "\n");
            if (timeCheck.isSelected()) {
                textsWriter.write(Note.NoteTimePrefix + DateTools.datetimeToString(note.getUpdateTime()) + "\n");
            }
            textsWriter.write(note.getHtml() + "\n");
        } catch (Exception e) {
            updateLogs(e.toString());
        }
    }

    protected void writeHtml(Connection conn, String bookName, Note note, FileWriter writer) {
        try {
            writer.write(indent + indent + "<div id=\"" + note.getNtid() + "\">\n"
                    + indent + indent + indent + "<H3><PRE><CODE>" + bookName + "</CODE></PRE></H3>\n");
            writer.write(indent + indent + indent + "<H4><PRE><CODE>" + note.getTitle() + "</CODE></PRE></H4>\n");
            if (timeCheck.isSelected()) {
                writer.write(indent + indent + indent + "<H5>" + DateTools.datetimeToString(note.getUpdateTime()) + "</H5>\n");
            }
            writer.write(indent + indent + indent + note.getHtml() + "\n"
                    + indent + indent + "</div><HR>\n\n");
        } catch (Exception e) {
            updateLogs(e.toString());
        }
    }

    protected void writeXml(Connection conn, String bookName, Note note) {
        try {
            xmlWriter.write(indent + indent + "<note>\n"
                    + indent + indent + indent + "<notebook><![CDATA[" + bookName + "]]></notebook>\n");
            xmlWriter.write(indent + indent + indent + "<title><![CDATA[" + note.getTitle() + "]]></title>\n");
            if (timeCheck.isSelected()) {
                xmlWriter.write(indent + indent + indent + "<time>" + DateTools.datetimeToString(note.getUpdateTime()) + "</time>\n");
            }
            xmlWriter.write(indent + indent + indent + "<code>\n"
                    + "<![CDATA[" + note.getHtml() + "]]>\n"
                    + indent + indent + indent + "</code>\n"
                    + indent + indent + "</note>\n\n");
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
        if (textsFile != null && textsFile.exists()) {
            TextEditorController controller = (TextEditorController) openStage(Fxmls.TextEditorFxml);
            controller.sourceFileChanged(textsFile);
        }
        popInformation(message("Count") + ": " + count);
    }

}
