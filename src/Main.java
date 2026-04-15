import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Main extends JPanel {

    // --- COLOR PALETTE ---
    private final Color CLOCK_FRAME_TOP = new Color(60, 60, 65);
    private final Color CLOCK_FRAME_BOTTOM = new Color(20, 20, 22);
    private final Color DIAL_BACKGROUND = new Color(15, 15, 18);
    private final Color TICK_MAJOR = new Color(220, 220, 230);
    private final Color TICK_MINOR = new Color(90, 90, 100);
    private final Color HAND_MAIN_FILL = new Color(245, 245, 250);
    private final Color HAND_MAIN_BEVEL = new Color(180, 180, 190);
    private final Color ACCENT = new Color(255, 69, 0); // Vibrant Red-Orange
    private final Color TEXT_PRIMARY = new Color(200, 200, 210);

    public Main() {
        // Ultra-smooth smoothness update
        Timer timer = new Timer(16, e -> repaint()); // Approx 60 FPS
        timer.start();
        setBackground(CLOCK_FRAME_BOTTOM);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // --- HIGH-QUALITY RENDERING SETTINGS ---
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        int width = getWidth();
        int height = getHeight();
        int centerX = width / 2;
        int centerY = height / 2;
        int maxRadius = Math.min(width, height) / 2 - 20;

        drawWindowBackground(g2d, width, height);

        // Define main geometry areas
        Area bezelArea = new Area(new Ellipse2D.Double(centerX - maxRadius, centerY - maxRadius, maxRadius * 2, maxRadius * 2));
        int dialRadius = maxRadius - 15;
        Area dialArea = new Area(new Ellipse2D.Double(centerX - dialRadius, centerY - dialRadius, dialRadius * 2, dialRadius * 2));

        // Draw Layers
        drawBeveledFrame(g2d, bezelArea, dialArea, centerX, centerY, maxRadius);
        drawDialFace(g2d, dialArea, centerX, centerY, dialRadius);
        drawDigitalComplication(g2d, centerX, centerY, dialRadius);
        drawHands(g2d, centerX, centerY, dialRadius);
    }

    private void drawWindowBackground(Graphics2D g2d, int w, int h) {
        // Rich, subtle background texture for the window
        LinearGradientPaint lgp = new LinearGradientPaint(
                0, 0, 0, h,
                new float[]{0f, 0.5f, 1f},
                new Color[]{CLOCK_FRAME_TOP, CLOCK_FRAME_BOTTOM, DIAL_BACKGROUND}
        );
        g2d.setPaint(lgp);
        g2d.fillRect(0, 0, w, h);
    }

    private void drawBeveledFrame(Graphics2D g2d, Area bezel, Area dial, int cx, int cy, int r) {
        // Frame 1: Outer "Brushed Metal" ring
        GradientPaint gpOuter = new GradientPaint(cx - r, cy - r, CLOCK_FRAME_TOP, cx + r, cy + r, CLOCK_FRAME_BOTTOM);
        g2d.setPaint(gpOuter);
        g2d.fill(bezel);

        // Frame 2: Polish Lip (thin bright inner ring)
        g2d.setColor(new Color(100, 100, 110));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.draw(new Ellipse2D.Double(cx - r + 13, cy - r + 13, (r - 13) * 2, (r - 13) * 2));
    }

    private void drawDialFace(Graphics2D g2d, Area dial, int cx, int cy, int r) {
        // Dial Background: Premium "Enamel" depth effect
        RadialGradientPaint rgpDial = new RadialGradientPaint(
                cx, cy, r,
                new float[]{0f, 0.9f, 1f},
                new Color[]{new Color(30, 30, 35), DIAL_BACKGROUND, Color.BLACK}
        );
        g2d.setPaint(rgpDial);
        g2d.fill(dial);

        // Concave Shadow around the inner bezel
        g2d.setPaint(new Color(0, 0, 0, 150));
        g2d.setStroke(new BasicStroke(4f));
        g2d.draw(new Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2));

        // --- TICKS & READABILITY ---
        g2d.setFont(new Font("Georgia", Font.BOLD, r / 6)); // Elegant serif for numbers
        FontMetrics fm = g2d.getFontMetrics();

        for (int i = 0; i < 60; i++) {
            double angle = Math.toRadians(i * 6 - 90);
            boolean isHour = (i % 5 == 0);

            // Ticks
            int tickStart = isHour ? r - 18 : r - 8;
            int startX = (int) (cx + Math.cos(angle) * tickStart);
            int startY = (int) (cy + Math.sin(angle) * tickStart);
            int endX = (int) (cx + Math.cos(angle) * (r - 3));
            int endY = (int) (cy + Math.sin(angle) * (r - 3));

            g2d.setColor(isHour ? TICK_MAJOR : TICK_MINOR);
            g2d.setStroke(new BasicStroke(isHour ? 3f : 1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
            g2d.draw(new Line2D.Double(startX, startY, endX, endY));

            // Numbers (Every 5 minutes)
            if (isHour) {
                String num = String.valueOf(i == 0 ? 12 : i / 5);
                int numR = r - 45; // Move numbers inward
                int nx = (int) (cx + Math.cos(angle) * numR) - fm.stringWidth(num) / 2;
                int ny = (int) (cy + Math.sin(angle) * numR) + fm.getAscent() / 3;

                // Subtle Glow/Shadow behind numbers for maximum contrast
                g2d.setColor(new Color(0, 0, 0, 200));
                g2d.drawString(num, nx + 1, ny + 1);
                g2d.setColor(TICK_MAJOR);
                g2d.drawString(num, nx, ny);
            }
        }
    }

    private void drawDigitalComplication(Graphics2D g2d, int cx, int cy, int r) {
        LocalDateTime now = LocalDateTime.now();

        String timeStr = now.format(DateTimeFormatter.ofPattern("HH : mm"));
        String dateStr = now.format(DateTimeFormatter.ofPattern("EEE dd MMM"));

        // Complication Pill
        int pillW = (int)(r * 0.9);
        int pillH = (int)(r * 0.28);
        int pillX = cx - pillW/2;
        int pillY = cy + (int)(r * 0.45);

        // Glassy Pill Background
        LinearGradientPaint glassGp = new LinearGradientPaint(
                pillX, pillY, pillX, pillY + pillH,
                new float[]{0f, 0.45f, 0.5f, 1f},
                new Color[]{new Color(40,40,45, 180), new Color(20,20,22, 180), Color.BLACK, new Color(25,25,30, 180)}
        );
        g2d.setPaint(glassGp);
        g2d.fillRoundRect(pillX, pillY, pillW, pillH, 15, 15);
        g2d.setColor(new Color(100,100,110, 100)); // Pill Border
        g2d.drawRoundRect(pillX, pillY, pillW, pillH, 15, 15);

        // Digital Readability Text
        g2d.setFont(new Font("Monospaced", Font.BOLD, r / 9));
        g2d.setColor(new Color(230,230,240));
        int textX = cx - g2d.getFontMetrics().stringWidth(timeStr)/2;
        g2d.drawString(timeStr, textX, pillY + g2d.getFontMetrics().getAscent() + 5);

        g2d.setFont(new Font("SansSerif", Font.PLAIN, r / 14));
        g2d.setColor(ACCENT);
        int dateX = cx - g2d.getFontMetrics().stringWidth(dateStr)/2;
        g2d.drawString(dateStr, dateX, pillY + pillH - 8);
    }

    private void drawHands(Graphics2D g2d, int cx, int cy, int r) {
        LocalDateTime time = LocalDateTime.now();
        double nanos = time.getNano() / 1_000_000_000.0;
        double secA = Math.toRadians((time.getSecond() * 6) + (nanos * 6) - 90);
        double minA = Math.toRadians((time.getMinute() * 6) + (time.getSecond() * 0.1) - 90);
        double hourA = Math.toRadians((time.getHour() * 30) + (time.getMinute() * 0.5) - 90);

        // 1. Layered "Soft" Shadows (drawn offset from center)
        drawShadowHand(g2d, cx+4, cy+5, hourA, r * 0.55, 10);
        drawShadowHand(g2d, cx+4, cy+5, minA, r * 0.82, 7);
        drawShadowHand(g2d, cx+2, cy+3, secA, r * 0.90, 2);

        // 2. Beveled Main Hands (Dauphine Style Path geometry)
        drawBeveledHand(g2d, cx, cy, hourA, r * 0.55, 10, true);
        drawBeveledHand(g2d, cx, cy, minA, r * 0.82, 7, false);

        // 3. Center Pin Hub (Stacked)
        g2d.setColor(CLOCK_FRAME_TOP);
        g2d.fill(new Ellipse2D.Double(cx - 9, cy - 9, 18, 18));
        g2d.setColor(ACCENT); // Accent pin
        g2d.fill(new Ellipse2D.Double(cx - 3, cy - 3, 6, 6));

        // 4. Accent Second Hand with Counterweight
        g2d.setColor(ACCENT);
        drawSimpleHand(g2d, cx, cy, secA, r * 0.90, 2f);
        drawSimpleHand(g2d, cx, cy, secA + Math.PI, r * 0.18, 4f); // Refined Tail
    }

    private void drawBeveledHand(Graphics2D g2d, int cx, int cy, double angle, double len, int width, boolean isHour) {
        // Creates a complex Dauphine-style hand shape
        Path2D.Double handPath = new Path2D.Double();
        handPath.moveTo(0, -width / 2.0); // Base Left
        handPath.lineTo(len * 0.8, -width / 4.0); // Taper start
        handPath.lineTo(len, 0); // Tip
        handPath.lineTo(len * 0.8, width / 4.0); // Taper end
        handPath.lineTo(0, width / 2.0); // Base Right
        handPath.closePath();

        AffineTransform at = new AffineTransform();
        at.translate(cx, cy);
        at.rotate(angle);
        Shape transformedHand = at.createTransformedShape(handPath);

        // Fill Main Side
        g2d.setColor(isHour ? HAND_MAIN_FILL : HAND_MAIN_BEVEL);
        g2d.fill(transformedHand);

        // Fill Bevel Side (split geometry to simulate lighting angle)
        g2d.setColor(isHour ? HAND_MAIN_BEVEL : HAND_MAIN_FILL);
        g2d.setStroke(new BasicStroke(0.5f));
        g2d.draw(transformedHand); // Polished edge
    }

    private void drawSimpleHand(Graphics2D g2d, int cx, int cy, double angle, double length, float thickness) {
        int endX = (int) (cx + Math.cos(angle) * length);
        int endY = (int) (cy + Math.sin(angle) * length);
        g2d.setStroke(new BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.draw(new Line2D.Double(cx, cy, endX, endY));
    }

    private void drawShadowHand(Graphics2D g2d, int cx, int cy, double angle, double length, float thickness) {
        g2d.setColor(new Color(0, 0, 0, 70)); // Very soft diffused shadow
        drawSimpleHand(g2d, cx, cy, angle, length, thickness + 2); // Slightly wider
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) {}

            JFrame frame = new JFrame("Luxury Timepiece Complication");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(700, 700);
            frame.setLocationRelativeTo(null);

            Main clockPanel = new Main();
            frame.add(clockPanel);
            frame.setVisible(true);
        });
    }
}