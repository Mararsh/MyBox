package mara.mybox.controller;

import java.nio.charset.Charset;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.StringTools;
import mara.mybox.tools.TextTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-8-25
 * @License Apache License Version 2.0
 */
public class DataFileTextController extends BaseDataFileController {

    protected Charset sourceCharset;
    protected boolean autoDetermine;

    @FXML
    protected CheckBox sourceWithNamesCheck;
    @FXML
    protected ToggleGroup sourceCharsetGroup;
    @FXML
    protected ComboBox<String> sourceCharsetSelector;
    @FXML
    protected RadioButton autoCharsetRadio;
    @FXML
    protected ControlSheetText sheetController;
    @FXML
    protected ControlTextWriteOptions writeOptionsController;

    public DataFileTextController() {
        baseTitle = message("EditTextDataFile");
        TipsLabelKey = "DataFileTextTips";
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Text);
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            dataController = sheetController;
            dataController.setParent(this);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            initSourceOptions();

            writeOptionsController.setControls(baseName);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void initSourceOptions() {
        try {
            autoDetermine = true;
            try {
                sourceCharset = Charset.forName(UserConfig.getString(baseName + "SourceCharset", Charset.defaultCharset().name()));
            } catch (Exception e) {
                sourceCharset = Charset.defaultCharset();
            }
            sourceCharsetGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkSourceCharset();
                }
            });
            sourceCharsetSelector.getItems().addAll(TextTools.getCharsetNames());
            sourceCharsetSelector.setValue(sourceCharset.name());
            sourceCharsetSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkSourceCharset();
                }
            });
            sourceWithNamesCheck.setSelected(UserConfig.getBoolean(baseName + "SourceWithNames", true));
            sourceWithNamesCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "SourceWithNames", newValue);
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void checkSourceCharset() {
        if (autoCharsetRadio.isSelected()) {
            autoDetermine = true;
            sourceCharsetSelector.setDisable(true);
        } else {
            autoDetermine = false;
            sourceCharset = Charset.forName(sourceCharsetSelector.getSelectionModel().getSelectedItem());
            sourceCharsetSelector.setDisable(false);
        }
        UserConfig.setString(baseName + "SourceCharset", sourceCharset.name());
    }

    @Override
    public void pickOptions() {
        try {
            sheetController.sourceCharset = sourceCharset;
            sheetController.sourceWithNames = sourceWithNamesCheck.isSelected();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    protected void updateInfoLabel() {
        if (sourceFile == null) {
            fileInfoLabel.setText("");
        } else {
            String info;
            info = message("FileSize") + ": " + FileTools.showFileSize(sourceFile.length()) + "\n"
                    + message("FileModifyTime") + ": " + DateTools.datetimeToString(sourceFile.lastModified()) + "\n"
                    + message("Charset") + ": " + sheetController.sourceCharset + "\n"
                    + message("Delimiter") + ": " + TextTools.delimiterMessage(sheetController.fileDelimiterName) + "\n"
                    + message("RowsNumber") + ": " + sheetController.rowsTotal() + "\n"
                    + (sheetController.columns == null ? "" : message("ColumnsNumber") + ": " + sheetController.columns.size() + "\n")
                    + message("FirstLineAsNames") + ": " + (sheetController.sourceWithNames ? message("Yes") : message("No")) + "\n"
                    + message("CurrentPage") + ": " + StringTools.format(sheetController.currentPage)
                    + " / " + StringTools.format(sheetController.pagesNumber) + "\n";
            if (sheetController.pagesNumber > 1 && sheetController.sheetInputs != null) {
                info += message("RowsRangeInPage")
                        + ": " + StringTools.format(sheetController.currentPageStart) + " - "
                        + StringTools.format(sheetController.currentPageStart + sheetController.sheetInputs.length - 1)
                        + " ( " + StringTools.format(sheetController.sheetInputs.length) + " )\n";
            }
            info += message("PageModifyTime") + ": " + DateTools.nowString();
            fileInfoLabel.setText(info);
        }
    }

    @FXML
    @Override
    public void saveAsAction() {
        sheetController.sourceFile = sourceFile;
        sheetController.saveAsType = saveAsType;
        sheetController.targetCharset = writeOptionsController.charset;
        sheetController.targetDelimiterName = writeOptionsController.delimiterName;
        sheetController.targetWithNames = writeOptionsController.withNamesCheck.isSelected();
        sheetController.saveAs();
    }

}
