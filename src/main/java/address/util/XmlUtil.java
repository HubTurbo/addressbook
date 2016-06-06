package address.util;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;

/**
 * Helps with reading from and writing to XML files.
 */
public class XmlUtil {

    /**
     * Returns the xml data in the file as an object of the specified type
     */
    public static <T> T getDataFromFile(File file, Class<T> classToConvert) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(classToConvert);
        Unmarshaller um = context.createUnmarshaller();

        return ((T) um.unmarshal(file));
    }

    /**
     * Saves the data in the file in xml format
     */
    public static <T> void saveDataToFile(File file, T data) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(data.getClass());
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        m.marshal(data, file);
    }

}
