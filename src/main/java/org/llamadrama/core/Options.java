package org.llamadrama.core;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public record Options(Path modelPath, String prompt, String systemPrompt, boolean interactive,
                      float temperature, float topp, long seed, int maxTokens, boolean stream, boolean echo) {

    public static final int DEFAULT_MAX_TOKENS = 512;

    private static void displayHelp() {
        System.out.println("Usage: java Llama3 [options]");
        System.out.println("Options:");
        for (OptParam param : OptParam.values()) {
            System.out.println("  " + param.formatHelp());
        }
        System.out.println("  --help                     Display this help message");
    }

    public static record CommandLineOption(String name, String value) {
    }

    public static List<CommandLineOption> parseCommandLine(String input) {
        String[] tokens = input.split(" (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        return IntStream.range(0, tokens.length)
                .filter(i -> tokens[i].startsWith("--"))
                .mapToObj(i -> {
                    String[] parts = tokens[i].split("=", 2);
                    String name = parts[0];
                    String value = parts.length > 1 ? parts[1] :
                            (i + 1 < tokens.length && !tokens[i + 1].startsWith("--")) ? tokens[++i].replaceAll("^\"|\"$", "") : "";
                    return new CommandLineOption(name, value);
                })
                .collect(Collectors.toList());
    }

    static Options parseOptions(String[] args) {
        return null;
//        Map<String, Object> parsed = IntStream.range(0, args.length)
//                .mapToObj(i -> {
//                    String arg = args[i];
//                    if (parsers.containsKey(arg) && i < args.length - 1 && !args[i + 1].startsWith("--")) {
//                        return Map.entry(arg, parsers.get(arg).apply(args[i + 1]));
//                    } else if (flags.contains(arg)) {
//                        return Map.entry(arg, true);
//                    }
//                    return null;
//                })
//                .filter(Objects::nonNull)
//                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
//
//        return new Options(
//                (Path) parsed.getOrDefault("--model", parsed.get("-m")),
//                (String) parsed.getOrDefault("--prompt", parsed.get("-p")),
//                (String) parsed.getOrDefault("--system-prompt", parsed.get("-sp")),
//                parsed.containsKey("--interactive") || parsed.containsKey("--chat") || parsed.containsKey("-i") || parsed.containsKey("--instruct"),
//                (Float) parsed.getOrDefault("--temperature", parsed.getOrDefault("-temp", 0.1f)),
//                (Float) parsed.getOrDefault("--top-p", 0.95f),
//                (Long) parsed.getOrDefault("--seed", 42L),
//                (Integer) parsed.getOrDefault("--max-tokens", DEFAULT_MAX_TOKENS),
//                (Boolean) parsed.getOrDefault("--stream", true),
//                (Boolean) parsed.getOrDefault("--echo", false)
//        );
    }

    public enum OptParam {
        MODEL("--model,-m", "<path>", "Path to model file", Paths::get),
        PROMPT("--prompt,-p", "<string>", "Input prompt", s -> s),
        SYSTEM_PROMPT("--system-prompt,-sp", "<string>", "System prompt", s -> s),
        TEMPERATURE("--temperature,-temp", "<float>", "Temperature setting", Float::parseFloat),
        TOP_P("--top-p", "<float>", "Top-p sampling value", Float::parseFloat),
        SEED("--seed", "<long>", "Random seed", Long::parseLong),
        MAX_TOKENS("--max-tokens,-n", "<int>", "Max tokens", Integer::parseInt),
        STREAM("--stream", "<boolean>", "Enable streaming", Boolean::parseBoolean),
        ECHO("--echo", "<boolean>", "Echo output", Boolean::parseBoolean),
        INTERACTIVE("--interactive,--chat,-i", "", "Run in chat mode", s -> true, true),
        INSTRUCT("--instruct", "", "Run in instruct mode", s -> true, true);

        private final Set<String> names;
        private final String placeholder;
        private final String description;
        private final Function<String, Object> parser;
        private final boolean flag;

        OptParam(String names, String placeholder, String description, Function<String, Object> parser) {
            this(names, placeholder, description, parser, false);
        }

        OptParam(String names, String placeholder, String description, Function<String, Object> parser, boolean flag) {
            this.names = new LinkedHashSet<>(Arrays.asList(names.split(",")));
            this.placeholder = placeholder;
            this.description = description;
            this.parser = parser;
            this.flag = flag;
        }

        public Set<String> names() {
            return names;
        }

        public String formatHelp() {
            return STR."\{names.stream()
                    .map(name -> name + (placeholder.isEmpty() ? "" : " " + placeholder))
                    .collect(Collectors.joining(", "))} \{description}";
        }

        public Function<String, Object> parser() {
            return parser;
        }

        public boolean isFlag() {
            return flag;
        }
    }
}
