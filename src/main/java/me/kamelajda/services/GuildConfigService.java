/*
 *
 * Copyright 2022 SpotifyB0T
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package me.kamelajda.services;

import lombok.extern.slf4j.Slf4j;
import me.kamelajda.jpa.models.GuildConfig;
import me.kamelajda.jpa.repository.GuildConfigRepository;
import me.kamelajda.utils.language.LanguageType;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GuildConfigService {

    private final GuildConfigRepository guildConfigRepository;

    public GuildConfigService(GuildConfigRepository guildConfigRepository) {
        this.guildConfigRepository = guildConfigRepository;
    }

    public GuildConfig load(Long guildId) {
        return load(guildId, LanguageType.ENGLISH);
    }

    public GuildConfig load(Long guildId, LanguageType forceLanguage) {
        GuildConfig config = guildConfigRepository.findById(guildId).orElse(null);
        if (config == null) return guildConfigRepository.save(GuildConfig.builder().guildId(guildId).language(forceLanguage).build());
        return config;
    }

    public void save(GuildConfig guildConfig) {
        guildConfigRepository.save(guildConfig);
    }

}
