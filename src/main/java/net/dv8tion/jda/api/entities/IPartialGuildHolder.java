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

import javax.annotation.Nullable;

//TODO document
public interface IPartialGuildHolder
{
    //TODO document
    default boolean hasGuild()
    {
        final PartialGuild partialGuild = getPartialGuild();
        if (partialGuild == null) return false;

        return partialGuild.isGuild();
    }

    @Nullable
    default Guild getGuild()
    {
        final PartialGuild partialGuild = getPartialGuild();
        if (partialGuild == null) return null;

        return partialGuild.asGuild();
    }

    //TODO document
    @Nullable
    PartialGuild getPartialGuild();
}
