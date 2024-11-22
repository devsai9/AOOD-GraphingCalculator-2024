public abstract class XYGrapher extends GoodDrawGraph {
    public abstract Coordinate xyStart();
    public abstract double xRange();
    public abstract double yRange();
    public abstract Coordinate getPoint(int pointNum);
}