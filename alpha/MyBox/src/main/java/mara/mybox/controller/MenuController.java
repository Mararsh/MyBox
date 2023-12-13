package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Window;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-7-27
 * @License Apache License Version 2.0
 */
public class MenuController extends BaseChildController {

    protected Node node;
    protected String baseStyle;
    protected double initX, initY;

    @FXML
    protected CheckBox childWindowCheck;
    @FXML
    protected HBox topBox, bottomBox;
    @FXML
    protected VBox nodesBox;
    @FXML
    protected Label titleLabel;
    @FXML
    protected Button functionsButton, closePopButton, closePop2Button;

    public MenuController() {
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            parentController = this;
            baseStyle = thisPane.getStyle();
            if (baseStyle == null) {
                baseStyle = "";
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void setStageStatus() {
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();

            PopTools.setWindowStyle(thisPane, baseName, baseStyle);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setParameters(BaseController parent, Node node, double x, double y) {
        try {
            this.parentController = parent;
            this.node = node;
            initX = x;
            initY = y;
            thisPane.requestFocus();
            if (openSourceButton != null) {
                openSourceButton.setDisable(parentController.sourceFile == null
                        || !parentController.sourceFile.exists());
            }

            String name = name(parent, node);
            Window window = getMyWindow();
            if (window instanceof Popup) {
                window.setX(x);
                window.setY(y);
            } else {
                if (parent != null && getMyStage() != null) {
                    myStage.setTitle(parent.getTitle());
                }

            }

            if (childWindowCheck != null) {
                childWindowCheck.setSelected(UserConfig.getBoolean(name + "AsChildWindow", true));
                childWindowCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        UserConfig.setBoolean(name + "AsChildWindow", childWindowCheck.isSelected());
                    }
                });

            }

            if (node != null && node.getId() != null) {
                setTitleid(node.getId());
            }

            setControlsStyle();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setTitle() {
        try {
            if (parentController != null) {
                if (getMyStage() != null) {
                    myStage.setTitle(parentController.getTitle());
                }

            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setWidth(double w) {
        thisPane.setPrefWidth(w);
        thisPane.applyCss();
        thisPane.layout();
    }

    public void setTitleid(String id) {
        if (titleLabel == null || id == null || id.isBlank()) {
            return;
        }
        titleLabel.setText(message("Target") + ": " + id);
    }

    public void setTitleLabel(String s) {
        if (titleLabel == null || s == null || s.isBlank()) {
            return;
        }
        titleLabel.setText(s);
    }

    public void addNode(Node node) {
        nodesBox.getChildren().add(node);
    }

    public void addNode(int index, Node node) {
        nodesBox.getChildren().add(index, node);
    }

    public void addFlowPane(List<Node> nodes) {
        addFlowPane(-1, nodes);
    }

    public void addFlowPane(int index, List<Node> nodes) {
        try {
            FlowPane flowPane = new FlowPane();
            flowPane.setMinHeight(Region.USE_PREF_SIZE);
            flowPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            flowPane.setVgap(5);
            flowPane.setHgap(5);
            if (nodes != null) {
                flowPane.getChildren().setAll(nodes);
            }
            if (index >= 0) {
                addNode(index, flowPane);
            } else {
                addNode(flowPane);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void openSourcePath() {
        parentController.openSourcePath();
    }

    @FXML
    protected void showStyles(Event event) {
        PopTools.popWindowStyles(this, baseStyle, event);
    }

    @FXML
    protected void popStyles(Event event) {
        if (UserConfig.getBoolean("WindowStylesPopWhenMouseHovering", false)) {
            showStyles(event);
        }
    }

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (!super.keyEventsFilter(event)) {
            if (parentController != null) {
                return parentController.keyEventsFilter(event);
            } else {
                return false;
            }
        }
        return true;
    }

    /*
        static methods
     */
    public static String name(BaseController parent, Node node) {
        try {
            if (parent == null) {
                return null;
            }
            String name = parent.getBaseName();
            if (node != null && node.getId() != null) {
                name += "_" + node.getId();
            }
            name += "_menu";
            return name;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static MenuController open(BaseController parent, Node node, double x, double y) {
        try {
            if (parent == null) {
                return null;
            }
            if (UserConfig.getBoolean(name(parent, node) + "AsChildWindow", true)) {
                MenuController controller = (MenuController) WindowTools.branchStage(
                        parent, Fxmls.MenuFxml);
                if (controller == null) {
                    return null;
                }
                controller.setParameters(parent, node, x, y);
                controller.requestMouse();
                return controller;
            } else {
                return pop(parent, node, x, y);
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static MenuController open(BaseController parent, Node node, Event event) {
        Point2D everntCoord = LocateTools.coordinate(event);
        return open(parent, node, everntCoord.getX(), everntCoord.getY() + LocateTools.PopOffsetY);
    }

    public static MenuController pop(BaseController parent, Node node, Event event) {
        Point2D everntCoord = LocateTools.coordinate(event);
        return pop(parent, node, everntCoord.getX(), everntCoord.getY() + LocateTools.PopOffsetY);
    }

    public static MenuController pop(BaseController parent, Node node, double x, double y) {
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
            MyBoxLog.error(e);
            return null;
        }
    }

    public static void closeAll() {
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof MenuController) {
                try {
                    MenuController controller = (MenuController) object;
                    controller.close();
                } catch (Exception e) {
                }
            }
        }
    }

}
