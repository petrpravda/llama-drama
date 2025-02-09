package org.llamadrama.core;

import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OptionsTest {
    @Test
    public void testParseCommandLine() {
        String input = "--model llama3.2-1b-q4_0.gguf --prompt \"Tell me a joke\"";
        List<Options.CommandLineOption> options = Options.parseCommandLine(input);
        assertEquals(2, options.size());
        assertEquals("--model", options.get(0).name());
        assertEquals("llama3.2-1b-q4_0.gguf", options.get(0).value());
        assertEquals("--prompt", options.get(1).name());
        assertEquals("Tell me a joke", options.get(1).value());
    }

    @Test
    public void testParseCommandLineWithSystemPrompt() {
        String input = "--model llama3.2-1b-q4_0.gguf --system-prompt \"Reply concisely, in French\" --prompt \"Who was Marie Curie?\"";
        List<Options.CommandLineOption> options = Options.parseCommandLine(input);
        assertEquals(3, options.size());
        assertEquals("--model", options.get(0).name());
        assertEquals("llama3.2-1b-q4_0.gguf", options.get(0).value());
        assertEquals("--system-prompt", options.get(1).name());
        assertEquals("Reply concisely, in French", options.get(1).value());
        assertEquals("--prompt", options.get(2).name());
        assertEquals("Who was Marie Curie?", options.get(2).value());
    }

    @Test
    public void testParseCommandLineWithChat() {
        String input = "--model llama3.2-1b-q4_0.gguf --system-prompt \"Answer concisely\" --chat";
        List<Options.CommandLineOption> options = Options.parseCommandLine(input);
        assertEquals(3, options.size());
        assertEquals("--model", options.get(0).name());
        assertEquals("llama3.2-1b-q4_0.gguf", options.get(0).value());
        assertEquals("--system-prompt", options.get(1).name());
        assertEquals("Answer concisely", options.get(1).value());
        assertEquals("--chat", options.get(2).name());
        assertEquals("", options.get(2).value());
    }

    @Test
    public void testParseCommandLineWithOnlyChat() {
        String input = "--model llama3.2-1b-q4_0.gguf --chat";
        List<Options.CommandLineOption> options = Options.parseCommandLine(input);
        assertEquals(2, options.size());
        assertEquals("--model", options.get(0).name());
        assertEquals("llama3.2-1b-q4_0.gguf", options.get(0).value());
        assertEquals("--chat", options.get(1).name());
        assertEquals("", options.get(1).value());
    }

    @Test
    public void testParseCommandLineWithStreamFalse() {
        String input = "--model llama3.2-1b-q4_0.gguf --prompt \"Print 5 emojis\" --stream=false";
        List<Options.CommandLineOption> options = Options.parseCommandLine(input);
        assertEquals(3, options.size());
        assertEquals("--model", options.get(0).name());
        assertEquals("llama3.2-1b-q4_0.gguf", options.get(0).value());
        assertEquals("--prompt", options.get(1).name());
        assertEquals("Print 5 emojis", options.get(1).value());
        assertEquals("--stream", options.get(2).name());
        assertEquals("false", options.get(2).value());
    }

    @Test
    public void testParseCommandLineWithChatAndMaxTokens() {
        String input = "-m /home/petr/.models/Llama-3.2-1B-Instruct-Q4_0.gguf --chat -n 2048";
        List<Options.CommandLineOption> options = Options.parseCommandLine(input);
        assertEquals(3, options.size());
        assertEquals("-m", options.get(0).name());
        assertEquals("/home/petr/.models/Llama-3.2-1B-Instruct-Q4_0.gguf", options.get(0).value());
        assertEquals("--chat", options.get(1).name());
        assertEquals("", options.get(1).value());
        assertEquals("-n", options.get(2).name());
        assertEquals("2048", options.get(2).value());
    }

    @Test
    public void testParseCommandLineWithPromptAndStreamFalse() {
        String input = "-m /home/petr/.models/Llama-3.2-1B-Instruct-Q4_0.gguf --prompt \"Print 5 emojis\" --stream=false";
        List<Options.CommandLineOption> options = Options.parseCommandLine(input);
        assertEquals(3, options.size());
        assertEquals("-m", options.get(0).name());
        assertEquals("/home/petr/.models/Llama-3.2-1B-Instruct-Q4_0.gguf", options.get(0).value());
        assertEquals("--prompt", options.get(1).name());
        assertEquals("Print 5 emojis", options.get(1).value());
        assertEquals("--stream", options.get(2).name());
        assertEquals("false", options.get(2).value());
    }
}
