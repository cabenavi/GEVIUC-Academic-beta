public class DataUCLine{
	
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
	public DataUCLine(){
	}
	//Cargar datos
	void loaddatafromdatabase(String typedb, String namedb, String userdb, String passdb, int[] H, int Duration[],int[] Tini_data, int[] Tfin_data, int object_id) throws Exception{
		
		int T=H.length;
		DataLine dataline= new DataLine();
        dataline.loaddatafromdatabase(typedb,namedb,userdb,passdb,Tini_data[0],Tfin_data[0],object_id);
		
		ID					= new int[T];
		Nombre				= new String[T];
		BusIni				= new int[T];
		BusFin				= new int[T];
		Resistencia			= new double[T];
		Reactancia			= new double[T];
		Largo				= new double[T];
		Voltaje				= new double[T];
		Fmax				= new double[T];
		Fmin				= new double[T];
		Propietario			= new String[T];
		tramos_maximo		= new int [T];
		Opera				= new String[T];
		matriz_alpha		= new double[dataline.matriz_alpha.length][T];
		
		loadint(this.ID,dataline.ID, H, Duration);	
	   	loadstring(this.Nombre,dataline.Nombre, H, Duration);
		loadint(this.BusIni,dataline.BusIni, H, Duration);
		loadint(this.BusFin,dataline.BusFin, H, Duration);
		loaddouble(this.Resistencia,dataline.Resistencia, H, Duration);
		loaddouble(this.Reactancia,dataline.Reactancia, H, Duration);
		loaddouble(this.Largo,dataline.Largo, H, Duration);
		loaddouble(this.Voltaje,dataline.Voltaje, H, Duration);
		loaddouble(this.Fmax,dataline.Fmax, H, Duration);
		loaddouble(this.Fmin,dataline.Fmin, H, Duration);
		loadstring(this.Propietario,dataline.Propietario, H, Duration);
		loadint(this.tramos_maximo,dataline.tramos_maximo, H, Duration);
		loadstring(this.Opera,dataline.Opera, H, Duration);
		
		int t,s;
		t=0;
		s=0;
		for(s=0;s<dataline.matriz_alpha.length;s++){
			for (t=0;t<T;t++){
				this.matriz_alpha[s][t]=dataline.matriz_alpha[s][t];	
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