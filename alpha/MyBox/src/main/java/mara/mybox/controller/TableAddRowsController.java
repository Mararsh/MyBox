package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-9-4
 * @License Apache License Version 2.0
 */
public class TableAddRowsController extends BaseChildController {

    protected BaseTablePagesController tableViewController;
    protected int number;

    @FXML
    protected ComboBox<String> rowSelector;
    @FXML
    protected ToggleGroup locationGroup;
    @FXML
    protected RadioButton frontRadio, endRadio, belowRadio, aboveRadio;
    @FXML
    protected TextField numberInput;
    @FXML
    protected HBox rowBox;

    public void setParameters(BaseTablePagesController tableViewController) {
        try {
            this.tableViewController = tableViewController;
            this.baseName = tableViewController.baseName;

            getMyStage().setTitle(tableViewController.getBaseTitle());

            String location = UserConfig.getString(baseName + "AddRowsLocation", message("Front"));
            if (location == null || message("Front").equals(location)) {
                frontRadio.setSelected(true);
            } else if (message("End").equals(location)) {
                endRadio.setSelected(true);
            } else if (message("Below").equals(location)) {
                belowRadio.setSelected(true);
            } else if (message("Above").equals(location)) {
                aboveRadio.setSelected(true);
            } else {
                frontRadio.setSelected(true);
            }
            rowBox.setVisible(belowRadio.isSelected() || aboveRadio.isSelected());
            locationGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    UserConfig.setString(baseName + "AddRowsLocation", ((RadioButton) newValue).getText());
                    rowBox.setVisible(belowRadio.isSelected() || aboveRadio.isSelected());
                }
            });

            number = UserConfig.getInt(baseName + "AddRowsNumber", 1);
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

            setSelector();

            okButton.disableProperty().bind(Bindings.isEmpty(numberInput.textProperty())
                    .or(numberInput.styleProperty().isEqualTo(UserConfig.badStyle()))
            );
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setSelector() {
        try {
            int thisSelect = rowSelector.getSelectionModel().getSelectedIndex();
            List<String> rows = new ArrayList<>();
            for (long i = 0; i < tableViewController.tableData.size(); i++) {
                rows.add("" + (i + 1));
            }
            rowSelector.getItems().setAll(rows);
            int tableSelect = tableViewController.selectedIndix();
            rowSelector.getSelectionModel().select(
                    tableSelect >= 0 ? tableSelect : (thisSelect >= 0 ? thisSelect : 0));

        } catch (Exception e) {
            MyBoxLog.error(e);
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

    public int addRow(int index, int number) {
        return tableViewController.addRows(index, number);
    }

    @FXML
    @Override
    public void okAction() {
        try {
            if (number < 1) {
                popError(message("InvalidParameters"));
                return;
            }
            int index = rowSelector.getSelectionModel().getSelectedIndex();
            if (frontRadio.isSelected()) {
                index = 0;
            } else if (index < 0 || endRadio.isSelected()) {
                index = tableViewController.tableData.size();
            } else if (belowRadio.isSelected()) {
                index++;
            }
            if (addRow(index, number) > 0) {
                close();
                tableViewController.popSuccessful();
            }

//            setSelector();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }


    /*
        static
     */
    public static TableAddRowsController open(BaseTablePagesController tableViewController) {
        try {
            TableAddRowsController controller = (TableAddRowsController) WindowTools.branchStage(
                    tableViewController, Fxmls.TableAddRowsFxml);
            controller.setParameters(tableViewController);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
