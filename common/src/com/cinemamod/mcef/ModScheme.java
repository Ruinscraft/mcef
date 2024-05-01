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

import com.mojang.logging.LogUtils;
import org.cef.callback.CefCallback;
import org.cef.handler.CefResourceHandler;
import org.cef.misc.IntRef;
import org.cef.misc.StringRef;
import org.cef.network.CefRequest;
import org.cef.network.CefResponse;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

// https://github.com/CinemaMod/mcef/blob/master-1.19.2/src/main/java/net/montoyo/mcef/example/ModScheme.java
public class ModScheme implements CefResourceHandler {
    private String contentType = null;
    private InputStream is = null;

    private final String url;

    public ModScheme(String url) {
        this.url = url;
    }

    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public boolean processRequest(CefRequest cefRequest, CefCallback cefCallback) {
        String url = this.url.substring("mod://".length());

        int pos = url.indexOf('/');
        if (pos < 0) {
            cefCallback.cancel();
            return false;
        }

        String mod = removeSlashes(url.substring(0, pos));
        String loc = removeSlashes(url.substring(pos + 1));

        if (mod.length() <= 0 || loc.length() <= 0 || mod.charAt(0) == '.' || loc.charAt(0) == '.') {
            LOGGER.warn("Invalid URL " + url);
            cefCallback.cancel();
            return false;
        }

        // TODO: this may or may not require forge/fabric specific code?
//        is = ModList.get().getModContainerById(mod).get().getMod().getClass().getResourceAsStream("/assets/" + mod.toLowerCase(Locale.US) + "/html/" + loc.toLowerCase());
        is = ModScheme.class.getClassLoader().getResourceAsStream("/assets/" + mod.toLowerCase(Locale.US) + "/html/" + loc.toLowerCase(Locale.US));
        if (is == null) {
            LOGGER.warn("Resource " + url + " NOT found!");
            cefCallback.cancel();
            return false; // TODO: 404?
        }

        contentType = null;
        pos = loc.lastIndexOf('.');
        if (pos >= 0 && pos < loc.length() - 2)
            contentType = MIMEUtil.mimeFromExtension(loc.substring(pos + 1));

        cefCallback.Continue();
        return true;
    }

    private String removeSlashes(String loc) {
        int i = 0;
        while (i < loc.length() && loc.charAt(i) == '/')
            i++;

        return loc.substring(i);
    }

    @Override
    public void getResponseHeaders(CefResponse cefResponse, IntRef contentLength, StringRef redir) {
        if (contentType != null)
            cefResponse.setMimeType(contentType);

        cefResponse.setStatus(200);
        cefResponse.setStatusText("OK");
        contentLength.set(0);
    }

    @Override
    public boolean readResponse(byte[] output, int bytesToRead, IntRef bytesRead, CefCallback cefCallback) {
        try {
            int ret = is.read(output, 0, bytesToRead);
            if (ret <= 0) {
                is.close();
                // 0 bytes read indicates to CEF/JCEF that there is no more data to read
                bytesRead.set(0);
                return false;
            }

            // tell CEF/JCEF how many bytes were read
            bytesRead.set(ret);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            // attempt to close the stream if possible
            try {
                is.close();
            } catch (Throwable ignored) {
            }

            return false;
        }
    }

    @Override
    public void cancel() {
        // attempt to free resources, just incase
        try {
            is.close();
        } catch (Throwable ignored) {
        }
    }
}
