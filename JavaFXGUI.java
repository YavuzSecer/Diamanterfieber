package gui;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.util.Duration;
import logic.*;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

import java.util.List;

/**
 *
 * @author Yavuz Secer, TINF100837
 */
public class JavaFXGUI implements GUIConnector {

    /**
     * Displays the actual Level
     */
    private final Label lvlLbl;

    /**
     * Displays the actual score
     */
    private final Label scoreLbl;

    /**
     * Displays the actual remaining moves
     */
    private final Label movesLbl;

    /**
     * Displays the Quest as a String
     */
    private final Label questLbl;

    /**
     * GridPane for the gamBoard
     */
    private final GridPane boardGPane;

    /**
     * Pre Image Path to the Stone Images
     */
    private final static String preImagePath = "gui/img/stoneImages/";



    /**
     * Constructor for JavaFXGUI
     */
    public JavaFXGUI(Label lvlLbl,
                     Label scoreLbl,
                     Label movesLbl,
                     Label questLbl,
                     GridPane board) {

        this.lvlLbl = lvlLbl;
        this.scoreLbl = scoreLbl;
        this.movesLbl = movesLbl;
        this.questLbl = questLbl;
        this.boardGPane = board;
    }

    /**
     * Shows the actual Level Number
     * @param lvlNo
     */
    public void showLevelNo(int lvlNo) {
        this.lvlLbl.setText(String.valueOf(lvlNo));
    }

    /**
     * Shows the actual Score
     * @param score
     */
    public void showScore(int score) {
        this.scoreLbl.setText(String.valueOf(score));
    }

    /**
     * Shows the actual remaining moves
     * @param moves
     */
    public void showMoves(int moves) {
        this.movesLbl.setText(String.valueOf(moves));
    }

    /**
     * Shows the Quest as a String
     * @param quest
     */
    public void showQuest(String quest) {
        this.questLbl.setText(quest);
    }

    /**
     * loads the Images for every generated Stone in the gameBoard
     * @param gameField
     */
    public void showStoneImages(GameField gameField) {

        for (int row = 0; row < LogConst.BOARD_ROWS; row++) {
            for (int col = 0; col < LogConst.BOARD_COLS; col++) {
                int gpNodeIdx = (row * GameLogic.ROW_CNT) + col;
                ImageView iv = (ImageView) this.boardGPane.getChildren().get(gpNodeIdx);
                iv.setImage(new Image(preImagePath
                        + gameField.getStoneAt(new Coords(row, col)).getStoneToken() + ".png"));
            }
        }
    }

    /**
     * Gets the first node found at cell col/row.
     * @param gridPane GridPane to look in
     * @param col      column to look at
     * @param row      row to look at
     * @return node that belongs to cell col/row
     */
    public static Node getNodeFromGridPane(GridPane gridPane, int col, int row) {
        Node foundNode = null;
        for (Node node : gridPane.getChildren()) {
            if (GridPane.getColumnIndex(node) == col && GridPane.getRowIndex(node) == row) {
                foundNode = node;
            }
        }
        return foundNode;
    }

    /**
     * Removes the imageviews from the cells col/row and upwards.
     * @param col      column to work in
     * @param row      row to start at
     * @param toDelete number of imageviews to delete
     */
    private void removeImageViewsFromCells(int col, int row, int toDelete) {
        for (int i = 0; i < toDelete; i++) {
            ImageView iv = (ImageView) gui.JavaFXGUI.getNodeFromGridPane(boardGPane, col, row - i);
            boardGPane.getChildren().remove(iv);
        }
    }

    /**
     * Adds imageview to given cell of gridpane and binding its size to
     * gridpanes cellsize.
     * @param gridPane gridpane to add the image to
     * @param iv       imageview to add
     * @param col      column to add in
     * @param row      row to add in
     */
    public static void addImageViewToPane(GridPane gridPane, ImageView iv, int col, int row) {
        gridPane.add(iv, col, row);
        iv.setPreserveRatio(false);
        int cols = gridPane.getRowConstraints().size();
        int rows = gridPane.getRowConstraints().size();
        iv.fitWidthProperty().bind(gridPane.widthProperty().divide(cols));
        iv.fitHeightProperty().bind(gridPane.heightProperty().divide(rows));
    }

    /**
     * Adds imageview to the gridPane in this class.
     * @param iv  imageview to add
     * @param col column of the cell to add to
     * @param row row of the cell to add to
     */
    private void addImageViewToPane(ImageView iv, int col, int row) {
        addImageViewToPane(boardGPane, iv, col, row);
    }

    public void updateGui(AnimationData aData) {
        final int rowHeight = (int) (boardGPane.getHeight() / boardGPane.getRowConstraints().size());
        Coords switchSourceCoords = aData.getSwitchSourceCoords();
        Coords switchTargetCoords = aData.getSwitchTargetCoords();

        // Apply player move
        ParallelTransition switchAnimation = switchStones(switchSourceCoords, switchTargetCoords);
        switchAnimation.play();
        // Revert switch, if the move was invalid
        if (aData.geteData().isEmpty()) {
            switchAnimation.setOnFinished(event -> {
                ParallelTransition switchBackAnimation = switchStones(switchSourceCoords, switchTargetCoords);
                switchBackAnimation.play();
            });
        } else {
            switchAnimation.setOnFinished(event -> {
                // Animate explosions for every found Structure
                for (ExplosionData eData : aData.geteData()) {
                    SequentialTransition explosionAnimation = new SequentialTransition();
                    // Coordinates of where the bonusStone appears
                    Coords bonusSource = eData.getBonusSourceCoords();
                    // Coordinates of where the bonusStone need to be repositioned
                    Coords bonusTarget = eData.getBonusTargetCoords();

                    // Remove all Structure elements and make Stones above drop to their target
                    // positions. Also translate them back to the same position for the animation
                    removeStructureAndReplaceIvs(eData, bonusTarget, bonusSource, rowHeight);
                    // This shall only proceed if the animation involves handeling a bonusStone
                    if (bonusSource != null && bonusTarget != null) {
                        int rowsToMove = bonusTarget.getRow() - bonusSource.getRow();
                        ImageView bonusIv = (ImageView) gui.JavaFXGUI.getNodeFromGridPane(boardGPane, bonusTarget.getCol(), bonusTarget.getRow());
                        // BonusStone shall fade in at the source Position
                        explosionAnimation = bonusStoneFadeIn(explosionAnimation, rowsToMove, bonusIv, rowHeight);
                        // Translate to targetPosition, if sourcePosition is not equal to targetPosition
                        explosionAnimation = bonusStoneMoveToTargetCoords(explosionAnimation, rowsToMove, bonusIv, rowHeight);
                    }
                    // Make the Stone ImageViews translate from their origin position to their new target positions
                    explosionAnimation = dropAndFillUpEmptySpace(explosionAnimation, eData, bonusTarget, bonusSource, rowHeight);
                    explosionAnimation.play();
                }
            });
        }
    }

    private ParallelTransition switchStones(Coords source, Coords target) {

        final int rowHeight = (int) (boardGPane.getHeight() / boardGPane.getRowConstraints().size());
        final int colWidth = (int) (boardGPane.getWidth() / boardGPane.getColumnConstraints().size());

        int sCol = source.getCol();
        int sRow = source.getRow();
        int tCol = target.getCol();
        int tRow = target.getRow();

        ImageView sourceIv = (ImageView) gui.JavaFXGUI.getNodeFromGridPane(boardGPane, sCol, sRow);
        ImageView targetIv = (ImageView) gui.JavaFXGUI.getNodeFromGridPane(boardGPane, tCol, tRow);

        removeImageViewsFromCells(tCol, tRow, 1);
        removeImageViewsFromCells(sCol, sRow, 1);

        GridPane.setConstraints(targetIv, sCol, sRow);
        GridPane.setConstraints(sourceIv, tCol, tRow);

        addImageViewToPane(sourceIv, tCol, tRow);
        addImageViewToPane(targetIv, sCol, sRow);

        ParallelTransition pt = new ParallelTransition();
        if (sCol == tCol && sRow > tRow) {                  // source up, target down
            pt.getChildren().add(verticalTransition(rowHeight, sourceIv));
            pt.getChildren().add(verticalTransition(-rowHeight, targetIv));
        }
        else if (sCol == tCol && sRow < tRow) {             // source sown, target up
            pt.getChildren().add(verticalTransition(-rowHeight, sourceIv));
            pt.getChildren().add(verticalTransition(rowHeight, targetIv));
        }
        else if (sRow == tRow && sCol > tCol) {             // source left, target right
            pt.getChildren().add(horizontalTransition(colWidth, sourceIv));
            pt.getChildren().add(horizontalTransition(-colWidth, targetIv));
        }
        else if (sRow == tRow && sCol < tCol) {             // source right, target left
            pt.getChildren().add(horizontalTransition(-colWidth, sourceIv));
            pt.getChildren().add(horizontalTransition(colWidth, targetIv));
        }
        return pt;
    }

    private void removeStructureAndReplaceIvs(ExplosionData eData,
                                              Coords bonusTargetCoords,
                                              Coords bonusSourceCoords,
                                              final int rowHeight) {
        // Removing the Structure and all stones above by deleting the ImageViews col by col
        for (DropInfo info : eData.getExplosionInfo()) {
            // Coordinates of the Structure element that is going to be removed in this col
            int col = info.getCoords().getCol();
            int row = info.getCoords().getRow();
            // If a bonusStone will apear, the heightOffset gets reduced by one
            int offset = getAppropiateOffset(bonusTargetCoords, info, col);

            // Remove the Structure and all ImageViews above
            removeImageViewsFromCells(col, row, row + 1);

            List<String> stoneToken = info.getFallingStoneToken();
            for (int r = row, i = 0; r >= 0; --r, ++i) {
                // Fill up removed Cells with new ImageViews values
                ImageView newIv = new ImageView(new Image(preImagePath + stoneToken.get(i) + ".png"));
                // Place each iv to their target Coords
                addImageViewToPane(newIv, col, r);
                // Translate all non-bonusStones to the position they were placed before
                if (ignoreBonusTargetCoordinates(bonusTargetCoords, bonusSourceCoords, r, col)) {
                    newIv.setTranslateY(-rowHeight * offset);
                }
            }
        }
    }

    // If the removed Structure results to generate a bonusStone, make it fade in at source position
    private SequentialTransition bonusStoneFadeIn(SequentialTransition explosionAnimation,
                                                  int sourceToTargetDiff,
                                                  ImageView bonusIv,
                                                  final int rowHeight) {
        FadeTransition bonusFadeIn = new FadeTransition(Duration.seconds(1), bonusIv);
        bonusFadeIn.setFromValue(0f);
        bonusFadeIn.setToValue(1f);
        // If the target Position is not the same, place it to target and translate to source position
        if (sourceToTargetDiff > 0) {
            bonusIv.setTranslateY(-rowHeight * sourceToTargetDiff);
        }

        explosionAnimation.getChildren().add(bonusFadeIn);
        return explosionAnimation;
    }

    // If the bonusStone must be moved from source Coordinates to target Coordinates
    private SequentialTransition bonusStoneMoveToTargetCoords(SequentialTransition explosionAnimation,
                                                              int sourceToTargetDiff,
                                                              ImageView bonusIv,
                                                              final int rowHeight) {
        // Difference in row from bonusSourceCoordinates to bonusTargetCoordinates
        if (sourceToTargetDiff > 0) {
            TranslateTransition moveToTargetCoords = new TranslateTransition(Duration.seconds(1), bonusIv);
            moveToTargetCoords.fromYProperty().set(-rowHeight * sourceToTargetDiff);
            moveToTargetCoords.toYProperty().set(0);
            explosionAnimation.getChildren().add(moveToTargetCoords);
        }
        return explosionAnimation;
    }

    private SequentialTransition dropAndFillUpEmptySpace(SequentialTransition explosionAnimation,
                                                         ExplosionData eData,
                                                         Coords bonusTargetCoords,
                                                         Coords bonusSourceCoords,
                                                         final int rowHeight) {
        ParallelTransition animateDrop = new ParallelTransition();
        for (int i = 0; i < eData.getExplosionInfo().size(); i++) {
            // List of all stoneToken to create respective ImageViews for each col
            List<DropInfo> allDropInfo = eData.getExplosionInfo();
            int col = allDropInfo.get(i).getCoords().getCol();
            int row = allDropInfo.get(i).getCoords().getRow();
            // If a bonusStone will apear, the heightOffset gets reduced by one
            int offset = getAppropiateOffset(bonusTargetCoords, allDropInfo.get(i), col);

            for (int r = row; r >= 0; --r) {
                // Drop all Stones above the removed Structure to fill up the empty space
                // Ignore possible bonusStones since they are being animated seperately
                if (ignoreBonusTargetCoordinates(bonusTargetCoords, bonusSourceCoords, r, col)) {
                    ImageView iv = (ImageView) gui.JavaFXGUI.getNodeFromGridPane(boardGPane, col, r);
                    TranslateTransition tt = new TranslateTransition(Duration.millis(1500), iv);
                    tt.fromYProperty().set(-rowHeight * offset);
                    tt.toYProperty().set(0);
                    animateDrop.getChildren().add(tt);
                }
            }

        }
        explosionAnimation.getChildren().add(animateDrop);
        return explosionAnimation;
    }

    private int getAppropiateOffset(Coords bonusTargetCoords, DropInfo dropInfo, int col) {
        int bonusOffset = (bonusTargetCoords != null && col == bonusTargetCoords.getCol()) ? 1 : 0;
        return dropInfo.getHeightOffset() - bonusOffset;
    }

    private boolean ignoreBonusTargetCoordinates(Coords bonusTargetCoords,
                                                 Coords bonusSourceCoords,
                                                 int row,
                                                 int col) {
        return bonusSourceCoords == null
                || bonusTargetCoords != null && col != bonusTargetCoords.getCol()
                || bonusTargetCoords != null && row != bonusTargetCoords.getRow();
    }

    private TranslateTransition horizontalTransition(int factor1, ImageView iv) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(1500), iv);
        tt.fromXProperty().set(factor1);
        tt.toXProperty().set(0);
        return tt;
    }

    private TranslateTransition verticalTransition(int factor1, ImageView iv) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(1500), iv);
        tt.fromYProperty().set(factor1);
        tt.toYProperty().set(0);
        return tt;
    }
}
