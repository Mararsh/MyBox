package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
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
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.IntTools;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2018-8-8
 * @License Apache License Version 2.0
 */
public class ImageSplitController extends BaseImagesListController {

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
    protected FlowPane splitPredefinedPane, splitSizePane, splitNumberPane,
            splitCustomized1Pane, splitCustomized2Pane;
    @FXML
    protected TextField rowsInput, colsInput, customizedRowsInput, customizedColsInput,
            widthInput, heightInput;
    @FXML
    protected CheckBox displaySizeCheck;
    @FXML
    protected VBox splitOptionsBox;
    @FXML
    protected Label promptLabel, sizeLabel;

    public ImageSplitController() {
        baseTitle = Languages.message("ImageSplit");
        TipsLabelKey = "ImageSplitTips";
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            operateOriginalSize = true;
            splitValid = new SimpleBooleanProperty(false);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            rightPane.disableProperty().bind(imageView.imageProperty().isNull());

            initSplitPane();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initSplitPane() {
        try {
            splitGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {
                    checkSplitMethod();
                }
            });

            widthInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    checkSizeValues();
                }
            });

            rowsInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (splitMethod == SplitMethod.ByNumber) {
                        checkNumberValues();
                    } else if (splitMethod == SplitMethod.BySize) {
                        checkSizeValues();
                    }
                }
            });
            colsInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (splitMethod == SplitMethod.ByNumber) {
                        checkNumberValues();
                    } else if (splitMethod == SplitMethod.BySize) {
                        checkSizeValues();
                    }
                }
            });

            okButton.disableProperty().bind(rowsInput.styleProperty().isEqualTo(UserConfig.badStyle())
                    .or(colsInput.styleProperty().isEqualTo(UserConfig.badStyle()))
            );

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    protected void initViewPane() {
        try {
            super.initViewPane();
            displaySizeCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                    indicateSplit();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void afterSceneLoaded() {
        super.afterSceneLoaded();
        checkSplitMethod();

    }

    protected void checkSplitMethod() {
        splitOptionsBox.getChildren().clear();
        imageView.setImage(image);
        List<Node> nodes = new ArrayList<>();
        nodes.addAll(maskPane.getChildren());
        for (Node node : nodes) {
            if (node.getId() != null && node.getId().startsWith("SplitLines")) {
                maskPane.getChildren().remove(node);
                node = null;
            }
        }
        sizeLabel.setText("");
        imageInfos.clear();

        RadioButton selected = (RadioButton) splitGroup.getSelectedToggle();
        if (Languages.message("Predefined").equals(selected.getText())) {
            splitMethod = SplitMethod.Predefined;
            splitOptionsBox.getChildren().addAll(splitPredefinedPane);
            promptLabel.setText("");
        } else if (Languages.message("Customize").equals(selected.getText())) {
            splitMethod = SplitMethod.Customize;
            splitOptionsBox.getChildren().addAll(splitCustomized1Pane, splitCustomized2Pane);
            promptLabel.setText(Languages.message("SplitCustomComments"));
            checkCustomValues();
        } else if (Languages.message("ByNumber").equals(selected.getText())) {
            splitMethod = SplitMethod.ByNumber;
            splitOptionsBox.getChildren().addAll(splitNumberPane, okButton);
            promptLabel.setText("");
            isSettingValues = true;
            rowsInput.setText("3");
            colsInput.setText("3");
            isSettingValues = false;
            checkNumberValues();
        } else if (Languages.message("BySize").equals(selected.getText())) {
            splitMethod = SplitMethod.BySize;
            splitOptionsBox.getChildren().addAll(splitSizePane, okButton);
            promptLabel.setText(Languages.message("SplitSizeComments"));
            isSettingValues = true;
            widthInput.setText((int) (getImageWidth() / (widthRatio() * 3)) + "");
            heightInput.setText((int) (getImageHeight() / (heightRatio() * 3)) + "");
            isSettingValues = false;
            checkSizeValues();
        }
        refreshStyle(splitOptionsBox);
    }

    protected void checkNumberValues() {
        if (isSettingValues) {
            return;
        }
        rowsNumber = -1;
        colsNumber = -1;
        if (rowsInput.getText().isEmpty()) {
            rowsNumber = 0;
            rowsInput.setStyle(null);
        } else {
            try {
                rowsNumber = Integer.parseInt(rowsInput.getText());
                rowsInput.setStyle(null);
                if (rowsNumber > 0) {
                    rowsInput.setStyle(null);
                } else {
                    rowsInput.setStyle(UserConfig.badStyle());
                }
            } catch (Exception e) {
                rowsInput.setStyle(UserConfig.badStyle());
            }
        }

        if (colsInput.getText().isEmpty()) {
            colsNumber = 0;
            colsInput.setStyle(null);
        } else {
            try {
                colsNumber = Integer.parseInt(colsInput.getText());
                colsInput.setStyle(null);
                if (colsNumber > 0) {
                    colsInput.setStyle(null);
                } else {
                    colsInput.setStyle(UserConfig.badStyle());
                }
            } catch (Exception e) {
                colsInput.setStyle(UserConfig.badStyle());
            }
        }
    }

    protected void checkSizeValues() {
        if (isSettingValues) {
            return;
        }
        try {
            int v = Integer.parseInt(widthInput.getText());
            if (v > 0 && v < getOperationWidth()) {
                widthInput.setStyle(null);
                width = v;
            } else {
                widthInput.setStyle(UserConfig.badStyle());
            }
        } catch (Exception e) {
            widthInput.setStyle(UserConfig.badStyle());
        }
        try {
            int v = Integer.parseInt(heightInput.getText());
            if (v > 0 && v < getOperationHeight()) {
                heightInput.setStyle(null);
                height = v;
            } else {
                heightInput.setStyle(UserConfig.badStyle());
            }
        } catch (Exception e) {
            heightInput.setStyle(UserConfig.badStyle());
        }
    }

    protected void checkCustomValues() {
        if (isSettingValues) {
            return;
        }
        boolean isValidRows = true, isValidcols = true;
        rows = new ArrayList<>();
        rows.add(0);
        rows.add(getOperationHeight() - 1);
        cols = new ArrayList<>();
        cols.add(0);
        cols.add(getOperationWidth() - 1);
        customizedRowsInput.setStyle(null);
        customizedColsInput.setStyle(null);

        if (!customizedRowsInput.getText().isEmpty()) {
            String[] rowStrings = customizedRowsInput.getText().split(",");
            for (String row : rowStrings) {
                try {
                    int value = Integer.parseInt(row.trim());
                    if (value < 0 || value > getOperationHeight() - 1) {
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
                    break;
                }
            }
        }

        if (!customizedColsInput.getText().isEmpty()) {
            String[] colStrings = customizedColsInput.getText().split(",");
            for (String col : colStrings) {
                try {
                    int value = Integer.parseInt(col.trim());
                    if (value <= 0 || value >= getOperationWidth() - 1) {
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

        if (isValidRows || isValidcols) {
            indicateSplit();
        } else {
            popInformation(Languages.message("SplitCustomComments"));
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
            isSettingValues = true;
            clearCols();
            clearRows();
            isSettingValues = false;
            checkSplitMethod();

            saveController.thumbsListButton.setVisible(imageInformation == null || !imageInformation.isIsSampled());

            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return false;
        }

    }

    protected void divideImageBySize() {
        if (width <= 0 || height <= 0) {
            return;
        }
        cols = new ArrayList<>();
        cols.add(0);
        int v = width - 1;
        while (v < getOperationWidth()) {
            cols.add(v);
            v += width - 1;
        }
        cols.add(getOperationWidth() - 1);

        rows = new ArrayList<>();
        rows.add(0);
        v = height - 1;
        while (v < getOperationHeight()) {
            rows.add(v);
            v += height - 1;
        }
        rows.add(getOperationHeight() - 1);

        indicateSplit();
    }

    protected void divideImageByNumber() {
        if (rowsNumber < 0 || colsNumber < 0) {
            return;
        }
        cols = new ArrayList<>();
        cols.add(0);
        for (int i = 1; i < colsNumber; ++i) {
            int v = i * getOperationWidth() / colsNumber;
            cols.add(v);
        }
        cols.add(getOperationWidth() - 1);
        rows = new ArrayList<>();
        rows.add(0);
        for (int i = 1; i < rowsNumber; ++i) {
            int v = i * getOperationHeight() / rowsNumber;
            rows.add(v);
        }
        rows.add(getOperationHeight() - 1);
        indicateSplit();
    }

    protected void divideImageByNumber(int rows, int cols) {
        isSettingValues = true;
        rowsInput.setText(rows + "");
        colsInput.setText(cols + "");
        isSettingValues = false;
        checkNumberValues();
        divideImageByNumber();
    }

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

    @FXML
    protected void clearRows() {
        customizedRowsInput.setText("");
    }

    @FXML
    protected void goRows() {
        checkCustomValues();
    }

    @FXML
    protected void clearCols() {
        customizedColsInput.setText("");
    }

    @FXML
    protected void goCols() {
        checkCustomValues();
    }

    protected void indicateSplit() {
        try {
            List<Node> nodes = new ArrayList<>();
            nodes.addAll(maskPane.getChildren());
            for (Node node : nodes) {
                if (node.getId() != null && node.getId().startsWith("SplitLines")) {
                    maskPane.getChildren().remove(node);
                    node = null;
                }
            }
            imageInfos.clear();
            sizeLabel.setText("");
            if (rows == null || cols == null
                    || rows.size() < 2 || cols.size() < 2
                    || (rows.size() == 2 && cols.size() == 2)) {
                imageView.setImage(image);
                splitValid.set(false);
                return;
            }
            Color strokeColor = Color.web(UserConfig.getString("StrokeColor", "#FF0000"));
            double strokeWidth = UserConfig.getInt("StrokeWidth", 2);
            double w = imageView.getBoundsInParent().getWidth();
            double h = imageView.getBoundsInParent().getHeight();
            double ratiox = w / imageView.getImage().getWidth();
            double ratioy = h / imageView.getImage().getHeight();
            for (int i = 0; i < rows.size(); ++i) {
                double row = rows.get(i) * ratioy * heightRatio();
                if (row <= 0 || row >= h - 1) {
                    continue;
                }
                Line line = new Line(0, row, w, row);
                line.setId("SplitLinesRows" + i);
                line.setStroke(strokeColor);
                line.setStrokeWidth(strokeWidth);
                line.getStrokeDashArray().clear();
                line.getStrokeDashArray().addAll(strokeWidth, strokeWidth * 3);
                line.setLayoutX(imageView.getLayoutX() + line.getLayoutX());
                line.setLayoutY(imageView.getLayoutY() + line.getLayoutY());
                maskPane.getChildren().add(line);
            }
            for (int i = 0; i < cols.size(); ++i) {
                double col = cols.get(i) * ratiox * widthRatio();
                if (col <= 0 || col >= w - 1) {
                    continue;
                }
                Line line = new Line(col, 0, col, h);
                line.setId("SplitLinesCols" + i);
                line.setStroke(strokeColor);
                line.setStrokeWidth(strokeWidth);
                line.getStrokeDashArray().clear();
                line.getStrokeDashArray().addAll(strokeWidth, strokeWidth * 3);
                line.setLayoutX(imageView.getLayoutX() + line.getLayoutX());
                line.setLayoutY(imageView.getLayoutY() + line.getLayoutY());
                maskPane.getChildren().add(line);
            }

            if (displaySizeCheck.isSelected()) {
                String style = " -fx-font-size: 1.2em; ";
                IntTools.sortList(rows);
                IntTools.sortList(cols);
                for (int i = 0; i < rows.size() - 1; ++i) {
                    double row = rows.get(i) * ratioy * heightRatio();
                    int hv = rows.get(i + 1) - rows.get(i) + 1;
                    for (int j = 0; j < cols.size() - 1; ++j) {
                        double col = cols.get(j) * ratiox * widthRatio();
                        int wv = cols.get(j + 1) - cols.get(j) + 1;
                        Text text = new Text(wv + "x" + hv);
                        text.setStyle(style);
                        text.setFill(strokeColor);
                        text.setLayoutX(imageView.getLayoutX() + col + 50);
                        text.setLayoutY(imageView.getLayoutY() + row + 50);
                        text.setId("SplitLinesText" + i + "x" + j);
                        maskPane.getChildren().add(text);
                    }
                }
            }

            String comments = Languages.message("SplittedNumber") + ": "
                    + (cols.size() - 1) * (rows.size() - 1);
            if (splitMethod == SplitMethod.ByNumber) {
                comments += "  " + Languages.message("EachSplittedImageActualSize") + ": "
                        + getOperationWidth() / (cols.size() - 1)
                        + " x " + getOperationHeight() / (rows.size() - 1);

            } else {
                comments += "  " + Languages.message("EachSplittedImageActualSizeComments");
            }
            sizeLabel.setText(comments);
            splitValid.set(true);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            splitValid.set(false);
        }
        makeList();
    }

    public synchronized void makeList() {
        imageInfos.clear();
        if (!splitValid.get()) {
            return;
        }
        List<ImageInformation> infos = new ArrayList<>();
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
                    infos.add(info);
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        imageInfos.setAll(infos);
    }

    @Override
    public void imageSingleClicked(MouseEvent event, DoublePoint p) {
        super.imageSingleClicked(event, p);
        if (image == null || splitMethod != SplitMethod.Customize) {
            return;
        }
//        imageView.setCursor(Cursor.OPEN_HAND);
        promptLabel.setText(Languages.message("SplitCustomComments"));

        if (event.getButton() == MouseButton.PRIMARY) {

            int y = (int) Math.round(p.getY() / heightRatio());
            String str = customizedRowsInput.getText().trim();
            if (str.isEmpty()) {
                customizedRowsInput.setText(y + "");
            } else {
                customizedRowsInput.setText(str + "," + y);
            }

            checkCustomValues();

        } else if (event.getButton() == MouseButton.SECONDARY) {
            int x = (int) Math.round(p.getX() / widthRatio());
            String str = customizedColsInput.getText().trim();
            if (str.isEmpty()) {
                customizedColsInput.setText(x + "");
            } else {
                customizedColsInput.setText(str + "," + x);
            }
            checkCustomValues();
        }
    }

    @Override
    public void drawMaskControls() {
        super.drawMaskControls();
        indicateSplit();
    }

    @FXML
    @Override
    public void okAction() {
        if (splitMethod == SplitMethod.ByNumber) {
            divideImageByNumber();
        } else if (splitMethod == SplitMethod.BySize) {
            divideImageBySize();
        }
    }

    @Override
    public boolean controlAltS() {
        saveController.saveAsAction();
        return false;
    }

}
