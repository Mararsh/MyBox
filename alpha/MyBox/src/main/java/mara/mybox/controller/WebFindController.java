package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import mara.mybox.data.FindReplaceString;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WebViewTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-5-5
 * @License Apache License Version 2.0
 */
public class WebFindController extends BaseWebViewController {

    protected int foundCount, foundItem;
    protected String loadedHtml;

    @FXML
    protected ComboBox<String> findFontSelector, foundItemSelector;
    @FXML
    protected HBox findBox, findResultsBox;
    @FXML
    protected ControlStringSelector findInputController;
    @FXML
    protected ColorSet findColorController, findBgColorController;
    @FXML
    protected Label foundLabel;
    @FXML
    protected Button goItemButton;
    @FXML
    protected CheckBox caseCheck, wrapCheck, regCheck;

    public WebFindController() {
        baseTitle = Languages.message("WebFind");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            findInputController.init(this, baseName + "Find", "find", 20);

            findColorController.init(this, baseName + "FindColor", Color.YELLOW);
            findBgColorController.init(this, baseName + "FindBgColor", Color.BLACK);

            List<String> fonts = new ArrayList();
            fonts.addAll(Arrays.asList("1em", "1.2em", "1.5em", "24px", "28px"));
            String saved = UserConfig.getUserConfigString(baseName + "Font", "1.2em");
            if (!fonts.contains(saved)) {
                fonts.add(0, saved);
            }
            findFontSelector.getItems().addAll(fonts);
            findFontSelector.getSelectionModel().select(saved);

            caseCheck.setSelected(UserConfig.getUserConfigBoolean(baseName + "CaseInsensitive", false));
            caseCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    UserConfig.setUserConfigBoolean(baseName + "CaseInsensitive", caseCheck.isSelected());
                }
            });

            regCheck.setSelected(UserConfig.getUserConfigBoolean(baseName + "RegularExpression", false));
            regCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    UserConfig.setUserConfigBoolean(baseName + "RegularExpression", regCheck.isSelected());
                }
            });

            wrapCheck.setSelected(UserConfig.getUserConfigBoolean(baseName + "WrapAround", false));
            wrapCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    UserConfig.setUserConfigBoolean(baseName + "WrapAround", wrapCheck.isSelected());
                }
            });

            goItemButton.setDisable(true);
            firstButton.setDisable(true);
            previousButton.setDisable(true);
            nextButton.setDisable(true);
            lastButton.setDisable(true);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    @Override
    public void find(String html) {
        loadedHtml = html;
        loadContents(html);
    }

    @FXML
    @Override
    public void goAction() {
        foundCount = 0;
        loadedHtml = null;
        super.goAction();
    }

    @Override
    public void afterPageLoaded() {
        try {
            super.afterPageLoaded();
            bottomLabel.setText(Languages.message("Count") + ": " + foundCount);
            if (loadedHtml == null) {
                loadedHtml = WebViewTools.getHtml(webEngine);
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    protected void findString() {
        try {
            foundCount = 0;
            String value = findInputController.value();
            if (value == null || value.isBlank()) {
                parentController.popError(Languages.message("InvalidData"));
                return;
            }
            findInputController.refreshList();
            loadedHtml = loadedHtml == null ? WebViewTools.getHtml(webEngine) : loadedHtml;
            if (loadedHtml == null || loadedHtml.isBlank()) {
                parentController.popInformation(Languages.message("NoData"));
                return;
            }
            foundItemSelector.getItems().clear();
            goItemButton.setDisable(true);
            firstButton.setDisable(true);
            previousButton.setDisable(true);
            nextButton.setDisable(true);
            lastButton.setDisable(true);
            String findString = HtmlWriteTools.textToHtml(value);

            String font = findFontSelector.getValue();
            UserConfig.setUserConfigString(baseName + "Font", font);

            FindReplaceString textsChecker = FindReplaceString.create()
                    .setOperation(FindReplaceString.Operation.FindNext)
                    .setIsRegex(false).setCaseInsensitive(true).setMultiline(true);

            FindReplaceString finder = FindReplaceString.create()
                    .setOperation(FindReplaceString.Operation.FindNext).setFindString(findString)
                    .setIsRegex(regCheck.isSelected()).setCaseInsensitive(caseCheck.isSelected()).setMultiline(true);
            String inputString = loadedHtml;
            String replaceSuffix = " style=\"color:" + findColorController.rgb()
                    + "; background: " + findBgColorController.rgb()
                    + "; font-size:" + font + ";\">" + findString + "</span>";
            StringBuilder s = new StringBuilder();
            String texts;
            while (!inputString.isBlank()) {
                textsChecker.setInputString(inputString).setFindString(">").setAnchor(0).run();
                if (textsChecker.getStringRange() == null) {
                    break;
                }
                s.append(inputString.substring(0, textsChecker.getLastEnd()));
                inputString = inputString.substring(textsChecker.getLastEnd());
                textsChecker.setInputString(inputString).setFindString("<").setAnchor(0).run();
                if (textsChecker.getStringRange() == null) {
                    texts = inputString;
                    inputString = "";
                } else {
                    if (textsChecker.getLastStart() > 0) {
                        texts = inputString.substring(0, textsChecker.getLastStart());
                    } else {
                        texts = "";
                    }
                    inputString = inputString.substring(textsChecker.getLastStart());
                }
                if (texts.isEmpty()) {
                    continue;
                }
                StringBuilder r = new StringBuilder();
                while (!texts.isBlank()) {
                    finder.setInputString(texts).setAnchor(0).run();
                    if (finder.getStringRange() == null) {
                        break;
                    }
                    String replaceString = "<span id=\"MyBoxSearchLocation" + (++foundCount) + "\" " + replaceSuffix;
                    if (finder.getLastStart() > 0) {
                        r.append(texts.substring(0, finder.getLastStart()));
                    }
                    r.append(replaceString);
                    texts = texts.substring(finder.getLastEnd());
                }
                r.append(texts);
                s.append(r.toString());
            }
            s.append(inputString);
            webEngine.loadContent(s.toString());
            foundLabel.setText(foundCount + "");
            if (foundCount > 0) {
                List<String> numbers = new ArrayList<>();
                for (int i = 1; i <= foundCount; i++) {
                    numbers.add(i + "");
                }
                foundItemSelector.getItems().setAll(numbers);
                foundItemSelector.getSelectionModel().select(0);
                goItemButton.setDisable(false);
                firstButton.setDisable(false);
                previousButton.setDisable(false);
                nextButton.setDisable(false);
                lastButton.setDisable(false);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void goItem(int index) {
        foundItem = index;
        if (foundItem < 1) {
            foundItem = wrapCheck.isSelected() ? foundCount : 1;
        }
        if (foundItem > foundCount) {
            foundItem = wrapCheck.isSelected() ? 1 : foundCount;
        }
        foundItemSelector.getSelectionModel().select(foundItem + "");
        String id = "MyBoxSearchLocation" + foundItem;
        webEngine.executeScript("document.getElementById('" + id + "').scrollIntoView();");
    }

    @FXML
    protected void goItem() {
        String item = foundItemSelector.getValue();
        if (item == null || item.isBlank()) {
            return;
        }
        goItem(Integer.parseInt(item));
    }

    @FXML
    @Override
    public void firstAction() {
        goItem(1);
    }

    @FXML
    @Override
    public void previousAction() {
        goItem(foundItem - 1);
    }

    @FXML
    @Override
    public void nextAction() {
        goItem(foundItem + 1);
    }

    @FXML
    @Override
    public void lastAction() {
        goItem(foundCount);
    }

}
