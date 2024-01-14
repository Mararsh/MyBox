package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-7-19
 * @License Apache License Version 2.0
 */
public class ImageLoadWidthController extends BaseChildController {

    protected BaseImageController imageController;

    @FXML
    protected ComboBox<String> loadWidthSelector;

    public ImageLoadWidthController() {
        baseTitle = message("LoadWidth");
    }

    public void setParameters(BaseImageController parent) {
        try {
            if (parent == null) {
                close();
                return;
            }
            imageController = parent;
            baseName = imageController.baseName;

            List<String> values = new ArrayList<>();
            values.addAll(Arrays.asList(message("OriginalSize"),
                    "512", "1024", "256", "128", "2048", "100", "80", "4096"));
            int v = imageController.loadWidth;
            if (v > 0) {
                values.add(0, v + "");
            }
            loadWidthSelector.getItems().setAll(values);
            loadWidthSelector.getSelectionModel().select(0);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void okAction() {
        String s = loadWidthSelector.getValue();
        int loadWidth;
        if (message("OriginalSize").equals(s)) {
            loadWidth = -1;
        } else {
            try {
                loadWidth = Integer.parseInt(s);
                if (loadWidth < 0) {
                    loadWidth = -1;
                }
            } catch (Exception e) {
                popError(message("InvalidData"));
                return;
            }
        }
        imageController.setLoadWidth(loadWidth);
        close();
    }

    /*
        static methods
     */
    public static ImageLoadWidthController open(BaseImageController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageLoadWidthController controller = (ImageLoadWidthController) WindowTools.childStage(
                    parent, Fxmls.ImageLoadWidthFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
