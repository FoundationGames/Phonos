package io.github.foundationgames.phonos;

import io.github.foundationgames.phonos.block.PhonosBlocks;
import io.github.foundationgames.phonos.item.ItemGroupQueue;
import io.github.foundationgames.phonos.item.PhonosItems;
import io.github.foundationgames.phonos.network.PayloadPackets;
import io.github.foundationgames.phonos.radio.RadioDevice;
import io.github.foundationgames.phonos.radio.RadioStorage;
import io.github.foundationgames.phonos.sound.SoundStorage;
import io.github.foundationgames.phonos.sound.emitter.SoundEmitter;
import io.github.foundationgames.phonos.sound.emitter.SoundEmitterStorage;
import io.github.foundationgames.phonos.world.sound.InputPlugPoint;
import io.github.foundationgames.phonos.world.sound.data.SoundDataTypes;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Phonos implements ModInitializer {
    public static final Logger LOG = LogManager.getLogger("phonos");

    public static final ItemGroupQueue PHONOS_ITEMS = new ItemGroupQueue(id("phonos"));

    @Override
    public void onInitialize() {
        Registry.register(Registries.ITEM_GROUP, PHONOS_ITEMS.id, FabricItemGroup.builder()
                .icon(PhonosBlocks.LOUDSPEAKER.asItem()::getDefaultStack)
                .displayName(PHONOS_ITEMS.displayName())
                .entries(PHONOS_ITEMS)
                .build());

        PayloadPackets.initCommon();

        PhonosBlocks.init();
        PhonosItems.init();

        SoundDataTypes.init();
        InputPlugPoint.init();

        ServerLifecycleEvents.SERVER_STARTING.register(e -> {
            RadioStorage.serverReset();
            SoundStorage.serverReset();
            SoundEmitterStorage.serverReset();
        });
        ServerTickEvents.END_WORLD_TICK.register(world -> SoundStorage.getInstance(world).tick(world));

        ServerBlockEntityEvents.BLOCK_ENTITY_LOAD.register((be, world) -> {
            if (be instanceof SoundEmitter p) {
                SoundEmitterStorage.getInstance(world).addEmitter(p);
            }
            if (be instanceof RadioDevice.Receiver rec) {
                rec.setAndUpdateChannel(rec.getChannel());
            }
        });
        ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register((be, world) -> {
            if (be instanceof SoundEmitter p) {
                SoundEmitterStorage.getInstance(world).removeEmitter(p);
            }
            if (be instanceof RadioDevice.Receiver rec) {
                rec.removeReceiver();
            }
        });

        RadioStorage.init();
    }

    public static Identifier id(String path) {
        return new Identifier("phonos", path);
    }

    public static float getPhonosVolume() {
        return 1;
    }
}
