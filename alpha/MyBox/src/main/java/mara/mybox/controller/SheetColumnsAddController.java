package mara.mybox.controller;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeStyleTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-9-4
 * @License Apache License Version 2.0
 */
public class SheetColumnsAddController extends BaseDataOperationController {

    protected int number;

    @FXML
    protected RadioButton beforeRadio;
    @FXML
    protected TextField numberInput;
    @FXML
    protected VBox colsBox;

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
                            numberInput.setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
                        numberInput.setStyle(UserConfig.badStyle());
                    }
                }
            });

            okButton.disableProperty().bind(Bindings.isEmpty(numberInput.textProperty())
                    .or(numberInput.styleProperty().isEqualTo(UserConfig.badStyle()))
            );

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void updateControls() {
        try {
            super.updateControls();

            colsBox.setVisible(sheetController.columns != null && !sheetController.columns.isEmpty());

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void plusAction() {
        numberInput.setText((number + 1) + "");
    }

    @FXML
    public void minusAction() {
        if (number <= 1) {
            return;
        }
        numberInput.setText((number - 1) + "");
    }

    @FXML
    @Override
    public void okAction() {
        try {
            if (number < 1) {
                popError(message("InvalidParameters"));
                return;
            }
            int col;
            if (sheetController.columns == null || sheetController.columns.isEmpty()) {
                col = 0;
            } else {
                col = colSelector.getSelectionModel().getSelectedIndex();
                if (col < 0) {
                    col = colSelector.getItems().size() - 1;
                }
            }
            sheetController.addCols(col, beforeRadio.isSelected(), number);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
