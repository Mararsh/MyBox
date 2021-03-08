package mara.mybox.controller;

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
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import mara.mybox.db.data.Note;
import mara.mybox.db.data.Notebook;
import static mara.mybox.db.data.Notebook.NotebooksSeparater;
import mara.mybox.db.data.NotesTools;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.data.VisitHistoryTools;
import mara.mybox.db.table.TableNote;
import mara.mybox.db.table.TableNotebook;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.fxml.TableDateCell;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;
import mara.mybox.value.CommonValues;
import org.w3c.dom.events.EventListener;

/**
 * @Author Mara
 * @CreateDate 2021-2-28
 * @License Apache License Version 2.0
 */
public class NotesController extends BaseDataTableController<Note> {

    protected TableNotebook tableNotebook;
    protected TableNote tableNote;
    protected Notebook loadedBook, editingBook;
    protected Note currentNote;
    protected EventListener linkListener;
    protected double linkX, linkY;

    @FXML
    protected ControlNotebookSelector notebooksController;
    @FXML
    protected TableColumn<Note, Long> ntidColumn;
    @FXML
    protected TableColumn<Note, String> titleColumn;
    @FXML
    protected TableColumn<Note, Date> timeColumn;
    @FXML
    protected TextField idInput, titleInput, timeInput;
    @FXML
    protected Button deleteBookButton, addBookButton, clearNotesButton, deleteNotesButton;
    @FXML
    protected FlowPane chainPane;
    @FXML
    protected TabPane notePane, noteEditPane;
    @FXML
    protected Tab noteViewTab, noteEditTab, noteTab, tagsTab;
    @FXML
    protected ControlWebview webviewController;
    @FXML
    protected ControlHtmlCodes codesController;
    @FXML
    protected Label notebookLabel;

    public NotesController() {
        baseTitle = AppVariables.message("Notes");

        SourceFileType = VisitHistory.FileType.Text;
        SourcePathType = VisitHistory.FileType.Text;
        TargetPathType = VisitHistory.FileType.Text;
        TargetFileType = VisitHistory.FileType.Text;
        AddFileType = VisitHistory.FileType.Text;
        AddPathType = VisitHistory.FileType.Text;

        sourcePathKey = VisitHistoryTools.getPathKey(VisitHistory.FileType.Text);

        sourceExtensionFilter = CommonFxValues.TextExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;
    }

    @Override
    public void setTableDefinition() {
        tableNotebook = new TableNotebook();
        tableNote = new TableNote();
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

            notebooksController.setValues(this, tableNotebook);
            notebooksController.treeView.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (popMenu != null && popMenu.isShowing()) {
                        popMenu.hide();
                    }
                    TreeItem<Notebook> node = notebooksController.treeView.getSelectionModel().getSelectedItem();
                    if (node == null) {
                        return;
                    }
                    if (event.getButton() == MouseButton.SECONDARY) {
                        popBookMenu(event, node);
                    } else {
                        loadBook(node);
                    }
                }
            });
            loadedBook = notebooksController.root;

            noteEditPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
                @Override
                public void changed(ObservableValue ov, Tab oldTab, Tab newTab) {
                    if (oldTab == noteEditTab) {
                        String text = codesController.codes();
                        webviewController.loadContents(text);
                    }
                }
            });

            webviewController.setValues(this, false, false);
            codesController.setValues(this);

            titleInput.setText(message("Note"));

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
            FxmlControl.removeTooltip(dataExportButton);
            FxmlControl.removeTooltip(dataImportButton);

            if (tableNotebook.size() < 2
                    && FxmlControl.askSure(getBaseTitle(), message("ImportNotesExample"))) {
                importExamples();
            } else {
                notebooksController.loadTree();
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    /*
        Notebooks
     */
    protected void popBookMenu(MouseEvent event, TreeItem<Notebook> node) {
        if (isSettingValues || node == null) {
            return;
        }
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
        TreeItem<Notebook> selectedNode = notebooksController.treeView.getSelectionModel().getSelectedItem();
        if (selectedNode == null) {
            selectedNode = notebooksController.treeView.getRoot();
            if (selectedNode == null) {
                return;
            }
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            TreeItem<Notebook> node = selectedNode;
            Notebook book = node.getValue();
            if (book == null) {
                return;
            }
            String chainName = ControlNotebookSelector.nodeName(node);
            if (!FxmlControl.askSure(getBaseTitle(), chainName, message("DeleteBook"))) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    if (book.getNbid() <= 1) {
                        notebooksController.root = tableNotebook.clear();
                        notebooksController.treeView.getRoot().getChildren().clear();
                    } else {
                        tableNotebook.deleteData(book);
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    node.getChildren().clear();
                    if (node.getParent() != null) {
                        node.getParent().getChildren().remove(node);
                    }
                    if (loadedBook != null && book.getNbid() == loadedBook.getNbid()) {
                        loadBook(null);
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
        TreeItem<Notebook> selectedNode = notebooksController.treeView.getSelectionModel().getSelectedItem();
        if (selectedNode == null) {
            selectedNode = notebooksController.treeView.getRoot();
            if (selectedNode == null) {
                return;
            }
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            TreeItem<Notebook> node = selectedNode;
            Notebook book = node.getValue();
            String chainName = ControlNotebookSelector.nodeName(node);
            String name = FxmlControl.askValue(getBaseTitle(), chainName, message("AddBook"), message("Notebook"));
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
        TreeItem<Notebook> selectedNode = notebooksController.treeView.getSelectionModel().getSelectedItem();
        if (selectedNode == null) {
            selectedNode = notebooksController.treeView.getRoot();
            if (selectedNode == null) {
                return;
            }
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            TreeItem<Notebook> node = selectedNode;
            Notebook book = node.getValue();
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
                    if (loadedBook != null && book.getNbid() == loadedBook.getNbid()) {
                        loadBook(node);
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
    protected void popExportMenu(MouseEvent mouseEvent) {
        try {
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu = new MenuItem(message("Export"));
            menu.setOnAction((ActionEvent event) -> {
                exportBook();
            });
            items.add(menu);
            items.add(new SeparatorMenuItem());

            CheckMenuItem checkMenu = new CheckMenuItem(message("ExportTime"));
            checkMenu.setSelected(AppVariables.getUserConfigBoolean(baseName + "ExportTime", false));
            checkMenu.setOnAction((ActionEvent menuItemEvent) -> {
                AppVariables.setUserConfigValue(baseName + "ExportTime", checkMenu.isSelected());
            });
            items.add(checkMenu);

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

    @FXML
    protected void exportBook() {
        TreeItem<Notebook> selectedNode = notebooksController.treeView.getSelectionModel().getSelectedItem();
        if (selectedNode == null) {
            selectedNode = notebooksController.treeView.getRoot();
            if (selectedNode == null) {
                return;
            }
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            TreeItem<Notebook> node = selectedNode;
            Notebook book = node.getValue();
            String name = ControlNotebookSelector.nodeName(node).replaceAll(Notebook.NotebooksSeparater, "-")
                    + "_" + DateTools.nowFileString() + ".txt";
            targetFile = chooseSaveFile(AppVariables.getUserConfigPath(targetPathKey), name, targetExtensionFilter);
            if (targetFile == null) {
                return;
            }
            recordFileWritten(targetFile);
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    TreeItem<Notebook> parent = node.getParent();
                    String parentName = null;
                    if (parent != null) {
                        parentName = ControlNotebookSelector.nodeName(parent);
                    }
                    return NotesTools.exportNotes(tableNotebook, tableNote, book, parentName,
                            targetFile, AppVariables.getUserConfigBoolean(baseName + "ExportTime", false));
                }

                @Override
                protected void whenSucceeded() {
                    popSuccessful();
                    TextEditerController controller = (TextEditerController) openStage(CommonValues.TextEditerFxml);
                    controller.hideRightPane();
                    controller.openTextFile(targetFile);
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
        Notes list
     */
    protected void loadBook(TreeItem<Notebook> node) {
        if (node == null) {
            loadedBook = null;
            tableData.clear();
            chainPane.getChildren().clear();
        } else {
            loadedBook = node.getValue();
            loadTableData();
            chainPane.getChildren().clear();
            if (loadedBook == null) {
                return;
            }
            List<TreeItem<Notebook>> ancestor = ControlNotebookSelector.ancestor(node);
            if (ancestor != null) {
                for (TreeItem<Notebook> aNode : ancestor) {
                    Notebook book = aNode.getValue();
                    Hyperlink link = new Hyperlink(book.getName());
                    link.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            loadBook(aNode);
                        }
                    });
                    chainPane.getChildren().add(link);
                    Label label = new Label(">");
                    chainPane.getChildren().add(label);
                }
            }
            Label label = new Label(loadedBook.getName());
            chainPane.getChildren().add(label);
            chainPane.applyCss();
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
        String chainName = "";
        for (Node node : chainPane.getChildren()) {
            chainName += ((Labeled) node).getText();
        }
        notebookLabel.setText(chainName);
        currentNote = note;
        loadNote();
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
            isSettingValues = true;
            webviewController.loadContents(html);
            codesController.load(html);
            isSettingValues = false;
        } else {
            idInput.setText("");
            timeInput.setText("");
            titleInput.setText(message("Note"));
            webviewController.loadContents("");
            codesController.load("");
        }
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
                    } else {
                        note.setNotebook(editingBook.getNbid());
                        currentNote = tableNote.insertData(note);
                    }
                    return currentNote != null;
                }

                @Override
                protected void whenSucceeded() {
                    popSuccessful();
                    if (editingBook.getNbid() == loadedBook.getNbid()) {
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
