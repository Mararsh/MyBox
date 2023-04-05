package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import mara.mybox.db.data.TreeNode;
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
public class NoteEditor extends ControlHtmlEditor {

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

            attributesController.setEditor(this);
            notesController.nodeController = attributesController;

            webViewController.linkInNewTab = true;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void attributesChanged() {
        attributesTab.setText(message("Attributes") + (attributesController.nodeChanged ? " *" : ""));
    }

    protected void editNote(TreeNode note) {
        updateFileStatus(false);
        attributesController.editNode(note);
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
        attributesController.copyNode();
        updateFileStatus(true);
    }

    protected void recoverNote() {
        editNote(attributesController.currentNode);
    }

    @Override
    protected void updateStageTitle() {
    }

    @FXML
    @Override
    public void saveAction() {
        notesController.saveAction();
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
    public String htmlByRichEditor() {
        return HtmlReadTools.body(richEditor.getHtmlText(), false);
    }

}
