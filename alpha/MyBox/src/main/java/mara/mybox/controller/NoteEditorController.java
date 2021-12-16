package mara.mybox.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.Note;
import mara.mybox.db.data.NoteTag;
import mara.mybox.db.data.Notebook;
import mara.mybox.db.data.Tag;
import mara.mybox.db.table.TableNote;
import mara.mybox.db.table.TableNoteTag;
import mara.mybox.db.table.TableNotebook;
import mara.mybox.db.table.TableTag;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WebViewTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.HtmlReadTools;
import mara.mybox.value.HtmlStyles;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-8-11
 * @License Apache License Version 2.0
 */
public class NoteEditorController extends HtmlEditorController {

    protected NotesController notesController;
    protected ControlNotebookSelector notebooksController;
    protected TableNotebook tableNotebook;
    protected TableNote tableNote;
    protected TableTag tableTag;
    protected TableNoteTag tableNoteTag;
    protected Note currentNote;
    protected Notebook bookOfCurrentNote;
    protected boolean tagsChanged;

    @FXML
    protected Label notebookLabel;
    @FXML
    protected TextField idInput, titleInput, timeInput;
    @FXML
    protected Tab attributesTab, tagsTab;
    @FXML
    protected FlowPane noteTagsPane;
    @FXML
    protected Button okNoteTagsButton, addNoteTagButton;
    @FXML
    protected ControlCheckBoxList tagsListController;

    public void setParameters(NotesController notesController) {
        try {
            this.notesController = notesController;
            this.baseName = notesController.baseName;
            notebooksController = notesController.notebooksController;
            tableNotebook = notesController.tableNotebook;
            tableNote = notesController.tableNote;
            tableTag = notesController.tableTag;
            tableNoteTag = notesController.tableNoteTag;
            saveButton = notesController.saveButton;
            currentNote = null;
            webViewController.defaultStyle = HtmlStyles.styleValue("Default");
            editorController.defaultStyle = HtmlStyles.styleValue("Default");

            initTabPane();
            initAttributesTab();
            initTagsTab();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void editNote(Note note) {
        if (!checkBeforeNextAction()) {
            return;
        }
        updateFileStatus(false);
        currentNote = note;
        addressChanged = true;
        loadNote();
    }

    protected void loadNote() {
        synchronized (this) {
            if (currentNote != null) {
                idInput.setText(currentNote.getNtid() + "");
                titleInput.setText(currentNote.getTitle());
                timeInput.setText(DateTools.datetimeToString(currentNote.getUpdateTime()));
                loadContents(currentNote.getHtml());
            } else {
                idInput.setText("");
                timeInput.setText("");
                titleInput.setText(message("Note"));
                loadContents("");
            }
            updateBookOfCurrentNote();
            refreshNoteTags();
        }
    }

    protected void updateBookOfCurrentNote() {
        synchronized (this) {
            SingletonTask noteTask = new SingletonTask<Void>(this) {
                private String chainName;

                @Override
                protected boolean handle() {
                    try ( Connection conn = DerbyBase.getConnection()) {
                        if (currentNote != null) {
                            if (bookOfCurrentNote == null || bookOfCurrentNote.getNbid() != currentNote.getNotebookid()) {
                                bookOfCurrentNote = tableNotebook.find(conn, currentNote.getNotebookid());
                            }
                        }
                        if (bookOfCurrentNote == null) {
                            bookOfCurrentNote = notebooksController.root(conn);
                        }
                        if (bookOfCurrentNote == null) {
                            return false;
                        }
                        chainName = notebooksController.chainName(conn, bookOfCurrentNote);
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    notebookLabel.setText(chainName);
                }
            };
            start(noteTask, false);
        }
    }

    @FXML
    protected void addNote() {
        editNote(null);
    }

    @FXML
    protected void copyNote() {
        if (!checkBeforeNextAction()) {
            return;
        }
        idInput.setText("");
        timeInput.setText("");
        titleInput.setText(titleInput.getText() + " - " + message("Copy"));
        currentNote = null;
        updateFileStatus(true);
    }

    @FXML
    protected void recoverNote() {
        loadNote();
    }

    /*
        attributes
     */
    public void initAttributesTab() {
        try {
            titleInput.setText(message("Note"));

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    /*
        html
     */
    @Override
    public String htmlCodes(String html) {
        return HtmlReadTools.body(html, false);
    }

    @Override
    public String htmlInWebview() {
        return HtmlReadTools.body(WebViewTools.getHtml(webEngine), false);
    }

    @Override
    public String htmlByEditor() {
        return HtmlReadTools.body(htmlEditor.getHtmlText(), false);
    }

    @FXML
    @Override
    public void saveAction() {
        synchronized (this) {
            title = titleInput.getText();
            if (title == null || title.isBlank()) {
                popError(message("TitleNonEmpty"));
                return;
            }
            String html = currentHtml(true);
            notesController.rightPane.setDisable(true);
            SingletonTask saveTask = new SingletonTask<Void>(this) {
                private Note note;
                private boolean notExist = false;

                @Override
                protected boolean handle() {
                    try ( Connection conn = DerbyBase.getConnection()) {
                        note = new Note();
                        note.setTitle(title);
                        note.setHtml(HtmlReadTools.body(html, false));
                        note.setUpdateTime(new Date());
                        if (currentNote != null) {
                            currentNote = tableNote.readData(conn, currentNote);
                            bookOfCurrentNote = tableNotebook.readData(conn, bookOfCurrentNote);
                            if (currentNote == null || bookOfCurrentNote == null) {
                                notExist = true;
                                currentNote = null;
                                return true;
                            } else {
                                note.setNtid(currentNote.getNtid());
                                note.setNotebook(currentNote.getNotebookid());
                                currentNote = tableNote.updateData(conn, note);
                            }
                        } else if (currentNote == null) {
                            if (bookOfCurrentNote == null) {
                                bookOfCurrentNote = notebooksController.root(conn);
                            }
                            note.setNtid(-1);
                            note.setNotebook(bookOfCurrentNote.getNbid());
                            currentNote = tableNote.insertData(conn, note);
                        }
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    return currentNote != null;
                }

                @Override
                protected void whenSucceeded() {
                    if (notExist) {
                        fileChanged = false;
                        copyNote();
                        popError(message("NotExist"));
                    } else {
                        popSaved();
                        idInput.setText(currentNote.getNtid() + "");
                        timeInput.setText(DateTools.datetimeToString(currentNote.getUpdateTime()));
                        updateBookOfCurrentNote();
                        if (notebooksController.selectedNode != null
                                && notebooksController.selectedNode.getNbid() == currentNote.getNotebookid()) {
                            notesController.refreshNotes();
                        }
                        notesController.refreshTimes();
                        updateFileStatus(false);
                    }
                }

                @Override
                protected void finalAction() {
                    notesController.rightPane.setDisable(false);
                }

            };
            start(saveTask, false);
        }
    }


    /*
        Note tags
     */
    public void initTagsTab() {
        try {
            tagsListController.setParent(notesController);

            tagsListController.checkedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldV, Boolean newV) {
                    tagsTab.setText(message("Tags") + " *");
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void refreshNoteTags() {
        synchronized (this) {
            tagsListController.clear();
            noteTagsPane.setDisable(true);
            SingletonTask noteTagsTask = new SingletonTask<Void>(this) {
                private List<String> tagsString;
                private List<Integer> selected;

                @Override
                protected boolean handle() {
                    tagsString = new ArrayList<>();
                    selected = new ArrayList<>();
                    try ( Connection conn = DerbyBase.getConnection()) {
                        List<Tag> tags = tableTag.readAll(conn);
                        if (tags != null && !tags.isEmpty()) {
                            for (Tag tag : tags) {
                                tagsString.add(tag.getTag());
                            }
                            if (currentNote != null) {
                                List<Long> noteTagIDs = tableNoteTag.readTags(conn, currentNote.getNtid());
                                if (noteTagIDs != null && !noteTagIDs.isEmpty()) {
                                    for (int i = 0; i < tags.size(); i++) {
                                        Tag tag = tags.get(i);
                                        if (noteTagIDs.contains(tag.getTgid())) {
                                            selected.add(i);
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    tagsListController.setValues(tagsString);
                    tagsListController.setCheckIndices(selected);
                    tagsChanged(false);
                }

                @Override
                protected void finalAction() {
                    noteTagsPane.setDisable(false);
                }

            };
            start(noteTagsTask, false, null);
        }
    }

    @FXML
    public void selectAllNoteTags() {
        tagsListController.checkAll();
        tagsChanged(true);
    }

    @FXML
    public void selectNoneNoteTags() {
        tagsListController.checkNone();
        tagsChanged(true);
    }

    @FXML
    public void addNoteTag() {
        notesController.addTag(true);
    }

    @FXML
    public void okNoteTags() {
        if (currentNote == null) {
            return;
        }
        synchronized (this) {
            notesController.rightPane.setDisable(true);
            SingletonTask noteTagsTask = new SingletonTask<Void>(this) {

                @Override
                protected boolean handle() {
                    List<String> selected = tagsListController.checkedValues();
                    long noteid = currentNote.getNtid();
                    try ( Connection conn = DerbyBase.getConnection();
                             PreparedStatement query = conn.prepareStatement(TableTag.QueryTag);
                             PreparedStatement delete = conn.prepareStatement(TableNoteTag.DeleteNoteTags)) {
                        delete.setLong(1, noteid);
                        delete.executeUpdate();
                        conn.commit();
                        if (selected != null) {
                            List<NoteTag> noteTags = new ArrayList<>();
                            for (String value : selected) {
                                Tag tag = tableTag.query(conn, query, value);
                                if (tag == null) {
                                    continue;
                                }
                                noteTags.add(new NoteTag(noteid, tag.getTgid()));
                            }
                            tableNoteTag.insertList(conn, noteTags);
                        }
                        conn.commit();
                    } catch (Exception e) {
                        MyBoxLog.error(e);
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    popSaved();
                    tagsChanged(false);
                }

                @Override
                protected void finalAction() {
                    notesController.rightPane.setDisable(false);
                }

            };
            start(noteTagsTask, false);
        }
    }

    public void tagsChanged(boolean changed) {
        tagsChanged = changed;
        tagsTab.setText(message("Tags") + (changed ? " *" : ""));
    }

    protected void bookChanged(Notebook book) {
        if (book == null) {
            return;
        }
        if (bookOfCurrentNote != null && book.getNbid() == bookOfCurrentNote.getNbid()) {
            bookOfCurrentNote = book;
            updateBookOfCurrentNote();
        }
    }

    @Override
    public boolean checkBeforeNextAction() {
        if (!fileChanged) {
            return true;
        } else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(getMyStage().getTitle());
            alert.setContentText(message("NoteChanged"));
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            ButtonType buttonSave = new ButtonType(message("Save"));
            ButtonType buttonNotSave = new ButtonType(message("NotSave"));
            ButtonType buttonCancel = new ButtonType(message("Cancel"));
            alert.getButtonTypes().setAll(buttonSave, buttonNotSave, buttonCancel);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            stage.toFront();

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == buttonSave) {
                saveAction();
                return false;
            } else {
                return result.get() == buttonNotSave;
            }
        }
    }

    /*
        panes
     */
    @Override
    public void showTabs() {
        try {
            super.showTabs();

            if (!UserConfig.getBoolean(baseName + "ShowAttributesTab", true)) {
                tabPane.getTabs().remove(attributesTab);
            }
            attributesTab.setOnCloseRequest(new EventHandler<Event>() {
                @Override
                public void handle(Event event) {
                    UserConfig.setBoolean(baseName + "ShowAttributesTab", false);
                }
            });

            if (!UserConfig.getBoolean(baseName + "ShowTagsTab", true)) {
                tabPane.getTabs().remove(tagsTab);
            }
            tagsTab.setOnCloseRequest(new EventHandler<Event>() {
                @Override
                public void handle(Event event) {
                    UserConfig.setBoolean(baseName + "ShowTagsTab", false);
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public List<MenuItem> makePanesMenu(MouseEvent mouseEvent) {
        List<MenuItem> items = super.makePanesMenu(mouseEvent);
        try {
            CheckMenuItem attributesMenu = new CheckMenuItem(message("Attributes"));
            attributesMenu.setSelected(tabPane.getTabs().contains(attributesTab));
            attributesMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean(baseName + "ShowAttributesTab", attributesMenu.isSelected());
                    if (attributesMenu.isSelected()) {
                        if (!tabPane.getTabs().contains(attributesTab)) {
                            tabPane.getTabs().add(tabPane.getTabs().size() - 1, attributesTab);
                        }
                    } else {
                        if (tabPane.getTabs().contains(attributesTab)) {
                            tabPane.getTabs().remove(attributesTab);
                        }
                    }
                    NodeStyleTools.refreshStyle(tabPane);
                }
            });
            items.add(0, attributesMenu);

            CheckMenuItem tagsMenu = new CheckMenuItem(message("Tags"));
            tagsMenu.setSelected(tabPane.getTabs().contains(tagsTab));
            tagsMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean(baseName + "ShowTagsTab", tagsMenu.isSelected());
                    if (tagsMenu.isSelected()) {
                        if (!tabPane.getTabs().contains(tagsTab)) {
                            tabPane.getTabs().add(tabPane.getTabs().size() - 1, tagsTab);
                        }
                    } else {
                        if (tabPane.getTabs().contains(tagsTab)) {
                            tabPane.getTabs().remove(tagsTab);
                        }
                    }
                    NodeStyleTools.refreshStyle(tabPane);
                }
            });
            items.add(tagsMenu);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
        return items;
    }

}
