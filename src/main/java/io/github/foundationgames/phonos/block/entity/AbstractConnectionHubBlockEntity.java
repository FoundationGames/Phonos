package io.github.foundationgames.phonos.block.entity;

import io.github.foundationgames.phonos.world.sound.block.BlockConnectionLayout;
import io.github.foundationgames.phonos.world.sound.block.OutputBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

public abstract class AbstractConnectionHubBlockEntity extends AbstractOutputBlockEntity implements Syncing, Ticking, OutputBlockEntity {
    public final boolean[] inputs;

    public AbstractConnectionHubBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, BlockConnectionLayout outputLayout, boolean[] inputs) {
        super(type, pos, state, outputLayout);

        this.inputs = inputs;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);

        for (int i = 0; i < this.inputs.length; i++) {
            this.inputs[i] = nbt.getBoolean("Input" + i);
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);

        for (int i = 0; i < this.inputs.length; i++) {
            nbt.putBoolean("Input" + i, this.inputs[i]);
        }
    }
}
