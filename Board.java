
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Board extends JFrame {
    // Configure board
    private static final int numRows = 8;
    private static final int numCols = 8;
    private static final int squareSize = 60;

    private final ChessBoardPanel chessBoardPane = new ChessBoardPanel();
    private Solver solver;
    private int[][] path;

    public Board() {
        super("Ancient Game: Knight's Tour");

        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(chessBoardPane, BorderLayout.NORTH);

        chessBoardPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = Math.floorDiv(e.getY(), squareSize);
                int col = Math.floorDiv(e.getX(), squareSize);
                startSolverThread(row, col);
            }
        });

        setResizable(false);
        setSize(numCols * squareSize, numRows * squareSize + 30);
        setVisible(true);
    }

    private void startSolverThread(int row, int col) {
        if (solver != null) {
            return;
        }

        solver = new Solver(new Config(numRows, numCols, row, col), new Solver.EventListener() {

            @Override
            public void onPathUpdated(int[][] newPath) {
                path = newPath;
                repaint();
            }

            @Override
            public void onSolved() {
                solver = null;
            }

        });

        new Thread(solver).start();
    }

    private class ChessBoardPanel extends JPanel {
        private static final int labelSize = Board.squareSize / 2;
        private final ImageIcon knightIcon;

        public ChessBoardPanel() {
            Image knightImage = new ImageIcon("resources/knight.png").getImage().getScaledInstance(labelSize, labelSize,
                    java.awt.Image.SCALE_SMOOTH);
            knightIcon = new ImageIcon(knightImage);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(numCols * squareSize, numRows * squareSize);
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            // Draw out the chess board
            for (int row = 0; row < numRows; row++) {
                for (int col = 0; col < numCols; col++) {
                    g.drawRect(col * squareSize, row * squareSize, squareSize, squareSize);
                    if ((row + col) % 2 == 0) {
                        g.setColor(Color.WHITE);
                    } else {
                        g.setColor(Color.BLACK);
                    }
                    g.fillRect(col * squareSize, row * squareSize, squareSize, squareSize);
                }
            }

            if (path == null) {
                return;
            }

            // Draw out the path
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(Color.GREEN);
            g2d.setStroke(new BasicStroke((float) squareSize / 20));
            int circleRadius = squareSize / 5;
            for (int i = 1; i < path.length; i++) {
                int fromX = path[i - 1][1];
                int fromY = path[i - 1][0];
                int toX = path[i][1];
                int toY = path[i][0];

                g2d.fillOval(squareSize / 2 + fromX * squareSize - circleRadius / 2,
                        squareSize / 2 + fromY * squareSize - circleRadius / 2, circleRadius,
                        circleRadius);

                g2d.drawLine(squareSize / 2 + fromX * squareSize, squareSize / 2 + fromY * squareSize,
                        squareSize / 2 + toX * squareSize, squareSize / 2 + toY * squareSize);
            }

            if (path.length > 0) {
                knightIcon.paintIcon(this, g, path[path.length - 1][1] * squareSize + labelSize / 2,
                        path[path.length - 1][0] * squareSize + labelSize / 2);
            }
        }
    }

    public static void main(String[] args) {
        Board board = new Board();
        board.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
