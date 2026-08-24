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
        send(ctx, "notenoughcalculator.help.main.cmd_calcpos");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.main.help_pages");
        sendClickablePage(ctx, "operators", "notenoughcalculator.help.main.page_operators");
        sendClickablePage(ctx, "functions", "notenoughcalculator.help.main.page_functions");
        sendClickablePage(ctx, "units", "notenoughcalculator.help.main.page_units");
        sendClickablePage(ctx, "variables", "notenoughcalculator.help.main.page_variables");
        sendClickablePage(ctx, "stats", "notenoughcalculator.help.main.page_stats");
        sendClickablePage(ctx, "market", "notenoughcalculator.help.main.page_market");
        sendClickablePage(ctx, "tax", "notenoughcalculator.help.main.page_tax");
        sendClickablePage(ctx, "examples", "notenoughcalculator.help.main.page_examples");
        sendClickablePage(ctx, "config", "notenoughcalculator.help.main.page_config");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.main.keyboard_shortcuts");
        send(ctx, "notenoughcalculator.help.main.keyboard_shortcuts_1");
        send(ctx, "notenoughcalculator.help.main.keyboard_shortcuts_2");
        send(ctx, "notenoughcalculator.help.main.keyboard_shortcuts_3");
        send(ctx, "notenoughcalculator.help.main.keyboard_shortcuts_4");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.main.footer");
    }

    public static void showOperatorsHelp(CommandContext<FabricClientCommandSource> ctx) {
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

        send(ctx, "notenoughcalculator.help.operators.bitwise");
        send(ctx, "notenoughcalculator.help.operators.bitwise_and");
        send(ctx, "notenoughcalculator.help.operators.bitwise_or");
        send(ctx, "notenoughcalculator.help.operators.bitwise_not");
        send(ctx, "notenoughcalculator.help.operators.bitwise_lshift");
        send(ctx, "notenoughcalculator.help.operators.bitwise_rshift");
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
        send(ctx, "notenoughcalculator.help.operators.pemdas_5");
        sendEmpty(ctx);

        sendClickablePage(ctx, "main", "notenoughcalculator.help.back");
    }

    public static void showFunctionsHelp(CommandContext<FabricClientCommandSource> ctx) {
        send(ctx, "notenoughcalculator.help.functions.title");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.functions.available");
        send(ctx, "notenoughcalculator.help.functions.sqrt");
        send(ctx, "notenoughcalculator.help.functions.abs");
        send(ctx, "notenoughcalculator.help.functions.floor");
        send(ctx, "notenoughcalculator.help.functions.ceil");
        send(ctx, "notenoughcalculator.help.functions.round");
        send(ctx, "notenoughcalculator.help.functions.log");
        send(ctx, "notenoughcalculator.help.functions.ln");
        send(ctx, "notenoughcalculator.help.functions.sin");
        send(ctx, "notenoughcalculator.help.functions.cos");
        send(ctx, "notenoughcalculator.help.functions.tan");
        send(ctx, "notenoughcalculator.help.functions.min");
        send(ctx, "notenoughcalculator.help.functions.max");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.functions.radix");
        send(ctx, "notenoughcalculator.help.functions.hex");
        send(ctx, "notenoughcalculator.help.functions.bin");
        send(ctx, "notenoughcalculator.help.functions.oct");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.functions.math_helpers");
        send(ctx, "notenoughcalculator.help.functions.pct");
        send(ctx, "notenoughcalculator.help.functions.gcd");
        send(ctx, "notenoughcalculator.help.functions.lcm");
        send(ctx, "notenoughcalculator.help.functions.clamp");
        send(ctx, "notenoughcalculator.help.functions.avg");
        send(ctx, "notenoughcalculator.help.functions.xor");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.functions.xp_tables");
        send(ctx, "notenoughcalculator.help.functions.skillxp");
        send(ctx, "notenoughcalculator.help.functions.huntingxp");
        send(ctx, "notenoughcalculator.help.functions.runecraftingxp");
        send(ctx, "notenoughcalculator.help.functions.socialxp");
        send(ctx, "notenoughcalculator.help.functions.cataxp");
        send(ctx, "notenoughcalculator.help.functions.slayerxp");
        send(ctx, "notenoughcalculator.help.functions.emanxp");
        send(ctx, "notenoughcalculator.help.functions.vampirexp");
        send(ctx, "notenoughcalculator.help.functions.perk");
        sendEmpty(ctx);

        sendClickablePage(ctx, "main", "notenoughcalculator.help.back");
    }

    public static void showStatsHelp(CommandContext<FabricClientCommandSource> ctx) {
        send(ctx, "notenoughcalculator.help.stats.title");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.stats.currency");
        send(ctx, "notenoughcalculator.help.stats.currency_desc");
        send(ctx, "notenoughcalculator.help.stats.currency_ex");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.stats.powders");
        send(ctx, "notenoughcalculator.help.stats.powders_desc");
        send(ctx, "notenoughcalculator.help.stats.powders_ex");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.stats.essences");
        send(ctx, "notenoughcalculator.help.stats.essences_desc");
        send(ctx, "notenoughcalculator.help.stats.essences_ex");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.stats.pets_bestiary");
        send(ctx, "notenoughcalculator.help.stats.pets_bestiary_desc");
        send(ctx, "notenoughcalculator.help.stats.pets_bestiary_ex");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.stats.player");
        send(ctx, "notenoughcalculator.help.stats.player_desc");
        send(ctx, "notenoughcalculator.help.stats.player_ex");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.stats.skills");
        send(ctx, "notenoughcalculator.help.stats.skills_desc");
        send(ctx, "notenoughcalculator.help.stats.slayer_desc");
        send(ctx, "notenoughcalculator.help.stats.skills_ex");
        sendEmpty(ctx);

        sendClickablePage(ctx, "main", "notenoughcalculator.help.back");
    }

    public static void showMarketHelp(CommandContext<FabricClientCommandSource> ctx) {
        send(ctx, "notenoughcalculator.help.market.title");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.market.queries");
        send(ctx, "notenoughcalculator.help.market.bzb");
        send(ctx, "notenoughcalculator.help.market.bzs");
        send(ctx, "notenoughcalculator.help.market.bzm");
        send(ctx, "notenoughcalculator.help.market.lb");
        send(ctx, "notenoughcalculator.help.market.lba");
        send(ctx, "notenoughcalculator.help.market.npc");
        send(ctx, "notenoughcalculator.help.market.motes");
        send(ctx, "notenoughcalculator.help.market.price");
        send(ctx, "notenoughcalculator.help.market.sack");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.market.quotes_note");
        sendEmpty(ctx);

        sendClickablePage(ctx, "main", "notenoughcalculator.help.back");
    }

    public static void showTaxHelp(CommandContext<FabricClientCommandSource> ctx) {
        send(ctx, "notenoughcalculator.help.tax.title");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.tax.bazaar");
        send(ctx, "notenoughcalculator.help.tax.bz_desc");
        send(ctx, "notenoughcalculator.help.tax.bz_ex1");
        send(ctx, "notenoughcalculator.help.tax.bz_ex2");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.tax.ah");
        send(ctx, "notenoughcalculator.help.tax.ah_desc");
        send(ctx, "notenoughcalculator.help.tax.ah_ex1");
        send(ctx, "notenoughcalculator.help.tax.ah_ex2");
        send(ctx, "notenoughcalculator.help.tax.ah_ex3");
        sendEmpty(ctx);

        sendClickablePage(ctx, "main", "notenoughcalculator.help.back");
    }

    public static void showUnitsHelp(CommandContext<FabricClientCommandSource> ctx) {
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

        sendClickablePage(ctx, "main", "notenoughcalculator.help.back");
    }

    public static void showVariablesHelp(CommandContext<FabricClientCommandSource> ctx) {
        send(ctx, "notenoughcalculator.help.variables.title");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.variables.builtin");
        send(ctx, "notenoughcalculator.help.variables.builtin_ans");
        send(ctx, "notenoughcalculator.help.variables.builtin_pi");
        send(ctx, "notenoughcalculator.help.variables.builtin_e");
        send(ctx, "notenoughcalculator.help.variables.builtin_api");
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
        send(ctx, "notenoughcalculator.help.variables.current_format", vars);
        sendEmpty(ctx);

        sendClickablePage(ctx, "main", "notenoughcalculator.help.back");
    }

    public static void showExamplesHelp(CommandContext<FabricClientCommandSource> ctx) {
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

        sendClickablePage(ctx, "main", "notenoughcalculator.help.back");
    }

    public static void showConfigHelp(CommandContext<FabricClientCommandSource> ctx) {
        send(ctx, "notenoughcalculator.help.config.title");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.config.file");
        send(ctx, "notenoughcalculator.help.config.file_location");
        send(ctx, "notenoughcalculator.help.config.file_edit");
        send(ctx, "notenoughcalculator.help.config.file_edit_1");
        send(ctx, "notenoughcalculator.help.config.file_edit_2");
        send(ctx, "notenoughcalculator.help.config.file_edit_3");
        send(ctx, "notenoughcalculator.help.config.file_edit_4");
        send(ctx, "notenoughcalculator.help.config.file_edit_5");
        sendEmpty(ctx);

        send(ctx, "notenoughcalculator.help.config.current");
        send(ctx, "notenoughcalculator.help.config.current_cmd");
        sendEmpty(ctx);

        sendClickablePage(ctx, "main", "notenoughcalculator.help.back");
    }
}
