package mara.mybox.controller;

import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-7-27
 * @License Apache License Version 2.0
 */
public abstract class BaseController_MouseEvents extends BaseController_KeyEvents {

    private MouseEvent mouseEvent;

    public void monitorMouseEvents() {
        try {
            if (thisPane != null) {
                thisPane.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
                    mouseEvent = event;
                    if (mouseEventsFilter(event)) {
                        MyBoxLog.debug("consume:" + this.getClass()
                                + " source:" + event.getSource().getClass() + " target:" + event.getTarget().getClass()
                                + " count:" + event.getClickCount() + " rightClick:" + (event.getButton() == MouseButton.SECONDARY));
                        event.consume();
                    }
                    mouseEvent = null;
                });

            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    // return whether handled
    public boolean mouseEventsFilter(MouseEvent event) {
        MyBoxLog.debug("consume:" + this.getClass()
                + " source:" + event.getSource().getClass() + " target:" + event.getTarget().getClass()
                + " count:" + event.getClickCount() + " rightClick:" + (event.getButton() == MouseButton.SECONDARY));
        if (event.isSecondaryButtonDown()) {
            return rightClickFilter(event);

        }
        return false;
    }

    public boolean rightClickFilter(MouseEvent event) {
        return false;
    }

}
