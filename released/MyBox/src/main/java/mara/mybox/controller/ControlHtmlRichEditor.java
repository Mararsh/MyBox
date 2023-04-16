package mara.mybox.controller;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.web.HTMLEditor;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-8-18
 * @License Apache License Version 2.0
 */
public class ControlHtmlRichEditor extends BaseController {

    protected final SimpleBooleanProperty textChanged;
    protected String lastText;

    @FXML
    protected HTMLEditor htmlEditor;

    public ControlHtmlRichEditor() {
        textChanged = new SimpleBooleanProperty(false);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            // https://stackoverflow.com/questions/31894239/javafx-htmleditor-how-to-implement-a-changelistener
            // As my testing, only DragEvent.DRAG_EXITED, KeyEvent.KEY_TYPED, KeyEvent.KEY_RELEASED working for HtmlEdior
            htmlEditor.setOnDragExited(new EventHandler<InputEvent>() {
                @Override
                public void handle(InputEvent event) {
                    textChanged();
                }
            });
            htmlEditor.setOnKeyReleased(new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent event) {
                    textChanged();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void textChanged() {
        String currentText = getContents();
        if (lastText == null || lastText.isEmpty()) {
            if (currentText != null && !currentText.isEmpty()) {
                textChanged.set(!textChanged.get());
            }
        } else if (!lastText.equals(currentText)) {
            textChanged.set(!textChanged.get());
        }
        lastText = currentText;
    }

    public void loadContents(String contents) {
        htmlEditor.setHtmlText(contents);
        lastText = getContents();
    }

    public String getContents() {
        return htmlEditor.getHtmlText();
    }

    public void setLabel(String msg) {
        bottomLabel.setText(msg);
    }

}
