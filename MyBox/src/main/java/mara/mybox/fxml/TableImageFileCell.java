package mara.mybox.fxml;

import java.awt.image.BufferedImage;
import java.io.File;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import javax.imageio.ImageIO;

/**
 * @Author Mara
 * @CreateDate 2019-3-15 14:17:47
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class TableImageFileCell<T, String> extends TableCell<T, String>
        implements Callback<TableColumn<T, String>, TableCell<T, String>> {

    protected int imageSize = 100;

    public TableImageFileCell() {

    }

    public TableImageFileCell(int imageSize) {
        this.imageSize = imageSize;
    }

    @Override
    public TableCell<T, String> call(TableColumn<T, String> param) {
        final ImageView imageview = new ImageView();
        imageview.setPreserveRatio(true);
        imageview.setFitWidth(imageSize);
        imageview.setFitHeight(imageSize);
        TableCell<T, String> cell = new TableCell<T, String>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                try {
                    File file = new File(item.toString());
                    BufferedImage image = ImageIO.read(file);
                    imageview.setImage(SwingFXUtils.toFXImage(image, null));
                    setGraphic(imageview);
                } catch (Exception e) {
                    setText(null);
                    setGraphic(null);
                }
            }
        };
        return cell;
    }
}
