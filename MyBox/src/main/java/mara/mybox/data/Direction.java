package mara.mybox.data;

import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2020-7-30
 * @License Apache License Version 2.0
 */
public class Direction {

    protected Name name;
    protected int angle; // clockwise

    public enum Name {
        East, West, North, South, EastNorth, WestNorth, EastSouth, WestSouth, Number
    }

    public Direction(Name value) {
        this.name = value == null ? defaultName() : value;
        this.angle = angle(name);
    }

    public Direction(String name) {
        this.name = name(name);
        this.angle = angle(this.name);
    }

    public Direction(int angle) {
        this.name = name(angle);
        this.angle = angle(angle);

    }

    public String name() {
        if (name == null) {
            name = defaultName();
        }
        return name.name();
    }

    /*
        Static methods
     */
    public static Direction defaultDirection() {
        return new Direction(defaultName());
    }

    public static Name defaultName() {
        return Name.East;
    }

    public static Name name(String name) {
        if (name == null || name.isBlank()) {
            return defaultName();
        }
        for (Name item : Name.values()) {
            if (name.equals(item.name()) || name.equals(message(item.name()))) {
                return item;
            }
        }
        return defaultName();
    }

    public static int angle(String name) {
        return angle(name(name));
    }

    public static int angle(Name name) {
        if (name == null) {
            name = defaultName();
        }
        int angle;
        switch (name) {
            case West:
                angle = 270;
                break;
            case North:
                angle = 0;
                break;
            case South:
                angle = 180;
                break;
            case EastNorth:
                angle = 135;
                break;
            case WestNorth:
                angle = 315;
                break;
            case EastSouth:
                angle = 135;
                break;
            case WestSouth:
                angle = 225;
                break;
            case East:
            default:
                angle = 90;
        }
        return angle;
    }

    public static int angle(int value) {
        int angle = value % 360;
        if (angle < 0) {
            angle += 360;
        }
        return angle;
    }

    public static Name name(int value) {
        int angle = angle(value);
        Name name;
        switch (angle) {
            case 90:
                name = Name.East;
                break;
            case 270:
                name = Name.West;
                break;
            case 0:
                name = Name.North;
                break;
            case 180:
                name = Name.South;
                break;
            case 315:
                name = Name.EastNorth;
                break;
            case 45:
                name = Name.WestNorth;
                break;
            case 135:
                name = Name.EastSouth;
                break;
            case 225:
                name = Name.WestSouth;
                break;
            default:
                name = Name.Number;
                break;
        }
        return name;
    }

    /*
        get/set
     */
    public Name getName() {
        return name;
    }

    public void setName(Name name) {
        this.name = name;
    }

    public int getAngle() {
        return angle;
    }

    public void setAngle(int angle) {
        this.angle = angle;
    }

}
