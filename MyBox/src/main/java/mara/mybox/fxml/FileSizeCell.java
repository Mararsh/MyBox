package mara.mybox.fxml;

import javafx.scene.control.TableCell;
import javafx.scene.text.Text;
import mara.mybox.data.FileInformation;
import mara.mybox.tools.FileTools;

/**
 * @Author Mara
 * @CreateDate 2019-3-15 14:24:30
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class FileSizeCell extends TableCell<FileInformation, Long> {

    private final Text text = new Text();

    public FileSizeCell() {
    }

    @Override
    protected void updateItem(final Long item, boolean empty) {
        super.updateItem(item, empty);
        if (item != null && item > 0) {
            text.setText(FileTools.showFileSize(item));
            setGraphic(text);
        }
    }

}
