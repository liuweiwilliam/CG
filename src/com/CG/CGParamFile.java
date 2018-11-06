
package com.CG;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.lang.String;

/**
 * Created by zhongyao on 2016/12/14.
 * use this class to parse xml data file
 */
public class CGParamFile {
    private String XMLFile = ""; // xml file path
    private String logfile = ""; // log file path
    private CGLogger logger; // log object

    CGParamFile(String XML) {
        this.setXMLFile(XML);
    }
    CGParamFile(String XML, String log) throws Exception {
        this.setXMLFile(XML);
        this.setLogfile(log);
        this.setLogger(new CGLogger(log));
    }

    void setXMLFile(String XMLFile) { this.XMLFile = XMLFile; }
    String getXMLFile() { return this.XMLFile; }
    void setLogfile(String logfile) { this.logfile = logfile; }
    String getLogfile() { return this.logfile; }
    void setLogger(CGLogger logger) { this.logger = logger; }
    CGLogger getLogger() { return this.logger; }
    
    // parse xml file
    CGParam parse() throws Exception {
        CGParam result = new CGParam();

        File xmlFl = new File(this.getXMLFile());
        if (!xmlFl.exists()) CGHelper.log(this.getLogfile().equals(""), this.getLogger(), "XML file \""+this.getXMLFile()+"\" opened faild, program abort !!!", "XML file \""+this.getXMLFile()+"\" opened faild.");;

		DocumentBuilderFactory bd = DocumentBuilderFactory.newInstance();
        DocumentBuilder bder = bd.newDocumentBuilder();
        Document doc = bder.parse(xmlFl);

        Element root = doc.getDocumentElement();
        if (root == null) {
        		CGHelper.log(this.getLogfile().equals(""), this.getLogger(), "XML file \"" + this.getXMLFile() + "\" analysed error !!!", "XML file \"" + this.getXMLFile() + "\" analysed error !!!");
            return null;
        }

        NodeList childs = root.getChildNodes();
        for (int i=0; i<childs.getLength(); i++) {
            Node node = childs.item(i);
            String node_title = node.getNodeName();
            if (node_title.equals("pair")) { // handle pair node
                CGPair p = new CGPair();
                p.setName(((Element)node).getAttribute("name"));
                p.setContent(((Element)node).getAttribute("content"));
                result.getPairs().add(p);
            } else if (node_title.equals("record")) { // handle record node
                CGRecord record = new CGRecord();
                record.setName(((Element) node).getAttribute("name"));

                NodeList rnodes = node.getChildNodes();
                for (int j=0; j<rnodes.getLength(); j++) {
                    Node rnode = rnodes.item(j);
                    if (rnode.getNodeName().equals("pair")) { // handle pair node of record node
                        CGPair p = new CGPair();
                        p.setName(((Element)rnode).getAttribute("name"));
                        p.setContent(((Element)rnode).getAttribute("content"));
                        record.getPairs().add(p);
                    }
                }

                result.getRecords().add(record);
            } else if (node_title.equals("list")) { // handle list node
                CGList list = new CGList();
                list.setName(((Element)node).getAttribute("name"));

                NodeList lnodes = node.getChildNodes();
                for (int j=0; j<lnodes.getLength(); j++) {
                		if (lnodes.item(j).getNodeName().equals("record")) { // handle record node of list node
                			Node lnode = lnodes.item(j);
                			CGRecord record = new CGRecord();

                			NodeList rnodes = lnode.getChildNodes();
                        for (int k=0; k<rnodes.getLength(); k++) {
                            Node rnode = rnodes.item(k);
                            if (rnode.getNodeName().equals("pair")) { // handle pair node for record node of list node
                                CGPair p = new CGPair();
                                p.setName(((Element)rnode).getAttribute("name"));
                                p.setContent(((Element)rnode).getAttribute("content"));
                                record.getPairs().add(p);
                            }
                        }
                        list.getRecords().add(record);
                		}
                }
                result.getLists().add(list);
            }
        }

        return result;
    }
}
