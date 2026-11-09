package mara.mybox.image.data;

import java.awt.image.BufferedImage;

/**
 * @Author Mara
 * @CreateDate 2026-11-5
 * @License Apache License Version 2.0
 *
 * Assisted by Doubao and DeepSeek
 */
public class PixelSampler {

    public enum SamplingAlgorithm {
        /**
         * Nearest‑Neighbor Sampling: Fastest, obvious aliasing, no smoothing
         */
        NearestNeighbor {
            @Override
            public Sampler create() {
                return new NearestSampler();
            }
        },
        /**
         * Bilinear Interpolation: Fast, basic smoothing, suitable for real-time
         * preview
         */
        BilinearInterpolation {
            @Override
            public Sampler create() {
                return new BilinearSampler();
            }
        },
        /**
         * Mitchell‑Netravali: Balanced sharpness and ringing suppression,
         * commonly used for scaling
         */
        MITCHELL {
            @Override
            public Sampler create() {
                return new MitchellSampler();
            }
        },
        /**
         * Catmull‑Rom Spline (non-standard bicubic): Produces relatively sharp
         * results
         */
        CATMULL_ROM {
            @Override
            public Sampler create() {
                return new CatmullRomSampler();
            }
        },
        /**
         * Lanczos‑2‑Lobe: Excellent image quality, high sharpness, moderate
         * computation cost
         */
        LANCZOS2 {
            @Override
            public Sampler create() {
                return new Lanczos2Sampler();
            }
        },
        /**
         * Lanczos‑3‑Lobe: Highest quality, better ringing suppression, highest
         * computation cost
         */
        LANCZOS3 {
            @Override
            public Sampler create() {
                return new Lanczos3Sampler();
            }
        };

        public abstract Sampler create();
    }

    public enum EdgeMode {
        CLAMP, // Clamp: Repeats edge pixels when out of bounds
        WRAP, // Wrap: Tile repeat for out-of-bounds coordinates
        MIRROR, // Mirror: Flip reflection across image boundary
        TRANSPARENT // Transparent: Treat out-of-bounds as fully transparent black
    }

    public abstract static class Sampler {

        private int i0 = -1;
        private int i1 = 2;
        private int j0 = -1;
        private int j1 = 2;
        private EdgeMode edgeMode = EdgeMode.CLAMP;

        protected Sampler() {
        }

        public final Sampler setWindow(int i0, int i1, int j0, int j1) {
            this.i0 = i0;
            this.i1 = i1;
            this.j0 = j0;
            this.j1 = j1;
            return this;
        }

        public Sampler setEdgeMode(EdgeMode edgeMode) {
            this.edgeMode = edgeMode;
            return this;
        }

        public EdgeMode getEdgeMode() {
            return edgeMode;
        }

        protected abstract float kernel(float offset);

        private int getPixel(BufferedImage img, int x, int y) {
            Integer[] pt = wrapCoord(x, y, img.getWidth(), img.getHeight(), edgeMode);
            if (pt == null) {
                return 0;
            }
            return img.getRGB(pt[0], pt[1]);
        }

        public boolean isReady() {
            return edgeMode != null;
        }

        public int sample(BufferedImage img, float x, float y) {
            if (!isReady()) {
                throw new IllegalStateException("Sampler not configured");
            }
            int xi = (int) Math.floor(x);
            int yi = (int) Math.floor(y);
            float fx = x - xi;
            float fy = y - yi;

            float totalW = 0f;
            float r = 0f, g = 0f, b = 0f, a = 0f;

            for (int j = j0; j <= j1; j++) {
                float wy = kernel(fy - j);
                for (int i = i0; i <= i1; i++) {
                    float wx = kernel(fx - i);
                    float w = wx * wy;
                    if (Math.abs(w) < 1e-6f) {
                        continue;
                    }

                    int p = getPixel(img, xi + i, yi + j);
                    int ca = (p >>> 24) & 0xff;
                    int cr = (p >>> 16) & 0xff;
                    int cg = (p >>> 8) & 0xff;
                    int cb = p & 0xff;

                    a += ca * w;
                    r += cr * w;
                    g += cg * w;
                    b += cb * w;
                    totalW += w;
                }
            }
            if (totalW <= 1e-6f) {
                return 0;
            }
            a /= totalW;
            r /= totalW;
            g /= totalW;
            b /= totalW;

            a = Math.clamp(a, 0, 255);
            r = Math.clamp(r, 0, 255);
            g = Math.clamp(g, 0, 255);
            b = Math.clamp(b, 0, 255);
            return ((int) a << 24) | ((int) r << 16) | ((int) g << 8) | (int) b;
        }
    }

    public static class NearestSampler extends Sampler {

        public NearestSampler() {
            super();
            setWindow(0, 0, 0, 0);
        }

        @Override
        protected float kernel(float offset) {
            return 1f;
        }

        @Override
        public int sample(BufferedImage img, float x, float y) {
            if (!isReady()) {
                throw new IllegalStateException("Sampler not configured");
            }
            int ix = Math.round(x);
            int iy = Math.round(y);
            Integer[] pt = wrapCoord(ix, iy, img.getWidth(), img.getHeight(), getEdgeMode());
            return pt == null ? 0 : img.getRGB(pt[0], pt[1]);
        }
    }

    public static class BilinearSampler extends Sampler {

        public BilinearSampler() {
            super();
            setWindow(0, 1, 0, 1);
        }

        @Override
        protected float kernel(float offset) {
            float d = Math.abs(offset);
            return d < 1f ? 1f - d : 0f;
        }
    }

    public static class MitchellSampler extends Sampler {

        public MitchellSampler() {
            super();
            setWindow(-1, 2, -1, 2);
        }

        @Override
        protected float kernel(float t) {
            float x = Math.abs(t);
            if (x < 1) {
                return (float) ((16 + x * x * (-12 + x * 6)) / 18.0);
            }
            if (x < 2) {
                return (float) ((32 + x * (-48 + x * (24 - x * 4))) / 18.0);
            }
            return 0f;
        }
    }

    public static class CatmullRomSampler extends Sampler {

        public CatmullRomSampler() {
            super();
            setWindow(-1, 2, -1, 2);
        }

        @Override
        protected float kernel(float t) {
            float x = Math.abs(t);
            if (x < 1f) {
                return 0.5f * (2 + x * x * x - 3 * x * x);
            }
            if (x < 2f) {
                return 0.5f * (4 - 8 * x + 5 * x * x - x * x * x);
            }
            return 0f;
        }
    }

    public static class Lanczos2Sampler extends Sampler {

        public Lanczos2Sampler() {
            super();
            setWindow(-2, 2, -2, 2);
        }

        @Override
        protected float kernel(float x) {
            x = Math.abs(x);
            if (x < 1e-6f) {
                return 1f;
            }
            if (x >= 2f) {
                return 0f;
            }
            return (float) (Math.sin(Math.PI * x) / (Math.PI * x) * Math.sin(Math.PI * x / 2.0) / (Math.PI * x / 2.0));
        }
    }

    public static class Lanczos3Sampler extends Sampler {

        public Lanczos3Sampler() {
            super();
            setWindow(-3, 3, -3, 3);
        }

        @Override
        protected float kernel(float x) {
            x = Math.abs(x);
            if (x < 1e-6f) {
                return 1f;
            }
            if (x >= 3f) {
                return 0f;
            }
            return (float) (Math.sin(Math.PI * x) / (Math.PI * x) * Math.sin(Math.PI * x / 3.0) / (Math.PI * x / 3.0));
        }
    }

    private static Integer[] wrapCoord(int x, int y, int w, int h, EdgeMode mode) {
        switch (mode) {
            case TRANSPARENT:
                if (x >= 0 && x < w && y >= 0 && y < h) {
                    return new Integer[]{x, y};
                }
                return null;
            case CLAMP:
                int nx = Math.clamp(x, 0, w - 1);
                int ny = Math.clamp(y, 0, h - 1);
                return new Integer[]{nx, ny};
            case MIRROR:
                return mirrorWrap(x, y, w, h);
            case WRAP:
                int rx = (x % w + w) % w;
                int ry = (y % h + h) % h;
                return new Integer[]{rx, ry};
            default:
                return null;
        }
    }

    private static Integer[] mirrorWrap(int x, int y, int w, int h) {
        int mx = mirrorOne(x, w);
        int my = mirrorOne(y, h);
        return new Integer[]{mx, my};
    }

    private static int mirrorOne(int p, int len) {
        if (len <= 1) {
            return 0;
        }
        int cycle = (len - 1) * 2;
        int mod = (p % cycle + cycle) % cycle;
        return mod < len ? mod : cycle - mod;
    }

}
