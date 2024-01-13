package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import mara.mybox.data.FileEditInformation.StringFilterType;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.tools.ByteTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-7-15
 * @License Apache License Version 2.0
 */
public class ControlTextFilter extends BaseController {

    protected StringFilterType filterType;
    protected String[] filterStrings;
    protected long maxLen;
    protected boolean isBytes;
    protected SimpleBooleanProperty valid;

    @FXML
    protected ToggleGroup filterGroup;
    @FXML
    protected Button exampleFilterButton;
    @FXML
    protected TextField filterInput;
    @FXML
    protected CheckBox filterLineNumberCheck;
    @FXML
    protected Label inputLabel;
    @FXML
    protected FlowPane buttonsPane;

    @Override
    public void initControls() {
        try {
            super.initControls();

            filterType = StringFilterType.IncludeOne;
            maxLen = Long.MAX_VALUE;
            isBytes = false;
            valid = new SimpleBooleanProperty(false);

            filterGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {
                    checkFilterType();
                }
            });

            filterInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (!isSettingValues) {
                        checkFilterStrings();
                    }
                }
            });

            filterLineNumberCheck.setSelected(UserConfig.getBoolean(baseName + "FilterRecordLineNumber", true));
            filterLineNumberCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "FilterRecordLineNumber", filterLineNumberCheck.isSelected());
                }
            });

            checkFilterType();
            checkFilterStrings();

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    protected void checkFilterType() {
        if (filterGroup == null) {
            return;
        }
        String selected = ((RadioButton) filterGroup.getSelectedToggle()).getText();
        for (StringFilterType type : StringFilterType.values()) {
            if (Languages.message(type.name()).equals(selected) || type.name().equals(selected)) {
                filterType = type;
                break;
            }
        }
        if (filterType == StringFilterType.MatchRegularExpression
                || filterType == StringFilterType.NotMatchRegularExpression
                || filterType == StringFilterType.IncludeRegularExpression
                || filterType == StringFilterType.NotIncludeRegularExpression) {
            if (!buttonsPane.getChildren().contains(exampleFilterButton)) {
                buttonsPane.getChildren().add(0, exampleFilterButton);
                refreshStyle(buttonsPane);
            }
            inputLabel.setVisible(false);

        } else {
            if (buttonsPane.getChildren().contains(exampleFilterButton)) {
                buttonsPane.getChildren().remove(exampleFilterButton);
            }
            inputLabel.setVisible(true);
        }
    }

    protected void checkFilterStrings() {
        filterStrings = null;
        String string = filterInput.getText();
        if (string.isEmpty()) {
            valid.set(false);
            return;
        }
        if (string.length() >= maxLen) {
            popError(Languages.message("FindStringLimitation"));
            valid.set(false);
            return;
        }
        if (filterType == StringFilterType.MatchRegularExpression
                || filterType == StringFilterType.NotMatchRegularExpression
                || filterType == StringFilterType.IncludeRegularExpression
                || filterType == StringFilterType.NotIncludeRegularExpression) {
            filterStrings = new String[1];
            filterStrings[0] = string;
        } else {
            if (isBytes) {
                validateBytes();
            } else {
                filterStrings = StringTools.splitByComma(string);
            }
        }
        valid.set(filterStrings != null && filterStrings.length > 0);
    }

    public void validateBytes() {
        if (filterInput.getText().trim().endsWith(",")) {
            return;
        }
        String[] strings = StringTools.splitByComma(filterInput.getText());
        List<String> vs = new ArrayList<>();
        for (String s : strings) {
            String v = ByteTools.formatTextHex(s);
            if (v == null) {
                filterInput.setStyle(UserConfig.badStyle());
                return;
            }
            if (v.length() >= maxLen * 3) {
                popError(Languages.message("FindStringLimitation"));
                filterInput.setStyle(UserConfig.badStyle());
                return;
            }
            vs.add(v);
        }
        if (vs.isEmpty()) {
            return;
        }
        filterStrings = new String[vs.size()];
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < vs.size(); ++i) {
            filterStrings[i] = vs.get(i);
            if (i == 0) {
                sb.append(filterStrings[i]);
            } else {
                sb.append(",").append(filterStrings[i]);
            }
        }
        filterInput.setStyle(null);
        final String fixed = sb.toString();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                isSettingValues = true;
                filterInput.setText(fixed);
                filterInput.end();
                isSettingValues = false;
            }
        });
    }

    public boolean pickValue() {
        if (valid.get()) {
            TableStringValues.add(baseName + "FilterString", filterInput.getText());
            return true;
        } else {
            return false;
        }
    }

    @FXML
    protected void showFilterExample(Event event) {
        PopTools.popRegexExamples(this, filterInput, event);
    }

    @FXML
    protected void popFilterExample(Event event) {
        if (UserConfig.getBoolean("RegexExamplesPopWhenMouseHovering", false)) {
            showFilterExample(event);
        }
    }

    @FXML
    protected void showHistories(Event event) {
        PopTools.popStringValues(this, filterInput, event, baseName + "FilterString", false);
    }

    @FXML
    protected void popHistories(Event event) {
        if (UserConfig.getBoolean(baseName + "FilterStringPopWhenMouseHovering", false)) {
            showHistories(event);
        }
    }

    @FXML
    public void clearAction(Event event) {
        filterInput.clear();
    }

}
