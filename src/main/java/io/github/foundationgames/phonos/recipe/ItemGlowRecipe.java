package io.github.foundationgames.phonos.recipe;

import io.github.foundationgames.phonos.Phonos;
import io.github.foundationgames.phonos.item.GlowableItem;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class ItemGlowRecipe extends SpecialCraftingRecipe {
    public ItemGlowRecipe(Identifier id, CraftingRecipeCategory category) {
        super(id, category);
    }
    @Override
    public boolean matches(RecipeInputInventory inventory, World world) {
        int glowableItems = 0;
        int glowInkSacs = 0;

        for (var stack : inventory.getInputStacks()) {
            if (stack.getItem() instanceof GlowableItem item && !item.isGlowing(stack)) {
                glowableItems++;
            } else if (stack.isOf(Items.GLOW_INK_SAC)) {
                glowInkSacs++;
            } else if (!stack.isEmpty()) {
                return false;
            }

            if (glowableItems > 1 || glowInkSacs > 1) {
                return false;
            }
        }

        return glowableItems == 1 && glowInkSacs == 1;
    }

    @Override
    public ItemStack craft(RecipeInputInventory inventory, DynamicRegistryManager registryManager) {
        for (var stack : inventory.getInputStacks()) {
            if (stack.getItem() instanceof GlowableItem item) {
                var result = stack.copy();
                item.setGlowing(result, true);

                return result;
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public boolean fits(int width, int height) {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Phonos.ITEM_GLOW_RECIPE_SERIALIZER;
    }
}
