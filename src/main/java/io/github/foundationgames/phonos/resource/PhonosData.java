package io.github.foundationgames.phonos.resource;

import com.swordglowsblue.artifice.api.Artifice;
import com.swordglowsblue.artifice.api.ArtificeResourcePack;
import com.swordglowsblue.artifice.impl.ArtificeDataResourcePackProvider;
import io.github.foundationgames.phonos.Phonos;
import net.minecraft.util.Identifier;

public class PhonosData {
    public static void registerData() {
        Artifice.registerData(Phonos.id("data_pack"), pack -> {

            //-----------------------RECIPES----------------------------------

            pack.addShapedRecipe(Phonos.id("redstone_chip"), builder -> {
                builder.pattern(
                        " A ",
                        "ABA",
                        " A "
                );
                builder.ingredientItem('A', new Identifier("stick"));
                builder.ingredientItem('B', new Identifier("redstone"));
                builder.result(Phonos.id("redstone_chip"), 2);
            });
            pack.addShapedRecipe(Phonos.id("loudspeaker"), builder -> {
                builder.pattern(
                        "AAA",
                        "ABA",
                        "CDC"
                );
                builder.ingredientTag('A', new Identifier("planks"));
                builder.ingredientItem('B', Phonos.id("redstone_chip"));
                builder.ingredientTag('C', new Identifier("stone_crafting_materials"));
                builder.ingredientItem('D', new Identifier("iron_ingot"));
                builder.result(Phonos.id("loudspeaker"), 1);
            });
            pack.addShapedRecipe(Phonos.id("radio_note_block"), builder -> {
                builder.pattern(
                        "AAA",
                        "ABA",
                        "CAC"
                );
                builder.ingredientTag('A', new Identifier("planks"));
                builder.ingredientItem('B', Phonos.id("redstone_chip"));
                builder.ingredientTag('C', new Identifier("stone_crafting_materials"));
                builder.result(Phonos.id("radio_note_block"), 1);
            });
            pack.addShapedRecipe(Phonos.id("radio_jukebox"), builder -> {
                builder.pattern(
                        "ABA",
                        "ACA",
                        "DED"
                );
                builder.ingredientTag('A', new Identifier("planks"));
                builder.ingredientItem('B', new Identifier("iron_ingot"));
                builder.ingredientItem('C', Phonos.id("redstone_chip"));
                builder.ingredientTag('D', new Identifier("stone_crafting_materials"));
                builder.ingredientItem('E', new Identifier("diamond"));
                builder.result(Phonos.id("radio_jukebox"), 1);
            });
            pack.addShapedRecipe(Phonos.id("channel_tuner_0"), builder -> {
                builder.pattern(
                        "  A",
                        " B ",
                        "C  "
                );
                builder.ingredientItem('A', new Identifier("iron_ingot"));
                builder.ingredientItem('B', Phonos.id("redstone_chip"));
                builder.ingredientItem('C', new Identifier("stick"));
                builder.result(Phonos.id("channel_tuner"), 1);
            });
            pack.addShapedRecipe(Phonos.id("channel_tuner_1"), builder -> {
                builder.pattern(
                        "A  ",
                        " B ",
                        "  C"
                );
                builder.ingredientItem('A', new Identifier("iron_ingot"));
                builder.ingredientItem('B', Phonos.id("redstone_chip"));
                builder.ingredientItem('C', new Identifier("stick"));
                builder.result(Phonos.id("channel_tuner"), 1);
            });
            pack.addShapedRecipe(Phonos.id("note_block_tuner_0"), builder -> {
                builder.pattern(
                        "  A",
                        " B ",
                        "C  "
                );
                builder.ingredientItem('A', new Identifier("gold_ingot"));
                builder.ingredientItem('B', Phonos.id("redstone_chip"));
                builder.ingredientItem('C', new Identifier("stick"));
                builder.result(Phonos.id("note_block_tuner"), 1);
            });
            pack.addShapedRecipe(Phonos.id("note_block_tuner_1"), builder -> {
                builder.pattern(
                        "A  ",
                        " B ",
                        "  C"
                );
                builder.ingredientItem('A', new Identifier("gold_ingot"));
                builder.ingredientItem('B', Phonos.id("redstone_chip"));
                builder.ingredientItem('C', new Identifier("stick"));
                builder.result(Phonos.id("note_block_tuner"), 1);
            });
            pack.addShapelessRecipe(Phonos.id("gourd_speaker"), builder -> builder
                    .ingredientItem(Phonos.id("loudspeaker"))
                    .ingredientItem(new Identifier("carved_pumpkin"))
                    .result(Phonos.id("gourd_speaker"), 1)
            );
            pack.addShapelessRecipe(Phonos.id("speak_o_lantern"), builder -> builder
                    .ingredientItem(Phonos.id("loudspeaker"))
                    .ingredientItem(new Identifier("jack_o_lantern"))
                    .result(Phonos.id("speak_o_lantern"), 1)
            );
            pack.addShapelessRecipe(Phonos.id("tiny_potato_speaker"), builder -> builder
                    .ingredientItem(Phonos.id("loudspeaker"))
                    .ingredientItem(new Identifier("potato"))
                    .ingredientTag(new Identifier("flowers"))
                    .result(Phonos.id("tiny_potato_speaker"), 1)
            );
            pack.addShapedRecipe(Phonos.id("copper_speaker"), builder -> {
                builder.pattern(
                        "AAA",
                        "ABA",
                        "CDC"
                );
                builder.ingredientItem('A', new Identifier("copper_ingot"));
                builder.ingredientItem('B', Phonos.id("redstone_chip"));
                builder.ingredientTag('C', new Identifier("stone_crafting_materials"));
                builder.ingredientItem('D', new Identifier("iron_ingot"));
                builder.result(Phonos.id("copper_speaker"), 1);
            });
            pack.addShapelessRecipe(Phonos.id("waxed_copper_speaker"), builder -> builder
                    .ingredientItem(Phonos.id("copper_speaker"))
                    .ingredientItem(new Identifier("honeycomb"))
                    .result(Phonos.id("waxed_copper_speaker"), 1)
            );
            pack.addShapelessRecipe(Phonos.id("waxed_exposed_copper_speaker"), builder -> builder
                    .ingredientItem(Phonos.id("exposed_copper_speaker"))
                    .ingredientItem(new Identifier("honeycomb"))
                    .result(Phonos.id("waxed_exposed_copper_speaker"), 1)
            );
            pack.addShapelessRecipe(Phonos.id("waxed_weathered_copper_speaker"), builder -> builder
                    .ingredientItem(Phonos.id("weathered_copper_speaker"))
                    .ingredientItem(new Identifier("honeycomb"))
                    .result(Phonos.id("waxed_weathered_copper_speaker"), 1)
            );

            //-----------------------------------LOOT-TABLES---------------------------------------------

            defaultLoot(pack, Phonos.id("loudspeaker"));
            defaultLoot(pack, Phonos.id("radio_note_block"));
            defaultLoot(pack, Phonos.id("radio_jukebox"));
            defaultLoot(pack, Phonos.id("gourd_speaker"));
            defaultLoot(pack, Phonos.id("speak_o_lantern"));
            defaultLoot(pack, Phonos.id("tiny_potato_speaker"));
            defaultLoot(pack, Phonos.id("copper_speaker"));
            defaultLoot(pack, Phonos.id("exposed_copper_speaker"));
            defaultLoot(pack, Phonos.id("weathered_copper_speaker"));
            defaultLoot(pack, Phonos.id("oxidized_copper_speaker"));
            defaultLoot(pack, Phonos.id("waxed_copper_speaker"));
            defaultLoot(pack, Phonos.id("waxed_exposed_copper_speaker"));
            defaultLoot(pack, Phonos.id("waxed_weathered_copper_speaker"));
        });
    }

    private static void defaultLoot(ArtificeResourcePack.ServerResourcePackBuilder pack, Identifier id) {
        pack.addLootTable(new Identifier(id.getNamespace(), "blocks/"+id.getPath()), builder -> builder.type(new Identifier("block")).pool(pool -> pool
            .rolls(1)
            .entry(entry -> entry.type(new Identifier("item")).name(id))
            .condition(new Identifier("survives_explosion"), object -> {})
        ));
    }
}
