package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import mara.mybox.bufferedimage.CombineTools;
import mara.mybox.bufferedimage.ImageCombine;
import mara.mybox.bufferedimage.ImageCombine.ArrayType;
import mara.mybox.bufferedimage.ImageCombine.CombineSizeType;
import mara.mybox.bufferedimage.ImageInformation;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2018-8-11
 * @License Apache License Version 2.0
 */
public class ImagesSpliceController extends ImageViewerController {

    protected ObservableList<ImageInformation> tableData;
    protected TableView<ImageInformation> tableView;
    protected ImageCombine imageCombine;

    @FXML
    protected ControlImagesTable tableController;
    @FXML
    protected ToggleGroup sizeGroup, arrayGroup;
    @FXML
    protected RadioButton arrayColumnRadio, arrayRowRadio, arrayColumnsRadio, keepSizeRadio, sizeBiggerRadio,
            sizeSmallerRadio, eachWidthRadio, eachHeightRadio, totalWidthRadio, totalHeightRadio;
    @FXML
    protected TextField totalWidthInput, totalHeightInput, eachWidthInput, eachHeightInput;
    @FXML
    protected ComboBox<String> columnsBox, intervalBox, MarginsBox;
    @FXML
    protected ControlColorSet colorSetController;
    @FXML
    protected HBox opBox;
    @FXML
    protected CheckBox openCheck;

    public ImagesSpliceController() {
        baseTitle = Languages.message("ImagesSplice");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            imageCombine = new ImageCombine();

            tableController.parentController = this;
            tableController.parentFxml = myFxml;

            tableData = tableController.tableData;
            tableView = tableController.tableView;

            tableData.addListener((ListChangeListener.Change<? extends ImageInformation> change) -> {
                if (!tableController.hasSampled()) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            okAction();
                        }
                    });
                }
            });

            initArraySection();
            initSizeSection();
            initTargetSection();

            saveButton.disableProperty().bind(Bindings.isEmpty(tableData));
            leftPaneCheck.setSelected(true);
            rightPaneCheck.setSelected(true);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    private void initArraySection() {
        try {
            columnsBox.getItems().addAll(Arrays.asList("2", "3", "4", "5", "6", "7", "8", "9", "10"));
            columnsBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov, String oldValue, String newValue) {
                    try {
                        int columnsValue = Integer.parseInt(newValue);
                        if (columnsValue > 0) {
                            imageCombine.setColumnsValue(columnsValue);
                            UserConfig.setString(baseName + "Columns", columnsValue + "");
                            ValidationTools.setEditorNormal(columnsBox);
                        } else {
                            imageCombine.setColumnsValue(-1);
                            ValidationTools.setEditorBadStyle(columnsBox);
                        }

                    } catch (Exception e) {
                        imageCombine.setColumnsValue(-1);
                        ValidationTools.setEditorBadStyle(columnsBox);
                    }
                }
            });
            columnsBox.getSelectionModel().select(UserConfig.getString(baseName + "Columns", "2"));

            intervalBox.getItems().addAll(Arrays.asList("5", "10", "15", "20", "1", "3", "30", "0"));
            intervalBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov,
                        String oldValue, String newValue) {
                    try {
                        int intervalValue = Integer.parseInt(newValue);
                        if (intervalValue >= 0) {
                            imageCombine.setIntervalValue(intervalValue);
                            UserConfig.setString(baseName + "Interval", intervalValue + "");
                            ValidationTools.setEditorNormal(intervalBox);
                        } else {
                            ValidationTools.setEditorBadStyle(intervalBox);
                        }

                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(intervalBox);
                    }
                }
            });
            intervalBox.getSelectionModel().select(UserConfig.getString(baseName + "Interval", "5"));

            MarginsBox.getItems().addAll(Arrays.asList("5", "10", "15", "20", "1", "3", "30", "0"));
            MarginsBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov,
                        String oldValue, String newValue) {
                    try {
                        int MarginsValue = Integer.parseInt(newValue);
                        if (MarginsValue >= 0) {
                            imageCombine.setMarginsValue(MarginsValue);
                            UserConfig.setString(baseName + "Margin", MarginsValue + "");
                            ValidationTools.setEditorNormal(MarginsBox);
                        } else {
                            ValidationTools.setEditorBadStyle(MarginsBox);
                        }

                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(MarginsBox);
                    }
                }
            });
            MarginsBox.getSelectionModel().select(UserConfig.getString(baseName + "Margin", "5"));

            colorSetController.init(this, baseName + "Color");
            imageCombine.setBgColor(colorSetController.color());
            colorSetController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue<? extends Paint> observable,
                        Paint oldValue, Paint newValue) {
                    imageCombine.setBgColor((Color) newValue);
                }
            });

            arrayGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {
                    RadioButton selected = (RadioButton) arrayGroup.getSelectedToggle();
                    if (Languages.message("SingleColumn").equals(selected.getText())) {
                        imageCombine.setArrayType(ArrayType.SingleColumn);
                        columnsBox.setDisable(true);
                        UserConfig.setString(baseName + "ArrayType", "SingleColumn");
                    } else if (Languages.message("SingleRow").equals(selected.getText())) {
                        imageCombine.setArrayType(ArrayType.SingleRow);
                        columnsBox.setDisable(true);
                        UserConfig.setString(baseName + "ArrayType", "SingleRow");
                    } else if (Languages.message("ColumnsNumber").equals(selected.getText())) {
                        imageCombine.setArrayType(ArrayType.ColumnsNumber);
                        columnsBox.setDisable(false);
                        UserConfig.setString(baseName + "ArrayType", "ColumnsNumber");
                    }
                }
            });
            String arraySelect = UserConfig.getString(baseName + "ArrayType", "SingleColumn");
            switch (arraySelect) {
                case "SingleColumn":
                    arrayColumnRadio.setSelected(true);
                    break;
                case "SingleRow":
                    arrayRowRadio.setSelected(true);
                    break;
                case "ColumnsNumber":
                    arrayColumnsRadio.setSelected(true);
                    break;
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    private void initSizeSection() {
        try {
            eachWidthInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkEachWidthValue();
                }
            });
            eachWidthInput.setText(UserConfig.getString(baseName + "EachWidth", ""));

            eachHeightInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkEachHeightValue();
                }
            });
            eachHeightInput.setText(UserConfig.getString(baseName + "EachHeight", ""));

            totalWidthInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkTotalWidthValue();
                }
            });
            totalWidthInput.setText(UserConfig.getString(baseName + "TotalWidth", ""));

            totalHeightInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkTotalHeightValue();
                }
            });
            totalHeightInput.setText(UserConfig.getString(baseName + "TotalHeight", ""));

            sizeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    totalWidthInput.setDisable(true);
                    totalWidthInput.setStyle(null);
                    totalHeightInput.setDisable(true);
                    totalHeightInput.setStyle(null);
                    eachWidthInput.setDisable(true);
                    eachWidthInput.setStyle(null);
                    eachHeightInput.setDisable(true);
                    eachHeightInput.setStyle(null);
                    RadioButton selected = (RadioButton) sizeGroup.getSelectedToggle();
                    if (Languages.message("KeepSize").equals(selected.getText())) {
                        imageCombine.setSizeType(CombineSizeType.KeepSize);
                        UserConfig.setString(baseName + "SizeType", "KeepSize");
                    } else if (Languages.message("AlignAsBigger").equals(selected.getText())) {
                        imageCombine.setSizeType(CombineSizeType.AlignAsBigger);
                        UserConfig.setString(baseName + "SizeType", "AlignAsBigger");
                    } else if (Languages.message("AlignAsSmaller").equals(selected.getText())) {
                        imageCombine.setSizeType(CombineSizeType.AlignAsSmaller);
                        UserConfig.setString(baseName + "SizeType", "AlignAsSmaller");
                    } else if (Languages.message("EachWidth").equals(selected.getText())) {
                        imageCombine.setSizeType(CombineSizeType.EachWidth);
                        eachWidthInput.setDisable(false);
                        checkEachWidthValue();
                        UserConfig.setString(baseName + "SizeType", "EachWidth");
                    } else if (Languages.message("EachHeight").equals(selected.getText())) {
                        imageCombine.setSizeType(CombineSizeType.EachHeight);
                        eachHeightInput.setDisable(false);
                        checkEachHeightValue();
                        UserConfig.setString(baseName + "SizeType", "EachHeight");
                    } else if (Languages.message("TotalWidth").equals(selected.getText())) {
                        imageCombine.setSizeType(CombineSizeType.TotalWidth);
                        totalWidthInput.setDisable(false);
                        checkTotalWidthValue();
                        UserConfig.setString(baseName + "SizeType", "TotalWidth");
                    } else if (Languages.message("TotalHeight").equals(selected.getText())) {
                        imageCombine.setSizeType(CombineSizeType.TotalHeight);
                        totalHeightInput.setDisable(false);
                        checkTotalHeightValue();
                        UserConfig.setString(baseName + "SizeType", "TotalHeight");
                    }
                }
            });
            String arraySelect = UserConfig.getString(baseName + "SizeType", "KeepSize");
            switch (arraySelect) {
                case "KeepSize":
                    keepSizeRadio.setSelected(true);
                    break;
                case "AlignAsBigger":
                    sizeBiggerRadio.setSelected(true);
                    break;
                case "AlignAsSmaller":
                    sizeSmallerRadio.setSelected(true);
                    break;
                case "EachWidth":
                    eachWidthRadio.setSelected(true);
                    break;
                case "EachHeight":
                    eachHeightRadio.setSelected(true);
                    break;
                case "TotalWidth":
                    totalWidthRadio.setSelected(true);
                    break;
                case "TotalHeight":
                    totalHeightRadio.setSelected(true);
                    break;
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    private void checkEachWidthValue() {
        try {
            int eachWidthValue = Integer.parseInt(eachWidthInput.getText());
            if (eachWidthValue > 0) {
                imageCombine.setEachWidthValue(eachWidthValue);
                eachWidthInput.setStyle(null);
                UserConfig.setString(baseName + "EachWidth", eachWidthValue + "");
            } else {
                imageCombine.setEachWidthValue(-1);
                eachWidthInput.setStyle(UserConfig.badStyle());
            }
        } catch (Exception e) {
            imageCombine.setEachWidthValue(-1);
            eachWidthInput.setStyle(UserConfig.badStyle());
        }
    }

    private void checkEachHeightValue() {
        try {
            int eachHeightValue = Integer.parseInt(eachHeightInput.getText());
            if (eachHeightValue > 0) {
                imageCombine.setEachHeightValue(eachHeightValue);
                eachHeightInput.setStyle(null);
                UserConfig.setString(baseName + "EachHeight", eachHeightValue + "");
            } else {
                imageCombine.setEachHeightValue(-1);
                eachHeightInput.setStyle(UserConfig.badStyle());
            }
        } catch (Exception e) {
            imageCombine.setEachHeightValue(-1);
            eachHeightInput.setStyle(UserConfig.badStyle());
        }
    }

    private void checkTotalWidthValue() {
        try {
            int totalWidthValue = Integer.parseInt(totalWidthInput.getText());
            if (totalWidthValue > 0) {
                imageCombine.setTotalWidthValue(totalWidthValue);
                totalWidthInput.setStyle(null);
                UserConfig.setString(baseName + "TotalWidth", totalWidthValue + "");
            } else {
                imageCombine.setTotalWidthValue(-1);
                totalWidthInput.setStyle(UserConfig.badStyle());
            }
        } catch (Exception e) {
            imageCombine.setTotalWidthValue(-1);
            totalWidthInput.setStyle(UserConfig.badStyle());
        }
    }

    private void checkTotalHeightValue() {
        try {
            int totalHeightValue = Integer.parseInt(totalHeightInput.getText());
            if (totalHeightValue > 0) {
                imageCombine.setTotalHeightValue(totalHeightValue);
                totalHeightInput.setStyle(null);
                UserConfig.setString(baseName + "TotalHeight", totalHeightValue + "");
            } else {
                imageCombine.setTotalHeightValue(-1);
                totalHeightInput.setStyle(UserConfig.badStyle());
            }
        } catch (Exception e) {
            imageCombine.setTotalHeightValue(-1);
            totalHeightInput.setStyle(UserConfig.badStyle());
        }
    }

    public void initTargetSection() {
        try {
            opBox.disableProperty().bind(Bindings.isEmpty(tableData).
                    or(tableController.hasSampled)
            );

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void okAction() {
        if (tableData == null || tableData.isEmpty()
                || totalWidthInput.getStyle().equals(UserConfig.badStyle())
                || totalHeightInput.getStyle().equals(UserConfig.badStyle())
                || eachWidthInput.getStyle().equals(UserConfig.badStyle())
                || eachHeightInput.getStyle().equals(UserConfig.badStyle())) {
            image = null;
            imageView.setImage(null);
            imageLabel.setText("");
            return;
        }
        if (task != null && !task.isQuit()) {
            return;
        }
        task = new SingletonCurrentTask<Void>(this) {
            @Override
            protected boolean handle() {
                if (imageCombine.getArrayType() == ArrayType.SingleColumn) {
                    image = CombineTools.combineSingleColumn(imageCombine, tableData, false, true);
                } else if (imageCombine.getArrayType() == ArrayType.SingleRow) {
                    image = CombineTools.combineSingleRow(imageCombine, tableData, false, true);
                } else if (imageCombine.getArrayType() == ArrayType.ColumnsNumber) {
                    image = combineImagesColumns(tableData);
                } else {
                    image = null;
                }
                return image != null;
            }

            @Override
            protected void whenSucceeded() {
                imageView.setImage(image);
                setZoomStep(image);
                fitSize();
                imageLabel.setText(Languages.message("CombinedSize") + ": "
                        + (int) image.getWidth() + "x" + (int) image.getHeight());
            }

        };
        start(task);
    }

    private Image combineImagesColumns(List<ImageInformation> imageInfos) {
        if (imageInfos == null || imageInfos.isEmpty() || imageCombine.getColumnsValue() <= 0) {
            return null;
        }
        try {
            List<ImageInformation> rowImages = new ArrayList<>();
            List<ImageInformation> rows = new ArrayList<>();
            for (ImageInformation imageInfo : imageInfos) {
                rowImages.add(imageInfo);
                if (rowImages.size() == imageCombine.getColumnsValue()) {
                    Image rowImage = CombineTools.combineSingleRow(imageCombine, rowImages, true, false);
                    rows.add(new ImageInformation(rowImage));
                    rowImages = new ArrayList<>();
                }
            }
            if (!rowImages.isEmpty()) {
                Image rowImage = CombineTools.combineSingleRow(imageCombine, rowImages, true, false);
                rows.add(new ImageInformation(rowImage));
            }
            Image newImage = CombineTools.combineSingleColumn(imageCombine, rows, false, true);
            return newImage;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public void load(List<ImageInformation> imageInfos) {
        if (imageInfos == null || imageInfos.isEmpty()) {
            return;
        }
        if (task != null && !task.isQuit()) {
            return;
        }
        task = new SingletonCurrentTask<Void>(this) {

            List<ImageInformation> data;

            @Override
            protected boolean handle() {
                try {
                    data = new ArrayList<>();
                    for (ImageInformation imageInfo : imageInfos) {
                        Image iimage = imageInfo.loadImage();
                        if (iimage == null) {
                            continue;
                        }
                        data.add(new ImageInformation(iimage));
                    }
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                tableController.tableData.setAll(data);
            }

        };
        start(task);

    }

    @FXML
    @Override
    public void saveAction() {
        saveAsAction();
    }

    /*
        static methods
     */
    public static ImagesSpliceController open(List<ImageInformation> imageInfos) {
        try {
            ImagesSpliceController controller = (ImagesSpliceController) WindowTools.openStage(Fxmls.ImagesSpliceFxml);
            controller.load(imageInfos);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
