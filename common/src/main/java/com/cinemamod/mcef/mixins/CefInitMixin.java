/*
 *     MCEF (Minecraft Chromium Embedded Framework)
 *     Copyright (C) 2023 CinemaMod Group
 *
 *     This library is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU Lesser General Public
 *     License as published by the Free Software Foundation; either
 *     version 2.1 of the License, or (at your option) any later version.
 *
 *     This library is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *     Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public
 *     License along with this library; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 *     USA
 */

package com.cinemamod.mcef.mixins;

import com.cinemamod.mcef.MCEF;
import com.cinemamod.mcef.internal.MCEFDownloadListener;
import com.cinemamod.mcef.internal.MCEFDownloaderMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.SafetyScreen;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.EditGameRulesScreen;
import net.minecraft.client.gui.screens.worldselection.ExperimentsScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(Minecraft.class)
public abstract class CefInitMixin {
    @Shadow
    public abstract void setScreen(@Nullable Screen guiScreen);

    @Unique
    private static AtomicBoolean recursionDetector = new AtomicBoolean(false);

    @Inject(at = @At("HEAD"), method = "setScreen", cancellable = true)
    public void redirScreen(Screen guiScreen, CallbackInfo ci) {
        if (!MCEF.isInitialized()) {
            boolean recursionValue = recursionDetector.get();
            recursionDetector.set(true);

            // regardless of what screen the game opens to, MCEF must try to initialize
            // if it does not, there are bigger problems
            if (
                // mods may try to set the screen before the first screen opens
                // in the event that this happens, recursion would happen
                // so if this is detected, not try to open the screen again, as that could cause a crash
                    !recursionValue ||
                            guiScreen instanceof TitleScreen ||
                            guiScreen instanceof LevelLoadingScreen ||
                            guiScreen instanceof ReceivingLevelScreen ||
                            guiScreen instanceof SelectWorldScreen ||
                            guiScreen instanceof DirectJoinServerScreen ||
                            guiScreen instanceof EditServerScreen ||
                            guiScreen instanceof ConnectScreen ||
                            guiScreen instanceof AccessibilityOnboardingScreen ||
                            guiScreen instanceof SafetyScreen ||
                            guiScreen instanceof JoinMultiplayerScreen ||
                            guiScreen instanceof CreateWorldScreen ||
                            guiScreen instanceof EditGameRulesScreen ||
                            guiScreen instanceof ExperimentsScreen ||
                            guiScreen instanceof PackSelectionScreen ||
                            guiScreen instanceof CreateFlatWorldScreen ||
                            guiScreen instanceof CreateBuffetWorldScreen
            ) {
                // If the download is done and didn't fail
                if (MCEFDownloadListener.INSTANCE.isDone() && !MCEFDownloadListener.INSTANCE.isFailed()) {
                    MCEF.getLogger().debug("MCEF already finished downloading, scheduling loading.");
                    Minecraft.getInstance().execute((() -> {
                        MCEF.getLogger().debug("MCEF is attempting to load.");
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            MCEF.getLogger().error("I don't even know what occurred here.", e);
                        }
                        MCEF.initialize();
                    }));
                }
                // If the download is not done and didn't fail
                else if (!MCEFDownloadListener.INSTANCE.isDone() && !MCEFDownloadListener.INSTANCE.isFailed()) {
                    MCEF.getLogger().debug("MCEF has not finished loading, displaying loading screen.");
                    setScreen(new MCEFDownloaderMenu((TitleScreen) guiScreen));
                    ci.cancel();
                }
                // If the download failed
                else if (MCEFDownloadListener.INSTANCE.isFailed()) {
                    MCEF.getLogger().error("MCEF failed to initialize!");
                }
            }

            recursionDetector.set(recursionValue);
        }
    }
}
