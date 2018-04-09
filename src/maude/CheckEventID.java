package maude;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

public class CheckEventID {
	
	public static String master = "C:\\Users\\hkang1.UTHSAHS\\Google Drive\\MAUDE\\filter_classifier\\mdrfoiThru2016.txt";
	
	public static HashMap<String, Integer> event_2008 = new HashMap<String, Integer>();
	public static HashMap<String, Integer> event_2009 = new HashMap<String, Integer>();
	public static HashMap<String, Integer> event_2010 = new HashMap<String, Integer>();
	public static HashMap<String, Integer> event_2011 = new HashMap<String, Integer>();
	public static HashMap<String, Integer> event_2012 = new HashMap<String, Integer>();
	public static HashMap<String, Integer> event_2013 = new HashMap<String, Integer>();
	public static HashMap<String, Integer> event_2014 = new HashMap<String, Integer>();
	public static HashMap<String, Integer> event_2015 = new HashMap<String, Integer>();
	public static HashMap<String, Integer> event_2016 = new HashMap<String, Integer>();
	
	public static HashSet<String> all = new HashSet<String>();
	
	public static void main(String[] args) throws IOException {
		browse();
		System.out.println(event_2008.size());
		System.out.println(event_2009.size());
		System.out.println(event_2010.size());
		System.out.println(event_2011.size());
		System.out.println(event_2012.size());
		System.out.println(event_2013.size());
		System.out.println(event_2014.size());
		System.out.println(event_2015.size());
		System.out.println(event_2016.size());
		System.out.println(all.size());
	}
	
	public static void browse() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(master));
		String line = br.readLine();
		
		while((line = br.readLine()) != null) {
			String[] thisline = line.split("\\|");
			String[] rn = thisline[2].split("-");
			
			all.add(rn[0]);
			
			if(rn.length < 3)				
				continue;			
			
			if(rn[1].equals("2008"))
				event_2008.put(rn[0], event_2008.getOrDefault(rn[0], 0) + 1);
			if(rn[1].equals("2009"))
				event_2009.put(rn[0], event_2009.getOrDefault(rn[0], 0) + 1);
			if(rn[1].equals("2010"))
				event_2010.put(rn[0], event_2010.getOrDefault(rn[0], 0) + 1);
			if(rn[1].equals("2011"))
				event_2011.put(rn[0], event_2011.getOrDefault(rn[0], 0) + 1);
			if(rn[1].equals("2012"))
				event_2012.put(rn[0], event_2012.getOrDefault(rn[0], 0) + 1);
			if(rn[1].equals("2013"))
				event_2013.put(rn[0], event_2013.getOrDefault(rn[0], 0) + 1);
			if(rn[1].equals("2014"))
				event_2014.put(rn[0], event_2014.getOrDefault(rn[0], 0) + 1);
			if(rn[1].equals("2015"))
				event_2015.put(rn[0], event_2015.getOrDefault(rn[0], 0) + 1);
			if(rn[1].equals("2016"))
				event_2016.put(rn[0], event_2016.getOrDefault(rn[0], 0) + 1);
		}		
		
		br.close();
	}
}
