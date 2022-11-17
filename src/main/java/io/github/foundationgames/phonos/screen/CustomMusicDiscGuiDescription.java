package io.github.foundationgames.phonos.screen;

import io.github.cottonmc.cotton.gui.SyncedGuiDescription;
import io.github.cottonmc.cotton.gui.widget.*;
import io.github.cottonmc.cotton.gui.widget.data.Axis;
import io.github.foundationgames.phonos.Phonos;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class CustomMusicDiscGuiDescription extends SyncedGuiDescription {
    private final ItemStack stack;
    private final int stackSlot;
    private final WTextField soundField;
    private final WLabel errorLabel;
    private int comparatorOutput = 0;

    public CustomMusicDiscGuiDescription(int syncId, PlayerInventory playerInventory, int stackSlot) {
        super(Phonos.CUSTOM_DISC_HANDLER, syncId, playerInventory);
        this.stack = playerInventory.getStack(stackSlot);
        this.stackSlot = stackSlot;

        WGridPanel root = new WGridPanel(9);
        root.setSize(300, 66);
        setRootPanel(root);

        WTextField soundIdField = new WTextField();
        soundIdField.setEditable(true);
        soundIdField.setMaxLength(99);
        soundIdField.setText(stack.getOrCreateSubNbt("MusicData").getString("SoundId"));
        this.soundField = soundIdField;
        root.add(soundIdField, 0, 1, 24, 7);

        WLabel comparatorLabel = new WLabel(Text.literal(""));
        root.add(comparatorLabel, 8, 4, 20, 7);

        WSlider comparatorOutField = new WSlider(1, 15, Axis.HORIZONTAL);
        comparatorOutField.setValue(stack.getOrCreateSubNbt("MusicData").getInt("ComparatorSignal"));
        comparatorOutField.setDraggingFinishedListener(i -> {
            this.comparatorOutput = i;
            comparatorLabel.setText(Text.literal(Integer.toString(i)));
        });
        root.add(comparatorOutField, 0, 4, 18, 4);

        WButton confirmSoundButton = new WButton();
        confirmSoundButton.setLabel(Text.translatable("button.phonos.set_sound_id"));
        confirmSoundButton.setOnClick(() -> setStackSoundId(soundIdField.getText()));
        root.add(confirmSoundButton, 25, 1, 8, 1);

        WButton confirmComparatorButton = new WButton();
        confirmComparatorButton.setLabel(Text.translatable("button.phonos.set_comparator_signal"));
        confirmComparatorButton.setOnClick(() -> {
            setDiscComparatorSignal(comparatorOutField.getValue());
            success("log.phonos.comparator_signal_success");
        });
        root.add(confirmComparatorButton, 19, 4, 14, 7);

        WLabel errorLabel = new WLabel(Text.literal(""));
        this.errorLabel = errorLabel;
        root.add(errorLabel, 0, 7);

        root.validate(this);
    }

    public static void registerServerPackets() {
        ServerPlayNetworking.registerGlobalReceiver(Phonos.id("set_disc_sound_id"), (server, player, handler, buf, sender) -> {
            ItemStack stack = player.getInventory().getStack(buf.readInt());
            String soundId = buf.readString(32767);
            server.execute(() -> stack.getOrCreateSubNbt("MusicData").putString("SoundId", soundId));
        });
        ServerPlayNetworking.registerGlobalReceiver(Phonos.id("set_disc_comparator_signal"), (server, player, handler, buf, sender) -> {
            ItemStack stack = player.getInventory().getStack(buf.readInt());
            int signal = buf.readInt();
            server.execute(() -> stack.getOrCreateSubNbt("MusicData").putInt("ComparatorSignal", signal));
        });
    }

    @Environment(EnvType.CLIENT)
    private void setDiscSoundId(String sid) {
        stack.getOrCreateSubNbt("MusicData").putString("SoundId", sid);
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(stackSlot);
        buf.writeString(sid);
        ClientPlayNetworking.send(Phonos.id("set_disc_sound_id"), buf);
    }

    @Environment(EnvType.CLIENT)
    private void setDiscComparatorSignal(int signal) {
        stack.getOrCreateSubNbt("MusicData").putInt("ComparatorSignal", signal);
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(stackSlot);
        buf.writeInt(signal);
        ClientPlayNetworking.send(Phonos.id("set_disc_comparator_signal"), buf);
    }

    private void setStackSoundId(String sid) {
        Identifier id = Identifier.tryParse(sid);
        if(id == null) error("log.phonos.sound_id_error");
        else {
            setDiscSoundId(id.toString());
            success("log.phonos.sound_id_success");
        }
    }

    private void error(String errorKey) {
        this.errorLabel.setText(Text.translatable(errorKey).formatted(Formatting.RED));
    }
    private void success(String successKey) {
        this.errorLabel.setText(Text.translatable(successKey).formatted(Formatting.DARK_GREEN));
    }
}
