package com.duoc.batch.advanced;

import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.file.FlatFileParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CustomSkipPolicy implements SkipPolicy {
    private static final Logger logger = LoggerFactory.getLogger(CustomSkipPolicy.class);
    private static final int MAX_SKIPS = Integer.MAX_VALUE;

    @Override
    public boolean shouldSkip(Throwable t, long skipCount) {
        if ((t instanceof FlatFileParseException || t instanceof NumberFormatException || t instanceof com.duoc.batch.model.InvalidDataException || t instanceof org.springframework.dao.DuplicateKeyException) && skipCount < MAX_SKIPS) {
            logger.warn("[SKIP] Línea omitida por error: {}", t.getMessage(), t);
            return true;
        }
        return false;
    }
}
