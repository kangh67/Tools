package maude;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Integrated_Search {
	public static String dev_2016 = "C:\\Users\\hkang1.UTHSAHS\\Google Drive\\MAUDE\\filter_classifier\\2016 MAUDE\\foidev2016.txt";
	public static String text_2016 = "C:\\Users\\hkang1.UTHSAHS\\Google Drive\\MAUDE\\filter_classifier\\2016 MAUDE\\foitext2016.txt";

	public static String genericKeywords = "C:\\Users\\hkang1.UTHSAHS\\Google Drive\\MAUDE\\filter_classifier\\2016 MAUDE\\integrated search\\keywords_gene.txt";
	public static String genericExclude = "C:\\Users\\hkang1.UTHSAHS\\Google Drive\\MAUDE\\filter_classifier\\2016 MAUDE\\integrated search\\keywords_gene_exclude.txt";
	public static String manuKeywords = "C:\\Users\\hkang1.UTHSAHS\\Google Drive\\MAUDE\\filter_classifier\\2016 MAUDE\\integrated search\\keywords_manu.txt";
	public static String narrativeKeywords = "C:\\Users\\hkang1.UTHSAHS\\Google Drive\\MAUDE\\filter_classifier\\2016 MAUDE\\integrated search\\keywords_narr.txt";
	
	public static String outputFolder = "C:\\Users\\hkang1.UTHSAHS\\Google Drive\\MAUDE\\filter_classifier\\2016 MAUDE\\integrated search\\";
	
	//Generic keywords
	public static ArrayList<String> key_g = new ArrayList<String>();
	
	//Manufacturer keywords
	public static ArrayList<String> key_m = new ArrayList<String>();
	
	//Narrative keywords
	public static ArrayList<String> key_t = new ArrayList<String>();
	
	//Narrative keywords - criteria
	public static HashMap<String, String> text_key_criteria = new HashMap<String, String>();
	
	//Exclusive keywords
	public static ArrayList<String> key_e = new ArrayList<String>();
	
	//Generic keywords and their report IDs
	public static HashMap<String, HashSet<String>> gene_key_IDs = new HashMap<String, HashSet<String>>();
	
	//IDs found by generic keywords
	public static HashSet<String> gene_IDs = new HashSet<String>();
	
	//Manufacturer keywords and their report IDs
	public static HashMap<String, HashSet<String>> manu_key_IDs = new HashMap<String, HashSet<String>>();
	
	//IDs found by manufacturer keywords
	public static HashSet<String> manu_IDs = new HashSet<String>();
		
	//Narrative keywords and their report IDs
	public static HashMap<String, HashSet<String>> text_key_IDs = new HashMap<String, HashSet<String>>();
	
	//IDs found by narrative keywords
	public static HashSet<String> text_IDs = new HashSet<String>();
	
	//IDs and their generic name and manufacturer name
	public static HashMap<String, String[]> IDs_gene_manu = new HashMap<String, String[]>();
	
	//Overlap IDs between generic and manufacturer
	public static HashSet<String> overlap_gene_manu = new HashSet<String>();
	
	//Overlap IDs between generic and narrative
	public static HashSet<String> overlap_gene_text = new HashSet<String>();
	
	//Overlap IDs between narrative and manufacturer
	public static HashSet<String> overlap_manu_text = new HashSet<String>();
	
	//Overlap IDs among generic, manufacturer, and narrative
	public static HashSet<String> overlap_all = new HashSet<String>();
	
	public static void main(String[] args) throws IOException {
		//Read Generic, manufacturer, and exclusive keywords to key_g, key_m, and key_e
		readKeywords();
	
		//Apply generic and manufacturer keywords on device data
		filterOnDevice();
				
		//Apply narrative keywords on narrative data
		filterOnText();
		
		writeLog();
		
		
	}
	
	public static void readKeywords() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(genericKeywords));
		String line = "";
		
		//Generic keywords
		while((line = br.readLine()) != null)
			key_g.add(line.trim().toUpperCase());		
		
		br.close();
		
		//Exclusive keywords
		br = new BufferedReader(new FileReader(genericExclude));		
		
		while((line = br.readLine()) != null)
			key_e.add(line.trim().toUpperCase());	
		
		br.close();
		
		//Manufacturer keywords
		br = new BufferedReader(new FileReader(manuKeywords));		
		
		while((line = br.readLine()) != null)
			key_m.add(line.trim().toUpperCase());		
		
		br.close();
		
		//Narrative keywords
		br = new BufferedReader(new FileReader(narrativeKeywords));		
				
		while((line = br.readLine()) != null) {
			String[] thisline = line.split("\t");
			key_t.add(thisline[0].trim().toUpperCase());
			text_key_criteria.put(thisline[0].trim().toUpperCase(), thisline[1].trim().toUpperCase());
		}
				
		br.close();
	}

	public static void filterOnDevice() throws IOException {		
		/*
		 * Get MDR_REPORT_KEYs whose generic name contains any generic keywords
		 */		
		BufferedReader br = new BufferedReader(new FileReader(dev_2016));		
		String line = br.readLine(); //the title line;		
		
		while((line = br.readLine()) != null) {
			String[] thisline = line.split("\\|");
			String name_g = nameFilter(thisline[7]);
			String name_m = nameFilter(thisline[8]);
			
			for(int i=0; i<key_g.size(); i++) {
				if(advancedSearch(key_g.get(i), name_g)) {
					boolean excludeFlag = false;
					
					for(int j=0; j<key_e.size(); j++) {
						if(name_g.contains(key_e.get(j)))
							excludeFlag = true;
					}
					
					if(excludeFlag)
						break;
					
					gene_IDs.add(thisline[0]);
					
					if(gene_key_IDs.containsKey(key_g.get(i)))
						gene_key_IDs.get(key_g.get(i)).add(thisline[0]);
					else {
						HashSet<String> set = new HashSet<String>();
						set.add(thisline[0]);
						gene_key_IDs.put(key_g.get(i), set);
					}
					
					if(!IDs_gene_manu.containsKey(thisline[0])) {
						String[] twoNames = new String[2];
						twoNames[0] = name_g;
						twoNames[1] = name_m;
						IDs_gene_manu.put(thisline[0], twoNames);
					}					
				}
			}
			
			for(int i=0; i<key_m.size(); i++) {
				if(advancedSearch(key_m.get(i), name_m)) {
					boolean excludeFlag = false;
					
					for(int j=0; j<key_e.size(); j++) {
						if(name_g.contains(key_e.get(j)))
							excludeFlag = true;
					}
					
					if(excludeFlag)
						break;
					
					manu_IDs.add(thisline[0]);
					
					if(manu_key_IDs.containsKey(key_m.get(i)))
						manu_key_IDs.get(key_m.get(i)).add(thisline[0]);
					else {
						HashSet<String> set = new HashSet<String>();
						set.add(thisline[0]);
						manu_key_IDs.put(key_m.get(i), set);
					}
					
					if(!IDs_gene_manu.containsKey(thisline[0])) {
						String[] twoNames = new String[2];
						twoNames[0] = name_g;
						twoNames[1] = name_m;
						IDs_gene_manu.put(thisline[0], twoNames);
					}					
				}
			}
		}
		
		br.close();		
	}
	
	public static void filterOnText() throws IOException {		
		BufferedReader br = new BufferedReader(new FileReader(text_2016));		
		String line = br.readLine(); //the title line;
		
		while((line = br.readLine()) != null) {			
			String[] thisline = line.split("\\|");		
			
			if(thisline.length < 6) 
				continue;
		
			if(thisline.length > 6) {
				for(int i=5; i<thisline.length-1; i++) {
					thisline[5] = thisline[5] + "|" + thisline[i+1];
				}
			}
			
			for(int i=0; i<key_t.size(); i++) {
				if(foundKey(text_key_criteria.get(key_t.get(i)), thisline[5])) {
					boolean excludeFlag = false;
					
					for(int j=0; j<key_e.size(); j++) {
						if(thisline[5].contains(key_e.get(j)))
							excludeFlag = true;
					}
					
					if(excludeFlag)
						break;
					
					text_IDs.add(thisline[0]);
					
					if(text_key_IDs.containsKey(key_t.get(i)))
						text_key_IDs.get(key_t.get(i)).add(thisline[0]);
					else {
						HashSet<String> set = new HashSet<String>();
						set.add(thisline[0]);
						text_key_IDs.put(key_t.get(i), set);
					}
				}
			}					
		}
		
		br.close();		
	}
	
	public static void writeLog() throws IOException {
		DataOutputStream dos = new DataOutputStream(new FileOutputStream(new File(outputFolder + "log.txt")));
		
		for(String ID : gene_IDs) {
			if(manu_IDs.contains(ID))
				overlap_gene_manu.add(ID);
			if(text_IDs.contains(ID))
				overlap_gene_text.add(ID);
			if(manu_IDs.contains(ID) && text_IDs.contains(ID))
				overlap_all.add(ID);
		}
		
		for(String ID : manu_IDs) {
			if(text_IDs.contains(ID))
				overlap_manu_text.add(ID);
		}
		
		dos.writeBytes("=== " + gene_IDs.size() + " reports found by " + key_g.size() + " generic keywords ===" + "\r\n");
		dos.writeBytes("=== " + manu_IDs.size() + " reports found by " + key_m.size() + " manufacturer keywords ===" + "\r\n");
		dos.writeBytes("=== " + text_IDs.size() + " reports found by " + key_t.size() + " narrative keywords ===" + "\r\n");
		dos.writeBytes("\r\n");
		dos.writeBytes("=== " + overlap_gene_manu.size() + " reports found by both generic and manufacturer keywords ===" + "\r\n");
		dos.writeBytes("=== " + overlap_gene_text.size() + " reports found by both generic and narrative keywords ===" + "\r\n");
		dos.writeBytes("=== " + overlap_manu_text.size() + " reports found by both manufacturer and narrative keywords ===" + "\r\n");
		dos.writeBytes("=== " + overlap_all.size() + " reports found by all three types of keywords ===" + "\r\n");
		dos.writeBytes("\r\n");
		dos.writeBytes("=== Generic keywords statistics ===" + "\r\n");		
		for(String key : gene_key_IDs.keySet())
			dos.writeBytes(key + "\t" + gene_key_IDs.get(key).size() + "\r\n");
		
		dos.writeBytes("\r\n");
		dos.writeBytes("=== Manufacturer keywords statistics ===" + "\r\n");		
		for(String key : manu_key_IDs.keySet())
			dos.writeBytes(key + "\t" + manu_key_IDs.get(key).size() + "\r\n");
		
		dos.writeBytes("\r\n");
		dos.writeBytes("=== Narrative keywords statistics ===" + "\r\n");		
		for(String key : text_key_IDs.keySet())
			dos.writeBytes(key + "\t" + text_key_IDs.get(key).size() + "\r\n");	
		
		dos.close();
	}
	
	public static String nameFilter(String s) {
		if(s.length() < 1)
			return s;
		else if(s.charAt(s.length()-1) == ',')
			return s.substring(0, s.length()-1);
		else
			return s;
	}
	
	public static boolean foundKey(String criteria, String narrative) {
		String[] or = criteria.split("\\|");
		if(or.length > 1) {
			boolean res = false;
			for(int i=0; i<or.length; i++)
				res |= foundKey(or[i], narrative);
			return res;
		}
		String[] and = criteria.split("&");
		if(and.length > 1) {
			boolean res = true;
			for(int i=0; i<and.length; i++)
				res &= foundKey(and[i], narrative);
			return res;
		}
		return narrative.matches(".*[^A-Z]" + criteria + "[^A-Z].*") | narrative.matches(criteria + "[^A-Z].*") | narrative.matches(".*[^A-Z]" + criteria) | narrative.matches(criteria);
	}
	
	public static boolean advancedSearch(String keyword, String text) {
		return text.matches(".*[^A-Z]" + keyword + "[^A-Z].*") | text.matches(keyword + "[^A-Z].*") | text.matches(".*[^A-Z]" + keyword) | text.matches(keyword);
	}
}
