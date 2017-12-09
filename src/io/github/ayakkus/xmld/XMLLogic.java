package io.github.ayakkus.xmld;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XMLLogic {

	public static void create (List<String> files, String handler, String file, String fileRoot) {
		try {
			if(file == ""){
				file ="output";
			}
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element root = XMLLogic.createElement(doc, file);
			doc.appendChild(root);
			for(String filepath : files){
				File f = new File(filepath);
				if(f.canRead()){
					switch(handler){
					case "java":
						JavaLogic.createXMLFromJava(f, doc, root);
						break;
					default:break;
					}
				}
			}
			createXMLFile(doc,fileRoot, file);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}

	}

	public static void createXMLFile (Document doc, String output, String outFile) {
		try{
			File f = new File(output+outFile+".xml");
			if(!f.exists()){
				f.createNewFile();
			}
			TransformerFactory transformerFactory=TransformerFactory.newInstance();
			Transformer transformer=transformerFactory.newTransformer();
			DOMSource source=new DOMSource(doc);
			StreamResult result=new StreamResult(new File(output+outFile+".xml"));
			transformer.transform(source, result);
		} 
		catch (IOException | TransformerException e){}
	}

	public static Element createElement (Document doc, String title, String contents, Attr... attributes) {
		Element newElement=doc.createElement(title);
		for(Attr attr : attributes){
			newElement.setAttributeNode(attr);
		}
		newElement.appendChild(doc.createTextNode(contents));
		return newElement;
	}

	public static Element createElement (Document doc, String title, Attr... attributes) {
		return createElement(doc, title, "", attributes);
	}

	public static Element setChild (Element p, Element... c) {
		for(Element a:c) {
			p.appendChild(a);
		}
		return p;
	}
	
	public static Attr createAttribute (Document doc, String title, String value) {
		Attr at = doc.createAttribute(title);
		at.setValue(value);
		return at;
	}

}