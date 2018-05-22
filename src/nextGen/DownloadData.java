package nextGen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

public class DownloadData {
	public static String nextGen_link = "https://newsflash.nextgen.com/helpdesk/NG-Webfiles/CustomerResourceCenter/NewsFlashArchive.asp";
	public static String output = "C:\\Users\\hkang1\\Google Drive\\AHRQ\\NextGen\\NextGen.txt";
	public static String file_dir = "C:\\Users\\hkang1\\Google Drive\\AHRQ\\NextGen\\documents\\";
	public static String temp_dir_htmltopdf = "C:\\Users\\hkang1\\NextGenFiles\\";
	
	//0:ID, 1:title, 2:product 3:versions affected, 4:event type, 5:date fixed, 6:date released_copyright, 7:date released_upload, 8:resolved no not, 9:the issue, 10:example, 11:actions required, 12:status, 13:figure number, 14:link, 15:link type, 16:file direction
	public static ArrayList<String[]> info_new = new ArrayList<String[]>();
	public static ArrayList<String[]> info_resolved = new ArrayList<String[]>();
	public static HashMap<String, String> event_types = new HashMap<String, String>();
	public static HashMap<String, String> products = new HashMap<String, String>();
	
	/**
	 * htmlToPdf tool is required. Installed at C:\Program Files\wkhtmltopdf. HtmlToPdf.java can execute wkhtmltopdf through cmd
	 * The names of pdf files which converted from html have been renamed by replacing all non-char/non-digit with "_".
	 * Dont' forget to merge converted pdf files with original pdf files by moving temp_dir_htmltopdf to file_dir
	 */
	
	public static void main(String[] args) throws IOException {
		
		//Get all possible event types
		readEventTypes();
		
		//Get all possible products
		readProducts();
		
		//read NextGen website to get basic info
		getSummary(nextGen_link);
		
		//readDetails(info_new);
		
		//readDetails(info_resolved);
		
		//Identify IDs, event types, and products from titles
		identify_ID_eventType_product(info_new);
		identify_ID_eventType_product(info_resolved);
		
		//Download documents, html and htm documents are converted to pdf files
		System.out.println("Downloading documents for new reports ...");
		download_files(info_new);
		System.out.println("Downloading documents for resolved reports ...");
		download_files(info_resolved);
		
		//Produce full table
		writeOutput();
		
	}
	
	public static void readEventTypes() {
		event_types.put("PPI", "Potential Protected Information (PPI)");
		event_types.put("PNCU", "Potential Non-Clinical Urgent (PNCU)");
		event_types.put("PPS", "Potential Patient Safety (PPS)");
	}
	
	public static void readProducts() {
		products.put("Advanced Audit", "Advanced Audit");
		products.put("BBP", "Background Business Processer (BBP)");
		products.put("Care Guidelines", "Care Guidelines");
		products.put("CareGuidelines", "Care Guidelines");
		products.put("ClinicalTemplates", "Clinical Templates (formerly NextGen Ambulatory KBM)");		
		products.put("DigitalPen", "Digital Pen");
		products.put("Doc MGMT", "Document Management");
		products.put("Document Management", "Document Management");
		products.put("EDR", "Electronic Dental Record (EDR)");
		products.put("EHR", "Ambulatory EHR");
		products.put("File Maintenance", "File Maintenance");
		products.put("HIE", "Health Information Exchange (HIE)");
		products.put("HQM", "Health Quality Measures (HQM)");
		products.put("Interface", "Interface");
		products.put("KBM", "Knowledge Base Model (KBM)");
		products.put("Mobile", "Mobile");
		products.put("NCS", "NextGen Healthcare Communication Services (NCS)");
		products.put("NG GO", "NextGen Go");
		products.put("NGC", "NextGen Inpatient Clinicals ");
		products.put("OM", "Optical Management (OM)");		
		products.put("Optical Management", "Optical Management (OM)");
		products.put("Optical Mgmt", "Optical Management (OM)");
		products.put("Patient Portal", "Patient Portal (PP)");
		products.put("PH", "Population Health (PH)");
		products.put("Pharmacy", "Pharmacy application");
		products.put("PM", "Practice Management (PM)");
		products.put("Portal", "Patient Portal (PP)");
		products.put("PP", "Patient Portal (PP)");
		products.put("QDW", "Quest Dental Web (QDW)");
		products.put("Rosetta", "Rosetta Management");
	}
	
	public static String getSummary(String urlString) throws IOException {		
		try {
			String web = null;	
		
			while(web == null) {
			
				StringBuffer html = new StringBuffer();
				URL url = new URL(urlString);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setInstanceFollowRedirects(false);	
				
				InputStreamReader isr;
				
				isr = new InputStreamReader(conn.getInputStream(), "UTF-8");
				
				BufferedReader br = new BufferedReader(isr);
				String temp;		
				
				Pattern p_resolved = Pattern.compile("<tbody id=\"resolvedBody\">");
				Pattern p_id_title = Pattern.compile("<td valign=\"top\"><a href=\"(download.+)\">(.+)</a></td>");		
				Pattern p_version_date = Pattern.compile("<td valign=\"top\">(.+)</td>");	
				Matcher m;					
				
				int count_new = 0;
				int count_re = 0;
				
				int count_pdf_new = 0;
				int count_htm_new = 0;
				int count_html_new = 0;
				int count_zip_new = 0;
				int count_pdf_re = 0;
				int count_htm_re = 0;
				int count_html_re = 0;
				int count_zip_re = 0;
				
				boolean flag_startOfResolvedCases = false;
				
				while ((temp = br.readLine()) != null) {
					html.append(temp).append("\n");
					
					m = p_resolved.matcher(temp);
					if(m.find())
						flag_startOfResolvedCases = true;
					
					//-----match id and title line
					m = p_id_title.matcher(temp);					
					if(m.find()) {
						String link = m.group(1).trim();
						String title = m.group(2).trim();
						String link_type = "";
						if(link.endsWith(".pdf") || link.endsWith(".PDF")) {
							link_type = "pdf";
							if(flag_startOfResolvedCases)
								count_pdf_re ++;
							else
								count_pdf_new ++;
						}else if(link.endsWith(".htm") || link.endsWith(".HTM")) {
							link_type = "htm";
							if(flag_startOfResolvedCases)
								count_htm_re ++;
							else
								count_htm_new ++;
						}else if(link.endsWith(".html") || link.endsWith(".HTML")) {
							link_type = "html";
							if(flag_startOfResolvedCases)
								count_html_re ++;
							else
								count_html_new ++;
						}else if(link.endsWith(".zip") || link.endsWith(".ZIP")) {
							link_type = "zip";
							if(flag_startOfResolvedCases)
								count_zip_re ++;
							else
								count_zip_new ++;
						}else
							System.out.println("New link type: " + link); //check new link types
						
						//0:ID, 1:title, 2:product 3:versions affected, 4:event type, 5:date fixed, 6:date released_copyright, 7:date released_upload, 8:resolved no not, 9:the issue, 10:example, 11:actions required, 12:status, 13:figure number, 14:link, 15:link type, 16:file direction
						String[] record = new String[17];
						for(int i=0; i<record.length; i++)
							record[i] = "";
						record[1] = title;
						record[8] = flag_startOfResolvedCases? "Resolved" : "New";
						record[15] = link_type;
						record[14] = link.replace("download.asp?file=", "https://newsflash.nextgen.com/helpdesk/NG-WebFiles/CustomerResourceCenter/").replaceAll(" ", "%20");
						
						temp = br.readLine();
						m = p_version_date.matcher(temp);
						if(m.find())
							record[3] = m.group(1).trim();
						temp = br.readLine();
						m = p_version_date.matcher(temp);
						if(m.find())
							record[7] = m.group(1).trim();
						
						if(link_type.equals("html") || link_type.equals("htm")) {
							
						}
						
						if(flag_startOfResolvedCases)
							info_resolved.add(record);
						else
							info_new.add(record);
					}
					//-----
				}
				
				count_new = count_pdf_new + count_htm_new + count_html_new + count_zip_new;
				count_re = count_pdf_re + count_htm_re + count_html_re + count_zip_re;
			
				//----Summary-----
				System.out.println("--- SUMMARY ---");
				System.out.println("\tNEW\tRESOLVED\tTOTAL");
				System.out.println("PDF\t" + count_pdf_new + "\t" + count_pdf_re + "\t" + (count_pdf_new + count_pdf_re));
				System.out.println("HTM\t" + count_htm_new + "\t" + count_htm_re + "\t" + (count_htm_new + count_htm_re));
				System.out.println("HTML\t" + count_html_new + "\t" + count_html_re + "\t" + (count_html_new + count_html_re));
				System.out.println("ZIP\t" + count_zip_new + "\t" + count_zip_re + "\t" + (count_zip_new + count_zip_re));
				System.out.println("TOTAL\t" + count_new + "\t" + count_re + "\t" + (count_new + count_re));
				//------
				
				br.close();
				isr.close();
				web = html.toString();		
			}	
			return web;
		}catch (java.net.ConnectException e) {
			return DownloadData.getSummary(urlString);
		}		
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
				
				isr = new InputStreamReader(conn.getInputStream(), "UTF-8");
				
				BufferedReader br = new BufferedReader(isr);
				String temp;				
				
				while ((temp = br.readLine()) != null)
					html.append(temp).append("\n");					
				
				br.close();
				isr.close();
				web = html.toString();		
			}	
			return web;
		}catch (java.net.ConnectException e) {
			return DownloadData.getSummary(urlString);
		}
	}
	
	public static void readDetails(ArrayList<String[]> info) {
		Pattern p_event_type = Pattern.compile("<title>(.+)</title>");
		
		Pattern p_product_1 = Pattern.compile("NextGen&reg;(.+?)[,0-9v][0-9\\.]");	//non-greedy
		Pattern p_product_2 = Pattern.compile("NextGen<[Ss][Uu][Pp]>&reg;</[Ss][Uu][Pp]>(.+?)[ ,0-9v][ 0-9\\.]");	//non-greedy
		Pattern p_product_3 = Pattern.compile("NextGen<[Ss][Uu][Pp]>&reg;</[Ss][Uu][Pp]> \n(.+?)[ ,0-9v][ 0-9\\.]");	//non-greedy
		Pattern p_product_4 = Pattern.compile("</strong>(.+?)[ ,0-9v][ 0-9\\.]");	//non-greedy
		Pattern p_product_5 = Pattern.compile("All versions of NextGen&reg;(.+?)</p>");	//non-greedy
		Pattern p_product_6 = Pattern.compile("All NextGen&reg;(.+?)versions");	//non-greedy
		Pattern p_product_7 = Pattern.compile("NextGen?(.+?)[,0-9v][0-9\\.]");	//non-greedy		
		Pattern p_product_8 = Pattern.compile("NextGen&reg;(.+?) \n +[ ,0-9v][ 0-9\\.]");	//non-greedy
		
		
		Matcher m_1;
		Matcher m_2;
		Matcher m_3;
		Matcher m_4;
		Matcher m_5;
		Matcher m_6;
		Matcher m_7;
		Matcher m_8;
		
		for(String[] s : info) {
			if(s[12].equals("html") || s[12].equals("htm")) {
				try {
					String html = getHtml(s[11]);
					html = html.replaceAll("®", "&reg;");
					m_1 = p_event_type.matcher(html);
					if(m_1.find())
						if(event_types.containsKey(m_1.group(1).trim()))
							s[4] = m_1.group(1).trim();
					
					m_1 = p_product_1.matcher(html);
					m_2 = p_product_2.matcher(html);
					m_3 = p_product_3.matcher(html);
					m_4 = p_product_4.matcher(html);
					m_5 = p_product_5.matcher(html);
					m_6 = p_product_6.matcher(html);
					m_7 = p_product_7.matcher(html);
					m_8 = p_product_8.matcher(html);
					
					if(m_1.find()) {
						s[2] = m_1.group(1).trim();
						products.put(s[2], "");
					}
					else if(m_2.find()) 
						s[2] = m_2.group(1).trim();
					else if(m_3.find()) 
						s[2] = m_3.group(1).trim();
					else if(m_4.find()) 
						s[2] = m_4.group(1).trim();
					else if(m_5.find()) 
						s[2] = m_5.group(1).trim();
					else if(m_6.find()) 
						s[2] = m_6.group(1).trim();
					else if(m_7.find()) 
						s[2] = m_7.group(1).trim();
					else if(m_8.find()) 
						s[2] = m_8.group(1).trim();
					else
						System.out.println("Failed to find PRODUCT: " + s[1]);
					
					if(!s[2].equals(""))
						;
					
				}catch(Exception e) {
					System.out.println("Failed to read: " + s[14]);
				}
			}
		}
	}
	
	public static void identify_ID_eventType_product(ArrayList<String[]> info) {
		for(String[] s : info) {
			String[] title_for_id_type = s[1].split(" ");
			
			//ID
			if(title_for_id_type[1].trim().startsWith("KI") || title_for_id_type[1].trim().matches("[0-9]+"))
				s[0] = title_for_id_type[1].trim();
			else if(title_for_id_type[0].trim().equalsIgnoreCase("Resolved")) {
				if(title_for_id_type[3].trim().matches("[0-9]+"))
					s[0] = title_for_id_type[3].trim();
				else if(title_for_id_type[2].trim().matches("[0-9]+"))
					s[0] = title_for_id_type[2].trim();
			}
			
			//event type
			for(String type : event_types.keySet()) {
				if(type.equalsIgnoreCase(title_for_id_type[0].trim()) || type.equalsIgnoreCase(title_for_id_type[1].trim()) || type.equalsIgnoreCase(title_for_id_type[2].trim()) || type.equalsIgnoreCase(title_for_id_type[3].trim()))
					s[4] = event_types.get(type);				
			}
			
			String[] title_for_product = s[1].replaceAll(" - ", " ").replaceAll("- ", " ").replaceAll(" -", " ").replaceAll("-", " ").split(" ");
			
			//product
			for(String pro : products.keySet()) {
				if(pro.equalsIgnoreCase(title_for_product[2].trim()) || pro.equalsIgnoreCase(title_for_product[3].trim()) || pro.equalsIgnoreCase(title_for_product[2].trim() + " " + title_for_product[3].trim()))
					s[2] = products.get(pro);
				else if(title_for_product.length > 4) {
					if(pro.equalsIgnoreCase(title_for_product[4].trim()))
						s[2] = products.get(pro);
				}else if (title_for_product.length > 5) {
					if(pro.equalsIgnoreCase(title_for_product[5].trim()))
						s[2] = products.get(pro);
				}
			}
		}
	}
	
	public static void download_files(ArrayList<String[]> info) {
		String url = "";
		String dir = "";
		
		
		
		for(String[] s : info) {
			if(s[15].equals("pdf") || s[15].equals("zip")) {
				url = s[14];
				dir = file_dir + s[1] + "." + s[15];
				try {
					downloadAndSave(url, dir);
					s[16] = "=HYPERLINK(\"./documents/" + s[1] + "." + s[15] + "\",\"" + s[1] + ".\" + s[15] + \"\")";					
				}catch (MalformedURLException e) {  
		            System.out.println("URL error - " + s[1]);  
		            //e.printStackTrace();  
		        } catch (IOException e) {  
		            System.out.println("I/O error - " + s[1]);  
		            //e.printStackTrace();  
		        }  
			}
			else if(s[15].equals("html") || s[15].equals("htm")) {
				url = s[14];
				String noSpaceTitle = new String(s[1]);
				noSpaceTitle = noSpaceTitle.replaceAll("[^0-9a-zA-Z]", "_");
				dir = temp_dir_htmltopdf + noSpaceTitle + ".pdf";
				System.out.println(url);
				System.out.println(dir);
				try {
					System.out.println(HtmlToPdf.convert(url, dir));					
					s[16] = "=HYPERLINK(\"./documents/" + noSpaceTitle + ".pdf\",\"" + noSpaceTitle + ".pdf\")";					
				}catch (Exception e) {  
		            System.out.println("htmltopdf error - " + s[1]);  
		            //e.printStackTrace();  
		        }
			}
		}
		
	}
	
	public static void downloadAndSave(String source, String destination) throws IOException {          
		URL url = new URL(source);   
        File file = new File(destination);   
        FileUtils.copyURLToFile(url, file);  
        //System.out.println(source + "download completed");         
    } 
	
	public static void writeOutput() throws IOException {		
		OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(new File(output)), "UTF-8");		
		
		//0:ID, 1:title, 2:product 3:versions affected, 4:event type, 5:date fixed, 6:date released_copyright, 7:date released_upload, 8:resolved no not, 9:the issue, 10:example, 11:actions required, 12:status, 13:figure number, 14:link, 15:link type, 16:file direction
		osw.write("ID" + "\t" + "Title" + "\t" + "Product" + "\t" + "Versions Affected" + "\t" + "Event Type" + "\t" + "Date Fixed" + "\t" + "Date Released_copyright" + "\t" + "Date Released_upload" + "\t" + "Resolved or Not" + "\t" + "The Issue" + "\t" + "Example" + "\t" + "Actions Required" + "\t" + "Status" + "\t" + "Figure Nums" + "\t" + "Link" + "\t" + "Link Type" + "\t" + "File Direction");
		osw.write("\r\n");
		
		for(String[] s : info_new) {
			osw.write(s[0]);
			for(int i=1; i<s.length; i++)
				osw.write("\t" + s[i]);
			osw.write("\r\n");
		}
		
		for(String[] s : info_resolved) {
			osw.write(s[0]);
			for(int i=1; i<s.length; i++)
				osw.write("\t" + s[i]);
			osw.write("\r\n");
		}
		
		osw.flush();
		osw.close();
	}		
	
	//determine encoding
	public static String getEncoding(String str) {        
		String encode = "GB2312";        
		try {        
			if (str.equals(new String(str.getBytes(encode), encode))) {        
				String s = encode;        
				return s;        
			}        
	    } catch (Exception exception) {        
	    }        
	    encode = "ISO-8859-1";        
	    try {        
	    	if (str.equals(new String(str.getBytes(encode), encode))) {        
	    		String s1 = encode;        
	            return s1;        
	        }        
	    } catch (Exception exception1) {        
	    }        
	    encode = "UTF-8";        
	    try {        
	    	if (str.equals(new String(str.getBytes(encode), encode))) {        
	    		String s2 = encode;        
	            return s2;        
	        }        
	    } catch (Exception exception2) {        
	    }        
	    encode = "GBK";        
	    try {        
	        if (str.equals(new String(str.getBytes(encode), encode))) {        
	        	String s3 = encode;        
	            return s3;        
	        }        
	    } catch (Exception exception3) {        
	    }        
	    return "";        
	}
}
