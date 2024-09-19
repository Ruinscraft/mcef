package com.cinemamod.mcef.mixins;

import com.cinemamod.mcef.MCEF;
import com.cinemamod.mcef.internal.MCEFDownloadListener;
import com.cinemamod.mcef.internal.MCEFDownloaderMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class CefInitMixin {
    @Shadow
    public abstract void setScreen(@Nullable Screen guiScreen);

    @Inject(at = @At("HEAD"), method = "setScreen", cancellable = true)
    public void redirScreen(Screen guiScreen, CallbackInfo ci) {
        if (!MCEF.isInitialized()) {
            if (guiScreen instanceof TitleScreen) {
                // If the download is done and didn't fail
                if (MCEFDownloadListener.INSTANCE.isDone() && !MCEFDownloadListener.INSTANCE.isFailed()) {
                    Minecraft.getInstance().execute((() -> {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        MCEF.initialize();
                    }));
                }
                // If the download is not done and didn't fail
                else if (!MCEFDownloadListener.INSTANCE.isDone() && !MCEFDownloadListener.INSTANCE.isFailed()) {
                    setScreen(new MCEFDownloaderMenu((TitleScreen) guiScreen));
                    ci.cancel();
                }
                // If the download failed
                else if (MCEFDownloadListener.INSTANCE.isFailed()) {
                    MCEF.getLogger().error("MCEF failed to initialize!");
                }
            }
        }
    }
}
