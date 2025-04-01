package org.schabi.newpipe.extractor.services.youtube;

import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.utils.JavaScript;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Manage the extraction and the usage of YouTube's player JavaScript needed data in the YouTube
 * service.
 *
 * <p>
 * YouTube restrict streaming their media in multiple ways by requiring their HTML5 clients to use
 * a signature timestamp, and on streaming URLs a signature deobfuscation function for some
 * contents and a throttling parameter deobfuscation one for all contents.
 * </p>
 *
 * <p>
 * This class provides access to methods which allows to get base JavaScript player's signature
 * timestamp and to deobfuscate streaming URLs' signature and/or throttling parameter of HTML5
 * clients.
 * </p>
 */
public final class YoutubeJavaScriptPlayerManager {

    @Nonnull
    private static final Map<String, String> CACHED_THROTTLING_PARAMETERS = new HashMap<>();

    private static String cachedJavaScriptPlayerCode;

    @Nullable
    private static Integer cachedSignatureTimestamp;
    @Nullable
    private static String cachedSignatureDeobfuscationFunction;
    @Nullable
    private static String cachedThrottlingDeobfuscationFunctionName;
    @Nullable
    private static String cachedThrottlingDeobfuscationFunction;

    @Nullable
    private static ParsingException throttlingDeobfFuncExtractionEx;
    @Nullable
    private static ParsingException sigDeobFuncExtractionEx;
    @Nullable
    private static ParsingException sigTimestampExtractionEx;

    private static final Map<String, String> STS_CACHE = new HashMap<>();
    private static final Map<String, String> DEOBFUSCATION_FUNC_CACHE = new HashMap<>();
    private static final Map<String, Exception> EXCEPTIONS_CACHE = new HashMap<>();

    private YoutubeJavaScriptPlayerManager() {
    }

    /**
     * Get the signature timestamp for a video.
     *
     * @param videoId the video ID
     * @return the signature timestamp
     * @throws ExtractionException if the timestamp could not be extracted
     */
    public static String getSignatureTimestamp(final String videoId)
            throws ExtractionException {
        try {
            if (STS_CACHE.containsKey(videoId)) {
                return STS_CACHE.get(videoId);
            }

            if (EXCEPTIONS_CACHE.containsKey(videoId)) {
                throw new ExtractionException("Previous extraction failed",
                        EXCEPTIONS_CACHE.get(videoId));
            }

            final String playerCode = YoutubeJavaScriptExtractor.extractJavaScriptCode(videoId);
            final String sts = YoutubeSignatureUtils.getSignatureTimestamp(playerCode);
            STS_CACHE.put(videoId, sts);
            return sts;
        } catch (final Exception e) {
            EXCEPTIONS_CACHE.put(videoId, e);
            throw new ExtractionException("Could not get signature timestamp", e);
        }
    }

    /**
     * Deobfuscate a signature.
     *
     * @param videoId the video ID
     * @param obfuscatedSignature the obfuscated signature
     * @return the deobfuscated signature
     * @throws ExtractionException if the signature could not be deobfuscated
     */
    public static String deobfuscateSignature(final String videoId,
                                            final String obfuscatedSignature)
            throws ExtractionException {
        try {
            String deobfuscationFunc = DEOBFUSCATION_FUNC_CACHE.get(videoId);

            if (deobfuscationFunc == null) {
                if (EXCEPTIONS_CACHE.containsKey(videoId)) {
                    throw new ExtractionException("Previous extraction failed",
                            EXCEPTIONS_CACHE.get(videoId));
                }

                final String playerCode = YoutubeJavaScriptExtractor.extractJavaScriptCode(videoId);
                deobfuscationFunc = YoutubeSignatureUtils.getDeobfuscationCode(playerCode);
                DEOBFUSCATION_FUNC_CACHE.put(videoId, deobfuscationFunc);
            }

            return JavaScript.run(deobfuscationFunc, "deobfuscate", obfuscatedSignature);
        } catch (final Exception e) {
            EXCEPTIONS_CACHE.put(videoId, e);
            throw new ExtractionException("Could not deobfuscate signature", e);
        }
    }

    /**
     * Clear all caches of this class.
     *
     * <p>
     * This includes:
     * <ul>
     *     <li>The base JavaScript player file</li>
     *     <li>The signature timestamp</li>
     *     <li>The signature deobfuscation function</li>
     *     <li>The throttling parameter deobfuscation function</li>
     *     <li>The throttling parameters cache</li>
     * </ul>
     * </p>
     */
    public static void clearAllCaches() {
        cachedJavaScriptPlayerCode = null;
        cachedSignatureTimestamp = null;
        cachedSignatureDeobfuscationFunction = null;
        cachedThrottlingDeobfuscationFunctionName = null;
        cachedThrottlingDeobfuscationFunction = null;
        throttlingDeobfFuncExtractionEx = null;
        sigDeobFuncExtractionEx = null;
        sigTimestampExtractionEx = null;
        CACHED_THROTTLING_PARAMETERS.clear();
        YoutubeJavaScriptExtractor.resetJavaScriptCode();
        STS_CACHE.clear();
        DEOBFUSCATION_FUNC_CACHE.clear();
        EXCEPTIONS_CACHE.clear();
    }

    /**
     * Extract the base JavaScript player file if it is not already done.
     *
     * @param videoId the video ID used to get the JavaScript base player file (an empty one can be
     *                passed, even it is not recommend in order to spoof better official YouTube
     *                clients)
     * @throws ParsingException if the extraction of the base JavaScript player file failed
     */
    private static void extractJavaScriptCodeIfNeeded(@Nonnull final String videoId)
            throws ParsingException {
        if (cachedJavaScriptPlayerCode == null) {
            cachedJavaScriptPlayerCode = YoutubeJavaScriptExtractor.extractJavaScriptCode(videoId);
        }
    }
} 