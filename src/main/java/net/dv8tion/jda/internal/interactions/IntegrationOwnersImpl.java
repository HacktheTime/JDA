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

import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.interactions.IntegrationOwners;
import net.dv8tion.jda.api.interactions.IntegrationType;
import net.dv8tion.jda.api.utils.data.DataObject;

import javax.annotation.Nonnull;

public class IntegrationOwnersImpl implements IntegrationOwners
{
    private final long guildIntegration;
    private final UserSnowflake userIntegration;

    public IntegrationOwnersImpl(DataObject authorizedIntegrationOwners)
    {
        this.userIntegration = UserSnowflake.fromId(authorizedIntegrationOwners.getLong(IntegrationType.USER_INSTALL.getType()));
        this.guildIntegration = authorizedIntegrationOwners.getLong(IntegrationType.GUILD_INSTALL.getType(), -1);
    }

    @Nonnull
    @Override
    public UserSnowflake getUserIntegration()
    {
        return userIntegration;
    }

    @Override
    public boolean hasGuildIntegration()
    {
        return guildIntegration != -1;
    }

    @Override
    public long getGuildIntegration()
    {
        if (!hasGuildIntegration())
            throw new IllegalStateException("This interaction has no guild integration owner");

        return guildIntegration;
    }
}
