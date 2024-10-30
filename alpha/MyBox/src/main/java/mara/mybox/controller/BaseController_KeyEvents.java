package mara.mybox.controller;

import javafx.event.EventTarget;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.AppVariables;

/**
 * @Author Mara
 * @CreateDate 2021-7-27
 * @License Apache License Version 2.0
 */
public abstract class BaseController_KeyEvents extends BaseController_Actions {

    private KeyEvent keyEvent;

    // Flter from top level. Always handle at higher level at first.
    public void monitorKeyEvents() {
        try {
            if (thisPane != null) {
                thisPane.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                    MyBoxLog.debug("KeyEvent.KEY_PRESSED");
                    if (keyEventsFilter(event)) {
                        event.consume();
                    }
                });
                thisPane.addEventFilter(KeyEvent.KEY_TYPED, event -> {
                    MyBoxLog.debug("KeyEvent.KEY_TYPED");
                    if (keyEventsFilter(event)) {
                        event.consume();
                    }
                });
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    // return whether handled
    public boolean keyEventsFilter(KeyEvent event) {
        try {
//            if (getMyWindow() != null) {
//                MyBoxLog.debug("window:" + getMyWindow().getClass() + "   isFocused:" + getMyWindow().isFocused());
//            }
            keyEvent = event;
            MyBoxLog.debug("filter:" + this.getClass()
                    + " text:" + event.getText() + " code:" + event.getCode() + " char:" + event.getCharacter()
                    + " source:" + event.getSource().getClass() + " target:" + (event.getTarget() == null ? "null" : event.getTarget()));

            if (event.isAltDown()) {
                return altFilter(event);

            } else {
                return keyFilter(event);

            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return false;
    }

    private boolean altFilter(KeyEvent event) {
        if (!event.isAltDown() || event.getCode() == null) {
            return false;
        }
        switch (event.getCode()) {
            case HOME:
                return altHome();
            case END:
                return altEnd();
            case PAGE_UP:
                return altPageUp();
            case PAGE_DOWN:
                return altPageDown();
        }
        return keyFilter(event);
    }

    private boolean keyFilter(KeyEvent event) {
        KeyCode code = event.getCode();
        if (code == null || code == KeyCode.UNDEFINED) {
            return inputFilter(event);
        }
        switch (code) {
            case ENTER:
                return keyEnter();

            case DELETE:
                return keyDelete();

            case HOME:
                return keyHome();

            case END:
                return keyEnd();

            case PAGE_UP:
                return keyPageUp();

            case PAGE_DOWN:
                return keyPageDown();

            case F1:
                return keyF1();

            case F2:
                return keyF2();

            case F3:
                return keyF3();

            case F4:
                return keyF4();

            case F5:
                return keyF5();

            case F6:
                return keyF6();

            case F7:
                return keyF7();

            case F8:
                return keyF8();

            case F9:
                return keyF9();

            case F10:
                return keyF10();

            case F11:
                return keyF11();

            case F12:
                return keyF12();

            case ESCAPE:
                return keyESC();

        }

        return inputFilter(event);
    }

    public boolean inputFilter(KeyEvent event) {
        if (event == null) {
            return false;
        }
        boolean omit = !event.isControlDown() && !event.isAltDown();
        if (omit && targetIsTextInput()) {
            return false;
        }
        return inputFilter(omit ? event.getCharacter() : event.getText(), omit);
    }

    public boolean inputFilter(String input, boolean omit) {
        if (input == null || (omit && AppVariables.ShortcutsCanNotOmitCtrlAlt)) {
            return false;
        }
//        MyBoxLog.debug("input:" + input.toUpperCase());
        switch (input.toUpperCase()) {
            case "E":
                return controlAltE();

            case "N":
                return controlAltN();

            case "C":
                return controlAltC();

            case "V":
                return controlAltV();

            case "A":
                return controlAltA();

            case "D":
                return omit ? false : controlAltD();

            case "Z":
                return controlAltZ();

            case "Y":
                return controlAltY();

            case "O":
                return controlAltO();

            case "X":
                return controlAltX();

            case "R":
                return controlAltR();

            case "S":
                return omit ? false : controlAltS();

            case "F":
                return controlAltF();

            case "H":
                return controlAltH();

            case "T":
                return controlAltT();

            case "G":
                return controlAltG();

            case "B":
                return controlAltB();

            case "I":
                return controlAltI();

            case "P":
                return controlAltP();

            case "W":
                return controlAltW();

            case "M":
                return controlAltM();

            case "J":
                return controlAltJ();

            case "Q":
                return controlAltQ();

            case "K":
                return controlAltK();

            case "U":
                return controlAltU();

            case "L":
                return omit ? false : controlAltL();

            case "-":
                setSceneFontSize(AppVariables.sceneFontSize - 1);
                return true;

            case "=":
                setSceneFontSize(AppVariables.sceneFontSize + 1);
                return true;

            case "0":
                return controlAlt0();

            case "1":
                return controlAlt1();

            case "2":
                return controlAlt2();

            case "3":
                return controlAlt3();

            case "4":
                return controlAlt4();

            case "5":
                return controlAlt5();

            case "6":
                return controlAlt6();

            case "7":
                return controlAlt7();

            case "8":
                return controlAlt8();

            case "9":
                return controlAlt9();

        }
        return false;
    }

    // Shortcuts like PageDown/PageUp/Home/End/Ctrl-c/v/x/z/y/a should work for text editing preferentially
    public boolean targetIsTextInput() {
        if (keyEvent == null || keyEvent.getTarget() == null) {
            return false;
        }
        String t = keyEvent.getTarget().toString();
//        MyBoxLog.console(this.getClass() + "  " + keyEvent.getCode() + "  " + t);
        if (t.contains("TextField") || t.contains("ComboBox")
                || t.contains("TextArea") || t.contains("WebView")) {
            return true;
        }
        // When popup is shown, event target is always popup pane even when focus is actually in text input
        return NodeTools.textInputFocus(getMyScene()) != null;
    }

    public boolean altPageUp() {
        if (previousButton != null && !previousButton.isDisabled() && previousButton.isVisible()) {
            previousAction();
            return true;
        } else if (pagePreviousButton != null && !pagePreviousButton.isDisabled() && pagePreviousButton.isVisible()) {
            pagePreviousAction();
            return true;
        }
        return false;
    }

    public boolean altPageDown() {
        if (nextButton != null && !nextButton.isDisabled() && nextButton.isVisible()) {
            nextAction();
        } else if (pageNextButton != null && !pageNextButton.isDisabled() && pageNextButton.isVisible()) {
            pageNextAction();
        }
        return false;
    }

    public boolean altHome() {
        if (firstButton != null && !firstButton.isDisabled() && firstButton.isVisible()) {
            firstAction();
            return true;
        } else if (pageFirstButton != null && !pageFirstButton.isDisabled() && pageFirstButton.isVisible()) {
            pageFirstAction();
            return true;
        }
        return false;
    }

    public boolean altEnd() {
        if (lastButton != null && !lastButton.isDisabled() && lastButton.isVisible()) {
            lastAction();
            return true;
        } else if (pageLastButton != null && !pageLastButton.isDisabled() && pageLastButton.isVisible()) {
            pageLastAction();
            return true;
        }
        return false;
    }

    public boolean controlAltC() {
        if (targetIsTextInput()) {
            return false;
        }
        if (copyButton != null) {
            if (!copyButton.isDisabled() && copyButton.isVisible()) {
                copyAction();
            }
            return true;
        } else if (copyToSystemClipboardButton != null) {
            if (!copyToSystemClipboardButton.isDisabled() && copyToSystemClipboardButton.isVisible()) {
                copyToSystemClipboard();
            }
            return true;
        } else if (copyToMyBoxClipboardButton != null) {
            if (!copyToMyBoxClipboardButton.isDisabled() && copyToMyBoxClipboardButton.isVisible()) {
                copyToMyBoxClipboard();
            }
            return true;
        }
        return false;
    }

    public boolean controlAltV() {
        if (targetIsTextInput()) {
            return false;
        }
        if (pasteButton != null) {
            if (!pasteButton.isDisabled() && pasteButton.isVisible()) {
                pasteAction();
            }
            return true;
        } else if (pasteContentInSystemClipboardButton != null) {
            if (!pasteContentInSystemClipboardButton.isDisabled() && pasteContentInSystemClipboardButton.isVisible()) {
                pasteContentInSystemClipboard();
            }
            return true;
        } else if (loadContentInSystemClipboardButton != null) {
            if (!loadContentInSystemClipboardButton.isDisabled() && loadContentInSystemClipboardButton.isVisible()) {
                loadContentInSystemClipboard();
            }
            return true;
        }
        return false;
    }

    public boolean controlAltA() {
        if (targetIsTextInput()) {
            return false;
        }
        if (allButton != null) {
            if (!allButton.isDisabled() && allButton.isVisible()) {
                allAction();
            }
            return true;
        } else if (selectAllButton != null) {
            if (!selectAllButton.isDisabled() && selectAllButton.isVisible()) {
                selectAllAction();
            }
            return true;
        }
        return false;
    }

    public boolean controlAltE() {
        if (startButton != null && !startButton.isDisabled() && startButton.isVisible()) {
            startAction();
            return true;
        } else if (okButton != null && !okButton.isDisabled() && okButton.isVisible()) {
            okAction();
            return true;
        } else if (playButton != null && !playButton.isDisabled() && playButton.isVisible()) {
            playAction();
            return true;
        }
        return false;
    }

    public boolean controlAltL() {
        if (clearButton != null && !clearButton.isDisabled() && clearButton.isVisible()) {
            clearAction();
            return true;
        }
        return false;
    }

    public boolean controlAltN() {
        if (createButton != null) {
            if (!createButton.isDisabled() && createButton.isVisible()) {
                createAction();
            }
            return true;
        } else if (addButton != null) {
            if (!addButton.isDisabled() && addButton.isVisible()) {
                addAction();
            }
            return true;
        } else if (addRowsButton != null) {
            if (!addRowsButton.isDisabled() && addRowsButton.isVisible()) {
                addRowsAction();
            }
            return true;
        }
        return false;
    }

    public boolean controlAltS() {
        if (saveButton != null) {
            if (!saveButton.isDisabled() && saveButton.isVisible()) {
                saveAction();
            }
            return true;
        }
        return false;
    }

    public boolean controlAltB() {
        if (saveAsButton != null) {
            if (!saveAsButton.isDisabled() && saveAsButton.isVisible()) {
                saveAsAction();
            }
            return true;
        }
        return false;
    }

    public boolean controlAltI() {
        if (infoButton != null) {
            if (!infoButton.isDisabled() && infoButton.isVisible()) {
                infoAction();
            }
            return true;
        }
        return false;
    }

    public boolean controlAltD() {
        if (targetIsTextInput()) {
            return false;
        }
        if (deleteButton != null) {
            if (!deleteButton.isDisabled() && deleteButton.isVisible()) {
                deleteAction();
            }
            return true;
        } else if (deleteRowsButton != null) {
            if (!deleteRowsButton.isDisabled() && deleteRowsButton.isVisible()) {
                deleteRowsAction();
            }
            return true;
        }
        return false;
    }

    public boolean controlAltO() {
        if (selectNoneButton != null) {
            if (!selectNoneButton.isDisabled() && selectNoneButton.isVisible()) {
                selectNoneAction();
            }
            return true;
        }
        return false;
    }

    public boolean controlAltX() {
        if (targetIsTextInput()) {
            return false;
        }
        if (cropButton != null) {
            if (!cropButton.isDisabled() && cropButton.isVisible()) {
                cropAction();
            }
            return true;
        }
        return false;
    }

    public boolean controlAltG() {
        if (goButton != null && !goButton.isDisabled() && goButton.isVisible()) {
            goAction();
            return true;
        }
        return false;
    }

    public boolean controlAltR() {
        if (recoverButton != null) {
            if (!recoverButton.isDisabled() && recoverButton.isVisible()) {
                recoverAction();
            }
            return true;
        }
        return false;
    }

    public boolean controlAltZ() {
        if (targetIsTextInput()) {
            return false;
        }
        if (undoButton != null) {
            if (!undoButton.isDisabled() && undoButton.isVisible()) {
                undoAction();
            }
            return true;
        }
        return false;
    }

    public boolean controlAltY() {
        if (targetIsTextInput()) {
            return false;
        }
        if (redoButton != null) {
            if (!redoButton.isDisabled() && redoButton.isVisible()) {
                redoAction();
            }
            return true;
        }
        return false;
    }

    public boolean controlAltF() {
        findAction();
        return true;
    }

    public boolean controlAltH() {
        replaceAction();
        return true;
    }

    public boolean controlAltT() {
        if (selectButton != null) {
            selectAction();
            return true;
        }
        return false;
    }

    public boolean controlAltP() {
        return popAction();
    }

    public boolean controlAltW() {
        if (withdrawButton != null) {
            if (!withdrawButton.isDisabled() && withdrawButton.isVisible()) {
                return withdrawAction();
            }
        }
        return false;
    }

    public boolean controlAltM() {
        EventTarget target = keyEvent.getTarget();
        if (target != null) {
            if (target instanceof TextInputControl) {
                TextClipboardPopController.open(myController, (TextInputControl) target);
                return true;
            }
            if (target instanceof ComboBox) {
                ComboBox cb = (ComboBox) target;
                if (cb.isEditable()) {
                    TextClipboardPopController.open(myController, cb);
                    return true;
                }
            }
        }
        myBoxClipBoard();
        return true;
    }

    public boolean controlAltJ() {
        systemClipBoard();
        return true;
    }

    public boolean controlAltQ() {
        return false;
    }

    public boolean controlAltK() {
        return false;
    }

    public boolean controlAltU() {
        if (previewButton != null) {
            if (!previewButton.isDisabled() && previewButton.isVisible()) {
                previewAction();
            }
            return true;
        }
        return false;
    }

    public boolean controlAlt0() {
        if (isPopup()) {
            return false;
        }
        myStage = getMyStage();
        if (myStage != null && myStage.isShowing()) {
            setAlwaysTop(!myStage.isAlwaysOnTop(), true);
            return true;
        }
        return false;
    }

    public boolean controlAlt1() {
        return false;
    }

    public boolean controlAlt2() {
        return false;
    }

    public boolean controlAlt3() {
        return false;
    }

    public boolean controlAlt4() {
        return false;
    }

    public boolean controlAlt5() {
        return false;
    }

    public boolean controlAlt6() {
        return false;
    }

    public boolean controlAlt7() {
        return false;
    }

    public boolean controlAlt8() {
        return false;
    }

    public boolean controlAlt9() {
        if (thisPane != null) {
            ImageEditorController.openImage(NodeTools.snap(thisPane));
            return true;
        }
        return false;
    }

    public boolean keyEnter() {
        return false;
    }

    public boolean keyHome() {
        if (targetIsTextInput()) {
            return false;
        }
        return altHome();
    }

    public boolean keyEnd() {
        if (targetIsTextInput()) {
            return false;
        }
        return altEnd();
    }

    public boolean keyPageUp() {
        if (targetIsTextInput()) {
            return false;
        }
        return altPageUp();
    }

    public boolean keyPageDown() {
        if (targetIsTextInput()) {
            return false;
        }
        return altPageDown();
    }

    public boolean keyDelete() {
        return controlAltD();
    }

    public boolean keyF1() {
        return controlAltE();
    }

    public boolean keyF2() {
        return controlAltG();
    }

    public boolean keyF3() {
        return controlAltU();
    }

    public boolean keyF4() {
        return controlAltP();
    }

    public boolean keyF5() {
        return controlAltB();
    }

    public boolean keyF6() {
        return menuAction();
    }

    public boolean keyF7() {
        operationsAction();
        return true;
    }

    public boolean keyF8() {
        mybox();
        return true;
    }

    public boolean keyF9() {
        popTips();
        return true;
    }

    public boolean keyF10() {
        return synchronizeAction();
    }

    public boolean keyF11() {
        if (leftPaneControl != null) {
            controlLeftPane();
            return true;

        } else if (leftPaneCheck != null) {
            leftPaneCheck.setSelected(!leftPaneCheck.isSelected());
            return true;
        }
        return false;
    }

    public boolean keyF12() {
        if (rightPaneControl != null) {
            controlRightPane();
            return true;

        } else if (rightPaneCheck != null) {
            rightPaneCheck.setSelected(!rightPaneCheck.isSelected());
            return true;
        }
        return false;
    }

    public boolean keyESC() {
        if (cancelButton != null && !cancelButton.isDisabled() && cancelButton.isVisible()) {
            cancelAction();
        }
        WindowTools.closeAllPopup();
        MenuController.closeAll();
        return true;
    }

}
