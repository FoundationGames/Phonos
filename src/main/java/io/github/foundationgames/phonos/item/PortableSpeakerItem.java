package io.github.foundationgames.phonos.item;

import java.util.Set;

import io.github.foundationgames.phonos.Phonos;
import io.github.foundationgames.phonos.util.PhonosUtil;
import io.github.foundationgames.phonos.world.RadioChannelState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

public class PortableSpeakerItem extends Item {
    public PortableSpeakerItem(Settings settings) {
        super(settings);
        // Register with event for disconnect and after the player changes the world.
        // Also, perhaps do something with the mixin to deal with the player dropping
        // or moving the item away from their inv.
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        // On a tick, add the player to the channel of this item.
        if(!stack.getOrCreateSubNbt("RadioData").contains("Playing")) stack.getOrCreateSubNbt("RadioData").putBoolean("Playing", false);
        if(!stack.getOrCreateSubNbt("RadioData").contains("Channel")) stack.getOrCreateSubNbt("RadioData").putInt("Channel", 0);
        if(world.isClient()) return;
        stack.getOrCreateSubNbt("RadioData").putBoolean("Playing", selected);
        RadioChannelState pstate = PhonosUtil.getRadioState((ServerWorld)world);
        int channel = stack.getOrCreateSubNbt("RadioData").getInt("Channel");
        if(entity instanceof ServerPlayerEntity && !((ServerPlayerEntity)entity).isDisconnected() && stack.getOrCreateSubNbt("RadioData").getBoolean("Playing")) {
            pstate.addEntityReciever(channel, entity);
        } else pstate.removeEntityReciever(channel, entity);
        // stack.getOrCreateSubNbt("RadioData").putInt("Channel",0);
        // stack.getOrCreateSubNbt("RadioData").putBoolean("Playing",false);
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

}
