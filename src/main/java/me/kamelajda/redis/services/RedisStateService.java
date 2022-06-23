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

package me.kamelajda.redis.services;

import me.kamelajda.redis.data.RedisSpotifyState;
import me.kamelajda.redis.repositories.RedisStateRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RedisStateService implements RedisService<RedisSpotifyState, String> {

    private final RedisStateRepository redisAvatarRepository;

    public RedisStateService(RedisStateRepository redisAvatarRepository) {
        this.redisAvatarRepository = redisAvatarRepository;
    }

    @Override
    public RedisSpotifyState save(RedisSpotifyState value) {
        redisAvatarRepository.removeByUserId(value.getUserId());
        value.setTimeToLive(5L);
        return redisAvatarRepository.save(value);
    }

    @Override
    public Optional<RedisSpotifyState> findValueById(String id) {
        return redisAvatarRepository.findById(id);
    }

    public void delete(String id) {
        redisAvatarRepository.deleteById(id);
    }

}
