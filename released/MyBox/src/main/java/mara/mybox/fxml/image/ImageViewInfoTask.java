package mara.mybox.fxml.image;

import javafx.application.Platform;
import javafx.scene.control.IndexedCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import mara.mybox.controller.BaseController;
import mara.mybox.fxml.FxTask;
import mara.mybox.image.data.ImageInformation;
import mara.mybox.value.AppVariables;

/**
 * @Author Mara
 * @CreateDate 2023-12-9
 * @License Apache License Version 2.0
 */
public class ImageViewInfoTask<Void> extends FxTask<Void> {

    private IndexedCell cell;
    private ImageInformation item = null;
    private ImageView view = null;

    public ImageViewInfoTask(BaseController controller) {
        this.controller = controller;
    }

    public ImageViewInfoTask<Void> setCell(IndexedCell cell) {
        this.cell = cell;
        return this;
    }

    public ImageViewInfoTask<Void> setItem(ImageInformation item) {
        this.item = item;
        return this;
    }

    public ImageViewInfoTask<Void> setView(ImageView view) {
        this.view = view;
        return this;
    }

    @Override
    public void run() {
        if (view == null || item == null) {
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
                    if (cell != null) {
                        cell.setGraphic(view);
                    }
                }
            });
        }
    }

}
