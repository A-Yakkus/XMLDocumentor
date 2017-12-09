package io.github.ayakkus.xmld;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A Class reader that generates XML elements from keywords in the class.
 * TODO implement comments
 * @author Yak
 * 
 */
public class JavaLogic {

	public static final String[] KEY_WORDS_ACCESS = new String[]{"public","private","protected"};
	public static final String[] KEY_WORDS_TYPE = new String[]{"class","enum","interface","@interface"};
	public static final String[] KEY_WORDS_MOD = new String[]{"static","volatile","final","abstract","throws","throw"};
	public static final String[] KEY_WORDS_DATA_TYPE = new String[]{"int","long","short","byte","boolean","char", "double","float","void"};
	
	public static void createXMLFromJava (File f, Document doc, Element r) {
		String filename = f.getName().substring(0, f.getName().length()-5);
		try
		{
			BufferedReader br2 = new BufferedReader(new FileReader(f));
			String s = "";
			Element root = createRootElement(br2, doc, filename);
			r.appendChild(root);
			Element constructorGroup = XMLLogic.createElement(doc, "Constructors");
			Element methodGroup = XMLLogic.createElement(doc, "Methods");
			Element fieldGroup = XMLLogic.createElement(doc, "Fields");
			Element metadata = XMLLogic.createElement(doc, "Metadata");
			XMLLogic.setChild(root, metadata,constructorGroup, methodGroup, fieldGroup);
			br2.close();
			BufferedReader br = new BufferedReader(new FileReader(f));
			List<String> text = new ArrayList<String>();
			while((s=br.readLine())!=null)
			{
				if(s.contains("package")&&!s.contains("(")){
					XMLLogic.setChild(metadata, XMLLogic.createElement(doc, "package", s.split(" ")[1].substring(0, s.split(" ")[1].length()-1)));
				}
				if(s.contains("import")&&!s.contains("(")){
					XMLLogic.setChild(metadata, XMLLogic.createElement(doc, "import", s.split(" ")[1].substring(0, s.split(" ")[1].length()-1)));
				}
				//Remove Tabs
				while(s.contains("\t"))
				{
					s=s.substring(1);
				}
				if(s.contains(" = ")&&!s.contains("if(")){
					Attr[] arr=createAttrsF(doc,s);
					XMLLogic.setChild(fieldGroup, XMLLogic.setChild(XMLLogic.createElement(doc, "field", arr), XMLLogic.createElement(doc, "fieldName",s.split(" ")[arr.length]), XMLLogic.createElement(doc, "value", s.split(" = ")[1])));
				}
				//code block
				if(s.contains("{")&&!s.contains("}"))
				{
					//constructor
					if(s.contains(filename+"("))
					{
						Element construct=XMLLogic.createElement(doc, "constructor", createAttrs(doc, s, false));
						construct=generateParameters(construct, s);
						
						XMLLogic.setChild(constructorGroup, construct);
					}
					else if(s.contains(") {")&&!s.contains("if("))
					{
						Attr[] arr=createAttrs(doc, s, true);
						Element method = generateParameters(XMLLogic.createElement(doc, "method" , arr),s);
						XMLLogic.setChild(methodGroup, method);
						XMLLogic.setChild(method, getAssignmentsInMethod(br, method, doc, s));
					}
				}
				text.add(s);
			}
			br.close();
			testForComments(text, doc, root, filename);
		}
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

	private static void testForComments(List<String> text, Document doc, Element root, String fName) {
		List<String> comment = new ArrayList<String>();
		Element comments = XMLLogic.createElement(doc, fName+"-comments");
		int indexA = 0;
		int indexB = 0;
		for(String s : text){
			if(s.contains(" class ") && !s.contains("=") && !s.contains("*")){
				indexA = text.indexOf(s);
				for(int i = 0; i<indexA;i++){
					String t = text.get(i);
					if(t.contains("/*")){
						while(!t.contains("*/")){t=text.get(i);comment.add(t);i++;};
					}
				}
				String result=GetListAsString(comment);
				Element classCom = XMLLogic.createElement(doc, "classComment");
				classCom.setTextContent(result);
				XMLLogic.setChild(comments, classCom);
				comment = new ArrayList<String>();
			}
			else if(s.contains(fName)){
				indexB = text.indexOf(s);
				for(int i = indexA; i<indexB;i++){
					String t = text.get(i);
					if(t.contains("/*")){
						while(!t.contains("*/")){comment.add(t);i++;t=text.get(i);};
					}
				}
				String result=GetListAsString(comment);
				Element conStructCom = XMLLogic.createElement(doc, "constructorComment");
				conStructCom.setTextContent(result);
				XMLLogic.setChild(comments, conStructCom);
				comment = new ArrayList<String>();
			}
		}
		XMLLogic.setChild(root, comments);
	}

	private static String GetListAsString(List<String> text) {
		String ret = "";
		for(String s : text){
			ret+=s;
		}
		ret = ret.replaceAll("\r", " ");
		ret = ret.replaceAll("\n", " ");
		ret = ret.replaceAll(" * ", " ");
		return ret;
	}

	private static Element getAssignmentsInMethod(BufferedReader br, Element root, Document doc, String title) {
		String s = "";
		Element e = XMLLogic.createElement(doc, "MethodWork", XMLLogic.createAttribute(doc, "name", title.split("\\(")[0].split(" ")[title.split("\\(")[0].split(" ").length-1]));
		try {
			while((s=br.readLine())!=null){
				if(s.equals("\t}")){
					break;
				}
				while(s.contains("\t"))
				{
					s=s.substring(1);
				}
				if(s.contains(" = ")&&!s.contains("if(")){
					Attr[] arr=createAttrsF(doc,s);
					XMLLogic.setChild(e, XMLLogic.setChild(XMLLogic.createElement(doc, "assignment", arr), XMLLogic.createElement(doc, "name",s.split(" ")[arr.length] )));
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return e;
	}

	private static Element createRootElement (BufferedReader br, Document doc, String filename) {
		String s ="";
		Element root=null;
		try {
			while((s=br.readLine())!=null){
				
				for(String t:KEY_WORDS_TYPE){
					if(s.contains(t) && root==null){
						root=XMLLogic.createElement(doc, t, createAttrsF(doc, s));
						Element supers=XMLLogic.createElement(doc, "super");
						if(s.contains("extends")){
							String[] strA=s.split(" ");
							int c=0;
							for(int i=0;i<strA.length;i++){
								if(strA[i].matches("extends")){
									c=i+1;
									break;
								}
							}
							XMLLogic.setChild(supers, XMLLogic.createElement(doc, "extend",strA[c]));
						}
						/*if(s.contains("implements")){
							String interfaces=s.split("implements")[1];
							for(String u:interfaces.split(", ")){
								XMLLogic.setChild(supers, XMLLogic.createElement(doc, "implInterface", u));
							}
						}*/
						XMLLogic.setChild(root, XMLLogic.createElement(doc, "name", filename));
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}	

		return root;
	}

	private static Attr[] createAttrs (Document doc, String input, boolean method) {
		List<Attr> attrs=new ArrayList<Attr>();
		int count=0;
		for(String t: KEY_WORDS_ACCESS)
		{
			if(input.contains(t)){
				attrs.add(XMLLogic.createAttribute(doc, "access", t));
			}
		}
		for(String t: KEY_WORDS_MOD)
		{
			if(input.contains(t))
			{
				attrs.add(XMLLogic.createAttribute(doc, "modifier"+count, t));
				count++;
			}

		}
		Attr[] ret=null;
		if(method)
		{
			ret=new Attr[attrs.size()+1];
			count=0;
			for(Attr a : attrs)
			{
				ret[count]=a;
				count++;
			}
			ret[count]=XMLLogic.createAttribute(doc, "returnType", input.split(" ")[attrs.size()]);
		}
		else
		{
			ret=new Attr[attrs.size()];
			count=0;
			for(Attr a : attrs)
			{
				ret[count]=a;
				count++;
			}
		}
		return ret;
	}

	private static Attr[] createAttrsF (Document doc, String input) {
		List<Attr> attrs=new ArrayList<Attr>();
		int count=0;
		input=input.split("=")[0];
		for(String t: KEY_WORDS_ACCESS)
		{
			if(input.contains(t))
			{
				attrs.add(XMLLogic.createAttribute(doc, "access", t));
			}
		}
		for(String t: KEY_WORDS_MOD)
		{
			if(input.contains(t))
			{
				attrs.add(XMLLogic.createAttribute(doc, "modifier"+count, t));
				count++;
			}
		}
		Attr[] ret=null;

		ret=new Attr[attrs.size()+1];
		count=0;
		for(Attr a : attrs)
		{
			ret[count]=a;
			count++;
		}
		ret[count]=XMLLogic.createAttribute(doc, "returnType", input.split(" ")[attrs.size()]);
		return ret;
	}

	private static Element generateParameters (Element root, String lineIn) {
		String[] params = getParam(lineIn);
		if(params.length>0)
		{
			for(String par:params)
			{
				XMLLogic.setChild(root, XMLLogic.createElement(root.getOwnerDocument(), "parameter", par.split(" ")[1], XMLLogic.createAttribute(root.getOwnerDocument(), "datatype", par.split(" ")[0].replace('(', ' ').trim())));
			}
		}
		return root;
	}

	private static String[] getParam (String lineIn) {
		String[] params=new String[]{};
		if(lineIn.substring(lineIn.indexOf("("), lineIn.indexOf(")")).split(", ").length>1){
			params=lineIn.substring(lineIn.indexOf("("), lineIn.indexOf(")")).split(", ");
		}
		else if(lineIn.indexOf("(")-lineIn.indexOf(")")<-1){
			params=new String[]{lineIn.substring(lineIn.indexOf("("), lineIn.indexOf(")"))};
		}
		return params;
	}

}
