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

import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.forums.ForumTag;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.channel.concrete.*;
import net.dv8tion.jda.internal.entities.channel.mixin.attribute.IPostContainerMixin;
import net.dv8tion.jda.internal.utils.Helpers;
import net.dv8tion.jda.internal.utils.UnlockHook;
import net.dv8tion.jda.internal.utils.cache.SortedSnowflakeCacheViewImpl;

import java.util.stream.IntStream;

public abstract class AbstractEntityBuilder
{
    protected final JDAImpl api;

    AbstractEntityBuilder(JDAImpl api)
    {
        this.api = api;
    }

    public JDAImpl getJDA()
    {
        return api;
    }

    protected void configureCategory(DataObject json, CategoryImpl channel)
    {
        channel
                .setName(json.getString("name"))
                .setPosition(json.getInt("position"));
    }

    protected void configureTextChannel(DataObject json, TextChannelImpl channel)
    {
        channel
                .setParentCategory(json.getLong("parent_id", 0))
                .setLatestMessageIdLong(json.getLong("last_message_id", 0))
                .setName(json.getString("name"))
                .setTopic(json.getString("topic", null))
                .setPosition(json.getInt("position"))
                .setNSFW(json.getBoolean("nsfw"))
                .setDefaultThreadSlowmode(json.getInt("default_thread_rate_limit_per_user", 0))
                .setSlowmode(json.getInt("rate_limit_per_user", 0));
    }

    protected void configureNewsChannel(DataObject json, NewsChannelImpl channel)
    {
        channel
                .setParentCategory(json.getLong("parent_id", 0))
                .setLatestMessageIdLong(json.getLong("last_message_id", 0))
                .setName(json.getString("name"))
                .setTopic(json.getString("topic", null))
                .setPosition(json.getInt("position"))
                .setNSFW(json.getBoolean("nsfw"));
    }

    protected void configureVoiceChannel(DataObject json, VoiceChannelImpl channel)
    {
        channel
                .setParentCategory(json.getLong("parent_id", 0))
                .setLatestMessageIdLong(json.getLong("last_message_id", 0))
                .setName(json.getString("name"))
                .setStatus(json.getString("status", ""))
                .setPosition(json.getInt("position"))
                .setUserLimit(json.getInt("user_limit"))
                .setNSFW(json.getBoolean("nsfw"))
                .setBitrate(json.getInt("bitrate"))
                .setRegion(json.getString("rtc_region", null))
//            .setDefaultThreadSlowmode(json.getInt("default_thread_rate_limit_per_user", 0))
                .setSlowmode(json.getInt("rate_limit_per_user", 0));
    }

    protected void configureStageChannel(DataObject json, StageChannelImpl channel)
    {
        channel
                .setParentCategory(json.getLong("parent_id", 0))
                .setLatestMessageIdLong(json.getLong("last_message_id", 0))
                .setName(json.getString("name"))
                .setPosition(json.getInt("position"))
                .setBitrate(json.getInt("bitrate"))
                .setUserLimit(json.getInt("user_limit", 0))
                .setNSFW(json.getBoolean("nsfw"))
                .setRegion(json.getString("rtc_region", null))
//            .setDefaultThreadSlowmode(json.getInt("default_thread_rate_limit_per_user", 0))
                .setSlowmode(json.getInt("rate_limit_per_user", 0));
    }

    protected void configureThreadChannel(DataObject json, ThreadChannelImpl channel)
    {
        DataObject threadMetadata = json.getObject("thread_metadata");

        if (!json.isNull("applied_tags") && api.isCacheFlagSet(CacheFlag.FORUM_TAGS))
        {
            DataArray array = json.getArray("applied_tags");
            channel.setAppliedTags(IntStream.range(0, array.length()).mapToLong(array::getUnsignedLong));
        }

        channel
                .setName(json.getString("name"))
                .setFlags(json.getInt("flags", 0))
                .setOwnerId(json.getLong("owner_id"))
                .setMemberCount(json.getInt("member_count"))
                .setMessageCount(json.getInt("message_count"))
                .setTotalMessageCount(json.getInt("total_message_count", 0))
                .setLatestMessageIdLong(json.getLong("last_message_id", 0))
                .setSlowmode(json.getInt("rate_limit_per_user", 0))
                .setLocked(threadMetadata.getBoolean("locked"))
                .setArchived(threadMetadata.getBoolean("archived"))
                .setInvitable(threadMetadata.getBoolean("invitable"))
                .setArchiveTimestamp(Helpers.toTimestamp(threadMetadata.getString("archive_timestamp")))
                .setCreationTimestamp(threadMetadata.isNull("create_timestamp") ? 0 : Helpers.toTimestamp(threadMetadata.getString("create_timestamp")))
                .setAutoArchiveDuration(ThreadChannel.AutoArchiveDuration.fromKey(threadMetadata.getInt("auto_archive_duration")));
    }

    protected void configureForumChannel(DataObject json, ForumChannelImpl channel)
    {
        if (api.isCacheFlagSet(CacheFlag.FORUM_TAGS))
        {
            DataArray tags = json.getArray("available_tags");
            for (int i = 0; i < tags.length(); i++)
                createForumTag(channel, tags.getObject(i), i);
        }

        channel
                .setParentCategory(json.getLong("parent_id", 0))
                .setFlags(json.getInt("flags", 0))
                .setDefaultReaction(json.optObject("default_reaction_emoji").orElse(null))
                .setDefaultSortOrder(json.getInt("default_sort_order", -1))
                .setDefaultLayout(json.getInt("default_forum_layout", -1))
                .setName(json.getString("name"))
                .setTopic(json.getString("topic", null))
                .setPosition(json.getInt("position"))
                .setDefaultThreadSlowmode(json.getInt("default_thread_rate_limit_per_user", 0))
                .setSlowmode(json.getInt("rate_limit_per_user", 0))
                .setNSFW(json.getBoolean("nsfw"));
    }

    protected void configureMediaChannel(DataObject json, MediaChannelImpl channel)
    {
        if (api.isCacheFlagSet(CacheFlag.FORUM_TAGS))
        {
            DataArray tags = json.getArray("available_tags");
            for (int i = 0; i < tags.length(); i++)
                createForumTag(channel, tags.getObject(i), i);
        }

        channel
                .setParentCategory(json.getLong("parent_id", 0))
                .setFlags(json.getInt("flags", 0))
                .setDefaultReaction(json.optObject("default_reaction_emoji").orElse(null))
                .setDefaultSortOrder(json.getInt("default_sort_order", -1))
                .setName(json.getString("name"))
                .setTopic(json.getString("topic", null))
                .setPosition(json.getInt("position"))
                .setDefaultThreadSlowmode(json.getInt("default_thread_rate_limit_per_user", 0))
                .setSlowmode(json.getInt("rate_limit_per_user", 0))
                .setNSFW(json.getBoolean("nsfw"));
    }

    public ForumTagImpl createForumTag(IPostContainerMixin<?> channel, DataObject json, int index)
    {
        final long id = json.getUnsignedLong("id");
        SortedSnowflakeCacheViewImpl<ForumTag> cache = channel.getAvailableTagCache();
        ForumTagImpl tag = (ForumTagImpl) cache.get(id);

        if (tag == null)
        {
            try (UnlockHook lock = cache.writeLock())
            {
                tag = new ForumTagImpl(id);
                cache.getMap().put(id, tag);
            }
        }

        tag.setName(json.getString("name"))
           .setModerated(json.getBoolean("moderated"))
           .setEmoji(json)
           .setPosition(index);
        return tag;
    }

    protected void configureMember(DataObject memberJson, MemberImpl member)
    {
        member.setNickname(memberJson.getString("nick", null));
        member.setAvatarId(memberJson.getString("avatar", null));
        if (!memberJson.isNull("flags"))
            member.setFlags(memberJson.getInt("flags"));

        long boostTimestamp = memberJson.isNull("premium_since")
                ? 0
                : Helpers.toTimestamp(memberJson.getString("premium_since"));
        member.setBoostDate(boostTimestamp);

        long timeOutTimestamp = memberJson.isNull("communication_disabled_until")
                ? 0
                : Helpers.toTimestamp(memberJson.getString("communication_disabled_until"));
        member.setTimeOutEnd(timeOutTimestamp);

        if (!memberJson.isNull("pending"))
            member.setPending(memberJson.getBoolean("pending"));

        // Load joined_at if necessary
        if (!memberJson.isNull("joined_at") && !member.hasTimeJoined())
        {
            member.setJoinDate(Helpers.toTimestamp(memberJson.getString("joined_at")));
        }
    }
}
