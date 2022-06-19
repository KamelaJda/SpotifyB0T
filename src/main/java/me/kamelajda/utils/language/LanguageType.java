package me.kamelajda.utils.language;

import com.neovisionaries.i18n.CountryCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.ZoneId;

@Getter
@AllArgsConstructor
public enum LanguageType {
    POLISH("pl", "Polski", ZoneId.of("Europe/Warsaw"), CountryCode.PL),
    ENGLISH("en-US", "English (US)", ZoneId.of("US/Central"), CountryCode.US);

    private final String shortName;
    private final String displayName;
    private final ZoneId timeZone;
    private final CountryCode countryCode;
}
