import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.Socket;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;

public class SubdomainSSLChecker {

    // Function to find subdomains using Sublist3r
    public static Set<String> findSubdomains(String domain) {
        Set<String> subdomains = new HashSet<>();
        try {
            ProcessBuilder pb = new ProcessBuilder("sublist3r", "-d", domain, "-o", "subdomains.txt");
            pb.directory(new File("."));  // current directory
            Process process = pb.start();
            process.waitFor();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            // Read the subdomains from the output file
            BufferedReader fileReader = new BufferedReader(new java.io.FileReader("subdomains.txt"));
            while ((line = fileReader.readLine()) != null) {
                subdomains.add(line.trim());
            }
            fileReader.close();
        } catch (Exception e) {
            System.err.println("Error finding subdomains: " + e.getMessage());
        }
        return subdomains;
    }

    // Function to get SSL certificate expiry date
    public static String getSSLExpiryDate(String domain) {
        try {
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, null, null);
            SSLSocketFactory factory = context.getSocketFactory();

            try (SSLSocket socket = (SSLSocket) factory.createSocket(domain, 443)) {
                socket.startHandshake();
                Certificate[] certs = socket.getSession().getPeerCertificates();
                X509Certificate cert = (X509Certificate) certs[0];
                return new SimpleDateFormat("yyyy-MM-dd").format(cert.getNotAfter());
            }
        } catch (Exception e) {
            System.err.println("Error getting SSL certificate for " + domain + ": " + e.getMessage());
        }
        return null;
    }

    public static void main(String[] args) {
        String domain = "example.com";  // Replace with your domain

        System.out.println("Finding subdomains for " + domain + "...");
        Set<String> subdomains = findSubdomains(domain);

        if (!subdomains.isEmpty()) {
            System.out.println("Found " + subdomains.size() + " subdomains. Checking SSL certificate expiry dates...");
            for (String subdomain : subdomains) {
                String expiryDate = getSSLExpiryDate(subdomain);
                if (expiryDate != null) {
                    System.out.println(subdomain + ": SSL certificate expires on " + expiryDate);
                } else {
                    System.out.println(subdomain + ": Could not retrieve SSL certificate expiry date");
                }
            }
        } else {
            System.out.println("No subdomains found.");
        }
    }
}
