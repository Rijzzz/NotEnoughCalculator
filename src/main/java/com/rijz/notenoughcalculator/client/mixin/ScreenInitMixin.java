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

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Screen.class, priority = 1)
public abstract class ScreenInitMixin {

    @Shadow public Minecraft minecraft;
    @Shadow public int width;
    @Shadow public int height;

    private void ensureScreenFieldsInitialized() {
        if (this.minecraft == null) {
            this.minecraft = Minecraft.getInstance();
        }
        if (this.minecraft != null && this.minecraft.getWindow() != null) {
            if (this.width <= 0) {
                this.width = this.minecraft.getWindow().getGuiScaledWidth();
            }
            if (this.height <= 0) {
                this.height = this.minecraft.getWindow().getGuiScaledHeight();
            }
        }
    }

    @Inject(method = "ensureEventsAreInitialized", at = @At("HEAD"), remap = false, require = 0)
    private void onEnsureEventsReturn(CallbackInfoReturnable<?> cir) {
        ensureScreenFieldsInitialized();
    }

    @Inject(method = "fabric_ensureEventsAreInitialized", at = @At("HEAD"), remap = false, require = 0)
    private void onFabricEnsureEventsReturn(CallbackInfoReturnable<?> cir) {
        ensureScreenFieldsInitialized();
    }

    @Inject(method = "fabric_getAllowCharTypeEvent", at = @At("HEAD"), remap = false, require = 0)
    private void onFabricGetAllowCharTypeEvent(CallbackInfoReturnable<?> cir) {
        ensureScreenFieldsInitialized();
    }
}
