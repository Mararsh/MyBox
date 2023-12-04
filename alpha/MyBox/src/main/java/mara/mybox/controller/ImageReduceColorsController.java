package mara.mybox.controller;

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
    protected int actualLoop = -1;
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
    public void reset() {
        super.reset();
        quantizationColors = null;
        paletteAddButton.setVisible(false);
        htmlButton.setVisible(false);
        quanTable = null;
        optionsController.resultsLabel.setText("");
    }

    @Override
    protected Image handleImage(Image inImage, ImageScope inScope) {
        try {
            calData = optionsController.quanDataCheck.isSelected();
            quantization = ImageQuantizationFactory.create(inImage, inScope,
                    optionsController, calData);
            quantization.setExcludeScope(excludeScope())
                    .setSkipTransparent(skipTransparent());
            if (optionsController.algorithm == QuantizationAlgorithm.KMeansClustering) {
                KMeansClusteringQuantization q = (KMeansClusteringQuantization) quantization;
                q.getKmeans().setMaxIteration(optionsController.kmeansLoop);
                handledImage = q.operateFxImage();
                actualLoop = q.getKmeans().getLoopCount();
            } else {
                handledImage = quantization.operateFxImage();
            }
            operation = message("ReduceColors");
            opInfo = optionsController.algorithm.name();
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

    /*
        static methods
     */
    public static ImageReduceColorsController open(BaseImageController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageReduceColorsController controller = (ImageReduceColorsController) WindowTools.branch(
                    parent.getMyWindow(), Fxmls.ImageReduceColorsFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
