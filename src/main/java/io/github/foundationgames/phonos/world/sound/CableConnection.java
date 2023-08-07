package io.github.foundationgames.phonos.world.sound;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.DyeColor;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class CableConnection {
    public final CablePlugPoint start;
    public final InputPlugPoint end;
    public final @Nullable DyeColor color;
    public final ItemStack drop;

    public CableConnection(CablePlugPoint start, InputPlugPoint end, @Nullable DyeColor color, ItemStack drop) {
        this.start = start;
        this.end = end;
        this.color = color;
        this.drop = drop;
    }

    public boolean shouldRemove(World world) {
        if (!end.canPlugExist(world)) {
            return true;
        }

        return false; //start.getPos(world).squaredDistanceTo(end.getPos(world)) > PhonosUtil.maxSquaredConnectionDistance(world);
    }

    public boolean isStatic() {
        return start.isStatic() && end.isStatic();
    }

    public void writeNbt(NbtCompound nbt) {
        if (color != null) {
            nbt.putString("color", color.getName());
        }

        var endData = new NbtCompound();
        end.writeNbt(endData);

        nbt.put("EndPoint", endData);

        nbt.put("item", drop.writeNbt(new NbtCompound()));
    }

    public static CableConnection readNbt(World world, CablePlugPoint start, NbtCompound nbt) {
        DyeColor color = null;
        if (nbt.contains("color")) {
            color = DyeColor.byName(nbt.getString("color"), null);
        }

        var endData = nbt.getCompound("EndPoint");
        if (endData == null) return null;

        var end = InputPlugPoint.readInputPoint(endData, world);
        if (end == null) return null;

        var cableData = nbt.getCompound("item");
        if (cableData == null) return null;

        var cable = ItemStack.fromNbt(cableData);
        if (cable == null) return null;

        return new CableConnection(start, end, color, cable);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CableConnection that = (CableConnection) o;
        return Objects.equals(start, that.start) && Objects.equals(end, that.end) && color == that.color && Objects.equals(drop, that.drop);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end, color, drop);
    }
}
