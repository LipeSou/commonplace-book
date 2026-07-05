package com.felipe.commonplace.tag;

import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Deriva as tags de um content markdown, sem NUNCA alterá-lo.
 *
 * Regras (estilo Obsidian):
 * - `#tag` = `#` colado num corpo de tag (letras/dígitos/`_`/`-`/`/`).
 * - `# Título` NÃO é tag (há espaço após o `#` → heading markdown).
 * - `palavra#x` NÃO é tag (o `#` vem colado a caractere de palavra — ex.: URL com âncora).
 * - Uma tag precisa de ao menos uma letra (puro número não conta: `#123` não é tag).
 * - Normalizadas em minúsculas para unificar `#Zen` e `#zen`.
 */
@Component
public class TagExtractor {

    // `#` não precedido por caractere de palavra nem por outro `#`, seguido do corpo da tag
    private static final Pattern TAG =
            Pattern.compile("(?<![\\w#])#([\\w/-]+)", Pattern.UNICODE_CHARACTER_CLASS);

    private static final Pattern TRAILING_SEP = Pattern.compile("[-/]+$");

    public Set<String> extract(String content) {
        Set<String> tags = new LinkedHashSet<>();
        if (content == null || content.isEmpty()) {
            return tags;
        }
        Matcher matcher = TAG.matcher(content);
        while (matcher.find()) {
            String raw = TRAILING_SEP.matcher(matcher.group(1)).replaceAll("");
            if (raw.isEmpty() || !hasLetter(raw)) {
                continue;
            }
            tags.add(raw.toLowerCase(Locale.ROOT));
        }
        return tags;
    }

    private boolean hasLetter(String value) {
        return value.codePoints().anyMatch(Character::isLetter);
    }
}
