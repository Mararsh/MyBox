package mara.mybox.data2d.writer;

import java.util.ArrayList;
import java.util.List;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-1-29
 * @License Apache License Version 2.0
 */
public class ListWriter extends Data2DWriter {

    protected List<List<String>> rows;

    @Override
    public boolean openWriter() {
        try {
            if (!super.openWriter()) {
                return false;
            }
            showInfo(message("Writing") + " " + message("SystemClipboard"));
            rows = new ArrayList<>();
            if (writeHeader && headerNames != null) {
                rows.add(headerNames);
            }
            return true;
        } catch (Exception e) {
            showError(e.toString());
            return false;
        }
    }

    @Override
    public void printTargetRow() {
        if (targetRow == null) {
            return;
        }
        rows.add(targetRow);
    }

    @Override
    public void closeWriter() {
        created = rows != null && !rows.isEmpty();
    }

    @Override
    public void showResult() {
    }

    public List<List<String>> getRows() {
        return rows;
    }

}
