package mara.mybox.data;

import mara.mybox.db.TableBase;

/**
 * @Author Mara
 * @CreateDate 2020-7-19
 * @License Apache License Version 2.0
 */
public class TableData implements Cloneable {

    protected TableBase table;
    protected int maxColumnIndex;
    protected long id;
    protected Object value0, value1, value2, value3, value4, value5, value6, value7, value8, value9, value10;
    protected Object value11, value12, value13, value14, value15, value16, value17, value18, value19, value20;

    private void init() {
        id = -1;
        maxColumnIndex = 20;
    }

    public TableData() {
        init();
    }

    public TableData(TableBase table) {
        this.table = table;
        init();
    }

    public boolean valid() {
        return true;
    }

    public Object getValue(int index) {
        if (table == null || index < 0 || index > maxColumnIndex) {
            return null;
        }
        switch (index) {
            case 0:
                return getValue0();
            case 1:
                return getValue1();
            case 2:
                return getValue2();
            case 3:
                return getValue3();
            case 4:
                return getValue4();
            case 5:
                return getValue5();
            case 6:
                return getValue6();
            case 7:
                return getValue7();
            case 8:
                return getValue8();
            case 9:
                return getValue9();
            case 10:
                return getValue10();
            case 11:
                return getValue11();
            case 12:
                return getValue12();
            case 13:
                return getValue13();
            case 14:
                return getValue14();
            case 15:
                return getValue15();
            case 16:
                return getValue16();
            case 17:
                return getValue17();
            case 18:
                return getValue18();
            case 19:
                return getValue19();
            case 20:
                return getValue20();
            default:
                return null;
        }
    }

    public void setValue(int index, Object value) {
        if (table == null || index < 0 || index > maxColumnIndex) {
            return;
        }
        switch (index) {
            case 0:
                setValue0(value);
                return;
            case 1:
                setValue1(value);
                return;
            case 2:
                setValue2(value);
                return;
            case 3:
                setValue3(value);
                return;
            case 4:
                setValue4(value);
                return;
            case 5:
                setValue5(value);
                return;
            case 6:
                setValue6(value);
                return;
            case 7:
                setValue7(value);
                return;
            case 8:
                setValue8(value);
                return;
            case 9:
                setValue9(value);
                return;
            case 10:
                setValue10(value);
                return;
            case 11:
                setValue11(value);
                return;
            case 12:
                setValue12(value);
                return;
            case 13:
                setValue13(value);
                return;
            case 14:
                setValue14(value);
                return;
            case 15:
                setValue15(value);
                return;
            case 16:
                setValue16(value);
                return;
            case 17:
                setValue17(value);
                return;
            case 18:
                setValue18(value);
                return;
            case 19:
                setValue19(value);
                return;
            case 20:
                setValue20(value);
                return;
        }
    }

    public Object getValue(String name) {
        if (table == null) {
            return null;
        }
        return getValue(table.columnIndex(name));
    }

    public void setValue(String name, Object value) {
        if (table == null) {
            return;
        }
        setValue(table.columnIndex(name), value);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        try {
            return super.clone();
        } catch (Exception e) {
            return null;
        }
    }

    /*
        static methods
     */
 /*
        customized  get/set
     */
    public TableBase getTable() {
        return table;
    }

    /*
        get/set
     */
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setTable(TableBase table) {
        this.table = table;
    }

    public Object getValue0() {
        return value0;
    }

    public void setValue0(Object value0) {
        this.value0 = value0;
    }

    public Object getValue1() {
        return value1;
    }

    public void setValue1(Object value1) {
        this.value1 = value1;
    }

    public Object getValue2() {
        return value2;
    }

    public void setValue2(Object value2) {
        this.value2 = value2;
    }

    public Object getValue3() {
        return value3;
    }

    public void setValue3(Object value3) {
        this.value3 = value3;
    }

    public Object getValue4() {
        return value4;
    }

    public void setValue4(Object value4) {
        this.value4 = value4;
    }

    public Object getValue5() {
        return value5;
    }

    public void setValue5(Object value5) {
        this.value5 = value5;
    }

    public Object getValue6() {
        return value6;
    }

    public void setValue6(Object value6) {
        this.value6 = value6;
    }

    public Object getValue7() {
        return value7;
    }

    public void setValue7(Object value7) {
        this.value7 = value7;
    }

    public Object getValue8() {
        return value8;
    }

    public void setValue8(Object value8) {
        this.value8 = value8;
    }

    public Object getValue9() {
        return value9;
    }

    public void setValue9(Object value9) {
        this.value9 = value9;
    }

    public Object getValue10() {
        return value10;
    }

    public void setValue10(Object value10) {
        this.value10 = value10;
    }

    public Object getValue11() {
        return value11;
    }

    public void setValue11(Object value11) {
        this.value11 = value11;
    }

    public Object getValue12() {
        return value12;
    }

    public void setValue12(Object value12) {
        this.value12 = value12;
    }

    public Object getValue13() {
        return value13;
    }

    public void setValue13(Object value13) {
        this.value13 = value13;
    }

    public Object getValue14() {
        return value14;
    }

    public void setValue14(Object value14) {
        this.value14 = value14;
    }

    public Object getValue15() {
        return value15;
    }

    public void setValue15(Object value15) {
        this.value15 = value15;
    }

    public Object getValue16() {
        return value16;
    }

    public void setValue16(Object value16) {
        this.value16 = value16;
    }

    public Object getValue17() {
        return value17;
    }

    public void setValue17(Object value17) {
        this.value17 = value17;
    }

    public Object getValue18() {
        return value18;
    }

    public void setValue18(Object value18) {
        this.value18 = value18;
    }

    public Object getValue19() {
        return value19;
    }

    public void setValue19(Object value19) {
        this.value19 = value19;
    }

    public Object getValue20() {
        return value20;
    }

    public void setValue20(Object value20) {
        this.value20 = value20;
    }

}
