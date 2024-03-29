/*
 * Copyright 2015 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.internal.entities;

import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.channel.concrete.*;
import net.dv8tion.jda.internal.entities.channel.mixin.attribute.IInteractionPermissionMixin;
import net.dv8tion.jda.internal.interactions.ChannelInteractionPermissions;
import net.dv8tion.jda.internal.utils.JDALogger;
import org.slf4j.Logger;

import javax.annotation.Nonnull;

public class InteractionEntityBuilder extends AbstractEntityBuilder
{
    private static final Logger LOG = JDALogger.getLog(InteractionEntityBuilder.class);

    private final long interactionChannelId;
    private final long interactionUserId;

    public InteractionEntityBuilder(JDAImpl api, long interactionChannelId, long interactionUserId)
    {
        super(api);
        this.interactionChannelId = interactionChannelId;
        this.interactionUserId = interactionUserId;
    }

    public GuildChannel createGuildChannel(PartialGuildImpl guildObj, DataObject channelData)
    {
        final ChannelType channelType = ChannelType.fromId(channelData.getInt("type"));
        switch (channelType)
        {
        case TEXT:
            return createTextChannel(guildObj, channelData);
        case NEWS:
            return createNewsChannel(guildObj, channelData);
        case STAGE:
            return createStageChannel(guildObj, channelData);
        case VOICE:
            return createVoiceChannel(guildObj, channelData);
        case CATEGORY:
            return createCategory(guildObj, channelData);
        case FORUM:
            return createForumChannel(guildObj, channelData);
        case MEDIA:
            return createMediaChannel(guildObj, channelData);
        default:
            LOG.debug("Cannot create channel for type " + channelData.getInt("type"));
            return null;
        }
    }

    public CategoryImpl createCategory(PartialGuildImpl guild, DataObject json)
    {
        final long id = json.getLong("id");
        final CategoryImpl channel = new CategoryImpl(id, guild);
        configureCategory(json, channel);
        configureChannelInteractionPermissions(channel, json);
        return channel;
    }

    public TextChannelImpl createTextChannel(PartialGuildImpl guildObj, DataObject json)
    {
        final long id = json.getLong("id");
        TextChannelImpl channel = new TextChannelImpl(id, guildObj);
        configureTextChannel(json, channel);
        configureChannelInteractionPermissions(channel, json);
        return channel;
    }

    public NewsChannelImpl createNewsChannel(@Nonnull PartialGuildImpl guildObj, DataObject json)
    {
        final long id = json.getLong("id");
        NewsChannelImpl channel = new NewsChannelImpl(id, guildObj);
        configureNewsChannel(json, channel);
        configureChannelInteractionPermissions(channel, json);
        return channel;
    }

    public VoiceChannelImpl createVoiceChannel(@Nonnull PartialGuildImpl guild, DataObject json)
    {
        final long id = json.getLong("id");
        VoiceChannelImpl channel = new VoiceChannelImpl(id, guild);
        configureVoiceChannel(json, channel);
        configureChannelInteractionPermissions(channel, json);
        return channel;
    }

    public StageChannelImpl createStageChannel(@Nonnull PartialGuildImpl guild, DataObject json)
    {
        final long id = json.getLong("id");
        final StageChannelImpl channel = new StageChannelImpl(id, guild);
        configureStageChannel(json, channel);
        configureChannelInteractionPermissions(channel, json);
        return channel;
    }

    public MediaChannelImpl createMediaChannel(PartialGuildImpl guild, DataObject json)
    {
        final long id = json.getLong("id");
        final MediaChannelImpl channel = new MediaChannelImpl(id, guild);
        configureMediaChannel(json, channel);
        configureChannelInteractionPermissions(channel, json);
        return channel;
    }

    public ThreadChannelImpl createThreadChannel(PartialGuildImpl guild, DataObject json)
    {
        final long id = json.getUnsignedLong("id");
        final ChannelType type = ChannelType.fromId(json.getInt("type"));
        ThreadChannelImpl channel = new ThreadChannelImpl(id, guild, type);
        configureThreadChannel(json, channel);
        configureChannelInteractionPermissions(channel, json);
        return channel;
    }

    public ForumChannelImpl createForumChannel(PartialGuildImpl guild, DataObject json)
    {
        final long id = json.getLong("id");
        final ForumChannelImpl channel = new ForumChannelImpl(id, guild);
        configureForumChannel(json, channel);
        configureChannelInteractionPermissions(channel, json);
        return channel;
    }

    private void configureChannelInteractionPermissions(IInteractionPermissionMixin<?> channel, DataObject json)
    {
        if (!channel.hasGuild())
            channel.setInteractionPermissions(new ChannelInteractionPermissions(interactionUserId, json.getLong("permissions")));
    }
}
