package theblockbox.huntersdream.util.handlers;

import static theblockbox.huntersdream.util.Transformation.VAMPIRE;
import static theblockbox.huntersdream.util.Transformation.WEREWOLF;

import java.io.File;
import java.util.stream.Stream;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import theblockbox.huntersdream.Main;
import theblockbox.huntersdream.blocks.tileentity.TileEntityCampfire;
import theblockbox.huntersdream.blocks.tileentity.TileEntitySilverFurnace;
import theblockbox.huntersdream.commands.CommandsMoonphase;
import theblockbox.huntersdream.commands.CommandsTransformation;
import theblockbox.huntersdream.commands.CommandsTransformationTexture;
import theblockbox.huntersdream.init.BlockInit;
import theblockbox.huntersdream.init.CapabilitiesInit;
import theblockbox.huntersdream.init.EntityInit;
import theblockbox.huntersdream.init.ItemInit;
import theblockbox.huntersdream.init.LootTableInit;
import theblockbox.huntersdream.init.PotionInit;
import theblockbox.huntersdream.init.SoundInit;
import theblockbox.huntersdream.init.StructureInit;
import theblockbox.huntersdream.util.Reference;
import theblockbox.huntersdream.util.SilverFurnaceRecipe;
import theblockbox.huntersdream.util.Transformation;
import theblockbox.huntersdream.util.collection.TransformationToFloatMap;
import theblockbox.huntersdream.util.compat.OreDictionaryCompat;
import theblockbox.huntersdream.util.effective_against_transformation.EffectiveAgainstTransformation;
import theblockbox.huntersdream.util.effective_against_transformation.EntityEffectiveAgainstTransformation;
import theblockbox.huntersdream.util.effective_against_transformation.ItemEffectiveAgainstTransformation;
import theblockbox.huntersdream.util.helpers.GeneralHelper;
import theblockbox.huntersdream.util.helpers.WerewolfHelper;
import theblockbox.huntersdream.world.gen.WorldGenOres;

/**
 * In this class everythings gets registered (items, blocks, entities, biomes,
 * ores, event handlers etc.)
 */
@Mod.EventBusSubscriber(modid = Reference.MODID)
public class RegistryHandler {
	private static File directory;

	// Registry events

	@SubscribeEvent
	public static void onBlockRegister(RegistryEvent.Register<Block> event) {
		BlockInit.onBlockRegister(event);
	}

	@SubscribeEvent
	public static void onItemRegister(RegistryEvent.Register<Item> event) {
		ItemInit.onItemRegister(event);
	}

	@SubscribeEvent
	public static void onEntityRegister(RegistryEvent.Register<EntityEntry> event) {
		EntityInit.registerEntities(event);
	}

	@SubscribeEvent
	public static void onPotionRegister(RegistryEvent.Register<Potion> event) {
		event.getRegistry().registerAll(PotionInit.POTIONS.toArray(new Potion[0]));
	}

	@SubscribeEvent
	public static void onPotionTypeRegister(RegistryEvent.Register<PotionType> event) {
		PotionInit.registerPotionTypes();
		event.getRegistry().registerAll(PotionInit.POTION_TYPES.toArray(new PotionType[0]));
	}

	@SubscribeEvent
	public static void onSoundRegister(RegistryEvent.Register<SoundEvent> event) {
		event.getRegistry().registerAll(SoundInit.SOUND_EVENTS.toArray(new SoundEvent[0]));
	}

	@SubscribeEvent
	public static void onModelRegister(ModelRegistryEvent event) {
		Stream.concat(ItemInit.ITEMS.stream(), BlockInit.BLOCKS.stream().map(Item::getItemFromBlock))
				.forEach(item -> Main.proxy.registerItemRenderer(item, 0, "inventory"));
		EntityInit.registerEntityRenders();
	}

	// Common

	public static void preInitCommon(FMLPreInitializationEvent event) {
		CapabilitiesInit.registerCapabilities();
		NetworkRegistry.INSTANCE.registerGuiHandler(Main.instance, new GuiHandler());
		Transformation.preInit();
		GameRegistry.registerTileEntity(TileEntitySilverFurnace.class,
				GeneralHelper.newResLoc("tile_entity_silver_furnace"));
		GameRegistry.registerTileEntity(TileEntityCampfire.class, GeneralHelper.newResLoc("tile_entity_campfire"));
		directory = event.getModConfigurationDirectory();
	}

	public static void initCommon(FMLInitializationEvent event) {
		PacketHandler.register();
		GameRegistry.addSmelting(BlockInit.ORE_SILVER, new ItemStack(ItemInit.INGOT_SILVER), 0.9F);
		MinecraftForge.addGrassSeed(new ItemStack(BlockInit.WOLFSBANE), 5);
		LootTableInit.register();
		StructureInit.register();
		OreDictionaryCompat.registerOres();
		GameRegistry.registerWorldGenerator(new WorldGenOres(), 0);
	}

	public static void postInitCommon(FMLPostInitializationEvent event) {
		// TODO: Make better values
		// register objects that are effective against a specific transformation

		new ItemEffectiveAgainstTransformation(
				GeneralHelper.getPredicateMatchesOreDict(OreDictionaryCompat.SILVER_NAMES),
				new TransformationToFloatMap().put(WEREWOLF, EffectiveAgainstTransformation.DEFAULT_EFFECTIVENESS)
						.put(VAMPIRE, EffectiveAgainstTransformation.DEFAULT_EFFECTIVENESS)).register();

		new EntityEffectiveAgainstTransformation(entity -> {
			if (entity instanceof EntityTippedArrow) {
				EntityTippedArrow arrow = (EntityTippedArrow) entity;
				// using AT to access fields
				return Stream.concat(arrow.potion.getEffects().stream(), arrow.customPotionEffects.stream())
						.map(PotionEffect::getPotion)
						.anyMatch(potion -> (potion == MobEffects.POISON) || (potion == PotionInit.POTION_WOLFSBANE));
			}
			return false;
		}, new TransformationToFloatMap().put(WEREWOLF, 1F)).register();

		new EntityEffectiveAgainstTransformation(entity -> {
			return (entity instanceof EntityLivingBase) && WerewolfHelper.isTransformed((EntityLivingBase) entity);
		}, new TransformationToFloatMap().put(VAMPIRE, 2.5F)).register();
	}

	// Client

	public static void preInitClient() {
	}

	public static void initClient() {
	}

	public static void postInitClient() {
	}

	// Server

	public static void preInitServer() {
	}

	public static void initServer() {
	}

	public static void postInitServer() {
		SilverFurnaceRecipe.setAndLoadFiles(directory);
	}

	public static void serverRegistries(FMLServerStartingEvent event) {
		event.registerServerCommand(new CommandsMoonphase());
		event.registerServerCommand(new CommandsTransformation());
		event.registerServerCommand(new CommandsTransformationTexture());
	}
}
