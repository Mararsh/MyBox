package mara.mybox.controller;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeStyleTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-9-4
 * @License Apache License Version 2.0
 */
public class DataRowsAddController extends BaseDataOperationController {

    protected int number;

    @FXML
    protected RadioButton aboveRadio;
    @FXML
    protected TextField numberInput;

    @Override
    public void setParameters(ControlSheet sheetController, int row, int col) {
        try {
            super.setParameters(sheetController, row, col);

            number = UserConfig.getInt(baseName + "Number", 1);
            if (number < 1) {
                number = 1;
            }
            numberInput.setText(number + "");
            numberInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov, String oldv, String newv) {
                    try {
                        int v = Integer.parseInt(newv);
                        if (v > 0) {
                            number = v;
                            numberInput.setStyle(null);
                            UserConfig.setInt(baseName + "Number", number);
                        } else {
                            numberInput.setStyle(NodeStyleTools.badStyle);
                        }
                    } catch (Exception e) {
                        numberInput.setStyle(NodeStyleTools.badStyle);
                    }
                }
            });

            okButton.disableProperty().bind(Bindings.isEmpty(numberInput.textProperty())
                    .or(numberInput.styleProperty().isEqualTo(NodeStyleTools.badStyle))
            );

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void okAction() {
        try {
            if (number < 1) {
                popError(message("InvalidParameters"));
                return;
            }
            int row = rowSelector.getSelectionModel().getSelectedIndex();
            if (row < 0) {
                popError(message("NoSelection"));
                return;
            }
            sheetController.addRows(row, aboveRadio.isSelected(), number);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
