package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.text.Font;
import mara.mybox.data.TextEditInformation;
import mara.mybox.data.VisitHistory;
import mara.mybox.data.tools.VisitHistoryTools;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.tools.TextTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;

/**
 * @Author Mara
 * @CreateDate 2018-9-21
 * @Description
 * @License Apache License Version 2.0
 */
public class TextEncodingBatchController extends FilesBatchController {

    protected boolean autoDetermine;
    protected TextEditInformation sourceInformation, targetInformation;

    @FXML
    protected ToggleGroup sourceGroup;
    @FXML
    protected ComboBox<String> sourceBox, targetBox;
    @FXML
    protected CheckBox targetBomCheck;

    public TextEncodingBatchController() {
        baseTitle = AppVariables.message("TextEncodingBatch");

        sourcePathKey = VisitHistoryTools.getPathKey(VisitHistory.FileType.Text);
        sourceExtensionFilter = CommonFxValues.TextExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            tableController.getNameFiltersSelector().getSelectionModel().select(1);
            tableController.getTableFiltersInput().setText("html  htm  txt md");

            startButton.disableProperty().bind(Bindings.isEmpty(targetPathInput.textProperty())
                    .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                    .or(Bindings.isEmpty(tableView.getItems()))
            );
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    @Override
    public void initOptionsSection() {

        sourceInformation = new TextEditInformation();
        targetInformation = new TextEditInformation();

        sourceGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                checkSource();
            }
        });

        List<String> setNames = TextTools.getCharsetNames();
        sourceBox.getItems().addAll(setNames);
        sourceBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String oldValue, String newValue) {
                sourceInformation.setCharset(Charset.forName(newValue));
            }
        });
        sourceBox.getSelectionModel().select(Charset.defaultCharset().name());
        checkSource();

        if (targetBox != null) {
            targetBox.getItems().addAll(setNames);
            targetBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    targetInformation.setCharset(Charset.forName(newValue));
                    if ("UTF-8".equals(newValue) || "UTF-16BE".equals(newValue)
                            || "UTF-16LE".equals(newValue) || "UTF-32BE".equals(newValue)
                            || "UTF-32LE".equals(newValue)) {
                        targetBomCheck.setDisable(false);
                    } else {
                        targetBomCheck.setDisable(true);
                        if ("UTF-16".equals(newValue) || "UTF-32".equals(newValue)) {
                            targetBomCheck.setSelected(true);
                        } else {
                            targetBomCheck.setSelected(false);
                        }
                    }
                }
            });
            targetBox.getSelectionModel().select(Charset.defaultCharset().name());

            Tooltip tips = new Tooltip(AppVariables.message("BOMcomments"));
            tips.setFont(new Font(16));
            FxmlControl.setTooltip(targetBomCheck, tips);
        }
    }

    protected void checkSource() {
        RadioButton selected = (RadioButton) sourceGroup.getSelectedToggle();
        if (message("DetermainAutomatically").equals(selected.getText())) {
            autoDetermine = true;
            sourceBox.setDisable(true);
        } else {
            autoDetermine = false;
            sourceInformation.setCharset(Charset.forName(sourceBox.getSelectionModel().getSelectedItem()));
            sourceBox.setDisable(false);
        }
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        try {
            countHandling(srcFile);
            File target = makeTargetFile(srcFile, targetPath);
            if (target == null) {
                return AppVariables.message("Skip");
            }
            sourceInformation.setFile(srcFile);
            if (autoDetermine) {
                boolean ok = TextTools.checkCharset(sourceInformation);
                if (!ok || sourceInformation == null) {
                    return AppVariables.message("Failed");
                }
            }
            targetInformation.setFile(target);
            targetInformation.setWithBom(targetBomCheck.isSelected());
            if (TextTools.convertCharset(sourceInformation, targetInformation)) {
                targetFileGenerated(target);
                return AppVariables.message("Successful");
            } else {
                return AppVariables.message("Failed");
            }
        } catch (Exception e) {
            logger.error(e.toString());
            return AppVariables.message("Failed");
        }
    }

}
