package com.enes.urgan;

import java.util.ArrayList;

public class Item {
    public void setUpdate(String newUpdate) {
        update = newUpdate;
    }

    public void setOlusturulma(String newOlusturulma) {
        olusturulma = newOlusturulma;
    }

    public void setContent(String newContent) {
        content = newContent;
    }

    public void setId(String newId) {
        id = newId;
    }

    public String getId() {
        return id;
    }

    public String getIsim() {
        return isim;
    }

    public int getTip() {
        return tip;
    }

    public ArrayList<String> getTaglar() {
        return taglar;
    }

    public void setTaglar(ArrayList<String> newTaglar) {
        taglar = newTaglar;
    }

    public void addTag(String tag) {
        taglar.add(tag);
    }

    public String getOlusturulma() {
        return olusturulma;
    }

    public String getUpdate() {
        return update;
    }

    public String getContent() {
        return content;
    }

    public boolean tagVarMi(String tag) {
        boolean isIn = false;
        for (int i=0; i<taglar.size(); i++) {
            if (tag.equals(taglar.get(i))) {
                isIn = true;
                break;
            }
        }
        return isIn;
    }

    private String id;
    private String isim;
    private int tip;
    private ArrayList<String> taglar;
    private String olusturulma;
    private String update;
    private String content;

    Item(String initName, String initContent, int initTip) {
        isim = initName;
        content = initContent;
        tip = initTip;
    }
}
