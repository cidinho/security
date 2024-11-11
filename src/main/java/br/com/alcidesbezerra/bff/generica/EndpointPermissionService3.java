import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.util.AntPathMatcher;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Service
public class EndpointPermissionService {
    private static final Logger log = LoggerFactory.getLogger(EndpointPermissionService.class);
    private final Map<String, String> endpointPermissions;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public EndpointPermissionService(@Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping requestMappingHandlerMapping) {
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = requestMappingHandlerMapping.getHandlerMethods();
        Map<String, String> tempPermissions = new HashMap<>();

        handlerMethods.forEach((requestMappingInfo, handlerMethod) -> {
            PreAuthorize preAuthorize = handlerMethod.getMethodAnnotation(PreAuthorize.class);

            if (nonNull(preAuthorize)) {
                try {
                    String path = requestMappingInfo.getPatternsCondition().getPatterns().iterator().next();
                    String method = requestMappingInfo.getMethodsCondition().getMethods().iterator().next().name();
                    String key = String.format("%s:%s", path, method);
                    String value = preAuthorize.value();

                    tempPermissions.put(key, value);
                    log.debug("Adicionado mapeamento de permissão para {}: {}", key, value);
                } catch (Exception e) {
                    log.error("Erro ao processar permissões de endpoint para {}: {}", 
                        handlerMethod.getMethod(), e.getMessage());
                }
            }
        });

        this.endpointPermissions = Collections.unmodifiableMap(tempPermissions);
    }

    public ResponseEntity<Map<String, Object>> getPermissionsForEndpoint(String path, String bodyType) {
        if (isBlank(path)) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Caminho não pode ser vazio"));
        }

        String[] pathParts = path.split(":");
        if (pathParts.length != 2) {
            log.warn("Formato de caminho inválido: {}. Esperado formato: 'caminho:método'", path);
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Formato de caminho inválido"));
        }

        String endpointPath = pathParts[0];
        String method = pathParts[1];

        String matchedPath = findMatchingPath(endpointPath, method);

        if (matchedPath == null) {
            log.warn("Nenhuma permissão encontrada para o caminho: {} e método: {}", endpointPath, method);
            return ResponseEntity.ok(Collections.singletonMap("roles", new String[]{}));
        }

        String permissionExpression = endpointPermissions.get(matchedPath);

        if (permissionExpression.contains("this.getPermission")) {
            log.info("Permissão dinâmica encontrada para {}: {}", matchedPath, permissionExpression);
            return ResponseEntity.ok(Collections.singletonMap("roles", getDynamicPermissions(permissionExpression, endpointPath, method, bodyType)));
        } else {
            String[] roles = extractRolesFromPreAuthorize(permissionExpression);
            log.info("Permissões encontradas para {}: {}", matchedPath, Arrays.toString(roles));
            return ResponseEntity.ok(Collections.singletonMap("roles", roles));
        }
    }

    private String[] getDynamicPermissions(String permissionExpression, String path, String method, String bodyType) {
        String typeParam = extractTypeParamFromExpression(permissionExpression);
        if (typeParam == null) {
            log.warn("Não foi possível extrair o parâmetro de tipo da expressão: {}", permissionExpression);
            return new String[]{};
        }

        if ("GET".equals(method)) {
            String pathType = extractTypeFromPath(path, typeParam);
            if (pathType != null) {
                return new String[]{getPermission(FormTypeEnum.valueOf(pathType))};
            }
        } else if ("POST".equals(method) && bodyType != null) {
            return new String[]{getPermission(FormTypeEnum.valueOf(bodyType))};
        }

        // Se não encontrou o tipo no caminho para GET ou não tem bodyType para POST
        log.info("Tipo não encontrado. Retornando todas as permissões possíveis.");
        return new String[]{
            getPermission(FormTypeEnum.CONTATO),
            getPermission(FormTypeEnum.OUVIDORIA)
        };
    }

    private String extractTypeParamFromExpression(String permissionExpression) {
        Pattern pattern = Pattern.compile("this\\.getPermission\$$#([^)]*)\$$");
        Matcher matcher = pattern.matcher(permissionExpression);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String extractTypeFromPath(String path, String variableName) {
        String[] pathParts = path.split("/");
        for (int i = 0; i < pathParts.length; i++) {
            if (pathParts[i].equals("{" + variableName + "}") && i + 1 < pathParts.length) {
                return pathParts[i + 1];
            }
        }
        return null;
    }

    private String getPermission(FormTypeEnum type) {
        if (FormTypeEnum.CONTATO.equals(type)) {
            return WLRoles.PERM_OMINI_CHANEL_PERFIL_FORMS_CONTACT.getKey();
        }

        if (FormTypeEnum.OUVIDORIA.equals(type)) {
            return WLRoles.PERM_OMINI_CHANEL_PERFIL_FORMS_OMBUDSMAN.getKey();
        }

        return "";
    }

    private String[] extractRolesFromPreAuthorize(String preAuthorizeValue) {
        if (isBlank(preAuthorizeValue)) {
            return new String[0];
        }

        return Arrays.stream(preAuthorizeValue
            .replaceAll("hasRole\\('|hasAnyRole\\('|hasAuthority\\('|hasPermission\\(|[{}'\"#user)]", "")
            .split("\\s*(or|and)\\s*|\\s*[,;]\\s*"))
            .map(String::trim)
            .filter(part -> !part.isEmpty())
            .toArray(String[]::new);
    }

    private String findMatchingPath(String requestedPath, String method) {
        String exactMatch = endpointPermissions.keySet().stream()
            .filter(key -> key.equals(requestedPath + ":" + method))
            .findFirst()
            .orElse(null);

        if (exactMatch != null) {
            return exactMatch;
        }

        return endpointPermissions.keySet().stream()
            .filter(key -> {
                String[] keyParts = key.split(":");
                return pathMatcher.match(keyParts[0], requestedPath) && keyParts[1].equals(method);
            })
            .findFirst()
            .orElse(null);
    }

    public static void main(String[] args) {
        EndpointPermissionService service = new EndpointPermissionService(new RequestMappingHandlerMapping());

        service.simulateDynamicPermissions();
    }

    private void simulateDynamicPermissions() {
        // Simular permissão com GET e variável de caminho
        ResponseEntity<Map<String, Object>> getResult = getPermissionsForEndpoint("/api/forms/{type}/CONTATO:GET", null);
        System.out.println("Permissões para GET com variável de caminho: " + getResult.getBody());

        // Simular permissão com GET sem variável de caminho
        ResponseEntity<Map<String, Object>> getNoTypeResult = getPermissionsForEndpoint("/api/forms:GET", null);
        System.out.println("Permissões para GET sem variável de caminho: " + getNoTypeResult.getBody());

        // Simular permissão com POST e tipo no corpo
        ResponseEntity<Map<String, Object>> postResult = getPermissionsForEndpoint("/api/forms:POST", "CONTATO");
        System.out.println("Permissões para POST com tipo no corpo: " + postResult.getBody());

        // Simular permissão com POST sem tipo no corpo
        ResponseEntity<Map<String, Object>> postNoTypeResult = getPermissionsForEndpoint("/api/forms:POST", null);
        System.out.println("Permissões para POST sem tipo no corpo: " + postNoTypeResult.getBody());

        // Simular rota sem permissão
        ResponseEntity<Map<String, Object>> noPermissionResult = getPermissionsForEndpoint("/api/public:GET", null);
        System.out.println("Permissões para rota sem permissão: " + noPermissionResult.getBody());
    }
}