package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.embed.swing.SwingFXUtils;
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
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.ImageDemoTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-9-2
 * @License Apache License Version 2.0
 */
public class ImageReduceColorsController extends BasePixelsController {

    protected List<Color> quantizationColors;
    protected StringTable quanTable;
    protected ImageQuantization quantization;
    protected boolean calData;

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
            operation = message("ReduceColors");
            paletteAddButton.setVisible(false);
            htmlButton.setVisible(false);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected boolean checkOptions() {
        if (!super.checkOptions()) {
            return false;
        }
        try {
            quantizationColors = null;
            paletteAddButton.setVisible(false);
            htmlButton.setVisible(false);
            quanTable = null;
            optionsController.resultsLabel.setText("");
            calData = optionsController.quanDataCheck.isSelected();
            return true;
        } catch (Exception e) {
            displayError(e.toString());
            return false;
        }
    }

    @Override
    protected Image handleImage(FxTask currentTask, Image inImage, ImageScope inScope) {
        try {
            quantization = ImageQuantizationFactory.createFX(inImage, inScope,
                    optionsController, calData);
            quantization.setImage(inImage).setScope(inScope)
                    .setExcludeScope(excludeScope())
                    .setSkipTransparent(skipTransparent())
                    .setTask(currentTask);
            opInfo = optionsController.algorithm.name();
            if (optionsController.algorithm == QuantizationAlgorithm.KMeansClustering) {
                KMeansClusteringQuantization q = (KMeansClusteringQuantization) quantization;
                q.getKmeans().setMaxIteration(optionsController.kmeansLoop);
                handledImage = q.operateFxImage();
            } else {
                handledImage = quantization.operateFxImage();
            }

            String name = null;
            if (imageController.sourceFile != null) {
                name = imageController.sourceFile.getName();
            }
            quanTable = quantization.countTable(currentTask, name);

            List<ImageQuantization.ColorCount> sortedCounts = quantization.getSortedCounts();
            if (sortedCounts != null && !sortedCounts.isEmpty()) {
                quantizationColors = new ArrayList<>();
                for (int i = 0; i < sortedCounts.size(); ++i) {
                    ImageQuantization.ColorCount count = sortedCounts.get(i);
                    Color color = ColorConvertTools.converColor(count.color);
                    quantizationColors.add(color);
                }
            }

            return handledImage;
        } catch (Exception e) {
            displayError(e.toString());
            return null;
        }
    }

    @Override
    protected void afterHandle() {
        if (quantization == null) {
            return;
        }
        optionsController.resultsLabel.setText("-----\n" + quantization.resultInfo());
        if (quanTable != null) {
            htmlButton.setVisible(true);
            if (calData) {
                htmlAction();
            }
        }
        if (quantizationColors != null && !quantizationColors.isEmpty()) {
            paletteAddButton.setVisible(true);
        }
    }

    @FXML
    public void htmlAction() {
        if (quanTable == null) {
            popError(message("NoData"));
            return;
        }
        HtmlTableController controller
                = (HtmlTableController) WindowTools.popStage(this, Fxmls.HtmlTableFxml);
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
    protected void makeDemoFiles(FxTask currentTask, List<String> files, Image demoImage) {
        ImageDemoTools.reduceColor(currentTask, files, SwingFXUtils.fromFXImage(demoImage, null));
    }

    /*
        static methods
     */
    public static ImageReduceColorsController open(BaseImageController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageReduceColorsController controller = (ImageReduceColorsController) WindowTools.branchStage(
                    parent, Fxmls.ImageReduceColorsFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
