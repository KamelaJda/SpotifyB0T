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

import java.util.List;
import me.kamelajda.jpa.models.UserConfig;
import me.kamelajda.jpa.repository.UserConfigRepository;
import me.kamelajda.utils.language.LanguageType;
import org.springframework.stereotype.Service;

@Service
public class UserConfigService {

  private final UserConfigRepository userConfigRepository;

  public UserConfigService(UserConfigRepository userConfigRepository) {
    this.userConfigRepository = userConfigRepository;
  }

  public UserConfig load(Long userId) {
    UserConfig config = userConfigRepository.findById(userId).orElse(null);
    if (config == null)
      config = userConfigRepository.save(UserConfig.builder().userId(userId).build());

    return config;
  }

  public List<UserConfig> getAllUsersByLanguage(LanguageType language) {
    return userConfigRepository.findAllByLanguageType(language);
  }

  public void changeLanguage(Long userId, LanguageType language) {
    UserConfig config = load(userId);
    config.setLanguageType(language);
    userConfigRepository.save(config);
  }

  public List<UserConfig> getAll() {
    return userConfigRepository.findAll();
  }
}
