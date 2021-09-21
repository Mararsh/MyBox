package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.stage.Stage;
import javafx.stage.Window;
import mara.mybox.db.data.Note;
import mara.mybox.db.data.Notebook;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2021-3-29
 * @License Apache License Version 2.0
 */
public class NotesMoveNotesController extends ControlNotebookSelector {

    public NotesMoveNotesController() {
        baseTitle = Languages.message("MoveNotes");
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
            long bookid = targetBook.getNbid();
            task = new SingletonTask<Void>() {

                private int count;
                private boolean updateNote = false;

                @Override
                protected boolean handle() {
                    try {
                        long noteid = -1;
                        if (notesController.noteEditorController.currentNote != null) {
                            noteid = notesController.noteEditorController.currentNote.getNtid();
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
                    notesController.popInformation(Languages.message("Moved") + ": " + count);
                    if (updateNote) {
                        notesController.noteEditorController.currentNote.setNotebook(bookid);
                        notesController.noteEditorController.bookOfCurrentNote = targetBook;
                        notesController.noteEditorController.updateBookOfCurrentNote();
                    }
                    closeStage();
                }
            };
            start(task);
        }
    }

    /*
        static methods
     */
    public static NotesMoveNotesController oneOpen(NotesController notesController) {
        NotesMoveNotesController controller = null;
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof NotesMoveNotesController) {
                try {
                    controller = (NotesMoveNotesController) object;
                    controller.toFront();
                    break;
                } catch (Exception e) {
                }
            }
        }
        if (controller == null) {
            controller = (NotesMoveNotesController) WindowTools.openStage(Fxmls.NotesMoveNotesFxml);
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
