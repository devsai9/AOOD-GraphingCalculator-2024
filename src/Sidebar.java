import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

class Sidebar extends JPanel {
    private final JPanel equationList;
    private final ArrayList<EquationBox> equationBoxes;
    private EquationListener listener;

    public Sidebar() {
        setLayout(new BorderLayout());

        equationList = new JPanel();
        equationList.setLayout(new BoxLayout(equationList, BoxLayout.Y_AXIS));

        equationBoxes = new ArrayList<>();

        addEquationBox();

        JScrollPane scrollPane = new JScrollPane(equationList);
        add(scrollPane, BorderLayout.CENTER);

        JButton addButton = new JButton("+");
        addButton.addActionListener(e -> addEquationBox());
        add(addButton, BorderLayout.SOUTH);
    }

    public void setEquationListener(EquationListener listener) {
        this.listener = listener;
    }

    private void addEquationBox() {
        EquationBox equationBox = new EquationBox();
        equationBox.setPreferredSize(new Dimension(245, 30));
        equationBox.setMaximumSize(new Dimension(245, 30));
        equationBox.setMinimumSize(new Dimension(245, 30));
        equationBoxes.add(equationBox);
        equationList.add(equationBox);

        equationBox.setEquationChangeListener(new EquationBox.EquationChangeListener() {
            @Override
            public void onEquationChange() {
                notifyEquationListener();
            }

            @Override
            public void onEquationRemove(EquationBox equationBox) {
                removeEquationBox(equationBox);
            }
        });

        equationList.revalidate();
        equationList.repaint();
    }

    private void removeEquationBox(EquationBox equationBox) {
        equationBoxes.remove(equationBox);
        equationList.remove(equationBox);
        equationList.revalidate();
        equationList.repaint();
        notifyEquationListener();
    }

    private void notifyEquationListener() {
        if (listener != null) {
            ArrayList<XYGrapher> graphers = new ArrayList<>();
            for (EquationBox box : equationBoxes) {
                XYGrapher grapher = box.getGrapher();
                if (grapher != null) {
                    graphers.add(grapher);
                }
            }
            listener.onEquationsUpdated(graphers);
        }
    }
}
