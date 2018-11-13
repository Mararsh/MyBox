package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.db.TableConvolutionKernel;
import mara.mybox.objects.CommonValues;
import mara.mybox.objects.ConvolutionKernel;
import static mara.mybox.fxml.FxmlTools.badStyle;
import mara.mybox.image.ImageEffectTools;

/**
 * @Author Mara
 * @CreateDate 2018-11-08
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureBatchConvolutionController extends ImageManufactureBatchController {

    private final ObservableList<ConvolutionKernel> kernels = FXCollections.observableArrayList();
    private ConvolutionKernel currentKernel;
    private boolean isSettingValues;

    @FXML
    private ComboBox<String> kernelBox;

    public ImageManufactureBatchConvolutionController() {

    }

    @Override
    protected void initializeNext2() {
        try {

            operationBarController.startButton.disableProperty().bind(Bindings.isEmpty(targetPathInput.textProperty())
                    .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                    .or(Bindings.isEmpty(sourceFilesInformation))
                    .or(Bindings.isEmpty(kernels))
            );

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    @Override
    protected void initOptionsSection() {
        try {

            kernelBox.setVisibleRowCount(10);
            kernelBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue ov, Number oldValue, Number newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    int index = newValue.intValue();
                    if (index < 0 || index >= kernels.size()) {
                        return;
                    }
                    currentKernel = kernels.get(index);
                }
            });
            kernels.addAll(TableConvolutionKernel.read());
            loadList();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void loadList(List<ConvolutionKernel> records) {
        kernels.clear();
        kernels.addAll(records);
        loadList();
    }

    public void loadList() {
        isSettingValues = true;
        kernelBox.getItems().clear();
        if (kernels == null || kernels.isEmpty()) {
            isSettingValues = false;
            return;
        }
        List<String> names = new ArrayList<>();
        for (ConvolutionKernel k : kernels) {
            names.add(k.getName());
        }
        kernelBox.getItems().addAll(names);
        isSettingValues = false;

        kernelBox.getSelectionModel().select(0);
    }

    @FXML
    private void manageKernels(ActionEvent event) {
        BaseController c = openStage(CommonValues.ConvolutionKernelManagerFxml, true);
        c.setParentController(getMyController());
        c.setParentFxml(getMyFxml());
    }

    @Override
    protected BufferedImage handleImage(BufferedImage source) {
        if (currentKernel == null) {
            return null;
        }
        try {
            BufferedImage target = ImageEffectTools.applyConvolution(source, currentKernel);
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

}
