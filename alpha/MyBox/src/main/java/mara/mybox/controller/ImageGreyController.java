package mara.mybox.controller;

import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import mara.mybox.bufferedimage.ImageGray;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.bufferedimage.ScaleTools;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-9-2
 * @License Apache License Version 2.0
 */
public class ImageGreyController extends ImageSelectScopeController {

    public ImageGreyController() {
        baseTitle = message("Grey");
    }

    @FXML
    @Override
    public void okAction() {
        if (!checkOptions()) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {
            private ImageScope scope;

            @Override
            protected boolean handle() {
                try {
                    scope = scope();
                    ImageGray imageGray = new ImageGray(editor.imageView.getImage(), scope);
                    imageGray.setExcludeScope(excludeRadio.isSelected())
                            .setSkipTransparent(ignoreTransparentCheck.isSelected());
                    handledImage = imageGray.operateFxImage();
                    return handledImage != null;
                } catch (Exception e) {
                    MyBoxLog.debug(e);
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                popSuccessful();
                editor.updateImage("Grey", null, scope, handledImage, cost);
                if (closeAfterCheck.isSelected()) {
                    close();
                }
            }
        };
        start(task);
    }

    @FXML
    protected void demo() {
        if (scopeController.srcImage() == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {
            private Image demoImage;

            @Override
            protected boolean handle() {
                try {
                    BufferedImage dbf = SwingFXUtils.fromFXImage(scopeController.srcImage(), null);
                    dbf = ScaleTools.scaleImageLess(dbf, 1000000);

                    ImageScope scope = new ImageScope();
                    scope.setScopeType(ImageScope.ScopeType.Rectangle)
                            .setRectangle(DoubleRectangle.xywh(
                                    dbf.getWidth() / 8, dbf.getHeight() / 8,
                                    dbf.getWidth() * 3 / 4, dbf.getHeight() * 3 / 4));

                    ImageGray imageGray = new ImageGray(dbf, scope);
                    demoImage = imageGray.operateFxImage();
                    return demoImage != null;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                ImagePopController.openImage(myController, demoImage);
            }

        };
        start(task);
    }

    @Override
    protected Image makeDemo(BufferedImage dbf, ImageScope scope) {
        try {
            ImageGray imageGray = new ImageGray(dbf, scope);
            return imageGray.operateFxImage();
        } catch (Exception e) {
            displayError(e.toString());
            return null;
        }
    }

    /*
        static methods
     */
    public static ImageGreyController open(BaseImageController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageGreyController controller = (ImageGreyController) WindowTools.openChildStage(
                    parent.getMyWindow(), Fxmls.ImageGreyFxml, false);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
