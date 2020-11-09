package io.github.foundationgames.phonos.item;

import io.github.foundationgames.phonos.Phonos;
import net.minecraft.item.Item;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;

public class PhonosItems {

    public static final Item CHANNEL_TUNER = register(new ChannelTunerItem(new Item.Settings().group(Phonos.PHONOS_ITEM_GROUP).maxCount(1)), "channel_tuner");
    public static final Item NOTE_BLOCK_TUNER = register(new NoteBlockTunerItem(new Item.Settings().group(Phonos.PHONOS_ITEM_GROUP).maxCount(1)), "note_block_tuner");
    public static final Item REDSTONE_CHIP = register(new Item(new Item.Settings().group(Phonos.PHONOS_ITEM_GROUP)), "redstone_chip");
    public static final Item CUSTOM_MUSIC_DISC = register(new CustomMusicDiscItem(new Item.Settings().group(Phonos.PHONOS_ITEM_GROUP).maxCount(1).rarity(Rarity.RARE)), "custom_music_disc");

    public static Item register(Item item, String name) {
        return Registry.register(Registry.ITEM, Phonos.id(name), item);
    }

    public static void init(){}
}
