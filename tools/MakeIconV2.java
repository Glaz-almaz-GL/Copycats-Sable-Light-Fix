import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Перерисовка иконки из image.png:
 * - Берём силуэт кота с прозрачным фоном (image.png)
 * - Кладём в круглую рамку с синим градиентом (как на скриншоте)
 * - Добавляем фонарь (lantern.png) в правый верхний угол с glow
 */
public class MakeIconV2 {
    public static void main(String[] args) throws Exception {
        BufferedImage cat = ImageIO.read(new File(args[0]));
        BufferedImage lantern = ImageIO.read(new File(args[1]));
        String out = args[2];

        int S = 128;
        BufferedImage icon = new BufferedImage(S, S, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = icon.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Круглый фон с синим градиентом (как на скриншоте)
        int pad = 4;
        int circR = S / 2 - pad;
        RadialGradientPaint bgGrad = new RadialGradientPaint(
                new Point2D.Float(S / 2f, S / 2f), circR * 0.85f,
                new float[]{0f, 1f},
                new Color[]{new Color(90, 150, 210), new Color(25, 60, 110)});
        g.setPaint(bgGrad);
        g.fillOval(pad, pad, circR * 2, circR * 2);

        // Тонкая белая обводка круга
        g.setColor(new Color(200, 220, 255, 140));
        g.setStroke(new BasicStroke(2.5f));
        g.drawOval(pad, pad, circR * 2 - 1, circR * 2 - 1);

        // 2. Силуэт кота — масштабируем и центрируем внутри круга
        int cw = 88, ch = 88;
        BufferedImage catScaled = scale(cat, cw, ch);
        BufferedImage catMasked = cropAlpha(catScaled);
        int catX = S / 2 - catMasked.getWidth() / 2 + 2;
        int catY = S / 2 - catMasked.getHeight() / 2 + 4;
        g.drawImage(catMasked, catX, catY, null);

        // 3. Фонарь в правом верхнем углу со свечением
        int ls = 44;
        int gx = S - ls - 10, gy = 8;
        for (int r = 5; r >= 1; r--) {
            g.setColor(new Color(255, 190, 70, 20 * r));
            g.fillOval(gx + ls / 2 - 12 * r, gy + ls / 2 - 12 * r, 24 * r + ls, 24 * r + ls);
        }
        g.drawImage(scale(lantern, ls, ls), gx, gy, null);

        g.dispose();
        ImageIO.write(icon, "png", new File(out));
        System.out.println("written: " + new File(out).getAbsolutePath());
    }

    /** Вырезает только непрозрачную часть изображения (bounding box alpha). */
    static BufferedImage cropAlpha(BufferedImage src) {
        int minX = src.getWidth(), maxX = 0, minY = src.getHeight(), maxY = 0;
        for (int y = 0; y < src.getHeight(); y++) {
            for (int x = 0; x < src.getWidth(); x++) {
                int a = (src.getRGB(x, y) >>> 24) & 0xff;
                if (a > 40) {
                    if (x < minX) minX = x;
                    if (x > maxX) maxX = x;
                    if (y < minY) minY = y;
                    if (y > maxY) maxY = y;
                }
            }
        }
        if (maxX < minX || maxY < minY) return src;
        return src.getSubimage(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }

    static BufferedImage scale(BufferedImage src, int w, int h) {
        BufferedImage dst = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = dst.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(src, 0, 0, w, h, null);
        g.dispose();
        return dst;
    }
}
