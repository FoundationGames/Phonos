package io.github.foundationgames.phonos.world.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.foundationgames.phonos.sound.custom.ServerCustomAudio;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.concurrent.CompletableFuture;

public class SatelliteArgumentType implements ArgumentType<Long> {
    public static final SimpleCommandExceptionType INVALID_ID_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.phonos.idList.invalid"));

    public SatelliteArgumentType() {
    }

    @Override
    public Long parse(StringReader reader) throws CommandSyntaxException {
        try {
            var str = reader.readString();
            return Long.parseUnsignedLong(str, 16);
        } catch (NumberFormatException ex) {
            throw INVALID_ID_EXCEPTION.create();
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        var input = builder.getInput().substring(builder.getStart());

        if (context.getSource() instanceof ServerCommandSource) {
            var savedIds = ServerCustomAudio.SAVED.keySet();

            if (input.isBlank()) {
                savedIds.forEach(l -> builder.suggest(Long.toHexString(l)));

                return builder.buildFuture();
            }

            builder.suggest(input);

            for (long id : savedIds) {
                var idStr = Long.toHexString(id);

                if (idStr.startsWith(input)) {
                    builder.suggest(idStr);
                }
            }

            return builder.buildFuture();
        }

        return Suggestions.empty();
    }
}
