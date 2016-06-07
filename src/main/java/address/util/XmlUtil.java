package address.util;

import address.exceptions.DataConversionException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileNotFoundException;

/**
 * Helps with reading from and writing to XML files.
 */
public class XmlUtil {

    /**
     * Returns the xml data in the file as an object of the specified type.
     * @param file Points to a valid xml file containing data that match the {@code classToConvert}.
     *             Cannot be null.
     * @param classToConvert The class corresponding to the xml data.
     *                       Cannot be null.
     * @throws FileNotFoundException Thrown if the file is missing.
     * @throws DataConversionException Thrown if the file is empty or does not have the correct format.
     */
    public static <T> T getDataFromFile(File file, Class<T> classToConvert) throws DataConversionException, FileNotFoundException {

        assert file != null;
        assert classToConvert != null;

        if(!file.exists()){
            throw new FileNotFoundException("File not found : " + file.getAbsolutePath());
        }

        try {
            JAXBContext context = JAXBContext.newInstance(classToConvert);
            Unmarshaller um = context.createUnmarshaller();

            return ((T) um.unmarshal(file));
        } catch (JAXBException e) {
            throw new DataConversionException(e);
        }
    }

    /**
     * Saves the data in the file in xml format.
     * @param file Points to a valid xml file containing data that match the {@code classToConvert}.
     *             Cannot be null.
     * @throws FileNotFoundException Thrown if the file is missing.
     * @throws DataConversionException Thrown if there is an error during converting the data
     *                                 into xml and writing to the file.
     */
    public static <T> void saveDataToFile(File file, T data) throws DataConversionException, FileNotFoundException {

        assert file != null;
        assert data != null;

        if(!file.exists()){
            throw new FileNotFoundException("File not found : " + file.getAbsolutePath());
        }

        try {
            JAXBContext context = JAXBContext.newInstance(data.getClass());
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            m.marshal(data, file);
        } catch (JAXBException e) {
            throw new DataConversionException(e);
        }
    }

}
