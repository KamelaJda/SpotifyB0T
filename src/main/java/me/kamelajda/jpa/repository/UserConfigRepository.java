package me.kamelajda.jpa.repository;

import me.kamelajda.jpa.models.UserConfig;
import me.kamelajda.utils.language.LanguageType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserConfigRepository extends JpaRepository<UserConfig, Long> {

    List<UserConfig> findAllByLanguageType(LanguageType language);

}
