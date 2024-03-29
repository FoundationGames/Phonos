package io.github.foundationgames.phonos;

import io.github.foundationgames.jsonem.JsonEM;
import io.github.foundationgames.phonos.block.PhonosBlocks;
import io.github.foundationgames.phonos.block.RadioNoteBlock;
import io.github.foundationgames.phonos.block.SoundPlayReceivable;
import io.github.foundationgames.phonos.client.ClientReceiverStorage;
import io.github.foundationgames.phonos.client.render.block.PlayerPianoBlockEntityRenderer;
import io.github.foundationgames.phonos.client.render.block.RadioRecorderBlockEntityRenderer;
import io.github.foundationgames.phonos.entity.SoundPlayEntityReceivable;
import io.github.foundationgames.phonos.item.PhonosItems;
import io.github.foundationgames.phonos.network.ClientPayloadPackets;
import io.github.foundationgames.phonos.resource.PhonosAssets;
import io.github.foundationgames.phonos.screen.CustomMusicDiscGuiDescription;
import io.github.foundationgames.phonos.screen.CustomMusicDiscScreen;
import io.github.foundationgames.phonos.screen.RadioJukeboxGuiDescription;
import io.github.foundationgames.phonos.screen.RadioJukeboxScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.object.builder.v1.client.model.FabricModelPredicateProviderRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.Random;

public class PhonosClient implements ClientModInitializer {
    public static final EntityModelLayer KEYBOARD_MODEL_LAYER = new EntityModelLayer(Phonos.id("keyboard"), "main");
    public static final EntityModelLayer PIANO_ROLL_MODEL_LAYER = new EntityModelLayer(Phonos.id("piano_roll"), "main");

    @Override
    public void onInitializeClient() {
        ClientPayloadPackets.initClient();

        PhonosAssets.init();

        ClientReceiverStorage.init();
        ClientReceiverStorage.registerPlaySoundCallback(((sound, blocks, entities, channel, volume, pitch, stoppable) -> {
            if(!stoppable) {
                PlayerEntity player = MinecraftClient.getInstance().player;
                ClientWorld world = MinecraftClient.getInstance().world;
                if(player != null && world != null) {
                    BlockPos pos = player.getBlockPos();
                    if(blocks != null) {
                        for(BlockPos receiver : blocks) {
                            if(pos.isWithinDistance(receiver, 30)) {
                                if(world.getBlockState(receiver).getBlock() instanceof SoundPlayReceivable) {
                                    ((SoundPlayReceivable)world.getBlockState(receiver).getBlock()).onReceivedSoundClient(world, world.getBlockState(receiver), receiver, channel, volume, pitch);
                                }
                            }
                        }
                    }
                    BlockPos receiver;
                    if(entities != null) {
                        for(Entity e : entities) {
                            receiver = e.getBlockPos();
                            if(pos.isWithinDistance(receiver, 30)) {
                                if(e instanceof SoundPlayEntityReceivable) {
                                    ((SoundPlayEntityReceivable)e).onRecievedSoundClient(world, e, channel, volume, pitch);
                                }
                            }
                        }
                    }
                }
            }
        }));

        FabricModelPredicateProviderRegistry.register(PhonosItems.CHANNEL_TUNER, new Identifier("tuned_channel"), (stack, world, entity, seed) -> (float)stack.getOrCreateSubNbt("TunerData").getInt("Channel") / 19);
        FabricModelPredicateProviderRegistry.register(PhonosItems.NOTE_BLOCK_TUNER, new Identifier("tuner_mode"), (stack, world, entity, seed) -> (float)stack.getOrCreateSubNbt("TunerData").getInt("Mode") / 2);
        FabricModelPredicateProviderRegistry.register(PhonosItems.BOOMBOX, new Identifier("radio_channel"), (stack, world, entity, seed) -> (float)stack.getOrCreateSubNbt("RadioData").getInt("Channel") / 19);
        FabricModelPredicateProviderRegistry.register(PhonosItems.FESTIVE_BOOMBOX, new Identifier("radio_channel"), (stack, world, entity, seed) -> (float)stack.getOrCreateSubNbt("RadioData").getInt("Channel") / 19);

        JsonEM.registerModelLayer(KEYBOARD_MODEL_LAYER);
        JsonEM.registerModelLayer(PIANO_ROLL_MODEL_LAYER);

        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> world != null && pos != null && state != null ? RadioNoteBlock.getColorFromNote(state.get(RadioNoteBlock.NOTE)) : 0xFFFFFF, PhonosBlocks.RADIO_NOTE_BLOCK);
        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> {
            int note = stack.getOrCreateSubNbt("TunerData").getInt("Note");
            return tintIndex > 0 ? -1 : RadioNoteBlock.getColorFromNote(note);
        }, PhonosItems.NOTE_BLOCK_TUNER);
        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> {
            int color = 0;
            String seed = stack.getOrCreateSubNbt("MusicData").getString("SoundId");
            if(seed != null) color = new Random(seed(seed)).nextInt(0xFFFFFF);
            return tintIndex > 0 ? -1 : color;
        }, PhonosItems.CUSTOM_MUSIC_DISC);

        BlockRenderLayerMap.INSTANCE.putBlock(PhonosBlocks.RADIO_NOTE_BLOCK, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(PhonosBlocks.BOOMBOX, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(PhonosBlocks.FESTIVE_BOOMBOX, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(PhonosBlocks.RADIO_RECORDER, RenderLayer.getTranslucent());

        BlockEntityRendererRegistry.register(PhonosBlocks.PLAYER_PIANO_ENTITY, PlayerPianoBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(PhonosBlocks.RADIO_PLAYER_PIANO_ENTITY, PlayerPianoBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(PhonosBlocks.RADIO_RECORDER_ENTITY, RadioRecorderBlockEntityRenderer::new);

        ScreenRegistry.<RadioJukeboxGuiDescription, RadioJukeboxScreen>register(Phonos.RADIO_JUKEBOX_HANDLER, (gui, inventory, title) -> new RadioJukeboxScreen(gui, inventory.player));
        ScreenRegistry.<CustomMusicDiscGuiDescription, CustomMusicDiscScreen>register(Phonos.CUSTOM_DISC_HANDLER, (gui, inventory, title) -> new CustomMusicDiscScreen(gui, inventory.player));
    }

    private static long seed(String s) {
        if(s == null) return 0;
        long l = 0;
        for(char c : s.toCharArray())
            l = 31L * l + c;
        return l;
    }
}
