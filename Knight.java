import javax.swing.*;
import java.awt.*;

public class Knight {
    private static final int labelSize = Board.squareSize / 2;

    private int row;
    private int col;
    private final ImageIcon knightIcon;

    public Knight(int r, int c) {
        row = r;
        col = c;
        Image knightImage = new ImageIcon("knight.png").getImage().getScaledInstance(labelSize, labelSize,
                java.awt.Image.SCALE_SMOOTH);
        knightIcon = new ImageIcon(knightImage);

    }

    public static boolean isOnBoard(int r, int c) {
        return r >= 0 && r < Board.numRows && c >= 0 && c < Board.numCols;
    }

    public void draw(Container c, Graphics g) {
        knightIcon.paintIcon(c, g, labelSize / 2 + col * Board.squareSize,
                labelSize / 2 + row * Board.squareSize);
    }

    public boolean move(int deltaRow, int deltaCol) {
        if (isOnBoard(row + deltaRow, col + deltaCol)) {
            row += deltaRow;
            col += deltaCol;
            return true; // did move
        }
        return false; // did not move
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

}
