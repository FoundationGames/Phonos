package io.github.foundationgames.phonos.util;

import io.github.foundationgames.phonos.block.entity.Ticking;
import io.github.foundationgames.phonos.item.AudioCableItem;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.function.IntFunction;

public enum PhonosUtil {;
    public static final float SQRT2DIV2 = (float) (Math.sqrt(2) / 2);

    public static final Object2IntMap<DyeColor> DYE_COLORS = new Object2IntArrayMap<>();
    public static Quaternionf rotationTo(Direction direction) {
        return switch (direction) {
            case NORTH -> RotationAxis.POSITIVE_Y.rotationDegrees(0);
            case EAST -> RotationAxis.POSITIVE_Y.rotationDegrees(270);
            case SOUTH -> RotationAxis.POSITIVE_Y.rotationDegrees(180);
            case WEST -> RotationAxis.POSITIVE_Y.rotationDegrees(90);
            case UP -> RotationAxis.POSITIVE_X.rotationDegrees(90);
            case DOWN -> RotationAxis.POSITIVE_X.rotationDegrees(270);
        };
    }

    public static Vec3d rotateTo(Vec3d vec, Direction dir) {
        return switch (dir) {
            case SOUTH -> vec.rotateY((float) Math.PI);
            case EAST -> vec.rotateY((float) Math.PI * 0.5f);
            case WEST -> vec.rotateY((float) Math.PI * -0.5f);
            case UP -> vec.rotateX((float) Math.PI * 0.5f);
            case DOWN -> vec.rotateX((float) Math.PI * -0.5f);
            default -> vec;
        };
    }

    public static int lerpLight(float delta, int packedA, int packedB) {
        return LightmapTextureManager.pack(
                MathHelper.lerp(delta,
                        LightmapTextureManager.getBlockLightCoordinates(packedA),
                        LightmapTextureManager.getBlockLightCoordinates(packedB)
                ),
                MathHelper.lerp(delta,
                        LightmapTextureManager.getSkyLightCoordinates(packedA),
                        LightmapTextureManager.getSkyLightCoordinates(packedB)
                )
        );
    }

    public static int slotOf(Inventory inv, ItemStack stack) {
        for (int i = 0; i < inv.size(); i++) {
            if(inv.getStack(i) == stack) return i;
        }
        return -1;
    }

    public static Vector4f vec3to4(Vector3f in, Vector4f out) {
        return out.set(in.x(), in.y(), in.z(), 1);
    }

    public static Vector3f vec4to3(Vector4f in, Vector3f out) {
        return out.set(in.x(), in.y(), in.z());
    }

    public static float pitchFromNote(int note) {
        return (float) Math.pow(2, (double)(note - 12) / 12);
    }
    public static int noteFromPitch(float pitch) {
        return (int) Math.round(17.3123404907 * Math.log(pitch) + 12);
    }

    public static int getColorFromNote(int note) {
        float d = (float)note/24;
        float r = Math.max(0.0F, MathHelper.sin((d + 0.0F) * 6.2831855F) * 0.65F + 0.35F);
        float g = Math.max(0.0F, MathHelper.sin((d + 0.33333334F) * 6.2831855F) * 0.65F + 0.35F);
        float b = Math.max(0.0F, MathHelper.sin((d + 0.6666667F) * 6.2831855F) * 0.65F + 0.35F);
        Color c = new Color(r, g, b);
        return c.getRGB();
    }

    public static double maxSquaredConnectionDistance(World world) {
        return 200;
    }

    public static boolean noneNull(Object ... vals) {
        for (var val : vals) {
            if (val == null) return false;
        }
        return true;
    }

    public static boolean holdingAudioCable(PlayerEntity player) {
        return player.getStackInHand(Hand.MAIN_HAND).getItem() instanceof AudioCableItem ||
                player.getStackInHand(Hand.OFF_HAND).getItem() instanceof AudioCableItem;
    }

    @SuppressWarnings("unchecked")
    public static <E extends BlockEntity & Ticking, G extends BlockEntity> BlockEntityTicker<G> blockEntityTicker(BlockEntityType<G> givenType, BlockEntityType<E> expectedType) {
        return expectedType == givenType ? (BlockEntityTicker<G>) (BlockEntityTicker<E>) Ticking::ticker : null;
    }

    public static void writeInt(OutputStream stream, int i) throws IOException {
        stream.write(i);
        stream.write(i >> 8);
        stream.write(i >> 16);
        stream.write(i >> 24);
    }

    public static int readInt(InputStream stream) throws IOException {
        int r = 0;
        r |= stream.read();
        r |= stream.read() << 8;
        r |= stream.read() << 16;
        r |= stream.read() << 24;

        return r;
    }

    public static Path getCustomSoundFolder(MinecraftServer server) {
        return server.getSavePath(WorldSavePath.ROOT).resolve("phonos");
    }

    public static void writeBufferToPacket(PacketByteBuf packet, ByteBuffer buffer) {
        int sizeCur = packet.writerIndex();
        int size = 0;
        packet.writeInt(0);
        int bufferCur = buffer.position();
        while (buffer.hasRemaining()) {
            packet.writeByte(buffer.get());
            size++;
        }
        buffer.position(bufferCur);
        int afterSizeCur = packet.writerIndex();

        packet.writerIndex(sizeCur);
        packet.writeInt(size);
        packet.writerIndex(afterSizeCur);
    }

    public static ByteBuffer readBufferFromPacket(PacketByteBuf packet, IntFunction<ByteBuffer> create) {
        int size = packet.readInt();
        var buffer = create.apply(size);
        for (int i = 0; i < size; i++) {
            buffer.put(packet.readByte());
        }
        buffer.rewind();

        return buffer;
    }

    static {
        for (var dye : DyeColor.values()) {
            int r = (int) (dye.getColorComponents()[0] * 0xFF);
            int g = (int) (dye.getColorComponents()[1] * 0xFF);
            int b = (int) (dye.getColorComponents()[2] * 0xFF);
            DYE_COLORS.put(dye, b | (g << 8) | (r << 16));
        }
    }
}
