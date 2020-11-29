package mara.mybox.controller;

import java.io.File;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Modality;
import mara.mybox.data.VisitHistory;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.image.file.ImageGifFile;
import mara.mybox.value.AppVariables;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-11-16
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageGifEditerController extends ImagesListController {

    protected int currentIndex, width;
    private boolean keepSize;

    @FXML
    protected ToggleGroup sizeGroup;
    @FXML
    protected TextField widthInput;
    @FXML
    private CheckBox loopCheck;

    public ImageGifEditerController() {
        baseTitle = AppVariables.message("ImageGifEditer");

        SourceFileType = VisitHistory.FileType.Gif;
        SourcePathType = VisitHistory.FileType.Gif;
        TargetFileType = VisitHistory.FileType.Gif;
        TargetPathType = VisitHistory.FileType.Gif;
        AddFileType = VisitHistory.FileType.Image;
        AddPathType = VisitHistory.FileType.Image;

        sourceExtensionFilter = CommonFxValues.GifExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;
    }

    @Override
    public void initOptionsSection() {
        try {
            optionsBox.setDisable(true);
            tableBox.setDisable(true);

            sizeGroup.selectedToggleProperty().addListener(
                    (ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) -> {
                        checkSizeType();
                    });
            checkSizeType();

            widthInput.textProperty().addListener(
                    (ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                        checkSize();
                    });

            saveButton.disableProperty().bind(Bindings.isEmpty(tableData)
                    .or(widthInput.styleProperty().isEqualTo(badStyle))
            );
            saveAsButton.disableProperty().bind(saveButton.disableProperty());
            viewButton.disableProperty().bind(saveButton.disableProperty());

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    private void checkSizeType() {
        RadioButton selected = (RadioButton) sizeGroup.getSelectedToggle();
        if (message("KeepImagesSize").equals(selected.getText())) {
            keepSize = true;
            widthInput.setStyle(null);
        } else {
            keepSize = false;
            checkSize();
        }
    }

    private void checkSize() {
        try {
            int v = Integer.valueOf(widthInput.getText());
            if (v > 0) {
                width = v;
                widthInput.setStyle(null);
            } else {
                widthInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            widthInput.setStyle(badStyle);
        }

    }

    @Override
    public void saveFileDo(final File outFile) {

        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                private String ret;

                @Override
                protected boolean handle() {
                    ret = ImageGifFile.writeImages(tableData, outFile,
                            loopCheck.isSelected(), keepSize, width);
                    if (ret.isEmpty()) {
                        return true;
                    } else {
                        error = ret;
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    popSuccessful();
                    if (outFile.equals(sourceFile)) {
                        setImageChanged(false);
                    }
                    if (viewCheck.isSelected()) {
                        try {
                            final ImageGifViewerController controller
                                    = (ImageGifViewerController) openStage(CommonValues.ImageGifViewerFxml);
                            controller.loadImage(outFile.getAbsolutePath());
                        } catch (Exception e) {
                            MyBoxLog.error(e.toString());
                        }
                    }
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    public void viewAction() {
        try {
            if (sourceFile != null) {
                final ImageGifViewerController controller
                        = (ImageGifViewerController) openStage(CommonValues.ImageGifViewerFxml);
                controller.loadImage(sourceFile.getAbsolutePath());
            } else {
                viewCheck.setSelected(true);
                saveAsAction();
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
