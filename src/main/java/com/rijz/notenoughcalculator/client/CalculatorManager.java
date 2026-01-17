package com.rijz.notenoughcalculator.client;

import com.rijz.notenoughcalculator.config.CalculatorConfig;
import com.rijz.notenoughcalculator.core.ExpressionEvaluator;
import com.rijz.notenoughcalculator.core.ResultFormatter;
import me.shedaniel.rei.api.client.REIRuntime;
import org.lwjgl.glfw.GLFW;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Pattern;

// Main calculator state manager with history tracking
// History automatically clears when leaving world/server for a fresh start each session
public class CalculatorManager {

    private final ExpressionEvaluator evaluator;
    private String lastSearchInput = "";
    private String lastFormattedResult = null;
    private String lastErrorMessage = null;
    private int historyIndex = -1;

    // Track whether we've shown the session reset notification to the user
    private boolean sessionResetNotified = false;

    // Pre-compiled regex patterns for better performance when checking calculations
    private static final Pattern OPERATOR_PATTERN = Pattern.compile(".*[+\\-*/^%].*");
    private static final Pattern UNIT_PATTERN = Pattern.compile(".*\\d+\\s*[kmbtseh](?:\\s|$|[+\\-*/^%()]|$).*", Pattern.CASE_INSENSITIVE);
    private static final Pattern STORAGE_UNIT_PATTERN = Pattern.compile(".*\\d+\\s*(?:sc|dc|eb)(?:\\s|$|[+\\-*/^%()]|$).*", Pattern.CASE_INSENSITIVE);
    private static final Pattern FUNCTION_PATTERN = Pattern.compile(".*(sqrt|abs|floor|ceil|round)\\s*\\(.*", Pattern.CASE_INSENSITIVE);
    private static final Pattern VARIABLE_PATTERN = Pattern.compile(".*(ans|\\$\\w+).*", Pattern.CASE_INSENSITIVE);
    private static final Pattern PAREN_PATTERN = Pattern.compile(".*[()].*");
    private static final Pattern NUMBER_ONLY = Pattern.compile("^\\s*\\d+\\.?\\d*\\s*$");

    // Pattern to detect trailing operators (incomplete expressions)
    private static final Pattern TRAILING_OPERATOR = Pattern.compile(".*[+\\-*/^%]\\s*$");

    // Common Minecraft items to avoid false positives when users search for items
    private static final Pattern MINECRAFT_ITEM = Pattern.compile("(?i).*(sword|pickaxe|axe|shovel|hoe|helmet|chestplate|leggings|boots|diamond|iron|gold|stone|wood|bow|arrow|block|ore|ingot|coal|redstone|lapis|emerald|netherite|pearl|eye|blaze|slime|magma|prismarine|quartz|obsidian|glowstone|hopper|chest|furnace|crafting|enchant|potion|book|bed).*");

    public CalculatorManager() {
        this.evaluator = new ExpressionEvaluator();
    }

    // Smart detection: Is this input a calculation or just a regular search?
    public boolean looksLikeCalculation(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }

        String trimmed = input.trim();

        // Single numbers without operators aren't calculations
        if (NUMBER_ONLY.matcher(trimmed).matches()) {
            return false;
        }

        // Don't treat common item searches as calculations unless they have operators
        if (MINECRAFT_ITEM.matcher(trimmed).matches() && !OPERATOR_PATTERN.matcher(trimmed).matches()) {
            return false;
        }

        // Check for calculation indicators in priority order
        if (OPERATOR_PATTERN.matcher(trimmed).matches()) {
            return true;
        }

        if (PAREN_PATTERN.matcher(trimmed).matches()) {
            return true;
        }

        if (FUNCTION_PATTERN.matcher(trimmed).matches()) {
            return true;
        }

        if (VARIABLE_PATTERN.matcher(trimmed).matches()) {
            return true;
        }

        if (UNIT_PATTERN.matcher(trimmed).matches()) {
            return true;
        }

        if (STORAGE_UNIT_PATTERN.matcher(trimmed).matches()) {
            return true;
        }

        return false;
    }

    // NEW: Check if expression is complete and ready to evaluate
    private boolean isExpressionComplete(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }

        String trimmed = input.trim();

        // Don't evaluate if it ends with an operator (incomplete)
        if (TRAILING_OPERATOR.matcher(trimmed).matches()) {
            return false;
        }

        // Check for balanced parentheses
        int parenCount = 0;
        for (char c : trimmed.toCharArray()) {
            if (c == '(') parenCount++;
            if (c == ')') parenCount--;
            if (parenCount < 0) return false; // More closing than opening
        }

        // Unbalanced parentheses = incomplete
        if (parenCount != 0) {
            return false;
        }

        // Check for incomplete function calls (function name without closing paren)
        if (trimmed.matches(".*(?:sqrt|abs|floor|ceil|round)\\s*\\([^)]*$")) {
            return false;
        }

        return true;
    }

    // Process search bar input and calculate if needed
    public String formatSearchBar(String input) {
        String cleanInput = ResultFormatter.cleanInput(input);

        // Reset history position when user types something new
        if (!cleanInput.equals(lastSearchInput)) {
            historyIndex = -1;
            lastSearchInput = cleanInput;

            // Only try to calculate if it looks like a calculation AND is complete
            if (looksLikeCalculation(cleanInput)) {
                if (isExpressionComplete(cleanInput)) {
                    calculateForDisplay(cleanInput);
                } else {
                    // Incomplete expression - clear results but don't show errors
                    lastFormattedResult = null;
                    lastErrorMessage = null;
                }
            } else {
                // Clear old results when switching to non-calculation searches
                lastFormattedResult = null;
                lastErrorMessage = null;
            }
        }

        return cleanInput;
    }

    // Calculate and store the result quietly - errors don't show visually
    // Uses evaluateQuiet() to avoid polluting history with incomplete expressions
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
        } catch (ExpressionEvaluator.EvalException e) {
            // Silently ignore errors - don't show them visually
            lastFormattedResult = null;
            lastErrorMessage = null;
        } catch (Exception e) {
            // Catch any unexpected errors to prevent crashes
            lastFormattedResult = null;
            lastErrorMessage = null;
        }
    }

    public String getLastFormattedResult() {
        return lastFormattedResult;
    }

    public String getLastErrorMessage() {
        return null; // Never show visual errors in REI
    }

    public boolean hasResult() {
        return lastFormattedResult != null;
    }

    public boolean hasError() {
        return false; // Never show visual errors in REI
    }

    // Calculate with exceptions for command usage (where errors should be shown)
    public BigDecimal calculate(String input) throws ExpressionEvaluator.EvalException {
        return evaluator.evaluate(ResultFormatter.cleanInput(input));
    }

    // Handle Ctrl+Z (undo) and Ctrl+Y (redo) for history navigation
    public void handleKeyPress(int keyCode, int modifiers) {
        CalculatorConfig config = CalculatorConfig.getInstance();

        if (!config.enableHistoryNavigation) {
            return;
        }

        List<String> history = evaluator.getHistory();

        if (history.isEmpty()) {
            return;
        }

        boolean isCtrlPressed = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0;

        // Ctrl+Z - Go back in history
        if (keyCode == GLFW.GLFW_KEY_Z && isCtrlPressed) {
            if (historyIndex == -1) {
                historyIndex = history.size() - 1;
            } else if (historyIndex > 0) {
                historyIndex--;
            }

            setSearchFieldText(history, historyIndex);
        }

        // Ctrl+Y - Go forward in history
        if (keyCode == GLFW.GLFW_KEY_Y && isCtrlPressed) {
            if (historyIndex != -1 && historyIndex < history.size() - 1) {
                historyIndex++;
                setSearchFieldText(history, historyIndex);
            } else if (historyIndex == history.size() - 1) {
                historyIndex = -1;
                clearSearchField();
            }
        }
    }

    // Safely update the REI search field text
    private void setSearchFieldText(List<String> history, int index) {
        if (index >= 0 && index < history.size()) {
            try {
                REIRuntime.getInstance().getSearchTextField().setText(history.get(index));
            } catch (Exception e) {
                // Silently ignore if REI isn't available
            }
        }
    }

    // Safely clear the REI search field
    private void clearSearchField() {
        try {
            REIRuntime.getInstance().getSearchTextField().setText("");
        } catch (Exception e) {
            // Silently ignore if REI isn't available
        }
    }

    // Store a custom variable with a calculated value
    public void setVariable(String name, String valueExpr) throws ExpressionEvaluator.EvalException {
        evaluator.setVariable(name, valueExpr);
    }

    // Get the list of previous calculations
    public List<String> getHistory() {
        return evaluator.getHistory();
    }

    // Wipe the calculation history (happens automatically when leaving world/server)
    public void clearHistory() {
        evaluator.clearHistory();
        historyIndex = -1;
        sessionResetNotified = false;
    }

    // Get the result from the last calculation for use with 'ans'
    public BigDecimal getLastAnswer() {
        return evaluator.getLastAnswer();
    }

    // Get info about all saved custom variables
    public String getVariablesInfo() {
        return evaluator.getVariablesInfo();
    }

    // Full reset of calculator state (called when leaving world/server)
    public void reset() {
        lastSearchInput = "";
        lastFormattedResult = null;
        lastErrorMessage = null;
        historyIndex = -1;
        sessionResetNotified = false;
    }

    // Check if we've told the user about the session reset
    public boolean isSessionResetNotified() {
        return sessionResetNotified;
    }

    // Mark that we've notified the user about the session reset
    public void markSessionReset() {
        sessionResetNotified = true;
    }
}