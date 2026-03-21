package cz.cvut.fel.pjv.warforpower.view.game;


public class GameFieldPositionGenerator {
    private static final double START_X = 202;
    private static final double START_Y = 132;
    private static final double TILE_STEP_X = 81;
    private static final double ROW_STEP_Y = 69.5;
    private static final double ROW_OFFSET_X = 40.5;

    private static final int CENTER_ROW_INDEX = 4;


    public double generatePosXForTile(int rowIndex, int tileIndex) {
        int rowDepth = CENTER_ROW_INDEX - Math.abs(CENTER_ROW_INDEX - rowIndex);
        double differenceX = rowDepth * ROW_OFFSET_X;

        return START_X - differenceX + TILE_STEP_X *tileIndex;
    }

    public double generatePosYForTile(int rowIndex) {
        return START_Y + rowIndex*ROW_STEP_Y;
    }
}