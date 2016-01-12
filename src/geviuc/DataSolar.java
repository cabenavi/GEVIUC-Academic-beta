import java.awt.*;
import java.io.*; 
import java.io.File; 
import java.util.Vector;
import java.io.FileOutputStream; 
import java.io.PrintStream;
import java.sql.*;


public class DataSolar extends DBObj{
	
	//atributos Solar
	int[]			Id;
	String[]		Nombre;
	double[]		PotenciaSolar;	//Potencia solar maxima de acuerdo a perfil horario ingresado	
	double[]		IntensidadSolar;
	double[]        FSolar;
	String[]        Propietario;
	double[]        CPFmax;    		// Control Primario de Frecuencia maximo expresado como porcentaje de PotenciaSolar (ex Rgmax)
	double[]		SpinningMax; 	// Reserva Giro 1 maxima expresada como porcentaje de PotenciaSolar (ex Rgsec_max)  
	double[]		SpinningMax2; 	// Reserva Giro 2 maxima expresada como porcentaje de PotenciaSolar  
	int[]			IsERNC;
	int[]			Barra;
	
	
	//Constructor
	public DataSolar(){
	}
	
	//Cargar datos
	void loaddatafromdatabase(String typedb, String namedb, String userdb, String passdb, int Tini_data, int Tfin_data,int object_id) throws Exception{  
		
		Thread thisThread = Thread.currentThread();
		
		int Nh = Tfin_data-Tini_data+1; //numero de horas
	
		Id 			 	 = new int[Nh];					//idAttribute=114
		Nombre			 = new String[Nh];				//idAttribute=115
		PotenciaSolar 	 = new double[Nh];				//idAttribute=116
		IntensidadSolar  = new double[Nh];				//idAttribute=117
		FSolar 			 = new double[Nh];              //idAttribute=118
		Propietario		 = new String[Nh];				//idAttribute=119
		CPFmax			 = new double[Nh];
		SpinningMax		 = new double[Nh];
		SpinningMax2	 = new double[Nh];		
		IsERNC			 = new int[Nh];					//idAttribute=210
		Barra			 = new int[Nh];
		
		//inicializacion de parametros
		for (int t=0;t<Nh;t++){
			Id[t] 				= 0;
			Nombre[t]			= "";
			PotenciaSolar[t]	= 0;
			IntensidadSolar[t]	= 0;
			FSolar[t]			= 0;
			Propietario[t]		="";
			CPFmax[t] 			= 0;
			SpinningMax[t] 		= 0;
			SpinningMax2[t] 	= 0;
			IsERNC[t]			= 0;
			Barra[t]			= 0;
		}
		
		//Coneccion a base de datos
		initconnection(typedb, namedb, userdb, passdb);
		
		//Cargando datos
		loadint("Id", 114, Id, Tini_data, Tfin_data, Nh, object_id); //idAttribute=114
		loadstring("Nombre", 115, Nombre, Tini_data, Tfin_data, Nh, object_id); //idAttribute=115
		loaddouble("PotenciaSolar", 116,PotenciaSolar, Tini_data, Tfin_data, Nh, object_id); //idAttribute=116
		loaddouble("IntensidadSolar", 117, IntensidadSolar, Tini_data, Tfin_data, Nh, object_id); //idAttribute=117
		loaddouble("FSolar", 118, FSolar,Tini_data, Tfin_data, Nh, object_id); //idAttribute=118
		loadstring("Propietario", 119,Propietario, Tini_data, Tfin_data, Nh, object_id); //idAttribute=119
		loaddouble("CPFmax", 204, CPFmax,Tini_data, Tfin_data, Nh, object_id); //idAttribute=204
		loaddouble("SpinningMax", 206, SpinningMax,Tini_data, Tfin_data, Nh, object_id); //idAttribute=206
		loadint("IsERNC", 210, IsERNC,Tini_data, Tfin_data, Nh, object_id); //idAttribute=206
		loadint("Barra", 219, Barra,Tini_data, Tfin_data, Nh, object_id); //idAttribute=222
		loaddouble("SpinningMax2", 235, SpinningMax2,Tini_data, Tfin_data, Nh, object_id); //idAttribute=235
		
		//provisional
		/*for (int t=0;t<Nh;t++){
			SpinningMax2[t] 	= SpinningMax[t];
			SpinningMax[t]		= CPFmax[t];
			CPFmax[t]			= 0;
		}*/
		
		////chequeo de consistencia de datos
		for (int t=0;t<Nh;t++){
			if(PotenciaSolar[t]<0){
				System.out.println("Error: Potencia solar menor que cero");
				System.out.println("Error: Periodo= "+t);
				System.out.println("Error: Planta solar= "+Nombre[t]);
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










