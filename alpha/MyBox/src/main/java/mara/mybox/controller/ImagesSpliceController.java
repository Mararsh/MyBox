package mara.mybox.controller;

import java.sql.Connection;
import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import mara.mybox.bufferedimage.CombineTools;
import mara.mybox.bufferedimage.ImageCombine;
import mara.mybox.bufferedimage.ImageCombine.ArrayType;
import mara.mybox.bufferedimage.ImageCombine.CombineSizeType;
import mara.mybox.bufferedimage.ImageInformation;
import mara.mybox.db.DerbyBase;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2018-8-11
 * @License Apache License Version 2.0
 */
public class ImagesSpliceController extends BaseController {

    protected ImageCombine imageCombine;
    protected int columns, interval, margins,
            eachWidthValue, eachHeightValue, totalWidthValue, totalHeightValue;

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
    protected ComboBox<String> columnsSelector, intervalSelector, marginsSelector;
    @FXML
    protected ControlColorSet colorController;
    @FXML
    protected BaseImageController viewController;
    @FXML
    protected VBox viewBox, sourceBox;

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

            initArray();
            initSize();
            initOthers();

            saveButton.disableProperty().bind(viewController.imageView.imageProperty().isNull());

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    private void initArray() {
        try {
            columns = UserConfig.getInt(baseName + "Columns", 2);
            if (columns <= 0) {
                columns = 2;
            }
            columnsSelector.getItems().addAll(Arrays.asList("2", "3", "4", "5", "6", "7", "8", "9", "10"));
            columnsSelector.setValue(columns + "");
            columnsSelector.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov, String oldValue, String newValue) {
                    checkArray();
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
            arrayGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {
                    checkArray();
                }
            });
            checkArray();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    private boolean checkArray() {
        if (arrayColumnRadio.isSelected() || arrayRowRadio.isSelected()) {
            columnsSelector.setDisable(true);
            ValidationTools.setEditorNormal(columnsSelector);
            return true;
        }
        columnsSelector.setDisable(false);
        int v;
        try {
            v = Integer.parseInt(columnsSelector.getValue());
        } catch (Exception e) {
            v = -1;
        }
        if (v > 0) {
            columns = v;
            ValidationTools.setEditorNormal(columnsSelector);
            return true;
        } else {
            ValidationTools.setEditorBadStyle(columnsSelector);
            popError(message("InvalidParameter") + ": " + message("ColumnsNumber"));
            return false;
        }
    }

    private void initSize() {
        try {
            eachWidthValue = UserConfig.getInt(baseName + "EachWidth", 500);
            if (eachWidthValue <= 0) {
                eachWidthValue = 500;
            }
            eachWidthInput.setText(eachWidthValue + "");
            eachWidthInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v, String ov, String nv) {
                    checkEachWidthValue();
                }
            });

            eachHeightValue = UserConfig.getInt(baseName + "EachHeight", 500);
            if (eachHeightValue <= 0) {
                eachHeightValue = 500;
            }
            eachHeightInput.setText(eachHeightValue + "");
            eachHeightInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v, String ov, String nv) {
                    checkEachHeightValue();
                }
            });

            totalWidthValue = UserConfig.getInt(baseName + "TotalWidth", 1000);
            if (totalWidthValue <= 0) {
                totalWidthValue = 1000;
            }
            totalWidthInput.setText(totalWidthValue + "");
            totalWidthInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v, String ov, String nv) {
                    checkTotalWidthValue();
                }
            });

            totalHeightValue = UserConfig.getInt(baseName + "TotalHeight", 1000);
            if (totalHeightValue <= 0) {
                totalHeightValue = 1000;
            }
            totalHeightInput.setText(totalHeightValue + "");
            totalHeightInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v, String ov, String nv) {
                    checkTotalHeightValue();
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
            sizeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> v, Toggle ov, Toggle nv) {
                    checkSize();
                }
            });
            checkSize();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    private boolean checkSize() {
        totalWidthInput.setDisable(true);
        totalWidthInput.setStyle(null);
        totalHeightInput.setDisable(true);
        totalHeightInput.setStyle(null);
        eachWidthInput.setDisable(true);
        eachWidthInput.setStyle(null);
        eachHeightInput.setDisable(true);
        eachHeightInput.setStyle(null);
        if (keepSizeRadio.isSelected()
                || sizeBiggerRadio.isSelected()
                || sizeSmallerRadio.isSelected()) {
            return true;
        } else if (eachWidthRadio.isSelected()) {
            eachWidthInput.setDisable(false);
            return checkEachWidthValue();
        } else if (eachHeightRadio.isSelected()) {
            eachHeightInput.setDisable(false);
            return checkEachHeightValue();
        } else if (totalWidthRadio.isSelected()) {
            totalWidthInput.setDisable(false);
            return checkTotalWidthValue();
        } else if (totalHeightRadio.isSelected()) {
            totalHeightInput.setDisable(false);
            return checkTotalHeightValue();
        }
        return false;
    }

    private boolean checkEachWidthValue() {
        int v;
        try {
            v = Integer.parseInt(eachWidthInput.getText());
        } catch (Exception e) {
            v = -1;
        }
        if (v > 0) {
            eachWidthValue = v;
            eachWidthInput.setStyle(null);
            return true;
        } else {
            eachWidthInput.setStyle(UserConfig.badStyle());
            popError(message("InvalidParameter") + ": " + message("EachWidth"));
            return false;
        }
    }

    private boolean checkEachHeightValue() {
        int v;
        try {
            v = Integer.parseInt(eachHeightInput.getText());
        } catch (Exception e) {
            v = -1;
        }
        if (v > 0) {
            eachHeightValue = v;
            eachHeightInput.setStyle(null);
            return true;
        } else {
            eachHeightInput.setStyle(UserConfig.badStyle());
            popError(message("InvalidParameter") + ": " + message("EachHeight"));
            return false;
        }
    }

    private boolean checkTotalWidthValue() {
        int v;
        try {
            v = Integer.parseInt(totalWidthInput.getText());
        } catch (Exception e) {
            v = -1;
        }
        if (v > 0) {
            totalWidthValue = v;
            totalWidthInput.setStyle(null);
            return true;
        } else {
            totalWidthInput.setStyle(UserConfig.badStyle());
            popError(message("InvalidParameter") + ": " + message("TotalWidth"));
            return false;
        }
    }

    private boolean checkTotalHeightValue() {
        int v;
        try {
            v = Integer.parseInt(totalHeightInput.getText());
        } catch (Exception e) {
            v = -1;
        }
        if (v > 0) {
            totalHeightValue = v;
            totalHeightInput.setStyle(null);
            return true;
        } else {
            totalHeightInput.setStyle(UserConfig.badStyle());
            popError(message("InvalidParameter") + ": " + message("TotalHeight"));
            return false;
        }
    }

    private void initOthers() {
        try {
            interval = UserConfig.getInt(baseName + "Interval", 0);
            intervalSelector.getItems().addAll(
                    Arrays.asList("0", "5", "-5", "1", "-1", "10", "-10", "15", "-15", "20", "-20", "30", "-30"));
            intervalSelector.setValue(interval + "");
            intervalSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v, String ov, String nv) {
                    checkInterval();
                }
            });

            margins = UserConfig.getInt(baseName + "Margins", 0);
            marginsSelector.getItems().addAll(Arrays.asList("0", "5", "-5", "10", "-10", "20", "-20", "30", "-30"));
            marginsSelector.setValue(margins + "");
            marginsSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v, String ov, String nv) {
                    checkMargins();
                }
            });

            colorController.init(this, baseName + "Color");

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    private boolean checkInterval() {
        try {
            interval = Integer.parseInt(intervalSelector.getValue());
            ValidationTools.setEditorNormal(intervalSelector);
            return true;
        } catch (Exception e) {
            ValidationTools.setEditorBadStyle(intervalSelector);
            popError(message("InvalidParameter") + ": " + message("Interval"));
            return false;
        }
    }

    private boolean checkMargins() {
        try {
            margins = Integer.parseInt(marginsSelector.getValue());
            ValidationTools.setEditorNormal(marginsSelector);
            return true;
        } catch (Exception e) {
            ValidationTools.setEditorBadStyle(marginsSelector);
            popError(message("InvalidParameter") + ": " + message("Margins"));
            return false;
        }
    }

    public boolean checkOptions() {
        if (tableController.tableData == null || tableController.tableData.isEmpty()
                || !checkArray() || !checkSize()
                || !checkInterval() || !checkMargins()) {
            return false;
        }
        try (Connection conn = DerbyBase.getConnection()) {
            if (arrayColumnRadio.isSelected()) {
                imageCombine.setArrayType(ArrayType.SingleColumn);
                UserConfig.setString(conn, baseName + "ArrayType", "SingleColumn");
            } else if (arrayRowRadio.isSelected()) {
                imageCombine.setArrayType(ArrayType.SingleRow);
                UserConfig.setString(conn, baseName + "ArrayType", "SingleRow");
            } else if (arrayColumnsRadio.isSelected()) {
                imageCombine.setArrayType(ArrayType.ColumnsNumber);
                imageCombine.setColumnsValue(columns);
                UserConfig.setString(conn, baseName + "ArrayType", "ColumnsNumber");
                UserConfig.setInt(conn, baseName + "Columns", columns);
            }
            if (keepSizeRadio.isSelected()) {
                imageCombine.setSizeType(CombineSizeType.KeepSize);
                UserConfig.setString(conn, baseName + "SizeType", "KeepSize");
            } else if (sizeBiggerRadio.isSelected()) {
                imageCombine.setSizeType(CombineSizeType.AlignAsBigger);
                UserConfig.setString(conn, baseName + "SizeType", "AlignAsBigger");
            } else if (sizeSmallerRadio.isSelected()) {
                imageCombine.setSizeType(CombineSizeType.AlignAsSmaller);
                UserConfig.setString(conn, baseName + "SizeType", "AlignAsSmaller");
            } else if (eachWidthRadio.isSelected()) {
                imageCombine.setSizeType(CombineSizeType.EachWidth);
                imageCombine.setEachWidthValue(eachWidthValue);
                UserConfig.setString(conn, baseName + "SizeType", "EachWidth");
                UserConfig.setInt(conn, baseName + "EachWidth", eachWidthValue);
            } else if (eachHeightRadio.isSelected()) {
                imageCombine.setSizeType(CombineSizeType.EachHeight);
                imageCombine.setEachHeightValue(eachHeightValue);
                UserConfig.setString(conn, baseName + "SizeType", "EachHeight");
                UserConfig.setInt(conn, baseName + "EachHeight", eachHeightValue);
            } else if (totalWidthRadio.isSelected()) {
                imageCombine.setSizeType(CombineSizeType.TotalWidth);
                imageCombine.setTotalWidthValue(totalWidthValue);
                UserConfig.setString(conn, baseName + "SizeType", "TotalWidth");
                UserConfig.setInt(conn, baseName + "TotalWidth", totalWidthValue);
            } else if (totalHeightRadio.isSelected()) {
                imageCombine.setSizeType(CombineSizeType.TotalHeight);
                imageCombine.setTotalHeightValue(totalHeightValue);
                UserConfig.setString(conn, baseName + "SizeType", "TotalHeight");
                UserConfig.setInt(conn, baseName + "TotalHeight", totalHeightValue);
            }
            imageCombine.setIntervalValue(interval);
            UserConfig.setInt(conn, baseName + "Interval", interval);
            imageCombine.setMarginsValue(margins);
            UserConfig.setInt(conn, baseName + "Margins", margins);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @FXML
    @Override
    public void goAction() {
        if (!checkOptions()) {
            return;
        }
        List<ImageInformation> imageInfos = tableController.selectedItems();
        if (imageInfos == null || imageInfos.isEmpty()) {
            imageInfos = tableController.tableData;
        }
        if (imageInfos == null || imageInfos.isEmpty()) {
            popError(message("SelectToHandle"));
            return;
        }
        List<ImageInformation> infos = imageInfos;
        imageCombine.setBgColor(colorController.color());
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            Image image;

            @Override
            protected boolean handle() {
                if (imageCombine.getArrayType() == ArrayType.SingleColumn) {
                    image = CombineTools.combineSingleColumn(this, imageCombine, infos, false, true);
                } else if (imageCombine.getArrayType() == ArrayType.SingleRow) {
                    image = CombineTools.combineSingleRow(this, imageCombine, infos, false, true);
                } else if (imageCombine.getArrayType() == ArrayType.ColumnsNumber) {
                    image = CombineTools.combineImagesColumns(this, imageCombine, infos);
                } else {
                    image = null;
                }
                return image != null;
            }

            @Override
            protected void whenSucceeded() {
                viewController.image = image;
                viewController.imageView.setImage(image);
                viewController.setZoomStep(image);
                viewController.fitSize();
                viewController.imageLabel.setText(Languages.message("CombinedSize") + ": "
                        + (int) image.getWidth() + "x" + (int) image.getHeight());
            }

        };
        start(task);
    }

    @FXML
    @Override
    public void saveAction() {
        viewController.saveAsAction();
    }

    @FXML
    @Override
    public boolean menuAction() {
        if (viewBox.isFocused() || viewBox.isFocusWithin()) {
            viewController.menuAction();
            return true;
        } else if (sourceBox.isFocused() || sourceBox.isFocusWithin()) {
            tableController.menuAction();
            return true;
        }
        return super.menuAction();
    }

    @FXML
    @Override
    public boolean popAction() {
        if (viewBox.isFocused() || viewBox.isFocusWithin()) {
            viewController.popAction();
            return true;
        } else if (sourceBox.isFocused() || sourceBox.isFocusWithin()) {
            tableController.popAction();
            return true;
        }
        return super.popAction();
    }

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (viewBox.isFocused() || viewBox.isFocusWithin()) {
            if (viewController.keyEventsFilter(event)) {
                return true;
            }
        } else if (sourceBox.isFocused() || sourceBox.isFocusWithin()) {
            if (tableController.keyEventsFilter(event)) {
                return true;
            }
        }
        return super.keyEventsFilter(event);
    }

    /*
        static methods
     */
    public static ImagesSpliceController open(List<ImageInformation> imageInfos) {
        try {
            ImagesSpliceController controller = (ImagesSpliceController) WindowTools.openStage(Fxmls.ImagesSpliceFxml);
            controller.tableController.tableData.setAll(imageInfos);;
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
