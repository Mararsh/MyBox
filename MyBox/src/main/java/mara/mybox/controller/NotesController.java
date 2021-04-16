package mara.mybox.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Modality;
import javafx.stage.Stage;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.Note;
import mara.mybox.db.data.NoteTag;
import mara.mybox.db.data.Notebook;
import static mara.mybox.db.data.Notebook.NotebooksSeparater;
import mara.mybox.db.data.Tag;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.table.TableNote;
import mara.mybox.db.table.TableNoteTag;
import mara.mybox.db.table.TableNotebook;
import mara.mybox.db.table.TableTag;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.fxml.TableDateCell;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.HtmlTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2021-2-28
 * @License Apache License Version 2.0
 */
public class NotesController extends BaseDataTableController<Note> {

    protected TableNotebook tableNotebook;
    protected TableNote tableNote;
    protected TableTag tableTag;
    protected TableNoteTag tableNoteTag;
    protected Note currentNote;
    protected Notebook selectedBook, bookOfCurrentNote;
    protected String queryConditions, queryLabel;

    @FXML
    protected ControlNotebookSelector notebooksController;
    @FXML
    protected ListView<Tag> tagsList, noteTagsList;
    @FXML
    protected ControlTimeTree timeController;
    @FXML
    protected TableColumn<Note, Long> ntidColumn;
    @FXML
    protected TableColumn<Note, String> titleColumn;
    @FXML
    protected TableColumn<Note, Date> timeColumn;
    @FXML
    protected TextField idInput, titleInput, timeInput;
    @FXML
    protected Button deleteBookButton, addBookButton, moveDataBookButton, copyBookButton, renameBookButton,
            refreshNotesButton, clearNotesButton, deleteNotesButton, moveDataNotesButton, copyNotesButton, addBookNoteButton,
            addNoteButton, queryTagsButton, deleteTagsButton, renameTagButton, okNoteTagsButton,
            refreshTimesButton, queryTimesButton, refreshTagsButton;
    @FXML
    protected VBox notesConditionBox;
    @FXML
    protected CheckBox subCheck;
    @FXML
    protected FlowPane namesPane, tagsPane, timesPane, noteTagsPane;
    @FXML
    protected Label conditionLabel;
    @FXML
    protected RadioButton titleRadio, contentsRadio;
    @FXML
    protected ControlStringSelector searchController;
    @FXML
    protected TabPane notePane, noteEditPane;
    @FXML
    protected Tab noteViewTab, noteCodesTab, noteEditorTab, noteStyleTab, noteTab, tagsTab;
    @FXML
    protected HTMLEditor htmlEditor;
    @FXML
    protected ControlWebview webviewController;
    @FXML
    protected ControlHtmlCodes codesController;
    @FXML
    protected Label notebookLabel;
    @FXML
    protected TextArea styleInput;

    public NotesController() {
        baseTitle = AppVariables.message("Notes");
        TipsLabelKey = "NotesComments";
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Text);
    }

    @Override
    public void setTableDefinition() {
        tableNotebook = new TableNotebook();
        tableNote = new TableNote();
        tableTag = new TableTag();
        tableNoteTag = new TableNoteTag();
        tableDefinition = tableNote;
        selectedBook = null;
        currentNote = null;
    }

    @Override
    protected void initColumns() {
        try {
            ntidColumn.setCellValueFactory(new PropertyValueFactory<>("ntid"));
            titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
            timeColumn.setCellValueFactory(new PropertyValueFactory<>("updateTime"));
            timeColumn.setCellFactory(new TableDateCell());

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initControls() {
        try {
            notebooksController.setParentController(this);
            notebooksController.setValues(this);
            webviewController.setValues(this, true, true);
            codesController.setValues(this);

            super.initControls();

            moveDataBookButton.setDisable(true);
            copyBookButton.setDisable(true);
            renameBookButton.setDisable(true);
            notebooksController.treeView.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (popMenu != null && popMenu.isShowing()) {
                        popMenu.hide();
                    }
                    TreeItem<Notebook> node = notebooksController.treeView.getSelectionModel().getSelectedItem();
                    if (event.getButton() == MouseButton.SECONDARY) {
                        popBookMenu(event, node);
                    } else if (node != null) {
                        loadBook(node.getValue());
                    }
                    boolean isRoot = node == null || node.getValue().isRoot();
                    moveDataBookButton.setDisable(isRoot);
                    copyBookButton.setDisable(isRoot);
                    renameBookButton.setDisable(isRoot);
                }
            });
            selectedBook = notebooksController.rootBook;

            searchController.init(this, baseName + "Saved", message("Note"), 20);

            noteEditPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
                @Override
                public void changed(ObservableValue ov, Tab oldTab, Tab newTab) {
                    String style = AppVariables.getUserConfigValue(baseName + "Style", null);
                    if (style != null && !style.isBlank()) {
                        style = "<style type=\"text/css\">\n" + style + "\n</style>";
                    } else {
                        style = "";
                    }
                    if (oldTab == noteCodesTab || oldTab == noteStyleTab) {
                        String html = style + HtmlTools.body(codesController.codes(), false);
                        webviewController.loadContents(html);
                        htmlEditor.setHtmlText(html);
                    } else if (oldTab == noteEditorTab) {
                        String html = HtmlTools.body(htmlEditor.getHtmlText(), false);
                        webviewController.loadContents(style + html);
                        codesController.load(html);
                    }
                }
            });

            titleInput.setText(message("Note"));
            styleInput.setText(AppVariables.getUserConfigValue(baseName + "Style", HtmlTools.DefaultStyle));

            tagsList.setCellFactory(p -> new ListCell<Tag>() {
                @Override
                public void updateItem(Tag item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(null);
                    if (empty || item == null) {
                        setText(null);
                        return;
                    }
                    setText(item.getTag());
                }
            });
            tagsList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            tagsList.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (popMenu != null && popMenu.isShowing()) {
                        popMenu.hide();
                    }
                    Tag selected = tagsList.getSelectionModel().getSelectedItem();
                    queryTagsButton.setDisable(selected == null);
                    deleteTagsButton.setDisable(selected == null);
                    renameTagButton.setDisable(selected == null);
                    if (event.getButton() == MouseButton.SECONDARY) {
                        popTagMenu(event, selected);
                    } else if (event.getClickCount() > 1) {
                        queryTags();
                    }
                }
            });

            noteTagsList.setCellFactory(p -> new ListCell<Tag>() {
                @Override
                public void updateItem(Tag item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(null);
                    if (empty || item == null) {
                        setText(null);
                        return;
                    }
                    setText(item.getTag());
                }
            });
            noteTagsList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

            subCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "IncludeSub", false));
            subCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldTab, Boolean newTab) {
                    if (selectedBook != null) {
                        loadTableData();
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void afterSceneLoaded() {
        try {
            super.afterSceneLoaded();

            FxmlControl.setTooltip(deleteBookButton, new Tooltip(message("DeleteBook")));
            FxmlControl.setTooltip(copyBookButton, new Tooltip(message("CopyBookAndContents")));
            FxmlControl.setTooltip(addBookButton, new Tooltip(message("AddBook")));
            FxmlControl.setTooltip(renameBookButton, new Tooltip(message("RenameBook")));
            FxmlControl.setTooltip(moveDataBookButton, new Tooltip(message("MoveBook")));
            FxmlControl.setTooltip(clearNotesButton, new Tooltip(message("ClearNotes")));
            FxmlControl.setTooltip(deleteNotesButton, new Tooltip(message("DeleteNotes")));
            FxmlControl.setTooltip(moveDataNotesButton, new Tooltip(message("MoveNotes")));
            FxmlControl.setTooltip(copyNotesButton, new Tooltip(message("CopyNotes")));
            FxmlControl.removeTooltip(importButton);

            if (tableNotebook.size() < 2
                    && FxmlControl.askSure(getBaseTitle(), message("ImportNotesExample"))) {
                importExamples();
            } else {
                refreshBooks();
            }
            refreshTags();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected void clearQuery() {
        selectedBook = null;
        queryConditions = null;
        queryLabel = null;
        tableData.clear();
        notesConditionBox.getChildren().clear();
        namesPane.getChildren().clear();
        currentPageStart = 1;
    }

    /*
        Notebooks
     */
    protected void popBookMenu(MouseEvent event, TreeItem<Notebook> selected) {
        if (isSettingValues) {
            return;
        }
        TreeItem<Notebook> node = selected == null ? notebooksController.treeView.getRoot() : selected;

        List<MenuItem> items = new ArrayList<>();
        MenuItem menu = new MenuItem(ControlNotebookSelector.nodeName(node));
        menu.setStyle("-fx-text-fill: #2e598a;");
        items.add(menu);
        items.add(new SeparatorMenuItem());

        menu = new MenuItem(message("AddBook"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            notebooksController.addBook();
        });
        items.add(menu);

        menu = new MenuItem(message("DeleteBook"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            deleteBook();
        });
        items.add(menu);

        menu = new MenuItem(message("MoveBook"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            moveBook();
        });
        menu.setDisable(node.getValue().isRoot());
        items.add(menu);

        menu = new MenuItem(message("CopyBookAndContents"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            copyBook(false);
        });
        menu.setDisable(node.getValue().isRoot());
        items.add(menu);

        menu = new MenuItem(message("CopyBookContents"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            copyBook(true);
        });
        menu.setDisable(node.getValue().isRoot());
        items.add(menu);

        menu = new MenuItem(message("RenameBook"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            renameBook();
        });
        menu.setDisable(node.getValue().isRoot());
        items.add(menu);

        items.add(new SeparatorMenuItem());

        menu = new MenuItem(message("Unfold"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            unfoldBooks();
        });
        items.add(menu);

        menu = new MenuItem(message("Fold"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            foldBooks();
        });
        items.add(menu);

        menu = new MenuItem(message("Refresh"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            refreshBooks();
        });
        items.add(menu);

        items.add(new SeparatorMenuItem());

        menu = new MenuItem(message("Export"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            exportBook();
        });
        items.add(menu);

        items.add(new SeparatorMenuItem());

        menu = new MenuItem(message("PopupClose"));
        menu.setStyle("-fx-text-fill: #2e598a;");
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = null;
        });
        items.add(menu);

        if (popMenu != null && popMenu.isShowing()) {
            popMenu.hide();
        }
        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        popMenu.getItems().addAll(items);
        popMenu.show(tableView, event.getScreenX(), event.getScreenY());

    }

    protected void refreshBooks() {
        notebooksController.loadTree();
        refreshTimes();
    }

    @FXML
    protected void deleteBook() {
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            TreeItem<Notebook> selectedNode = notebooksController.treeView.getSelectionModel().getSelectedItem();
            if (selectedNode == null) {
                selectedNode = notebooksController.treeView.getRoot();
                if (selectedNode == null) {
                    return;
                }
            }
            TreeItem<Notebook> node = selectedNode;
            Notebook book = node.getValue();
            if (book == null) {
                return;
            }
            if (book.isRoot()) {
                if (!FxmlControl.askSure(getBaseTitle(), message("DeleteBook"), message("SureDeleteAll"))) {
                    return;
                }
            } else {
                String chainName = ControlNotebookSelector.nodeName(node);
                if (!FxmlControl.askSure(getBaseTitle(), chainName, message("DeleteBook"))) {
                    return;
                }
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    try ( Connection conn = DerbyBase.getConnection()) {
                        if (book.isRoot()) {
                            notebooksController.rootBook = tableNotebook.clear(conn);
                        } else {
                            tableNotebook.deleteData(conn, book);
                        }
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    if (book.isRoot()) {
                        notebooksController.treeView.getRoot().getChildren().clear();
                        loadBook(null);
                        editNote(null);
                    } else {
                        node.getChildren().clear();
                        if (node.getParent() != null) {
                            node.getParent().getChildren().remove(node);
                        }
                        if (selectedBook != null && book.getNbid() == selectedBook.getNbid()) {
                            loadBook(null);
                        }
                        if (bookOfCurrentNote != null && book.getNbid() == bookOfCurrentNote.getNbid()) {
                            editNote(null);
                        }
                    }
                    refreshTimes();
                    popSuccessful();
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    protected void renameBook() {
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            TreeItem<Notebook> selectedNode = notebooksController.treeView.getSelectionModel().getSelectedItem();
            if (selectedNode == null) {
                return;
            }
            TreeItem<Notebook> node = selectedNode;
            Notebook book = node.getValue();
            if (book == null || book.isRoot()) {
                return;
            }
            String chainName = ControlNotebookSelector.nodeName(node);
            String name = FxmlControl.askValue(getBaseTitle(), chainName, message("RenameBook"), book.getName() + "m");
            if (name == null || name.isBlank()) {
                return;
            }
            if (name.contains(NotebooksSeparater)) {
                popError(message("NotebookNameNotInclude") + " \"" + NotebooksSeparater + "\"");
                return;
            }
            task = new SingletonTask<Void>() {
                private Notebook updatedBook;

                @Override
                protected boolean handle() {
                    book.setName(name);
                    updatedBook = tableNotebook.updateData(book);
                    return updatedBook != null;
                }

                @Override
                protected void whenSucceeded() {
                    notebooksController.treeView.refresh();
                    bookChanged(updatedBook);
                    popSuccessful();
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    protected void moveBook() {
        TreeItem<Notebook> selectedNode = notebooksController.treeView.getSelectionModel().getSelectedItem();
        if (selectedNode == null || selectedNode.getValue().isRoot()) {
            return;
        }
        String chainName = ControlNotebookSelector.nodeName(selectedNode);
        NotesMoveNotebookController controller = (NotesMoveNotebookController) FxmlStage.openStage(CommonValues.NotesMoveNotebookFxml);
        controller.setValues(this, selectedNode.getValue(), chainName);
    }

    @FXML
    protected void copyBook() {
        copyBook(false);
    }

    protected void copyBook(Boolean onlyContents) {
        TreeItem<Notebook> selectedNode = notebooksController.treeView.getSelectionModel().getSelectedItem();
        if (selectedNode == null || selectedNode.getValue().isRoot()) {
            return;
        }
        String chainName = ControlNotebookSelector.nodeName(selectedNode);
        NotesCopyNotebookController controller = (NotesCopyNotebookController) FxmlStage.openStage(CommonValues.NotesCopyNotebookFxml);
        controller.setValues(this, selectedNode.getValue(), chainName, onlyContents);
    }

    @FXML
    protected void exportBook() {
        NotesExportController exportController = (NotesExportController) FxmlStage.openStage(CommonValues.NotesExportFxml);
        exportController.setValues(this);
    }

    @FXML
    protected void popImportMenu(MouseEvent mouseEvent) {
        try {
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu = new MenuItem(message("ImportNotesFile"));
            menu.setOnAction((ActionEvent event) -> {
                NotesImportController c = (NotesImportController) FxmlStage.openStage(CommonValues.NotesImportFxml);
                c.notesController = this;
            });
            items.add(menu);

            menu = new MenuItem(message("ImportNotesExample"));
            menu.setOnAction((ActionEvent event) -> {
                importExamples();
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());
            menu = new MenuItem(message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                if (popMenu != null && popMenu.isShowing()) {
                    popMenu.hide();
                }
                popMenu = null;
            });
            items.add(menu);

            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);
            popMenu.getItems().addAll(items);
            popMenu.show(tableView, mouseEvent.getScreenX(), mouseEvent.getScreenY());

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void importExamples() {
        NotesImportController controller = (NotesImportController) FxmlStage.openStage(CommonValues.NotesImportFxml);
        controller.importExamples(this);
    }

    @FXML
    protected void foldBooks() {
        TreeItem<Notebook> selectedNode = notebooksController.treeView.getSelectionModel().getSelectedItem();
        if (selectedNode == null) {
            selectedNode = notebooksController.treeView.getRoot();
        }
        notebooksController.fold(selectedNode);
    }

    @FXML
    protected void unfoldBooks() {
        TreeItem<Notebook> selectedNode = notebooksController.treeView.getSelectionModel().getSelectedItem();
        if (selectedNode == null) {
            selectedNode = notebooksController.treeView.getRoot();
        }
        notebooksController.expandChildren(selectedNode);
    }

    protected void loadBook(Notebook book) {
        clearQuery();
        selectedBook = book;
        if (book != null) {
            queryConditions = " notebook=" + book.getNbid();
            loadTableData();
        }
    }

    protected void bookChanged(Notebook book) {
        if (book == null) {
            return;
        }
        if (bookOfCurrentNote != null && book.getNbid() == bookOfCurrentNote.getNbid()) {
            bookOfCurrentNote = book;
            updateBookOfCurrentNote();
        }
        if (selectedBook != null && selectedBook.getNbid() == book.getNbid()) {
            selectedBook = book;
            makeConditionPane();
        }
    }

    /*
        Tags
     */
    protected void popTagMenu(MouseEvent event, Tag tag) {
        if (isSettingValues) {
            return;
        }
        List<MenuItem> items = new ArrayList<>();
        MenuItem menu;

        if (tag != null) {
            menu = new MenuItem(tag.getTag());
            menu.setStyle("-fx-text-fill: #2e598a;");
            items.add(menu);
            items.add(new SeparatorMenuItem());
        }

        menu = new MenuItem(message("Add"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            addTag();
        });
        items.add(menu);

        menu = new MenuItem(message("Delete"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            deleteTags();
        });
        menu.setDisable(tag == null);
        items.add(menu);

        menu = new MenuItem(message("Rename"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            renameTag();
        });
        menu.setDisable(tag == null);
        items.add(menu);

        items.add(new SeparatorMenuItem());

        menu = new MenuItem(message("Query"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            queryTags();
        });
        menu.setDisable(tag == null);
        items.add(menu);

        menu = new MenuItem(message("Refresh"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            refreshBooks();
        });
        items.add(menu);

        items.add(new SeparatorMenuItem());
        menu = new MenuItem(message("PopupClose"));
        menu.setStyle("-fx-text-fill: #2e598a;");
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = null;
        });
        items.add(menu);

        if (popMenu != null && popMenu.isShowing()) {
            popMenu.hide();
        }
        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        popMenu.getItems().addAll(items);
        popMenu.show(tableView, event.getScreenX(), event.getScreenY());

    }

    @FXML
    public void refreshTags() {
        synchronized (this) {
            tagsList.getItems().clear();
            tagsPane.setDisable(true);
            SingletonTask tagsTask = new SingletonTask<Void>() {
                private List<Tag> tags;

                @Override
                protected boolean handle() {
                    tags = tableTag.readAll();
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    tagsList.getItems().setAll(tags);
                }

                @Override
                protected void finalAction() {
                    tagsPane.setDisable(false);
                }

            };
            tagsTask.setSelf(tagsTask);
            Thread thread = new Thread(tagsTask);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    public void addTag() {
        addTag(false);
    }

    @FXML
    public void selectAllTags() {
        tagsList.getSelectionModel().selectAll();
    }

    @FXML
    public void selectNoneTags() {
        tagsList.getSelectionModel().clearSelection();
    }

    @FXML
    protected void renameTag() {
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            int index = tagsList.getSelectionModel().getSelectedIndex();
            if (index < 0) {
                return;
            }
            Tag tag = tagsList.getItems().get(index);
            String name = FxmlControl.askValue(getBaseTitle(), tag.getTag(), message("Rename"), tag.getTag() + "m");
            if (name == null || name.isBlank()) {
                return;
            }
            task = new SingletonTask<Void>() {
                private Tag updated;

                @Override
                protected boolean handle() {
                    updated = new Tag(name).setTgid(tag.getTgid());
                    updated = tableTag.updateData(updated);
                    return updated != null;
                }

                @Override
                protected void whenSucceeded() {
                    tagsList.getItems().set(index, updated);
                    refreshNoteTags();
                    popSuccessful();
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    public void queryTags() {
        List<Tag> selected = tagsList.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            popError(message("TagsQueryComments"));
            return;
        }
        clearQuery();
        queryConditions = TableNote.tagsCondition(selected);
        queryLabel = message("Tag") + ":";
        for (Tag tag : selected) {
            queryLabel += " " + tag.getTag();
        }
        loadTableData();
    }

    @FXML
    public void clearTags() {
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            if (!FxmlControl.askSure(getBaseTitle(), message("Tags"), message("SureDeleteAll"))) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    return tableTag.clearData() >= 0;
                }

                @Override
                protected void whenSucceeded() {
                    tagsList.getItems().clear();
                    noteTagsList.getItems().clear();
                    popSuccessful();
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    public void deleteTags() {
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            List<Tag> selected = tagsList.getSelectionModel().getSelectedItems();
            if (selected == null) {
                return;
            }
            if (!FxmlControl.askSure(getBaseTitle(), message("Tags"), message("SureDelete"))) {
                return;
            }
            List<Tag> tags = new ArrayList<>();
            tags.addAll(selected);
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    return tableTag.deleteData(tags) >= 0;
                }

                @Override
                protected void whenSucceeded() {
                    tagsList.getItems().removeAll(tags);
                    refreshNoteTags();
                    popSuccessful();
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    /*
        Times
     */
    @FXML
    protected void refreshTimes() {
        synchronized (this) {
            timeController.clearTree();
            timesPane.setDisable(true);
            SingletonTask timesTask = new SingletonTask<Void>() {
                private List<Date> times;

                @Override
                protected boolean handle() {
                    times = TableNote.times();
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    timeController.loadTree("update_time", times, false);
                }

                @Override
                protected void finalAction() {
                    timesPane.setDisable(false);
                }

            };
            timesTask.setSelf(timesTask);
            Thread thread = new Thread(timesTask);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    protected void queryTimes() {
        String c = timeController.check();
        if (c == null) {
            popError(message("MissTime"));
            return;
        }
        clearQuery();
        queryConditions = c;
        queryLabel = timeController.getFinalTitle();
        loadTableData();
    }

    /*
        Search
     */
    @FXML
    protected void search() {
        String s = searchController.value();
        if (s == null || s.isBlank()) {
            popError(message("InvalidData"));
            return;
        }
        String[] values = StringTools.splitBySpace(s);
        if (values == null || values.length == 0) {
            popError(message("InvalidData"));
            return;
        }
        searchController.refreshList();
        clearQuery();
        if (titleRadio.isSelected()) {
            queryConditions = null;
            queryLabel = message("Title") + ":";
            for (String v : values) {
                if (queryConditions != null) {
                    queryConditions += " OR ";
                } else {
                    queryConditions = " ";
                }
                queryConditions += " ( title like '%" + DerbyBase.stringValue(v) + "%' ) ";
                queryLabel += " " + v;
            }

        } else {
            queryConditions = null;
            queryLabel = message("Contents") + ":";
            for (String v : values) {
                if (queryConditions != null) {
                    queryConditions += " OR ";
                } else {
                    queryConditions = " ";
                }
                queryConditions += " ( html like '%" + DerbyBase.stringValue(v) + "%' ) ";
                queryLabel += " " + v;
            }
        }
        loadTableData();
    }

    /*
        Notes list
     */
    @Override
    public void postLoadedTableData() {
        makeConditionPane();
    }

    public void makeConditionPane() {
        notesConditionBox.getChildren().clear();
        if (selectedBook == null) {
            if (queryLabel != null) {
                conditionLabel.setText(queryLabel);
                notesConditionBox.getChildren().add(conditionLabel);
            }
            notesConditionBox.applyCss();
            return;
        }
        synchronized (this) {
            SingletonTask bookTask = new SingletonTask<Void>() {
                private List<Notebook> ancestor;

                @Override
                protected boolean handle() {
                    ancestor = tableNotebook.ancestor(selectedBook.getNbid());
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    List<Node> nodes = new ArrayList<>();
                    if (ancestor != null) {
                        for (Notebook book : ancestor) {
                            Hyperlink link = new Hyperlink(book.getName());
                            link.setWrapText(true);
                            link.setMinHeight(Region.USE_PREF_SIZE);
                            link.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    loadBook(book);
                                }
                            });
                            nodes.add(link);
                            nodes.add(new Label(">"));
                        }
                    }
                    Label label = new Label(selectedBook.getName());
                    label.setWrapText(true);
                    label.setMinHeight(Region.USE_PREF_SIZE);
                    nodes.add(label);
                    namesPane.getChildren().setAll(nodes);
                    notesConditionBox.getChildren().addAll(namesPane, subCheck);
                    notesConditionBox.applyCss();
                }
            };
            bookTask.setSelf(bookTask);
            Thread thread = new Thread(bookTask);
            thread.setDaemon(true);
            thread.start();
        }

    }

    @Override
    public int readDataSize() {
        if (selectedBook != null && subCheck.isSelected()) {
            return tableNote.withSubSize(tableNotebook, selectedBook.getNbid());

        } else if (queryConditions != null) {
            return TableNote.conditionSize(queryConditions);

        } else {
            return 0;
        }
    }

    @Override
    public List<Note> readPageData() {
        if (selectedBook != null && subCheck.isSelected()) {
            return tableNote.withSub(tableNotebook, selectedBook.getNbid(), currentPageStart - 1, currentPageSize);

        } else if (queryConditions != null) {
            return tableNote.queryConditions(queryConditions, currentPageStart - 1, currentPageSize);

        } else {
            return null;
        }
    }

    @Override
    protected int clearData() {
        if (queryConditions != null) {
            return tableNote.deleteCondition(queryConditions);

        } else {
            return -1;
        }
    }

    @Override
    protected List<MenuItem> makeTableContextMenu() {
        try {
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;

            menu = new MenuItem(message("DeleteNotes"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                deleteNotes();
            });
            menu.setDisable(deleteNotesButton.isDisabled());
            items.add(menu);

            menu = new MenuItem(message("CopyNotes"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                copyNotes();
            });
            menu.setDisable(deleteNotesButton.isDisabled());
            items.add(menu);

            menu = new MenuItem(message("MoveNotes"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                moveNotes();
            });
            menu.setDisable(deleteNotesButton.isDisabled());
            items.add(menu);

            menu = new MenuItem(message("AddNote"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                addBookNote();
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());

            menu = new MenuItem(message("ClearNotes"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                clearNotes();
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());

            if (pageNextButton != null && pageNextButton.isVisible() && !pageNextButton.isDisabled()) {
                menu = new MenuItem(message("NextPage"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    pageNextAction();
                });
                items.add(menu);
            }

            if (pagePreviousButton != null && pagePreviousButton.isVisible() && !pagePreviousButton.isDisabled()) {
                menu = new MenuItem(message("PreviousPage"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    pagePreviousAction();
                });
                items.add(menu);
            }

            menu = new MenuItem(message("Refresh"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                refreshAction();
            });
            items.add(menu);

            return items;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    @Override
    public void itemClicked() {
        editAction(null);
    }

    @Override
    protected void checkSelected() {
        if (isSettingValues) {
            return;
        }
        int selection = tableView.getSelectionModel().getSelectedIndices().size();
        deleteNotesButton.setDisable(selection == 0);
        copyNotesButton.setDisable(selection == 0);
        moveDataNotesButton.setDisable(selection == 0);
        selectedLabel.setText(message("Selected") + ": " + selection);
    }

    @FXML
    protected void refreshNotes() {
        refreshAction();
    }

    @FXML
    protected void addBookNote() {
        bookOfCurrentNote = selectedBook;
        editNote(null);
    }

    @FXML
    protected void copyNotes() {
        NotesCopyNotesController.oneOpen(this);
    }

    @FXML
    protected void moveNotes() {
        NotesMoveNotesController.oneOpen(this);
    }

    @FXML
    protected void clearNotes() {
        clearAction();
        refreshTimes();
    }

    @FXML
    protected void deleteNotes() {
        deleteAction();
        refreshTimes();
    }

    @Override
    protected int deleteData(List<Note> data) {
        int ret = super.deleteData(data);
        if (ret <= 0) {
            return ret;
        }
        if (currentNote == null || data == null || data.isEmpty()) {
            return 0;
        }
        for (Note note : data) {
            if (note.getNtid() == currentNote.getNtid()) {
                Platform.runLater(() -> {
                    editNote(null);
                });
                break;
            }
        }
        return ret;
    }

    @FXML
    @Override
    public void editAction(ActionEvent event) {
        Note note = tableView.getSelectionModel().getSelectedItem();
        if (note != null) {
            editNote(note);
        }
    }

    /*
        edit note
     */
    @FXML
    protected void addNote() {
        editNote(null);
    }

    protected void editNote(Note note) {
        currentNote = note;
        loadNote();
    }

    protected void loadNote() {
        synchronized (this) {
            if (currentNote != null) {
                idInput.setText(currentNote.getNtid() + "");
                titleInput.setText(currentNote.getTitle());
                timeInput.setText(DateTools.datetimeToString(currentNote.getUpdateTime()));
                String html = currentNote.getHtml();
                String style = AppVariables.getUserConfigValue(baseName + "Style", HtmlTools.DefaultStyle);
                if (style != null && !style.isBlank()) {
                    style = "<!DOCTYPE html><html><head><style type=\"text/css\">\n" + style + "\n</style></head></html>";
                } else {
                    style = "";
                }
                isSettingValues = true;
                webviewController.loadContents(style + html);
                codesController.load(html);
                htmlEditor.setHtmlText(style + html);
                isSettingValues = false;
            } else {
                idInput.setText("");
                timeInput.setText("");
                titleInput.setText(message("Note"));
                webviewController.loadContents("");
                htmlEditor.setHtmlText("");
                codesController.load("");
            }
            updateBookOfCurrentNote();
            refreshNoteTags();
            showRightPane();
        }
    }

    protected void updateBookOfCurrentNote() {
        synchronized (this) {
            SingletonTask noteTask = new SingletonTask<Void>() {
                private List<Notebook> ancestor;

                @Override
                protected boolean handle() {
                    try ( Connection conn = DerbyBase.getConnection()) {
                        if (currentNote != null) {
                            if (bookOfCurrentNote == null || bookOfCurrentNote.getNbid() != currentNote.getNotebookid()) {
                                bookOfCurrentNote = tableNotebook.find(conn, currentNote.getNotebookid());
                            }
                        } else {
                            bookOfCurrentNote = null;
                        }
                        if (bookOfCurrentNote == null) {
                            bookOfCurrentNote = notebooksController.rootBook;
                        }
                        if (bookOfCurrentNote == null) {
                            return false;
                        }
                        ancestor = tableNotebook.ancestor(conn, bookOfCurrentNote.getNbid());
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    String chainName = "";
                    if (ancestor != null) {
                        for (Notebook book : ancestor) {
                            chainName += book.getName() + ">";
                        }
                    }
                    chainName += bookOfCurrentNote.getName();
                    notebookLabel.setText(chainName);
                }
            };
            noteTask.setSelf(noteTask);
            Thread thread = new Thread(noteTask);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    protected void copyNote() {
        idInput.setText("");
        timeInput.setText("");
        titleInput.setText(titleInput.getText() + " - " + message("Copy"));
        currentNote = null;
    }

    @FXML
    protected void recoverNote() {
        loadNote();
    }

    @FXML
    @Override
    public void saveAction() {
        synchronized (this) {
            String title = titleInput.getText();
            if (title == null || title.isBlank()) {
                popError(message("TitleNonEmpty"));
                return;
            }
            rightPane.setDisable(true);
            SingletonTask saveTask = new SingletonTask<Void>() {
                private Note note;

                @Override
                protected boolean handle() {
                    note = new Note();
                    note.setTitle(title);
                    String html;
                    if (noteEditPane.getSelectionModel().getSelectedItem() == noteEditorTab) {
                        html = HtmlTools.body(htmlEditor.getHtmlText(), false);
                    } else {
                        html = HtmlTools.body(codesController.codes(), false);
                    }
                    note.setHtml(StringTools.discardBlankLines(html));
                    note.setUpdateTime(new Date());
                    if (currentNote != null) {
                        note.setNtid(currentNote.getNtid());
                        note.setNotebook(currentNote.getNotebookid());
                        currentNote = tableNote.updateData(note);
                    } else {
                        if (bookOfCurrentNote == null) {
                            bookOfCurrentNote = notebooksController.rootBook;
                        }
                        note.setNotebook(bookOfCurrentNote.getNbid());
                        currentNote = tableNote.insertData(note);
                    }
                    return currentNote != null;
                }

                @Override
                protected void whenSucceeded() {
                    popSuccessful();
                    timeInput.setText(DateTools.datetimeToString(currentNote.getUpdateTime()));
                    updateBookOfCurrentNote();
                    if (selectedBook != null && selectedBook.getNbid() == bookOfCurrentNote.getNbid()) {
                        refreshNotes();
                    }
                    refreshTimes();
                }

                @Override
                protected void finalAction() {
                    rightPane.setDisable(false);
                }

            };
            saveTask.setSelf(saveTask);
            Thread thread = new Thread(saveTask);
            thread.setDaemon(true);
            thread.start();
        }
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
            for (HtmlTools.HtmlStyle style : HtmlTools.HtmlStyle.values()) {
                menu = new MenuItem(message(style.name()));
                menu.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        styleInput.setText(HtmlTools.styleValue(style));
                        AppVariables.setUserConfigValue(baseName + "Style", styleInput.getText());
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

            FxmlControl.locateCenter((Region) mouseEvent.getSource(), popMenu);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void setStyle() {
        AppVariables.setUserConfigValue(baseName + "Style", styleInput.getText());
    }

    @FXML
    public void clearStyle() {
        styleInput.clear();
    }

    /*
        Note tags
     */
    @FXML
    public void refreshNoteTags() {
        synchronized (this) {
            noteTagsList.getItems().clear();
            noteTagsPane.setDisable(true);
            SingletonTask noteTagsTask = new SingletonTask<Void>() {
                private List<Tag> noteTags = null;
                private int count = 0;

                @Override
                protected boolean handle() {
                    try ( Connection conn = DerbyBase.getConnection()) {
                        List<Tag> tags = tableTag.readAll(conn);
                        if (tags != null && !tags.isEmpty() && currentNote != null) {
                            List<Long> noteTagIDs = tableNoteTag.readTags(conn, currentNote.getNtid());
                            if (noteTagIDs != null && !noteTagIDs.isEmpty()) {
                                noteTags = new ArrayList<>();
                                for (Tag tag : tags) {
                                    if (noteTagIDs.contains(tag.getTgid())) {
                                        noteTags.add(count++, tag);
                                    } else {
                                        noteTags.add(tag);
                                    }
                                }
                            } else {
                                noteTags = tags;
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
                    if (noteTags != null) {
                        noteTagsList.getItems().setAll(noteTags);
                        noteTagsList.getSelectionModel().selectRange(0, count);
                    }
                }

                @Override
                protected void finalAction() {
                    noteTagsPane.setDisable(false);
                }

            };
            noteTagsTask.setSelf(noteTagsTask);
            Thread thread = new Thread(noteTagsTask);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    public void selectAllNoteTags() {
        noteTagsList.getSelectionModel().selectAll();
    }

    @FXML
    public void selectNoneNoteTags() {
        noteTagsList.getSelectionModel().clearSelection();
    }

    @FXML
    public void addNoteTag() {
        addTag(true);
    }

    public void addTag(boolean forCurrentNote) {
        synchronized (this) {
            String name = FxmlControl.askValue(getBaseTitle(), message("Add"), message("Tag"), message("Tag") + new Date().getTime());
            if (name == null || name.isBlank()) {
                return;
            }
            for (Tag tag : tagsList.getItems()) {
                if (name.equals(tag.getTag())) {
                    popError(message("AlreadyExisted"));
                    return;
                }
            }
            rightPane.setDisable(true);
            SingletonTask tagTask = new SingletonTask<Void>() {
                private Tag tag = null;
                private boolean updateCurrent;

                @Override
                protected boolean handle() {
                    try ( Connection conn = DerbyBase.getConnection()) {
                        tag = tableTag.insertData(conn, new Tag(name));
                        updateCurrent = forCurrentNote && tag != null && currentNote != null;
                        if (updateCurrent) {
                            tableNoteTag.insertData(conn, new NoteTag(currentNote.getNtid(), tag.getTgid()));
                        }
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    return tag != null;
                }

                @Override
                protected void whenSucceeded() {
                    tagsList.getItems().add(0, tag);
                    noteTagsList.getItems().add(0, tag);
                    if (updateCurrent) {
                        noteTagsList.getSelectionModel().selectFirst();
                    }
                    popSuccessful();
                }

                @Override
                protected void finalAction() {
                    rightPane.setDisable(false);
                }

            };
            tagTask.setSelf(tagTask);
            Thread thread = new Thread(tagTask);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    public void okNoteTags() {
        if (currentNote == null) {
            return;
        }
        synchronized (this) {
            rightPane.setDisable(true);
            SingletonTask noteTagsTask = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    List<Tag> selected = noteTagsList.getSelectionModel().getSelectedItems();
                    List<Long> tags = new ArrayList();
                    if (selected != null) {
                        for (Tag tag : selected) {
                            tags.add(tag.getTgid());
                        }
                    }
                    long noteid = currentNote.getNtid();
                    try ( Connection conn = DerbyBase.getConnection();
                             PreparedStatement statement = conn.prepareStatement(TableNoteTag.DeleteNoteTags)) {
                        statement.setLong(1, noteid);
                        statement.executeUpdate();
                        conn.setAutoCommit(false);
                        for (Long tagid : tags) {
                            tableNoteTag.insertData(conn, new NoteTag(noteid, tagid));
                        }
                        conn.commit();
                    } catch (Exception e) {
                        MyBoxLog.error(e);
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    popSuccessful();
                }

                @Override
                protected void finalAction() {
                    rightPane.setDisable(false);
                }

            };
            noteTagsTask.setSelf(noteTagsTask);
            Thread thread = new Thread(noteTagsTask);
            thread.setDaemon(true);
            thread.start();
        }
    }

    protected void initInterfaces() {
        try {
            getMyStage().toBack();
            openHandlingStage(Modality.WINDOW_MODAL, message("FirstRunInfo"));
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        closeStage();
                    });
                }
            }, 6000);
        } catch (Exception e) {
            closeStage();
        }
    }


    /*
        static methods
     */
    public static NotesController oneOpen() {
        NotesController controller = null;
        Stage stage = FxmlStage.findStage(message("Notes"));
        if (stage != null && stage.getUserData() != null) {
            try {
                controller = (NotesController) stage.getUserData();
            } catch (Exception e) {
            }
        }
        if (controller == null) {
            controller = (NotesController) FxmlStage.openStage(CommonValues.NotesFxml);
        }
        if (controller != null) {
            controller.getMyStage().toFront();
            controller.getMyStage().requestFocus();
        }
        return controller;
    }

    public static NotesController firstRun() {
        NotesController controller = oneOpen();
        if (controller != null) {
            controller.initInterfaces();
        }
        return controller;
    }

}
