
import java.util.Arrays;
import java.util.Comparator;

public class Solver implements Runnable {
    // Legal moves
    private static final int[] deltaRow = { 1, 1, -1, -1, 2, 2, -2, -2 };
    private static final int[] deltaCol = { 2, -2, 2, -2, 1, -1, 1, -1 };

    private static final int invalidStep = -1;

    private final Config config;
    private final EventListener listener;

    public Solver(Config config, EventListener listener) {
        this.config = config;
        this.listener = listener;
    }

    @Override
    public void run() {
        boolean[][] board = new boolean[config.numRows][config.numCols]; // To keep track of the cells which are visited
                                                                         // by the Knight
        board[config.initialRow][config.initialCol] = true; // Set the knight's initial position as visited
        int[] steps = new int[config.numRows * config.numCols - 1];
        Arrays.fill(steps, invalidStep);
        solve(board, steps, config.initialRow, config.initialCol, 0);
        listener.onSolved();
    }

    private boolean solve(boolean[][] board, int[] steps, int row, int col, int depth) {
        // Return true if all cells are visited
        if (depth == config.numRows * config.numCols - 1) {
            return true;
        }

        sleep(200);

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

            steps[depth] = nextMove;
            listener.onPathUpdated(buildPathFromSteps(steps));

            if (solve(board, steps, nextRow, nextCol, depth + 1)) {
                return true;
            }

            // Revert the move if no solution found
            steps[depth] = invalidStep;
            board[nextRow][nextCol] = false;
            listener.onPathUpdated(buildPathFromSteps(steps));
        }

        return false;
    }

    private int[][] buildPathFromSteps(int[] steps) {
        int[][] path = new int[steps.length + 1][2];
        path[0][0] = config.initialRow;
        path[0][1] = config.initialCol;
        int numPoints = 1;
        for (int i = 0; i < steps.length; i++) {
            int deltaIndex = steps[i];
            if (deltaIndex == invalidStep) {
                break;
            }
            path[i + 1][0] = path[i][0] + deltaRow[deltaIndex];
            path[i + 1][1] = path[i][1] + deltaCol[deltaIndex];
            numPoints++;
        }
        return Arrays.copyOf(path, numPoints);
    }

    private int[] getNextMoves(boolean[][] board, int row, int col) {
        int index = 0;
        int[] nextMoves = new int[deltaRow.length];
        for (int i = 0; i < deltaRow.length; i++) {
            int newRow = row + deltaRow[i];
            int newCol = col + deltaCol[i];
            if (isOnBoard(newRow, newCol) && !board[newRow][newCol]) {
                nextMoves[index] = i;
                index++;
            }
        }

        int[] result = new int[index];
        System.arraycopy(nextMoves, 0, result, 0, index);
        return result;
    }

    private int countPossibleMoves(boolean[][] board, int row, int col) {
        return getNextMoves(board, row, col).length;
    }

    private boolean isOnBoard(int row, int col) {
        return row >= 0 && row < config.numRows && col >= 0 && col < config.numCols;
    }

    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public interface EventListener {
        public void onPathUpdated(int[][] path);

        public void onSolved();
    }
}
