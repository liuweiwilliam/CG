package com.CG;

import java.util.ArrayList;

/**
 * Created by zhongyao on 2016/12/14.
 * java class to store xml data file's list node
 */
public class CGList {
	private String name = ""; // list node name
	private ArrayList<CGRecord> records = new ArrayList<CGRecord>(); // all records nodes in a list node

    // get values of all record nodes' key pair node for a list node
    ArrayList<String> getValues(String key) {
        ArrayList<String> a = new ArrayList<String>();

        for (int i=0; i<this.getRecords().size(); i++) {
            String value = this.getRecords().get(i).getValue(key);
            if (!value.equals("")) a.add(value);
        }
        return a;
    }

    void setName(String name) { this.name = name; }
    String getName() { return this.name; }
    void setRecords(ArrayList<CGRecord> records) { this.records = records; }
    ArrayList<CGRecord> getRecords() { return this.records; }

    // format a list object to string
    @Override
    public String toString() {
        String result = "";
        result = result + this.getName() + "\n";

        for (int i=0; i<this.getRecords().size(); i++) {
            result = result + this.getRecords().get(i).toString();
        }

        return result;
    }
}
