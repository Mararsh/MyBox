package mara.mybox.fxml.cell;

import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import mara.mybox.data.ImageItem;

/**
 * @Author Mara
 * @CreateDate 2020-1-5
 * @License Apache License Version 2.0
 */
public class ListImageItemCell extends ListCell<ImageItem> {

    protected int imageSize = 60;
    private ImageView view;
    private Rectangle rect;

    public ListImageItemCell() {
        init();
    }

    public ListImageItemCell(int imageSize) {
        this.imageSize = imageSize;
        init();
    }

    private void init() {
        view = new ImageView();
        view.setPreserveRatio(true);
        view.setFitHeight(imageSize);
        rect = new Rectangle();
        rect.setWidth(40);
        rect.setHeight(40);
    }

    @Override
    public void updateItem(ImageItem item, boolean empty) {
        super.updateItem(item, empty);
        setText(null);
        if (empty || item == null) {
            setGraphic(null);
            return;
        }
        setGraphic(item.makeNode(imageSize, true));
    }

}
