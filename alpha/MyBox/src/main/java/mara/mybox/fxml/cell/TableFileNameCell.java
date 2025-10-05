package mara.mybox.fxml.cell;

import java.awt.image.BufferedImage;
import java.io.File;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import mara.mybox.controller.BaseController;
import mara.mybox.fxml.FxTask;
import mara.mybox.image.file.ImageFileReaders;
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
    private BaseController controller;

    public TableFileNameCell(BaseController controller) {
        this.controller = controller;
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
                setText(item);
                String suffix = FileNameTools.ext(item).toLowerCase();
                if (FileExtensions.SupportedImages.contains(suffix)) {
                    ImageViewFileTask task = new ImageViewFileTask(controller)
                            .setCell(this).setView(imageview)
                            .setFilename(item).setThumbWidth(thumbWidth);
                    Thread thread = new Thread(task);
                    thread.setDaemon(false);
                    thread.start();
                }
            }
        };
        return cell;
    }

    public class ImageViewFileTask<Void> extends FxTask<Void> {

        private IndexedCell cell;
        private String filename = null;
        private ImageView view = null;
        private int thumbWidth = AppVariables.thumbnailWidth;

        public ImageViewFileTask(BaseController controller) {
            this.controller = controller;
        }

        public ImageViewFileTask<Void> setCell(IndexedCell cell) {
            this.cell = cell;
            return this;
        }

        public ImageViewFileTask<Void> setFilename(String filename) {
            this.filename = filename;
            return this;
        }

        public ImageViewFileTask<Void> setView(ImageView view) {
            this.view = view;
            return this;
        }

        public ImageViewFileTask<Void> setThumbWidth(int thumbWidth) {
            this.thumbWidth = thumbWidth;
            return this;
        }

        @Override
        public void run() {
            if (view == null || filename == null) {
                return;
            }
            File file = new File(filename);
            BufferedImage image = ImageFileReaders.readImage(this, file, thumbWidth);
            if (image != null) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        view.setImage(SwingFXUtils.toFXImage(image, null));
                        if (cell != null) {
                            cell.setGraphic(view);
                        }
                    }
                });
            }
        }

    }

}
