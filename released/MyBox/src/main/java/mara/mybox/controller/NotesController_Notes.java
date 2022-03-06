package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Region;
import mara.mybox.db.data.Note;
import mara.mybox.db.data.Notebook;
import mara.mybox.db.table.TableNote;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.cell.TableDateCell;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-8-11
 * @License Apache License Version 2.0
 */
public abstract class NotesController_Notes extends NotesController_Tags {

    protected void initNotes() {
        try {
            noteEditorController.setParameters((NotesController) this);

            subCheck.setSelected(UserConfig.getBoolean(baseName + "IncludeSub", false));
            subCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldTab, Boolean newTab) {
                    if (notebooksController.selectedNode != null) {
                        loadTableData();
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    protected void initColumns() {
        try {
            super.initColumns();
            ntidColumn.setCellValueFactory(new PropertyValueFactory<>("ntid"));
            titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
            timeColumn.setCellValueFactory(new PropertyValueFactory<>("updateTime"));
            timeColumn.setCellFactory(new TableDateCell());

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();

            NodeStyleTools.setTooltip(clearNotesButton, new Tooltip(Languages.message("ClearNotes")));
            NodeStyleTools.setTooltip(deleteNotesButton, new Tooltip(Languages.message("DeleteNotes")));
            NodeStyleTools.setTooltip(moveDataNotesButton, new Tooltip(Languages.message("MoveNotes")));
            NodeStyleTools.setTooltip(copyNotesButton, new Tooltip(Languages.message("CopyNotes")));

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void postLoadedTableData() {
        super.postLoadedTableData();
        makeConditionPane();
    }

    public void makeConditionPane() {
        notesConditionBox.getChildren().clear();
        if (notebooksController.selectedNode == null) {
            if (queryConditionsString != null) {
                conditionLabel.setText(queryConditionsString);
                notesConditionBox.getChildren().add(conditionLabel);
            }
            notesConditionBox.applyCss();
            return;
        }
        synchronized (this) {
            SingletonTask bookTask = new SingletonTask<Void>(this) {
                private List<Notebook> ancestor;

                @Override
                protected boolean handle() {
                    ancestor = tableNotebook.ancestor(notebooksController.selectedNode.getNbid());
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
                    Label label = new Label(notebooksController.selectedNode.getName());
                    label.setWrapText(true);
                    label.setMinHeight(Region.USE_PREF_SIZE);
                    nodes.add(label);
                    namesPane.getChildren().setAll(nodes);
                    notesConditionBox.getChildren().setAll(namesPane, subCheck);
                    notesConditionBox.applyCss();
                }
            };
            start(bookTask, false);
        }

    }

    @Override
    public long readDataSize() {
        if (notebooksController.selectedNode != null && subCheck.isSelected()) {
            return TableNote.withSubSize(tableNotebook, notebooksController.selectedNode.getNbid());

        } else if (queryConditions != null) {
            return tableNote.conditionSize(queryConditions);

        } else {
            return 0;
        }
    }

    @Override
    public List<Note> readPageData() {
        if (notebooksController.selectedNode != null && subCheck.isSelected()) {
            return tableNote.withSub(tableNotebook, notebooksController.selectedNode.getNbid(), startRowOfCurrentPage, pageSize);

        } else if (queryConditions != null) {
            return tableNote.queryConditions(queryConditions, orderColumns, startRowOfCurrentPage, pageSize);

        } else {
            return null;
        }
    }

    @Override
    protected long clearData() {
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

            menu = new MenuItem(Languages.message("DeleteNotes"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                deleteNotes();
            });
            menu.setDisable(deleteNotesButton.isDisabled());
            items.add(menu);

            menu = new MenuItem(Languages.message("CopyNotes"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                copyNotes();
            });
            menu.setDisable(deleteNotesButton.isDisabled());
            items.add(menu);

            menu = new MenuItem(Languages.message("MoveNotes"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                moveNotes();
            });
            menu.setDisable(deleteNotesButton.isDisabled());
            items.add(menu);

            menu = new MenuItem(Languages.message("AddNote"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                addBookNote();
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());

            menu = new MenuItem(Languages.message("ClearNotes"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                clearNotes();
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());

            if (pageNextButton != null && pageNextButton.isVisible() && !pageNextButton.isDisabled()) {
                menu = new MenuItem(Languages.message("NextPage"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    pageNextAction();
                });
                items.add(menu);
            }

            if (pagePreviousButton != null && pagePreviousButton.isVisible() && !pagePreviousButton.isDisabled()) {
                menu = new MenuItem(Languages.message("PreviousPage"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    pagePreviousAction();
                });
                items.add(menu);
            }

            menu = new MenuItem(Languages.message("Refresh"));
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
        editAction();
    }

    @Override
    protected void checkButtons() {
        if (isSettingValues) {
            return;
        }
        super.checkButtons();

        boolean isEmpty = tableData == null || tableData.isEmpty();
        boolean none = isEmpty || tableView.getSelectionModel().getSelectedItem() == null;
        deleteNotesButton.setDisable(none);
        copyNotesButton.setDisable(none);
        moveDataNotesButton.setDisable(none);
    }

    @FXML
    protected void refreshNotes() {
        refreshAction();
    }

    @FXML
    protected void addBookNote() {
        showRightPane();
        noteEditorController.bookOfCurrentNote = notebooksController.selectedNode;
        noteEditorController.editNote(null);
    }

    @FXML
    protected void copyNotes() {
        NotesCopyNotesController.oneOpen((NotesController) this);
    }

    @FXML
    protected void moveNotes() {
        NotesMoveNotesController.oneOpen((NotesController) this);
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

    @FXML
    @Override
    public void editAction() {
        Note note = tableView.getSelectionModel().getSelectedItem();
        if (note != null) {
            noteEditorController.editNote(note);
        }
    }

    @Override
    protected void loadBook(Notebook book) {
        clearQuery();
        notebooksController.selectedNode = book;
        if (book != null) {
            queryConditions = " notebook=" + book.getNbid();
            loadTableData();
        }
    }

    protected void bookChanged(Notebook book) {
        if (book == null) {
            return;
        }
        if (notebooksController.selectedNode != null && notebooksController.selectedNode.getNbid() == book.getNbid()) {
            notebooksController.selectedNode = book;
            makeConditionPane();
        }
        noteEditorController.bookChanged(book);
    }

    @FXML
    protected void addNote() {
        noteEditorController.addNote();
    }

    @FXML
    protected void copyNote() {
        noteEditorController.copyNote();
    }

    @FXML
    protected void recoverNote() {
        noteEditorController.recoverNote();
    }

    @FXML
    @Override
    public void saveAction() {
        noteEditorController.saveAction();
    }

}
