package mara.mybox.controller.base;

import java.io.File;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import mara.mybox.color.ChromaticAdaptation;
import mara.mybox.data.VisitHistory;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.fxml.RecentVisitMenu;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVaribles;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2019-6-2 10:59:16
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class ChromaticityBaseController extends BaseController {

    protected int scale = 8;
    protected double sourceX, sourceY, sourceZ, targetX, targetY, targetZ;
    protected ChromaticAdaptation.ChromaticAdaptationAlgorithm algorithm;
    protected String exportName;

    @FXML
    protected TextField scaleInput;
    @FXML
    protected ToggleGroup algorithmGroup;

    public ChromaticityBaseController() {
        baseTitle = AppVaribles.message("Chromaticity");
        exportName = "ChromaticityData";

        SourceFileType = VisitHistory.FileType.Text;
        SourcePathType = VisitHistory.FileType.Text;
        TargetPathType = VisitHistory.FileType.Text;
        TargetFileType = VisitHistory.FileType.Text;
        AddFileType = VisitHistory.FileType.Text;
        AddPathType = VisitHistory.FileType.Text;

        sourceExtensionFilter = CommonValues.TxtExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;
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
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    checkScale();
                }
            });
            int p = AppVaribles.getUserConfigInt("MatrixDecimalScale", 8);
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
                scaleInput.setStyle(badStyle);
            } else {
                scale = p;
                scaleInput.setStyle(null);
                AppVaribles.setUserConfigInt("MatrixDecimalScale", scale);
            }
        } catch (Exception e) {
            scaleInput.setStyle(badStyle);
        }
    }

    @FXML
    public void openLinks() {
        openStage(CommonValues.ChromaticLinksFxml);
    }

    @FXML
    public void popExportPath(MouseEvent event) {
        if (AppVaribles.fileRecentNumber <= 0) {
            return;
        }
        new RecentVisitMenu(this, event) {
            @Override
            public List<VisitHistory> recentFiles() {
                return null;
            }

            @Override
            public List<VisitHistory> recentPaths() {
                return VisitHistory.getRecentPath(VisitHistory.FileType.Text);
            }

            @Override
            public void handleSelect() {
                exportAction();
            }

            @Override
            public void handleFile(String fname) {

            }

            @Override
            public void handlePath(String fname) {
                handleTargetPath(fname);
            }

        }.pop();
    }

    // should rewrite this
    public String exportTexts() {
        return "";
    }

    @FXML
    public void exportAction() {
        final File file = chooseSaveFile(AppVaribles.getUserConfigPath(targetPathKey),
                exportName, CommonValues.TxtExtensionFilter, true);
        if (file == null) {
            return;
        }
        recordFileWritten(file, targetPathKey, VisitHistory.FileType.Text, VisitHistory.FileType.Text);

        task = new Task<Void>() {
            private boolean ok;

            @Override
            protected Void call() throws Exception {
                ok = FileTools.writeFile(file, exportTexts());
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (ok) {
                            view(file);
//                            browseURI(file.toURI());
                            popInformation(AppVaribles.message("Successful"));
                        } else {
                            popInformation(AppVaribles.message("failed"));
                        }
                    }
                });
            }

        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

}
