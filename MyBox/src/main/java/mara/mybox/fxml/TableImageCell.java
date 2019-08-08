package mara.mybox.fxml;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.image.ImageView;
import javafx.util.Callback;

/**
 * @Author Mara
 * @CreateDate 2019-3-15 14:17:47
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class TableImageCell<T, Image> extends TableCell<T, Image>
        implements Callback<TableColumn<T, Image>, TableCell<T, Image>> {

    @Override
    public TableCell<T, Image> call(TableColumn<T, Image> param) {
        final ImageView imageview = new ImageView();
        imageview.setPreserveRatio(true);
        imageview.setFitWidth(100);
        imageview.setFitHeight(100);
        TableCell<T, Image> cell = new TableCell<T, Image>() {
            @Override
            public void updateItem(Image item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null) {
                    javafx.scene.image.Image image = (javafx.scene.image.Image) item;
                    imageview.setImage(image);
                    setGraphic(imageview);
                }
            }
        };
        return cell;
    }
}
