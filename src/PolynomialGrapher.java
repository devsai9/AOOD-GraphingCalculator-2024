public abstract class PolynomialGrapher extends FunctionGrapher {
    abstract public double[] coefficients();

    public double yValue(double xValue) {
        double[] coefficientsL = coefficients();
        double sum = 0;
        for (int i = 0; i < coefficientsL.length; i++) {
            sum += coefficientsL[i] * Math.pow(xValue, i);
        }
        return sum;
    }
}