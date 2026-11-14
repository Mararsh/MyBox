package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Window;
import mara.mybox.data.DoublePoint;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.image.FxColorTools;
import mara.mybox.fxml.image.ImageViewTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.StringTools;
import static mara.mybox.value.AppVariables.sceneFontSize;
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
    @FXML
    public void paneSize() {
        if (imageView == null || imageView.getImage() == null || scrollPane == null) {
            return;
        }
        try {
            ImageViewTools.paneSize(scrollPane, imageView);
            refinePane();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void loadedSize() {
        if (imageView == null || imageView.getImage() == null || scrollPane == null) {
            return;
        }
        try {
            ImageViewTools.imageSize(scrollPane, imageView);
            refinePane();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        status
     */
    public void fitSize() {
        if (scrollPane == null || imageView == null || imageView.getImage() == null) {
            return;
        }
        try {
            if (scrollPane.getHeight() < imageHeight()
                    || scrollPane.getWidth() < imageWidth()) {
                paneSize();
            } else {
                loadedSize();
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void zoomStepChanged() {
        xZoomStep = zoomStep;
        yZoomStep = zoomStep;
    }

    protected void setZoomStep(Image image) {
        if (image == null) {
            return;
        }
        zoomStep = (int) image.getWidth() / 10;
        if (zoomStepSelector != null) {
            zoomStepSelector.setValue(zoomStep + "");
        } else {
            xZoomStep = (int) image.getWidth() / 10;
            yZoomStep = (int) image.getHeight() / 10;
        }
    }

    public void viewSizeChanged(double change) {
        if (isSettingValues || change < sizeChangeAware
                || imageView == null || imageView.getImage() == null) {
            return;
        }
        refinePane();
        notifySize();
    }

    public void paneSizeChanged(double change) {
        viewSizeChanged(change);
    }

    public void refinePane() {
        if (isSettingValues || scrollPane == null || imageView == null || imageView.getImage() == null) {
            return;
        }
        LocateTools.moveCenter(scrollPane, imageView);
        scrollPane.setVvalue(scrollPane.getVmin());
        updateLabelsTitle();
        drawMaskRulersGrids();
    }

    public synchronized void updateLabelsTitle() {
        try {
            updateStageTitle();
            updateImageLabel();
            updateSizeLabel();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void updateStageTitle() {
        try {
            if (getMyStage() == null || !isTopPane) {
                return;
            }
            myStage.setTitle(getBaseTitle() + fileTitle());
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public String fileTitle() {
        try {
            String title;
            if (sourceFile != null) {
                title = " " + sourceFile.getAbsolutePath();
                if (framesNumber > 1 && frameIndex >= 0) {
                    title += " - " + message("Frame") + " " + (frameIndex + 1);
                }
                if (imageInformation != null && imageInformation.isIsScaled()) {
                    title += " - " + message("Scaled");
                }
            } else {
                title = "";
            }
            if (imageChanged) {
                title += "  " + "*";
            }
            return title;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return "";
        }
    }

    public void updateImageLabel() {
        try {
            if (imageLabel == null) {
                return;
            }
            String imageInfo = "", fileInfo = "", loadInfo = "";
            if (sourceFile != null) {
                fileInfo = message("File") + ":" + sourceFile.getAbsolutePath() + "\n"
                        + message("FileSize") + ":" + FileTools.showFileSize(sourceFile.length()) + "\n"
                        + message("ModifyTime") + ":" + DateTools.datetimeToString(sourceFile.lastModified());
            }
            if (framesNumber > 1) {
                imageInfo = message("FramesNumber") + ":" + framesNumber + "\n";
                if (frameIndex >= 0) {
                    imageInfo += message("CurrentFrame") + ":" + (frameIndex + 1) + "\n";
                }
            }
            if (imageInformation != null) {
                imageInfo += message("Format") + ":" + imageInformation.getImageFormat() + "\n"
                        + message("Pixels") + ":" + (int) imageInformation.getWidth() + "x" + (int) imageInformation.getHeight();
                if (imageInformation.isIsScaled()) {
                    imageInfo += "\n" + message("Scaled");
                }
            } else if (imageView != null && imageView.getImage() != null) {
                imageInfo += message("Pixels") + ":" + (int) imageView.getImage().getWidth() + "x" + (int) imageView.getImage().getHeight();
            }
            if (imageView != null && imageView.getImage() != null) {
                loadInfo = message("LoadedSize") + ":"
                        + (int) imageView.getImage().getWidth() + "x" + (int) imageView.getImage().getHeight() + "\n"
                        + message("DisplayedSize") + ":"
                        + (int) imageView.getBoundsInParent().getWidth() + "x" + (int) imageView.getBoundsInParent().getHeight();
            }
            String more = moreDisplayInfo();
            loadInfo += (!loadInfo.isBlank() && !more.isBlank() ? "\n" : "") + more;
            if (imageChanged) {
                loadInfo += "\n" + message("ImageChanged");
            }
            String finalInfo = fileInfo + "\n" + imageInfo + "\n" + loadInfo;
            imageLabel.setText(StringTools.replaceLineBreak(finalInfo));

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void updateSizeLabel() {
        try {
            if (imageView == null || imageView.getImage() == null) {
                return;
            }
            if (borderLine != null) {
                borderLine.setLayoutX(imageView.getLayoutX() - 1);
                borderLine.setLayoutY(imageView.getLayoutY() - 1);
                borderLine.setWidth(viewWidth() + 2);
                borderLine.setHeight(viewHeight() + 2);
            }
            if (sizeText != null) {
                sizeText.setText((int) (imageView.getImage().getWidth()) + "x" + (int) (imageView.getImage().getHeight()));
                sizeText.setTextAlignment(TextAlignment.LEFT);
                if (imageView.getImage().getWidth() >= imageView.getImage().getHeight()) {
                    sizeText.setX(borderLine.getBoundsInParent().getMinX());
                    sizeText.setY(borderLine.getBoundsInParent().getMinY() - sceneFontSize - 1);
                } else {
                    sizeText.setX(borderLine.getBoundsInParent().getMinX() - sizeText.getBoundsInParent().getWidth() - sceneFontSize);
                    sizeText.setY(borderLine.getBoundsInParent().getMaxY() - sceneFontSize - 1);
                }

            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    protected String moreDisplayInfo() {
        return "";
    }

    public void setImageChanged(boolean imageChanged) {
        this.imageChanged = imageChanged;
        updateLabelsTitle();
        if (imageChanged) {
            refinePane();
            notifySize();
        }
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

    protected void popContextMenu(double x, double y) {
        if (imageView == null || imageView.getImage() == null) {
            return;
        }
        MenuImageViewController.imageViewMenu((BaseImageController) this, x, y);
    }

    /*
        rulers and grid
     */
    public void drawMaskRulersGrids() {
        drawMaskGrid();
        drawMaskGrid9();
        drawMaskGridMizi();
        drawMaskRulerX();
        drawMaskRulerY();
    }

    private void drawMaskRulerX() {
        if (maskPane == null || imageView == null || imageView.getImage() == null) {
            return;
        }
        clearMaskNodes("MaskRulerX");
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

    private void drawMaskRulerY() {
        if (maskPane == null || imageView == null || imageView.getImage() == null) {
            return;
        }
        clearMaskNodes("MaskRulerY");
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

    public void drawMaskGrid() {
        drawMaskGridY();
        drawMaskGridX();
    }

    private void drawMaskGridX() {
        if (maskPane == null || imageView == null || imageView.getImage() == null) {
            return;
        }
        clearMaskNodes("GridLinesX");
        if (UserConfig.getBoolean("ImageGridLines", false)) {
            Color lineColor = gridColor();
            int lineWidth = UserConfig.getInt("GridLinesWidth", 1);
            double imageHeight = imageHeight() / heightRatio();
            double hratio = viewHeight() / imageHeight;
            int istep = getRulerStep(imageHeight);
            int interval = UserConfig.getInt("GridLinesInterval", -1);
            interval = interval <= 0 ? istep : interval;
            double width = viewWidth();
            float opacity = 0.1f;
            try {
                opacity = Float.parseFloat(UserConfig.getString("GridLinesOpacity", "0.1"));
            } catch (Exception e) {
            }
            for (int j = interval; j < imageHeight; j += interval) {
                double y = j * hratio;
                Line line = new Line(0, y, width, y);
                line.setId("GridLinesX" + j);
                line.setStroke(lineColor);
                line.setStrokeWidth(lineWidth);
                line.setLayoutX(imageView.getLayoutX() + line.getLayoutX());
                line.setLayoutY(imageView.getLayoutY() + line.getLayoutY());
                line.setOpacity(opacity);
                maskPane.getChildren().add(line);
            }
            String style = " -fx-font-size: 0.8em; ";
            int step10 = istep * 10;
            boolean showText = !UserConfig.getBoolean("ImageRulerXY", false);
            for (int j = step10; j < imageHeight; j += step10) {
                double y = j * hratio;
                Line line = new Line(0, y, width, y);
                line.setId("GridLinesX" + j);
                line.setStroke(lineColor);
                line.setStrokeWidth(lineWidth + 2);
                line.setLayoutX(imageView.getLayoutX() + line.getLayoutX());
                line.setLayoutY(imageView.getLayoutY() + line.getLayoutY());
                line.setOpacity(opacity);
                maskPane.getChildren().add(line);
                if (showText) {
                    Text text = new Text(j + " ");
                    text.setStyle(style);
                    text.setFill(lineColor);
                    text.setLayoutX(imageView.getLayoutX());
                    text.setLayoutY(imageView.getLayoutY() + y + 8);
                    text.setId("GridLinesXtext" + j);
                    maskPane.getChildren().add(text);
                }
            }
        }
    }

    private void drawMaskGridY() {
        if (maskPane == null || imageView == null || imageView.getImage() == null) {
            return;
        }
        clearMaskNodes("GridLinesY");
        if (UserConfig.getBoolean("ImageGridLines", false)) {
            Color lineColor = gridColor();
            int lineWidth = UserConfig.getInt("GridLinesWidth", 1);
            lineWidth = lineWidth <= 0 ? 1 : lineWidth;
            double imageWidth = imageWidth() / widthRatio();
            double wratio = viewWidth() / imageWidth;
            double height = viewHeight();
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
                Line line = new Line(x, 0, x, height);
                line.setId("GridLinesY" + i);
                line.setStroke(lineColor);
                line.setStrokeWidth(lineWidth);
                line.setLayoutX(imageView.getLayoutX() + line.getLayoutX());
                line.setLayoutY(imageView.getLayoutY() + line.getLayoutY());
                line.setOpacity(opacity);
                maskPane.getChildren().add(line);
            }
            int step10 = istep * 10;
            String style = " -fx-font-size: 0.8em; ";
            boolean showText = !UserConfig.getBoolean("ImageRulerXY", false);
            for (int i = step10; i < imageWidth; i += step10) {
                double x = i * wratio;
                Line line = new Line(x, 0, x, height);
                line.setId("GridLinesY" + i);
                line.setStroke(lineColor);
                line.setStrokeWidth(lineWidth + 1);
                line.setLayoutX(imageView.getLayoutX() + line.getLayoutX());
                line.setLayoutY(imageView.getLayoutY() + line.getLayoutY());
                line.setOpacity(opacity);
                maskPane.getChildren().add(line);
                if (showText) {
                    Text text = new Text(i + " ");
                    text.setStyle(style);
                    text.setFill(lineColor);
                    text.setLayoutX(imageView.getLayoutX() + x - 10);
                    text.setLayoutY(imageView.getLayoutY() + 15);
                    text.setId("GridLinesYtext" + i);
                    maskPane.getChildren().add(text);
                }
            }
        }
    }

    private void clearMaskNodes(String prefix) {
        if (maskPane == null || imageView.getImage() == null) {
            return;
        }
        List<Node> nodes = new ArrayList<>();
        nodes.addAll(maskPane.getChildren());
        for (Node node : nodes) {
            if (node != null && node.getId() != null && node.getId().startsWith(prefix)) {
                maskPane.getChildren().remove(node);
                node = null;
            }
        }
    }

    public void drawMaskGrid9() {
        drawMaskGrid9X();
        drawMaskGrid9Y();
    }

    private void drawMaskGrid9X() {
        if (maskPane == null || imageView == null || imageView.getImage() == null) {
            return;
        }
        clearMaskNodes("Grid9X");
        if (UserConfig.getBoolean("ImageGrid9", false)) {
            Color lineColor = grid9Color();
            int lineWidth = 1;
            double width = viewWidth();
            double istep = viewHeight() / 3d;
            float opacity = 0.5f;
            double y = 0;
            for (int j = 1; j < 3; j++) {
                y += istep;
                Line line = new Line(0, y, width, y);
                line.setId("Grid9X" + j);
                line.setStroke(lineColor);
                line.setStrokeWidth(lineWidth);
                line.setLayoutX(imageView.getLayoutX() + line.getLayoutX());
                line.setLayoutY(imageView.getLayoutY() + line.getLayoutY());
                line.setOpacity(opacity);
                maskPane.getChildren().add(line);
            }
        }
    }

    private void drawMaskGrid9Y() {
        if (maskPane == null || imageView == null || imageView.getImage() == null) {
            return;
        }
        clearMaskNodes("Grid9Y");
        if (UserConfig.getBoolean("ImageGrid9", false)) {
            Color color = grid9Color();
            int lineWidth = 1;
            double height = viewHeight();
            double istep = viewWidth() / 3d;
            float opacity = 0.5f;
            double x = 0;
            for (int i = 1; i < 3; i++) {
                x += istep;
                Line line = new Line(x, 0, x, height);
                line.setId("Grid9Y" + i);
                line.setStroke(color);
                line.setStrokeWidth(lineWidth);
                line.setLayoutX(imageView.getLayoutX() + line.getLayoutX());
                line.setLayoutY(imageView.getLayoutY() + line.getLayoutY());
                line.setOpacity(opacity);
                maskPane.getChildren().add(line);
            }
        }
    }

    public void drawMaskGridMizi() {
        if (maskPane == null || imageView == null || imageView.getImage() == null) {
            return;
        }
        clearMaskNodes("GridMizi");
        if (!UserConfig.getBoolean("ImageGridMizi", false)) {
            return;
        }
        Color lineColor = GridMiziColor();
        int lineWidth = 1;
        double width = viewWidth();
        double height = viewHeight();
        float opacity = 0.5f;

        double y = height / 2d;
        Line linex = new Line(0, y, width, y);
        linex.setId("GridMiziX");
        linex.setStroke(lineColor);
        linex.setStrokeWidth(lineWidth);
        linex.setLayoutX(imageView.getLayoutX() + linex.getLayoutX());
        linex.setLayoutY(imageView.getLayoutY() + linex.getLayoutY());
        linex.setOpacity(opacity);
        maskPane.getChildren().add(linex);
        Color color = GridMiziColor();

        double x = width / 2d;
        Line liney = new Line(x, 0, x, height);
        liney.setId("GridMiziY");
        liney.setStroke(color);
        liney.setStrokeWidth(lineWidth);
        liney.setLayoutX(imageView.getLayoutX() + liney.getLayoutX());
        liney.setLayoutY(imageView.getLayoutY() + liney.getLayoutY());
        liney.setOpacity(opacity);
        maskPane.getChildren().add(liney);

        Line lines1 = new Line(0, 0, width, height);
        lines1.setId("GridMiziS1");
        lines1.setStroke(color);
        lines1.setStrokeWidth(lineWidth);
        lines1.setLayoutX(imageView.getLayoutX() + lines1.getLayoutX());
        lines1.setLayoutY(imageView.getLayoutY() + lines1.getLayoutY());
        lines1.setOpacity(opacity);
        maskPane.getChildren().add(lines1);

        Line lines2 = new Line(width, 0, 0, height);
        lines2.setId("GridMiziS2");
        lines2.setStroke(color);
        lines2.setStrokeWidth(lineWidth);
        lines2.setLayoutX(imageView.getLayoutX() + lines2.getLayoutX());
        lines2.setLayoutY(imageView.getLayoutY() + lines2.getLayoutY());
        lines2.setOpacity(opacity);
        maskPane.getChildren().add(lines2);

    }

    protected Color rulerColor() {
        return Color.web(UserConfig.getString("RulerColor", "#FF0000"));
    }

    protected Color gridColor() {
        return Color.web(UserConfig.getString("GridLinesColor", Color.LIGHTGRAY.toString()));
    }

    protected Color grid9Color() {
        return Color.web(UserConfig.getString("Grid9Color", Color.BLUE.toString()));
    }

    protected Color GridMiziColor() {
        return Color.web(UserConfig.getString("GridMiziColor", Color.GREEN.toString()));
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
                    controller.drawMaskRulersGrids();
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

    public static void updateMaskGrid9() {
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof BaseImageController) {
                try {
                    BaseImageController controller = (BaseImageController) object;
                    controller.drawMaskGrid9();
                } catch (Exception e) {
                }
            }
        }
    }

    public static void updateMaskGridMizi() {
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof BaseImageController) {
                try {
                    BaseImageController controller = (BaseImageController) object;
                    controller.drawMaskGridMizi();
                } catch (Exception e) {
                }
            }
        }
    }

}
