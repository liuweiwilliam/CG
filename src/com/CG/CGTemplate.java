
package com.CG;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * Created by zhongyao on 2016/12/15.
 * use this class to read template file contents
 */
public class CGTemplate {
    private String fileName = ""; // template file path

    CGTemplate(String path) {
        this.setFileName(path);
    }

    void setFileName(String fileName) { this.fileName = fileName; }
    String getFileName() { return this.fileName; }

    // read template file contents
    String getContents() throws Exception {
    		if (this.getFileName().equals("") || !new File(this.getFileName()).exists()) return "";
        BufferedReader br = new BufferedReader(new FileReader(this.getFileName()));
        String data = "";
        String line = "";
        while ((line=br.readLine()) != null) {
            data = data+line+"\n";
        }
        br.close();
        return data;
    }
}
