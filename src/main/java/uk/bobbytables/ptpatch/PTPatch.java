package uk.bobbytables.ptpatch;

import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod(modid = References.MOD_ID, name = References.MOD_NAME, version = References.VERSION)
public class PTPatch {
    @SideOnly(Side.CLIENT)
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityClayKiln.class, new TileEntityClayKiln.TileEntityClayKilnRenderer());
    }
}
