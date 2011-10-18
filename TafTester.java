package TafTester;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale; 

import sk.diko.manageFiles.LoginTextFile; 


public class TafTester {

	Connection m_oraConnection = null;			// connection to oracle
	LoginTextFile m_cfgLst = null;				// used as a reference to list of configuration values
	LoginTextFile m_logFile = null;				// log file (not mandatory)
	int m_logLevel = 1;							// log level
	Statement m_statement = null;				// current sql statement
	ResultSet m_rs = null;						// current result set
	int m_columnCount = 0;						// count of current columns
	int m_rowCount = 0;							// count of current rows
	int m_updateCount = -1;						// update count
	
	
	/**
	 * small program which call oracle commands via jdbc, using specified connection string
	 * todo: bug fixing :); commit/rollback, connection pool
	 * @author Peter Diko, 2011
	 */
	public TafTester ()
	{
		
	}
	
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws SQLException 
	 */
	public static final void main(String[] args) throws SQLException, IOException {
		
		boolean printUsageFlg = true;
		String cfgFileNm = null;
		String logFileNm = null;
		String logLevel = null;
		
		// Set the default locale to custom locale
		Locale locale = new Locale("en", "US");
		Locale.setDefault(locale);
		
		if ( args.length > 0 )
		{
			int size = args.length;
			
			for ( int ii = 0; ii < size; ii++ )
			{
				if ( args[ii].startsWith("cfg=") )
				{
					cfgFileNm = args[0].replaceFirst("cfg=", "");
					printUsageFlg = false;
				}
				else if ( args[ii].startsWith("log=") )
					logFileNm = args[ii].replaceFirst("log=", "");
				else if ( args[ii].startsWith("logLevel=") )
					logLevel = args[ii].replaceFirst("logLevel=", "");
			}
		}
		
		if ( printUsageFlg )
			PrintUsage();
		else
		{
			TafTester oraTaf = null;
			
			try
			{
				oraTaf = new TafTester();
				
				if ( oraTaf == null )
					throw new Exception("TafTester class not initialized!");
				else
				{
					int level = 1;
					if ( logLevel != null )
						level = Integer.valueOf(logLevel).intValue();
					
					oraTaf.RunTaf(cfgFileNm, logFileNm, level);
				}
			}
			catch ( Exception e )
			{
				//System.out.println(e.getMessage());
				System.out.println(GetFullExceptionText(e));
			}
		}
	}
	
    protected static void PrintUsage( )
    {
    	String info = "Usage: cfg=[config file] <log=[log file]> <logLevel=[log level]>";
    	
    	info += "\n\nconfig file format: [key]|[value]";
    	info += "\nconfig file keys:";
    	info += "\nconnectString - jdbc connect string, f.e. jdbc:oracle:thin:@127.0.0.1:1521:orcl";
    	info += "\nlogin - login name for for connection";
    	info += "\npassword - password for connection";
    	info += "\ndefaultSql - first sql command executed automaticaly";
    	info += "\n# - comment";
    	info += "\n\nlog level: 0 - Debug, 1 - Info, 2 - Warning, 3 - Error";
    	
    	System.out.println(info);
	}
    
    protected void RunTaf(String CfgFileNm, String LogFileNm, int LogLevel) throws Exception
    {
    	try
    	{
			// open log
			if ( LogFileNm != null )
			{
				OpenLogFile(LogFileNm, LogLevel);
			}
			
			WriteLog("Info", "Running TAF");
			
			// read config
			ReadConfig(CfgFileNm);
			
			// connect oracle
			String connStr = m_cfgLst.getConfigValue("connectString");
			String user = m_cfgLst.getConfigValue("login");
			
			System.out.println("Connecting using: " + connStr);
			System.out.println("Login: " + user);
			
			ConnectOracle(connStr, user, m_cfgLst.getConfigValue("password"));
			
			System.out.println("Connected");
			System.out.println();
			
			try
			{
				// execute default
				String defSql = m_cfgLst.getConfigValue("defaultSql");
				if ( defSql != null )
				{
					WriteLog("Info", "Default sql: defSql");
					System.out.println("Executing default sql: " + defSql);
					ExecSql(defSql);
					PrintResults(1);
				}
			}
			catch ( SQLException e )
	    	{
	    		String msg = e.getMessage();
	    		int code = e.getErrorCode();
	    		String state=e.getSQLState();
	    		WriteLog("Error", code + ": " + msg + " SQLState: " + state);
	    		System.out.println(code + ": " + msg + " SQLState: " + state);
	    	}
			
			// wait for command
			String cmd;
		    BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
		    	
		    while (true)
		    {
		    	try
		    	{
		    		System.out.print("Command> ");
		    		
		    		cmd = stdin.readLine();
		    		
		    		if ( cmd.equals("help") )
		    		{
		    			DisplayHelp();
		    		}
		    		else if ( cmd.equals("exec") )
		    		{
		    			String sql = "";
		    			String line;
		    			System.out.print("SQL> ");
		    			while (true)
		    			{
		    				line = stdin.readLine();
		    				sql +=line;
		    				
		    				if ( line.endsWith(";") )
		    				{
		    					sql = sql.substring(0, sql.lastIndexOf(";"));
		    					m_statement = ExecSql(sql);
		    					PrintResults(1);
		    					break;
		    				}

		    				if ( line.equals("") )
		    					break;
		    			}
		    		}
		    		else if ( cmd.equals("next") )
		    		{
		    			PrintResults(1);
		    		}
		    		else if ( cmd.equals("more") )
		    		{
		    			PrintResults(10);
		    		}
		    		else if ( cmd.equals("rest") )
		    		{
		    			PrintResults(-1);
		    		}
		    		else if ( cmd.equals("exit") )
		    		{
		    			WriteLog("Info", "Exiting programm");
		    			break;
		    		}
		       	}
		    	catch ( SQLException e )
		    	{
	    		String msg = e.getMessage();
	    		int code = e.getErrorCode();
	    		String SqlState = e.getSQLState();
	    		WriteLog("Error", code + " " + msg + " - SQLState: " + SqlState);
	    		System.out.println(code + " " + msg + " - SQLState: " + SqlState);
		    	}
		    }
	    }
    	catch ( Exception e )
    	{
    		WriteLog("Error", GetFullExceptionText(e));
    		throw e;
    	}
    	finally
    	{
    		WriteLog("Info", "Stopping TAF");
    		DisconnectOracle();
			CloseLogFile();
    	}
    }
    
    protected Statement ExecSql(String SqlQuery) throws SQLException, IOException
    {
    	WriteLog("Debug", "Creating statement");
    	
    	m_columnCount = 0;
    	m_rowCount = 0;
    	m_rs = null;
    	
    	if ( m_statement == null )
    		m_statement = m_oraConnection.createStatement();
    	
    	WriteLog("Debug", "Statement created");
    	
   		WriteLog("Info", "Executing: " + SqlQuery);
   		if ( m_statement.execute(SqlQuery) )
   		{
   			m_updateCount = -1;
   		}
   		// update
   		else
   		{
   			// to ensure this is a first call for it
   			m_updateCount = m_statement.getUpdateCount();
   		}
    	
    	return m_statement;
    }
    
    protected void PrintResults( int CountToPrint ) throws SQLException, IOException
    {
    	boolean printHeaderFlg = false;
    	boolean moreResultsFlg = true;
    	StringBuffer buf = null;
    	int displayedResults = 0;
    	
    	// check for update
    	if ( m_updateCount != -1 )
    	{
    		try
	    	{
		   		WriteLog("Info", "Displaying number of updated rows: " + m_updateCount);
		   		
		   		if ( m_updateCount == 0 )
		   			System.out.println("Rows updated : " + m_updateCount + " (alter, create, truncate, drop...)");
		   		else    		
		   			System.out.println("Rows updated : " + m_updateCount);
		   		
		   		if ( m_statement.getMoreResults() == false )
		   		{
		   			m_statement.close();
		   			m_statement = null;
		   			m_updateCount = -1;
		   		}
		   		else
		   		{
		   			m_updateCount = m_statement.getUpdateCount();
		   		}
            } // try
	   		catch ( SQLException e )
	    	{
	    		String msg = e.getMessage();
	    		int code = e.getErrorCode();
	    		String SqlState = e.getSQLState();
	    		WriteLog("Error", code + " " + msg + " - SQLState: " + SqlState);
	    		System.out.println(code + " " + msg + " - SQLState: " + SqlState);
	    	} 
    	}
    	else
    	{
	    	WriteLog("Info", "Displaying results, count: " + CountToPrint);
	    	
	    	while ( moreResultsFlg )
	    	{
	    		if ( m_rs == null )
	    		{
	         		try
    	        	{
		    			if ( m_statement != null )
		    			{   
		    				if ( CountToPrint > 0 )
		    					m_statement.setFetchSize(CountToPrint);
		    				else
		    					m_statement.setFetchSize(0);
		    				m_rs = m_statement.getResultSet();
		    				printHeaderFlg = true;
		    				WriteLog("Debug", "Reading new result set");
		    			}
		    			else
		    				moreResultsFlg = false;
		    		} // try
		    		catch ( SQLException e )
			    	{
			    		String msg = e.getMessage();
			    		int code = e.getErrorCode();
			    		String SqlState = e.getSQLState();
			    		WriteLog("Error", code + " " + msg + " - SQLState: " + SqlState);
			    		System.out.println(code + " " + msg + " - SQLState: " + SqlState);
			    	} 
	    		}
	    	
	    		if ( m_rs == null )
	    			break;
			    	
	    		if ( printHeaderFlg )
	    			m_columnCount = PrintHeader(m_rs);
	    	
	//			buf = new StringBuffer("");
	
				while ( m_rs.next() )
				{
					// Loop through each column, getting the column
					// data and displaying
					
					buf = new StringBuffer("");
					
					for ( int ii=1; ii <= m_columnCount; ii++ )
					{
						if ( ii > 1 )
						{
							buf.append(", ");
						}
	
						buf.append(m_rs.getString(ii));
					} // Loop through each column
	
					buf.append("\n");
					m_rowCount++;
					System.out.println(buf);
					displayedResults++;
					if ( displayedResults == CountToPrint )
						break;
				}
	
				if ( displayedResults == CountToPrint )
					break;
	
	    		try
	    		{	
					moreResultsFlg = m_statement.getMoreResults();
					m_rs = null;
					if ( moreResultsFlg == false )
					{
						m_statement.close();
						m_statement = null;
					}
	    		}
	    		catch ( SQLException e )
		    	{
		    		String msg = e.getMessage();
		    		int code = e.getErrorCode();
		    		String SqlState = e.getSQLState();
		    		WriteLog("Error", code + " " + msg + " - SQLState: " + SqlState);
		    		System.out.println(code + " " + msg + " - SQLState: " + SqlState);
		    	} 
	    	}
    	
	    	if ( displayedResults > 0 )
	    		System.out.println("Rows : " + m_rowCount);
	    	else
	    		System.out.println("No more results");
	    	
	    	WriteLog("Info", "Really displayed results, count: " + displayedResults);
    	}
    }
    
    protected void DisplayHelp()
    {
    	System.out.println();
    	System.out.println("Commands:");
    	System.out.println("exec - enter sql to execute");
    	System.out.println("next - next one result");
    	System.out.println("more - bunch of results");
    	System.out.println("rest - all remained results");
    	System.out.println("help - display this help");
    	System.out.println("exit - exit");
    	System.out.println();
    }
    	
    protected int PrintHeader(ResultSet RS) throws SQLException, IOException
    {
    	WriteLog("Info", "Displaying columns");
    	
    	ResultSetMetaData rsmd = RS.getMetaData();

		int numCols = rsmd.getColumnCount();
		StringBuffer buf = new StringBuffer();

		// get column header info
		for ( int ii = 1; ii <= numCols; ii++ )
		{
			if (ii > 1)
			{
				buf.append(",");
			}

			buf.append(rsmd.getColumnLabel(ii));
		}
		buf.append("\n");
		
		System.out.println(buf);
		
		return numCols;
    }
	
	protected void ReadConfig(String CfgFileNm) throws IOException
	{
		WriteLog("Info", "Reading config file: " + CfgFileNm);
		
		LoginTextFile cfgFile = new LoginTextFile(CfgFileNm, true);
		cfgFile.readStandardConfig();
		m_cfgLst = cfgFile;
		
		cfgFile.close();
		
		WriteLog("Info", "Config file read");
	}
	
	protected void OpenLogFile(String LogFileNm, int LogLevel) throws IOException
	{
		if ( LogFileNm != null )
		{
			m_logFile = new LoginTextFile(LogFileNm, false, true);
			m_logLevel = LogLevel;
			WriteLog("Debug", "Log file initialized, log level" + LogLevel);
		}
	}
	
	protected void CloseLogFile() throws IOException
	{
		WriteLog("Debug", "Closing log file");
		if ( m_logFile != null )
		{
			m_logFile.close();
			m_logFile = null;
		}
	}
	
	
	protected Connection ConnectOracle (String ConnectStr, String LoginStr, String PasswdStr) throws ClassNotFoundException, SQLException, IOException
	{
		WriteLog("Info", "Connecting oracle using connection string: " + ConnectStr);
		WriteLog("Debug", "Login=" + LoginStr + "\nPasswd=" + PasswdStr);
		
		Class.forName("oracle.jdbc.OracleDriver");
		//Class.forName("oracle.jdbc.driver.OracleDriver");
		
		m_oraConnection = DriverManager.getConnection(ConnectStr, LoginStr, PasswdStr);
		
		WriteLog("Info", "Connected to oracle");
		return m_oraConnection;
	}
	
	protected void DisconnectOracle () throws SQLException, IOException
	{
		WriteLog("Info", "Closing oracle connection");
		try
		{
			if ( m_statement != null )
			{
				m_statement.close();
				m_statement = null;
			}
			
			if ( m_oraConnection != null )
			{
				m_oraConnection.close();
				m_oraConnection = null;
		    }
		}
	    catch ( SQLException e )
    	{
    		String msg = e.getMessage();
    		int code = e.getErrorCode();
    		String SqlState = e.getSQLState();
    		WriteLog("Error", code + " " + msg + " - SQLState: " + SqlState);
    		System.out.println(code + " " + msg + " - SQLState: " + SqlState);
    	} 
		
		WriteLog("Info", "Oracle connection closed");
	}
	
	protected void WriteLog( String Level, String Txt ) throws IOException
	{
		int levelNum = 2;
		
		if ( m_logFile != null )
		{
			// decode level
			if ( Level.equalsIgnoreCase("Debug") )
				levelNum = 0;
			else if ( Level.equalsIgnoreCase("Info") )
				levelNum = 1;
			if ( Level.equalsIgnoreCase("Warning") )
				levelNum = 2;
			if ( Level.equalsIgnoreCase("Error") )
				levelNum = 3;
			
			SimpleDateFormat df = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
			Date now = new Date();
						
			if ( levelNum >= m_logLevel )
				m_logFile.writeLine("[" + df.format(now) + "] [" + Level + "] " + Txt);
		}
	}
	
	protected static String GetFullExceptionText(Exception Excp)
	{
		String text = Excp.getMessage();
		StackTraceElement[] elmLst = Excp.getStackTrace();
		
		if ( elmLst != null )
		{
			int size = elmLst.length;
			
			if ( size > 20 )
				size = 20;
			
			for ( int ii = 0; ii < size; ii++ )
			{
				text += "\n" + elmLst[ii].toString();
			}
			
		}
		
		return text;
	}

}
