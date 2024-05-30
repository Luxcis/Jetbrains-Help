package com.jetbrains.help.dbeaver;

import lombok.Getter;

/**
 * @author Zhuang
 * @since 2024/5/29
 */
@Getter
public enum LicenseType {
    STANDARD('S', "Yearly subscription", true, true),
    YEAR_UPDATE('Y', "Perpetual", false, false),
    YEAR_CORPORATE('C', "Corporate", false, false),
    ULTIMATE('U', "Ultimate", false, false),
    LIMITED('L', "Limited", true, true),
    PARTNER('P', "Technical partner", false, false),
    TRIAL('T', "Trial", true, true),
    ACADEMIC('A', "Academic", true, true),
    TEAM('M', "Yearly subscription (Team)", true, true),
    CUSTOM('X', "Custom", false, false);

    private final char id;
    private final String displayName;
    private boolean isExtendable;
    private boolean needsEndTime;

    LicenseType(char id, String displayName, boolean isExtendable, boolean needsEndTime) {
        this.id = id;
        this.displayName = displayName;
        this.isExtendable = isExtendable;
        this.needsEndTime = needsEndTime;
    }
}
