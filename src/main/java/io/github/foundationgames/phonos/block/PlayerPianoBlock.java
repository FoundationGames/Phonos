package io.github.foundationgames.phonos.block;

import io.github.foundationgames.phonos.block.entity.PlayerPianoBlockEntity;
import net.minecraft.block.Block;
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

public class PlayerPianoBlock extends PianoBlock implements BlockEntityProvider {
    public PlayerPianoBlock(Settings settings, Block neighbor) {
        super(settings, Side.LEFT, neighbor);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        return ActionResult.SUCCESS;
    }

    @Override
    public void onPowered(BlockState state, World world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof PlayerPianoBlockEntity pianoEntity) {
            pianoEntity.togglePianoRoll();
        }
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new PlayerPianoBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return RadioJukeboxBlock.typeCheck(type, PhonosBlocks.PLAYER_PIANO_ENTITY, PlayerPianoBlockEntity::tick);
    }
}
