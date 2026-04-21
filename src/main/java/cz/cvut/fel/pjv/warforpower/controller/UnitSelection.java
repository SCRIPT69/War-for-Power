package cz.cvut.fel.pjv.warforpower.controller;

import cz.cvut.fel.pjv.warforpower.model.units.Unit;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores currently selected units.
 * Supports selection of up to two units at the same time.
 */
public class UnitSelection {
    private final List<Unit> selectedUnits = new ArrayList<>(2);

    /**
     * Clears current unit selection.
     */
    public void clear() {
        selectedUnits.clear();
    }

    /**
     * Returns true if at least one unit is selected.
     *
     * @return true if selection is not empty
     */
    public boolean hasSelection() {
        return !selectedUnits.isEmpty();
    }

    /**
     * Returns number of currently selected units.
     *
     * @return selection size
     */
    public int size() {
        return selectedUnits.size();
    }

    /**
     * Returns immutable snapshot of selected units.
     *
     * @return selected units
     */
    public List<Unit> getSelectedUnits() {
        return List.copyOf(selectedUnits);
    }

    /**
     * Returns the first selected unit.
     *
     * @return first selected unit
     * @throws IllegalStateException if no unit is selected
     */
    public Unit getFirstSelectedUnit() {
        if (selectedUnits.isEmpty()) {
            throw new IllegalStateException("No unit is currently selected.");
        }
        return selectedUnits.getFirst();
    }

    /**
     * Selects exactly one unit and clears any previous selection.
     *
     * @param unit unit to select
     */
    public void selectSingle(Unit unit) {
        if (unit == null) {
            throw new IllegalArgumentException("Unit cannot be null.");
        }

        selectedUnits.clear();
        selectedUnits.add(unit);
    }

    /**
     * Selects exactly two units and clears any previous selection.
     *
     * @param first first unit
     * @param second second unit
     */
    public void selectPair(Unit first, Unit second) {
        if (first == null || second == null) {
            throw new IllegalArgumentException("Selected units cannot be null.");
        }

        selectedUnits.clear();
        selectedUnits.add(first);
        selectedUnits.add(second);
    }

    /**
     * Returns true if the specified unit is already selected.
     *
     * @param unit unit to test
     * @return true if the unit is selected
     */
    public boolean contains(Unit unit) {
        return selectedUnits.contains(unit);
    }
}