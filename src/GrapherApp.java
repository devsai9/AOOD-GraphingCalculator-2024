import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class GrapherApp extends JFrame implements EquationListener {

    private final GraphPanel graphPanel;

    public GrapherApp() {
        setTitle("Graphing Calculator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        Sidebar sidebar = new Sidebar();
        sidebar.setEquationListener(this);
        add(sidebar, BorderLayout.WEST);

        graphPanel = new GraphPanel();
        graphPanel.setPreferredSize(new Dimension(600, 400));
        add(graphPanel, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    @Override
    public void onEquationsUpdated(ArrayList<XYGrapher> updatedGraphers) {
        graphPanel.updateGraph(updatedGraphers.toArray(new XYGrapher[0]), new Coordinate(-10, -10), 20, 20);
    }
}

class GraphPanel extends JPanel {
    private XYGrapher[] graphers;
    private Coordinate start;
    private double xRange;
    private double yRange;

    public void updateGraph(XYGrapher[] graphers, Coordinate start, double xRange, double yRange) {
        this.graphers = graphers;
        this.start = start;
        this.xRange = xRange;
        this.yRange = yRange;

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (graphers == null || start == null) return;

        GrapherUtils.drawGraph(g, graphers, start, xRange, yRange, 0, 0, getWidth(), getHeight());
    }
}