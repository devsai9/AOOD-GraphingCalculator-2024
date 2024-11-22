public abstract class DiscontinuousFunctionGrapher extends FunctionGrapher {
    public boolean exclude(double xValue) { return false; }

    public Coordinate getPoint(int pointNum) {
        double x = xValue(pointNum);
        boolean toExclude = exclude(x);

        if (toExclude) {
            return new Coordinate(x, 0, false, false);
        }

        return super.getPoint(pointNum);
    }
}