import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class Board extends JFrame implements Runnable {
    // Configure board
    public static final int numRows = 5;
    public static final int numCols = 5;
    public static final int squareSize = 60;

    // Legal moves
    private static final int[] deltaRow = { 1, 1, -1, -1, 2, 2, -2, -2 };
    private static final int[] deltaCol = { 2, -2, 2, -2, 1, -1, 1, -1 };

    private static final int invalidStep = -1;

    private final ChessBoardPanel chessBoardPane = new ChessBoardPanel();
    private Knight knight;
    private Thread thread;
    private int[] path = new int[numRows * numCols - 1];
    private int initialRow;
    private int initialCol;

    public Board() {
        super("Ancient Game: Knight's Tour");

        Arrays.fill(path, invalidStep);

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

    @Override
    public void run() {
        boolean[][] board = new boolean[numRows][numCols]; // To keep track of the cells which are visited by the Knight
        board[knight.getRow()][knight.getCol()] = true; // Set the knight's initial position as visited
        repaint();
        solve(board, 0);
        thread = null;
    }

    private void startSolverThread(int row, int col) {
        if (Knight.isOnBoard(row, col) && thread == null) {
            // Set the initial conditions
            Arrays.fill(path, invalidStep);
            initialRow = row;
            initialCol = col;
            knight = new Knight(row, col);
            repaint();

            // Start the thread
            thread = new Thread(this);
            thread.start();
        }
    }

    private boolean solve(boolean[][] board, int depth) {
        // Return true if all cells are visited
        if (depth == numRows * numCols - 1) {
            repaint();
            return true;
        }

        sleep(200);

        int row = knight.getRow();
        int col = knight.getCol();
        int[] nextMoves = getNextMoves(board, row, col);

        // Count the number of possible moves for each of the next positions
        int[][] nextMovesWithOnwardMovesCount = new int[nextMoves.length][2];
        for (int i = 0; i < nextMoves.length; i++) {
            int nextMove = nextMoves[i];
            nextMovesWithOnwardMovesCount[i][0] = nextMove;
            nextMovesWithOnwardMovesCount[i][1] = countPossibleMoves(board, row + deltaRow[nextMove],
                    col + deltaCol[nextMove]);
        }

        // Sort the nextMovesWithOnwardMovesCount array by the number of onward moves
        // Reference: https://en.wikipedia.org/wiki/Knight%27s_tour#Warnsdorf%27s_rule
        Arrays.sort(nextMovesWithOnwardMovesCount, new Comparator<int[]>() {
            public int compare(int[] a, int[] b) {
                return Integer.compare(a[1], b[1]);
            }
        });

        // Loop through each of the possible next moves
        for (int i = 0; i < nextMovesWithOnwardMovesCount.length; i++) {
            int nextMove = nextMovesWithOnwardMovesCount[i][0];
            int changeR = deltaRow[nextMove];
            int changeC = deltaCol[nextMove];
            int nextRow = row + changeR;
            int nextCol = col + changeC;

            // Set next position as visited
            assert board[nextRow][nextCol] == false;
            board[nextRow][nextCol] = true;

            path[depth] = nextMove;
            knight.move(changeR, changeC);
            repaint();

            if (solve(board, depth + 1)) {
                return true;
            }

            // Revert the move if no solution found
            path[depth] = invalidStep;
            knight.move(-changeR, -changeC);
            repaint();
            board[nextRow][nextCol] = false;
        }

        return false;
    }

    private static int[] getNextMoves(boolean[][] board, int row, int col) {
        int index = 0;
        int[] nextMoves = new int[deltaRow.length];
        for (int i = 0; i < deltaRow.length; i++) {
            int newRow = row + deltaRow[i];
            int newCol = col + deltaCol[i];
            if (Knight.isOnBoard(newRow, newCol) && !board[newRow][newCol]) {
                nextMoves[index] = i;
                index++;
            }
        }

        int[] result = new int[index];
        System.arraycopy(nextMoves, 0, result, 0, index);
        return result;
    }

    private static int countPossibleMoves(boolean[][] board, int row, int col) {
        return getNextMoves(board, row, col).length;
    }

    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private class ChessBoardPanel extends JPanel {
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

            // Draw out the path
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(Color.GREEN);
            g2d.setStroke(new BasicStroke((float) squareSize / 20));
            int fromX = initialCol;
            int fromY = initialRow;
            int circleRadius = squareSize / 5;
            for (int i = 0; i < path.length; i++) {
                int deltaIndex = path[i];
                if (deltaIndex == invalidStep) {
                    break;
                }
                int toX = fromX + deltaCol[deltaIndex];
                int toY = fromY + deltaRow[deltaIndex];
                g2d.fillOval(squareSize / 2 + fromX * squareSize - circleRadius / 2,
                        squareSize / 2 + fromY * squareSize - circleRadius / 2, circleRadius,
                        circleRadius);
                g2d.drawLine(squareSize / 2 + fromX * squareSize, squareSize / 2 + fromY * squareSize,
                        squareSize / 2 + toX * squareSize, squareSize / 2 + toY * squareSize);
                fromX = toX;
                fromY = toY;
            }

            if (knight != null) {
                knight.draw(this, g);
            }
        }
    }

    public static void main(String[] args) {
        Board board = new Board();
        board.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
