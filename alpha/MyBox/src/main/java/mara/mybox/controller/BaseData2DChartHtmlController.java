package mara.mybox.controller;

import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
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

    @FXML
    protected CheckBox absoluateCheck;
    @FXML
    protected ComboBox<String> widthSelector;

    @Override
    public void initControls() {
        try {
            super.initControls();

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

            absoluateCheck.setSelected(UserConfig.getBoolean(baseName + "Absoluate", true));
            absoluateCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "Absoluate", absoluateCheck.isSelected());
                    okAction();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void setParameters(ControlData2DEditTable editController) {
        try {
            super.setParameters(editController);
            okAction();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void objectChanged() {
        super.objectChanged();
        okAction();
    }

    @Override
    public void rowNumberCheckChanged() {
        super.rowNumberCheckChanged();
        okAction();
    }

    @Override
    public boolean checkOptions() {
        if (isSettingValues) {
            return true;
        }
        boolean ok = super.checkOptions();
        categorysCol = data2D.colOrder(selectedCategory);
        return ok;
    }

    @FXML
    @Override
    public void okAction() {
        if (!checkOptions() || !initData()) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonTask<Void>(this) {

            private String html;

            @Override
            protected boolean handle() {
                try {
                    data2D.setTask(task);
                    readData();
                    html = handleData();
                    return html != null;
                } catch (Exception e) {
                    MyBoxLog.error(e);
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                outputHtml(html);
            }

        };
        start(task);
    }

    protected String handleData() {
        return null;
    }

    protected void outputHtml(String html) {
        webViewController.loadContents(html);
    }

    protected String jsBody() {
        StringBuilder s = new StringBuilder();
        s.append("<BODY onload='initChecks();'>\n");
        s.append(" <script>\n"
                + "    function initChecks() {\n"
                + "      showClass('RowNumber', document.getElementById('RowNumberCheck').checked);  \n"
                + "      showClass('DataValue', document.getElementById('DataValueCheck').checked);  \n"
                + "      showClass('Percentage', document.getElementById('PercentageCheck').checked);  \n"
                + "      showClass('Category', document.getElementById('CategoryCheck').checked);  \n"
                + "      showClass('Calculated', document.getElementById('CalculatedCheck').checked);  \n"
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
                .append(UserConfig.getBoolean(baseName + "ShowRowNumber", false) ? "checked" : "")
                .append(" onclick=\"showClass('RowNumber', this.checked);\">")
                .append(message("RowNumber")).append("</INPUT>\n");

        s.append("    <INPUT id=\"DataValueCheck\"  type=\"checkbox\"")
                .append(UserConfig.getBoolean(baseName + "ShowValue", false) ? "checked" : "")
                .append(" onclick=\"showClass('DataValue', this.checked);\">")
                .append(message("Value")).append("</INPUT>\n");

        s.append("    <INPUT id=\"PercentageCheck\"  type=\"checkbox\" ")
                .append(UserConfig.getBoolean(baseName + "ShowPercentage", true) ? "checked" : "")
                .append(" onclick=\"showClass('Percentage', this.checked);\">")
                .append(message("Percentage")).append("</INPUT>\n");

        s.append("    <INPUT id=\"CategoryCheck\"  type=\"checkbox\" ")
                .append(UserConfig.getBoolean(baseName + "ShowCategory", true) ? "checked" : "")
                .append(" onclick=\"showClass('Category', this.checked);\">")
                .append(message("Category")).append("</INPUT>\n");

        s.append("    <INPUT id=\"CalculatedCheck\"  type=\"checkbox\" ")
                .append(UserConfig.getBoolean(baseName + "ShowCalculatedValues", true) ? "checked" : "")
                .append(" onclick=\"showClass('Calculated', this.checked);\">")
                .append(message("CalculatedValues")).append("</INPUT>\n");
        s.append("</DIV>\n").append("<HR/>\n");
        return s.toString();
    }

    protected String jsComments() {
        return "\n<HR/>\n<P align=left style=\"font-size:0.8em;\">* " + message("HtmlEditableComments") + "</P>\n";
    }

    public void pageLoaded() {
        Element docNode = webViewController.webEngine.getDocument().getDocumentElement();
        EventTarget t = (EventTarget) docNode;
        t.removeEventListener("click", clickListener, false);
        t.addEventListener("click", clickListener, false);
    }

    @Override
    public void cleanPane() {
        clickListener = null;
        super.cleanPane();
    }

}
