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

package net.dv8tion.jda.api.entities;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.managers.GuildManager;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Set;

//TODO document
public interface PartialGuild extends ISnowflake
{
    /**
     * Returns the {@link net.dv8tion.jda.api.JDA JDA} instance of this Guild
     *
     * @return the corresponding JDA instance
     */
    @Nonnull
    JDA getJDA();

    default boolean isGuild()
    {
        return this instanceof Guild;
    }

    @Nonnull
    default Guild asGuild()
    {
        if (isGuild())
            return ((Guild) this);
        throw new IllegalStateException("Cannot get a full guild object from an unknown guild");
    }

    /**
     * The Features of the {@link net.dv8tion.jda.api.entities.Guild Guild}.
     *
     * <p>Features can be updated using {@link GuildManager#setFeatures(Collection)}.
     *
     * @return Never-null, unmodifiable Set containing all of the Guild's features.
     *
     * @see <a target="_blank" href="https://discord.com/developers/docs/resources/guild#guild-object-guild-features">List of Features</a>
     */
    @Nonnull
    Set<String> getFeatures();

    /**
     * Whether the invites for this guild are paused/disabled.
     * <br>This is equivalent to {@code getFeatures().contains("INVITES_DISABLED")}.
     *
     * @return True, if invites are paused/disabled
     */
    default boolean isInvitesDisabled()
    {
        return getFeatures().contains("INVITES_DISABLED");
    }
}
