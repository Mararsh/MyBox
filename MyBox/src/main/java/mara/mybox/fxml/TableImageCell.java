package mara.mybox.fxml;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import mara.mybox.image.ImageInformation;

/**
 * @Author Mara
 * @CreateDate 2019-3-15 14:17:47
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class TableImageCell<T> extends TableCell<T, ImageInformation>
        implements Callback<TableColumn<T, ImageInformation>, TableCell<T, ImageInformation>> {

    @Override
    public TableCell<T, ImageInformation> call(TableColumn<T, ImageInformation> param) {
        final ImageView imageview = new ImageView();
        imageview.setPreserveRatio(true);
        imageview.setFitWidth(100);
        imageview.setFitHeight(100);
        TableCell<T, ImageInformation> cell = new TableCell<T, ImageInformation>() {
            @Override
            public void updateItem(ImageInformation item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                imageview.setImage(item.loadThumbnail());
                imageview.setRotate(item.getThumbnailRotation());
                setGraphic(imageview);
            }
        };
        return cell;
    }
}
