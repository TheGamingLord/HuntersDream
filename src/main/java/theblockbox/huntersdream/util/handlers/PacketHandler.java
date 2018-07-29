package theblockbox.huntersdream.util.handlers;

// using static import for CLIENT and SERVER enum constants
import static net.minecraftforge.fml.relauncher.Side.CLIENT;
import static net.minecraftforge.fml.relauncher.Side.SERVER;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import theblockbox.huntersdream.network.MessageBase;
import theblockbox.huntersdream.network.TransformationMessage;
import theblockbox.huntersdream.network.TransformationTextureIndexMessage;
import theblockbox.huntersdream.network.TransformationWerewolfNightOverMessage;
import theblockbox.huntersdream.network.TransformationWerewolfNoControlMessage;
import theblockbox.huntersdream.network.TransformationXPMessage;
import theblockbox.huntersdream.util.ExecutionPath;
import theblockbox.huntersdream.util.Reference;
import theblockbox.huntersdream.util.exceptions.WrongSideException;
import theblockbox.huntersdream.util.helpers.TransformationHelper;
import theblockbox.huntersdream.util.interfaces.ITransformationPlayer;

public class PacketHandler {
	public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(Reference.MODID);
	public static int networkID = 0;

	public static void register() {
		for (Packets packet : Packets.values()) {
			INSTANCE.registerMessage(packet.getMessageClass(), packet.CLASS, networkID++, packet.SIDE);
		}
	}

	public enum Packets {
		TRANSFORMATION(new TransformationMessage()), NIGHT_OVER(
				new TransformationWerewolfNightOverMessage()), NO_CONTROL(
						new TransformationWerewolfNoControlMessage()), XP(new TransformationXPMessage()), TEXTURE_INDEX(
								new TransformationTextureIndexMessage(), SERVER);

		private final MessageBase<?> MESSAGE_BASE;
		public final Class<? extends IMessage> CLASS;
		/** Side that receives package */
		public final Side SIDE;
		/** The result of {@link MessageBase#getName()} */
		public final String NAME;

		private Packets(MessageBase<?> messageBase) {
			this(messageBase, CLIENT);
		}

		// I have literally no idea what this does but it works
		@SuppressWarnings("unchecked")
		public <T> Class<T> getMessageClass() {
			return (Class<T>) this.CLASS;
		}

		// Remember: Don't trust the client
		private Packets(MessageBase<?> messageBase, Side side) {
			this.MESSAGE_BASE = messageBase;
			this.SIDE = side;
			this.CLASS = messageBase.getClass();
			this.NAME = messageBase.getName();
		}

		public void sync(ExecutionPath path, EntityPlayer player, Object... args) {
			ITransformationPlayer cap = TransformationHelper.getCap(player);
			int playerID = player.getEntityId();
			EntityPlayerMP playerMP = null;
			if (player instanceof EntityPlayerMP) {
				playerMP = (EntityPlayerMP) player;
			}
			// you can get the sp player through Minecraft.getMinecraft().player;

			if ((this.SIDE == SERVER && player.world.isRemote)
					|| (this.SIDE == Side.CLIENT && (!player.world.isRemote))) {

				switch (this) {
				// Server
				case TRANSFORMATION:
					// could contain render changes
					INSTANCE.sendToAll(new TransformationMessage(cap.getXP(), cap.transformed(),
							cap.getTransformationInt(), playerID, cap.getTextureIndex()));
					break;
				case XP:
					INSTANCE.sendToAll(new TransformationXPMessage(cap.getXP(), playerID));
					break;
				case NIGHT_OVER:
					// only changes player view
					INSTANCE.sendTo(new TransformationWerewolfNightOverMessage(playerID), playerMP);
					break;
				case NO_CONTROL:
					// only changes player view
					INSTANCE.sendTo(
							new TransformationWerewolfNoControlMessage(playerID, Integer.parseInt(args[0].toString())),
							playerMP);
					break;

				// Client
				case TEXTURE_INDEX:
					// only cosmetic changes, so you can trust the client
					INSTANCE.sendToServer(new TransformationTextureIndexMessage(cap.getTextureIndex()));
					break;

				default:
					throw new IllegalArgumentException("Illegal arguments: Couldn't find packet " + this.toString()
							+ "\nAdditional info:\nWrong side? "
							+ (player.world.isRemote ? (this.SIDE == SERVER) : (this.SIDE == CLIENT)) + "\nPlayer: "
							+ player.getName() + "\nAdditional argument length: " + args.length + "\nPath: "
							+ path.get());
				}

				if (ConfigHandler.showPacketMessages)
					System.out.println(this.MESSAGE_BASE.getName() + " packet sent on side "
							+ (player.world.isRemote ? CLIENT : SERVER).toString() + "\nPath: " + path.get());
			} else {
				throw new WrongSideException("Packet " + this.NAME + " couldn't be sent\nPath: " + path.get(),
						(this.SIDE == SERVER ? CLIENT : SERVER));
			}
		}
	}
}
