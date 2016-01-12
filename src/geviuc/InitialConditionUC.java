// condiciones iniciales para UC

public class InitialConditionUC{
	
	int     	 EtpArrIni;   //EtpArrIni etapa a partir de la cual puede haber rampa de arranque
	int     	 EtpParIni;   //EtpParIni etapa a partir de la cual puede haber rampa de parada
	int[]   	 NetpA;       //NetpA[t]  numero de escalones de arranque "hacia atras" a partir de t
	int[]	  	 NetpP;       //NetpP[t]  numero de escalones de arranque "hacia adelante" a partir de t
	int     	 EtpOnIni;	  //EtpOnIni etapa a partir de la cual aplico condicion Ton, depende de condiciones iniciales
	int     	 EtpOffIni;   //EtpOffFin 
	int     	 EtpOnFin;	  //EtpOnFin 
	int     	 EtpOffFin;   //EtpOffIni etapa a partir de la cual aplico condicion Ton, depende de condiciones iniciales
	int[]   	 EtpOn;	  	  //EtpOn[t]  etapas a partir de t durate las cuales la maquina tiene que estar encendida, depende de t si las etapas son de ancho variable          
	int[]   	 EtpOff;	  //EtpOff[t] etapas durate las cuales la maquina tiene que estar apagada, depende de t si las etapas son de ancho variable 
	int[]   	 Arra;        //
	int[]   	 Par;         //
	double[][] 	 PaFx;        //Potencia asignada a cada escalon de rampa de arranque
	double[][]   PpFx;        //Potencia asignada a cada escalon de rampa de parada
	int          n_arraq;     //numero de restricciones por consumo qa en rampas de arranque
	int          n_par;       //numero de restricciones por consumo qp en rampas de parada 
	int          EtpArrBg;    //etapa hasta la cual Bg=0
	int          EtpArrBgEq1; //etapa hasta la cual Bg=1
	int          Tfs;         //tiempo que lleva apagada la maquina
	int[][]		 matriz_ia;	
	int          n_ia_max;    
	int          n_ip_max;
	int			 Bgi;		  //condicion inicial, 1 si unidad estaba encendidad en t=1, 0 en caso contrario	
	
	int[] 		 NARR;       //NARR[t] numero de escalones de rampa de arranque en t	
	int[]        NPAR;       //NPAR[t] numero de escalones de rampa de parada en t       
	
	//funcion de costo
	double[][]	 Pminuc;	  //potencia minima de cada tramo	
	double[][]	 Pmaxuc;      //potencia maxima de cada tramo
	double[][]	 Alfauc;      //potencia minima de cada tramo	
	double[][]	 Betauc;      //potencia maxima de cada tramo
	int[][][] M;			  //M[t][iv][0 o 1] matriz que contiene informacion de inicio y fin de cada tramo asociada a variable binaria		
	int[] Niv;			      //Niv[T] matriz que indica la cantidad de variables binarias por periodo
	int n_iv;				  //numero de tramos maximos
	int[] Ntramos;	          //Ntramos[t] numero de tramos de la funcion de costos por periodo					
	
	//costos de partida
	double[] CPartida;	      //contiene costo de partida hot, warm y cold	
	int[] TPartida;			  //tiempo hot,warm
	int[] IPartida;           //tiempo a partir del cual se aplica restriccion	
	int[][] NCP;			  //vector que indica periodos "hacia atras" que se debe considerar 
	int[] Bgni;       		  //condicion inicial para costo de partida
	
	//constructor
	public InitialConditionUC(){
	}
	
	public void Calculate(double[] GradS, double[] GradB, int[] GradRA, int[] GradRP,int[]H,int[] Ton,int[] Toff,int[] Tini,double[] PiniN, double[] Pming, double[] Pmaxg, double[][] Pmin, double[][] Pmax, double[][] Alfa, double[][] Beta,int[] Ns, double[] HotStart_upCost, double[] WarmStart_upCost, double[] ColdStart_upCost, int[] Thot, int[] Twarm){
		
		int T=H.length;
		int n_periodos=0;
		int TonIni = 1; 	//tiempo a partir del cual se aplica restriccion Ton
		int ToffIni = 1; 	//tiempo a partir del cual se aplica restriccion Toff
		
		int Tacu; 			//variables auxiliares
		int ia;   			//variables auxiliares
		int r;   			//variables auxiliares 
		int max_tiempo;		//variables auxiliares
		//tiempos
		int t_GradOpN = 0;
		int n_ia_max_aux = 1;
		int n_ip_max_aux = 1;
		int t_GradRA_ini = 0;
		
		NetpA		=new int[T];
		NetpP		=new int[T];
		EtpOn 		=new int[T];
		EtpOff		=new int[T];
		Arra		=new int[T];
		Par 		=new int[T];
		
		for(int t=0;t<T;t++){
			n_periodos=n_periodos+H[t];
		}
		int k=0;
		/////////////////////////////////////////Condicion inicial Bgi//////////////////////////////////////////////////////////
		Bgi=0;
		if(PiniN[0]>0){
			Bgi =1;		
		}
		/////////////////////////////////////////EtpArrIni EtpParIni////////////////////////////////////////////////////////////
		int t_total;
		int t_total_par;
		int taux=0;
		int taux_par=0;
		EtpArrIni = 1;
		EtpParIni = 1;

		///////////hora a partir de la cual se aplica la restriccion Ton y Toff//////////////////////////////////////////////////////
		//System.out.println("Tini= "+Tini[0]+" PiniN= "+PiniN[0]+" Toff= "+Toff[0]+ "Ton= "+Ton[0]);
		//caso 1
		if(Tini[0]>0 & PiniN[0]>=Pming[0] & Tini[0]>=Ton[0]){
			//System.out.println("caso 1");
			TonIni =1;
			ToffIni=1;
		}

		//caso 2
		else if(Tini[0]>0 & PiniN[0]>=Pming[0] & Tini[0]<Ton[0]){
			//System.out.println("caso 2");	
			TonIni=(int)(Ton[0]-Tini[0])+1;//anterior a este tiempo la maquina debe estar encendida
	    	ToffIni=(int)(Ton[0]-Tini[0])+1;		
		}
		
		//caso 3
		else if(PiniN[0]<Pming[0] & PiniN[0]>0){
			//System.out.println("caso 3");
			//caso raro
			TonIni=1;
	    	ToffIni=1;
		}
		
		//caso 4
		else if(PiniN[0]==0 & Math.abs(Tini[0])>=Toff[0]){
			//System.out.println("caso 4");
			TonIni=1;
			ToffIni=1;	
		}

		//caso 5
		else if(PiniN[0]==0 & Math.abs(Tini[0])<Toff[0]){
			//System.out.println("caso 5");
			ToffIni=(int)(Toff[0]+Tini[0])+1; //anterior a este tiempo la maquina debe estar apagada
			TonIni=(int)(Toff[0]+Tini[0])+1; 
		}
	
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		EtpOnIni = 0;
		EtpOffIni = 0;
		// Etapa a partir de la cual se aplica restriccion TminOn: EtpOnIni
    	Tacu = 0;
    	k=0;
   		if(TonIni <= T){ //caso TonIni menor que horizonte de tiempo T
    		while(Tacu < TonIni){ 
    			Tacu = Tacu + H[k];
        		EtpOnIni = EtpOnIni + 1;
        		k++;
        	}
    	}
   		else{
   			EtpOnIni = T; 
   		}
   		
   		// Etapa a partir de la cual se aplica restriccion TminOff: EtpOffIni
   		Tacu = 0;
    	k=0;
    	while(Tacu < ToffIni & k <T){ 
    		Tacu = Tacu + H[k];
        	EtpOffIni= EtpOffIni+ 1;
        	k++;
    	}
    	Tacu = 0;
    	k=0;
    	
    	///////////////////////////////////////////////////////////////////////////////////////////////////////////////// 	
		EtpOnFin = 0;
		EtpOffFin = 0;
		
		// Etapa hasta la cual se puede exigir estar en operacion TminOn periodos: EtpOnFin	
		Tacu =H[T-1];
    	ia=1;
    	if(Ton[0] <= T){ // Tiempo Ton menor que horizonte de tiempo T
			while(Tacu < Ton[0] ){
				Tacu = Tacu + H[T-1-ia];
    			EtpOnFin = EtpOnFin + 1;
    			ia++;
    		}
			Tacu =H[T-1];
			ia=0;		
		}
		else{
			EtpOnFin=T;
		} 	
   
 		// Etapa hasta la cual se puede exigir estar fuera de servicio TminOff periodos: EtpOffFin
 		Tacu=H[T-1];
   		ia=1; 	 	
   		if(Toff[0] > n_periodos){
			Toff[0] = H.length;
		}
		while(Tacu < (Toff[0] + GradRP[0]+ GradRA[0]) & (T-1-ia)>=0){ // ANTES DECIA while(Tacu < (Toff[j])) while(Tacu < (Toff[j]+t_GradRP[j]))
			Tacu = Tacu + H[T-1-ia];
    		EtpOffFin = EtpOffFin + 1;
    		ia++;
    	}
    	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    	// Etapas hacia adelante para restriccion TminOn: EtpOn
		int ta;
		for(k=0;k<T;k++){
			EtpOn[k]=0;
			EtpOff[k]=0;
		}
		
		for(k=EtpOnIni-1; k<=T-EtpOnFin-1;k++){//ok
			EtpOn[k]= 1;
    		Tacu = H[k];
    		ta=1;
    		while(Tacu<Ton[0]){//ok
    			//System.out.println("aqui "+" t= "+t+" Tacu= "+Tacu+" EtpOn= "+EtpOn[j][t]+"Ton= "+Ton[j]);
    			Tacu = Tacu + H[k+ta];
        		EtpOn[k] = EtpOn[k]+ 1;
        		ta=ta+1;
        	}
   		}
   		
    	// Etapas hacia adelante para restriccion TminOff: EtpOff
    	for(k=EtpOffIni-1; k<=T-EtpOffFin-1;k++){
			EtpOff[k]= 1;
    		Tacu = H[k];
    		ta=1;
    		while(Tacu<(Toff[0]+GradRP[0]+GradRA[0]) & (k+ta)<T){ //antes decia while(Tacu<Toff[j))
    			Tacu = Tacu + H[k+ta];
        		EtpOff[k] = EtpOff[k]+ 1;
        		ta=ta+1;
        	}
        	//System.out.println("EtpOff= "+EtpOff[t]+"  t= "+(t+1));
   		}
   		
   		//System.out.println("EtpOffIni= "+EtpOffIni);
   		//System.out.println("EtpOffFin= "+EtpOffFin);
   		//System.out.println("T= "+T);
   		
   		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
   		//Etapa hasta la cual Bg=0, para condiciones iniciales
    	EtpArrBg=0;	
    	t_total=0;
    	taux=0;
    	k=0;
   		
   		if(GradRP[0]!=0 & GradRA[0]!=0 & GradS[0]!=0){
 			//System.out.println("CASE 1");
    		//caso1
			if(PiniN[0]>Pming[0] & Tini[0]>=Ton[0]){
				//System.out.println("caso 1 t_Bg");
				t_total=0;		
				//System.out.println("t_total t_Bg= "+t_total);	
				while(taux<=t_total){
					taux=taux+H[k];
					EtpArrBg=EtpArrBg+1;
					k++;
				}
				k=0;
				t_total=0;
				taux=0;	
			}
			
			//caso2
			else if(PiniN[0] > Pming[0] & Tini[0] < Ton[0] ){
				//System.out.println("caso 2 t_Bg");
				t_total=0;		
				//System.out.println("t_total t_Bg= "+t_total);
			
				while(taux<=t_total){
					taux=taux+H[k];
					EtpArrBg=EtpArrBg+1;
					k++;
				}
				k=0;				
				t_total=0;
				taux=0;	
			}
		
			//caso3
			else if(PiniN[0] > 0 & PiniN[0] < Pming[0]){
				//System.out.println("caso 3 t_Bg");
				t_total=t_GradRA_ini;		
				//System.out.println("t_total Bg= "+t_total);
		
				while(taux<=t_total){
					if(taux<n_periodos){
						taux=taux+H[k];
						EtpArrBg = EtpArrBg + 1;
						k++;
					}
            		else{
            			//System.out.println("La maquina no para en el periodo de evaluacion");		
					return;
					}
				}
				k=0;
				t_total=0;
				taux=0;	
			}
    		//caso4
			else if(PiniN[0] ==0 & Math.abs(Tini[0])>=Toff[0]){
				System.out.println("caso 4 t_Bg");
				t_total=0;
				//t_total=GradRA[0];//Modificado, para permitir arranque al comienzo del periodo de evaluacion
				while(taux<=t_total){
					EtpArrBg=EtpArrBg+1;
					taux=taux+H[k];
					k++;
				}	
			}
    		//caso5
			else if(PiniN[0] ==0 & Math.abs(Tini[0])<Toff[0]){
				//System.out.println("caso 5 t_Bg");	
				t_total=(int) (Toff[0]-Math.abs(Tini[0])+GradRA[0]);
				//System.out.println("t_total t_Bg= "+t_total);		
	
				while(taux<=t_total){
					EtpArrBg=EtpArrBg+1;
					taux=taux+H[k];
					k++;
				}
				k=0;
				t_total=0;
				taux=0;
			}
	
		//System.out.println("EtpArrBg= "+EtpArrBg[j]);
		}
    	else if(GradRP[0]==0 & GradRA[0]==0 & GradS[0]!=0){
     		//System.out.println("CASE 2");	
    		//caso1
			if(PiniN[0]>Pming[0] & Tini[0]>=Ton[0]){
				//System.out.println("caso 1 t_Bg");
				t_total=0;		
				//System.out.println("t_total t_Bg= "+t_total);	
				while(taux<=t_total){
					taux=taux+H[k];
					EtpArrBg=EtpArrBg+1;
					k++;
				}
				k=0;
				t_total=0;
				taux=0;	
			}
			
			//caso2
			else if(PiniN[0]>Pming[0] & Tini[0]<Ton[0]){
				//System.out.println("caso 2 t_Bg");
				t_total=0;		
				//System.out.println("t_total t_Bg= "+t_total);
				while(taux<=t_total){
					taux=taux+H[k];
					EtpArrBg=EtpArrBg+1;
					k++;
				}
				k=0;				
				t_total=0;
				taux=0;	
			}
			//caso3
			else if(PiniN[0]==0 & Math.abs(Tini[0])>=Toff[0]){
				//System.out.println("caso 3 t_Bg");
				EtpArrBg=1;
			}
    		//caso4
			else if(PiniN[0]==0 & Math.abs(Tini[0])<Toff[0]){
				//System.out.println("caso 4 t_Bg");	
				t_total=(int) (Toff[0]-Math.abs(Tini[0]));
				//System.out.println("t_total t_Bg= "+t_total);		
				while(taux<=t_total){
					EtpArrBg=EtpArrBg+1;
					taux=taux+H[k];
					k++;
				}
				k=0;
				t_total=0;
				taux=0;
			}
		}
    	else if(GradRP[0]==0 & GradRA[0]==0 & GradS[0]==0){
   			//System.out.println("CASE 3");
    		//caso1
			if(PiniN[0]>Pming[0] & Tini[0]>=Ton[0]){
				//System.out.println("caso 1 t_Bg");
				EtpArrBg=1;
			}
			//caso2
			else if(PiniN[0]>Pming[0] & Tini[0]<Ton[0]){
				//System.out.println("caso 2 t_Bg");
				t_total=0;		
				//System.out.println("t_total t_Bg= "+t_total);
				while(taux<=t_total){
					taux=taux+H[k];
					EtpArrBg=EtpArrBg+1;
					k++;
				}
				k=0;				
				t_total=0;
				taux=0;	
			}		
    		//caso3
			else if(PiniN[0]==0 & Math.abs(Tini[0])>=Toff[0]){
				//System.out.println("caso 3 t_Bg");
				EtpArrBg=1;
			}
    	
    		//caso4
			else if(PiniN[0]==0 & Math.abs(Tini[0])<Toff[0]){
				//System.out.println("caso 4 t_Bg");	
				t_total=(int) (Toff[0]-Math.abs(Tini[0]));
				//System.out.println("t_total t_Bg= "+t_total);		
				
				while(taux<=t_total){
					EtpArrBg=EtpArrBg+1;
					taux=taux+H[k];
					k++;
				}		
				k=0;
				t_total=0;
				taux=0;
			}
		
		}
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// etapa hasta la cual Bg=1, para condiciones iniciales
		taux=0;
		k=0;
		EtpArrBgEq1=0;
	    if(Ton[0]>0 & Tini[0]>0){ // para maquinas con tiempo Ton
	    	if(Tini[0]<Ton[0]){
	    		t_total=Ton[0]-Tini[0];	
	    		//System.out.println("Ton[j]= "+Ton[j]+"Tini[j]= "+Tini[j]);
	    		while(taux < t_total & k<T){
	    			EtpArrBgEq1 = EtpArrBgEq1 +1;
	    			taux=taux+H[k];
	    			k++;	
	    		}
	    	} 	
	    }
	    	
		//////////////////////NetpA/////////////////////////////////////////////////////////////////////////////////////////////////
		int n_arraq_aux=0;	
		
		//inicializo
		for(int t=0;t<T;t++){
			NetpA[t]=0;
		}
		
		n_arraq=0;
		//System.out.println("GradRA= "+GradRA[0]);	
		if(GradRA[0]>0){ //verifico si la maquina tiene rampa de arranque
			for(int t=EtpArrIni-1;t<T;t++){
				ia=0;
				Tacu=0;
				if(t-ia-1>=0){
					if(H[t-ia-1]<GradRA[0]){ 
						while(t>ia){
							if(Tacu<GradRA[0]){
								Tacu = Tacu + H[t-ia-1];           
								NetpA[t]= NetpA[t]+ 1;      
								n_arraq_aux++;
								ia++;
							}
							else{
								ia=100000;
							}
						}
					}
					else{
						NetpA[0]=1;
						NetpA[t]=1; 	
						n_arraq=n_arraq+1;
					}
				}
				//System.out.println("NetPa= "+NetpA[t]+" t= "+t);	
			}
		}
		if(n_arraq_aux>n_arraq){
			n_arraq=n_arraq_aux;
		}
		n_arraq_aux=0;
		
		/////////////////////////NetpP//////////////////////////////////////////////////////////////////////////////////////////////
		//inicializacion
		for(int t=0;t<T;t++){
			NetpP[t]=0;
		}

		int ip;
		int n_par_aux=0;	
		n_par=0;

		if(GradRP[0] > 0){ //verifico si la maquina tiene rampa de parada
			for(int t=EtpParIni-1;t<T;t++){
				ip=0;
				Tacu=0;
				//System.out.println(H[t+ip]);
				if(H[t+ip]<GradRP[0]){ //si ancho de tiempo es mas grande que tiempo de arranqe
					while(t+ip<T){
						if(Tacu<GradRP[0]){
							Tacu = Tacu + H[t+ip];           
							NetpP[t]= NetpP[t]+ 1;      
							n_par_aux++;
							ip++;
						}
						else{
							ip=100000;
						}
					}
				}
				else{
					NetpP[t]=1;	
					n_par=n_par+1;
				}
				//System.out.println("NetpP= "+NetpP[t]+" t= "+(t+1));
			}
		}
		if(n_par_aux>n_par){
			n_par=n_par_aux;
		}		
		n_par_aux=0;

		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	  	if(GradRA[0] != 0){
			for(int t=0;t<T;t++){
				if(n_ia_max_aux < NetpA[t]){
					n_ia_max_aux =NetpA[t];
				}	
			}
		}
		else{
			n_ia_max_aux =0;
		}
		
		n_ia_max=0;
	
		if(n_ia_max_aux>=n_ia_max){
			n_ia_max=n_ia_max_aux;
		}
	    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	  	if(GradRP[0]!=0){
			for(int t=0;t<T;t++){
				if(n_ip_max_aux<NetpP[t]){
					n_ip_max_aux=NetpP[t];
				}	
			}
		}
		else{
			n_ip_max_aux = 0;
		}
		n_ip_max=0;
		
		if(n_ip_max_aux>=n_ip_max){
			n_ip_max=n_ip_max_aux;
		}
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		PaFx 		= new double[T][n_ia_max];        
		PpFx 		= new double[T][n_ip_max];  
		matriz_ia 	= new int[n_ia_max][T];
	
		for(int t=0;t<T;t++){
			for(ia=0;ia<n_ia_max;ia++){
				matriz_ia[ia][t]=0;	
			}
		}	
	
		////////////////////////////Arra////////////////////////////////////////////////////////////////////////////////////////	
		for(int t=0;t<T;t++){
			for(ia=0;ia<n_ia_max;ia++){
				Arra[t]=0;
			}
		}
		for(int t=EtpArrIni-1;t<T;t++){
			k=0;	
			while(k<NetpA[t] & (t-k-1)>=0){
				Arra[t-k-1]=Arra[t-k-1]+1;
				k++;
			}
		}
		ia=0;
		////////////////////////////Par/////////////////////////////////////////////////////////////////////////////////////////	
		for(int t=0;t<T;t++){
			Par[t]=0;
		}
		
		for(int t=EtpParIni-1;t<T;t++){
			k=0;	
			while(k<NetpP[t]){
				//System.out.println("t= "+(t+1)+" k= "+k+"NetpP= "+NetpP[j][t]);
				Par[t+k]=Par[t+k]+1;
				//System.out.println("t= "+(t+1)+" k= "+k+"NetpP= "+NetpP[j][t]+" Par= "+Par[j][t+k]);
				k++;
			}
		}
	
		ip=0;
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//n_par
		n_par=0;
		if(GradRA[0]>0 & GradRP[0]>0){
			for(int t=0;t<T;t++){
				for(ip=0;ip<Par[t];ip++){
					n_par=n_par+1;
				}
			}
		}
	
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//n_arraq
		if(GradRA[0]>0 & GradRP[0]>0){
			for(int t=0;t<T;t++){
				for(ia=0;ia<Arra[t];ia++){
					n_arraq=n_arraq+1;
				}
			}
		}
		
		///////////////////////PaFx PpFx//////////////////////////////////////////////////////////////////////////////////////////
		double to;
		double too;
		int iaa;
		int ipp;
	
		//inicializo potencias de rampa de arranque y parada en cero
		int t=0;
		for(t=0;t<T;t++){
			for(ia=0;ia<n_ia_max;ia++){
				PaFx[t][ia]=0;
			}
			for(ip=0;ip<n_ip_max;ip++){
				PpFx[t][ip]=0;	
			}
		}
		
    	iaa=0;
		if(GradRA[0]>0){
			for(t=1;t<T;t++){  // (OJO= decia t=EtpArrIni - 1)
				iaa=0;
				if(EtpArrIni >= 1){
					too=0;
					to=H[t-1];
					for(ia=0;ia<NetpA[t];ia++){
						if(ia==0 & to >GradRA[0]){
							PaFx[t][ia]=Pming[t]*GradRA[0]/(2*H[t-1]);
						}
						if(ia==0 & to <=GradRA[0]){
							PaFx[t][ia]=Pming[t]-((Pming[t]/GradRA[0]))*H[t-1]/2;
						}
						else if(ia>0 & ia <NetpA[t]-1){
							PaFx[t][ia]=PaFx[t][ia-1]-((Pming[t]/GradRA[0]))*H[t-ia-1]/2-((Pming[t]/GradRA[0]))*H[t-ia]/2;
						}
						else if(ia==(NetpA[t]-1) &(t-1-ia+1)>=0 & ia>=1 & (too+H[t-1-ia])>=GradRA[0]){
							//PaFx[t][ia]=Pming[0]/GradRA[0]*(GradRA[0]-too)/(2*H[t-ia-1]);
							PaFx[t][ia]=(0.5*(GradRA[0]-too)*(PaFx[t][ia-1]-((Pming[t]/GradRA[0]))*H[t-ia]/2))/H[t-1-ia];
						}
						else if(ia==(NetpA[t]-1) &(t-1-ia+1)>=0 & ia>=1 & (too+H[t-1-ia])< GradRA[0]){
							PaFx[t][ia]=PaFx[t][ia-1]-((Pming[t]/GradRA[0]))*H[t-ia-1]/2-((Pming[t]/GradRA[0]))*H[t-ia]/2;
						}
						
						if((t-1-ia-1)>=0){
							to=H[t-1-ia-1]+to;
						}
						if((t-ia-1)>=0){
							too=H[t-ia-1]+too;
						}
					} 
				}
				
			}
	 	}
	 	
	 	ipp=0;
		int ip_aux=0;
		int t_futuro=0;
		
		for(t=EtpParIni-1;t<T;t++){
			to=GradRP[0];
			ip_aux=0;
			t_futuro=0;
			
			while(ip_aux<NetpP[t]){
				t_futuro=H[t+ip_aux]+t_futuro;	
				ip_aux++;
			}
	
			ipp=0;
			to=H[t];
			//condicion inicial considero PiniN
			if(t==0){
				for(ip=0;ip<NetpP[t];ip++){
					if(ip==0 & t<T & to>GradRP[0]){
						//System.out.println("caso 1 t= "+(t+1));	
						PpFx[t][ip]=PiniN[0]*GradRP[0]/(2*H[t]);
					}
					else if(ip==0 & t<T & to<=GradRP[0]){
						//System.out.println("caso 2 t= "+(t+1));	
						PpFx[t][ip]=PiniN[0]-((PiniN[0]/GradRP[0]))*H[t]/2;
					}		
					else if(ip>0 & ip<NetpP[t]-1 &(t+ip)<T){
						//System.out.println("caso 3 t= "+(t+1));	
						PpFx[t][ip]=PpFx[t][ip-1]-((PiniN[0]/GradRP[0]))*H[t+ip]/2-((PiniN[0]/GradRP[0]))*H[t+ip-1]/2;
					}
					else if((ip==(NetpP[t]-1)) & t_futuro<GradRP[0]){
						//System.out.println("caso 4 t= "+(t+1));	
						PpFx[t][ip]=PpFx[t][ip-1]-((PiniN[0]/GradRP[0]))*H[t+ip]/2-((PiniN[0]/GradRP[0]))*H[t+ip-1]/2;
					}
					else if((ip==(NetpP[t]-1)) & t_futuro>=GradRP[0] & ip>=1){
						//System.out.println("caso 5 t= "+(t+1));	
						PpFx[t][ip]=(PpFx[t][ip-1]-((PiniN[0]/GradRP[0]))*H[t+ip-1]/2)*(H[t+ip]+GradRP[0]-to)*0.5/H[t+ip];			
					}
					
					if(t+ip+1<T){
						to=to+H[t+ip+1];
					}
				}
			}
			else{
				for(ip=0;ip<NetpP[t];ip++){
				
					if(ip==0 & t<T & to>GradRP[0]){
						//System.out.println("caso 1 t= "+(t+1));	
						PpFx[t][ip]=Pming[0]*GradRP[0]/(2*H[t]);
					}
					else if(ip==0 & t<T & to<=GradRP[0]){
						//System.out.println("caso 2 t= "+(t+1));	
						PpFx[t][ip]=Pming[0]-((Pming[0]/GradRP[0]))*H[t]/2;
					}		
					else if(ip>0 & ip<NetpP[t]-1 &(t+ip)<T){
						//System.out.println("caso 3 t= "+(t+1));	
						PpFx[t][ip]=PpFx[t][ip-1]-((Pming[0]/GradRP[0]))*H[t+ip]/2-((Pming[0]/GradRP[0]))*H[t+ip-1]/2;
					}
					else if((ip==(NetpP[t]-1)) & t_futuro<GradRP[0]){
						//System.out.println("caso 4 t= "+(t+1));	
						PpFx[t][ip]=PpFx[t][ip-1]-((Pming[0]/GradRP[0]))*H[t+ip]/2-((Pming[0]/GradRP[0]))*H[t+ip-1]/2;
					}
					else if((ip==(NetpP[t]-1)) & t_futuro>=GradRP[0] & ip>=1){
						//System.out.println("caso 5 t= "+(t+1));	
						PpFx[t][ip]=(PpFx[t][ip-1]-((Pming[0]/GradRP[0]))*H[t+ip-1]/2)*(H[t+ip]+GradRP[0]-to)*0.5/H[t+ip];			
					}
					
					if(t+ip+1<T){
						to=to+H[t+ip+1];
					}
				}
			}
		}
	
		/////////////////////////matriz_ia/////////////////////////////////////////////////////////////////////////////////////
		if(GradRA[0]>0){
			for(t=1; t<T; t++){   // (OJO= decia t=EtpArrIni - 1)
				for(ia=0;ia<NetpA[t];ia++){
					matriz_ia[ia][t-ia-1]=1;
				}
			}
		}
   		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    	if(PiniN[0]==0){
    		Tfs=Math.abs(Tini[0]);
    	}
    	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
		//calculo del numero de escalones de rampa de arranque que hay en t
		NARR=new int[T];
		//inicializacion
		for(t=0;t<T;t++){
			NARR[t]=0;
		}
		calcula_narr(NARR,GradRA,H);
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//calculo de numero de escalones de rampa de parada que hay en t
		NPAR=new int[T];
		//inicializacion
		for(t=0;t<T;t++){
			NPAR[t]=0;
		}
		calcula_npar(NPAR,GradRP,H);
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//funcion de costo
		double[][] tramo_valido = new double[T][2];
		tramo_valido(Pmax,Pmin,Pming,Pmaxg,Ns,tramo_valido);	
		int tramos = n_tramos(Pmax,Pmin,Pming,Pmaxg,tramo_valido);
		n_iv = tramos;
		//System.out.println("tramos= "+tramos);
		Pmaxuc = new double[tramos][T];
		Pminuc = new double[tramos][T];
		Alfauc = new double[tramos][T];
		Betauc = new double[tramos][T];
		Ntramos = new int[T];
		tramos_maximo(Pmax,Pmin,Alfa,Beta,Pming,Pmaxg, tramo_valido, Pminuc, Pmaxuc,Alfauc,Betauc,Ntramos);
		//matriz M
		M = new int[T][tramos][2];
		Niv = new int[T];
		calcula_Niv(Alfauc,Betauc,Pminuc,Pmaxuc,Niv,tramos,Ntramos);
		calcula_M(Alfauc,Betauc,Pminuc,Pmaxuc,Niv,M,tramos);
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//costo de partida
		CPartida = new double[3];
		TPartida = new int[3];
		IPartida = new int[3];
		
		//costo de partida
		CPartida[0] = HotStart_upCost[0];
		CPartida[1] = WarmStart_upCost[0];
		CPartida[2] = ColdStart_upCost[0];
		//tiempo de funcion costo de partida
		TPartida[0] = 0+GradRA[0];
		TPartida[1] = Thot[0] + GradRA[0];
		TPartida[2] = Twarm[0] + GradRA[0];
		
		//tiempo a partir del cual se puede aplicar restriccion
		IPartida[0] = 0;
		if(Tini[0]>0){
			IPartida[1] = 0;
			IPartida[2] = 0;
		}
		else{
			//solo para H[t]=1, falta caso H[t]<>1
			//IPartida[1] = TPartida[1]+Tini[0]-GradRA[0];
			//IPartida[2] = TPartida[2]+Tini[0]-GradRA[0];
			//Revisar!!!
			IPartida[1] = 0;
			IPartida[2] = 0;
		}
		//vector que contabiliza cuantos periodos "hacia atras" debo considerar
		NCP =new int[3][T];
		//inicializacion
		for(k=0;k<3;k++){
			for(t=0;t<T;t++){
				NCP[k][t]=0;	
			}
		}
		for(k=0;k<3;k++){
			//partida en caliente: hot
			if(k==0){
				for(t=0;t<T;t++){
					NCP[k][t]=1;	
				}
			}
			//partida en tibio o frio
			else{
				for(t=0;t<T;t++){
					taux = 0;
					r    = 1;
					while(taux <= TPartida[k]){
						if(t-r>=0){
							taux=taux+H[t-r];	
						}
						//relacionado con Bgni, para t < 0
						else{
							taux=taux+1;	
						}
						NCP[k][t]=NCP[k][t]+1;	
						r++;
					}
				}	
			}
		}
		//Bgni se utiliza para los tiempos t<0, Cpartida >= C*(Bg(t)-Bg(t-1)- ....... -Bg(t-ttt*))
		max_tiempo=Math.abs(Tini[0]);
		//System.out.println("max tiempo= "+max_tiempo);
		for(k=0;k<3;k++){
			if(TPartida[0] > max_tiempo){
				max_tiempo = TPartida[0];	
			}	
		}
		Bgni=new int[max_tiempo];
		for(t=0;t<max_tiempo;t++){
			if(Tini[0]>0){  //si central estaba encendida
				Bgni[t]=1;
			}
			else{          //si central estaba apagada 
				Bgni[t]=0;		
			}
		}
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	}
	
	//funcion que determina que tramos de la funcion de costos se pueden activar
	static void tramo_valido(double[][] pmax, double[][] pmin, double[] pming, double[] pmaxg, int[] Ns, double[][] tramo_valido){
		
		for(int t=0;t<pming.length;t++){
			for(int iv=0;iv<Ns[t];iv++){
				if(pming[t]>= pmin[iv][t]){
					tramo_valido[t][0]=iv;	
				}
				if(pmaxg[t]<= pmax[Ns[t]-iv-1][t]){
					tramo_valido[t][1]=Ns[t]-iv-1;	
				}
			}
			//casos especiales, en caso que potencia minima sea menor a potencia de primer tramo o potencia maxima mayor a potencia de ultimo tramo
			if(pming[t]< pmin[0][t]){
				tramo_valido[t][0]=0;	
			}
			if(pmaxg[t]>pmax[Ns[t]-1][t]){
				tramo_valido[t][1]=Ns[t]-1;	
			}
			//System.out.println("t= "+t+ " pming= "+pming[t]+ " pmaxg= "+pmaxg[t]+" tramo valido ini= "+tramo_valido[t][0]+" tramo valido fin= "+tramo_valido[t][1]);		
		}
	}
	
	//funcion que calcula el maximo numero de tramos por periodo
	static int n_tramos(double[][] pmax, double[][] pmin, double[] pming, double[] pmaxg, double[][] tramo_valido){
		int ntramos_max = 0;
		int ntramos_aux = 0;
		
		//numero maximo de tramos
		//System.out.println("tramo_valido length= "+tramo_valido.length);
		for(int t=0;t<tramo_valido.length;t++){
			//System.out.println("tramo valido 0= "+tramo_valido[t][0]);
			//System.out.println("tramo valido 1= "+tramo_valido[t][1]);
			ntramos_aux = (int)(tramo_valido[t][1]-tramo_valido[t][0]+1);
			if(ntramos_aux>ntramos_max){
				ntramos_max = ntramos_aux;
			}
		}
		return ntramos_max;
	}
	
	//funcion que calcula las potencias maximas y minimas por tramos
	static void tramos_maximo(double[][] pmax, double[][] pmin, double[][] alfa, double[][] beta, double[] pming, double[] pmaxg,double[][] tramo_valido, double[][] pminuc, double[][] pmaxuc,double[][] alfauc, double[][] betauc, int[] Ntramos){
	
		int k=0;
		
		for(int t=0;t<tramo_valido.length;t++){
			k=0;
			for(int i=(int)tramo_valido[t][0];i<=(int)tramo_valido[t][1];i++){
				//System.out.println("i= "+i);
				//primer tramo
				if(i==tramo_valido[t][0] & k != (tramo_valido[t][1]-tramo_valido[t][0])){
					pminuc[k][t]=pming[t];
					pmaxuc[k][t]=pmax[i][t];
					alfauc[k][t]=alfa[i][t];
					betauc[k][t]=beta[i][t];
					//System.out.println("pminuc= "+pminuc[k][t] + "k= "+k);
					//System.out.println("pmaxuc= "+pmaxuc[k][t] + "k= "+k);
					k++;
				}
				else if(i==tramo_valido[t][0] & k==(tramo_valido[t][1]-tramo_valido[t][0])){
					pminuc[k][t]=pming[t];
					pmaxuc[k][t]=pmaxg[t];
					alfauc[k][t]=alfa[i][t];
					betauc[k][t]=beta[i][t];
					//System.out.println("pminuc= "+pminuc[k][t] + "k= "+k);
					//System.out.println("pmaxuc= "+pmaxuc[k][t] + "k= "+k);
					k++;
				}
				//ultimo tramo
				else if(i==tramo_valido[t][1]){
					pminuc[k][t]=pmin[i][t];
					pmaxuc[k][t]=pmaxg[t];
					alfauc[k][t]=alfa[i][t];
					betauc[k][t]=beta[i][t];
					//System.out.println("pminuc= "+pminuc[k][t] + "k= "+k);
					//System.out.println("pmaxuc= "+pmaxuc[k][t] + "k= "+k);
					k++;
				}
				//tramos intermedios
				else{
					pminuc[k][t]=pmin[i][t];
					pmaxuc[k][t]=pmax[i][t];
					alfauc[k][t]=alfa[i][t];
					betauc[k][t]=beta[i][t];
					//System.out.println("pminuc= "+pminuc[k][t] + "k= "+k);
					//System.out.println("pmaxuc= "+pmaxuc[k][t] + "k= "+k);
					k++;
				}	
			}
			Ntramos[t]=k;
		}				
	}
	//funcion que calcula la cantidad de variables binarias por periodos
	static void calcula_Niv(double[][] alfauc,double[][] betauc, double[][] pminuc,double[][] pmaxuc,int[] Niv, int tramos, int[] Ntramos){
		int cont=1;
		for(int t=0;t<Niv.length;t++){
			cont=1;
			for(int iv=0;iv<Ntramos[t];iv++){
				if(iv > 0){
					//caso cambio de pendiente: se pasa de pendiente mayor a una menor
					if(alfauc[iv][t]< alfauc[iv-1][t]){
						//System.out.println("iv= "+iv+" "+alfauc[iv][t]+" "+ alfauc[iv-1][t]);
						cont++;
					}
					//caso no continuidad de los tramos de la funcion de costo
					else if(100*Math.abs((alfauc[iv-1][t]*pmaxuc[iv-1][t]+betauc[iv-1][t])-(alfauc[iv][t]*pminuc[iv][t]+betauc[iv][t]))/(Math.abs((alfauc[iv-1][t]*pmaxuc[iv-1][t]+betauc[iv-1][t])))>0.01){
						//System.out.println("iv= "+iv+" "+(alfauc[iv-1][t]*pmaxuc[iv-1][t]+betauc[iv-1][t])+" "+  (alfauc[iv][t]*pminuc[iv][t]+betauc[iv][t]));
						cont++;
					}
				}
			}
			Niv[t]=cont;
			//System.out.println("Niv= "+Niv[t]+" t= "+t);
		}
	}
	//funcion que calcula la matriz M
	static void calcula_M(double[][] alfauc,double[][] betauc,double[][] pminuc,double[][] pmaxuc, int[] Niv, int[][][] M, int tramos){
		int cont=0;
		for(int t=0;t<Niv.length;t++){
			cont=0;
			for(int iv=0;iv<tramos;iv++){
				//tamo inicial
				if(iv==0){
					M[t][cont][0] = 0;
					M[t][cont][1] = 0;
				}
				//se detectan cambios en la pendiente
				if(iv > 0){
					//caso cambio de pendiente: se pasa de pendiente mayor a una menor
					if(alfauc[iv][t]< alfauc[iv-1][t]){
						cont++;
						M[t][cont][0] = iv;
						M[t][cont][1] = iv;
					}
					//caso no continuidad de los tramos de la funcion de costo
					else if(100*Math.abs((alfauc[iv-1][t]*pmaxuc[iv-1][t]+betauc[iv-1][t])-(alfauc[iv][t]*pminuc[iv][t]+betauc[iv][t]))/(Math.abs((alfauc[iv-1][t]*pmaxuc[iv-1][t]+betauc[iv-1][t])))>0.01){
						cont++;
						M[t][cont][0] = iv;
						M[t][cont][1] = iv;
					}
					//caso convexo  >>> no estoy 100% seguro
					else if(alfauc[iv][t] > alfauc[iv-1][t]){
						M[t][cont][1] = iv;
					}
				}
			}
		}
	}
	//funcion que calcula el numero de escalones de la rampa de arranque en t
	static void calcula_narr(int NARR[], int GradRA[], int H[]){
		boolean bandera=false;
		int T=H.length;
		int k=0;
		int d=0;
		int t=T-1;
		
		while(t>=1){
			d=0;
			k=1;
			bandera=false;
			while(bandera==false & t-k>=0){
				NARR[t-k]=NARR[t-k]+1;
				d=d+H[t-k];
				k=k+1;
				if(d>=GradRA[0]){
					bandera=true;				
				}
			}
			t=t-1;
		}
		
	}
		
	//funcion que calcula el numero de escalones de la rampa de parada en t
	static void calcula_npar(int NPAR[], int GradRP[], int H[]){
		boolean bandera=false;
		int T=H.length;
		int k=0;
		int d=0;
		int t=0;
		
		while(t<=T-1){
			d=0;
			k=0;    // en este caso k empieza en 0 pq Bgp es igual a 1 cuando comienza la rampa
			bandera=false;
			while(bandera==false & (t+k)<T){
				NPAR[t+k]=NPAR[t+k]+1;
				d=d+H[t+k];
				k=k+1;
				if(d>=GradRP[0]){
					bandera=true;				
				}
			}
			t=t+1;
		}
		
	}
}
	
