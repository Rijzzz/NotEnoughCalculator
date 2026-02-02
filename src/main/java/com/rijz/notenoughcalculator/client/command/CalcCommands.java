/*
 * This file is part of Not Enough Calculator.
 *
 * Not Enough Calculator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Not Enough Calculator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.rijz.notenoughcalculator.client.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.rijz.notenoughcalculator.client.NotEnoughCalculatorClient;
import com.rijz.notenoughcalculator.config.CalculatorConfig;
import com.rijz.notenoughcalculator.core.ExpressionEvaluator;
import com.rijz.notenoughcalculator.core.ResultFormatter;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

import java.math.BigDecimal;
import java.util.List;

/**
 * Command handlers for calculator chat commands.
 */
public class CalcCommands {

    // Hardcoded: show max 10 history entries at once
    private static final int MAX_HISTORY_DISPLAY = 15;

    private static Text t(String key, Object... args) {
        return Text.translatable(key, args);
    }

    private static void send(CommandContext<FabricClientCommandSource> ctx, String key, Object... args) {
        ctx.getSource().getPlayer().sendMessage(t(key, args), false);
    }

    private static void sendLiteral(CommandContext<FabricClientCommandSource> ctx, String text) {
        ctx.getSource().getPlayer().sendMessage(Text.literal(text), false);
    }

    private static void sendEmpty(CommandContext<FabricClientCommandSource> ctx) {
        ctx.getSource().getPlayer().sendMessage(Text.literal(""), false);
    }

    public static int executeCalc(CommandContext<FabricClientCommandSource> ctx) {
        CalculatorConfig config = CalculatorConfig.getInstance();
        String expr = StringArgumentType.getString(ctx, "expression");

        try {
            BigDecimal result = NotEnoughCalculatorClient.getCalculatorManager().calculate(expr);
            String formatted = ResultFormatter.formatWithUnits(result);

            sendLiteral(ctx, config.getOperatorColorCode() + expr + " " +
                    t("notenoughcalculator.result.equals").getString() +
                    config.getResultColorCode() + formatted);
        } catch (ExpressionEvaluator.EvalException e) {
            sendLiteral(ctx, config.getErrorColorCode() +
                    t("notenoughcalculator.result.error_prefix").getString() + e.getMessage());
        }

        return 1;
    }

    public static int executeHistory(CommandContext<FabricClientCommandSource> ctx) {
        List<String> history = NotEnoughCalculatorClient.getCalculatorManager().getHistory();

        if (history.isEmpty()) {
            send(ctx, "notenoughcalculator.history.empty");
        } else {
            send(ctx, "notenoughcalculator.history.title");
            sendEmpty(ctx);

            // Show last 10 entries max (hardcoded)
            int maxDisplay = Math.min(MAX_HISTORY_DISPLAY, history.size());
            for (int i = Math.max(0, history.size() - maxDisplay); i < history.size(); i++) {
                sendLiteral(ctx, "§7" + (i + 1) + ". §f" + history.get(i));
            }

            if (history.size() > maxDisplay) {
                sendEmpty(ctx);
                send(ctx, "notenoughcalculator.history.showing", maxDisplay, history.size());
            }

            sendEmpty(ctx);
            send(ctx, "notenoughcalculator.history.tip");
        }

        return 1;
    }

    public static int executeClear(CommandContext<FabricClientCommandSource> ctx) {
        NotEnoughCalculatorClient.getCalculatorManager().clearHistory();
        send(ctx, "notenoughcalculator.history.cleared");
        return 1;
    }

    public static int executeSet(CommandContext<FabricClientCommandSource> ctx) {
        CalculatorConfig config = CalculatorConfig.getInstance();
        String varName = StringArgumentType.getString(ctx, "variable");
        String valueExpr = StringArgumentType.getString(ctx, "value");

        try {
            NotEnoughCalculatorClient.getCalculatorManager().setVariable(varName, valueExpr);
            BigDecimal result = NotEnoughCalculatorClient.getCalculatorManager().calculate(valueExpr);
            String formatted = ResultFormatter.formatWithUnits(result);

            send(ctx, "notenoughcalculator.variable.set", varName,
                    t("notenoughcalculator.result.equals").getString(),
                    config.getResultColorCode() + formatted);
        } catch (ExpressionEvaluator.EvalException e) {
            sendLiteral(ctx, config.getErrorColorCode() +
                    t("notenoughcalculator.result.error_prefix").getString() + e.getMessage());
        }

        return 1;
    }

    public static int executeHelp(CommandContext<FabricClientCommandSource> ctx) {
        return executeHelpPage(ctx, "main");
    }

    public static int executeHelpPage(CommandContext<FabricClientCommandSource> ctx) {
        String page;
        try {
            page = StringArgumentType.getString(ctx, "page");
        } catch (IllegalArgumentException e) {
            page = "main";
        }
        return executeHelpPage(ctx, page);
    }

    private static int executeHelpPage(CommandContext<FabricClientCommandSource> ctx, String page) {
        switch (page.toLowerCase()) {
            case "operators":
                showOperatorsHelp(ctx);
                break;
            case "functions":
                showFunctionsHelp(ctx);
                break;
            case "units":
                showUnitsHelp(ctx);
                break;
            case "variables":
                showVariablesHelp(ctx);
                break;
            case "examples":
                showExamplesHelp(ctx);
                break;
            case "config":
                showConfigHelp(ctx);
                break;
            default:
                showMainHelp(ctx);
                break;
        }
        return 1;
    }

    private static void showMainHelp(CommandContext<FabricClientCommandSource> ctx) {
        send(ctx, "notenoughcalculator.help.main.title");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.main.quick_start");
        send(ctx, "notenoughcalculator.help.main.quick_start_1");
        send(ctx, "notenoughcalculator.help.main.quick_start_2");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.main.commands");
        sendLiteral(ctx, "§e/calc <expression> §7- " + t("notenoughcalculator.command.calc.description").getString());
        sendLiteral(ctx, "§e/calchist §7- " + t("notenoughcalculator.command.calchist.description").getString());
        sendLiteral(ctx, "§e/calcclear §7- " + t("notenoughcalculator.command.calcclear.description").getString());
        sendLiteral(ctx, "§e/calcset <var> <value> §7- " + t("notenoughcalculator.command.calcset.description").getString());
        sendLiteral(ctx, "§e/calcconfig §7- " + t("notenoughcalculator.command.calcconfig.description").getString());
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.main.help_pages");
        sendLiteral(ctx, "§e/calchelp operators §7- Learn about +, -, *, /, ^, %");
        sendLiteral(ctx, "§e/calchelp functions §7- Learn about sqrt, abs, floor, etc.");
        sendLiteral(ctx, "§e/calchelp units §7- Learn Skyblock units (k, m, b, s, e, h)");
        sendLiteral(ctx, "§e/calchelp variables §7- Learn about ans, $custom variables");
        sendLiteral(ctx, "§e/calchelp examples §7- See practical examples");
        sendLiteral(ctx, "§e/calchelp config §7- Learn about configuration");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.main.keyboard_shortcuts");
        send(ctx, "notenoughcalculator.help.main.keyboard_shortcuts_1");
        send(ctx, "notenoughcalculator.help.main.keyboard_shortcuts_2");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.main.footer");
    }

    private static void showOperatorsHelp(CommandContext<FabricClientCommandSource> ctx) {
        send(ctx, "notenoughcalculator.help.operators.title");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.operators.basic");
        send(ctx, "notenoughcalculator.help.operators.add");
        send(ctx, "notenoughcalculator.help.operators.subtract");
        send(ctx, "notenoughcalculator.help.operators.multiply");
        send(ctx, "notenoughcalculator.help.operators.divide");
        send(ctx, "notenoughcalculator.help.operators.power");
        send(ctx, "notenoughcalculator.help.operators.modulo");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.operators.parentheses");
        send(ctx, "notenoughcalculator.help.operators.parentheses_desc");
        send(ctx, "notenoughcalculator.help.operators.parentheses_note");
        send(ctx, "notenoughcalculator.help.operators.parentheses_example");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.operators.pemdas");
        send(ctx, "notenoughcalculator.help.operators.pemdas_1");
        send(ctx, "notenoughcalculator.help.operators.pemdas_2");
        send(ctx, "notenoughcalculator.help.operators.pemdas_3");
        send(ctx, "notenoughcalculator.help.operators.pemdas_4");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.back");
    }

    private static void showFunctionsHelp(CommandContext<FabricClientCommandSource> ctx) {
        send(ctx, "notenoughcalculator.help.functions.title");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.functions.available");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.functions.sqrt");
        send(ctx, "notenoughcalculator.help.functions.sqrt_example_1");
        send(ctx, "notenoughcalculator.help.functions.sqrt_example_2");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.functions.abs");
        send(ctx, "notenoughcalculator.help.functions.abs_example_1");
        send(ctx, "notenoughcalculator.help.functions.abs_example_2");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.functions.floor");
        send(ctx, "notenoughcalculator.help.functions.floor_example_1");
        send(ctx, "notenoughcalculator.help.functions.floor_example_2");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.functions.ceil");
        send(ctx, "notenoughcalculator.help.functions.ceil_example_1");
        send(ctx, "notenoughcalculator.help.functions.ceil_example_2");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.functions.round");
        send(ctx, "notenoughcalculator.help.functions.round_example_1");
        send(ctx, "notenoughcalculator.help.functions.round_example_2");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.functions.combining");
        send(ctx, "notenoughcalculator.help.functions.combining_example");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.back");
    }

    private static void showUnitsHelp(CommandContext<FabricClientCommandSource> ctx) {
        send(ctx, "notenoughcalculator.help.units.title");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.units.currency");
        send(ctx, "notenoughcalculator.help.units.currency_k");
        send(ctx, "notenoughcalculator.help.units.currency_m");
        send(ctx, "notenoughcalculator.help.units.currency_b");
        send(ctx, "notenoughcalculator.help.units.currency_t");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.units.item");
        send(ctx, "notenoughcalculator.help.units.item_s");
        send(ctx, "notenoughcalculator.help.units.item_e");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.units.storage");
        send(ctx, "notenoughcalculator.help.units.storage_h");
        send(ctx, "notenoughcalculator.help.units.storage_sc");
        send(ctx, "notenoughcalculator.help.units.storage_dc");
        send(ctx, "notenoughcalculator.help.units.storage_eb");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.units.usage");
        send(ctx, "notenoughcalculator.help.units.usage_1");
        send(ctx, "notenoughcalculator.help.units.usage_2");
        send(ctx, "notenoughcalculator.help.units.usage_3");
        send(ctx, "notenoughcalculator.help.units.usage_4");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.units.tips");
        send(ctx, "notenoughcalculator.help.units.tips_1");
        send(ctx, "notenoughcalculator.help.units.tips_2");
        send(ctx, "notenoughcalculator.help.units.tips_3");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.back");
    }

    private static void showVariablesHelp(CommandContext<FabricClientCommandSource> ctx) {
        send(ctx, "notenoughcalculator.help.variables.title");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.variables.builtin");
        send(ctx, "notenoughcalculator.help.variables.builtin_ans");
        send(ctx, "notenoughcalculator.help.variables.builtin_example_1");
        send(ctx, "notenoughcalculator.help.variables.builtin_example_2");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.variables.custom");
        send(ctx, "notenoughcalculator.help.variables.custom_desc");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.variables.setting");
        send(ctx, "notenoughcalculator.help.variables.setting_example_1");
        send(ctx, "notenoughcalculator.help.variables.setting_example_2");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.variables.using");
        send(ctx, "notenoughcalculator.help.variables.using_example_1");
        send(ctx, "notenoughcalculator.help.variables.using_example_2");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.variables.practical");
        send(ctx, "notenoughcalculator.help.variables.practical_1_title");
        send(ctx, "notenoughcalculator.help.variables.practical_1_step1");
        send(ctx, "notenoughcalculator.help.variables.practical_1_step2");
        send(ctx, "notenoughcalculator.help.variables.practical_1_step3");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.variables.practical_2_title");
        send(ctx, "notenoughcalculator.help.variables.practical_2_step1");
        send(ctx, "notenoughcalculator.help.variables.practical_2_step2");
        send(ctx, "notenoughcalculator.help.variables.practical_2_step3");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.variables.current");
        String vars = NotEnoughCalculatorClient.getCalculatorManager().getVariablesInfo();
        sendLiteral(ctx, "§7" + vars);
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.back");
    }

    private static void showExamplesHelp(CommandContext<FabricClientCommandSource> ctx) {
        send(ctx, "notenoughcalculator.help.examples.title");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.examples.auction");
        send(ctx, "notenoughcalculator.help.examples.auction_1");
        send(ctx, "notenoughcalculator.help.examples.auction_1_ex");
        send(ctx, "notenoughcalculator.help.examples.auction_2");
        send(ctx, "notenoughcalculator.help.examples.auction_2_ex");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.examples.inventory");
        send(ctx, "notenoughcalculator.help.examples.inventory_1");
        send(ctx, "notenoughcalculator.help.examples.inventory_1_ex");
        send(ctx, "notenoughcalculator.help.examples.inventory_2");
        send(ctx, "notenoughcalculator.help.examples.inventory_2_ex");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.examples.mining");
        send(ctx, "notenoughcalculator.help.examples.mining_1");
        send(ctx, "notenoughcalculator.help.examples.mining_1_ex");
        send(ctx, "notenoughcalculator.help.examples.mining_2");
        send(ctx, "notenoughcalculator.help.examples.mining_2_ex");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.examples.crafting");
        send(ctx, "notenoughcalculator.help.examples.crafting_1");
        send(ctx, "notenoughcalculator.help.examples.crafting_1_ex");
        send(ctx, "notenoughcalculator.help.examples.crafting_2");
        send(ctx, "notenoughcalculator.help.examples.crafting_2_ex");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.examples.complex");
        send(ctx, "notenoughcalculator.help.examples.complex_1");
        send(ctx, "notenoughcalculator.help.examples.complex_1_ex");
        send(ctx, "notenoughcalculator.help.examples.complex_2");
        send(ctx, "notenoughcalculator.help.examples.complex_2_ex");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.examples.tips");
        send(ctx, "notenoughcalculator.help.examples.tips_1");
        send(ctx, "notenoughcalculator.help.examples.tips_2");
        send(ctx, "notenoughcalculator.help.examples.tips_3");
        send(ctx, "notenoughcalculator.help.examples.tips_4");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.back");
    }

    private static void showConfigHelp(CommandContext<FabricClientCommandSource> ctx) {
        send(ctx, "notenoughcalculator.help.config.title");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.config.file");
        send(ctx, "notenoughcalculator.help.config.file_location");
        send(ctx, "notenoughcalculator.help.config.file_edit");
        send(ctx, "notenoughcalculator.help.config.file_edit_1");
        send(ctx, "notenoughcalculator.help.config.file_edit_2");
        send(ctx, "notenoughcalculator.help.config.file_edit_3");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.config.current");
        send(ctx, "notenoughcalculator.help.config.current_cmd");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.back");
    }

    public static int executeConfig(CommandContext<FabricClientCommandSource> ctx) {
        CalculatorConfig config = CalculatorConfig.getInstance();

        send(ctx, "notenoughcalculator.config.title");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.config.display_settings");
        send(ctx, "notenoughcalculator.config.inline_results",
                config.showInlineResults ?
                        t("notenoughcalculator.config.yes").getString() :
                        t("notenoughcalculator.config.no").getString());
        send(ctx, "notenoughcalculator.config.unit_suggestions",
                config.showUnitSuggestions ?
                        t("notenoughcalculator.config.yes").getString() :
                        t("notenoughcalculator.config.no").getString());
        send(ctx, "notenoughcalculator.config.comma_formatting",
                config.enableCommaFormatting ?
                        t("notenoughcalculator.config.yes").getString() :
                        t("notenoughcalculator.config.no").getString());
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.config.calculation_settings");
        send(ctx, "notenoughcalculator.config.decimal_precision", config.decimalPrecision);
        // Removed max_history line since it's now hardcoded at 15
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.config.features");
        send(ctx, "notenoughcalculator.config.history_navigation",
                config.enableHistoryNavigation ?
                        t("notenoughcalculator.config.enabled").getString() :
                        t("notenoughcalculator.config.disabled").getString());
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.config.edit_file");

        return 1;
    }
}