package io.github.foundationgames.phonos.screen;

import io.github.cottonmc.cotton.gui.SyncedGuiDescription;
import io.github.cottonmc.cotton.gui.widget.*;
import io.github.cottonmc.cotton.gui.widget.data.Axis;
import io.github.foundationgames.phonos.Phonos;
import io.github.foundationgames.phonos.block.entity.RadioJukeboxBlockEntity;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.function.IntConsumer;

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
        soundIdField.setText(stack.getOrCreateSubTag("MusicData").getString("SoundId"));
        this.soundField = soundIdField;
        root.add(soundIdField, 0, 1, 24, 7);

        WLabel comparatorLabel = new WLabel(new LiteralText(""));
        root.add(comparatorLabel, 8, 4, 20, 7);

        WSlider comparatorOutField = new WSlider(1, 15, Axis.HORIZONTAL);
        comparatorOutField.setValue(stack.getOrCreateSubTag("MusicData").getInt("ComparatorSignal"));
        comparatorOutField.setDraggingFinishedListener(i -> {
            this.comparatorOutput = i;
            comparatorLabel.setText(new LiteralText(Integer.toString(i)));
        });
        root.add(comparatorOutField, 0, 4, 18, 4);

        WButton confirmSoundButton = new WButton();
        confirmSoundButton.setLabel(new TranslatableText("button.phonos.set_sound_id"));
        confirmSoundButton.setOnClick(() -> setStackSoundId(soundIdField.getText()));
        root.add(confirmSoundButton, 25, 1, 8, 1);

        WButton confirmComparatorButton = new WButton();
        confirmComparatorButton.setLabel(new TranslatableText("button.phonos.set_comparator_signal"));
        confirmComparatorButton.setOnClick(() -> {
            setDiscComparatorSignal(comparatorOutField.getValue());
            success("log.phonos.comparator_signal_success");
        });
        root.add(confirmComparatorButton, 19, 4, 14, 7);

        WLabel errorLabel = new WLabel(new LiteralText(""));
        this.errorLabel = errorLabel;
        root.add(errorLabel, 0, 7);

        root.validate(this);
    }

    public static void registerServerPackets() {
        ServerSidePacketRegistry.INSTANCE.register(Phonos.id("set_disc_sound_id"), (ctx, buf) -> {
            ItemStack stack = ctx.getPlayer().inventory.getStack(buf.readInt());
            String soundId = buf.readString(32767);
            ctx.getTaskQueue().execute(() -> {
                stack.getOrCreateSubTag("MusicData").putString("SoundId", soundId);
            });
        });
        ServerSidePacketRegistry.INSTANCE.register(Phonos.id("set_disc_comparator_signal"), (ctx, buf) -> {
            ItemStack stack = ctx.getPlayer().inventory.getStack(buf.readInt());
            int signal = buf.readInt();
            ctx.getTaskQueue().execute(() -> {
                stack.getOrCreateSubTag("MusicData").putInt("ComparatorSignal", signal);
            });
        });
    }

    @Environment(EnvType.CLIENT)
    private void setDiscSoundId(String sid) {
        stack.getOrCreateSubTag("MusicData").putString("SoundId", sid);
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(stackSlot);
        buf.writeString(sid);
        ClientSidePacketRegistry.INSTANCE.sendToServer(Phonos.id("set_disc_sound_id"), buf);
    }

    @Environment(EnvType.CLIENT)
    private void setDiscComparatorSignal(int signal) {
        stack.getOrCreateSubTag("MusicData").putInt("ComparatorSignal", signal);
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(stackSlot);
        buf.writeInt(signal);
        ClientSidePacketRegistry.INSTANCE.sendToServer(Phonos.id("set_disc_comparator_signal"), buf);
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
        this.errorLabel.setText(new TranslatableText(errorKey).formatted(Formatting.RED));
    }
    private void success(String successKey) {
        this.errorLabel.setText(new TranslatableText(successKey).formatted(Formatting.DARK_GREEN));
    }
}
