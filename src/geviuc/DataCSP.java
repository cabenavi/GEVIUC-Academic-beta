import java.awt.*;
import java.io.*; 
import java.io.File; 
import java.util.Vector;
import java.io.FileOutputStream; 
import java.io.PrintStream;
import java.sql.*;


public class DataCSP extends DBObj{
	
	//atributos CSP
	int[]			Id;
	String[]		Nombre;
	double[]		PotenciaSolar;
	double[]		PerdidasAlmacenamiento;
	double[]        EficienciaAlmacenamiento;
	double[]        EficienciaInyeccion;
	double[]        Vmin;
	double[]        Vmax;
	String[]        Propietario;
	double[]		Vini;
	double[]		Vfin;
	double[]		Pmin;
	double[]		Pmax;
	int[]			IsERNC;
	
	//Constructor
	public DataCSP(){
	}
	
	//Cargar datos
	void loaddatafromdatabase(String typedb, String namedb, String userdb, String passdb, int Tini_data, int Tfin_data,int object_id) throws Exception{  
		
		Thread thisThread = Thread.currentThread();
		
		int Nh = Tfin_data-Tini_data+1; //numero de horas
	
		Id 			 	 		 = new int[Nh];					//idAttribute=190
		Nombre					 = new String[Nh];				//idAttribute=191
		PotenciaSolar 	 		 = new double[Nh];				//idAttribute=197
		PerdidasAlmacenamiento   = new double[Nh];				//idAttribute=192
		EficienciaAlmacenamiento = new double[Nh];              //idAttribute=193
		EficienciaInyeccion		 = new double[Nh];              //idAttribute=194
		Vmin					 = new double[Nh];              //idAttribute=195
		Vmax					 = new double[Nh];              //idAttribute=196
		Propietario		 		 = new String[Nh];				//idAttribute=198
		Vini					 = new double[Nh];              //idAttribute=199
		Vfin					 = new double[Nh];              //idAttribute=200
		Pmin					 = new double[Nh];              //idAttribute=201
		Pmax					 = new double[Nh];              //idAttribute=202
		IsERNC					 = new int[Nh];              	//idAttribute=213
		
		//inicializacion de parametros
		for (int t=0;t<Nh;t++){
			Id[t] 						= 0;
			Nombre[t]					= "";
			PotenciaSolar[t]			= 0;
			PerdidasAlmacenamiento[t]	= 0;
			EficienciaAlmacenamiento[t]	= 0;
			EficienciaInyeccion[t]		= 0;
			Vmin[t]						= 0;
			Vmax[t]						= 0;
			Propietario[t]				= "";
			Vini[t]						= 0;
			Vfin[t]						= 0;
			Pmin[t]						= 0;
			Pmax[t]						= 0;
			IsERNC[t]					= 0;
		
		}
		
		//Coneccion a base de datos
		initconnection(typedb, namedb, userdb, passdb);
		
		//Cargando datos
		loadint("Id", 190, Id, Tini_data, Tfin_data, Nh, object_id); 
		loadstring("Nombre", 191, Nombre, Tini_data, Tfin_data, Nh, object_id); 
		loaddouble("PotenciaSolar", 197,PotenciaSolar, Tini_data, Tfin_data, Nh, object_id); 
		loaddouble("PerdidasAlmacenamiento", 192, PerdidasAlmacenamiento, Tini_data, Tfin_data, Nh, object_id); 
		loaddouble("EficienciaAlmacenamiento", 193, EficienciaAlmacenamiento, Tini_data, Tfin_data, Nh, object_id); 
		loaddouble("EficienciaInyeccion", 194, EficienciaInyeccion, Tini_data, Tfin_data, Nh, object_id); 
		loaddouble("Vmin", 195, Vmin, Tini_data, Tfin_data, Nh, object_id);
		loaddouble("Vmax", 196, Vmax, Tini_data, Tfin_data, Nh, object_id);
		loadstring("Propietario", 198,Propietario, Tini_data, Tfin_data, Nh, object_id); 
		loaddouble("Vini", 199, Vini, Tini_data, Tfin_data, Nh, object_id);
		loaddouble("Vfin", 200, Vfin, Tini_data, Tfin_data, Nh, object_id);
		loaddouble("Pmin", 201, Pmin, Tini_data, Tfin_data, Nh, object_id);
		loaddouble("Pmax", 202, Pmax, Tini_data, Tfin_data, Nh, object_id);
		loadint("IsERNC", 213, IsERNC, Tini_data, Tfin_data, Nh, object_id); 
		
		////chequeo de consistencia de datos
		for (int t=0;t<Nh;t++){
			if(PotenciaSolar[t]<0){
				System.out.println("Error: Potencia solar menor que cero");
				System.out.println("Error: Periodo= "+t);
				System.out.println("Error: Paner solar= "+Nombre[t]);
				thisThread.suspend();
			}
		}
	}
	
//**************************************************************************************************************************
// Funcion para cargar datos tipo double
//**************************************************************************************************************************	
	
	public void loaddouble(String NameAttribute, int id, double[] attribute,int tini, int tfin, int Nh, int object_id) throws Exception{
		
		String consult1	= "Select * from tblData where idAttribute = " + id + " and " + " idObject = "+object_id; 
		Statement com1 	= mylink.createStatement();
		ResultSet res1 	= com1.executeQuery(consult1);
		int fromTime;
		int toTime;
		double data;
			
		while (res1.next()) {
			fromTime = res1.getInt("fromTime");
			toTime	 = res1.getInt("toTime");
			data	 = res1.getDouble("valueData");
			
			for( int t= fromTime;t<=toTime;t++){
				//chequeo que datos esten dentro de ventana de datos de entrada
				if((t-tini)>=0 & (t-tini)<Nh){
					attribute[t-tini]= data;
					//System.out.println("NameAtri= "+NameAttribute+" id= " + id +" atribute = " + attribute[t-tini] + " t = " +(t-tini+1)+ " t= "+t);		
				}	
			}
		
		}	
	res1.close();
	com1.close();
	}
		
//******************************************************************************************************************************
// Funcion para cargar datos tipo int
//*******************************************************************************************************************************	
		
	public void loadint(String NameAttribute, int id, int[] attribute,int tini, int tfin, int Nh, int object_id ) throws Exception{
		
		String consult1	= "Select * from tblData where idAttribute = " + id + " and " + " idObject = "+object_id; 
		Statement com1 	= mylink.createStatement();
		ResultSet res1 	= com1.executeQuery(consult1);
		int fromTime;
		int toTime;
		int data;
			
		while (res1.next()) {
			fromTime = res1.getInt("fromTime");
			toTime	 = res1.getInt("toTime");
			data	 = res1.getInt("valueData");
			
			for( int t= fromTime;t<=toTime;t++){
				//chequeo que datos esten dentro de ventana de datos de entrada
				if((t-tini)>=0 & (t-tini)<Nh){
					attribute[t-tini]= data;	
				}	
			}
		
		}	
	res1.close();
	com1.close();
	}
//**************************************************************************************************************************************
//Funcion para cargar datos tipo String
//***************************************************************************************************************************************
	public void loadstring(String NameAttribute, int id, String[] attribute,int tini, int tfin, int Nh, int object_id ) throws Exception{
		
		String consult1	= "Select * from tblData where idAttribute = " + id + " and " + " idObject = "+object_id; 
		Statement com1 	= mylink.createStatement();
		ResultSet res1 	= com1.executeQuery(consult1);
		int fromTime;
		int toTime;
		String data;
			
		while (res1.next()) {
			fromTime = res1.getInt("fromTime");
			toTime	 = res1.getInt("toTime");
			data	 = res1.getString("valueData");
			
			for( int t= fromTime;t<=toTime;t++){
				//chequeo que datos esten dentro de ventana de datos de entrada
				if((t-tini)>=0 & (t-tini)<Nh){
					attribute[t-tini]= data;	
				}	
			}
		}	
	res1.close();
	com1.close();
	}		

}










