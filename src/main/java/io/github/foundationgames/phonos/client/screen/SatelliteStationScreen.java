package io.github.foundationgames.phonos.client.screen;

import io.github.foundationgames.phonos.Phonos;
import io.github.foundationgames.phonos.block.entity.SatelliteStationBlockEntity;
import io.github.foundationgames.phonos.network.ClientPayloadPackets;
import io.github.foundationgames.phonos.sound.custom.ClientCustomAudioUploader;
import io.github.foundationgames.phonos.sound.stream.AudioDataQueue;
import io.github.foundationgames.phonos.sound.stream.AudioFileUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class SatelliteStationScreen extends Screen {
    public static final Text TITLE = Text.translatable("container.phonos.satellite_station");
    public static final Text UPLOAD = Text.translatable("button.phonos.satellite_station.upload");
    public static final Text DRAG_PROMPT = Text.translatable("hint.phonos.satellite_station.drag").formatted(Formatting.GRAY, Formatting.ITALIC);

    protected final SatelliteStationBlockEntity station;
    private Text fileName = DRAG_PROMPT;
    private Text status = Text.empty();
    private AudioDataQueue toUpload;

    private ButtonWidget uploadButton;

    public SatelliteStationScreen(SatelliteStationBlockEntity entity) {
        super(TITLE);

        this.station = entity;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    protected void init() {
        super.init();

        this.uploadButton = this.addDrawableChild(ButtonWidget.builder(UPLOAD, b -> this.beginUpload())
                .position(this.width / 2 - 80, 150)
                .size(160, 20)
                .build());

        this.uploadButton.active = this.toUpload != null;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);

        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.client.textRenderer, this.title, this.width / 2, 80, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.client.textRenderer, this.fileName, this.width / 2, 100, 0xDDDDDD);
        context.drawCenteredTextWithShadow(this.client.textRenderer, this.status, this.width / 2, 120, 0xFFFFFF);
    }

    @Override
    public void filesDragged(List<Path> paths) {
        super.filesDragged(paths);

        if (paths.size() > 0) {
            var path = paths.get(0);

            try (var in = Files.newInputStream(path)) {
                var aud = AudioFileUtil.dataOfVorbis(in);
                if (aud != null) {
                    this.toUpload = aud;
                    this.fileName = Text.literal(path.getFileName().toString());
                    this.status = Text.translatable("status.phonos.satellite.ready_upload").formatted(Formatting.GREEN);
                } else {
                    this.status = Text.translatable("status.phonos.satellite.mono_only").formatted(Formatting.GOLD);
                    this.fileName = DRAG_PROMPT;
                    this.toUpload = null;
                }
            } catch (IOException ex) {
                Phonos.LOG.error("Error reading ogg file " + path, ex);
                this.status = Text.translatable("status.phonos.satellite.invalid_format").formatted(Formatting.RED);
                this.fileName = DRAG_PROMPT;
                this.toUpload = null;
            }
        }

        if (uploadButton != null) {
            uploadButton.active = toUpload != null;
        }
    }

    public void beginUpload() {
        if (toUpload != null) {
            ClientCustomAudioUploader.queueForUpload(station.streamId, toUpload);
            ClientPayloadPackets.sendRequestSatelliteUploadSession(station);

            uploadButton.active = false;
            this.status = Text.translatable("status.phonos.satellite.uploading").formatted(Formatting.YELLOW);
        }
    }
}
