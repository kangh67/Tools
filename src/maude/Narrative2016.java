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
import java.util.Map.Entry;
import java.util.Set;

public class Narrative2016 {
	/**
	 * 2016
	 
	public static String dev_2016 = "C:\\Users\\hkang1.UTHSAHS\\Google Drive\\MAUDE\\filter_classifier\\2016 MAUDE\\foidev2016.txt";
	public static String text_2016 = "C:\\Users\\hkang1.UTHSAHS\\Google Drive\\MAUDE\\filter_classifier\\2016 MAUDE\\foitext2016.txt";

	public static String genericKeywords = "C:\\Users\\hkang1.UTHSAHS\\Google Drive\\MAUDE\\filter_classifier\\genericKeywordsList.txt";
	public static String genericExclude = "C:\\Users\\hkang1.UTHSAHS\\Google Drive\\MAUDE\\filter_classifier\\genericExcludeList.txt";
	public static String manuKeywords = "C:\\Users\\hkang1.UTHSAHS\\Google Drive\\MAUDE\\filter_classifier\\manuKeywordsList.txt";
	
	public static String outputFolder = "C:\\Users\\hkang1.UTHSAHS\\Google Drive\\MAUDE\\filter_classifier\\2016 MAUDE\\";
	**/
	
	/**
	 * 2008-2010; 2011-2013; 2014-2016
	 */
	public static String dev = "C:\\Users\\hkang1.UTHSAHS\\Google Drive\\MAUDE\\filter_classifier\\2011-13 MAUDE\\foidev2013.txt";
	public static String text = "C:\\Users\\hkang1.UTHSAHS\\Google Drive\\MAUDE\\filter_classifier\\2011-13 MAUDE\\foitext2013.txt";
	public static String master = "C:\\Users\\hkang1.UTHSAHS\\Google Drive\\MAUDE\\filter_classifier\\mdrfoiThru2016.txt";

	public static String genericKeywords = "C:\\Users\\hkang1.UTHSAHS\\Google Drive\\MAUDE\\filter_classifier\\2016 MAUDE\\integrated search\\keywords_gene.txt";
	public static String genericExclude = "C:\\Users\\hkang1.UTHSAHS\\Google Drive\\MAUDE\\filter_classifier\\2016 MAUDE\\integrated search\\keywords_gene_exclude.txt";
	public static String manuKeywords = "C:\\Users\\hkang1.UTHSAHS\\Google Drive\\MAUDE\\filter_classifier\\2016 MAUDE\\integrated search\\keywords_manu.txt";
	
	public static String outputFolder = "C:\\Users\\hkang1.UTHSAHS\\Google Drive\\MAUDE\\filter_classifier\\2011-13 MAUDE\\event\\";
	
	//all MDR this year
	public static HashSet<String> MDRthisyear = new HashSet<String>();
	
	//all events in this year. <REPORT_NUMBER(0), [REPORT_NUMBER-1, REPORT_NUMBER-2,...]>
	public static HashMap<String, HashSet<String>> event_all = new HashMap<String, HashSet<String>>();
	
	//events filtered by generic name. <REPORT_NUMBER(0), [REPORT_NUMBER-1, REPORT_NUMBER-2,...]>
	public static HashMap<String, HashSet<String>> event_gene = new HashMap<String, HashSet<String>>();
	
	//events filtered by manufacturer name. <REPORT_NUMBER(0), [REPORT_NUMBER-1, REPORT_NUMBER-2,...]>
	public static HashMap<String, HashSet<String>> event_manu = new HashMap<String, HashSet<String>>();
	
	//unique events filtered by generic name and manufacturer name. <REPORT_NUMBER(0), [REPORT_NUMBER-1, REPORT_NUMBER-2,...]>
	public static HashMap<String, HashSet<String>> event_merge = new HashMap<String, HashSet<String>>();
	
	public static void main(String[] args) throws IOException {
		//Get <MDR_REPORT_KEY, {GENERIC NAME, MANUFACTURER_NAME}> by using generic name filter
		System.out.println("Applying generic name filter... (filterOnDevice_generic)");
		HashMap<String, String[]> hm_filterGeneric = filterOnDevice_generic();
		
		//Get <MDR_REPORT_KEY, {GENERIC NAME, MANUFACTURER_D_NAME}> by using manufacturer name filter
		System.out.println("Applying manufacturer name filter...(filterOnDevice_manu)");
		HashMap<String, String[]> hm_filterManu = filterOnDevice_manu();
		
		//Analyze and add event number
		System.out.println("Analyzing unique events of the filtered reports... (analyzeEvent)");
		hm_filterGeneric = analyzeEvent(hm_filterGeneric, "Generic");
		hm_filterManu = analyzeEvent(hm_filterManu, "Manu");
		
		System.out.println("--- Event number this year: " + event_all.size());
		System.out.println("--- Event number filtered by generic names: " + event_gene.size());
		System.out.println("--- Event number filtered by manufacturer names: " + event_manu.size());
		
		//Remove the overlaps with hm_filterGeneric from hm_filterManu
		System.out.println("Analyzing overlaps...(removeOverlapsFromManu)");
		hm_filterManu = removeOverlapsFromManu(hm_filterGeneric, hm_filterManu);
		
		//Get and write filtering result by using generic names
		System.out.println("Adding narrative fields to the filtered reports...(filterOnText)");
		filterOnText(hm_filterGeneric, hm_filterManu);		
		
		//write filtering result by using generic names			
		System.out.println("Writing all filtered reports to a CVS file...(writeResult_CSV)");
		writeResult_TXT(hm_filterGeneric, hm_filterManu);
		
		System.out.println("Completed!");
	}
	
	public static HashMap<String, String[]> filterOnDevice_generic() throws IOException {
		/*
		 * Get generic keywords
		 */
		ArrayList<String> keywords = new ArrayList<String>();		
		BufferedReader br_generic = new BufferedReader(new FileReader(genericKeywords));
		HashMap<String, Integer> keywords_stat = new HashMap<String, Integer>();
		String line = "";
		
		while((line = br_generic.readLine()) != null) {
			keywords.add(line.trim().toUpperCase());
			keywords_stat.put(line.trim().toUpperCase(), 0);
		}
		
		br_generic.close();
		//---------------
		
		/*
		 * Get generic excluded keywords
		 */
		ArrayList<String> excludes = new ArrayList<String>();		
		BufferedReader br_exclude = new BufferedReader(new FileReader(genericExclude));		
		
		while((line = br_exclude.readLine()) != null) {
			excludes.add(line.trim().toUpperCase());			
		}
		
		br_exclude.close();
		//---------------
		
		/*
		 * Get MDR_REPORT_KEYs whose generic name contains any generic keywords
		 */
		HashMap<String, String[]> hm = new HashMap<String, String[]>();
		BufferedReader br = new BufferedReader(new FileReader(dev));
		
		line = br.readLine(); //the title line;		
		int total = 0;
		
		while((line = br.readLine()) != null) {
			String[] thisline = line.split("\\|");
			if(thisline.length < 9)
				continue;
			String name_g = nameFilter(thisline[7]);
			String name_m = nameFilter(thisline[8]);
			String name_b = nameFilter(thisline[6]);
			
			for(int i=0; i<keywords.size(); i++) {
				if(advancedSearch(keywords.get(i), name_g)) {
					boolean excludeFlag = false;
					
					for(int j=0; j<excludes.size(); j++) {
						if(name_g.contains(excludes.get(j)))
							excludeFlag = true;
					}
					
					if(excludeFlag)
						break;
					
					if(!hm.containsKey(thisline[0])) {
						String[] threeNames = new String[9];
						threeNames[0] = name_g;
						threeNames[1] = name_m;
						threeNames[2] = name_b;
						hm.put(thisline[0], threeNames);
					}
					
					keywords_stat.put(keywords.get(i), keywords_stat.get(keywords.get(i)) + 1);
					break;
				}				
			}			
			total ++;
			MDRthisyear.add(thisline[0]);
		}
		
		br.close();
		//-----
		
		int matchedKeys = 0;
		for(int i=0; i<keywords.size(); i++) {
			if(keywords_stat.get(keywords.get(i)) != 0)
				matchedKeys ++;	
		}
		
		/*
		for (Entry<String, String> entry : hm.entrySet()) {
			System.out.println(entry.getKey() + ":" + entry.getValue());
		}
		*/
		
		System.out.println("--- Total device records: " + total);
		System.out.println("--- Filtered by generic names: " + hm.size());
		System.out.println("--- Matched generic keywords: " + matchedKeys + "/" + keywords.size());
		
		return hm;
	}
	
	public static String nameFilter(String s) {
		if(s.length() < 1)
			return s;
		else if(s.charAt(s.length()-1) == ',')
			return s.substring(0, s.length()-1);
		else
			return s;
	}
	
	public static HashMap<String, String[]> filterOnDevice_manu() throws IOException {
		/*
		 * Get manufacturer keywords
		 */
		ArrayList<String> keywords = new ArrayList<String>();		
		BufferedReader br_manu = new BufferedReader(new FileReader(manuKeywords));
		HashMap<String, Integer> keywords_stat = new HashMap<String, Integer>();
		String line = "";
		
		while((line = br_manu.readLine()) != null) {
			keywords.add(line.trim().toUpperCase());
			keywords_stat.put(line.trim().toUpperCase(), 0);
		}
		
		br_manu.close();
		//---------------
		
		/*
		 * Get generic excluded keywords
		 */
		ArrayList<String> excludes = new ArrayList<String>();		
		BufferedReader br_exclude = new BufferedReader(new FileReader(genericExclude));		
		
		while((line = br_exclude.readLine()) != null) {
			excludes.add(line.trim().toUpperCase());			
		}
		
		br_exclude.close();
		//---------------
		
		/*
		 * Get MDR_REPORT KEYs whose manufacturer name contains any manufacturer keywords
		 */
		HashMap<String, String[]> hm = new HashMap<String, String[]>();
		BufferedReader br = new BufferedReader(new FileReader(dev));
		
		line = br.readLine(); //the title line;
		int total = 0;
		
		while((line = br.readLine()) != null) {
			String[] thisline = line.split("\\|");
			if(thisline.length < 9)
				continue;
			String name_g = nameFilter(thisline[7]);
			String name_m = nameFilter(thisline[8]);
			String name_b = nameFilter(thisline[6]);
			
			for(int i=0; i<keywords.size(); i++) {
				if(advancedSearch(keywords.get(i), name_m)) {
					boolean excludeFlag = false;
					
					for(int j=0; j<excludes.size(); j++) {
						if(name_g.contains(excludes.get(j)))
							excludeFlag = true;
					}
					
					if(excludeFlag)
						break;
					
					if(!hm.containsKey(thisline[0])) {
						String[] threeNames = new String[9];
						threeNames[0] = name_g;
						threeNames[1] = name_m;
						threeNames[2] = name_b;
						hm.put(thisline[0], threeNames);
					}	
					
					keywords_stat.put(keywords.get(i), keywords_stat.get(keywords.get(i)) + 1);
					break;
				}
			}	
			total ++;
		}
		
		br.close();
		//-----
		
		int matchedKeys = 0;
		for(int i=0; i<keywords.size(); i++) {
			if(keywords_stat.get(keywords.get(i)) != 0)
				matchedKeys ++;	
		}
		
		/*
		for (Entry<String, String> entry : hm.entrySet()) {
			System.out.println(entry.getKey() + ":" + entry.getValue());
		}
		*/
		
		System.out.println("--- Total device records: " + total);
		System.out.println("--- Filtered by manufacturer names: " + hm.size());
		System.out.println("--- Matched manufacturer keywords: " + matchedKeys + "/" + keywords.size());
		
		return hm;
	}
	
	public static HashMap<String, String[]> analyzeEvent(HashMap<String, String[]> genericOrManu, String type) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(master));
		String line = br.readLine();
		
		while((line = br.readLine()) != null) {
			String[] thisline = line.split("\\|");
			String[] reportNumber = thisline[2].split("-");
			
			if(MDRthisyear.contains(thisline[0])) {
				if(event_all.containsKey(reportNumber[0]))
					event_all.get(reportNumber[0]).add(thisline[0]);
				else {
					HashSet<String> newarray = new HashSet<String>();
					newarray.add(thisline[0]);
					event_all.put(reportNumber[0], newarray);
				}
			}			
			
			if(genericOrManu.containsKey(thisline[0].trim())) {	
				if(type.equalsIgnoreCase("Generic")) {
					if(event_gene.containsKey(reportNumber[0]))
						event_gene.get(reportNumber[0]).add(thisline[0]);
					else {
						HashSet<String> newarray = new HashSet<String>();
						newarray.add(thisline[0]);
						event_gene.put(reportNumber[0], newarray);
					}
				}else if(type.equalsIgnoreCase("Manu")) {
					if(event_manu.containsKey(reportNumber[0]))
						event_manu.get(reportNumber[0]).add(thisline[0]);
					else {
						HashSet<String> newarray = new HashSet<String>();
						newarray.add(thisline[0]);
						event_manu.put(reportNumber[0], newarray);
					}
				}
				genericOrManu.get(thisline[0])[3] = reportNumber[0];
				genericOrManu.get(thisline[0])[4] = thisline[2];
			}
		}
		
		br.close();	
		
		return genericOrManu;
	}
	
	public static HashMap<String, String[]> removeOverlapsFromManu(HashMap<String, String[]> generic, HashMap<String, String[]> manu) throws IOException {
		HashMap<String, String[]> newManu = new HashMap<String, String[]>();
		
		for (Entry<String, String[]> entry : manu.entrySet()) {
			if(!generic.containsKey(entry.getKey()))
				newManu.put(entry.getKey(), entry.getValue());
		}
		
		for(String event : event_gene.keySet()) {
			event_merge.put(event, event_gene.get(event));
		}
		
		for(String event : event_manu.keySet()) {
			if(event_merge.containsKey(event)) {
				for(String s : event_manu.get(event))
					event_merge.get(event).add(s);
			}
			else
				event_merge.put(event, event_gene.get(event));
		}
		
		System.out.println("--- Overlaped MDR between generic and manufacturer results: " + (manu.size() - newManu.size()));
		System.out.println("--- Unique MDR between generic and manufacturer results: " + (generic.size() + newManu.size()));
		System.out.println("--- Unique events between generic and manufacturer results: " + event_merge.size());
		
		return newManu;
	}
	
	public static void filterOnText(HashMap<String, String[]> generic, HashMap<String, String[]> manu) throws IOException {			
		Set<String> unique = new HashSet<String>();
		BufferedReader br = new BufferedReader(new FileReader(text));
		int total = 0;
		
		String line = br.readLine(); //the title line;
		
		while((line = br.readLine()) != null) {
			total ++;
			String[] thisline = line.split("\\|");
			
			//System.out.println(total + ": " + thisline[0] + "|" +thisline[1]);
			
			unique.add(thisline[0]);			
			
			//add text to the reports filtered by generic and manufacturer names 
			if(generic.containsKey(thisline[0])) {
				if(thisline.length < 6) 
					continue;
			
				if(thisline.length > 6) {
					for(int i=5; i<thisline.length-1; i++) {
						thisline[5] = thisline[5] + "|" + thisline[i+1];
					}
				}
				
				//D=B5 Event Description
				if(thisline[2].equalsIgnoreCase("D")) {
					if(generic.get(thisline[0])[5] == null)
						generic.get(thisline[0])[5] = thisline[5];
					else
						generic.get(thisline[0])[5] += "><" + thisline[5];
				}
				//D=H3 Device Evaluated by Manufacturer
				else if(thisline[2].equalsIgnoreCase("E")) {
					if(generic.get(thisline[0])[7] == null)
						generic.get(thisline[0])[7] = thisline[5];
					else
						generic.get(thisline[0])[7] += "><" + thisline[5];
				}
				//N=H10 Additional Mfr Narrative
				else if(thisline[2].equalsIgnoreCase("N")) {
					if(generic.get(thisline[0])[6] == null)
						generic.get(thisline[0])[6] = thisline[5];
					else
						generic.get(thisline[0])[6] += "><" + thisline[5];
				}
				//No text type Code
				else {
					if(generic.get(thisline[0])[8] == null)
						generic.get(thisline[0])[8] = thisline[5];
					else
						generic.get(thisline[0])[8] += "><" + thisline[5];
				}									
			}
			if(manu.containsKey(thisline[0])) {
				if(thisline.length < 6) 
					continue;
			
				if(thisline.length > 6) {
					for(int i=5; i<thisline.length-1; i++) {
						thisline[5] = thisline[5] + "|" + thisline[i+1];
					}
				}
				
				//D=B5 Event Description
				if(thisline[2].equalsIgnoreCase("D")) {
					if(manu.get(thisline[0])[5] == null)
						manu.get(thisline[0])[5] = thisline[5];
					else
						manu.get(thisline[0])[5] += "><" + thisline[5];
				}
				//D=H3 Device Evaluated by Manufacturer
				else if(thisline[2].equalsIgnoreCase("E")) {
					if(manu.get(thisline[0])[7] == null)
						manu.get(thisline[0])[7] = thisline[5];
					else
						manu.get(thisline[0])[7] += "><" + thisline[5];
				}
				//N=H10 Additional Mfr Narrative
				else if(thisline[2].equalsIgnoreCase("N")) {
					if(manu.get(thisline[0])[6] == null)
						manu.get(thisline[0])[6] = thisline[5];
					else
						manu.get(thisline[0])[6] += "><" + thisline[5];
				}
				//No text type Code
				else {
					if(manu.get(thisline[0])[8] == null)
						manu.get(thisline[0])[8] = thisline[5];
					else
						manu.get(thisline[0])[8] += "><" + thisline[5];
				}	
			}
		}
		
		br.close();
		
		/*
		for (Entry<String, String> entry : hm_text.entrySet()) {
			System.out.println(entry.getKey() + ":" + entry.getValue());
		}
		*/
		
		System.out.println("--- Total narrative records: " + total);
		System.out.println("--- Total unique records: " + unique.size());					
	}
	
	public static void writeResult_TXT(HashMap<String, String[]> generic, HashMap<String, String[]> manu) throws IOException {
		String fileName = outputFolder + "Result_filter.txt";
		DataOutputStream dos = new DataOutputStream(new FileOutputStream(new File(fileName)));
		
		dos.writeBytes("MDR_REPORT_KEY" + "\t" + "HCFA_OR_MANUFACTURER_OR_DISTRIBUTOR_ID" + "\t" + "REPORT_NUMBER" + "\t" + "GENERIC_NAME" + "\t" + "MANUFACTURER_NAME" + "\t" + "BRAND_NAME" + "\t" + "TEXT_EVENT_DESCRIPTION" + "\t" + "TEXT_ADDITIONAL_MFR_NARRATIVE" + "\t" + "TEXT_DEVICE_EVALUATED_BY_MANUFACTURER"  + "\t" + "TEXT_OTHER" + "\r\n");
		
		for(String mdr : generic.keySet()) {			
			String[] info = generic.get(mdr);
			dos.writeBytes(mdr + "\t" + info[3] + "\t" + info[4] + "\t" + info[0] + "\t" + info[1] + "\t" + info[2] + "\t" + info[5] + "\t" + info[6] + "\t" + info[7] + "\t" + info[8] + "\r\n");				
		}
		
		for(String mdr : manu.keySet()) {			
			String[] info = manu.get(mdr);
			dos.writeBytes(mdr + "\t" + info[3] + "\t" + info[4] + "\t" + info[0] + "\t" + info[1] + "\t" + info[2] + "\t" + info[5] + "\t" + info[6] + "\t" + info[7] + "\t" + info[8] + "\r\n");				
		}
		
		dos.close();
	}
	
	/**
	public static void writeResult_ARFF(HashMap<String, String> hm_text, HashMap<String, String[]> name, String type) throws IOException {
		String fileName = outputFolder + "Result_" + type + "_filter.arff";
		DataOutputStream dos = new DataOutputStream(new FileOutputStream(new File(fileName)));
		
		dos.writeBytes("@relation " + type + "_filter" + "\r\n");
		dos.writeBytes("\r\n");
		dos.writeBytes("@attribute MDR_REPORT_KEY numeric" + "\r\n");
		dos.writeBytes("@attribute Generic_name" + "\r\n");
		dos.writeBytes("@attribute Manufacturer_name" + "\r\n");
		dos.writeBytes("@attribute TEXT string" + "\r\n");
		dos.writeBytes("@attribute HIT_OR_NOT {YES,NO}" + "\r\n");
		dos.writeBytes("\r\n");
		dos.writeBytes("@data" + "\r\n");
		
		for (Entry<String, String> entry : hm_text.entrySet())
			dos.writeBytes(entry.getKey() + ",\"" + fixStr(name.get(entry.getKey())[0]) + "\",\"" + fixStr(name.get(entry.getKey())[1]) + "\",\"" + fixStr(entry.getValue()) + "\",NO" + "\r\n");					
				
		dos.close();
	}
	**/
	
	public static String fixStr(String s) {
		return s == null ? s : s.replaceAll("\"", "'");
	}
	
	public static boolean advancedSearch(String keyword, String text) {
		return text.matches(".*[^A-Z]" + keyword + "[^A-Z].*") | text.matches(keyword + "[^A-Z].*") | text.matches(".*[^A-Z]" + keyword) | text.matches(keyword);
	}
}
