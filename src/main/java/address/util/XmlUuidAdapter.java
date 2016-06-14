package address.util;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.UUID;

/**
 * Created by Prime Mover on 14/6/2016.
 */
public class XmlUuidAdapter extends XmlAdapter<String, UUID> {
    @Override
    public UUID unmarshal(String v) {
        return UUID.fromString(v);
    }

    @Override
    public String marshal(UUID v) {
        return v.toString();
    }
}
