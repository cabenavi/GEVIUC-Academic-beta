import java.awt.*;
import java.io.*; 
import java.io.File; 
import java.util.Vector;
import java.io.FileOutputStream; 
import java.io.PrintStream;
import java.sql.*;


public class DataCost2 extends DBObj{
	
	//datos costos
	double[][]		Pmin;
	double[][]		Pmax;
	double[][]		Alfa;
	double[][] 		Beta;
	int[]			Ns;
	private int 	nsmax = 10;		
	
	
	//Constructor
	public DataCost2(){
	}
	
	//Cargar datos
	void loaddatafromdatabase(String typedb, String namedb, String userdb, String passdb, int Tini_data, int Tfin_data, int object_id) throws Exception{  
		
		int Nh = Tfin_data - Tini_data + 1; //numero de horas
					
		Pmin			 = new double[nsmax][Nh];
		Pmax		 	 = new double[nsmax][Nh];
		Alfa 			 = new double[nsmax][Nh];
		Beta 			 = new double[nsmax][Nh];
		Ns				 = new int[Nh];
		
		//inicializacion de parametros
		for(int is=0;is<nsmax;is++){
			for (int t=0;t<Nh;t++){
			Pmin[is][t]		= 0;
			Pmax[is][t]		= 0;
			Alfa[is][t]		= 0;
			Beta[is][t]		= 0;
			}
		}
		for (int t=0;t<Nh;t++){
			Ns[t] = 0;
		}
		
		//Coneccion a base de datos
		initconnection(typedb, namedb, userdb, passdb);
		
		//Cargando datos
		loaddouble("Pmin", 31, Pmin, Tini_data, Tfin_data, Nh, object_id);  //idAttribute=31
		loaddouble("Pmax", 32, Pmax, Tini_data, Tfin_data, Nh, object_id);  //idAttribute=32
		loaddouble("Alfa", 33, Alfa, Tini_data, Tfin_data, Nh, object_id);  //idAttribute=33
		loaddouble("Beta", 34, Beta, Tini_data, Tfin_data, Nh, object_id);  //idAttribute=34
		
		//Determinacion de maxima cantidad de tramos por periodo
		for (int t=0;t<Nh;t++){
			for(int is=0;is<nsmax;is++){
				if(is==0 & Pmin[is][t]==0 & Pmax[is][t]==0){ //modificacion realizada el 05/06/2011
					Ns[t] = Ns[t]+ 1;
				}
				else if(Pmin[is][t]>0 || Pmax[is][t]>0){
					Ns[t] = Ns[t]+ 1;
				}
			}
		}

	}
	
//**************************************************************************************************************************
// Funcion para cargar datos tipo double
//**************************************************************************************************************************	
	
	public void loaddouble(String NameAttribute, int id, double[][] attribute,int tini, int tfin, int Nh, int object_id) throws Exception{
		
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

