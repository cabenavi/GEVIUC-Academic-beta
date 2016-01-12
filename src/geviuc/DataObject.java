import java.awt.*;
import java.io.*; 
import java.io.File; 
import java.util.Vector;
import java.io.FileOutputStream; 
import java.io.PrintStream;
import java.sql.*;
import java.sql.Connection;
import java.sql.DatabaseMetaData;



public class DataObject extends DBObj{
	
	//id de objetos
	int[]			id_tgenerator;
	int[]			id_hgenerator;
	int[]			id_reserv;
	int[]			id_hdb_ser;
	int[]			id_hdb_paspur;
	int[]			id_hdb_irr;
	int[]			id_hdb_affl;
	int[]			id_busbar;
	int[]			id_line;
	int[]			id_load;
	int[]			id_solar;
	int[]			id_eolic;
	int[]			id_battery;
	int[]			id_pump;
	int[]			id_ens;
	int[]			id_csp;
	int[]			id_banderas;
	
	//Constructor
	public DataObject(){
	}
	//Cargar datos
	void loaddatafromdatabase(String typedb, String namedb, String userdb, String passdb) throws Exception{  
		
		int count=0;
		//Coneccion a base de datos
		initconnection(typedb, namedb, userdb, passdb);
		
		//cargando datos
		count = numberof(1,count);
		id_tgenerator = new int[count];
		loadint(1,id_tgenerator);  // id = 1
		
		count = numberof(2,count);
		id_hgenerator = new int[count];
		loadint(2,id_hgenerator);	 // id = 2
		
		count = numberof(3,count);
		id_reserv = new int[count];
		loadint(3,id_reserv);			 // id = 3
		
		count = numberof(4,count);
		id_hdb_ser = new int[count];
		loadint(4,id_hdb_ser);			 // id = 4
		
		count = numberof(5,count);
		id_hdb_paspur = new int[count];
		loadint(5,id_hdb_paspur);		 // id = 5
		
		count = numberof(6,count);
		id_hdb_irr = new int[count];
		loadint(6,id_hdb_irr); 			 // id = 6
		
		count = numberof(7,count);
		id_hdb_affl = new int[count];
		loadint(7,id_hdb_affl);			 // id = 7
		
		count = numberof(8,count);
		id_busbar = new int[count];
		loadint(8,id_busbar);			 // id = 8
		
		count = numberof(9,count);
		id_line = new int[count];
		loadint(9,id_line);				 // id = 9
		
		count = numberof(10,count);
		id_load = new int[count];
		loadint(10,id_load);			 // id = 10
	
		count = numberof(11,count);
		id_solar = new int[count];
		loadint(11,id_solar);			 // id = 11
	
		count = numberof(12,count);
		id_eolic = new int[count];
		loadint(12,id_eolic);			 // id = 12
	
		count = numberof(13,count);
		id_battery = new int[count];
		loadint(13,id_battery);			 // id = 13
	
		count = numberof(14,count);
		id_pump = new int[count];
		loadint(14,id_pump);			 // id = 14
		
		count = numberof(19,count);
		id_ens = new int[count];
		loadint(19,id_ens);			     // id = 19
		
		count = numberof(15,count);
		id_csp = new int[count];
		loadint(15,id_csp);	     		// id = 15
		
		count = numberof(16,count);
		id_banderas = new int[count];
		loadint(16,id_banderas);	     // id = 16

	}
	
	public void loadint(int id, int[] id_object) throws Exception{
		
		String consult1 = "Select idObject,nameObject from tblObject where idClass = " + id;
		Statement com1 	= mylink.createStatement();
		ResultSet res1 	= com1.executeQuery(consult1);
		
		int k=0;
		while (res1.next()) {
			id_object[k] = res1.getInt("idObject");
			System.out.println("ID Objeto: "+id_object[k]+ " Nombre:"+res1.getString("nameObject"));
			k++;
		}	
	res1.close();
	com1.close();
	}
	
	public int numberof(int id, int count) throws Exception{
		
		String consult1	= "Select COUNT(idObject) as rowcount from tblObject where idClass = " + id;
		Statement com1 	= mylink.createStatement();
		ResultSet res1 	= com1.executeQuery(consult1);
		res1.next();
		count =  res1.getInt("rowcount");
		//System.out.println("numero de filas= "+count);	
		res1.close();
		com1.close();
		
		return count;
	}	    	    
	
}