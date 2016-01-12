import java.awt.*;
import java.io.*; 
import java.io.File; 
import java.util.Vector;
import java.io.FileOutputStream; 
import java.io.PrintStream;
import java.sql.*;


public class DataUCCSP{
	
	//atributos CSP
	int[]			Id;
	String[]		Nombre;
	double[]		PotenciaSolar;
	double[]		PerdidasAlmacenamiento;
	double[]        EficienciaAlmacenamiento;
	double[]        EficienciaInyeccion;
	double[]        Vmin;
	double[]        Vmax;
	String[]        Propietario;
	double[]		Vini;
	double[]		Vfin;
	double[]		Pmin;
	double[]		Pmax;
	int[]			IsERNC;
		
	//Link a base de datos
	private Connection mylink;
	
	//Constructor
	public DataUCCSP(){
	}
	
	//Cargar datos
	void loaddatafromdatabase(String typedb, String namedb, String userdb, String passdb, int[] H, int Duration[],int[] Tini_data, int[] Tfin_data, int object_id) throws Exception{
		
		int T=H.length;
		DataCSP datacsp = new DataCSP();
        datacsp.loaddatafromdatabase(typedb,namedb,userdb,passdb,Tini_data[0],Tfin_data[0],object_id);
		
		Id							= new int[T];
		Nombre						= new String[T];
		PotenciaSolar				= new double[T];
		PerdidasAlmacenamiento		= new double[T];
		EficienciaAlmacenamiento	= new double[T];
		EficienciaInyeccion			= new double[T];
		Vmin						= new double[T];
		Vmax						= new double[T];
		Propietario					= new String[T];
		Vini						= new double[T];              
		Vfin					 	= new double[T];              
		Pmin					 	= new double[T];              
		Pmax					 	= new double[T];
		IsERNC						= new int[T];	
		
		loadint(this.Id,datacsp.Id, H, Duration);	
	   	loadstring(this.Nombre,datacsp.Nombre, H, Duration);
		loaddouble(this.PotenciaSolar,datacsp.PotenciaSolar, H, Duration);
		loaddouble(this.PerdidasAlmacenamiento,datacsp.PerdidasAlmacenamiento, H, Duration);
		loaddouble(this.EficienciaAlmacenamiento,datacsp.EficienciaAlmacenamiento, H, Duration);
		loaddouble(this.EficienciaInyeccion,datacsp.EficienciaInyeccion, H, Duration);
		loaddouble(this.Vmin,datacsp.Vmin, H, Duration);
		loaddouble(this.Vmax,datacsp.Vmax, H, Duration);
		loadstring(this.Propietario,datacsp.Propietario, H, Duration);
		loaddouble(this.Vini,datacsp.Vini, H, Duration);
		loaddouble(this.Vfin,datacsp.Vfin, H, Duration);
		loaddouble(this.Pmin,datacsp.Pmin, H, Duration);
		loaddouble(this.Pmax,datacsp.Pmax, H, Duration);
		loadint(this.IsERNC,datacsp.IsERNC, H, Duration);
		
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









