package io.github.foundationgames.phonos.world.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.foundationgames.phonos.Phonos;
import io.github.foundationgames.phonos.block.entity.SatelliteStationBlockEntity;
import io.github.foundationgames.phonos.radio.RadioStorage;
import io.github.foundationgames.phonos.sound.custom.ServerCustomAudio;
import io.github.foundationgames.phonos.util.PhonosUtil;
import io.github.foundationgames.phonos.world.RadarPoints;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.PosArgument;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class PhonosCommands {
    public static void init() {
        ArgumentTypeRegistry.registerArgumentType(Phonos.id("satellite"),
                SatelliteArgumentType.class, ConstantArgumentSerializer.of(SatelliteArgumentType::new));

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("phonos")
                    .then(literal("radar")
                            .requires(src -> src.hasPermissionLevel(2))
                            .then(argument(
                                    "channel",
                                    IntegerArgumentType.integer(0, RadioStorage.CHANNEL_COUNT - 1))
                                    .executes(ctx -> radar(
                                            ctx.getSource(),
                                            ctx.getArgument("channel", Integer.class)
                                    ))
                            )
                    )
                    .then(literal("satellite")
                            .then(literal("inspect").then(argument("pos", BlockPosArgumentType.blockPos())
                                            .executes(ctx -> satelliteInspect(
                                                    ctx.getSource(),
                                                    ctx.getArgument("pos", PosArgument.class).toAbsoluteBlockPos(ctx.getSource())
                                            ))
                                    )
                            )
                            .then(literal("list")
                                    .requires(src -> src.hasPermissionLevel(2))
                                    .executes(ctx -> satelliteList(
                                            ctx.getSource()
                                    ))
                            )
                            .then(literal("crash")
                                    .requires(src -> src.hasPermissionLevel(4) && src.getPlayer() != null)
                                    .then(argument("id", new SatelliteArgumentType())
                                            .executes(ctx -> satelliteCrash(
                                                    ctx.getSource(),
                                                    ctx.getArgument("id", Long.class)
                                            ))
                                    )
                            )
                    )
            );
        });
    }

    public static int satelliteInspect(ServerCommandSource source, BlockPos pos) {
        var world = source.getWorld();

        if (!ServerCustomAudio.loaded()) {
            source.sendError(Text.translatable("command.phonos.satellite.not_loaded"));

            return 1;
        }

        if (world.getBlockEntity(pos) instanceof SatelliteStationBlockEntity be) {
            long id = be.streamId;

            if (!ServerCustomAudio.SAVED.containsKey(id)) {
                source.sendError(Text.translatable("command.phonos.satellite.inspect.no_upload"));

                return 1;
            }

            var aud = ServerCustomAudio.SAVED.get(id);
            double sizeKB = (double)(aud.originalSize / 100) / 10D;
            int duration = (int) Math.ceil((double) aud.originalSize / aud.sampleRate);
            source.sendMessage(Text.translatable("command.phonos.satellite.entry",
                    Long.toHexString(id),
                    PhonosUtil.duration(duration),
                    sizeKB));

            return 1;
        }

        source.sendError(Text.translatable("command.phonos.satellite.inspect.invalid"));
        return 1;
    }

    public static int satelliteList(ServerCommandSource source) {
        if (!ServerCustomAudio.loaded()) {
            source.sendError(Text.translatable("command.phonos.satellite.not_loaded"));

            return 1;
        }

        var set = ServerCustomAudio.SAVED.long2ObjectEntrySet();

        if (set.isEmpty()) {
            source.sendError(Text.translatable("command.phonos.satellite.list.none"));

            return 1;
        }

        double totalSizeKB = 0;

        for (var entry : set) {
            double sizeKB = (double)(entry.getValue().originalSize / 100) / 10D;
            int duration = (int) Math.ceil((double) entry.getValue().originalSize / entry.getValue().sampleRate);
            source.sendMessage(Text.translatable("command.phonos.satellite.entry",
                    Long.toHexString(entry.getLongKey()),
                    PhonosUtil.duration(duration),
                    sizeKB));
            totalSizeKB += entry.getValue().originalSize;
        }

        totalSizeKB = (double)((int)totalSizeKB / 100) / 10D;
        source.sendMessage(Text.translatable("command.phonos.satellite.list.info", set.size(), totalSizeKB));

        return 1;
    }

    public static int satelliteCrash(ServerCommandSource source, long id) throws CommandSyntaxException {
        if (!ServerCustomAudio.loaded()) {
            source.sendError(Text.translatable("command.phonos.satellite.not_loaded"));

            return 1;
        }

        var idStr = Long.toHexString(id);
        Phonos.LOG.info("Satellite {} was crashed via command by player {}.", idStr, source.getPlayerOrThrow());

        ServerCustomAudio.deleteSaved(source.getServer(), id);
        source.sendMessage(Text.translatable("command.phonos.satellite.crash", idStr));

        return 1;
    }

    public static int radar(ServerCommandSource source, int channel) {
        var world = source.getWorld();
        var origin = source.getPosition();

        var radar = RadarPoints.get(world);
        var pos = new BlockPos.Mutable();

        var result = new BlockPos.Mutable();
        double minSqDist = Double.POSITIVE_INFINITY;

        var points = radar.getPoints(channel);
        if (points == null || points.size() == 0) {
            source.sendError(Text.translatable("command.phonos.radar.none_found", channel));

            return 1;
        }

        for (long l : radar.getPoints(channel)) {
            pos.set(l);

            double sqDist = origin.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ());
            if (sqDist < minSqDist) {
                result.set(pos);
                minSqDist = sqDist;
            }
        }

        sendCoordinates(source, "command.phonos.radar.success", result.up());

        return 1;
    }

    private static void sendCoordinates(ServerCommandSource source, String key, BlockPos pos) {
        Text coords = Texts.bracketed(
                Text.translatable("chat.coordinates", pos.getX(), pos.getY(), pos.getZ())
        ).styled(style ->
                style.withColor(Formatting.GREEN)
                        .withClickEvent(
                                new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp @s " + pos.getX() + " " + pos.getY() + " " + pos.getZ()))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable("chat.coordinates.tooltip"))));

        source.sendFeedback(() -> Text.translatable(key, coords), false);
    }
}
