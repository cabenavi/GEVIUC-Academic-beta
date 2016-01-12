public class DataUCCost2{
	
	//atributos maquinas termicas
	//datos costos
	double[][]		Pmin;
	double[][]		Pmax;
	double[][]		Alfa;
	double[][] 		Beta;
	int[]			Ns;
	int n_iv;
	int nsmax = 10;		
	
	
	//Constructor
	public DataUCCost2(){
	}
	//Cargar datos
	void loaddatafromdatabase(String typedb, String namedb, String userdb, String passdb, int[] H, int Duration[],int[] Tini_data, int[] Tfin_data, int object_id) throws Exception{
		
		int T=H.length;
		DataCost2 datacost = new DataCost2();
        datacost.loaddatafromdatabase(typedb,namedb,userdb,passdb,Tini_data[0],Tfin_data[0],object_id);
		
		Pmin			 = new double[nsmax][T];
		Pmax		 	 = new double[nsmax][T];
		Alfa 			 = new double[nsmax][T];
		Beta 			 = new double[nsmax][T];
		Ns				 = new int[T];
				
		//System.out.println("Cost function Ns");
		loadint(this.Ns,datacost.Ns, H, Duration);
	   	
	  	for(int is=0;is<nsmax;is++){
		  	//System.out.println("Cost function Pmin");
			loaddouble(this.Pmin[is],datacost.Pmin[is], H, Duration);
		   	//System.out.println("Cost function Pmax");
			loaddouble(this.Pmax[is],datacost.Pmax[is], H, Duration);
		   	//System.out.println("Cost function Alfa");
			loaddouble(this.Alfa[is],datacost.Alfa[is], H, Duration);
		    //System.out.println("Cost function Beta");
			loaddouble(this.Beta[is],datacost.Beta[is], H, Duration);
	   	}
	   	
	   	n_iv=0;
	   	for (int t=0;t<Ns.length;t++){
			if(this.Ns[t]>n_iv){
				n_iv = this.Ns[t];
			}
		}
	   	//System.out.println("n_iv= "+n_iv);
	   	
	   	if(n_iv==0){
	   		System.out.println("Warning: central "+ object_id + " no tiene datos de funcion de costos");
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