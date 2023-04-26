package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import mara.mybox.db.data.ColorPaletteName;
import mara.mybox.db.table.TableColor;
import mara.mybox.db.table.TableColorPalette;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-3-10
 * @License Apache License Version 2.0
 */
public class ColorPaletteSelectorController extends BaseChildController {

    protected TableColor tableColor;
    protected TableColorPalette tableColorPalette;
    protected ColorPalettePopupController popupController;

    @FXML
    protected ControlColorPaletteSelector palettesController;

    public ColorPaletteSelectorController() {
        baseTitle = message("SelectColorPalette");
    }

    public void setParameter(ColorPalettePopupController popupController) {
        try {
            super.initControls();

            this.popupController = popupController;

            palettesController.setParameter(null, false);

            okButton.disableProperty().bind(palettesController.palettesList.getSelectionModel().selectedItemProperty().isNull());
            palettesController.loadPalettes();
            palettesController.palettesList.setOnMouseClicked((MouseEvent event) -> {
                if (event.getClickCount() > 1) {
                    okAction();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    @FXML
    @Override
    public void okAction() {
        ColorPaletteName palette = palettesController.palettesList.getSelectionModel().getSelectedItem();
        if (palette == null) {
            popError(message("SelectPaletteCopyColors"));
            return;
        }
        close();
        popupController.loadPalette(palette.getName());
    }

    @FXML
    @Override
    public void cancelAction() {
        closeStage();
    }

    /*
        static
     */
    public static ColorPaletteSelectorController open(ColorPalettePopupController popupController) {
        try {
            ColorPaletteSelectorController controller = (ColorPaletteSelectorController) WindowTools.openChildStage(
                    popupController.getMyWindow(), Fxmls.ColorPaletteSelectorFxml, true);
            controller.setParameter(popupController);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
