package org.llamadrama.core;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    public record CommandLineOption(String name, String value) {
    }

    public static List<CommandLineOption> parseCommandLine(String input) {
        String[] tokens = input.split(" (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        List<CommandLineOption> options = new ArrayList<>();
        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].startsWith("--") || tokens[i].startsWith("-")) {
                String name = tokens[i];
                String value = "";
                if (name.contains("=")) {
                    String[] parts = name.split("=", 2);
                    name = parts[0];
                    value = parts[1];
                } else if (i + 1 < tokens.length && !(tokens[i + 1].startsWith("--") || tokens[i + 1].startsWith("-"))) {
                    value = tokens[++i].replaceAll("^\"|\"$", "");
                }
                options.add(new CommandLineOption(name, value));
            }
        }
        return options;
    }

    static Options parseOptions(String[] args) {
        String input = Arrays.stream(args)
                .map(arg -> arg.contains(" ") ? "\"" + arg + "\"" : arg)
                .collect(Collectors.joining(" "));

        List<CommandLineOption> options = parseCommandLine(input);

        Map<String, String> optionMap = options.stream()
                .collect(Collectors.toMap(CommandLineOption::name, CommandLineOption::value));

        Function<OptParam, Object> getValue = param -> param.names.stream()
                .filter(optionMap::containsKey)
                .findFirst()
                .map(name -> param.parser.apply(optionMap.get(name)))
                .orElse(param.defaultValue);

        return new Options(
                (Path) getValue.apply(OptParam.MODEL),
                (String) getValue.apply(OptParam.PROMPT),
                (String) getValue.apply(OptParam.SYSTEM_PROMPT),
                optionMap.keySet().stream().anyMatch(OptParam.INTERACTIVE.names::contains),
                (Float) getValue.apply(OptParam.TEMPERATURE),
                (Float) getValue.apply(OptParam.TOP_P),
                (Long) getValue.apply(OptParam.SEED),
                (Integer) getValue.apply(OptParam.MAX_TOKENS),
                (Boolean) getValue.apply(OptParam.STREAM),
                (Boolean) getValue.apply(OptParam.ECHO)
        );
    }

    public enum OptParam {
        MODEL("--model,-m", "<path>", "Path to model file", Paths::get, null),
        PROMPT("--prompt,-p", "<string>", "Input prompt", s -> s, null),
        SYSTEM_PROMPT("--system-prompt,-sp", "<string>", "System prompt", s -> s, null),
        TEMPERATURE("--temperature,-temp", "<float>", "Temperature setting", Float::parseFloat, 0.1f),
        TOP_P("--top-p", "<float>", "Top-p sampling value", Float::parseFloat, 0.95f),
        SEED("--seed", "<long>", "Random seed", Long::parseLong, 42L),
        MAX_TOKENS("--max-tokens,-n", "<int>", "Max tokens", Integer::parseInt, DEFAULT_MAX_TOKENS),
        STREAM("--stream", "<boolean>", "Enable streaming", Boolean::parseBoolean, true),
        ECHO("--echo", "<boolean>", "Echo output", Boolean::parseBoolean, false),
        INTERACTIVE("--interactive,--chat,-i", "", "Run in chat mode", s -> true, true),
        INSTRUCT("--instruct", "", "Run in instruct mode", s -> true, true);

        final Set<String> names;
        final String placeholder;
        final String description;
        final Function<String, Object> parser;
        final Object defaultValue;

        OptParam(String names, String placeholder, String description, Function<String, Object> parser, Object defaultValue) {
            this.names = new LinkedHashSet<>(Arrays.asList(names.split(",")));
            this.placeholder = placeholder;
            this.description = description;
            this.parser = parser;
            this.defaultValue = defaultValue;
        }

        public String formatHelp() {
            return STR."\{names.stream()
                    .map(name -> name + (placeholder.isEmpty() ? "" : " " + placeholder))
                    .collect(Collectors.joining(", "))} \{description}";
        }
    }
}
