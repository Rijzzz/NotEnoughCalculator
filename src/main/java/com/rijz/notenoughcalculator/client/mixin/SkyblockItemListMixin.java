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

package com.rijz.notenoughcalculator.client.mixin;

import com.rijz.notenoughcalculator.client.NotEnoughCalculatorClient;
import com.rijz.notenoughcalculator.config.CalculatorConfig;
import com.rijz.notenoughcalculator.core.ExpressionEvaluator.EvalResult;
import com.rijz.notenoughcalculator.core.ResultFormatter;
import kotlin.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.operationpotato.itemlist.utils.CalcUtils", remap = false)
public class SkyblockItemListMixin {

    @Inject(method = "isExpression", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void onIsExpression(String text, CallbackInfoReturnable<Boolean> cir) {
        if (CalculatorConfig.getInstance().enableItemListIntegration && !CalculatorConfig.getInstance().forceStandaloneMode) {
            // Return false so SkyBlock Item List does not draw its basic native yellow text.
            // StandaloneOverlayRenderer will render mostly everything
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "isExpression", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void onIsExpressionStatic(String text, CallbackInfoReturnable<Boolean> cir) {
        if (CalculatorConfig.getInstance().enableItemListIntegration && !CalculatorConfig.getInstance().forceStandaloneMode) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "calculateExpression", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void onCalculateExpression(String text, CallbackInfoReturnable<Pair<String, Boolean>> cir) {
        if (CalculatorConfig.getInstance().enableItemListIntegration && !CalculatorConfig.getInstance().forceStandaloneMode) {
            handleCalculateExpression(text, cir);
        }
    }

    @Inject(method = "calculateExpression", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void onCalculateExpressionStatic(String text, CallbackInfoReturnable<Pair<String, Boolean>> cir) {
        if (CalculatorConfig.getInstance().enableItemListIntegration && !CalculatorConfig.getInstance().forceStandaloneMode) {
            handleCalculateExpression(text, cir);
        }
    }

    private static void handleCalculateExpression(String text, CallbackInfoReturnable<Pair<String, Boolean>> cir) {
        try {
            String cleanInput = ResultFormatter.cleanInput(text);
            EvalResult evalResult = NotEnoughCalculatorClient.getCalculatorManager().getEvaluator()
                    .evaluateQuietResult(cleanInput);
            String formatted = ResultFormatter.formatResult(evalResult);
            Pair<String, Boolean> pair = new Pair<>(formatted != null ? formatted : "", true);
            cir.setReturnValue(pair);
        } catch (Exception e) {
            boolean isExplicit = text != null && text.trim().startsWith("=");
            if (isExplicit) {
                Pair<String, Boolean> pair = new Pair<>("ERR: " + e.getMessage(), false);
                cir.setReturnValue(pair);
            } else {
                Pair<String, Boolean> pair = new Pair<>("", false);
                cir.setReturnValue(pair);
            }
        }
    }
}
