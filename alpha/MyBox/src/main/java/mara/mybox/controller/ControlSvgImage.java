package mara.mybox.controller;

import java.io.File;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxImageTools;
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
        try {
            doc = SvgTools.focus(doc, node, 0.5f);
//            doc = SvgTools.removeSize(doc);
            File tmpFile = SvgTools.docToImage(this, doc, -1, -1, null);
            if (tmpFile != null && tmpFile.exists()) {
                loadImage(FxImageTools.readImage(tmpFile));
                FileDeleteTools.delete(tmpFile);
            } else {
                loadImage(null);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadBackGround() {
        try {
            File tmpFile = svgShapeControl.optionsController.toImage();
            if (tmpFile != null && tmpFile.exists()) {
                loadImage(FxImageTools.readImage(tmpFile));
                FileDeleteTools.delete(tmpFile);
            } else {
                loadImage(null);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected void finalRefineView() {
        paneSize();
    }

}
