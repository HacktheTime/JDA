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

import net.dv8tion.jda.api.entities.PartialGuild;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.utils.EntityString;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Set;

public class PartialGuildImpl implements PartialGuild
{
    protected final JDAImpl api;
    protected final long id;
    private Set<String> features;

    public PartialGuildImpl(JDAImpl api, long id)
    {
        this.id = id;
        this.api = api;
    }

    @Override
    public long getIdLong()
    {
        return id;
    }

    @Nonnull
    @Override
    public JDAImpl getJDA()
    {
        return api;
    }

    @Nonnull
    @Override
    public Set<String> getFeatures()
    {
        return features;
    }

    public PartialGuildImpl setFeatures(Set<String> features)
    {
        this.features = Collections.unmodifiableSet(features);
        return this;
    }

    // -- Object overrides --

    @Override
    public boolean equals(Object o)
    {
        if (o == this)
            return true;
        if (!(o instanceof PartialGuildImpl))
            return false;
        PartialGuildImpl oGuild = (PartialGuildImpl) o;
        return this.id == oGuild.id;
    }

    @Override
    public int hashCode()
    {
        return Long.hashCode(id);
    }

    @Override
    public String toString()
    {
        return new EntityString(this).toString();
    }
}
