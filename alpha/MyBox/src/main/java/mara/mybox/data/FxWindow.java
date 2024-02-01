package mara.mybox.data;

import javafx.stage.Stage;
import javafx.stage.Window;
import mara.mybox.controller.BaseController;
import mara.mybox.controller.BaseController_Attributes.StageType;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-9-13
 * @License Apache License Version 2.0
 */
public class FxWindow {

    protected Window window;
    protected String title, type, name, modality;
    protected int width, height, x, y;
    protected boolean isShowing, isAlwaysOnTop, isFocused,
            isChild, isFullScreen, isIconified, isMaximized, isResizable;

    public FxWindow() {
        init();
    }

    final public void init() {
        window = null;
        title = null;
        type = null;
        modality = null;
        name = null;
        isShowing = isAlwaysOnTop = isFocused = isChild
                = isFullScreen = isIconified = isMaximized = isResizable
                = false;
    }

    public FxWindow(Window w) {
        init();
        this.window = w;
        if (window == null) {
            return;
        }
        type = StageType.Normal.name();
        name = window.getClass().toString();
        width = (int) window.getWidth();
        height = (int) window.getHeight();
        x = (int) window.getX();
        y = (int) window.getY();
        isShowing = window.isShowing();
        isFocused = window.isFocused();
        if (window instanceof Stage) {
            Stage stage = (Stage) window;
            title = stage.getTitle();
            if (null == stage.getModality()) {
                modality = null;
            } else {
                switch (stage.getModality()) {
                    case APPLICATION_MODAL:
                        modality = message("Application");
                        break;
                    case WINDOW_MODAL:
                        modality = message("Window");
                        break;
                    default:
                        modality = null;
                        break;
                }
            }
            isAlwaysOnTop = stage.isAlwaysOnTop();
            isChild = stage.getOwner() != null;
            isFullScreen = stage.isFullScreen();
            isIconified = stage.isIconified();
            isMaximized = stage.isMaximized();
            isResizable = stage.isResizable();
            Object u = stage.getUserData();
            if (u != null && (u instanceof BaseController)) {
                BaseController controller = (BaseController) u;
                name = controller.getClass().toString();
                type = controller.getStageType().name();
            }
        }
    }

    /*
        get/set
     */
    public Window getWindow() {
        return window;
    }

    public void setWindow(Window window) {
        this.window = window;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getModality() {
        return modality;
    }

    public void setModality(String modality) {
        this.modality = modality;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public boolean isIsShowing() {
        return isShowing;
    }

    public void setIsShowing(boolean isShowing) {
        this.isShowing = isShowing;
    }

    public boolean isIsAlwaysOnTop() {
        return isAlwaysOnTop;
    }

    public void setIsAlwaysOnTop(boolean isAlwaysOnTop) {
        this.isAlwaysOnTop = isAlwaysOnTop;
    }

    public boolean isIsFocused() {
        return isFocused;
    }

    public void setIsFocused(boolean isFocused) {
        this.isFocused = isFocused;
    }

    public boolean isIsChild() {
        return isChild;
    }

    public void setIsChild(boolean isChild) {
        this.isChild = isChild;
    }

    public boolean isIsFullScreen() {
        return isFullScreen;
    }

    public void setIsFullScreen(boolean isFullScreen) {
        this.isFullScreen = isFullScreen;
    }

    public boolean isIsIconified() {
        return isIconified;
    }

    public void setIsIconified(boolean isIconified) {
        this.isIconified = isIconified;
    }

    public boolean isIsMaximized() {
        return isMaximized;
    }

    public void setIsMaximized(boolean isMaximized) {
        this.isMaximized = isMaximized;
    }

    public boolean isIsResizable() {
        return isResizable;
    }

    public void setIsResizable(boolean isResizable) {
        this.isResizable = isResizable;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
