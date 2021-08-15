package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Window;
import mara.mybox.db.data.Tag;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.table.TableNote;
import mara.mybox.db.table.TableNoteTag;
import mara.mybox.db.table.TableNotebook;
import mara.mybox.db.table.TableTag;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-2-28
 * @License Apache License Version 2.0
 */
public class NotesController extends NotesController_Notes {

    public NotesController() {
        baseTitle = Languages.message("Notes");
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
        notebooksController.selectedNode = null;

    }

    @Override
    public void initControls() {
        try {
            notebooksController.setParameters(this);
            noteEditorController.setParameters(this);

            timeController.setParent(this, false);
            searchController.init(this, baseName + "Saved", Languages.message("Note"), 20);

            super.initControls();

            notebooksController.selectedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldTab, Boolean newTab) {
                    loadBook(notebooksController.selectedNode);
                }
            });

            subCheck.setSelected(UserConfig.getBoolean(baseName + "IncludeSub", false));
            subCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldTab, Boolean newTab) {
                    if (notebooksController.selectedNode != null) {
                        loadTableData();
                    }
                }
            });

            timeController.queryNodesButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    queryTimes();
                }
            });
            timeController.refreshNodesButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    refreshTimes();
                }
            });

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
            tagsList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tag>() {
                @Override
                public void changed(ObservableValue ov, Tag oldValue, Tag newValue) {
                    queryTagsButton.setDisable(newValue == null);
                    deleteTagsButton.setDisable(newValue == null);
                    renameTagButton.setDisable(newValue == null);
                }
            });
            tagsList.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (popMenu != null && popMenu.isShowing()) {
                        popMenu.hide();
                    }
                    Tag selected = tagsList.getSelectionModel().getSelectedItem();
                    if (event.getButton() == MouseButton.SECONDARY) {
                        popTagMenu(event, selected);
                    } else if (event.getClickCount() > 1) {
                        queryTags();
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

            if (tableNotebook.size() < 2
                    && PopTools.askSure(getBaseTitle(), Languages.message("ImportExamples"))) {
                notebooksController.importExamples();
            } else {
                loadBooks();
            }
            refreshTags();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
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

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (!super.keyEventsFilter(event)) {
            return noteEditorController.keyEventsFilter(event);
        }
        return true;
    }

    /*
        static methods
     */
    public static NotesController oneOpen() {
        NotesController controller = null;
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof NotesController) {
                try {
                    controller = (NotesController) object;
                    controller.toFront();
                    break;
                } catch (Exception e) {
                }
            }
        }
        if (controller == null) {
            controller = (NotesController) WindowTools.openStage(Fxmls.NotesFxml);
        }
        return controller;
    }

}
