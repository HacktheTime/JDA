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

import net.dv8tion.jda.api.interactions.IntegrationOwners;
import net.dv8tion.jda.api.interactions.IntegrationType;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.utils.EntityString;

import javax.annotation.Nullable;

public class IntegrationOwnersImpl implements IntegrationOwners
{
    private final Long guildIntegration;
    private final long userIntegration;

    public IntegrationOwnersImpl(DataObject authorizedIntegrationOwners)
    {
        this.userIntegration = authorizedIntegrationOwners.getLong(IntegrationType.USER_INSTALL.getType(), 0);

        if (authorizedIntegrationOwners.hasKey(IntegrationType.GUILD_INSTALL.getType()))
            this.guildIntegration = authorizedIntegrationOwners.getLong(IntegrationType.GUILD_INSTALL.getType());
        else
            this.guildIntegration = null;
    }

    @Override
    public long getAuthorizingUserIdLong()
    {
        return userIntegration;
    }

    @Nullable
    @Override
    public Long getAuthorizingGuildIdLong()
    {
        return guildIntegration;
    }

    @Override
    public String toString()
    {
        return new EntityString(this)
                .addMetadata("user", getAuthorizingUserId())
                .addMetadata("guild", getAuthorizingGuildId())
                .toString();
    }
}
