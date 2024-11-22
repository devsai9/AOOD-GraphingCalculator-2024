public abstract class ParametricGrapher extends XYGrapher {
    // public abstract Coordinate xyStart();
    // public abstract double xRange();
    // public abstract double yRange();

    public abstract double tInterval();
    public abstract double tStart();
    public abstract double tEnd();

    public abstract double xValue(double t);
    public abstract double yValue(double t);

    public Coordinate getPoint(int pointNum) {
        double tL = tStart() + (pointNum * tInterval());
        if (tL > tEnd()) return null;
        return new Coordinate(xValue(tL), yValue(tL));
    }
}