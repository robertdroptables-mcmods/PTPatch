package uk.bobbytables.ptpatch;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import primal_tech.configs.ConfigHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@MethodsReturnNonnullByDefault
public class TileEntityClayKiln extends TileEntity implements ITickable, ISidedInventory {
    private NonNullList<ItemStack> kilnItemStacks = NonNullList.withSize(2, ItemStack.EMPTY);
    private boolean active = false;
    private boolean running = false;
    private int runTime = 0;
    private int temp = 200;
    
    private IItemHandler itemHandler;
    
    @Override
    public int getSizeInventory() {
        return this.kilnItemStacks.size();
    }
    
    @Override
    public boolean isEmpty() {
        for (ItemStack itemStack : this.kilnItemStacks)
            if (!itemStack.isEmpty())
                return false;
        
        return true;
    }
    
    @Override
    public ItemStack getStackInSlot(int index) {
        return this.kilnItemStacks.get(index);
    }
    
    @Override
    public ItemStack decrStackSize(int index, int count) {
        ItemStack result = ItemStackHelper.getAndSplit(this.kilnItemStacks, index, count);
        doBlockUpdate();
        return result;
    }
    
    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack result = ItemStackHelper.getAndRemove(this.kilnItemStacks, index);
        doBlockUpdate();
        return result;
    }
    
    @Override
    public void setInventorySlotContents(int index, @Nonnull ItemStack stack) {
        this.kilnItemStacks.set(index, stack);
        doBlockUpdate();
    }
    
    @Override
    public int getInventoryStackLimit() {
        return 1;
    }
    
    @Override
    public boolean isUsableByPlayer(@Nonnull EntityPlayer player) {
        if (this.world.getTileEntity(this.pos) != this)
            return false;
        else
            return player.getDistanceSq((double) this.pos.getX() + 0.5D, (double) this.pos.getY() + 0.5D, (double) this.pos.getZ() + 0.5D) <= 64.0D;
    }
    
    @Override
    public void openInventory(@Nonnull EntityPlayer player) {
    
    }
    
    @Override
    public void closeInventory(@Nonnull EntityPlayer player) {
    
    }
    
    // TODO: Store more state information
    
    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.kilnItemStacks = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
        ItemStackHelper.loadAllItems(compound, kilnItemStacks);
    }
    
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        ItemStackHelper.saveAllItems(compound, this.kilnItemStacks);
        return compound;
    }
    
    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
    }
    
    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound compound = new NBTTagCompound();
        return writeToNBT(compound);
    }
    
    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        readFromNBT(pkt.getNbtCompound());
    }
    
    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
        return oldState.getBlock() != newSate.getBlock();
    }
    
    private void doBlockUpdate() {
        IBlockState state = this.world.getBlockState(this.pos);
        this.world.notifyBlockUpdate(this.pos, state, state, 3);
    }
    
    @Override
    public boolean isItemValidForSlot(int index, @Nonnull ItemStack stack) {
        return index != 1; // Allow automation to insert into slot 0, not slot 1
    }
    
    @Override
    public int getField(int id) {
        return 0;
    }
    
    @Override
    public void setField(int id, int value) {
    
    }
    
    @Override
    public int getFieldCount() {
        return 0;
    }
    
    @Override
    public void clear() {
        kilnItemStacks.clear();
    }
    
    @Override
    public void update() {
        if (world.isRemote) return;
        
        if (active) {
            ItemStack inputItemStack = this.getStackInSlot(0);
            if (!inputItemStack.isEmpty()) {
                if (this.running) {
                    if (!this.hasHeatSource()) return;
                    --this.runTime;
                    if (this.runTime < 0) {
                        this.running = false;
                        this.setInventorySlotContents(0, ItemStack.EMPTY);
                        this.setInventorySlotContents(1, ClayKilnRecipesModified.getResult(inputItemStack));
                    }
                } else {
                    ItemStack result = ClayKilnRecipesModified.getResult(inputItemStack);
                    if (!result.isEmpty()) {
                        this.running = true;
                        this.runTime = ClayKilnRecipesModified.getCookTime(inputItemStack);
                    }
                }
            } else {
                if (this.running) {
                    this.runTime = 0;
                    this.running = false;
                }
            }
        } else {
            IBlockState blockState = this.world.getBlockState(this.pos);
            if (!blockState.getValue(BlockClayKiln.FIRED)) {
                if (this.hasHeatSource()) {
                    --temp;
                } else if (temp < 200) {
                    ++temp;
                }
                if (temp < 0) {
                    this.world.setBlockState(this.pos, blockState.withProperty(BlockClayKiln.FIRED, true));
                    this.active = true;
                }
            } else {
                this.active = true;
            }
        }
    }
    
    private boolean hasHeatSource() {
        ResourceLocation blockDownLoc = this.world.getBlockState(this.pos.down()).getBlock().getRegistryName();
        
        if (blockDownLoc == null) return false;
        
        String blockDown = blockDownLoc.toString();
        
        for (String source : ConfigHandler.FIRE_SOURCES) {
            if (blockDown.equals(source)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public String getName() {
        return "ptpatch.inv.claykiln";
    }
    
    @Override
    public boolean hasCustomName() {
        return false;
    }
    
    @Override
    public int[] getSlotsForFace(@Nonnull EnumFacing side) {
        return new int[0];
    }
    
    @Override
    public boolean canInsertItem(int index, @Nonnull ItemStack itemStackIn, @Nonnull EnumFacing direction) {
        return false;
    }
    
    @Override
    public boolean canExtractItem(int index, @Nonnull ItemStack stack, @Nonnull EnumFacing direction) {
        return false;
    }
    
    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }
    
    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return (T) (itemHandler == null ? itemHandler = new InventoryWrapper(this) : itemHandler);
        }
        return super.getCapability(capability, facing);
    }
    
    class InventoryWrapper implements IItemHandlerModifiable {
        private final IInventory inventory;
        
        private InventoryWrapper(IInventory inventory) {
            this.inventory = inventory;
        }
        
        @Override
        public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
            this.inventory.setInventorySlotContents(slot, stack);
        }
        
        @Override
        public int getSlots() {
            return this.inventory.getSizeInventory();
        }
        
        @Nonnull
        @Override
        public ItemStack getStackInSlot(int slot) {
            return this.inventory.getStackInSlot(slot);
        }
        
        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            if (this.inventory.isEmpty() && slot == 0) {
                ItemStack stackCopy = stack.copy();
                
                if (!simulate) {
                    ItemStack splitStack = stackCopy.splitStack(1);
                    this.inventory.setInventorySlotContents(slot, splitStack);
                    this.inventory.markDirty();
                } else {
                    stackCopy.shrink(1);
                }
                return stackCopy;
            }
            return stack;
        }
        
        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (amount == 0 || slot == 0) return ItemStack.EMPTY;
            
            ItemStack currentStack = this.inventory.getStackInSlot(slot).copy();
            if (!simulate) {
                this.inventory.setInventorySlotContents(slot, ItemStack.EMPTY);
            }
            return currentStack;
        }
        
        @Override
        public int getSlotLimit(int slot) {
            return inventory.getInventoryStackLimit();
        }
    }
    
    @SideOnly(Side.CLIENT)
    static class TileEntityClayKilnRenderer extends TileEntitySpecialRenderer<TileEntityClayKiln> {
        @Override
        public void render(TileEntityClayKiln te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
            if (te.isEmpty()) return;
            
            int activeSlot = te.getStackInSlot(0).isEmpty() ? 1 : 0;
            ItemStack stack = te.getStackInSlot(activeSlot);
            
            GlStateManager.pushMatrix();
            GlStateManager.translate(x + 0.5D, y + 0.3D, z + 0.5D);
            GlStateManager.scale(0.35D, 0.35D, 0.35D);
            
            EnumFacing facing = te.world.getBlockState(te.pos).getValue(BlockClayKiln.FACING);
            if (facing == EnumFacing.SOUTH) GlStateManager.rotate(0, 0, 1, 0);
            else if (facing == EnumFacing.EAST) GlStateManager.rotate(90, 0, 1, 0);
            else if (facing == EnumFacing.NORTH) GlStateManager.rotate(180, 0, 1, 0);
            else if (facing == EnumFacing.WEST) GlStateManager.rotate(270, 0, 1, 0);
            
            bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            Minecraft.getMinecraft().getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
            Minecraft.getMinecraft().getRenderItem().renderItem(stack, Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(stack, null, null));
            GlStateManager.popMatrix();
        }
    }
}
