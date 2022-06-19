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
