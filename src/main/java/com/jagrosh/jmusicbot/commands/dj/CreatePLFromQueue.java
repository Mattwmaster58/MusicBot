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
import com.jagrosh.jmusicbot.playlist.PlaylistLoader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;


public class CreatePLFromQueue extends DJCommand
{
    public CreatePLFromQueue(Bot bot)
    {
        super(bot);
        this.name = "createfromqueue";
        this.help = "creates a playlist based on the current items in the queue";
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

        if(!bot.getPlaylistLoader().folderExists())
            bot.getPlaylistLoader().createFolder();
        if(!bot.getPlaylistLoader().folderExists())
        {
            event.replyWarning("Playlists folder does not exist around could not be created!");
            return;
        }
        PlaylistLoader.Playlist playlist = bot.getPlaylistLoader().getPlaylist(event.getArgs());
        HashSet<String> currentEntries = new HashSet<String>();
        StringBuilder builder = new StringBuilder();
        if (playlist != null) {
            currentEntries.addAll(playlist.getItems());
            playlist.getItems().forEach(item -> builder.append("\r\n").append(item));
        }

        int skipped = 0;
        for(String item: uris) {
            if (currentEntries.contains(item)) {skipped++;} else {builder.append("\r\n").append(item);}
        }
        try
        {
            bot.getPlaylistLoader().writePlaylist(event.getArgs(), builder.toString());
        }
        catch (IOException e)
        {
            event.replyError("Unable to write new items to playlist:"+e.getLocalizedMessage());
            return;
        }
        event.replySuccess("Added " + (uris.size() - skipped) + " songs to playlist \"" + event.getArgs() + "\" from the queue (skipped " + skipped + " duplicates)");
    }
}
