import javax.swing.*;
import java.awt.*;

class EquationBox extends JPanel {
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
        cleanedEquation = cleanedEquation.replace("y=", "");

        double[] coefficients = new double[11];

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

                if (exponent >= coefficients.length) {
                    throw new Exception("Exponent too large, increase array size.");
                }
                coefficients[exponent] += coefficient;

            } else {
                double constant = Double.parseDouble(term);
                coefficients[0] += constant;
            }
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

    private XYGrapher parseParametricEquation(String cleanedEquation) throws Exception {
        String[] parts = cleanedEquation.split(";");

        if (parts.length != 3) {
            throw new Exception("Invalid parametric equation format.");
        }

        String xFunction = parts[0].split("=")[1].trim();
        String yFunction = parts[1].split("=")[1].trim();
        String tRangeString = parts[2].trim();

        String[] tRangeParts = tRangeString.split("<");
        if (tRangeParts.length != 3) {
            throw new Exception("Invalid t range format. Expected format: tStart<t<tEnd.");
        }

        double tStart = Double.parseDouble(tRangeParts[0].trim());
        double tEnd = Double.parseDouble(tRangeParts[2].trim());

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
                return 2 * t;
            }

            @Override
            public double yValue(double t) {
                return 0.1 * t;
            }

            @Override
            public Coordinate xyStart() {
                return new Coordinate(tStart, xValue(tStart));
            }

            @Override
            public double xRange() {
                return tEnd - tStart;
            }

            @Override
            public double yRange() {
                return 2;
            }
        };
    }

    interface EquationChangeListener {
        void onEquationChange();
        void onEquationRemove(EquationBox equationBox);
    }
}