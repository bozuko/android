package com.fuzz.android.globals;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Node;

public class GlobalFunctions {


	private static String prettyPrint(String xml,int indent){
		//Log.v("XML", xml);
		StringBuilder string = new StringBuilder();
		//string.append("\t");
		for(int i=0; i<indent; i++){
			string.append("\t");
		}
		boolean close = false;
		boolean open = false;
		for(int i=0; i<xml.length(); i++){
			if(close){
				if(xml.charAt(i) == '>'){
					string.append(xml.charAt(i));
					//string.append(prettyPrint(xml.substring(i+1),indent-1));
					//i = xml.length();

					//					Log.v("StringBuilder", string.toString());
					close = false;
					//indent--;
					string.append("\n");
					for(int j=0; j<indent; j++){
						string.append("\t");
					}
				}else{
					string.append(xml.charAt(i));
				}
			}else if(open){
				if(xml.charAt(i) == '/'){
					if(xml.charAt(i+1) == '>'){
						string.append(xml.charAt(i++));
						string.append(xml.charAt(i));
						//						Log.v("StringBuilder", string.toString());
						open = false;
						string.append("\n");
						for(int j=0; j<indent; j++){
							string.append("\t");
						}
					}else{
						string.append(xml.charAt(i));
					}
				}
				else if(xml.charAt(i) == '>'){
					string.append(xml.charAt(i));
					//string.append(prettyPrint(xml.substring(i+1),indent+1));
					//i = xml.length();
					open = false;
					//					Log.v("StringBuilder", string.toString());
					indent++;
					string.append("\n");
					for(int j=0; j<indent; j++){
						string.append("\t");
					}
				}else{
					string.append(xml.charAt(i));
				}
			}else if(xml.charAt(i) == '<'){
				if(xml.charAt(i+1) == '/'){
					//close = true;
					//					Log.v("StringBuilder", string.toString());
					close = true;
					indent--;
					string.append("\n");
					for(int j=0; j<indent; j++){
						string.append("\t");
					}

					string.append(xml.charAt(i++));
					string.append(xml.charAt(i));
				}else{
					open = true;
					string.append(xml.charAt(i));
				}
			}else{
				string.append(xml.charAt(i));
			}
		}


		return string.toString();
	}

	public static String prettyPrint(String xml) {
		// TODO Auto-generated method stub
		StringBuilder string = new StringBuilder();

		string.append(prettyPrint(xml,0));


		return string.toString();
	}

	private static final long POLY64REV = 0x95AC9329AC4BC9B5L;
	private static final long INITIALCRC = 0xFFFFFFFFFFFFFFFFL;

	private static boolean init = false;
	private static long[] CRCTable = new long[256];

	public static final long Crc64Long(String in) {
		if (in == null || in.length() == 0) {
			return 0;
		}
		// http://bioinf.cs.ucl.ac.uk/downloads/crc64/crc64.c
		long crc = INITIALCRC, part;
		if (!init) {
			for (int i = 0; i < 256; i++) {
				part = i;
				for (int j = 0; j < 8; j++) {
					int value = ((int) part & 1);
					if (value != 0)
						part = (part >> 1) ^ POLY64REV;
					else
						part >>= 1;
				}
				CRCTable[i] = part;
			}
			init = true;
		}
		int length = in.length();
		for (int k = 0; k < length; ++k) {
			char c = in.charAt(k);
			crc = CRCTable[(((int) crc) ^ c) & 0xff] ^ (crc >> 8);
		}
		return crc;
	}


	public static String getNodeText(Node n) {
		// TODO Auto-generated method stub
		if(n != null){
			//Log.v("NODE",n.getNodeName() + " " + n.getNodeValue());
			if(n.hasChildNodes()){
				if(n.getFirstChild().getNodeType() == Node.TEXT_NODE || n.getFirstChild().getNodeType() == Node.CDATA_SECTION_NODE){
					String value = "";
					for(int j=0; j<n.getChildNodes().getLength(); j++){
						if (n.getChildNodes().item(j).getNodeType() == Node.CDATA_SECTION_NODE)
						{
							CDATASection section = (CDATASection)n.getChildNodes().item(j);
							value = value + section.getNodeValue();
						}else{
							value = value + n.getChildNodes().item(j).getNodeValue();
						}
					}
					return value;
				}
			}else{
				return n.getNodeValue();
			}
		}

		return "";
	}

	public static String sanitize(String inString){
		if(inString == null){
			return "0.00";
		}else{
			try{
				Double.parseDouble(inString);
				return inString;
			}catch(Throwable t){
				return "0.00";
			}
		}
	}
}
