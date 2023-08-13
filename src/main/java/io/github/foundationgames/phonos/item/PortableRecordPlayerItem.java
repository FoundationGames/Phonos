package io.github.foundationgames.phonos.item;

import io.github.foundationgames.phonos.network.ClientPayloadPackets;
import io.github.foundationgames.phonos.sound.SoundStorage;
import io.github.foundationgames.phonos.sound.emitter.SoundEmitter;
import io.github.foundationgames.phonos.sound.emitter.SoundEmitterStorage;
import io.github.foundationgames.phonos.sound.emitter.SoundEmitterTree;
import io.github.foundationgames.phonos.util.UniqueId;
import io.github.foundationgames.phonos.world.sound.data.SoundEventSoundData;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.ClickType;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PortableRecordPlayerItem extends Item implements SoundEmitterItem {
    public static final Text NO_DISC = Text.translatable("tooltip.phonos.item.no_disc").formatted(Formatting.RED);
    public static final Text HOW_TO_PLAY = Text.translatable("tooltip.phonos.item.record_player_hint").formatted(Formatting.GRAY, Formatting.ITALIC);
    public static final Text PLAYING = Text.translatable("tooltip.phonos.item.playing").formatted(Formatting.GOLD);

    public PortableRecordPlayerItem(Settings settings) {
        super(settings);
    }

    @Override
    public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
        if (clickType == ClickType.RIGHT) {
            boolean result = false;
            boolean needsSync = player.isCreative() && player.getWorld().isClient();
            ItemStack sync = null;

            if (hasRecord(stack)) {
                if (otherStack.isEmpty()) {
                    if (needsSync) {
                        sync = stack.copy();
                    }

                    cursorStackReference.set(this.removeRecordAndStop(stack, player.getWorld()));
                    result = true;
                }
            } else if (otherStack.getItem() instanceof MusicDiscItem disc) {
                if (!hasEmitterId(stack)) {
                    refreshEmitterId(stack);
                }

                if (needsSync) {
                    sync = stack.copy();
                }

                this.putRecordAndPlay(stack, disc, otherStack, player.getWorld());
                cursorStackReference.set(ItemStack.EMPTY);
                result = true;
            }

            if (sync != null) {
                if (player.currentScreenHandler instanceof CreativeInventoryScreen.CreativeScreenHandler) {
                    ClientPayloadPackets.sendFakeCreativeSlotClick(sync, otherStack, clickType);
                }
            }

            return result;
        }

        return super.onClicked(stack, otherStack, slot, clickType, player, cursorStackReference);
    }

    public boolean hasEmitterId(ItemStack stack) {
        return stack.hasNbt() && stack.getNbt().contains("uid");
    }

    public long getEmitterId(ItemStack stack) {
        return stack.hasNbt() ? stack.getNbt().getLong("uid") : 0;
    }

    public long refreshEmitterId(ItemStack stack) {
        long id = UniqueId.random();
        stack.getOrCreateNbt().putLong("uid", id);

        return id;
    }

    public void removeEmitterId(ItemStack stack) {
        if (stack.hasNbt()) {
            stack.getNbt().remove("uid");
        }
    }

    public boolean hasRecord(ItemStack stack) {
        return stack.getSubNbt("Record") != null;
    }

    public void putRecordAndPlay(ItemStack stack, MusicDiscItem disc, ItemStack record, World world) {
        stack.setSubNbt("Record", record.writeNbt(new NbtCompound()));
        long emitterId = getEmitterId(stack);

        if (!world.isClient()) {
            SoundEmitterStorage.getInstance(world).addEmitter(SoundEmitter.noOp(emitterId));
            SoundStorage.getInstance(world).play(world, SoundEventSoundData.create(
                            emitterId, Registries.SOUND_EVENT.getEntry(disc.getSound()), SoundCategory.RECORDS, 2, 1),
                    new SoundEmitterTree(emitterId));
        }
    }

    public ItemStack getRecord(ItemStack stack) {
        var nbt = stack.getSubNbt("Record");
        if (this.hasRecord(stack)) {
            return ItemStack.fromNbt(nbt);
        }

        return ItemStack.EMPTY;
    }

    public ItemStack removeRecordAndStop(ItemStack stack, World world) {
        var nbt = stack.getSubNbt("Record");
        if (nbt != null) {
            if (!world.isClient() && hasEmitterId(stack)) {
                long emitterId = getEmitterId(stack);
                SoundEmitterStorage.getInstance(world).removeEmitter(emitterId);
                SoundStorage.getInstance(world).stop(world, emitterId);
            }

            removeEmitterId(stack);
            stack.removeSubNbt("Record");

            return ItemStack.fromNbt(nbt);
        }

        return ItemStack.EMPTY;
    }

    @Override
    public void onItemEntityDestroyed(ItemEntity entity) {
        super.onItemEntityDestroyed(entity);

        var world = entity.getWorld();

        if (!world.isClient()) {
            var stack = entity.getStack();
            var disc = removeRecordAndStop(stack, world);

            if (!disc.isEmpty()) {
                entity.getWorld().spawnEntity(new ItemEntity(world, entity.getX(), entity.getY(), entity.getZ(), disc));
            }
        }
    }

    @Override
    public boolean hasParentEmitter(ItemStack stack) {
        return hasEmitterId(stack);
    }

    @Override
    public boolean createsEmitter(ItemStack stack) {
        return true;
    }

    @Override
    public long getParentEmitter(ItemStack stack) {
        return getEmitterId(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);

        var record = this.getRecord(stack);
        if (record.getItem() instanceof MusicDiscItem disc) {
            tooltip.add(PLAYING);
            tooltip.add(disc.getDescription().copy().formatted(Formatting.BLUE));
            tooltip.add(TOOLTIP_HINT);
        } else {
            tooltip.add(NO_DISC);
            tooltip.add(HOW_TO_PLAY);
        }
    }
}
