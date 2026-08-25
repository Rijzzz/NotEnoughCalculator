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

import com.mojang.brigadier.context.CommandContext;
import com.rijz.notenoughcalculator.client.NotEnoughCalculatorClient;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

public class CalculatorHelpFormatter {

    private static Component t(String key, Object... args) {
        return Component.translatable(key, args);
    }

    private static void send(CommandContext<FabricClientCommandSource> ctx, String key, Object... args) {
        ctx.getSource().getPlayer().sendSystemMessage(t(key, args));
    }

    private static void sendClickablePage(CommandContext<FabricClientCommandSource> ctx, String pageName,
            String translationKey) {
        String fullText = t(translationKey).getString();
        MutableComponent msg = Component.literal(fullText);
        msg.setStyle(msg.getStyle()
                .withClickEvent(new ClickEvent.RunCommand("/calchelp " + pageName))
                .withHoverEvent(new HoverEvent.ShowText(t("notenoughcalculator.help.click_to_open", pageName))));
        ctx.getSource().getPlayer().sendSystemMessage(msg);
    }

    private static void sendEmpty(CommandContext<FabricClientCommandSource> ctx) {
        ctx.getSource().getPlayer().sendSystemMessage(Component.literal(""));
    }

    /**
     * Sends a block of translation keys, separated by empty lines at null entries.
     * Entries starting with "@" are clickable page links in format "@pageName:translationKey".
     */
    private static void sendBlock(CommandContext<FabricClientCommandSource> ctx, String... entries) {
        for (String entry : entries) {
            if (entry == null) {
                sendEmpty(ctx);
            } else if (entry.startsWith("@")) {
                int colon = entry.indexOf(':');
                String page = entry.substring(1, colon);
                String key = entry.substring(colon + 1);
                sendClickablePage(ctx, page, key);
            } else {
                send(ctx, entry);
            }
        }
    }

    public static int showHelpPage(CommandContext<FabricClientCommandSource> ctx, String page) {
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
            case "stats":
            case "api":
                showStatsHelp(ctx);
                break;
            case "market":
                showMarketHelp(ctx);
                break;
            case "tax":
                showTaxHelp(ctx);
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

    public static void showMainHelp(CommandContext<FabricClientCommandSource> ctx) {
        sendBlock(ctx,
                "notenoughcalculator.help.main.title",
                null,
                "notenoughcalculator.help.main.quick_start",
                "notenoughcalculator.help.main.quick_start_1",
                "notenoughcalculator.help.main.quick_start_2",
                null,
                "notenoughcalculator.help.main.commands",
                "notenoughcalculator.help.main.cmd_calc",
                "notenoughcalculator.help.main.cmd_calchist",
                "notenoughcalculator.help.main.cmd_calcclear",
                "notenoughcalculator.help.main.cmd_calcset",
                "notenoughcalculator.help.main.cmd_calcconfig",
                "notenoughcalculator.help.main.cmd_calcpos",
                null,
                "notenoughcalculator.help.main.help_pages",
                "@operators:notenoughcalculator.help.main.page_operators",
                "@functions:notenoughcalculator.help.main.page_functions",
                "@units:notenoughcalculator.help.main.page_units",
                "@variables:notenoughcalculator.help.main.page_variables",
                "@stats:notenoughcalculator.help.main.page_stats",
                "@market:notenoughcalculator.help.main.page_market",
                "@tax:notenoughcalculator.help.main.page_tax",
                "@examples:notenoughcalculator.help.main.page_examples",
                "@config:notenoughcalculator.help.main.page_config",
                null,
                "notenoughcalculator.help.main.keyboard_shortcuts",
                "notenoughcalculator.help.main.keyboard_shortcuts_1",
                "notenoughcalculator.help.main.keyboard_shortcuts_2",
                "notenoughcalculator.help.main.keyboard_shortcuts_3",
                "notenoughcalculator.help.main.keyboard_shortcuts_4",
                null,
                "notenoughcalculator.help.main.footer");
    }

    public static void showOperatorsHelp(CommandContext<FabricClientCommandSource> ctx) {
        sendBlock(ctx,
                "notenoughcalculator.help.operators.title",
                null,
                "notenoughcalculator.help.operators.basic",
                "notenoughcalculator.help.operators.add",
                "notenoughcalculator.help.operators.subtract",
                "notenoughcalculator.help.operators.multiply",
                "notenoughcalculator.help.operators.divide",
                "notenoughcalculator.help.operators.power",
                "notenoughcalculator.help.operators.modulo",
                "notenoughcalculator.help.operators.factorial",
                null,
                "notenoughcalculator.help.operators.bitwise",
                "notenoughcalculator.help.operators.bitwise_and",
                "notenoughcalculator.help.operators.bitwise_or",
                "notenoughcalculator.help.operators.bitwise_not",
                "notenoughcalculator.help.operators.bitwise_lshift",
                "notenoughcalculator.help.operators.bitwise_rshift",
                null,
                "notenoughcalculator.help.operators.literals",
                "notenoughcalculator.help.operators.binary",
                "notenoughcalculator.help.operators.hex",
                "notenoughcalculator.help.operators.octal",
                null,
                "notenoughcalculator.help.operators.parentheses",
                "notenoughcalculator.help.operators.parentheses_desc",
                "notenoughcalculator.help.operators.parentheses_note",
                "notenoughcalculator.help.operators.parentheses_example",
                null,
                "notenoughcalculator.help.operators.pemdas",
                "notenoughcalculator.help.operators.pemdas_1",
                "notenoughcalculator.help.operators.pemdas_2",
                "notenoughcalculator.help.operators.pemdas_3",
                "notenoughcalculator.help.operators.pemdas_4",
                "notenoughcalculator.help.operators.pemdas_5",
                null,
                "@main:notenoughcalculator.help.back");
    }

    public static void showFunctionsHelp(CommandContext<FabricClientCommandSource> ctx) {
        sendBlock(ctx,
                "notenoughcalculator.help.functions.title",
                null,
                "notenoughcalculator.help.functions.available",
                "notenoughcalculator.help.functions.sqrt",
                "notenoughcalculator.help.functions.abs",
                "notenoughcalculator.help.functions.floor",
                "notenoughcalculator.help.functions.ceil",
                "notenoughcalculator.help.functions.round",
                "notenoughcalculator.help.functions.log",
                "notenoughcalculator.help.functions.ln",
                "notenoughcalculator.help.functions.sin",
                "notenoughcalculator.help.functions.cos",
                "notenoughcalculator.help.functions.tan",
                "notenoughcalculator.help.functions.min",
                "notenoughcalculator.help.functions.max",
                null,
                "notenoughcalculator.help.functions.radix",
                "notenoughcalculator.help.functions.hex",
                "notenoughcalculator.help.functions.bin",
                "notenoughcalculator.help.functions.oct",
                null,
                "notenoughcalculator.help.functions.math_helpers",
                "notenoughcalculator.help.functions.pct",
                "notenoughcalculator.help.functions.gcd",
                "notenoughcalculator.help.functions.lcm",
                "notenoughcalculator.help.functions.clamp",
                "notenoughcalculator.help.functions.avg",
                "notenoughcalculator.help.functions.xor",
                null,
                "notenoughcalculator.help.functions.xp_tables",
                "notenoughcalculator.help.functions.skillxp",
                "notenoughcalculator.help.functions.huntingxp",
                "notenoughcalculator.help.functions.runecraftingxp",
                "notenoughcalculator.help.functions.socialxp",
                "notenoughcalculator.help.functions.cataxp",
                "notenoughcalculator.help.functions.slayerxp",
                "notenoughcalculator.help.functions.emanxp",
                "notenoughcalculator.help.functions.vampirexp",
                "notenoughcalculator.help.functions.perk",
                null,
                "@main:notenoughcalculator.help.back");
    }

    public static void showStatsHelp(CommandContext<FabricClientCommandSource> ctx) {
        sendBlock(ctx,
                "notenoughcalculator.help.stats.title",
                null,
                "notenoughcalculator.help.stats.currency",
                "notenoughcalculator.help.stats.currency_desc",
                "notenoughcalculator.help.stats.currency_ex",
                null,
                "notenoughcalculator.help.stats.powders",
                "notenoughcalculator.help.stats.powders_desc",
                "notenoughcalculator.help.stats.powders_ex",
                null,
                "notenoughcalculator.help.stats.essences",
                "notenoughcalculator.help.stats.essences_desc",
                "notenoughcalculator.help.stats.essences_ex",
                null,
                "notenoughcalculator.help.stats.pets_bestiary",
                "notenoughcalculator.help.stats.pets_bestiary_desc",
                "notenoughcalculator.help.stats.pets_bestiary_ex",
                null,
                "notenoughcalculator.help.stats.player",
                "notenoughcalculator.help.stats.player_desc",
                "notenoughcalculator.help.stats.player_ex",
                null,
                "notenoughcalculator.help.stats.skills",
                "notenoughcalculator.help.stats.skills_desc",
                "notenoughcalculator.help.stats.slayer_desc",
                "notenoughcalculator.help.stats.skills_ex",
                null,
                "@main:notenoughcalculator.help.back");
    }

    public static void showMarketHelp(CommandContext<FabricClientCommandSource> ctx) {
        sendBlock(ctx,
                "notenoughcalculator.help.market.title",
                null,
                "notenoughcalculator.help.market.queries",
                "notenoughcalculator.help.market.bzb",
                "notenoughcalculator.help.market.bzs",
                "notenoughcalculator.help.market.bzm",
                "notenoughcalculator.help.market.lb",
                "notenoughcalculator.help.market.lba",
                "notenoughcalculator.help.market.npc",
                "notenoughcalculator.help.market.motes",
                "notenoughcalculator.help.market.price",
                "notenoughcalculator.help.market.sack",
                null,
                "notenoughcalculator.help.market.quotes_note",
                null,
                "@main:notenoughcalculator.help.back");
    }

    public static void showTaxHelp(CommandContext<FabricClientCommandSource> ctx) {
        sendBlock(ctx,
                "notenoughcalculator.help.tax.title",
                null,
                "notenoughcalculator.help.tax.bazaar",
                "notenoughcalculator.help.tax.bz_desc",
                "notenoughcalculator.help.tax.bz_ex1",
                "notenoughcalculator.help.tax.bz_ex2",
                null,
                "notenoughcalculator.help.tax.ah",
                "notenoughcalculator.help.tax.ah_desc",
                "notenoughcalculator.help.tax.ah_ex1",
                "notenoughcalculator.help.tax.ah_ex2",
                "notenoughcalculator.help.tax.ah_ex3",
                null,
                "@main:notenoughcalculator.help.back");
    }

    public static void showUnitsHelp(CommandContext<FabricClientCommandSource> ctx) {
        sendBlock(ctx,
                "notenoughcalculator.help.units.title",
                null,
                "notenoughcalculator.help.units.currency",
                "notenoughcalculator.help.units.currency_k",
                "notenoughcalculator.help.units.currency_m",
                "notenoughcalculator.help.units.currency_b",
                "notenoughcalculator.help.units.currency_t",
                null,
                "notenoughcalculator.help.units.item",
                "notenoughcalculator.help.units.item_s",
                "notenoughcalculator.help.units.item_e",
                null,
                "notenoughcalculator.help.units.storage",
                "notenoughcalculator.help.units.storage_h",
                "notenoughcalculator.help.units.storage_sc",
                "notenoughcalculator.help.units.storage_dc",
                "notenoughcalculator.help.units.storage_eb",
                null,
                "notenoughcalculator.help.units.usage",
                "notenoughcalculator.help.units.usage_1",
                "notenoughcalculator.help.units.usage_2",
                "notenoughcalculator.help.units.usage_3",
                "notenoughcalculator.help.units.usage_4",
                null,
                "notenoughcalculator.help.units.tips",
                "notenoughcalculator.help.units.tips_1",
                "notenoughcalculator.help.units.tips_2",
                "notenoughcalculator.help.units.tips_3",
                null,
                "@main:notenoughcalculator.help.back");
    }

    public static void showVariablesHelp(CommandContext<FabricClientCommandSource> ctx) {
        sendBlock(ctx,
                "notenoughcalculator.help.variables.title",
                null,
                "notenoughcalculator.help.variables.builtin",
                "notenoughcalculator.help.variables.builtin_ans",
                "notenoughcalculator.help.variables.builtin_pi",
                "notenoughcalculator.help.variables.builtin_e",
                "notenoughcalculator.help.variables.builtin_api",
                null,
                "notenoughcalculator.help.variables.custom",
                "notenoughcalculator.help.variables.custom_desc",
                null,
                "notenoughcalculator.help.variables.setting",
                "notenoughcalculator.help.variables.setting_example_1",
                "notenoughcalculator.help.variables.setting_example_2",
                null,
                "notenoughcalculator.help.variables.using",
                "notenoughcalculator.help.variables.using_example_1",
                "notenoughcalculator.help.variables.using_example_2",
                null,
                "notenoughcalculator.help.variables.practical",
                "notenoughcalculator.help.variables.practical_1_title",
                "notenoughcalculator.help.variables.practical_1_step1",
                "notenoughcalculator.help.variables.practical_1_step2",
                "notenoughcalculator.help.variables.practical_1_step3",
                null,
                "notenoughcalculator.help.variables.practical_2_title",
                "notenoughcalculator.help.variables.practical_2_step1",
                "notenoughcalculator.help.variables.practical_2_step2",
                "notenoughcalculator.help.variables.practical_2_step3");
        sendEmpty(ctx);
        send(ctx, "notenoughcalculator.help.variables.current");
        String vars = NotEnoughCalculatorClient.getCalculatorManager().getVariablesInfo();
        send(ctx, "notenoughcalculator.help.variables.current_format", vars);
        sendEmpty(ctx);
        sendClickablePage(ctx, "main", "notenoughcalculator.help.back");
    }

    public static void showExamplesHelp(CommandContext<FabricClientCommandSource> ctx) {
        sendBlock(ctx,
                "notenoughcalculator.help.examples.title",
                null,
                "notenoughcalculator.help.examples.auction",
                "notenoughcalculator.help.examples.auction_1",
                "notenoughcalculator.help.examples.auction_1_ex",
                "notenoughcalculator.help.examples.auction_2",
                "notenoughcalculator.help.examples.auction_2_ex",
                null,
                "notenoughcalculator.help.examples.inventory",
                "notenoughcalculator.help.examples.inventory_1",
                "notenoughcalculator.help.examples.inventory_1_ex",
                "notenoughcalculator.help.examples.inventory_2",
                "notenoughcalculator.help.examples.inventory_2_ex",
                null,
                "notenoughcalculator.help.examples.mining",
                "notenoughcalculator.help.examples.mining_1",
                "notenoughcalculator.help.examples.mining_1_ex",
                "notenoughcalculator.help.examples.mining_2",
                "notenoughcalculator.help.examples.mining_2_ex",
                null,
                "notenoughcalculator.help.examples.crafting",
                "notenoughcalculator.help.examples.crafting_1",
                "notenoughcalculator.help.examples.crafting_1_ex",
                "notenoughcalculator.help.examples.crafting_2",
                "notenoughcalculator.help.examples.crafting_2_ex",
                null,
                "notenoughcalculator.help.examples.complex",
                "notenoughcalculator.help.examples.complex_1",
                "notenoughcalculator.help.examples.complex_1_ex",
                "notenoughcalculator.help.examples.complex_2",
                "notenoughcalculator.help.examples.complex_2_ex",
                null,
                "notenoughcalculator.help.examples.tips",
                "notenoughcalculator.help.examples.tips_1",
                "notenoughcalculator.help.examples.tips_2",
                "notenoughcalculator.help.examples.tips_3",
                "notenoughcalculator.help.examples.tips_4",
                null,
                "@main:notenoughcalculator.help.back");
    }

    public static void showConfigHelp(CommandContext<FabricClientCommandSource> ctx) {
        sendBlock(ctx,
                "notenoughcalculator.help.config.title",
                null,
                "notenoughcalculator.help.config.file",
                "notenoughcalculator.help.config.file_location",
                "notenoughcalculator.help.config.file_edit",
                "notenoughcalculator.help.config.file_edit_1",
                "notenoughcalculator.help.config.file_edit_2",
                "notenoughcalculator.help.config.file_edit_3",
                "notenoughcalculator.help.config.file_edit_4",
                "notenoughcalculator.help.config.file_edit_5",
                null,
                "notenoughcalculator.help.config.current",
                "notenoughcalculator.help.config.current_cmd",
                null,
                "@main:notenoughcalculator.help.back");
    }
}
