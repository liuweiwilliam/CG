
package com.CG;

import java.util.ArrayList;

/**
 * Created by zhongyao on 2016/12/9.
 * this class will store all data of a xml data file
 */
public class CGParam {
    private ArrayList<CGPair> pairs = new ArrayList<CGPair>(); // all pair nodes
    private ArrayList<CGRecord> records = new ArrayList<CGRecord>(); // all records nodes
    private ArrayList<CGList> lists = new ArrayList<CGList>(); // all list nodes
    private CGLogger logger; // log object
    private String logfile = ""; // log file path

    CGParam() {}
    CGParam(CGLogger logger) throws Exception {
        this.setLogger(logger);
    }

    void setPairs(ArrayList<CGPair> pairs) { this.pairs = pairs; }
    ArrayList<CGPair> getPairs() { return this.pairs; }
    void setRecords(ArrayList<CGRecord> records) { this.records = records; }
    ArrayList<CGRecord> getRecords() { return this.records; }
    void setLists(ArrayList<CGList> lists) { this.lists = lists; }
    ArrayList<CGList> getLists() { return this.lists; }
    void setLogger(CGLogger logger) { this.logger = logger; }
    CGLogger getLogger() { return this.logger; }
    void setLogfile(String logfile) { this.logfile = logfile; }
    String getLogfile() { return this.logfile; }

    // get a pair node, record node or list node's value(s) by its name
    ArrayList<String> getValues(String key) throws Exception {
        ArrayList<String> a = new ArrayList<>();

        // get a pair node value
        for (int i=0; i<this.getPairs().size(); i++) {
            if (this.getPairs().get(i).getName().equals(key)) {
                a.add(this.getPairs().get(i).getContent());
                return a;
            }
        }

        if (!key.contains(".")) return a;
        int firstPoint = key.indexOf(".");
        String firstString = key.substring(0, firstPoint);
        String newKey = key.substring(firstPoint+1, key.length());

        // get a record node values
        for (int j=0; j<this.getRecords().size(); j++) {
            if (this.getRecords().get(j).getName().equals(firstString)) {
                String value = this.getRecords().get(j).getValue(newKey);
                if (!value.equals("")) {
                    a.add(value);
                    return a;
                }
            }
        }

        // get a list node values
        for (int k=0; k<this.getLists().size(); k++) {
            if (this.getLists().get(k).getName().equals(firstString)) {
                ArrayList<String> values = this.getLists().get(k).getValues(newKey);
                if (values.size()>0) a = values;
                return a;
            }
        }
        
        // check a list node's child(all record nodes) whether have same pair node "name" property with each other 
        for (int i=0; i<this.getLists().size(); i++) {
        		int len = this.getLists().get(i).getRecords().get(0).getPairs().size();
        		int jlen;
        		for (int j=1; j<this.getLists().get(i).getRecords().size(); j++) {
        			jlen = this.getLists().get(i).getRecords().get(j).getPairs().size();
        			if (len != jlen) CGHelper.log(this.getLogfile().equals(""), this.getLogger(), "XML file list \"" + this.getLists().get(i).getRecords().get(j).getName() + "\" " + j+1 + "th records miss " + (len-jlen) + " elements, program abort !!!", "XML file list \"" + this.getLists().get(i).getRecords().get(j).getName() + "\" " + j+1 + "th records miss " + (len-jlen) + " elements.");
        		}
        }

        return a;
    }
    
    // filter a param object, remove pair, record or list not include in keys list
    // this function won't handle key "list1.name2"
    public CGParam justNeed(ArrayList<String> keys) {
    		ArrayList<CGPair> pairs = new ArrayList<CGPair>();
    		ArrayList<CGRecord> records = new ArrayList<CGRecord>();
    		ArrayList<CGList> lists = new ArrayList<CGList>();
    	
    		// handle pair
    		for (CGPair pair: this.getPairs()) {
    			for (int i=0; i<keys.size(); i++) {
		   		if (pair.getName().equals(keys.get(i))) {
		   			pairs.add(pair);
		   			keys.remove(i);
		   			i--;
		   		}
		   	}
    		}
    	
    		// h	andle record
    		for (CGRecord record: this.getRecords()) {
    			for (int i=0; i<keys.size(); i++) {
		   		if (record.getName().equals(keys.get(i))) {
		   			records.add(record);
		   			keys.remove(i);
		   			i--;
		   		}
    			}
    		}
    	
    		// handle list
    		for (CGList list: this.getLists()) {
    			for (int i=0; i<keys.size(); i++) {
		   		if (list.getName().equals(keys.get(i))) {
		   			lists.add(list);
		   			keys.remove(i);
		   			i--;
		   		}
    			}
    		}

    		this.setPairs(pairs);
    		this.setRecords(records);
    		this.setLists(lists);
    		return this;
    }

    // append another param object data to this param object
    public void append(CGParam p) {
    		// append pairs
    		if (this.getPairs().size()==0) {
    			this.setPairs(p.getPairs());
    		} else {
    			for (int i=0; i<p.getPairs().size(); i++) {
    				for (int j=0; j<this.getPairs().size(); j++) {
    					if (p.getPairs().get(i).getName().equals(this.getPairs().get(j).getName())) {
    						continue;
    					} else {
    						if (j==(this.getPairs().size()-1)) {
    							this.getPairs().add(p.getPairs().get(i));
    						}
    					}
    				}
    			}
    		}
    	
    		// append records
    		if (this.getRecords().size()==0) {
    			this.setRecords(p.getRecords());
    		} else {
    			for (int i=0; i<p.getRecords().size(); i++) {
    				for (int j=0; j<this.getRecords().size(); j++) {
    					if (p.getRecords().get(i).getName().equals(this.getRecords().get(j).getName())) {
    						continue;
    					} else {
    						if (j==(this.getRecords().size()-1)) {
    							this.getRecords().add(p.getRecords().get(i));
    						}
    					}
    				}
    			}
    		}

    		// append lists
    		if (this.getLists().size()==0) {
    			this.setLists(p.getLists());
    		} else {
    			for (int i=0; i<p.getLists().size(); i++) {
    				for (int j=0; j<this.getLists().size(); j++) {
    					if (p.getLists().get(i).getName().equals(this.getLists().get(j).getName())) {
    						continue;
    					} else {
    						if (j==(this.getLists().size()-1)) {
    							this.getLists().add(p.getLists().get(i));
    						}
    					}
    				}
    			}
    		}
    }

    // format a param object to string
    @Override
    public String toString() {
        String result = "";

        for (int i=0; i<this.getPairs().size(); i++) {
            result = result + this.getPairs().get(i).getName() + " " + this.getPairs().get(i).getContent() + "\n";
        }
        for (int j=0; j<this.getRecords().size(); j++) {
            result = result + this.getRecords().get(j).getName() + "\n";
            for (int k=0; k<this.getRecords().get(j).getPairs().size(); k++) {
                result = result + this.getRecords().get(j).getPairs().get(k).getName() + " " + this.getRecords().get(j).getPairs().get(k).getContent() + "\n";
            }
        }
        for (int k=0; k<this.getLists().size(); k++) {
            result = result + this.getLists().get(k).getName() + "\n";
            for (int m=0; m<this.getLists().get(k).getRecords().size(); m++) {
                result = result + this.getLists().get(k).getRecords().get(m).getName();
                for (int n=0; n<this.getLists().get(k).getRecords().get(m).getPairs().size(); n++) {
                    result = result + this.getLists().get(k).getRecords().get(m).getPairs().get(n).getName() + " " + this.getLists().get(k).getRecords().get(m).getPairs().get(n).getContent() + "\n";
                }
            }
        }

        return result;
    }
}
