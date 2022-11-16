package io.github.foundationgames.phonos.block;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import io.github.foundationgames.phonos.Phonos;
import io.github.foundationgames.phonos.block.entity.PlayerPianoBlockEntity;
import io.github.foundationgames.phonos.block.entity.RadioJukeboxBlockEntity;
import io.github.foundationgames.phonos.block.entity.RadioRecorderBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.registry.OxidizableBlocksRegistry;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.registry.Registry;

public class PhonosBlocks {
    private static final AbstractBlock.Settings BOOMBOX_SETTINGS = FabricBlockSettings.copy(Blocks.JUKEBOX).sounds(BoomboxBlock.SOUNDS).strength(0F, 5F);

    public static final Block LOUDSPEAKER = register(new LoudspeakerBlock(FabricBlockSettings.copy(Blocks.JUKEBOX)), "loudspeaker");
    public static final Block RADIO_JUKEBOX = register(new RadioJukeboxBlock(FabricBlockSettings.copy(Blocks.JUKEBOX)), "radio_jukebox");
    public static final Block RADIO_NOTE_BLOCK = register(new RadioNoteBlock(FabricBlockSettings.copy(Blocks.JUKEBOX)), "radio_note_block");
    public static final Block BOOMBOX = Registry.register(Registry.BLOCK, Phonos.id("boombox"), new BoomboxBlock(BOOMBOX_SETTINGS));
    public static final Block PIANO = Registry.register(Registry.BLOCK, Phonos.id("piano"), new PianoBlock(FabricBlockSettings.copy(Blocks.JUKEBOX).dropsNothing(), PianoBlock.Side.RIGHT, null));
    public static final Block PLAYER_PIANO = register(new PlayerPianoBlock(FabricBlockSettings.copy(Blocks.JUKEBOX), PIANO), "player_piano");
    public static final Block RADIO_PLAYER_PIANO = register(new RadioPlayerPianoBlock(FabricBlockSettings.copy(Blocks.JUKEBOX), PIANO), "radio_player_piano");
    public static final Block RADIO_RECORDER = register(new RadioRecorderBlock(FabricBlockSettings.copy(Blocks.JUKEBOX)), "radio_recorder");

    public static final Block GOURD_SPEAKER = registerExtra(new LoudspeakerBlock(AbstractBlock.Settings.of(Material.WOOD, MapColor.ORANGE).strength(1.0F).sounds(BlockSoundGroup.WOOD)), "gourd_speaker");
    public static final Block SPEAK_O_LANTERN = registerExtra(new LoudspeakerBlock(AbstractBlock.Settings.of(Material.WOOD, MapColor.ORANGE).strength(1.0F).sounds(BlockSoundGroup.WOOD).luminance(state -> 15)), "speak_o_lantern");
    public static final Block FESTIVE_BOOMBOX = Registry.register(Registry.BLOCK, Phonos.id("festive_boombox"), new BoomboxBlock(BOOMBOX_SETTINGS));
    public static final Block TINY_POTATO_SPEAKER = registerExtra(new PotatoSpeakerBlock(AbstractBlock.Settings.of(Material.WOOD, MapColor.CLEAR).strength(0.3F).sounds(BlockSoundGroup.WOOD)), "tiny_potato_speaker");
    public static final Block OXIDIZED_COPPER_SPEAKER = new OxidizableLoudspeakerBlock(Oxidizable.OxidationLevel.OXIDIZED, FabricBlockSettings.copy(Blocks.OXIDIZED_COPPER));
    public static final Block WEATHERED_COPPER_SPEAKER = new OxidizableLoudspeakerBlock(Oxidizable.OxidationLevel.WEATHERED, FabricBlockSettings.copy(Blocks.WEATHERED_COPPER));
    public static final Block EXPOSED_COPPER_SPEAKER = new OxidizableLoudspeakerBlock(Oxidizable.OxidationLevel.EXPOSED, FabricBlockSettings.copy(Blocks.EXPOSED_COPPER));
    public static final Block COPPER_SPEAKER = new OxidizableLoudspeakerBlock(Oxidizable.OxidationLevel.UNAFFECTED, FabricBlockSettings.copy(Blocks.COPPER_BLOCK));
    public static final Block WAXED_COPPER_SPEAKER = new LoudspeakerBlock(FabricBlockSettings.copy(Blocks.WAXED_COPPER_BLOCK));
    public static final Block WAXED_EXPOSED_COPPER_SPEAKER = new LoudspeakerBlock(FabricBlockSettings.copy(Blocks.WAXED_EXPOSED_COPPER));
    public static final Block WAXED_WEATHERED_COPPER_SPEAKER = new LoudspeakerBlock(FabricBlockSettings.copy(Blocks.WAXED_WEATHERED_COPPER));
    public static final Block WAXED_OXIDIZED_COPPER_SPEAKER = new LoudspeakerBlock(FabricBlockSettings.copy(Blocks.WAXED_OXIDIZED_COPPER));

    public static BlockEntityType<RadioJukeboxBlockEntity> RADIO_JUKEBOX_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, Phonos.id("radio_jukebox"), FabricBlockEntityTypeBuilder.create(RadioJukeboxBlockEntity::new, RADIO_JUKEBOX).build(null));
    public static BlockEntityType<PlayerPianoBlockEntity> PLAYER_PIANO_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, Phonos.id("player_piano"), FabricBlockEntityTypeBuilder.create(PlayerPianoBlockEntity::new, PLAYER_PIANO).build(null));
    public static BlockEntityType<PlayerPianoBlockEntity.Radio> RADIO_PLAYER_PIANO_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, Phonos.id("radio_player_piano"), FabricBlockEntityTypeBuilder.create(PlayerPianoBlockEntity.Radio::new, RADIO_PLAYER_PIANO).build(null));
    public static BlockEntityType<RadioRecorderBlockEntity> RADIO_RECORDER_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, Phonos.id("radio_recorder"), FabricBlockEntityTypeBuilder.create(RadioRecorderBlockEntity::new, RADIO_RECORDER).build(null));

    public static final BiMap<Block, Block> OXIDIZABLES = ImmutableBiMap.<Block, Block>builder().put(COPPER_SPEAKER, EXPOSED_COPPER_SPEAKER).put(EXPOSED_COPPER_SPEAKER, WEATHERED_COPPER_SPEAKER).put(WEATHERED_COPPER_SPEAKER, OXIDIZED_COPPER_SPEAKER).build();
    public static final BiMap<Block, Block> WAXABLES = ImmutableBiMap.<Block, Block>builder().put(COPPER_SPEAKER, WAXED_COPPER_SPEAKER).put(EXPOSED_COPPER_SPEAKER, WAXED_EXPOSED_COPPER_SPEAKER).put(WEATHERED_COPPER_SPEAKER, WAXED_WEATHERED_COPPER_SPEAKER).put(OXIDIZED_COPPER_SPEAKER, WAXED_OXIDIZED_COPPER_SPEAKER).build();

    private static Block register(Block block, String name) {
        Registry.register(Registry.ITEM, Phonos.id(name), new BlockItem(block, new Item.Settings().group(Phonos.PHONOS_ITEM_GROUP)));
        return Registry.register(Registry.BLOCK, Phonos.id(name), block);
    }

    private static Block registerExtra(Block block, String name) {
        Registry.register(Registry.ITEM, Phonos.id(name), new BlockItem(block, new Item.Settings().group(Phonos.PHONOS_EXTRAS_GROUP)));
        return Registry.register(Registry.BLOCK, Phonos.id(name), block);
    }

    public static void init() {
        OXIDIZABLES.forEach(OxidizableBlocksRegistry::registerOxidizableBlockPair);
        WAXABLES.forEach(OxidizableBlocksRegistry::registerWaxableBlockPair);
        
        registerExtra(COPPER_SPEAKER, "copper_speaker");
        registerExtra(EXPOSED_COPPER_SPEAKER, "exposed_copper_speaker");
        registerExtra(WEATHERED_COPPER_SPEAKER, "weathered_copper_speaker");
        registerExtra(OXIDIZED_COPPER_SPEAKER, "oxidized_copper_speaker");
        registerExtra(WAXED_COPPER_SPEAKER, "waxed_copper_speaker");
        registerExtra(WAXED_EXPOSED_COPPER_SPEAKER, "waxed_exposed_copper_speaker");
        registerExtra(WAXED_WEATHERED_COPPER_SPEAKER, "waxed_weathered_copper_speaker");
        registerExtra(WAXED_OXIDIZED_COPPER_SPEAKER, "waxed_oxidized_copper_speaker");
    }
}
