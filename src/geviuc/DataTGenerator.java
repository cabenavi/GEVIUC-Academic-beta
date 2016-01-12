import java.awt.*;
import java.io.*; 
import java.io.File; 
import java.util.Vector;
import java.io.FileOutputStream; 
import java.io.PrintStream;
import java.sql.*;


public class DataTGenerator extends DBObj{
	
	//atributos maquinas termicas
	int[]			ID;
	String[]		Nombre;   							//Nombre configuracion
	String[]		Propietario;
	String[]        NombreCentral;						//Nombre central
	String[]		Tecnologia;
	String[]		Combustible;	
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
	double[]        Vini;
	double[]        Vfin;
	double[]        Vmin;
	double[]        Vmax;
	double[]        KEstanque;
    double[]        AEstanque;
	double[]        SpinningMax;     //reserva en giro maxima
	double[]        SpinningMax2;     //reserva en giro maxima v2 
	double[]        CPFMax;          //control primario de frecuencia maximo
	int[]			Reserva_Pronta;  //1 si aporta reserva pronto, 0 en caso contrario
    int[]			Commitment;      //	
	double[]		StopCost;		 //costo de partida
	int[]			IDAcoplaTV;		 //id de la turbina a gas (TG) a la que esta acoplada turbina vapor
	double[]		FactorAcoplaTVTG;// factor de acoplamiento entre la potencia de la TV con la potencia de la TG, PTV= FactorAcoplaTVTG x FTG 
	int[] 			IsERNC; 		 // 1 si es ERNC, 0 en caso contrario
	int[] 			Dependence;		 // 
	double[]		Fdependence;
	
	//Constructor
	public DataTGenerator(){
	}
	
	//Cargar datos
	void loaddatafromdatabase(String typedb, String namedb, String userdb, String passdb, int Tini_data, int Tfin_data,int object_id) throws Exception{  
		
		Thread thisThread = Thread.currentThread();
		
		int Nh = Tfin_data-Tini_data+1; //numero de horas
	
		ID				 = new int[Nh];					//idAttribute=1
		Nombre 			 = new String[Nh];				//idAttribute=2
		NombreCentral	 = new String[Nh];				//idAttribute=185		
		Tecnologia		 = new String[Nh];				//idAttribute=186
		Combustible		 = new String[Nh];				//idAttribute=187
		TminOn 			 = new int[Nh];					//idAttribute=3
		TminOff 		 = new int[Nh];					//idAttribute=4
		TiniR			 = new int[Nh];					//idAttribute=5
		TiniN 			 = new int[Nh];					//idAttribute=6	
		PiniN 			 = new double[Nh];				//idAttribute=7	
		GradRA 			 = new int[Nh];					//idAttribute=8
		GradRP 			 = new int[Nh]; 				//idAttribute=9	
		CostGradRA		 = new double[Nh];				//idAttribute=10
		CostGradRP		 = new double[Nh];				//idAttribute=11
		GradS 			 = new double[Nh];				//idAttribute=12
		GradB 			 = new double[Nh];				//idAttribute=13
		GradRE			 = new double[Nh];				//idAttribute=14
		OwnConsuption 	 = new double[Nh];				//idAttribute=15
		HotStart_upCost  = new double[Nh];				//idAttribute=16
		WarmStart_upCost = new double[Nh];				//idAttribute=17
		ColdStart_upCost = new double[Nh];				//idAttribute=18
		Thot			 = new int[Nh];					//idAttribute=19
		Twarm			 = new int[Nh];					//idAttribute=20
		Pming 			 = new double[Nh];				//idAttribute=21
		Pmaxg 			 = new double[Nh];				//idAttribute=22
		CespMeRA 		 = new double[Nh];				//idAttribute=23
		CespMeRP 		 = new double[Nh];				//idAttribute=24
		PoderCal		 = new double[Nh];				//idAttribute=25				
		Pcomb		     = new double[Nh];				//idAttribute=26   	
		Barra			 = new int[Nh];					//idAttribute=27 
		Unavalaible		 = new int[Nh];					//idAttribute=28	
		ForcedGenerator  = new double[Nh];				//idAttribute=29
		Vini			 = new double[Nh];              //idAttribute=141
		Vfin			 = new double[Nh];              //idAttribute=153
		Vmin			 = new double[Nh];              //idAttribute=142
		Vmax			 = new double[Nh];              //idAttribute=143
		KEstanque		 = new double[Nh];              //idAttribute=144
        AEstanque		 = new double[Nh];              //idAttribute=146
		Propietario	     = new String[Nh];          	//idAttribute=146
		SpinningMax		 = new double[Nh];              //idAttribute=180
		SpinningMax2	 = new double[Nh];              //idAttribute=207
		CPFMax		 	 = new double[Nh];              //idAttribute=181
        Reserva_Pronta 	 = new int[Nh];					//idAttribute=182
		Commitment		 = new int[Nh];					//idAttribute=183
		StopCost		 = new double[Nh];				//idAttribute=184
		IDAcoplaTV		 = new int[Nh];					//idAttribute=188	
		FactorAcoplaTVTG = new double[Nh];              //idAttribute=189
		IsERNC 			 = new int[Nh];                 //idAttribute=209
		Dependence		 = new int[Nh];					//idAttribute=233 
		Fdependence		 = new double[Nh];					//idAttribute=234 
		
		
		//inicializacion de parametros
		for (int t=0;t<Nh;t++){
			ID[t]				= 0;
			Nombre[t] 			= "";
			NombreCentral[t]	="";
			Tecnologia[t]		="";
			Combustible[t]		="";
			Propietario[t] 		= "";
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
			Vini[t]	            = 0;
			Vfin[t]	            = 0;
			Vmin[t]	            = 0;
			Vmax[t] 	        = 0;
			KEstanque[t]	    = 0;
			AEstanque[t]	    = 0;
			SpinningMax[t]	    = 10000;
			SpinningMax2[t]	    = 10000;
			CPFMax[t]	   		= 0;
			Reserva_Pronta[t]	= 0;
			Commitment[t]		= 0;
			StopCost[t]			= 0;
			IDAcoplaTV[t]		= 0;
			FactorAcoplaTVTG[t] = 0;
			IsERNC[t] 			= 0;
			Dependence[t]		= 0;
			Fdependence[t]		= 0;
		}
		
		//Coneccion a base de datos
		initconnection(typedb, namedb, userdb, passdb);
		
		//Cargando datos
		loadint("ID", 1, ID, Tini_data, Tfin_data, Nh, object_id);
		loadstring("Nombre", 2, Nombre, Tini_data, Tfin_data, Nh, object_id);
		loadstring("Propietario", 30, Propietario, Tini_data, Tfin_data, Nh, object_id);
		loadstring("NombreCentral", 185, NombreCentral, Tini_data, Tfin_data, Nh, object_id);
		loadstring("Tecnologia", 186, Tecnologia, Tini_data, Tfin_data, Nh, object_id);
		loadstring("Combustible", 187, Combustible, Tini_data, Tfin_data, Nh, object_id);		
		loadint("TminOn", 3, TminOn, Tini_data, Tfin_data, Nh, object_id);
		loadint("TminOff", 4, TminOff, Tini_data, Tfin_data, Nh, object_id);
		loadint("TiniR", 5, TiniR, Tini_data, Tfin_data, Nh, object_id);
		loadint("TiniN", 6, TiniN, Tini_data, Tfin_data, Nh, object_id);
		loaddouble("PiniN", 7, PiniN, Tini_data, Tfin_data, Nh, object_id);
		loadint("GradRA", 8, GradRA, Tini_data, Tfin_data, Nh, object_id);
		loadint("GradRP", 9, GradRP, Tini_data, Tfin_data, Nh, object_id);
		loaddouble("CostGradRA", 10, CostGradRA, Tini_data, Tfin_data, Nh, object_id);
		loaddouble("CostGradRP", 11, CostGradRA, Tini_data, Tfin_data, Nh, object_id);
		loaddouble("GradS", 12, GradS, Tini_data, Tfin_data, Nh, object_id);
		loaddouble("GradB", 13, GradS, Tini_data, Tfin_data, Nh, object_id);
		loaddouble("GradRE", 14, GradRE, Tini_data, Tfin_data, Nh, object_id);
		loaddouble("OwnConsuption", 15, OwnConsuption, Tini_data, Tfin_data, Nh, object_id);
		loaddouble("HotStart_upCost", 16, HotStart_upCost, Tini_data, Tfin_data, Nh, object_id);
		loaddouble("WarmStart_upCost", 17, WarmStart_upCost, Tini_data, Tfin_data, Nh, object_id);
		loaddouble("ColdStart_upCost", 18, ColdStart_upCost, Tini_data, Tfin_data, Nh, object_id);
		loadint("Thot", 19, Thot, Tini_data, Tfin_data, Nh, object_id);
		loadint("Twarm", 20, Twarm, Tini_data, Tfin_data, Nh, object_id);
		loaddouble("Pming", 21, Pming, Tini_data, Tfin_data, Nh, object_id);
		loaddouble("Pmaxg", 22, Pmaxg, Tini_data, Tfin_data, Nh, object_id);
		loaddouble("CespMeRA", 23, CespMeRA, Tini_data, Tfin_data, Nh, object_id);
		loaddouble("CespMeRP", 24, CespMeRP, Tini_data, Tfin_data, Nh, object_id);
		loaddouble("PoderCal", 25, PoderCal, Tini_data, Tfin_data, Nh, object_id);
		loaddouble("Pcomb", 26, Pcomb, Tini_data, Tfin_data, Nh, object_id);
		loadint("Barra", 27, Barra, Tini_data, Tfin_data, Nh, object_id);
		loadint("Unavalaible", 28, Unavalaible, Tini_data, Tfin_data, Nh, object_id);
		loaddouble("ForcedGenerator", 29, ForcedGenerator, Tini_data, Tfin_data, Nh, object_id);
		loaddouble("Vini", 141,Vini, Tini_data, Tfin_data, Nh, object_id);
		loaddouble("Vfin", 153,Vfin, Tini_data, Tfin_data, Nh, object_id);
		loaddouble("Vmin", 142,Vmin, Tini_data, Tfin_data, Nh, object_id);
		loaddouble("Vmax", 143,Vmax, Tini_data, Tfin_data, Nh, object_id);
		loaddouble("KEstanque", 144,KEstanque, Tini_data, Tfin_data, Nh, object_id);
	    loaddouble("AEstanque", 146,AEstanque, Tini_data, Tfin_data, Nh, object_id);
		loaddouble("SpinningMax", 180,SpinningMax, Tini_data, Tfin_data, Nh, object_id);
	    loaddouble("CPFMax", 181,CPFMax, Tini_data, Tfin_data, Nh, object_id);
		loadint("Reserva_Pronta", 182, Reserva_Pronta, Tini_data, Tfin_data, Nh, object_id);
		loadint("Commitment", 183, Commitment, Tini_data, Tfin_data, Nh, object_id);
		loaddouble("StopCost", 184,StopCost, Tini_data, Tfin_data, Nh, object_id);
		loadint("IDAcoplaTV",188,IDAcoplaTV, Tini_data, Tfin_data, Nh, object_id);
		loaddouble("FactorAcoplaTVTG", 189,FactorAcoplaTVTG, Tini_data, Tfin_data, Nh, object_id);
		loaddouble("SpinningMax2", 207,SpinningMax2, Tini_data, Tfin_data, Nh, object_id);
		loadint("IsERNC", 209, IsERNC, Tini_data, Tfin_data, Nh, object_id);
		loadint("Dependence", 233, Dependence, Tini_data, Tfin_data, Nh, object_id);
		loaddouble("Fdependence", 234, Fdependence, Tini_data, Tfin_data, Nh, object_id);
		
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
				System.out.println("WARNING: Potencia minima mayor a potencia maxima");
				System.out.println("WARNING: Periodo= "+t);
				System.out.println("WARNING: Central= "+Nombre[t]);
				//thisThread.suspend();
			}
			if(Pming[t]<0){
				System.out.println("Error: Pming menor que cero");
				System.out.println("Error: Periodo= "+t);
				System.out.println("Error: Central= "+Nombre[t]);
				thisThread.suspend();
			}
			if(Pmaxg[t]<0){
				System.out.println("Error: Pmaxg menor que cero");
				System.out.println("Error: Periodo= "+t);
				System.out.println("Error: Central= "+Nombre[t]);
				thisThread.suspend();
			}
			if(TminOn[t]<=0){
				System.out.println("Error: TminOn menor o igual cero. Usar valor mayor o igual a 1");
				System.out.println("Error: Periodo= "+t);
				System.out.println("Error: Central= "+Nombre[t]);
				thisThread.suspend();
			}
			if(TminOff[t]<=0){
				System.out.println("Error: TminOff menor o igual cero. Usar valor mayor o igual a 1");
				System.out.println("Error: Periodo= "+t);
				System.out.println("Error: Central= "+Nombre[t]);
				thisThread.suspend();
			}
			if(GradRA[t]<0){
				System.out.println("Error: Periodos de arranque menor que cero");
				System.out.println("Error: Periodo= "+t);
				System.out.println("Error: Central= "+Nombre[t]);
				thisThread.suspend();
			}
			if(GradRP[t]<0){
				System.out.println("Error: Periodos de parada menor que cero");
				System.out.println("Error: Periodo= "+t);
				System.out.println("Error: Central= "+Nombre[t]);
				thisThread.suspend();
			}
			if(CostGradRA[t]<0){
				System.out.println("Error: Costo rampa de arranque menor que cero");
				System.out.println("Error: Periodo= "+t);
				System.out.println("Error: Central= "+Nombre[t]);
				thisThread.suspend();
			}
			if(CostGradRP[t]<0){
				System.out.println("Error: Costo rampa parada menor que cero");
				System.out.println("Error: Periodo= "+t);
				System.out.println("Error: Central= "+Nombre[t]);
				thisThread.suspend();
			}
			if(OwnConsuption[t]<0){
				System.out.println("Error: Consumo propio menor que cero");
				System.out.println("Error: Periodo= "+t);
				System.out.println("Error: Central= "+Nombre[t]);
				thisThread.suspend();
			}
			if(HotStart_upCost[t]<0){
				System.out.println("Error: Costo de partida en caliente menor que cero");
				System.out.println("Error: Periodo= "+t);
				System.out.println("Error: Central= "+Nombre[t]);
				thisThread.suspend();
			}
			if(WarmStart_upCost[t]<0){
				System.out.println("Error: Costo de partida en tibio menor que cero");
				System.out.println("Error: Periodo= "+t);
				System.out.println("Error: Central= "+Nombre[t]);
				thisThread.suspend();
			}
			if(ColdStart_upCost[t]<0){
				System.out.println("Error: Costo de partida en frio menor que cero");
				System.out.println("Error: Periodo= "+t);
				System.out.println("Error: Central= "+Nombre[t]);
				thisThread.suspend();
			}
			if(Thot[t]<0){
				System.out.println("Error: Tiempo de partida en caliente menor que cero");
				System.out.println("Error: Periodo= "+t);
				System.out.println("Error: Central= "+Nombre[t]);
				thisThread.suspend();
			}
			if(Twarm[t]<0){
				System.out.println("Error: Tiempo de partida en tibio menor que cero");
				System.out.println("Error: Periodo= "+t);
				System.out.println("Error: Central= "+Nombre[t]);
				thisThread.suspend();
			}
			if(Thot[t]>Twarm[t]){
				System.out.println("Error: Tiempo de partida en caliente es menor a tiempo de partida en tibio");
				System.out.println("Error: Periodo= "+t);
				System.out.println("Error: Central= "+Nombre[t]);
				thisThread.suspend();
			}
			if(Pcomb[t]<0){
				System.out.println("Error: Precio combustible menor que cero");
				System.out.println("Error: Periodo= "+t);
				System.out.println("Error: Central= "+Nombre[t]);
				thisThread.suspend();
			}
			if(Barra[t]<0){
				System.out.println("Error: Barra menor que cero");
				System.out.println("Error: Periodo= "+t);
				System.out.println("Error: Central= "+Nombre[t]);
				thisThread.suspend();
			}
			if(Vini[t]<0){
				System.out.println("Error: Volumen inicial estanque menor que cero");
				System.out.println("Error: Periodo= "+t);
				System.out.println("Error: Central= "+Nombre[t]);
				thisThread.suspend();
			}
			if(Vfin[t]<0){
				System.out.println("Error: Volumen final estanque menor que cero");
				System.out.println("Error: Periodo= "+t);
				System.out.println("Error: Central= "+Nombre[t]);
				thisThread.suspend();
			}
			if(Vmin[t]<0){
				System.out.println("Error: Volumen minimo estanque menor que cero");
				System.out.println("Error: Periodo= "+t);
				System.out.println("Error: Central= "+Nombre[t]);
				thisThread.suspend();
			}
			if(Vmax[t]<0){
				System.out.println("Error: Volumen maximo estanque menor que cero");
				System.out.println("Error: Periodo= "+t);
				System.out.println("Error: Central= "+Nombre[t]);
				thisThread.suspend();
			}
			if(Vmax[t]<Vmin[t]){
				System.out.println("Error: Volumen maximo menor que volumen minimo");
				System.out.println("Error: Periodo= "+t);
				System.out.println("Error: Central= "+Nombre[t]);
				thisThread.suspend();
			}
			if(Vini[t]<Vmin[t] || Vini[t]>Vmax[t]){
				System.out.println("Error: Volumen inicial fuera de rango, no esta entre Vmin y Vmax");
				System.out.println("Error: Periodo= "+t);
				System.out.println("Error: Central= "+Nombre[t]);
				thisThread.suspend();
			}
			if(Vfin[t]<Vmin[t] || Vfin[t]>Vmax[t]){
				System.out.println("Error: Volumen final fuera de rango, no esta entre Vmin y Vmax");
				System.out.println("Error: Periodo= "+t);
				System.out.println("Error: Central= "+Nombre[t]);
				thisThread.suspend();
			}
			if(StopCost[t]<0){
				System.out.println("Error: Costo de parada menor a cero");
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










