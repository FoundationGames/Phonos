package io.github.foundationgames.phonos.item;

import io.github.foundationgames.phonos.world.sound.InputPlugPoint;
import io.github.foundationgames.phonos.world.sound.block.InputBlock;
import io.github.foundationgames.phonos.world.sound.block.OutputBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class AudioCableItem extends Item {
    public final @Nullable DyeColor color;

    public AudioCableItem(@Nullable DyeColor color, Settings settings) {
        super(settings);
        this.color = color;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        var world = context.getWorld();

        if (world.isClient() || (context.getPlayer() != null && !context.getPlayer().canModifyBlocks())) {
            return ActionResult.PASS;
        }

        var pos = context.getBlockPos();
        var hitPos = context.getHitPos();
        var stack = context.getStack();

        boolean hasInput = stack.hasNbt() && stack.getNbt().contains("Input");
        boolean hasOutput = stack.hasNbt() && stack.getNbt().contains("Output");
        boolean deplete = context.getPlayer() == null || !context.getPlayer().isCreative();

        if (world.getBlockEntity(pos) instanceof OutputBlockEntity outputs && outputs.canConnect(context)) {
            var nbt = stack.getOrCreateSubNbt("Output");
            nbt.putLong("block", pos.asLong());
            nbt.putDouble("x", hitPos.x);
            nbt.putDouble("y", hitPos.y);
            nbt.putDouble("z", hitPos.z);

            return this.tryCreateConnection(stack, world, deplete);
        } else if (world.getBlockState(pos).getBlock() instanceof InputBlock inputs && inputs.canInputConnect(context)) {
            var nbt = stack.getOrCreateSubNbt("Input");
            nbt.putLong("block", pos.asLong());
            nbt.putDouble("x", hitPos.x);
            nbt.putDouble("y", hitPos.y);
            nbt.putDouble("z", hitPos.z);

            return this.tryCreateConnection(stack, world, deplete);
        } else if (hasInput || hasOutput) {
            stack.setNbt(null);
        }

        return super.useOnBlock(context);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        // TODO sometime
        return super.useOnEntity(stack, user, entity, hand);
    }

    public ActionResult tryCreateConnection(ItemStack stack, World world, boolean deplete) {
        if (stack.hasNbt() && stack.getNbt().contains("Input")) {
            var inputNbt = stack.getSubNbt("Input");
            InputPlugPoint inputPlug = null;

            if (inputNbt.contains("block")) {
                var pos = BlockPos.fromLong(inputNbt.getLong("block"));

                if (world.isPosLoaded(pos.getX(), pos.getZ())) {
                    var hitPos = new Vec3d(inputNbt.getDouble("x"), inputNbt.getDouble("y"), inputNbt.getDouble("z"));
                    var state = world.getBlockState(pos);

                    if (state.getBlock() instanceof InputBlock block) {
                        int index = block.getInputLayout().getClosestIndexClicked(hitPos, pos, block.getRotation(state));

                        if (index >= 0) {
                            inputPlug = block.getInputLayout().inputOfConnection(
                                    InputPlugPoint.BLOCK_TYPE, pos, index);
                        }
                    }
                }
            }

            if (inputPlug != null && stack.getNbt().contains("Output")) {
                var outputNbt = stack.getSubNbt("Output");

                if (outputNbt.contains("block")) {
                    var pos = BlockPos.fromLong(outputNbt.getLong("block"));

                    if (world.isPosLoaded(pos.getX(), pos.getZ())) {
                        var hitPos = new Vec3d(outputNbt.getDouble("x"), outputNbt.getDouble("y"), outputNbt.getDouble("z"));
                        var cableStack = deplete ? stack.split(1) : stack.copyWithCount(1);

                        if (cableStack.hasNbt()) {
                            cableStack.removeSubNbt("Input");
                            cableStack.removeSubNbt("Output");
                        }

                        if (world.getBlockEntity(pos) instanceof OutputBlockEntity entity &&
                                entity.addConnection(hitPos, this.color, inputPlug, cableStack)) {
                            inputPlug.setConnected(world, true);
                            stack.setNbt(null);

                            return ActionResult.SUCCESS;
                        } else {
                            stack.setNbt(null);
                        }
                    }
                }
            }
        }

        return ActionResult.FAIL;
    }
}
