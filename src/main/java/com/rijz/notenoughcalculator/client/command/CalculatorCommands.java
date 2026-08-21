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
import com.rijz.notenoughcalculator.client.gui.CalculatorConfigScreen;
import com.rijz.notenoughcalculator.client.util.ReflectionUtils;
import com.rijz.notenoughcalculator.client.util.SyntaxHighlighter;
import com.rijz.notenoughcalculator.config.CalculatorConfig;
import com.rijz.notenoughcalculator.core.ExpressionEvaluator.EvalException;
import com.rijz.notenoughcalculator.core.ExpressionEvaluator.EvalResult;
import com.rijz.notenoughcalculator.core.ResultFormatter;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

import java.math.BigDecimal;
import java.util.List;

public class CalculatorCommands {


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

        String prefix = t("notenoughcalculator.result.prefix").getString();
        try {
            EvalResult evalRes = NotEnoughCalculatorClient.getCalculatorManager().calculateResult(expr);
            String formatted = ResultFormatter.formatResultWithUnits(evalRes);
            String copyText = config.enableFullEquationCopy ? (expr + " = " + formatted) : formatted;
            String highlightedExpr = config.enableSyntaxHighlighting ? SyntaxHighlighter.highlight(expr) : "§f" + expr;

            MutableComponent msg = Component.literal(prefix + highlightedExpr +
                    ResultFormatter.getEqualsSign() +
                    config.getChatResultColorCode() + formatted);

            Component tooltip = t("notenoughcalculator.chat.click_to_copy_tooltip");
            msg.setStyle(msg.getStyle()
                    .withClickEvent(new ClickEvent.RunCommand("/calccopy " + copyText))
                    .withHoverEvent(new HoverEvent.ShowText(tooltip)));

            ctx.getSource().getPlayer().sendSystemMessage(msg);
        } catch (EvalException e) {
            sendLiteral(ctx, prefix + config.getErrorColorCode() +
                    t("notenoughcalculator.result.error_prefix").getString() + e.getMessage());
        }

        return 1;
    }

    public static int executeCopy(CommandContext<FabricClientCommandSource> ctx) {
        String text = StringArgumentType.getString(ctx, "text");
        Minecraft.getInstance().keyboardHandler.setClipboard(text);
        send(ctx, "notenoughcalculator.chat.copied_to_clipboard", text);
        return 1;
    }

    public static int executeHistory(CommandContext<FabricClientCommandSource> ctx) {
        List<String> history = NotEnoughCalculatorClient.getCalculatorManager().getHistory();

        if (history.isEmpty()) {
            send(ctx, "notenoughcalculator.history.empty");
        } else {
            send(ctx, "notenoughcalculator.history.title");
            sendEmpty(ctx);


            int maxDisplay = Math.min(MAX_HISTORY_DISPLAY, history.size());
            for (int i = Math.max(0, history.size() - maxDisplay); i < history.size(); i++) {
                send(ctx, "notenoughcalculator.history.item", (i + 1), history.get(i));
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
                    ResultFormatter.getEqualsSign(),
                    config.getChatResultColorCode() + formatted);
        } catch (EvalException e) {
            sendLiteral(ctx, config.getErrorColorCode() +
                    t("notenoughcalculator.result.error_prefix").getString() + e.getMessage());
        }

        return 1;
    }

    public static int executeHelp(CommandContext<FabricClientCommandSource> ctx) {
        return CalculatorHelpFormatter.showHelpPage(ctx, "main");
    }

    public static int executeHelpPage(CommandContext<FabricClientCommandSource> ctx) {
        String page;
        try {
            page = StringArgumentType.getString(ctx, "page");
        } catch (IllegalArgumentException e) {
            page = "main";
        }
        return CalculatorHelpFormatter.showHelpPage(ctx, page);
    }

    public static int executeConfig(CommandContext<FabricClientCommandSource> ctx) {
        Minecraft client = Minecraft.getInstance();
        client.execute(() -> {
            Screen currentScreen = ReflectionUtils.getCurrentScreen(client);
            CalculatorConfigScreen.openScreen(client, new CalculatorConfigScreen(currentScreen));
        });
        return 1;
    }
}