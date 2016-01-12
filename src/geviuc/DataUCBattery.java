import java.awt.*;
import java.io.*; 
import java.io.File; 
import java.util.Vector;
import java.io.FileOutputStream; 
import java.io.PrintStream;
import java.sql.*;


public class DataUCBattery{
	
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
	int[]			Ns;
	double[][]      Esocmin;
	double[][]      Esocmax;
	double[][]      Alfa;
    double[][]      Sigma;
	double[]        Pio;
	double[]        Cb;
	int n_iv;
	private int nsmax = 10;		
				
	//Link a base de datos
	private Connection mylink;
	
	//Constructor
	public DataUCBattery(){
	}
	
	//Cargar datos
	void loaddatafromdatabase(String typedb, String namedb, String userdb, String passdb, int[] H, int Duration[],int[] Tini_data, int[] Tfin_data, int object_id) throws Exception{
		
		int T=H.length;
		DataBattery databattery = new DataBattery();
        databattery.loaddatafromdatabase(typedb,namedb,userdb,passdb,Tini_data[0],Tfin_data[0],object_id);
		
		Id 			= new int[T];
		Nombre 		= new String[T];
		Emin 		= new double[T];
		Emax 		= new double[T];
		Pmin 		= new double[T];
		Pmax 		= new double[T];
		Nid         = new double[T];              
		Nic         = new double[T];              
        Rb          = new double[T];              
		Propietario = new String[T];
		Eini 		= new double[T];
		Efin 		= new double[T];
		Ns          = new int[T];
		Pio         = new double[T];
		Cb          = new double[T];
		
		Esocmin			= new double[nsmax][T];
		Esocmax	 	 	= new double[nsmax][T];
		Alfa 			= new double[nsmax][T];
		Sigma 			= new double[nsmax][T];
		
		loadint(this.Ns,databattery.Ns, H, Duration);
		loadint(this.Id,databattery.Id, H, Duration);	
	   	loadstring(this.Nombre,databattery.Nombre, H, Duration);
		loaddouble(this.Emin,databattery.Emin, H, Duration);
		loaddouble(this.Emax,databattery.Emax, H, Duration);
		loaddouble(this.Pmin,databattery.Pmin, H, Duration);
		loaddouble(this.Pmax,databattery.Pmax, H, Duration);
		loaddouble(this.Nid,databattery.Nid, H, Duration);
		loaddouble(this.Nic,databattery.Nic, H, Duration);
		loaddouble(this.Rb,databattery.Rb, H, Duration);
		loadstring(this.Propietario,databattery.Propietario, H, Duration);
		loaddouble(this.Eini,databattery.Eini, H, Duration);
		loaddouble(this.Efin,databattery.Efin, H, Duration);
		loaddouble(this.Pio,databattery.Pio, H, Duration);
		loaddouble(this.Cb,databattery.Cb, H, Duration);
		
	   	
	  	for(int is=0;is<nsmax;is++){
		  	//System.out.println("Cost function Pmin");
			loaddouble(this.Esocmin[is],databattery.Esocmin[is], H, Duration);
			loaddouble(this.Esocmax[is],databattery.Esocmax[is], H, Duration);
			//System.out.println("Cost function Alfa");
			loaddouble(this.Alfa[is],databattery.Alfa[is], H, Duration);
		    //System.out.println("Cost function Beta");
			loaddouble(this.Sigma[is],databattery.Sigma[is], H, Duration);
	   	}
		
		n_iv=0;
	   	for (int t=0;t<Ns.length;t++){
			if(this.Ns[t]>n_iv){
				n_iv = this.Ns[t];
			}
		}
		
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









