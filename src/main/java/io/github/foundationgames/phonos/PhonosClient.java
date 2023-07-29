package io.github.foundationgames.phonos;

import io.github.foundationgames.jsonem.JsonEM;
import io.github.foundationgames.phonos.block.PhonosBlocks;
import io.github.foundationgames.phonos.client.render.block.CableOutputBlockEntityRenderer;
import io.github.foundationgames.phonos.client.render.block.RadioLoudspeakerBlockEntityRenderer;
import io.github.foundationgames.phonos.client.render.block.RadioTransceiverBlockEntityRenderer;
import io.github.foundationgames.phonos.client.render.block.SatelliteStationBlockEntityRenderer;
import io.github.foundationgames.phonos.config.PhonosClientConfig;
import io.github.foundationgames.phonos.item.AudioCableItem;
import io.github.foundationgames.phonos.item.PhonosItems;
import io.github.foundationgames.phonos.network.ClientPayloadPackets;
import io.github.foundationgames.phonos.radio.RadioDevice;
import io.github.foundationgames.phonos.radio.RadioStorage;
import io.github.foundationgames.phonos.sound.ClientSoundStorage;
import io.github.foundationgames.phonos.sound.SoundStorage;
import io.github.foundationgames.phonos.sound.custom.ClientCustomAudioUploader;
import io.github.foundationgames.phonos.sound.emitter.SoundEmitter;
import io.github.foundationgames.phonos.sound.emitter.SoundEmitterStorage;
import io.github.foundationgames.phonos.sound.stream.ClientIncomingStreamHandler;
import io.github.foundationgames.phonos.util.PhonosUtil;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientBlockEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.state.property.Properties;

public class PhonosClient implements ClientModInitializer {
    public static final EntityModelLayer AUDIO_CABLE_END_LAYER = new EntityModelLayer(Phonos.id("audio_cable_end"), "main");
    public static final EntityModelLayer SATELLITE_LAYER = new EntityModelLayer(Phonos.id("satellite"), "main");

    @Override
    public void onInitializeClient() {
        PhonosClientConfig.get(); // Load if not already

        ClientPayloadPackets.initClient();
        ClientSoundStorage.initClient();

        JsonEM.registerModelLayer(AUDIO_CABLE_END_LAYER);
        JsonEM.registerModelLayer(SATELLITE_LAYER);

        BlockRenderLayerMap.INSTANCE.putBlock(PhonosBlocks.ELECTRONIC_NOTE_BLOCK, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(PhonosBlocks.RADIO_TRANSCEIVER, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(PhonosBlocks.RADIO_LOUDSPEAKER, RenderLayer.getCutout());

        BlockEntityRendererFactories.register(PhonosBlocks.ELECTRONIC_NOTE_BLOCK_ENTITY, CableOutputBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(PhonosBlocks.ELECTRONIC_JUKEBOX_ENTITY, CableOutputBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(PhonosBlocks.CONNECTION_HUB_ENTITY, CableOutputBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(PhonosBlocks.RADIO_TRANSCEIVER_ENTITY, RadioTransceiverBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(PhonosBlocks.RADIO_LOUDSPEAKER_ENTITY, RadioLoudspeakerBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(PhonosBlocks.SATELLITE_STATION_ENTITY, SatelliteStationBlockEntityRenderer::new);

        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) ->
                world != null && pos != null && state != null ?
                        PhonosUtil.getColorFromNote(state.get(Properties.NOTE)) : 0xFFFFFF,
                PhonosBlocks.ELECTRONIC_NOTE_BLOCK);

        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> {
            if (tintIndex == 0 && stack.getItem() instanceof AudioCableItem aud && aud.color != null) {
                return PhonosUtil.DYE_COLORS.getInt(aud.color);
            }

            return 0xFFFFFF;
        }, PhonosItems.ALL_AUDIO_CABLES);

        ClientEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity == MinecraftClient.getInstance().player) {
                RadioStorage.clientReset();
                SoundStorage.clientReset();
                SoundEmitterStorage.clientReset();
            }
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            ClientIncomingStreamHandler.reset();
            ClientCustomAudioUploader.reset();
        });

        ClientTickEvents.END_WORLD_TICK.register(world -> SoundStorage.getInstance(world).tick(world));

        ClientBlockEntityEvents.BLOCK_ENTITY_LOAD.register((be, world) -> {
            if (be instanceof SoundEmitter p) {
                SoundEmitterStorage.getInstance(world).addEmitter(p);
            }
            if (be instanceof RadioDevice.Receiver rec) {
                rec.setAndUpdateChannel(rec.getChannel());
            }
        });
        ClientBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register((be, world) -> {
            if (be instanceof SoundEmitter p) {
                SoundEmitterStorage.getInstance(world).removeEmitter(p);
            }
            if (be instanceof RadioDevice.Receiver rec) {
                rec.removeReceiver();
            }
        });

        //ScreenRegistry.<RadioJukeboxGuiDescription, RadioJukeboxScreen>register(Phonos.RADIO_JUKEBOX_HANDLER, (gui, inventory, title) -> new RadioJukeboxScreen(gui, inventory.player));
    }
}
