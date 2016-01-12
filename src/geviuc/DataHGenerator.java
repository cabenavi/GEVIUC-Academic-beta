import java.awt.*;
import java.io.*; 
import java.io.File; 
import java.util.Vector;
import java.io.FileOutputStream; 
import java.io.PrintStream;
import java.sql.*;


public class DataHGenerator extends DBObj{
	
	//atributos unidades hidraulicas
	int[]			ID;
	String[]		Nombre;
	int[] 			TminOn;
	int[] 			TminOff;
	int[]			TiniR;
	int[]			TiniN;
	double[]		PiniN;
	int[] 			GradRA;
	int[] 			GradRP;
	double[]		CostGradRA;
	double[]		CostGradRP;
	double[]     	GradS;
	double[]     	GradB;
	double[]		GradRE;
	double[]		OwnConsuption;
	double[]		HotStart_upCost;
	double[]		WarmStart_upCost;
	double[] 		ColdStart_upCost;
	int[] 			Thot;
	int[]			Twarm;
	double[]		Pming;
	double[] 		Pmaxg;
	double[]     	CespMeRA;
	double[]     	CespMeRP;
	double[]     	PoderCal;
	double[]		Pcomb;   
	int[]			Barra;
	int[]			Unavalaible;
	double[]		ForcedGenerator;
	double[]		Rend; 							//Rendimiento
	double[]		Qvmin;							//Vertimiento minimo
	double[]		Qvmax;							//Vertimiento maximo
	int[]			Ctur;							//ID central aguas abajo que recibe caudal turbinado
	int[]			Cver;							//ID central aguas abajo que recibe caudal vertido
	double[]        SpinningMax;     				//reserva en giro maxima
	double[]        SpinningMax2;     				//reserva en giro maxima v2 
	double[]        CPFMax;          				//control primario de frecuencia maximo
	int[]			Reserva_Pronta;  				//1 si aporta reserva pronto, 0 en caso contrario
    int[]			Commitment;      				//	
	double[]		StopCost;		 				//costo de partida
	int[] 			IsERNC; 		 				//1 si es ERNC, 0 en caso contrario
	double[]		Aflu;							//Afluente
	String[]		Type;							//Tipo de central hidraulica E: asociada a Embalse, S: Pasada en Serie, P: Pasada puta, R: Riego o Restriccion
	
	//Constructor
	public DataHGenerator(){
	}
	
	//Cargar datos
	void loaddatafromdatabase(String typedb, String namedb, String userdb, String passdb, int Tini_data, int Tfin_data,int object_id) throws Exception{  
		
		Thread thisThread = Thread.currentThread();
		
		int Nh = Tfin_data-Tini_data+1; //numero de horas
	
		ID				 = new int[Nh];					
		Nombre 			 = new String[Nh];				
		TminOn 			 = new int[Nh];					
		TminOff 		 = new int[Nh];					
		TiniR			 = new int[Nh];					
		TiniN 			 = new int[Nh];					
		PiniN 			 = new double[Nh];				
		GradRA 			 = new int[Nh];					
		GradRP 			 = new int[Nh]; 				
		CostGradRA		 = new double[Nh];				
		CostGradRP		 = new double[Nh];				
		GradS 			 = new double[Nh];				
		GradB 			 = new double[Nh];				
		GradRE			 = new double[Nh];				
		OwnConsuption 	 = new double[Nh];				
		HotStart_upCost  = new double[Nh];				
		WarmStart_upCost = new double[Nh];				
		ColdStart_upCost = new double[Nh];				
		Thot			 = new int[Nh];					
		Twarm			 = new int[Nh];					
		Pming 			 = new double[Nh];				
		Pmaxg 			 = new double[Nh];				
		CespMeRA 		 = new double[Nh];				
		CespMeRP 		 = new double[Nh];				
		PoderCal		 = new double[Nh];							
		Pcomb		     = new double[Nh];				  	
		Barra			 = new int[Nh];					
		Unavalaible		 = new int[Nh];						
		ForcedGenerator  = new double[Nh];				
		Rend			 = new double[Nh];
		Qvmin			 = new double[Nh];							
		Qvmax			 = new double[Nh];							
		Ctur			 = new int[Nh];							
		Cver			 = new int[Nh];
		SpinningMax		 = new double[Nh];              //idAttribute=
		SpinningMax2	 = new double[Nh];              //idAttribute=
		CPFMax		 	 = new double[Nh];              //idAttribute=
        Reserva_Pronta 	 = new int[Nh];					//idAttribute=
		Commitment		 = new int[Nh];					//idAttribute=
		StopCost		 = new double[Nh];				//idAttribute=
		IsERNC 			 = new int[Nh];                 //idAttribute=	
		Aflu			 = new double[Nh];
		Type			 = new String[Nh];
		
		//inicializacion de parametros
		for (int t=0;t<Nh;t++){
			ID[t]				= 0;
			Nombre[t] 			= "";
			TminOn[t] 			= 1;
			TminOff[t] 			= 1;
			TiniR[t]			= 1;
			TiniN[t] 			= 1;
			PiniN[t] 			= 0;
			GradRA[t] 			= 0;
			GradRP[t] 			= 0;
			CostGradRA[t]		= 0;
			CostGradRP[t]		= 0;
			GradS[t] 			= 0;
			GradB[t] 			= 0;
			GradRE[t]			= 0;
			OwnConsuption[t] 	= 0;
			HotStart_upCost[t]  = 0;
			WarmStart_upCost[t] = 0;
			ColdStart_upCost[t] = 0;
			Thot[t]			 	= 0;
			Twarm[t]			= 0;
			Pming[t] 			= 0;
			Pmaxg[t] 			= 0;
			CespMeRA[t] 		= 0;
			CespMeRP[t] 		= 0;
			PoderCal[t]			= 0;
			Pcomb[t]			= 0;   
			Barra[t]			= 0;
			Unavalaible[t]		= 0;
			ForcedGenerator[t]	= 0;
			Rend[t]				= 0;		
			Qvmin[t]			= 0;			
			Qvmax[t]			= 0;			
			Ctur[t]				= 0;			
			Cver[t]				= 0;
			SpinningMax[t]	    = 10000;
			SpinningMax2[t]	    = 10000;
			CPFMax[t]	   		= 0;
			Reserva_Pronta[t]	= 0;
			Commitment[t]		= 0;
			StopCost[t]			= 0;
			IsERNC[t] 			= 0;
			Aflu[t]				= 0;
			Type[t]				= "";	
		}
		
		//Coneccion a base de datos
		initconnection(typedb, namedb, userdb, passdb);
		
		//Cargando datos
		loadint("ID", 63, ID, Tini_data, Tfin_data, Nh, object_id);
		loadstring("Nombre", 64, Nombre, Tini_data, Tfin_data, Nh, object_id);
		loadint("TminOn", 65, TminOn, Tini_data, Tfin_data, Nh, object_id);
		loadint("TminOff", 66, TminOff, Tini_data, Tfin_data, Nh, object_id);
		loadint("TiniR", 67, TiniR, Tini_data, Tfin_data, Nh, object_id);
		loadint("TiniN", 68, TiniN, Tini_data, Tfin_data, Nh, object_id);
		loaddouble("PiniN", 69, PiniN, Tini_data, Tfin_data, Nh, object_id);
		loadint("GradRA", 70, GradRA, Tini_data, Tfin_data, Nh, object_id);
		loadint("GradRP", 71, GradRP, Tini_data, Tfin_data, Nh, object_id);
		loaddouble("CostGradRA",72, CostGradRA, Tini_data, Tfin_data, Nh, object_id);
		loaddouble("CostGradRP", 73, CostGradRA, Tini_data, Tfin_data, Nh, object_id);
		loaddouble("GradS", 74, GradS, Tini_data, Tfin_data, Nh, object_id);
		loaddouble("GradB", 75, GradS, Tini_data, Tfin_data, Nh, object_id);
		loaddouble("GradRE", 76, GradRE, Tini_data, Tfin_data, Nh, object_id);
		loaddouble("OwnConsuption", 77, OwnConsuption, Tini_data, Tfin_data, Nh, object_id);
		loaddouble("HotStart_upCost", 78, HotStart_upCost, Tini_data, Tfin_data, Nh, object_id);
		loaddouble("WarmStart_upCost", 79, WarmStart_upCost, Tini_data, Tfin_data, Nh, object_id);
		loaddouble("ColdStart_upCost", 80, ColdStart_upCost, Tini_data, Tfin_data, Nh, object_id);
		loadint("Thot", 81, Thot, Tini_data, Tfin_data, Nh, object_id);
		loadint("Twarm", 82, Twarm, Tini_data, Tfin_data, Nh, object_id);
		loaddouble("Pming", 83, Pming, Tini_data, Tfin_data, Nh, object_id);
		loaddouble("Pmaxg", 84, Pmaxg, Tini_data, Tfin_data, Nh, object_id);
		loaddouble("CespMeRA", 85, CespMeRA, Tini_data, Tfin_data, Nh, object_id);
		loaddouble("CespMeRP", 86, CespMeRP, Tini_data, Tfin_data, Nh, object_id);
		loaddouble("PoderCal", 87, PoderCal, Tini_data, Tfin_data, Nh, object_id);
		loaddouble("Pcomb", 88, Pcomb, Tini_data, Tfin_data, Nh, object_id);
		loadint("Barra", 89, Barra, Tini_data, Tfin_data, Nh, object_id);
		loadint("Unavalaible", 90, Unavalaible, Tini_data, Tfin_data, Nh, object_id);
		loaddouble("ForcedGenerator", 91, ForcedGenerator, Tini_data, Tfin_data, Nh, object_id);
		loaddouble("Rend", 99, Rend, Tini_data, Tfin_data, Nh, object_id);
		loaddouble("Qvmin", 101, Qvmin, Tini_data, Tfin_data, Nh, object_id);
		loaddouble("Qvmax", 100, Qvmax, Tini_data, Tfin_data, Nh, object_id);
		loadint("Ctur", 102, Ctur, Tini_data, Tfin_data, Nh, object_id);
		loadint("Cver", 103, Cver, Tini_data, Tfin_data, Nh, object_id);
		loaddouble("SpinningMax", 223,SpinningMax, Tini_data, Tfin_data, Nh, object_id);
	    loaddouble("CPFMax", 224,CPFMax, Tini_data, Tfin_data, Nh, object_id);
		loadint("Reserva_Pronta", 225, Reserva_Pronta, Tini_data, Tfin_data, Nh, object_id);
		loadint("Commitment", 226, Commitment, Tini_data, Tfin_data, Nh, object_id);
		loaddouble("StopCost", 227,StopCost, Tini_data, Tfin_data, Nh, object_id);
		loaddouble("SpinningMax2", 230,SpinningMax2, Tini_data, Tfin_data, Nh, object_id);
		loadint("IsERNC", 217, IsERNC, Tini_data, Tfin_data, Nh, object_id);
		loaddouble("Aflu", 218,Aflu, Tini_data, Tfin_data, Nh, object_id);
		loadstring("Type", 231,Type, Tini_data, Tfin_data, Nh, object_id);	
			
		//chequeo de consistencia de datos
		
		for (int t=0;t<Nh;t++){
			//no puede haber indisponibilidad y generacion forzada simultaneamente
			if(Unavalaible[t]==1 & ForcedGenerator[t]>0){
				System.out.println("Error: No puede haber indisponibilidad y generacion forzada simulataneamente");
				System.out.println("Error: Periodo= "+t);
				System.out.println("Error: Central= "+Nombre[t]);
				thisThread.suspend();
			}
			//potencia minima mayor a potencia maxima
			if(Pming[t]>Pmaxg[t]){
				System.out.println("Error: Potencia minima mayor a potencia maxima");
				System.out.println("Error: Periodo= "+t);
				System.out.println("Error: Central= "+Nombre[t]);
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










