package org.schabi.newpipe.extractor.utils;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ScriptableObject;

/**
 * Utility class to help run JavaScript code.
 */
public final class JavaScript {

    private JavaScript() {
    }

    /**
     * Compile JavaScript code and throw an exception if it fails.
     *
     * @param function the JavaScript code to compile
     */
    public static void compileOrThrow(final String function) {
        try {
            final Context context = Context.enter();
            context.setOptimizationLevel(-1);

            // If it doesn't compile it throws an exception here
            context.compileString(function, null, 1, null);
        } finally {
            Context.exit();
        }
    }

    /**
     * Run a JavaScript function with the given arguments.
     *
     * @param function the JavaScript code containing the function
     * @param functionName the name of the function to run
     * @param parameters the arguments to pass to the function
     * @return the result of the function call
     */
    public static String run(final String function,
                             final String functionName,
                             final String... parameters) {
        try {
            final Context context = Context.enter();
            context.setOptimizationLevel(-1);
            final ScriptableObject scope = context.initSafeStandardObjects();

            context.evaluateString(scope, function, functionName, 1, null);
            final Function jsFunction = (Function) scope.get(functionName, scope);
            final Object result = jsFunction.call(context, scope, scope, parameters);
            return result.toString();
        } finally {
            Context.exit();
        }
    }
}
