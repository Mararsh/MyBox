package mara.mybox.fxml;

import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * @Author Mara
 * @CreateDate 2019-3-15 14:17:47
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class ListImageNameCell extends ListCell<String> {

    private ImageView view;
    private int height;

    public ListImageNameCell() {
        height = 40;
        init();
    }

    public ListImageNameCell(int height) {
        this.height = height;
        init();
    }

    private void init() {
        view = new ImageView();
        view.setPreserveRatio(true);
        view.setFitHeight(height);
    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (!empty && item != null) {
            view.setImage(new Image(item));
            setGraphic(view);
        } else {
            setGraphic(null);
            setText(null);
        }
    }
}
