package com.github.cao.awa.kora.entrypoint.exception;

public class KoraEntrypointStageFailedException extends RuntimeException {
    public final String stage;
    public final Throwable cause;

    public KoraEntrypointStageFailedException(String stage, Throwable cause){
        this.stage = stage;
        this.cause = cause;
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
