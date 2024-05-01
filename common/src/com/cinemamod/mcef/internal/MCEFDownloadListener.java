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

package com.cinemamod.mcef.internal;

import org.spongepowered.asm.mixin.Unique;

public class MCEFDownloadListener {
    // TODO: I kinda would like to keep other mods from accessing this, but mixin complicates stuff
    @Unique
    public static final MCEFDownloadListener INSTANCE = new MCEFDownloadListener();

    private String task;
    private float percent;
    private boolean done;
    private boolean failed;

    public void setTask(String name) {
        this.task = name;
        this.percent = 0;
    }

    public String getTask() {
        return task;
    }

    public void setProgress(float percent) {
        this.percent = percent;
    }

    public float getProgress() {
        return percent;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public boolean isDone() {
        return done;
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
    }

    public boolean isFailed() {
        return failed;
    }
}
