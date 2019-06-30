package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import mara.mybox.controller.base.ImageBatchBaseController;
import mara.mybox.data.VisitHistory;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.value.AppVaribles;
import mara.mybox.image.ImageAttributes;
import mara.mybox.tools.SystemTools;
import static mara.mybox.value.AppVaribles.getMessage;
import static mara.mybox.value.AppVaribles.logger;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-7-5
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageConverterBatchController12 extends ImageBatchBaseController {

    public ImageAttributes imageAttributes = new ImageAttributes();
    public boolean noRatio;

    @FXML
    public ComboBox<String> rgbSelector, cmykSelector, cieSelector, othersSelector;
    @FXML
    public ToggleGroup fileFormatGroup, csGroup, QualityGroup, CompressionGroup, binaryGroup;
    @FXML
    public RadioButton RGB, ARGB, Gray, Binary, fullQuality;
    @FXML
    public TextField iccInput, qualityInput, thresholdInput;
    @FXML
    public HBox qualityBox, compressBox, colorBox;
    @FXML
    public RadioButton rawSelect, pcxSelect;
    @FXML
    public CheckBox ditherCheck;
    @FXML
    public ToggleGroup ratioGroup;
    @FXML
    public CheckBox keepCheck, alphaCheck;
    @FXML
    public HBox ratioBox, ratioBaseBox, sizeBox;
    @FXML
    public Button iccSelectButton;

    public ImageConverterBatchController12() {
        baseTitle = AppVaribles.getMessage("ImageConverterBatch");

    }

    @Override
    public void initOptionsSection() {
        rgbSelector.getItems().addAll(Arrays.asList(
                "sRGB", "Apple RGB", "Adebo RGB", "YCbCr", "HLS", "HSV"
        ));
        rgbSelector.getSelectionModel().select(0);

        cmykSelector.getItems().addAll(Arrays.asList(
                "ECI CMYK", "CMY"
        ));
        cmykSelector.getSelectionModel().select(0);

        cieSelector.getItems().addAll(Arrays.asList(
                "CIE XYZ", "CIE Lab", "CIE Luv", "CIE Yxy"
        ));
        cieSelector.getSelectionModel().select(0);

        othersSelector.getItems().addAll(Arrays.asList(
                getMessage("ShadesOfGray"), getMessage("BlackOrWhite")
        ));
        othersSelector.getSelectionModel().select(0);

    }

    @Override
    public void initializeNext2() {
        try {
//            startButton.disableProperty().bind(
//                    Bindings.isEmpty(tableView.getItems())
//                            .or(Bindings.isEmpty(targetPathInput.textProperty()))
//                            .or(targetPathInput.styleProperty().isEqualTo(badStyle))
//                            .or(imageConverterAttributesController.xInput.styleProperty().isEqualTo(badStyle))
//                            .or(imageConverterAttributesController.yInput.styleProperty().isEqualTo(badStyle))
//                            .or(imageConverterAttributesController.qualityBox.disableProperty().isEqualTo(new SimpleBooleanProperty(false)).and(imageConverterAttributesController.qualityInput.styleProperty().isEqualTo(badStyle)))
//                            .or(imageConverterAttributesController.colorBox.disableProperty().isEqualTo(new SimpleBooleanProperty(false)).and(imageConverterAttributesController.thresholdInput.styleProperty().isEqualTo(badStyle)))
//            );
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void popIccFile(MouseEvent event) {
        if (popMenu != null && popMenu.isShowing()) {
            popMenu.hide();
            popMenu = null;
        }
        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        int fileNumber = AppVaribles.fileRecentNumber * 2 / 3 + 1;
        List<VisitHistory> his;
        his = VisitHistory.getRecentFile(VisitHistory.FileType.Icc, fileNumber);
        if (his != null && !his.isEmpty()) {
            for (VisitHistory h : his) {
                final String fname = h.getResourceValue();
                MenuItem menu = new MenuItem(fname);
                menu.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        iccFileSelected(new File(fname));
                    }
                });
                popMenu.getItems().add(menu);
            }
        }

        int pathNumber = AppVaribles.fileRecentNumber / 3 + 1;
        his = VisitHistory.getRecentPath(VisitHistory.FileType.Icc, pathNumber);
        List<String> paths = new ArrayList();
        if (his != null) {
            for (VisitHistory h : his) {
                String pathname = h.getResourceValue();
                paths.add(pathname);
            }
        }
        paths.add(SystemTools.IccProfilePath());
        popMenu.getItems().add(new SeparatorMenuItem());
        for (String path : paths) {
            MenuItem menu = new MenuItem(path);
            final String p = path;
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    selectIccFile(new File(p));
                }
            });
            popMenu.getItems().add(menu);
        }

        popMenu.getItems().add(new SeparatorMenuItem());
        MenuItem menu = new MenuItem(getMessage("MenuClose"));
        menu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                popMenu.hide();
                popMenu = null;
            }
        });
        popMenu.getItems().add(menu);

        FxmlControl.locateBelow((Region) event.getSource(), popMenu);

    }

    @FXML
    public void selectIccAction(ActionEvent event) {
        selectIccFile(AppVaribles.getUserConfigPath(sourcePathKey));
    }

    public void selectIccFile(File path) {
        try {
            final FileChooser fileChooser = new FileChooser();
            if (path != null && path.exists()) {
                fileChooser.setInitialDirectory(path);
            }
            fileChooser.getExtensionFilters().addAll(CommonValues.IccProfileExtensionFilter);
            File file = fileChooser.showOpenDialog(myStage);
            if (file == null || !file.exists()) {
                return;
            }
            iccFileSelected(file);
        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    public void iccFileSelected(File file) {
        iccInput.setText(file.getAbsolutePath());
        recordFileOpened(file, VisitHistory.FileType.Icc, VisitHistory.FileType.Icc);
    }

    @Override
    public void makeMoreParameters() {
        makeBatchParameters();
    }

}
