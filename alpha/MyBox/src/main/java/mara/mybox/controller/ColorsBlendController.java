package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import mara.mybox.data.StringTable;
import mara.mybox.db.data.ColorData;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.image.FxColorTools;
import mara.mybox.image.data.PixelsBlend;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2025-6-7
 * @License Apache License Version 2.0
 */
public class ColorsBlendController extends ColorQueryController {

    protected ColorData colorOverlay, colorBlended;

    @FXML
    protected Tab overlayTab, blendTab;
    @FXML
    protected ControlColorInput colorOverlayController;
    @FXML
    protected ControlImagesBlend blendController;

    public ColorsBlendController() {
        baseTitle = message("BlendColors");
    }

    @Override
    public void initMore() {
        try {
            colorController.setParameter(baseName + "Base", Color.YELLOW);
            colorController.updateNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    goAction();
                }
            });

            colorOverlayController.setParameter(baseName + "Overlay", Color.SKYBLUE);
            colorOverlayController.updateNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    goAction();
                }
            });

            blendController.setParameters(this);
            blendController.modeList.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (event.getClickCount() > 1) {
                        goAction();
                    }
                }
            });

            goAction();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void okAction() {
        goAction();
    }

    @FXML
    @Override
    public void goAction() {
        try {
            PixelsBlend blender = blendController.pickValues(-1f);
            if (blender == null) {
                popError(message("SelectToHandle") + ": " + message("BlendMode"));
                return;
            }
            String separator = pickSeparator();
            if (separator == null || separator.isEmpty()) {
                popError(message("InvalidParamter") + ": " + message("ValueSeparator"));
                return;
            }

            colorData = colorController.colorData;
            if (colorData == null || colorData.getRgba() == null) {
                popError(message("SelectToHandle") + ": " + message("BaseColor"));
                return;
            }
            colorData = new ColorData(colorData.getRgba()).setvSeparator(separator).convert();

            colorOverlay = colorOverlayController.colorData;
            if (colorOverlay == null || colorOverlay.getRgba() == null) {
                popError(message("SelectToHandle") + ": " + message("OverlayColor"));
                return;
            }
            colorOverlay = new ColorData(colorOverlay.getRgba()).setvSeparator(separator).convert();

            colorBlended = new ColorData(blender.blend(colorOverlay.getColorValue(), colorData.getColorValue()))
                    .setvSeparator(separator).convert();

            String html = "<html><body contenteditable=\"false\">\n"
                    + "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                    + "<svg xmlns=\"http://www.w3.org/2000/svg\" height=\"400\" width=\"600\">\n"
                    + "    <circle cx=\"200\" cy=\"200\" fill=\"" + colorData.css() + "\" r=\"198\"/>\n"
                    + "    <circle cx=\"400\" cy=\"200\" fill=\"" + colorOverlay.css()
                    + "\" r=\"198\"/>\n"
                    + "    <path d=\"M 299.50,372.34 A 199.00 199.00 0 0 0 300 28 A 199.00 199.00 0 0 0 300 372\" "
                    + "  fill=\"" + colorBlended.css() + "\" />\n"
                    + "</svg>\n";

            StringTable table = new StringTable();
            List<String> row = new ArrayList<>();
            row.addAll(Arrays.asList(message("BlendMode"), blender.modeName()));
            table.add(row);
            row = new ArrayList<>();
            row.addAll(Arrays.asList(message("Weight2"), blender.getWeight() + ""));
            table.add(row);
            html += table.div();

            List<String> names = new ArrayList<>();
            names.addAll(Arrays.asList(message("Data"), message("Base"),
                    message("Overlay"), message("BlendColors")));
            table = new StringTable(names);
            table = FxColorTools.colorsTable(table, colorData, colorOverlay, colorBlended);
            html += table.div();

            html += "</body></html>";
            htmlController.displayHtml(html);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void addColor() {
        if (colorBlended == null) {
            return;
        }
        ColorsManageController.addOneColor(colorBlended.getColor());
    }

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (overlayTab.isSelected()) {
            if (colorOverlayController.keyEventsFilter(event)) {
                return true;
            }
        } else if (blendTab.isSelected()) {
            if (blendController.keyEventsFilter(event)) {
                return true;
            }
        }
        return super.keyEventsFilter(event);
    }

    /*
        static
     */
    public static ColorsBlendController open() {
        try {
            ColorsBlendController controller = (ColorsBlendController) WindowTools.openStage(Fxmls.ColorsBlendFxml);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
