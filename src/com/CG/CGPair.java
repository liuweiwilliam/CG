
package com.CG;

/**
 * Created by zhongyao on 2016/12/14.
 * java class to store xml data file's pair node
 */
public class CGPair {
    private String name = ""; // name property of a pair node
    private String content = ""; // content property of a pair node

    CGPair() { }
    CGPair (String name, String content) {
    		this.setName(name);
    		this.setContent(content);
    }

    void setName(String name) { this.name = name; }
    String getName() { return this.name; }
    void setContent(String content) { this.content = content; }
    String getContent() { return this.content; }

    // format a record object to string
    @Override
    public String toString() {
        return this.getName() + " " + this.getContent() + "\n";
    }
}

