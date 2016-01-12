import java.awt.*;
import java.io.*; 
import java.io.File; 
import java.util.Vector;
import java.io.FileOutputStream; 
import java.io.PrintStream;
import java.sql.*;


public class DataBattery extends DBObj{
	
	//atributos Battery
	int[]			Id;
	String[]		Nombre;
	double[]		Emin;
	double[]		Emax;
	double[]        Pmin;
	double[]        Pmax;
	double[]        Nid;
	double[]        Nic;
	double[]        Rb;
	String[]        Propietario;
	double[]        Eini;
	double[]        Efin;
	double[][]      Esocmin;
	double[][]      Esocmax;
	double[][]      Alfa;
    double[][]      Sigma;
	double[]        Pio;
	double[]        Cb; 
    int[]           Ns;                     	
	private int     nsmax = 10;
	
	//Constructor
	public DataBattery(){
	}
	
	//Cargar datos
	void loaddatafromdatabase(String typedb, String namedb, String userdb, String passdb, int Tini_data, int Tfin_data,int object_id) throws Exception{  
		
		Thread thisThread = Thread.currentThread();
		
		int Nh = Tfin_data-Tini_data+1; //numero de horas
	
		Id 			 	 = new int[Nh];					//idAttribute=126
		Nombre			 = new String[Nh];				//idAttribute=127
		Emin 	 		 = new double[Nh];				//idAttribute=128
		Emax             = new double[Nh];				//idAttribute=129
		Pmin 			 = new double[Nh];              //idAttribute=130
		Pmax 			 = new double[Nh];              //idAttribute=131
		Nid              = new double[Nh];              //idAttribute=132
		Nic              = new double[Nh];              //idAttribute=156
        Rb               = new double[Nh];              //idAttribute=161 		
		Propietario		 = new String[Nh];				//idAttribute=133
		Eini             = new double[Nh];              //idAttribute=137
		Efin             = new double[Nh];              //idAttribute=155
		Ns               = new int[Nh];
		Esocmin			 = new double[nsmax][Nh];       //idAttribute=157 
	    Esocmax			 = new double[nsmax][Nh];       //idAttribute=158
	    Alfa             = new double[nsmax][Nh];       //idAttribute=159  
        Sigma            = new double[nsmax][Nh];       //idAttribute=160   	
		Pio              = new double[Nh];              //idAttribute=161
		Cb               = new double[Nh];              //idAttribute=174
		
		//inicializacion de parametros
		for (int t=0;t<Nh;t++){
			Id[t] 				= 0;
			Nombre[t]			= "";
			Emin[t]				= 0;
			Emax[t]				= 0;
			Pmin[t]				= 0;
			Pmax[t]				= 0;
			Nid[t]              = 0;
			Nic[t]              = 0;
			Rb[t]               = 0;
			Propietario[t]		="";
			Eini[t]             = 0;
			Efin[t]             = 0;
			Ns[t]				= 0;
			Pio[t]              = 0;
			Cb[t]               = 0;
		}
		
		for(int is=0;is<nsmax;is++){
			for (int t=0;t<Nh;t++){
				Esocmin[is][t]	=0;
				Esocmax[is][t]	=0;
				Alfa[is][t]		=0;
				Sigma[is][t]	=0;
			}
		}
		
		//Coneccion a base de datos
		initconnection(typedb, namedb, userdb, passdb);
		
		//Cargando datos
		loadint("Id", 126, Id, Tini_data, Tfin_data, Nh, object_id); //idAttribute=126
		loadstring("Nombre", 127, Nombre, Tini_data, Tfin_data, Nh, object_id); //idAttribute=127
		loaddouble("Emin", 128,Emin, Tini_data, Tfin_data, Nh, object_id); //idAttribute=128
		loaddouble("Emax", 129, Emax, Tini_data, Tfin_data, Nh, object_id); //idAttribute=129
		loaddouble("Pmin", 130, Pmin, Tini_data, Tfin_data, Nh, object_id); //idAttribute=130
		loaddouble("Pmax", 131, Pmax, Tini_data, Tfin_data, Nh, object_id); //idAttribute=131
		loaddouble("Nid", 132, Nid, Tini_data, Tfin_data, Nh, object_id); //idAttribute=132
		loaddouble("Nic", 156, Nic, Tini_data, Tfin_data, Nh, object_id); //idAttribute=156
		loaddouble("Rb", 161, Rb, Tini_data, Tfin_data, Nh, object_id); //idAttribute=161
		loadstring("Propietario", 133,Propietario, Tini_data, Tfin_data, Nh, object_id); //idAttribute=133
		loaddouble("Eini", 137,Eini, Tini_data, Tfin_data, Nh, object_id); //idAttribute=137
		loaddouble("Efin", 155,Efin, Tini_data, Tfin_data, Nh, object_id); //idAttribute=155
		loaddouble("Pio", 162,Pio, Tini_data, Tfin_data, Nh, object_id); //idAttribute=161
		loaddouble("Cb", 174,Cb, Tini_data, Tfin_data, Nh, object_id); //idAttribute=174
		
		//Cargando datos con capas
		loaddouble2("Esocmin", 157, Esocmin, Tini_data, Tfin_data, Nh, object_id);  //idAttribute=31
		loaddouble2("Esocmax", 158, Esocmax, Tini_data, Tfin_data, Nh, object_id);  //idAttribute=32
		loaddouble2("Alfa", 159, Alfa, Tini_data, Tfin_data, Nh, object_id);  //idAttribute=33
		loaddouble2("Sigma", 160, Sigma, Tini_data, Tfin_data, Nh, object_id);  //idAttribute=34
		
		//Determinacion de maxima cantidad de tramos por periodo
		for (int t=0;t<Nh;t++){
			for(int is=0;is<nsmax;is++){
				if(Esocmin[is][t]>0 || Esocmax[is][t]>0){
					Ns[t] = Ns[t]+ 1;
				}
			}
		}
		
		////chequeo de consistencia de datos
		for (int t=0;t<Nh;t++){
			if(Emin[t]<0){
				System.out.println("Error: Energia minima menor que cero");
				System.out.println("Error: Periodo= "+t);
				System.out.println("Error: Bateria= "+Nombre[t]);
				thisThread.suspend();
			}
			if(Emax[t]<0){
				System.out.println("Error: Energia maxima menor que cero");
				System.out.println("Error: Periodo= "+t);
				System.out.println("Error: Bateria= "+Nombre[t]);
				thisThread.suspend();
			}
			if(Emin[t] > Emax[t]){
				System.out.println("Error: Energia minima mayor que energia maxima");
				System.out.println("Error: Periodo= "+t);
				System.out.println("Error: Bateria= "+Nombre[t]);
				thisThread.suspend();
			}
			if(Pmax[t] <0){
				System.out.println("Error: Potencia maxima menor que cero");
				System.out.println("Error: Periodo= "+t);
				System.out.println("Error: Bateria= "+Nombre[t]);
				thisThread.suspend();
			}
			if(Pmax[t] == 0){
				System.out.println("Warning: Potencia maxima igual a cero");
				System.out.println("Warning: Periodo= "+t);
				System.out.println("Warning: Bateria= "+Nombre[t]);
				thisThread.suspend();
			}
			if(Nid[t] < 0 || Nic[t] < 0){
				System.out.println("Error: Coeficiente de perdida menor que cero");
				System.out.println("Error: Periodo= "+t);
				System.out.println("Error: Bateria= "+Nombre[t]);
				thisThread.suspend();
			}
			if(Eini[t] <0){
				System.out.println("Error: Energia inicial menor que cero");
				System.out.println("Error: Periodo= "+t);
				System.out.println("Error: Bateria= "+Nombre[t]);
				thisThread.suspend();
			}
			if(Efin[t] <0){
				System.out.println("Error: Energia final menor que cero");
				System.out.println("Error: Periodo= "+t);
				System.out.println("Error: Bateria= "+Nombre[t]);
				thisThread.suspend();
			}
			if(Eini[t] < Emin[t] || Eini[t] > Emax[t]){
				System.out.println("Error: Energia inicial fuera de rango, no esta entre Emin y Emax");
				System.out.println("Error: Periodo= "+t);
				System.out.println("Error: Bateria= "+Nombre[t]);
				thisThread.suspend();
			}
			if(Efin[t] < Emin[t] || Efin[t] > Emax[t]){
				System.out.println("Error: Energia final fuera de rango, no esta entre Emin y Emax");
				System.out.println("Error: Periodo= "+t);
				System.out.println("Error: Bateria= "+Nombre[t]);
				thisThread.suspend();
			}
			if(Pio[t] <0){
				System.out.println("Error: Consumo propio de inversor menor que cero");
				System.out.println("Error: Periodo= "+t);
				System.out.println("Error: Bateria= "+Nombre[t]);
				thisThread.suspend();
			}
			
			for(int is=0;is<nsmax;is++){
				if(Esocmin[is][t] <0){
					System.out.println("Error: Esocmin menor que cero");
					System.out.println("Error: Tramo= " + is);	
					System.out.println("Error: Periodo= "+t);
					System.out.println("Error: Bateria= "+Nombre[t]);
					thisThread.suspend();
				}
				if(Esocmax[is][t] <0){
					System.out.println("Error: Esocmax menor que cero");
					System.out.println("Error: Tramo= " + is);	
					System.out.println("Error: Periodo= "+t);
					System.out.println("Error: Bateria= "+Nombre[t]);
					thisThread.suspend();
				}
				if(Esocmax[is][t] > 1){
					System.out.println("Error: Esocmax mayor que 1");
					System.out.println("Error: Tramo= " + is);	
					System.out.println("Error: Periodo= "+t);
					System.out.println("Error: Bateria= "+Nombre[t]);
					thisThread.suspend();
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










