package dev.rollczi.litecommands.argument.parser.input;

import dev.rollczi.litecommands.argument.Argument;
import dev.rollczi.litecommands.argument.parser.ParseResult;
import dev.rollczi.litecommands.argument.parser.Parser;
import dev.rollczi.litecommands.argument.parser.ParserSet;
import dev.rollczi.litecommands.LiteCommandsException;
import dev.rollczi.litecommands.invalidusage.InvalidUsage;
import dev.rollczi.litecommands.invocation.Invocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

class NamedTypedParseableInput implements ParseableInput<NamedTypedParseableInput.TypeMixedInputMatcher> {

    private final List<String> routes = new ArrayList<>();
    private final Map<String, Object> namedArguments = new LinkedHashMap<>();

    NamedTypedParseableInput(List<String> routes, Map<String, Object> namedArguments) {
        this.routes.addAll(routes);
        this.namedArguments.putAll(namedArguments);
    }

    @Override
    public TypeMixedInputMatcher createMatcher() {
        return new TypeMixedInputMatcher();
    }

    @Override
    public List<String> asList() {
        List<String> rawArgs = new ArrayList<>();

        namedArguments.forEach((named, object) -> {
            rawArgs.add(named);
            rawArgs.add(object.toString());
        });

        return Collections.unmodifiableList(rawArgs);
    }

    class TypeMixedInputMatcher implements ParseableInputMatcher<TypeMixedInputMatcher> {

        private int routePosition = 0;
        private final Set<String> consumedArguments = new HashSet<>();

        public TypeMixedInputMatcher() {}

        public TypeMixedInputMatcher(int routePosition) {
            this.routePosition = routePosition;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <SENDER, PARSED> ParseResult<PARSED> nextArgument(Invocation<SENDER> invocation, Argument<PARSED> argument, ParserSet<SENDER, PARSED> parserSet) {
            Object input = namedArguments.get(argument.getName());

            if (input == null) {
                return ParseResult.failure(InvalidUsage.Cause.MISSING_ARGUMENT);
            }

            Class<PARSED> outType = argument.getWrapperFormat().getParsedType();
            consumedArguments.add(argument.getName());

            if (outType.isAssignableFrom(input.getClass())) {
                return ParseResult.success((PARSED) input);
            }

            return this.parseInput(invocation, argument, parserSet, input);
        }

        @SuppressWarnings("unchecked")
        private <SENDER, INPUT, OUT> ParseResult<OUT> parseInput(Invocation<SENDER> invocation, Argument<OUT> argument, ParserSet<SENDER, OUT> parserSet, INPUT input) {
            Class<INPUT> inputType = (Class<INPUT>) input.getClass();
            Parser<SENDER, INPUT, OUT> parser = parserSet.getParser(inputType)
                .orElseThrow(() -> new LiteCommandsException("No parser for input type " + inputType.getName()));

            return parser.parse(invocation, argument, input);
        }

        @Override
        public boolean hasNextRoute() {
            return routePosition < routes.size();
        }

        @Override
        public String showNextRoute() {
            return routes.get(routePosition);
        }

        @Override
        public String nextRoute() {
            return routes.get(routePosition++);
        }

        @Override
        public TypeMixedInputMatcher copy() {
            return new TypeMixedInputMatcher(routePosition);
        }

        @Override
        public EndResult endMatch() {
            if (consumedArguments.size() < namedArguments.size()) {
                return EndResult.failed(InvalidUsage.Cause.TOO_MANY_ARGUMENTS);
            }

            return EndResult.success();
        }

    }

}
