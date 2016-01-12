public class DataUCLoad{
	
	//atributos demanda
	String[]		Nombre;
	double[] 		Load;
	int[]			Bus;
	int[]			Id;
	int[]			Ns;
	double[][]		Ensmin;
	double[][]		Ensmax;
	double[][]		Alfa;
	double[][]		Beta;
	double[]        Cdmin;
    double[]        Cdmax;
    double[]        Eperdmin;
    double[]        Eperdmax;
	double[]        Cpperd;
	int[]           Td;  
	int n_iv;
	private int nsmax = 10;		
	
	//Constructor
	public DataUCLoad(){
	}
	//Cargar datos
	void loaddatafromdatabase(String typedb, String namedb, String userdb, String passdb, int[] H, int Duration[],int[] Tini_data, int[] Tfin_data, int object_id) throws Exception{
		
		int T=H.length;
		DataLoad dataload = new DataLoad();
        dataload.loaddatafromdatabase(typedb,namedb,userdb,passdb,Tini_data[0],Tfin_data[0],object_id);
		
		Nombre			 = new String[T];
		Load		 	 = new double[T];
		Bus 			 = new int[T];
		Id 			     = new int[T];	
		Ns          	 = new int[T];
		Ensmin			 = new double[nsmax][T];
		Ensmax	 		 = new double[nsmax][T];
		Alfa 			 = new double[nsmax][T];
		Beta 			 = new double[nsmax][T];
		Cdmin			 = new double[T];
		Cdmax			 = new double[T];
		Eperdmin		 = new double[T];
		Eperdmax		 = new double[T];
		Cpperd			 = new double[T];
		Cdmax			 = new double[T];
		Td				 = new int[T];
		
		loadint(this.Ns,dataload.Ns, H, Duration);	
	   	loadstring(this.Nombre,dataload.Nombre, H, Duration);
		loaddouble(this.Load,dataload.Load, H, Duration);
		loadint(this.Bus,dataload.Bus, H, Duration);
		loadint(this.Id,dataload.Id, H, Duration);
		loadstring(this.Nombre,dataload.Nombre, H, Duration);
		loaddouble(this.Cdmin,dataload.Cdmin, H, Duration);
		loaddouble(this.Cdmax,dataload.Cdmax, H, Duration);
		loaddouble(this.Eperdmin,dataload.Eperdmin, H, Duration);
		loaddouble(this.Eperdmax,dataload.Eperdmax, H, Duration);
		loaddouble(this.Cpperd,dataload.Cpperd, H, Duration);
		loadint(this.Td,dataload.Td, H, Duration);
		
		for(int is=0;is<nsmax;is++){
			loaddouble(this.Ensmin[is],dataload.Ensmin[is], H, Duration);
			loaddouble(this.Ensmax[is],dataload.Ensmax[is], H, Duration);
			loaddouble(this.Alfa[is],dataload.Alfa[is], H, Duration);
			loaddouble(this.Beta[is],dataload.Beta[is], H, Duration);
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