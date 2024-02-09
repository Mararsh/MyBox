package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import mara.mybox.bufferedimage.ImageInformation;
import mara.mybox.data.DoublePoint;
import mara.mybox.data.DoubleShape;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.IntTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2018-8-8
 * @License Apache License Version 2.0
 */
public class ImageSplitController extends BaseShapeController {

    protected List<ImageInformation> imageInfos;
    protected List<Integer> rows, cols;
    protected int rowsNumber, colsNumber, width, height;
    protected SimpleBooleanProperty splitValid;
    protected SplitMethod splitMethod;

    public static enum SplitMethod {
        Predefined, ByNumber, BySize, Customize
    }

    @FXML
    protected ToggleGroup splitGroup;
    @FXML
    protected RadioButton predefinedRadio, sizeRadio, numbersRadio, customizeRadio;
    @FXML
    protected FlowPane splitPredefinedPane, splitSizePane, splitNumberPane,
            splitCustomized1Pane, splitCustomized2Pane;
    @FXML
    protected TextField rowsInput, colsInput, customizedRowsInput, customizedColsInput,
            widthInput, heightInput;
    @FXML
    protected CheckBox displaySizeCheck;
    @FXML
    protected VBox splitOptionsBox, splitCustomizeBox;
    @FXML
    protected Label promptLabel, sizeLabel;

    public ImageSplitController() {
        baseTitle = message("ImageSplit");
        TipsLabelKey = "ImageSplitTips";
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            imageInfos = new ArrayList<>();
            splitValid = new SimpleBooleanProperty(false);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            displaySizeCheck.setSelected(UserConfig.getBoolean(baseName + "DisplaySize", true));
            displaySizeCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                    UserConfig.setBoolean(baseName + "DisplaySize", displaySizeCheck.isSelected());
                    indicateSplit();
                }
            });

            splitGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {
                    checkSplitMethod();
                }
            });

            checkSplitMethod();

            rightPane.disableProperty().bind(imageView.imageProperty().isNull());

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean afterImageLoaded() {
        try {
            imageInfos.clear();
            if (!super.afterImageLoaded()) {
                return false;
            }
            cols = new ArrayList<>();
            rows = new ArrayList<>();
            splitValid.set(false);
            clearCols();
            clearRows();

            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    protected void checkSplitMethod() {
        try {
            initSplit();

            splitOptionsBox.getChildren().clear();
            if (predefinedRadio.isSelected()) {
                splitMethod = SplitMethod.Predefined;
                splitOptionsBox.getChildren().addAll(splitPredefinedPane);
                promptLabel.setText("");

            } else if (customizeRadio.isSelected()) {
                splitMethod = SplitMethod.Customize;
                splitOptionsBox.getChildren().addAll(splitCustomizeBox, goButton, promptLabel);
                promptLabel.setText(message("SplitImageCustom"));

            } else if (numbersRadio.isSelected()) {
                splitMethod = SplitMethod.ByNumber;
                splitOptionsBox.getChildren().addAll(splitNumberPane, goButton);
                promptLabel.setText("");
                rowsInput.setText("3");
                colsInput.setText("3");

            } else if (sizeRadio.isSelected()) {
                splitMethod = SplitMethod.BySize;
                splitOptionsBox.getChildren().addAll(splitSizePane, goButton, promptLabel);
                promptLabel.setText(message("SplitImageSize"));
                widthInput.setText((int) (imageWidth() / (widthRatio() * 3)) + "");
                heightInput.setText((int) (imageHeight() / (heightRatio() * 3)) + "");

            }

            refreshStyle(splitOptionsBox);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        predeined
     */
    @FXML
    protected void do42Action(ActionEvent event) {
        divideImageByNumber(4, 2);
    }

    @FXML
    protected void do24Action(ActionEvent event) {
        divideImageByNumber(2, 4);
    }

    @FXML
    protected void do41Action(ActionEvent event) {
        divideImageByNumber(4, 1);
    }

    @FXML
    protected void do14Action(ActionEvent event) {
        divideImageByNumber(1, 4);
    }

    @FXML
    protected void do43Action(ActionEvent event) {
        divideImageByNumber(4, 3);
    }

    @FXML
    protected void do34Action(ActionEvent event) {
        divideImageByNumber(3, 4);
    }

    @FXML
    protected void do44Action(ActionEvent event) {
        divideImageByNumber(4, 4);
    }

    @FXML
    protected void do13Action(ActionEvent event) {
        divideImageByNumber(1, 3);
    }

    @FXML
    protected void do31Action(ActionEvent event) {
        divideImageByNumber(3, 1);
    }

    @FXML
    protected void do12Action(ActionEvent event) {
        divideImageByNumber(1, 2);
    }

    @FXML
    protected void do21Action(ActionEvent event) {
        divideImageByNumber(2, 1);
    }

    @FXML
    protected void do32Action(ActionEvent event) {
        divideImageByNumber(3, 2);
    }

    @FXML
    protected void do23Action(ActionEvent event) {
        divideImageByNumber(2, 3);
    }

    @FXML
    protected void do22Action(ActionEvent event) {
        divideImageByNumber(2, 2);
    }

    @FXML
    protected void do33Action(ActionEvent event) {
        divideImageByNumber(3, 3);

    }

    /*
        by size
     */
    protected void pickSize() {
        try {
            int v = Integer.parseInt(widthInput.getText());
            if (v > 0 && v < operationWidth()) {
                widthInput.setStyle(null);
                width = v;
            } else {
                widthInput.setStyle(UserConfig.badStyle());
                popError(message("InvalidParameter") + ": " + message("Width"));
                return;
            }
        } catch (Exception e) {
            widthInput.setStyle(UserConfig.badStyle());
            popError(message("InvalidParameter") + ": " + message("Width"));
            return;
        }
        try {
            int v = Integer.parseInt(heightInput.getText());
            if (v > 0 && v < operationHeight()) {
                heightInput.setStyle(null);
                height = v;
            } else {
                heightInput.setStyle(UserConfig.badStyle());
                popError(message("InvalidParameter") + ": " + message("Height"));
                return;
            }
        } catch (Exception e) {
            heightInput.setStyle(UserConfig.badStyle());
            popError(message("InvalidParameter") + ": " + message("Height"));
            return;
        }
        divideImageBySize();
    }

    protected void divideImageBySize() {
        if (width <= 0 || height <= 0) {
            return;
        }
        try {
            cols = new ArrayList<>();
            cols.add(0);
            int v = width;
            while (v < operationWidth()) {
                cols.add(v);
                v += width;
            }
            cols.add(operationWidth());

            rows = new ArrayList<>();
            rows.add(0);
            v = height;
            while (v < operationHeight()) {
                rows.add(v);
                v += height;
            }
            rows.add(operationHeight());

            indicateSplit();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        by number
     */
    protected void divideImageByNumber(int rows, int cols) {
        try {
            rowsInput.setText(rows + "");
            colsInput.setText(cols + "");
            pickNumbers();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void pickNumbers() {
        if (checkNumberValues()) {
            divideImageByNumber();
        }
    }

    protected boolean checkNumberValues() {
        try {
            int v = Integer.parseInt(rowsInput.getText());
            if (v > 0) {
                rowsNumber = v;
                rowsInput.setStyle(null);
            } else {
                rowsInput.setStyle(UserConfig.badStyle());
                popError(message("InvalidParameter") + ": " + message("RowsNumber"));
                return false;
            }
        } catch (Exception e) {
            rowsInput.setStyle(UserConfig.badStyle());
            popError(message("InvalidParameter") + ": " + message("RowsNumber"));
            return false;
        }
        try {
            int v = Integer.parseInt(colsInput.getText());
            if (v > 0) {
                colsNumber = v;
                colsInput.setStyle(null);
            } else {
                colsInput.setStyle(UserConfig.badStyle());
                popError(message("InvalidParameter") + ": " + message("ColumnsNumber"));
                return false;
            }
        } catch (Exception e) {
            colsInput.setStyle(UserConfig.badStyle());
            popError(message("InvalidParameter") + ": " + message("ColumnsNumber"));
            return false;
        }
        return true;
    }

    protected void divideImageByNumber() {
        if (rowsNumber <= 0 || colsNumber <= 0) {
            return;
        }
        try {
            cols = new ArrayList<>();
            cols.add(0);
            int w = (int) operationWidth();
            for (int i = 1; i < colsNumber; ++i) {
                int v = i * w / colsNumber;
                cols.add(v);
            }
            cols.add(w);
            rows = new ArrayList<>();
            rows.add(0);
            int h = (int) operationHeight();
            for (int i = 1; i < rowsNumber; ++i) {
                int v = i * h / rowsNumber;
                rows.add(v);
            }
            rows.add(h);
            indicateSplit();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }


    /*
        customize
     */
    protected void pickCustomize() {
        try {
            boolean isValidRows = true, isValidcols = true;
            rows = new ArrayList<>();
            rows.add(0);
            rows.add(operationHeight());
            cols = new ArrayList<>();
            cols.add(0);
            cols.add(operationWidth());
            customizedRowsInput.setStyle(null);
            customizedColsInput.setStyle(null);

            if (!customizedRowsInput.getText().isEmpty()) {
                String[] rowStrings = customizedRowsInput.getText().split(",");
                for (String row : rowStrings) {
                    try {
                        int value = Integer.parseInt(row.trim());
                        if (value < 0 || value > operationHeight() - 1) {
                            customizedRowsInput.setStyle(UserConfig.badStyle());
                            isValidRows = false;
                            break;
                        }
                        if (!rows.contains(value)) {
                            rows.add(value);
                        }
                    } catch (Exception e) {
                        customizedRowsInput.setStyle(UserConfig.badStyle());
                        isValidRows = false;
                    }
                }
            }

            if (!customizedColsInput.getText().isEmpty()) {
                String[] colStrings = customizedColsInput.getText().split(",");
                for (String col : colStrings) {
                    try {
                        int value = Integer.parseInt(col.trim());
                        if (value <= 0 || value >= operationWidth() - 1) {
                            customizedColsInput.setStyle(UserConfig.badStyle());
                            isValidcols = false;
                            break;
                        }
                        if (!cols.contains(value)) {
                            cols.add(value);
                        }
                    } catch (Exception e) {
                        customizedColsInput.setStyle(UserConfig.badStyle());
                        isValidcols = false;
                        break;
                    }
                }
            }

            if (!isValidRows) {
                popError(message("InvalidParameter") + ": " + message("SplittingRows"));
            }
            if (!isValidcols) {
                popError(message("InvalidParameter") + ": " + message("SplittingColumns"));
            }

            if (isValidRows && isValidcols) {
                indicateSplit();
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    protected void clearRows() {
        customizedRowsInput.setText("");
    }

    @FXML
    protected void clearCols() {
        customizedColsInput.setText("");
    }

    /*
        handle
     */
    @FXML
    @Override
    public void goAction() {
        try {
            switch (splitMethod) {
                case ByNumber:
                    pickNumbers();
                    break;
                case BySize:
                    pickSize();
                    break;
                case Customize:
                    pickCustomize();
                    break;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    protected void initSplit() {
        try {
            List<Node> nodes = new ArrayList<>();
            nodes.addAll(maskPane.getChildren());
            for (Node node : nodes) {
                if (node != null && node.getId() != null
                        && node.getId().startsWith("SplitLines")) {
                    maskPane.getChildren().remove(node);
                }
            }
            imageView.setImage(image);
            sizeLabel.setText("");
            imageInfos.clear();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void indicateSplit() {
        try {
            initSplit();
            if (rows == null || cols == null
                    || rows.size() < 2 || cols.size() < 2
                    || (rows.size() == 2 && cols.size() == 2)) {
                splitValid.set(false);
                return;
            }
            IntTools.sortList(rows);
            IntTools.sortList(cols);
            Color strokeColor = strokeColor();
            double strokeWidth = strokeWidth();
            double w = viewWidth();
            double h = viewHeight();
            double ratiox = viewXRatio() * widthRatio();
            double ratioy = viewXRatio() * heightRatio();
            for (int i = 0; i < rows.size(); ++i) {
                double row = rows.get(i) * ratioy;
                if (row <= 0 || row >= h - 1) {
                    continue;
                }
                Line line = new Line(0, row, w, row);
                addLine(i, line, false, ratioy, strokeColor, strokeWidth);
            }
            for (int i = 0; i < cols.size(); ++i) {
                double col = cols.get(i) * ratiox;
                if (col <= 0 || col >= w - 1) {
                    continue;
                }
                Line line = new Line(col, 0, col, h);
                addLine(i, line, true, ratiox, strokeColor, strokeWidth);
            }

            if (displaySizeCheck.isSelected()) {
                String style = " -fx-font-size: 1.2em; ";
                for (int i = 0; i < rows.size() - 1; ++i) {
                    double row = rows.get(i) * ratioy;
                    int hv = rows.get(i + 1) - rows.get(i);
                    for (int j = 0; j < cols.size() - 1; ++j) {
                        double col = cols.get(j) * ratiox;
                        int wv = cols.get(j + 1) - cols.get(j);
                        Text text = new Text(wv + "x" + hv);
                        text.setStyle(style);
                        text.setFill(strokeColor);
                        text.setId("SplitLinesText" + i + "x" + j);
                        text.setLayoutX(imageView.getLayoutX());
                        text.setLayoutY(imageView.getLayoutY());
                        text.setX(col + 10);
                        text.setY(row + 10);
                        maskPane.getChildren().add(text);
                    }
                }
            }

            String comments = message("SplittedNumber") + ": "
                    + (cols.size() - 1) * (rows.size() - 1);
            sizeLabel.setText(comments);
            splitValid.set(true);

        } catch (Exception e) {
            MyBoxLog.error(e);
            splitValid.set(false);
        }
        makeList();
    }

    protected void addLine(int index, Line line, boolean isCol,
            double ratio, Color strokeColor, double strokeWidth) {
        if (isCol) {
            line.setId("SplitLinesCols" + index);
        } else {
            line.setId("SplitLinesRows" + index);
        }
        line.setStroke(strokeColor);
        line.setStrokeWidth(strokeWidth);
        line.getStrokeDashArray().clear();
        line.getStrokeDashArray().addAll(strokeWidth, strokeWidth * 3);
        line.setLayoutX(imageView.getLayoutX());
        line.setLayoutY(imageView.getLayoutY());
        maskPane.getChildren().add(line);
        line.setCursor(defaultShapeCursor());
        line.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                controlPressed(event);
            }
        });
        line.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {

            }
        });
        line.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                scrollPane.setPannable(true);
                double offsetX = imageOffsetX(event);
                double offsetY = imageOffsetY(event);
                if (!DoubleShape.changed(offsetX, offsetY)) {
                    return;
                }
                if (isCol) {
                    double x = event.getX();
                    line.setStartX(x);
                    line.setEndX(x);
                    cols.set(index, (int) (x / ratio));
                } else {
                    double y = event.getY();
                    line.setStartY(y);
                    line.setEndY(y);
                    rows.set(index, (int) (y / ratio));
                }
                lineChanged();
            }
        });
        line.hoverProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                if (newValue && popLineMenuCheck.isSelected()) {
                    popNodeMenu(line, lineMenu(line, index, isCol, ratio));
                }
            }
        });

    }

    protected List<MenuItem> lineMenu(Line line, int index, boolean isCol, double ratio) {
        try {
            if (line == null) {
                return null;
            }
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;

            int currentValue;
            String name, type;
            if (isCol) {
                name = message("Column");
                type = "x";
                currentValue = cols.get(index);
            } else {
                name = message("Row");
                type = "y";
                currentValue = rows.get(index);
            }

            menu = new MenuItem(name + " " + index + "\n" + type + ": " + currentValue);
            menu.setStyle("-fx-text-fill: #2e598a;");
            items.add(menu);
            items.add(new SeparatorMenuItem());

            menu = new MenuItem(message("MoveTo"), StyleTools.getIconImageView("iconMove.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                String value = PopTools.askValue(baseTitle, name, type, currentValue + "");
                if (value == null || value.isBlank()) {
                    return;
                }
                try {
                    int iv = Integer.parseInt(value);
                    double vv = iv * ratio;
                    if (isCol) {
                        line.setStartX(vv);
                        line.setEndX(vv);
                        cols.set(index, iv);
                    } else {
                        line.setStartY(vv);
                        line.setEndY(vv);
                        rows.set(index, iv);
                    }
                    lineChanged();
                } catch (Exception e) {
                    popError(message("InvalidValue"));
                }
            });
            items.add(menu);

            menu = new MenuItem(message("Delete"), StyleTools.getIconImageView("iconDelete.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                if (isCol) {
                    cols.remove(index);
                } else {
                    rows.remove(index);
                }
                lineChanged();
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());

            return items;

        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    protected void lineChanged() {
        try {
            customizeRadio.setSelected(true);

            String s = "";
            for (int col : cols) {
                if (col <= 0 || col >= operationWidth()) {
                    continue;
                }
                if (s.isEmpty()) {
                    s += col;
                } else {
                    s += "," + col;

                }
            }
            customizedColsInput.setText(s);

            s = "";
            for (int row : rows) {
                if (row <= 0 || row >= operationHeight()) {
                    continue;
                }
                if (s.isEmpty()) {
                    s += row;
                } else {
                    s += "," + row;

                }
            }
            customizedRowsInput.setText(s);

            indicateSplit();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public synchronized void makeList() {
        if (imageInfos == null) {
            return;
        }
        imageInfos.clear();
        if (!splitValid.get()) {
            return;
        }
        try {
            int x1, y1, x2, y2;
            for (int i = 0; i < rows.size() - 1; ++i) {
                y1 = rows.get(i);
                y2 = rows.get(i + 1);
                for (int j = 0; j < cols.size() - 1; ++j) {
                    x1 = cols.get(j);
                    x2 = cols.get(j + 1);
                    ImageInformation info;
                    if (imageInformation != null) {
                        info = imageInformation.cloneAttributes();
                    } else {
                        info = new ImageInformation(image);
                    }
                    info.setRegion(x1, y1, x2, y2);
                    imageInfos.add(info);
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public void imageSingleClicked(MouseEvent event, DoublePoint p) {
        if (image == null || splitMethod != SplitMethod.Customize
                || event.getButton() != MouseButton.SECONDARY) {
            return;
        }
        try {
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;
            double px = scale(p.getX());
            double py = scale(p.getY());
            menu = new MenuItem(message("Point") + ": " + px + ", " + py);
            menu.setStyle("-fx-text-fill: #2e598a;");
            items.add(menu);
            items.add(new SeparatorMenuItem());

            menu = new MenuItem(message("AddRowAtPoint"), StyleTools.getIconImageView("iconAdd.png"));
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent mevent) {
                    int y = (int) Math.round(p.getY() / heightRatio());
                    String str = customizedRowsInput.getText().trim();
                    if (str.isEmpty()) {
                        customizedRowsInput.setText(y + "");
                    } else {
                        customizedRowsInput.setText(str + "," + y);
                    }
                    pickCustomize();
                }
            });
            items.add(menu);

            menu = new MenuItem(message("AddColAtPoint"), StyleTools.getIconImageView("iconAdd.png"));
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent mevent) {
                    int x = (int) Math.round(p.getX() / widthRatio());
                    String str = customizedColsInput.getText().trim();
                    if (str.isEmpty()) {
                        customizedColsInput.setText(x + "");
                    } else {
                        customizedColsInput.setText(str + "," + x);
                    }
                    pickCustomize();
                }
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());

            popEventMenu(event, items);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean redrawMaskShape() {
        super.redrawMaskShape();
        indicateSplit();
        return true;
    }

    @FXML
    @Override
    public void playAction() {
        if (imageInfos == null || imageInfos.isEmpty()) {
            popError(message("NoData"));
            return;
        }
        ImagesPlayController.playImages(imageInfos);
    }

    @FXML
    @Override
    public void saveAsAction() {
        if (imageInfos == null || imageInfos.isEmpty()) {
            popError(message("NoData"));
            return;
        }
        ImagesSaveController.saveImages(this, imageInfos);
    }

    @FXML
    @Override
    public void editFrames() {
        if (imageInfos == null || imageInfos.isEmpty()) {
            popError(message("NoData"));
            return;
        }
        ImagesEditorController.openImages(imageInfos);
    }

}
