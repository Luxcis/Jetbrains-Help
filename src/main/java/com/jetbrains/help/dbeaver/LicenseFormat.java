package com.jetbrains.help.dbeaver;

import lombok.Getter;

/**
 * @author Zhuang
 * @since 2024/5/29
 */
@Getter
public enum LicenseFormat {
    STANDARD((byte) 0, 218, "Initial basic license format"),
    EXTENDED((byte) 1, 238, "Extended format with owner email and corporate license info"),
    ADVANCED((byte) 2, 490, "Advanced format for role-based licenses");

    private final byte id;
    private final int encryptedLength;
    private final String description;

    LicenseFormat(byte id, int encryptedLength, String description) {
        this.id = id;
        this.encryptedLength = encryptedLength;
        this.description = description;
    }

}
