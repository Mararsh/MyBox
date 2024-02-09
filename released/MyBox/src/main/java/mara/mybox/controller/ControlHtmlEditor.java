package mara.mybox.controller;

import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2018-7-31
 * @License Apache License Version 2.0
 */
public class ControlHtmlEditor extends BaseHtmlFormat {

    protected HtmlEditorController htmlEditor;

    public void setParameters(HtmlEditorController htmlEditor) {
        try {
            this.htmlEditor = htmlEditor;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean writePanes(String html) {
        super.writePanes(html);
        if (htmlEditor != null) {
            htmlEditor.sourceFile = sourceFile;
        }
        return true;
    }

    @Override
    protected void updateStatus(boolean changed) {
        super.updateStatus(changed);
        if (getMyStage() == null || htmlEditor == null) {
            return;
        }
        htmlEditor.updateStatus(changed);
    }

}
