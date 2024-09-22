package com.cinemamod.mcef.mixins;

import com.cinemamod.mcef.MCEF;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Mixin(Minecraft.class)
public class CefShutdownMixin {
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
                    ProcessBuilder killProcess = new ProcessBuilder("taskkill", "/F", "/IM", processName);
                    killProcess.start();
                }
            } catch (IOException e) {
                MCEF.getLogger().error("JCEF Process Check Failed. There still may be lingering processes running in the background and eating up system resources. Please report this error to the developer.", e);
            }
        }
    }
}
