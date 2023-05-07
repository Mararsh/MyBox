package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import mara.mybox.data.FindReplaceString;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WebViewTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.tools.HtmlWriteTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-5-5
 * @License Apache License Version 2.0
 */
public class HtmlFindController extends WebAddressController {

    protected static final String ItemPrefix = "MyBoxSearchLocation";
    protected int foundCount, foundItem;
    protected String sourceAddress, sourceHtml;
    protected LoadingController loading;
    protected boolean isQuerying;

    @FXML
    protected ComboBox<String> findFontSelector, foundItemSelector;
    @FXML
    protected TextField findInput;
    @FXML
    protected ColorSet findColorController, findBgColorController, currentColorController, currentBgColorController;
    @FXML
    protected Label foundLabel;
    @FXML
    protected Button goItemButton, queryButton, examplePopFindButton;
    @FXML
    protected CheckBox caseCheck, wrapCheck, regCheck;

    public HtmlFindController() {
        baseTitle = message("WebFind");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            findColorController.init(this, baseName + "FindColor", Color.YELLOW);
            findBgColorController.init(this, baseName + "FindBgColor", Color.BLACK);
            currentColorController.init(this, baseName + "CurrentColor", Color.RED);
            currentBgColorController.init(this, baseName + "CurrentBgColor", Color.BLACK);

            List<String> fonts = new ArrayList();
            fonts.addAll(Arrays.asList("1em", "1.2em", "1.5em", "24px", "28px"));
            String saved = UserConfig.getString(baseName + "Font", "1.2em");
            if (!fonts.contains(saved)) {
                fonts.add(0, saved);
            }
            findFontSelector.getItems().addAll(fonts);
            findFontSelector.getSelectionModel().select(saved);

            caseCheck.setSelected(UserConfig.getBoolean(baseName + "CaseInsensitive", false));
            caseCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "CaseInsensitive", caseCheck.isSelected());
                }
            });

            regCheck.setSelected(UserConfig.getBoolean(baseName + "RegularExpression", false));
            regCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "RegularExpression", regCheck.isSelected());
                }
            });

            wrapCheck.setSelected(UserConfig.getBoolean(baseName + "WrapAround", false));
            wrapCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "WrapAround", wrapCheck.isSelected());
                }
            });

            goItemButton.setDisable(true);
            firstButton.setDisable(true);
            previousButton.setDisable(true);
            nextButton.setDisable(true);
            lastButton.setDisable(true);

            examplePopFindButton.disableProperty().bind(regCheck.selectedProperty().not());

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(queryButton, new Tooltip(message("Query") + "\nF1"));
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void find(String html) {
        sourceHtml = html;
        foundCount = 0;
        loadContents(html);
    }

    @FXML
    @Override
    public void goAction() {
        foundCount = 0;
        sourceHtml = null;
        reset();
        super.goAction();
    }

    protected void reset() {
        foundCount = 0;
        foundLabel.setText("");
        foundItemSelector.getItems().clear();
        goItemButton.setDisable(true);
        firstButton.setDisable(true);
        previousButton.setDisable(true);
        nextButton.setDisable(true);
        lastButton.setDisable(true);
        isQuerying = false;
    }

    @Override
    public void pageLoaded() {
        try {
            super.pageLoaded();

            if (sourceHtml == null) {
                sourceHtml = WebViewTools.getHtml(webEngine);
                sourceAddress = webViewController.address;

            } else if (isQuerying) {
                popInformation(message("Found") + ": " + foundCount);

            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    protected void queryAction() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        if (sourceHtml == null) {
            sourceHtml = WebViewTools.getHtml(webEngine);
        }
        if (sourceHtml == null || sourceHtml.isBlank()) {
            popError(message("NoData"));
            return;
        }
        String string = findInput.getText();
        if (string == null || string.isBlank()) {
            popError(message("InvalidParameters") + ": " + message("Find"));
            return;
        }
        TableStringValues.add("HtmlFindHistories", string);
        reset();
        isQuerying = true;
        task = new SingletonTask<Void>(this) {

            private StringBuilder results;

            @Override
            protected boolean handle() {
                try {
                    String findString = HtmlWriteTools.stringToHtml(string);

                    String font = findFontSelector.getValue();
                    UserConfig.setString(baseName + "Font", font);

                    FindReplaceString textsChecker = FindReplaceString.create()
                            .setOperation(FindReplaceString.Operation.FindNext)
                            .setIsRegex(false).setCaseInsensitive(true).setMultiline(true);

                    FindReplaceString finder = FindReplaceString.create()
                            .setOperation(FindReplaceString.Operation.FindNext).setFindString(findString)
                            .setIsRegex(regCheck.isSelected()).setCaseInsensitive(caseCheck.isSelected()).setMultiline(true);
                    String inputString = sourceHtml;
                    String replaceSuffix = " style=\"" + itemsStyle() + "\" >" + findString + "</span>";

                    results = new StringBuilder();
                    String texts;

                    textsChecker.setInputString(inputString).setFindString("</head>").setAnchor(0).handleString();
                    if (textsChecker.getStringRange() != null) {
                        results.append(inputString.substring(0, textsChecker.getLastEnd()));
                        inputString = inputString.substring(textsChecker.getLastEnd());
                    }
                    while (!inputString.isBlank()) {
                        textsChecker.setInputString(inputString).setFindString(">").setAnchor(0).handleString();
                        if (textsChecker.getStringRange() == null) {
                            break;
                        }
                        results.append(inputString.substring(0, textsChecker.getLastEnd()));
                        inputString = inputString.substring(textsChecker.getLastEnd());
                        textsChecker.setInputString(inputString).setFindString("<").setAnchor(0).handleString();
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
                            finder.setInputString(texts).setAnchor(0).handleString();
                            if (finder.getStringRange() == null) {
                                break;
                            }
                            String replaceString = "<span id=\"" + ItemPrefix + (++foundCount) + "\" " + replaceSuffix;
                            if (finder.getLastStart() > 0) {
                                r.append(texts.substring(0, finder.getLastStart()));
                            }
                            r.append(replaceString);
                            texts = texts.substring(finder.getLastEnd());
                            Platform.runLater(() -> {
                                loading.setInfo(message("Found") + ": " + foundCount);
                            });
                        }
                        r.append(texts);
                        results.append(r.toString());
                    }
                    results.append(inputString);

//                        String prehead = HtmlReadTools.preHtml(sourceHtml);
//                        String head = HtmlReadTools.tag(sourceHtml, "head", true);
//                        html = (prehead != null ? prehead : "")
//                                + "<html>\n"
//                                + (head != null ? head : "")
//                                + "\n<body>\n"
//                                + results.toString()
//                                + "\n</body>\n</html>";
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                String info = message("Found") + ": " + foundCount;
                foundLabel.setText(info);

                foundItem = 0;
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
                loadContents(results.toString());
            }

            @Override
            protected void finalAction() {
                loading = null;
                task = null;
            }

        };
        loading = start(task);
    }

    protected String itemsStyle() {
        return "color:" + findColorController.rgb()
                + "; background: " + findBgColorController.rgb()
                + "; font-size:" + findFontSelector.getValue() + ";";
    }

    protected String currentStyle() {
        return "color:" + currentColorController.rgb()
                + "; background: " + currentBgColorController.rgb()
                + "; font-size:" + findFontSelector.getValue() + ";";
    }

    protected void setStyle(int id, String style) {
        try {
            if (id <= 0 || id > foundCount) {
                return;
            }
            webEngine.executeScript("document.getElementById('" + ItemPrefix + id + "').setAttribute('style', '" + style + "');");
        } catch (Exception e) {
        }
    }

    protected void scrollTo(int id) {
        try {
            if (id <= 0 || id > foundCount) {
                return;
            }
            webEngine.executeScript("document.getElementById('" + ItemPrefix + id + "').scrollIntoView();");
        } catch (Exception e) {
        }
    }

    // 1-based
    protected void goItem(int index) {
        setStyle(foundItem, itemsStyle());
        foundItem = index;
        if (foundItem < 1) {
            foundItem = wrapCheck.isSelected() ? foundCount : 1;
        }
        if (foundItem > foundCount) {
            foundItem = wrapCheck.isSelected() ? 1 : foundCount;
        }
        foundItemSelector.getSelectionModel().select(foundItem + "");
        scrollTo(foundItem);
        setStyle(foundItem, currentStyle());
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

    @FXML
    protected void showFindExample(Event event) {
        PopTools.popRegexExamples(this, findInput, event);
    }

    @FXML
    protected void popFindExample(Event event) {
        if (UserConfig.getBoolean("RegexExamplesPopWhenMouseHovering", false)) {
            showFindExample(event);
        }
    }

    @FXML
    @Override
    public void recoverAction() {
        reset();
        loadContents(sourceAddress, sourceHtml);
    }

    @FXML
    protected void showFindHistories(Event event) {
        PopTools.popStringValues(this, findInput, event, "HtmlFindHistories", false, true);
    }

    @FXML
    public void popFindHistories(Event event) {
        if (UserConfig.getBoolean("HtmlFindHistoriesPopWhenMouseHovering", false)) {
            showFindHistories(event);
        }
    }

    @Override
    public boolean keyF1() {
        queryAction();
        return true;
    }

}
