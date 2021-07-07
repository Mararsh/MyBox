package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.IndexRange;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.Clipboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Window;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.fxml.FxmlWindow;
import mara.mybox.tools.HtmlTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;
import org.w3c.dom.Element;

/**
 * @Author Mara
 * @CreateDate 2021-6-27
 * @License Apache License Version 2.0
 */
public class PopNodesController extends BaseController {

//    protected Popup window;
    protected Node owner;
    protected double x, y;
    protected TextInputControl textInput;
    protected WebView webView;
    protected BaseWebViewController webViewController;
    protected Clipboard clipboard;
    protected Button copyMyBoxButton, copyTextMyBoxButton, copyHtmlMyBoxButton, copyTextClipButton, copyHtmlClipButton;

    @FXML
    protected VBox nodesBox;
    @FXML
    protected Label titleLabel;
    @FXML
    protected Button functionsButton;

    public PopNodesController() {
        baseTitle = AppVariables.message("Value");

    }

    @Override
    public void initControls() {
        try {
            parentController = this;
            if (functionsButton != null) {
                functionsButton.setVisible(false);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setParameters(BaseController parent) {
        try {
            this.parentController = parent;
            setStyle();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setStyle() {
        setStyle(thisPane);
    }

    public void setStyle(Parent pane) {
        try {
            String style = AppVariables.getUserConfigValue("PopWindowStyle", "; -fx-text-fill: black; -fx-background-color: #ececec;");
            if (!style.isBlank()) {
                pane.setStyle((pane.getStyle() != null ? pane.getStyle() : "") + style);
                FxmlControl.addLabelStyle(pane, style);
            }
            FxmlControl.refreshStyle(pane);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void addNode(Node node) {
        nodesBox.getChildren().add(node);
    }

    public void addFlowPane(List<Node> nodes) {
        try {

            FlowPane flowPane = new FlowPane();
            flowPane.setMinHeight(Region.USE_PREF_SIZE);
            flowPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            flowPane.setVgap(5);
            flowPane.setHgap(5);
            if (nodes != null) {
                flowPane.getChildren().setAll(nodes);
            }

            addNode(flowPane);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void addEditPane(TextInputControl textArea) {
        try {
            this.textInput = textArea;
            if (textArea == null) {
                return;
            }

            List<Node> editNodes = new ArrayList<>();
            undoButton = new Button(message("Undo"));
            undoButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    textArea.undo();
                    checkEditPane();
                }
            });
            editNodes.add(undoButton);

            redoButton = new Button(message("Redo"));
            redoButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    textArea.redo();
                    checkEditPane();
                }
            });
            editNodes.add(redoButton);

            cropButton = new Button(message("Crop"));
            cropButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    textArea.cut();
                    checkEditPane();
                }
            });
            editNodes.add(cropButton);

            deleteButton = new Button(message("Delete"));
            deleteButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    textArea.deleteText(textArea.getSelection());
                    checkEditPane();
                }
            });
            editNodes.add(deleteButton);

            clearButton = new Button(message("Clear"));
            clearButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    textArea.clear();
                    checkEditPane();
                }
            });
            editNodes.add(clearButton);

            selectAllButton = new Button(message("SelectAll"));
            selectAllButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    textArea.selectAll();
                    checkEditPane();
                }
            });
            editNodes.add(selectAllButton);

            selectNoneButton = new Button(message("SelectNone"));
            selectNoneButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    textArea.deselect();
                    checkEditPane();
                }
            });
            editNodes.add(selectNoneButton);

            addFlowPane(editNodes);

            List<Node> copyNodes = new ArrayList<>();

            copyButton = new Button(AppVariables.getUserConfigBoolean("MonitorTextClipboard", true)
                    ? message("CopyToClipboards") : message("CopyToSystemClipboard"));
            copyButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    copyToSystemClipboard(textArea.getSelectedText());
                    checkEditPane();
                }
            });
            copyNodes.add(copyButton);

            copyMyBoxButton = new Button(message("CopyToMyBoxClipboard"));
            copyMyBoxButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    copyToMyBoxClipboard(textArea.getSelectedText());
                }
            });
            copyNodes.add(copyMyBoxButton);
            addFlowPane(copyNodes);

            List<Node> pasteNodes = new ArrayList<>();
            pasteButton = new Button(message("PasteContentInSystemClipboard"));
            pasteButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    textArea.paste();
                    checkEditPane();
                }
            });
            pasteNodes.add(pasteButton);

            Button myboxButton = new Button(message("TextInMyBoxClipboard"));
            myboxButton.setOnMouseEntered(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    try {
                        Window window = getWindow();
                        popup = FxmlWindow.popWindow(parentController, CommonValues.PopTextsClipboardFxml,
                                (Node) event.getSource(), window.getX(), window.getY());
                        if (popup == null) {
                            return;
                        }
                        Object object = popup.getUserData();
                        if (object != null && object instanceof PopTextsClipboardController) {
                            ((PopTextsClipboardController) object).setParameters(parentController, textArea);
                        }
                    } catch (Exception e) {
                        MyBoxLog.error(e.toString());
                    }
                }
            });
            pasteNodes.add(myboxButton);

            addFlowPane(pasteNodes);

            checkEditPane();

            textArea.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkEditPane();
                }
            });

            textArea.selectionProperty().addListener(new ChangeListener<IndexRange>() {
                @Override
                public void changed(ObservableValue ov, IndexRange oldValue, IndexRange newValue) {
                    checkEditPane();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void checkEditPane() {
        if (textInput == null) {
            return;
        }
        if (undoButton != null) {
            undoButton.setDisable(!textInput.isEditable() || textInput.isDisable() || !textInput.isUndoable());
        }
        if (redoButton != null) {
            redoButton.setDisable(!textInput.isEditable() || textInput.isDisable() || !textInput.isRedoable());
        }
        boolean selectNone = textInput.getSelection().getLength() < 1;
        if (cropButton != null) {
            cropButton.setDisable(!textInput.isEditable() || textInput.isDisable() || selectNone);
        }
        if (deleteButton != null) {
            deleteButton.setDisable(!textInput.isEditable() || textInput.isDisable() || selectNone);
        }
        if (clearButton != null) {
            clearButton.setDisable(!textInput.isEditable() || textInput.isDisable());
        }
        if (copyButton != null) {
            copyButton.setDisable(selectNone);
        }
        if (copyMyBoxButton != null) {
            copyMyBoxButton.setDisable(selectNone);
        }
        if (pasteButton != null) {
            if (clipboard == null) {
                clipboard = Clipboard.getSystemClipboard();
            }
            pasteButton.setDisable(!textInput.isEditable() || textInput.isDisable() || !clipboard.hasString());
        }
        boolean empty = textInput.getLength() < 1;
        if (selectAllButton != null) {
            selectAllButton.setDisable(empty);
        }
        if (selectNoneButton != null) {
            selectNoneButton.setDisable(empty);
        }
    }

    public void addWebviewPane(BaseWebViewController webViewController, Element element) {
        try {
            if (webViewController == null) {
                return;
            }
            this.webViewController = webViewController;
            this.webView = webViewController.webView;
            if (webView == null) {
                return;
            }
            String tag;
            if (element != null) {
                tag = element.getTagName();
            } else {
                tag = null;
            }
            titleLabel.setText(tag);

            functionsButton.setVisible(true);

            List<Node> editNodes = new ArrayList<>();

            if (element != null) {
                Button selectNodeButton = new Button(message("SelectNode"));
                selectNodeButton.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        String id = element.getAttribute("id");
                        String newid = new Date().getTime() + "";
                        element.setAttribute("id", newid);
                        HtmlTools.selectNode(webView.getEngine(), newid);
                        if (id != null) {
                            element.setAttribute("id", id);
                        } else {
                            element.removeAttribute("id");
                        }
                        checkWebviewPane();
                    }
                });
                editNodes.add(selectNodeButton);
            }

            selectAllButton = new Button(message("SelectAll"));
            selectAllButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    HtmlTools.selectAll(webView.getEngine());
                    checkWebviewPane();
                }
            });
            editNodes.add(selectAllButton);

            selectNoneButton = new Button(message("SelectNone"));
            selectNoneButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    HtmlTools.selectNone(webView.getEngine());
                    checkEditPane();
                }
            });
            editNodes.add(selectNoneButton);

            addFlowPane(editNodes);

            List<Node> copyTextNodes = new ArrayList<>();
            copyTextNodes.add(new Label(message("Text")));
            copyTextClipButton = new Button(AppVariables.getUserConfigBoolean("MonitorTextClipboard", true)
                    ? message("CopyToClipboards") : message("CopyToSystemClipboard"));
            copyTextClipButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    String text = HtmlTools.selectedText(webView.getEngine());
                    if (text == null || text.isEmpty()) {
                        popError(message("SelectedNone"));
                        return;
                    }
                    copyToSystemClipboard(text);
                    checkWebviewPane();
                }
            });
            copyTextNodes.add(copyTextClipButton);

            copyTextMyBoxButton = new Button(message("CopyToMyBoxClipboard"));
            copyTextMyBoxButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    String text = HtmlTools.selectedText(webView.getEngine());
                    if (text == null || text.isEmpty()) {
                        popError(message("SelectedNone"));
                        return;
                    }
                    copyToMyBoxClipboard(text);
                    checkWebviewPane();
                }
            });
            copyTextNodes.add(copyTextMyBoxButton);
            addFlowPane(copyTextNodes);

            List<Node> copyHtmlNodes = new ArrayList<>();
            copyHtmlNodes.add(new Label("Html"));
            copyHtmlClipButton = new Button(AppVariables.getUserConfigBoolean("MonitorTextClipboard", true)
                    ? message("CopyToClipboards") : message("CopyToSystemClipboard"));
            copyHtmlClipButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    String html = HtmlTools.selectedHtml(webView.getEngine());
                    if (html == null || html.isEmpty()) {
                        popError(message("SelectedNone"));
                        return;
                    }
                    copyToSystemClipboard(html);
                    checkWebviewPane();
                }
            });
            copyHtmlNodes.add(copyHtmlClipButton);

            copyHtmlMyBoxButton = new Button(message("CopyToMyBoxClipboard"));
            copyHtmlMyBoxButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    String html = HtmlTools.selectedHtml(webView.getEngine());
                    if (html == null || html.isEmpty()) {
                        popError(message("SelectedNone"));
                        return;
                    }
                    copyToMyBoxClipboard(html);
                    checkWebviewPane();
                }
            });
            copyHtmlNodes.add(copyHtmlMyBoxButton);
            addFlowPane(copyHtmlNodes);

            setParameters(webViewController.getParentController());

            checkWebviewPane();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void checkWebviewPane() {
        if (webView == null) {
            return;
        }
//        String selected = HtmlTools.selectedHtml(webView.getEngine());
//        boolean selectNone = selected == null || selected.isBlank();
//        if (copyHtmlClipButton != null) {
//            copyHtmlClipButton.setDisable(selectNone);
//        }
//        if (copyHtmlMyBoxButton != null) {
//            copyHtmlMyBoxButton.setDisable(selectNone);
//        }
//        if (copyTextMyBoxButton != null) {
//            copyTextMyBoxButton.setDisable(selectNone);
//        }
//        if (copyTextClipButton != null) {
//            copyTextClipButton.setDisable(selectNone);
//        }
    }

    @FXML
    public void popFunctionsMenu(MouseEvent mouseEvent) {
        if (webViewController == null) {
            return;
        }
        webViewController.popFunctionsMenu(mouseEvent);
    }

    public void setFlowPane(BaseController parent, List<Node> nodes) {
        addFlowPane(nodes);
        setParameters(parent);
    }

    @FXML
    public void popStyles(MouseEvent mouseEvent) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            MenuItem menu;
            Map<String, String> styles = new LinkedHashMap<>();
            styles.put("Default", "; -fx-text-fill: black; -fx-background-color: #ececec;");
            styles.put("Transparent", "; -fx-text-fill: black; -fx-background-color: transparent;");
            styles.put("Console", "; -fx-text-fill: #CCFF99; -fx-background-color: black;");
            styles.put("Blackboard", "; -fx-text-fill: white; -fx-background-color: #336633;");
            styles.put("Ago", "; -fx-text-fill: white; -fx-background-color: darkblue;");
            styles.put("Book", "; -fx-text-fill: black; -fx-background-color: #F6F1EB;");
            for (String style : styles.keySet()) {
                menu = new MenuItem(message(style));
                menu.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        AppVariables.setUserConfigValue("PopWindowStyle", styles.get(style));
                        setStyle();
                    }
                });
                popMenu.getItems().add(menu);
            }

            popMenu.getItems().add(new SeparatorMenuItem());
            menu = new MenuItem(message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    popMenu.hide();
                }
            });
            popMenu.getItems().add(menu);

            FxmlControl.locateCenter((Region) mouseEvent.getSource(), popMenu);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void closeAction() {
        thisPane.getScene().getWindow().hide();
    }

    @FXML
    @Override
    public void cancelAction() {
        closeAction();
    }

    @FXML
    @Override
    public void closePopup() {
        closeAction();
    }
}
