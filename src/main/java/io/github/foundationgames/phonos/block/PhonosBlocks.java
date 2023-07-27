package io.github.foundationgames.phonos.block;

import io.github.foundationgames.phonos.Phonos;
import io.github.foundationgames.phonos.block.entity.*;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class PhonosBlocks {
    public static final Block LOUDSPEAKER = register(new LoudspeakerBlock(FabricBlockSettings.copy(Blocks.NOTE_BLOCK)), "loudspeaker");
    public static final Block ELECTRONIC_NOTE_BLOCK = register(new ElectronicNoteBlock(FabricBlockSettings.copy(Blocks.NOTE_BLOCK)), "electronic_note_block");
    public static final Block ELECTRONIC_JUKEBOX = register(new ElectronicJukeboxBlock(FabricBlockSettings.copy(Blocks.JUKEBOX)), "electronic_jukebox");
    public static final Block CONNECTION_HUB = register(new ConnectionHubBlock(FabricBlockSettings.copy(Blocks.OAK_PLANKS)), "connection_hub");
    public static final Block RADIO_TRANSCEIVER = register(new RadioTransceiverBlock(FabricBlockSettings.copy(Blocks.OAK_SLAB)), "radio_transceiver");
    public static final Block RADIO_LOUDSPEAKER = register(new RadioLoudspeakerBlock(FabricBlockSettings.copy(Blocks.NOTE_BLOCK)), "radio_loudspeaker");
    public static final Block SATELLITE_STATION = register(new SatelliteStationBlock(FabricBlockSettings.copy(Blocks.OAK_SLAB)), "satellite_station");

    public static BlockEntityType<ElectronicNoteBlockEntity> ELECTRONIC_NOTE_BLOCK_ENTITY = Registry.register(
            Registries.BLOCK_ENTITY_TYPE, Phonos.id("electronic_note_block"),
            BlockEntityType.Builder.create(ElectronicNoteBlockEntity::new, ELECTRONIC_NOTE_BLOCK).build(null));
    public static BlockEntityType<ElectronicJukeboxBlockEntity> ELECTRONIC_JUKEBOX_ENTITY = Registry.register(
            Registries.BLOCK_ENTITY_TYPE, Phonos.id("electronic_jukebox"),
            BlockEntityType.Builder.create(ElectronicJukeboxBlockEntity::new, ELECTRONIC_JUKEBOX).build(null));
    public static BlockEntityType<ConnectionHubBlockEntity> CONNECTION_HUB_ENTITY = Registry.register(
            Registries.BLOCK_ENTITY_TYPE, Phonos.id("connection_hub"),
            BlockEntityType.Builder.create(ConnectionHubBlockEntity::new, CONNECTION_HUB).build(null));
    public static BlockEntityType<RadioTransceiverBlockEntity> RADIO_TRANSCEIVER_ENTITY = Registry.register(
            Registries.BLOCK_ENTITY_TYPE, Phonos.id("radio_transceiver"),
            BlockEntityType.Builder.create(RadioTransceiverBlockEntity::new, RADIO_TRANSCEIVER).build(null));
    public static BlockEntityType<RadioLoudspeakerBlockEntity> RADIO_LOUDSPEAKER_ENTITY = Registry.register(
            Registries.BLOCK_ENTITY_TYPE, Phonos.id("radio_loudspeaker"),
            BlockEntityType.Builder.create(RadioLoudspeakerBlockEntity::new, RADIO_LOUDSPEAKER).build(null));
    public static BlockEntityType<SatelliteStationBlockEntity> SATELLITE_STATION_ENTITY = Registry.register(
            Registries.BLOCK_ENTITY_TYPE, Phonos.id("satellite_station"),
            BlockEntityType.Builder.create(SatelliteStationBlockEntity::new, SATELLITE_STATION).build(null));

    private static Block register(Block block, String name) {
        var item = Registry.register(Registries.ITEM, Phonos.id(name), new BlockItem(block, new Item.Settings()));
        Phonos.PHONOS_ITEMS.queue(item);
        return Registry.register(Registries.BLOCK, Phonos.id(name), block);
    }

    public static void init() {
    }
}
