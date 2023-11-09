package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import mara.mybox.bufferedimage.ColorConvertTools;
import mara.mybox.bufferedimage.ImageQuantization;
import mara.mybox.bufferedimage.ImageQuantization.QuantizationAlgorithm;
import mara.mybox.bufferedimage.ImageQuantizationFactory;
import mara.mybox.bufferedimage.ImageQuantizationFactory.KMeansClusteringQuantization;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.data.StringTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-9-2
 * @License Apache License Version 2.0
 */
public class ImageReduceColorsController extends ImageSelectScopeController {

    protected List<Color> quantizationColors;
    protected StringTable quanTable;

    @FXML
    protected ControlImageQuantization optionsController;
    @FXML
    protected Button paletteAddButton, htmlButton;

    public ImageReduceColorsController() {
        baseTitle = message("ReduceColors");
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(paletteAddButton, message("AddInColorPalette"));
            NodeStyleTools.setTooltip(htmlButton, message("ShowData"));
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    protected void initMore() {
        try {
            super.initMore();
            if (editor == null) {
                close();
                return;
            }
            reset();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void reset() {
        quantizationColors = null;
        paletteAddButton.setVisible(false);
        htmlButton.setVisible(false);
        quanTable = null;
        optionsController.resultsLabel.setText("");
    }

    @FXML
    @Override
    public void okAction() {
        if (!checkOptions()) {
            return;
        }
        reset();
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {
            private ImageScope scope;
            private ImageQuantization quantization;
            private int actualLoop = -1;
            private boolean calData;

            @Override
            protected boolean handle() {
                try {
                    scope = scope();
                    calData = optionsController.quanDataCheck.isSelected();
                    quantization = ImageQuantizationFactory.create(
                            editor.imageView.getImage(), scope,
                            optionsController, calData);
                    quantization.setExcludeScope(excludeRadio.isSelected())
                            .setSkipTransparent(ignoreTransparentCheck.isSelected());
                    if (optionsController.algorithm == QuantizationAlgorithm.KMeansClustering) {
                        KMeansClusteringQuantization q = (KMeansClusteringQuantization) quantization;
                        q.getKmeans().setMaxIteration(optionsController.kmeansLoop);
                        handledImage = q.operateFxImage();
                        actualLoop = q.getKmeans().getLoopCount();
                    } else {
                        handledImage = quantization.operateFxImage();
                    }
                    return handledImage != null;
                } catch (Exception e) {
                    MyBoxLog.debug(e);
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                popSuccessful();
                editor.updateImage("ReduceColors", optionsController.algorithm.name(),
                        scope, handledImage, cost);
                String name = null;
                if (editor.sourceFile != null) {
                    name = editor.sourceFile.getName();
                }
                quanTable = quantization.countTable(name);
                if (quanTable != null) {
                    htmlButton.setVisible(true);
                    if (calData) {
                        htmlAction();
                    }
                }
                if (actualLoop >= 0) {
                    optionsController.resultsLabel.setText(message("ActualLoop") + ":" + actualLoop);
                }
                List<ImageQuantization.ColorCount> sortedCounts = quantization.getSortedCounts();
                if (sortedCounts != null && !sortedCounts.isEmpty()) {
                    quantizationColors = new ArrayList<>();
                    for (int i = 0; i < sortedCounts.size(); ++i) {
                        ImageQuantization.ColorCount count = sortedCounts.get(i);
                        Color color = ColorConvertTools.converColor(count.color);
                        quantizationColors.add(color);
                    }
                    paletteAddButton.setVisible(true);
                }
                if (closeAfterCheck.isSelected()) {
                    close();
                }

            }
        };
        start(task);
    }

    @FXML
    public void htmlAction() {
        if (quanTable == null) {
            popError(message("NoData"));
            return;
        }
        HtmlTableController controller
                = (HtmlTableController) WindowTools.openStage(Fxmls.HtmlTableFxml);
        controller.loadTable(quanTable);
    }

    @FXML
    public void addColors() {
        if (quantizationColors == null || quantizationColors.isEmpty()) {
            popError(message("NoData"));
            return;
        }
        ColorsManageController.addColors(quantizationColors);
    }

    @Override
    protected Image makeDemo(BufferedImage dbf, ImageScope scope) {
        try {
            ImageQuantization quantization = ImageQuantizationFactory.create(
                    dbf, scope,
                    QuantizationAlgorithm.PopularityQuantization,
                    16, 256, 2, 4, 3, false, true, true);
            return quantization.operateFxImage();
        } catch (Exception e) {
            displayError(e.toString());
            return null;
        }
    }

    /*
        static methods
     */
    public static ImageReduceColorsController open(BaseImageController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageReduceColorsController controller = (ImageReduceColorsController) WindowTools.openChildStage(
                    parent.getMyWindow(), Fxmls.ImageReduceColorsFxml, false);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
