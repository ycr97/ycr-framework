package com.ycr.framework.encrypt.context;

import com.ycr.framework.encrypt.handler.EncryptHandler;

/**
 * 加解密处理器持有者。
 *
 * @author ycr
 */
public final class EncryptHandlerHolder {

    private static volatile EncryptHandler encryptHandler;

    private EncryptHandlerHolder() {
    }

    public static void set(EncryptHandler handler) {
        if (handler == null) {
            throw new IllegalArgumentException("EncryptHandler must not be null");
        }
        encryptHandler = handler;
    }

    public static EncryptHandler getRequired() {
        EncryptHandler handler = encryptHandler;
        if (handler == null) {
            throw new IllegalStateException("EncryptHandler has not been initialized");
        }
        return handler;
    }

    public static void clear() {
        encryptHandler = null;
    }
}
