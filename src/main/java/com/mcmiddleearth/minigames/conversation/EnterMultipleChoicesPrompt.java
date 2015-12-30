/*
 * Copyright (C) 2015 MCME
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mcmiddleearth.minigames.conversation;

import com.mcmiddleearth.minigames.quizQuestion.ChoiceQuestion;
import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import static org.bukkit.conversations.Prompt.END_OF_CONVERSATION;
import org.bukkit.conversations.ValidatingPrompt;

/**
 *
 * @author Eriol_Eandur
 */
class EnterMultipleChoicesPrompt extends ValidatingPrompt {

    @Override
    public String getPromptText(ConversationContext cc) {
        cc.setSessionData("input", true);
        return "[Hint] Type in the letters of the correct answers.";
    }

    @Override
    protected Prompt acceptValidatedInput(ConversationContext cc, String string) {
        cc.setSessionData("answer", string);
        return END_OF_CONVERSATION;
    }
    
    @Override
    protected String getFailedValidationText(ConversationContext context, String invalidInput) {
        return ChatColor.RED+"[Invalid input] Type in chat the letters of the correct answer. For example type: 'ACD'";
    }
    
    @Override
    protected boolean isInputValid(ConversationContext cc, String string) {
       return ChoiceQuestion.isAnswerValid(string);
    }

    
}