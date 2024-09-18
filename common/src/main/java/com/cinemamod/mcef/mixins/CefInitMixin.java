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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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



    /**
     * Temporary Workaround to address lingering JCEF processes.
     * @author Blobanium
     */
    @Inject(at = @At("TAIL"), method = "close")
    public void close(CallbackInfo ci) {
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            String processName = "jcef_helper.exe";
            try {
                ProcessBuilder processBuilder = new ProcessBuilder("tasklist");
                Process process = processBuilder.start();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                boolean isRunning = false;
                while ((line = reader.readLine()) != null) {
                    if (line.contains(processName)) {
                        isRunning = true;
                        break;
                    }
                }
                reader.close();

                if (isRunning) {
                    MCEF.getLogger().warn("JCEF Processes are still running when they should have been shut down, attempting to close them to ensure processes are terminated and dont use up CPU resources after closing minecraft.");
                    ProcessBuilder killProcess = new ProcessBuilder("taskkill", "/IM", processName);
                    killProcess.start();
                }
            } catch (IOException e) {
                MCEF.getLogger().error("JCEF Process Check Failed. There still may be lingering processes running in the background and eating up system resources. Please report this error to the developer.", e);
            }
        }
    }
}
