package mara.mybox.fxml;

import javafx.scene.shape.Rectangle;
import javafx.stage.Popup;

/**
 * @Author Mara
 * @CreateDate 2020-09-04
 * @License Apache License Version 2.0
 */
public class ColorPalette extends Popup {

    protected Rectangle rect;

    public ColorPalette(Rectangle rect) {
        this.rect = rect;
        init();
    }

    private void init() {

    }

}
