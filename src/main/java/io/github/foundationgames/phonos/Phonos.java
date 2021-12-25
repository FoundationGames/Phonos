package io.github.foundationgames.phonos;

import io.github.foundationgames.phonos.block.PhonosBlocks;
import io.github.foundationgames.phonos.block.entity.RadioJukeboxBlockEntity;
import io.github.foundationgames.phonos.item.PhonosItems;
import io.github.foundationgames.phonos.mixin.PersistentStateManagerAccess;
import io.github.foundationgames.phonos.network.PayloadPackets;
import io.github.foundationgames.phonos.resource.PhonosData;
import io.github.foundationgames.phonos.screen.CustomMusicDiscGuiDescription;
import io.github.foundationgames.phonos.screen.RadioJukeboxGuiDescription;
import io.github.foundationgames.phonos.util.PhonosUtil;
import io.github.foundationgames.phonos.village.TechnicianVillagerProfession;
import io.github.foundationgames.phonos.world.RadioChannelState;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Phonos implements ModInitializer {

    public static final Logger LOG = LogManager.getLogger("phonos");

    public static final ItemGroup PHONOS_ITEM_GROUP = FabricItemGroupBuilder.build(
            id("phonos"),
            () -> new ItemStack(PhonosBlocks.LOUDSPEAKER));
    public static final ItemGroup PHONOS_EXTRAS_GROUP = FabricItemGroupBuilder.build(
            id("extras"),
            () -> new ItemStack(PhonosBlocks.SPEAK_O_LANTERN));

    public static final ScreenHandlerType<RadioJukeboxGuiDescription> RADIO_JUKEBOX_HANDLER = ScreenHandlerRegistry.registerExtended(Phonos.id("radio_jukebox"), (syncId, playerInv, buf) -> {
        RadioJukeboxBlockEntity be = null;
        BlockPos p = buf.readBlockPos();
        if(playerInv.player.world.getBlockEntity(p) instanceof RadioJukeboxBlockEntity) be = (RadioJukeboxBlockEntity)playerInv.player.world.getBlockEntity(p);
        return new RadioJukeboxGuiDescription(syncId, playerInv, ScreenHandlerContext.EMPTY, be);
    });
    public static final ScreenHandlerType<CustomMusicDiscGuiDescription> CUSTOM_DISC_HANDLER = ScreenHandlerRegistry.registerExtended(Phonos.id("custom_music_disc"), (syncId, playerInv, buf) -> {
        int slot = buf.readInt();
        return new CustomMusicDiscGuiDescription(syncId, playerInv, slot);
    });

    @Override
    public void onInitialize() {
        PayloadPackets.initCommon();

        PhonosBlocks.init();
        PhonosItems.init();
        TechnicianVillagerProfession.init();

        PhonosData.registerData();

        ServerTickEvents.END_WORLD_TICK.register(world -> {
            var pStates = world.getPersistentStateManager();
            if (((PersistentStateManagerAccess)pStates).phonos$getLoadedStates().containsKey(RadioChannelState.ID)) {
                PhonosUtil.getRadioState(world).tick();
            }
        });
    }

    public static Identifier id(String path) {
        return new Identifier("phonos", path);
    }
}
