package org.mo.model;

import lombok.Value;

/**
 * Generic class for attachment
 *
 */
@Value
public class Attachment<T> {

    private T attachment;
}
