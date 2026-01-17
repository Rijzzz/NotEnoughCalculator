package com.rijz.notenoughcalculator.client;

import com.rijz.notenoughcalculator.config.CalculatorConfig;
import com.rijz.notenoughcalculator.core.ExpressionEvaluator;
import com.rijz.notenoughcalculator.core.ResultFormatter;
import me.shedaniel.rei.api.client.REIRuntime;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Manages calculator state and handles live calculation display in REI search bar.
 *
 * Responsibilities:
 * - Real-time expression evaluation as user types
 * - Equation-level history for Ctrl+Z/Y navigation (whole equations, not keystrokes)
 * - Completed calculation history for /calchist command
 * - Detecting whether input is a calculation vs item search
 */
public class CalculatorManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(CalculatorManager.class);

    // Hardcoded limit: store max 15 equations
    private static final int MAX_EQUATIONS = 15;

    private final ExpressionEvaluator evaluator;
    private String lastSearchInput = "";
    private String lastFormattedResult = null;
    private String lastErrorMessage = null;

    // Two separate history systems:
    // - reiSearchHistory: completed equations for Ctrl+Z/Y (e.g., "100+50", "200*3")
    // - completedHistory: finished calculations for /calchist display
    private final List<String> reiSearchHistory = new ArrayList<>();
    private final List<String> completedHistory = new ArrayList<>();
    private int reiHistoryIndex = -1;  // -1 means not navigating history
    private String savedCurrentInput = "";  // What user was typing before first Ctrl+Z

    // Track the current equation being typed (to know when to save it)
    private String currentEquation = "";

    // Track uncommitted calculations (shown in real-time but not saved yet)
    private String lastCompletedExpression = null;
    private BigDecimal lastCompletedResult = null;
    private boolean hasUncommittedCalculation = false;

    private boolean sessionResetNotified = false;

    // Precompiled regex patterns (compiled once for performance)
    private static final Pattern OPERATOR_PATTERN = Pattern.compile(".*[+\\-*/^%].*");
    private static final Pattern UNIT_PATTERN = Pattern.compile(".*\\d+\\s*[kmbtseh](?:\\s|$|[+\\-*/^%()])", Pattern.CASE_INSENSITIVE);
    private static final Pattern STORAGE_UNIT_PATTERN = Pattern.compile(".*\\d+\\s*(?:sc|dc|eb)(?:\\s|$|[+\\-*/^%()])", Pattern.CASE_INSENSITIVE);
    private static final Pattern FUNCTION_PATTERN = Pattern.compile(".*(sqrt|abs|floor|ceil|round)\\s*\\(", Pattern.CASE_INSENSITIVE);
    private static final Pattern VARIABLE_PATTERN = Pattern.compile(".*(ans|\\$\\w+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern PAREN_PATTERN = Pattern.compile(".*[()].*");
    private static final Pattern NUMBER_ONLY = Pattern.compile("^\\s*\\d+\\.?\\d*\\s*$");
    private static final Pattern TRAILING_OPERATOR = Pattern.compile(".*[+\\-*/^%]\\s*$");
    private static final Pattern MINECRAFT_ITEM = Pattern.compile("(?i).*(sword|pickaxe|axe|shovel|hoe|helmet|chestplate|leggings|boots|diamond|iron|gold|stone|wood|bow|arrow|block|ore|ingot|coal|redstone|lapis|emerald|netherite|pearl|eye|blaze|slime|magma|prismarine|quartz|obsidian|glowstone|hopper|chest|furnace|crafting|enchant|potion|book|bed)");

    public CalculatorManager() {
        this.evaluator = new ExpressionEvaluator();
    }

    /**
     * Heuristic check: is this a calculation or an item search?
     *
     * Tricky cases:
     * - "64" could be just a number OR a stack
     * - "diamond" is an item, not a calculation
     * - "diamond*2" is a calculation (even though it has "diamond" in it)
     */
    public boolean looksLikeCalculation(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }

        String trimmed = input.trim();

        // Plain number with no operators? Probably searching for items
        if (NUMBER_ONLY.matcher(trimmed).matches()) {
            return false;
        }

        // Item name without operators? Definitely a search
        if (MINECRAFT_ITEM.matcher(trimmed).matches() && !OPERATOR_PATTERN.matcher(trimmed).matches()) {
            return false;
        }

        // Now check for calculation indicators
        if (OPERATOR_PATTERN.matcher(trimmed).matches()) return true;
        if (PAREN_PATTERN.matcher(trimmed).matches()) return true;
        if (FUNCTION_PATTERN.matcher(trimmed).matches()) return true;
        if (VARIABLE_PATTERN.matcher(trimmed).matches()) return true;
        if (UNIT_PATTERN.matcher(trimmed).matches()) return true;
        if (STORAGE_UNIT_PATTERN.matcher(trimmed).matches()) return true;

        return false;
    }

    /**
     * Check if expression is complete enough to evaluate.
     * We don't want to show errors while someone is mid-typing.
     *
     * Incomplete examples:
     * - "5 +" (trailing operator)
     * - "sqrt(" (unclosed function)
     * - "(5 + 3" (unclosed paren)
     */
    private boolean isExpressionComplete(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }

        String trimmed = input.trim();

        // Can't end with operator
        if (TRAILING_OPERATOR.matcher(trimmed).matches()) {
            return false;
        }

        // Check parentheses are balanced
        int parenCount = 0;
        for (char c : trimmed.toCharArray()) {
            if (c == '(') parenCount++;
            if (c == ')') parenCount--;
            if (parenCount < 0) return false;  // More closing than opening
        }
        if (parenCount != 0) return false;  // Unmatched parens

        // Can't end with incomplete function call
        if (trimmed.matches(".*(?:sqrt|abs|floor|ceil|round)\\s*\\([^)]*$")) {
            return false;
        }

        return true;
    }

    /**
     * Main method called on every keystroke in REI search bar.
     *
     * Handles:
     * 1. Detecting manual typing vs history navigation
     * 2. Committing equations when user clears search
     * 3. Live calculation + display
     */
    public String formatSearchBar(String input) {
        String cleanInput = ResultFormatter.cleanInput(input);

        // If user manually types while in history mode, exit history mode
        if (reiHistoryIndex != -1 && !cleanInput.equals(lastSearchInput)) {
            LOGGER.debug("User typed '{}' while navigating history, exiting history mode", cleanInput);
            reiHistoryIndex = -1;
            savedCurrentInput = "";
        }

        // Detect when user clears the search bar (commits equation)
        if (cleanInput.isEmpty() && !lastSearchInput.isEmpty()) {
            // Save the completed equation to history (not individual keystrokes)
            if (!currentEquation.isEmpty() && looksLikeCalculation(currentEquation)) {
                addToReiHistory(currentEquation);
                currentEquation = "";
            }
            commitPendingCalculation();
        }

        // Update current equation being typed
        if (!cleanInput.equals(lastSearchInput)) {
            // If we're not navigating history, update the current equation
            if (reiHistoryIndex == -1) {
                currentEquation = cleanInput;
            }

            lastSearchInput = cleanInput;

            // Calculate and show result if it looks complete
            if (looksLikeCalculation(cleanInput)) {
                if (isExpressionComplete(cleanInput)) {
                    calculateForDisplay(cleanInput);
                } else {
                    // Still typing, don't show anything
                    lastFormattedResult = null;
                    lastErrorMessage = null;
                }
            } else {
                lastFormattedResult = null;
                lastErrorMessage = null;
            }
        }

        return cleanInput;
    }

    /**
     * Add completed equation to REI search history (for Ctrl+Z).
     * Only saves FULL equations, not individual keystrokes.
     *
     * Example: "100+50" = 1 entry (not 7 entries for each character)
     */
    private void addToReiHistory(String equation) {
        if (equation == null || equation.trim().isEmpty()) {
            return;
        }

        // Don't duplicate the last entry
        if (!reiSearchHistory.isEmpty() && reiSearchHistory.get(reiSearchHistory.size() - 1).equals(equation)) {
            return;
        }

        // Add the completed equation
        reiSearchHistory.add(equation);

        // Keep only the last 15 equations (hardcoded limit)
        while (reiSearchHistory.size() > MAX_EQUATIONS) {
            reiSearchHistory.remove(0);
        }

        LOGGER.debug("Equation saved to history: '{}' (total: {})", equation, reiSearchHistory.size());
    }

    /**
     * Calculate result for live display (doesn't add to /calchist).
     * Runs quietly - errors are suppressed to avoid spam while typing.
     */
    private void calculateForDisplay(String input) {
        if (input == null || input.trim().isEmpty()) {
            lastFormattedResult = null;
            lastErrorMessage = null;
            return;
        }

        try {
            BigDecimal result = evaluator.evaluateQuiet(input);
            lastFormattedResult = ResultFormatter.formatWithCommas(result);
            lastErrorMessage = null;

            // Mark as uncommitted (will be committed when user clears search)
            lastCompletedExpression = input;
            lastCompletedResult = result;
            hasUncommittedCalculation = true;

        } catch (ExpressionEvaluator.EvalException e) {
            // Silently ignore errors during live typing
            lastFormattedResult = null;
            lastErrorMessage = null;
        } catch (Exception e) {
            lastFormattedResult = null;
            lastErrorMessage = null;
        }
    }

    /**
     * Save pending calculation to /calchist history.
     * Called when user clears search bar or closes REI.
     */
    private void commitPendingCalculation() {
        if (hasUncommittedCalculation && lastCompletedExpression != null && lastCompletedResult != null) {
            addToCompletedHistory(lastCompletedExpression, lastCompletedResult);
            lastCompletedExpression = null;
            lastCompletedResult = null;
            hasUncommittedCalculation = false;
            LOGGER.debug("Committed calculation to history");
        }
    }

    // Public wrapper for external calls (e.g., when REI closes)
    public void commitPendingCalculationPublic() {
        // Also save current equation to REI history before committing
        if (!currentEquation.isEmpty() && looksLikeCalculation(currentEquation)) {
            addToReiHistory(currentEquation);
            currentEquation = "";
        }
        commitPendingCalculation();
    }

    /**
     * Add to completed calculation history (shown in /calchist).
     * Stores up to 15 calculations.
     */
    private void addToCompletedHistory(String expression, BigDecimal result) {
        String historyEntry = expression + " = " + ResultFormatter.formatWithCommas(result);

        // Don't duplicate last entry
        if (completedHistory.isEmpty() || !completedHistory.get(completedHistory.size() - 1).equals(historyEntry)) {
            completedHistory.add(historyEntry);

            // Keep only last 15 calculations
            while (completedHistory.size() > MAX_EQUATIONS) {
                completedHistory.remove(0);
            }

            LOGGER.debug("Added to completed history: '{}'", historyEntry);
        }
    }

    public String getLastFormattedResult() {
        return lastFormattedResult;
    }

    public String getLastErrorMessage() {
        return null;  // We don't show errors in live view
    }

    public boolean hasResult() {
        return lastFormattedResult != null;
    }

    public boolean hasError() {
        return false;
    }

    /**
     * Calculate via /calc command (this version DOES add to history).
     */
    public BigDecimal calculate(String input) throws ExpressionEvaluator.EvalException {
        String cleanInput = ResultFormatter.cleanInput(input);
        BigDecimal result = evaluator.evaluate(cleanInput);

        // Command-based calculations go straight to history
        addToCompletedHistory(cleanInput, result);

        return result;
    }

    /**
     * Handle Ctrl+Z (undo) and Ctrl+Y (redo) keyboard shortcuts.
     * Navigates through completed equations, not individual keystrokes.
     */
    public void handleKeyPress(int keyCode, int modifiers) {
        CalculatorConfig config = CalculatorConfig.getInstance();

        if (!config.enableHistoryNavigation) {
            return;
        }

        boolean isCtrlPressed = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0;

        // Ctrl+Z: Undo (go back to previous equation)
        if (keyCode == GLFW.GLFW_KEY_Z && isCtrlPressed) {
            // Save current equation before navigating
            if (reiHistoryIndex == -1 && !reiSearchHistory.isEmpty()) {
                try {
                    savedCurrentInput = REIRuntime.getInstance().getSearchTextField().getText();
                    // Also save current equation to history if it's a calculation
                    if (!savedCurrentInput.isEmpty() && looksLikeCalculation(savedCurrentInput)) {
                        currentEquation = savedCurrentInput;
                        addToReiHistory(savedCurrentInput);
                    }
                    LOGGER.debug("Saved current input: '{}'", savedCurrentInput);
                } catch (Exception e) {
                    savedCurrentInput = "";
                }
            }

            if (reiSearchHistory.isEmpty()) {
                LOGGER.debug("History is empty, can't undo");
                return;
            }

            if (reiHistoryIndex == -1) {
                // Start from most recent (skip the one we just added)
                reiHistoryIndex = reiSearchHistory.size() - 2;
                if (reiHistoryIndex < 0) reiHistoryIndex = 0;
            } else if (reiHistoryIndex > 0) {
                // Go back one more
                reiHistoryIndex--;
            } else {
                // Already at oldest
                LOGGER.debug("Already at oldest history entry");
                return;
            }

            setSearchFieldText(reiHistoryIndex);
            LOGGER.info("Ctrl+Z: Undo to equation: '{}'", reiSearchHistory.get(reiHistoryIndex));
        }

        // Ctrl+Y: Redo (go forward to next equation)
        if (keyCode == GLFW.GLFW_KEY_Y && isCtrlPressed) {
            if (reiHistoryIndex == -1) {
                LOGGER.debug("Not in history mode, can't redo");
                return;
            }

            if (reiHistoryIndex < reiSearchHistory.size() - 1) {
                // Go forward one
                reiHistoryIndex++;
                setSearchFieldText(reiHistoryIndex);
                LOGGER.info("Ctrl+Y: Redo to equation: '{}'", reiSearchHistory.get(reiHistoryIndex));
            } else {
                // We're at newest, restore what user was typing
                reiHistoryIndex = -1;
                restoreSavedInput();
                LOGGER.info("Ctrl+Y: Restored current input: '{}'", savedCurrentInput);
            }
        }
    }

    /**
     * Update search field with a history entry.
     * Forces update without adding to history (to avoid infinite loop).
     */
    private void setSearchFieldText(int index) {
        if (index >= 0 && index < reiSearchHistory.size()) {
            try {
                String historyText = reiSearchHistory.get(index);
                REIRuntime.getInstance().getSearchTextField().setText(historyText);

                // Update internal state without triggering history save
                lastSearchInput = historyText;
                currentEquation = "";  // Don't save while navigating

                if (looksLikeCalculation(historyText) && isExpressionComplete(historyText)) {
                    calculateForDisplay(historyText);
                } else {
                    lastFormattedResult = null;
                }
            } catch (Exception e) {
                LOGGER.error("Failed to set search field text: ", e);
            }
        }
    }

    /**
     * Restore what user was typing before they started pressing Ctrl+Z.
     */
    private void restoreSavedInput() {
        try {
            REIRuntime.getInstance().getSearchTextField().setText(savedCurrentInput);
            lastSearchInput = savedCurrentInput;
            currentEquation = savedCurrentInput;  // Resume tracking this equation

            if (looksLikeCalculation(savedCurrentInput) && isExpressionComplete(savedCurrentInput)) {
                calculateForDisplay(savedCurrentInput);
            } else {
                lastFormattedResult = null;
            }

            savedCurrentInput = "";
        } catch (Exception e) {
            LOGGER.error("Failed to restore saved input: ", e);
        }
    }

    public void setVariable(String name, String valueExpr) throws ExpressionEvaluator.EvalException {
        evaluator.setVariable(name, valueExpr);
    }

    public List<String> getHistory() {
        return new ArrayList<>(completedHistory);
    }

    public List<String> getCommandHistory() {
        return evaluator.getHistory();
    }

    public List<String> getReiHistory() {
        return new ArrayList<>(reiSearchHistory);
    }

    public void clearHistory() {
        evaluator.clearHistory();
        reiSearchHistory.clear();
        completedHistory.clear();
        reiHistoryIndex = -1;
        savedCurrentInput = "";
        currentEquation = "";
        lastSearchInput = "";
        lastFormattedResult = null;
        lastErrorMessage = null;
        lastCompletedExpression = null;
        lastCompletedResult = null;
        hasUncommittedCalculation = false;
        sessionResetNotified = false;
        LOGGER.info("Cleared all history");
    }

    public BigDecimal getLastAnswer() {
        return evaluator.getLastAnswer();
    }

    public String getVariablesInfo() {
        return evaluator.getVariablesInfo();
    }

    /**
     * Reset everything when player leaves world/server.
     * Prevents history from persisting across sessions.
     */
    public void reset() {
        lastSearchInput = "";
        lastFormattedResult = null;
        lastErrorMessage = null;
        reiSearchHistory.clear();
        completedHistory.clear();
        reiHistoryIndex = -1;
        savedCurrentInput = "";
        currentEquation = "";
        lastCompletedExpression = null;
        lastCompletedResult = null;
        hasUncommittedCalculation = false;
        sessionResetNotified = false;
        evaluator.clearHistory();
        LOGGER.info("Calculator reset (session ended)");
    }

    public boolean isSessionResetNotified() {
        return sessionResetNotified;
    }

    public void markSessionReset() {
        sessionResetNotified = true;
    }
}