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

package me.kamelajda.jpa.repository;

import me.kamelajda.jpa.models.ArtistInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ArtistInfoRepository extends JpaRepository<ArtistInfo, Long> {

    Optional<ArtistInfo> findBySpotifyId(String spotifyId);

    List<ArtistInfo> findAllBySubscribeUsers_UserId(Long subscribeUsers_userId);

    Set<ArtistInfo> findAllBySubscribeUsers_UserIdIn(Collection<Long> usersIds);

}
