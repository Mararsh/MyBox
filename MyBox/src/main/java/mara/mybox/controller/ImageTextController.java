package mara.mybox.controller;

import java.util.List;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.input.DragEvent;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.web.HTMLEditor;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2018-07-24
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageTextController extends BaseController {

    protected ImageManufactureRichTextController parent;

    protected int lastHtmlLen, lastCodesLen, snapHeight, snapCount;
    protected List<Image> snaps;
    protected Image finalImage;

    @FXML
    protected HTMLEditor htmlEditor;

    @FXML
    protected HBox barBox, closeBox;

    public ImageTextController() {
        baseTitle = AppVariables.message("Texts");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            htmlEditor.addEventHandler(DragEvent.DRAG_EXITED, new EventHandler<InputEvent>() { // work
                @Override
                public void handle(InputEvent event) {
                    checkHtmlEditorChanged();
                }
            });
            htmlEditor.setOnKeyReleased(new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent event) {
                    checkHtmlEditorChanged();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    private void checkHtmlEditorChanged() {
        if (parent == null || parent.webView == null || !parent.webView.isVisible()) {
            return;
        }
        parent.webEngine.loadContent​(htmlEditor.getHtmlText());
    }

    public void init(ImageManufactureRichTextController parent) {
        this.parent = parent;

        Object contents = FxmlControl.getHtml(parent.webEngine);
        if (contents != null) {
            htmlEditor.setHtmlText((String) contents);
        } else {
            htmlEditor.setHtmlText(message("ImageTextComments"));
        }

    }

    @FXML
    public void previewAction() {
//        if (paletteController == null || !paletteController.getMyStage().isShowing()) {
//            paletteController = (ColorPaletteController) openStage(CommonValues.ColorPaletteFxml);
//        }

    }

    @FXML
    @Override
    public void okAction() {
        if (parent == null || parent.webView == null || !parent.webView.isVisible()) {
            return;
        }
        parent.webEngine.loadContent​(htmlEditor.getHtmlText());
        if (saveCloseCheck.isSelected()) {
            this.closeStage();
        }
    }

}
