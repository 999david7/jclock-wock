import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Main extends JPanel {

    private final Color bgColor = new Color(18, 18, 20);
    private final Color bezelColor1 = new Color(50, 50, 55);
    private final Color bezelColor2 = new Color(20, 20, 25);
    private final Color textColor = new Color(220, 220, 230);
    private final Color accentColor = new Color(255, 87, 34); // Vibrant Deep Orange

    public Main() {
        // 30ms timer for ultra-smooth hand sweeping
        Timer timer = new Timer(30, e -> repaint());
        timer.start();
        setBackground(bgColor);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Enable high-quality rendering
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        int width = getWidth();
        int height = getHeight();
        int centerX = width / 2;
        int centerY = height / 2;
        int radius = Math.min(width, height) / 2 - 40;

        drawBackground(g2d, width, height);
        drawBezel(g2d, centerX, centerY, radius);
        drawFace(g2d, centerX, centerY, radius);
        drawDigitalDisplay(g2d, centerX, centerY, radius);
        drawHands(g2d, centerX, centerY, radius);
    }

    private void drawBackground(Graphics2D g2d, int width, int height) {
        // Soft radial gradient for a premium dark mode feel
        RadialGradientPaint rgp = new RadialGradientPaint(
                width / 2f, height / 2f, Math.max(width, height),
                new float[]{0f, 1f},
                new Color[]{new Color(35, 35, 40), bgColor}
        );
        g2d.setPaint(rgp);
        g2d.fillRect(0, 0, width, height);
    }

    private void drawBezel(Graphics2D g2d, int centerX, int centerY, int radius) {
        // Outer metallic rim effect
        GradientPaint gp = new GradientPaint(
                centerX - radius, centerY - radius, bezelColor1,
                centerX + radius, centerY + radius, bezelColor2
        );
        g2d.setPaint(gp);
        g2d.fill(new Ellipse2D.Double(centerX - radius - 5, centerY - radius - 5, radius * 2 + 10, radius * 2 + 10));

        // Inner dial background
        g2d.setColor(new Color(24, 24, 28));
        g2d.fill(new Ellipse2D.Double(centerX - radius, centerY - radius, radius * 2, radius * 2));
    }

    private void drawFace(Graphics2D g2d, int centerX, int centerY, int radius) {
        // Draw Ticks
        for (int i = 0; i < 60; i++) {
            double angle = Math.toRadians(i * 6 - 90);
            boolean isHour = (i % 5 == 0);

            int length = isHour ? 15 : 7;
            int startX = (int) (centerX + Math.cos(angle) * (radius - length));
            int startY = (int) (centerY + Math.sin(angle) * (radius - length));
            int endX = (int) (centerX + Math.cos(angle) * radius);
            int endY = (int) (centerY + Math.sin(angle) * radius);

            g2d.setColor(isHour ? textColor : new Color(100, 100, 110));
            g2d.setStroke(new BasicStroke(isHour ? 3 : 1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.draw(new Line2D.Double(startX, startY, endX, endY));
        }

        // Draw Numbers (Readability enhancement)
        g2d.setFont(new Font("SansSerif", Font.BOLD, radius / 6));
        FontMetrics fm = g2d.getFontMetrics();
        g2d.setColor(textColor);

        for (int i = 1; i <= 12; i++) {
            String num = String.valueOf(i);
            double angle = Math.toRadians((i * 30) - 90);
            // Position numbers inward from the ticks
            int numRadius = radius - 35;
            int nx = (int) (centerX + Math.cos(angle) * numRadius) - fm.stringWidth(num) / 2;
            int ny = (int) (centerY + Math.sin(angle) * numRadius) + fm.getAscent() / 3;
            g2d.drawString(num, nx, ny);
        }
    }

    private void drawDigitalDisplay(Graphics2D g2d, int centerX, int centerY, int radius) {
        LocalDateTime now = LocalDateTime.now();

        // Digital Time
        String timeStr = now.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        g2d.setFont(new Font("Monospaced", Font.BOLD, radius / 8));
        FontMetrics fmTime = g2d.getFontMetrics();
        int timeX = centerX - fmTime.stringWidth(timeStr) / 2;
        int timeY = centerY + (int)(radius * 0.45);

        // Date
        String dateStr = now.format(DateTimeFormatter.ofPattern("EEE, MMM d"));
        g2d.setFont(new Font("SansSerif", Font.PLAIN, radius / 12));
        FontMetrics fmDate = g2d.getFontMetrics();
        int dateX = centerX - fmDate.stringWidth(dateStr) / 2;
        int dateY = timeY + fmDate.getHeight();

        // Draw subtle background pill for text
        int pillWidth = Math.max(fmTime.stringWidth(timeStr), fmDate.stringWidth(dateStr)) + 30;
        int pillHeight = fmTime.getHeight() + fmDate.getHeight() + 10;
        g2d.setColor(new Color(0, 0, 0, 80));
        g2d.fillRoundRect(centerX - pillWidth / 2, timeY - fmTime.getAscent() - 5, pillWidth, pillHeight, 20, 20);

        // Draw Text
        g2d.setColor(new Color(200, 200, 200));
        g2d.drawString(timeStr, timeX, timeY);
        g2d.setColor(accentColor);
        g2d.drawString(dateStr, dateX, dateY);
    }

    private void drawHands(Graphics2D g2d, int centerX, int centerY, int radius) {
        LocalDateTime time = LocalDateTime.now();

        double secAngle = Math.toRadians((time.getSecond() * 6) + (time.getNano() / 1_000_000_000.0 * 6) - 90);
        double minAngle = Math.toRadians((time.getMinute() * 6) + (time.getSecond() * 0.1) - 90);
        double hourAngle = Math.toRadians((time.getHour() * 30) + (time.getMinute() * 0.5) - 90);

        // Draw drop shadows first
        g2d.setColor(new Color(0, 0, 0, 100)); // Semi-transparent black
        drawHand(g2d, centerX + 3, centerY + 3, hourAngle, radius * 0.5, 8);
        drawHand(g2d, centerX + 3, centerY + 3, minAngle, radius * 0.75, 5);
        drawHand(g2d, centerX + 3, centerY + 3, secAngle, radius * 0.85, 2);

        // Draw actual hands
        g2d.setColor(Color.WHITE);
        drawHand(g2d, centerX, centerY, hourAngle, radius * 0.5, 8); // Hour
        drawHand(g2d, centerX, centerY, minAngle, radius * 0.75, 5); // Minute

        // Second Hand with counter-weight
        g2d.setColor(accentColor);
        drawHand(g2d, centerX, centerY, secAngle, radius * 0.85, 2);
        drawHand(g2d, centerX, centerY, secAngle + Math.PI, radius * 0.15, 3); // Tail

        // Center Pin
        g2d.setColor(accentColor);
        g2d.fill(new Ellipse2D.Double(centerX - 8, centerY - 8, 16, 16));
        g2d.setColor(new Color(24, 24, 28));
        g2d.fill(new Ellipse2D.Double(centerX - 4, centerY - 4, 8, 8));
    }

    private void drawHand(Graphics2D g2d, int cx, int cy, double angle, double length, float thickness) {
        int endX = (int) (cx + Math.cos(angle) * length);
        int endY = (int) (cy + Math.sin(angle) * length);
        g2d.setStroke(new BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.draw(new Line2D.Double(cx, cy, endX, endY));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) {}

            JFrame frame = new JFrame("Enhanced Clock");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(600, 600);
            frame.setLocationRelativeTo(null);

            Main clockPanel = new Main();
            frame.add(clockPanel);
            frame.setVisible(true);
        });
    }
}