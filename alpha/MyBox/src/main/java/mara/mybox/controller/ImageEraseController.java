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
public class ImageEraseController extends BaseImageEditController {

    protected int strokeWidth;

    @FXML
    protected ComboBox<String> widthSelector;
    @FXML
    protected Label commentsLabel;
    @FXML
    protected CheckBox coordinatePenCheck;

    public ImageEraseController() {
        baseTitle = message("Eraser");
    }

    @Override
    protected void initMore() {
        try {
            operation = "Eraser";

            widthSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(newValue);
                        if (v >= 0) {
                            strokeWidth = v;
                            UserConfig.setInt(interfaceName + "StrokeWidth", v);
                            if (shapeStyle == null) {
                                shapeStyle = new ShapeStyle(interfaceName);
                            }
                            shapeStyle.setStrokeWidth(strokeWidth);
                            showMaskPolylines();
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
    public boolean afterImageLoaded() {
        try {
            if (!super.afterImageLoaded() || image == null) {
                return false;
            }
            shapeStyle = new ShapeStyle(interfaceName);
            shapeStyle.setStrokeWidth(strokeWidth);
            shapeStyle.setStrokeColor(Color.RED);
            showAnchors = false;
            popAnchorMenu = false;
            addPointWhenClick = false;
            popShapeMenu = false;
            supportPath = false;
            maskPolylinesData = new DoublePolylines();
            commentsLabel.setText(message("ImageEraserComments"));

            widthSelector.getItems().clear();
            List<String> ws = new ArrayList<>();
            ws.addAll(Arrays.asList("3", "0", "1", "2", "5", "8", "10", "15", "25", "30",
                    "50", "80", "100", "150", "200", "300", "500"));
            int max = (int) (image.getWidth() / 20);
            int step = max / 10;
            for (int w = 10; w < max; w += step) {
                if (!ws.contains(w + "")) {
                    ws.add(0, w + "");
                }
            }
            widthSelector.getItems().setAll(ws);
            strokeWidth = UserConfig.getInt(interfaceName + "StrokeWidth", 50);
            if (strokeWidth <= 0) {
                strokeWidth = 50;
            }
            widthSelector.setValue(strokeWidth + "");

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
        showMaskPolylines();
        return true;
    }

    @FXML
    @Override
    public void cancelAction() {
        withdrawAction();
    }

    @FXML
    @Override
    public void clearAction() {
        loadImage(editor.imageView.getImage());

    }

    @Override
    protected void handleImage() {
        handledImage = EliminateTools.drawErase(editor.imageView.getImage(),
                maskPolylinesData, strokeWidth);
    }

    /*
        static methods
     */
    public static ImageEraseController open(ImageEditorController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageEraseController controller = (ImageEraseController) WindowTools.openChildStage(
                    parent.getMyWindow(), Fxmls.ImageEraseFxml, false);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
