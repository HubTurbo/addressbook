package address.status;

import address.model.Person;

/**
 * An event triggered when an AddressBook's contact is edited.
 */
public class PersonEditedStatus extends PersonBaseStatus {

    private Person uneditedPerson;

    public PersonEditedStatus(Person uneditedPerson, Person person) {
        super(person);
        this.uneditedPerson = uneditedPerson;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        if (uneditedPerson.equals(person)){
            sb.append(String.format("%s %s has been edited", person.getFirstName(), person.getLastName()));
        } else {
            sb.append(String.format("%s %s has been edited to %s %s", uneditedPerson.getFirstName(),
                                                                      uneditedPerson.getLastName(),
                                                                      person.getFirstName(), person.getLastName()));
        }

        if (uneditedPerson.getStreet().length() != 0 && !uneditedPerson.getStreet().equals(person.getStreet())) {
            sb.append(String.format(", from %s to %s", uneditedPerson.getStreet(), person.getStreet()));
        }

        if (uneditedPerson.getCity().length() != 0 && !uneditedPerson.getCity().equals(person.getCity())) {
            sb.append(String.format(", from %s to %s", uneditedPerson.getCity(), person.getCity()));
        }

        if (uneditedPerson.getPostalCode() != person.getPostalCode()) {
            sb.append(String.format(", from %d to %d", uneditedPerson.getPostalCode(), person.getPostalCode()));
        }

        if (uneditedPerson.getBirthday() != null && person.getBirthday() != null
            && !uneditedPerson.getBirthday().equals(person.getBirthday())) {
            sb.append(String.format(", from %s to %s", uneditedPerson.getBirthday().toString(),
                                                       person.getBirthday().toString()));
        }

        if (uneditedPerson.getGithubUserName().length() != 0
            && !uneditedPerson.getGithubUserName().equals(person.getGithubUserName())) {
            sb.append(String.format(", from %s to %s", uneditedPerson.getGithubUserName(), person.getGithubUserName()));
        }

        return sb.toString();
    }
}
