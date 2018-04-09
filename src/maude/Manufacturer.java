/*
 * Get all manufacturer names from http://www.capterra.com/electronic-medical-records-software/
 */

package maude;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Manufacturer {
	public static String EHRurl = "http://www.capterra.com/electronic-medical-records-software/";
	
	public static String Output_ManufacturerList = "C:\\Users\\hkang1.UTHSAHS\\Google Drive\\MAUDE\\keywords\\manufacturerList.txt";

	public static void main(String[] args) throws IOException {
		String url = getHtml(EHRurl);
		
		Pattern p = Pattern.compile("color-gray\">by(.+)</h3>");
		Matcher m;
		
		m = p.matcher(url);		
		
		DataOutputStream dos = new DataOutputStream(new FileOutputStream(new File(Output_ManufacturerList)));
		while(m.find())
			dos.writeBytes(m.group(1).replaceAll("&amp;", "&").trim() + "\r\n");			
		
		dos.close();
	}
	
	public static String getHtml(String urlString) throws IOException {
		
		try {
			String web = null;	
		
			while(web == null) {
			
				StringBuffer html = new StringBuffer();
				URL url = new URL(urlString);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setInstanceFollowRedirects(false);	
				
				InputStreamReader isr;
				
				isr = new InputStreamReader(conn.getInputStream());
				
				BufferedReader br = new BufferedReader(isr);
				String temp;
				
				//--- catch the jumped page
				temp = br.readLine();
				if(temp.contains("Object moved")) {
					temp = br.readLine();
					Pattern p = Pattern.compile("<a href=\"(.+)\">here");
					Matcher m;
					
					m = p.matcher(temp);
					
					if(m.find()) {						
						urlString = "http://webmm.ahrq.gov" + m.group(1).replaceAll("%2f", "/").replaceAll("%3f", "?").replaceAll("%3d", "=").replaceAll("%26", "&");
						url = new URL(urlString);
						conn = (HttpURLConnection) url.openConnection();
						conn.setInstanceFollowRedirects(false);
						isr = new InputStreamReader(conn.getInputStream());
						br = new BufferedReader(isr);
					}
				}
				//-------
				
				while ((temp = br.readLine()) != null) {
					html.append(temp).append("\n");
				}
			
				br.close();
				isr.close();
				web = html.toString();
			
		
			}	
			return web;
		}catch (java.net.ConnectException e) {
			return Manufacturer.getHtml(urlString);
		}		
	}
}
