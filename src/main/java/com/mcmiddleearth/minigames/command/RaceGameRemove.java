/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mcmiddleearth.minigames.command;

import com.mcmiddleearth.minigames.data.PluginData;
import com.mcmiddleearth.minigames.game.AbstractGame;
import com.mcmiddleearth.minigames.game.GameType;
import com.mcmiddleearth.minigames.game.RaceGame;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Eriol_Eandur
 */
public class RaceGameRemove extends AbstractGameCommand{
    
    public RaceGameRemove(String... permissionNodes) {
        super(0, true, permissionNodes);
        cmdGroup = CmdGroup.RACE;
        setShortDescription(": Removes a race checkpoint.");
        setUsageDescription(" [checkpointID]: Removes the race checkpoint with [checkpointID]. Without argument attempts to remove a nearby (10 blocks) checkpoint.");
    }
    
    @Override
    protected void execute(CommandSender cs, String... args) {
        AbstractGame game = getGame((Player) cs);
        if(game != null && isManager((Player) cs, game)
                        && isCorrectGameType((Player) cs, game, GameType.RACE)) {
            if(game.isAnnounced()) {
                sendAlreadyAnnouncedErrorMessage(cs);
                return;
            }
            RaceGame raceGame = (RaceGame) game;
            Location loc = ((Player) cs).getLocation();
            if(args.length==0) {
                if(raceGame.getCheckpointManager().removeCheckpointLocation(loc)) {
                    sendRemovedMessage(cs);
                    return;
                }
                sendNotNearMessage(cs);
            } 
            else {
                try {
                    int id = Integer.parseInt(args[0]);
                    if(raceGame.getCheckpointManager().removeCheckpointLocation(id)) {
                        sendRemovedMessage(cs);
                        return;
                    }
                    sendIdNotValidException(cs);
                }
                catch(NumberFormatException e){
                    sendNotANumberException(cs);
                }
            }
        }
    }

    private void sendRemovedMessage(CommandSender cs) {
        PluginData.getMessageUtil().sendInfoMessage(cs, "Checkpoint removed.");
    }

    private void sendNotNearMessage(CommandSender cs) {
        PluginData.getMessageUtil().sendErrorMessage(cs, "No Checkpoint found near you.");
    }

    private void sendIdNotValidException(CommandSender cs) {
        PluginData.getMessageUtil().sendErrorMessage(cs, "No Checkpoint with that ID.");
    }

    private void sendNotANumberException(CommandSender cs) {
        PluginData.getMessageUtil().sendErrorMessage(cs, "Not a number.");
    }

}
