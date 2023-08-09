package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.image.PixelReader;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.Window;
import mara.mybox.data.DoublePoint;
import mara.mybox.data.ShapeStyle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fximage.ImageViewTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-8-10
 * @License Apache License Version 2.0
 */
public abstract class BaseImageController_Mask extends BaseImageController_ImageView {

    public void initMaskPane() {
        if (maskPane == null) {
            return;
        }
        try {
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
        values
     */
    public Color strokeColor() {
        try {
            return Color.web(UserConfig.getString("StrokeColor", ShapeStyle.DefaultStrokeColor));
        } catch (Exception e) {
            return Color.web(ShapeStyle.DefaultStrokeColor);
        }
    }

    public float strokeWidth() {
        float v = UserConfig.getFloat("StrokeWidth", 2);
        if (v < 0) {
            v = 2;
        }
        return v;
    }

    public Color anchorColor() {
        try {
            return Color.web(UserConfig.getString("AnchorColor", ShapeStyle.DefaultAnchorColor));
        } catch (Exception e) {
            return Color.web(ShapeStyle.DefaultAnchorColor);
        }
    }

    public float anchorSize() {
        float v = UserConfig.getFloat("AnchorSize", 10);
        if (v < 0) {
            v = 10;
        }
        return v;
    }

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

    @FXML
    public void imageClicked(MouseEvent event) {
//        MyBoxLog.debug("imageClicked");
    }

    public void paneClicked(MouseEvent event, DoublePoint p) {
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
        if (p == null) {
            xyText.setText("");
            return null;
        }
        PixelReader pixelReader = imageView.getImage().getPixelReader();
        Color color = pixelReader.getColor((int) p.getX(), (int) p.getY());
        String s = (int) Math.round(p.getX() / widthRatio()) + ","
                + (int) Math.round(p.getY() / heightRatio()) + "\n"
                + FxColorTools.colorDisplaySimple(color);
        if (isPickingColor) {
            if (this instanceof ImageManufactureScopeController_Base) {
                s = message("PickingColorsForScope") + "\n" + s;
            } else {
                s = message("PickingColorsNow") + "\n" + s;
            }
        }
        xyText.setText(s);
        xyText.setX(event.getX() + 10);
        xyText.setY(event.getY());
        return p;
    }

    protected void initViewControls() {
        try {
            if (rulerXCheck != null) {
                rulerXCheck.setSelected(UserConfig.getBoolean("ImageRulerXY", false));
                rulerXCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        UserConfig.setBoolean("ImageRulerXY", rulerXCheck.isSelected());
                        drawMaskRulers();
                    }
                });
            }
            if (gridCheck != null) {
                gridCheck.setSelected(UserConfig.getBoolean("ImageGridLines", false));
                gridCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        UserConfig.setBoolean("ImageGridLines", gridCheck.isSelected());
                        drawMaskGrid();
                    }
                });
            }

            if (coordinateCheck != null) {
                coordinateCheck.setSelected(UserConfig.getBoolean("ImagePopCooridnate", false));
                coordinateCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        UserConfig.setBoolean("ImagePopCooridnate", coordinateCheck.isSelected());
                        checkCoordinate();
                    }
                });
            }

            if (renderController != null) {
                renderController.setParentController(this);
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

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
            Color lineColor = Color.web(UserConfig.getString("GridLinesColor", Color.LIGHTGRAY.toString()));
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
            Color lineColor = Color.web(UserConfig.getString("GridLinesColor", Color.LIGHTGRAY.toString()));
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

    protected void checkCoordinate() {
        if (xyText != null) {
            xyText.setText("");
            xyText.setFill(strokeColor());
        }
    }


    /*
        static
     */
    public static void updateMaskRulerXY() {
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof BaseImageController_Mask) {
                try {
                    BaseImageController_Mask controller = (BaseImageController_Mask) object;
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
            if (object != null && object instanceof BaseImageController_Mask) {
                try {
                    BaseImageController_Mask controller = (BaseImageController_Mask) object;
                    controller.drawMaskGrid();
                } catch (Exception e) {
                }
            }
        }
    }

}
