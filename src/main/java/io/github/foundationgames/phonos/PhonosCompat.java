package io.github.foundationgames.phonos;

import io.github.foundationgames.phonos.util.PhonosUtil;
import io.github.foundationgames.phonos.village.TechnicianVillagerProfession;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class PhonosCompat {
    public static TechnicianVillagerProfession.BasicTradeFactory getPatchouliBookTrade() {
        if(FabricLoader.getInstance().isModLoaded("patchouli")) {
            return new TechnicianVillagerProfession.BasicTradeFactory(i(Items.EMERALD, 3), PhonosUtil.create(() -> {
                ItemStack i = i(Registry.ITEM.get(new Identifier("patchouli:guide_book")));
                i.getOrCreateNbt().putString("patchouli:book", "phonos:phonos_guidebook");
                return i;
            }), 1, 7, 0.2f);
        }
        return new TechnicianVillagerProfession.BasicTradeFactory(i(Items.BOOK), i(Items.EMERALD, 2), i(Items.WRITABLE_BOOK),1, 7, 0.2f);
    }

    private static ItemStack i(ItemConvertible item) { return new ItemStack(item); }
    private static ItemStack i(ItemConvertible item, int count) { return new ItemStack(item, count); }
}
