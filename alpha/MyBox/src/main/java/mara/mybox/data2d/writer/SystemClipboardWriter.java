package mara.mybox.data2d.writer;

import java.util.List;
import mara.mybox.controller.BaseController;
import mara.mybox.fxml.TextClipboardTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-1-29
 * @License Apache License Version 2.0
 */
public class SystemClipboardWriter extends Data2DWriter {

    protected BaseController controller;
    protected StringBuilder builder;

    @Override
    public boolean openWriter() {
        try {
            if (!super.openWriter()) {
                return false;
            }
            showInfo(message("Writing") + " " + message("SystemClipboard"));
            builder = new StringBuilder();
            if (writeHeader && headerNames != null) {
                appendRow(headerNames);
            }
            status = Status.Openned;
            return true;
        } catch (Exception e) {
            showError(e.toString());
            return false;
        }
    }

    @Override
    public void printTargetRow() {
        appendRow(printRow);
    }

    public void appendRow(List<String> row) {
        if (builder == null || row == null) {
            return;
        }
        String s = null;
        for (String v : row) {
            if (s == null) {
                s = v;
            } else {
                s += "," + v;
            }
        }
        builder.append(s).append("\n");
    }

    @Override
    public void finishWork() {
        if (isFailed() || builder == null) {
            showInfo(message("Failed"));
            status = Status.Failed;
            return;
        }
        if (targetRowIndex == 0 || builder.isEmpty()) {
            showInfo(message("NoData"));
            status = Status.NoData;
            return;
        }
        status = Status.Created;
    }

    @Override
    public boolean showResult() {
        if (builder == null || builder.isEmpty()) {
            return false;
        }
        TextClipboardTools.copyToSystemClipboard(
                controller != null ? controller : operate.getController(),
                builder.toString());
        return true;
    }

    /*
        set
     */
    public SystemClipboardWriter setController(BaseController controller) {
        this.controller = controller;
        return this;
    }

}
