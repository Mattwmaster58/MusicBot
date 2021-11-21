/*
 * Copyright 2019 John Grosh <john.a.grosh@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jagrosh.jmusicbot.commands.dj;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.commands.DJCommand;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


public class CreatePLFromQueue extends DJCommand
{
    public CreatePLFromQueue(Bot bot)
    {
        super(bot);
        this.name = "createfromqueue";
        this.help = "removes all entries by a user from the queue";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.beListening = false;
        this.bePlaying = true;
    }

    @Override
    public void doCommand(CommandEvent event) {
        if (event.getArgs().isEmpty()) {
            event.replyError("You need to specify a playlist name!");
            return;
        }
        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        List<String> uris = new java.util.ArrayList<>(Collections.singletonList(handler.getPlayer().getPlayingTrack().getInfo().uri));
        uris.addAll(handler.getQueue().getList().stream().map(queuedTrack -> queuedTrack.getTrack().getInfo().uri).collect(Collectors.toList()));
        File directory = new File("Playlists");
        if (! directory.exists()){
            if (!directory.mkdir())
            {
                event.replyError("Failed to create Playlists directory");
            }
        }
        FileWriter writer = null;
        boolean writeError = false;
        try
        {
            writer = new FileWriter("./Playlists/" + event.getArgs() + ".txt");
        }
        catch (IOException e)
        {
            event.replyError(e.getMessage());
            return;
        }
        try
        {
            for(String str: uris)
            {
                writer.write(str + System.lineSeparator());
            }
        } catch (IOException g) {
            writeError = true;
            event.replyError(g.getMessage());
        }
        try
        {
            writer.close();
        }
        catch (IOException g)
        {
            writeError = true;
            event.replyError(g.getMessage());
        }
        if (!writeError)
        {
            event.replySuccess("Added " + uris.size() + " songs to playlist " + event.getArgs());
        }
    }
}
