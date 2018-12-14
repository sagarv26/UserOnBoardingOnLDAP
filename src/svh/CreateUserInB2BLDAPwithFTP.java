package svh;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;

import svh.PropertyFileHelper;

public class CreateUserInB2BLDAPwithFTP {
	
	public static final String LDAPctxfactory =  "com.sun.jndi.ldap.LdapCtxFactory";
	public static FileWriter fileWriter = null,fileWriterWithoutPwd = null,fileErrorDetail=null;
	private static Properties props = null;
	public static FtpFileTransfer ftpTrans;
	public static int count=0;
	public static ArrayList<String> ErrorInfo=new ArrayList<String>();
	public static final String DummyText="\r\n\r\n  ******************************************************  \r\n\r\n";
	
	
	public static DirContext getConnection() throws NamingException{
	 Hashtable<String, String> env = new Hashtable<String, String>();
	    env.put(Context.INITIAL_CONTEXT_FACTORY, LDAPctxfactory);
	    env.put(Context.PROVIDER_URL, props.getProperty("LDAPURL"));
	    env.put(Context.SECURITY_AUTHENTICATION,"simple");
	    env.put(Context.SECURITY_PRINCIPAL,props.getProperty("UserName")); 
	    env.put(Context.SECURITY_CREDENTIALS,props.getProperty("UserPassword")); 
	    System.out.println("Returning the Context");
	   return new InitialDirContext(env);
	}
	
	public static void main(String s[]) throws IOException {
		
		PropertyFileHelper propertyFileHelper = PropertyFileHelper
				.getInstance();
		props = propertyFileHelper.getProperties();
		DirContext dctx =null;
		BufferedReader br = null;
		String[] lineArray= new String[7];
		String line=null;
		String uid=null;
		Boolean blank=false;
		
		//Date Formating
		Date date =new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("dd-M-yyyy hh:mm:ss"); 
		String strDate = formatter.format(date);  
	    String[] split=strDate.split(" ");
	    String todayDate=split[0].replaceAll("-", "");
	    
	    //File with Credentials
		String fileName=props.getProperty("OutputPath")+"b2b_Cred_"+todayDate+".csv";
		
		//File Without Credentials 
		String fileNameWithoutPwd=props.getProperty("OutputPathWithoutPwd")+"comp_userinfo_"+todayDate+".csv";
		
		//File for Error Detail
		String ErrorDetail=props.getProperty("ErrorDetail")+"comp_Error_"+todayDate+".txt";
		
		try {
			 dctx = getConnection();
			 String baseDN = "ou=pereople,ou=test,o=company.com";
			 
			 String source=props.getProperty("SharedPath")+"Java_b2b.csv";
			 String dest=props.getProperty("LocalPath");
			 
			 FtpFileTransfer ftpTrans=new FtpFileTransfer();
			 ftpTrans.downloadFile(source, dest);
			 br=new BufferedReader(new FileReader(dest));
			 
			 fileWriter = new FileWriter(fileName);
			 fileWriterWithoutPwd = new FileWriter(fileNameWithoutPwd);
			 fileErrorDetail = new FileWriter(ErrorDetail);
			 
			 writeCsvFile("firstName","lastName","employeenumber","uid","password");
			 writeCsvFileWithOutPwd("firstName", "lastName", "employeeNumber", "uid");
			 
			 while((line=br.readLine())!=null){
			 lineArray=line.split(",");
			 
			 //First 4 Column of Input File Should be filled 
			 if(lineArray.length>=4){
			 
			 //To Validate blank input
			 blank=ValidateBlank(lineArray[0],lineArray[1],lineArray[2],lineArray[3]);
			 
			 
			 //Input should not be blank
			 if(blank){

			 //Validate with Employee number
			 if(!checkUserExistanceWithEmpNo(lineArray[2],baseDN,dctx)){				 
			 uid=RandomUID(lineArray[0],lineArray[1],lineArray[2],lineArray[3]);		
			 
			 while(checkUserExistance(uid,baseDN,dctx)){
				 
			 count++;
			 //Get Random UID
			 uid=RandomUID(lineArray[0],lineArray[1],lineArray[2],lineArray[3]);
			 }
			 
			 count=0;

			 //Get Random Password
			 String pwd=RandomPassword();	

			 if(blank){
			 System.out.println(uid+" | "+lineArray[0]+" | "+lineArray[1]+" | "+pwd+" | "+lineArray[2]+" | "+lineArray[5]+" | "+lineArray[8]);
			 CreateUsers(uid,lineArray[0],lineArray[1],pwd,lineArray[2],lineArray[5],lineArray[8],dctx);
			 
			 //Starting Index for Group
			 int start=9;
			 
			 for(int i=start;i < lineArray.length;i++ ){

				 if(!lineArray[i].trim().equals("")){
					String group=getGroupDN(lineArray[i], "ou=group, o=test.com", dctx);
					if(!group.equals("")){
					AddGroup(uid,group,dctx);
					}else{
						LoggerUtil.debugLog("Unable to find group : "+lineArray[i]);
					}
				 }
				   
			}
			 
			 
			 }	
				 }else{
					 writeCsvFile(lineArray[0],lineArray[1],lineArray[2], "Employee Number Already assigned", "Employee Number Already assigned"); 
					 writeCsvFileWithOutPwd(lineArray[0],lineArray[1],lineArray[2], "Employee Number Already assigned");
				 }
				   }
			         }

			 }	 
			 
		} catch (NamingException e2) {	
			e2.printStackTrace();
			LoggerUtil.errorLog("Error : "+e2);
			try {
				fileErrorDetail.write(e2+DummyText);
				ErrorInfo.add(e2+DummyText);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}catch(java.lang.NumberFormatException Nfe){
			System.out.println("You should enter any digits\n");
			System.out.println("Please Try Again\n");
			LoggerUtil.errorLog("User Entered Inavlid Choice \n Program Exiting !!!");
		System.exit(0);
		}
		catch (Exception e1) {
			e1.printStackTrace();
			LoggerUtil.errorLog("Error : "+e1);
			try {
				fileErrorDetail.write(e1+DummyText);
				ErrorInfo.add(e1+DummyText);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		finally{
			System.out.println("Closing the Connection If it is opened\n");
			
			if(dctx != null){
				try {
					dctx.close();
					fileWriter.flush();
					fileWriter.close();
					fileWriterWithoutPwd.flush();
					fileWriterWithoutPwd.close();
					fileErrorDetail.flush();
					fileErrorDetail.close();
								
					//Upload file to Shared path
					 String RemoteFile=props.getProperty("SharedPath")+"b2b_Cred_"+todayDate+".csv";
					 String UserExtract="\\\\C:\\b2b_Cred_"+todayDate+".csv";
					 
					 FtpFileTransfer ftp=new FtpFileTransfer();
					 ftp.uploadFile(UserExtract, RemoteFile);
					 
					 ArrayList<String> dl=new ArrayList<String>();
					 dl.add("sagar.v.hande@test.com");	   
					 System.out.println("Error count: "+ErrorInfo.size());	
							
					 Mailer.send("sagar.v.hande@test.com",dl,"Error :"+ErrorInfo.size(),ErrorInfo);
		
				} catch (NamingException e) {			
					System.out.println("Unable to Close the Connection\n");					
					fileErrorDetail.write(e+DummyText);
					ErrorInfo.add(e+DummyText);					
					LoggerUtil.errorLog("Error : "+e);
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
					fileErrorDetail.write(e+DummyText);
					ErrorInfo.add(e+DummyText);				
					LoggerUtil.errorLog("Error : "+e);
				}
			}
		}
	}
	
	private static Boolean ValidateBlank(String string, String string2, String string3, String string4) {
		if(string.trim().equals("") | string2.trim().equals("") | string3.trim().equals("") | string4.trim().equals("")){
		writeCsvFile(string,string2, string3, "Blank", "Blank");
		LoggerUtil.debugLog("ID not created as given input ("+string+" or "+string2+" or "+string3+") is blank\n");
		return false;
		}else{		
			return true;
		}
		
	}

	private static void AddGroup(String uid, String group,DirContext dctx) {		
		
        try {
        	Attributes uniquemember = new BasicAttributes("uniquemember", "uid="+uid+",ou=user,ou=test,o=company.com"); 
			dctx.modifyAttributes(group, LdapContext.ADD_ATTRIBUTE, uniquemember);
			LoggerUtil.debugLog("group: "+group+" added to "+uid+", ");
		} catch (NamingException e) {
			e.printStackTrace();
			try {
				fileErrorDetail.write(e+DummyText);
				ErrorInfo.add(e+DummyText);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			LoggerUtil.errorLog("Error : "+e);
		}
        
	}

	private static String RandomPassword() {
		SecureRandom random = new SecureRandom();

	    /** different dictionaries used */
	   String ALPHA_CAPS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	   String ALPHA = "abcdefghijklmnopqrstuvwxyz";
	   String NUMERIC = "0123456789";
	   String SPECIAL_CHARS = "@#";
	   
	   String dic=ALPHA_CAPS+ALPHA+NUMERIC;
	   
	   String result = "";
	    for (int i = 0; i < 5; i++) {
	        int index = random.nextInt(dic.length());
	        result += dic.charAt(index);
	    }
	    for (int i = 0; i < 1; i++) {
	    	result+=SPECIAL_CHARS.charAt(random.nextInt(SPECIAL_CHARS.length()));
	    }
	    for (int i = 0; i < 2; i++) {
	        int index = random.nextInt(dic.length());
	        result += dic.charAt(index);
	    }
		
		return result;
	}

	private static String RandomUID(String firstname,String lastname,String employeenumber,String company) {
		
		String digit = null;
		String fn=firstname.substring(0, 2).toLowerCase();
		String ln=lastname.substring(0, 2).toLowerCase();
		String com=company.substring(0, 3).toLowerCase();

		if(count>10){
		   digit=""+count;
		}else{
			digit="0"+count;
		}				
					
		String result = com+fn+ln+digit;

	    String uid=result;

	return uid;
	}

	private static void writeCsvFile(String firstName, String lastName, String employeeNumber, String uid, String pwd) {
		
		//Delimiter used in CSV file
		String COMMA_DELIMITER = ",";
		String NEW_LINE_SEPARATOR = "\n";
				
		try {		

			fileWriter.append(firstName);
			fileWriter.append(COMMA_DELIMITER);
			fileWriter.append(lastName);
			fileWriter.append(COMMA_DELIMITER);
			fileWriter.append(employeeNumber);
			fileWriter.append(COMMA_DELIMITER);
			fileWriter.append(uid);
			fileWriter.append(COMMA_DELIMITER);
			fileWriter.append(pwd);
			fileWriter.append(NEW_LINE_SEPARATOR);

		} catch (Exception e) {
			System.out.println("Error in CsvFileWriter !!!");
			LoggerUtil.debugLog("Error in CsvFileWriter !!!");
			LoggerUtil.errorLog("Error : "+e);
			try {
				fileErrorDetail.write(e+DummyText);
				ErrorInfo.add(e+DummyText);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		} 		
		
	}
	
	private static void writeCsvFileWithOutPwd(String firstName, String lastName, String employeeNumber, String uid) {
		
		//Delimiter used in CSV file
		String COMMA_DELIMITER = ",";
		String NEW_LINE_SEPARATOR = "\n";
				
		try {		

			fileWriterWithoutPwd.append(firstName);
			fileWriterWithoutPwd.append(COMMA_DELIMITER);
			fileWriterWithoutPwd.append(lastName);
			fileWriterWithoutPwd.append(COMMA_DELIMITER);
			fileWriterWithoutPwd.append(employeeNumber);
			fileWriterWithoutPwd.append(COMMA_DELIMITER);
			fileWriterWithoutPwd.append(uid);
			fileWriterWithoutPwd.append(NEW_LINE_SEPARATOR);

		} catch (Exception e) {
			System.out.println("Error in CsvFileWriter !!!");
			LoggerUtil.debugLog("Error in CsvFileWriter !!!");
			LoggerUtil.errorLog("Error : "+e);
			try {
				fileErrorDetail.write(e+DummyText);
				ErrorInfo.add(e+DummyText);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		} 
		
	
		
	}
	
	public static boolean CreateUsers(String uid,String firstName,String lastName,String password,String emplid, String b2atitle, String storenumber, DirContext dctx){
		       boolean UserCreated = false;
			   
		try{
		       System.out.println(uid);
		       System.out.println(firstName);
		       System.out.println(lastName+ "\n");
    	
    		   Attribute givenName = new BasicAttribute("givenName",firstName);
               Attribute sn = new BasicAttribute("sn",lastName);
               Attribute cn = new BasicAttribute("cn",firstName+" "+lastName);
               Attribute employeenumber = new BasicAttribute("employeenumber",emplid);
               Attribute businesscategory = new BasicAttribute("businesscategory","person");
               Attribute employeetype = new BasicAttribute("svuidmusertype","B2B");            
               Attribute ID = new BasicAttribute("uid",uid);
//			   b2atitle=b2atitle.replaceAll("##", ", ");
               Attribute title = new BasicAttribute("title",b2atitle);
               Attribute jobaction = new BasicAttribute("absjobaction","Hired");
               Attribute carlicense = new BasicAttribute("carlicense","16777216");
               Attribute roomnumber = new BasicAttribute("roomnumber",storenumber);

               Attribute userPassword = new BasicAttribute("userPassword",password);
               Attribute Objectclass = new BasicAttribute("objectclass");
               Objectclass.add(" ");
               Objectclass.add(" ");
               Objectclass.add(" ");
               Objectclass.add("user");
               Objectclass.add(" ");
               Objectclass.add(" ");
   
               Attributes battrs = new BasicAttributes();
               battrs.put(givenName);
               battrs.put(businesscategory);
               battrs.put(title);
               battrs.put(sn);
               battrs.put(jobaction);
               battrs.put(carlicense);             
               battrs.put(employeenumber);
               battrs.put(employeetype);
               battrs.put(cn);
               battrs.put(ID);
               battrs.put(roomnumber);
               battrs.put(Objectclass);
               battrs.put(userPassword);
               
               dctx.createSubcontext("uid="+uid+",ou=user,ou=group,o=company.com", battrs);
               LoggerUtil.debugLog("\nUID: "+uid+" created, ");
               writeCsvFile(firstName, lastName, emplid, uid, password);
               writeCsvFileWithOutPwd(firstName, lastName, emplid, uid);

               UserCreated = true;
		
		}	catch(Exception e) {
        	LoggerUtil.errorLog("Error While Adding the User\n");
			System.out.println("Exceptin is thrown");
			e.printStackTrace();
			LoggerUtil.errorLog("Error : "+e);
			try {
				fileErrorDetail.write(e+DummyText);
				ErrorInfo.add(e+DummyText);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

	return UserCreated;
	
	}
			
	public static boolean checkUserExistance(String uid,String baseDN,DirContext dctx){
			boolean userExist = false;
			String filter = "(uid=" + uid +")";
			System.out.println("Validating existance of ID");
			System.out.println("filter is "+filter);
			System.out.println("base dn is " + baseDN);
			try {
				NamingEnumeration<?> answer = dctx.search(baseDN, filter,null);
				if(answer.hasMoreElements()){
					System.out.println("UID is present\n");
					 LoggerUtil.debugLog(uid+" UID is present\n");
					userExist = true;}
				else{
					System.out.println("User is not present\n");
					
				}

			} catch (NamingException e) {
				e.printStackTrace();
				LoggerUtil.errorLog("Error : "+e);
			}
			return userExist;
				}

	public static boolean checkUserExistanceWithEmpNo(String empNo,String baseDN,DirContext dctx){
		boolean userExist = false;
		String filter = "(employeenumber=" + empNo +")";
		System.out.println("Validating existance of ID");
		System.out.println("filter is "+filter);
		System.out.println("base dn is " + baseDN);
		try {
			NamingEnumeration<?> answer = dctx.search(baseDN, filter,null);
			if(answer.hasMoreElements()){
				System.out.println("EmployeeNumber Already assigned\n");
				 LoggerUtil.debugLog(empNo+" : EmployeeNumber Already assigned\n");
				userExist = true;}
			else{
				System.out.println("EmployeeNumber is not assigned\n");
			}
		} catch (NamingException e) {
			e.printStackTrace();
			LoggerUtil.errorLog("Error : "+e);
			try {
				fileErrorDetail.write(e+DummyText);
				ErrorInfo.add(e+DummyText);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		return userExist;
			}
	
	public static String getGroupDN(String cn,String baseDN,DirContext dctx){
		String entryDN="";
		String filter = "(cn=" + cn +"*)";
		SearchControls sc=new SearchControls();
		
		try {
			sc.setReturningAttributes(new String[]{"entryDN"});
		    sc.setSearchScope(2);
			NamingEnumeration<?> answer = dctx.search(baseDN, filter,sc);
			if(answer.hasMoreElements()){
				SearchResult sr = (SearchResult)answer.next();
			    Attributes entryAttrs = sr.getAttributes();		    
			    entryDN = entryAttrs.get("entryDN")!=null?entryAttrs.get("entryDN").get().toString():"";			    
			}
			else{
				System.out.println("Group is not present\n");
			}
		} catch (NamingException e) {
			e.printStackTrace();
			LoggerUtil.errorLog("Error : "+e);
			try {
				fileErrorDetail.write(e+DummyText);
				ErrorInfo.add(e+DummyText);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		return entryDN;
		
		
		}
}
