package mara.mybox.fxml.cell;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import mara.mybox.tools.FileNameTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.FileExtensions;

/**
 * @Author Mara
 * @CreateDate 2019-3-15 14:17:47
 * @License Apache License Version 2.0
 */
public class TableFileNameCell<T> extends TableCell<T, String>
        implements Callback<TableColumn<T, String>, TableCell<T, String>> {

    protected int thumbWidth = AppVariables.thumbnailWidth;

    public TableFileNameCell() {

    }

    public TableFileNameCell(int imageSize) {
        this.thumbWidth = imageSize;
    }

    @Override
    public TableCell<T, String> call(TableColumn<T, String> param) {
        final ImageView imageview = new ImageView();
        imageview.setPreserveRatio(true);
        imageview.setFitWidth(thumbWidth);
        imageview.setFitHeight(thumbWidth);
        TableCell<T, String> cell = new TableCell<T, String>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(null);
                setGraphic(null);
                if (empty || item == null) {
                    return;
                }
                String suffix = FileNameTools.suffix(item).toLowerCase();
                if (FileExtensions.SupportedImages.contains(suffix)) {
                    ImageFileCellTask task = new ImageFileCellTask()
                            .setCell(this).setView(imageview)
                            .setFilename(item).setThumbWidth(thumbWidth);
                    Thread thread = new Thread(task);
                    thread.setDaemon(false);
                    thread.start();
                } else {
                    setText(item);
                }
            }
        };
        return cell;
    }

}
