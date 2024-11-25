import java.awt.*;
import java.util.ArrayList;

public class GrapherUtils {

    public static void drawGraph(Graphics g, XYGrapher[] graphers, Coordinate start, double xRange, double yRange, int xPixelStart, int yPixelStart, int pixelsWide, int pixelsHigh) {
        Graphics2D g2 = (Graphics2D) g;
        Stroke origStroke = g2.getStroke();
        for (XYGrapher grapher : graphers) {
            ArrayList<Coordinate> coords = new ArrayList<>();
            int i = 0;

            for (Coordinate point = grapher.getPoint(i); point != null; point = grapher.getPoint(i)) {
                i++;
                if (point != null) {
                    double xPixel = xPixelStart + (point.getX() - start.getX()) * (pixelsWide / xRange);
                    double yPixel = yPixelStart + (start.getY() + yRange - point.getY()) * (pixelsHigh / yRange);
                    coords.add(new Coordinate(xPixel, yPixel, point.drawFrom(), point.drawTo()));
                }
            }

            int yAxis = xPixelStart - (int) (start.getX() * (pixelsWide / xRange));
            int xAxis = yPixelStart + (int) ((start.getY() + yRange) * (pixelsHigh / yRange));

            g.setColor(new Color(165, 165, 165));
            g2.setStroke(origStroke);
            if (xAxis > 0 && xAxis < pixelsHigh) {
                g.drawLine(0, xAxis, pixelsWide, xAxis);
            }

            if (yAxis > 0 && yAxis < pixelsWide) {
                g.drawLine(yAxis, 0, yAxis, pixelsHigh);
            }

            g.setColor(grapher.getGraphColor());
            g2.setStroke(new BasicStroke(2.0f));

            for (int j = 1; j < coords.size(); j++) {
                Coordinate current = coords.get(j);
                Coordinate previous = coords.get(j - 1);
                if (current.drawTo() && previous.drawFrom()) {
                    g2.drawLine(
                            (int) Math.round(previous.getX()),
                            (int) Math.round(previous.getY()),
                            (int) Math.round(current.getX()),
                            (int) Math.round(current.getY())
                    );
                }
            }
        }
    }
}