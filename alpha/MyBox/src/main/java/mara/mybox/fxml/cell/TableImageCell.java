package mara.mybox.fxml.cell;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import mara.mybox.value.AppVariables;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-3-15 14:17:47
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class TableImageCell<T> extends TableCell<T, Image>
        implements Callback<TableColumn<T, Image>, TableCell<T, Image>> {

    protected int thumbWidth = UserConfig.getUserConfigInt("ThumbnailWidth", 100);

    @Override
    public TableCell<T, Image> call(TableColumn<T, Image> param) {
        final ImageView imageview = new ImageView();
        imageview.setPreserveRatio(true);
        imageview.setFitWidth(thumbWidth);
        imageview.setFitHeight(thumbWidth);
        TableCell<T, Image> cell = new TableCell<T, Image>() {
            @Override
            public void updateItem(Image item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                imageview.setImage(item);
                setGraphic(imageview);
            }
        };
        return cell;
    }
}
