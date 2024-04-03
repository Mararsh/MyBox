package mara.mybox.controller;

import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;
import org.w3c.dom.Element;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

/**
 * @Author Mara
 * @CreateDate 2022-4-20
 * @License Apache License Version 2.0
 */
public abstract class BaseData2DChartHtmlController extends BaseData2DChartController {

    protected int barWidth = 100, categorysCol;
    protected EventListener clickListener;
    protected boolean randomColor;
    protected List<Integer> otherIndices;

    @FXML
    protected ToggleGroup compareGroup;
    @FXML
    protected RadioButton absoluateRadio, minMaxRadio;
    @FXML
    protected ComboBox<String> widthSelector;
    @FXML
    protected ControlWebView webViewController;

    @Override
    public void initOptions() {
        try {
            super.initOptions();

            webViewController.setParent(this);

            clickListener = new EventListener() {
                @Override
                public void handleEvent(Event ev) {
                    try {
                        Element element = (Element) ev.getTarget();
                        if (!"INPUT".equalsIgnoreCase(element.getTagName())) {
                            return;
                        }
                        String id = element.getAttribute("id");
                        if (id == null) {
                            return;
                        }
                        boolean check = (boolean) webViewController.executeScript("document.getElementById('" + id + "').checked;");
                        switch (id) {
                            case "RowNumberCheck":
                                UserConfig.setBoolean(baseName + "ShowRowNumber", check);
                                break;
                            case "DataValueCheck":
                                UserConfig.setBoolean(baseName + "ShowValue", check);
                                break;
                            case "PercentageCheck":
                                UserConfig.setBoolean(baseName + "ShowPercentage", check);
                                break;
                            case "CategoryCheck":
                                UserConfig.setBoolean(baseName + "ShowCategory", check);
                                break;
                            case "CalculatedCheck":
                                UserConfig.setBoolean(baseName + "ShowCalculatedValues", check);
                                break;
                            case "OthersCheck":
                                UserConfig.setBoolean(baseName + "ShowOthers", check);
                                break;
                        }
                    } catch (Exception e) {
                        MyBoxLog.console(e.toString());
                    }
                }
            };
            webViewController.pageLoadedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    pageLoaded();
                }
            });

            barWidth = UserConfig.getInt(baseName + "Width", 150);
            if (barWidth < 0) {
                barWidth = 100;
            }
            widthSelector.getItems().addAll(
                    Arrays.asList("150", "100", "200", "50", "80", "120", "300")
            );
            widthSelector.setValue(barWidth + "");
            widthSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(widthSelector.getValue());
                        if (v > 0) {
                            barWidth = v;
                            UserConfig.setInt(baseName + "Width", v);
                            widthSelector.getEditor().setStyle(null);
                            okAction();
                        } else {
                            widthSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
                        widthSelector.getEditor().setStyle(UserConfig.badStyle());
                    }
                }
            });

            if (UserConfig.getBoolean(baseName + "Absoluate", true)) {
                absoluateRadio.setSelected(true);
            } else {
                minMaxRadio.setSelected(true);
            }
            compareGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    UserConfig.setBoolean(baseName + "Absoluate", absoluateRadio.isSelected());
                    okAction();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean showRowNumber() {
        return true;
    }

    @Override
    public boolean checkOptions() {
        if (!super.checkOptions()) {
            return false;
        }
        categorysCol = -1;
        if (data2D != null) {
            categorysCol = data2D.colOrder(selectedCategory);
        }
        randomColor = false;
        return true;
    }

    @Override
    public void outputData() {
        if (task != null && !task.isQuit()) {
            return;
        }
        task = new FxSingletonTask<Void>(this) {

            private String html;

            @Override
            protected boolean handle() {
                html = makeHtml();
                return html != null;
            }

            @Override
            protected void whenSucceeded() {
                outputHtml(html);
            }

        };
        start(task);
    }

    @Override
    public void drawChart() {
        outputData();
    }

    protected String makeHtml() {
        return null;
    }

    protected void outputHtml(String html) {
        webViewController.loadContents(html);
    }

    protected String jsBody() {
        boolean showRowNumber = UserConfig.getBoolean(baseName + "ShowRowNumber", false);
        boolean ShowValue = UserConfig.getBoolean(baseName + "ShowValue", false);
        boolean ShowPercentage = UserConfig.getBoolean(baseName + "ShowPercentage", true);
        boolean ShowCategory = UserConfig.getBoolean(baseName + "ShowCategory", true);
        boolean ShowOthers = UserConfig.getBoolean(baseName + "ShowOthers", true);
        boolean ShowCalculatedValues = UserConfig.getBoolean(baseName + "ShowCalculatedValues", true);
        StringBuilder s = new StringBuilder();
        s.append("<BODY onload='initChecks();'>\n");
        s.append(" <script>\n"
                + "    function initChecks() {\n"
                + "      showClass('RowNumber', " + showRowNumber + ");  \n"
                + "      showClass('DataValue', " + ShowValue + ");  \n"
                + "      showClass('Percentage', " + ShowPercentage + ");  \n"
                + "      showClass('Category', " + ShowCategory + ");  \n"
                + "      showClass('Others', " + ShowOthers + ");  \n"
                + "      showClass('Calculated', " + ShowCalculatedValues + ");  \n"
                + "    }\n"
                + "    function showClass(className, show) {\n"
                + "      var nodes = document.getElementsByClassName(className);  　\n"
                + "      if ( show ) {\n"
                + "           for (var i = 0 ; i < nodes.length; i++) {\n"
                + "              nodes[i].style.display = '';\n"
                + "           }\n"
                + "       } else {\n"
                + "           for (var i = 0 ; i < nodes.length; i++) {\n"
                + "              nodes[i].style.display = 'none';\n"
                + "           }\n"
                + "       }\n"
                + "    }\n"
                + "  </script>\n\n");
        s.append("<DIV>\n");
        s.append("    <INPUT id=\"RowNumberCheck\" type=\"checkbox\" ")
                .append(showRowNumber ? "checked" : "")
                .append(" onclick=\"showClass('RowNumber', this.checked);\" />")
                .append(message("RowNumber")).append("\n");

        s.append("    <INPUT id=\"DataValueCheck\"  type=\"checkbox\"")
                .append(ShowValue ? "checked" : "")
                .append(" onclick=\"showClass('DataValue', this.checked);\" />")
                .append(message("Value")).append("\n");

        s.append("    <INPUT id=\"PercentageCheck\"  type=\"checkbox\" ")
                .append(ShowPercentage ? "checked" : "")
                .append(" onclick=\"showClass('Percentage', this.checked);\" />")
                .append(message("Percentage")).append("\n");

        if (categoryColumnSelector != null) {
            s.append("    <INPUT id=\"CategoryCheck\"  type=\"checkbox\" ")
                    .append(ShowCategory ? "checked" : "")
                    .append(" onclick=\"showClass('Category', this.checked);\" />")
                    .append(message("Category")).append("\n");
        }
        if (otherColumnsPane != null) {
            s.append("    <INPUT id=\"OthersCheck\"  type=\"checkbox\" ")
                    .append(ShowOthers ? "checked" : "")
                    .append(" onclick=\"showClass('Others', this.checked);\" />")
                    .append(message("Others")).append("\n");
        }

        s.append("    <INPUT id=\"CalculatedCheck\"  type=\"checkbox\" ")
                .append(ShowCalculatedValues ? "checked" : "")
                .append(" onclick=\"showClass('Calculated', this.checked);\" />")
                .append(message("CalculatedValues")).append("\n");
        s.append("</DIV>\n").append("<HR/>\n");
        return s.toString();
    }

    protected String jsComments() {
        return "\n<HR/>\n<P align=left style=\"font-size:0.8em;\">* "
                + message("HtmlEditableComments") + "</P>\n";
    }

    @FXML
    public void randomColors() {
        randomColor = true;
        outputData();
    }

    public void pageLoaded() {
        Element docNode = webViewController.webEngine.getDocument().getDocumentElement();
        EventTarget t = (EventTarget) docNode;
        t.removeEventListener("click", clickListener, false);
        t.addEventListener("click", clickListener, false);
    }

    @FXML
    public void dataAction() {
        if (outputData == null || outputData.isEmpty()) {
            popError(message("NoData"));
            return;
        }
        Data2DManufactureController.openData(baseTitle, outputColumns, outputData);
    }

    @FXML
    public void editAction() {
        webViewController.editAction();
    }

    @FXML
    @Override
    public void popFunctionsMenu(javafx.event.Event event) {
        if (UserConfig.getBoolean("WebviewFunctionsPopWhenMouseHovering", true)) {
            showFunctionsMenu(event);
        }
    }

    @FXML
    @Override
    public void showFunctionsMenu(javafx.event.Event event) {
        webViewController.showFunctionsMenu(event);
    }

    @Override
    public void cleanPane() {
        clickListener = null;
        super.cleanPane();
    }

}
