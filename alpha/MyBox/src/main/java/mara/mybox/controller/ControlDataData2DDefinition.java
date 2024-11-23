package mara.mybox.controller;

import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.data2d.tools.Data2DDefinitionTools;
import mara.mybox.db.data.DataNode;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-3-20
 * @License Apache License Version 2.0
 */
public class ControlDataData2DDefinition extends BaseDataValuesController {

    protected Data2D data2D;
    protected short scale;
    protected int maxRandom;

    @FXML
    protected ControlData2DDefColumns columnsController;
    @FXML
    protected TextField nameInput;
    @FXML
    protected TextArea descInput;
    @FXML
    protected ComboBox<String> scaleSelector, randomSelector;

    @Override
    public void initEditor() {
        try {
            data2D = null;
            columnsController.setParameters(this);

            scale = (short) UserConfig.getInt(baseName + "Scale", 2);
            if (scale < 0) {
                scale = 2;
            }
            scaleSelector.getItems().addAll(
                    Arrays.asList("2", "1", "0", "3", "4", "5", "6", "7", "8", "10", "12", "15")
            );
            scaleSelector.setValue(scale + "");
            scaleSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        valueChanged(true);
                    });

            maxRandom = UserConfig.getInt(baseName + "MaxRandom", 100000);
            if (maxRandom < 0) {
                maxRandom = 100000;
            }
            randomSelector.getItems().addAll(Arrays.asList("1", "100", "10", "1000", "10000", "1000000", "10000000"));
            randomSelector.setValue(maxRandom + "");
            randomSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        valueChanged(true);
                    });

            descInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    valueChanged(true);
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected void editValues() {
        try {
            String def = null;
            if (nodeEditor.currentNode != null) {
                def = nodeEditor.currentNode.getStringValue("data2d_definition");
            }
            data2D = Data2DDefinitionTools.fromXML(def);
            if (data2D == null) {
                data2D = new DataFileCSV();
            }
            load(data2D);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void load(Data2D data) {
        try {
            data2D = data;

            columnsController.load(data2D);

            isSettingValues = true;
            if (data2D != null) {
                nameInput.setText(data2D.getDataName());
                scaleSelector.setValue(data2D.getScale() + "");
                randomSelector.setValue(data2D.getMaxRandom() + "");
                descInput.setText(data2D.getComments());
            } else {
                nameInput.setText(message("NewData"));
                scaleSelector.setValue(scale + "");
                randomSelector.setValue(maxRandom + "");
                descInput.clear();
            }
            isSettingValues = false;
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected DataNode pickValues(DataNode node) {
        if (node == null) {
            return null;
        }
        int colSize = columnsController.tableData.size();
        if (colSize < 1) {
            popError(message("InvalidData") + ": " + message("Columns"));
            return null;
        }
        scale = -1;
        try {
            int v = Integer.parseInt(scaleSelector.getValue());
            if (v >= 0 && v <= 15) {
                scale = (short) v;
                UserConfig.setInt(baseName + "Scale", v);
            }
        } catch (Exception e) {
        }
        if (scale < 0) {
            popError(message("InvalidData") + ": " + message("DecimalScale"));
            return null;
        }

        maxRandom = -1;
        try {
            int v = Integer.parseInt(randomSelector.getValue());
            if (v > 0) {
                maxRandom = v;
                UserConfig.setInt(baseName + "MaxRandom", v);
            }
        } catch (Exception e) {
        }
        if (maxRandom < 0) {
            popError(message("InvalidData") + ": " + message("MaxRandom"));
            return null;
        }

        try {
            if (data2D == null) {
                data2D = new DataFileCSV();
            }
            data2D.setColumns(columnsController.tableData)
                    .setColsNumber(colSize)
                    .setScale(scale)
                    .setMaxRandom(maxRandom)
                    .setComments(descInput.getText())
                    .setDataName(nameInput.getText());
            String def = Data2DDefinitionTools.toXML(data2D, true, "");
            node.setValue("data2d_definition", def);
            return node;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

//    public void pasteNode(InfoNode node) {
//        if (node == null) {
//            return;
//        }
//        if (task != null) {
//            task.cancel();
//        }
//        task = new FxSingletonTask<Void>(this) {
//            Data2D data;
//
//            @Override
//            protected boolean handle() {
//                try {
//                    data = Data2DDefinitionTools.definitionFromXML(this, myController, node.getInfo());
//                    return true;
//                } catch (Exception e) {
//                    error = e.toString();
//                    return false;
//                }
//            }
//
//            @Override
//            protected void whenSucceeded() {
//                if (data != null && data.getColumns() != null) {
//                    columnsController.tableData.addAll(data.getColumns());
//                }
//                tabPane.getSelectionModel().select(valueTab);
//            }
//
//        };
//        start(task);
//    }
    /*
        static
     */
    public static DataTreeController loadData(Data2D data) {
        try {
            DataTreeController controller = DataTreeController.data2DDefinition(null, false);
            ((ControlDataData2DDefinition) controller.nodeController.dataController).load(data);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
