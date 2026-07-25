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
import com.rijz.notenoughcalculator.client.gui.CalculatorConfigScreen;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.math.BigDecimal;
import java.util.List;

/**
 * Command handlers for calculator chat commands.
 */
public class CalcCommands {

    // Hardcoded: show max 15 history entries at once
    private static final int MAX_HISTORY_DISPLAY = 15;

    private static Component t(String key, Object... args) {
        return Component.translatable(key, args);
    }

    private static void send(CommandContext<FabricClientCommandSource> ctx, String key, Object... args) {
        ctx.getSource().getPlayer().sendSystemMessage(t(key, args));
    }

    private static void sendLiteral(CommandContext<FabricClientCommandSource> ctx, String text) {
        ctx.getSource().getPlayer().sendSystemMessage(Component.literal(text));
    }

    private static void sendEmpty(CommandContext<FabricClientCommandSource> ctx) {
        ctx.getSource().getPlayer().sendSystemMessage(Component.literal(""));
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

            // Show last 15 entries max (hardcoded)
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
            BigDecimal result = NotEnoughCalculatorClient.getCalculatorManager().calculate(valueExpr);
            NotEnoughCalculatorClient.getCalculatorManager().setVariableDirect(varName, result);
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
        send(ctx, "notenoughcalculator.help.main.cmd_calc");
        send(ctx, "notenoughcalculator.help.main.cmd_calchist");
        send(ctx, "notenoughcalculator.help.main.cmd_calcclear");
        send(ctx, "notenoughcalculator.help.main.cmd_calcset");
        send(ctx, "notenoughcalculator.help.main.cmd_calcconfig");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.main.help_pages");
        send(ctx, "notenoughcalculator.help.main.page_operators");
        send(ctx, "notenoughcalculator.help.main.page_functions");
        send(ctx, "notenoughcalculator.help.main.page_units");
        send(ctx, "notenoughcalculator.help.main.page_variables");
        send(ctx, "notenoughcalculator.help.main.page_examples");
        send(ctx, "notenoughcalculator.help.main.page_config");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.main.keyboard_shortcuts");
        send(ctx, "notenoughcalculator.help.main.keyboard_shortcuts_1");
        send(ctx, "notenoughcalculator.help.main.keyboard_shortcuts_2");
        send(ctx, "notenoughcalculator.help.main.keyboard_shortcuts_3");
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
        send(ctx, "notenoughcalculator.help.operators.factorial");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.operators.literals");
        send(ctx, "notenoughcalculator.help.operators.binary");
        send(ctx, "notenoughcalculator.help.operators.hex");
        send(ctx, "notenoughcalculator.help.operators.octal");
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

        send(ctx, "notenoughcalculator.help.functions.log");
        send(ctx, "notenoughcalculator.help.functions.log_example_1");
        send(ctx, "notenoughcalculator.help.functions.log_example_2");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.functions.ln");
        send(ctx, "notenoughcalculator.help.functions.ln_example_1");
        send(ctx, "notenoughcalculator.help.functions.ln_example_2");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.functions.sin");
        send(ctx, "notenoughcalculator.help.functions.sin_example_1");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.functions.cos");
        send(ctx, "notenoughcalculator.help.functions.cos_example_1");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.functions.tan");
        send(ctx, "notenoughcalculator.help.functions.tan_example_1");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.functions.min");
        send(ctx, "notenoughcalculator.help.functions.min_example_1");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.functions.max");
        send(ctx, "notenoughcalculator.help.functions.max_example_1");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.functions.combining");
        send(ctx, "notenoughcalculator.help.functions.combining_example");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.functions.factorial");
        send(ctx, "notenoughcalculator.help.functions.factorial_example_1");
        send(ctx, "notenoughcalculator.help.functions.factorial_example_2");
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
        send(ctx, "notenoughcalculator.help.variables.builtin_pi");
        send(ctx, "notenoughcalculator.help.variables.builtin_e");
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
        Minecraft client = Minecraft.getInstance();
        client.execute(() -> {
            Screen currentScreen = NotEnoughCalculatorClient.getCurrentScreen(client);
            CalculatorConfigScreen.openScreen(client, new CalculatorConfigScreen(currentScreen));
        });
        return 1;
    }
}