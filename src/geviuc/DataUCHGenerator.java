public class DataUCHGenerator{
	
	//atributos maquinas termicas
	int[]			ID;
	String[]		Nombre;
	int[] 			TminOn;
	int[] 			TminOff;
	int[]			TiniR;
	int[]			TiniN;
	double[]		PiniN;
	int[] 			GradRA;
	int[] 			GradRP;
	double[]		CostGradRA;
	double[]		CostGradRP;
	double[]     	GradS;
	double[]     	GradB;
	double[]		GradRE;
	double[]		OwnConsuption;
	double[]		HotStart_upCost;
	double[]		WarmStart_upCost;
	double[] 		ColdStart_upCost;
	int[] 			Thot;
	int[]			Twarm;
	double[]		Pming;
	double[] 		Pmaxg;
	double[]     	CespMeRA;
	double[]     	CespMeRP;
	double[]     	PoderCal;
	double[]		Pcomb;   
	int[]			Barra;
	int[]			Unavalaible;
	double[]		ForcedGenerator;	
	double[]		Rend; 							
	double[]		Qvmin;							
	double[]		Qvmax;							
	int[]			Ctur;							
	int[]			Cver;
	double[]        SpinningMax;     				
	double[]        SpinningMax2;     				
	double[]        CPFMax;          				
	int[]			Reserva_Pronta;  				
    int[]			Commitment;      				
	double[]		StopCost;		 				
	int[] 			IsERNC;
    double[]		Aflu;
	String[]		Type;	
	
	//Constructor
	public DataUCHGenerator(){
	}
	//Cargar datos
	void loaddatafromdatabase(String typedb, String namedb, String userdb, String passdb, int[] H, int Duration[],int[] Tini_data, int[] Tfin_data, int object_id) throws Exception{
		
		int T=H.length;
		DataHGenerator datahgenerator = new DataHGenerator();
		datahgenerator.loaddatafromdatabase(typedb,namedb,userdb,passdb,Tini_data[0],Tfin_data[0],object_id);
		
		ID				 = new int[T];				
		Nombre 			 = new String[T];			
		TminOn 			 = new int[T];			
		TminOff 		 = new int[T];			
		TiniR			 = new int[T];					
		TiniN 			 = new int[T];					
		PiniN 			 = new double[T];				
		GradRA 			 = new int[T];					
		GradRP 			 = new int[T]; 				
		CostGradRA		 = new double[T];				
		CostGradRP		 = new double[T];				
		GradS 			 = new double[T];				
		GradB 			 = new double[T];				
		GradRE			 = new double[T];				
		OwnConsuption 	 = new double[T];				
		HotStart_upCost  = new double[T];				
		WarmStart_upCost = new double[T];				
		ColdStart_upCost = new double[T];				
		Thot			 = new int[T];					
		Twarm			 = new int[T];					
		Pming 			 = new double[T];				
		Pmaxg 			 = new double[T];				
		CespMeRA 		 = new double[T];				
		CespMeRP 		 = new double[T];				
		PoderCal		 = new double[T];				
		Pcomb		     = new double[T];				
		Barra			 = new int[T];
		Unavalaible		 = new int[T];				
		ForcedGenerator  = new double[T];
 		Rend			 = new double[T];
		Qvmin			 = new double[T];							
		Qvmax			 = new double[T];							
		Ctur			 = new int[T];							
		Cver			 = new int[T];
		SpinningMax		 = new double[T];              
		SpinningMax2	 = new double[T];             
		CPFMax		 	 = new double[T];             
        Reserva_Pronta 	 = new int[T];					
		Commitment		 = new int[T];					
		StopCost		 = new double[T];				
		IsERNC 			 = new int[T];
		Aflu			 = new double[T];
		Type			 = new String[T];	
		
		//System.out.println("ID");
		loadint(this.ID,datahgenerator.ID, H, Duration);
		//System.out.println("Nombre");
		loadstring(this.Nombre,datahgenerator.Nombre, H, Duration);
		//System.out.println("TminOn");
		loadint(this.TminOn,datahgenerator.TminOn, H, Duration);
		//System.out.println("TminOff");
		loadint(this.TminOff,datahgenerator.TminOff, H, Duration);
		//System.out.println("TiniR");
		loadint(this.TiniR,datahgenerator.TiniR, H, Duration);
		//System.out.println("TiniN");
		loadint(this.TiniN,datahgenerator.TiniN, H, Duration);
		//System.out.println("PiniN");
		loaddouble(this.PiniN,datahgenerator.PiniN, H, Duration);
		//System.out.println("GradRA");
		loadint(this.GradRA,datahgenerator.GradRA, H, Duration);
		//System.out.println("GradRP");
		loadint(this.GradRP,datahgenerator.GradRP, H, Duration);
		//System.out.println("CostGradRA");
		loaddouble(this.CostGradRA,datahgenerator.CostGradRA, H, Duration);
		//System.out.println("CostGradRP");
		loaddouble(this.CostGradRP,datahgenerator.CostGradRP, H, Duration);
		//System.out.println("CostGradS");
		loaddouble(this.GradS,datahgenerator.GradS, H, Duration);
		//System.out.println("CostGradB");
		loaddouble(this.GradB,datahgenerator.GradB, H, Duration);
		//System.out.println("CostGradRE");
		loaddouble(this.GradRE,datahgenerator.GradRE, H, Duration);
		//System.out.println("OwnConsuption");
		loaddouble(this.OwnConsuption,datahgenerator.OwnConsuption, H, Duration);
		//System.out.println("HotStart_upCost");
		loaddouble(this.HotStart_upCost,datahgenerator.HotStart_upCost, H, Duration);
		//System.out.println("WarmStart_upCost");
		loaddouble(this.WarmStart_upCost,datahgenerator.WarmStart_upCost, H, Duration);
	    //System.out.println("ColdStart_upCost"); 
		loaddouble(this.ColdStart_upCost,datahgenerator.ColdStart_upCost, H, Duration);
		//System.out.println("Thot");
		loadint(this.Thot,datahgenerator.Thot, H, Duration);
		//System.out.println("Twarm");
		loadint(this.Twarm,datahgenerator.Twarm, H, Duration);
		//System.out.println("Pmin");
		loaddouble(this.Pming,datahgenerator.Pming, H, Duration);
		//System.out.println("Pmax");
		loaddouble(this.Pmaxg,datahgenerator.Pmaxg, H, Duration);
		//System.out.println("CespMeRA");
		loaddouble(this.CespMeRA,datahgenerator.CespMeRA, H, Duration);
		//System.out.println("CespMeRP");
		loaddouble(this.CespMeRP,datahgenerator.CespMeRP, H, Duration);
		//System.out.println("PoderCal");
		loaddouble(this.PoderCal,datahgenerator.PoderCal, H, Duration);
		//System.out.println("Pcomb");
		loaddouble(this.Pcomb,datahgenerator.Pcomb, H, Duration);
		//System.out.println("Barra");
		loadint(this.Barra,datahgenerator.Barra, H, Duration);
		//System.out.println("Unavalaible");
		loadint(this.Unavalaible,datahgenerator.Unavalaible, H, Duration);
		//System.out.println("ForcedGenerator");
		loaddouble(this.ForcedGenerator,datahgenerator.ForcedGenerator, H, Duration);
		//System.out.println("Rend");
		loaddouble(this.Rend,datahgenerator.Rend, H, Duration);
		//System.out.println("Qvmin");
		loaddouble(this.Qvmin,datahgenerator.Qvmin, H, Duration);
		//System.out.println("Qvmax");
		loaddouble(this.Qvmax,datahgenerator.Qvmax, H, Duration);
		//System.out.println("Ctur");
		loadint(this.Ctur,datahgenerator.Ctur, H, Duration);
		//System.out.println("Cver");
		loadint(this.Cver,datahgenerator.Cver, H, Duration);
		//System.out.println("SpinningMax");
		loaddouble(this.SpinningMax,datahgenerator.SpinningMax,H,Duration);
		//System.out.println("SpinningMax2");
		loaddouble(this.SpinningMax2,datahgenerator.SpinningMax2,H,Duration);
		//System.out.println("CPFMax");
		loaddouble(this.CPFMax,datahgenerator.CPFMax,H,Duration);
		//System.out.println("Reserva_Pronta");
		loadint(this.Reserva_Pronta,datahgenerator.Reserva_Pronta,H,Duration);
		//System.out.println("Commitment");
		loadint(this.Commitment,datahgenerator.Commitment,H,Duration);
		//System.out.println("StopCost");
		loaddouble(this.StopCost,datahgenerator.StopCost,H,Duration);
		//System.out.println("IsERNC");
		loadint(this.IsERNC,datahgenerator.IsERNC,H,Duration);
		//System.out.println("Aflu");
		loaddouble(this.Aflu,datahgenerator.Aflu,H,Duration);
		//System.out.println("Type");
		loadstring(this.Type,datahgenerator.Type,H,Duration);
		
		//la potencia maxima, potencia minima e indisponibilidad tienen un tratamiento especial
		indispot(datahgenerator.Pmaxg,datahgenerator.Pming,datahgenerator.Unavalaible,datahgenerator.ForcedGenerator,H);
		
	}
	//carga valores tipo double
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
	//carga valores tipo int
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
	//carga valore tipo string
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
	//funcion que calcula la potencia maxima y minima considerando la indisponibilidad de la central y generacion forzada
	public void indispot(double Pmaxg[],double Pming[],int Unavalaible[], double ForcedGenerator[], int H[]){
		int t,k;
		int tini=0;
		double pmax=0;
		double pmin=0;
		k=0;
		
		//Ajuste de potencia maxima y minima por generacion forzada
		while(k<H.length){
			pmax=0;
			for(t=tini;t<tini+H[k];t++){
				if(ForcedGenerator[t]>0){
					Pmaxg[t]=ForcedGenerator[t];
					Pming[t]=ForcedGenerator[t];
				}
			}
			tini=tini+H[k];	
			k++;
		}
		//Ajuste de potencia maxima por mantenimientos
		tini=0;
		k=0;
		while(k<H.length){
			pmax=0;
			pmin=0;
			for(t=tini;t<tini+H[k];t++){
				//calculo promedio ponderado de la potencia
				pmax=pmax+Pmaxg[t]*(1-Unavalaible[t]);
				pmin=pmin+Pming[t];
			}
			this.Pmaxg[k]=pmax/H[k];
			this.Pming[k]=pmin/H[k];
			tini=tini+H[k];	
			k++;
		}

		k=0;
		while(k<H.length){
			//si potencia ajustada es nula
			if(this.Pmaxg[k]==0){
				this.Unavalaible[k]=1;
				this.Pming[k]=0;
			}
			//si potencia ajustada es menor a potencia minima, la potencia es nula
			else if(this.Pmaxg[k]>0 & this.Pmaxg[k]<this.Pming[k]){
				this.Unavalaible[k]=1;
				this.Pming[k]=0;
			}
			else{
				this.Unavalaible[k]=0;
			}
			k++;
		}
			
	
	}
	


}	