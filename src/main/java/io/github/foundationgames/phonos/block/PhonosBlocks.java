package io.github.foundationgames.phonos.block;

import io.github.foundationgames.phonos.Phonos;
import io.github.foundationgames.phonos.block.entity.RadioJukeboxBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.registry.Registry;

public class PhonosBlocks {

    public static final Block LOUDSPEAKER = register(new LoudspeakerBlock(FabricBlockSettings.copy(Blocks.JUKEBOX)), "loudspeaker");
    public static final Block RADIO_JUKEBOX = register(new RadioJukeboxBlock(FabricBlockSettings.copy(Blocks.JUKEBOX)), "radio_jukebox");
    public static final Block RADIO_NOTE_BLOCK = register(new RadioNoteBlock(FabricBlockSettings.copy(Blocks.JUKEBOX)), "radio_note_block");

    public static final Block GOURD_SPEAKER = registerExtra(new LoudspeakerBlock(AbstractBlock.Settings.of(Material.WOOD, MaterialColor.ORANGE).strength(1.0F).sounds(BlockSoundGroup.WOOD)), "gourd_speaker");
    public static final Block SPEAK_O_LANTERN = registerExtra(new LoudspeakerBlock(AbstractBlock.Settings.of(Material.WOOD, MaterialColor.ORANGE).strength(1.0F).sounds(BlockSoundGroup.WOOD).luminance(state -> 15)), "speak_o_lantern");
    public static final Block TINY_POTATO_SPEAKER = registerExtra(new PotatoSpeakerBlock(AbstractBlock.Settings.of(Material.WOOD, MaterialColor.CLEAR).strength(0.3F).sounds(BlockSoundGroup.WOOD)), "tiny_potato_speaker");

    public static BlockEntityType<RadioJukeboxBlockEntity> RADIO_JUKEBOX_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, Phonos.id("radio_jukebox"), BlockEntityType.Builder.create(RadioJukeboxBlockEntity::new, RADIO_JUKEBOX).build(null));

    private static Block register(Block block, String name) {
        Registry.register(Registry.ITEM, Phonos.id(name), new BlockItem(block, new Item.Settings().group(Phonos.PHONOS_ITEM_GROUP)));
        return Registry.register(Registry.BLOCK, Phonos.id(name), block);
    }

    private static Block registerExtra(Block block, String name) {
        Registry.register(Registry.ITEM, Phonos.id(name), new BlockItem(block, new Item.Settings().group(Phonos.PHONOS_EXTRAS_GROUP)));
        return Registry.register(Registry.BLOCK, Phonos.id(name), block);
    }

    public static void init() {}
}
