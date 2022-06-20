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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
@Table
@Builder
public class ArtistInfo {

    public ArtistInfo(Long id, String spotifyId, String displayName, String thumbnailUrl, String link, String lastAlbumName, String lastAlbumDate, String lastAlbumLink) {
        this.id = id;
        this.spotifyId = spotifyId;
        this.displayName = displayName;
        this.thumbnailUrl = thumbnailUrl;
        this.link = link;
        this.lastAlbumName = lastAlbumName;
        this.lastAlbumDate = lastAlbumDate;
        this.lastAlbumLink = lastAlbumLink;
    }

    @Id @GeneratedValue private Long id;

    private String spotifyId;

    private String displayName;

    private String thumbnailUrl;

    private String link;

    private String lastAlbumName;
    private String lastAlbumDate;
    private String lastAlbumLink;

    @ToString.Exclude
    @LazyCollection(value = LazyCollectionOption.EXTRA)
    @Builder.Default
    @ManyToMany
    @Fetch(FetchMode.SUBSELECT)
    private Set<UserConfig> subscribeUsers = new HashSet<>();

    @ToString.Exclude
    @LazyCollection(value = LazyCollectionOption.EXTRA)
    @Builder.Default
    @ManyToMany
    @Fetch(FetchMode.SUBSELECT)
    private Set<GuildConfig> subscribeGuilds = new HashSet<>();

}
