import java.awt.*;
import java.io.*; 
import java.io.File; 
import java.util.Vector;
import java.io.FileOutputStream; 
import java.io.PrintStream;
import java.sql.*;


public class DataUCEolic{
	
	//atributos Eolic
	int[]			Id;
	String[]		Nombre;
	double[]		Viento;
	double[]		PotenciaEolic;
	double[]        FEolic;
	String[]        Propietario;
	double[]		CPFmax;
	double[]		SpinningMax;
	double[]		SpinningMax2;
	int[]			IsERNC;
	int[]			Barra;
	
	//Link a base de datos
	private Connection mylink;
	
	//Constructor
	public DataUCEolic(){
	}
	
	//Cargar datos
	void loaddatafromdatabase(String typedb, String namedb, String userdb, String passdb, int[] H, int Duration[],int[] Tini_data, int[] Tfin_data, int object_id) throws Exception{
		
		int T=H.length;
		DataEolic dataeolic = new DataEolic();
        dataeolic.loaddatafromdatabase(typedb,namedb,userdb,passdb,Tini_data[0],Tfin_data[0],object_id);
		
		Id 				= new int[T];
		Nombre 			= new String[T];
		Viento 			= new double[T];
		PotenciaEolic   = new double[T];
		FEolic   		= new double[T];
		Propietario     = new String[T];
		CPFmax			= new double[T];
		SpinningMax		= new double[T];
		SpinningMax2	= new double[T];
		IsERNC			= new int[T];
		Barra			= new int[T];
		
		loadint(this.Id,dataeolic.Id, H, Duration);	
	   	loadstring(this.Nombre,dataeolic.Nombre, H, Duration);
		loaddouble(this.Viento,dataeolic.Viento, H, Duration);
		loaddouble(this.PotenciaEolic,dataeolic.PotenciaEolic, H, Duration);
		loaddouble(this.FEolic,dataeolic.FEolic, H, Duration);
		loadstring(this.Propietario,dataeolic.Propietario, H, Duration);
		loaddouble(this.CPFmax,dataeolic.CPFmax, H, Duration);
		loaddouble(this.SpinningMax,dataeolic.SpinningMax, H, Duration);
		loaddouble(this.SpinningMax2,dataeolic.SpinningMax2, H, Duration);
		loadint(this.IsERNC,dataeolic.IsERNC, H, Duration);	
	   	loadint(this.Barra,dataeolic.Barra, H, Duration);	
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









