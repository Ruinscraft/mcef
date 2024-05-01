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

package com.cinemamod.mcef;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.MinecraftClient;

public class MCEFSettings {
    private static final Path PATH = MinecraftClient.getInstance().runDirectory
            .toPath()
            .resolve("config")
            .resolve("mcef")
            .resolve("mcef.properties");
    private static int deleteRetries = 0;

    private boolean skipDownload;
    private String downloadMirror;
    private String userAgent;
    private boolean useCache;

    public MCEFSettings() {
        skipDownload = false;
        downloadMirror = "https://mcef-download.cinemamod.com";
        userAgent = null;
        useCache = true;
    }

    public boolean isSkipDownload() {
        return skipDownload;
    }

    public void setSkipDownload(boolean skipDownload) {
        this.skipDownload = skipDownload;
        saveAsync();
    }

    public String getDownloadMirror() {
        return downloadMirror;
    }

    public void setDownloadMirror(String downloadMirror) {
        this.downloadMirror = downloadMirror;
        saveAsync();
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
        saveAsync();
    }

    public boolean isUsingCache() {
        return useCache;
    }

    public void setUseCache(boolean useCache) {
        this.useCache = useCache;
        saveAsync();
    }

    public void saveAsync() {
        CompletableFuture.runAsync(() -> {
            try {
                save();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void save() throws IOException {
        File file = PATH.toFile();

        file.getParentFile().mkdirs();

        if (!file.exists()) {
            file.createNewFile();
        }

        Properties properties = new Properties();
        properties.setProperty("skip-download", String.valueOf(skipDownload));
        properties.setProperty("download-mirror", String.valueOf(downloadMirror));
        properties.setProperty("user-agent", String.valueOf(userAgent));
        properties.setProperty("use-cache", String.valueOf(useCache));

        try (FileOutputStream output = new FileOutputStream(file)) {
            properties.store(output, null);
        }
    }

    public void load() throws IOException {
        File file = PATH.toFile();

        if (!file.exists()) {
            save();
        }

        Properties properties = new Properties();

        try (FileInputStream input = new FileInputStream(file)) {
            properties.load(input);
        }

        try {
            skipDownload = Boolean.parseBoolean(properties.getProperty("skip-download"));
            downloadMirror = properties.getProperty("download-mirror");
            userAgent = properties.getProperty("user-agent");
            useCache = Boolean.parseBoolean(properties.getProperty("use-cache"));
        } catch (Exception e) {
            // Delete and re-create the file if there was a parsing error
            if (deleteRetries++ > 20)
                return; // Stop gap
            file.delete();
            save();
        }
    }
}
