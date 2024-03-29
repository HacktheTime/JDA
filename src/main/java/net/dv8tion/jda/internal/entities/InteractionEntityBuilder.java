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

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.PartialGuild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.*;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.channel.concrete.*;
import net.dv8tion.jda.internal.entities.channel.mixin.attribute.IInteractionPermissionMixin;
import net.dv8tion.jda.internal.interactions.ChannelInteractionPermissions;
import net.dv8tion.jda.internal.interactions.MemberInteractionPermissions;
import net.dv8tion.jda.internal.utils.JDALogger;
import org.slf4j.Logger;

import javax.annotation.Nonnull;

public class InteractionEntityBuilder extends AbstractEntityBuilder
{
    private static final Logger LOG = JDALogger.getLog(InteractionEntityBuilder.class);

    private final EntityBuilder entityBuilder = api.getEntityBuilder();
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

    public Category createCategory(PartialGuildImpl guild, DataObject json)
    {
        if (guild.isGuild())
            return entityBuilder.createCategory(json, guild.getIdLong());

        final long id = json.getLong("id");
        final CategoryImpl channel = new CategoryImpl(id, guild);
        configureCategory(json, channel);
        configureChannelInteractionPermissions(channel, json);
        return channel;
    }

    public TextChannel createTextChannel(PartialGuildImpl guild, DataObject json)
    {
        if (guild.isGuild())
            return entityBuilder.createTextChannel(json, guild.getIdLong());

        final long id = json.getLong("id");
        TextChannelImpl channel = new TextChannelImpl(id, guild);
        configureTextChannel(json, channel);
        configureChannelInteractionPermissions(channel, json);
        return channel;
    }

    public NewsChannel createNewsChannel(@Nonnull PartialGuildImpl guild, DataObject json)
    {
        if (guild.isGuild())
            return entityBuilder.createNewsChannel(json, guild.getIdLong());

        final long id = json.getLong("id");
        NewsChannelImpl channel = new NewsChannelImpl(id, guild);
        configureNewsChannel(json, channel);
        configureChannelInteractionPermissions(channel, json);
        return channel;
    }

    public VoiceChannel createVoiceChannel(@Nonnull PartialGuildImpl guild, DataObject json)
    {
        if (guild.isGuild())
            return entityBuilder.createVoiceChannel(json, guild.getIdLong());

        final long id = json.getLong("id");
        VoiceChannelImpl channel = new VoiceChannelImpl(id, guild);
        configureVoiceChannel(json, channel);
        configureChannelInteractionPermissions(channel, json);
        return channel;
    }

    public StageChannel createStageChannel(@Nonnull PartialGuildImpl guild, DataObject json)
    {
        if (guild.isGuild())
            return entityBuilder.createStageChannel(json, guild.getIdLong());

        final long id = json.getLong("id");
        final StageChannelImpl channel = new StageChannelImpl(id, guild);
        configureStageChannel(json, channel);
        configureChannelInteractionPermissions(channel, json);
        return channel;
    }

    public MediaChannel createMediaChannel(PartialGuildImpl guild, DataObject json)
    {
        if (guild.isGuild())
            return entityBuilder.createMediaChannel(json, guild.getIdLong());

        final long id = json.getLong("id");
        final MediaChannelImpl channel = new MediaChannelImpl(id, guild);
        configureMediaChannel(json, channel);
        configureChannelInteractionPermissions(channel, json);
        return channel;
    }

    public ThreadChannel createThreadChannel(PartialGuildImpl guild, DataObject json)
    {
        if (guild.isGuild())
            return entityBuilder.createThreadChannel(json, guild.getIdLong());

        final long id = json.getUnsignedLong("id");
        final ChannelType type = ChannelType.fromId(json.getInt("type"));
        ThreadChannelImpl channel = new ThreadChannelImpl(id, guild, type);
        configureThreadChannel(json, channel);
        configureChannelInteractionPermissions(channel, json);
        return channel;
    }

    public ForumChannel createForumChannel(PartialGuildImpl guild, DataObject json)
    {
        if (guild.isGuild())
            return entityBuilder.createForumChannel(json, guild.getIdLong());

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

    public Member createMember(PartialGuildImpl guild, DataObject memberJson)
    {
        if (guild.isGuild())
            return entityBuilder.createMember((GuildImpl) guild, memberJson);

        User user = entityBuilder.createUser(memberJson.getObject("user"));
        MemberImpl member = new MemberImpl(guild, user);
        configureMember(memberJson, member);

        // Absent outside interactions and in message mentions
        if (memberJson.hasKey("permissions"))
            member.setInteractionPermissions(new MemberInteractionPermissions(interactionChannelId, memberJson.getLong("permissions")));

        return member;
    }

    public Role createRole(PartialGuild guild, DataObject roleJson)
    {
        if (guild.isGuild())
            return entityBuilder.createRole((GuildImpl) guild, roleJson, guild.getIdLong());

        final long id = roleJson.getLong("id");
        RoleImpl role = new RoleImpl(id, guild);
        configureRole(roleJson, role, id);
        return role;
    }
}
