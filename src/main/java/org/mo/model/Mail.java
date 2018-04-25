package org.mo.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Singular;

import java.util.List;

/**
 * Generic class encapsulating the information of an email object
 *
 */
@Data
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class Mail<T> {

    /**
     * Mail sender
     */
    @NonNull
    private String from;

    /**
     * Mail subject
     */
    private String subject;

    /**
     * Mail content
     */
    private String body;

    /**
     * The list of attachments if any
     */
    @Singular
    private List<Attachment> attachments;

    /**
     * The original message
     */
    private T message;

}
