package com.kettle.livingarmor.savedata;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

public class CursedKingSavedData extends SavedData {
    private static final String LAST_EVENT_TIME_KEY = "LastEventTime";
    private long lastEventTime;

    public CursedKingSavedData() {
        this.lastEventTime = 0; // Default to 0 if not found
    }

    public long getLastEventTime() {
        return lastEventTime;
    }

    public void setLastEventTime(long time) {
        this.lastEventTime = time;
        this.setDirty(); // Marks the data as changed so Minecraft saves it
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putLong(LAST_EVENT_TIME_KEY, lastEventTime);
        return tag;
    }

    public static CursedKingSavedData load(CompoundTag tag) {
        CursedKingSavedData data = new CursedKingSavedData();
        data.lastEventTime = tag.getLong(LAST_EVENT_TIME_KEY);
        return data;
    }
}