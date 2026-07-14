package co.edu.escuelaing.techcup.logistics.support;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

/**
 * Construye tokens con forma de JWT (header.payload.firma) sin firmar, igual a lo que
 * JwtClaimsFilter espera decodificar en tests de slice: el filtro nunca verifica la
 * firma (esa responsabilidad es del API Gateway), solo decodifica el payload base64url.
 */
public final class JwtTestSupport {

    private JwtTestSupport() {
    }

    public static String token(UUID userId, String... roles) {
        StringBuilder rolesJson = new StringBuilder("[");
        for (int i = 0; i < roles.length; i++) {
            if (i > 0) {
                rolesJson.append(",");
            }
            rolesJson.append("\"").append(roles[i]).append("\"");
        }
        rolesJson.append("]");

        String payload = "{\"sub\":\"" + userId + "\",\"roles\":" + rolesJson + "}";
        return encode("{\"alg\":\"none\"}") + "." + encode(payload) + ".sig";
    }

    public static String bearer(UUID userId, String... roles) {
        return "Bearer " + token(userId, roles);
    }

    private static String encode(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }
}
