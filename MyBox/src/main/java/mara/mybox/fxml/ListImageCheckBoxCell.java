package mara.mybox.fxml;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.CheckBox;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import javafx.util.Callback;
import mara.mybox.data.ImageItem;

/**
 * @Author Mara
 * @CreateDate 2020-1-5
 * @License Apache License Version 2.0
 */
public class ListImageCheckBoxCell extends CheckBoxListCell<ImageItem> {

    protected int imageSize = 60;
    private ImageView view;
    private Rectangle rect;

    public ListImageCheckBoxCell() {
        init();
    }

    public ListImageCheckBoxCell(int imageSize) {
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
        Callback<ImageItem, ObservableValue<Boolean>> itemToBoolean
                = (ImageItem item) -> item.getSelected();
        setSelectedStateCallback(itemToBoolean);
    }

    @Override
    public void updateItem(ImageItem item, boolean empty) {
        super.updateItem(item, empty);
        setText(null);
        if (empty || item == null) {
            setGraphic(null);
            return;
        }
        try {
            CheckBox cb = (CheckBox) getGraphic();
            cb.setText(null);
            cb.setGraphic(item.makeNode(imageSize));
        } catch (Exception e) {
            setGraphic(null);
        }
    }

}
