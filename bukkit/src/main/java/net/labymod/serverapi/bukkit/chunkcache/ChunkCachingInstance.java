package net.labymod.serverapi.bukkit.chunkcache;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.netty.channel.Channel;
import net.jpountz.xxhash.XXHashFactory;
import net.labymod.serverapi.bukkit.LabyModPlugin;
import net.labymod.serverapi.bukkit.chunkcache.handle.Chunk12Handle;
import net.labymod.serverapi.bukkit.chunkcache.handle.Chunk8Handle;
import net.labymod.serverapi.bukkit.chunkcache.handle.ChunkHandle;
import net.labymod.serverapi.bukkit.event.MessageReceiveEvent;
import net.labymod.serverapi.bukkit.utils.ViaUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class ChunkCachingInstance implements Listener {

    public static final XXHashFactory hashFactory = XXHashFactory.fastestInstance();
    public static final String PM_CHANNEL = "CCP";

    private static boolean IS_VIA = false;
    private static boolean IS_12 = false;

    private final Map<UUID, PlayerState> data = new ConcurrentHashMap<>();
    private ProtocolManager proto;

    public ChunkCachingInstance() {
        final ChunkHandle handle;
        PacketType[] types;
        IS_12 = Bukkit.getServer().getBukkitVersion().startsWith( "v1_12_R1" );
        IS_VIA = Bukkit.getPluginManager().isPluginEnabled( "ViaVersion" );
        if ( LabyModPlugin.getInstance().getPacketUtils().getVersion().equalsIgnoreCase( "v1_8_R3" ) ) {
            // 1.8.8
            handle = new Chunk8Handle();
            types = new PacketType[]{PacketType.Play.Server.MAP_CHUNK, PacketType.Play.Server.MAP_CHUNK_BULK, PacketType.Play.Server.UPDATE_SIGN};
        } else if ( IS_12 || IS_VIA ) {
            handle = null;
            types = null;
        } else {
            debug( "Unsupported LabyHash Server version (use 1.8.8 or 1.12.x)" );
            return;
        }

        Bukkit.getScheduler().runTaskTimerAsynchronously( LabyModPlugin.getInstance(), () -> {
            long millis = System.currentTimeMillis() - TimeUnit.SECONDS.toMillis( 10 );
            data.forEach( ( uuid, state ) -> state.clearOlder( millis ) );
        }, 100, 100 );

        if ( handle != null ) {
            proto = ProtocolLibrary.getProtocolManager();
            proto.addPacketListener( new PacketAdapter( LabyModPlugin.getInstance(), ListenerPriority.HIGH, types ) {
                @Override
                public void onPacketSending( PacketEvent event ) {
                    if ( IS_12 ) {
                        return;
                    }
                    if ( IS_VIA ) {
                        int ver = ViaUtils.getVersion( event.getPlayer().getUniqueId() );
                        if ( 335 <= ver && ver <= 340 ) {
                            return;
                        }
                    }

                    PlayerState data = ChunkCachingInstance.this.data.get( event.getPlayer().getUniqueId() );
                    if ( data == null ) {
                        return;
                    }

                    if ( handle.handle( event.getPlayer(), event.getPacketType(), data, event.getPacket() ) ) {
                        event.setCancelled( true );
                    }
                }
            } );
        }


        Bukkit.getPluginManager().registerEvents( this, LabyModPlugin.getInstance() );
        Bukkit.getMessenger().registerOutgoingPluginChannel( LabyModPlugin.getInstance(), PM_CHANNEL );
        Bukkit.getMessenger().registerIncomingPluginChannel( LabyModPlugin.getInstance(), PM_CHANNEL, ( pmchannel, player, bytes ) -> {
            ByteBuffer buffer = ByteBuffer.wrap( bytes );
            byte opcode = buffer.get();
            if ( opcode == 0x21 ) {
                PlayerState state = data.get( player.getUniqueId() );
                if ( state == null ) {
                    return;
                }
                // This is a client requesting chunks!
                short length = buffer.getShort();
                boolean[] mask = new boolean[length];
                int[][] send = new int[length][2];
                for ( int i = 0; i < send.length; i++ ) {
                    mask[i] = buffer.get() == 1;
                    send[i][0] = buffer.getInt();
                    send[i][1] = buffer.getInt();
                }
                state.handleRequest( proto, player, mask, send );
            }
        } );
    }

    @EventHandler
    public void onMessageReceivedEvent( MessageReceiveEvent event ) {
        if ( event.getMessageKey().equals( "INFO" ) && event.getJsonElement() instanceof JsonObject ) {
            JsonObject jsonElement = (JsonObject) event.getJsonElement();
            JsonElement ccp = jsonElement.get( "ccp" );
            if ( ccp != null && ccp.isJsonObject() ) {
                JsonObject obj = ccp.getAsJsonObject();
                JsonElement enabled = obj.get( "enabled" );
                if ( enabled != null && enabled.isJsonPrimitive() && enabled.getAsBoolean() ) {
                    JsonElement version = obj.get( "version" );
                    if ( version != null && version.isJsonPrimitive() && version.getAsInt() >= 2 ) {
                        Player player = event.getPlayer();
                        PlayerState playerState = new PlayerState();
                        data.putIfAbsent( player.getUniqueId(), playerState );

                        boolean v112 = IS_12;
                        if ( IS_VIA && !v112 ) {
                            int ver = ViaUtils.getVersion( player.getUniqueId() );
                            if ( 335 <= ver && ver <= 340 ) {
                                v112 = true;
                            }
                        }
                        if ( v112 ) {
                            Channel channel = LabyModPlugin.getInstance().getPacketUtils().getChannel( player );
                            channel.pipeline().addAfter( "compress", "laby_chunks", new Chunk12Handle( player, playerState ) );
                            log( "Enabling 1.12 player %s", player.getName() );
                        } else {
                            log( "Enabling 1.8.9 player %s", player.getName() );
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onChangeWorld( PlayerChangedWorldEvent event ) {
        PlayerState state = data.get( event.getPlayer().getUniqueId() );
        if ( state != null ) {
            state.clear();
        }
    }

    @EventHandler
    public void onQuit( PlayerQuitEvent event ) {
        data.remove( event.getPlayer().getUniqueId() );
    }

    public static void log( String msg, Object... args ) {
        LabyModPlugin.getInstance().getLogger().log( Level.INFO, "[ccp] " + String.format( msg, args ) );
    }

    public static void debug( String msg, Object... args ) {
        // LabyModPlugin.getInstance().getLogger().log( Level.INFO, "[CHUNKZ] " + String.format( msg, args ) );
    }
}