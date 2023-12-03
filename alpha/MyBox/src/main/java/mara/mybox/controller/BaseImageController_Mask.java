package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.image.PixelReader;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Window;
import mara.mybox.data.DoublePoint;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fximage.ImageViewTools;
import mara.mybox.tools.DoubleTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-8-10
 * @License Apache License Version 2.0
 */
public abstract class BaseImageController_Mask extends BaseImageController_Base {

    public void initMaskPane() {
        try {
            if (maskPane == null) {
                return;
            }
            maskPane.prefWidthProperty().bind(imageView.fitWidthProperty());
            maskPane.prefHeightProperty().bind(imageView.fitHeightProperty());

            if (maskPane.getOnMouseClicked() == null) {
                maskPane.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        paneClicked(event);
                    }
                });
            }

            if (maskPane.getOnMouseMoved() == null) {
                maskPane.setOnMouseMoved(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        showXY(event);
                    }
                });
            }

            if (maskPane.getOnMouseDragged() == null) {
                maskPane.setOnMouseDragged(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        showXY(event);
                    }
                });
            }

            if (maskPane.getOnMousePressed() == null) {
                maskPane.setOnMousePressed(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        showXY(event);
                    }
                });
            }

            if (maskPane.getOnMouseReleased() == null) {
                maskPane.setOnMouseReleased(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        showXY(event);
                    }
                });
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        image
     */
    @Override
    public void viewSizeChanged(double change) {
        if (isSettingValues || change < sizeChangeAware
                || imageView == null || imageView.getImage() == null) {
            return;
        }
        refinePane();
    }

    public void setImageChanged(boolean imageChanged) {
        this.imageChanged = imageChanged;
        updateLabelsTitle();
    }

    /*
        values
     */
    public double viewXRatio() {
        return viewWidth() / imageWidth();
    }

    public double viewYRatio() {
        return viewHeight() / imageHeight();
    }

    public double imageXRatio() {
        return imageWidth() / viewWidth();
    }

    public double imageYRatio() {
        return imageHeight() / viewHeight();
    }

    public double maskEventX(MouseEvent event) {
        return event.getX() * imageXRatio();
    }

    public double maskEventY(MouseEvent event) {
        return event.getY() * imageYRatio();
    }

    public double imageOffsetX(MouseEvent event) {
        return (event.getX() - mouseX) * imageXRatio();
    }

    public double imageOffsetY(MouseEvent event) {
        return (event.getY() - mouseY) * imageYRatio();
    }

    public double nodeX(Node node) {
        return node.getLayoutX() * imageXRatio();
    }

    public double nodeY(Node node) {
        return node.getLayoutX() * imageXRatio();
    }

    public double scale(double d) {
        return scale(d, UserConfig.imageScale());
    }

    public double scale(double d, int scale) {
        return DoubleTools.scale(d, scale);
    }

    /*
        event
     */
    @FXML
    public void paneClicked(MouseEvent event) {
        if (imageView.getImage() == null) {
            imageView.setCursor(Cursor.OPEN_HAND);
            return;
        }
        DoublePoint p = ImageViewTools.getImageXY(event, imageView);
        paneClicked(event, p);
        event.consume();
    }

    public void paneClicked(MouseEvent event, DoublePoint p) {
    }

    @FXML
    public void imageClicked(MouseEvent event) {
//        MyBoxLog.debug("imageClicked");
    }

    @FXML
    public DoublePoint showXY(MouseEvent event) {
        if (xyText == null || !xyText.isVisible()) {
            return null;
        }
        if (!isPickingColor && !UserConfig.getBoolean("ImagePopCooridnate", false)) {
            xyText.setText("");
            return null;
        }
        DoublePoint p = ImageViewTools.getImageXY(event, imageView);
        showXY(event, p);
        return p;
    }

    public DoublePoint showXY(MouseEvent event, DoublePoint p) {
        try {
            if (p == null) {
                xyText.setText("");
                return null;
            }
            int x = (int) p.getX();
            int y = (int) p.getY();
            String s = (int) Math.round(x / widthRatio()) + ","
                    + (int) Math.round(y / heightRatio());
            if (x >= 0 && x < imageView.getImage().getWidth()
                    && y >= 0 && y < imageView.getImage().getHeight()) {
                PixelReader pixelReader = imageView.getImage().getPixelReader();
                Color color = pixelReader.getColor(x, y);
                s += "\n" + FxColorTools.colorDisplaySimple(color);
            }
            if (isPickingColor) {
                s = pickingColorTips() + "\n" + s;
            }
            xyText.setText(s);
            xyText.setX(event.getX() + 10);
            xyText.setY(event.getY());
            xyText.setFill(xyColor());
            xyText.setFont(xyFont());
            return p;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    public Color xyColor() {
        return rulerColor();
    }

    public Font xyFont() {
        return new Font(12);
    }

    public String pickingColorTips() {
        return message("PickingColorsNow");
    }

    @FXML
    public void controlPressed(MouseEvent event) {
        scrollPane.setPannable(false);
        mouseX = event.getX();
        mouseY = event.getY();
    }

    /*
        rulers and grid
     */
    public void drawMaskRulers() {
        drawMaskGrid();
        drawMaskRulerX();
        drawMaskRulerY();
    }

    private void drawMaskRulerX() {
        if (maskPane == null || imageView == null || imageView.getImage() == null) {
            return;
        }
        clearMaskRulerX();
        if (UserConfig.getBoolean("ImageRulerXY", false)) {
            Color strokeColor = Color.web(UserConfig.getString("RulerColor", "#FF0000"));
            double imageWidth = imageWidth() / widthRatio();
            double ratio = viewWidth() / imageWidth;
            int step = getRulerStep(imageWidth);
            for (int i = step; i < imageWidth; i += step) {
                double x = i * ratio;
                Line line = new Line(x, 0, x, 8);
                line.setId("MaskRulerX" + i);
                line.setStroke(strokeColor);
                line.setStrokeWidth(1);
                line.setLayoutX(imageView.getLayoutX() + line.getLayoutX());
                line.setLayoutY(imageView.getLayoutY() + line.getLayoutY());
                maskPane.getChildren().add(line);
            }
            String style = " -fx-font-size: 0.8em; ";
            int step10 = step * 10;
            for (int i = step10; i < imageWidth; i += step10) {
                double x = i * ratio;
                Line line = new Line(x, 0, x, 15);
                line.setId("MaskRulerX" + i);
                line.setStroke(strokeColor);
                line.setStrokeWidth(2);
                line.setLayoutX(imageView.getLayoutX() + line.getLayoutX());
                line.setLayoutY(imageView.getLayoutY() + line.getLayoutY());
                maskPane.getChildren().add(line);
                Text text = new Text(i + " ");
                text.setStyle(style);
                text.setFill(strokeColor);
                text.setLayoutX(imageView.getLayoutX() + x - 10);
                text.setLayoutY(imageView.getLayoutY() + 30);
                text.setId("MaskRulerXtext" + i);
                maskPane.getChildren().add(text);
            }
        }
    }

    private void clearMaskRulerX() {
        if (maskPane == null || imageView == null || imageView.getImage() == null) {
            return;
        }
        List<Node> nodes = new ArrayList<>();
        nodes.addAll(maskPane.getChildren());
        for (Node node : nodes) {
            if (node.getId() != null && node.getId().startsWith("MaskRulerX")) {
                maskPane.getChildren().remove(node);
                node = null;
            }
        }
    }

    private void drawMaskRulerY() {
        if (maskPane == null || imageView == null || imageView.getImage() == null) {
            return;
        }
        clearMaskRulerY();
        if (UserConfig.getBoolean("ImageRulerXY", false)) {
            Color strokeColor = Color.web(UserConfig.getString("RulerColor", "#FF0000"));
            double imageHeight = imageHeight() / heightRatio();
            double ratio = viewHeight() / imageHeight;
            int step = getRulerStep(imageHeight);
            for (int j = step; j < imageHeight; j += step) {
                double y = j * ratio;
                Line line = new Line(0, y, 8, y);
                line.setId("MaskRulerY" + j);
                line.setStroke(strokeColor);
                line.setStrokeWidth(1);
                line.setLayoutX(imageView.getLayoutX() + line.getLayoutX());
                line.setLayoutY(imageView.getLayoutY() + line.getLayoutY());
                maskPane.getChildren().add(line);
            }
            String style = " -fx-font-size: 0.8em; ";
            int step10 = step * 10;
            for (int j = step10; j < imageHeight; j += step10) {
                double y = j * ratio;
                Line line = new Line(0, y, 15, y);
                line.setId("MaskRulerY" + j);
                line.setStroke(strokeColor);
                line.setStrokeWidth(2);
                line.setLayoutX(imageView.getLayoutX() + line.getLayoutX());
                line.setLayoutY(imageView.getLayoutY() + line.getLayoutY());
                Text text = new Text(j + " ");
                text.setStyle(style);
                text.setFill(strokeColor);
                text.setLayoutX(imageView.getLayoutX() + 25);
                text.setLayoutY(imageView.getLayoutY() + y + 8);
                text.setId("MaskRulerYtext" + j);
                maskPane.getChildren().addAll(line, text);
            }
        }
    }

    private void clearMaskRulerY() {
        if (maskPane == null || imageView.getImage() == null) {
            return;
        }
        List<Node> nodes = new ArrayList<>();
        nodes.addAll(maskPane.getChildren());
        for (Node node : nodes) {
            if (node.getId() != null && node.getId().startsWith("MaskRulerY")) {
                maskPane.getChildren().remove(node);
                node = null;
            }
        }
    }

    public void drawMaskGrid() {
        drawMaskGridX();
        drawMaskGridY();
    }

    private void drawMaskGridX() {
        if (maskPane == null || imageView == null || imageView.getImage() == null) {
            return;
        }
        clearMaskGridX();
        if (UserConfig.getBoolean("ImageGridLines", false)) {
            Color lineColor = gridColor();
            int lineWidth = UserConfig.getInt("GridLinesWidth", 1);
            lineWidth = lineWidth <= 0 ? 1 : lineWidth;
            double imageWidth = imageWidth() / widthRatio();
            double imageHeight = imageHeight() / heightRatio();
            double wratio = viewWidth() / imageWidth;
            double hratio = viewHeight() / imageHeight;
            int istep = getRulerStep(imageWidth);
            int interval = UserConfig.getInt("GridLinesInterval", -1);
            interval = interval <= 0 ? istep : interval;
            float opacity = 0.1f;
            try {
                opacity = Float.parseFloat(UserConfig.getString("GridLinesOpacity", "0.1"));
            } catch (Exception e) {
            }
            for (int i = interval; i < imageWidth; i += interval) {
                double x = i * wratio;
                Line line = new Line(x, 0, x, imageHeight * hratio);
                line.setId("GridLinesX" + i);
                line.setStroke(lineColor);
                line.setStrokeWidth(lineWidth);
                line.setLayoutX(imageView.getLayoutX() + line.getLayoutX());
                line.setLayoutY(imageView.getLayoutY() + line.getLayoutY());
                line.setOpacity(opacity);
                maskPane.getChildren().add(line);
            }
            int step10 = istep * 10;
            String style = " -fx-font-size: 0.8em; ";
            for (int i = step10; i < imageWidth; i += step10) {
                double x = i * wratio;
                Line line = new Line(x, 0, x, imageHeight * hratio);
                line.setId("GridLinesX" + i);
                line.setStroke(lineColor);
                line.setStrokeWidth(lineWidth + 1);
                line.setLayoutX(imageView.getLayoutX() + line.getLayoutX());
                line.setLayoutY(imageView.getLayoutY() + line.getLayoutY());
                line.setOpacity(opacity);
                maskPane.getChildren().add(line);
                if (!UserConfig.getBoolean("ImageRulerXY", false)) {
                    Text text = new Text(i + " ");
                    text.setStyle(style);
                    text.setFill(lineColor);
                    text.setLayoutX(imageView.getLayoutX() + x - 10);
                    text.setLayoutY(imageView.getLayoutY() + 15);
                    text.setId("GridLinesXtext" + i);
                    maskPane.getChildren().add(text);
                }
            }
        }
    }

    private void drawMaskGridY() {
        if (maskPane == null || imageView == null || imageView.getImage() == null) {
            return;
        }
        clearMaskGridY();
        if (UserConfig.getBoolean("ImageGridLines", false)) {
            Color lineColor = gridColor();
            int lineWidth = UserConfig.getInt("GridLinesWidth", 1);
            double imageWidth = imageWidth() / widthRatio();
            double imageHeight = imageHeight() / heightRatio();
            double wratio = viewWidth() / imageWidth;
            double hratio = viewHeight() / imageHeight;
            int istep = getRulerStep(imageHeight);
            int interval = UserConfig.getInt("GridLinesInterval", -1);
            interval = interval <= 0 ? istep : interval;
            double w = imageWidth * wratio;
            float opacity = 0.1f;
            try {
                opacity = Float.parseFloat(UserConfig.getString("GridLinesOpacity", "0.1"));
            } catch (Exception e) {
            }
            for (int j = interval; j < imageHeight; j += interval) {
                double y = j * hratio;
                Line line = new Line(0, y, w, y);
                line.setId("GridLinesY" + j);
                line.setStroke(lineColor);
                line.setStrokeWidth(lineWidth);
                line.setLayoutX(imageView.getLayoutX() + line.getLayoutX());
                line.setLayoutY(imageView.getLayoutY() + line.getLayoutY());
                line.setOpacity(opacity);
                maskPane.getChildren().add(line);
            }
            String style = " -fx-font-size: 0.8em; ";
            int step10 = istep * 10;
            for (int j = step10; j < imageHeight; j += step10) {
                double y = j * hratio;
                Line line = new Line(0, y, w, y);
                line.setId("GridLinesY" + j);
                line.setStroke(lineColor);
                line.setStrokeWidth(lineWidth + 2);
                line.setLayoutX(imageView.getLayoutX() + line.getLayoutX());
                line.setLayoutY(imageView.getLayoutY() + line.getLayoutY());
                line.setOpacity(opacity);
                maskPane.getChildren().add(line);
                if (!UserConfig.getBoolean("ImageRulerXY", false)) {
                    Text text = new Text(j + " ");
                    text.setStyle(style);
                    text.setFill(lineColor);
                    text.setLayoutX(imageView.getLayoutX());
                    text.setLayoutY(imageView.getLayoutY() + y + 8);
                    text.setId("GridLinesYtext" + j);
                    maskPane.getChildren().add(text);
                }
            }
        }
    }

    private void clearMaskGridX() {
        if (maskPane == null || imageView.getImage() == null) {
            return;
        }
        List<Node> nodes = new ArrayList<>();
        nodes.addAll(maskPane.getChildren());
        for (Node node : nodes) {
            if (node.getId() != null && node.getId().startsWith("GridLinesX")) {
                maskPane.getChildren().remove(node);
                node = null;
            }
        }
    }

    private void clearMaskGridY() {
        if (maskPane == null || imageView.getImage() == null) {
            return;
        }
        List<Node> nodes = new ArrayList<>();
        nodes.addAll(maskPane.getChildren());
        for (Node node : nodes) {
            if (node.getId() != null && node.getId().startsWith("GridLinesY")) {
                maskPane.getChildren().remove(node);
                node = null;
            }
        }
    }

    protected Color rulerColor() {
        return Color.web(UserConfig.getString("RulerColor", "#FF0000"));
    }

    protected Color gridColor() {
        return Color.web(UserConfig.getString("GridLinesColor", Color.LIGHTGRAY.toString()));
    }

    /*
        static
     */
    public static void updateMaskRulerXY() {
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof BaseImageController) {
                try {
                    BaseImageController controller = (BaseImageController) object;
                    controller.drawMaskRulers();
                } catch (Exception e) {
                }
            }
        }
    }

    public static void updateMaskGrid() {
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof BaseImageController) {
                try {
                    BaseImageController controller = (BaseImageController) object;
                    controller.drawMaskGrid();
                } catch (Exception e) {
                }
            }
        }
    }

}
