package address.util;

import com.google.gson.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Converts an object to its JSON string representation and vice versa
 */
public class JsonUtil {

    private static Gson gson = new GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(LocalDateTime.class,
                (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) -> {
                    Instant instant = src.atZone(ZoneId.systemDefault()).toInstant();
                    long epochMilli = instant.toEpochMilli();
                    return new JsonPrimitive(epochMilli);
                })
        .registerTypeAdapter(LocalDateTime.class,
                (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) -> {
                    Instant instant = Instant.ofEpochMilli(json.getAsJsonPrimitive().getAsLong());
                    return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
                })
        .create();

    private JsonUtil() {}

    /**
     * Converts a given string representation of a JSON data to instance of a class
     * @param <T> The generic type to create an instance of
     * @return The instance of T with the specified values in the JSON string
     */
    public static <T> T fromJsonString(String json, Class<T> instanceClass) {
        T instance = gson.fromJson(json, instanceClass);
        System.out.println("Instance successfully created: " + instanceClass.toString());
        return instance;
    }

    /**
     * Converts a given instance of a class into its JSON data string representation
     * @param instance The T object to be converted into the JSON string
     * @param instanceClass The class of the object
     * @param <T> The generic type to create an instance of
     * @return JSON data representation of the given class instance, in string
     */
    public static <T> String toJsonString(T instance, Class<T> instanceClass) {
        String json = gson.toJson(instance, instanceClass);
        System.out.println("Successfully created json string of: " + instanceClass.toString());
        return json;
    }
}
