package com.felipe.commonplace.note;

/**
 * Uma linha crua do resultado da busca: qual nota casou e o trecho que a fez casar.
 * A nota em si é carregada depois, pelo JPA, para o resultado sair igual a qualquer outro.
 */
public interface SearchProjection {

    Long getId();

    String getSnippet();
}
