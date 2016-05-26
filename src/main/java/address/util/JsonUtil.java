package address.util;

import address.model.ContactGroup;
import address.model.ModelPerson;
import address.model.Person;
import address.model.adapter.PersonAdapter;
import com.google.gson.*;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Converts an object to its JSON string representation and vice versa
 */
public class JsonUtil {

    private static Gson gson = new GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(LocalDateTime.class,
                (JsonSerializer<LocalDateTime>) (localDateTime, typeOfT, context) -> {
                    Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
                    return new JsonPrimitive(instant.toEpochMilli());
                })
        .registerTypeAdapter(LocalDateTime.class,
                (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) -> {
                    System.out.println("LocalDateTime adapter was called");
                    Instant instant = Instant.ofEpochMilli(json.getAsJsonPrimitive().getAsLong());
                    return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
                })
        .registerTypeAdapter(SimpleStringProperty.class,
                (JsonSerializer<SimpleStringProperty>) (stringProp, typeOfT, context) ->
                        new JsonPrimitive(stringProp.get()))
        .registerTypeAdapter(SimpleStringProperty.class,
                (JsonDeserializer<SimpleStringProperty>) (json, typeOfT, context) ->
                        new SimpleStringProperty(json.getAsJsonPrimitive().getAsString()))
        .registerTypeAdapter(SimpleIntegerProperty.class,
                (JsonSerializer<SimpleIntegerProperty>) (intProp, typeOfT, context) ->
                        new JsonPrimitive(intProp.get()))
        .registerTypeAdapter(SimpleIntegerProperty.class,
                (JsonDeserializer<SimpleIntegerProperty>) (json, typeOfT, context) ->
                        new SimpleIntegerProperty(json.getAsJsonPrimitive().getAsInt()))
        .registerTypeAdapter(Person.class, new PersonAdapter())
        .registerTypeAdapter(ModelPerson.class, new PersonAdapter())
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
