public class Coordinate {
    private double x;
    private double y;
    private boolean drawTo;
    private boolean drawFrom;

    public Coordinate(double x, double y) {
        this.x = x;
        this.y = y;
        drawTo = true;
        drawFrom = true;
    }

    public Coordinate(double x, double y, boolean drawFrom, boolean drawTo) {
        this.x = x;
        this.y = y;
        this.drawTo = drawTo;
        this.drawFrom = drawFrom;
    }

    public double getX() { return x; }

    public double getY() { return y; }

    public boolean drawTo() { return drawTo; }

    public boolean drawFrom() { return drawFrom; }
}