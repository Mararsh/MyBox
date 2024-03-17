package mara.mybox.controller;

import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WebViewTools;
import mara.mybox.fxml.style.HtmlStyles;
import mara.mybox.tools.HtmlReadTools;

/**
 * @Author Mara
 * @CreateDate 2018-7-31
 * @License Apache License Version 2.0
 */
public class ControlNoteEditor extends BaseHtmlFormat {

    protected NoteEditor noteEditor;

    public void setParameters(NoteEditor noteEditor) {
        try {
            this.noteEditor = noteEditor;

            webViewController.linkInNewTab = true;
            webViewController.defaultStyle = HtmlStyles.TableStyle;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected void updateStatus(boolean changed) {
        super.updateStatus(changed);
        if (!isSettingValues && noteEditor != null) {
            noteEditor.valueChanged(changed);
        }
    }

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
        return HtmlReadTools.body(richEditorController.getContents(), false);
    }
}
