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

package net.dv8tion.jda.api.entities.detached;

/**
 * Represents an entity which might not be known by the bot.
 * It is mostly limited to user-installed interactions,
 * where the bot {@link net.dv8tion.jda.api.entities.Guild#isDetached() isn't in the guild},
 * and in group DMs.
 * <br>Some information may be unavailable on detached entities,
 * and most {@link net.dv8tion.jda.api.requests.RestAction RestActions} will throw a {@link net.dv8tion.jda.api.exceptions.DetachedEntityException DetachedEntityException}.
 */
public interface IDetachableEntity
{
    /**
     * Whether this entity is detached.
     *
     * <p>A detached entity is not known to the bot.
     * This is the case for interactions that happen through user-installed commands
     * in channels that the bot is not a member of.
     * For instance, when a command is used in a group channel or a guild that the bot is not a member in.
     *
     * @return {@code True}, if the entity is detached
     */
    boolean isDetached();
}
