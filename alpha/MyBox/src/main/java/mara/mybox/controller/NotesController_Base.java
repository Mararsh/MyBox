package mara.mybox.controller;

import java.util.Date;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import mara.mybox.db.data.Note;
import mara.mybox.db.table.TableNote;
import mara.mybox.db.table.TableNoteTag;
import mara.mybox.db.table.TableNotebook;
import mara.mybox.db.table.TableTag;

/**
 * @Author Mara
 * @CreateDate 2021-9-24
 * @License Apache License Version 2.0
 */
public abstract class NotesController_Base extends BaseSysTableController<Note> {

    protected TableNotebook tableNotebook;
    protected TableNote tableNote;
    protected TableTag tableTag;
    protected TableNoteTag tableNoteTag;

    @FXML
    protected ControlNotebookSelector notebooksController;
    @FXML
    protected ControlListCheckBox tagsListController;
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
    @FXML
    protected TableColumn<Note, Long> ntidColumn;
    @FXML
    protected TableColumn<Note, String> titleColumn;
    @FXML
    protected TableColumn<Note, Date> timeColumn;
    @FXML
    protected Button refreshNotesButton, clearNotesButton, deleteNotesButton, moveDataNotesButton, copyNotesButton,
            addBookNoteButton, addNoteButton, queryTagsButton, deleteTagsButton, renameTagButton,
            refreshTimesButton, queryTimesButton, refreshTagsButton;
    @FXML
    protected CheckBox subCheck;
    @FXML
    protected Label conditionLabel;

}
