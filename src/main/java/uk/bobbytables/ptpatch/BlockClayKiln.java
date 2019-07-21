package uk.bobbytables.ptpatch;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import primal_tech.PrimalTech;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

@MethodsReturnNonnullByDefault
public class BlockClayKiln extends BlockHorizontal {
    public static final PropertyBool FIRED = PropertyBool.create("fired");
    private static final AxisAlignedBB box = new AxisAlignedBB(0, 0, 0, 1, 13D / 16D, 1);
    
    public BlockClayKiln() {
        super(Material.ROCK);
        setDefaultState(blockState.getBaseState()
                .withProperty(FACING, EnumFacing.NORTH)
                .withProperty(FIRED, false));
        setHardness(1.5F);
        setResistance(10.0F);
        setSoundType(SoundType.STONE);
        setCreativeTab(PrimalTech.TAB);
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return box;
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public boolean isFullBlock(IBlockState state) {
        return false;
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }
    
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, FIRED);
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public IBlockState getStateFromMeta(int meta) {
        EnumFacing facing = EnumFacing.byHorizontalIndex(meta);
        boolean fired = (meta & 0x8) != 0;
        return getDefaultState().withProperty(FACING, facing).withProperty(FIRED, fired);
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }
    
    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex() |
                (state.getValue(FIRED) ? 0x8 : 0x0);
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
        return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
    }
    
    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        if (state.getValue(FIRED))
            return Items.BRICK;
        else
            return Item.getItemFromBlock(this);
    }
    
    @Nullable
    @Override
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        return new TileEntityClayKiln();
    }
    
    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }
    
    @Override
    public void breakBlock(World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        TileEntityClayKiln tile = (TileEntityClayKiln) worldIn.getTileEntity(pos);
        if (tile != null)
            InventoryHelper.dropInventoryItems(worldIn, pos, tile);
        super.breakBlock(worldIn, pos, state);
    }
    
    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (worldIn.isRemote) return true;
        
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (!(tileEntity instanceof TileEntityClayKiln)) return true;
        TileEntityClayKiln tileClayKiln = (TileEntityClayKiln) tileEntity;
        
        if (tileClayKiln.isEmpty()) {
            // Nothing in kiln, try insert
            ItemStack heldItem = tileClayKiln.isItemValidForSlot(0, playerIn.getHeldItemMainhand()) ? playerIn.getHeldItemMainhand() :
                    tileClayKiln.isItemValidForSlot(0, playerIn.getHeldItemOffhand()) ? playerIn.getHeldItemOffhand() : null;
            
            if (heldItem == null) return true;
            
            tileClayKiln.setInventorySlotContents(0, heldItem.splitStack(1));
        } else {
            // Kiln contains something, get slot and respond appropriately
            int slot = tileClayKiln.getStackInSlot(0).isEmpty() ? 1 : 0;
            ItemStack removedItem = tileClayKiln.removeStackFromSlot(slot);
            
            if (!playerIn.addItemStackToInventory(removedItem)) {
                InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), removedItem);
            } else {
                worldIn.playSound(playerIn, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 0.5F, 2F);
            }
            
            if (slot == 1) {
                EntityXPOrb orb = new EntityXPOrb(worldIn, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1);
                worldIn.spawnEntity(orb);
            }
        }
        
        return true;
    }
}
