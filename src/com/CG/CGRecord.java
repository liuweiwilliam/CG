package com.CG;

import java.util.ArrayList;

/**
 * Created by zhongyao on 2016/12/14.
 * java class to store xml data file's record node
 */
public class CGRecord {
    private String name = ""; // record node name
    private ArrayList<CGPair> pairs = new ArrayList<CGPair>(); // all pair nodes in a record node

    void setPairs(ArrayList<CGPair> pairs) { this.pairs = pairs; }
    ArrayList<CGPair> getPairs() { return this.pairs; }
    void setName(String name) { this.name = name; }
    String getName() { return this.name; }

    // get value of a subnode(pair node) for a record node
    String getValue(String key) {
        for (int i=0; i<this.getPairs().size(); i++) {
            if (this.getPairs().get(i).getName().equals(key)) {
                return this.getPairs().get(i).getContent();
            }
        }
        return "";
    }

    // format a record object to string
    @Override
    public String toString() {
        String result = "";

        if (!this.getName().equals("")) result = result+this.name+"\n";
        for (int i=0; i<this.getPairs().size(); i++) {
            result = result + this.getPairs().get(i).toString();
        }

        return result;
    }
}
