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

package net.dv8tion.jda.internal.interactions;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.UnknownGuild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.IntegrationOwners;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.entities.MemberImpl;
import net.dv8tion.jda.internal.entities.UnknownGuildImpl;
import net.dv8tion.jda.internal.entities.UserImpl;
import net.dv8tion.jda.internal.entities.channel.concrete.PrivateChannelImpl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;

public class InteractionImpl implements Interaction
{
    protected final long id;
    protected final long channelId;
    protected final int type;
    protected final String token;
    protected final UnknownGuild guild;
    protected final Member member;
    protected final User user;
    protected final Channel channel;
    protected final DiscordLocale userLocale;
    protected final InteractionContextType context;
    protected final IntegrationOwners integrationOwners;
    protected final Set<Permission> appPermissions;
    protected final JDAImpl api;

    //This is used to give a proper error when an interaction is ack'd twice
    // By default, discord only responds with "unknown interaction" which is horrible UX so we add a check manually here
    private boolean isAck;

    public InteractionImpl(JDAImpl jda, DataObject data)
    {
        this.api = jda;
        this.id = data.getUnsignedLong("id");
        this.token = data.getString("token");
        this.type = data.getInt("type");
        this.guild = getOrCreateUnknownGuild(data);
        this.channelId = data.getUnsignedLong("channel_id", 0L);
        this.userLocale = DiscordLocale.from(data.getString("locale", "en-US"));
        // Absent in guild-scoped commands
        if (data.hasKey("context"))
            this.context = InteractionContextType.fromKey(data.getString("context"));
        else
            this.context = null;
        this.appPermissions = Collections.unmodifiableSet(Permission.getPermissions(data.getLong("app_permissions")));
        this.integrationOwners = new IntegrationOwnersImpl(data.optObject("authorizing_integration_owners").orElseGet(DataObject::empty));

        DataObject channelJson = data.getObject("channel");
        if (guild instanceof GuildImpl)
        {
            member = jda.getEntityBuilder().createMember((GuildImpl) guild, data.getObject("member"));
            jda.getEntityBuilder().updateMemberCache((MemberImpl) member);
            user = member.getUser();

            GuildChannel channel = ((Guild) guild).getGuildChannelById(channelJson.getUnsignedLong("id"));
            if (channel == null && ChannelType.fromId(channelJson.getInt("type")).isThread())
                channel = api.getEntityBuilder().createThreadChannel((GuildImpl) guild, channelJson, guild.getIdLong(), false);
            if (channel == null)
                throw new IllegalStateException("Failed to create channel instance for interaction! Channel Type: " + channelJson.getInt("type"));
            this.channel = channel;
        }
        else if (guild instanceof UnknownGuildImpl)
        {
            member = jda.getEntityBuilder().createMemberFromUnknownGuild((UnknownGuildImpl) guild, data.getObject("member"))
                    .setInteractionPermissions(data.getObject("member").getLong("permissions"));
            user = member.getUser();

            channelJson.put("permission_overwrites", DataArray.fromCollection(Collections.singleton(
                    DataObject.empty()
                            .put("id", getUser().getIdLong())
                            .put("type", 1)
                            //TODO something something implicit permissions?
                            .put("allow", channelJson.getLong("permissions"))
                            .put("deny", 0)
            )));

            channel = jda.getEntityBuilder().createGuildChannelFromUnknownGuild((UnknownGuildImpl) guild, channelJson);
            if (channel == null)
                throw new IllegalStateException("Failed to create channel instance for interaction! Channel Type: " + channelJson.getInt("type"));
        }
        else
        {
            member = null;
            long channelId = channelJson.getUnsignedLong("id");
            ChannelType type = ChannelType.fromId(channelJson.getInt("type"));
            if (type != ChannelType.PRIVATE)
                throw new IllegalArgumentException("Received interaction in unexpected channel type! Type " + type + " is not supported yet!");
            PrivateChannel channel = jda.getPrivateChannelById(channelId);
            if (channel == null)
            {
                channel = jda.getEntityBuilder().createPrivateChannel(
                    DataObject.empty()
                        .put("id", channelId)
                        .put("recipient", data.getObject("user"))
                );
            }
            this.channel = channel;

            User user = channel.getUser();
            if (user == null)
            {
                user = jda.getEntityBuilder().createUser(data.getObject("user"));
                ((PrivateChannelImpl) channel).setUser(user);
                ((UserImpl) user).setPrivateChannel(channel);
            }
            this.user = user;
        }
    }

    private UnknownGuild getOrCreateUnknownGuild(DataObject data)
    {
        UnknownGuild unknownGuild = null;
        if (data.hasKey("guild_id"))
        {
            final long guildId = data.getUnsignedLong("guild_id");
            unknownGuild = api.getGuildById(guildId);
            if (unknownGuild == null)
                unknownGuild = new UnknownGuildImpl(api, guildId);
        }
        return unknownGuild;
    }

    // Used to allow interaction hook to send messages after acknowledgements
    // This is implemented only in DeferrableInteractionImpl where a hook is present!
    public synchronized void releaseHook(boolean success) {}

    // Ensures that one cannot acknowledge an interaction twice
    public synchronized boolean ack()
    {
        boolean wasAck = isAck;
        this.isAck = true;
        return wasAck;
    }

    @Override
    public synchronized boolean isAcknowledged()
    {
        return isAck;
    }

    @Override
    public long getIdLong()
    {
        return id;
    }

    @Override
    public int getTypeRaw()
    {
        return type;
    }

    @Nonnull
    @Override
    public String getToken()
    {
        return token;
    }

    @Override
    public boolean hasGuild()
    {
        return guild instanceof Guild;
    }

    @Nullable
    @Override
    public Guild getGuild()
    {
        if (hasGuild())
            return (Guild) guild;
        throw new IllegalStateException("Cannot get a full guild object from an unknown guild");
    }

    @Nullable
    @Override
    public UnknownGuild getUnknownGuild()
    {
        return guild;
    }

    @Nullable
    @Override
    public Channel getChannel()
    {
        return channel;
    }

    @Override
    public long getChannelIdLong()
    {
        return channelId;
    }

    @Nonnull
    public DiscordLocale getUserLocale()
    {
        return userLocale;
    }

    @Nonnull
    @Override
    public InteractionContextType getContext()
    {
        return context;
    }

    @Nonnull
    @Override
    public Set<Permission> getApplicationPermissions()
    {
        return appPermissions;
    }

    @Nullable
    @Override
    public IntegrationOwners getIntegrationOwners()
    {
        return integrationOwners;
    }

    @Nonnull
    @Override
    public User getUser()
    {
        return user;
    }

    @Nullable
    @Override
    public Member getMember()
    {
        return member;
    }

    @Nonnull
    @Override
    public JDA getJDA()
    {
        return api;
    }
}
