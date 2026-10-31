import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Берёт исходную иконку copycats_icon.png (600x600, круг) и добавляет
 * фонарь (lantern.png) в правый верхний угол со свечением.
 * Использование: java AddLantern <source.png> <lantern.png> <out.png> [size]
 */
public class AddLantern {
    public static void main(String[] args) throws Exception {
        BufferedImage src = ImageIO.read(new File(args[0]));
        BufferedImage lantern = ImageIO.read(new File(args[1]));
        String out = args[2];
        int S = args.length > 3 ? Integer.parseInt(args[3]) : src.getWidth();

        BufferedImage icon = new BufferedImage(S, S, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = icon.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // Исходная иконка целиком
        g.drawImage(src, 0, 0, S, S, null);

        // Фонарь в правом верхнем углу, внутри круга, со свечением
        int ls = (int) (S * 0.30);
        int gx = (int) (S * 0.64), gy = (int) (S * 0.12);
        // Мягкое тёплое свечение, обрезанное по кругу (не выходит за иконку)
        Shape oldClip = g.getClip();
        g.setClip(new java.awt.geom.Ellipse2D.Float(0, 0, S, S));
        for (int r = 5; r >= 1; r--) {
            g.setColor(new Color(255, 200, 90, 12 * r));
            int pad = (int) (ls * 0.16) * r;
            // центр свечения = центр фонаря
            g.fillOval(gx + ls / 2 - (ls + pad) / 2, gy + ls / 2 - (ls + pad) / 2, ls + pad, ls + pad);
        }
        g.setClip(oldClip);
        g.drawImage(scale(lantern, ls, ls), gx, gy, null);

        g.dispose();
        ImageIO.write(icon, "png", new File(out));
        System.out.println("written: " + new File(out).getAbsolutePath() + " (" + S + "x" + S + ")");
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
