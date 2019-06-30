package mara.mybox.controller.base;

import java.io.File;
import java.util.ArrayList;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import mara.mybox.data.VisitHistory;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.value.CommonValues;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import mara.mybox.color.ChromaticAdaptation;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVaribles;
import static mara.mybox.value.AppVaribles.getMessage;

/**
 * @Author Mara
 * @CreateDate 2019-6-2 10:59:16
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class ChromaticityBaseController extends BaseController {

    public int scale = 8;
    public double sourceX, sourceY, sourceZ, targetX, targetY, targetZ;
    public ChromaticAdaptation.ChromaticAdaptationAlgorithm algorithm;
    public String exportName;

    @FXML
    public TextField scaleInput;
    @FXML
    public ToggleGroup algorithmGroup;

    public ChromaticityBaseController() {
        baseTitle = AppVaribles.getMessage("Chromaticity");
        exportName = "ChromaticityData";

        SourceFileType = VisitHistory.FileType.Text;
        SourcePathType = VisitHistory.FileType.Text;
        TargetPathType = VisitHistory.FileType.Text;
        TargetFileType = VisitHistory.FileType.Text;
        AddFileType = VisitHistory.FileType.Text;
        AddPathType = VisitHistory.FileType.Text;

        fileExtensionFilter = CommonValues.TxtExtensionFilter;
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
    public void popExportPath(MouseEvent event) { //
        if (popMenu != null && popMenu.isShowing()) {
            popMenu.hide();
            popMenu = null;
        }
        List<VisitHistory> his = VisitHistory.getRecentPath(VisitHistory.FileType.Text);
        List<String> paths = new ArrayList();
        if (his != null) {
            for (VisitHistory h : his) {
                String pathname = h.getResourceValue();
                paths.add(pathname);
            }
        }
        if (paths.isEmpty()) {
            return;
        }
        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        MenuItem dmenu = new MenuItem(getMessage("RecentAccessedDirectories"));
        dmenu.setStyle("-fx-text-fill: #2e598a;");
        popMenu.getItems().add(dmenu);
        MenuItem menu;
        for (String path : paths) {
            menu = new MenuItem(path);
            final String p = path;
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    AppVaribles.setUserConfigValue(targetPathKey, p);
                    exportAction();
                }
            });
            popMenu.getItems().add(menu);
        }

        popMenu.getItems().add(new SeparatorMenuItem());
        menu = new MenuItem(getMessage("MenuClose"));
        menu.setStyle("-fx-text-fill: #2e598a;");
        menu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                popMenu.hide();
                popMenu = null;
            }
        });
        popMenu.getItems().add(menu);

        FxmlControl.locateBelow((Region) event.getSource(), popMenu);

    }

    // should rewrite this
    public String exportTexts() {
        return "";
    }

    @FXML
    public void exportAction() {
        final FileChooser fileChooser = new FileChooser();
        File path = AppVaribles.getUserConfigPath(targetPathKey);
        if (path.exists()) {
            fileChooser.setInitialDirectory(path);
        }
        fileChooser.setInitialFileName(exportName);
        fileChooser.getExtensionFilters().addAll(CommonValues.TxtExtensionFilter);
        final File file = fileChooser.showSaveDialog(getMyStage());
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
                            popInformation(AppVaribles.getMessage("Successful"));
                        } else {
                            popInformation(AppVaribles.getMessage("failed"));
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
