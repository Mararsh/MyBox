package mara.mybox.controller;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.stage.Modality;
import javafx.stage.Stage;
import mara.mybox.db.data.Note;
import mara.mybox.db.data.Notebook;
import mara.mybox.fxml.FxmlStage;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2021-3-29
 * @License Apache License Version 2.0
 */
public class NotesMoveNotesController extends ControlNotebookSelector {

    public NotesMoveNotesController() {
        baseTitle = message("MoveNotes");
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
                alertError(message("SelectNotes"));
                notesController.getMyStage().requestFocus();
                return;
            }
            TreeItem<Notebook> targetNode = treeView.getSelectionModel().getSelectedItem();
            if (targetNode == null) {
                alertError(message("SelectNotebook"));
                return;
            }
            Notebook targetBook = targetNode.getValue();
            if (targetBook == null) {
                return;
            }
            if (equal(targetBook, notesController.notebooksController.selectedNode)) {
                alertError(message("TargetShouldDifferentWithSource"));
                return;
            }
            long bookid = targetBook.getNbid();
            task = new SingletonTask<Void>() {

                private int count;
                private boolean updateNote = false;

                @Override
                protected boolean handle() {
                    try {
                        long noteid = -1;
                        if (notesController.currentNote != null) {
                            noteid = notesController.currentNote.getNtid();
                        }
                        for (Note note : notes) {
                            note.setNotebook(bookid);
                            if (note.getNtid() == noteid) {
                                updateNote = true;
                            }
                        }
                        count = tableNote.updateList(notes);
                        return count > 0;
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    if (notesController == null || !notesController.getMyStage().isShowing()) {
                        notesController = NotesController.oneOpen();
                    } else {
                        notesController.bookChanged(targetBook);
                    }
                    notesController.notebooksController.loadTree(targetBook);
                    notesController.popInformation(message("Moved") + ": " + count);
                    if (updateNote) {
                        notesController.currentNote.setNotebook(bookid);
                        notesController.bookOfCurrentNote = targetBook;
                        notesController.updateBookOfCurrentNote();
                    }
                    closeStage();
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
    public static NotesMoveNotesController oneOpen(NotesController notesController) {
        NotesMoveNotesController controller = null;
        Stage stage = FxmlStage.findStage(message("MoveNotes"));
        if (stage != null && stage.getUserData() != null) {
            try {
                controller = (NotesMoveNotesController) stage.getUserData();
            } catch (Exception e) {
            }
        }
        if (controller == null) {
            controller = (NotesMoveNotesController) FxmlStage.openStage(CommonValues.NotesMoveNotesFxml);
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
