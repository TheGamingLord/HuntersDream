package theblockbox.huntersdream.api.event;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

/**
 * WerewolfTransformingEvent is fired when an entity transforms (back). The
 * entity's transformation is always
 * {@link theblockbox.huntersdream.api.Transformation#NONE}. <br>
 * <br>
 * This event is {@link Cancelable}.<br>
 * If this event is canceled, the entity won't transform (back). <br>
 * <br>
 * This event does not have a result.
 * {@link net.minecraftforge.fml.common.eventhandler.Event.HasResult}<br>
 * <br>
 * This event is fired on the {@link MinecraftForge#EVENT_BUS}.
 **/
@Cancelable
public class WerewolfTransformingEvent extends LivingEvent {
	private final boolean transformingBack;
	private final WerewolfTransformingEvent.WerewolfTransformingReason reason;

	public WerewolfTransformingEvent(EntityLivingBase entity, boolean transformingBack,
			WerewolfTransformingEvent.WerewolfTransformingReason reason) {
		super(entity);
		this.transformingBack = transformingBack;
		this.reason = reason;
		if (entity instanceof EntityPlayer) {
			this.setCanceled(MinecraftForge.EVENT_BUS
					.post(new WerewolfTransformingEvent.PlayerWerewolfTransformingEvent((EntityPlayer) entity, transformingBack, reason)));
		}
	}

	/**
	 * Returns the reason why the werewolf transformed.
	 */
	public WerewolfTransformingEvent.WerewolfTransformingReason getTransformingEventReason() {
		return this.reason;
	}

	/**
	 * Returns true if the werewolf is transforming back into its normal form, false
	 * if the entity is transforming into its werewolf form.
	 */
	public boolean transformingBack() {
		return this.transformingBack;
	}

	/** The reason why the werewolf transformed */
	public static class WerewolfTransformingReason {
		/** Caused by full moon */
		public static final WerewolfTransformingEvent.WerewolfTransformingReason FULL_MOON = new WerewolfTransformingEvent.WerewolfTransformingReason();
		/** Caused by full moon ending */
		public static final WerewolfTransformingEvent.WerewolfTransformingReason FULL_MOON_END = new WerewolfTransformingEvent.WerewolfTransformingReason();
	}

	/** This event is the player version of {@link WerewolfTransformingEvent} */
	@Cancelable
	public static class PlayerWerewolfTransformingEvent extends PlayerEvent {
		private final boolean isTransformingBack;
		private final WerewolfTransformingEvent.WerewolfTransformingReason transformationReason;

		public PlayerWerewolfTransformingEvent(EntityPlayer player, boolean transformingBack,
				WerewolfTransformingEvent.WerewolfTransformingReason reason) {
			super(player);
			this.isTransformingBack = transformingBack;
			this.transformationReason = reason;
		}

		/**
		 * Returns the reason why the werewolf transformed.
		 */
		public WerewolfTransformingEvent.WerewolfTransformingReason getTransformingEventReason() {
			return this.transformationReason;
		}

		/**
		 * Returns true if the werewolf is transforming back into its normal form, false
		 * if the entity is transforming into its werewolf form.
		 */
		public boolean transformingBack() {
			return this.isTransformingBack;
		}
	}
}
