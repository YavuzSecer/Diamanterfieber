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
 * @author Yavuz
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
            ImageView iv = (ImageView) JavaFXGUI.getNodeFromGridPane(boardGPane, col, row - i);
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

    private ParallelTransition switchStones(Coords source, Coords target, AnimationData aData) {

        final int rowHeight = (int) (boardGPane.getHeight() / boardGPane.getRowConstraints().size());
        final int colWidth = (int) (boardGPane.getWidth() / boardGPane.getColumnConstraints().size());

        int sCol = source.getCol();
        int sRow = source.getRow();
        int tCol = target.getCol();
        int tRow = target.getRow();

        ImageView sourceIv = (ImageView) JavaFXGUI.getNodeFromGridPane(boardGPane, sCol, sRow);
        ImageView targetIv = (ImageView) JavaFXGUI.getNodeFromGridPane(boardGPane, tCol, tRow);

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

        if (!aData.geteData().isEmpty()) {
            pt.setOnFinished(event -> {
                ExplosionData explosionData = aData.geteData().get(0);
                for (DataPerColumn dropInfo : explosionData.getExplosionInfo()) {
                    for (int i = 0; i < dropInfo.getHeightOffset(); i++) {
                        removeImageViewsFromCells(dropInfo.getCoords().getCol(), dropInfo.getCoords().getRow() - i, 1);
                    }
                }
            });
        }
        return pt;
    }

    public void updateGui(AnimationData aData) {
        final int rowHeight = (int) (boardGPane.getHeight() / boardGPane.getRowConstraints().size());
        // Apply player move
        ParallelTransition switchAnimation = animateSwitch(aData);
        // If move was not valid, move back
        switchAnimation.setOnFinished(event -> {
            if (aData.geteData().isEmpty()) {
                Coords source = aData.getSwitchSourceCoords();
                Coords target = aData.getSwitchTargetCoords();
                ParallelTransition switchBackAnimation = switchStones(source, target, aData);
                switchBackAnimation.play();
            }

            SequentialTransition explosionAnimation = new SequentialTransition();
            for (ExplosionData eData : aData.geteData()) {
                ParallelTransition structureRemoveAnimation = removeStructure(eData);
                structureRemoveAnimation.setOnFinished(event1 -> {
                    SequentialTransition bonusAppearanceAnimation = animateBonusAppearance(eData, rowHeight);
                    bonusAppearanceAnimation.setOnFinished(event2 -> {
                        ParallelTransition stoneDropAnimation = animateDrop(eData, rowHeight);
                        stoneDropAnimation.setOnFinished(event3 -> explosionAnimation.getChildren().add(structureRemoveAnimation));
                        stoneDropAnimation.play();
                    });
                    bonusAppearanceAnimation.play();
                });
                structureRemoveAnimation.play();
            }
            explosionAnimation.play();
        });
        switchAnimation.play();
    }

    private ParallelTransition removeStructure(ExplosionData eData) {
        ParallelTransition pt = new ParallelTransition();
        // Pause before the structure is about to be removed
        PauseTransition pause = new PauseTransition(Duration.millis(300));
        pause.setOnFinished(event -> {
            // Remove the structure ImageViews
            for (DataPerColumn info : eData.getExplosionInfo()) {
                removeImageViewsFromCells(info.getCoords().getCol(), info.getCoords().getRow(), info.getHeightOffset());
            }
        });
        pt.getChildren().add(pause);
        return pt;
    }

    private ParallelTransition animateSwitch(AnimationData aData) {
        Coords source = aData.getSwitchSourceCoords();
        Coords target = aData.getSwitchTargetCoords();
        return switchStones(source, target, aData);
    }

    private SequentialTransition animateBonusAppearance(ExplosionData eData, final int rowHeight) {
        Coords source = eData.getBonusSourceCoords();
        Coords target = eData.getBonusTargetCoords();
        SequentialTransition sq = new SequentialTransition();

        if (eData.getBonusSourceCoords() != null && eData.getBonusTargetCoords() != null) {
            int offset = target.getRow() - source.getRow();
            String bonusToken = eData.getBonusToken();
            ImageView iv = new ImageView(new Image(preImagePath + bonusToken + ".png"));
            addImageViewToPane(iv, target.getCol(), target.getRow());
            iv.setTranslateY(-rowHeight * offset);

            FadeTransition ft = new FadeTransition(Duration.millis(1000), iv);
            ft.setFromValue(0);
            ft.setToValue(1.0);
            sq.getChildren().add(ft);
            if (offset > 0) {
                iv.setTranslateY(-(rowHeight * offset));
                TranslateTransition tt = new TranslateTransition(Duration.millis(1000), iv);
                tt.fromYProperty().set(-(rowHeight * offset));
                tt.toYProperty().set(0);
                sq.getChildren().add(tt);
            }
        }
        return sq;
    }

    private ParallelTransition animateDrop(ExplosionData eData, final int rowHeight) {
        ParallelTransition allMovements = new ParallelTransition();
        for (int i = 0; i < eData.getExplosionInfo().size(); i++) {
            DataPerColumn dropInfo = eData.getExplosionInfo().get(i);
            int col = dropInfo.getCoords().getCol();
            int row = dropInfo.getCoords().getRow();
            int heightOffset = dropInfo.getHeightOffset();
            int bonusOffset = (eData.getBonusTargetCoords() != null && eData.getBonusTargetCoords().getCol() == col) ? 1 : 0;
            int newRow = row - bonusOffset;

            List<String> allValues = eData.getExplosionInfo().get(i).getFallingStoneToken();
            //System.out.println("[" + row + "/" + col + "] :\n" + allValues.toString());
            removeImageViewsFromCells(col, newRow, newRow + 1);

            ImageView[] newIvs = createNewIvsAndSetThemToTheirTargetCells(col, newRow, heightOffset - bonusOffset, allValues, rowHeight);
            allMovements = addCreatedTransitions(newIvs, heightOffset - bonusOffset, rowHeight, allMovements);
        }
        return allMovements;
    }

    private ImageView[] createNewIvsAndSetThemToTheirTargetCells(int col, int row, int heightOffset, List<String> newValues, final int rowHeight) {
        ImageView[] newIvs = new ImageView[newValues.size()];
        for (int i = 0; i < newValues.size(); i++) {
            Image img = new Image(preImagePath + newValues.get(i) + ".png");
            newIvs[i] = new ImageView(img);
            addImageViewToPane(newIvs[i], col, row - i);
            newIvs[i].setTranslateY(-rowHeight * heightOffset);
        }
        return newIvs;
    }

    private ParallelTransition addCreatedTransitions(ImageView[] ivs,
                                                     int rowsToMove,
                                                     final int rowHeight,
                                                     ParallelTransition allMovements) {
        for (ImageView iv : ivs) { //drop each given image
            //the x-Position has to be set for the translation
            //the translation may start above the visible column, it ends at the targetcell

            TranslateTransition tt = new TranslateTransition(Duration.millis(1500), iv);
            tt.fromYProperty().set(-(rowHeight * rowsToMove));
            tt.toYProperty().set(0);

            //add the tt-Movement to the parallel-transistion of all movements
            allMovements.getChildren().add(tt);
        }
        return allMovements;
    }

}
