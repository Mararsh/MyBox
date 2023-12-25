package mara.mybox.fxml.cell;

import mara.mybox.fximage.ImageViewInfoTask;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import mara.mybox.bufferedimage.ImageInformation;

/**
 * @Author Mara
 * @CreateDate 2019-3-15 14:17:47
 * @License Apache License Version 2.0
 */
public class TableImageInfoCell<T> extends TableCell<T, ImageInformation>
        implements Callback<TableColumn<T, ImageInformation>, TableCell<T, ImageInformation>> {

    @Override
    public TableCell<T, ImageInformation> call(TableColumn<T, ImageInformation> param) {
        final ImageView imageview = new ImageView();
        imageview.setPreserveRatio(true);

        TableCell<T, ImageInformation> cell = new TableCell<T, ImageInformation>() {
            @Override
            public void updateItem(ImageInformation item, boolean empty) {
                super.updateItem(item, empty);
                setText(null);
                setGraphic(null);
                if (empty || item == null) {
                    return;
                }
                ImageViewInfoTask task = new ImageViewInfoTask()
                        .setCell(this).setView(imageview).setItem(item);
                Thread thread = new Thread(task);
                thread.setDaemon(false);
                thread.start();
            }
        };
        return cell;
    }

}
