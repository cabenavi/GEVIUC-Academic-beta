import java.awt.*;
import java.io.*; 
import java.io.File; 
import java.util.Vector;
import java.io.FileOutputStream; 
import java.io.PrintStream;
import java.sql.*;


public class DataUCReserv{
	
	//atributos Embalse
	int[]			Id;
	String[]		Nombre;
	double[]		Vmin;
	double[]		Vmax;
	int[] 	        Ctur;
	int[] 	        Cver;
	int[] 	        Cfil;
	double[]		Aflu;
	double[]		Ret;
	double[]		Qvmin;
	double[]		Qvmax;
	String[]		Propietario;
	double[]		Vini;	
	double[]		Vfin;
	double[] 		Vermin; //Vertimiento minimo
    double[]		Vermax; //Vertimiento maximo
	int[]			ID_Central;		
	double[]		ReserveMinVolume; //Nivel de embalse minimo para aportar reserva	
			
	//Link a base de datos
	private Connection mylink;
	
	//Constructor
	public DataUCReserv(){
	}
	
	//Cargar datos
	void loaddatafromdatabase(String typedb, String namedb, String userdb, String passdb, int[] H, int Duration[],int[] Tini_data, int[] Tfin_data, int object_id) throws Exception{
		
		int T=H.length;
		DataReserv datareserv = new DataReserv();
        datareserv.loaddatafromdatabase(typedb,namedb,userdb,passdb,Tini_data[0],Tfin_data[0],object_id);
		
		Id 				= new int[T];
		Nombre 			= new String[T];
		Vmin 			= new double[T];
		Vmax 			= new double[T];
		Ctur            = new int[T];
		Cver            = new int[T];
		Cfil            = new int[T];
		Aflu            = new double[T];
		Ret             = new double[T];
		Qvmin           = new double[T];
		Qvmax           = new double[T];		
		Propietario     = new String[T];
		Vini            = new double[T];
		Vfin            = new double[T];
		Vermin 			= new double[T];
		Vermax 			= new double[T];
		ID_Central		= new int[T];
		ReserveMinVolume =new double[T];
			
		loadint(this.Id,datareserv.Id, H, Duration);	
	   	loadstring(this.Nombre,datareserv.Nombre, H, Duration);
		loaddouble(this.Vmin,datareserv.Vmin, H, Duration);
		loaddouble(this.Vmax,datareserv.Vmax, H, Duration);
		loadint(this.Ctur,datareserv.Ctur, H, Duration);
		loadint(this.Cver,datareserv.Cver, H, Duration);
		loadint(this.Cfil,datareserv.Cfil, H, Duration);
		loaddouble(this.Aflu,datareserv.Aflu, H, Duration);
		loaddouble(this.Ret,datareserv.Ret, H, Duration);
		loaddouble(this.Qvmin,datareserv.Qvmin, H, Duration);
		loaddouble(this.Qvmax,datareserv.Qvmax, H, Duration);
		loadstring(this.Propietario,datareserv.Propietario, H, Duration);
		loaddouble(this.Vini,datareserv.Vini, H, Duration);
		loaddouble(this.Vfin,datareserv.Vfin, H, Duration);
		loaddouble(this.Vermin,datareserv.Vermin, H, Duration);
		loaddouble(this.Vermax,datareserv.Vermax, H, Duration);
		loadint(this.ID_Central,datareserv.ID_Central, H, Duration);
		loaddouble(this.ReserveMinVolume,datareserv.ReserveMinVolume, H, Duration);
	}
	public void loaddouble(double[] attribute1,double[] attribute2, int[] H, int[] Duration) throws Exception{
		
		int ta=0;
		double aux=0;
		int aux2=0;
		for(int t=0;t<H.length;t++){
			for(int tt=ta;tt<ta+H[t];tt++){
				aux=aux+attribute2[tt]*Duration[tt]; //promedio ponderado
				aux2=aux2+Duration[tt];
			}	
		attribute1[t]=aux/aux2;
		//System.out.println("atribute = " + attribute1[t] + " t = " +(t+1));	
		ta=ta+H[t];
		aux=0;
		aux2=0;
		}
	}
	public void loadint(int[] attribute1,int[] attribute2, int[] H, int[] Duration) throws Exception{
		
		int ta=0;
		int aux=0;
		int aux2=0;
		for(int t=0;t<H.length;t++){
			for(int tt=ta;tt<ta+H[t];tt++){
				aux=aux+attribute2[tt]*Duration[tt]; //promedio ponderado
				aux2=aux2+Duration[tt];
			}	
		attribute1[t]=aux/aux2;	
		//System.out.println("atribute = " + attribute1[t] + " t = " +(t+1));	
		ta=ta+H[t];
		aux=0;
		aux2=0;
		}
	}
	public void loadstring(String[] attribute1,String[] attribute2, int[] H, int[] Duration) throws Exception{
		
		int ta=0;
		String aux="";
		for(int t=0;t<H.length;t++){
			for(int tt=ta;tt<ta+H[t];tt++){
				aux=attribute2[tt];
			}	
		attribute1[t]=aux;	
		//System.out.println("atribute = " + attribute1[t] + " t = " +(t+1));	
		ta=ta+H[t];
		}
	}
}	










