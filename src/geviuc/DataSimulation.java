import java.awt.*;
import java.io.*; 
import java.io.File; 
import java.util.Vector;
import java.io.FileOutputStream; 
import java.io.PrintStream;
import java.sql.*;

public class DataSimulation extends DBObj{
	
	//Datos simulacion
	int[] 		Tini_data;		//periodo inicial con datos
	int[] 		Tfin_data;		//periodo final con datos	
	int[] 		Tini_simu;		//periodo de inicio de simulacion
	int[] 		Tfin_simu;		//periodo final de simulacion
	double[]	Spinning;		//reserva en giro
	double[]	Spinning2;		//reserva en giro v2
    double[]    CPF;			//control primario de frecuencia
    double[]	ERNCLimit;		//limite ERNC 	
	double[] 	Tmax;			//tiempo maximo de simulacion
	double[] 	Relative_gap;	//gap relativo para simulacion
	int[] 	Export_lp;		//generar archivo en formato LP
	double[]	EENSprice;		//costo de la energia no suministrada(en esta version no se utiliza)		
	double[]	Cpperd;		    //penalizacion de la energia de perdida
	double[]	Cpvfic;		    //penalizacion volumen ficticio
	String[]	Heurbat;		//heuristica bateria
	double[]    UnitT;          //unidad de tiempo
	int[]		Enfasis;		//enfasis
	String[]	pOutFName;		// Main Output file Name, idAttribute=235   
	String[]	pOutPath;		// Folder for output files, idAttribute=236
	int[]		cpxParMod;    	//idAttribute=237	
	int[]		cpxMaxThds;    	//idAttribute=238
	double []	Sbase;			//idAttribute=239 --> Agregado por YGF
	double []	Vbase;			//idAttribute=241 --> Agregado por YGF
	double []	Zbase;			//idAttribute=242 --> Agregado por YGF			
	int []	NumeroTramos;	//idAttribute=243 --> Agregado por YGF
			
	//Base de datos
		
	//Constructor
	public DataSimulation(){
	}
	
	//Cargar datos
	void loaddatafromdatabase(String typedb, String namedb, String userdb, String passdb) throws Exception{
		
		Tini_data 		= new int[3];			//idAttribute=53
		Tfin_data		= new int[3];			//idAttribute=54
		Tini_simu		= new int[3];			//idAttribute=55
		Tfin_simu		= new int[3];			//idAttribute=56
		Tmax			= new double[3];		//idAttribute=57	
		Relative_gap	= new double[3];	    //idAttribute=58
		Export_lp		= new int[3];    		//idAttribute=59		
		
		EENSprice		= new double[3];		//idAttribute=61	
		Cpperd          = new double[3];	    //idAttribute=169
		Cpvfic          = new double[3];        //idAttribute=170 
		Heurbat         = new String[3];        //idAttribute=175 
		UnitT           = new double[3];		//idAttribute=62
		Enfasis			= new int[3];			//idAttribute=176
		pOutFName		= new String[3]; 		// Main Output file Name, idAttribute=235  
		pOutPath		= new String[3]; 		// Folder for output files, idAttribute=236
		cpxParMod		= new int[3];    	//idAttribute=237	
		cpxMaxThds		= new int[3];    	//idAttribute=238
		Sbase			= new double [3];	//idAttribute=239 --> Agregado por YGF
		Vbase			= new double [3];  	//idAttribute=241 --> Agregado por YGF
		Zbase			= new double [3];	//idAttribute=242 --> Agregado por YGF
		NumeroTramos	= new int [3];	//idAttribute=243 --> Agregado por YGF
	
		
			
		for (int i=0;i<3;i++){
			Tini_data[i]	= 0;
			Tfin_data[i]	= 0;
			Tini_simu[i]	= 0;
			Tfin_simu[i]	= 0;
			Tmax[i]			= 0;
			Relative_gap[i]	= 0;
			Export_lp[i]	= 0;
			EENSprice[i]	= 0;
			Cpperd[i]		= 0;
			Cpvfic[i]		= 0;
			Heurbat[i]      = "NO";
			UnitT[i]		= 0;
			Enfasis[i]		= 0;
			Export_lp[i]	= 0;
			cpxParMod[i]	= 0;	
			cpxMaxThds[i]	= 0;
			Sbase[i]		= 0; //--> Agregado por YGF
			Vbase[i]		= 0; //--> Agregado por YGF
			Zbase[i]		= 0; //--> Agregado por YGF
			NumeroTramos[i]	= 0; //--> Agregado por YGF				
		}
		pOutFName[0]= 	"SolutionUC.csv";
		pOutPath[0]= 	"..\\Out\\";		
		
		
		//Coneccion a base de datos
		initconnection(typedb, namedb, userdb, passdb);
		
		//Cargando datos
		loadint("Tini_data", 53,Tini_data);
		loadint("Tfin_data", 54, Tfin_data);
		loadint("Tini_simu", 55,Tini_simu);
		loadint("Tfin_simu", 56, Tfin_simu);
		loaddouble("Tmax", 57, Tmax);
		loaddouble("Relative_gap", 58, Relative_gap);
		loadint("Export_lp", 59, Export_lp);

		loadstring("OutFName", 235, pOutFName);
		loadstring("OutPath", 236, pOutPath);
		loadint("cpxParMod", 237, cpxParMod);		
		loadint("cpxMaxThds", 238, cpxMaxThds);
		
		loaddouble("EENSprice", 61,EENSprice);
		loaddouble("Cpperd", 169,Cpperd);
		loaddouble("Cpvfic", 170,Cpvfic);
		loaddouble("UnitT", 62,UnitT);
		loadstring("Heurbat", 175,Heurbat);
		loadint("Enfasis", 176,Enfasis);
		loaddouble("Sbase", 239,Sbase);
		loaddouble("Vbase", 241,Vbase);
		loaddouble("Zbase", 242,Zbase);
		loadint("NumeroTramos", 243,NumeroTramos);
		
		//Atributos de sistema variable en el tiempo
		
		//Reserva en giro
		int Nh = Tfin_data[0]-Tini_data[0]+1; 		//numero de periodos
		Spinning        = new double[Nh];			//idAttribute=176  --> Modificado en version 20120328
		Spinning2       = new double[Nh];			//idAttribute=208  --> Modificado en version 20121226
		CPF          	= new double[Nh];			//idAttribute=176  --> Modificado en version 20120328
		ERNCLimit		= new double[Nh];       	//idAttribute=213  --> Modificado en version 20130205
		
		for (int t=0;t<Nh;t++){
			Spinning[t]		= 0;
			Spinning2[t]	= 0;
			CPF[t]			= 0;
			ERNCLimit[t]	= 0;
		}
		
		
		loaddouble2("SPinningReserve", 178, Spinning, Tini_data[0], Tfin_data[0], Nh); //--> Modificado en version 20120328
		loaddouble2("CPF", 179, CPF, Tini_data[0], Tfin_data[0], Nh); //--> Modificado en version 20120328
		loaddouble2("SPinningReserve2", 208, Spinning2, Tini_data[0], Tfin_data[0], Nh); //--> Modificado en version 20121226
		loaddouble2("ERNCLimit", 213, ERNCLimit, Tini_data[0], Tfin_data[0], Nh); //--> Modificado en version 20130205
	}
	
//**************************************************************************************************************************
// Funcion para cargar datos tipo double
//**************************************************************************************************************************	
	
	public void loaddouble(String NameAttribute, int id, double[] attribute) throws Exception{
		
		String consult1	= "Select * from tblData where idAttribute = " + id; 
		Statement com1 	= mylink.createStatement();
		ResultSet res1 	= com1.executeQuery(consult1);
		int fromTime;
		int toTime;
			
			while (res1.next()) {
				fromTime = res1.getInt("fromTime");
				toTime	 = res1.getInt("toTime");
				
				attribute[0]=res1.getDouble("valueData");
				attribute[1]=fromTime;
				attribute[2]=toTime;
			}
	res1.close();
	com1.close();
		}
		
//******************************************************************************************************************************
// Funcion para cargar datos tipo int
//*******************************************************************************************************************************	
		
		public void loadint(String NameAttribute, int id, int[] attribute) throws Exception{
		
		String consult1	= "Select * from tblData where idAttribute = " + id; 
		Statement com1 	= mylink.createStatement();
		ResultSet res1 	= com1.executeQuery(consult1);
		int fromTime;
		int toTime;
			
			while (res1.next()) {
				fromTime = res1.getInt("fromTime");
				toTime	 = res1.getInt("toTime");
				
				attribute[0]=res1.getInt("valueData");
				attribute[1]=fromTime;
				attribute[2]=toTime;
				
			}
		
	res1.close();
	com1.close();
		}
//**************************************************************************************************************************************
//Funcion para cargar datos tipo String
//***************************************************************************************************************************************
		public void loadstring(String NameAttribute, int id, String[] attribute) throws Exception{
		
		String consult1	= "Select * from tblData where idAttribute = " + id; 
		Statement com1 	= mylink.createStatement();
		ResultSet res1 	= com1.executeQuery(consult1);
		String fromTime;
		String toTime;
			
			while (res1.next()) {
				fromTime = res1.getString("fromTime");
				toTime	 = res1.getString("toTime");
				
				attribute[0]=res1.getString("valueData");
				attribute[1]=fromTime;
				attribute[2]=toTime;
			}
		
	res1.close();
	com1.close();
		}
		
//**************************************************************************************************************************
// Funcion para cargar datos tipo double
//**************************************************************************************************************************	
	
	public void loaddouble2(String NameAttribute, int id, double[] attribute,int tini, int tfin, int Nh) throws Exception{
		
		String consult1	= "Select * from tblData where idAttribute = " + id; 
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
				}	
			}
		
		}	
	res1.close();
	com1.close();
	}
		

}










