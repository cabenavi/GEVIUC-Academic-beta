public class DataUCTGenerator{
	
	//atributos maquinas termicas
	int[]			ID;
	String[]		Nombre;
	String[]		Propietario;
	String[]        NombreCentral;						//Nombre central
	String[]		Tecnologia;
	String[]		Combustible;	
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
	double[]        Vini;
	double[]        Vfin;
	double[]        Vmin;
	double[]        Vmax;
	double[]        KEstanque;
	double[]        AEstanque;
	double[]        SpinningMax;
	double[]        SpinningMax2;
	double[]        CPFMax;
	int[]			Reserva_Pronta;
	int[]			Commitment;
	double[]		StopCost;
	int[]			IDAcoplaTV;		 //id de la turbina a gas (TG) a la que esta acoplada turbina vapor
	double[]		FactorAcoplaTVTG;// factor de acoplamiento entre la potencia de la TV con la potencia de la TG, PTV= FactorAcoplaTVTG x FTG 
	int[]			IsERNC;
	int[] 			Dependence;		 // 1 si depende de otra central para operar, 0 en caso contrario
	double[]		Fdependence;	 // factor de acoplamiento de potencia

	//Constructor
	public DataUCTGenerator(){
	}
	//Cargar datos
	void loaddatafromdatabase(String typedb, String namedb, String userdb, String passdb, int[] H, int Duration[],int[] Tini_data, int[] Tfin_data, int object_id) throws Exception{
		
		int T=H.length;
		DataTGenerator datatgenerator = new DataTGenerator();
		datatgenerator.loaddatafromdatabase(typedb,namedb,userdb,passdb,Tini_data[0],Tfin_data[0],object_id);
		
		ID				 = new int[T];				
		Nombre 			 = new String[T];
		Propietario 	 = new String[T];
		NombreCentral	 = new String[T];				//idAttribute=185		
		Tecnologia		 = new String[T];				//idAttribute=186
		Combustible		 = new String[T];				//idAttribute=187	
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
		Vini			 = new double[T];              
		Vfin			 = new double[T];        
		Vmin			 = new double[T];             
		Vmax			 = new double[T];              
		KEstanque		 = new double[T];              	
		AEstanque		 = new double[T];      
		SpinningMax		 = new double[T];
		SpinningMax2	 = new double[T];
		CPFMax		 	 = new double[T];
		Reserva_Pronta	 = new int[T];
		Commitment		 = new int[T];	
		StopCost		 = new double[T];	
		IDAcoplaTV		 = new int[T];					//idAttribute=188	
		FactorAcoplaTVTG = new double[T];               //idAttribute=189	
		IsERNC			 = new int[T];	
		Dependence		 = new int[T];
		Fdependence		 = new double[T];
		
		//System.out.println("ID");
		loadint(this.ID,datatgenerator.ID, H, Duration);
		//System.out.println("Nombre");
		loadstring(this.Nombre,datatgenerator.Nombre, H, Duration);
		loadstring(this.Propietario,datatgenerator.Propietario, H, Duration);
		//System.out.println("TminOn");
		loadint(this.TminOn,datatgenerator.TminOn, H, Duration);
		//System.out.println("TminOff");
		loadint(this.TminOff,datatgenerator.TminOff, H, Duration);
		//System.out.println("TiniR");
		loadint(this.TiniR,datatgenerator.TiniR, H, Duration);
		//System.out.println("TiniN");
		loadint(this.TiniN,datatgenerator.TiniN, H, Duration);
		//System.out.println("PiniN");
		loaddouble(this.PiniN,datatgenerator.PiniN, H, Duration);
		//System.out.println("GradRA");
		loadint(this.GradRA,datatgenerator.GradRA, H, Duration);
		//System.out.println("GradRP");
		loadint(this.GradRP,datatgenerator.GradRP, H, Duration);
		//System.out.println("CostGradRA");
		loaddouble(this.CostGradRA,datatgenerator.CostGradRA, H, Duration);
		//System.out.println("CostGradRP");
		loaddouble(this.CostGradRP,datatgenerator.CostGradRP, H, Duration);
		//System.out.println("CostGradS");
		loaddouble(this.GradS,datatgenerator.GradS, H, Duration);
		//System.out.println("CostGradB");
		loaddouble(this.GradB,datatgenerator.GradB, H, Duration);
		//System.out.println("CostGradRE");
		loaddouble(this.GradRE,datatgenerator.GradRE, H, Duration);
		//System.out.println("OwnConsuption");
		loaddouble(this.OwnConsuption,datatgenerator.OwnConsuption, H, Duration);
		//System.out.println("HotStart_upCost");
		loaddouble(this.HotStart_upCost,datatgenerator.HotStart_upCost, H, Duration);
		//System.out.println("WarmStart_upCost");
		loaddouble(this.WarmStart_upCost,datatgenerator.WarmStart_upCost, H, Duration);
	    //System.out.println("ColdStart_upCost"); 
		loaddouble(this.ColdStart_upCost,datatgenerator.ColdStart_upCost, H, Duration);
		//System.out.println("Thot");
		loadint(this.Thot,datatgenerator.Thot, H, Duration);
		//System.out.println("Twarm");
		loadint(this.Twarm,datatgenerator.Twarm, H, Duration);
		//System.out.println("Pmin");
		loaddouble(this.Pming,datatgenerator.Pming, H, Duration);
		//System.out.println("Pmax");
		//loaddouble(this.Pmaxg,datatgenerator.Pmaxg, H, Duration);
		//System.out.println("CespMeRA");
		loaddouble(this.CespMeRA,datatgenerator.CespMeRA, H, Duration);
		//System.out.println("CespMeRP");
		loaddouble(this.CespMeRP,datatgenerator.CespMeRP, H, Duration);
		//System.out.println("PoderCal");
		loaddouble(this.PoderCal,datatgenerator.PoderCal, H, Duration);
		//System.out.println("Pcomb");
		loaddouble(this.Pcomb,datatgenerator.Pcomb, H, Duration);
		//System.out.println("Barra");
		loadint(this.Barra,datatgenerator.Barra, H, Duration);
		//System.out.println("Unavalaible");
		//loadint(this.Unavalaible,datatgenerator.Unavalaible, H, Duration);
		//System.out.println("ForcedGenerator");
		loaddouble(this.ForcedGenerator,datatgenerator.ForcedGenerator, H, Duration);
		//System.out.println("Vini");
		loaddouble(this.Vini,datatgenerator.Vini,H,Duration);
		//System.out.println("Vfin");
		loaddouble(this.Vfin,datatgenerator.Vfin,H,Duration);
		//System.out.println("Vmin");
		loaddouble(this.Vmin,datatgenerator.Vmin,H,Duration);
		//System.out.println("Vmax");
		loaddouble(this.Vmax,datatgenerator.Vmax,H,Duration);
		//System.out.println("KEstanque");
		loaddouble(this.KEstanque,datatgenerator.KEstanque,H,Duration);
		//System.out.println("AEstanque");
		loaddouble(this.AEstanque,datatgenerator.AEstanque,H,Duration);
		//System.out.println("SpinningMax");
		loaddouble(this.SpinningMax,datatgenerator.SpinningMax,H,Duration);
		//System.out.println("SpinningMax2");
		loaddouble(this.SpinningMax2,datatgenerator.SpinningMax2,H,Duration);
		//System.out.println("CPFMax");
		loaddouble(this.CPFMax,datatgenerator.CPFMax,H,Duration);
		//System.out.println("Reserva_Pronta");
		loadint(this.Reserva_Pronta,datatgenerator.Reserva_Pronta,H,Duration);
		//System.out.println("Commitment");
		loadint(this.Commitment,datatgenerator.Commitment,H,Duration);
		//System.out.println("StopCost");
		loaddouble(this.StopCost,datatgenerator.StopCost,H,Duration);
		//System.out.println("NombreCentral");
		loadstring(this.NombreCentral,datatgenerator.NombreCentral, H, Duration);
		//System.out.println("Tecnologia");
		loadstring(this.Tecnologia,datatgenerator.Tecnologia, H, Duration);
		//System.out.println("Combustible");
		loadstring(this.Combustible,datatgenerator.Combustible, H, Duration);
		//System.out.println("StopCost");
		loadint(this.IDAcoplaTV,datatgenerator.IDAcoplaTV,H,Duration);
		//System.out.println("FactorAcoplaTVTG");
		loaddouble(this.FactorAcoplaTVTG,datatgenerator.FactorAcoplaTVTG,H,Duration);
		//System.out.println("IsERNC");
		loadint(this.IsERNC,datatgenerator.IsERNC,H,Duration);
		//System.out.println("Dependence");
		loadint(this.Dependence,datatgenerator.Dependence,H,Duration);
		//System.out.println("Fdependence");
		loaddouble(this.Fdependence,datatgenerator.Fdependence,H,Duration);
		
		
		//la potencia maxima, potencia minima e indisponibilidad tienen un tratamiento especial
		indispot(datatgenerator.Pmaxg,datatgenerator.Pming,datatgenerator.Unavalaible,datatgenerator.ForcedGenerator,H);
		
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