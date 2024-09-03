import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SubdomainFinder {

    // Function to find subdomains by performing wildcard DNS search
    public static Set<String> findSubdomains(String domain) {
        Set<String> subdomains = new HashSet<>();
        
        try {
            // Using wildcard to find subdomains
            String wildcardDomain = "*." + domain;
            InetAddress[] addresses = InetAddress.getAllByName(wildcardDomain);
            for (InetAddress address : addresses) {
                subdomains.add(address.getHostName());
            }
        } catch (Exception e) {
            System.out.println("Wildcard search didn't work, attempting recursive lookup...");
        }

        // If wildcard search doesn't work, attempt recursive lookups on common patterns
        for (int i = 1; i <= 5; i++) {
            String pattern = "[a-z]{" + i + "}." + domain;
            subdomains.addAll(performRecursiveDNS(pattern, domain));
        }

        return subdomains;
    }

    // Function to perform recursive DNS lookup using a pattern
    public static Set<String> performRecursiveDNS(String pattern, String domain) {
        Set<String> subdomains = new HashSet<>();
        Pattern regex = Pattern.compile(pattern);

        for (int i = 0; i < Math.pow(26, pattern.length() - domain.length() + 1); i++) {
            String candidate = generateCandidate(i, pattern.length() - domain.length() + 1) + domain;
            Matcher matcher = regex.matcher(candidate);
            if (matcher.find() && isDomainExists(candidate)) {
                subdomains.add(candidate);
            }
        }
        return subdomains;
    }

    // Generate a candidate subdomain based on an index
    private static String generateCandidate(int index, int length) {
        StringBuilder sb = new StringBuilder();
        while (length-- > 0) {
            sb.append((char) ('a' + (index % 26)));
            index /= 26;
        }
        return sb.toString();
    }

    // Function to check if a domain exists by resolving its IP address
    private static boolean isDomainExists(String domain) {
        try {
            InetAddress.getByName(domain);
            System.out.println("Found: " + domain);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void main(String[] args) {
        String domain = "example.com";  // Replace with your domain

        System.out.println("Finding subdomains for " + domain + "...");
        Set<String> subdomains = findSubdomains(domain);

        if (!subdomains.isEmpty()) {
            System.out.println("Found " + subdomains.size() + " subdomains:");
            for (String subdomain : subdomains) {
                System.out.println(subdomain);
            }
        } else {
            System.out.println("No subdomains found.");
        }
    }
}
