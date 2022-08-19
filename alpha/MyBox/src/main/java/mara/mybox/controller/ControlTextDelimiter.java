package mara.mybox.controller;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-9-16
 * @License Apache License Version 2.0
 */
public class ControlTextDelimiter extends BaseController {

    protected String delimiterName;
    protected SimpleBooleanProperty changedNotify;

    @FXML
    protected ToggleGroup delimiterGroup;
    @FXML
    protected RadioButton blankRadio, blank4Radio, blank8Radio, blanksRadio, tabRadio, commaRadio,
            lineRadio, atRadio, sharpRadio, semicolonsRadio, stringRadio,
            hyphenRadio, plusRadio, colonRadio, andRadio, percentRadio, exclamationRadio, quoteRadio,
            questionRadio, dotRadio, asteriskRadio, slashRadio, backslashRadio,
            underlineRadio, equalRadio, lessRadio, greateRadio, singleQuoteRadio;
    @FXML
    protected TextField delimiterInput;

    public ControlTextDelimiter() {
        changedNotify = new SimpleBooleanProperty(false);
    }

    public void setControls(String baseName, boolean hasBlanks) {
        try {
            this.baseName = baseName;

            setDelimiter(UserConfig.getString(baseName + "TextDelimiter", ","));

            delimiterGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    delimiterInput.setStyle(null);
                    if (stringRadio.isSelected()) {
                        String v = delimiterInput.getText();
                        if (v == null || v.isBlank()) {
                            delimiterInput.setStyle(UserConfig.badStyle());
                            return;
                        }
                        delimiterName = v;
                    } else if (blankRadio.isSelected()) {
                        delimiterName = "Blank";
                    } else if (blank4Radio.isSelected()) {
                        delimiterName = "Blank4";
                    } else if (blank8Radio.isSelected()) {
                        delimiterName = "Blank8";
                    } else if (blanksRadio.isSelected()) {
                        delimiterName = "Blanks";
                    } else if (tabRadio.isSelected()) {
                        delimiterName = "Tab";
                    } else if (commaRadio.isSelected()) {
                        delimiterName = ",";
                    } else if (lineRadio.isSelected()) {
                        delimiterName = "|";
                    } else if (atRadio.isSelected()) {
                        delimiterName = "@";
                    } else if (sharpRadio.isSelected()) {
                        delimiterName = "#";
                    } else if (semicolonsRadio.isSelected()) {
                        delimiterName = ";";
                    } else if (hyphenRadio.isSelected()) {
                        delimiterName = "-";
                    } else if (plusRadio.isSelected()) {
                        delimiterName = "+";
                    } else if (colonRadio.isSelected()) {
                        delimiterName = ":";
                    } else if (andRadio.isSelected()) {
                        delimiterName = "&";
                    } else if (percentRadio.isSelected()) {
                        delimiterName = "%";
                    } else if (exclamationRadio.isSelected()) {
                        delimiterName = "!";
                    } else if (quoteRadio.isSelected()) {
                        delimiterName = "\"";
                    } else if (singleQuoteRadio.isSelected()) {
                        delimiterName = "'";
                    } else if (questionRadio.isSelected()) {
                        delimiterName = "?";
                    } else if (dotRadio.isSelected()) {
                        delimiterName = ".";
                    } else if (asteriskRadio.isSelected()) {
                        delimiterName = "*";
                    } else if (slashRadio.isSelected()) {
                        delimiterName = "\\";
                    } else if (backslashRadio.isSelected()) {
                        delimiterName = "/";
                    } else if (underlineRadio.isSelected()) {
                        delimiterName = "_";
                    } else if (equalRadio.isSelected()) {
                        delimiterName = "=";
                    } else if (lessRadio.isSelected()) {
                        delimiterName = "<";
                    } else if (greateRadio.isSelected()) {
                        delimiterName = ">";
                    }
                    UserConfig.setString(baseName + "TextDelimiter", delimiterName);
                    changedNotify.set(!changedNotify.get());
                }
            });

            delimiterInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (!stringRadio.isSelected()) {
                        return;
                    }
                    if (newValue == null || newValue.isBlank()) {
                        delimiterInput.setStyle(UserConfig.badStyle());
                        return;
                    }
                    delimiterName = newValue;
                    UserConfig.setString(baseName + "TextDelimiter", delimiterName);
                    delimiterInput.setStyle(null);
                    changedNotify.set(!changedNotify.get());
                }
            });

            if (!hasBlanks) {
                if (blanksRadio.isSelected()) {
                    blankRadio.setSelected(true);
                }
                blanksRadio.setDisable(true);
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setDelimiter(String name) {
        try {
            if (name == null) {
                return;
            }
            delimiterName = name;
            switch (delimiterName.toLowerCase()) {
                case "blank":
                case " ":
                    blankRadio.setSelected(true);
                    break;
                case "blank4":
                case "    ":
                    blank4Radio.setSelected(true);
                    break;
                case "blank8":
                case "        ":
                    blank8Radio.setSelected(true);
                    break;
                case "blanks":
                    blanksRadio.setSelected(true);
                    break;
                case "tab":
                case "\t":
                    tabRadio.setSelected(true);
                    break;
                case ",":
                    commaRadio.setSelected(true);
                    break;
                case "|":
                    lineRadio.setSelected(true);
                    break;
                case "@":
                    atRadio.setSelected(true);
                    break;
                case "#":
                    sharpRadio.setSelected(true);
                    break;
                case ";":
                    semicolonsRadio.setSelected(true);
                    break;
                case "-":
                    hyphenRadio.setSelected(true);
                    break;
                case "+":
                    plusRadio.setSelected(true);
                    break;
                case ":":
                    colonRadio.setSelected(true);
                    break;
                case "&":
                    andRadio.setSelected(true);
                    break;
                case "%":
                    percentRadio.setSelected(true);
                    break;
                case "!":
                    exclamationRadio.setSelected(true);
                    break;
                case "\"":
                    quoteRadio.setSelected(true);
                    break;
                case "'":
                    singleQuoteRadio.setSelected(true);
                    break;
                case "?":
                    questionRadio.setSelected(true);
                    break;
                case ".":
                    dotRadio.setSelected(true);
                    break;
                case "*":
                    asteriskRadio.setSelected(true);
                    break;
                case "\\":
                    slashRadio.setSelected(true);
                    break;
                case "/":
                    backslashRadio.setSelected(true);
                    break;
                case "_":
                    underlineRadio.setSelected(true);
                    break;
                case "=":
                    equalRadio.setSelected(true);
                    break;
                case "<":
                    lessRadio.setSelected(true);
                    break;
                case ">":
                    greateRadio.setSelected(true);
                    break;
                default:
                    if (delimiterName.isBlank()) {
                        blanksRadio.setSelected(true);
                    } else {
                        stringRadio.setSelected(true);
                        delimiterInput.setText(delimiterName);
                    }
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public String getDelimiter() {
        return delimiterName;
    }

    @FXML
    public void popRegexExample(MouseEvent mouseEvent) {
        PopTools.popRegexExample(this, delimiterInput, mouseEvent);
    }

    @Override
    public void cleanPane() {
        try {
            changedNotify = null;
            delimiterName = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
