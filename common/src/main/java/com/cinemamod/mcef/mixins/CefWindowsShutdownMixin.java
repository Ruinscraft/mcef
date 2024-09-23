package com.cinemamod.mcef.mixins;

import com.cinemamod.mcef.MCEF;
import com.cinemamod.mcef.MCEFPlatform;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Mixin(Minecraft.class)
public class CefWindowsShutdownMixin {
    /**
     * Temporary workaround to address lingering JCEF processes on Windows.
     * @author Blobanium
     */
    @Inject(at = @At("TAIL"), method = "close")
    public void close(CallbackInfo ci) {
        if (MCEFPlatform.getPlatform().isWindows()) {
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
                    MCEF.getLogger().warn("JCEF is still running, killing to avoid lingering processes.");
                    ProcessBuilder killProcess = new ProcessBuilder("taskkill", "/F", "/IM", processName);
                    killProcess.start();
                }
            } catch (Exception e) {
                MCEF.getLogger().error("Unable to check if JCEF is still running. There may be lingering processes.", e);
            }
        }
    }
}
