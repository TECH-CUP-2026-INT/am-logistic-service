package co.edu.escuelaing.techcup.logistics.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca un metodo de controller que solo puede ser invocado por un usuario
 * con rol "organizador". El rol llega ya validado por el API Gateway en el
 * header X-User-Role; este servicio unicamente lo verifica.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RequireOrganizador {
}
