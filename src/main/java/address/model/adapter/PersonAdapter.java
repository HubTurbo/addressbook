package address.model.adapter;

import address.model.ContactGroup;
import address.model.Person;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.stream.Collectors;

public class PersonAdapter extends TypeAdapter<Person> {
    @Override
    public void write(JsonWriter out, Person value) throws IOException {
        out.beginObject();
        out.name("firstName").value(value.getFirstName());
        out.name("lastName").value(value.getLastName());
        out.name("street").value(value.getStreet());
        out.name("postalCode").value(value.getPostalCode());
        out.name("city").value(value.getCity());
        if (value.getBirthday() == null) {
            out.name("birthday").nullValue();
        } else {
            Instant instant = value.getBirthday().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
            out.name("birthday").value(instant.toEpochMilli());
        }
        out.name("githubUsername").value(value.getGithubUserName());
        Instant instant = value.getUpdatedAt().atZone(ZoneId.systemDefault()).toInstant();
        out.name("updatedAt").value(instant.toEpochMilli());
        out.name("contactGroups").value(String.join(";",
                value.getContactGroupsCopy().stream().map(e -> e.getName()).collect(Collectors.toList())));
        out.endObject();
    }

    @Override
    public Person read(JsonReader in) throws IOException {
        final Person person = new Person();

        in.beginObject();
        while (in.hasNext()) {
            switch (in.nextName()) {
                case "firstName":
                    person.setFirstName(in.nextString());
                    break;
                case "lastName":
                    person.setLastName(in.nextString());
                    break;
                case "street":
                    person.setStreet(in.nextString());
                    break;
                case "postalCode":
                    person.setPostalCode(Integer.parseInt(in.nextString()));
                    break;
                case "city":
                    person.setCity(in.nextString());
                    break;
                case "birthday":
                    try {
                        in.nextNull();
                    } catch(IOException e) {
                        // birthday is not null
                        person.setBirthday(LocalDate.ofEpochDay(Long.parseLong(in.nextString())));
                    }
                    break;
                case "githubUsername":
                    person.setGithubUserName(in.nextString());
                    break;
                case "contactGroups":
                    person.setContactGroups(Arrays.asList(in.nextString().split(";")).stream()
                            .map(ContactGroup::new).collect(Collectors.toList()));
                    break;
                case "updatedAt":
                    in.nextString(); // since cannot set, let it be
                    break;
            }
        }
        in.endObject();

        return person;
    }
}
