package io.github.tomhula.jecnaapi.java;

import kotlin.coroutines.Continuation;
import kotlin.coroutines.EmptyCoroutineContext;
import kotlinx.coroutines.BuildersKt;
import kotlinx.coroutines.CoroutineStart;
import kotlinx.coroutines.GlobalScope;
import kotlinx.coroutines.future.FutureKt;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class Java
{
    /**
     * Converts a Kotlin suspend function to a Java {@link CompletableFuture}.
     *
     * @param block The Kotlin suspend function. Provides a {@link Continuation} as a parameter.
     * @return The {@link CompletableFuture} that will be completed when the Kotlin suspend function is finished.
     */
    public static <T> CompletableFuture<T> suspendToFuture(Function<? super Continuation<? super T>, ?> block)
    {
        return FutureKt.future(
                GlobalScope.INSTANCE,
                GlobalScope.INSTANCE.getCoroutineContext(),
                CoroutineStart.DEFAULT,
                (scope, continuation) -> block.apply(continuation)
        );
    }

    /**
     * Runs a Kotlin suspend function in a blocking manner.
     *
     * @param block The Kotlin suspend function. Provides a {@link Continuation} as a parameter.
     * @return The result of the Kotlin suspend function.
     */
    public static <T> T runBlocking(Function<? super Continuation<? super T>, ?> block)
    {
        try
        {
            return BuildersKt.runBlocking(
                    EmptyCoroutineContext.INSTANCE,
                    (scope, continuation) -> block.apply(continuation)
            );
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
