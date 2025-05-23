package com.jetbrains.help.context;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.crypto.PemUtil;
import cn.hutool.crypto.SecureUtil;
import com.jetbrains.help.util.FileTools;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.io.File;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;

@Slf4j(topic = "证书上下文")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CertificateContextHolder {

    private static final String ROOT_KEY_FILE_NAME = "external/certificate/root.key";
    private static final String PRIVATE_KEY_FILE_NAME = "external/certificate/private.key";
    private static final String PUBLIC_KEY_FILE_NAME = "external/certificate/public.key";
    private static final String CET_FILE_NAME = "external/certificate/ca.crt";

    private static final String DBEAVER_ROOT_KEY_FILE_NAME = "external/certificate/dbeaverRoot.key";
    private static final String DBEAVER_PUBLIC_KEY_FILE_NAME = "external/certificate/dbeaverPublic.key";
    private static final String DBEAVER_PRIVATE_KEY_FILE_NAME = "external/certificate/dbeaverPrivate.key";

    private static File rootKeyFile;
    private static File privateKeyFile;
    private static File publicKeyFile;
    private static File crtFile;
    private static File dbeaverRootKeyFile;
    private static File dbeaverPublicKeyFile;
    private static File dbeaverPrivateKeyFile;

    public static void init() {
        log.info("初始化中...");
        rootKeyFile = FileTools.getFileOrCreat(ROOT_KEY_FILE_NAME);
        if (!FileTools.fileExists(PRIVATE_KEY_FILE_NAME)
                || !FileTools.fileExists(PUBLIC_KEY_FILE_NAME)
                || !FileTools.fileExists(CET_FILE_NAME)) {
            log.info("证书生成中...");
            generateCertificate();
            generateDbeaverKey();
            log.info("证书生成成功!");
        } else {
            privateKeyFile = FileTools.getFileOrCreat(PRIVATE_KEY_FILE_NAME);
            publicKeyFile = FileTools.getFileOrCreat(PUBLIC_KEY_FILE_NAME);
            crtFile = FileTools.getFileOrCreat(CET_FILE_NAME);
            dbeaverPublicKeyFile = FileTools.getFileOrCreat(DBEAVER_PUBLIC_KEY_FILE_NAME);
            dbeaverPrivateKeyFile = FileTools.getFileOrCreat(DBEAVER_PRIVATE_KEY_FILE_NAME);
            dbeaverRootKeyFile = FileTools.getFileOrCreat(DBEAVER_ROOT_KEY_FILE_NAME);
        }
        log.info("初始化成功!");
    }

    public static File rootKeyFile() {
        return CertificateContextHolder.rootKeyFile;
    }

    public static File privateKeyFile() {
        return CertificateContextHolder.privateKeyFile;
    }

    public static File publicKeyFile() {
        return CertificateContextHolder.publicKeyFile;
    }

    public static File crtFile() {
        return CertificateContextHolder.crtFile;
    }

    public static File dbeaverRootKeyFile() {
        return CertificateContextHolder.dbeaverRootKeyFile;
    }

    public static File dbeaverPublicKeyFile() {
        return CertificateContextHolder.dbeaverPublicKeyFile;
    }

    public static File dbeaverPrivateKeyFile() {
        return CertificateContextHolder.dbeaverPrivateKeyFile;
    }

    public static void generateCertificate() {
        KeyPair keyPair = SecureUtil.generateKeyPair("RSA", 4096);
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();
        privateKeyFile = FileTools.getFileOrCreat(PRIVATE_KEY_FILE_NAME);
        PemUtil.writePemObject("PRIVATE KEY", privateKey.getEncoded(), FileUtil.getWriter(privateKeyFile, StandardCharsets.UTF_8, false));
        publicKeyFile = FileTools.getFileOrCreat(PUBLIC_KEY_FILE_NAME);
        PemUtil.writePemObject("PUBLIC KEY", publicKey.getEncoded(), FileUtil.getWriter(publicKeyFile, StandardCharsets.UTF_8, false));
        JcaX509v3CertificateBuilder certificateBuilder = new JcaX509v3CertificateBuilder(
                new X500Name("CN=JetProfile CA"),
                BigInteger.valueOf(System.currentTimeMillis()),
                DateUtil.yesterday(),
                DateUtil.date().offset(DateField.YEAR, 100),
                new X500Name("CN=Jetbrains-Help"),
                SubjectPublicKeyInfo.getInstance(publicKey.getEncoded()));
        try {
            ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA").build(privateKey);
            Certificate certificate = new JcaX509CertificateConverter().setProvider("BC").getCertificate(certificateBuilder.build(signer));
            crtFile = FileTools.getFileOrCreat(CET_FILE_NAME);
            PemUtil.writePemObject("CERTIFICATE", certificate.getEncoded(), FileUtil.getWriter(crtFile, StandardCharsets.UTF_8, false));
        } catch (OperatorCreationException e) {
            throw new IllegalArgumentException("证书运算符创建异常!", e);
        } catch (CertificateEncodingException e) {
            throw new IllegalArgumentException("证书编码异常", e);
        } catch (CertificateException e) {
            throw new IllegalArgumentException("证书读取异常", e);
        }
    }

    public static void generateDbeaverKey() {
        KeyPair keyPair = SecureUtil.generateKeyPair("RSA", 2048);
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();
        dbeaverPublicKeyFile = FileTools.getFileOrCreat(DBEAVER_PUBLIC_KEY_FILE_NAME);
        PemUtil.writePemObject("PUBLIC KEY", publicKey.getEncoded(), FileUtil.getWriter(dbeaverPublicKeyFile, StandardCharsets.UTF_8, false));
        dbeaverPrivateKeyFile = FileTools.getFileOrCreat(DBEAVER_PRIVATE_KEY_FILE_NAME);
        PemUtil.writePemObject("PRIVATE KEY", privateKey.getEncoded(), FileUtil.getWriter(dbeaverPrivateKeyFile, StandardCharsets.UTF_8, false));
        dbeaverRootKeyFile = FileTools.getFileOrCreat(DBEAVER_ROOT_KEY_FILE_NAME);
    }
}
