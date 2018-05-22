package nextGen;

import java.io.File;  

public class HtmlToPdf {  
   
    private static final String toPdfTool = "C:\\Program Files\\wkhtmltopdf\\bin\\wkhtmltopdf.exe";  
      
    public static void main(String[] args) {
    	//test
    	convert("https://newsflash.nextgen.com/helpdesk/NG-WebFiles/CustomerResourceCenter/NewsFlashesResolved2/PPS%2050739%20KBM%20--%20Alerts%20Button%20May%20Not%20Display%20Post-Upgrade.html", "C:\\Users\\hkang1\\123.pdf");
    	convert("https://wkhtmltopdf.org", "C:\\Users\\hkang1\\456.pdf");    	
    }
    
    public static boolean convert(String srcPath, String destPath){  
        File file = new File(destPath);  
        File parent = file.getParentFile();  
        //create dir if necessary  
        if(!parent.exists()){  
            parent.mkdirs();  
        }  
          
        StringBuilder cmd = new StringBuilder();  
        cmd.append(toPdfTool);  
        cmd.append(" ");  
        cmd.append(srcPath);  
        cmd.append(" ");  
        cmd.append(destPath);  
          
        boolean result = true;  
        try{  
            Process proc = Runtime.getRuntime().exec(cmd.toString());  
            HtmlToPdfInterceptor error = new HtmlToPdfInterceptor(proc.getErrorStream());  
            HtmlToPdfInterceptor output = new HtmlToPdfInterceptor(proc.getInputStream());  
            error.start();  
            output.start();  
            proc.waitFor();  
        }catch(Exception e){  
            result = false;  
            e.printStackTrace();  
        }  
          
        return result;  
    }     
}  
