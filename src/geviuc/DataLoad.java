import java.awt.*;
import java.io.*; 
import java.io.File; 
import java.util.Vector;
import java.io.FileOutputStream; 
import java.io.PrintStream;
import java.sql.*;


public class DataLoad extends DBObj{
	
	//atributos demanda
	String[]		Nombre;
	double[] 		Load;
	int[]			Bus;
	int[]			Id;
	double[][]      Ensmin;
	double[][]      Ensmax;
	double[][]      Alfa;
    double[][]      Beta;
	double[]        Cdmin;
    double[]        Cdmax;
	double[]        Eperdmin;
	double[]        Eperdmax;
	double[]        Cpperd;
    int[]           Td;     	
    int[]           Ns;                     	
	private int     nsmax = 10;
		
		
	//Constructor
	public DataLoad(){
	}
	
	//Cargar datos
	void loaddatafromdatabase(String typedb, String namedb, String userdb, String passdb, int Tini_data, int Tfin_data,int object_id) throws Exception{  
		
		Thread thisThread = Thread.currentThread();
		
		int Nh = Tfin_data-Tini_data+1; //numero de periodos
	
		Nombre 			 = new String[Nh];				//idAttribute=50
		Load			 = new double[Nh];				//idAttribute=51
		Bus 			 = new int[Nh];					//idAttribute=52
		Id 			     = new int[Nh];					//idAttribute=145
		Ns               = new int[Nh];
		Ensmin			 = new double[nsmax][Nh];       //idAttribute=165 
	    Ensmax			 = new double[nsmax][Nh];       //idAttribute=166
	    Alfa             = new double[nsmax][Nh];       //idAttribute=167  
        Beta             = new double[nsmax][Nh];       //idAttribute=168   	
		Cdmin            = new double[Nh];
		Cdmax            = new double[Nh];
		Eperdmin	     = new double[Nh];
		Eperdmax	     = new double[Nh];
		Cpperd		     = new double[Nh];
		Td               = new int[Nh];
		
		//inicializacion de parametros
		for (int t=0;t<Nh;t++){
			Nombre[t] 			= "";
			Load[t]				= 0;
			Bus[t]				= 0;
			Id[t]				= 0;
			Ns[t]				= 0;
			Cdmin[t]			= 1;
			Cdmax[t]			= 1;
			Eperdmin[t]         = -10000;
			Eperdmax[t]         = 0;
			Cpperd[t]			= 0;	
			Td[t]				= 0;
		}
		
		for(int is=0;is<nsmax;is++){
			for (int t=0;t<Nh;t++){
				//valores por defecto primer tramo de la curva de energia no suministrada
				if(is==0){
					Ensmin[is][t]	=0;
					Ensmax[is][t]	=10000;
					Alfa[is][t]		=450;
					Beta[is][t]		=0;
				}
				else{
					Ensmin[is][t]	=0;
					Ensmax[is][t]	=0;
					Alfa[is][t]		=0;
					Beta[is][t]		=0;
				}
			}
		}
		
		//Coneccion a base de datos
		initconnection(typedb, namedb, userdb, passdb);
		
		//Cargando datos
		loadstring("Nombre", 50, Nombre, Tini_data, Tfin_data, Nh, object_id); //idAttribute=50
		loadint("Bus", 51, Bus, Tini_data, Tfin_data, Nh, object_id); //idAttribute=51
		loaddouble("Load", 52, Load, Tini_data, Tfin_data, Nh, object_id); //idAttribute=52
		loadint("Id", 145, Id, Tini_data, Tfin_data, Nh, object_id); //idAttribute=145
		loaddouble("Cdmin", 171, Cdmin, Tini_data, Tfin_data, Nh, object_id); //idAttribute=171
		loaddouble("Cdmax", 172, Cdmax, Tini_data, Tfin_data, Nh, object_id); //idAttribute=172
		loaddouble("Eperdmin", 176, Eperdmin, Tini_data, Tfin_data, Nh, object_id); //idAttribute=176
		loaddouble("Eperdmax", 177, Eperdmax, Tini_data, Tfin_data, Nh, object_id); //idAttribute=177
		loaddouble("Cpperd", 178, Cpperd, Tini_data, Tfin_data, Nh, object_id); //idAttribute=178
		loadint("Td", 173, Td, Tini_data, Tfin_data, Nh, object_id); //idAttribute=173
		//Cargando datos con capas
		loaddouble2("Ensmin", 165, Ensmin, Tini_data, Tfin_data, Nh, object_id);  //idAttribute=165
		loaddouble2("Ensmax", 166, Ensmax, Tini_data, Tfin_data, Nh, object_id);  //idAttribute=166
		loaddouble2("Alfa", 167, Alfa, Tini_data, Tfin_data, Nh, object_id);  //idAttribute=166
		loaddouble2("Sigma", 168, Beta, Tini_data, Tfin_data, Nh, object_id);  //idAttribute=167
		
		for (int t=0;t<Nh;t++){
			for(int is=0;is<nsmax;is++){
				if(Ensmin[is][t]>0 || Ensmax[is][t]>0){
					Ns[t] = Ns[t]+ 1;
				}
			}
		}
		
		////chequeo de consistencia de datos
		for (int t=0;t<Nh;t++){
			if(Bus[t]<0){
				System.out.println("Error: Barra menor que cero");
				System.out.println("Error: Periodo= "+t);
				System.out.println("Error: Carga= "+Nombre[t]);
				thisThread.suspend();
			}
			if(Load[t]<0){
				System.out.println("Error: Demanda menor que cero");
				System.out.println("Error: Periodo= "+t);
				System.out.println("Error: Carga= "+Nombre[t]);
				thisThread.suspend();
			}
			if(Cdmin[t]<0){
				System.out.println("Error: Control de demanda minimo menor que cero");
				System.out.println("Error: Periodo= "+t);
				System.out.println("Error: Carga= "+Nombre[t]);
				thisThread.suspend();
			}
			if(Cdmax[t]<0){
				System.out.println("Error: Control de demanda maximo menor que cero");
				System.out.println("Error: Periodo= "+t);
				System.out.println("Error: Carga= "+Nombre[t]);
				thisThread.suspend();
			}
			if(Cdmin[t]>1){
				System.out.println("Error: Control de demanda minimo mayor que 1");
				System.out.println("Error: Periodo= "+t);
				System.out.println("Error: Carga= "+Nombre[t]);
				thisThread.suspend();
			}
			if(Cdmax[t]<1){
				System.out.println("Error: Control de demanda maximo menor que 1");
				System.out.println("Error: Periodo= "+t);
				System.out.println("Error: Carga= "+Nombre[t]);
				thisThread.suspend();
			}
			if(Cdmax[t]>1.3){
				System.out.println("Warning: Control de demanda maximo mayor a 30%");
				System.out.println("Warning: Periodo= "+t);
				System.out.println("Warning: Carga= "+Nombre[t]);
			}
			if(Cdmin[t]<0.7){
				System.out.println("Warning: Control de demanda minimo mayor a 30%");
				System.out.println("Warning: Periodo= "+t);
				System.out.println("Warning: Carga= "+Nombre[t]);
			}
			if(Td[t]< 0){
				System.out.println("Error: Ventana de demanda constante menor que cero");
				System.out.println("Error: Periodo= "+t);
				System.out.println("Error: Carga= "+Nombre[t]);
				thisThread.suspend();
			}
			if(t==0 & Eperdmin[0]<0){
				System.out.println("Warning: Se esta considernado perdidas de energia: "+Nombre[t]);
			}
			
			for(int is=0;is<Ns[t];is++){
				if(Ensmin[is][t]< 0){
					System.out.println("Error: Potencia minima energia no suministrada menor que cero");
					System.out.println("Error: Trama= "+is);
					System.out.println("Error: Periodo= "+t);
					System.out.println("Error: Carga= "+Nombre[t]);
					thisThread.suspend();
				}
				if(Ensmax[is][t]< 0){
					System.out.println("Error: Potencia maxima curva costo energia no suministrada menor que cero");
					System.out.println("Error: Trama= "+is);
					System.out.println("Error: Periodo= "+t);
					System.out.println("Error: Carga= "+Nombre[t]);
					thisThread.suspend();
				}
				if(Alfa[is][t]< 0){
					System.out.println("Error: Pendiente curva costo energia no suministrada menor que cero");
					System.out.println("Error: Trama= "+is);
					System.out.println("Error: Periodo= "+t);
					System.out.println("Error: Carga= "+Nombre[t]);
				}
				if(Beta[is][t]< 0){
					System.out.println("Error: Coeficiente curva costo posicion energia no suministrada menor que cero");
					System.out.println("Error: Trama= "+is);
					System.out.println("Error: Periodo= "+t);
					System.out.println("Error: Carga= "+Nombre[t]);
				}	
				if(Alfa[is][t] == 0){
					System.out.println("Warning: Pendiente curva costo energia no suministrada igual cero");
					System.out.println("Warning: Trama= "+is);
					System.out.println("Warning: Periodo= "+t);
					System.out.println("Warning: Carga= "+Nombre[t]);
				}
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
//**************************************************************************************************************************
// Funcion para cargar datos tipo double con varias capas
//**************************************************************************************************************************	
	
	public void loaddouble2(String NameAttribute, int id, double[][] attribute,int tini, int tfin, int Nh, int object_id) throws Exception{
		
		String consult1	= "Select * from tblData where idAttribute = " + id + " and " + " idObject = "+object_id; 
		Statement com1 	= mylink.createStatement();
		ResultSet res1 	= com1.executeQuery(consult1);
		int fromTime;
		int toTime;
		int capa;
		double data;
			
		while (res1.next()) {
			fromTime = res1.getInt("fromTime");
			toTime	 = res1.getInt("toTime");
			data	 = res1.getDouble("valueData");
			capa     = res1.getInt("layer");
			
			if(capa <= nsmax)
				for( int t= fromTime;t<=toTime;t++){
					//chequeo que datos esten dentro de ventana de datos de entrada
					if((t-tini)>=0 & (t-tini)<Nh){
						attribute[capa-1][t-tini]= data;
						//System.out.println("NameAtri= "+NameAttribute+" id= " + id +" atribute = " + attribute[capa-1][t-tini] + " t = " +(t-tini+1)+ " t= "+t);		
					}	
				}
			else{
				System.out.println("Datos supera maximo numero de tramos " + " fromTime= "+ fromTime + " toTime= "+ toTime + " object_id= "+ object_id);	
				return;
			}
		}	
	res1.close();
	com1.close();
	}

}










