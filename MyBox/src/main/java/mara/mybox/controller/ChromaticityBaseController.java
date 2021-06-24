package mara.mybox.controller;

import java.io.File;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import mara.mybox.color.ChromaticAdaptation;
import mara.mybox.data.StringTable;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.data.VisitHistoryTools;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.fxml.RecentVisitMenu;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.HtmlTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;

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
        baseTitle = AppVariables.message("Chromaticity");
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
            int p = AppVariables.getUserConfigInt("MatrixDecimalScale", 8);
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
                AppVariables.setUserConfigInt("MatrixDecimalScale", scale);
            }
        } catch (Exception e) {
            scaleInput.setStyle(badStyle);
        }
    }

    @FXML
    public void aboutColor() {
        openLink(aboutColorHtml());
    }

    public static File aboutColorHtml() {
        try {
            StringTable table = new StringTable(null, message("ResourcesAboutColor"));
            table.newLinkRow("ICCWebsite", "http://www.color.org");
            table.newLinkRow("ICCProfileTags", "https://sno.phy.queensu.ca/~phil/exiftool/TagNames/ICC_Profile.html");
            table.newLinkRow("IccProfilesECI", "http://www.eci.org/en/downloads");
            table.newLinkRow("IccProfilesAdobe", "https://supportdownloads.adobe.com/detail.jsp?ftpID=3680");
            table.newLinkRow("ColorSpace", "http://brucelindbloom.com/index.html?WorkingSpaceInfo.html#Specifications");
            table.newLinkRow("StandardsRGB", "https://www.w3.org/Graphics/Color/sRGB.html");
            table.newLinkRow("RGBXYZMatrices", "http://brucelindbloom.com/index.html?Eqn_RGB_XYZ_Matrix.html");
            table.newLinkRow("ColorCalculator", "http://www.easyrgb.com/en/math.php");
            table.newLinkRow("", "http://brucelindbloom.com/index.html?ColorCalculator.html");
            table.newLinkRow("", "http://davengrace.com/cgi-bin/cspace.pl");
            table.newLinkRow("ColorData", "https://www.rit.edu/science/pocs/useful-data");
            table.newLinkRow("", "http://www.thefullwiki.org/Standard_illuminant");
            table.newLinkRow("ColorTopics", "https://www.codeproject.com/Articles/1202772/Color-Topics-for-Programmers");
            table.newLinkRow("", "https://www.w3.org/TR/css-color-4/#lab-to-rgb");
            table.newLinkRow("ChromaticAdaptation", "http://brucelindbloom.com/index.html?Eqn_RGB_XYZ_Matrix.html");
            table.newLinkRow("ChromaticityDiagram", "http://demonstrations.wolfram.com/CIEChromaticityDiagram/");

            File htmFile = HtmlTools.writeHtml(table.html());
            return htmFile;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    @FXML
    public void popExportPath(MouseEvent event) {
        if (AppVariables.fileRecentNumber <= 0) {
            return;
        }
        new RecentVisitMenu(this, event) {
            @Override
            public List<VisitHistory> recentFiles() {
                return null;
            }

            @Override
            public List<VisitHistory> recentPaths() {
                return VisitHistoryTools.getRecentPath(VisitHistory.FileType.Text);
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
        final File file = chooseSaveFile(AppVariables.getUserConfigPath(targetPathKey),
                exportName, CommonFxValues.TextExtensionFilter);
        if (file == null) {
            return;
        }
        recordFileWritten(file, VisitHistory.FileType.Text);

        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    return FileTools.writeFile(file, exportTexts()) != null;
                }

                @Override
                protected void whenSucceeded() {
                    view(file);
                    popSuccessful();
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

}
