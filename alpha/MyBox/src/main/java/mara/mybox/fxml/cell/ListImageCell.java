package mara.mybox.fxml.cell;

import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * @Author Mara
 * @CreateDate 2019-3-15 14:17:47
 * @License Apache License Version 2.0
 */
public class ListImageCell extends ListCell<Image> {

    private ImageView view;
    private final int height;

    public ListImageCell() {
        height = 30;
        init();
    }

    public ListImageCell(int height) {
        this.height = height;
        init();
    }

    private void init() {
        view = new ImageView();
        view.setPreserveRatio(true);
        view.setFitHeight(height);
    }

    @Override
    protected void updateItem(Image item, boolean empty) {
        super.updateItem(item, empty);
        if (!empty && item != null) {
            view.setImage(item);
            setGraphic(view);
            setText(item.getUrl());
        } else {
            setGraphic(null);
            setText(null);
        }
    }
}
