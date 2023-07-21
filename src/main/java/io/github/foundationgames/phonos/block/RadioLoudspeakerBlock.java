package io.github.foundationgames.phonos.block;

import io.github.foundationgames.phonos.block.entity.RadioLoudspeakerBlockEntity;
import io.github.foundationgames.phonos.util.PhonosUtil;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class RadioLoudspeakerBlock extends AbstractLoudspeakerBlock implements BlockEntityProvider {
    public RadioLoudspeakerBlock(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        var side = hit.getSide();
        var facing = state.get(FACING);

        if (side == facing.getOpposite()) {
            if (!world.isClient()) {
                int inc = player.isSneaking() ? -1 : 1;
                if (world.getBlockEntity(pos) instanceof RadioLoudspeakerBlockEntity be) {
                    be.setAndUpdateChannel(be.getChannel() + inc);
                    be.markDirty();
                }

                return ActionResult.CONSUME;
            }

            return ActionResult.SUCCESS;
        }

        return super.onUse(state, world, pos, player, hand, hit);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new RadioLoudspeakerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return PhonosUtil.blockEntityTicker(type, PhonosBlocks.RADIO_LOUDSPEAKER_ENTITY);
    }
}
