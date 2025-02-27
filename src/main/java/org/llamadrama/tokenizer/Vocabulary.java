package org.llamadrama.tokenizer;

import java.util.Map;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public record Vocabulary(String[] tokens, Map<String, Integer> tokenToIndex) {
    public Vocabulary(String[] vocabulary) {
        this(vocabulary, IntStream.range(0, vocabulary.length)
                .boxed()
                .collect(Collectors.toUnmodifiableMap(i -> vocabulary[i], i -> i)));
    }

    public String get(int tokenIndex) {
        return tokens[tokenIndex];
    }

    public OptionalInt getIndex(String token) {
        Integer value = tokenToIndex.get(token);
        return value != null ? OptionalInt.of(value) : OptionalInt.empty();
    }

    public int size() {
        return tokens.length;
    }
}
