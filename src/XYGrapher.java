import java.awt.*;

public abstract class XYGrapher extends GoodDrawGraph {
    private Color graphColor = Color.BLACK;

    public abstract Coordinate xyStart();
    public abstract double xRange();
    public abstract double yRange();
    public abstract Coordinate getPoint(int pointNum);

    public void setGraphColor(Color color) {
        this.graphColor = color;
    }

    public Color getGraphColor() {
        return this.graphColor;
    }
}