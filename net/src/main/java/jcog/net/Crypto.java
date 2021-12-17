package jcog.net;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.*;

/**
 * from https:
 *
 * https:
 */
public enum Crypto {
	;

	public static void generatePubandPrivateKeyFiles(String path, String id)
            throws NoSuchAlgorithmException, NoSuchProviderException, IOException {
        SecureRandom r = new SecureRandom();
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA", "SUN");
        keyGen.initialize(1024, r);
        KeyPair pair = keyGen.generateKeyPair();
        PrivateKey priv = pair.getPrivate();
        PublicKey pub = pair.getPublic();
        {
            FileOutputStream sigfos = new FileOutputStream(new File(path, id));
            sigfos.write(priv.getEncoded());
            sigfos.close();
        }
        FileOutputStream sigfos = new FileOutputStream(new File(path, id + ".pub"));
        sigfos.write(pub.getEncoded());
        sigfos.close();
    }

    public static void main(String[] args) throws
            NoSuchAlgorithmException, NoSuchProviderException, IOException {
        generatePubandPrivateKeyFiles(args[0], args[1]);
    }

}
