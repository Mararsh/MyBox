package mara.mybox.controller;

import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.input.MouseEvent;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-4-20
 * @License Apache License Version 2.0
 */
public abstract class BaseData2DHtmlChartController extends BaseData2DChartController {

    protected int barWidth = 100;

    @FXML
    protected CheckBox zeroCheck, valueCheck, percentageCheck, calculatedCheck;
    @FXML
    protected ComboBox<String> widthSelector;
    @FXML
    protected ControlWebView webViewController;

    @Override
    public void initControls() {
        try {
            super.initControls();

            webViewController.setParent(this);

            barWidth = UserConfig.getInt(baseName + "Width", 150);
            if (barWidth < 0) {
                barWidth = 100;
            }
            widthSelector.getItems().addAll(
                    Arrays.asList("150", "100", "200", "50", "80", "120", "300")
            );
            widthSelector.setValue(barWidth + "");
            widthSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(widthSelector.getValue());
                        if (v > 0) {
                            barWidth = v;
                            UserConfig.setInt(baseName + "Width", v);
                            widthSelector.getEditor().setStyle(null);
                            okAction();
                        } else {
                            widthSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
                        widthSelector.getEditor().setStyle(UserConfig.badStyle());
                    }
                }
            });

            zeroCheck.setSelected(UserConfig.getBoolean(baseName + "ZeroBased", true));
            zeroCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "ZeroBased", zeroCheck.isSelected());
                    okAction();
                }
            });

            if (valueCheck != null) {
                valueCheck.setSelected(UserConfig.getBoolean(baseName + "ShowValue", true));
                valueCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        UserConfig.setBoolean(baseName + "ShowValue", valueCheck.isSelected());
                        okAction();
                    }
                });
            }

            if (percentageCheck != null) {
                percentageCheck.setSelected(UserConfig.getBoolean(baseName + "ShowPercentage", true));
                percentageCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        UserConfig.setBoolean(baseName + "ShowPercentage", percentageCheck.isSelected());
                        okAction();
                    }
                });
            }

            if (calculatedCheck != null) {
                calculatedCheck.setSelected(UserConfig.getBoolean(baseName + "ShowCalculatedValues", true));
                calculatedCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        UserConfig.setBoolean(baseName + "ShowCalculatedValues", calculatedCheck.isSelected());
                        okAction();
                    }
                });
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void setParameters(ControlData2DEditTable editController) {
        try {
            super.setParameters(editController);

            sourceController.showAllPages(false);

            okAction();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void objectChanged() {
        super.objectChanged();
        okAction();
    }

    @Override
    public void rowNumberCheckChanged() {
        super.rowNumberCheckChanged();
        okAction();
    }

    @FXML
    @Override
    public void okAction() {
        if (!checkOptions()) {
            return;
        }
        task = new SingletonTask<Void>(this) {

            private String html;

            @Override
            protected boolean handle() {
                html = handleData();
                return html != null;
            }

            @Override
            protected void whenSucceeded() {
                outputHtml(html);
            }

        };
        start(task);
    }

    protected String handleData() {
        return null;
    }

    protected void outputHtml(String html) {
        webViewController.loadContents(html);
    }

    @FXML
    public void editAction() {
        webViewController.editAction();
    }

    @FXML
    public void popFunctionsMenu(MouseEvent mouseEvent) {
        webViewController.popFunctionsMenu(mouseEvent);
    }

}
