import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;
import java.io.IOException;
import java.net.URL;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SSLCertificateExpiry {

    public static void main(String[] args) {
        String hostname = "www.digicert.com";  // Replace with the domain you're checking

        try {
            URL url = new URL("https://" + hostname);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.connect();

            Certificate[] certs = connection.getServerCertificates();
            for (Certificate cert : certs) {
                if (cert instanceof X509Certificate) {
                    X509Certificate x509Cert = (X509Certificate) cert;
                    Date expiryDate = x509Cert.getNotAfter();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                    System.out.println("SSL certificate for " + hostname + " expires on: " + dateFormat.format(expiryDate));
                }
            }

        } catch (SSLPeerUnverifiedException e) {
            System.out.println("Failed to verify SSL peer: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("I/O error: " + e.getMessage());
        }
    }
}
