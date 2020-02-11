/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mara.mybox.data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.TableLocation;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;

/**
 *
 * @author mara
 */
public class Location {

    protected long dataid = -1;
    protected String dataSet, address, source, comments, imageLocation, dataLabel;
    protected double longitude, latitude, altitude, precision, speed,
            dataValue = Double.MIN_VALUE, dataSize = Double.MIN_VALUE;
    protected int direction, coordinateSystem;
    protected boolean timeBC;
    protected long dataTime = -1;

    public static Location create() {
        return new Location();
    }

    public static List<Location> ChinaEarlyCultures() {
        String dataset = message("ChinaEarlyCultures");
        List<Location> data = TableLocation.read(dataset, 1);
        if (data != null && !data.isEmpty()) {
            return data;
        }
        data = new ArrayList<>();
        Location location = create().setDataSet(dataset).setDataLabel("裴李岗文化\n公元前5500～前4900年")
                .setDataValue(-4900)
                .setComments("裴李岗文化是新石器时代早期的文化，公元前5500～前4900年。\n"
                        + "其遗址位于黄河流城中游地区，因其遗迹最早发现于河南省新郑市的裴李岗村而得名。\n"
                        + "已发现的遗迹有住地、灰坑、陶窖和墓葬等。裴李岗人的房屋均为半地穴式建筑，以圆形为主，并有阶梯式门道。\n"
                        + "从遗址挖掘出的石器，如石铲、石镰、石磨盘来看，此时已出现农业生产，作物主要为粟。"
                        + "另外，畜牧业也已经出现，裴李岗人已经懂得在木栅栏和洞穴中饲养猪、牛、羊、鸡等家畜和家禽。\n"
                        + "遗址中发现的陶器多为泥质红陶，均为手制，大多数陶器都没有纹饰，裴李岗文化是中国已知的最早的陶器文化。")
                .setLongitude(113.659836).setLatitude(34.435688)
                .setAddress("河南省郑州市新郑市裴李岗村");
        data.add(location);

        location = create().setDataSet(dataset).setDataLabel("磁山文化\n公元前5400～前5100年")
                .setDataValue(-5100)
                .setComments("磁山文化是中国华北地区的新石器文化，公元前5400～前5100年。\n"
                        + "因首在河北省邯郸市武安磁山发现而得名，是世界上粮食粟、家鸡和中原核桃最早发现地。\n"
                        + "在磁山遗址，一共发现了189个储存粮食的“窖穴”。这些“粮仓”形似袋状，窖口直径大都为1—2米，深浅不一，最浅的只有0.85米，而最深的则达到了5米。"
                        + "这些窖穴中的“粟灰”一般堆积厚度为0.2—2米，有10个甚至达到了2米以上。如果按照比重、体积推测，这189个“粮仓”中储存的粟，至少应在5万公斤以上。"
                        + "而在当时简陋的生产条件下，剩余这么多的粮食几乎是不可想像的。")
                .setLongitude(114.134526).setLatitude(36.654066)
                .setAddress("河北省邯郸市武安市武安");
        data.add(location);

        location = create().setDataSet(dataset).setDataLabel("大地湾文化\n公元前2800～前60000年")
                .setDataValue(-2800)
                .setComments("大地湾文化是中国黄河中游最早也是延续时间最长的旧石器文化和新石器时代文化，约距今4800至60000年，位于甘肃省天水市秦安邵店村。\n"
                        + "第1—3文化层形成于60000—20000年前，地层中仅发现石英砸击技术产品，如石英石片、碎片等；"
                        + "第4文化层形成于20000—13000年前，细石器技术产品和大地湾一期陶片开始出现，但在遗物总体数量上处于从属地位；\n"
                        + "第5文化层形成于13000—7000年前，以细石器和大地湾一期陶片为主；第6文化层形成于7000—5000年前，主要文化遗物为半坡和仰韶晚期陶片。\n"
                        + "大地湾遗址的彩陶是中国已知最古老的彩陶之一。大地湾晚期F411房屋的大型地画，距今5000年，更是迄今最早且保存完整的绘画作品。")
                .setLongitude(105.904519).setLatitude(35.013761)
                .setAddress("甘肃省天水市秦安县邵店村");
        data.add(location);

        if (TableLocation.write(data)) {
            return data;
        } else {
            return null;
        }
    }

    public static List<Location> LiBaiFootprints() {
        String dataset = message("LiBaiFootprints");
        List<Location> data = TableLocation.read(dataset, 1);
        if (data != null && !data.isEmpty()) {
            return data;
        }

        if (TableLocation.write(data)) {
            return data;
        } else {
            return null;
        }
    }

    public static void importChinaEarlyCultures() {
        File file;
        if ("zh".equals(AppVariables.getLanguage())) {
            file = FxmlControl.getInternalFile("/data/db/Location_zh.del",
                    "AppTemp", "Location_zh.del");
        } else {
            file = FxmlControl.getInternalFile("/data/db/Location_en.del",
                    "AppTemp", "Location_en.del");
        }
        DerbyBase.importData("Location", file.getAbsolutePath(), false);
    }

    /*
        get/set
     */
    public long getDataid() {
        return dataid;
    }

    public Location setDataid(long dataid) {
        this.dataid = dataid;
        return this;
    }

    public String getDataSet() {
        return dataSet;
    }

    public Location setDataSet(String dataSet) {
        this.dataSet = dataSet;
        return this;
    }

    public String getSource() {
        return source;
    }

    public Location setSource(String source) {
        this.source = source;
        return this;
    }

    public String getComments() {
        return comments;
    }

    public Location setComments(String comments) {
        this.comments = comments;
        return this;
    }

    public double getLongitude() {
        return longitude;
    }

    public Location setLongitude(double longitude) {
        this.longitude = longitude;
        return this;
    }

    public double getLatitude() {
        return latitude;
    }

    public Location setLatitude(double latitude) {
        this.latitude = latitude;
        return this;
    }

    public double getAltitude() {
        return altitude;
    }

    public Location setAltitude(double altitude) {
        this.altitude = altitude;
        return this;
    }

    public double getPrecision() {
        return precision;
    }

    public Location setPrecision(double precision) {
        this.precision = precision;
        return this;
    }

    public double getSpeed() {
        return speed;
    }

    public Location setSpeed(double speed) {
        this.speed = speed;
        return this;
    }

    public int getDirection() {
        return direction;
    }

    public Location setDirection(int direction) {
        this.direction = direction;
        return this;
    }

    public long getDataTime() {
        return dataTime;
    }

    public Location setDataTime(long dataTime) {
        this.dataTime = dataTime;
        return this;
    }

    public String getImageLocation() {
        return imageLocation;
    }

    public Location setImageLocation(String imageLocation) {
        this.imageLocation = imageLocation;
        return this;
    }

    public int getCoordinateSystem() {
        return coordinateSystem;
    }

    public Location setCoordinateSystem(int coordinateSystem) {
        this.coordinateSystem = coordinateSystem;
        return this;
    }

    public String getAddress() {
        return address;
    }

    public Location setAddress(String address) {
        this.address = address;
        return this;
    }

    public double getDataValue() {
        return dataValue;
    }

    public Location setDataValue(double dataValue) {
        this.dataValue = dataValue;
        return this;
    }

    public double getDataSize() {
        return dataSize;
    }

    public Location setDataSize(double dataSize) {
        this.dataSize = dataSize;
        return this;
    }

    public boolean isTimeBC() {
        return timeBC;
    }

    public Location setTimeBC(boolean timeBC) {
        this.timeBC = timeBC;
        return this;
    }

    public String getDataLabel() {
        return dataLabel;
    }

    public Location setDataLabel(String dataLabel) {
        this.dataLabel = dataLabel;
        return this;
    }

}
