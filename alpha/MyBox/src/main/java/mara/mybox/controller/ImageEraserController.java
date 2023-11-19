package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import mara.mybox.data.DoublePolylines;
import mara.mybox.data.ShapeStyle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.EliminateTools;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-8-13
 * @License Apache License Version 2.0
 */
public class ImageEraserController extends BaseImageEditController {

    protected int strokeWidth;

    @FXML
    protected ControlColorSet colorController;
    @FXML
    protected ComboBox<String> widthSelector;
    @FXML
    protected Label commentsLabel;
    @FXML
    protected CheckBox coordinatePenCheck;

    public ImageEraserController() {
        baseTitle = message("Eraser");
    }

    @Override
    protected void initMore() {
        try {
            operation = "Eraser";

            colorController.init(this, baseName + "Color", Color.RED);
            colorController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue v, Paint ov, Paint nv) {
                    if (isSettingValues) {
                        return;
                    }
                    if (shapeStyle == null) {
                        shapeStyle = new ShapeStyle(baseName);
                        shapeStyle.setStrokeWidth(strokeWidth);
                    }
                    shapeStyle.setStrokeColor(colorController.color());
                    drawMaskPolylines();
                }
            });

            widthSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue o, String ov, String nv) {
                    try {
                        if (isSettingValues) {
                            return;
                        }
                        int v = Integer.parseInt(nv);
                        if (v > 0) {
                            strokeWidth = v;
                            UserConfig.setInt(baseName + "StrokeWidth", v);
                            if (shapeStyle == null) {
                                shapeStyle = new ShapeStyle(baseName);
                                shapeStyle.setStrokeColor(colorController.color());
                            }
                            shapeStyle.setStrokeWidth(strokeWidth);
                            drawMaskPolylines();
                            ValidationTools.setEditorNormal(widthSelector);
                        } else {
                            ValidationTools.setEditorBadStyle(widthSelector);
                        }
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(widthSelector);
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    @Override
    public void maskShapeDataChanged() {
        drawMaskPolylines();
    }

    @Override
    public boolean afterImageLoaded() {
        try {
            if (!super.afterImageLoaded() || image == null) {
                return false;
            }
            strokeWidth = UserConfig.getInt(baseName + "StrokeWidth", 50);
            if (strokeWidth <= 0) {
                strokeWidth = 50;
            }
            shapeStyle = new ShapeStyle(baseName);
            shapeStyle.setStrokeWidth(strokeWidth);
            shapeStyle.setStrokeColor(colorController.color());
            showAnchors = false;
            popItemMenu = popAnchorMenuCheck.isSelected();
            addPointWhenClick = false;
            popShapeMenu = false;
            supportPath = false;
            maskPolylinesData = new DoublePolylines();

            List<String> ws = new ArrayList<>();
            ws.addAll(Arrays.asList("3", "1", "2", "5", "8", "10", "15", "25", "30",
                    "50", "80", "100", "150", "200", "300", "500"));
            int max = (int) (image.getWidth() / 20);
            int step = max / 10;
            for (int w = 10; w < max; w += step) {
                if (!ws.contains(w + "")) {
                    ws.add(0, w + "");
                }
            }

            if (!ws.contains(strokeWidth + "")) {
                ws.add(0, strokeWidth + "");
            }
            isSettingValues = true;
            widthSelector.getItems().setAll(ws);
            widthSelector.setValue(strokeWidth + "");
            isSettingValues = false;

            showMaskPolylines();

            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    @FXML
    @Override
    public boolean withdrawAction() {
        if (imageView == null || imageView.getImage() == null) {
            return false;
        }
        maskPolylinesData.removeLastLine();
        drawMaskPolylines();
        return true;
    }

    @FXML
    @Override
    public void clearAction() {
        loadImage(srcImage());

    }

    @Override
    protected void handleImage() {
        handledImage = EliminateTools.drawErase(srcImage(), maskPolylinesData, strokeWidth);
    }

    /*
        static methods
     */
    public static ImageEraserController open(ImageEditorController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageEraserController controller = (ImageEraserController) WindowTools.openChildStage(
                    parent.getMyWindow(), Fxmls.ImageEraserFxml, false);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
