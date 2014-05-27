package dk.dma.msinm.common.db;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Produces the {@code @Sql} injections.
 */
public class SqlProducer {

    private static Map<String, String> SQL_CACHE = new ConcurrentHashMap<>();

    @Produces
    @Sql
    public String loadSqlFromResource(InjectionPoint ip) {
        String resourceName = ip.getAnnotated().getAnnotation(Sql.class).value();
        Class<?> clazz = ip.getMember().getDeclaringClass();
        return loadResourceText(clazz, resourceName);
    }

    /**
     * Loads, caches and returns the text of the given file. The file must be placed
     * in the same package as the class.
     *
     * @param clazz the class which defines the location of the file
     * @param resourceName the name of the file
     * @return the text content of the file
     */
    public static String loadResourceText(Class<?> clazz, String resourceName) {
        // Check if the resource is cached
        String cacheKey = clazz.getName() + "-" + resourceName;
        if (SQL_CACHE.containsKey(cacheKey)) {
            return SQL_CACHE.get(cacheKey);
        }

        // Not cached - load it
        try (BufferedReader r = new BufferedReader(new InputStreamReader(clazz.getResourceAsStream(resourceName)))) {
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                result.append(line).append(System.lineSeparator());
            }
            SQL_CACHE.put(cacheKey, result.toString());
            return result.toString();
        } catch (Exception ex) {
            throw new IllegalArgumentException("Undefined resource " + resourceName + " relative to class " + clazz, ex);
        }
    }
}
