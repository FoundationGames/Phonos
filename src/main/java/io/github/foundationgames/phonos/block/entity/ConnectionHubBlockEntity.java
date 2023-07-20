package io.github.foundationgames.phonos.block.entity;

import io.github.foundationgames.phonos.block.ConnectionHubBlock;
import io.github.foundationgames.phonos.block.PhonosBlocks;
import io.github.foundationgames.phonos.world.sound.InputPlugPoint;
import io.github.foundationgames.phonos.world.sound.block.BlockConnectionLayout;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.state.property.Properties;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class ConnectionHubBlockEntity extends AbstractConnectionHubBlockEntity {
    public static final BlockConnectionLayout OUTPUT_LAYOUT = new BlockConnectionLayout()
            .addPoint(-4, 0, 6, Direction.WEST)
            .addPoint(4, 0, 6, Direction.EAST)
            .addPoint(0, 4, 6, Direction.UP)
            .addPoint(0, -4, 6, Direction.DOWN);

    public ConnectionHubBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, OUTPUT_LAYOUT, new boolean[4]);
    }

    public ConnectionHubBlockEntity(BlockPos pos, BlockState state) {
        this(PhonosBlocks.CONNECTION_HUB_ENTITY, pos, state);
    }

    @Override
    public boolean canConnect(ItemUsageContext ctx) {
        var side = ctx.getSide();
        var facing = ctx.getWorld().getBlockState(ctx.getBlockPos()).get(Properties.FACING);

        if (side != facing && side != facing.getOpposite()) {
            return !this.outputs.isOutputPluggedIn(OUTPUT_LAYOUT.getClosestIndexClicked(ctx.getHitPos(), this.getPos(), facing));
        }

        return false;
    }

    @Override
    public boolean addConnection(Vec3d hitPos, @Nullable DyeColor color, InputPlugPoint destInput, ItemStack cable) {
        var facing = this.getRotation();
        int index = OUTPUT_LAYOUT.getClosestIndexClicked(hitPos, this.getPos(), facing);

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

    @Override
    public Direction getRotation() {
        if (this.getCachedState().getBlock() instanceof ConnectionHubBlock block) {
            return block.getRotation(this.getCachedState());
        }

        return Direction.NORTH;
    }
}
