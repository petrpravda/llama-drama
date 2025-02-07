package org.llamadrama.core;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public record Options(Path modelPath, String prompt, String systemPrompt, boolean interactive,
                      float temperature, float topp, long seed, int maxTokens, boolean stream, boolean echo) {

    public static final int DEFAULT_MAX_TOKENS = 512;

    private static void displayHelp() {
        System.out.println("Usage: java Llama3 [options]");
        System.out.println("Options:");
        System.out.println("  --model, -m <path>         Path to model file");
        System.out.println("  --prompt, -p <string>      Input prompt");
        System.out.println("  --system-prompt, -sp <string> System prompt");
        System.out.println("  --temperature, -temp <float> Temperature setting");
        System.out.println("  --top-p <float>            Top-p sampling value");
        System.out.println("  --seed <long>              Random seed");
        System.out.println("  --max-tokens, -n <int>     Max tokens");
        System.out.println("  --stream <boolean>         Enable streaming");
        System.out.println("  --echo <boolean>           Echo output");
        System.out.println("  --interactive, --chat, -i  Run in chat mode");
        System.out.println("  --instruct                 Run in instruct mode");
        System.out.println("  --help                     Display this help message");
    }

    static Options parseOptions(String[] args) {
        //List<String> argList = Arrays.asList(args);

        Map<String, Function<String, Object>> parsers = new LinkedHashMap<>();
        parsers.put("--model", Paths::get);
        parsers.put("-m", Paths::get);
        parsers.put("--prompt", s -> s);
        parsers.put("-p", s -> s);
        parsers.put("--system-prompt", s -> s);
        parsers.put("-sp", s -> s);
        parsers.put("--temperature", Float::parseFloat);
        parsers.put("-temp", Float::parseFloat);
        parsers.put("--top-p", Float::parseFloat);
        parsers.put("--seed", Long::parseLong);
        parsers.put("--max-tokens", Integer::parseInt);
        parsers.put("-n", Integer::parseInt);
        parsers.put("--stream", Boolean::parseBoolean);
        parsers.put("--echo", Boolean::parseBoolean);

        Set<String> flags = Set.of("--interactive", "--chat", "-i", "--instruct");

//        Map<String, Object> parsed = new HashMap<>();
//        for (int i = 0; i < args.length; i++) {
//            String arg = args[i];
//            if (parsers.containsKey(arg)) {
//                if (i + 1 < args.length && !args[i + 1].startsWith("--")) {
//                    parsed.put(arg, parsers.get(arg).apply(args[i + 1]));
//                    i++; // Skip next argument as it is consumed
//                }
//            } else if (flags.contains(arg)) {
//                parsed.put(arg, true);
//            }
//        }

        Map<String, Object> parsed = StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(new Iterator<Map.Entry<String, Object>>() {
                    private int i = 0;

                    @Override
                    public boolean hasNext() {
                        return i < args.length;
                    }

                    @Override
                    public Map.Entry<String, Object> next() {
                        String arg = args[i++];
                        if (parsers.containsKey(arg) && i < args.length && !args[i].startsWith("--")) {
                            return Map.entry(arg, parsers.get(arg).apply(args[i++]));
                        } else if (flags.contains(arg)) {
                            return Map.entry(arg, true);
                        }
                        return null;
                    }
                }, Spliterator.ORDERED), false)
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return new Options(
                (Path) parsed.getOrDefault("--model", parsed.get("-m")),
                (String) parsed.getOrDefault("--prompt", null),
                (String) parsed.getOrDefault("--system-prompt", null),
                parsed.containsKey("--interactive") || parsed.containsKey("--chat") || parsed.containsKey("-i") || parsed.containsKey("--instruct"),
                (Float) parsed.getOrDefault("--temperature", 1.0f),
                (Float) parsed.getOrDefault("--top-p", 1.0f),
                (Long) parsed.getOrDefault("--seed", 42L),
                (Integer) parsed.getOrDefault("--max-tokens", DEFAULT_MAX_TOKENS),
                (Boolean) parsed.getOrDefault("--stream", true),
                (Boolean) parsed.getOrDefault("--echo", false)
        );
    }

    private static String nextArg(List<String> args, String key) {
        int index = args.indexOf(key);
        return (index != -1 && index + 1 < args.size() && !args.get(index + 1).startsWith("--"))
                ? args.get(index + 1)
                : "true";  // Defaults for flags
    }
}
