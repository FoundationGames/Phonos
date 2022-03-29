package io.github.foundationgames.phonos.block.entity;

import io.github.foundationgames.phonos.block.PhonosBlocks;
import io.github.foundationgames.phonos.block.PianoBlock;
import io.github.foundationgames.phonos.network.PayloadPackets;
import io.github.foundationgames.phonos.util.PhonosUtil;
import io.github.foundationgames.phonos.util.piano.BuiltinPianoRolls;
import io.github.foundationgames.phonos.util.piano.PianoKeyboard;
import io.github.foundationgames.phonos.util.piano.PianoRoll;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class PlayerPianoBlockEntity extends BlockEntity {
    public final PianoKeyboard keyboard = new PianoKeyboard();

    private PianoRoll.Player playingRoll = null;

    public PlayerPianoBlockEntity(BlockPos pos, BlockState state) {
        super(PhonosBlocks.PLAYER_PIANO_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, PlayerPianoBlockEntity self) {
        if (self.playingRoll != null) {
            self.playingRoll.tick();
        }

        self.keyboard.tick();
    }

    public void togglePianoRoll() {
        if (this.playingRoll == null || this.playingRoll.done()) {
            this.playingRoll = BuiltinPianoRolls.CHOPSTICKS.createPlayable(this::playNote);
        } else {
            this.playingRoll = null;
        }
    }

    public void playNote(float pitch) {
        var state = this.world.getBlockState(this.pos);

        if (state.getBlock() instanceof PianoBlock piano) {
            int key = MathHelper.clamp(PhonosUtil.noteFromPitch(pitch), 0, 24);

            this.world.playSound(null, this.pos, piano.getInstrument(pitch, state, this.world, this.pos).getSound(), SoundCategory.RECORDS, 3.0F, pitch);
            this.keyboard.press(key);

            if (!world.isClient()) {
                world.getPlayers().forEach(player -> {
                    if (player instanceof ServerPlayerEntity sPlayer && player.getBlockPos().isWithinDistance(this.getPos(), 100)) {
                        PayloadPackets.sendPianoKeyPress(sPlayer, this, key);
                    }
                });
            }
        }
    }
}
