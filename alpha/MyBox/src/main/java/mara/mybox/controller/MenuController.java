package mara.mybox.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Window;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.NodeTools;
import mara.mybox.fxml.PopTools;

import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-7-27
 * @License Apache License Version 2.0
 */
public class MenuController extends BaseController {

    protected Node node;
    protected String baseStyle;
    protected double initX, initY;

    @FXML
    protected HBox topBox, bottomBox;
    @FXML
    protected VBox nodesBox;
    @FXML
    protected Label titleLabel;
    @FXML
    protected Button functionsButton;

    public MenuController() {
    }

    @Override
    public void initControls() {
        try {
            parentController = this;
            baseStyle = thisPane.getStyle();
            if (baseStyle == null) {
                baseStyle = "";
            }
            String style = UserConfig.getString(baseName + "WindowStyle", "");
            setLabelsStyle(baseStyle + style);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void setStageStatus(String prefix, int minSize) {
        setAsPopup(baseName);
    }

    public void setParameters(BaseController parent, Node node, double x, double y) {
        try {
            this.parentController = parent;
            initX = x;
            initY = y;
            thisPane.requestFocus();

            Window window = getMyWindow();
            if (window instanceof Popup) {
                window.setX(x);
                window.setY(y);
            }
            setControlsStyle();

            if (node != null) {
                setTitle(Languages.message("Target") + ": " + node.getId());
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setWidth(double w) {
        thisPane.setPrefWidth(w);
        thisPane.layout();
        thisPane.applyCss();
    }

    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    public void setLabelsStyle(String style) {
        thisPane.setStyle(style);
        setLabelsStyle(thisPane, style);
    }

    public void setLabelsStyle(Node node, String style) {
        if (node instanceof Label) {
            node.setStyle(style);
        } else if (node instanceof Parent && !(node instanceof TableView)) {
            for (Node child : ((Parent) node).getChildrenUnmodifiable()) {
                setLabelsStyle(child, style);
            }
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

    @FXML
    public void popFunctionsMenu(MouseEvent mouseEvent) {

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
            styles.put("Default", "");
            styles.put("Transparent", "; -fx-text-fill: black; -fx-background-color: transparent;");
            styles.put("Console", "; -fx-text-fill: #CCFF99; -fx-background-color: black;");
            styles.put("Blackboard", "; -fx-text-fill: white; -fx-background-color: #336633;");
            styles.put("Ago", "; -fx-text-fill: white; -fx-background-color: darkblue;");
            styles.put("Book", "; -fx-text-fill: black; -fx-background-color: #F6F1EB;");
            for (String name : styles.keySet()) {
                menu = new MenuItem(Languages.message(name));
                menu.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        String style = styles.get(name);
                        UserConfig.setString(baseName + "WindowStyle", style);
                        setLabelsStyle(baseStyle + style);
                    }
                });
                popMenu.getItems().add(menu);
            }

            popMenu.getItems().add(new SeparatorMenuItem());

            menu = new MenuItem(Languages.message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    popMenu.hide();
                }
            });
            popMenu.getItems().add(menu);

            LocateTools.locateMouse(mouseEvent, popMenu);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void cancelAction() {
        closeStage();
    }

    @Override
    public boolean keyESC() {
        closeStage();
        return false;
    }

    @Override
    public boolean keyF6() {
        closeStage();
        return false;
    }

    /*
        static methods
     */
    public static MenuController open(BaseController parent, Node node, double x, double y) {
        try {
            try {
                if (parent == null) {
                    return null;
                }
                Popup popup = PopTools.popWindow(parent, Fxmls.MenuFxml, node, x, y);
                if (popup == null) {
                    return null;
                }
                Object object = popup.getUserData();
                if (object == null && !(object instanceof MenuController)) {
                    return null;
                }
                MenuController controller = (MenuController) object;
                controller.setParameters(parent, node, x, y);
                return controller;
            } catch (Exception e) {
                MyBoxLog.error(e.toString());
                return null;
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
