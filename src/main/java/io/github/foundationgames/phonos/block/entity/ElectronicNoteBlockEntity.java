package io.github.foundationgames.phonos.block.entity;

import io.github.foundationgames.phonos.block.PhonosBlocks;
import io.github.foundationgames.phonos.world.sound.InputPlugPoint;
import io.github.foundationgames.phonos.world.sound.block.BlockConnectionLayout;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class ElectronicNoteBlockEntity extends AbstractConnectionHubBlockEntity {
    public static final BlockConnectionLayout OUTPUT_LAYOUT = new BlockConnectionLayout()
            .addPoint(-8, -4, -4, Direction.WEST)
            .addPoint(8, -4, 4, Direction.EAST)
            .addPoint(-4, -4, 8, Direction.SOUTH)
            .addPoint(4, -4, -8, Direction.NORTH);

    public ElectronicNoteBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, OUTPUT_LAYOUT, new boolean[4]);
    }

    public ElectronicNoteBlockEntity(BlockPos pos, BlockState state) {
        this(PhonosBlocks.ELECTRONIC_NOTE_BLOCK_ENTITY, pos, state);
    }

    @Override
    public boolean canConnect(ItemUsageContext ctx) {
        var side = ctx.getSide();
        if (side != Direction.UP && side != Direction.DOWN) {
            var relPos = ctx.getHitPos().subtract(ctx.getBlockPos().toCenterPos());

            if (relPos.getY() < 0) {
                return !this.outputs.isOutputPluggedIn(OUTPUT_LAYOUT.getClosestIndexClicked(ctx.getHitPos(), this.getPos()));
            }
        }

        return false;
    }

    @Override
    public boolean addConnection(Vec3d hitPos, @Nullable DyeColor color, InputPlugPoint destInput, ItemStack cable) {
        int index = OUTPUT_LAYOUT.getClosestIndexClicked(hitPos, this.getPos());

        if (this.outputs.tryPlugOutputIn(index, color, destInput, cable)) {
            this.markDirty();
            this.sync();
            return true;
        }

        return false;
    }

    @Override
    public boolean forwards() {
        return true;
    }
}
