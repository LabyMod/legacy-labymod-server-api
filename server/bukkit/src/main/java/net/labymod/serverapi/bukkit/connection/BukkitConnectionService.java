package net.labymod.serverapi.bukkit.connection;

import com.comphenix.protocol.ProtocolLibrary;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import io.netty.channel.Channel;
import java.util.List;
import java.util.UUID;
import net.labymod.serverapi.api.connection.ConnectionService;
import net.labymod.serverapi.api.extension.AddonExtension;
import net.labymod.serverapi.api.extension.ModificationExtension;
import net.labymod.serverapi.api.extension.PackageExtension;
import net.labymod.serverapi.api.permission.PermissionService;
import net.labymod.serverapi.api.player.LabyModPlayer;
import net.labymod.serverapi.api.player.LabyModPlayer.Factory;
import net.labymod.serverapi.api.player.LabyModPlayerService;
import net.labymod.serverapi.api.protocol.ChunkCachingProtocol;
import net.labymod.serverapi.api.protocol.ShadowProtocol;
import net.labymod.serverapi.api.protocol.chunkcaching.ChunkCaching;
import net.labymod.serverapi.api.protocol.chunkcaching.LabyModPlayerChunkCaching;
import net.labymod.serverapi.bukkit.event.BukkitLabyModPlayerLoginEvent;
import net.labymod.serverapi.bukkit.protocol.chunkcaching.ChunkCacheNewerHandle;
import net.labymod.serverapi.bukkit.util.NetworkHelper;
import net.labymod.serverapi.common.guice.LabyModInjector;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@Singleton
public class BukkitConnectionService implements ConnectionService<Player>, Listener {

  private final ChunkCaching<Player> chunkCaching;
  private final PermissionService permissionService;
  private final LabyModPlayer.Factory<Player> labyModPlayerFactory;
  private final LabyModPlayerService<Player> labyModPlayerService;

  @Inject
  private BukkitConnectionService(
      ChunkCaching<Player> chunkCaching,
      PermissionService permissionService,
      Factory<Player> labyModPlayerFactory,
      LabyModPlayerService<Player> labyModPlayerService) {
    this.chunkCaching = chunkCaching;
    this.permissionService = permissionService;
    this.labyModPlayerFactory = labyModPlayerFactory;
    this.labyModPlayerService = labyModPlayerService;
  }

  @EventHandler
  public void login(BukkitLabyModPlayerLoginEvent event) {
    Player player = event.getPlayer();
    this.login(
        player,
        player.getName(),
        player.getUniqueId(),
        event.getVersion(),
        event.getChunkCachingProtocol(),
        event.getShadowProtocol(),
        event.getAddonExtensions(),
        event.getModificationExtensions());
  }

  /** {@inheritDoc} */
  @Override
  public void login(
      Player player,
      String username,
      UUID uniqueId,
      String version,
      ChunkCachingProtocol chunkCachingProtocol,
      ShadowProtocol shadowProtocol,
      List<AddonExtension> addons,
      List<ModificationExtension> modifications,
      List<PackageExtension> packages) {
    LabyModPlayer<Player> labyModPlayer =
        this.labyModPlayerFactory.create(
            player,
            username,
            uniqueId,
            version,
            chunkCachingProtocol,
            shadowProtocol,
            addons,
            modifications);

    if (chunkCachingProtocol.isEnabled() && chunkCachingProtocol.getVersion() >= 2) {
      LabyModPlayerChunkCaching<Player> labyModPlayerChunkCaching =
          LabyModInjector.getInstance()
              .getInjectedInstance(new TypeLiteral<LabyModPlayerChunkCaching<Player>>() {});
      if (this.chunkCaching.getCache().putIfAbsent(uniqueId, labyModPlayerChunkCaching) != null) {
        return;
      }

      int protocolVersion =
          ProtocolLibrary.getProtocolManager().getProtocolVersion(player.getPlayer());

      if (335 >= protocolVersion) {
        Channel channel = NetworkHelper.getInstance().getChannel(player.getPlayer());
        channel
            .pipeline()
            .addAfter(
                "compress",
                "laby_chunks",
                new ChunkCacheNewerHandle(player.getPlayer(), labyModPlayerChunkCaching));
        System.out.println("Enabling chunk caching for >= 1.12.x");
      }
    }

    this.labyModPlayerService.registerPlayer(labyModPlayer);
    this.permissionService.sendPermissions(uniqueId);
  }

  /** {@inheritDoc} */
  @Override
  public void disconnect(UUID uniqueId) {
    this.labyModPlayerService.unregisterPlayerIf(player -> player.getUniqueId().equals(uniqueId));
  }
}
