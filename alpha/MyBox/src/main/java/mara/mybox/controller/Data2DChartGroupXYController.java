package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-10-14
 * @License Apache License Version 2.0
 */
public class Data2DChartGroupXYController extends Data2DChartXYController {

    protected int maxData = -1;
    protected List<String> sorts;

    @FXML
    protected ControlData2DGroup groupController;
    @FXML
    protected ControlSelection sortController;
    @FXML
    protected TextField maxInput;
    @FXML
    protected CheckBox displayAllCheck;
    @FXML
    protected ControlPlay playController;

    public Data2DChartGroupXYController() {
        baseTitle = message("XYChart");
        TipsLabelKey = "DataChartXYTips";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            groupController.setParameters(this);
            groupController.columnsController.selectedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    makeSortList();
                }
            });

            sortController.setParameters(this, message("Sort"), message("Sort"));

            if (playController != null) {
                playController.setParameters(this);

                playController.frameNodify.addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
//                    displayFrame(playController.currentIndex);
                    }
                });

                playController.stopNodify.addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
//                    closeFile();
                    }
                });

                playController.intervalNodify.addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
//                    if (imageInfos != null) {
//                        for (ImageInformation info : imageInfos) {
//                            info.setDuration(playController.interval);
//                        }
//                    }
                    }
                });
            }

            maxData = UserConfig.getInt(baseName + "MaxDataNumber", -1);
            if (maxData > 0) {
                maxInput.setText(maxData + "");
            }
            maxInput.setStyle(null);
            maxInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    String maxs = maxInput.getText();
                    if (maxs == null || maxs.isBlank()) {
                        maxData = -1;
                        maxInput.setStyle(null);
                        UserConfig.setLong(baseName + "MaxDataNumber", -1);
                    } else {
                        try {
                            maxData = Integer.valueOf(maxs);
                            maxInput.setStyle(null);
                            UserConfig.setLong(baseName + "MaxDataNumber", maxData);
                        } catch (Exception e) {
                            maxInput.setStyle(UserConfig.badStyle());
                        }
                    }
                }
            });

            displayAllCheck.setSelected(UserConfig.getBoolean(baseName + "DisplayAll", true));
            displayAllCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                UserConfig.setBoolean(baseName + "DisplayAll", displayAllCheck.isSelected());
                noticeMemory();
                refreshAction();
            });

            displayAllCheck.visibleProperty().bind(allPagesRadio.selectedProperty());

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void refreshControls() {
        try {
            super.refreshControls();

            groupController.refreshControls();

            if (!data2D.isValid()) {
                sortController.loadNames(null);
                return;
            }
            makeSortList();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void makeSortList() {
        try {
            List<String> names = new ArrayList<>();
            names.add(message("Count") + "-" + message("Descending"));
            names.add(message("Count") + "-" + message("Ascending"));
            if (groupController.byValues()) {
                List<String> groups = groupController.groupNames;
                if (groups != null) {
                    for (String name : groups) {
                        names.add(name + "-" + message("Descending"));
                        names.add(name + "-" + message("Ascending"));
                    }
                }
            }
            sortController.loadNames(names);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void noticeMemory() {
        noticeLabel.setVisible(isAllPages() && displayAllCheck.isSelected());
    }

    @Override
    public boolean initData() {
        if (!groupController.pickValues()) {
            return false;
        }
        if (categoryColumnSelector != null) {
            return super.initData();
        } else {
            checkObject();
            checkInvalidAs();
            return true;
        }
    }

    /*
        static
     */
    public static Data2DChartGroupXYController open(ControlData2DLoad tableController) {
        try {
            Data2DChartGroupXYController controller = (Data2DChartGroupXYController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DChartXYFxml, false);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
