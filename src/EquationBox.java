import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class EquationBox extends JPanel {
    private final JTextField equationField;
    private EquationChangeListener listener;
    private final JButton removeButton;
    private final JLabel statusCircle;
    private boolean isGraphed = true;
    private boolean isValidEquation = false;
    private final JPopupMenu colorMenu;
    private Color currentColor = Color.BLACK;

    public EquationBox() {
        setLayout(new FlowLayout(FlowLayout.LEFT));

        statusCircle = new JLabel("◯");
        statusCircle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        statusCircle.setForeground(Color.RED);
        add(statusCircle);

        equationField = new JTextField(15);
        add(equationField);

        removeButton = new JButton("❌");
        removeButton.addActionListener(e -> removeEquationBox());
        add(removeButton);

        colorMenu = new JPopupMenu();
        createColorMenu();

        addStatusCircleListeners();

        equationField.getDocument().addDocumentListener((SimpleDocumentListener) () -> {
            try {
                XYGrapher grapher = parseEquation(equationField.getText());
                isValidEquation = true;
                updateStatusCircleColor();
                if (listener != null) listener.onEquationChange();
            } catch (Exception e) {
                isValidEquation = false;
                if (listener != null) listener.onEquationChange();
                updateStatusCircleColor();
            }
        });
    }

    private void addStatusCircleListeners() {
        statusCircle.addMouseListener(new java.awt.event.MouseAdapter() {
            private Timer holdTimer;

            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                holdTimer = new Timer(500, e -> showColorMenu(evt.getXOnScreen(), evt.getYOnScreen()));
                holdTimer.setRepeats(false);
                holdTimer.start();
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                if (holdTimer != null && holdTimer.isRunning()) {
                    holdTimer.stop();
                    if (isValidEquation) {
                        toggleGraphingStatus();
                    }
                }
            }
        });
    }

    private void createColorMenu() {
        Color[] colors = {
                Color.BLACK,
                new Color(255, 87, 82), // Light Red
                new Color(65, 159, 255), // Light Blue
                new Color(86, 216, 108), // Light Green
                new Color(250, 126, 25), // Light Orange
                new Color(140, 96, 242) // Light Purple
        };
        for (Color color : colors) {
            JMenuItem menuItem = new JMenuItem(" ");
            menuItem.setBackground(color);
            menuItem.setOpaque(true);
            menuItem.addActionListener(e -> {
                currentColor = color;
                statusCircle.setForeground(currentColor);
                if (listener != null) listener.onEquationChange();
            });
            colorMenu.add(menuItem);
        }
    }

    private void showColorMenu(int x, int y) {
        colorMenu.show(this, x - this.getLocationOnScreen().x, y - this.getLocationOnScreen().y);
    }

    private void toggleGraphingStatus() {
        isGraphed = !isGraphed;
        updateStatusCircleColor();
        if (listener != null) listener.onEquationChange();
    }

    private void updateStatusCircleColor() {
        if (!isValidEquation) {
            statusCircle.setForeground(Color.RED);
            statusCircle.setText("◯");
        } else if (isGraphed) {
            statusCircle.setForeground(currentColor);
            statusCircle.setText("●");
        } else {
            statusCircle.setForeground(Color.BLACK);
            statusCircle.setText("◯");
        }
    }

    public void setEquationChangeListener(EquationChangeListener listener) {
        this.listener = listener;
    }

    public XYGrapher getGrapher() {
        if (isGraphed && isValidEquation) {
            try {
                XYGrapher grapher = parseEquation(equationField.getText());
                grapher.setGraphColor(currentColor);
                return grapher;
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

        if (cleanedEquation.isEmpty()) {
            throw new Exception("Invalid polynomial equation format. Equation cannot be empty.");
        }

        Map<Integer, Double> coefficientMap = getCoefficientMap(cleanedEquation);

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

                int exponent = 1;
                if (parts.length > 1 && parts[1].startsWith("^")) {
                    exponent = Integer.parseInt(parts[1].substring(1));
                }

                double coefficient = parts[0].isEmpty() || parts[0].equals("+") ? 1 :
                        parts[0].equals("-") ? -1 :
                                Double.parseDouble(parts[0]);

                coefficientMap.put(exponent, coefficientMap.getOrDefault(exponent, 0.0) + coefficient);

            } else if (!term.isEmpty()) {
                double constant = Double.parseDouble(term);
                coefficientMap.put(0, coefficientMap.getOrDefault(0, 0.0) + constant);
            }
        }
        return coefficientMap;
    }

    private XYGrapher parseParametricEquation(String cleanedEquation) throws Exception {
        String[] parts = cleanedEquation.split(";");
        if (parts.length != 3) {
            throw new Exception("Invalid parametric equation format.");
        }

        String xFunction = parts[0].split("=")[1].trim();
        String yFunction = parts[1].split("=")[1].trim();
        String tRangeString = parts[2].trim();

        tRangeString = tRangeString.replaceAll("(?<![0-9.])pi", String.valueOf(Math.PI))
                .replaceAll("(?<![0-9.])π", String.valueOf(Math.PI))
                .replaceAll("([0-9.]+)pi", "$1*" + Math.PI)
                .replaceAll("([0-9.]+)π", "$1*" + Math.PI);

        String[] tRangeParts = tRangeString.split("<");
        if (tRangeParts.length != 3 || !tRangeParts[1].equals("t")) {
            throw new Exception("Invalid t range format. Expected format: tStart<t<tEnd.");
        }
        double tStart = evaluateExpression(tRangeParts[0].trim());
        double tEnd = evaluateExpression(tRangeParts[2].trim());

        Map<String, Function<Double, Double>> trigFunctions = new HashMap<>();
        trigFunctions.put("sin", Math::sin);
        trigFunctions.put("cos", Math::cos);
        trigFunctions.put("tan", Math::tan);
        trigFunctions.put("sec", t -> 1 / Math.cos(t));
        trigFunctions.put("csc", t -> 1 / Math.sin(t));
        trigFunctions.put("cot", t -> 1 / Math.tan(t));

        Function<String, Function<Double, Double>> parseEquation = equation -> {
            for (String func : trigFunctions.keySet()) {
                if (equation.contains(func)) {
                    String[] components = equation.split(func + "\\(");

                    double coefficient = components[0].trim().isEmpty() ? 1 : Double.parseDouble(components[0].trim());

                    double innerCoefficient = components[1].trim().isEmpty() || components[1].startsWith("t")
                            ? 1
                            : Double.parseDouble(components[1].replace("t)", "").trim());

                    Function<Double, Double> trigFunction = trigFunctions.get(func);
                    return t -> coefficient * trigFunction.apply(innerCoefficient * t);
                }
            }
            throw new IllegalArgumentException("Unsupported function in equation: " + equation);
        };

        Function<Double, Double> xEvaluator = parseEquation.apply(xFunction);
        Function<Double, Double> yEvaluator = parseEquation.apply(yFunction);

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
        expression = expression.replace(" ", "");
        if (expression.contains("*")) {
            String[] parts = expression.split("\\*");
            return Double.parseDouble(parts[0]) * Double.parseDouble(parts[1]);
        }
        return Double.parseDouble(expression);
    }

    interface EquationChangeListener {
        void onEquationChange();
        void onEquationRemove(EquationBox equationBox);
    }
}