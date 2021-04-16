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

    @Override
    public Notebook getIgnoreBook() {
        return notesController.selectedBook;
    }

    @FXML
    @Override
    public void okAction() {
        if (notesController == null) {
            closeStage();
            return;
        }
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
            TreeItem<Notebook> selectedNode = treeView.getSelectionModel().getSelectedItem();
            if (selectedNode == null) {
                alertError(message("SelectNotebook"));
                return;
            }
            task = new SingletonTask<Void>() {

                private int count;

                @Override
                protected boolean handle() {
                    try {
                        long bookid = selectedNode.getValue().getNbid();
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
                    }
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
            controller.setValues(notesController);
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
