package mara.mybox.controller;

import java.io.File;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import mara.mybox.color.ChromaticAdaptation;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.data.VisitHistoryTools;
import mara.mybox.fxml.HelpTools;
import mara.mybox.fxml.RecentVisitMenu;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.tools.TextFileTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.FileFilters;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-6-2 10:59:16
 * @License Apache License Version 2.0
 */
public class ChromaticityBaseController extends BaseWebViewController {

    protected int scale = 8;
    protected double sourceX, sourceY, sourceZ, targetX, targetY, targetZ;
    protected ChromaticAdaptation.ChromaticAdaptationAlgorithm algorithm;
    protected String exportName;

    @FXML
    protected TextField scaleInput;
    @FXML
    protected ToggleGroup algorithmGroup;

    public ChromaticityBaseController() {
        baseTitle = Languages.message("Chromaticity");
        exportName = "ChromaticityData";
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Text);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            initOptions();
        } catch (Exception e) {

        }
    }

    public void initOptions() {
        if (algorithmGroup != null) {
            algorithmGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkAlgorithm();
                }
            });
            checkAlgorithm();
        }

        if (scaleInput != null) {
            scaleInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkScale();
                }
            });
            int p = UserConfig.getInt("MatrixDecimalScale", 8);
            scaleInput.setText(p + "");
        }
    }

    public void checkAlgorithm() {
        try {
            RadioButton selected = (RadioButton) algorithmGroup.getSelectedToggle();
            switch (selected.getText()) {
                case "Bradford":
                    algorithm = ChromaticAdaptation.ChromaticAdaptationAlgorithm.Bradford;
                    break;
                case "XYZ Scaling":
                    algorithm = ChromaticAdaptation.ChromaticAdaptationAlgorithm.XYZScaling;
                    break;
                case "Von Kries":
                    algorithm = ChromaticAdaptation.ChromaticAdaptationAlgorithm.VonKries;
                    break;
                default:
                    algorithm = ChromaticAdaptation.ChromaticAdaptationAlgorithm.Bradford;
            }
        } catch (Exception e) {
            algorithm = ChromaticAdaptation.ChromaticAdaptationAlgorithm.Bradford;
        }
    }

    public void checkScale() {
        try {
            int p = Integer.parseInt(scaleInput.getText());
            if (p < 0) {
                scaleInput.setStyle(UserConfig.badStyle());
            } else {
                scale = p;
                scaleInput.setStyle(null);
                UserConfig.setInt("MatrixDecimalScale", scale);
            }
        } catch (Exception e) {
            scaleInput.setStyle(UserConfig.badStyle());
        }
    }

    @FXML
    public void aboutColor() {
        openHtml(HelpTools.aboutColor());
    }

    public void showExportPathMenu(Event event) {
        if (AppVariables.fileRecentNumber <= 0) {
            return;
        }
        new RecentVisitMenu(this, event, true) {

            @Override
            public List<VisitHistory> recentPaths() {
                return VisitHistoryTools.getRecentPathWrite(VisitHistory.FileType.Text);
            }

            @Override
            public void handleSelect() {
                exportAction();
            }

            @Override
            public void handlePath(String fname) {
                handleTargetPath(fname);
            }

        }.pop();
    }

    @FXML
    public void pickExportPath(Event event) {
        if (UserConfig.getBoolean("RecentVisitMenuPopWhenMouseHovering", true)
                || AppVariables.fileRecentNumber <= 0) {
            exportAction();
        } else {
            showExportPathMenu(event);
        }
    }

    @FXML
    public void popExportPath(Event event) {
        if (UserConfig.getBoolean("RecentVisitMenuPopWhenMouseHovering", true)) {
            showExportPathMenu(event);
        }
    }

    // should rewrite this
    public String exportTexts() {
        return "";
    }

    @FXML
    public void exportAction() {
        final File file = chooseSaveFile(UserConfig.getPath(baseName + "TargetPath"),
                exportName, FileFilters.TextExtensionFilter);
        if (file == null) {
            return;
        }
        if (task != null && !task.isQuit()) {
            return;
        }
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                if (TextFileTools.writeFile(file, exportTexts()) == null) {
                    return false;
                }
                recordFileWritten(file, VisitHistory.FileType.Text);
                return true;
            }

            @Override
            protected void whenSucceeded() {
                view(file);
                popSuccessful();
            }

        };
        start(task);
    }

}
