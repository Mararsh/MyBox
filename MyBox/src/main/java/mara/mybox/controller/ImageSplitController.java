package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
import javafx.scene.control.ComboBox;
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
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.fxml.FxmlImageManufacture;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.image.ImageManufacture;
import mara.mybox.image.file.ImageFileReaders;
import mara.mybox.image.file.ImageFileWriters;
import static mara.mybox.image.file.ImageTiffFile.getPara;
import static mara.mybox.image.file.ImageTiffFile.getWriter;
import static mara.mybox.image.file.ImageTiffFile.getWriterMeta;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.PdfTools;
import mara.mybox.tools.PdfTools.PdfImageFormat;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;
import mara.mybox.value.CommonValues;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.font.PDFont;

/**
 * @Author Mara
 * @CreateDate 2018-8-8
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageSplitController extends ImageViewerController {

    private List<Integer> rows, cols;
    private int rowsNumber, colsNumber, width, height,
            marginSize, pageWidth, pageHeight, jpegQuality, threshold;
    protected SimpleBooleanProperty splitValid;
    private SplitMethod splitMethod;
    private LoadingController imageController, pdfController, tiffController;
    private SingletonTask imageTask, pdfTask, tiffTask;
    private boolean pdfImageSize;

    public static enum SplitMethod {
        Predefined, ByNumber, BySize, Customize
    }

    @FXML
    private ToggleGroup splitGroup, pdfSizeGroup;
    @FXML
    private FlowPane splitPredefinedPane, splitSizePane, splitNumberPane,
            splitCustomized1Pane, splitCustomized2Pane;
    @FXML
    private Button saveImagesButton, saveTiffButton, savePdfButton;
    @FXML
    private TextField rowsInput, colsInput, customizedRowsInput, customizedColsInput,
            widthInput, heightInput, customWidthInput, customHeightInput, authorInput, headerInput;
    @FXML
    private CheckBox displaySizeCheck, pageNumberCheck;
    @FXML
    protected ComboBox<String> MarginsBox, standardSizeBox, standardDpiBox, fontBox;
    @FXML
    private VBox splitOptionsBox, optionsBox, showBox;
    @FXML
    private HBox opBox;
    @FXML
    private Label promptLabel;

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
            logger.error(e.toString());
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            initCommon();
            initSplitTab();
            initPdfTab();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void afterSceneLoaded() {
        super.afterSceneLoaded();
        checkSplitMethod();
        checkPageSize();
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
                        .or(customWidthInput.styleProperty().isEqualTo(badStyle))
                        .or(customHeightInput.styleProperty().isEqualTo(badStyle))
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

    protected void initPdfTab() {

        pdfSizeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                checkPageSize();
            }
        });

        standardSizeBox.getItems().addAll(Arrays.asList("A4-" + message("Horizontal") + " (16k)  29.7cm x 21.0cm",
                "A4 (16k)  21.0cm x 29.7cm",
                "A5 (32k)  14.8cm x 21.0cm",
                "A6 (64k)  10.5cm x 14.8cm",
                "A3 (8k)   29.7cm x 42.0cm",
                "A2 (4k)   42.0cm x 59.4cm",
                "A1 (2k)   59.4cm x 84.1cm",
                "A0 (1k)   84.1cm x 118.9cm",
                "B5        17.6cm x 25.0cm",
                "B4	    25.0cm x 35.3cm",
                "B2	    35.3cm x 50.0cm",
                "C4	    22.9cm x 32.4cm",
                "C5	    16.2cm x 22.9cm",
                "C6	    11.4cm x 16.2cm"
        ));
        standardSizeBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov,
                    String oldValue, String newValue) {
                checkStandardValues();
            }
        });
        standardSizeBox.getSelectionModel().select(0);
        pdfImageSize = true;

        standardDpiBox.getItems().addAll(Arrays.asList(
                "72 dpi",
                "96 dpi",
                "150 dpi",
                "300 dpi",
                "450 dpi",
                "720 dpi",
                "120 dpi",
                "160 dpi",
                "240 dpi",
                "320 dpi"
        ));
        standardDpiBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov,
                    String oldValue, String newValue) {
                checkStandardValues();
            }
        });
        standardDpiBox.getSelectionModel().select(0);

        customWidthInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                checkPdfCustomValues();
            }
        });
        customHeightInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                checkPdfCustomValues();
            }
        });

        MarginsBox.getItems().addAll(Arrays.asList("20", "10", "15", "5", "25", "30"));
        MarginsBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov,
                    String oldValue, String newValue) {
                try {
                    marginSize = Integer.valueOf(newValue);
                    if (marginSize >= 0) {
                        FxmlControl.setEditorNormal(MarginsBox);
                    } else {
                        marginSize = 0;
                        FxmlControl.setEditorBadStyle(MarginsBox);
                    }

                } catch (Exception e) {
                    marginSize = 0;
                    FxmlControl.setEditorBadStyle(MarginsBox);
                }
            }
        });
        MarginsBox.getSelectionModel().select("20");

        if (fontBox != null) {
            fontBox.getItems().addAll(Arrays.asList(
                    "幼圆",
                    "仿宋",
                    "隶书",
                    "Helvetica",
                    "Courier",
                    "Times New Roman"
            ));
            fontBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov,
                        String oldValue, String newValue) {
                }
            });
            fontBox.getSelectionModel().select(0);

        }

        authorInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                AppVariables.setUserConfigValue("AuthorKey", newValue);
            }
        });
        authorInput.setText(AppVariables.getUserConfigValue("AuthorKey", System.getProperty("user.name")));

    }

    protected void checkPageSize() {
        standardSizeBox.setDisable(true);
        standardDpiBox.setDisable(true);
        customWidthInput.setDisable(true);
        customHeightInput.setDisable(true);
        customWidthInput.setStyle(null);
        customHeightInput.setStyle(null);
        pdfImageSize = false;

        RadioButton selected = (RadioButton) pdfSizeGroup.getSelectedToggle();
        if (AppVariables.message("ImagesSize").equals(selected.getText())) {
            pdfImageSize = true;
        } else if (AppVariables.message("StandardSize").equals(selected.getText())) {
            standardSizeBox.setDisable(false);
            standardDpiBox.setDisable(false);
            checkStandardValues();

        } else if (AppVariables.message("Custom").equals(selected.getText())) {
            customWidthInput.setDisable(false);
            customHeightInput.setDisable(false);
            checkPdfCustomValues();
        }

//        AppVariables.setUserConfigValue(ImageCombineSizeKey, selected.getText());
    }

    private int calculateCmPixels(float cm, int dpi) {
        return (int) Math.round(cm * dpi / 2.54);
    }

    protected void checkStandardValues() {
        String d = standardDpiBox.getSelectionModel().getSelectedItem();
        int dpi = 72;
        try {
            dpi = Integer.valueOf(d.substring(0, d.length() - 4));
        } catch (Exception e) {
        }
        String s = standardSizeBox.getSelectionModel().getSelectedItem();
        if (s.startsWith("A4-" + message("Horizontal"))) {
            pageWidth = calculateCmPixels(29.7f, dpi);
            pageHeight = calculateCmPixels(21.0f, dpi);
        } else {
            switch (s.substring(0, 2)) {
                case "A4":
                    pageWidth = calculateCmPixels(21.0f, dpi);
                    pageHeight = calculateCmPixels(29.7f, dpi);
                    break;
                case "A5":
                    pageWidth = calculateCmPixels(14.8f, dpi);
                    pageHeight = calculateCmPixels(21.0f, dpi);
                    break;
                case "A6":
                    pageWidth = calculateCmPixels(10.5f, dpi);
                    pageHeight = calculateCmPixels(14.8f, dpi);
                    break;
                case "A3":
                    pageWidth = calculateCmPixels(29.7f, dpi);
                    pageHeight = calculateCmPixels(42.0f, dpi);
                    break;
                case "A2":
                    pageWidth = calculateCmPixels(42.0f, dpi);
                    pageHeight = calculateCmPixels(59.4f, dpi);
                    break;
                case "A1":
                    pageWidth = calculateCmPixels(59.4f, dpi);
                    pageHeight = calculateCmPixels(84.1f, dpi);
                    break;

                case "A0":
                    pageWidth = calculateCmPixels(84.1f, dpi);
                    pageHeight = calculateCmPixels(118.9f, dpi);
                    break;
                case "B5":
                    pageWidth = calculateCmPixels(17.6f, dpi);
                    pageHeight = calculateCmPixels(25.0f, dpi);
                    break;
                case "B4":
                    pageWidth = calculateCmPixels(25.0f, dpi);
                    pageHeight = calculateCmPixels(35.3f, dpi);
                    break;
                case "B2":
                    pageWidth = calculateCmPixels(35.3f, dpi);
                    pageHeight = calculateCmPixels(50.0f, dpi);
                    break;
                case "C4":
                    pageWidth = calculateCmPixels(22.9f, dpi);
                    pageHeight = calculateCmPixels(32.4f, dpi);
                    break;
                case "C5":
                    pageWidth = calculateCmPixels(16.2f, dpi);
                    pageHeight = calculateCmPixels(22.9f, dpi);
                    break;
                case "C6":
                    pageWidth = calculateCmPixels(11.4f, dpi);
                    pageHeight = calculateCmPixels(16.2f, dpi);
                    break;
            }
        }
        customWidthInput.setText(pageWidth + "");
        customHeightInput.setText(pageHeight + "");
    }

    protected void checkPdfCustomValues() {

        RadioButton selected = (RadioButton) pdfSizeGroup.getSelectedToggle();
        if (!AppVariables.message("Custom").equals(selected.getText())) {
            return;
        }
        try {
            pageWidth = Integer.valueOf(customWidthInput.getText());
            if (pageWidth > 0) {
                customWidthInput.setStyle(null);
            } else {
                pageWidth = 0;
                customWidthInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            pageWidth = 0;
            customWidthInput.setStyle(badStyle);
        }

        try {
            pageHeight = Integer.valueOf(customHeightInput.getText());
            if (pageHeight > 0) {
                customHeightInput.setStyle(null);
            } else {
                pageHeight = 0;
                customHeightInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            pageHeight = 0;
            customHeightInput.setStyle(badStyle);
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
            logger.debug(e.toString());
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
            promptLabel.setText(comments);
            splitValid.set(true);
        } catch (Exception e) {
            logger.error(e.toString());
            splitValid.set(false);
        }

    }

    @Override
    public void drawMaskControls() {
        super.drawMaskControls();
        indicateSplit();
    }

    private File validationBeforeSave(List<FileChooser.ExtensionFilter> ext, String diagTitle) {
        if (image == null || !splitValid.getValue()
                || rows == null || cols == null
                || rows.size() < 1 || cols.size() < 1) {
            return null;
        }
        String prefix = null;
        if (sourceFile != null) {
            prefix = FileTools.getFilePrefix(sourceFile.getName());
        }
        final File tFile = chooseSaveFile(diagTitle, AppVariables.getUserConfigPath(targetPathKey),
                prefix, ext, true);
        if (tFile == null) {
            return null;
        }
        recordFileWritten(tFile);

        return tFile;
    }

    @FXML
    public void saveAsImagesAction(ActionEvent event) {
        if (image == null || rows == null || rows.isEmpty()
                || cols == null || cols.isEmpty()) {
            return;
        }
        final File tFile
                = validationBeforeSave(CommonFxValues.ImageExtensionFilter, message("FilePrefixInput"));
        if (tFile == null) {
            return;
        }
        if (imageTask != null) {
            imageTask.cancel();
            imageController = null;
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
                        if (imageTask == null || !imageTask.isRunning() || imageController == null) {
                            return;
                        }
                        imageController.setInfo(MessageFormat.format(AppVariables.message("NumberFileGenerated"),
                                number + "/" + total, "\"" + fileName + "\""));
                        imageController.setProgress(number * 1f / total);
                    }
                });
            }

            @Override
            protected void whenSucceeded() {
                multipleFilesGenerated(fileNames);
            }

        };
        imageController = openHandlingStage(imageTask, Modality.WINDOW_MODAL);
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
        final File tFile = validationBeforeSave(CommonFxValues.PdfExtensionFilter, null);
        if (tFile == null) {
            return;
        }
        final boolean inPageNumber = pageNumberCheck.isSelected();
        final String header = headerInput.getText();
        final String fontName = fontBox.getSelectionModel().getSelectedItem();
        if (pdfTask != null) {
            pdfTask.cancel();
            pdfController = null;
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
                        PDFont font = PdfTools.getFont(document, fontName);
                        PDDocumentInformation info = new PDDocumentInformation();
                        info.setCreationDate(Calendar.getInstance());
                        info.setModificationDate(Calendar.getInstance());
                        info.setProducer("MyBox v" + CommonValues.AppVersion);

                        info.setAuthor(authorInput.getText());
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
                                PdfTools.writePage(document, font, sourceFormat, target,
                                        ++count, total, PdfImageFormat.Original,
                                        threshold, jpegQuality, pdfImageSize, inPageNumber,
                                        pageWidth, pageHeight, marginSize, header, true);

                                updateLabel(total, (i + 1) * (j + 1));
                            }
                        }
                        document.save(tmpFile);
                        document.close();
                    }

                    if (tFile.exists()) {
                        tFile.delete();
                    }
                    tmpFile.renameTo(tFile);
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            protected void updateLabel(final int total, final int number) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (pdfTask == null || !pdfTask.isRunning() || pdfController == null) {
                            return;
                        }
                        pdfController.setInfo(MessageFormat.format(AppVariables.message("NumberPageWritten"),
                                number + "/" + total));
                        pdfController.setProgress(number * 1f / total);
                    }
                });
            }

            @Override
            protected void whenSucceeded() {
                popSuccessful();
                FxmlStage.openPdfViewer(null, tFile);
            }

        };
        pdfController = openHandlingStage(pdfTask, Modality.WINDOW_MODAL);
        Thread thread = new Thread(pdfTask);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    protected void saveAsTiffAction(ActionEvent event) {
        if (image == null || rows == null || rows.isEmpty()
                || cols == null || cols.isEmpty()) {
            return;
        }
        final File tFile = validationBeforeSave(CommonFxValues.TiffExtensionFilter, null);
        if (tFile == null) {
            return;
        }
        if (tiffTask != null) {
            tiffTask.cancel();
            tiffController = null;
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
                    try {
                        if (tFile.exists()) {
                            tFile.delete();
                        }
                        tmpFile.renameTo(tFile);
                    } catch (Exception e) {
                        return false;
                    }
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }

            }

            protected void updateLabel(final int total, final int number) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (tiffTask == null || !tiffTask.isRunning() || tiffController == null) {
                            return;
                        }
                        tiffController.setInfo(MessageFormat.format(AppVariables.message("NumberImageWritten"),
                                number + "/" + total));
                        tiffController.setProgress(number * 1f / total);
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
        tiffController = openHandlingStage(tiffTask, Modality.WINDOW_MODAL);
        Thread thread = new Thread(tiffTask);
        thread.setDaemon(true);
        thread.start();

    }

    @Override
    public boolean checkBeforeNextAction() {
        if (imageTask != null && imageTask.isRunning()) {
            imageTask.cancel();
            imageTask = null;
        }
        if (pdfTask != null && pdfTask.isRunning()) {
            pdfTask.cancel();
            pdfTask = null;
        }
        if (tiffTask != null && tiffTask.isRunning()) {
            tiffTask.cancel();
            tiffTask = null;
        }
        return true;
    }

}
