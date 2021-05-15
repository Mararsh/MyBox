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
public class NotesCopyNotesController extends ControlNotebookSelector {

    public NotesCopyNotesController() {
        baseTitle = message("CopyNotes");
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
                    notesController.popInformation(message("Copied") + ": " + count);
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
    public static NotesCopyNotesController oneOpen(NotesController notesController) {
        NotesCopyNotesController controller = null;
        Stage stage = FxmlStage.findStage(message("CopyNotes"));
        if (stage != null && stage.getUserData() != null) {
            try {
                controller = (NotesCopyNotesController) stage.getUserData();
            } catch (Exception e) {
            }
        }
        if (controller == null) {
            controller = (NotesCopyNotesController) FxmlStage.openStage(CommonValues.NotesCopyNotesFxml);
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
