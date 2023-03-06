package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-2-12
 * @License Apache License Version 2.0
 */
public class SVGEditorController extends BaseController {

    @FXML
    protected TextArea docArea;
    @FXML
    protected ControlWebView webViewController;

    public SVGEditorController() {
        baseTitle = message("SVGEditor");
        TipsLabelKey = "SVGEditorTips";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

//            Document doc = SvgTools.document();
//            SVGGraphics2D g = new SVGGraphics2D(doc);
//
//            Shape circle = new Ellipse2D.Double(0, 0, 50, 50);
//            g.setPaint(Color.red);
//            g.fill(circle);
//            g.translate(60, 0);
//            g.setPaint(Color.green);
//            g.fill(circle);
//            g.translate(60, 0);
//            g.setPaint(Color.blue);
//            g.fill(circle);
//            g.setSVGCanvasSize(new Dimension(180, 50));
//
//            String svg = SvgTools.toText(g);
//            webViewController.loadContents(svg);
//            docArea.setText(svg);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public static SVGEditorController open(BaseController parent, String file) {
        try {
            SVGEditorController controller = (SVGEditorController) WindowTools.openStage(Fxmls.SVGEditorFxml);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
