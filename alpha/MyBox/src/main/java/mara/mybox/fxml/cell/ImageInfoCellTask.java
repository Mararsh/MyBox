package mara.mybox.fxml.cell;

import javafx.application.Platform;
import javafx.scene.control.IndexedCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import mara.mybox.bufferedimage.ImageInformation;
import mara.mybox.fxml.FxTask;
import mara.mybox.value.AppVariables;

/**
 * @Author Mara
 * @CreateDate 2023-12-9
 * @License Apache License Version 2.0
 */
public class ImageInfoCellTask<Void> extends FxTask<Void> {

    private IndexedCell cell;
    private ImageInformation item = null;
    private ImageView view = null;

    public ImageInfoCellTask<Void> setCell(IndexedCell cell) {
        this.cell = cell;
        return this;
    }

    public ImageInfoCellTask<Void> setItem(ImageInformation item) {
        this.item = item;
        return this;
    }

    public ImageInfoCellTask<Void> setView(ImageView view) {
        this.view = view;
        return this;
    }

    @Override
    public void run() {
        if (cell == null || view == null || item == null) {
            return;
        }
        int width = item.getWidth() > AppVariables.thumbnailWidth
                ? AppVariables.thumbnailWidth : (int) item.getWidth();
        Image image = item.loadThumbnail(this, width);
        if (image != null) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    view.setImage(image);
                    view.setRotate(item.getThumbnailRotation());
                    view.setFitWidth(width);
                    cell.setGraphic(view);
                }
            });
        }
    }

}
