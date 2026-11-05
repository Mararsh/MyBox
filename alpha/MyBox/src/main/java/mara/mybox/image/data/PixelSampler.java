/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mara.mybox.image.data;

import java.awt.image.BufferedImage;

public class PixelSampler {

    public enum SampleMode {
        NEAREST(new NearestSampler()),
        BILINEAR(new BilinearSampler()),
        MITCHELL(new MitchellSampler()),
        CATMULL_ROM(new CatmullRomSampler()),
        LANCZOS2(new Lanczos2Sampler()),
        LANCZOS3(new Lanczos3Sampler());

        public final Sampler sampler;

        SampleMode(Sampler s) {
            sampler = s;
        }
    }

    public abstract static class Sampler {

        protected static final float PI = (float) Math.PI;

        protected final int iMin;
        protected final int iMax;
        protected final int jMin;
        protected final int jMax;

        protected Sampler(int iMin, int iMax, int jMin, int jMax) {
            this.iMin = iMin;
            this.iMax = iMax;
            this.jMin = jMin;
            this.jMax = jMax;
        }

        protected abstract float weight(float t);

        public int sample(BufferedImage image, float x, float y) {
            final int imgW = image.getWidth();
            final int imgH = image.getHeight();
            int xi = (int) Math.floor(x);
            int yi = (int) Math.floor(y);
            float fx = x - xi;
            float fy = y - yi;

            float sumW = 0f;
            float sumR = 0f;
            float sumG = 0f;
            float sumB = 0f;
            float sumA = 0f;

            for (int j = jMin; j <= jMax; j++) {
                float wy = weight(fy - j);
                if (Math.abs(wy) < 1e-8f) {
                    continue;
                }
                for (int i = iMin; i <= iMax; i++) {
                    float wx = weight(fx - i);
                    if (Math.abs(wx) < 1e-8f) {
                        continue;
                    }
                    float w = wx * wy;
                    int px = xi + i;
                    int py = yi + j;
                    if (px < 0 || py < 0 || px >= imgW || py >= imgH) {
                        continue;
                    }

                    int p = image.getRGB(px, py);
                    int a = (p >>> 24) & 0xff;
                    if (a == 0) {
                        continue;
                    }
                    int r = (p >> 16) & 0xff;
                    int g = (p >> 8) & 0xff;
                    int b = p & 0xff;

                    sumW += w;
                    sumR += r * w;
                    sumG += g * w;
                    sumB += b * w;
                    sumA += a * w;
                }
            }

            if (sumW < 1e-6f) {
                return 0;
            }
            float invW = 1.0f / sumW;
            int outA = Math.clamp((int) (sumA * invW + 0.5f), 0, 255);
            int outR = Math.clamp((int) (sumR * invW + 0.5f), 0, 255);
            int outG = Math.clamp((int) (sumG * invW + 0.5f), 0, 255);
            int outB = Math.clamp((int) (sumB * invW + 0.5f), 0, 255);

            return (outA << 24) | (outR << 16) | (outG << 8) | outB;
        }

        protected int getPixel(BufferedImage img, int x, int y) {
            x = Math.clamp(x, 0, img.getWidth() - 1);
            y = Math.clamp(y, 0, img.getHeight() - 1);
            return img.getRGB(x, y);
        }

        protected boolean isTransparent(int p) {
            int ca = (p >>> 24);
            int cr = (p >> 16) & 0xff;
            int cg = (p >> 8) & 0xff;
            int cb = p & 0xff;
            return cr == 0 && cg == 0 && cb == 0 && ca == 0;
        }

        protected int pack(float a, float r, float g, float b, float totalW) {
            if (totalW < 1e-6f) {
                return 0;
            }
            float inv = 1.0f / totalW;
            int ia = Math.clamp((int) (a * inv), 0, 255);
            int ir = Math.clamp((int) (r * inv), 0, 255);
            int ig = Math.clamp((int) (g * inv), 0, 255);
            int ib = Math.clamp((int) (b * inv), 0, 255);
            return (ia << 24) | (ir << 16) | (ig << 8) | ib;
        }

        protected float sinc(float x) {
            if (Math.abs(x) < 1e-6f) {
                return 1f;
            }
            return (float) (Math.sin(PI * x) / (PI * x));
        }
    }

    public static class NearestSampler extends Sampler {

        public NearestSampler() {
            super(0, 0, 0, 0);
        }

        @Override
        public int sample(BufferedImage target, float x, float y) {
            int ix = Math.round(x);
            int iy = Math.round(y);
            int w = target.getWidth();
            int h = target.getHeight();
            if (ix < 0 || iy < 0 || ix >= w || iy >= h) {
                return 0;
            }
            return getPixel(target, ix, iy);
        }

        @Override
        protected float weight(float t) {
            return 0;
        }
    }

    public static class BilinearSampler extends Sampler {

        public BilinearSampler() {
            super(0, 1, 0, 1);
        }

        @Override
        protected float weight(float t) {
            if (t >= 0 && t <= 1) {
                return 1 - t;
            }
            return 0;
        }
    }

    public static class MitchellSampler extends Sampler {

        public MitchellSampler() {
            super(-1, 2, -1, 2);
        }

        @Override
        protected float weight(float t) {
            t = Math.abs(t);
            if (t < 1f) {
                return (float) (0.5 * (2 + t * t * (-5 + 3 * t)));
            } else if (t < 2f) {
                return (float) (0.5 * (4 + t * (-8 + t * (5 - t))));
            }
            return 0f;
        }
    }

    public static class CatmullRomSampler extends Sampler {

        public CatmullRomSampler() {
            super(-1, 2, -1, 2);
        }

        @Override
        protected float weight(float t) {
            t = Math.abs(t);
            if (t < 1f) {
                return (float) (1.5 * t * t * t - 2.5 * t * t + 1);
            } else if (t < 2f) {
                return (float) (-0.5 * t * t * t + 2.5 * t * t - 4 * t + 2);
            }
            return 0f;
        }
    }

    public static class Lanczos2Sampler extends Sampler {

        public Lanczos2Sampler() {
            super(-2, 2, -2, 2);
        }

        @Override
        protected float weight(float t) {
            t = Math.abs(t);
            if (t >= 2f) {
                return 0f;
            }
            return sinc(t) * sinc(t / 2f);
        }
    }

    public static class Lanczos3Sampler extends Sampler {

        public Lanczos3Sampler() {
            super(-3, 3, -3, 3);
        }

        @Override
        protected float weight(float t) {
            t = Math.abs(t);
            if (t >= 3f) {
                return 0f;
            }
            return sinc(t) * sinc(t / 3f);
        }
    }

}
