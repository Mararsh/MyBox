package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-5-20
 * @License Apache License Version 2.0
 */
public class ControlSplit extends BaseController {

    protected double size;
    protected int number;
    protected List<Double> list;
    protected SplitType splitType;
    protected SimpleBooleanProperty valid;
    protected boolean isPositiveInteger;

    @FXML
    protected ToggleGroup splitGroup;
    @FXML
    protected RadioButton sizeRadio, numberRadio, listRadio;
    @FXML
    protected TextField sizeInput, numberInput, listInput;

    public enum SplitType {
        Size, Number, List
    }

    public ControlSplit() {
        splitType = SplitType.Size;
        valid = new SimpleBooleanProperty(false);
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(listInput, new Tooltip(Languages.message("StartEndComments")));
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void setParameters(BaseController parent) {
        setParameters(parent, true);
    }

    public void setParameters(BaseController parent, boolean isInteger) {
        try {
            parentController = parent;
            baseName = baseName + "_" + parent.baseName;
            this.isPositiveInteger = isInteger;

            valid.bind(sizeInput.styleProperty().isNotEqualTo(UserConfig.badStyle())
                    .and(numberInput.styleProperty().isNotEqualTo(UserConfig.badStyle()))
                    .and(listInput.styleProperty().isNotEqualTo(UserConfig.badStyle())));

            splitGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkSplitType();
                }
            });

            sizeInput.setText(UserConfig.getString(baseName + "Size", "100"));
            sizeInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    checkSize();
                }
            });

            numberInput.setText(UserConfig.getString(baseName + "Number", "3"));
            numberInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    checkNumber();
                }
            });

            listInput.setText(UserConfig.getString(baseName + "List", "1-10,11-20"));
            listInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    checkList();
                }
            });

            checkSplitType();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    public void checkSplitType() {
        sizeInput.setDisable(true);
        numberInput.setDisable(true);
        listInput.setDisable(true);
        sizeInput.setStyle(null);
        numberInput.setStyle(null);
        listInput.setStyle(null);

        if (sizeRadio.isSelected()) {
            splitType = SplitType.Size;
            sizeInput.setDisable(false);
            checkSize();

        } else if (numberRadio.isSelected()) {
            splitType = SplitType.Number;
            numberInput.setDisable(false);
            checkNumber();

        } else if (listRadio.isSelected()) {
            splitType = SplitType.List;
            listInput.setDisable(false);
            checkList();
        }
    }

    private void checkSize() {
        if (isSettingValues) {
            return;
        }
        try {
            double v = Double.valueOf(sizeInput.getText());
            if (v > 0) {
                size = v;
                if (isPositiveInteger) {
                    long psize = Math.round(size);
                    UserConfig.setString(baseName + "Size", psize + "");
                    isSettingValues = true;
                    sizeInput.setText(psize + "");
                    isSettingValues = false;
                } else {
                    UserConfig.setString(baseName + "Size", size + "");
                }
                sizeInput.setStyle(null);
            } else {
                sizeInput.setStyle(UserConfig.badStyle());
            }
        } catch (Exception e) {
            sizeInput.setStyle(UserConfig.badStyle());
        }
    }

    private void checkNumber() {
        try {
            int v = Integer.valueOf(numberInput.getText());
            if (v > 0) {
                numberInput.setStyle(null);
                number = v;
                UserConfig.setString(baseName + "Number", number + "");
            } else {
                numberInput.setStyle(UserConfig.badStyle());
            }
        } catch (Exception e) {
            numberInput.setStyle(UserConfig.badStyle());
        }
    }

    private void checkList() {
        if (isPositiveInteger) {
            list = new ArrayList<>();
            try {
                String[] ss = listInput.getText().split(",");
                for (String item : ss) {
                    String[] values = item.split("-");
                    if (values.length != 2) {
                        continue;
                    }
                    try {
                        int start = Integer.valueOf(values[0].trim());
                        int end = Integer.valueOf(values[1].trim());
                        if (start > 0 && end >= start) {  // 1-based, include start and end
                            list.add(start + 0d);
                            list.add(end + 0d);
                        }
                    } catch (Exception e) {
                    }
                }
                if (list.isEmpty()) {
                    listInput.setStyle(UserConfig.badStyle());
                } else {
                    listInput.setStyle(null);
                    UserConfig.setString(baseName + "List", listInput.getText());
                }
            } catch (Exception e) {
                listInput.setStyle(UserConfig.badStyle());
            }
        } else {
            list = new ArrayList<>();
            try {
                String[] ss = listInput.getText().split(",");
                for (String item : ss) {
                    String[] values = item.split("-");
                    if (values.length != 2) {
                        continue;
                    }
                    try {
                        double start = Double.valueOf(values[0].trim());
                        double end = Double.valueOf(values[1].trim());
                        if (end >= start) {
                            list.add(start);
                            list.add(end);
                        }
                    } catch (Exception e) {
                    }
                }
                if (list.isEmpty()) {
                    listInput.setStyle(UserConfig.badStyle());
                } else {
                    listInput.setStyle(null);
                    UserConfig.setString(baseName + "List", listInput.getText());
                }
            } catch (Exception e) {
                listInput.setStyle(UserConfig.badStyle());
            }
        }
    }

    public int pSize() {
        return (int) Math.round(size);
    }

    public List<Integer> pList() {
        if (list == null) {
            return null;
        }
        List<Integer> plist = new ArrayList<>();
        for (double d : list) {
            plist.add((int) Math.round(d));
        }
        return plist;
    }

    public int size(long total, int number) {
        int nsize = (int) (total / number);
        if (total % number == 0) {
            return nsize;
        } else {
            return nsize + 1;
        }
    }

}
