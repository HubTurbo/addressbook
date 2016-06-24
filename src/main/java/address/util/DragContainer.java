package address.util;

import javafx.scene.input.DataFormat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 */
public class DragContainer implements Serializable{

    private static final long serialVersionUID = -1458406119115196098L;

    List<Integer> data = new ArrayList<>();

    public static final DataFormat ADDRESS_BOOK_PERSON_UUID = new DataFormat("addressbook.MainApp");

    public List<Integer> getData() {
        return data;
    }

    public void addData(Integer data) {
        this.data.add(data);
    }

    public void addAllData(Collection<Integer> datas) {
        this.data.addAll(datas);
    }
}
