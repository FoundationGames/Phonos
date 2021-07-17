package io.github.foundationgames.phonos.resource;

import io.github.foundationgames.phonos.Phonos;
import net.devtech.arrp.api.RRPCallback;
import net.devtech.arrp.api.RuntimeResourcePack;
import net.devtech.arrp.json.loot.JCondition;
import net.devtech.arrp.json.loot.JEntry;
import net.devtech.arrp.json.loot.JLootTable;
import net.devtech.arrp.json.loot.JRoll;
import net.devtech.arrp.json.recipe.*;
import net.minecraft.util.Identifier;

public class PhonosData {
    public static final RuntimeResourcePack PHONOS_DATA = RuntimeResourcePack.create("phonos:data");

    public static void registerData() {
        RuntimeResourcePack pack = PHONOS_DATA;

        // RECIPES
        // Ingredient names may contain $ at the beginning,
        // this is my own shortcut for the "phonos" namespace
        // Ingredients with a # are counted as tags
        // See PhonosData#ingredientOf
        shaped(pack, "redstone_chip",
                new String[]{
                        " A ",
                        "ABA",
                        " A "
                },
                "redstone_chip", 2,
                "stick", "redstone"
        );
        shaped(pack, "loudspeaker",
                new String[]{
                        "AAA",
                        "ABA",
                        "CDC"
                },
                "loudspeaker", 1,
                "#planks", "$redstone_chip", "#stone_crafting_materials", "iron_ingot"
        );
        shaped(pack, "radio_note_block",
                new String[]{
                        "AAA",
                        "ABA",
                        "CAC"
                },
                "radio_note_block", 1,
                "#planks", "$redstone_chip", "#stone_crafting_materials"
        );
        shaped(pack, "radio_jukebox",
                new String[]{
                        "ABA",
                        "ACA",
                        "DED"
                },
                "radio_jukebox", 1,
                "#planks", "iron_ingot", "$redstone_chip", "#stone_crafting_materials", "diamond"
        );
        shaped(pack, "channel_tuner",
                new String[]{
                        "  A",
                        " B ",
                        "C  "
                },
                "channel_tuner", 1,
                "iron_ingot", "$redstone_chip", "stick"
        );
        shaped(pack, "note_block_tuner",
                new String[]{
                        "  A",
                        " B ",
                        "C  "
                },
                "note_block_tuner", 1,
                "gold_ingot", "$redstone_chip", "stick"
        );
        shaped(pack, "copper_speaker",
                new String[]{
                        "AAA",
                        "ABA",
                        "CDC"
                },
                "copper_speaker", 1,
                "copper_ingot", "$redstone_chip", "#stone_crafting_materials", "iron_ingot"
        );

        shapeless(pack, "gourd_speaker",
                "gourd_speaker", 1,
                "$loudspeaker", "carved_pumpkin"
        );
        shapeless(pack, "speak_o_lantern",
                "speak_o_lantern", 1,
                "$loudspeaker", "jack_o_lantern"
        );
        shapeless(pack, "tiny_potato_speaker",
                "tiny_potato_speaker", 1,
                "$loudspeaker", "potato", "#flowers"
        );
        shapeless(pack, "waxed_copper_speaker",
                "waxed_copper_speaker", 1,
                "$copper_speaker", "honeycomb"
        );
        shapeless(pack, "waxed_exposed_copper_speaker",
                "waxed_exposed_copper_speaker", 1,
                "$exposed_copper_speaker", "honeycomb"
        );
        shapeless(pack, "waxed_weathered_copper_speaker",
                "waxed_weathered_copper_speaker", 1,
                "$weathered_copper_speaker", "honeycomb"
        );
        shapeless(pack, "waxed_oxidized_copper_speaker",
                "waxed_oxidized_copper_speaker", 1,
                "$oxidized_copper_speaker", "honeycomb"
        );

        // loot tables
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

        RRPCallback.AFTER_VANILLA.register(l -> l.add(pack));
    }

    // y e s
    private static final String THE_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private static void shaped(RuntimeResourcePack pack, String id, String[] pattern, String result, int count, String ... ingredients) {
        var keys = JKeys.keys();
        for (int i = 0; i < ingredients.length; i++) {
            keys.key(String.valueOf(THE_ALPHABET.charAt(i)), ingredientOf(ingredients[i]));
        }
        pack.addRecipe(Phonos.id(id), JRecipe.shaped(JPattern.pattern(pattern), keys, JResult.stackedResult("phonos:"+result, count)));
    }

    private static void shapeless(RuntimeResourcePack pack, String id, String result, int count, String ... ingredients) {
        var ing = JIngredients.ingredients();
        for (var i : ingredients) {
            ing.add(ingredientOf(i));
        }
        pack.addRecipe(Phonos.id(id), JRecipe.shapeless(ing, JResult.stackedResult("phonos:"+result, count)));
    }

    private static JIngredient ingredientOf(String ingName) {
        if (ingName.startsWith("$")) ingName = "phonos:"+ingName.replaceFirst("[$]", "");
        else if (!ingName.contains(":")) ingName = "minecraft:"+ingName;
        var r = JIngredient.ingredient();
        if (ingName.contains("#")) r.tag(ingName.replaceFirst("[#]", ""));
        else r.item(ingName);
        return r;
    }

    private static void defaultLoot(RuntimeResourcePack pack, Identifier id) {
        pack.addLootTable(new Identifier(id.getNamespace(), "blocks/"+id.getPath()), new JLootTable("minecraft:block").pool(JLootTable.pool()
                .rolls(new JRoll(1, 1))
                .entry(new JEntry().type("minecraft:item").name(id.toString()))
                .condition(new JCondition("minecraft:survives_explosion"))
        ));
    }
}
