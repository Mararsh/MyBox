package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import mara.mybox.data.StringTable;
import mara.mybox.db.data.ColorData;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.image.FxColorTools;
import static mara.mybox.fxml.image.FxColorTools.color2css;
import mara.mybox.image.data.PixelsBlend;
import mara.mybox.image.data.PixelsBlendFactory;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2025-6-7
 * @License Apache License Version 2.0
 */
public class ColorsBlendController extends ColorQueryController {

    protected ColorData colorOverlay, colorBlended;
    protected String separator;

    @FXML
    protected Tab overlayTab, blendTab;
    @FXML
    protected ControlColorInput colorOverlayController;
    @FXML
    protected ControlColorsBlend blendController;

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
            blendController.changeNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    goAction();
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

    public boolean pickColors() {
        try {
            separator = pickSeparator();
            if (separator == null || separator.isEmpty()) {
                popError(message("InvalidParamter") + ": " + message("ValueSeparator"));
                return false;
            }

            colorData = colorController.colorData;
            if (colorData == null || colorData.getRgba() == null) {
                popError(message("SelectToHandle") + ": " + message("BaseColor"));
                return false;
            }
            colorData = new ColorData(colorData.getRgba())
                    .setColorName(blendController.baseAboveCheck.isSelected()
                            ? message("OverlayColor") : message("BaseColor"))
                    .setvSeparator(separator).convert();

            colorOverlay = colorOverlayController.colorData;
            if (colorOverlay == null || colorOverlay.getRgba() == null) {
                popError(message("SelectToHandle") + ": " + message("OverlayColor"));
                return false;
            }
            colorOverlay = new ColorData(colorOverlay.getRgba())
                    .setColorName(blendController.baseAboveCheck.isSelected()
                            ? message("BaseColor") : message("OverlayColor"))
                    .setvSeparator(separator).convert();
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @FXML
    @Override
    public void goAction() {
        try {
            if (!pickColors()) {
                return;
            }
            PixelsBlend blender = blendController.pickValues(-1f);
            if (blender == null) {
                popError(message("SelectToHandle") + ": " + message("BlendMode"));
                return;
            }

            colorBlended = new ColorData(blender.blend(colorOverlay.getColorValue(), colorData.getColorValue()))
                    .setColorName(blender.modeName())
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
            table = blendController.baseAboveCheck.isSelected()
                    ? FxColorTools.colorsTable(table, colorOverlay, colorData, colorBlended)
                    : FxColorTools.colorsTable(table, colorData, colorOverlay, colorBlended);
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

    @FXML
    public void demo() {
        if (!pickColors()) {
            return;
        }
        FxSingletonTask demoTask = new FxSingletonTask<Void>(this) {
            private String html;

            @Override
            protected boolean handle() {
                try {
                    StringTable table = new StringTable(message("BlendColors"));
                    List<String> row = new ArrayList<>();
                    row.addAll(Arrays.asList(message("Color"), message("Name"),
                            message("Weight"), message("BaseImageAboveOverlay"),
                            message("Hue"), message("Saturation"), message("Brightness"),
                            message("RYBAngle"), message("Opacity"),
                            message("RGBA"), message("RGB"),
                            message("sRGB"), message("HSBA"), message("CalculatedCMYK"),
                            "Adobe RGB", "Apple RGB", "ECI RGB",
                            "sRGB Linear", "Adobe RGB Linear", "Apple RGB Linear",
                            "ECI CMYK", "Adobe CMYK Uncoated FOGRA29",
                            "XYZ",
                            "CIE-L*ab", "LCH(ab)", "CIE-L*uv", "LCH(uv)",
                            message("Value")));
                    table.add(row);
                    table.add(colorRow(colorData, -1, false));
                    table.add(colorRow(colorOverlay, -1, false));

                    PixelsBlend blender;
                    PixelsBlend.ImagesBlendMode mode;
                    int v1 = colorOverlay.getColorValue(), v2 = colorData.getColorValue();
                    for (String name : PixelsBlendFactory.blendModes()) {
                        if (!isWorking()) {
                            return false;
                        }
                        mode = PixelsBlendFactory.blendMode(name);
                        blender = PixelsBlendFactory.create(mode).setBlendMode(mode);

                        blender.setWeight(1.0F).setBaseAbove(false);
                        ColorData blended = new ColorData(blender.blend(v1, v2))
                                .setColorName(blender.modeName())
                                .setvSeparator(separator).convert();
                        table.add(colorRow(blended, 1.0f, false));

                        blender.setWeight(0.5F).setBaseAbove(false);
                        blended = new ColorData(blender.blend(v1, v2))
                                .setColorName(blender.modeName())
                                .setvSeparator(separator).convert();
                        table.add(colorRow(blended, 0.5f, false));

                        blender.setWeight(1.0F).setBaseAbove(true);
                        blended = new ColorData(blender.blend(v1, v2))
                                .setColorName(blender.modeName())
                                .setvSeparator(separator).convert();
                        table.add(colorRow(blended, 1.0f, true));

                        blender.setWeight(0.5F).setBaseAbove(true);
                        blended = new ColorData(blender.blend(v1, v2))
                                .setColorName(blender.modeName())
                                .setvSeparator(separator).convert();
                        table.add(colorRow(blended, 0.5f, true));
                    }
                    html = table.html();
                    return html != null;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            protected List<String> colorRow(ColorData color, float weight, boolean above) {
                List<String> row = new ArrayList<>();
                row.add("<DIV style=\"width: 50px;  background-color:"
                        + color2css(color.getColor()) + "; \">&nbsp;&nbsp;&nbsp;</DIV>");
                row.addAll(Arrays.asList(color.getColorName(),
                        weight >= 0 ? weight + "" : "", above ? message("Yes") : "",
                        color.getHue(), color.getSaturation(), color.getBrightness(),
                        color.getRybAngle(), color.getOpacity(),
                        color.getRgba(), color.getRgb(),
                        color.getSrgb(), color.getHsb(), color.getCalculatedCMYK(),
                        color.getAdobeRGB(), color.getAppleRGB(), color.getEciRGB(),
                        color.getSRGBLinear(), color.getAdobeRGBLinear(), color.getAppleRGBLinear(),
                        color.getEciCMYK(), color.getAdobeCMYK(),
                        color.getXyz(),
                        color.getCieLab(), color.getLchab(), color.getCieLuv(), color.getLchuv(),
                        color.getColorValue() + ""));
                return row;
            }

            @Override
            protected void whenSucceeded() {
                HtmlPopController.showHtml(myController, html);
            }

        };
        start(demoTask);
    }

    @Override
    public boolean handleKeyEvent(KeyEvent event) {
        if (overlayTab.isSelected()) {
            if (colorOverlayController.handleKeyEvent(event)) {
                return true;
            }
        } else if (blendTab.isSelected()) {
            if (blendController.handleKeyEvent(event)) {
                return true;
            }
        }
        return super.handleKeyEvent(event);
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
