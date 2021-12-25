package io.github.foundationgames.phonos.resource;

import io.github.foundationgames.phonos.Phonos;
import net.devtech.arrp.api.RRPCallback;
import net.devtech.arrp.api.RuntimeResourcePack;
import net.devtech.arrp.json.blockstate.JBlockModel;
import net.devtech.arrp.json.blockstate.JState;
import net.devtech.arrp.json.blockstate.JVariant;
import net.devtech.arrp.json.models.JModel;
import net.devtech.arrp.json.models.JTextures;
import net.minecraft.util.Identifier;

public class PhonosAssets {
    public static final RuntimeResourcePack PHONOS_ASSETS = RuntimeResourcePack.create("phonos:assets");

    public static void init() {
        RuntimeResourcePack pack = PHONOS_ASSETS;

        // CHANNEL TUNER
        JModel ctModel = new JModel().parent("item/handheld").textures(JModel.textures().var("layer0", "phonos:item/channel_tuner"));
        for (int i = 0; i < 20; i++) {
            ctModel.addOverride(JModel.override(JModel.condition().parameter("tuned_channel", (float)i / 19), Phonos.id("item/channel_tuner_"+i)));
            pack.addModel(new JModel().parent("item/handheld").textures(JModel.textures().var("layer0", "phonos:item/channel_tuner_"+i)), Phonos.id("item/channel_tuner_"+i));
        }
        pack.addModel(
                ctModel,
                Phonos.id("item/channel_tuner")
        );
        
        // BOOMBOX ITEMS
        addBoomboxItem(pack, "boombox");
        addBoomboxItem(pack, "festive_boombox");

        // REDSTONE CHIP
        addGeneratedItem(pack, "redstone_chip");

        // NOTE BLOCK TUNER
        pack.addModel(
                new JModel()
                    .parent("item/handheld")
                    .textures(
                            JModel.textures()
                                    .var("layer0", "phonos:item/note_block_tuner_inner")
                                    .var("layer1", "phonos:item/note_block_tuner")
                    )
                    .addOverride(JModel.override(JModel.condition().parameter("tuner_mode", 0.5), Phonos.id("item/note_block_tuner_copy")))
                    .addOverride(JModel.override(JModel.condition().parameter("tuner_mode", 1), Phonos.id("item/note_block_tuner_adjust"))),
                Phonos.id("item/note_block_tuner")
        );
        pack.addModel(
                new JModel()
                        .parent("item/generated")
                        .textures(
                                JModel.textures()
                                        .var("layer0", "phonos:item/note_block_tuner_inner")
                                        .var("layer1", "phonos:item/note_block_tuner_copy")
                        ),
                Phonos.id("item/note_block_tuner_copy")
        );
        pack.addModel(
                new JModel()
                        .parent("item/generated")
                        .textures(
                                JModel.textures()
                                        .var("layer0", "phonos:item/note_block_tuner_inner")
                                        .var("layer1", "phonos:item/note_block_tuner_adjust")
                        ),
                Phonos.id("item/note_block_tuner_adjust")
        );

        // CUSTOM MUSIC DISC
        pack.addModel(
                new JModel()
                        .parent("item/generated")
                        .textures(
                                JModel.textures()
                                        .var("layer0", "phonos:item/music_disc_inner")
                                        .var("layer1", "phonos:item/music_disc_outer")
                        ),
                Phonos.id("item/custom_music_disc")
        );

        // TUNABLE BLOCK MODELS
        for (int i = 0; i < 20; i++) {
            addSidedBlockModel(
                    pack,
                    "block/loudspeaker_tuned_"+i,
                    Phonos.id("block/speaker_bottom"),
                    Phonos.id("block/speaker_side"),
                    Phonos.id("block/speaker_top_"+i)
            );
            addSidedBlockModel(
                    pack,
                    "block/gourd_speaker_tuned_"+i,
                    new Identifier("block/pumpkin_top"),
                    Phonos.id("block/gourd_speaker_side"),
                    Phonos.id("block/gourd_speaker_top_"+i)
            );
            addSidedBlockModel(
                    pack,
                    "block/speak_o_lantern_tuned_"+i,
                    new Identifier("block/pumpkin_top"),
                    Phonos.id("block/speak_o_lantern_side"),
                    Phonos.id("block/gourd_speaker_top_"+i)
            );
            addSidedBlockModel(
                    pack,
                    "block/radio_jukebox_off_tuned_"+i,
                    Phonos.id("block/speaker_bottom"),
                    Phonos.id("block/radio_jukebox_side_off"),
                    Phonos.id("block/speaker_top_"+i)
            );
            addSidedBlockModel(
                    pack,
                    "block/radio_jukebox_on_tuned_"+i,
                    Phonos.id("block/speaker_bottom"),
                    Phonos.id("block/radio_jukebox_side_on"),
                    Phonos.id("block/speaker_top_"+i)
            );
            addCopperSpeakerModels(pack, "", i);
            addCopperSpeakerModels(pack, "exposed_", i);
            addCopperSpeakerModels(pack, "weathered_", i);
            addCopperSpeakerModels(pack, "oxidized_", i);
            addTinyPotatoSpeakerModel(pack, i);
            addBoomboxModels(pack, "boombox", i);
            addBoomboxModels(pack, "festive_boombox", i);
            pack.addModel(new JModel().parent("phonos:block/radio_note_block_base").textures(new JTextures()
                    .var("side", "phonos:block/radio_note_block_side")
                    .var("overlay", "phonos:block/radio_note_block_overlay")
                    .var("bottom", "phonos:block/radio_note_block_bottom")
                    .var("top", "phonos:block/speaker_top_"+i)
            ), Phonos.id("block/radio_note_block_tuned_"+i));
        }

        // TUNABLE BLOCKSTATES
        addTunableBlockState(pack, "loudspeaker", "block/loudspeaker_tuned_");
        addTunableBlockState(pack, "radio_note_block", "block/radio_note_block_tuned_");

        addTunableBlockState(pack, "gourd_speaker", "block/gourd_speaker_tuned_");
        addTunableBlockState(pack, "speak_o_lantern", "block/speak_o_lantern_tuned_");

        addTunableBlockState(pack, "copper_speaker", "block/copper_speaker_tuned_");
        addTunableBlockState(pack, "exposed_copper_speaker", "block/exposed_copper_speaker_tuned_");
        addTunableBlockState(pack, "weathered_copper_speaker", "block/weathered_copper_speaker_tuned_");
        addTunableBlockState(pack, "oxidized_copper_speaker", "block/oxidized_copper_speaker_tuned_");
        addTunableBlockState(pack, "waxed_copper_speaker", "block/copper_speaker_tuned_");
        addTunableBlockState(pack, "waxed_exposed_copper_speaker", "block/exposed_copper_speaker_tuned_");
        addTunableBlockState(pack, "waxed_weathered_copper_speaker", "block/weathered_copper_speaker_tuned_");
        addTunableBlockState(pack, "waxed_oxidized_copper_speaker", "block/oxidized_copper_speaker_tuned_");

        var tinyPotatoVar = JState.variant();
        for (int i = 0; i < 20; i++) {
            tinyPotatoVar.put("facing=north,channel="+i, JState.model(Phonos.id("block/tiny_potato_speaker_tuned_"+i)).y(0));
            tinyPotatoVar.put("facing=south,channel="+i, JState.model(Phonos.id("block/tiny_potato_speaker_tuned_"+i)).y(180));
            tinyPotatoVar.put("facing=east,channel="+i, JState.model(Phonos.id("block/tiny_potato_speaker_tuned_"+i)).y(90));
            tinyPotatoVar.put("facing=west,channel="+i, JState.model(Phonos.id("block/tiny_potato_speaker_tuned_"+i)).y(270));
        }
        pack.addBlockState(new JState().add(tinyPotatoVar), Phonos.id("tiny_potato_speaker"));

        var jukeboxVar = JState.variant();
        for (int i = 0; i < 20; i++) {
            jukeboxVar.put("playing=true,channel="+i, JState.model(Phonos.id("block/radio_jukebox_on_tuned_"+i)));
            jukeboxVar.put("playing=false,channel="+i, JState.model(Phonos.id("block/radio_jukebox_off_tuned_"+i)));
        }
        pack.addBlockState(new JState().add(jukeboxVar), Phonos.id("radio_jukebox"));

        addBoomboxBlockState(pack, "boombox");
        addBoomboxBlockState(pack, "festive_boombox");

        // ITEM MODELS FOR SPEAKERS
        addSidedBlockModel(
                pack,
                "item/loudspeaker",
                Phonos.id("block/speaker_bottom"),
                Phonos.id("block/speaker_side"),
                Phonos.id("block/speaker_top")
        );
        addSidedBlockModel(
                pack,
                "item/gourd_speaker",
                new Identifier("block/pumpkin_top"),
                Phonos.id("block/gourd_speaker_side"),
                Phonos.id("block/gourd_speaker_top")
        );
        addSidedBlockModel(
                pack,
                "item/speak_o_lantern",
                new Identifier("block/pumpkin_top"),
                Phonos.id("block/speak_o_lantern_side"),
                Phonos.id("block/gourd_speaker_top")
        );
        addSidedBlockModel(
                pack,
                "item/radio_jukebox",
                Phonos.id("block/speaker_bottom"),
                Phonos.id("block/radio_jukebox_side_off"),
                Phonos.id("block/speaker_top")
        );
        pack.addModel(new JModel().parent("phonos:block/radio_note_block_base").textures(new JTextures()
                .var("side", "phonos:block/radio_note_block_side")
                .var("overlay", "phonos:block/radio_note_block_overlay")
                .var("bottom", "phonos:block/radio_note_block_bottom")
                .var("top", "phonos:block/speaker_top")
        ), Phonos.id("item/radio_note_block"));

        RRPCallback.AFTER_VANILLA.register(l -> l.add(pack));
    }

    public static void addGeneratedItem(RuntimeResourcePack pack, String item) {
        pack.addModel(new JModel().parent("item/generated").textures(JModel.textures().var("layer0", "phonos:item/"+item)), Phonos.id("item/"+item));
    }

    public static void addBoomboxItem(RuntimeResourcePack pack, String boomboxName) {
        JModel bbModel = new JModel().parent("phonos:block/"+boomboxName+"_base").textures(JModel.textures().var("display", "phonos:block/"+boomboxName+"_display"));
        for (int i = 0; i < 20; i++) {
            bbModel.addOverride(JModel.override(JModel.condition().parameter("radio_channel", (float)i / 19), Phonos.id("block/"+boomboxName+"_tuned_"+i)));
        }
        pack.addModel(
                bbModel,
                Phonos.id("item/"+boomboxName)
        );
    }

    public static void addSidedBlockModel(RuntimeResourcePack pack, String path, Identifier bottom, Identifier side, Identifier top) {
        pack.addModel(
                new JModel()
                    .parent("minecraft:block/cube")
                    .textures(
                            new JTextures()
                                    .var("particle", side.toString())
                                    .var("north", side.toString())
                                    .var("south", side.toString())
                                    .var("east", side.toString())
                                    .var("west", side.toString())
                                    .var("up", top.toString())
                                    .var("down", bottom.toString())
                    ),
                Phonos.id(path)
        );
    }

    public static void addCopperSpeakerModels(RuntimeResourcePack pack, String prefix, int iter) {
        addSidedBlockModel(
                pack,
                "block/"+prefix+"copper_speaker_tuned_"+iter,
                Phonos.id("block/speaker_bottom"),
                Phonos.id("block/"+prefix+"copper_speaker_side"),
                Phonos.id("block/copper_speaker_top_"+iter)
        );
        if (iter == 0) {
            var itemModelPath = "item/"+prefix+"copper_speaker";
            addSidedBlockModel(
                    pack,
                    itemModelPath,
                    Phonos.id("block/speaker_bottom"),
                    Phonos.id("block/"+prefix+"copper_speaker_side"),
                    Phonos.id("block/copper_speaker_top")
            );
            pack.addModel(new JModel().parent("phonos:"+itemModelPath), Phonos.id("item/waxed_"+prefix+"copper_speaker"));
        }
    }

    public static void addBoomboxModels(RuntimeResourcePack pack, String boomboxName, int iter) {
        pack.addModel(new JModel().parent("phonos:block/"+boomboxName+"_base").textures(new JTextures()
                .var("display", "phonos:block/"+boomboxName+"_display_"+iter)
        ), Phonos.id("block/"+boomboxName+"_tuned_"+iter));
    }

    public static void addTinyPotatoSpeakerModel(RuntimeResourcePack pack, int iter) {
        if (iter == 0) {
            pack.addModel(new JModel().parent("phonos:block/tiny_potato_speaker_base").textures(JModel.textures()
                    .var("potato", "phonos:block/tiny_potato_speaker")
                    .var("display", "phonos:block/tiny_potato_speaker")
            ), Phonos.id("item/tiny_potato_speaker"));
        }
        pack.addModel(new JModel().parent("phonos:block/tiny_potato_speaker_base").textures(JModel.textures()
                .var("potato", "phonos:block/tiny_potato_speaker")
                .var("display", "phonos:block/tiny_potato_speaker_display_"+iter)
        ), Phonos.id("block/tiny_potato_speaker_tuned_"+iter));
    }

    public static void addTunableBlockState(RuntimeResourcePack pack, String path, String prefix) {
        var variants = new JVariant();
        for (int i = 0; i < 20; i++) {
            variants.put("channel="+i, new JBlockModel(Phonos.id(prefix+i)));
        }
        pack.addBlockState(new JState().add(variants), Phonos.id(path));
    }

    public static void addBoomboxBlockState(RuntimeResourcePack pack, String boomboxName) {
        var variant = JState.variant();
        for (int i = 0; i < 20; i++) {
            variant.put("facing=north,channel="+i, JState.model(Phonos.id("block/"+boomboxName+"_tuned_"+i)).y(0));
            variant.put("facing=south,channel="+i, JState.model(Phonos.id("block/"+boomboxName+"_tuned_"+i)).y(180));
            variant.put("facing=east,channel="+i, JState.model(Phonos.id("block/"+boomboxName+"_tuned_"+i)).y(90));
            variant.put("facing=west,channel="+i, JState.model(Phonos.id("block/"+boomboxName+"_tuned_"+i)).y(270));
        }
        pack.addBlockState(new JState().add(variant), Phonos.id(boomboxName));
    }

    public static void addBlockItem(RuntimeResourcePack pack, String path, String parent) {
        pack.addModel(new JModel().parent(parent), Phonos.id(path));
    }
}
