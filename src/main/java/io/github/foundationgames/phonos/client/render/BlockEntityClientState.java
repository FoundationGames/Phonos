package io.github.foundationgames.phonos.client.render;

import io.github.foundationgames.phonos.world.sound.CableConnection;
import io.github.foundationgames.phonos.world.sound.block.BlockEntityOutputs;
import net.minecraft.client.gl.VertexBuffer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BlockEntityClientState {
    public boolean dirty;
    public @Nullable VertexBuffer buffer = null;
    private List<CableConnection> cachedCons = new ArrayList<>();

    public void genState(BlockEntityOutputs outs) {
        List<CableConnection> connections = new ArrayList<>();
        outs.forEach((i, o) -> connections.add(o));

        if (!cachedCons.equals(connections) || this.buffer == null) {
            // Mark ourselves as needing re-rendering
            dirty = true;

            // Close the existing buffer
            if (buffer != null) {
                buffer.close();
            }

            buffer = null;
            cachedCons = connections;
        }
    }

    public void close() {
        if (buffer != null) {
            buffer.close();
        }

        buffer = null;
    }
}
