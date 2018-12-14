package svh;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class PropertyFileHelper {
	private static Properties props;
	private static PropertyFileHelper propertyFileHelper = null;
	
	private PropertyFileHelper()
	{
		
	}
		
	public Properties getProperties()
	{
		if(props == null)
		{
		    props = new Properties();
		    
		    try 
			{
				ClassLoader classLoader = this.getClass().getClassLoader();
				props.load(classLoader.getResourceAsStream("config/soxreport.properties"));	  
				
			} 
			catch (FileNotFoundException exc) 
			{
	            exc.printStackTrace();
	        }catch (IOException exc) 
	        {
	            exc.printStackTrace();
	        }
		}
		        
        return props;
	}
	
	public static PropertyFileHelper getInstance()
	{
		if (propertyFileHelper == null)
		{
			propertyFileHelper = new PropertyFileHelper();
		}
		
		return propertyFileHelper;
	}
}
