import java.awt.*;
import java.io.*; 
import java.io.File; 
import java.util.Vector;
import java.io.FileOutputStream; 
import java.io.PrintStream;
import java.sql.*;


public class DataPeriod extends DBObj{
	
	//Datos tiempo
	int[]	Group;
	int[]	Duration;
	int[]	H;
	

	//Base de datos
	
	//Constructor
	public DataPeriod(){
	}
	
	//Cargar datos
	void loaddatafromdatabase(String typedb, String namedb, String userdb, String passdb, int Tini_data, int Tfin_data) throws Exception{
	
		int Nh = Tfin_data-Tini_data+1; //numero de horas
		System.out.println("");
		System.out.println("Numero de periodos= "+Nh);
		Group 		= new int[Nh];
		Duration 	= new int[Nh];
		
		//Coneccion a base de datos
		initconnection(typedb, namedb, userdb, passdb);
		
		String consult1	= "SELECT Period,Duration,Group2 FROM tblPeriod"; 
		Statement com1 	= mylink.createStatement();
		ResultSet res1 	= com1.executeQuery(consult1);
		
		//extraigo datos de duracion de periodos
		int Period=0;
		while(res1.next()){
			Period=res1.getInt("Period");
			//chequeo que periodo este dentro de rango de Tini_data y Tfin_data
			if(Period>=Tini_data & Period<=Tfin_data){
				Group[Period-Tini_data]=res1.getInt("Group2");
				Duration[Period-Tini_data]=res1.getInt("Duration");
				//System.out.println("Period= "+ Period + "  Group= "+Group[Period-Tini_data]+"  Duration= "+Duration[Period-Tini_data]);
			}
		}
		
		//calculo de H[t]
		int i=0;
		int netapas=0;
		while(i<Nh){
			if(i==0){
				netapas=netapas+1;	
			}
			else if(i>0 & Group[i]!= Group[i-1]){
				netapas=netapas+1;
			}
		i=i+1;	
		}
		
		H=new int[netapas];
		int tt=0;
		int cont=0;
		
		i=0;
		while(i<Nh){
			if(i==0){
				H[tt]=Duration[i];
			}
			else if(i>0 & Group[i]== Group[i-1]){
				H[tt]=H[tt]+Duration[i];
			}
			else if(i>0 & Group[i]!= Group[i-1]){
				tt++;
				H[tt]=Duration[i];
			}
		i=i+1;	
		}
			res1.close();
	com1.close();
	}

}