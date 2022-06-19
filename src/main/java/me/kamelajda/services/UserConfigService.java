package me.kamelajda.services;

import me.kamelajda.jpa.models.UserConfig;
import me.kamelajda.jpa.repository.UserConfigRepository;
import me.kamelajda.utils.language.LanguageType;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserConfigService {

    private final UserConfigRepository userConfigRepository;

    public UserConfigService(UserConfigRepository userConfigRepository) {
        this.userConfigRepository = userConfigRepository;
    }

    public UserConfig load(Long userId) {
        UserConfig config = userConfigRepository.findById(userId).orElse(null);
        if (config == null) config = userConfigRepository.save(UserConfig.builder().userId(userId).build());

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
