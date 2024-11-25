import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class EquationBox extends JPanel {
    private final JTextField equationField;
    private EquationChangeListener listener;
    private final JButton removeButton;
    private final JLabel statusCircle;
    private boolean isGraphed = true;
    private boolean isValidEquation = true;

    public EquationBox() {
        setLayout(new FlowLayout(FlowLayout.LEFT));

        statusCircle = new JLabel("●");
        statusCircle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        statusCircle.setForeground(Color.GREEN);
        add(statusCircle);

        equationField = new JTextField(15);
        add(equationField);

        removeButton = new JButton("❌");
        removeButton.addActionListener(e -> removeEquationBox());
        add(removeButton);

        statusCircle.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (isValidEquation) {
                    toggleGraphingStatus();
                }
            }
        });

        equationField.getDocument().addDocumentListener((SimpleDocumentListener) () -> {
            try {
                XYGrapher grapher = parseEquation(equationField.getText());
                isValidEquation = true;
                statusCircle.setForeground(isGraphed ? Color.GREEN : Color.BLACK);
                if (listener != null) listener.onEquationChange();
            } catch (Exception e) {
                isValidEquation = false;
                if (listener != null) listener.onEquationChange();
                statusCircle.setForeground(Color.RED);
            }
        });
    }

    private void toggleGraphingStatus() {
        isGraphed = !isGraphed;
        statusCircle.setForeground(isGraphed ? Color.GREEN : Color.BLACK);

        if (listener != null) listener.onEquationChange();
    }

    public void setEquationChangeListener(EquationChangeListener listener) {
        this.listener = listener;
    }

    public XYGrapher getGrapher() {
        if (isGraphed && isValidEquation) {
            try {
                return parseEquation(equationField.getText());
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private void removeEquationBox() {
        if (listener != null) {
            listener.onEquationRemove(this);
        }
    }

    private XYGrapher parseEquation(String equation) throws Exception {
        String cleanedEquation = equation.replaceAll("\\s+", "");

        if (cleanedEquation.contains(";") && cleanedEquation.contains("<")) {
            return parseParametricEquation(cleanedEquation);
        }

        return parsePolynomialEquation(cleanedEquation);
    }

    private XYGrapher parsePolynomialEquation(String cleanedEquation) throws Exception {
        if (cleanedEquation.isEmpty() || cleanedEquation.equals("x") || cleanedEquation.charAt(0) != 'y') {
            throw new Exception("Invalid polynomial equation format.");
        }

        cleanedEquation = cleanedEquation.replace("y=", "").trim();

        // Validate the cleaned equation
        if (cleanedEquation.isEmpty()) {
            throw new Exception("Invalid polynomial equation format. Equation cannot be empty.");
        }

        // Use a map to store coefficients dynamically
        Map<Integer, Double> coefficientMap = getCoefficientMap(cleanedEquation);

        // Convert the map to an array for PolynomialGrapher
        int maxExponent = coefficientMap.keySet().stream().max(Integer::compare).orElse(0);
        double[] coefficients = new double[maxExponent + 1];

        for (Map.Entry<Integer, Double> entry : coefficientMap.entrySet()) {
            coefficients[entry.getKey()] = entry.getValue();
        }

        return new PolynomialGrapher() {
            @Override
            public double[] coefficients() {
                return coefficients;
            }

            @Override
            public Coordinate xyStart() {
                return new Coordinate(-10, -10);
            }

            @Override
            public double xIncrement() {
                return 0.1;
            }

            @Override
            public double xRange() {
                return 20;
            }

            @Override
            public double yRange() {
                return 20;
            }
        };
    }

    private Map<Integer, Double> getCoefficientMap(String cleanedEquation) {
        Map<Integer, Double> coefficientMap = new HashMap<>();
        String[] terms = cleanedEquation.split("(?=[+-])");

        for (String term : terms) {
            if (term.contains("x")) {
                String[] parts = term.split("x");

                // Default exponent is 1 if no ^ is provided
                int exponent = 1;
                if (parts.length > 1 && parts[1].startsWith("^")) {
                    exponent = Integer.parseInt(parts[1].substring(1));
                }

                // Default coefficient is 1 if no number is provided
                double coefficient = parts[0].isEmpty() || parts[0].equals("+") ? 1 :
                        parts[0].equals("-") ? -1 :
                                Double.parseDouble(parts[0]);

                coefficientMap.put(exponent, coefficientMap.getOrDefault(exponent, 0.0) + coefficient);

            } else if (!term.isEmpty()) { // Ensure term is not empty before parsing as a constant
                double constant = Double.parseDouble(term);
                coefficientMap.put(0, coefficientMap.getOrDefault(0, 0.0) + constant);
            }
        }
        return coefficientMap;
    }

    private XYGrapher parseParametricEquation(String cleanedEquation) throws Exception {
        // Split the equation into its components
        String[] parts = cleanedEquation.split(";");
        if (parts.length != 3) {
            throw new Exception("Invalid parametric equation format.");
        }

        // Extract x function, y function, and t range
        String xFunction = parts[0].split("=")[1].trim();
        String yFunction = parts[1].split("=")[1].trim();
        String tRangeString = parts[2].trim();

        // Replace standalone "pi" or "π" with Math.PI and handle coefficients
        tRangeString = tRangeString.replaceAll("(?<![0-9.])pi", String.valueOf(Math.PI))
                .replaceAll("(?<![0-9.])π", String.valueOf(Math.PI))
                .replaceAll("([0-9.]+)pi", "$1*" + Math.PI)
                .replaceAll("([0-9.]+)π", "$1*" + Math.PI);

        // Parse the t range
        String[] tRangeParts = tRangeString.split("<");
        if (tRangeParts.length != 3 || !tRangeParts[1].equals("t")) {
            throw new Exception("Invalid t range format. Expected format: tStart<t<tEnd.");
        }
        double tStart = evaluateExpression(tRangeParts[0].trim());
        double tEnd = evaluateExpression(tRangeParts[2].trim());

        // Supported trigonometric functions
        Map<String, Function<Double, Double>> trigFunctions = new HashMap<>();
        trigFunctions.put("sin", Math::sin);
        trigFunctions.put("cos", Math::cos);
        trigFunctions.put("tan", Math::tan);
        trigFunctions.put("sec", t -> 1 / Math.cos(t));
        trigFunctions.put("csc", t -> 1 / Math.sin(t));
        trigFunctions.put("cot", t -> 1 / Math.tan(t));

        // Parser for evaluating a single equation
        Function<String, Function<Double, Double>> parseEquation = equation -> {
            for (String func : trigFunctions.keySet()) {
                if (equation.contains(func)) {
                    String[] components = equation.split(func + "\\(");

                    // Parse the coefficient, default to 1 if missing
                    double coefficient = components[0].trim().isEmpty() ? 1 : Double.parseDouble(components[0].trim());

                    // Parse the inner coefficient, default to 1 if missing
                    double innerCoefficient = components[1].trim().isEmpty() || components[1].startsWith("t")
                            ? 1
                            : Double.parseDouble(components[1].replace("t)", "").trim());

                    Function<Double, Double> trigFunction = trigFunctions.get(func);
                    return t -> coefficient * trigFunction.apply(innerCoefficient * t);
                }
            }
            throw new IllegalArgumentException("Unsupported function in equation: " + equation);
        };

        // Parse x and y equations
        Function<Double, Double> xEvaluator = parseEquation.apply(xFunction);
        Function<Double, Double> yEvaluator = parseEquation.apply(yFunction);

        // Return the parametric grapher
        return new ParametricGrapher() {
            @Override
            public double tInterval() {
                return 0.1;
            }

            @Override
            public double tStart() {
                return tStart;
            }

            @Override
            public double tEnd() {
                return tEnd;
            }

            @Override
            public double xValue(double t) {
                return xEvaluator.apply(t);
            }

            @Override
            public double yValue(double t) {
                return yEvaluator.apply(t);
            }

            @Override
            public Coordinate xyStart() {
                return new Coordinate(xValue(tStart), yValue(tStart));
            }

            @Override
            public double xRange() {
                return tEnd - tStart;
            }

            @Override
            public double yRange() {
                return tEnd - tStart;
            }
        };
    }

    private double evaluateExpression(String expression) throws Exception {
        expression = expression.replace(" ", ""); // Remove whitespace
        if (expression.contains("*")) {
            String[] parts = expression.split("\\*");
            return Double.parseDouble(parts[0]) * Double.parseDouble(parts[1]);
        }
        return Double.parseDouble(expression); // No multiplication, just a number
    }

    interface EquationChangeListener {
        void onEquationChange();
        void onEquationRemove(EquationBox equationBox);
    }
}