package com.felipe.commonplace.link;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WikiLinkExtractorTest {

    private final WikiLinkExtractor extractor = new WikiLinkExtractor();

    @Test
    void extraiOsTitulosNaOrdemEmQueAparecem() {
        assertThat(extract("vejo [[Wabi-sabi]] e depois [[O vazio]]"))
                .containsExactly("Wabi-sabi", "O vazio");
    }

    @Test
    void ignoraApelidoEAncora() {
        assertThat(extract("[[O vazio|o nada]] e [[Bushido#virtudes]]"))
                .containsExactly("O vazio", "Bushido");
    }

    @Test
    void aparaEspacosENaoRepeteOMesmoAlvo() {
        assertThat(extract("[[  Zen  ]] outra vez [[zen]]")).containsExactly("Zen");
    }

    @Test
    void naoConfundeComLinkMarkdownNemColcheteSolto() {
        assertThat(extract("[texto](http://x) [ainda] [[  ]] [[\n]]")).isEmpty();
    }

    @Test
    void contentVazioOuNuloNaoQuebra() {
        assertThat(extract(null)).isEmpty();
        assertThat(extract("")).isEmpty();
    }

    private List<String> extract(String content) {
        return List.copyOf(extractor.extract(content));
    }
}
