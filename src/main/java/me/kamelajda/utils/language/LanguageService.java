package me.kamelajda.utils.language;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class LanguageService {

    private final Map<LanguageType, Language> languages = new HashMap<>();

    public LanguageService() {
        for (LanguageType value : LanguageType.values()) {
            Language language = new Language(value);
            language.loadMessages();
            languages.put(value, language);
        }
    }

    public Language get(LanguageType lang) {
        return languages.getOrDefault(lang, languages.values().stream().findAny().orElseThrow());
    }

}
