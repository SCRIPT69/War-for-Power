package cz.cvut.fel.pjv.warforpower.controller;

import cz.cvut.fel.pjv.warforpower.model.game.Game;
import cz.cvut.fel.pjv.warforpower.model.players.Player;
import cz.cvut.fel.pjv.warforpower.model.tiles.BaseTile;
import cz.cvut.fel.pjv.warforpower.model.tiles.HexTile;
import cz.cvut.fel.pjv.warforpower.model.tiles.HexTileCoords;
import cz.cvut.fel.pjv.warforpower.model.tiles.OccupiableTile;
import cz.cvut.fel.pjv.warforpower.model.units.Unit;
import cz.cvut.fel.pjv.warforpower.view.game.TileHighlightType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Resolves tile highlights that should be shown
 * for the current unit selection state.
 */
public class SelectionTileHighlightResolver {
    private final Game game;

    /**
     * Creates resolver bound to the specified game instance.
     *
     * @param game current game
     */
    public SelectionTileHighlightResolver(Game game) {
        if (game == null) {
            throw new IllegalArgumentException("Game cannot be null.");
        }
        this.game = game;
    }

    /**
     * Resolves all tile highlights for the current selection state.
     * If no unit is selected, tutorial base highlight may be returned.
     *
     * @param unitSelection current unit selection
     * @param currentPlayer current player
     * @param currentRound current round
     * @return map of tile coordinates to highlight type
     */
    public Map<HexTileCoords, TileHighlightType> resolve(
            UnitSelection unitSelection,
            Player currentPlayer,
            int currentRound) {

        if (unitSelection == null) {
            throw new IllegalArgumentException("Unit selection cannot be null.");
        }
        if (currentPlayer == null) {
            throw new IllegalArgumentException("Current player cannot be null.");
        }

        Map<HexTileCoords, TileHighlightType> highlights = new HashMap<>();

        if (!unitSelection.hasSelection()) {
            applyTutorialBaseHighlightIfNeeded(highlights, currentPlayer, currentRound);
            return highlights;
        }

        List<Unit> selectedUnits = unitSelection.getSelectedUnits();

        if (selectedUnits.size() == 1) {
            applySingleUnitHighlights(highlights, selectedUnits.getFirst());
        } else if (selectedUnits.size() == 2) {
            applyDualUnitHighlights(highlights, selectedUnits.getFirst(), selectedUnits.get(1));
        }

        return highlights;
    }

    /**
     * Adds tutorial base highlight during the first round
     * when no unit is currently selected.
     *
     * @param highlights output highlight map
     * @param currentPlayer current player
     * @param currentRound current round
     */
    private void applyTutorialBaseHighlightIfNeeded(
            Map<HexTileCoords, TileHighlightType> highlights,
            Player currentPlayer,
            int currentRound) {

        if (currentRound != 1) {
            return;
        }

        List<BaseTile> currentPlayerBases = game.getGameMap().getBasesOfPlayer(currentPlayer);
        if (!currentPlayerBases.isEmpty()) {
            highlights.put(
                    currentPlayerBases.getFirst().getTileCoords(),
                    TileHighlightType.TUTORIAL_BASE
            );
        }
    }

    /**
     * Adds movement and attack highlights for one selected unit.
     *
     * @param highlights output highlight map
     * @param unit selected unit
     */
    private void applySingleUnitHighlights(
            Map<HexTileCoords, TileHighlightType> highlights,
            Unit unit) {

        for (OccupiableTile tile : game.getMovementOptions(unit)) {
            highlights.put(tile.getTileCoords(), TileHighlightType.MOVE);
        }

        for (HexTile tile : game.getAttackOptions(unit)) {
            highlights.put(tile.getTileCoords(), TileHighlightType.ATTACK);
        }
    }

    /**
     * Adds movement and attack highlights shared by two selected units.
     *
     * @param highlights output highlight map
     * @param first first selected unit
     * @param second second selected unit
     */
    private void applyDualUnitHighlights(
            Map<HexTileCoords, TileHighlightType> highlights,
            Unit first,
            Unit second) {

        for (OccupiableTile tile : game.getSharedMovementOptions(first, second)) {
            highlights.put(tile.getTileCoords(), TileHighlightType.MOVE);
        }

        for (HexTile tile : game.getSharedAttackOptions(first, second)) {
            highlights.put(tile.getTileCoords(), TileHighlightType.ATTACK);
        }
    }
}