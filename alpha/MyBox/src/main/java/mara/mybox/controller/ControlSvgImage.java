package mara.mybox.controller;

import java.io.File;
import javafx.scene.image.Image;
import mara.mybox.fximage.FxImageTools;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.SvgTools;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @Author Mara
 * @CreateDate 2023-7-5
 * @License Apache License Version 2.0
 */
public class ControlSvgImage extends BaseShapeController {

    protected ControlSvgShape svgShapeControl;

    public void loadDoc(Document doc, Element node) {
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            private Image image;

            @Override
            protected boolean handle() {
                try {
                    Document fdoc = SvgTools.focus(this, doc, node, 0.5f);
                    if (fdoc == null || !isWorking()) {
                        return false;
                    }
//            doc = SvgTools.removeSize(doc);
                    File tmpFile = SvgTools.docToImage(this, myController, fdoc, -1, -1, null);
                    if (tmpFile == null || !isWorking()) {
                        return false;
                    }
                    if (tmpFile.exists()) {
                        image = FxImageTools.readImage(this, tmpFile);
                        FileDeleteTools.delete(tmpFile);
                    } else {
                        image = null;
                    }
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                loadImage(image);
            }

        };
        start(task);

    }

    public void loadBackGround() {
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            private Image image;

            @Override
            protected boolean handle() {
                try {
                    image = null;
                    File tmpFile = svgShapeControl.optionsController.toImage(this);
                    if (tmpFile != null && tmpFile.exists()) {
                        image = FxImageTools.readImage(this, tmpFile);
                        FileDeleteTools.delete(tmpFile);
                    }
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                loadImage(image);
            }

        };
        start(task);
    }

}
