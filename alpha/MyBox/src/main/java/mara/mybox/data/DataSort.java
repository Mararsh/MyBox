package mara.mybox.data;

import java.util.ArrayList;
import java.util.List;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-12-1
 * @License Apache License Version 2.0
 */
public class DataSort {

    protected String name;
    protected boolean ascending;

    public DataSort() {
        this.name = null;
        this.ascending = true;
    }

    public DataSort(String name, boolean ascending) {
        this.name = name;
        this.ascending = ascending;
    }

    /*
        static
     */
    public static List<DataSort> parse(List<String> orders) {
        if (orders == null || orders.isEmpty()) {
            return null;
        }
        List<DataSort> sorts = new ArrayList<>();
        String descString = "-" + message("Descending");
        String ascString = "-" + message("Ascending");
        int desclen = descString.length();
        int asclen = ascString.length();
        String sname;
        boolean asc;
        for (String order : orders) {
            if (order.endsWith(descString)) {
                sname = order.substring(0, order.length() - desclen);
                asc = false;
            } else if (order.endsWith(ascString)) {
                sname = order.substring(0, order.length() - asclen);
                asc = true;
            } else {
                continue;
            }
            sorts.add(new DataSort(sname, asc));
        }
        return sorts;
    }

    public static List<String> parseNames(List<String> orders) {
        if (orders == null || orders.isEmpty()) {
            return null;
        }
        List<String> names = new ArrayList<>();
        String descString = "-" + message("Descending");
        String ascString = "-" + message("Ascending");
        int desclen = descString.length();
        int asclen = ascString.length();
        String sname;
        for (String order : orders) {
            if (order.endsWith(descString)) {
                sname = order.substring(0, order.length() - desclen);
            } else if (order.endsWith(ascString)) {
                sname = order.substring(0, order.length() - asclen);
            } else {
                continue;
            }
            if (!names.contains(sname)) {
                names.add(sname);
            }
        }
        return names;
    }

    public static String toString(List<DataSort> sorts) {
        if (sorts == null || sorts.isEmpty()) {
            return null;
        }
        String orderBy = null, piece;
        for (DataSort sort : sorts) {
            piece = sort.getName() + (sort.isAscending() ? " ASC" : " DESC");
            if (orderBy == null) {
                orderBy = piece;
            } else {
                orderBy += ", " + piece;
            }
        }
        return orderBy;
    }

    public static String parseToString(List<String> orders) {
        return toString(parse(orders));
    }

    /*
        get/set
     */
    public String getName() {
        return name;
    }

    public DataSort setName(String name) {
        this.name = name;
        return this;
    }

    public boolean isAscending() {
        return ascending;
    }

    public DataSort setAscending(boolean ascending) {
        this.ascending = ascending;
        return this;
    }

}
