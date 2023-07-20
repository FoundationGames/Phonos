package io.github.foundationgames.phonos.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.List;

public class ItemGroupQueue implements ItemGroup.EntryCollector {
    public final Identifier id;
    private final List<Item> items = new ArrayList<>();

    public ItemGroupQueue(Identifier id) {
        this.id = id;
    }

    public Text displayName() {
        return Text.translatable(Util.createTranslationKey("itemGroup", id));
    }

    public void queue(Item item) {
        this.items.add(item);
    }

    @Override
    public void accept(ItemGroup.DisplayContext displayContext, ItemGroup.Entries entries) {
        items.forEach(item -> entries.add(item.getDefaultStack()));
    }
}
