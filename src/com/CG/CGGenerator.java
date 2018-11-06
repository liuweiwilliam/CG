
package com.CG;

import java.io.*;
import java.util.*;

/**
 * Created by zhongyao on 2016/12/15.
 */

@SuppressWarnings("unused")
public class CGGenerator {
	private String configurefile = ""; // configure file path
    private String templatefile = ""; // template file path
    private String paramsfile = ""; // xml data file path
    private String logfile = ""; // log file path
    private CGLogger logger; // log object
    private LinkedList<String> keywords = new LinkedList<String>(); // keywords cannot use e.g. loop, append
    private String outpath = ""; // generate(output) file path
    private String contents = ""; // template file contents
    private boolean append = false; // append data to a file rather than directly cover it
    private CGParam param; // an object stores all data from xml data file

    CGGenerator() {
        this.initialize();
    }
    CGGenerator(String configurefile) {
        this.initialize();
        this.setConfigurefile(configurefile);
    }
    CGGenerator(String configure, String logfile) throws Exception {
        this.initialize();
        this.setConfigurefile(configure);
        this.setLogfile(logfile);
        this.setLogger(new CGLogger(this.getLogfile()));
    }
    void initialize() {
        this.getKeywords().add("export");
        this.getKeywords().add("loop");
        this.getKeywords().add("import");
    }

    void setConfigurefile(String configurefile) { this.configurefile = configurefile; }
    String getConfigurefile() { return this.configurefile; }
    private void setTemplatefile(String templatefile) { this.templatefile = templatefile; }
    private String getTemplatefile() { return this.templatefile; }
	private void setParamsfile(String paramsfile) { this.paramsfile = paramsfile; }
	private String getParamsfile() { return this.paramsfile; }
    void setLogfile(String logfile) { this.logfile = logfile; }
    String getLogfile() { return this.logfile; }
    void setLogger(CGLogger logger) { this.logger = logger; }
    CGLogger getLogger() { return this.logger; }
    void setKeywords(LinkedList<String> keywords) { this.keywords = keywords; }
    LinkedList<String> getKeywords() { return this.keywords; }
    void setOutpath(String outpath) { this.outpath = outpath; }
    String getOutpath() { return this.outpath; }
    void setContents(String contents) { this.contents = contents; }
    String getContents() { return this.contents; }
    void setAppend(boolean append) { this.append = append; }
    boolean getAppend() { return this.append; }
    private void setParam(CGParam param) { this.param = param; }
    private CGParam getParam() { return this.param; }

    // generate driver function
    boolean generate() throws Exception {
    	System.out.println("start generate.");
    		
        // parse configure xml file
        ArrayList<CGList> lists;
        if (this.getLogfile().equals("")) {
    			lists = new CGParamFile(this.getConfigurefile()).parse().getLists();
        	} else {
        		lists = new CGParamFile(this.getConfigurefile(), this.getLogfile()).parse().getLists();
        }
        if (lists.size() != 1) {
        	CGHelper.log(this.getLogfile().equals(""), this.getLogger(), "configure file \"" + this.getConfigurefile() + "\" syntax error, program abort !!!", "configure file \"" + this.getConfigurefile() + "\" syntax error.");
    	}

        // iterate all configurations
    	for (int i=0; i<lists.get(0).getRecords().size(); i++) {
    		// 1. read template and param(xml data) file, one configurations maybe has multiply param files
    		CGParam p = new CGParam();
    		String template = lists.get(0).getRecords().get(i).getValue("template");
    		if (!new File(template).exists()) CGHelper.log(this.getLogfile().equals(""), this.getLogger(), "template file \""+template+"\" don't exist, program abort !!!", "template file \""+template+"\" don't exist.");
    		String[] params = lists.get(0).getRecords().get(i).getValue("params").split(",");

            CGTemplate tp = new CGTemplate(template);
            
            // 2. parse param(xml data) file to a java object
    		for (String param: params) {
    			CGParamFile pf;
    			int left=0;
    			int right=0;
    			if ((left=param.indexOf("["))>0) {
    				if ((right = param.indexOf("]", left)) < 0) {
    					CGHelper.log(this.getLogfile().equals(""), this.getLogger(), "configure file\""+this.getConfigurefile()+"\" has an error params file syntax. ");
    					continue;
    				}
    				String path = param.substring(0, left);
    				ArrayList<String> keys = new ArrayList<String>();
    				for (String key: param.substring(left+1, right).split(":")) {
    					keys.add(key);
    				}
    				if (!new File(path).exists()) {
    					CGHelper.log(this.getLogfile().equals(""), this.getLogger(), "param file \""+path+"\" don't exist.");
    					continue;
    				}
    				if (this.getLogfile().equals("")) {
    					pf = new CGParamFile(path);
                    } else {
                   		pf = new CGParamFile(path, this.getLogfile());
                    }
    				p.append(pf.parse().justNeed(keys));
    			} else {
    				if (!new File(param).exists()) {
    					CGHelper.log(this.getLogfile().equals(""), this.getLogger(), "param file \""+param+"\" don't exist.");
    					continue;
    				}
    				if (this.getLogfile().equals("")) {
    					pf = new CGParamFile(param);
                    } else {
                   		pf = new CGParamFile(param, this.getLogfile());
                    }
    				p.append(pf.parse());
    			}
   			}
    		p.getPairs().addAll((new CGSystem()).getAll());

    		// 3. some specific operations
    		this.setTemplatefile(template);
            this.setParam(p);
            this.setContents(tp.getContents());

            this.handleImport();
            this.handleDoubleSB(true);
            this.handleLoop();
            this.handleExport();
            this.handle();
            this.handleDoubleSB(false);
            this.setContents(this.handleSB(this.getContents(), false));
            
            // 4. append if need, and output contents to file
            if (this.getAppend()) {
            	BufferedReader br = new BufferedReader(new FileReader(this.getOutpath()));
                String data = "";
                String line = "";
                while ((line = br.readLine()) != null) {
                    data = data+line+"\n";
                }
                br.close();
                if (data.length() > 0) this.setContents(data+this.getContents());
            }
            OutputStream ost = new FileOutputStream(this.getOutpath());
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(ost, "UTF-8"));
            bw.write(this.getContents());
            bw.flush();
            bw.close();
    	}

        System.out.println("generate success.");
        return true;
    }

    // handle normal contents after import, loop, export and SB handled
    // this function fairly handle SB
    @SuppressWarnings("null")
	private void handle() throws Exception {
    	String ctcp = this.getContents();
    	int indexif = -1;
    	int indexendif = -1;
    	while ((indexif=ctcp.indexOf("[if", indexif)) >= 0) {
    		if ((indexendif=this.findPositionUseStack(ctcp, indexif+1, "[if", "[endif]")) <= 0) {
    			indexif++;
    			continue;
    		}
    		int rsbpos = ctcp.indexOf("]", indexif);
    		String condition = ctcp.substring(indexif+"[if".length(), rsbpos);
    		if (condition==null || condition.length()<=0) {
    			indexif++;
   				continue;
    		}

    		// replace all space of condition string
    		int indexspace = 0;
    		while ((indexspace=condition.indexOf(" ", indexspace)) >= 0) {
    			condition = condition.substring(0, indexspace)+condition.substring(indexspace+1, condition.length());
    			indexspace++;
    		}
    	
    		// j	udge condition
    		int eqpos = condition.indexOf("==");
    		int nepos = condition.indexOf("!=");
    		if (eqpos<=0 && nepos<=0) {
    			indexif++;
    			continue;
    		}

    		boolean equal = false;
    		if (eqpos <= 0) eqpos = 1;
    		if (nepos <= 0) {
    			nepos = 1;
    			equal = true;
    		}
    		int symbolpos = eqpos*nepos;

    		ArrayList<String> values = new ArrayList<String>();
    		values = this.getParam().getValues(condition.substring(0, symbolpos)); 

    		String value = values.get(0);

    		if (equal == true) {
    			if (value.equals(condition.substring(symbolpos+2, condition.length()))) {
    				ctcp = CGHelper.divideHeadTail(ctcp, "", indexif, rsbpos, indexendif, indexendif+"[endif]".length()-1);
    			} else {
    				ctcp = CGHelper.divideHeadTail(ctcp, "", indexif, indexif, indexif, indexendif+"[endif]".length()-1);
    			}
    		} else {
    			if (!value.equals(condition.substring(symbolpos+2, condition.length()))) {
    				ctcp = CGHelper.divideHeadTail(ctcp, "", indexif, rsbpos, indexendif, indexendif+"[endif]".length()-1);
    			} else {
    				ctcp = CGHelper.divideHeadTail(ctcp, "", indexif, indexif, indexif, indexendif+"[endif]".length()-1);
    			}
    		}
    	}
    	this.setContents(ctcp);
    	
        ArrayList<Integer> positions = new ArrayList<Integer>();
        String rslt = this.getContents();
        if (rslt!=null || !rslt.equals("")) {
            for (int i=(positions = this.findSBPosition(rslt, 0)).size()/2; i>0; i--) {
                rslt = this.replaceSBVariable(rslt, positions.get(i*2-2).intValue(), positions.get(i*2-1).intValue());
            }
        }
        this.setContents(rslt);
    }
    // handle export keyword
    private void handleExport() throws Exception {
    	// 1. get export path
        String ct = this.getContents();
        int eindex, cindex;
        eindex = ct.indexOf("[export]");
        String path = "";
        if (eindex < 0) {
        	if ((eindex=ct.indexOf("[append]")) < 0) CGHelper.log(this.getLogfile().equals(""), this.getLogger(), "a template file is necessary to have keyword \"export\" or \"append\", program abort !!!", "a template file is necessary to have keyword \"export\" or \"append\".");
        	cindex = ct.indexOf("\n", eindex);
            path = ct.substring(eindex+"[append]".length(), cindex);
            this.setAppend(true);
        } else {
        	if (ct.indexOf("[append]") >= 0) CGHelper.log(this.getLogfile().equals(""), this.getLogger(), "a template file don't allow has keyword \"import\" and \"append\" at the same time, program abort !!!", "a template file don't allow has keyword \"import\" and \"append\" at the same time.");
        	cindex = ct.indexOf("\n", eindex);
            path = ct.substring(eindex+"[export]".length(), cindex);
        }
        
        // 2. replace SB in export path
        ArrayList<Integer> position = this.findSBPosition(path, 0);
        for (int i=position.size()/2; i>0; i--) {
            path = this.replaceSBVariable(path, position.get(2*i-2), position.get(2*i-1));
        }
        this.setOutpath(path);

        // 3. create export or append path miss directory
        File fl = new File(path);
        if (!fl.exists()) {
            CGHelper.mkdir(path);
            fl.createNewFile();
        }
        
        // remove [export] or [append] line from contents
        ct = ct.substring(0, eindex) + ct.substring(cindex+1, ct.length());
        this.setContents(ct);
    }
    // handle import keyword
    private void handleImport() throws Exception {
        String ct = this.getContents();
        int iindex, cindex;
        
        // iterate all import keywords
        while ((iindex=ct.indexOf("[import]")) >= 0) {
        	// 1. get import path
        	cindex = ct.indexOf("\n", iindex);
        	String path = ct.substring(iindex+"[import]".length(), cindex);
        	ArrayList<Integer> position = this.findSBPosition(path, 0);
        	for (int i=position.size()/2; i>0; i--) {
        		path = this.replaceSBVariable(path, position.get(2*i-2), position.get(2*i-1));
        	}

        	if (!new File(path).exists()) {
        		CGHelper.log(this.getLogfile().equals(""), this.getLogger(), "import file \""+path+"\" don't exist.");;
        		return;
        	}

        	// 2. plug contents of import file in this.contents and remove [import] line
        	CGTemplate tp = new CGTemplate(path);
        	String iptcts = tp.getContents();
        	if (iptcts.indexOf("[export]") > 0) CGHelper.log(this.getLogfile().equals(""), this.getLogger(), "impot file \'" + path + "\' has illegal keyword \"export\", program abort !!!", "impot file \'" + path + "\' has illegal keyword \"export\".");
        	if (iptcts.indexOf("[append]") > 0) CGHelper.log(this.getLogfile().equals(""), this.getLogger(), "impot file \'" + path + "\' has illegal keyword \"append\", program abort !!!", "impot file \'" + path + "\' has illegal keyword \"append\".");
        	if (iptcts.indexOf("[import]") > 0) CGHelper.log(this.getLogfile().equals(""), this.getLogger(), "impot file \'" + path + "\' has illegal keyword \"append\", program abort !!!", "impot file \'" + path + "\' has illegal keyword \"import\".");
        	ct = ct.substring(0, iindex) + iptcts + ct.substring(cindex+1, ct.length());
        	this.setContents(ct);
        }
    }
    // handle loop keyword
    private void handleLoop() throws Exception {
        ArrayList<Integer> pos = new ArrayList<Integer>();
        ArrayList<Integer> subpos = new ArrayList<Integer>();
        String ct, ctcp, loop, startloop, endloop, lname;
        int start, end, times, index;
        start = 0;

        // iterate all loop keywords
        while ((start = this.getContents().indexOf("[loop ", start)) >= 0) {
        		// 1. find loopstart and endloop position
            loop = "";
            index = this.getContents().indexOf("]", start);
            lname = this.getContents().substring(start+"[loop ".length(), index);
            startloop = "[loop " + lname + "]";
            endloop = "[endloop " + lname + "]";
            end = this.getContents().indexOf(endloop, index);
            if (end <= 0) CGHelper.log(this.getLogfile().equals(""), this.getLogger(), "template file \"" + this.getTemplatefile() + "\" has incrorrect loop syntax format nearby \"" + startloop + "\", program abort !!!", "template file \"" + this.getTemplatefile() + "\" has incrorrect loop syntax format nearby \"" + startloop + "\".");
            
            // 2. get loop body
            ct = CGHelper.divideHeadTail(this.getContents(), "", 0, start+startloop.length()-1, end, this.getContents().length());
            
            // 3. judge loop body whether use loop list (loop body must use loop list)
            pos = this.findSBPosition(ct, 0);
            if (pos.size()<=0) {
            	CGHelper.log(this.getLogfile().equals(""), this.getLogger(), "undefine any list parameter in loop content \"" + ct + "\".");

            	int spos = this.getContents().indexOf(startloop);
            	int epos = this.getContents().indexOf(endloop)+endloop.length();
            	String newct = this.handleSB(this.getContents().substring(spos, epos), true);
            	this.setContents(this.getContents().substring(0, spos)+newct+this.getContents().substring(epos, this.getContents().length()));

            	continue;
            }

            subpos = new ArrayList<Integer>();
            for (int i=0; i<pos.size()/2; i++) {
            	String var = ct.substring(pos.get(2*i).intValue(), pos.get(2*i+1).intValue());
                if (var.contains(lname)) {
                	if (var.contains("if") && (var.contains("==") || var.contains("!="))) continue;
                    subpos.add(pos.get(2*i));
                    subpos.add(pos.get(2*i+1));
                }
            }

            if (subpos.size() <= 0) {
            	CGHelper.log(this.getLogfile().equals(""), this.getLogger(), "loop body \"" + ct + "\" don't use loop list \"" + lname + "\".");

            	// 避免在后续替换方括号变量时受其干扰，需要把newct的中括号替换成特殊字符，在最后写文件之前再替换回来
            	int spos = this.getContents().indexOf(startloop);
            	int epos = this.getContents().indexOf(endloop)+endloop.length();
            	String newct = this.handleSB(this.getContents().substring(spos, epos), true);
            	this.setContents(this.getContents().substring(0, spos)+newct+this.getContents().substring(epos, this.getContents().length()));

            	continue;
            }

            // 4. replace loop body
            times = this.getParam().getValues(ct.substring(subpos.get(0).intValue()+1, subpos.get(1).intValue())).size();
            for (int j=0; j<times; j++) {
                ctcp = ct;
                for (int k=subpos.size()/2; k>0; k--) {
                	ctcp = this.replaceSBVariable(ctcp, subpos.get(2*k-2), subpos.get(2*k-1), j);
                }
                
                // 5. handle condition statement
                int indexif = 0;
                int indexendif;

                while ((indexif=ctcp.indexOf("[if", indexif)) >= 0) {
                	if ((indexendif=this.findPositionUseStack(ctcp, indexif+1, "[if", "[endif]")) <= 0) {
                		indexif++;
                		continue;
                	}
                	int rsbpos = ctcp.indexOf("]", indexif);
                	String condition = ctcp.substring(indexif+"[if".length(), rsbpos);
                	if (condition==null || condition.length()<=0) {
                		indexif++;
                		continue;
                	}

                	// replace all space of condition string
                	int indexspace = 0;
                	while ((indexspace=condition.indexOf(" ", indexspace)) >= 0) {
                		condition = condition.substring(0, indexspace)+condition.substring(indexspace+1, condition.length());
                		indexspace++;
                	}

                	// judge condition
                	int eqpos = condition.indexOf("==");
                	int nepos = condition.indexOf("!=");
                	if (eqpos<=0 && nepos<=0) {
                		indexif++;
                		continue;
                	}

                	boolean equal = false;
                	if (eqpos <= 0) eqpos = 1;
                	if (nepos <= 0) {
                		nepos = 1;
                		equal = true;
                	}
                	int symbolpos = eqpos*nepos;

                	ArrayList<String> values = new ArrayList<String>();
                	if ((values = this.getParam().getValues(condition.substring(0, symbolpos))).size() != times) CGHelper.log(this.getLogfile().equals(""), this.getLogger(), "condition statement \"" + condition + "\"" + " has illegal variable !!!"); 

                	String value = values.get(j);

                	if (equal == true) {
                		if (value.equals(condition.substring(symbolpos+2, condition.length()))) {
                			ctcp = CGHelper.divideHeadTail(ctcp, "", indexif, rsbpos, indexendif, indexendif+"[endif]".length()-1);
                		} else {
                			ctcp = CGHelper.divideHeadTail(ctcp, "", indexif, indexif, indexif, indexendif+"[endif]".length()-1);
                		}
               		} else {
                		if (!value.equals(condition.substring(symbolpos+2, condition.length()))) {
                			ctcp = CGHelper.divideHeadTail(ctcp, "", indexif, rsbpos, indexendif, indexendif+"[endif]".length()-1);
                		} else {
                			ctcp = CGHelper.divideHeadTail(ctcp, "", indexif, indexif, indexif, indexendif+"[endif]".length()-1);
                		}
               		}
                }
                loop += ctcp;
            }
            ct = loop;

            this.setContents(CGHelper.divideHeadTail(this.getContents(), ct, start, start, start, end+endloop.length()-1));
        }
    }
    // handle [[ -> OXFFFFFFFF, ]] -> OXGGGGGGGG
    private void handleDoubleSB(boolean start) {
        int index = 0;
        String contents = this.getContents();

        if (start) {
            while ((index = contents.indexOf("[[")) >= 0) {
                contents = contents.substring(0, index) + "OXFFFFFFFF" + contents.substring(index+"[[".length(), contents.length());
            }
            while ((index = contents.indexOf("]]")) >= 0) {
                contents = contents.substring(0, index) + "OXGGGGGGGG" + contents.substring(index+"]]".length(), contents.length());
            }
        } else {
            while ((index = contents.indexOf("OXFFFFFFFF")) >= 0) {
                contents = contents.substring(0, index) + "[" + contents.substring(index+"OXFFFFFFFF".length(), contents.length());
            }
            while ((index = contents.indexOf("OXGGGGGGGG")) >= 0) {
                contents = contents.substring(0, index) + "]" + contents.substring(index+"OXGGGGGGGG".length(), contents.length());
            }
        }
        this.setContents(contents);
    }
    // handle [ -> OXFFFF, ] -> OXGGGG
    private String handleSB(String ct, boolean start) {
        int index = 0;

        if (start) {
            while ((index = ct.indexOf("[")) >= 0) {
            	ct = ct.substring(0, index) + "OXFFFF" + ct.substring(index+"[".length(), ct.length());
            }
            while ((index = ct.indexOf("]")) >= 0) {
            	ct = ct.substring(0, index) + "OXGGGG" + ct.substring(index+"]".length(), ct.length());
            }
        } else {
            while ((index = ct.indexOf("OXFFFF")) >= 0) {
            	ct = ct.substring(0, index) + "[" + ct.substring(index+"OXFFFF".length(), ct.length());
            }
            while ((index = ct.indexOf("OXGGGG")) >= 0) {
            	ct = ct.substring(0, index) + "]" + ct.substring(index+"OXGGGG".length(), ct.length());
            }
        }
        
        return ct;
    }

    // find all SBs' start and end location
    /**
    1.while循环，条件是能找到left或right
 	2.找from位置开始的left(“[”位置)和right(“[”位置)
 	3.对比left和right的大小，把小的赋给from
 	4.通过检查存放from的ArrayList a检查from是否正确
 	5.把from加到a
 	6.from++
    */
    private ArrayList<Integer> findSBPosition (String ct, int from) throws Exception {
        ArrayList<Integer> a = new ArrayList<Integer>();
        if (ct==null || ct.equals("")) return a;

        int left = 0;
        int right = 0;
        while (left>=0 || right>=0) {
            left = ct.indexOf("[", from);
            right = ct.indexOf("]", from);
            if (left==right) break;
            if (left<0 && right>=0) {
                a.add(right);
                break;
            }

            if (left > right) {
                from = right;
                if (a.size()>0) if (ct.charAt(a.get(a.size()-1)) != "[".toCharArray()[0]) {
                	CGHelper.log(this.getLogfile().equals(""), this.getLogger(), "Square brackets quantity of template file \"" + this.getTemplatefile() + "\" error when matches square brackets, program abort !!!", "Square brackets quantity of template file \"" + this.getTemplatefile() + "\" error when matches square brackets.");
                }
            } else {
                from = left;
                if (a.size()>0) if (ct.charAt(a.get(a.size()-1)) != "]".toCharArray()[0]) CGHelper.log(this.getLogfile().equals(""), this.getLogger(), "Square brackets quantity of template file \"" + this.getTemplatefile() + "\" error when matches square brackets, program abort !!!", "Square brackets quantity of template file \"" + this.getTemplatefile() + "\" error when matches square brackets.");
            }
            a.add(from);
            from++;
        }

        if (a.size()%2 != 0) CGHelper.log(this.getLogfile().equals(""), this.getLogger(), "Square brackets quantity of template file \"" + this.getTemplatefile() + "\" error when matches square brackets, program abort !!!", "Square brackets quantity of template file \"" + this.getTemplatefile() + "\" error when matches square brackets.");
        return a;
    }
    
    // replace SB variable use its value
    private String replaceSBVariable(String ct, int start, int end) throws Exception {
        if (ct==null || ct.equals("")) return "";

        // get variable from a SB
        String var = ct.substring(start+1, end);
        if (var.equals("loop") || var.equals("endloop")) CGHelper.log(this.getLogfile().equals(""), this.getLogger(), "find unhandled template keyword loop, program abort !!!", "find unhandled template keyword loop.");
        for (String keyword: this.getKeywords()) {
            if (var.contains(keyword)) CGHelper.log(this.getLogfile().equals(""), this.getLogger(), "too many keywords \"" + keyword + "\", program abort !!!", "too many keywords \"" + keyword + "\".");
        }

        // 2. get variable value
        ArrayList<String> values = this.getParam().getValues(var);
        if (values.size()<=0) {
        	CGHelper.log(this.getLogfile().equals(""), this.getLogger(), "unknown keyword or variable name \"" + var + "\".");
        	return ct;
        }

        // 3. replace variable use its value
        return (ct.substring(0, start) + values.get(0) + ct.substring(end+1, ct.length()));
    }
    // similarly above function
    private String replaceSBVariable(String ct, int start, int end, int index) throws Exception {
    	String var = ct.substring(start+1, end);

        ArrayList<String> values = this.getParam().getValues(var);
        if (values.size()<=0) {
        	CGHelper.log(this.getLogfile().equals(""), this.getLogger(), "unknown variable name \"" + var + "\", program abort !!!", "unknown variable name \"" + var + "\".");
        	return ct;
        }

        if (values.size() > 1) return (ct.substring(0, start) + values.get(index) + ct.substring(end+1, ct.length()));
        return (ct.substring(0, start) + values.get(0) + ct.substring(end+1, ct.length()));
    }
    private int findPositionUseStack(String ct, int loc, String left, String right) {
    	int index_left=0, index_right=0;
    	Stack<Integer> s = new Stack<Integer>();
    	s.push(1);
    	while (loc != -1) {
    		index_left = ct.indexOf(left, loc);
    		index_right = ct.indexOf(right, loc);
    		if (index_right==-1) return -1;
    		if (index_left==-1 && index_right!=-1) {
    			if (s.size()==1) {
    				return index_right;
    			} else {
    				s.pop();
    				loc = index_right+right.length();
    				continue;
    			}
    		}
    		if (index_left<index_right) {
    			s.push(1);
    			loc = index_left+left.length();
    		}
    		if (index_left>index_right) {
    			s.pop();
    			if (s.empty()) {
    				loc = index_right;
    				break;
    			}
    			loc = index_right+right.length();
    		}
    		if (index_left==index_right) return -1;
    	}
    	return loc;
    }
}
