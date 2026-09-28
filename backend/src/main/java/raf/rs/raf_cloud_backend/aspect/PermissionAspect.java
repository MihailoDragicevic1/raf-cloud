package raf.rs.raf_cloud_backend.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import raf.rs.raf_cloud_backend.annotation.Secured;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Set;

@Aspect
@Component
public class PermissionAspect {

    @Around("@annotation(secured)")
    public Object checkPermission(ProceedingJoinPoint joinPoint, Secured secured) throws Throwable {
        String requiredPermission = secured.value();

        if (requiredPermission.isEmpty()) {
            return joinPoint.proceed();
        }

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return new ResponseEntity<>(
                    new ErrorResponse(403, "Access denied: No request context"),
                    HttpStatus.FORBIDDEN
            );
        }

        HttpServletRequest request = attributes.getRequest();

        Object permissionsObj = request.getAttribute("permissions");
        if (permissionsObj == null) {
            return new ResponseEntity<>(
                    new ErrorResponse(403, "Access denied: User not authenticated"),
                    HttpStatus.FORBIDDEN
            );
        }

        Set<String> userPermissions = new java.util.HashSet<>();
        if (permissionsObj instanceof Collection) {
            userPermissions.addAll((Collection<? extends String>) permissionsObj);
        }

        if (!userPermissions.contains(requiredPermission)) {
            return new ResponseEntity<>(
                    new ErrorResponse(403, "Access denied: Missing permission '" + requiredPermission + "'"),
                    HttpStatus.FORBIDDEN
            );
        }

        return joinPoint.proceed();
    }

    public static class ErrorResponse {
        public int status;
        public String message;

        public ErrorResponse(int status, String message) {
            this.status = status;
            this.message = message;
        }

        public int getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }
    }
}

