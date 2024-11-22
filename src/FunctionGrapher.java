public abstract class FunctionGrapher extends XYGrapher {
    public abstract double xIncrement();

    public double xValue(int pointNum) {
        return ((double) pointNum) * xIncrement() + xyStart().getX();
    }

    public abstract double yValue(double xValue);

    public Coordinate getPoint(int pointNum) {
        double x = xValue(pointNum);
        if (x > xyStart().getX() + xRange()) return null;

        return new Coordinate(x, yValue(x));
    }
}