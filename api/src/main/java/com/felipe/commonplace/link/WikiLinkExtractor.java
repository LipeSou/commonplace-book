package com.felipe.commonplace.link;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Deriva os alvos dos {@code [[wikilinks]]} de um content markdown, sem NUNCA alterá-lo.
 *
 * Regras (estilo Obsidian):
 * - {@code [[Título]]} → alvo "Título".
 * - {@code [[Título|apelido]]} → alvo "Título" (o apelido é só exibição).
 * - {@code [[Título#seção]]} → alvo "Título" (a âncora não muda a nota alvo).
 * - Espaços nas pontas somem; o miolo do título fica como foi escrito.
 * - Duplicatas caem: {@code [[Zen]]} e {@code [[zen]]} são o mesmo alvo (vence a 1ª grafia).
 */
@Component
public class WikiLinkExtractor {

    private static final Pattern WIKILINK = Pattern.compile("\\[\\[([^\\[\\]\\n]+)]]");

    /**
     * Os títulos alvo, na ordem em que aparecem no texto, sem repetição.
     */
    public Collection<String> extract(String content) {
        Map<String, String> byKey = new LinkedHashMap<>();
        if (content == null || content.isEmpty()) {
            return byKey.values();
        }
        Matcher matcher = WIKILINK.matcher(content);
        while (matcher.find()) {
            String title = targetOf(matcher.group(1));
            if (!title.isEmpty()) {
                byKey.putIfAbsent(title.toLowerCase(Locale.ROOT), title);
            }
        }
        return byKey.values();
    }

    private String targetOf(String inside) {
        int cut = indexOfAny(inside, '|', '#');
        return (cut < 0 ? inside : inside.substring(0, cut)).trim();
    }

    private int indexOfAny(String value, char... chars) {
        int found = -1;
        for (char c : chars) {
            int at = value.indexOf(c);
            if (at >= 0 && (found < 0 || at < found)) {
                found = at;
            }
        }
        return found;
    }
}
