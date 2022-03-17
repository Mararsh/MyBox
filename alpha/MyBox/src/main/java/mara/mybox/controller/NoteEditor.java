package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import mara.mybox.db.data.TreeLeaf;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WebViewTools;
import mara.mybox.fxml.style.HtmlStyles;
import mara.mybox.tools.HtmlReadTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-8-11
 * @License Apache License Version 2.0
 */
public class NoteEditor extends HtmlEditorController {

    protected NotesController notesController;
    protected boolean tagsChanged;

    @FXML
    protected Tab attributesTab;
    @FXML
    protected NoteAttributes attributesController;

    public void setParameters(NotesController notesController) {
        try {
            this.notesController = notesController;
            this.baseName = notesController.baseName;
            saveButton = notesController.saveButton;
            webViewController.defaultStyle = HtmlStyles.styleValue("Default");
            editorController.defaultStyle = HtmlStyles.styleValue("Default");

            attributesController.setEditor(this);
            notesController.leafController = attributesController;
            attributesController.changeNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue v, Boolean ov, Boolean nv) {
                    attributesTab.setText(message("Attributes") + (attributesController.leafChanged ? " *" : ""));;
                }
            });
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void editNote(TreeLeaf note) {
        updateFileStatus(false);
        attributesController.editLeaf(note);
        if (note != null) {
            loadContents(note.getValue());
        } else {
            loadContents("");
        }
    }

    protected void addNote() {
        editNote(null);
    }

    protected void copyNote() {
        attributesController.copyLeaf();
        updateFileStatus(true);
    }

    protected void recoverNote() {
        editNote(notesController.currentLeaf);
    }

    /*
        html
     */
    @Override
    public String htmlCodes(String html) {
        return HtmlReadTools.body(html, false);
    }

    @Override
    public String htmlInWebview() {
        return HtmlReadTools.body(WebViewTools.getHtml(webEngine), false);
    }

    @Override
    public String htmlByEditor() {
        return HtmlReadTools.body(htmlEditor.getHtmlText(), false);
    }

}
