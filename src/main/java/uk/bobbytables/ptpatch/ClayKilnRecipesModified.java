package uk.bobbytables.ptpatch;

import net.minecraft.item.ItemStack;
import primal_tech.recipes.ClayKilnRecipes;

import java.lang.reflect.Field;
import java.util.ArrayList;

class ClayKilnRecipesModified {
    private static Field recipes;
    private static Field output;
    private static Field input;
    private static Field cookTime;
    
    static {
        try {
            recipes = ClayKilnRecipes.class.getDeclaredField("recipes");
            recipes.setAccessible(true);
            output = ClayKilnRecipes.class.getDeclaredField("output");
            output.setAccessible(true);
            input = ClayKilnRecipes.class.getDeclaredField("input");
            input.setAccessible(true);
            cookTime = ClayKilnRecipes.class.getDeclaredField("cookTime");
            cookTime.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static ItemStack getResult(ItemStack testInput) {
        try {
            for (ClayKilnRecipes recipe : (ArrayList<ClayKilnRecipes>) recipes.get(null)) {
                ItemStack recipeInput = (ItemStack) input.get(recipe);
                if (ItemStack.areItemsEqual(recipeInput, testInput)) {
                    return ((ItemStack) output.get(recipe)).copy();
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        
        return ItemStack.EMPTY;
    }
    
    public static int getCookTime(ItemStack testInput) {
        try {
            for (ClayKilnRecipes recipe : (ArrayList<ClayKilnRecipes>) recipes.get(null)) {
                ItemStack recipeInput = (ItemStack) input.get(recipe);
                if (ItemStack.areItemsEqual(recipeInput, testInput)) {
                    return ((int) cookTime.get(recipe));
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        
        return 0;
    }
}
