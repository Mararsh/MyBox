package mara.mybox.data2d.writer;

import java.util.List;
import mara.mybox.fxml.TextClipboardTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-1-29
 * @License Apache License Version 2.0
 */
public class SystemClipboardWriter extends Data2DWriter {

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
            return true;
        } catch (Exception e) {
            showError(e.toString());
            return false;
        }
    }

    @Override
    public void printTargetRow() {
        appendRow(targetRow);
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
    public void closeWriter() {
        created = builder != null && !builder.isEmpty();
    }

    @Override
    public void showResult() {
        if (controller == null || builder == null || builder.isEmpty()) {
            return;
        }
        TextClipboardTools.copyToSystemClipboard(controller, builder.toString());
    }

}
