import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Иконка "Copycats Sable Light Fix": изометрическая копият-панель (Create)
 * с фонарём (Minecraft) в правом верхнем углу.
 * Использование: java MakeIcon <copycat_base.png> <lantern.png> <out.png>
 */
public class MakeIcon {
    public static void main(String[] args) throws Exception {
        BufferedImage copycat = ImageIO.read(new File(args[0]));
        BufferedImage lantern = ImageIO.read(new File(args[1]));
        String out = args[2];

        int S = 128;
        BufferedImage icon = new BufferedImage(S, S, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = icon.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        // Тёмный фон со скруглением
        g.setColor(new Color(24, 26, 30));
        g.fillRoundRect(0, 0, S, S, 20, 20);

        // --- Изометрическая копият-плита ---
        // Лицевая сторона копиир-панели: тёмная основа + светлая X-рамка,
        // нарисованная поверх (тот же узор, что у copycat_base в Create).
        int cx = S / 2, cy = S / 2 + 6;
        int halfW = 46, halfH = 23;
        int sideH = 11;

        BufferedImage panel = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D pg = panel.createGraphics();
        pg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        pg.setColor(new Color(112, 118, 124));
        pg.fillRect(0, 0, 16, 16);
        pg.setColor(new Color(168, 175, 182));
        pg.drawRect(0, 0, 15, 15);
        pg.drawLine(3, 3, 12, 12);
        pg.drawLine(12, 3, 3, 12);
        pg.setColor(new Color(88, 94, 100));
        pg.drawLine(4, 4, 11, 11);
        pg.drawLine(11, 4, 4, 11);
        pg.dispose();
        // Лёгкий шум для текстуры металла
        java.util.Random rnd = new java.util.Random(7);
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                int p = panel.getRGB(x, y);
                int d = rnd.nextInt(14) - 7;
                int r = clamp(((p >> 16) & 0xff) + d), gg = clamp(((p >> 8) & 0xff) + d), b = clamp((p & 0xff) + d);
                panel.setRGB(x, y, (p & 0xff000000) | (r << 16) | (gg << 8) | b);
            }
        }
        BufferedImage inner = scale(panel, 160, 160);
        BufferedImage mask = opaqueMask(inner);

        BufferedImage top = xform(inner, mask, S, (u, v) -> new double[]{
                cx + (u - v) * halfW, cy + (u + v - 1.0) * halfH});
        BufferedImage side = xform(inner, mask, S, (u, v) -> new double[]{
                cx + (u - v) * halfW, cy + (u + v - 1.0) * halfH + sideH});
        shade(side, 0.55);

        // Тень под плитой
        g.setColor(new Color(0, 0, 0, 80));
        g.fillOval(cx - halfW - 6, cy + halfH + sideH - 6, 2 * halfW + 12, 16);
        g.drawImage(side, 0, 0, null);
        g.drawImage(top, 0, 0, null);

        // --- Фонарь в правом верхнем углу со свечением ---
        int ls = 46;
        int gx = S - ls - 6, gy = 6;
        for (int r = 4; r >= 1; r--) {
            g.setColor(new Color(255, 190, 70, 22 * r));
            int pad = 7 * r;
            g.fillOval(gx + ls / 2 - ls / 3 - pad / 2, gy + ls / 2 - ls / 3 - pad / 2,
                    (ls * 2) / 3 + pad, (ls * 2) / 3 + pad);
        }
        g.drawImage(scale(lantern, ls, ls), gx, gy, null);

        g.dispose();
        ImageIO.write(icon, "png", new File(out));
        System.out.println("written: " + new File(out).getAbsolutePath());
    }

    static BufferedImage opaqueMask(BufferedImage src) {
        BufferedImage mask = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < src.getHeight(); y++) {
            for (int x = 0; x < src.getWidth(); x++) {
                int a = (src.getRGB(x, y) >>> 24) & 0xff;
                mask.getRaster().setSample(x, y, 0, a > 40 ? 255 : 0);
            }
        }
        return mask;
    }

    static int clamp(int v) { return Math.max(0, Math.min(255, v)); }

    interface Map2D { double[] apply(double u, double v); }

    /** Прямое отображение единичного квадрата src в параллелограмм (обратное сэмплирование),
     *  с учётом mask для пропуска прозрачных пикселей. */
    static BufferedImage xform(BufferedImage src, BufferedImage mask, int S, Map2D f) {
        double[] p0 = f.apply(0, 0), px = f.apply(1, 0), py = f.apply(0, 1);
        double ax = px[0] - p0[0], ay = px[1] - p0[1];
        double bx = py[0] - p0[0], by = py[1] - p0[1];
        double det = ax * by - ay * bx;
        BufferedImage dst = new BufferedImage(S, S, BufferedImage.TYPE_INT_ARGB);
        int sw = src.getWidth(), sh = src.getHeight();
        for (int y = 0; y < S; y++) {
            for (int x = 0; x < S; x++) {
                double dx = x + 0.5 - p0[0], dy = y + 0.5 - p0[1];
                double u = (dx * by - dy * bx) / det;
                double v = (dy * ax - dx * ay) / det;
                if (u < 0 || u >= 1 || v < 0 || v >= 1) continue;
                int sx = Math.min(sw - 1, Math.max(0, (int) (u * sw)));
                int sy = Math.min(sh - 1, Math.max(0, (int) (v * sh)));
                int ma = mask.getRaster().getSample(sx, sy, 0);
                if (ma < 40) continue;  // только где в оригинале есть пиксель
                int p = src.getRGB(sx, sy);
                dst.setRGB(x, y, p);
            }
        }
        return dst;
    }

    static void shade(BufferedImage img, double k) {
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int p = img.getRGB(x, y);
                int a = (p >>> 24) & 0xff;
                if (a == 0) continue;
                int r = (int) (((p >> 16) & 0xff) * k);
                int gg = (int) (((p >> 8) & 0xff) * k);
                int b = (int) ((p & 0xff) * k);
                img.setRGB(x, y, (a << 24) | (r << 16) | (gg << 8) | b);
            }
        }
    }

    static BufferedImage scale(BufferedImage src, int w, int h) {
        BufferedImage dst = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = dst.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(src, 0, 0, w, h, null);
        g.dispose();
        return dst;
    }
}
