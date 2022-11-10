package me.michqql.itemabilities.item.item;

public class Property {

    private String tag;
    private Number value;

    public Property(String tag, Number value) {
        this.tag = tag;
        this.value = value;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Number getValue() {
        return value;
    }

    public void setValue(Number value) {
        this.value = value;
    }
}
