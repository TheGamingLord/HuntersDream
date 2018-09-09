package theblockbox.huntersdream.world.gen.village.component;

import java.util.Random;

import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureVillagePieces;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;

public class StructureVillageComponent extends StructureVillagePieces.House1 {
	protected ResourceLocation structureLocation;
	protected int avgGroundLevel = -1;
	protected int xSize;
	protected int ySize;
	protected int zSize;

	public StructureVillageComponent(StructureBoundingBox boundingBox, EnumFacing facing,
			ResourceLocation structureLocation, int xSize, int ySize, int zSize) {
		this.setCoordBaseMode(facing);
		this.boundingBox = boundingBox;
		this.structureLocation = structureLocation;
		if (structureLocation == null)
			throw new NullPointerException("The parameter structure location is not allowed to be null");
		this.xSize = xSize;
		this.ySize = ySize;
		this.zSize = zSize;
	}

	@Override
	public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox boundingBoxIn) {
		if (this.avgGroundLevel < 0) {
			this.avgGroundLevel = this.getAverageGroundLevel(worldIn, boundingBoxIn) - 1;
			if (this.avgGroundLevel < 0) {
				return true;
			} else {
				this.boundingBox.offset(0, this.avgGroundLevel - this.boundingBox.maxY + ySize - 1, 0);
			}
		}

		this.fillWithBlocks(worldIn, boundingBoxIn, 0, 0, 0, xSize - 1, ySize - 1, zSize - 1,
				Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
		this.spawnStructure(worldIn, randomIn, boundingBoxIn);

		for (int i = 0; i < xSize; i++) {
			for (int j = 0; j < zSize; j++) {
				this.clearCurrentPositionBlocksUpwards(worldIn, i, ySize, j, boundingBoxIn);
				this.replaceAirAndLiquidDownwards(worldIn, Blocks.DIRT.getDefaultState(), i, -1, j, boundingBoxIn);
			}
		}
		return true;
	}

	public void spawnStructure(World worldIn, Random randomIn, StructureBoundingBox boundingBoxIn) {
		Template template = worldIn.getSaveHandler().getStructureTemplateManager()
				.getTemplate(worldIn.getMinecraftServer(), this.structureLocation);
		BlockPos pos = new BlockPos(this.getXWithOffset(0, 0), this.getYWithOffset(0), this.getZWithOffset(0, 0));
		if (template != null) {
			template.addBlocksToWorld(worldIn, pos, this.getPlacementSettings(worldIn, randomIn, boundingBoxIn, pos));
		}
	}

	public PlacementSettings getPlacementSettings(World worldIn, Random randomIn, StructureBoundingBox boundingBoxIn,
			BlockPos pos) {
		EnumFacing facing = this.getCoordBaseMode();
		Mirror mirror = (facing == EnumFacing.SOUTH || facing == EnumFacing.WEST) ? Mirror.NONE : Mirror.LEFT_RIGHT;
		Rotation rotation = (facing == EnumFacing.NORTH || facing == EnumFacing.SOUTH) ? Rotation.NONE
				: Rotation.CLOCKWISE_90;
		return new PlacementSettings().setRotation(rotation).setMirror(mirror).setBoundingBox(boundingBoxIn);
	}
}