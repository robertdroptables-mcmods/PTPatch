package uk.bobbytables.ptpatch;

import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import primal_tech.ModBlocks;

import javax.annotation.Nullable;
import java.util.List;

@Mod.EventBusSubscriber(modid = References.MOD_ID)
public class Registrar {
    static Block CLAY_KILN;
    static ItemBlock CLAY_KILN_ITEM;
    
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        CLAY_KILN = new BlockClayKiln().setRegistryName("primal_tech", "clay_kiln").setTranslationKey("primal_tech.clay_kiln");
        event.getRegistry().register(CLAY_KILN);
        GameRegistry.registerTileEntity(TileEntityClayKiln.class, new ResourceLocation("ptpatch:clay_kiln"));
        ModBlocks.CLAY_KILN = CLAY_KILN;
    }
    
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void registerItems(RegistryEvent.Register<Item> event) {
        CLAY_KILN_ITEM = (ItemBlock) new ItemBlock(CLAY_KILN) {
            @Override
            @SideOnly(Side.CLIENT)
            public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> list, ITooltipFlag flag) {
                list.add(TextFormatting.YELLOW + new TextComponentTranslation("tooltip.clay_kiln").getFormattedText());
            }
        }.setRegistryName(CLAY_KILN.getRegistryName()).setTranslationKey("primal_tech.clay_kiln");
        event.getRegistry().register(CLAY_KILN_ITEM);
        ModBlocks.CLAY_KILN_ITEM = CLAY_KILN_ITEM;
    }
}
