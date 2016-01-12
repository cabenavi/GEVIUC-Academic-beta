import java.io.*; 
import java.sql.*;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

public class DBObj {
static Connection mylink=null;
static int ntry=0;
    public DBObj() {
    }
//***************************************************************************************************************************
// Close DB Connection
//**************************************************************************************************************************    
    static void closeLink(){
    	if (mylink!= null){
    		try{
    			ntry=0;
				mylink.close();
				System.out.println("");
    		}catch(Exception e){
    			System.out.println("Can't Close DB Link");
    		}
    		mylink= null;
    	}
    }
//***************************************************************************************************************************
// Open DB Connection
//**************************************************************************************************************************

	static void initconnection(String typedb, String namedb, String userdb, String passdb){
	 	if (mylink!=null){
	 		ntry++;
	 	//	System.out.print(".");
			return;
	 	}
	 	System.out.println("/");
		//Access
		String completenamedb=null;
		//Postgresql
		String url = "jdbc:postgresql://localhost/moo";
	
		//MSACCESS DB:
		if(typedb.equals("odbc")){
	//		completenamedb="jdbc:odbc:"+namedb;
			try {
				//Acceso al driver
	 //           Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
				
				// Setting up the DataSource object
//1				sun.jdbc.odbc.ee.DataSource ds = new sun.jdbc.odbc.ee.DataSource();
//1				ds.setUser(userdb);
//1				ds.setPassword(passdb);
//1				ds.setDatabaseName(namedb);				
//1				mylink = ds.getConnection();
        		//String dsn = "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ=." + File.separator + "Database" + File.separator + DBName + ".mdb";
        		
				String dsn = "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb,*.accdb)};DBQ="+namedb;
        		
				
				System.out.print("Conectando con DB en '"+namedb+"'... ");
        		
				Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
				completenamedb="jdbc:odbc:"+"SIC-GIZ-2";
				
				//mylink = DriverManager.getConnection(dsn, userdb, passdb);            	
				mylink = DriverManager.getConnection(completenamedb, userdb, passdb);
				
				//mylink = ds.getConnection();
				System.out.println("Link Creado.");
				
	        }
/*	        catch(java.lang.ClassNotFoundException e) {
	        	System.err.print("ClassNotFoundException: ");
	           	System.err.println(e.getMessage());
	        } */
			catch(Exception e) {
	        	System.err.print("Exception: ");
	           	System.err.println(e.getMessage());
	        }
		    /*try {
				mylink = DriverManager.getConnection(completenamedb, userdb, passdb);
			} 
			catch(SQLException ex) {
				System.err.println("SQLException: " + ex.getMessage());
			}*/
	    }
	    //POSTGRESQL DB:
	    else if(typedb.equals("postgresql")){
	    	try {
	    		//Acceso al driver
	            Class.forName("org.postgresql.Driver");
	        }
	        catch(java.lang.ClassNotFoundException e) {
	        	System.err.print("ClassNotFoundException: ");
	           	System.err.println(e.getMessage());
	        }
	         try {
				mylink = DriverManager.getConnection(url, userdb, passdb);
			} 
			catch(SQLException ex) {
				System.err.println("SQLException: " + ex.getMessage());
			}
	    }
		//MySQL DB:
	    else if(typedb.equals("mysql")){
			System.out.println("conectando a BD mysql");
			try {
				//Acceso al driver
				Class.forName("com.mysql.jdbc.Driver");
			}
			catch(java.lang.ClassNotFoundException e){
				System.err.print("ClassNotFoundException: ");
	           	System.err.println(e.getMessage());
			}
			try{
				mylink = DriverManager.getConnection("jdbc:mysql://localhost/"+namedb, userdb, passdb);
			}
			catch(SQLException ex) {
				System.err.println("SQLException: " + ex.getMessage());
			}	
		}
	}
    
}