package org.llamadrama.tokenizer;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.llamadrama.util.Pair;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class TokenizerTest {
    private Tokenizer tokenizer;
    private Map<String, Integer> specialTokens;

    @BeforeEach
    void setUp() {
        Vocabulary vocabulary = new Vocabulary(new String[]{
                "a", "b", "c", "ab", "h", "e", "l", "o", "t", "s", "1", "2", "3", "!", "@", "#"});

        // Set up special tokens
        specialTokens = new HashMap<>();
        specialTokens.put("<|endoftext|>", 50256);

        // Set up merges
        List<Pair<Integer, Integer>> merges = List.of(
                new Pair<>(0, 1)  // merge 'a' and 'b' into 'ab'
        );

        // Create tokenizer with a simple regex pattern
        tokenizer = new Tokenizer(vocabulary, merges, "[a-z0-9!@#]+", specialTokens);
    }

    @Test
    void testConstructor() {
        assertNotNull(tokenizer);
        assertEquals(specialTokens, tokenizer.getSpecialTokens());
    }

    @Test
    void testIsSpecialToken() {
        assertTrue(tokenizer.isSpecialToken(50256));
        assertFalse(tokenizer.isSpecialToken(0));
    }

    @Test
    void testEncodeOrdinary() {
        List<Integer> encoded = tokenizer.encodeOrdinary("ab");
        assertEquals(List.of(3), encoded);  // 'ab' should be encoded as a single token
    }

    @Test
    void testEncodeOrdinaryWithUnknown() {
        Exception exception = assertThrows(NoSuchElementException.class, () -> {
            tokenizer.encodeOrdinary("abx");
        });

        assertTrue(exception.getMessage().contains("Token or special token \"abx\" not present"));
    }

    @Test
    void testEncodeWithSpecialTokens() {
        List<Integer> encoded = tokenizer.encode("ab<|endoftext|>", Set.of("<|endoftext|>"));
        assertEquals(List.of(3), encoded);
    }

    @Test
    void testEncodeWithDisallowedSpecialTokens() {
        Exception exception = assertThrows(NoSuchElementException.class, () -> {
            tokenizer.encode("ab<|endoftext|>", Set.of());;
        });

        assertThat(exception.getMessage(), containsString("Token or special token \"endoftext\" not present, failed on \"n\""));
    }

    @Test
    void testDecodeSimple() {
        List<Integer> tokens = Arrays.asList(0, 1);  // 'a', 'b'
        String decoded = tokenizer.decode(tokens);
        assertEquals("ab", decoded);
    }

    @ParameterizedTest
    @ValueSource(strings = {"hello", "test123", "!@#"})
    void testEncodeDecode(String input) {
        int[] encoded = tokenizer.encode(input);
        String decoded = tokenizer.decode(Arrays.stream(encoded).boxed().toList());
        assertEquals(input, decoded);
    }

    @Test
    void testReplaceControlCharacters() {
        String input = "Hello\u0000World";
        String result = Tokenizer.replaceControlCharacters(input);
        assertTrue(result.contains("\\u0000"));
        assertFalse(result.contains("\u0000"));
    }

    @Test
    void testEncodeAsList() {
        List<Integer> encoded = tokenizer.encodeAsList("ab");
        assertNotNull(encoded);
        assertTrue(encoded instanceof List);
    }

    @Test
    void testByteToUnicodeConsistency() {
        // Test that all byte values are mapped
        for (int i = 0; i < 256; i++) {
            assertTrue(Tokenizer.BYTE_ENCODER.containsKey(i),
                    "Byte value " + i + " should be mapped");
        }

        // Test bijection between encoder and decoder
        assertEquals(Tokenizer.BYTE_ENCODER.size(), Tokenizer.BYTE_DECODER.size(),
                "Encoder and decoder should have same size");

        Tokenizer.BYTE_ENCODER.forEach((key, value) ->
                assertEquals(key, Tokenizer.BYTE_DECODER.get(value),
                        "Encoder and decoder should be inverse functions"));
    }

    @ParameterizedTest
    @MethodSource("provideEdgeCases")
    void testEdgeCases(String input, Set<String> allowedSpecial) {
        assertDoesNotThrow(() -> tokenizer.encode(input, allowedSpecial));
    }

    private static Stream<Arguments> provideEdgeCases() {
        return Stream.of(
                Arguments.of("", Set.of()),
                Arguments.of(" ", Set.of()),
                Arguments.of("\n", Set.of())
        );
    }
}
