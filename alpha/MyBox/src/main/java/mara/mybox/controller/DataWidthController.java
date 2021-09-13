package mara.mybox.controller;

import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-9-6
 * @License Apache License Version 2.0
 */
public class DataWidthController extends BaseDataOperationController {

    protected int width;

    @FXML
    protected TextField widthInput;

    @Override
    public void setParameters(ControlSheet sheetController, int row, int col) {
        try {
            super.setParameters(sheetController, row, col);

            if (col >= 0) {
                width = sheetController.columns.get(col).getWidth();
            } else {
                width = UserConfig.getInt(baseName + "Width", 100);
                if (width <= 0) {
                    width = 100;
                }
            }
            widthInput.setText(width + "");
            widthInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov, String oldv, String newv) {
                    try {
                        int v = Integer.parseInt(newv);
                        if (v > 0) {
                            width = v;
                            widthInput.setStyle(null);
                            UserConfig.setInt(baseName + "Width", width);
                        } else {
                            widthInput.setStyle(NodeStyleTools.badStyle);
                        }
                    } catch (Exception e) {
                        widthInput.setStyle(NodeStyleTools.badStyle);
                    }
                }
            });

            okButton.disableProperty().bind(Bindings.isEmpty(widthInput.textProperty())
                    .or(widthInput.styleProperty().isEqualTo(NodeStyleTools.badStyle))
            );

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void plusAction() {
        widthInput.setText((width + 1) + "");
    }

    @FXML
    public void minusAction() {
        if (width <= 1) {
            return;
        }
        widthInput.setText((width - 1) + "");
    }

    @FXML
    @Override
    public void okAction() {
        try {
            List<Integer> cols = cols();
            sheetController.widthCols(cols, width);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

}
