package me.kamelajda.jpa.models;

import lombok.*;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.util.ArrayList;
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

    @Id @GeneratedValue private Long id;

    private String spotifyId;

    private String displayName;

    private String thumbnailUrl;

    private String link;

    private String lastAlbumName;
    private String lastAlbumDate;
    private String lastAlbumLink;

    @ManyToMany
    @ToString.Exclude
    @LazyCollection(LazyCollectionOption.FALSE)
    @Builder.Default
    private Set<UserConfig> subscribeUsers = new HashSet<>();

}
