/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mcmiddleearth.minigames.game;

import com.mcmiddleearth.minigames.MiniGamesPlugin;
import com.mcmiddleearth.minigames.data.PluginData;
import com.mcmiddleearth.minigames.scoreboard.GameScoreboard;
import com.mcmiddleearth.pluginutils.PlayerUtil;
import com.mcmiddleearth.minigames.utils.MinigamesMessageUtil;
import com.mcmiddleearth.pluginutils.message.FancyMessage;
import com.mcmiddleearth.pluginutils.message.MessageType;
import com.mcmiddleearth.pluginutils.message.MessageUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 *
 * @author Eriol_Eandur
 */
public abstract class AbstractGame {
    
    protected final static TeleportCause TeleportCause_WARP = TeleportCause.NETHER_PORTAL;
   
    protected final static TeleportCause TeleportCause_FORCE = TeleportCause.END_PORTAL;
    
    @Getter
    private final String name;
    
    @Getter
    private boolean announced = false;
    
    @Getter
    private OfflinePlayer manager;

    @Getter
    private final GameType type;
    
    @Getter
    private final List<UUID> players = new ArrayList<>();
    
    private final List<UUID> bannedPlayers = new ArrayList<>();

    private final List<UUID> spectators = new ArrayList<>();
    
    private final List<UUID> invitedPlayers = new ArrayList<>();
    
    private final List<UUID> leaveMessaged = new ArrayList<>();
    
    @Getter
    private Location warp = null;
    
    @Getter
    @Setter
    private boolean warpAllowed = true;
    
    @Getter
    private boolean spectateAllowed = true;
    
    @Getter
    @Setter
    private boolean privat = false;
    
    @Getter
    private boolean flightAllowed = true;
    
    @Getter
    @Setter
    private boolean teleportAllowed = true;
    
    @Getter
    @Setter
    private boolean gm3Allowed = false;
    
    @Getter
    private final GameScoreboard board;
    
    private boolean managerOnlineLastTime = true; //for cleanup task
    
    public AbstractGame(Player manager, String name, GameType type, GameScoreboard board) {
        this.name = name;
        this.manager = manager;
        this.board = board;
        this.type = type;
        if(manager!=null) {
            warp = manager.getLocation();
            manager.setScoreboard(getBoard().getScoreboard());
            BukkitRunnable cleanupTask = new BukkitRunnable() {
                @Override
                public void run() {
                    if(!getManager().isOnline()) {
                        if(!managerOnlineLastTime) {
                            end(null);
                            cancel();
                        }
                        else {
                            managerOnlineLastTime = false;
                        }
                    }
                    else {
                        managerOnlineLastTime = true;
                    }
                }};
            cleanupTask.runTaskTimer(MiniGamesPlugin.getPluginInstance(), 3000, 3000);
        }
    }
    
    public void end(Player sender) {
        sendGameEndMessage(sender);    
        for(Player player: getOnlinePlayers()) {
                removePlayer(player); 
            }
            PluginData.removeGame(this);
    }
    
    public int countOnlinePlayer() {
        return getOnlinePlayers().size();
    }
    
    public void setManager(OfflinePlayer manager) {
        if(manager != null && PlayerUtil.getOnlinePlayer(manager)!=null) {
            Player oldManager = PlayerUtil.getOnlinePlayer(manager);
            if(!PluginData.isInGame(oldManager)) {
                oldManager.setScoreboard(Bukkit.getServer().getScoreboardManager().getMainScoreboard());
            }
        }
        this.manager = manager;
    }
    
    public OfflinePlayer getPlayer(String name) {
        Player onlinePlayer = Bukkit.getPlayer(name);
        if(onlinePlayer!=null && players.contains(onlinePlayer.getUniqueId())) {
            return onlinePlayer;
        }
        for(UUID player : players) {
            if(Bukkit.getOfflinePlayer(player).getName().equalsIgnoreCase(name)) {
                return Bukkit.getOfflinePlayer(player);
            }
        }
        return null;
    }
    
    public OfflinePlayer getBannedPlayer(String name) {
        for(UUID player : bannedPlayers) {
            if(Bukkit.getOfflinePlayer(player).getName().equalsIgnoreCase(name)) {
                return Bukkit.getOfflinePlayer(player);
            }
        }
        return null;
    }
    
    public List<Player> getOnlinePlayers() {
        List<Player> online = new ArrayList<>();
        for(UUID player : players) {
            Player onlinePlayer = Bukkit.getPlayer(player);
            if(onlinePlayer!=null) {
                online.add(onlinePlayer);
            }
        }
        return online;
    }
    
    public void addPlayer(Player player) {
        if(!flightAllowed) {
            player.setFlying(false);
        }
        if(!gm3Allowed && player.getGameMode().equals(GameMode.SPECTATOR)) {
            player.setGameMode(GameMode.SURVIVAL);
        }
        players.add(player.getUniqueId());
        getBoard().incrementPlayer();
        player.setScoreboard(board.getScoreboard());
    }
    
    public void addSpectator(Player player) {
        spectators.add(player.getUniqueId());
        player.setScoreboard(getBoard().getScoreboard());
    }
    
    public void removeSpectator(Player player) {
        if(spectators.remove(player.getUniqueId())) {
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
    }
    
    public boolean isSpectating(Player player) {
        return spectators.contains(player.getUniqueId());
    }
    
    public void removePlayer(OfflinePlayer player) {
        if(players.remove(player.getUniqueId())) {
            getBoard().decrementPlayer();
            Player onlinePlayer = PlayerUtil.getOnlinePlayer(player);
            if(onlinePlayer!=null && !PlayerUtil.isSame(onlinePlayer,manager)) {
                onlinePlayer.setScoreboard(Bukkit.getServer().getScoreboardManager().getMainScoreboard());
            }
        }
    }
    
    public boolean isBanned(OfflinePlayer player) {
        return bannedPlayers.contains(player.getUniqueId());
    }
    
    public boolean isInGame(OfflinePlayer player) {
        return players.contains(player.getUniqueId());
    }

    public void setBanned(OfflinePlayer player) {
        bannedPlayers.add(player.getUniqueId());
    }
        
    public void setUnbanned(OfflinePlayer player) {
        bannedPlayers.remove(player.getUniqueId());
    }
        
    public void playerJoinServer(PlayerJoinEvent event) {
        event.getPlayer().setScoreboard(board.getScoreboard());
        getBoard().incrementPlayer();
        if(!flightAllowed) {
            event.getPlayer().setFlying(false);
        }
    }
    
    public void playerLeaveServer(PlayerQuitEvent event) {
        event.getPlayer().setScoreboard(Bukkit.getServer().getScoreboardManager().getMainScoreboard());
        getBoard().decrementPlayer();
    }
    
    public int allowedRadius(Player player) {
        return Integer.MAX_VALUE;
    }
    
    public void playerMove(PlayerMoveEvent event) {
        Location to = event.getTo();
        if(!to.getWorld().equals(getWarp().getWorld())) {
            event.getPlayer().teleport(getWarp(), TeleportCause_FORCE);
            MessageUtil.sendErrorMessage(event.getPlayer(),"You can't go to another world while in this game.");
            return;
        }
        Location from = event.getFrom();
        if(to.distance(getWarp())>allowedRadius(event.getPlayer())) {
            //event.setCancelled(true);
            if(!leaveMessaged.contains(event.getPlayer().getUniqueId())) {
                sendLeaveNotAllowed(event.getPlayer());
                final UUID uuid = event.getPlayer().getUniqueId();
                leaveMessaged.add(uuid);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        leaveMessaged.remove(uuid);
                    }
                }.runTaskLater(MiniGamesPlugin.getPluginInstance(), 200);
            }
            Vector vel = to.toVector().subtract(event.getFrom().toVector());
            Vector radial = event.getPlayer().getLocation().toVector().subtract(getWarp().toVector());
            Vector tangential = new Vector(radial.getZ(),radial.getY(), -radial.getX());
            tangential = tangential.multiply(1/tangential.length());
            double dot = tangential.dot(vel);
            tangential = tangential.multiply(tangential.dot(vel));
            Location newTo = new Location(from.getWorld(), 
                                          from.getX()+tangential.getX(),
                                          from.getY()+tangential.getY(),
                                          from.getZ()+tangential.getZ(),
                                          to.getYaw(), to.getPitch());
            Vector radialOld = radial.clone();
            Vector radialNorm = radial.multiply(1/radial.length()).clone();
            if(newTo.distance(getWarp())>allowedRadius(event.getPlayer())) {
                radial = radial.multiply(allowedRadius(event.getPlayer()));
                radial = radial.subtract(radialNorm.multiply(0.01));
                radial = radial.subtract(radialOld);
                newTo = newTo.add(radial);
            }
            final Player play = event.getPlayer();
            if(!newTo.getBlock().isEmpty()) {
                newTo.setY(newTo.getBlockY()+1);
            }
            final Location loc = newTo.clone();
            radialNorm = radialNorm.multiply(-0.4);
            //radialNorm.setY(0);
            if(!loc.clone().add(radialNorm.clone().multiply(3)).getBlock().isEmpty()) {
                radialNorm.setY(0);
            }
            if(!loc.clone().add(radialNorm.clone().multiply(3)).getBlock().isEmpty()) {
                radialNorm = new Vector(0,0,0);
            }
            final Vector push = radialNorm;
            new BukkitRunnable() {
                @Override
                public void run() {
                    play.teleport(loc, TeleportCause_FORCE);  
                    play.setVelocity(push);
                }
            }.runTaskLater(MiniGamesPlugin.getPluginInstance(), 1);
        }
    }
    
    public void playerTeleport(PlayerTeleportEvent event) {
        if((!teleportAllowed && !event.getCause().equals(TeleportCause_FORCE))
                             && !event.getCause().equals(PlayerTeleportEvent.TeleportCause.UNKNOWN)) {
            event.setCancelled(true);
            sendTeleportNotAllowed(event.getPlayer());
        }
    }
    
    public void playerToggleFlight(PlayerToggleFlightEvent event) {
        if(!flightAllowed) {
            event.getPlayer().setFlying(false);
            event.setCancelled(true);
            sendFlightNotAllowed(event.getPlayer());
        }
    }
    
    public void playerChangeGameMode(PlayerGameModeChangeEvent event) {
        if(!gm3Allowed && event.getNewGameMode().equals(GameMode.SPECTATOR)) {
            event.setCancelled(true);
            sendGm3NotAllowed(event.getPlayer());
        }
    }
    
    public void forceTeleport(Player player, Location loc) {
        //boolean teleport = teleportAllowed;
        player.teleport(loc, TeleportCause_FORCE);
        //teleportAllowed = teleport;
    }
    
    public void warp(Player player, Location loc) {
        player.teleport(loc, TeleportCause_WARP);
    }

    
    public boolean joinAllowed() {
        return announced;
    }
    
    public void announceGame() {
        announced = true;
        if(!isPrivat()) {
            sendAnnounceGameMessage();
        }
    }
    
    public boolean isInvited(OfflinePlayer player) {
        return invitedPlayers.contains(player.getUniqueId());
    }
    
    public void invite(OfflinePlayer player) {
        invitedPlayers.add(player.getUniqueId());
    }
    
    public void setFlightAllowed(boolean allowed) {
        if(this.flightAllowed && !allowed)  {
            for(Player player : getOnlinePlayers()) {
                player.setFlying(false);
            }
        }
        flightAllowed = allowed;
    }
    
    public void setSpectateAllowed(boolean allowed) {
        if(this.spectateAllowed && !allowed)  {
            List<UUID> copyOfSpectators = new ArrayList<>();
            copyOfSpectators.addAll(spectators);
            for(UUID uuid : copyOfSpectators) {
                Player player = Bukkit.getPlayer(uuid);
                if(player!=null) {
                    removeSpectator(player);
                    sendNoMoreSpectatingMessage(player);
                }
            }
        }
        spectateAllowed = allowed;
    }
    
    public String getGameChatTag(Player player) {
        if(PluginData.isManager(player)) {
            return ChatColor.DARK_AQUA + "<Manager "; 
        }
        else {
            return ChatColor.BLUE + "<Participant "; 
        }
    }
    
    protected void sendAnnounceGameMessage() {
        MessageUtil.sendBroadcastMessage(new FancyMessage(MessageType.INFO).
                                             addClickable(MessageUtil.STRESSED+manager.getName() 
                                                              + MessageUtil.INFO+" started a new "
                                                              + MessageUtil.STRESSED+getType().toString()
                                                              + MessageUtil.INFO+" game. "
                                                              + "To play that game, "
                                                              + MessageUtil.STRESSED+"click here "
                                                              + MessageUtil.INFO+"or type in chat: /game join "+getName(),"/game join "+getName()));
        /*MessageUtil.sendBroadcastMessage("§2"+manager.getName() + "§b started a new §2"
                                                              + getType().toString()+"§b game. "
                                                              + "To play that game, click here or type in chat: /game join §2"
                                                              + getName());*/
    }
    
    public void sendGameEndMessage(Player sender) {
        MinigamesMessageUtil.sendAllInfoMessage(sender, this, "The game "+ getName()+" ended.");
    }

    private void sendLeaveNotAllowed(Player player) {
        MessageUtil.sendErrorMessage(player, "You are not allowed to leave game area.");
    }

    private void sendTeleportNotAllowed(Player player) {
        MessageUtil.sendErrorMessage(player, "You are not allowed to teleport in this game.");
    }

    private void sendFlightNotAllowed(Player player) {
        MessageUtil.sendErrorMessage(player, "You are not allowed to fly in this game.");
    }

    private void sendGm3NotAllowed(Player player) {
        MessageUtil.sendErrorMessage(player, "You are not allowed to switch to spectator mode in this game.");
    }

    private void sendNoMoreSpectatingMessage(Player player) {
        MessageUtil.sendInfoMessage(player, "Spectating in game '"+MessageUtil.STRESSED+getName()
                                           +MessageUtil.INFO+"' is no longer allowed.");
    }
}
