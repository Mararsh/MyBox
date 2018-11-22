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
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.db.TableConvolutionKernel;
import mara.mybox.fxml.FxmlEffectTools;
import mara.mybox.fxml.FxmlTools;
import static mara.mybox.objects.AppVaribles.getMessage;
import mara.mybox.objects.CommonValues;
import mara.mybox.objects.ConvolutionKernel;
import mara.mybox.objects.ImageScope;

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

    protected void initConvolutionTab() {
        try {

            Tooltip tips = new Tooltip(getMessage("CTRL+c"));
            tips.setFont(new Font(16));
            FxmlTools.quickTooltip(kernelBox, tips);

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

    @Override
    protected void initInterface() {
        try {
            if (values == null || values.getImage() == null) {
                return;
            }
            super.initInterface();
            values.getScope().setOperationType(ImageScope.OperationType.Convolution);

            isSettingValues = true;

            isSettingValues = false;
        } catch (Exception e) {
            logger.debug(e.toString());
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

    public void selectKernel(ConvolutionKernel kernel) {
        if (kernelBox.getItems().contains(kernel.getName())) {
            kernelBox.getSelectionModel().select(kernel.getName());
        } else {
            applyKernel(kernel);
        }
    }

    @FXML
    private void manageKernels(ActionEvent event) {
        BaseController c = openStage(CommonValues.ConvolutionKernelManagerFxml, true);
        c.setParentController(getMyController());
        c.setParentFxml(getMyFxml());
    }

    @Override
    protected void keyEventsHandler(KeyEvent event) {
        super.keyEventsHandler(event);
        String key = event.getText();
        if (key == null || key.isEmpty()) {
            return;
        }
        if (event.isControlDown()) {
            switch (key) {
                case "c":
                case "C":
                    kernelBox.show();
                    break;
            }
        }
    }

    public void applyKernel(ConvolutionKernel inKernel) {
        currentKernel = inKernel;
        applyKernel();
    }

    private void applyKernel() {
        if (isSettingValues || currentKernel == null) {
            return;
        }
        if (task != null && task.isRunning()) {
            return;
        }
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    final Image newImage;
                    if (scope == null || scope.getScopeType() == ImageScope.ScopeType.All) {
                        newImage = FxmlEffectTools.applyConvolution(values.getCurrentImage(), currentKernel);
                    } else if (scope.getScopeType() == ImageScope.ScopeType.Matting) {
                        newImage = FxmlEffectTools.applyConvolutionByMatting(values.getCurrentImage(), currentKernel, scope);
                    } else {
                        newImage = FxmlEffectTools.applyConvolutionByScope(values.getCurrentImage(), currentKernel, scope);
                    }

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
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

}
