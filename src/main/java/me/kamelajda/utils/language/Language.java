package me.kamelajda.utils.language;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Properties;

@Slf4j
@RequiredArgsConstructor
public class Language {

    private final LanguageType languageType;

    private Properties property = null;

    public void loadMessages() {
        Properties p = new Properties();

        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("language/messages-" + languageType.getShortName() + ".properties");

        if (stream == null) {
            log.error("Cannot load language " + languageType + "!");
            return;
        }

        try {
            p.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
            property = p;
        } catch (IOException e) {
            log.error("Cannot load language", e);
        }
    }

    public String get(String key) {
        if (property != null && property.containsKey(key)) return property.getProperty(key, key);
        return key;
    }

    public String get(String key, String... toReplace) {
        return get(key, (Object[]) toReplace);
    }

    public String get(String key, Object... toReplace) {
        ArrayList<String> parsedArray = new ArrayList<>();
        for (Object k : toReplace) parsedArray.add(k.toString());
        return String.format(get(key), parsedArray.toArray());
    }

}
