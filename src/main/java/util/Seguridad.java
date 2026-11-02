// util/Seguridad.java
package util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Hash de contraseñas con PBKDF2-HMAC-SHA256 y sal aleatoria (incluido en Java, sin librerías extra).
 *
 * Formato guardado en la base de datos:  pbkdf2_sha256$ITERACIONES$SAL_BASE64$HASH_BASE64
 * Ejemplo: pbkdf2_sha256$120000$Q2FkYS... $nH9r...
 *
 * La sal hace que dos usuarios con la misma contraseña tengan hashes distintos, y las
 * iteraciones hacen que probar contraseñas por fuerza bruta sea lento.
 */
public final class Seguridad {

    private static final String PREFIJO = "pbkdf2_sha256";
    private static final int ITERACIONES = 120_000;
    private static final int BYTES_SAL = 16;
    private static final int BITS_HASH = 256;
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private Seguridad() {
    }

    /** Genera el hash (con sal nueva) de una contraseña. */
    public static String hashContrasena(String contrasena) {
        byte[] sal = new byte[BYTES_SAL];
        ALEATORIO.nextBytes(sal);
        byte[] hash = pbkdf2(contrasena.toCharArray(), sal, ITERACIONES);
        Base64.Encoder b64 = Base64.getEncoder().withoutPadding();
        return PREFIJO + "$" + ITERACIONES + "$" + b64.encodeToString(sal) + "$" + b64.encodeToString(hash);
    }

    /** true si el valor guardado ya es un hash generado por esta clase. */
    public static boolean esHash(String guardado) {
        return guardado != null && guardado.startsWith(PREFIJO + "$") && guardado.split("\\$").length == 4;
    }

    /**
     * Verifica una contraseña contra el valor guardado.
     * Si el valor guardado todavía está en texto plano (bases antiguas), lo compara directamente;
     * en ese caso conviene volver a guardarlo con {@link #hashContrasena(String)}.
     */
    public static boolean verificar(String contrasena, String guardado) {
        if (contrasena == null || guardado == null) {
            return false;
        }
        if (!esHash(guardado)) {
            return MessageDigest.isEqual(contrasena.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                    guardado.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
        try {
            String[] partes = guardado.split("\\$");
            int iteraciones = Integer.parseInt(partes[1]);
            byte[] sal = Base64.getDecoder().decode(partes[2]);
            byte[] esperado = Base64.getDecoder().decode(partes[3]);
            byte[] calculado = pbkdf2(contrasena.toCharArray(), sal, iteraciones);
            return MessageDigest.isEqual(esperado, calculado);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] pbkdf2(char[] contrasena, byte[] sal, int iteraciones) {
        PBEKeySpec spec = new PBEKeySpec(contrasena, sal, iteraciones, BITS_HASH);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("No se pudo calcular el hash de la contraseña", e);
        } finally {
            spec.clearPassword();
        }
    }

    /** Utilidad de consola: imprime el hash de una contraseña (útil para cargarlo en la base de datos). */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Uso: java util.Seguridad <contraseña>");
            return;
        }
        System.out.println(hashContrasena(args[0]));
    }
}
