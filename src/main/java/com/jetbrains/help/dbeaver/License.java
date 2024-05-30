package com.jetbrains.help.dbeaver;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;

/**
 * @author Zhuang
 * @since 2024/5/29
 */
public class License {
    public static final long FLAG_NONE = 0L;
    public static final long FLAG_ROLE_BASED = 1L;
    public static final long FLAG_CANCELED = 2L;
    public static final long FLAG_RESELLER = 4L;
    public static final long FLAG_SUBSCRIPTION = 8L;
    public static final long FLAG_LIMITED = 16L;
    public static final long FLAG_LIMITED_VERSION = 32L;
    public static final long FLAG_SERVER_LICENSE = 64L;
    public static final long FLAG_UNLIMITED_USERS = 256L;
    public static final long FLAG_UNLIMITED_TIME = 512L;
    public static final long FLAG_UNLIMITED_SERVERS = 1024L;
    public static final long FLAG_MULTI_INSTANCE = 2048L;

    private final String licenseId;
    private final LicenseType licenseType;
    private final Date licenseIssueTime;
    private final Date licenseStartTime;
    private final Date licenseEndTime;
    private final String productId;
    private final String productVersion;
    private final String ownerId;
    private final String ownerCompany;
    private final String ownerName;
    private long flags;
    private String ownerEmail;
    private byte yearsNumber;
    private byte reserved1;
    private short usersNumber;
    private LicenseFormat licenseFormat;

    public License(String licenseId, LicenseType licenseType, Date licenseIssueTime, Date licenseStartTime, Date licenseEndTime, long flags, String productId, String productVersion, String ownerId, String ownerCompany, String ownerName, String ownerEmail) {
        this.licenseFormat = (flags & 1L) != 0L ? LicenseFormat.ADVANCED : LicenseFormat.EXTENDED;
        this.licenseId = licenseId;
        this.licenseType = licenseType;
        this.licenseIssueTime = licenseIssueTime;
        this.licenseStartTime = licenseStartTime;
        this.licenseEndTime = licenseEndTime;
        this.flags = flags;
        this.productId = productId;
        this.productVersion = productVersion;
        this.ownerId = ownerId;
        this.ownerCompany = ownerCompany;
        this.ownerName = ownerName;
        this.ownerEmail = ownerEmail;
        this.yearsNumber = 1;
        this.reserved1 = 0;
        this.usersNumber = 1;
    }

    public byte[] getData() {
        ByteArrayOutputStream output = new ByteArrayOutputStream(this.licenseFormat.getEncryptedLength());
        output.write(this.licenseFormat.getId());
        writeStringToBuffer(output, this.licenseId, 16);
        output.write(this.licenseType.getId());
        writeDateToBuffer(output, this.licenseIssueTime);
        writeDateToBuffer(output, this.licenseStartTime);
        writeDateToBuffer(output, this.licenseEndTime);
        writeLongToBuffer(output, this.flags);
        writeStringToBuffer(output, this.productId, 16);
        writeStringToBuffer(output, this.productVersion, 8);
        writeStringToBuffer(output, this.ownerId, 16);
        writeStringToBuffer(output, this.ownerCompany, 64);
        if (this.licenseFormat == LicenseFormat.STANDARD) {
            writeStringToBuffer(output, this.ownerName, 64);
        } else {
            writeStringToBuffer(output, this.ownerName, 32);
            writeStringToBuffer(output, this.ownerEmail, 48);
            output.write(this.yearsNumber);
            output.write(this.reserved1);
            writeShortToBuffer(output, this.usersNumber);
        }

        return output.toByteArray();
    }

    public void writeStringToBuffer(ByteArrayOutputStream output, String value, int length) {
        output.writeBytes(getStringData(value, length));
    }

    public void writeDateToBuffer(ByteArrayOutputStream output, Date date) {
        long value = date == null ? 0L : date.getTime();
        writeLongToBuffer(output, value);
    }

    public byte[] getStringData(String value, int length) {
        byte[] bytes = value == null ? new byte[0] : value.getBytes(StandardCharsets.UTF_8);
        byte[] data = Arrays.copyOf(bytes, length);
        Arrays.fill(data, Math.min(bytes.length, length), length, (byte) 32);
        return data;
    }

    public void writeLongToBuffer(ByteArrayOutputStream output, long value) {
        ByteBuffer buffer = ByteBuffer.wrap(new byte[8]);
        buffer.putLong(value);
        output.writeBytes(buffer.array());
    }

    public void writeShortToBuffer(ByteArrayOutputStream output, short value) {
        ByteBuffer buffer = ByteBuffer.wrap(new byte[2]);
        buffer.putShort(value);
        output.writeBytes(buffer.array());
    }
}
