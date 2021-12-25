package io.github.foundationgames.phonos.village;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import io.github.foundationgames.phonos.Phonos;
import io.github.foundationgames.phonos.PhonosCompat;
import io.github.foundationgames.phonos.block.PhonosBlocks;
import io.github.foundationgames.phonos.item.PhonosItems;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.fabric.api.object.builder.v1.villager.VillagerProfessionBuilder;
import net.fabricmc.fabric.api.object.builder.v1.world.poi.PointOfInterestHelper;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.registry.Registry;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.poi.PointOfInterestType;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.BiFunction;

public enum TechnicianVillagerProfession {;

    public static final PointOfInterestType TECHNICIAN_WORKSTATION = PointOfInterestHelper.register(Phonos.id("technician_job_site"), 1, 1, PhonosBlocks.RADIO_JUKEBOX);

    public static final VillagerProfession TECHNICIAN_PROFESSION = VillagerProfessionBuilder.create().id(Phonos.id("technician")).workSound(SoundEvents.BLOCK_DISPENSER_FAIL).workstation(TECHNICIAN_WORKSTATION).build();

    private static final List<Item> TRADABLE_MUSIC_DISCS = Lists.newArrayList(Items.MUSIC_DISC_11, Items.MUSIC_DISC_13, Items.MUSIC_DISC_BLOCKS, Items.MUSIC_DISC_CAT, Items.MUSIC_DISC_CHIRP, Items.MUSIC_DISC_FAR, Items.MUSIC_DISC_MALL, Items.MUSIC_DISC_MELLOHI, Items.MUSIC_DISC_STAL, Items.MUSIC_DISC_STRAD, Items.MUSIC_DISC_WAIT, Items.MUSIC_DISC_WARD);

    public static void init() {
        Registry.register(Registry.VILLAGER_PROFESSION, Phonos.id("technician"), TECHNICIAN_PROFESSION);

        TradeOffers.PROFESSION_TO_LEVELED_TRADE.put(TECHNICIAN_PROFESSION, new Int2ObjectOpenHashMap<>(ImmutableMap.of(
                1, new TradeOffers.Factory[] {
                        PhonosCompat.getPatchouliBookTrade(),
                        new BasicTradeFactory(i(Items.EMERALD, 3), i(PhonosItems.REDSTONE_CHIP, 5), 10, 2, 0.0f)
                },
                2, new TradeOffers.Factory[] {
                        new BasicTradeFactory(i(Items.EMERALD, 8), i(PhonosItems.CHANNEL_TUNER), 3, 4, 0.0f),
                        new BasicTradeFactory(i(Items.EMERALD, 12), i(PhonosItems.NOTE_BLOCK_TUNER), 2, 5, 0.0f),
                        new BasicTradeFactory(i(Items.EMERALD, 8), i(PhonosItems.BOOMBOX), 2, 5, 0.0f),
                        new BasicTradeFactory(i(PhonosBlocks.LOUDSPEAKER), i(Items.EMERALD, 4), 4, 5, 0.0f),
                        new BasicTradeFactory(i(Items.EMERALD, 4), i(Blocks.NOTE_BLOCK), 2, 5, 0.0f)
                },
                3, new TradeOffers.Factory[] {
                        new BasicTradeFactory(i(Blocks.PUMPKIN), i(Items.EMERALD, 4), i(PhonosBlocks.GOURD_SPEAKER),10, 4, 0.0f),
                        new BasicTradeFactory(i(Blocks.NOTE_BLOCK, 4), i(Items.EMERALD, 5), i(PhonosBlocks.RADIO_NOTE_BLOCK, 4),10, 4, 0.0f),
                        new BasicTradeFactory(i(PhonosBlocks.BOOMBOX, 1), i(Items.EMERALD, 1), i(PhonosBlocks.FESTIVE_BOOMBOX, 1),10, 4, 0.0f)
                },
                4, new TradeOffers.Factory[] {
                        new BasicTradeFactory(i(Blocks.JUKEBOX, 1), i(Items.EMERALD, 4), i(PhonosBlocks.RADIO_JUKEBOX, 1), 10, 7, 0.02f),
                        new BasicTradeFactory(i(Items.EMERALD, 2), i(Blocks.REDSTONE_TORCH, 6),20, 3, 0.03f)
                },
                5, new TradeOffers.Factory[] {
                        new BasicTradeFactory((e, r) -> i(Items.EMERALD, 30 + r.nextInt(20)), Optional.empty(), (e, r) -> i(TRADABLE_MUSIC_DISCS.get(r.nextInt(TRADABLE_MUSIC_DISCS.size()))), 1, 5, 0.001f),
                        new BasicTradeFactory(i(Items.POTATO, 1), i(Items.EMERALD, 7), i(PhonosBlocks.TINY_POTATO_SPEAKER, 1), 3, 10, 0.0f)
                }
        )));
    }

    private static ItemStack i(ItemConvertible item) { return new ItemStack(item); }
    private static ItemStack i(ItemConvertible item, int count) { return new ItemStack(item, count); }

    public static class BasicTradeFactory implements TradeOffers.Factory {
        private final BiFunction<Entity, Random, ItemStack> stackA;
        private final Optional<BiFunction<Entity, Random, ItemStack>> stackB;
        private final BiFunction<Entity, Random, ItemStack> result;
        private final int maxUses;
        private final int experience;
        private final float priceMultiplier;

        public BasicTradeFactory(BiFunction<Entity, Random, ItemStack> stackA, Optional<BiFunction<Entity, Random, ItemStack>> stackB, BiFunction<Entity, Random, ItemStack> result, int maxUses, int experience, float priceMultiplier) {
            this.stackA = stackA; this.stackB = stackB; this.result = result; this.maxUses = maxUses; this.experience = experience; this.priceMultiplier = priceMultiplier;
        }

        public BasicTradeFactory(ItemStack stackA, ItemStack stackB, ItemStack result, int maxUses, int experience, float priceMultiplier) {
            this((e, r) -> stackA, Optional.of((e, r) -> stackB), (e, r) -> result, maxUses, experience, priceMultiplier);
        }

        public BasicTradeFactory(ItemStack stack, ItemStack result, int maxUses, int experience, float priceMultiplier) {
            this((e, r) -> stack, Optional.empty(), (e, r) -> result, maxUses, experience, priceMultiplier);
        }

        @Override
        public TradeOffer create(Entity entity, Random random) {
            ItemStack b = ItemStack.EMPTY;
            if(stackB.isPresent()) b = stackB.get().apply(entity, random);
            return new TradeOffer(this.stackA.apply(entity, random), b, result.apply(entity, random), this.maxUses, this.experience, this.priceMultiplier);
        }
    }
}
