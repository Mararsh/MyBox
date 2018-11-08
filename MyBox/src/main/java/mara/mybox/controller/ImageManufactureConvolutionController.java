/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import mara.mybox.controller.ImageManufactureController;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.db.TableConvolutionKernel;
import mara.mybox.image.FxmlImageTools;
import mara.mybox.objects.CommonValues;
import mara.mybox.objects.ConvolutionKernel;

/**
 * FXML Controller class
 *
 * @author mara
 */
public class ImageManufactureConvolutionController extends ImageManufactureController {

    private List<ConvolutionKernel> kernels;
    private ConvolutionKernel currentKernel;

    @FXML
    private Label kernelLabel;
    @FXML
    private ComboBox<String> kernelBox;

    @Override
    protected void initializeNext2() {
        try {
            initCommon();
            initConvolutionTab();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    protected void initInterface() {
        try {
            if (values == null || values.getImage() == null) {
                return;
            }
            super.initInterface();

            isSettingValues = true;

            isSettingValues = false;
        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    protected void initConvolutionTab() {
        try {

            kernelBox.setVisibleRowCount(15);
            kernels = TableConvolutionKernel.read();
            loadList(kernels);
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
                    applyKernel();
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void loadList(List<ConvolutionKernel> records) {
        isSettingValues = true;
        kernels = records;
        kernelBox.getItems().clear();
        if (records == null) {
            return;
        }
        List<String> names = new ArrayList<>();
        for (ConvolutionKernel k : kernels) {
            names.add(k.getName());
        }
        kernelBox.getItems().addAll(names);
        isSettingValues = false;
    }

    @FXML
    private void manageKernels(ActionEvent event) {
        BaseController c = openStage(CommonValues.ConvolutionKernelManagerFxml, true);
        c.setParentController(getMyController());
        c.setParentFxml(getMyFxml());
    }

    public void applyKernel(ConvolutionKernel inKernel) {
        currentKernel = inKernel;
        applyKernel();
    }

    private void applyKernel() {
        if (isSettingValues || currentKernel == null) {
            return;
        }
        Task kernelTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    final Image newImage = FxmlImageTools.applyConvolutionKernel(values.getCurrentImage(),
                            currentKernel);
                    recordImageHistory(ImageOperationType.Convolution, newImage);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            values.setUndoImage(values.getCurrentImage());
                            values.setCurrentImage(newImage);
                            imageView.setImage(newImage);
                            setImageChanged(true);
                        }
                    });
                } catch (Exception e) {
                    logger.debug(e.toString());
                }
                return null;
            }
        };
        openHandlingStage(kernelTask, Modality.WINDOW_MODAL);
        Thread thread = new Thread(kernelTask);
        thread.setDaemon(true);
        thread.start();
    }

}
