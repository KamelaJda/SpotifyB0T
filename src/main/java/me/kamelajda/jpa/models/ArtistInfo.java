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

package me.kamelajda.jpa.models;

import lombok.*;
import me.kamelajda.jpa.repository.ArtistInfoRepository;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
@Table
@Builder
public class ArtistInfo {

    /**
     * @see ArtistInfoRepository#findAllBySubscribeUsers_UserId(UserConfig)
     * @see ArtistInfoRepository#findAllBySubscribeGuilds(GuildConfig)
     */
    public ArtistInfo(ArtistInfo artistInfo) {
        this.id = artistInfo.id;
        this.spotifyId = artistInfo.spotifyId;
        this.displayName = artistInfo.displayName;
        this.thumbnailUrl = artistInfo.thumbnailUrl;
        this.link = artistInfo.link;
        this.lastAlbum = artistInfo.lastAlbum;
        this.lastTrack = artistInfo.lastTrack;
        this.lastFeat = artistInfo.lastFeat;
    }

    @Id @GeneratedValue private Long id;

    private String spotifyId;

    private String displayName;

    private String thumbnailUrl;

    private String link;

    @OneToOne
    private ArtistCreation lastAlbum;

    @OneToOne
    private ArtistCreation lastTrack;

    @OneToOne
    private ArtistCreation lastFeat;

    @ToString.Exclude
    @LazyCollection(value = LazyCollectionOption.EXTRA)
    @Builder.Default
    @ManyToMany
    private Set<UserConfig> subscribeUsers = new HashSet<>();

    @ToString.Exclude
    @LazyCollection(value = LazyCollectionOption.EXTRA)
    @Builder.Default
    @ManyToMany
    private Set<GuildConfig> subscribeGuilds = new HashSet<>();

}
