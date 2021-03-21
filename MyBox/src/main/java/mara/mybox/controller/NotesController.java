package mara.mybox.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
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
    protected Notebook loadedBook, editingBook;

    @FXML
    protected ControlNotebookSelector notebooksController;
    @FXML
    protected ListView<Tag> tagsList, noteTagsList;
    @FXML
    protected TableColumn<Note, Long> ntidColumn;
    @FXML
    protected TableColumn<Note, String> titleColumn;
    @FXML
    protected TableColumn<Note, Date> timeColumn;
    @FXML
    protected TextField idInput, titleInput, timeInput;
    @FXML
    protected Button deleteBookButton, addBookButton, clearNotesButton, deleteNotesButton, moveDataButton,
            queryTagsButton, deleteTagsButton, renameTagButton, okNoteTagsButton;
    @FXML
    protected FlowPane chainPane;
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
        loadedBook = null;
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
            super.initControls();

            moveDataButton.setDisable(true);

            notebooksController.setValues(this, tableNotebook);
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
                    moveDataButton.setDisable(node == null || node.getValue().isRoot());
                }
            });
            loadedBook = notebooksController.rootBook;

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
                        String html = style + HtmlTools.bodyWithoutTag(codesController.codes());
                        webviewController.loadContents(html);
                        htmlEditor.setHtmlText(html);
                    } else if (oldTab == noteEditorTab) {
                        String html = HtmlTools.bodyWithoutTag(htmlEditor.getHtmlText());
                        webviewController.loadContents(style + html);
                        codesController.load(html);
                    }
                }
            });

            webviewController.setValues(this, false, false);
            codesController.setValues(this);

            titleInput.setText(message("Note"));
            styleInput.setText(AppVariables.getUserConfigValue(baseName + "Style", null));

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

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void afterSceneLoaded() {
        try {
            super.afterSceneLoaded();

            FxmlControl.setTooltip(deleteBookButton, new Tooltip(message("DeleteBook")));
            FxmlControl.setTooltip(addBookButton, new Tooltip(message("AddBook")));
            FxmlControl.setTooltip(clearNotesButton, new Tooltip(message("ClearNotes")));
            FxmlControl.setTooltip(deleteNotesButton, new Tooltip(message("DeleteNotes")));
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
            addBook();
        });
        items.add(menu);

        menu = new MenuItem(message("DeleteBook"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            deleteBook();
        });
        items.add(menu);

        menu = new MenuItem(message("RenameBook"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            renameBook();
        });
        menu.setDisable(node.getValue().isRoot());
        items.add(menu);

        menu = new MenuItem(message("Move"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            moveBook();
        });
        menu.setDisable(node.getValue().isRoot());
        items.add(menu);

        items.add(new SeparatorMenuItem());

        menu = new MenuItem(message("Export"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            exportBook();
        });
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
    protected void refreshBooks() {
        notebooksController.loadTree();
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

                private boolean clear, loadNull, editNull;

                @Override
                protected boolean handle() {
                    try ( Connection conn = DerbyBase.getConnection()) {
                        if (book.isRoot()) {
                            notebooksController.rootBook = tableNotebook.clear(conn);
                            clear = true;
                            loadNull = true;
                            editNull = true;
                        } else {
                            tableNotebook.deleteData(book);
                            if (loadedBook != null) {
                                if (tableNotebook.find(conn, loadedBook.getNbid()) == null) {
                                    loadNull = true;
                                }
                            }
                            if (editingBook != null) {
                                if (tableNotebook.find(conn, editingBook.getNbid()) == null) {
                                    editNull = true;
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
                    if (clear) {
                        notebooksController.treeView.getRoot().getChildren().clear();
                    } else {
                        node.getChildren().clear();
                        if (node.getParent() != null) {
                            node.getParent().getChildren().remove(node);
                        }
                    }
                    if (loadNull) {
                        loadBook(null);
                    }
                    if (editNull) {
                        editNote(null);
                    }
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
    protected void addBook() {
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
            String chainName = ControlNotebookSelector.nodeName(node);
            String name = FxmlControl.askValue(getBaseTitle(), chainName, message("AddBook"), message("Notebook") + "m");
            if (name == null || name.isBlank() || name.contains(NotebooksSeparater)) {
                return;
            }
            task = new SingletonTask<Void>() {
                private Notebook newBook;

                @Override
                protected boolean handle() {
                    newBook = new Notebook(book.getNbid(), name);
                    newBook = tableNotebook.insertData(newBook);
                    return newBook != null;
                }

                @Override
                protected void whenSucceeded() {
                    TreeItem<Notebook> newNode = new TreeItem<>(newBook);
                    node.getChildren().add(newNode);
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
                selectedNode = notebooksController.treeView.getRoot();
                if (selectedNode == null) {
                    return;
                }
            }
            TreeItem<Notebook> node = selectedNode;
            Notebook book = node.getValue();
            if (book == null || book.isRoot()) {
                return;
            }
            String chainName = ControlNotebookSelector.nodeName(node);
            String name = FxmlControl.askValue(getBaseTitle(), chainName, message("RenameBook"), book.getName() + "m");
            if (name == null || name.isBlank() || name.contains(NotebooksSeparater)) {
                return;
            }
            task = new SingletonTask<Void>() {
                @Override
                protected boolean handle() {
                    book.setName(name);
                    return tableNotebook.updateData(book) != null;
                }

                @Override
                protected void whenSucceeded() {
                    notebooksController.treeView.refresh();
                    makeLoadedNamePanes();
                    makeEditingBookLabel();
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
        controller.setSource(this, selectedNode.getValue(), chainName);
    }

    @FXML
    protected void exportBook() {
        NotesExportController exportController = (NotesExportController) FxmlStage.openStage(CommonValues.NotesExportFxml);
        exportController.setSource(this);
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
        notebooksController.loadTree(false);
    }

    @FXML
    protected void unfoldBooks() {
        notebooksController.loadTree(true);
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
        tagsList.getItems().clear();
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {
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

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
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
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            List<Tag> tags = tagsList.getSelectionModel().getSelectedItems();
            if (tags == null || tags.isEmpty()) {
                return;
            }
            loadedBook = null;
            tableData.clear();
            chainPane.getChildren().clear();
            task = new SingletonTask<Void>() {
                private List<Note> notes;

                @Override
                protected boolean handle() {
                    notes = tableNoteTag.readNotes(tags);
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    tableData.setAll(notes);
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
                    tableTag.clearData();
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
        Notes list
     */
    protected void loadBook(Notebook node) {
        loadedBook = node;
        if (node == null) {
            tableData.clear();
            chainPane.getChildren().clear();
        } else {
            loadTableData();
        }
    }

    @Override
    public void postLoadedTableData() {
        makeLoadedNamePanes();
    }

    public void makeLoadedNamePanes() {
        if (loadedBook == null) {
            chainPane.getChildren().clear();
            return;
        }
        synchronized (this) {
            SingletonTask bookTask = new SingletonTask<Void>() {
                private List<Notebook> ancestor;

                @Override
                protected boolean handle() {
                    ancestor = tableNotebook.ancestor(loadedBook.getNbid());
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    List<Node> nodes = new ArrayList<>();
                    if (ancestor != null) {
                        for (Notebook book : ancestor) {
                            Hyperlink link = new Hyperlink(book.getName());
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
                    nodes.add(new Label(loadedBook.getName()));
                    chainPane.getChildren().setAll(nodes);
                    chainPane.applyCss();
                }
            };
            openHandlingStage(bookTask, Modality.WINDOW_MODAL);
            bookTask.setSelf(bookTask);
            Thread thread = new Thread(bookTask);
            thread.setDaemon(true);
            thread.start();
        }

    }

    @Override
    public int readDataSize() {
        if (loadedBook == null) {
            return 0;
        }
        return TableNote.bookSize(loadedBook.getNbid());
    }

    @Override
    public List<Note> readPageData() {
        if (loadedBook == null) {
            return null;
        }
        return tableNote.queryBook(loadedBook.getNbid(), currentPageStart - 1, currentPageSize);
    }

    @Override
    protected int clearData() {
        if (loadedBook == null) {
            return -1;
        }
        return tableNote.clearBook(loadedBook.getNbid());
    }

    @Override
    protected List<MenuItem> makeTableContextMenu() {
        try {
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;

            menu = new MenuItem(message("DeleteNotes"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                deleteAction();
            });
            menu.setDisable(deleteNotesButton.isDisabled());
            items.add(menu);

            menu = new MenuItem(message("ClearNotes"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                clearAction();
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
        selectedLabel.setText(message("Selected") + ": " + selection);
    }

    @FXML
    protected void refreshNotes() {
        refreshAction();
    }

    @FXML
    protected void clearNotes() {
        clearAction();
    }

    @FXML
    protected void deleteNotes() {
        deleteAction();
    }

    @FXML
    @Override
    public void editAction(ActionEvent event) {
        editNote(tableView.getSelectionModel().getSelectedItem());
    }

    @FXML
    protected void addNote() {
        editNote(null);
    }

    protected void editNote(Note note) {
        editingBook = loadedBook;
        currentNote = note;
        makeEditingBookLabel();
        loadNote();
    }

    public void makeEditingBookLabel() {
        if (editingBook == null) {
            notebookLabel.setText("");
            return;
        }
        synchronized (this) {
            SingletonTask noteTask = new SingletonTask<Void>() {
                private List<Notebook> ancestor;

                @Override
                protected boolean handle() {
                    ancestor = tableNotebook.ancestor(editingBook.getNbid());
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
                    chainName += editingBook.getName();
                    notebookLabel.setText(chainName);
                }
            };
            openHandlingStage(noteTask, Modality.WINDOW_MODAL);
            noteTask.setSelf(noteTask);
            Thread thread = new Thread(noteTask);
            thread.setDaemon(true);
            thread.start();
        }

    }

    /*
        edit note
     */
    protected void loadNote() {
        if (currentNote != null) {
            idInput.setText(currentNote.getNtid() + "");
            titleInput.setText(currentNote.getTitle());
            timeInput.setText(DateTools.datetimeToString(currentNote.getUpdateTime()));
            String html = currentNote.getHtml();
            String style = AppVariables.getUserConfigValue(baseName + "Style", null);
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
        refreshNoteTags();
        showRightPane();
    }

    @FXML
    protected void copyNote() {
        idInput.setText("");
        timeInput.setText("");
        currentNote = null;
    }

    @FXML
    protected void recoverNote() {
        loadNote();
    }

    @FXML
    @Override
    public void saveAction() {
        String title = titleInput.getText();
        if (title == null || title.isBlank()) {
            popError(message("TitleNonEmpty"));
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {
                private Note note;

                @Override
                protected boolean handle() {
                    note = new Note();
                    note.setTitle(title);
                    note.setHtml(StringTools.discardBlankLines(codesController.codes()));
                    note.setUpdateTime(new Date());
                    if (currentNote != null) {
                        note.setNtid(currentNote.getNtid());
                        note.setNotebook(currentNote.getNotebookid());
                        currentNote = tableNote.updateData(note);
                    } else if (editingBook != null) {
                        note.setNotebook(editingBook.getNbid());
                        currentNote = tableNote.insertData(note);
                    } else {
                        return false;
                    }
                    return currentNote != null;
                }

                @Override
                protected void whenSucceeded() {
                    popSuccessful();
                    if (editingBook != null && loadedBook != null
                            && editingBook.getNbid() == loadedBook.getNbid()) {
                        loadTableData();
                    }
                    loadNote();
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
        noteTagsList.getItems().clear();
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {
                private List<Tag> tags = null;
                private List<Long> noteTags = null;

                @Override
                protected boolean handle() {
                    tags = tableTag.readAll();
                    if (currentNote != null) {
                        noteTags = tableNoteTag.readTags(currentNote.getNtid());
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    noteTagsList.getItems().setAll(tags);
                    if (tags != null && noteTags != null) {
                        for (int i = 0; i < tags.size(); i++) {
                            if (noteTags.contains(tags.get(i).getTgid())) {
                                noteTagsList.getSelectionModel().select(i);
                            }
                        }
                    } else {
                        selectNoneNoteTags();
                    }
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
            if (task != null && !task.isQuit()) {
                return;
            }
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
            task = new SingletonTask<Void>() {
                private Tag tag;
                private boolean updateCurrent;

                @Override
                protected boolean handle() {
                    tag = tableTag.insertData(new Tag(name));
                    updateCurrent = forCurrentNote && tag != null && currentNote != null;
                    if (updateCurrent) {
                        tableNoteTag.insertData(new NoteTag(currentNote.getNtid(), tag.getTgid()));
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

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
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
            if (task != null && !task.isQuit()) {
                return;
            }
            List<Tag> selected = noteTagsList.getSelectionModel().getSelectedItems();
            List<Long> tags = new ArrayList();
            if (selected != null) {
                for (Tag tag : selected) {
                    tags.add(tag.getTgid());
                }
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
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

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    /*
        static methods
     */
    public static NotesController oneOpen(boolean load) {
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
        } else if (load) {
            controller.refreshBooks();
        }
        if (controller != null) {
            controller.getMyStage().requestFocus();
        }
        return controller;
    }

}
