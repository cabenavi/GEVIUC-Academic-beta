import java.awt.*;
import java.io.*; 
import java.io.File; 
import java.util.Vector;
import java.io.FileOutputStream; 
import java.io.PrintStream;
import java.sql.*;


public class DataPump extends DBObj{
	
	//atributos Battery
	int[]			Id;
	String[]		Nombre;
	double[]        PBomba;
	double[]        KBomba;
	String[]        EmbBomba;
	int[]			Barra;
			
	//Constructor
	public DataPump(){
	}
	
	//Cargar datos
	void loaddatafromdatabase(String typedb, String namedb, String userdb, String passdb, int Tini_data, int Tfin_data,int object_id) throws Exception{  
		
		Thread thisThread = Thread.currentThread();
		
		int Nh = Tfin_data-Tini_data+1; //numero de horas
	
		Id 			 	 = new int[Nh];					//idAttribute=126
		Nombre			 = new String[Nh];				//idAttribute=127
		PBomba           = new double[Nh];              //idAttribute=147
        KBomba           = new double[Nh];              //idAttribute=148
		EmbBomba 		 = new String[Nh];              //idAttribute=149
		Barra 			 = new int[Nh];					//idAttribute=150
			
		//inicializacion de parametros
		for (int t=0;t<Nh;t++){
			Id[t] 				= 0;
			Nombre[t]			= "";
			PBomba[t]           = 0;    
			KBomba[t]           = 0;
			EmbBomba[t]	        = "";
			Barra[t] 			= 0;
		}
		
		//Coneccion a base de datos
		initconnection(typedb, namedb, userdb, passdb);
		
		//Cargando datos
		loadint("Id", 147, Id, Tini_data, Tfin_data, Nh, object_id);            //idAttribute=147
		loadstring("Nombre", 148, Nombre, Tini_data, Tfin_data, Nh, object_id); //idAttribute=148
		loaddouble("PBomba", 149, PBomba, Tini_data, Tfin_data, Nh, object_id); //idAttribute=149
		loaddouble("KBomba", 150, KBomba, Tini_data, Tfin_data, Nh, object_id); //idAttribute=150
		loadstring("EmbBomba", 151,EmbBomba, Tini_data, Tfin_data, Nh, object_id);//idAttribute=151
		loadint("Barra", 152, Barra, Tini_data, Tfin_data, Nh, object_id);        //idAttribute=152
		
		////chequeo de consistencia de datos
		for (int t=0;t<Nh;t++){
			if(PBomba[t]<0){
				System.out.println("Error: Potencia menor que cero");
				System.out.println("Error: Periodo= "+t);
				System.out.println("Error: Bomba= "+Nombre[t]);
				thisThread.suspend();
			}
			if(KBomba[t]<0){
				System.out.println("Error: Rendimiento menor que cero");
				System.out.println("Error: Periodo= "+t);
				System.out.println("Error: Bomba= "+Nombre[t]);
				thisThread.suspend();
			}
			if(Barra[t]<0){
				System.out.println("Error: Barra menor que cero");
				System.out.println("Error: Periodo= "+t);
				System.out.println("Error: Bomba= "+Nombre[t]);
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










