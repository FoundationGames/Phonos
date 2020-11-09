package io.github.foundationgames.phonos.network;

import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;

public enum RecieverStorageOperation {

    ADD((byte)0x00), REMOVE((byte)0x01), CLEAR((byte)0x02);

    private final byte id;

    RecieverStorageOperation(byte id) {
        this.id = id;
        MapContainer.valueMap.put(this.id, this);
    }

    public byte asByte() {
        return id;
    }

    public static RecieverStorageOperation fromByte(byte from) {
        return MapContainer.valueMap.get(from);
    }

    private static class MapContainer {
        private static final Byte2ObjectMap<RecieverStorageOperation> valueMap = new Byte2ObjectOpenHashMap<>();
    }
}
