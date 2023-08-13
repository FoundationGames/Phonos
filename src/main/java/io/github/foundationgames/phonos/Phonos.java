package io.github.foundationgames.phonos;

import io.github.foundationgames.phonos.block.PhonosBlocks;
import io.github.foundationgames.phonos.item.ItemGroupQueue;
import io.github.foundationgames.phonos.item.PhonosItems;
import io.github.foundationgames.phonos.network.PayloadPackets;
import io.github.foundationgames.phonos.radio.RadioDevice;
import io.github.foundationgames.phonos.radio.RadioStorage;
import io.github.foundationgames.phonos.recipe.ItemGlowRecipe;
import io.github.foundationgames.phonos.sound.SoundStorage;
import io.github.foundationgames.phonos.sound.custom.ServerCustomAudio;
import io.github.foundationgames.phonos.sound.emitter.SoundEmitter;
import io.github.foundationgames.phonos.sound.emitter.SoundEmitterStorage;
import io.github.foundationgames.phonos.sound.stream.ServerOutgoingStreamHandler;
import io.github.foundationgames.phonos.util.PhonosUtil;
import io.github.foundationgames.phonos.world.command.PhonosCommands;
import io.github.foundationgames.phonos.world.sound.InputPlugPoint;
import io.github.foundationgames.phonos.world.sound.data.SoundDataTypes;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.FallibleItemDispenserBehavior;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.world.GameRules;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;

public class Phonos implements ModInitializer {
    public static final Logger LOG = LogManager.getLogger("phonos");

    public static final ItemGroupQueue PHONOS_ITEMS = new ItemGroupQueue(id("phonos"));

    public static final GameRules.Key<GameRules.IntRule> PHONOS_UPLOAD_LIMIT_KB = GameRuleRegistry.register(
            "phonosUploadLimitKB", GameRules.Category.MISC, GameRuleFactory.createIntRule(-1, -1));

    public static final Identifier STREAMED_SOUND = Phonos.id("streamed");

    public static final RecipeSerializer<ItemGlowRecipe> ITEM_GLOW_RECIPE_SERIALIZER = Registry.register(
            Registries.RECIPE_SERIALIZER, Phonos.id("crafting_special_itemglow"), new SpecialRecipeSerializer<>(ItemGlowRecipe::new));

    @Override
    public void onInitialize() {
        Registry.register(Registries.ITEM_GROUP, PHONOS_ITEMS.id, FabricItemGroup.builder()
                .icon(PhonosBlocks.LOUDSPEAKER.asItem()::getDefaultStack)
                .displayName(PHONOS_ITEMS.displayName())
                .entries(PHONOS_ITEMS)
                .build());

        PayloadPackets.initCommon();

        PhonosBlocks.init();
        PhonosItems.init();

        SoundDataTypes.init();
        InputPlugPoint.init();

        ServerLifecycleEvents.SERVER_STARTING.register(e -> {
            RadioStorage.serverReset();
            SoundStorage.serverReset();
            SoundEmitterStorage.serverReset();
            ServerOutgoingStreamHandler.reset();
        });

        ServerLifecycleEvents.SERVER_STARTED.register(e -> {
            ServerCustomAudio.reset();
            try {
                var path = PhonosUtil.getCustomSoundFolder(e);
                if (!Files.exists(path)) Files.createDirectory(path);

                ServerCustomAudio.load(path);
            } catch (IOException ex) {
                Phonos.LOG.error("Error loading custom audio files", ex);
            }
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                ServerCustomAudio.onPlayerDisconnect(handler.getPlayer()));

        ServerTickEvents.END_WORLD_TICK.register(world -> SoundStorage.getInstance(world).tick(world));
        ServerTickEvents.START_SERVER_TICK.register(ServerOutgoingStreamHandler::tick);

        ServerBlockEntityEvents.BLOCK_ENTITY_LOAD.register((be, world) -> {
            if (be instanceof SoundEmitter p) {
                SoundEmitterStorage.getInstance(world).addEmitter(p);
            }
            if (be instanceof RadioDevice.Receiver rec) {
                rec.setAndUpdateChannel(rec.getChannel());
            }
        });
        ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register((be, world) -> {
            if (be instanceof SoundEmitter p) {
                SoundEmitterStorage.getInstance(world).removeEmitter(p);
            }
            if (be instanceof RadioDevice.Receiver rec) {
                rec.removeReceiver();
            }
        });

        DispenserBlock.registerBehavior(PhonosItems.HEADSET, new FallibleItemDispenserBehavior() {
            @Override
            protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
                this.setSuccess(ArmorItem.dispenseArmor(pointer, stack));
                return stack;
            }
        });

        RadioStorage.init();
        PhonosCommands.init();
    }

    public static Identifier id(String path) {
        return new Identifier("phonos", path);
    }
}
