/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mcmiddleearth.minigames.command;

import com.mcmiddleearth.minigames.data.PluginData;
import com.mcmiddleearth.minigames.game.AbstractGame;
import com.mcmiddleearth.minigames.game.GameType;
import com.mcmiddleearth.minigames.game.QuizGame;
import com.mcmiddleearth.minigames.quizQuestion.QuestionType;
import com.mcmiddleearth.minigames.utils.MessageUtil;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Eriol_Eandur
 */
public class QuizGameQuestion extends AbstractGameCommand{
    
    public QuizGameQuestion(String... permissionNodes) {
        super(0, true, permissionNodes);
        setShortDescription(": ");
        setUsageDescription(": ");
    }
    
    @Override
    protected void execute(CommandSender cs, String... args) {
        AbstractGame game = getGame((Player) cs);
        if(game != null && isManager((Player) cs, game) 
                        && isCorrectGameType((Player) cs, game, GameType.LORE_QUIZ)) {
            QuizGame quizGame = (QuizGame) game;
            QuestionType type = null;
            if(args.length>0) {
                type = QuestionType.getQuestionType(args[0]);
                if(type==null) {
                    sendInvalidQuestionType(cs);
                    return;
                }
            }
            if(type == null) {
                type = QuestionType.SINGLE;
            }
            PluginData.getCreateQuestionFactory().start((Player)cs, quizGame, type);
        }
    }
 
    private void sendInvalidQuestionType(CommandSender cs) {
        MessageUtil.sendErrorMessage(cs, "You specified an invalid question type. Valid question types are: /game question [free|number|mulit|single]");
    }
    
 }
