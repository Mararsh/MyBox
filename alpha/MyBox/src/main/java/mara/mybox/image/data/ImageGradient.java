package mara.mybox.image.data;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.RadialGradientPaint;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;

public class ImageGradient {

    public enum GradientType {
        LinearOverlay, // 线性渐变图层叠加（整张图渐变覆层）
        RadialOverlay, // 径向渐变图层叠加
        ConicOverlay, // 锥形/角度渐变图层叠加
        AlongPathOverlay, // 沿路径渐变（原生不支持，采样实现）
        SoftLight          // 柔光渐变层（模拟 PS 柔光）
    }

    // member variables 对外参数成员
    private FxTask task;
    private GradientType gradientType = GradientType.LinearOverlay;
    private BufferedImage source, target;

    // Linear
    private float x1, y1, x2, y2;
    // Radial 新增径向参数
    private float cx, cy, fx, fy, radius;
    // Conic 新增锥形参数
    private float conicCx, conicCy, startAngle;

    private float[] fractions = new float[]{0f, 1f}; // testing
    private Color[] colors = new Color[]{new Color(0, 0, 255, 255), new Color(255, 0, 0, 255)}; // testing
    private CycleMethod cycle = CycleMethod.NO_CYCLE;
    private AlphaComposite blend = AlphaComposite.SrcOver;
    private float alpha = 0.6f;

    // internal variables 内部临时变量，不做get/set
    private int imageWidth, imageHeight;
    private Graphics2D g2d;
    private MultipleGradientPaint paint;

    public ImageGradient() {
    }

    public Image render(Image image, FxTask inTask) {
        if (image == null) {
            return null;
        }
        source = SwingFXUtils.fromFXImage(image, null);
        task = inTask;
        target = render();
        if (target == null || (task != null && !task.isWorking())) {
            return null;
        }
        Image newImage = SwingFXUtils.toFXImage(target, null);
        return newImage;
    }

    public BufferedImage render() {
        try {
            if (!isValid()) {
                return null;
            }
            imageWidth = source.getWidth();
            imageHeight = source.getHeight();
            target = null;
            paint = null;
            switch (gradientType) {
                case LinearOverlay:
                    paint = linearOverlay();
                    break;
                case RadialOverlay:
                    paint = radialOverlay();
                    break;
                case ConicOverlay:
                    paint = conicOverlay();
                    break;
                case AlongPathOverlay:
                    // 原生无实现，预留占位
                    MyBoxLog.info("AlongPathOverlay not implemented yet");
                    break;
                case SoftLight:
                    // 柔光不属于MultipleGradientPaint，预留占位
                    MyBoxLog.info("SoftLight not implemented yet");
                    break;
            }
            if (paint != null) {
                target = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
                g2d = target.createGraphics();
                g2d.drawImage(source, 0, 0, null);
                g2d.setComposite(AlphaComposite.getInstance(blend.getRule(), alpha));
                g2d.setPaint(paint);
//                g2d.fillOval(0, 0, imageWidth, imageHeight);
                g2d.fillOval(100, 100, 300, 300);
                g2d.dispose();
            }
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public boolean isValid() {
        return source != null && fractions != null && colors != null
                && cycle != null && blend != null;
    }

    public MultipleGradientPaint linearOverlay() {
        try {
            x1 = 50;
            y1 = 100;
            x2 = 200;
            y2 = 300;
            return new LinearGradientPaint(x1, y1, x2, y2, fractions, colors, cycle);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public MultipleGradientPaint radialOverlay() {
        try {
            Point2D center = new Point2D.Float(cx, cy);
            Point2D focus = new Point2D.Float(fx, fy);
            return new RadialGradientPaint(center, radius, focus, fractions, colors, cycle);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    /**
     * Java 原生AWT没有ConicGradientPaint，这里注释说明，预留方法 需要自定义实现锥形渐变，这里先返回null占位
     */
    public MultipleGradientPaint conicOverlay() {
        MyBoxLog.info("ConicOverlay: AWT native no conic gradient paint, need custom implementation");
        return null;
    }

    // ===================== Getter & Setter 只放成员变量，内部变量(imageWidth/g2d/paint)不生成 =====================
    public FxTask getTask() {
        return task;
    }

    public ImageGradient setTask(FxTask task) {
        this.task = task;
        return this;
    }

    public GradientType getGradientType() {
        return gradientType;
    }

    public ImageGradient setGradientType(GradientType gradientType) {
        this.gradientType = gradientType;
        return this;
    }

    public BufferedImage getSource() {
        return source;
    }

    public ImageGradient setSource(BufferedImage source) {
        this.source = source;
        return this;
    }

    public BufferedImage getTarget() {
        return target;
    }

    public ImageGradient setTarget(BufferedImage target) {
        this.target = target;
        return this;
    }

    public float getX1() {
        return x1;
    }

    public ImageGradient setX1(float x1) {
        this.x1 = x1;
        return this;
    }

    public float getY1() {
        return y1;
    }

    public ImageGradient setY1(float y1) {
        this.y1 = y1;
        return this;
    }

    public float getX2() {
        return x2;
    }

    public ImageGradient setX2(float x2) {
        this.x2 = x2;
        return this;
    }

    public float getY2() {
        return y2;
    }

    public ImageGradient setY2(float y2) {
        this.y2 = y2;
        return this;
    }

    public float getCx() {
        return cx;
    }

    public ImageGradient setCx(float cx) {
        this.cx = cx;
        return this;
    }

    public float getCy() {
        return cy;
    }

    public ImageGradient setCy(float cy) {
        this.cy = cy;
        return this;
    }

    public float getFx() {
        return fx;
    }

    public ImageGradient setFx(float fx) {
        this.fx = fx;
        return this;
    }

    public float getFy() {
        return fy;
    }

    public ImageGradient setFy(float fy) {
        this.fy = fy;
        return this;
    }

    public float getRadius() {
        return radius;
    }

    public ImageGradient setRadius(float radius) {
        this.radius = radius;
        return this;
    }

    public float getConicCx() {
        return conicCx;
    }

    public ImageGradient setConicCx(float conicCx) {
        this.conicCx = conicCx;
        return this;
    }

    public float getConicCy() {
        return conicCy;
    }

    public ImageGradient setConicCy(float conicCy) {
        this.conicCy = conicCy;
        return this;
    }

    public float getStartAngle() {
        return startAngle;
    }

    public ImageGradient setStartAngle(float startAngle) {
        this.startAngle = startAngle;
        return this;
    }

    public float[] getFractions() {
        return fractions;
    }

    public ImageGradient setFractions(float[] fractions) {
        this.fractions = fractions;
        return this;
    }

    public Color[] getColors() {
        return colors;
    }

    public ImageGradient setColors(Color[] colors) {
        this.colors = colors;
        return this;
    }

    public CycleMethod getCycle() {
        return cycle;
    }

    public ImageGradient setCycle(CycleMethod cycle) {
        this.cycle = cycle;
        return this;
    }

    public AlphaComposite getBlend() {
        return blend;
    }

    public ImageGradient setBlend(AlphaComposite blend) {
        this.blend = blend;
        return this;
    }

    public float getAlpha() {
        return alpha;
    }

    public ImageGradient setAlpha(float alpha) {
        this.alpha = alpha;
        return this;
    }

}
