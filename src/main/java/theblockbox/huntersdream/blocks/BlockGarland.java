package theblockbox.huntersdream.blocks;

import it.unimi.dsi.fastutil.objects.AbstractObject2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import theblockbox.huntersdream.Main;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Random;

// TODO: test if #withRotation actually works
// TODO: Make everything cleaner
// TODO: Make better hitbox
public abstract class BlockGarland<T extends BlockGarland<T>> extends Block {
    public static final PropertyBool NORTH = PropertyBool.create("north");
    public static final PropertyBool SOUTH = PropertyBool.create("south");
    public static final PropertyBool WEST = PropertyBool.create("west");
    public static final PropertyBool EAST = PropertyBool.create("east");
    public static final PropertyBool[] PROPERTIES = {BlockGarland.NORTH, BlockGarland.SOUTH, BlockGarland.WEST, BlockGarland.EAST};
    public static final AxisAlignedBB DOWN_AABB = new AxisAlignedBB(0.0D, 0.05D, 0.0D, 1.0D, 0.05D, 1.0D);
    public static final AxisAlignedBB UP_AABB = new AxisAlignedBB(0.0D, 0.95D, 0.0D, 1.0D, 0.95D, 1.0D);
    public static final AxisAlignedBB NORTH_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.05D, 1.0D, 1.0D, 0.05D);
    public static final AxisAlignedBB SOUTH_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.95D, 1.0D, 1.0D, 0.95D);
    public static final AxisAlignedBB WEST_AABB = new AxisAlignedBB(0.05D, 0.0D, 0.0D, 0.05D, 1.0D, 1.0D);
    public static final AxisAlignedBB EAST_AABB = new AxisAlignedBB(0.95D, 0.0D, 0.0D, 0.95D, 1.0D, 1.0D);

    private final boolean hasTopBlock;
    private final boolean hasBottomBlock;

    static {
        for (int i = 0; i < BlockGarland.PROPERTIES.length; i++) {
            if (!BlockGarland.PROPERTIES[i].getName().equals(EnumFacing.byIndex(i + 2).toString())) {
                Main.getLogger().warn("The array BlockGarland.PROPERTIES is in the order "
                        + Arrays.toString(BlockGarland.PROPERTIES) + ", while the array EnumFacing.VALUES is in the order "
                        + Arrays.toString(EnumFacing.VALUES) + ". This is a bug and could lead to problems with " +
                        "Hunter's Dream's garlands. If you see this message, please open a new issue on our issue tracker!");
                break;
            }
        }
    }

    public BlockGarland(boolean hasTopBlock, boolean hasBottomBlock) {
        // TODO: Better material and soundtype?
        super(Material.PLANTS);
        this.setSoundType(SoundType.PLANT);
        IBlockState defaultState = this.getDefaultState();
        for (PropertyBool property : BlockGarland.PROPERTIES) {
            defaultState = defaultState.withProperty(property, false);
        }
        this.setDefaultState(defaultState);
        this.hasTopBlock = hasTopBlock;
        this.hasBottomBlock = hasBottomBlock;
    }

    @Nullable
    public static EnumFacing getFacingFromProperty(PropertyBool property) {
        for (int i = 0; i < BlockGarland.PROPERTIES.length; i++) {
            if (BlockGarland.PROPERTIES[i] == property) {
                return EnumFacing.byIndex(i + 2);
            }
        }
        return null;
    }

    public abstract T getWithProperties(boolean hasTop, boolean hasBottom);

    public abstract boolean isTheSameAs(BlockGarland<?> otherBlock);

    public boolean hasTopBlock() {
        return this.hasTopBlock;
    }

    public boolean hasBottomBlock() {
        return this.hasBottomBlock;
    }

    public IBlockState copyStateWithProperties(boolean hasTop, boolean hasBottom, IBlockState toCopy) {
        return this.getWithProperties(hasTop, hasBottom).getStateFromMeta(toCopy.getBlock().getMetaFromState(toCopy));
    }

    /**
     * Returns a pair of an {@link IBlockState} and an int. <br>
     * The {@link IBlockState} represents the new block state,
     * the int represents the amount of removed garlands.
     * (Should be used to calculate the drops.)
     */
    public Object2IntMap.Entry<IBlockState> checkSides(IBlockAccess blockAccess, BlockPos posIn) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(posIn);
        IBlockState state = blockAccess.getBlockState(posIn);
        BlockGarland<?> block = (BlockGarland<?>) state.getBlock();

        BlockGarland<?> blockToReturn = this.getWithProperties(false, false);
        int removedGarlands = 0;
        // TODO: Finish

        if (block.hasTopBlock()) {
            if (this.isAllowedNeighbor(blockAccess, pos.move(EnumFacing.UP), EnumFacing.DOWN))
                blockToReturn = this.getWithProperties(true, false);
            else
                removedGarlands++;
            pos.move(EnumFacing.DOWN);
        }

        if (block.hasBottomBlock()) {
            if (this.isAllowedNeighbor(blockAccess, pos.move(EnumFacing.DOWN), EnumFacing.UP))
                blockToReturn = this.getWithProperties(blockToReturn.hasTopBlock(), true);
            else
                removedGarlands++;
            pos.move(EnumFacing.UP);
        }

        IBlockState stateToReturn = blockToReturn.getDefaultState();
        for (PropertyBool property : BlockGarland.PROPERTIES) {
            if (state.getValue(property)) {
                EnumFacing facing = BlockGarland.getFacingFromProperty(property);
                if (facing != null) {
                    EnumFacing opposite = facing.getOpposite();
                    if (this.isAllowedNeighbor(blockAccess, pos.move(facing), opposite)) {
                        stateToReturn = stateToReturn.withProperty(property, true);
                    } else {
                        // TODO: Needed? (Doesn't the default state only have properties with false?)
                        stateToReturn = stateToReturn.withProperty(property, false);
                        removedGarlands++;
                    }
                    pos.move(opposite);
                }
            }
        }
        return new AbstractObject2IntMap.BasicEntry<>(stateToReturn, removedGarlands);
    }

    public boolean isAllowedState(IBlockState state) {
        if (state.getBlock() instanceof BlockGarland) {
            BlockGarland<?> block = (BlockGarland<?>) state.getBlock();
            if (this.isTheSameAs(block)) {
                if (block.hasTopBlock() || block.hasBottomBlock()) {
                    return true;
                } else {
                    for (PropertyBool property : BlockGarland.PROPERTIES) {
                        if (state.getValue(property)) {
                            return true;
                        }
                    }
                    return false;
                }
            }
        }
        throw new IllegalArgumentException("The block of the passed blockstate (" + state + ") is not the same as " + this);
    }

    public boolean isAllowedNeighbor(IBlockAccess blockAccess, BlockPos pos, EnumFacing facing) {
        return blockAccess.getBlockState(pos).getBlockFaceShape(blockAccess, pos, facing) == BlockFaceShape.SOLID;
    }

    @Override
    public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
        return new ItemStack(this.getWithProperties(false, false));
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Item.getItemFromBlock(this.getWithProperties(false, false));
    }

    @Override
    public int quantityDropped(IBlockState state, int fortune, Random random) {
        int toReturn = 0;
        for (PropertyBool property : BlockGarland.PROPERTIES)
            if (state.getValue(property))
                toReturn++;
        BlockGarland<?> block = (BlockGarland<?>) state.getBlock();
        if (block.hasTopBlock())
            toReturn++;
        if (block.hasBottomBlock())
            toReturn++;
        return toReturn;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, BlockGarland.PROPERTIES);
    }

    // final so that #copyStateWithProperties will always work correctly
    @Override
    public final IBlockState getStateFromMeta(int meta) {
        IBlockState toReturn = this.getDefaultState();
        for (int i = 0; i < BlockGarland.PROPERTIES.length; i++) {
            toReturn = toReturn.withProperty(BlockGarland.PROPERTIES[i], (meta & (1 << i)) != 0);
        }
        return toReturn;
    }

    @Override
    public final int getMetaFromState(IBlockState state) {
        int toReturn = 0;
        for (int i = 0; i < BlockGarland.PROPERTIES.length; i++) {
            toReturn |= (state.getValue(BlockGarland.PROPERTIES[i]) ? 1 : 0) << i;
        }
        return toReturn;
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY,
                                            float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        // TODO: When updating to 1.13, check if the array still has the same ordering
        IBlockState oldState = world.getBlockState(pos);
        Block oldBlock = oldState.getBlock();
        if ((oldBlock instanceof BlockGarland) && this.isTheSameAs((BlockGarland<?>) oldBlock)) {
            BlockGarland<?> block = (BlockGarland<?>) oldBlock;
            switch (facing.getOpposite()) {
                case DOWN:
                    return this.copyStateWithProperties(block.hasTopBlock(), true, oldState);
                case UP:
                    return this.copyStateWithProperties(true, block.hasBottomBlock(), oldState);
                default:
                    return oldState.withProperty(BlockGarland.PROPERTIES[facing.getOpposite().getIndex() - 2], true);
            }
        } else {
            switch (facing.getOpposite()) {
                case DOWN:
                    return this.getWithProperties(false, true).getDefaultState();
                case UP:
                    return this.getWithProperties(true, false).getDefaultState();
                default:
                    return this.getWithProperties(false, false).getDefaultState()
                            .withProperty(BlockGarland.PROPERTIES[facing.getOpposite().getIndex() - 2], true);
            }
        }
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        switch (rot) {
            case CLOCKWISE_90:
                return state.withProperty(BlockGarland.NORTH, state.getValue(BlockGarland.EAST))
                        .withProperty(BlockGarland.EAST, state.getValue(BlockGarland.SOUTH))
                        .withProperty(BlockGarland.SOUTH, state.getValue(BlockGarland.WEST))
                        .withProperty(BlockGarland.WEST, state.getValue(BlockGarland.NORTH));
            case CLOCKWISE_180:
                return state.withProperty(BlockGarland.NORTH, state.getValue(BlockGarland.SOUTH))
                        .withProperty(BlockGarland.EAST, state.getValue(BlockGarland.WEST))
                        .withProperty(BlockGarland.SOUTH, state.getValue(BlockGarland.NORTH))
                        .withProperty(BlockGarland.WEST, state.getValue(BlockGarland.EAST));
            case COUNTERCLOCKWISE_90:
                return state.withProperty(BlockGarland.NORTH, state.getValue(BlockGarland.WEST))
                        .withProperty(BlockGarland.EAST, state.getValue(BlockGarland.NORTH))
                        .withProperty(BlockGarland.SOUTH, state.getValue(BlockGarland.EAST))
                        .withProperty(BlockGarland.WEST, state.getValue(BlockGarland.SOUTH));
            default:
                return state;
        }
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        AxisAlignedBB toReturn = Block.FULL_BLOCK_AABB;
        int properties = 0;
        if (state.getBlock() instanceof BlockGarland) {
            BlockGarland<?> block = (BlockGarland<?>) state.getBlock();
            if (block.hasBottomBlock()) {
                toReturn = BlockGarland.DOWN_AABB;
                properties++;
            }
            if (block.hasTopBlock()) {
                toReturn = BlockGarland.UP_AABB;
                properties++;
            }
            if (state.getValue(BlockGarland.NORTH)) {
                toReturn = BlockGarland.NORTH_AABB;
                properties++;
            }
            if (state.getValue(BlockGarland.SOUTH)) {
                toReturn = BlockGarland.SOUTH_AABB;
                properties++;
            }
            if (state.getValue(BlockGarland.WEST)) {
                toReturn = BlockGarland.WEST_AABB;
                properties++;
            }
            if (state.getValue(BlockGarland.EAST)) {
                toReturn = BlockGarland.EAST_AABB;
                properties++;
            }
        }
        return (properties == 1) ? toReturn : Block.FULL_BLOCK_AABB;
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return Block.NULL_AABB;
    }

    @Override
    public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos) {
        return true;
    }

    @Override
    public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side) {
        if (this.isAllowedNeighbor(worldIn, pos.offset(side.getOpposite()), side)) {
            if (super.canPlaceBlockOnSide(worldIn, pos, side)) {
                return true;
            } else {
                IBlockState state = worldIn.getBlockState(pos);
                if (state.getBlock() instanceof BlockGarland) {
                    BlockGarland<?> block = (BlockGarland<?>) state.getBlock();
                    if (this.isTheSameAs(block)) {
                        switch (side) {
                            case DOWN:
                                return !block.hasTopBlock();
                            case UP:
                                return !block.hasBottomBlock();
                            default:
                                return !state.getValue(BlockGarland.PROPERTIES[side.getOpposite().getIndex() - 2]);
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (!worldIn.isRemote) {
            Object2IntMap.Entry<IBlockState> returned = this.checkSides(worldIn, pos);
            IBlockState returnedState = returned.getKey();
            if (returned.getIntValue() > 0)
                InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY() - 0.5D, pos.getZ(),
                        new ItemStack(this.getWithProperties(false, false), returned.getIntValue()));
            if (returnedState != state)
                worldIn.setBlockState(pos, this.isAllowedState(returnedState) ? returnedState : Blocks.AIR.getDefaultState());
        }
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }
}