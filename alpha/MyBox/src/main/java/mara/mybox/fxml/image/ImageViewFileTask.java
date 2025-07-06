package mara.mybox.fxml.image;

import java.awt.image.BufferedImage;
import java.io.File;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.IndexedCell;
import javafx.scene.image.ImageView;
import mara.mybox.controller.BaseController;
import mara.mybox.fxml.FxTask;
import mara.mybox.image.file.ImageFileReaders;
import mara.mybox.value.AppVariables;

/**
 * @Author Mara
 * @CreateDate 2023-12-9
 * @License Apache License Version 2.0
 */
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
