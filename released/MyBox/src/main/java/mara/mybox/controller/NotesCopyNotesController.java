package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import mara.mybox.db.data.Note;
import mara.mybox.db.data.Notebook;
import mara.mybox.fxml.WindowTools;
import static mara.mybox.value.Languages.message;

import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2021-3-29
 * @License Apache License Version 2.0
 */
public class NotesCopyNotesController extends ControlNotebookSelector {

    public NotesCopyNotesController() {
        baseTitle = Languages.message("CopyNotes");
    }

    @FXML
    @Override
    public void okAction() {
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            List<Note> notes = notesController.tableView.getSelectionModel().getSelectedItems();
            if (notes == null || notes.isEmpty()) {
                alertError(Languages.message("SelectNotes"));
                notesController.getMyStage().requestFocus();
                return;
            }
            TreeItem<Notebook> targetNode = treeView.getSelectionModel().getSelectedItem();
            if (targetNode == null) {
                alertError(Languages.message("SelectNotebook"));
                return;
            }
            Notebook targetBook = targetNode.getValue();
            if (targetBook == null) {
                return;
            }
            if (equal(targetBook, notesController.notebooksController.selectedNode)) {
                alertError(Languages.message("TargetShouldDifferentWithSource"));
                return;
            }
            task = new SingletonTask<Void>() {

                private int count;

                @Override
                protected boolean handle() {
                    try {
                        long bookid = targetBook.getNbid();
                        List<Note> newNotes = new ArrayList<>();
                        for (Note note : notes) {
                            Note newNote = new Note(bookid, note.getTitle(), note.getHtml(), null);
                            newNotes.add(newNote);
                        }
                        count = tableNote.insertList(newNotes);
                        return count > 0;
                    } catch (Exception e) {
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    if (notesController == null || !notesController.getMyStage().isShowing()) {
                        notesController = NotesController.oneOpen();
                    } else {
                        notesController.refreshTimes();
                        notesController.bookChanged(targetBook);
                    }
                    notesController.notebooksController.loadTree(targetBook);
                    notesController.popInformation(Languages.message("Copied") + ": " + count);
                    closeStage();
                }
            };
            start(task);
        }
    }

    /*
        static methods
     */
    public static NotesCopyNotesController oneOpen(NotesController notesController) {
        NotesCopyNotesController controller = null;
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof NotesCopyNotesController) {
                try {
                    controller = (NotesCopyNotesController) object;
                    controller.toFront();
                    break;
                } catch (Exception e) {
                }
            }
        }
        if (controller == null) {
            controller = (NotesCopyNotesController) WindowTools.openStage(Fxmls.NotesCopyNotesFxml);
        }
        if (controller != null) {
            controller.setCaller(notesController);
            Stage cstage = controller.getMyStage();
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        cstage.requestFocus();
                        cstage.toFront();
                    });
                }
            }, 500);
        }
        return controller;
    }

}
