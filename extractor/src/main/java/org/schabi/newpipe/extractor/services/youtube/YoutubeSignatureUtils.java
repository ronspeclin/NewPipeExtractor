package org.schabi.newpipe.extractor.services.youtube;

import org.schabi.newpipe.extractor.exceptions.ParsingException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to help extract signature deobfuscation code from YouTube's JavaScript player.
 */
public final class YoutubeSignatureUtils {

    private static final Pattern STS_PATTERN = Pattern.compile("signatureTimestamp[=:](\\d+)");
    private static final Pattern DEOBFUSCATION_FUNC_NAME_PATTERN = Pattern.compile("\\b[cs]s\\s*&&\\s*[adf]\\.set\\([^,]+\\s*,\\s*encodeURIComponent\\s*\\(\\s*([a-zA-Z0-9$]+)\\(");
    private static final Pattern DEOBFUSCATION_FUNC_BODY_PATTERN = Pattern.compile("(?!h\\.)" + "([\\w$]*)" + "=function\\(\\w+\\)\\{[a-z=\\.\\(\\\"\\)]*;return [a-zA-Z0-9$]*\\.reverse\\(\\)\\}");

    private YoutubeSignatureUtils() {
    }

    /**
     * Extract the signature timestamp from the JavaScript player code.
     *
     * @param playerCode the JavaScript player code
     * @return the signature timestamp
     * @throws ParsingException if the timestamp could not be found
     */
    public static String getSignatureTimestamp(final String playerCode) throws ParsingException {
        final Matcher matcher = STS_PATTERN.matcher(playerCode);
        if (!matcher.find()) {
            throw new ParsingException("Could not find signature timestamp pattern");
        }
        return matcher.group(1);
    }

    /**
     * Extract the deobfuscation code from the JavaScript player code.
     *
     * @param playerCode the JavaScript player code
     * @return the deobfuscation code
     * @throws ParsingException if the deobfuscation code could not be found
     */
    public static String getDeobfuscationCode(final String playerCode) throws ParsingException {
        // Find the name of the function that deobfuscates signatures
        final Matcher nameMatcher = DEOBFUSCATION_FUNC_NAME_PATTERN.matcher(playerCode);
        if (!nameMatcher.find()) {
            throw new ParsingException("Could not find deobfuscation function name pattern");
        }
        final String funcName = nameMatcher.group(1);

        // Find the function body
        final Pattern funcBodyPattern = Pattern.compile(
                "(?:function\\s+" + Pattern.quote(funcName) + "|[{;,]\\s*" + Pattern.quote(funcName) + "\\s*=\\s*function|var\\s+" + Pattern.quote(funcName) + "\\s*=\\s*function)\\s*\\([^)]\\)\\s*\\{[^}]+\\}");
        final Matcher bodyMatcher = funcBodyPattern.matcher(playerCode);
        if (!bodyMatcher.find()) {
            throw new ParsingException("Could not find deobfuscation function body pattern");
        }

        return bodyMatcher.group();
    }
} 