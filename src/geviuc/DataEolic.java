import java.awt.*;
import java.io.*; 
import java.io.File; 
import java.util.Vector;
import java.io.FileOutputStream; 
import java.io.PrintStream;
import java.sql.*;


public class DataEolic extends DBObj{
	
	//atributos Eolic
	int[]			Id;
	String[]		Nombre;
	double[]		Viento;
	double[]		PotenciaEolic;
	double[]        FEolic;
	String[]        Propietario;
	double[]		CPFmax;
	double[]		SpinningMax;
	double[]		SpinningMax2;
	int[]			IsERNC;
	int[]			Barra;
	
	
	//Constructor
	public DataEolic(){
	}
	
	//Cargar datos
	void loaddatafromdatabase(String typedb, String namedb, String userdb, String passdb, int Tini_data, int Tfin_data,int object_id) throws Exception{  
		
		Thread thisThread = Thread.currentThread();
		
		int Nh = Tfin_data-Tini_data+1; //numero de horas
	
		Id 			 	 = new int[Nh];					//idAttribute=120
		Nombre			 = new String[Nh];				//idAttribute=121
		Viento 	 		 = new double[Nh];				//idAttribute=122
		PotenciaEolic    = new double[Nh];				//idAttribute=123
		FEolic 			 = new double[Nh];              //idAttribute=124
		Propietario		 = new String[Nh];				//idAttribute=125
		CPFmax			 = new double[Nh];              //idAttribute=202
		SpinningMax		 = new double[Nh];              //idAttribute=205
		SpinningMax2	 = new double[Nh];              //idAttribute=236
		IsERNC			 = new int[Nh];					//idAttribute=211
		Barra			 = new int[Nh];
		
		//inicializacion de parametros
		for (int t=0;t<Nh;t++){
			Id[t] 				= 0;
			Nombre[t]			= "";
			Viento[t]			= 0;
			PotenciaEolic[t]	= 0;
			FEolic[t]			= 0;
			Propietario[t]		="";
			CPFmax[t]			= 0;
			SpinningMax[t]		= 0;
			SpinningMax2[t]		= 0;
			IsERNC[t]			= 0;
			Barra[t]			= 0;
		}
		
		//Coneccion a base de datos
		initconnection(typedb, namedb, userdb, passdb);
		
		//Cargando datos
		loadint("Id", 120, Id, Tini_data, Tfin_data, Nh, object_id); //idAttribute=120
		loadstring("Nombre", 121, Nombre, Tini_data, Tfin_data, Nh, object_id); //idAttribute=121
		loaddouble("Viento", 122, Viento, Tini_data, Tfin_data, Nh, object_id); //idAttribute=122
		loaddouble("PotenciaEolic", 123, PotenciaEolic, Tini_data, Tfin_data, Nh, object_id); //idAttribute=123
		loaddouble("FEolic", 124, FEolic,Tini_data, Tfin_data, Nh, object_id); //idAttribute=124
		loadstring("Propietario", 125,Propietario, Tini_data, Tfin_data, Nh, object_id); //idAttribute=125
		loaddouble("CPFmax", 203, CPFmax,Tini_data, Tfin_data, Nh, object_id); //idAttribute=203
		loaddouble("SpinningMax", 205, SpinningMax,Tini_data, Tfin_data, Nh, object_id); //idAttribute=205
		loadint("IsERNC", 211, IsERNC, Tini_data, Tfin_data, Nh, object_id); //idAttribute=211
		loadint("Barra", 220, Barra, Tini_data, Tfin_data, Nh, object_id); //idAttribute=220
		loaddouble("SpinningMax2", 236, SpinningMax2,Tini_data, Tfin_data, Nh, object_id); //idAttribute=236
		
		//provisional
		/*for (int t=0;t<Nh;t++){
			SpinningMax2[t] 	= SpinningMax[t];
			SpinningMax[t]		= CPFmax[t];
			CPFmax[t]			= 0;
		}*/
		
		////chequeo de consistencia de datos
		for (int t=0;t<Nh;t++){
			if(PotenciaEolic[t]<0){
				System.out.println("Error: Potencia eolica menor que cero");
				System.out.println("Error: Periodo= "+t);
				System.out.println("Error: Central= "+Nombre[t]);
				thisThread.suspend();
			}
			if(CPFmax[t]<0){
				System.out.println("Error: CPF menor que cero");
				System.out.println("Error: Periodo= "+t);
				System.out.println("Error: Planta solar= "+Nombre[t]);
				thisThread.suspend();
			}
			if(CPFmax[t]>1){
				System.out.println("Error: CPF mayor que 1");
				System.out.println("Error: Periodo= "+t);
				System.out.println("Error: Planta solar= "+Nombre[t]);
				thisThread.suspend();
			}
			if(SpinningMax[t]<0){
				System.out.println("Error: Reserva Giro 1 menor que cero");
				System.out.println("Error: Periodo= "+t);
				System.out.println("Error: Planta solar= "+Nombre[t]);
				thisThread.suspend();
			}
			if(SpinningMax[t]>1){
				System.out.println("Error: Reserva Giro 1 mayor que 1");
				System.out.println("Error: Periodo= "+t);
				System.out.println("Error: Planta solar= "+Nombre[t]);
				thisThread.suspend();
			}
			if(SpinningMax2[t]<0){
				System.out.println("Error: Reserva Giro 2 menor que cero");
				System.out.println("Error: Periodo= "+t);
				System.out.println("Error: Planta solar= "+Nombre[t]);
				thisThread.suspend();
			}
			if(SpinningMax2[t]>1){
				System.out.println("Error: Reserva Giro 2 mayor que 1");
				System.out.println("Error: Periodo= "+t);
				System.out.println("Error: Planta solar= "+Nombre[t]);
				thisThread.suspend();
			}
			if(Barra[t]<0){
				System.out.println("Error: Barra menor que cero");
				System.out.println("Error: Periodo= "+t);
				System.out.println("Error: Planta solar= "+Nombre[t]);
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










