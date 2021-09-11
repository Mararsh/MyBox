package mara.mybox.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.Note;
import mara.mybox.db.data.NoteTag;
import mara.mybox.db.data.Tag;
import mara.mybox.db.table.TableNote;
import mara.mybox.db.table.TableNoteTag;
import mara.mybox.db.table.TableNotebook;
import mara.mybox.db.table.TableTag;
import mara.mybox.fxml.PopTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-8-11
 * @License Apache License Version 2.0
 */
public class NotesController_Notebooks extends BaseDataTableController<Note> {

    protected TableNotebook tableNotebook;
    protected TableNote tableNote;
    protected TableTag tableTag;
    protected TableNoteTag tableNoteTag;

    @FXML
    protected ControlNotebookSelector notebooksController;
    @FXML
    protected ListView<Tag> tagsList;
    @FXML
    protected VBox timesBox;
    @FXML
    protected FlowPane tagsPane, namesPane;
    @FXML
    protected VBox notesConditionBox;
    @FXML
    protected RadioButton titleRadio, contentsRadio;
    @FXML
    protected ControlTimeTree timeController;
    @FXML
    protected ControlStringSelector searchController;
    @FXML
    protected NoteEditorController noteEditorController;

    protected void loadBooks() {
        notebooksController.loadTree();
        refreshTimes();
    }

    protected void clearQuery() {
        notebooksController.selectedNode = null;
        notebooksController.changedNode = null;
        queryConditions = null;
        queryConditionsString = null;
        tableData.clear();
        notesConditionBox.getChildren().clear();
        namesPane.getChildren().clear();
        currentPageStart = 1;
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

        menu = new MenuItem(Languages.message("Add"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            addTag();
        });
        items.add(menu);

        menu = new MenuItem(Languages.message("Delete"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            deleteTags();
        });
        menu.setDisable(tag == null);
        items.add(menu);

        menu = new MenuItem(Languages.message("Rename"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            renameTag();
        });
        menu.setDisable(tag == null);
        items.add(menu);

        items.add(new SeparatorMenuItem());

        menu = new MenuItem(Languages.message("Query"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            queryTags();
        });
        menu.setDisable(tag == null);
        items.add(menu);

        menu = new MenuItem(Languages.message("Refresh"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            refreshTags();
        });
        items.add(menu);

        items.add(new SeparatorMenuItem());
        menu = new MenuItem(Languages.message("PopupClose"));
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
            thread.setDaemon(false);
            thread.start();
        }
    }

    @FXML
    public void addTag() {
        addTag(false);
    }

    public void addTag(boolean forCurrentNote) {
        synchronized (this) {
            String name = PopTools.askValue(getBaseTitle(), message("Add"), message("Tag"), message("Tag") + new Date().getTime());
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
                        updateCurrent = forCurrentNote && tag != null && noteEditorController.currentNote != null;
                        if (updateCurrent) {
                            tableNoteTag.insertData(conn, new NoteTag(noteEditorController.currentNote.getNtid(), tag.getTgid()));
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
                    if (updateCurrent) {
                        noteEditorController.refreshNoteTags();
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
            thread.setDaemon(false);
            thread.start();
        }
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
            String name = PopTools.askValue(getBaseTitle(), tag.getTag(), Languages.message("Rename"), tag.getTag() + "m");
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
                    noteEditorController.refreshNoteTags();
                    popSuccessful();
                }
            };
            handling(task);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    @FXML
    public void queryTags() {
        List<Tag> selected = tagsList.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            popError(Languages.message("TagsQueryComments"));
            return;
        }
        clearQuery();
        queryConditions = TableNote.tagsCondition(selected);
        queryConditionsString = Languages.message("Tag") + ":";
        for (Tag tag : selected) {
            queryConditionsString += " " + tag.getTag();
        }
        loadTableData();
    }

    @FXML
    public void clearTags() {
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            if (!PopTools.askSure(getBaseTitle(), Languages.message("Tags"), Languages.message("SureDeleteAll"))) {
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
                    noteEditorController.tagsListController.clear();
                    popSuccessful();
                }

            };
            handling(task);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
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
            if (!PopTools.askSure(getBaseTitle(), Languages.message("Tags"), Languages.message("SureDelete"))) {
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
                    noteEditorController.refreshNoteTags();
                    popSuccessful();
                }

            };
            handling(task);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
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
            timesBox.setDisable(true);
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
                    timesBox.setDisable(false);
                }

            };
            timesTask.setSelf(timesTask);
            Thread thread = new Thread(timesTask);
            thread.setDaemon(false);
            thread.start();
        }
    }

    @FXML
    protected void queryTimes() {
        String c = timeController.check();
        if (c == null) {
            popError(Languages.message("MissTime"));
            return;
        }
        clearQuery();
        queryConditions = c;
        queryConditionsString = timeController.getFinalTitle();
        loadTableData();
    }

    /*
        Search
     */
    @FXML
    protected void search() {
        String s = searchController.value();
        if (s == null || s.isBlank()) {
            popError(Languages.message("InvalidData"));
            return;
        }
        String[] values = StringTools.splitBySpace(s);
        if (values == null || values.length == 0) {
            popError(Languages.message("InvalidData"));
            return;
        }
        searchController.refreshList();
        clearQuery();
        if (titleRadio.isSelected()) {
            queryConditions = null;
            queryConditionsString = Languages.message("Title") + ":";
            for (String v : values) {
                if (queryConditions != null) {
                    queryConditions += " OR ";
                } else {
                    queryConditions = " ";
                }
                queryConditions += " ( title like '%" + DerbyBase.stringValue(v) + "%' ) ";
                queryConditionsString += " " + v;
            }

        } else {
            queryConditions = null;
            queryConditionsString = Languages.message("Contents") + ":";
            for (String v : values) {
                if (queryConditions != null) {
                    queryConditions += " OR ";
                } else {
                    queryConditions = " ";
                }
                queryConditions += " ( html like '%" + DerbyBase.stringValue(v) + "%' ) ";
                queryConditionsString += " " + v;
            }
        }
        loadTableData();
    }

}
