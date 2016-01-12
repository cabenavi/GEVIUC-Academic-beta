import java.awt.*;
import java.io.*; 
import java.io.File; 
import java.util.Vector;
import java.io.FileOutputStream; 
import java.io.PrintStream;
import java.sql.*;


public class DataReserv extends DBObj{
	
	//atributos Embalse
	int[]			Id;
	String[]		Nombre;
	double[]		Vmin;
	double[]		Vmax;
	int[] 		    Ctur;  //Central que recibe caudal turbiado aguas abajo
	int[] 	        Cver;  //Central que recibe caudal vertido aguas abajo
	int[] 	        Cfil;  //Central que recibe caudal filtrado aguas abajo
	double[]		Aflu;
	double[]		Ret;
	double[]		Qvmin;
    double[]		Qvmax;
	String[]		Propietario;
	double[]		Vini;	
	double[]		Vfin;
	double[] 		Vermin; //Vertimiento minimo
    double[]		Vermax; //Vertimiento maximo	
	int[]			ID_Central; //Central a la que esta conectada embalse
	double[]		ReserveMinVolume; //Nivel de embalse minimo para aportar reserva	
	
	
	Thread thisThread = Thread.currentThread();
	
	//Constructor
	public DataReserv(){
	}
	
	//Cargar datos
	void loaddatafromdatabase(String typedb, String namedb, String userdb, String passdb, int Tini_data, int Tfin_data,int object_id) throws Exception{  
		
		int Nh = Tfin_data-Tini_data+1; //numero de horas
	
		Id = new int[Nh];
		Nombre = new String[Nh];
		Vmin = new double[Nh];
		Vmax = new double[Nh];
		Ctur = new int[Nh];
		Cver = new int[Nh];
		Cfil = new int[Nh];
		Aflu = new double[Nh];
		Ret = new double[Nh];
		Qvmin = new double[Nh];
        Qvmax = new double[Nh];
		Propietario = new String[Nh];
		Vini = new double[Nh];
		Vfin = new double[Nh];
		Vermin =new double[Nh];
		Vermax =new double[Nh];
		ID_Central=new int[Nh];
		ReserveMinVolume=new double[Nh];
		//inicializacion de parametros
		for (int t=0;t<Nh;t++){
			Id[t] 		= 0;
			Nombre[t] 	= "";
			Vmin[t] 	= 0;
			Vmax[t] 	= 0;
			Ctur[t] 	= 0;
			Cver[t] 	= 0;
			Cfil[t] 	= 0;
			Aflu[t] 	= 0;
			Ret[t] 		= 0;
			Qvmin[t]    = 0;
			Qvmax[t]    = 0;
			Propietario[t] = "";
			Vini[t]        = 0;
			Vfin[t]        = 0;
			Vermin[t]		=0;
			Vermax[t]		=0;
			ID_Central[t]	=0;
			ReserveMinVolume[t]=0;
		}
		
		//Coneccion a base de datos
		initconnection(typedb, namedb, userdb, passdb);
		
		//Cargando datos
		loadint("Id", 134, Id, Tini_data, Tfin_data, Nh, object_id); //idAttribute=134
		loadstring("Nombre", 135, Nombre, Tini_data, Tfin_data, Nh, object_id); //idAttribute=135
		loaddouble("Vmin", 104, Vmin, Tini_data, Tfin_data, Nh, object_id); //idAttribute=104
		loaddouble("Vmax", 105, Vmax, Tini_data, Tfin_data, Nh, object_id); //idAttribute=105
		loadint("Ctur", 106,Ctur, Tini_data, Tfin_data, Nh, object_id); //idAttribute=106
		loadint("Cver", 107,Cver, Tini_data, Tfin_data, Nh, object_id); //idAttribute=107
		loadint("Cfil", 108,Cfil, Tini_data, Tfin_data, Nh, object_id); //idAttribute=108
		loaddouble("Aflu", 109,Aflu, Tini_data, Tfin_data, Nh, object_id); //idAttribute=109
		loaddouble("Ret", 110, Ret, Tini_data, Tfin_data, Nh, object_id); //idAttribute=110
		loaddouble("Qvmin", 111, Qvmin, Tini_data, Tfin_data, Nh, object_id); //idAttribute=111
		loaddouble("Qvmax", 112, Qvmax, Tini_data, Tfin_data, Nh, object_id); //idAttribute=112
		loadstring("Propietario", 113,Propietario, Tini_data, Tfin_data, Nh, object_id); //idAttribute=113
		loaddouble("Vini", 136, Vini, Tini_data, Tfin_data, Nh, object_id); //idAttribute=136
		loaddouble("Vfin", 154, Vfin, Tini_data, Tfin_data, Nh, object_id); //idAttribute=154
		loaddouble("Vermin", 214, Vermin, Tini_data, Tfin_data, Nh, object_id); //idAttribute=214
		loaddouble("Vermax", 215, Vermax, Tini_data, Tfin_data, Nh, object_id); //idAttribute=215
		loadint("ID_Central", 216, ID_Central, Tini_data, Tfin_data, Nh, object_id); //idAttribute=216
		loaddouble("ReserveMinVolume", 232, ReserveMinVolume, Tini_data, Tfin_data, Nh, object_id); //idAttribute=232
		
		//chequeo de consistencia de datos
		for (int t=0;t<Nh;t++){
			//no puede haber indisponibilidad y generacion forzada simultaneamente
			if(Vini[t]<Vmin[t]){
				System.out.println("WARNING: Volumen inicial menor que volumen minimo");
				System.out.println("WARNING: Periodo= "+t);
				System.out.println("WARNING: Embalse= "+Nombre[t]);
				thisThread.suspend();
			}
			if(Vfin[t]<Vmin[t]){
				System.out.println("WARNING: Volumen final menor que volumen minimo");
				System.out.println("WARNING: Periodo= "+t);
				System.out.println("WARNING: Embalse= "+Nombre[t]);
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










