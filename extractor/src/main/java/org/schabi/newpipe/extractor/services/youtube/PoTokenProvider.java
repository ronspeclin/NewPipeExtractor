package org.schabi.newpipe.extractor.services.youtube;

import javax.annotation.Nullable;

/**
 * Interface to provide {@code poToken}s to YouTube player requests.
 *
 * <p>
 * On some major clients, YouTube requires that the integrity of the device passes some checks to
 * allow playback.
 * </p>
 *
 * <p>
 * These checks involve running codes to verify the integrity and using their result to generate
 * one or multiple {@code poToken}(s) (which stands for proof of origin token(s)).
 * </p>
 *
 * <p>
 * These tokens may have a role in triggering the sign in requirement.
 * </p>
 *
 * <p>
 * If an implementation does not want to return a {@code poToken} for a specific client, it <b>must
 * return {@code null}</b>.
 * </p>
 *
 * <p>
 * <b>Implementations of this interface are expected to be thread-safe, as they may be accessed by
 * multiple threads.</b>
 * </p>
 */
public interface PoTokenProvider {

    /**
     * Get a {@link PoTokenResult} specific to the desktop website, a.k.a. the WEB InnerTube client.
     *
     * @return a {@link PoTokenResult} specific to the WEB InnerTube client
     */
    @Nullable
    PoTokenResult getWebClientPoToken(String videoId);

    /**
     * Get a {@link PoTokenResult} specific to the Android app, a.k.a. the ANDROID InnerTube client.
     *
     * @return a {@link PoTokenResult} specific to the ANDROID InnerTube client
     */
    @Nullable
    PoTokenResult getAndroidClientPoToken(String videoId);

    /**
     * Get a {@link PoTokenResult} specific to the iOS app, a.k.a. the IOS InnerTube client.
     *
     * @return a {@link PoTokenResult} specific to the IOS InnerTube client
     */
    @Nullable
    PoTokenResult getIosClientPoToken(String videoId);
} 