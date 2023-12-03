package mara.mybox.fxml.cell;

import javafx.application.Platform;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import mara.mybox.bufferedimage.ImageInformation;
import mara.mybox.value.AppVariables;

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
                new Thread() {
                    @Override
                    public void run() {
                        int width = item.getWidth() > AppVariables.thumbnailWidth
                                ? AppVariables.thumbnailWidth : (int) item.getWidth();
                        Image image = item.loadThumbnail(width);
                        if (image != null) {
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    imageview.setImage(image);
                                    imageview.setRotate(item.getThumbnailRotation());
                                    imageview.setFitWidth(width);
                                    setGraphic(imageview);
                                }
                            });
                        }
                    }
                }.start();
            }
        };
        return cell;
    }

}
