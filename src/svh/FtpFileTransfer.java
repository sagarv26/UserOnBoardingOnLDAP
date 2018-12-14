package svh;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.Properties;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

public class FtpFileTransfer {
	private static Properties props = null;
	
	public void uploadFile(String UserExtract,String RemoteFile){
		PropertyFileHelper propertyFileHelper = PropertyFileHelper
				.getInstance();
		props = propertyFileHelper.getProperties();
		String serverAddress = props.getProperty("serverAddress"); 
        int port = 21;
        String FTPUser = props.getProperty("FTPUser");
        String FTPPwd = props.getProperty("FTPPwd");
  
		FTPClient ftpClient = new FTPClient();
		
			try {
				ftpClient.connect(serverAddress, port);
				ftpClient.login(FTPUser, FTPPwd);
				ftpClient.enterLocalPassiveMode();
				
				ftpClient.setFileType(FTP.BINARY_FILE_TYPE);							
		         
		        InputStream inputStream = new FileInputStream(new File(UserExtract));
		 
		        System.out.println("Start uploading file");
		        LoggerUtil.debugLog("Start uploading file");
		        OutputStream outputStream = ftpClient.storeFileStream(RemoteFile);
		     
		        byte[] bytesIn = new byte[4096];
		        int read = 0;
		 
		        while ((read = inputStream.read(bytesIn)) != -1) {
		             outputStream.write(bytesIn, 0, read);
		         }
	             inputStream.close();
		         outputStream.close();
		 
	           boolean completed = ftpClient.completePendingCommand();
		         if (completed) {
		               System.out.println("file uploaded successfully.");
		               LoggerUtil.debugLog("file uploaded successfully.");
		          }
				
			} catch (SocketException e) {
				e.printStackTrace();
				LoggerUtil.errorLog("Error : "+e.getMessage());
			} catch (IOException e) {
				e.printStackTrace();
				LoggerUtil.errorLog("Error : "+e.toString());
			} finally {
				if (ftpClient.isConnected()) {
				try {
					ftpClient.logout();
					ftpClient.disconnect();
				} catch (IOException e) {
					e.printStackTrace();
					LoggerUtil.errorLog("Error : "+e);
				}
}
			}	
			
	}
		
	public void downloadFile(String remoteFilePath,String destination ){
		PropertyFileHelper propertyFileHelper = PropertyFileHelper
				.getInstance();
		props = propertyFileHelper.getProperties();

		String serverAddress = props.getProperty("serverAddress"); 
        int port = 21;
        String FTPUser = props.getProperty("FTPUser");
        String FTPPwd = props.getProperty("FTPPwd");
  
        FTPClient ftpClient = new FTPClient();
        try {
  
            ftpClient.connect(serverAddress, port);
            ftpClient.login(FTPUser,FTPPwd);
 
            ftpClient.enterLocalPassiveMode();
            File localfile = new File(destination);
            OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(localfile));
            boolean success = ftpClient.retrieveFile(remoteFilePath, outputStream);
 
            outputStream.close();
  
            if (success) {
            	LoggerUtil.debugLog("Ftp file successfully download.");
                System.out.println("Ftp file successfully download.");
            }else{
            	LoggerUtil.errorLog("File may be not exist..");
                System.out.println("File may be not exist..");
            }
  
        } catch (IOException ex) {
            System.out.println("Error occurs in downloading files from ftp Server : " + ex.getMessage());
            LoggerUtil.errorLog("Error occurs in downloading files from ftp Server : " + ex.getMessage());
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                LoggerUtil.errorLog("Error : "+ex.getMessage());
            }
        }
    }
	
	
}

