package mara.mybox.controller;

import javafx.fxml.FXML;
import mara.mybox.data.DoublePath;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-8-13
 * @License Apache License Version 2.0
 */
public class ImageSVGPathController extends BaseImageShapeController {

    protected DoublePath initData;

    @FXML
    protected ControlPath2D pathController;

    public ImageSVGPathController() {
        baseTitle = message("SVGPath");
    }

    @Override
    protected void initMore() {
        try {
            super.initMore();
            operation = message("SVGPath");

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setInitData(DoublePath initData) {
        this.initData = initData;
    }

    @Override
    public boolean afterImageLoaded() {
        try {
            if (!super.afterImageLoaded()) {
                return false;
            }
            if (initData != null) {
                loadSvgPath(initData);
                initData = null;
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public void setInputs() {
        if (maskPathData != null) {
            pathController.loadPath(maskPathData.getContent());
        } else {
            pathController.loadPath(null);
        }
    }

    @Override
    public boolean pickShape() {
        try {
            if (!pathController.pickValue()) {
                return false;
            }
            maskPathData.setContent(pathController.getText());
            maskPathData.setSegments(pathController.getSegments());
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public void initShape() {
        try {
            maskPathData = null;
            showMaskPath();

            goAction();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        static methods
     */
    public static ImageSVGPathController open(BaseImageController parent) {
        return loadPath(parent, null);
    }

    public static ImageSVGPathController loadPath(BaseImageController parent, DoublePath pathData) {
        try {
            if (parent == null) {
                return null;
            }
            ImageSVGPathController controller = (ImageSVGPathController) WindowTools.referredStage(
                    parent, Fxmls.ImageSVGPathFxml);
            if (pathData != null) {
                controller.setInitData(pathData);
            }
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
