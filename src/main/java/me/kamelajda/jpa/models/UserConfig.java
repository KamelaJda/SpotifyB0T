package me.kamelajda.jpa.models;

import lombok.*;
import me.kamelajda.utils.language.LanguageType;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
@Table
@AllArgsConstructor
@Builder
public class UserConfig {

    @Id private Long userId;

    @Builder.Default
    private LanguageType languageType = LanguageType.values()[0];

}
