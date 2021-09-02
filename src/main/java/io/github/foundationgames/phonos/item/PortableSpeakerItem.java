package io.github.foundationgames.phonos.item;

import io.github.foundationgames.phonos.util.PhonosUtil;
import io.github.foundationgames.phonos.world.RadioChannelState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public class PortableSpeakerItem extends Item {
    public PortableSpeakerItem(Settings settings) {
        super(settings);
        // Register with event for disconnect and after the player changes the world.
        // Also, perhaps do something with the mixin to deal with the player dropping
        // or moving the item away from their inv.
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if(user instanceof ServerPlayerEntity) stopPlaying(stack, (ServerPlayerEntity) user);
        int s = stack.getOrCreateSubNbt("RadioData").getInt("Channel");
        s += user.isSneaking() ? -1 : 1;
        if(s > 19) s = 0;
        if(s < 0) s = 19;
        stack.getOrCreateSubNbt("RadioData").putInt("Channel", s);
        return TypedActionResult.consume(stack);
    }

    public static void keepPlaying(ItemStack stack, ServerPlayerEntity user) {
        RadioChannelState pstate = PhonosUtil.getRadioState(user.getServerWorld());
        int s = stack.getOrCreateSubNbt("RadioData").getInt("Channel");
        if(user != null && !user.isDisconnected()) {
            pstate.addEntityReciever(s, user);
        }
    }

    public static void stopPlaying(ItemStack stack, ServerPlayerEntity user) {
        RadioChannelState pstate = PhonosUtil.getRadioState(user.getServerWorld());
        int channel = stack.getOrCreateSubNbt("RadioData").getInt("Channel");
        if(user != null && !user.isDisconnected()) {
            pstate.removeEntityReciever(channel, user);
        }
    }

    public static void onPlayDisconnect(ServerPlayNetworkHandler handler, MinecraftServer server) {
        PlayerInventory inventory = handler.getPlayer().getInventory();
        for(int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if(stack.getItem() instanceof PortableSpeakerItem) {
                RadioChannelState pstate = PhonosUtil.getRadioState(handler.getPlayer().getServerWorld());
                int channel = stack.getOrCreateSubNbt("RadioData").getInt("Channel");
                pstate.removeEntityReciever(channel, handler.getPlayer());
            }
        }
    }

    @Override
    public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks) {
        if(group == this.getGroup() || group == ItemGroup.SEARCH) {
            ItemStack stack = new ItemStack(this);
            stack.getOrCreateSubNbt("RadioData").putInt("Channel", 0);
            stacks.add(stack);
        }
    }

}
