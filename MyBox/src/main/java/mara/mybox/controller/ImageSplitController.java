package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageOutputStream;
import mara.mybox.data.DoublePoint;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.data.VisitHistory.FileType;
import mara.mybox.db.data.VisitHistoryTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.fxml.FxmlImageManufacture;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.fxml.RecentVisitMenu;
import mara.mybox.image.ImageManufacture;
import mara.mybox.image.file.ImageFileReaders;
import mara.mybox.image.file.ImageFileWriters;
import static mara.mybox.image.file.ImageTiffFile.getPara;
import static mara.mybox.image.file.ImageTiffFile.getWriter;
import static mara.mybox.image.file.ImageTiffFile.getWriterMeta;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.PdfTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;
import mara.mybox.value.CommonValues;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageXYZDestination;

/**
 * @Author Mara
 * @CreateDate 2018-8-8
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageSplitController extends ImageViewerController {

    private List<Integer> rows, cols;
    private int rowsNumber, colsNumber, width, height;
    protected SimpleBooleanProperty splitValid;
    private SplitMethod splitMethod;
    private LoadingController imageLoading, pdfLoading, tiffLoading;
    private SingletonTask imageTask, pdfTask, tiffTask;

    public static enum SplitMethod {
        Predefined, ByNumber, BySize, Customize
    }

    @FXML
    protected ToggleGroup splitGroup;
    @FXML
    protected FlowPane splitPredefinedPane, splitSizePane, splitNumberPane,
            splitCustomized1Pane, splitCustomized2Pane;
    @FXML
    protected Button saveImagesButton, saveTiffButton, savePdfButton;
    @FXML
    protected TextField rowsInput, colsInput, customizedRowsInput, customizedColsInput,
            widthInput, heightInput;
    @FXML
    protected CheckBox displaySizeCheck;
    @FXML
    protected ControlPdfWriteOptions pdfOptionsController;
    @FXML
    protected VBox splitOptionsBox, optionsBox, showBox;
    @FXML
    protected HBox opBox;
    @FXML
    protected Label promptLabel, sizeLabel;

    public ImageSplitController() {
        baseTitle = AppVariables.message("ImageSplit");
        TipsLabelKey = "ImageSplitTips";
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            operateOriginalSize = true;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            initCommon();
            initSplitTab();
            pdfOptionsController.set(baseName, true);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void afterSceneLoaded() {
        super.afterSceneLoaded();
        checkSplitMethod();
        FxmlControl.setTooltip(okButton, new Tooltip(message("OK") + "\nF1 / CTRL+g"));

    }

    protected void initCommon() {
        opBox.disableProperty().bind(imageView.imageProperty().isNull());
        optionsBox.disableProperty().bind(imageView.imageProperty().isNull());
        showBox.disableProperty().bind(imageView.imageProperty().isNull());

        splitValid = new SimpleBooleanProperty(false);

        displaySizeCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov,
                    Boolean old_val, Boolean new_val) {
                indicateSplit();
            }
        });

        saveImagesButton.disableProperty().bind(
                splitValid.not()
        );
        savePdfButton.disableProperty().bind(
                splitValid.not()
                        .or(pdfOptionsController.customWidthInput.styleProperty().isEqualTo(badStyle))
                        .or(pdfOptionsController.customHeightInput.styleProperty().isEqualTo(badStyle))
                        .or(pdfOptionsController.marginSelector.styleProperty().isEqualTo(badStyle))
                        .or(pdfOptionsController.jpegQualitySelector.styleProperty().isEqualTo(badStyle))
                        .or(pdfOptionsController.thresholdInput.styleProperty().isEqualTo(badStyle))
        );
        saveTiffButton.disableProperty().bind(
                splitValid.not()
        );
    }

    protected void initSplitTab() {
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

        customizedRowsInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                checkCustomValues();
            }
        });
        customizedColsInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                checkCustomValues();
            }
        });

        okButton.disableProperty().bind(rowsInput.styleProperty().isEqualTo(badStyle)
                .or(colsInput.styleProperty().isEqualTo(badStyle))
        );
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
        RadioButton selected = (RadioButton) splitGroup.getSelectedToggle();
        if (AppVariables.message("Predefined").equals(selected.getText())) {
            splitMethod = SplitMethod.Predefined;
            splitOptionsBox.getChildren().addAll(splitPredefinedPane);
            promptLabel.setText("");
        } else if (AppVariables.message("Customize").equals(selected.getText())) {
            splitMethod = SplitMethod.Customize;
            splitOptionsBox.getChildren().addAll(splitCustomized1Pane, splitCustomized2Pane);
            promptLabel.setText(AppVariables.message("SplitCustomComments"));
            checkCustomValues();
        } else if (AppVariables.message("ByNumber").equals(selected.getText())) {
            splitMethod = SplitMethod.ByNumber;
            splitOptionsBox.getChildren().addAll(splitNumberPane, okButton);
            promptLabel.setText("");
            isSettingValues = true;
            rowsInput.setText("3");
            colsInput.setText("3");
            isSettingValues = false;
            checkNumberValues();
        } else if (AppVariables.message("BySize").equals(selected.getText())) {
            splitMethod = SplitMethod.BySize;
            splitOptionsBox.getChildren().addAll(splitSizePane, okButton);
            promptLabel.setText(AppVariables.message("SplitSizeComments"));
            isSettingValues = true;
            widthInput.setText((int) (getImageWidth() / (widthRatio() * 3)) + "");
            heightInput.setText((int) (getImageHeight() / (heightRatio() * 3)) + "");
            isSettingValues = false;
            checkSizeValues();
        }
        FxmlControl.refreshStyle(splitOptionsBox);

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
                rowsNumber = Integer.valueOf(rowsInput.getText());
                rowsInput.setStyle(null);
                if (rowsNumber > 0) {
                    rowsInput.setStyle(null);
                } else {
                    rowsInput.setStyle(badStyle);
                }
            } catch (Exception e) {
                rowsInput.setStyle(badStyle);
            }
        }

        if (colsInput.getText().isEmpty()) {
            colsNumber = 0;
            colsInput.setStyle(null);
        } else {
            try {
                colsNumber = Integer.valueOf(colsInput.getText());
                colsInput.setStyle(null);
                if (colsNumber > 0) {
                    colsInput.setStyle(null);
                } else {
                    colsInput.setStyle(badStyle);
                }
            } catch (Exception e) {
                colsInput.setStyle(badStyle);
            }
        }
    }

    protected void checkSizeValues() {
        if (isSettingValues) {
            return;
        }
        try {
            int v = Integer.valueOf(widthInput.getText());
            if (v > 0 && v < getOperationWidth()) {
                widthInput.setStyle(null);
                width = v;
            } else {
                widthInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            widthInput.setStyle(badStyle);
        }
        try {
            int v = Integer.valueOf(heightInput.getText());
            if (v > 0 && v < getOperationHeight()) {
                heightInput.setStyle(null);
                height = v;
            } else {
                heightInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            heightInput.setStyle(badStyle);
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
                    int value = Integer.valueOf(row.trim());
                    if (value < 0 || value > getOperationHeight() - 1) {
                        customizedRowsInput.setStyle(badStyle);
                        isValidRows = false;
                        break;
                    }
                    if (!rows.contains(value)) {
                        rows.add(value);
                    }
                } catch (Exception e) {
                    customizedRowsInput.setStyle(badStyle);
                    isValidRows = false;
                    break;
                }
            }
        }

        if (!customizedColsInput.getText().isEmpty()) {
            String[] colStrings = customizedColsInput.getText().split(",");
            for (String col : colStrings) {
                try {
                    int value = Integer.valueOf(col.trim());
                    if (value <= 0 || value >= getOperationWidth() - 1) {
                        customizedColsInput.setStyle(badStyle);
                        isValidcols = false;
                        break;
                    }
                    if (!cols.contains(value)) {
                        cols.add(value);
                    }
                } catch (Exception e) {
                    customizedColsInput.setStyle(badStyle);
                    isValidcols = false;
                    break;
                }
            }
        }

        if (isValidRows || isValidcols) {
            indicateSplit();
        } else {
            popInformation(message("SplitCustomComments"));
        }
    }

    @Override
    public boolean afterImageLoaded() {
        try {
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

            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return false;
        }

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
    protected void clearCols() {
        customizedColsInput.setText("");
    }

    @Override
    public void imageSingleClicked(MouseEvent event, DoublePoint p) {
        super.imageSingleClicked(event, p);
        if (image == null || splitMethod != SplitMethod.Customize) {
            return;
        }
//        imageView.setCursor(Cursor.OPEN_HAND);
        promptLabel.setText(message("SplitCustomComments"));

        if (event.getButton() == MouseButton.PRIMARY) {

            int y = (int) Math.round(p.getY() / heightRatio());
            String str = customizedRowsInput.getText().trim();
            if (str.isEmpty()) {
                customizedRowsInput.setText(y + "");
            } else {
                customizedRowsInput.setText(str + "," + y);
            }

        } else if (event.getButton() == MouseButton.SECONDARY) {
            int x = (int) Math.round(p.getX() / widthRatio());
            String str = customizedColsInput.getText().trim();
            if (str.isEmpty()) {
                customizedColsInput.setText(x + "");
            } else {
                customizedColsInput.setText(str + "," + x);
            }
        }
    }

    protected void indicateSplit() {
        try {
            if (image == null) {
                return;
            }
            List<Node> nodes = new ArrayList<>();
            nodes.addAll(maskPane.getChildren());
            for (Node node : nodes) {
                if (node.getId() != null && node.getId().startsWith("SplitLines")) {
                    maskPane.getChildren().remove(node);
                    node = null;
                }
            }
            if (rows == null || cols == null
                    || rows.size() < 2 || cols.size() < 2
                    || (rows.size() == 2 && cols.size() == 2)) {
                imageView.setImage(image);
                splitValid.set(false);
                return;
            }
            Color strokeColor = Color.web(AppVariables.getUserConfigValue("StrokeColor", "#FF0000"));
            double strokeWidth = AppVariables.getUserConfigInt("StrokeWidth", 2);
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
                DoubleTools.sortList(rows);
                DoubleTools.sortList(cols);
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

            String comments = AppVariables.message("SplittedNumber") + ": "
                    + (cols.size() - 1) * (rows.size() - 1);
            if (splitMethod == SplitMethod.ByNumber) {
                comments += "  " + AppVariables.message("EachSplittedImageActualSize") + ": "
                        + getOperationWidth() / (cols.size() - 1)
                        + " x " + getOperationHeight() / (rows.size() - 1);

            } else {
                comments += "  " + AppVariables.message("EachSplittedImageActualSizeComments");
            }
            sizeLabel.setText(comments);
            splitValid.set(true);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            splitValid.set(false);
        }

    }

    @Override
    public void drawMaskControls() {
        super.drawMaskControls();
        indicateSplit();
    }

    @FXML
    public void popSaveAsPdf(MouseEvent event) { //
        if (AppVariables.fileRecentNumber <= 0) {
            return;
        }
        new RecentVisitMenu(this, event) {
            @Override
            public List<VisitHistory> recentFiles() {
                return null;
            }

            @Override
            public List<VisitHistory> recentPaths() {
                return recentTargetPaths();
            }

            @Override
            public void handleSelect() {
                saveAsPdfAction();
            }

            @Override
            public void handleFile(String fname) {

            }

            @Override
            public void handlePath(String fname) {
                handleTargetPath(fname);
            }

        }.setFileType(FileType.PDF).pop();
    }

    @FXML
    public void popSaveAsTif(MouseEvent event) { //
        if (AppVariables.fileRecentNumber <= 0) {
            return;
        }
        new RecentVisitMenu(this, event) {
            @Override
            public List<VisitHistory> recentFiles() {
                return null;
            }

            @Override
            public List<VisitHistory> recentPaths() {
                return recentTargetPaths();
            }

            @Override
            public void handleSelect() {
                saveAsTiffAction();
            }

            @Override
            public void handleFile(String fname) {

            }

            @Override
            public void handlePath(String fname) {
                handleTargetPath(fname);
            }

        }.setFileType(FileType.Tif).pop();
    }

    private File validationBeforeSave(List<FileChooser.ExtensionFilter> ext, int fileType, String diagTitle) {
        if (image == null || !splitValid.getValue()
                || rows == null || cols == null
                || rows.size() < 1 || cols.size() < 1) {
            return null;
        }
        String prefix = null;
        if (sourceFile != null) {
            prefix = FileTools.getFilePrefix(sourceFile.getName()) + "-p";
        }
        final File tFile = chooseSaveFile(diagTitle, VisitHistoryTools.getSavedPath(fileType), prefix, ext);
        if (tFile == null) {
            return null;
        }
        recordFileWritten(tFile, fileType);
        return tFile;
    }

    @FXML
    public void saveAsImagesAction(ActionEvent event) {
        if (image == null || rows == null || rows.isEmpty()
                || cols == null || cols.isEmpty()) {
            return;
        }
        final File tFile
                = validationBeforeSave(CommonFxValues.ImageExtensionFilter, FileType.Image, message("FilePrefixInput"));
        if (tFile == null) {
            return;
        }
        if (imageTask != null) {
            imageTask.cancel();
            imageLoading = null;
        }
        imageTask = new SingletonTask<Void>() {
            List<String> fileNames = new ArrayList<>();

            @Override
            protected boolean handle() {
                int x1, y1, x2, y2;
                String targetFormat = FileTools.getFileSuffix(tFile.getAbsolutePath()).toLowerCase();
                String filePrefix = FileTools.getFilePrefix(tFile.getAbsolutePath());
                String sourceFormat = null, sourceFilename = null;
                BufferedImage sourceImage = null;
                if (sourceFile != null && imageInformation != null) {
                    sourceFormat = imageInformation.getImageFormat();
                    sourceFilename = sourceFile.getAbsolutePath();
                }
                if ((imageInformation == null) || !imageInformation.isIsScaled()) {
                    sourceImage = FxmlImageManufacture.bufferedImage(image);
                }
                int total = (rows.size() - 1) * (cols.size() - 1);
                for (int i = 0; i < rows.size() - 1; ++i) {
                    if (imageTask == null || isCancelled()) {
                        return false;
                    }
                    y1 = rows.get(i);
                    y2 = rows.get(i + 1);
                    for (int j = 0; j < cols.size() - 1; ++j) {
                        if (imageTask == null || isCancelled()) {
                            return false;
                        }
                        x1 = cols.get(j);
                        x2 = cols.get(j + 1);
                        BufferedImage target;
                        if (sourceImage != null) {
                            target = ImageManufacture.cropOutside(sourceImage, x1, y1, x2, y2);
                        } else {
                            target = ImageFileReaders.readFrame(sourceFormat, sourceFilename, x1, y1, x2, y2);
                        }
                        if (imageTask == null || isCancelled()) {
                            return false;
                        }
                        final String fileName = filePrefix + "_"
                                + (rows.size() - 1) + "x" + (cols.size() - 1) + "_"
                                + (i + 1) + "-" + (j + 1)
                                + "." + targetFormat;
                        ImageFileWriters.writeImageFile(target, targetFormat, fileName);
                        fileNames.add(new File(fileName).getAbsolutePath());
                        updateLabel(fileName, total, (i + 1) * (j + 1));
                    }
                }
                return true;
            }

            protected void updateLabel(final String fileName, final int total, final int number) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (imageTask == null || imageTask.isQuit() || imageLoading == null) {
                            return;
                        }
                        imageLoading.setInfo(MessageFormat.format(AppVariables.message("NumberFileGenerated"),
                                number + "/" + total, "\"" + fileName + "\""));
                        imageLoading.setProgress(number * 1f / total);
                    }
                });
            }

            @Override
            protected void whenSucceeded() {
                multipleFilesGenerated(fileNames);
            }

        };
        imageLoading = openHandlingStage(imageTask, Modality.WINDOW_MODAL);
        Thread thread = new Thread(imageTask);
        thread.setDaemon(true);
        thread.start();

    }

    @FXML
    protected void saveAsPdfAction() {
        if (image == null || rows == null || rows.isEmpty()
                || cols == null || cols.isEmpty()) {
            return;
        }
        final File tFile = validationBeforeSave(CommonFxValues.PdfExtensionFilter, FileType.PDF, null);
        if (tFile == null) {
            return;
        }
        if (pdfTask != null) {
            pdfTask.cancel();
            pdfLoading = null;
        }
        pdfTask = new SingletonTask<Void>() {

            @Override
            protected boolean handle() {
                try {
                    String sourceFormat = null, sourceFilename = null;
                    BufferedImage sourceImage = null;
                    if (sourceFile != null && imageInformation != null) {
                        sourceFormat = imageInformation.getImageFormat();
                        sourceFilename = sourceFile.getAbsolutePath();
                    }
                    if ((imageInformation == null) || !imageInformation.isIsScaled()) {
                        sourceImage = FxmlImageManufacture.bufferedImage(image);
                    }
                    File tmpFile = FileTools.getTempFile();
                    try ( PDDocument document = new PDDocument(AppVariables.pdfMemUsage)) {
                        PDDocumentInformation info = new PDDocumentInformation();
                        info.setCreationDate(Calendar.getInstance());
                        info.setModificationDate(Calendar.getInstance());
                        info.setProducer("MyBox v" + CommonValues.AppVersion);
                        info.setAuthor(pdfOptionsController.authorInput.getText());
                        document.setDocumentInformation(info);
                        document.setVersion(1.0f);
                        int x1, y1, x2, y2;
                        int count = 0;
                        int total = (rows.size() - 1) * (cols.size() - 1);
                        for (int i = 0; i < rows.size() - 1; ++i) {
                            if (pdfTask == null || isCancelled()) {
                                return false;
                            }
                            y1 = rows.get(i);
                            y2 = rows.get(i + 1);
                            for (int j = 0; j < cols.size() - 1; ++j) {
                                if (pdfTask == null || isCancelled()) {
                                    return false;
                                }
                                x1 = cols.get(j);
                                x2 = cols.get(j + 1);
                                BufferedImage target;
                                if (sourceImage != null) {
                                    target = ImageManufacture.cropOutside(sourceImage, x1, y1, x2, y2);
                                } else {
                                    target = ImageFileReaders.readFrame(sourceFormat, sourceFilename, x1, y1, x2, y2);
                                }
                                if (pdfTask == null || isCancelled()) {
                                    return false;
                                }

                                PdfTools.writePage(document, sourceFormat, target, ++count, total, pdfOptionsController);
                                updateLabel(total, (i + 1) * (j + 1));
                            }
                        }
                        PDPage page = document.getPage(0);
                        PDPageXYZDestination dest = new PDPageXYZDestination();
                        dest.setPage(page);
                        dest.setZoom(pdfOptionsController.zoom / 100.0f);
                        dest.setTop((int) page.getCropBox().getHeight());
                        PDActionGoTo action = new PDActionGoTo();
                        action.setDestination(dest);
                        document.getDocumentCatalog().setOpenAction(action);

                        document.save(tmpFile);
                        document.close();
                    }
                    return FileTools.rename(tmpFile, tFile);
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            protected void updateLabel(final int total, final int number) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (pdfTask == null || pdfTask.isQuit() || pdfLoading == null) {
                            return;
                        }
                        pdfLoading.setInfo(MessageFormat.format(AppVariables.message("NumberPageWritten"),
                                number + "/" + total));
                        pdfLoading.setProgress(number * 1f / total);
                    }
                });
            }

            @Override
            protected void whenSucceeded() {
                popSuccessful();
                FxmlStage.openPdfViewer(null, tFile);
            }

        };
        pdfLoading = openHandlingStage(pdfTask, Modality.WINDOW_MODAL);
        Thread thread = new Thread(pdfTask);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    protected void saveAsTiffAction() {
        if (image == null || rows == null || rows.isEmpty()
                || cols == null || cols.isEmpty()) {
            return;
        }
        final File tFile = validationBeforeSave(CommonFxValues.TiffExtensionFilter, FileType.Tif, null);
        if (tFile == null) {
            return;
        }
        if (tiffTask != null) {
            tiffTask.cancel();
            tiffLoading = null;
        }
        tiffTask = new SingletonTask<Void>() {

            @Override
            protected boolean handle() {

                try {
                    String sourceFormat = null, sourceFilename = null;
                    BufferedImage sourceImage = null;
                    if (sourceFile != null && imageInformation != null) {
                        sourceFormat = imageInformation.getImageFormat();
                        sourceFilename = sourceFile.getAbsolutePath();
                    }
                    if ((imageInformation == null) || !imageInformation.isIsScaled()) {
                        sourceImage = FxmlImageManufacture.bufferedImage(image);
                    }
                    ImageWriter writer = getWriter();
                    File tmpFile = FileTools.getTempFile();
                    try ( ImageOutputStream out = ImageIO.createImageOutputStream(tmpFile)) {
                        writer.setOutput(out);
                        ImageWriteParam param = getPara(null, writer);
                        writer.prepareWriteSequence(null);
                        int x1, y1, x2, y2;
                        int total = (rows.size() - 1) * (cols.size() - 1);
                        for (int i = 0; i < rows.size() - 1; ++i) {
                            if (tiffTask == null || isCancelled()) {
                                return false;
                            }
                            y1 = rows.get(i);
                            y2 = rows.get(i + 1);
                            for (int j = 0; j < cols.size() - 1; ++j) {
                                if (tiffTask == null || isCancelled()) {
                                    return false;
                                }
                                x1 = cols.get(j);
                                x2 = cols.get(j + 1);
                                BufferedImage bufferedImage;
                                if (sourceImage != null) {
                                    bufferedImage = ImageManufacture.cropOutside(sourceImage, x1, y1, x2, y2);
                                } else {
                                    bufferedImage = ImageFileReaders.readFrame(sourceFormat, sourceFilename, x1, y1, x2, y2);
                                }
                                if (tiffTask == null || isCancelled()) {
                                    return false;
                                }
                                IIOMetadata metaData = getWriterMeta(null, bufferedImage, writer, param);
                                if (tiffTask == null || isCancelled()) {
                                    return false;
                                }
                                writer.writeToSequence(new IIOImage(bufferedImage, null, metaData), param);
                                updateLabel(total, (i + 1) * (j + 1));
                            }
                        }
                        writer.endWriteSequence();
                        out.flush();
                    }
                    writer.dispose();
                    return FileTools.rename(tmpFile, tFile);
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }

            }

            protected void updateLabel(final int total, final int number) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (tiffTask == null || tiffTask.isQuit() || tiffLoading == null) {
                            return;
                        }
                        tiffLoading.setInfo(MessageFormat.format(AppVariables.message("NumberImageWritten"),
                                number + "/" + total));
                        tiffLoading.setProgress(number * 1f / total);
                    }
                });
            }

            @Override
            protected void whenSucceeded() {
                popSuccessful();
                final ImageFramesViewerController controller
                        = (ImageFramesViewerController) openStage(CommonValues.ImageFramesViewerFxml);
                controller.selectSourceFile(tFile);
            }

        };
        tiffLoading = openHandlingStage(tiffTask, Modality.WINDOW_MODAL);
        Thread thread = new Thread(tiffTask);
        thread.setDaemon(true);
        thread.start();

    }

    @Override
    public boolean checkBeforeNextAction() {
        if (imageTask != null && !imageTask.isQuit()) {
            imageTask.cancel();
            imageTask = null;
        }
        if (pdfTask != null && !pdfTask.isQuit()) {
            pdfTask.cancel();
            pdfTask = null;
        }
        if (tiffTask != null && !tiffTask.isQuit()) {
            tiffTask.cancel();
            tiffTask = null;
        }
        return true;
    }

}
