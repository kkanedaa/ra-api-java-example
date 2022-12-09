package ch.swisssign.ra.app.certificate;

import java.io.IOException;
import java.io.StringWriter;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.ExtensionsGenerator;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.bouncycastle.util.io.pem.PemObject;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public final class CertificateSigningRequestGenerator {

  private static final String CERTIFICATE_REQUEST = "CERTIFICATE REQUEST";

  public String newRequest(final List<String> domains)
      throws IOException, NoSuchAlgorithmException, NoSuchProviderException,
          OperatorCreationException {

    Security.addProvider(new BouncyCastleProvider());

    final var keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");

    log.info("Generate private key, it will take a while");
    keyPairGenerator.initialize(4096, SecureRandom.getInstanceStrong());

    final var keyPair = keyPairGenerator.generateKeyPair();

    log.info("Private key has been generated");

    final var nameBuilder = new X500NameBuilder(X500Name.getDefaultStyle());
    nameBuilder.addRDN(BCStyle.CN, domains.get(0));

    final List<GeneralName> subjectAltNames = new ArrayList<>(domains.size());
    domains.forEach(domain -> subjectAltNames.add(new GeneralName(GeneralName.dNSName, domain)));

    final var subjectAltName = new GeneralNames(subjectAltNames.toArray(new GeneralName[0]));

    final ExtensionsGenerator extGen = new ExtensionsGenerator();
    extGen.addExtension(Extension.subjectAlternativeName, false, subjectAltName.toASN1Primitive());

    final PKCS10CertificationRequestBuilder p10Builder =
        new JcaPKCS10CertificationRequestBuilder(nameBuilder.build(), keyPair.getPublic());

    p10Builder.addAttribute(PKCSObjectIdentifiers.pkcs_9_at_extensionRequest, extGen.generate());

    final var csBuilder = new JcaContentSignerBuilder("SHA256withRSA");

    final ContentSigner signer = csBuilder.build(keyPair.getPrivate());

    final PKCS10CertificationRequest request = p10Builder.build(signer);

    final var pemObject = new PemObject(CERTIFICATE_REQUEST, request.getEncoded());

    final var stringWriter = new StringWriter();

    final var pemWriter = new JcaPEMWriter(stringWriter);

    pemWriter.writeObject(pemObject);
    pemWriter.close();

    stringWriter.close();

    return stringWriter.toString();
  }
}
