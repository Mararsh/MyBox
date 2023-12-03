package mara.mybox.fxml.cell;

import java.awt.image.BufferedImage;
import java.io.File;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import mara.mybox.imagefile.ImageFileReaders;
import mara.mybox.value.AppVariables;

/**
 * @Author Mara
 * @CreateDate 2019-3-15 14:17:47
 * @License Apache License Version 2.0
 */
public class TableImageFileCell<T> extends TableCell<T, String>
        implements Callback<TableColumn<T, String>, TableCell<T, String>> {

    protected int thumbWidth = AppVariables.thumbnailWidth;

    public TableImageFileCell() {

    }

    public TableImageFileCell(int imageSize) {
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
                if (!empty && item != null) {
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                File file = new File(item);
                                BufferedImage image = ImageFileReaders.readImage(file, thumbWidth);
                                if (image != null) {
                                    Platform.runLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            imageview.setImage(SwingFXUtils.toFXImage(image, null));
                                            setGraphic(imageview);
                                        }
                                    });
                                }
                            } catch (Exception e) {
                            }
                        }
                    }.start();
                }
            }
        };
        return cell;
    }
}
