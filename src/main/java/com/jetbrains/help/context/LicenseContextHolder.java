package com.jetbrains.help.context;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.PemUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.SignUtil;
import cn.hutool.crypto.asymmetric.Sign;
import cn.hutool.json.JSONUtil;
import com.jetbrains.help.dbeaver.License;
import com.jetbrains.help.dbeaver.LicenseType;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jcajce.provider.digest.Keccak;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static cn.hutool.crypto.asymmetric.SignAlgorithm.SHA1withRSA;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LicenseContextHolder {

    public static String generateJetbrainsLicense(String licensesName, String assigneeName, String expiryDate, Set<String> productCodeSet) {
        String licenseId = IdUtil.fastSimpleUUID();
        List<Product> products = productCodeSet.stream()
                .map(productCode -> new Product()
                        .setCode(productCode)
                        .setFallbackDate(expiryDate)
                        .setPaidUpTo(expiryDate))
                .toList();
        LicensePart licensePart = new LicensePart()
                .setLicenseId(licenseId)
                .setLicenseeName(licensesName)
                .setAssigneeName(assigneeName)
                .setProducts(products);
        String licensePartJson = JSONUtil.toJsonStr(licensePart);
        String licensePartBase64 = Base64.encode(licensePartJson);
        PrivateKey privateKey = PemUtil.readPemPrivateKey(IoUtil.toStream(CertificateContextHolder.privateKeyFile()));
        PublicKey publicKey = PemUtil.readPemPublicKey(IoUtil.toStream(CertificateContextHolder.publicKeyFile()));
        Certificate certificate = SecureUtil.readX509Certificate(IoUtil.toStream(CertificateContextHolder.crtFile()));
        Sign sign = SignUtil.sign(SHA1withRSA, privateKey.getEncoded(), publicKey.getEncoded());
        String signatureBase64 = Base64.encode(sign.sign(licensePartJson));
        String certBase64;
        try {
            certBase64 = Base64.encode(certificate.getEncoded());
        } catch (CertificateEncodingException e) {
            throw new IllegalArgumentException("Certificate extraction failed", e);
        }
        return CharSequenceUtil.format("{}-{}-{}-{}", licenseId, licensePartBase64, signatureBase64, certBase64);
    }

    @Data
    public static class LicensePart {

        private String licenseId;
        private String licenseeName;
        private String assigneeName;
        private List<Product> products;
        private String metadata = "0120230914PSAX000005";
    }

    @Data
    public static class Product {
        private String code;
        private String fallbackDate;
        private String paidUpTo;
    }

    @SneakyThrows
    public static String generateDbeaverLicense(String productId, String productVersion, String company, String name, String email) {
        PrivateKey privateKey = PemUtil.readPemPrivateKey(IoUtil.toStream(CertificateContextHolder.dbeaverPrivateKeyFile()));
        License license = new License("JL-0FB16-000A2GC", LicenseType.ULTIMATE, new Date(), new Date(), null, License.FLAG_UNLIMITED_SERVERS, productId, productVersion, "10000", company, name, email);
        byte[] licenseData = license.getData();
        byte[] licenseEncrypted = encrypt(licenseData, privateKey);
        return Base64.encode(licenseEncrypted);
    }

    public static byte[] encrypt(byte[] data, Key key) throws Exception {
        return cipherAsymmetric(data, key, 1);
    }

    public static byte[] cipherAsymmetric(byte[] data, Key key, int mode) throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int chunkSize = mode == 2 ? 256 : 245;
        int chunkCount = data.length / chunkSize;
        if (data.length % chunkSize > 0) {
            ++chunkCount;
        }

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");

        for (int i = 0; i < chunkCount; ++i) {
            cipher.init(mode, key);
            int offset = i * chunkSize;
            int length = chunkSize;
            if (offset + chunkSize > data.length) {
                length = data.length - chunkSize * i;
            }

            byte[] segment = Arrays.copyOfRange(data, offset, offset + length);
            byte[] segmentEncrypted = cipher.doFinal(segment);
            buffer.write(segmentEncrypted);
        }

        return buffer.toByteArray();
    }

    public static String generateFinalShellLicense(String machineCode) {
        String code = machineCode + "FF3Go(*Xvbb5s2";
        Keccak.Digest384 digest384 = new Keccak.Digest384();
        return HexUtil.encodeHexStr(digest384.digest(code.getBytes())).substring(12, 28);
    }
}
