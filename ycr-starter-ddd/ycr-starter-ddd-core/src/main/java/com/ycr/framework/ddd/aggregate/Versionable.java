package com.ycr.framework.ddd.aggregate;

/**
 * The aggregate use version to manage optimistic lock
 *
 * @author meixuesong
 */
public interface Versionable {
    int NEW_VERSION = 0;
    int getVersion();
}
