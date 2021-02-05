package io.github.foundationgames.phonos.resource;

import com.google.gson.JsonElement;
import com.swordglowsblue.artifice.api.Artifice;
import io.github.foundationgames.phonos.Phonos;
import net.minecraft.util.Identifier;

import java.math.BigDecimal;

public class PhonosResources {

    public static void init() {
        Artifice.registerAssets(Phonos.id("resource_pack"), pack -> {
            pack.addItemModel(Phonos.id("channel_tuner"), builder -> {
                builder.parent(new Identifier("item/handheld")).texture("layer0", Phonos.id("item/channel_tuner"));
                for (int i = 0; i < 20; i++) {
                    int ii = i;
                    builder.override(override -> override.predicate("tuned_channel", ii).model(Phonos.id("item/channel_tuner_"+ii)));
                }
            });
            pack.addItemModel(Phonos.id("redstone_chip"), builder -> {
                builder.parent(new Identifier("item/generated")).texture("layer0", Phonos.id("item/redstone_chip"));
            });
            pack.addItemModel(Phonos.id("note_block_tuner"), builder -> {
                builder
                        .parent(new Identifier("item/handheld"))
                        .texture("layer0", Phonos.id("item/note_block_tuner_inner"))
                        .texture("layer1", Phonos.id("item/note_block_tuner"));
                builder.override(override -> override.predicate("tuner_mode", 1).model(Phonos.id("item/note_block_tuner_copy")));
                builder.override(override -> override.predicate("tuner_mode", 2).model(Phonos.id("item/note_block_tuner_adjust")));
            });
            pack.addItemModel(Phonos.id("note_block_tuner_copy"), builder -> builder
                        .parent(new Identifier("item/handheld"))
                        .texture("layer0", Phonos.id("item/note_block_tuner_inner"))
                        .texture("layer1", Phonos.id("item/note_block_tuner_copy"))
            );
            pack.addItemModel(Phonos.id("note_block_tuner_adjust"), builder -> builder
                    .parent(new Identifier("item/handheld"))
                    .texture("layer0", Phonos.id("item/note_block_tuner_inner"))
                    .texture("layer1", Phonos.id("item/note_block_tuner_adjust"))
            );
            pack.addItemModel(Phonos.id("custom_music_disc"), builder -> {
                builder.parent(new Identifier("item/generated"))
                        .texture("layer0", Phonos.id("item/music_disc_inner"))
                        .texture("layer1", Phonos.id("item/music_disc_outer"));
            });
            for (int i = 0; i < 20; i++) {
                int ii = i;
                pack.addBlockModel(Phonos.id("loudspeaker_tuned_"+i), builder -> builder
                        .parent(new Identifier("block/cube"))
                        .texture("particle", Phonos.id("block/speaker_side"))
                        .texture("north", Phonos.id("block/speaker_side"))
                        .texture("south", Phonos.id("block/speaker_side"))
                        .texture("east", Phonos.id("block/speaker_side"))
                        .texture("west", Phonos.id("block/speaker_side"))
                        .texture("down", Phonos.id("block/speaker_bottom"))
                        .texture("up", Phonos.id("block/speaker_top_"+ii))
                );
                pack.addBlockModel(Phonos.id("gourd_speaker_tuned_"+i), builder -> builder
                        .parent(new Identifier("block/cube"))
                        .texture("particle", Phonos.id("block/gourd_speaker_side"))
                        .texture("north", Phonos.id("block/gourd_speaker_side"))
                        .texture("south", Phonos.id("block/gourd_speaker_side"))
                        .texture("east", Phonos.id("block/gourd_speaker_side"))
                        .texture("west", Phonos.id("block/gourd_speaker_side"))
                        .texture("down", new Identifier("block/pumpkin_top"))
                        .texture("up", Phonos.id("block/gourd_speaker_top_"+ii))
                );
                pack.addBlockModel(Phonos.id("speak_o_lantern_tuned_"+i), builder -> builder
                        .parent(new Identifier("block/cube"))
                        .texture("particle", Phonos.id("block/speak_o_lantern_side"))
                        .texture("north", Phonos.id("block/speak_o_lantern_side"))
                        .texture("south", Phonos.id("block/speak_o_lantern_side"))
                        .texture("east", Phonos.id("block/speak_o_lantern_side"))
                        .texture("west", Phonos.id("block/speak_o_lantern_side"))
                        .texture("down", new Identifier("block/pumpkin_top"))
                        .texture("up", Phonos.id("block/gourd_speaker_top_"+ii))
                );
                pack.addBlockModel(Phonos.id("radio_note_block_tuned_"+i), builder -> builder
                        .parent(Phonos.id("block/radio_note_block_base"))
                        .texture("side", Phonos.id("block/radio_note_block_side"))
                        .texture("overlay", Phonos.id("block/radio_note_block_overlay"))
                        .texture("bottom", Phonos.id("block/radio_note_block_bottom"))
                        .texture("top", Phonos.id("block/speaker_top_"+ii))
                );
                pack.addBlockModel(Phonos.id("tiny_potato_speaker_tuned_"+i), builder -> builder
                        .parent(Phonos.id("block/tiny_potato_speaker_base"))
                        .texture("potato", Phonos.id("block/tiny_potato_speaker"))
                        .texture("display", Phonos.id("block/tiny_potato_speaker_display_"+ii))
                );
                pack.addBlockModel(Phonos.id("radio_jukebox_off_tuned_"+i), builder -> builder
                        .parent(new Identifier("block/cube"))
                        .texture("particle", Phonos.id("block/radio_jukebox_side_off"))
                        .texture("north", Phonos.id("block/radio_jukebox_side_off"))
                        .texture("south", Phonos.id("block/radio_jukebox_side_off"))
                        .texture("east", Phonos.id("block/radio_jukebox_side_off"))
                        .texture("west", Phonos.id("block/radio_jukebox_side_off"))
                        .texture("down", Phonos.id("block/speaker_bottom"))
                        .texture("up", Phonos.id("block/speaker_top_"+ii))
                );
                pack.addBlockModel(Phonos.id("radio_jukebox_on_tuned_"+i), builder -> builder
                        .parent(new Identifier("block/cube"))
                        .texture("particle", Phonos.id("block/radio_jukebox_side_on"))
                        .texture("north", Phonos.id("block/radio_jukebox_side_on"))
                        .texture("south", Phonos.id("block/radio_jukebox_side_on"))
                        .texture("east", Phonos.id("block/radio_jukebox_side_on"))
                        .texture("west", Phonos.id("block/radio_jukebox_side_on"))
                        .texture("down", Phonos.id("block/speaker_bottom"))
                        .texture("up", Phonos.id("block/speaker_top_"+ii))
                );
                pack.addBlockModel(Phonos.id("copper_speaker_tuned_"+i), builder -> builder
                        .parent(new Identifier("block/cube"))
                        .texture("particle", Phonos.id("block/copper_speaker_side"))
                        .texture("north", Phonos.id("block/copper_speaker_side"))
                        .texture("south", Phonos.id("block/copper_speaker_side"))
                        .texture("east", Phonos.id("block/copper_speaker_side"))
                        .texture("west", Phonos.id("block/copper_speaker_side"))
                        .texture("down", Phonos.id("block/speaker_bottom"))
                        .texture("up", Phonos.id("block/copper_speaker_top_"+ii))
                );
                pack.addBlockModel(Phonos.id("exposed_copper_speaker_tuned_"+i), builder -> builder
                        .parent(new Identifier("block/cube"))
                        .texture("particle", Phonos.id("block/exposed_copper_speaker_side"))
                        .texture("north", Phonos.id("block/exposed_copper_speaker_side"))
                        .texture("south", Phonos.id("block/exposed_copper_speaker_side"))
                        .texture("east", Phonos.id("block/exposed_copper_speaker_side"))
                        .texture("west", Phonos.id("block/exposed_copper_speaker_side"))
                        .texture("down", Phonos.id("block/speaker_bottom"))
                        .texture("up", Phonos.id("block/copper_speaker_top_"+ii))
                );
                pack.addBlockModel(Phonos.id("weathered_copper_speaker_tuned_"+i), builder -> builder
                        .parent(new Identifier("block/cube"))
                        .texture("particle", Phonos.id("block/weathered_copper_speaker_side"))
                        .texture("north", Phonos.id("block/weathered_copper_speaker_side"))
                        .texture("south", Phonos.id("block/weathered_copper_speaker_side"))
                        .texture("east", Phonos.id("block/weathered_copper_speaker_side"))
                        .texture("west", Phonos.id("block/weathered_copper_speaker_side"))
                        .texture("down", Phonos.id("block/speaker_bottom"))
                        .texture("up", Phonos.id("block/copper_speaker_top_"+ii))
                );
                pack.addBlockModel(Phonos.id("oxidized_copper_speaker_tuned_"+i), builder -> builder
                        .parent(new Identifier("block/cube"))
                        .texture("particle", Phonos.id("block/oxidized_copper_speaker_side"))
                        .texture("north", Phonos.id("block/oxidized_copper_speaker_side"))
                        .texture("south", Phonos.id("block/oxidized_copper_speaker_side"))
                        .texture("east", Phonos.id("block/oxidized_copper_speaker_side"))
                        .texture("west", Phonos.id("block/oxidized_copper_speaker_side"))
                        .texture("down", Phonos.id("block/speaker_bottom"))
                        .texture("up", Phonos.id("block/copper_speaker_top_"+ii))
                );
                pack.addItemModel(Phonos.id("channel_tuner_"+i), builder -> builder
                        .parent(new Identifier("item/handheld"))
                        .texture("layer0", Phonos.id("item/channel_tuner_"+ii))
                );
            }
            pack.addBlockState(Phonos.id("loudspeaker"), builder -> {
                for (int i = 0; i < 20; i++) {
                    int ii = i;
                    builder.variant("channel="+i, variant -> variant.model(Phonos.id("block/loudspeaker_tuned_"+ii)));
                }
            });
            pack.addBlockState(Phonos.id("gourd_speaker"), builder -> {
                for (int i = 0; i < 20; i++) {
                    int ii = i;
                    builder.variant("channel="+i, variant -> variant.model(Phonos.id("block/gourd_speaker_tuned_"+ii)));
                }
            });
            pack.addBlockState(Phonos.id("speak_o_lantern"), builder -> {
                for (int i = 0; i < 20; i++) {
                    int ii = i;
                    builder.variant("channel="+i, variant -> variant.model(Phonos.id("block/speak_o_lantern_tuned_"+ii)));
                }
            });
            pack.addBlockState(Phonos.id("copper_speaker"), builder -> {
                for (int i = 0; i < 20; i++) {
                    int ii = i;
                    builder.variant("channel="+i, variant -> variant.model(Phonos.id("block/copper_speaker_tuned_"+ii)));
                }
            });
            pack.addBlockState(Phonos.id("exposed_copper_speaker"), builder -> {
                for (int i = 0; i < 20; i++) {
                    int ii = i;
                    builder.variant("channel="+i, variant -> variant.model(Phonos.id("block/exposed_copper_speaker_tuned_"+ii)));
                }
            });
            pack.addBlockState(Phonos.id("weathered_copper_speaker"), builder -> {
                for (int i = 0; i < 20; i++) {
                    int ii = i;
                    builder.variant("channel="+i, variant -> variant.model(Phonos.id("block/weathered_copper_speaker_tuned_"+ii)));
                }
            });
            pack.addBlockState(Phonos.id("oxidized_copper_speaker"), builder -> {
                for (int i = 0; i < 20; i++) {
                    int ii = i;
                    builder.variant("channel="+i, variant -> variant.model(Phonos.id("block/oxidized_copper_speaker_tuned_"+ii)));
                }
            });
            pack.addBlockState(Phonos.id("waxed_copper_speaker"), builder -> {
                for (int i = 0; i < 20; i++) {
                    int ii = i;
                    builder.variant("channel="+i, variant -> variant.model(Phonos.id("block/copper_speaker_tuned_"+ii)));
                }
            });
            pack.addBlockState(Phonos.id("waxed_exposed_copper_speaker"), builder -> {
                for (int i = 0; i < 20; i++) {
                    int ii = i;
                    builder.variant("channel="+i, variant -> variant.model(Phonos.id("block/exposed_copper_speaker_tuned_"+ii)));
                }
            });
            pack.addBlockState(Phonos.id("waxed_weathered_copper_speaker"), builder -> {
                for (int i = 0; i < 20; i++) {
                    int ii = i;
                    builder.variant("channel="+i, variant -> variant.model(Phonos.id("block/weathered_copper_speaker_tuned_"+ii)));
                }
            });
            pack.addBlockState(Phonos.id("tiny_potato_speaker"), builder -> {
                for (int i = 0; i < 20; i++) {
                    int ii = i;
                    builder.variant("facing=north,channel="+i, variant -> variant.model(Phonos.id("block/tiny_potato_speaker_tuned_"+ii)).rotationY(0));
                    builder.variant("facing=south,channel="+i, variant -> variant.model(Phonos.id("block/tiny_potato_speaker_tuned_"+ii)).rotationY(180));
                    builder.variant("facing=east,channel="+i, variant -> variant.model(Phonos.id("block/tiny_potato_speaker_tuned_"+ii)).rotationY(90));
                    builder.variant("facing=west,channel="+i, variant -> variant.model(Phonos.id("block/tiny_potato_speaker_tuned_"+ii)).rotationY(270));
                }
            });
            pack.addBlockState(Phonos.id("radio_note_block"), builder -> {
                for (int i = 0; i < 20; i++) {
                    int ii = i;
                    builder.variant("channel="+i, variant -> variant.model(Phonos.id("block/radio_note_block_tuned_"+ii)));
                }
            });
            pack.addBlockState(Phonos.id("radio_jukebox"), builder -> {
                for (int i = 0; i < 20; i++) {
                    int ii = i;
                    builder.variant("playing=false,channel="+i, variant -> variant.model(Phonos.id("block/radio_jukebox_off_tuned_"+ii)));
                    builder.variant("playing=true,channel="+i, variant -> variant.model(Phonos.id("block/radio_jukebox_on_tuned_"+ii)));
                }
            });
            pack.addItemModel(Phonos.id("loudspeaker"), builder -> builder
                .parent(new Identifier("block/cube"))
                        .texture("particle", Phonos.id("block/speaker_side"))
                        .texture("north", Phonos.id("block/speaker_side"))
                        .texture("south", Phonos.id("block/speaker_side"))
                        .texture("east", Phonos.id("block/speaker_side"))
                        .texture("west", Phonos.id("block/speaker_side"))
                        .texture("down", Phonos.id("block/speaker_bottom"))
                        .texture("up", Phonos.id("block/speaker_top"))
            );
            pack.addItemModel(Phonos.id("radio_note_block"), builder -> builder
                    .parent(new Identifier("block/cube"))
                    .texture("particle", Phonos.id("block/radio_note_block_side"))
                    .texture("north", Phonos.id("block/radio_note_block_side"))
                    .texture("south", Phonos.id("block/radio_note_block_side"))
                    .texture("east", Phonos.id("block/radio_note_block_side"))
                    .texture("west", Phonos.id("block/radio_note_block_side"))
                    .texture("down", Phonos.id("block/radio_note_block_bottom"))
                    .texture("up", Phonos.id("block/speaker_top"))
            );
            pack.addItemModel(Phonos.id("radio_jukebox"), builder -> builder
                    .parent(new Identifier("block/cube"))
                    .texture("particle", Phonos.id("block/radio_jukebox_side_off"))
                    .texture("north", Phonos.id("block/radio_jukebox_side_off"))
                    .texture("south", Phonos.id("block/radio_jukebox_side_off"))
                    .texture("east", Phonos.id("block/radio_jukebox_side_off"))
                    .texture("west", Phonos.id("block/radio_jukebox_side_off"))
                    .texture("down", Phonos.id("block/speaker_bottom"))
                    .texture("up", Phonos.id("block/speaker_top"))
            );
            pack.addItemModel(Phonos.id("gourd_speaker"), builder -> builder
                    .parent(new Identifier("block/cube"))
                    .texture("particle", Phonos.id("block/gourd_speaker_side"))
                    .texture("north", Phonos.id("block/gourd_speaker_side"))
                    .texture("south", Phonos.id("block/gourd_speaker_side"))
                    .texture("east", Phonos.id("block/gourd_speaker_side"))
                    .texture("west", Phonos.id("block/gourd_speaker_side"))
                    .texture("down", new Identifier("block/pumpkin_top"))
                    .texture("up", Phonos.id("block/gourd_speaker_top"))
            );
            pack.addItemModel(Phonos.id("speak_o_lantern"), builder -> builder
                    .parent(new Identifier("block/cube"))
                    .texture("particle", Phonos.id("block/speak_o_lantern_side"))
                    .texture("north", Phonos.id("block/speak_o_lantern_side"))
                    .texture("south", Phonos.id("block/speak_o_lantern_side"))
                    .texture("east", Phonos.id("block/speak_o_lantern_side"))
                    .texture("west", Phonos.id("block/speak_o_lantern_side"))
                    .texture("down", new Identifier("block/pumpkin_top"))
                    .texture("up", Phonos.id("block/gourd_speaker_top"))
            );
            pack.addItemModel(Phonos.id("copper_speaker"), builder -> builder
                    .parent(new Identifier("block/cube"))
                    .texture("particle", Phonos.id("block/copper_speaker_side"))
                    .texture("north", Phonos.id("block/copper_speaker_side"))
                    .texture("south", Phonos.id("block/copper_speaker_side"))
                    .texture("east", Phonos.id("block/copper_speaker_side"))
                    .texture("west", Phonos.id("block/copper_speaker_side"))
                    .texture("down", Phonos.id("block/speaker_bottom"))
                    .texture("up", Phonos.id("block/copper_speaker_top"))
            );
            pack.addItemModel(Phonos.id("exposed_copper_speaker"), builder -> builder
                    .parent(new Identifier("block/cube"))
                    .texture("particle", Phonos.id("block/exposed_copper_speaker_side"))
                    .texture("north", Phonos.id("block/exposed_copper_speaker_side"))
                    .texture("south", Phonos.id("block/exposed_copper_speaker_side"))
                    .texture("east", Phonos.id("block/exposed_copper_speaker_side"))
                    .texture("west", Phonos.id("block/exposed_copper_speaker_side"))
                    .texture("down", Phonos.id("block/speaker_bottom"))
                    .texture("up", Phonos.id("block/copper_speaker_top"))
            );
            pack.addItemModel(Phonos.id("weathered_copper_speaker"), builder -> builder
                    .parent(new Identifier("block/cube"))
                    .texture("particle", Phonos.id("block/weathered_copper_speaker_side"))
                    .texture("north", Phonos.id("block/weathered_copper_speaker_side"))
                    .texture("south", Phonos.id("block/weathered_copper_speaker_side"))
                    .texture("east", Phonos.id("block/weathered_copper_speaker_side"))
                    .texture("west", Phonos.id("block/weathered_copper_speaker_side"))
                    .texture("down", Phonos.id("block/speaker_bottom"))
                    .texture("up", Phonos.id("block/copper_speaker_top"))
            );
            pack.addItemModel(Phonos.id("oxidized_copper_speaker"), builder -> builder
                    .parent(new Identifier("block/cube"))
                    .texture("particle", Phonos.id("block/oxidized_copper_speaker_side"))
                    .texture("north", Phonos.id("block/oxidized_copper_speaker_side"))
                    .texture("south", Phonos.id("block/oxidized_copper_speaker_side"))
                    .texture("east", Phonos.id("block/oxidized_copper_speaker_side"))
                    .texture("west", Phonos.id("block/oxidized_copper_speaker_side"))
                    .texture("down", Phonos.id("block/speaker_bottom"))
                    .texture("up", Phonos.id("block/copper_speaker_top"))
            );
            pack.addItemModel(Phonos.id("waxed_copper_speaker"), builder -> builder
                    .parent(new Identifier("block/cube"))
                    .texture("particle", Phonos.id("block/copper_speaker_side"))
                    .texture("north", Phonos.id("block/copper_speaker_side"))
                    .texture("south", Phonos.id("block/copper_speaker_side"))
                    .texture("east", Phonos.id("block/copper_speaker_side"))
                    .texture("west", Phonos.id("block/copper_speaker_side"))
                    .texture("down", Phonos.id("block/speaker_bottom"))
                    .texture("up", Phonos.id("block/copper_speaker_top"))
            );
            pack.addItemModel(Phonos.id("waxed_exposed_copper_speaker"), builder -> builder
                    .parent(new Identifier("block/cube"))
                    .texture("particle", Phonos.id("block/exposed_copper_speaker_side"))
                    .texture("north", Phonos.id("block/exposed_copper_speaker_side"))
                    .texture("south", Phonos.id("block/exposed_copper_speaker_side"))
                    .texture("east", Phonos.id("block/exposed_copper_speaker_side"))
                    .texture("west", Phonos.id("block/exposed_copper_speaker_side"))
                    .texture("down", Phonos.id("block/speaker_bottom"))
                    .texture("up", Phonos.id("block/copper_speaker_top"))
            );
            pack.addItemModel(Phonos.id("waxed_weathered_copper_speaker"), builder -> builder
                    .parent(new Identifier("block/cube"))
                    .texture("particle", Phonos.id("block/weathered_copper_speaker_side"))
                    .texture("north", Phonos.id("block/weathered_copper_speaker_side"))
                    .texture("south", Phonos.id("block/weathered_copper_speaker_side"))
                    .texture("east", Phonos.id("block/weathered_copper_speaker_side"))
                    .texture("west", Phonos.id("block/weathered_copper_speaker_side"))
                    .texture("down", Phonos.id("block/speaker_bottom"))
                    .texture("up", Phonos.id("block/copper_speaker_top"))
            );
            pack.addItemModel(Phonos.id("tiny_potato_speaker"), builder -> builder
                    .parent(Phonos.id("block/tiny_potato_speaker_base"))
                    .texture("potato", Phonos.id("block/tiny_potato_speaker"))
                    .texture("display", Phonos.id("block/tiny_potato_speaker"))
            );
        });
    }
}
