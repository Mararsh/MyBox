package mara.mybox.fxml.cell;

import java.awt.image.BufferedImage;
import java.io.File;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.IndexedCell;
import javafx.scene.image.ImageView;
import mara.mybox.fxml.FxTask;
import mara.mybox.imagefile.ImageFileReaders;
import mara.mybox.value.AppVariables;

/**
 * @Author Mara
 * @CreateDate 2023-12-9
 * @License Apache License Version 2.0
 */
public class ImageFileCellTask<Void> extends FxTask<Void> {

    private IndexedCell cell;
    private String filename = null;
    private ImageView view = null;
    private int thumbWidth = AppVariables.thumbnailWidth;

    public ImageFileCellTask<Void> setCell(IndexedCell cell) {
        this.cell = cell;
        return this;
    }

    public ImageFileCellTask<Void> setFilename(String filename) {
        this.filename = filename;
        return this;
    }

    public ImageFileCellTask<Void> setView(ImageView view) {
        this.view = view;
        return this;
    }

    public ImageFileCellTask<Void> setThumbWidth(int thumbWidth) {
        this.thumbWidth = thumbWidth;
        return this;
    }

    @Override
    public void run() {
        if (cell == null || view == null || filename == null) {
            return;
        }
        File file = new File(filename);
        BufferedImage image = ImageFileReaders.readImage(this, file, thumbWidth);
        if (image != null) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    view.setImage(SwingFXUtils.toFXImage(image, null));
                    cell.setGraphic(view);
                }
            });
        }
    }

}
