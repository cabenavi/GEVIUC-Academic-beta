import java.awt.*;
import java.io.*; 
import java.io.File; 
import java.util.Vector;
import java.io.FileOutputStream; 
import java.io.PrintStream;
import java.sql.*;


public class DataLine extends DBObj{
	
	//atributos linea
	int[]			ID;
	String[]		Nombre;
	int[]			BusIni;
	int[]			BusFin;
	double[]		Resistencia;
	double[]		Reactancia;
	double[]		Largo;
	double[]		Voltaje;
	double[]		Fmax;
	double[]		Fmin;
	String[]		Propietario;
	double[][] 		matriz_alpha;	//matriz que con pendientes de linealizacion de perdidas cuadraticas
	int [] 			tramos_maximo;  //tramos maximos para linealizar perdidas 
	String[]		Opera;			//si la linea opera o no opera
	double			Sbase; 
	
	//Constructor
	public DataLine(){
	}
	
	//Cargar datos
	void loaddatafromdatabase(String typedb, String namedb, String userdb, String passdb, int Tini_data, int Tfin_data,int object_id) throws Exception{  
		
		Thread thisThread = Thread.currentThread();
		
		int t=0;
		int s=0;
		int Nh = Tfin_data-Tini_data+1; //numero de periodos
		

		Sbase=100; 
		
		ID					= new int[Nh];
		Nombre				= new String[Nh];
		BusIni				= new int[Nh];
		BusFin				= new int[Nh];
		Resistencia			= new double[Nh];
		Reactancia			= new double[Nh];
		Largo				= new double[Nh];
		Voltaje				= new double[Nh];
		Fmax				= new double[Nh];
		Fmin				= new double[Nh];
		Propietario			= new String[Nh];
		tramos_maximo		= new int [Nh];
		Opera				= new String[Nh];
	
		
		//inicializacion de parametros
		for (t=0;t<Nh;t++){
			ID[t]					= 0;
			Nombre[t]				= "";
			BusIni[t]				= 0;
			BusFin[t]				= 0;
			Resistencia[t]			= 0;
			Reactancia [t]			= 0;
			Largo[t]				= 0;
			Voltaje[t]				= 0;
			Fmax[t]					= 0;
			Fmin[t]					= 0;
			Propietario[t]			= "";
			tramos_maximo[t]		=0;
			Opera[t]				="";
		}

		
		//Coneccion a base de datos
		initconnection(typedb, namedb, userdb, passdb);
		
		//Cargando datos
		loadint("ID", 39, ID, Tini_data, Tfin_data, Nh, object_id); 						//idAttribute=39
		loadstring("Nombre", 40, Nombre, Tini_data, Tfin_data, Nh, object_id); 				//idAttribute=40
		loadint("BusIni", 41, BusIni, Tini_data, Tfin_data, Nh, object_id); 				//idAttribute=41
		loadint("BusFin", 42, BusFin, Tini_data, Tfin_data, Nh, object_id); 				//idAttribute=42
		loaddouble("Resistencia", 43, Resistencia, Tini_data, Tfin_data, Nh, object_id); 	//idAttribute=43
		loaddouble("Reactancia", 44, Reactancia, Tini_data, Tfin_data, Nh, object_id); 		//idAttribute=44
		loaddouble("Largo", 45, Largo, Tini_data, Tfin_data, Nh, object_id); 				//idAttribute=45
		loaddouble("Voltaje", 46,Voltaje, Tini_data, Tfin_data, Nh, object_id); 			//idAttribute=46
		loaddouble("Fmin", 48,Fmin, Tini_data, Tfin_data, Nh, object_id); 					//idAttribute=48
		loaddouble("Fmax", 47,Fmax, Tini_data, Tfin_data, Nh, object_id); 					//idAttribute=47
		loadstring("Propietario", 49, Propietario, Tini_data, Tfin_data, Nh, object_id); 	//idAttribute=49		
		loadint("tramos_maximo", 240, tramos_maximo, Tini_data, Tfin_data, Nh, object_id);	//idAttribute=240
		loadstring("Opera", 244, Opera, Tini_data, Tfin_data, Nh, object_id);				//idAttribute=244
		
		
		matriz_alpha		= new double[tramos_maximo[0]][Nh];
		//inicializado coeficientes de las perdidas
		for (t=0;t<Nh;t++){
			for(s=0;s<tramos_maximo[0];s++){
				matriz_alpha[s][t]=0;
			}
		}
		
		//calculo coeficientes para linearlizar perdidas de la linea de transmision
		
		for (t=0;t<Nh;t++){

			Resistencia[t]	= Resistencia[t]*Largo[t];

		}
		
		for (t=0;t<Nh;t++){

			Reactancia [t]	= Reactancia [t]*Largo[t];

		}
		
		tramos_perdidas(Nh, tramos_maximo[0], matriz_alpha, Sbase, Fmax,Voltaje,Resistencia);
		
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
			
			//System.out.println("fromTime= "+fromTime);
			//System.out.println("toTime= "+toTime);
			//System.out.println("data= "+data);
			
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
//*************************************************************************************************************************************
	
	//calculo de coeficientes para linear perdidas de linea de transmision
	public void tramos_perdidas(int Nh, int tramos_maximo, double matriz_alpha[][], double Sbase, double Fmax[],double Voltaje[], double Resistencia[]) throws Exception{
	
		//calculo de coeficientes de linealizacion de tramos de lineas
		int t, s;
		s=0;
		t=0;
		while(s < tramos_maximo){
			if(s==0){
				for(t=0;t<Nh;t++){
//				if (Fmax[t]==0){
//					matriz_alpha[s][t]=999999;
//				}
//				else{
				
				
					matriz_alpha[s][t]=(0.5*Resistencia[t]*Sbase*Math.pow(Voltaje[t],-2)*Math.pow(((Fmax[t]/Sbase)/tramos_maximo),2))/(((Fmax[t]/Sbase)/tramos_maximo));
//				}
				}
			}
			else{
				for(t=0;t<Nh;t++){
//				if (Fmax[t]==0){
//					matriz_alpha[s][t]=999999;
//				}
//				else{
//				
				
					matriz_alpha[s][t]=(0.5*Resistencia[t]*Sbase*Math.pow(Voltaje[t],-2)*Math.pow((s+1)*((Fmax[t]/Sbase)/tramos_maximo),2)-0.5*Resistencia[t]*Sbase*Math.pow(Voltaje[t],-2)*Math.pow(s*((Fmax[t]/Sbase)/tramos_maximo),2))/(((Fmax[t]/Sbase)/tramos_maximo));
//				}
				}
			}
			s++;
		}
	}		

	
}










