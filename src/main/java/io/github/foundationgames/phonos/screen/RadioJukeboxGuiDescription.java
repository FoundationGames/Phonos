package io.github.foundationgames.phonos.screen;

import io.github.cottonmc.cotton.gui.SyncedGuiDescription;
import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import io.github.cottonmc.cotton.gui.widget.WItemSlot;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import io.github.cottonmc.cotton.gui.widget.WPlayerInvPanel;
import io.github.cottonmc.cotton.gui.widget.data.Texture;
import io.github.foundationgames.phonos.Phonos;
import io.github.foundationgames.phonos.block.entity.RadioJukeboxBlockEntity;
import io.github.foundationgames.phonos.item.CustomMusicDiscItem;
import io.github.foundationgames.phonos.util.SoundUtil;
import io.github.foundationgames.phonos.screen.widget.WBasicButton;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

public class RadioJukeboxGuiDescription extends SyncedGuiDescription {
    private static final Identifier TEXTURE = Phonos.id("textures/gui/container/radio_jukebox.png");

    private final WItemSlot discs;
    private final WPlayerInvPanel pinvPanel;
    private final RadioJukeboxBlockEntity blockEntity;

    private final WBasicButton playButton;
    private final WBasicButton forwardButton;
    private final WBasicButton backButton;
    private final WBasicButton shuffleButton;
    private final WBasicButton pitchField;
    private final WBasicButton[] durationFields = new WBasicButton[6];
    private final WBasicButton progressBar;

    public RadioJukeboxGuiDescription(int syncId, PlayerInventory playerInventory, ScreenHandlerContext ctx, RadioJukeboxBlockEntity blockEntity) {
        super(Phonos.RADIO_JUKEBOX_HANDLER, syncId, playerInventory, getBlockInventory(ctx), getBlockPropertyDelegate(ctx));
        this.blockEntity = blockEntity;

        WGridPanel root = new WGridPanel(1);
        root.setSize(162, 154);
        setRootPanel(root);

        //WText utitle = new WText(new TranslatableText("container.phonos.radio_jukebox.title"), 0x252525);
        //utitle.setSize(51, 15);
        //root.add(utitle, 0, 3);
        WLabel title = new WLabel(new TranslatableText("container.phonos.radio_jukebox.title"), 0x252525);
        title.setSize(51, 15);
        root.add(title, 0, 3);

        WItemSlot discSlots = WItemSlot.of(blockEntity, 0, 6, 1);
        discSlots.setFilter(stack -> stack.getItem() instanceof MusicDiscItem || stack.getItem() instanceof CustomMusicDiscItem);
        root.add(discSlots, 27, 32);
        this.discs = discSlots;

        //PLAY BUTTON --------------------------------------------------------------------------------------------------------------------
        WBasicButton playButton = new WBasicButton(19, 14);
        playButton.setWhenClicked((button, x, y, mouseButton) -> {
            if(blockEntity != null) blockEntity.performSyncedOperation(RadioJukeboxBlockEntity.Ops.PLAY_STOP, 0);
            SoundUtil.playPositionedSound(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, 1.0f, 1.57f, this.playerInventory.player.getBlockPos());
        });
        this.playButton = playButton;
        root.add(playButton, 71, 2);

        //SKIP BUTTONS --------------------------------------------------------------------------------------------------------------------
        WBasicButton forwardButton = new WBasicButton(18, 14);
        forwardButton.setWhenClicked((button, x, y, mouseButton) -> {
            SoundUtil.playPositionedSound(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, 1.0f, 1.57f, this.playerInventory.player.getBlockPos());
            if(blockEntity != null) blockEntity.performSyncedOperation(RadioJukeboxBlockEntity.Ops.NEXT_SONG, 0);
        });
        this.forwardButton = forwardButton;
        root.add(forwardButton, 91, 2);

        WBasicButton backButton = new WBasicButton(18, 14);
        backButton.setWhenClicked((button, x, y, mouseButton) -> {
            SoundUtil.playPositionedSound(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, 1.0f, 1.57f, this.playerInventory.player.getBlockPos());
            if(blockEntity != null) blockEntity.performSyncedOperation(RadioJukeboxBlockEntity.Ops.PREV_SONG, 0);
        });
        this.backButton = backButton;
        root.add(backButton, 52, 2);

        //SHUFFLE TOGGLE --------------------------------------------------------------------------------------------------------------------
        WBasicButton shuffleButton = new WBasicButton(15, 13);
        shuffleButton.setWhenClicked((button, x, y, mouseButton) -> {
            if(blockEntity != null) blockEntity.performSyncedOperation(RadioJukeboxBlockEntity.Ops.SET_SHUFFLE, blockEntity.doShuffle ? 0 : 1);
            SoundUtil.playPositionedSound(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, 1.0f, 1.57f, this.playerInventory.player.getBlockPos());
        });
        this.shuffleButton = shuffleButton;
        root.add(shuffleButton, 143, 40);

        //PITCH FIELD --------------------------------------------------------------------------------------------------------------------
        WBasicButton pitchAdjust = new WBasicButton(15, 13);
        pitchAdjust.setWhenScrolledOver((button, x, y, amount) -> {
            int value = 10;
            boolean g = true;
            if(blockEntity != null) {
                g = blockEntity.isPlaying();
                value = (int)(blockEntity.pitch * 10);
            }
            value += (amount > 0 ? 1 : -1);
            value = Math.min(Math.max(1, value), 20);
            if(!g) {
                blockEntity.performSyncedOperation(RadioJukeboxBlockEntity.Ops.SET_PITCH, value);
                SoundUtil.playPositionedSound(SoundEvents.BLOCK_NOTE_BLOCK_BASEDRUM, SoundCategory.MASTER, 0.3f, (float)value / 10, this.playerInventory.player.getBlockPos());
            }
        });
        this.pitchField = pitchAdjust;
        root.add(pitchAdjust, 4, 40);

        //DURATION FIELDS
        for (int i = 0; i < 6; i++) {
            WBasicButton durationField = new WBasicButton(18, 9);
            int fi = 5 - i;
            durationField.setWhenScrolledOver((button, x, y, amount) -> {
                int dur = 1;
                boolean g = true;
                boolean s = Screen.hasShiftDown();
                if (blockEntity != null) {
                    dur = (fi == 0 ? blockEntity.disc1Duration : dur);
                    dur = (fi == 1 ? blockEntity.disc2Duration : dur);
                    dur = (fi == 2 ? blockEntity.disc3Duration : dur);
                    dur = (fi == 3 ? blockEntity.disc4Duration : dur);
                    dur = (fi == 4 ? blockEntity.disc5Duration : dur);
                    dur = (fi == 5 ? blockEntity.disc6Duration : dur);
                    g = blockEntity.isPlaying();
                }
                int m = 1;
                if(s && (dur == 1 || dur == 599)) m = 4;
                else if(s) m = 5;
                dur += m * (amount>0?1:-1);
                dur = Math.min(Math.max(1, dur), 599);
                if(!g) {
                    blockEntity.performSyncedOperation(RadioJukeboxBlockEntity.SLOT_2_OP.get(fi), dur);
                    SoundUtil.playPositionedSound(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, 0.1f, 1.85f - (s ? 0.3f : 0), this.playerInventory.player.getBlockPos());
                }
            });
            durationFields[i] = durationField;
            root.add(durationField, 116 - (i * 18), 51);
        }

        WBasicButton progressBar = new WBasicButton(108, 13);
        this.progressBar = progressBar;
        root.add(progressBar, 27, 17);

        pinvPanel = this.createPlayerInventoryPanel();
        root.add(pinvPanel, 0, 69);

        root.validate(this);
    }

    @Override
    public void addPainters() {
        super.addPainters();
        getRootPanel().setBackgroundPainter((x, y, widget) -> drawTexture(createTexture(TEXTURE, 0, 0, 176, 166, 256, 256), x-7, y-3, 176, 166));
        discs.setBackgroundPainter((x, y, widget) -> {
            for (int i = 0; i < 6; i++) {
                if(blockInventory.getStack(i).isEmpty()) drawTexture(createTexture(TEXTURE, 176, 42, 18, 18, 256, 256), x+(i*18), y, 18, 18);
            }
        });
        playButton.setBackgroundPainter((x, y, mouseX, mouseY, button) -> {
            boolean g = true;
            if(blockEntity != null) g = blockEntity.isPlaying();
            drawTexture(createTexture(TEXTURE, 176+(g?19:0), 86, 19, 14, 256, 256), x, y, 19, 14);
            if(button.isWithinBounds(mouseX, mouseY)) drawTexture(createTexture(TEXTURE, 176, 100, 19, 14, 256, 256), x, y, 19, 14);
        });
        forwardButton.setBackgroundPainter((x, y, mouseX, mouseY, button) -> {
            drawTexture(createTexture(TEXTURE, 214, 86, 18, 14, 256, 256), x, y, 18, 14);
            if(button.isWithinBounds(mouseX, mouseY)) drawTexture(createTexture(TEXTURE, 195, 100, 18, 14, 256, 256), x, y, 18, 14);
        });
        backButton.setBackgroundPainter((x, y, mouseX, mouseY, button) -> {
            drawTexture(createTexture(TEXTURE, 232, 86, 18, 14, 256, 256), x, y, 18, 14);
            if(button.isWithinBounds(mouseX, mouseY)) drawTexture(createTexture(TEXTURE, 195, 100, 18, 14, 256, 256), x, y, 18, 14);
        });
        shuffleButton.setBackgroundPainter((x, y, mouseX, mouseY, button) -> {
            if(blockEntity != null && blockEntity.doShuffle) drawTexture(createTexture(TEXTURE, 176, 60, 15, 13, 256, 256), x, y, 15, 13);
            else drawTexture(createTexture(TEXTURE, 191, 60, 15, 13, 256, 256), x, y, 15, 13);
            if(button.isWithinBounds(mouseX, mouseY)) drawTexture(createTexture(TEXTURE, 176, 73, 15, 13, 256, 256), x, y, 15, 13);
        });
        pitchField.setBackgroundPainter((x, y, mouseX, mouseY, button) -> {
            boolean g = true;
            if(blockEntity != null) g = blockEntity.isPlaying();
            int d = 10;
            if(blockEntity != null) d = (int)(blockEntity.pitch * 10);
            if(g) {
                drawTexture(createTexture(TEXTURE, 208, 114, 15, 13, 256, 256), x, y, 15, 13);
            } else {
                if(!button.isWithinBounds(mouseX, mouseY)) drawTexture(createTexture(TEXTURE, 176, 114, 15, 13, 256, 256), x, y, 15, 13);
                else drawTexture(createTexture(TEXTURE, 191, 114, 17, 13, 256, 256), x, y, 17, 13);
            }
            int t = (int)Math.floor((float)d /10);
            drawDigit(x+3, y+4, t, g);
            drawDigit(x+9, y+4, d - (t*10), g);
        });
        for (int i = 0; i < durationFields.length; i++) {
            WBasicButton durationField = durationFields[i];
            int fi = 5 - i;
            durationField.setBackgroundPainter((x, y, mouseX, mouseY, button) -> {
                boolean g = blockEntity.isPlaying();
                int v = 0;
                if(g) {
                    drawTexture(createTexture(TEXTURE, 196, 14, 22, 9, 256, 256), x, y, 22, 9);
                } else {
                    if(button.isWithinBounds(mouseX, mouseY)) v += 9;
                    drawTexture(createTexture(TEXTURE, 176, 14+v, 20+(button.isWithinBounds(mouseX, mouseY)?2:0), 9, 256, 256), x, y, 20+(button.isWithinBounds(mouseX, mouseY)?2:0), 9);
                }
                int dur = 0;
                dur = (fi == 0 ? blockEntity.disc1Duration : dur);
                dur = (fi == 1 ? blockEntity.disc2Duration : dur);
                dur = (fi == 2 ? blockEntity.disc3Duration : dur);
                dur = (fi == 3 ? blockEntity.disc4Duration : dur);
                dur = (fi == 4 ? blockEntity.disc5Duration : dur);
                dur = (fi == 5 ? blockEntity.disc6Duration : dur);
                int min = (int)Math.floor((float)dur / 60);
                int secTens = (int)Math.floor((float)(dur - min*60) / 10);
                int secOnes = dur - ((min * 60) + (secTens * 10));
                drawDigit(x+4, y+2, min, g);
                drawDigit(x+10, y+2, secTens, g);
                drawDigit(x+14, y+2, secOnes, g);
            });
        }
        progressBar.setBackgroundPainter((x, y, mouseX, mouseY, button) -> {
            int track = 0;
            float progress = 0;
            boolean playing = false;
            if(blockEntity != null) {
                track = blockEntity.getPlayingSong();
                progress = 1.0f - blockEntity.getProgress();
                playing = blockEntity.isPlaying();
            }
            if(playing) {
                int len = ((track+1)*18) - 9;
                drawTexture(createTexture(TEXTURE, 0, 166, len, 9, 256, 256), x, y+2, len, 9);
                drawTexture(createTexture(TEXTURE, 202, 127, 13, 15, 256, 256), x+len-6, y, 13, 15);
                drawTexture(createTexture(TEXTURE, 189, 127, Math.round(13 * progress), 15, 256, 256), x+len-6, y, Math.round(13 * progress), 15);
            } else {
                drawTexture(createTexture(TEXTURE, 176, 127, 13, 15, 256, 256), x+3, y, 13, 15);
            }
        });
        pinvPanel.setBackgroundPainter((x, y, widget) -> {});
    }

    private static void drawTexture(Texture texture, int x, int y, int width, int height) {
        ScreenDrawing.texturedRect(x, y, width, height, texture, 0xFFFFFF, 1.0f);
    }

    private static Texture createTexture(Identifier texture, int u, int v, int width, int height, int texWidth, int texHeight) {
        float u0 = (float)u/texWidth;
        float v0 = (float)v/texHeight;
        float u1 = (float)(u+width)/texWidth;
        float v1 = (float)(v+height)/texHeight;
        return new Texture(texture, u0, v0, u1, v1);
    }

    private static void drawDigit(int x, int y, int digit, boolean grayed) {
        digit = Math.max(0, Math.min(9, digit));
        drawTexture(createTexture(TEXTURE, 176+(digit*3), 32+(grayed?5:0), 3, 5, 256, 256), x, y, 3, 5);
    }
}
