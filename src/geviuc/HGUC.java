import ilog.concert.*;
import ilog.cplex.*;

public class HGUC{
	                  
	//variables binarias o continuas
	boolean var_bin=true;
	
	/****************************** VARIABLES PROBLEMA OPTIMIZACION *************************************************************************/
	//varibles continuas
	IloNumVar[][]	P;  		//P[iv][t]              	//potencia en operacion normal
	IloNumVar[][]   Pa;  		//Pa[ia][t]             	//potencia en rampa arranque
	IloNumVar[][]   Pp;  		//Pp[ip][t]             	//potencia en rampa parada
	IloNumVar[]     Pt;  		//P[t]                  	//potencia total Pt=P+Pa+Pp
	IloNumVar[]     Rg;  		//Rg[t]                 	//reserva en giro		
	IloNumVar[]     Rg2;  		//Rg[t]                 	//reserva en giro v2 (Ejemplo: reserva en giro para 10 min) 	
	IloNumVar[][]	q;      	//q[iv][t]             		//consumo de combustible en operacion normal   
	IloNumVar[][] 	qa;     	//qa[ia][t]           		//consumo de combustible en rampa arranque
	IloNumVar[][] 	qp;     	//qp[ip][t]            		//consumo de combustible en rampa parada
	IloNumVar[][] 	costo;                            		//costo de combustible
	IloNumVar[] 	Costo_AP_VNC1; //Costo_AP_VNC1[t]     	//costo arraque para t<T2
	IloNumVar[] 	Costo_AP_VNC2; //Costo_AP_VNC2[t]     	//costo arraque para t>T2
	IloNumVar[]		V;			   //V[t]				  	//volumen del estanque en el instante t	 	
	IloNumVar[]		Qver;								    //caudal turbinado
	
	IloNumVar[]		Vfic;									// volumen fictico, variable de holgura para satisfacer restriccion de potencia minima
	IloNumVar[]		Vfic2;									// volumen fictico, variable de holgura para satisfacer restriccion de caudal vertido minimo
	
	
	//variables binarias
	IloNumVar[]     Bg;     //Bg[t]
	IloNumVar[]    Bga;  	//Bga[t]                //1 cuando arranca
	IloNumVar[]    Bgp;  	//Bgp[t]                //1 cuando para
	IloNumVar[][]    B;  	//B[t][iv]              //variable binaria a cada tramo iv
	IloNumVar[][] Bpar;  	//Bpar[ia][t]           //variable binaria para rampa parada
	IloNumVar[][] Barr;  	//Barr[ia][t]           //variable binaria para rampa parada  
	IloNumVar[] 	  Bs;  	//Bs[t]                    //para unidades con rampa de arranque y parada
	IloNumVar[]      Bsp;  	//Bsp[t]                   //variable binaria que se activa cuando la rampa de parada se detiene
	IloNumVar[]      Bsa;  	//Bsa[t]                   //variable binaria que se activa cuando la rampa de arranque parte
	IloNumVar[][]    Bsu;  	//Bsu[is][t]               //variable binaria que se activa cuando maquina arranque y tiempo de parada supera el tiempo Tenf[is]
	
	//variables enteras
	IloNumVar[]      Bc;   //Bsa[t]                   //variable binaria que cuenta tiempo fuera de servicio
	IloNumVar[][]    Bx;   //Bx[is][t]                //variable binaria auxiliar
	IloNumVar[][]    Bh;   //Bh[is][t]                //variable binaria auxiliar de holgura
	
	//Variables de flujo
	IloNumVar[]    Y;  	
	IloNumVar[]    X;  	
	
	
	/******************************* FIN VARIABLES PROBLEMA OPTIMIZACION ********************************************************************************/
	
	//Atributos: todos variables en el tiempo
	int[]			ID;					// ID[t] id del generador
	String[]		Nombre; 			// Nombre[t]nombre del generador
	int[] 			TminOn;				// TminOn[t] Tiempo minimo de operacion
	int[] 			TminOff;			// TminOff[t] Tiempo minimo fuera de servicio
	int[]			TiniR;				// TiniR[t]
	int[]			TiniN;				// TiniN[t] tiempo inicial que lleva encendida o apagada la unidad generadora
	double[]		PiniN;				// PiniN[t] potencia inicial en t=-1 para operacion normal 
	int[] 			GradRA;				// GradRA[t]
	int[] 			GradRP;				// GradRP[t]
	double[]		CostGradRA;			// CostGradRA[t] 
	double[]		CostGradRP;			// CostGradRP[t]
	double[]     	GradS;				// GradS[t] gradiente de subida
	double[]     	GradB;				// GradB[t] gradiente de bajada					
	double[]		GradRE;				// GradRE[t]
	double[]		OwnConsuption;		// OwnConsuption[t] consumo propio
	double[]		HotStart_upCost;	// HotStart_upCost[t] 		 
	double[]		WarmStart_upCost;	// WarmStart_upCost[t]
	double[] 		ColdStart_upCost;	// ColdStart_upCost[t]
	int[] 			Thot;				// Thot[t]
	int[]			Twarm;				// Twarm[t]
	double[]		Pming;				// Pming[t] minimo tecnico, minimo tecnico puede variar en el tiempo
	double[] 		Pmaxg;				// Pmaxg[t] maximo tecnico, maximo tecnico puede variar en el tiempo		
	double[]     	CespMeRA;			// CespMeRA[t] costo especifico medio rampa arranque
	double[]     	CespMeRP;			// CespMeRP[t] costo especifico medio rampa parada			
	double[]     	PoderCal;			// PoderCal[t] poder calorifico combustible 
	double[]		Pcomb;   			// Pcomb[t] precio combustible  
	int[]			Barra;				// Barra[t] barra a la cual se encuentra conectado generador
	int[]			Unavalaible;	    // Unavalaible[t] indisponibilidad del generador	
	double[]		ForcedGenerator;	// ForcedGenerator[t] generacion forzada del generador 
	double[]		Rend; 				//Rendimiento
	double[]		Qvmin;				//Vertimiento minimo
	double[]		Qvmax;				//Vertimiento maximo
	int[]			Ctur;				//ID central aguas abajo que recibe caudal turbinado
	int[]			Cver;				//ID central aguas abajo que recibe caudal vertido
	double[]        SpinningMax;		// SpinningMax[t] reserva en giro maxima que puede aportar central
	double[]        SpinningMax2;		// SpinningMax2[t] reserva en giro maxima que puede aportar central. Reserva para 10 min
	double[]        CPFMax;				// Control primario de frecuencia maxima que puede aportar central
	int[]			Reserva_Pronta;		// Reserva pronta, 1 si central aporta reserva pronta, 0 en caso contrario
    int[]			Commitment;			// 1 se central tiene activado minimos tecnicos, 0 en caso contrario 
	double[]		StopCost;			// StopCost[t] Costo de detencion
	int[]			IDAcoplaTV;		 	// IDAcoplaTG[t] id de la turbina a gas (TG) a la que esta acoplada turbina vapor
	double[]		FactorAcoplaTVTG;	// FactorAcoplaTVTG[t] factor de acoplamiento entre la potencia de la TV con la potencia de la TG, PTV= FactorAcoplaTVTG x FTG 
	int[]			IsERNC;				// 1 si la central es ERNC, 0 en caso contrario		
    double[]		Aflu;				// Afluente
	String[]		Type;				// Tipo de central hidraulica: E, S, P o R	
	
	//Atributos funcion de costo variable en el tiempo
	double[][]		Pmin;				// Pmin[iv][t] potencia minima para tramo de funcion de costo
	double[][]		Pmax;				// Pmax[iv][t] potencia maxima para tramo de funcion de costo
	double[][]		Alfa;				// Alfa[iv][t] pendiende 		
	double[][] 		Beta;			    // Beta[iv][t] pendiende 		
	double[][]		Pminuc;				// Pminuc[iv][t] potencia minima para tramo de funcion de costo
	double[][]		Pmaxuc;				// Pmaxuc[iv][t] potencia maxima para tramo de funcion de costo
	double[][]		Alfauc;				// Alfauc[iv][t] pendiende 		
	double[][] 		Betauc;			    // Betauc[iv][t] pendiende 		
	int[]			Ns;					// Ns[t] numero de tramos de la funcion de costo para cada periodo
	int[]			Niv;				// Niv[t] numero de variables binarias por periodo
	int[][][]	M;				 		// M[t][iv][0 o 1] matriz que contiene tramo inicial y final de la f. de costo asociada a cada variable binaria
	int             Niv_max;            // maximo de tramos por periodos entre t=1 y T								 
	int             Niv_max2;           // maximo de variables para la potencia entre t=1 y T		
	
	//antiguos
	int        	    Bgi;      	  		// Bgi condicion inicial para Bg	
	//datos de entrada, algunos se puede calcular
	
	int[]      		H;           	// etapas de evaluacion
	int        		T;           	// numero de etapas=largo de H[]
	int[]   		Arra;          	// matriz que dice para cada t, cuantos tramos de rampa arranque se pueden activar  
	int[]  	     	Par;
	int   			n_ia_max;		// numero de maximo de escalones de rampa arranque, depende de H[]
	int   			n_ip_max;		// numero de maximo de escalones de rampa parada,   depende de H[]
	int[][] 		matriz_ia;      // matriz que contiene un 1 si escalon ia de de rampa de parada esta presente  
	int  			n_arraq;		// numero total de etapas de arranque que intervienen, para definir largo de vector restriccion
	int  			n_par;			// numero total de etapas de parada  que intervienen, para definir largo de vector restriccion
	int   			EtpArrIni;      // EtpArrIni etapa a partir de la cual puede haber rampa de arranque
	int		   		EtpParIni;		// EtpParIni etapa a partir de la cual puede haber rampa de parada
	int[]	 		NetpA;          // NetpA[t]  numero de escalones de arranque "hacia atras" para cada t
	int[]	 		NetpP;          // NetpP[t]  numero de escalones de arranque "hacia adelante" para cada t
	double[][]		PaFx;      		// PaFx[ia][t] potencia asignada a cada escalon de la rampa de arranque  
	double[][]		PpFx;			// PpFx[ia][t] potencia asignada a cada escalon de la rampa de parada  
	int[] 			EtpOn;         	// EtpOn[t] etapas durate las cuales la maquina tiene que estar encendida, depende de t si las etapas son de ancho variable          
	int[] 			EtpOff;       	// EtpOff[t] etapas durate las cuales la maquina tiene que estar apagada, depende de t si las etapas son de ancho variable 
	int 			EtpOnFin;		// EtpOnFin etapa hasta la cual no se puede exigir a la maquina un tiempo Toff
	int 			EtpOffFin;		// EtpOffFin etapa hasta la cual no se puede exigir a la maquina un tiempo Toff
	int 			EtpOnIni;		// EtpOnIni etapa a partir de la cual aplico condicion Ton, depende de condiciones iniciales
	int 			EtpOffIni;		// EtpOffIni etapa a partir de la cual aplico condicion Toff, depende de condiciones iniciales 
	int 			EtpArrBg;		// EtpArrBg etapa hasta la cual Bg=0
	int 			EtpArrBgEq1;	// EtpArrBg etapa hasta la cual Bg=1               
	int 			Tfs;
	int 			n_is;			// numero de tramos para costos de partida
	int				n_iv;			// numero de tramos maximos de la funcion de costos
	int[]           NARR;           // ARRA[t] numero de escalones de la rampa de arranque en t
	int[]           NPAR;            // PAR[t] numero de escalones de la rampa de parada en t
	double          UnitT;          // Unidad de Tiempo
	double 			set_bh1[][];	
	double 			set_bgh1[];	
	
	
	//costos de partida
	double[] CPartida;	      //contiene costo de partida hot, warm y cold	
	int[] TPartida;			  //tiempo hot,warm
	int[] IPartida;           //tiempo a partir del cual se aplica restriccion	
	int[][] NCP;			  //vector que indica periodos "hacia atras" que se debe considerar 
	int[] Bgni;       		  //condicion inicial para costo de partida
	
	//
	double[][] sospesos;
	//factor de escala
	private double fes;
	private double fcon;
	
	//variable auxiliar
	private double max_aux;
	
	
	//constructor
	public HGUC(){
	}
	
	public void InitUC(int[] ID,String[] Nombre,int[] TminOn,int[] TminOff, double[] PiniN, int[] TiniN, int[] GradRA,int[] GradRP, double[] GradS, double[] Pming, double[] Pmaxg, double[] CespMeRA,double[] CespMeRP, double[] PoderCal, double[] OwnConsuption,int[] Barra,int[] Unavalaible,double[] ForcedGenerator, double[] Pcomb, double[][] Pmin,double[][] Pmax, double[] Rend, double[] Qvmin, double[] Qvmax, int[] Ctur, int[] Cver, double[] SpinningMax, double[] SpinningMax2,double[] CPFMax, int[] Reserva_Pronta, int[] Commitment, double[] StopCost, double[] Aflu, String[] Type){
		//atributos
		this.ID					= ID;
		this.Nombre     		= Nombre;
		this.TminOn   			= TminOn;
		this.TminOff  			= TminOff;
		this.PiniN       		= PiniN;
		this.TiniN	  			= TiniN;   
		this.GradRA 			= GradRA;
		this.GradRP 			= GradRP;     
		this.GradS 				= GradS;
		this.Pming    			= Pming;       
		this.Pmaxg    			= Pmaxg;
		this.CespMeRA 			= CespMeRA;   
		this.CespMeRP 			= CespMeRP;  
		this.PoderCal 			= PoderCal;   
		this.OwnConsuption		= OwnConsuption;
		this.Barra    			= Barra;
		this.Unavalaible		= Unavalaible;
		this.ForcedGenerator	= ForcedGenerator;
		this.Pcomb				= Pcomb;
		this.Pmin     			= Pmin;        
		this.Pmax     			= Pmax;  
		this.Rend				= Rend; 							
		this.Qvmin				= Qvmin;							
		this.Qvmax				= Qvmax;							
		this.Ctur				= Ctur;							
		this.Cver				= Cver;
		this.SpinningMax		= SpinningMax;
		this.SpinningMax2		= SpinningMax2; 
		this.CPFMax				= CPFMax;
		this.Reserva_Pronta		= Reserva_Pronta;
		this.Commitment			= Commitment;
		this.StopCost			= StopCost;
		this.FactorAcoplaTVTG	= FactorAcoplaTVTG;
		this.IsERNC				= IsERNC;
		this.Aflu				= Aflu;	
		this.Type				= Type;
	
	}
	
	//se cargan datos auxiliares
	public void InitDataUC(int EtpArrIni,int EtpParIni,int[] NetpA,int[][] matriz_ia,int[] NetpP,int[]EtpOn,int[]EtpOff,int EtpOnFin,int EtpOffFin,int EtpOnIni,int EtpOffIni,int[] Arra,int[] Par,int NARR[],int NPAR[],int n_ia_max,int n_ip_max,int n_arraq,int n_par, double[][] PaFx,double[][] PpFx,int EtpArrBg,int EtpArrBgEq1,int Tfs,int Bgi,double[][] Pminuc,double[][] Pmaxuc,double[][] Alfauc,double[][] Betauc,int[] Niv,int[][][] M, int n_iv, double[] CPartida, int[] TPartida, int[] IPartida, int[][] NCP, int[] Bgni, double UnitT, double[][] set_bh1,double[] set_bgh1, double fes, double fcon){
    
    	this.n_ia_max 	=n_ia_max;    
		this.n_ip_max 	=n_ip_max;
		this.matriz_ia	=matriz_ia;
		   
		//condiciones iniciales y de borde
		this.EtpArrIni	=EtpArrIni; 
    	this.EtpParIni	=EtpParIni; 
    	this.NetpA    	=NetpA;         
    	this.NetpP    	=NetpP;
    	this.EtpOn    	=EtpOn;     
		this.EtpOff	  	=EtpOff;    
		this.EtpOnFin 	=EtpOnFin;  
		this.EtpOffFin	=EtpOffFin; 
		this.EtpOnIni 	=EtpOnIni;  
		this.EtpOffIni	=EtpOffIni; 
		this.Arra     	=Arra;
		this.Par      	=Par;      
		this.PaFx	  	=PaFx;           
    	this.PpFx	  	=PpFx;           
		this.n_arraq  	=n_arraq;
		this.n_par    	=n_par;
		this.EtpArrBg 	=EtpArrBg;
		this.EtpArrBgEq1=EtpArrBgEq1;
		this.Tfs	  	=Tfs;
		this.Bgi	  	=Bgi;
		this.Bgni       =Bgni;
		//rampa de arranque y parada
		this.NARR       =NARR;
		this.NPAR       =NPAR;
		//funcion de costo
		this.Pminuc		=Pminuc;
		this.Pmaxuc		=Pmaxuc;
		this.Alfauc		=Alfauc;
		this.Betauc		=Betauc;			    		
		this.Niv		=Niv;				
		this.M			=M;
		this.n_iv		=n_iv;
		//costo de partida
		this.CPartida = CPartida;	      //contiene costo de partida hot, warm y cold	
		this.TPartida = TPartida;		  //tiempo hot,warm
		this.IPartida = IPartida;         //tiempo a partir del cual se aplica restriccion	
		this.NCP 	  = NCP;			  //vector que indica periodos "hacia atras" que se debe considerar 
		this.Bgni	  = Bgni;             //condicion inicial para costo de partida	
		this.UnitT	  = UnitT;
		
		//maximo de variables binarias
		Niv_max=0;
		
		int ncc=0;
		for(int t=0;t<Niv.length;t++){
			ncc=0;
			for(int iv=0;iv<Niv[t];iv++){
				//cuento cuantos tramos convexos hay en iv
				for(int ic=M[t][iv][0];ic<=M[t][iv][1];ic++){
					ncc++;
				}
			}
			//numero de variables para representar la potencia
			if(ncc>Niv_max2){
				Niv_max2=ncc;
			}
			//numero de variables binarias B
			if(Niv[t]>Niv_max){
				Niv_max=Niv[t];
			}
		}		
		
		this.set_bh1=set_bh1;
		this.set_bgh1=set_bgh1;
		
		this.fes 	= fes;
		this.fcon	= fcon;
	}
	
	//Inicializacion de variables
	public void InitVariables(int[] H,int lp, IloCplex cplex){
		
		
		this.H       	=H;       
		T=H.length;
		
		//cotas variables
		double[] lb		= new double[T];
    	double[] ub		= new double[T];
    	double[] lbint	= new double[T];
    	double[] ubint	= new double[T];
    	double[] lb_bool= new double[T];
    	double[] ub_bool= new double[T];
    	int[] lb_int	= new int[T];
    	int[] ub_int	= new int[T];
    	double[] lb_iv		= new double[Niv_max];
    	double[] ub_iv		= new double[Niv_max];
		double[] lb_aux		= new double[T];
    	
    	//nombre variables
    	String[][] 		nombreP      = new String[Niv_max][T];
	    String[][]  	nombrePa     = new String[n_ia_max][T];
	    String[][]  	nombrePp     = new String[n_ip_max][T];
	    String[]    	nombrePt     = new String[T];
	    String[]    	nombreRg     = new String[T];
	    String[]    	nombreRg2    = new String[T];
		String[][]  	nombreB      = new String[T][Niv_max];
	    String[][]  	nombreq      = new String[Niv_max][T];
	    String[][]  	nombreqa     = new String[n_ia_max][T];
	    String[][]  	nombreqp     = new String[n_ip_max][T];
	    String[]    	nombreBg     = new String[T];
	    String[][]  	nombreBarr   = new String[n_ia_max][T];
	    String[][]  	nombreBpar   = new String[n_ip_max][T];
	    String[]    	nombreBga    = new String[T];
	    String[]    	nombreBgp    = new String[T];
	    String[]		nombreBs     = new String[T];
	    String[]      	nombreBc     = new String[T];
	    String[]      	nombreBsp    = new String[T];
	    String[]      	nombreBsa    = new String[T];
	    String[][]    	nombreC      = new String[Niv_max][T];
	    String[]      	nombreC_AP_1 = new String[T];
	    String[]      	nombreC_AP_2 = new String[T];
	    String[] 		nombreR		 = new String[T];
	    String[][]    	nombreBsu    = new String[n_is][T];
	    String[][]    	nombreBx     = new String[n_is][T];
	    String[][]    	nombreBh     = new String[n_is][T];
    	String[]        nombreV      = new String[T];
		String[]		nombreQver	 = new String[T];
		String[]        nombreVfic   = new String[T];
    	String[]        nombreVfic2   = new String[T];
		
		String[]    	nombreX    = new String[T];
	    String[]    	nombreY    = new String[T];
		
		
    	sospesos  = new double[T][Niv_max];
    			
    	for(int t=0;t<T;t++){
        	lb[t]=0;
			lb_aux[t]=-Double.MAX_VALUE;
        	ub[t]=Double.MAX_VALUE;
        	lbint[t]=0;
        	ubint[t]=1;	
    		lb_int[t]=0;
        	ub_int[t]=Integer.MAX_VALUE;	
    		lb_bool[t]=0;
        	ub_bool[t]=1;
    	}
    	for(int iv=0;iv<Niv_max;iv++){
    		lb_iv[iv]=0;
        	ub_iv[iv]=1;
    	}
    
	    for(int iv=0;iv<Niv_max;iv++){
	    	for(int t=0;t<T;t++){
		    	nombreP[iv][t]   ="Ph" +"(" + ID[t] + ","+(iv+1)+","+(t+1)+")";
		    	nombreB[t][iv]   ="Bh" +"(" + ID[t] + ","+(t+1)+","+(iv+1)+")";
		    	nombreq[iv][t]   ="q" +"(" + ID[t] + ","+(iv+1)+","+(t+1)+")";
				nombreC[iv][t]   ="Costo"+"(" + ID[t] +","+(iv+1)+","+(t+1)+")";
			}
	    }
    	for(int t=0;t<T;t++){
    		for(int iv=0;iv<Niv_max;iv++){
    			sospesos[t][iv]	 = iv;
    		}
    	}
    	
    	for(int ia=0;ia<n_ia_max;ia++){
    		for(int t=0;t<T;t++){
	    		nombrePa[ia][t]     ="Pa"+"(" + ID[t] + ","+(ia+1)+","+(t+1)+")";	
	    		nombreBarr[ia][t]   ="Barr"+"(" + ID[t] + ","+(ia+1)+","+(t+1)+")";
	            nombreqa[ia][t]     ="qa"+"(" + ID[t] + ","+(ia+1)+","+(t+1)+")";
	    	}
	    }  
    	
    	for(int ip=0;ip<n_ip_max;ip++){
			for(int t=0;t<T;t++){
	    		nombrePp[ip][t]     ="Pp"+"(" + ID[t] + ","+(ip+1)+","+(t+1)+")";		
	    		nombreBpar[ip][t]   ="Bpar"+"(" + ID[t] + ","+(ip+1)+","+(t+1)+")";
	            nombreqp[ip][t]     ="qp"+"(" + ID[t] + ","+(ip+1)+","+(t+1)+")";
	    	}
	    }
    	
    	for(int t=0;t<T;t++){
			nombreBg[t]   ="Bgh" +"(" + ID[t] +","+(t+1)+")";
			nombreBga[t]  ="Bgah"+"(" + ID[t] +","+(t+1)+")";
			nombreBgp[t]  ="Bgph"+"(" + ID[t] +","+(t+1)+")";
			nombreRg[t]   ="Rgh" +"(" + ID[t] +","+(t+1)+")";
			nombreRg2[t]  ="Rg2h" +"(" + ID[t] +","+(t+1)+")";
			nombrePt[t]   ="Pth" +"(" + ID[t] +","+(t+1)+")";
	    	nombreV[t]    ="V" +"(" + ID[t] +","+(t+1)+")"; 	
			nombreX[t]    ="Xh" +"(" + ID[t] +","+(t+1)+")";
			nombreY[t]    ="Yh"+"(" + ID[t] +","+(t+1)+")";
			nombreQver[t] ="Qver"+"(" + ID[t] +","+(t+1)+")";
			nombreVfic[t] ="Vfic" +"(" + ID[t] +","+(t+1)+")";
			nombreVfic2[t]="Vfic2" +"(" + ID[t] +","+(t+1)+")";
		
		}
    
	    for(int t=0;t<T;t++){
		    nombreBs[t]    ="Bs" +"(" + ID[t] +","+(t+1)+")";
		    nombreBsp[t]   ="Bsp" +"(" + ID[t] +","+(t+1)+")";
		    nombreBsa[t]   ="Bsa" +"(" + ID[t] +","+(t+1)+")";	
		    nombreBc[t]    ="Bc" +"(" + ID[t] +","+(t+1)+")";
		    nombreC_AP_1[t]="CPartida1" +"(" + ID[t] +","+(t+1)+")";
		    nombreC_AP_2[t]="Costo_AP_VNC2" +"(" + ID[t] +","+(t+1)+")";
	    }
    
	    for(int is=0;is<n_is;is++){
	    	for(int t=0;t<T;t++){
		    	nombreBsu[is][t]   ="Bsu" +"(" + ID[t] +","+(is+1)+","+(t+1)+")";
		    	nombreBx[is][t]    ="Bx" +"(" + ID[t] +","+(is+1)+","+(t+1)+")";
		    	nombreBh[is][t]    ="Bh" +"(" + ID[t] +","+(is+1)+","+(t+1)+")";
	    	}
	    }
	    
	    
	    //variables continuas
		P      			= new IloNumVar[Niv_max][T];	
		Pt      		= new IloNumVar[T];
		Rg              = new IloNumVar[T];
		Rg2             = new IloNumVar[T];
		Costo_AP_VNC1	= new IloNumVar[T];
		Costo_AP_VNC2	= new IloNumVar[T];
		q      			= new IloNumVar[Niv_max][T];
		costo			= new IloNumVar[Niv_max][T];
		V				= new IloNumVar[T];
		Qver			= new IloNumVar[T];
		Vfic			= new IloNumVar[T];
		Vfic2			= new IloNumVar[T];
		
		if(GradRA[0]>0 || GradRP[0]>0){
			Pa     			= new IloNumVar[n_ia_max][T];	
			Pp     			= new IloNumVar[n_ip_max][T];		
			qa     			= new IloNumVar[n_ia_max][T];
			qp     			= new IloNumVar[n_ip_max][T];
		}
	    
		// si defino algunas variables binarias como continuas 
		Bc     			= new IloNumVar[T];
		Bx     			= new IloNumVar[n_is][T];
		Bh     			= new IloNumVar[n_is][T];
		B      			= new IloNumVar[T][Niv_max];
		Bsu    			= new IloIntVar[n_is][T];
		Bg     			= new IloNumVar[T];
		//caso unidad con rampa de arranque
		if(GradRA[0]>0 || GradRP[0]>0){
			Bs     			= new IloNumVar[T];
			Bsp	   			= new IloNumVar[T];
			Bsa	   			= new IloNumVar[T];
			Barr   			= new IloNumVar[n_ia_max][T];
			Bpar   			= new IloNumVar[n_ip_max][T];
		}
		if(GradRA[0]>0 || GradRP[0]>0){
			Bga    			= new IloNumVar[T];
			Bgp    			= new IloNumVar[T];
		}
		
		X    			= new IloNumVar[T];
		Y    			= new IloNumVar[T];
		
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		try{
			if(lp==1){
				
				
				for(int iv=0;iv<Niv_max2;iv++){
		    		P[iv]   =cplex.numVarArray(T,lb_aux,ub,nombreP[iv]);
				}
				for(int iv=0;iv<Niv_max;iv++){
    				q[iv]   =cplex.numVarArray(T,lb,ub,nombreq[iv]);
					costo[iv]   =cplex.numVarArray(T,lb,ub,nombreC[iv]);
				}
 
    	    	Bga     =cplex.numVarArray(T,lbint,ubint,nombreBga);
    	    	Bgp     =cplex.numVarArray(T,lbint,ubint,nombreBgp);
    	   		
    	    	for(int t=0;t<T;t++){
					Bg[t]  	=cplex.numVar(0,1,nombreBg[t]);
					B[t]   	=cplex.numVarArray(Niv_max,lb_iv,ub_iv,nombreB[t]);
					Rg[t]   =cplex.numVar(0,Double.MAX_VALUE,nombreRg[t]);
					Rg2[t]  =cplex.numVar(0,Double.MAX_VALUE,nombreRg2[t]);
					
					//para centrales con vertimiento minimo distinto de cero se agrega variable de holgura Vfic2 -> Qver puede ser menor que vertimiento minimo
					//version original
					//Qver[t] =cplex.numVar(Qvmin[t],Qvmax[t],nombreQver[t]);
					//version modificada
					Qver[t] =cplex.numVar(0,Qvmax[t],nombreQver[t]);
					
					
					//version original, modificacion hecha para acotar volumenes ficticios
					//Vfic[t]   = cplex.numVar(0,Double.MAX_VALUE,nombreVfic[t]);
					if(Rend[t]>0){
						max_aux=Math.max(fcon*H[t]*(Pming[t]/Rend[t]-Aflu[t]),0);
						
						if(Commitment[t]==0 & Pming[t]>0)
						Vfic[t]   = cplex.numVar(0,max_aux,nombreVfic[t]);
						
						if(Qvmin[t]>0)
						Vfic2[t]  = cplex.numVar(0,fcon*H[t]*Qvmin[t],nombreVfic2[t]);
					
					}
					else{
						System.out.println("WARNING: Rendimiento menor que 0. Ingresar valor mayor que cero, central: "+Nombre[t]);
					}
				
				
				}
    	    	
    			Pt = cplex.numVarArray(T,lb,ub,nombrePt);
				
				if(GradRA[0]>0){
    				for(int ia=0;ia<n_ia_max;ia++){
    					Barr[ia]  =cplex.numVarArray(T,lb,ub,nombreBarr[ia]);
		    			Pa[ia]    =cplex.numVarArray(T,lb,ub,nombrePa[ia]);
		    			qa[ia]    =cplex.numVarArray(T,lb,ub,nombreqa[ia]); 
		    		}
		    	}	
    			if(GradRP[0]>0){
    				for(int ip=0;ip<n_ip_max;ip++){
    					Bpar[ip]  =cplex.numVarArray(T,lb,ub,nombreBpar[ip]);
    					Pp[ip]    =cplex.numVarArray(T,lb,ub,nombrePp[ip]);
		    			qp[ip]    =cplex.numVarArray(T,lb,ub,nombreqp[ip]); 
		    		}
    			}
    		
    			if(GradRA[0]>0 & GradRP[0]>0 ){
    				Bs 			 =cplex.numVarArray(T,lb,ub,nombreBs);
    				Bsa			 =cplex.numVarArray(T,lb,ub,nombreBsa);
    				Bsp			 =cplex.numVarArray(T,lb,ub,nombreBsp);
    			}
    			
    			Bc =cplex.numVarArray(T,lb,ub,nombreBc);
    			Costo_AP_VNC1=cplex.numVarArray(T,lb,ub,nombreC_AP_1);
				Costo_AP_VNC2=cplex.numVarArray(T,lb,ub,nombreC_AP_2);
			
				for(int is=0;is<n_is;is++){
					Bsu[is]=cplex.boolVarArray(T,nombreBsu[is]);
					Bx[is]=cplex.numVarArray(T,lb,ub,nombreBx[is]);
					Bh[is]=cplex.numVarArray(T,lb,ub,nombreBh[is]);
				}
				
				X     =cplex.numVarArray(T,lbint,ubint,nombreX);
    	    	Y     =cplex.numVarArray(T,lbint,ubint,nombreY);
			
    		}
		}
		catch (IloException e) {
			System.err.println("Concert Definicion de variables '" + e + "' caught");
		}
		
	}
	
	///////////////////////////////////Restricciones/////////////////////////////////////////////////////////////////////////////
	
	//////////////////////////////////Restriccion Pmin////////////////////////////////////////////////////////////////////////////////////////////
    	public void restrpmin(IloCplex cplex, int t){
	    	
	    	//System.out.println("restriccion pmin");
			try{
				P[0][t].setLB(0);
			}		
			catch (IloException e) {
				System.err.println("Concert exception: Restriccion potencia minima '" + e + "' caught");
			}
		}
	//////////////////////////////////Restriccion Pmax///////////////////////////////////////////////////////////////////////////////////////////
		public void restrpmax(IloCplex cplex, int t){
	    	
	    	//System.out.println("restriccion pmax");
			try{
				P[0][t].setUB(Pmaxg[t]-Pming[t]);
			}
			catch (IloException e) {
				System.err.println("Concert exception: Restriccion potencia maxima '" + e + "' caught");
			}
		}
	//////////////////////////////////Restriccion Tminon///////////////////////////////////////////////////////////////////////////////////////////
		public void restrtmon(IloCplex cplex, int t){
			
			//System.out.println("restriccion tminon");
			try{
				if(TminOn[0]>1){			
					IloNumExpr sum_bg = cplex.constant(0);
					double sum_t=0;
					if(EtpOnIni-1<=t & t<T-EtpOnFin){
						sum_bg=cplex.constant(0);
						sum_t=0; 	
		 				for(int ta=t;ta<t+EtpOn[t];ta++){
		 					sum_bg=cplex.sum(sum_bg,cplex.prod(H[ta],B[ta][0]));
		 					sum_t=sum_t+H[ta];
		 				}
		 				if(t==0){
		 					cplex.addGe(cplex.sum(sum_bg,cplex.prod(-1*sum_t,B[t][0])),-sum_t*Bgi,"Ton_1_"+ID[t]+"_"+(t+1));		  	
			 			}	
		 				else{
			 				cplex.addGe(cplex.sum(sum_bg,cplex.prod(-1*sum_t,B[t][0]),cplex.prod(sum_t,B[t-1][0])),0,"Tonh_1_"+ID[t]+"_"+(t+1));		 
			 			}
					} 	
			 		if(T-EtpOnFin<=t & t<T){
			 			sum_bg=cplex.constant(0); 
			 			sum_t=0;
			 			for(int ta=t;ta<T;ta++){
		 					sum_bg=cplex.sum(sum_bg,cplex.prod(-H[ta],B[ta][0]));	
		 					sum_t=sum_t+H[ta];
		 				}
		 				if(t==0){
		 					cplex.addLe(cplex.sum(cplex.prod(sum_t,B[t][0]),sum_bg),sum_t*Bgi,"Ton_2_"+ID[t]+"_"+(t+1));		 
			 			}
			 			else{
			 				cplex.addLe(cplex.sum(cplex.prod((T-t),B[t][0]),cplex.prod(-1*sum_t,B[t-1][0]),sum_bg),0,"Tonh_2_"+ID[t]+"_"+(t+1));		 
			 			}	
					}
				}
			}		
			catch (IloException e) {
				System.err.println("Concert exception: Tiempo minimo operacion '" + e + "' caught");
			}
		}
				
	//////////////////////////////////Restriccion Tminoff///////////////////////////////////////////////////////////////////////////////////////////
		public void restrtmof(IloCplex cplex, int t){
			
			//System.out.println("restriccion tminoff");
			try{
				IloNumExpr sum_bg = cplex.constant(0);
				double sum_h=0;				
				if(TminOff[0]>1){	
					if(EtpOffIni-1<=t & t<T-EtpOffFin){
						sum_bg = cplex.constant(0);
						sum_h=0;
						for(int ta=t;ta<t+EtpOff[t];ta++){
							sum_bg=cplex.sum(sum_bg,cplex.prod(H[ta],B[ta][0]));	
							sum_h=sum_h+H[ta];
						}
						if(t==0){
	 						cplex.addLe(cplex.sum(sum_bg,cplex.prod(-1*sum_h,B[t][0])),sum_h-sum_h*Bgi,"Toffh_1_"+ID[t]+"_"+(t+1));		 
	 					}
						else{
	 						cplex.addLe(cplex.sum(sum_bg,cplex.prod(-sum_h,B[t][0]),cplex.prod(sum_h,B[t-1][0])),sum_h,"Toffh_1_"+ID[t]+"_"+(t+1));		 
	 					}
					}
	     			if(T-EtpOffFin<=t & t<T){
	     				sum_bg = cplex.constant(0);
						sum_h=0;
	 					for(int ta=t;ta<T;ta++){
							sum_bg=cplex.sum(sum_bg,cplex.prod(H[ta],B[ta][0]));	
							sum_h=sum_h+H[ta];
						}
	    	 			cplex.addLe(cplex.sum(cplex.prod(sum_h,B[t-1][0]),cplex.prod(-1*sum_h,B[t][0]),sum_bg),sum_h,"Toffh_2_"+ID[t]+"_"+(t+1));		 
	    	 		}		
				}
			}
			catch (IloException e) {
				System.err.println("Concert exception: Tiempo minimo fuera servicio '" + e + "' caught");
			}
		}
	
	/////////////////////////////////////Restriccion definicion Bg//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////Restriccion Costo de Partida///////////////////////////////////////////////////////////////////////////////////////////////////////////
		public void restrdebg(IloCplex cplex, int t){
		
			//System.out.println("Restriccion definicion de Bg");
			try{
				IloNumExpr sumbg= cplex.constant(0);	
				sumbg= cplex.constant(0);
				for(int iv=0;iv<Niv[t];iv++){
					if(set_bgh1[t] !=1 || set_bh1[iv][t] !=0){
						sumbg= cplex.sum(B[t][iv],sumbg);
					}
				}
				//cplex.addEq(cplex.sum(sumbg,cplex.prod(-1,Bg[t])),0,"Bg_"+ID[t]+"_"+(t+1));
			}
			catch (IloException e) {
				System.err.println("Concert exception: Bg '" + e + "' caught");
			}
		
		}
	
	/////////////////////////////////////Restriccion logica Bga Bgp//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////Restriccion Costo de Partida///////////////////////////////////////////////////////////////////////////////////////////////////////////
		public void restrbgap(IloCplex cplex, int t){
			
			//System.out.println("Restriccion condiciones logicas Bga y Bgp");
			try{
				//Bga, Bgp solo para unidades con rampa de arranque y parada
				if(GradRP[0]>0 || GradRA[0]>0 || StopCost[0]>0){
					cplex.addLe(cplex.sum(Bga[t],cplex.prod(-1,B[t][0])),0,"CondBgah_"+ID[t]+"_"+(t+1));
					cplex.addLe(cplex.sum(Bgp[t],B[t][0]),1,"CondBgph_"+ID[t]+"_"+(t+1));
					if(t==0){
						cplex.addEq(cplex.sum(B[t][0],Bgp[t],cplex.prod(-1,Bga[t])),Bgi,"CondBaBpBg1h_"+ID[t]+"_"+(t+1)); 
					}	
					else{
						cplex.addEq(cplex.sum(cplex.sum(B[t][0],cplex.prod(-1,B[t-1][0])),Bgp[t],cplex.prod(-1,Bga[t])),0,"CondBaBpBg2h_"+ID[t]+"_"+(t+1));//CondBaBpBg2
					}	
				}
			}			
			catch(IloException e){
				System.err.println("Concert exception Condiciones logicas Bga Bgp " + e + "' caught");
    		}
    	}
    	
    //////////////////////////////////////Restriccion definicion Barr//////////////////////////////////////////////////////////////////////////////////////////
		public void restrbarr(IloCplex cplex, int t){
			
			//System.out.println("Restriccion definicion Barr");
			try{
				if(GradRA[0]>0){
    				if(EtpArrIni-1<=t & t<T){
     					int ia=0;
     					while(ia < NetpA[t] & (t-ia-1)>=0){
     						cplex.addEq(cplex.sum(Barr[ia][t-ia-1],cplex.prod(-1,Bga[t])),0,"RamArrb1h_"+ID[t] +"_"+(ia+1)+"_"+(t+1));
     						ia++;
     					}
    	 			}
    	 		}	
    	 	}
    	 	catch(IloException e){
				System.err.println("Concert exception definicion Barr " + e + "' caught");
    		}
    	 }
    
    //////////////////////////////////////Restriccion definicion Bpar//////////////////////////////////////////////////////////////////////////////////////////
    	
    	public void restrbpar(IloCplex cplex, int t){
		 		
			//System.out.println("Restriccion definicion Barr");
			try{	
				if(EtpParIni-1<=t & t<T){
     				for(int ip=0;ip<NetpP[t];ip++){
     					if(t<T-ip){
     						cplex.addEq(cplex.sum(Bpar[ip][t+ip],cplex.prod(-1,Bgp[t])),0,"RamParb1h_"+ID[t]+"_"+(ip+1)+"_"+(t+1));
    	 				}
    	 			}
    	 		}
			}
			catch(IloException e){
				System.err.println("Concert exception definicion Bpar " + e + "' caught");
    		}
    	}
    
    //////////////////////////////////////Restriccion modo operacion//////////////////////////////////////////////////////////////////////////////////////////
    	
    	public void restrmodo(IloCplex cplex, int t){
		 		
			//System.out.println("Restriccion modo operacion");
			//la central puede estar en arranque o parada o operacion
			try{	
				IloNumExpr sum_arr;
				IloNumExpr sum_par;
				sum_arr=cplex.constant(0);
				sum_par=cplex.constant(0);
				
				if(GradRA[0]>0){
					if(t>=EtpArrIni-NetpA[EtpArrIni-1]-1){ //antes no estaba esta condicion
						for(int ia=0;ia<n_ia_max;ia++){
							if(matriz_ia[ia][t]!=0){
								sum_arr=cplex.sum(sum_arr,Barr[ia][t]);						
							}
						}
					}
				}
				if(GradRP[0]>0){
					for(int ip=0;ip<Par[t];ip++){
						sum_par=cplex.sum(sum_par,Bpar[ip][t]);	
					}
				}
				
				if(GradRA[t]>0 || GradRP[t]>0){
					cplex.addLe(cplex.sum(sum_arr,sum_par,Bg[t]),1,"SoloArrOpNPar1h_"+ID[t]+"_"+(t+1));
				}
				
				
			}
			catch(IloException e){
				System.err.println("Concert exception modo de operacion " + e + "' caught");
    		}		
    	}
    
    //////////////////////////////////////Restriccion condicion inicial//////////////////////////////////////////////////////////////////////////////////////////
    	public void restrinic(IloCplex cplex, int t){

			//System.out.println("restriccion condicion inicial");
			try{
				if(t<EtpArrBg-1){
					//Bg[t].setLB(0);
					//Bg[t].setUB(0);
					for(int iv=0;iv<Niv[t];iv++){
						B[t][iv].setLB(0);
						B[t][iv].setUB(0);
					}
				}
				else if(t<EtpArrBgEq1){
					B[t][0].setLB(1);
					B[t][0].setUB(1);
				}		
			}
			catch(IloException e){
				System.err.println("Concert exception Condicion inicial'" + e + "' caught");
			}
    	
    	}
    
    //////////////////////////////////////Restriccion generacion forzada//////////////////////////////////////////////////////////////////////////////////////////
   		public void restrforz(IloCplex cplex, int t){

   			//System.out.println("restriccion condicion inicial");
    		try{	
    			//si central esta indisponible
				if(Pming[t]==0 & Pming[t]==Pmaxg[t]){
					for(int iv=0;iv<Niv[t];iv++){
						B[t][iv].setLB(0);
						B[t][iv].setUB(0);
					}
					//Bg[t].setLB(0);
					//Bg[t].setUB(0);
				}
				//si hay generacion forzada, la potencia minima es igual a la potencia maxima, por lo tanto, Bg=1
				if(Pming[t]!=0 & Pming[t]==Pmaxg[t] & ForcedGenerator[t]>0){
					Bg[t].setLB(1);
					Bg[t].setUB(1);
					B[t][0].setLB(0);
					B[t][0].setUB(0);
				}
			}
    		catch(IloException e){
				System.err.println("Concert exception Generacion forzada e indisponibilidad'" + e + "' caught");
    		}
   		}
   	//////////////////////////////////////Restriccion Bga=0//////////////////////////////////////////////////////////////////////////////////////////
   		public void restrsbga(IloCplex cplex, int t){

   			try{
				if(GradRP[0]>0 || GradRA[0]>0){
					Bga[t].setLB(0);
					Bga[t].setUB(0);
				}			
			}
			catch(IloException e){
				System.err.println("Concert exception Bga=1 " + e + "' caught");
    		}
   		}
   	//////////////////////////////////////Restriccion Bgp=0/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
   		public void restrsbgp(IloCplex cplex, int t){

   			try{
				if(GradRP[0]>0 || GradRA[0]>0){
					Bgp[t].setLB(0);
					Bgp[t].setUB(0);
				}			
			}
			catch(IloException e){
				System.err.println("Concert exception Bga=1 " + e + "' caught");
    		}
   		}
	//////////////////////////////////////Restriccion costo partida//////////////////////////////////////////////////////////////////////////////////////////
		
		public void restrcpar(IloCplex cplex, int t){
	    	
	    	//System.out.println("restriccion costo partida");
    		try{
			    IloNumExpr sum_bgn=cplex.constant(0);
				int sum_bgin=0;
	    		int tt=0;
	    		int k=0;
	    		int r=0;
				
				if(StopCost[t]==0){
					for(int p=0;p<CPartida.length;p++){
						//chequeo que tiempo sea mayor o igual al tiempo a partir del cual se puede aplicar restriccion
						//ademas NCP debe ser mayor que 0 para formular restriccion
						sum_bgn=cplex.constant(0);
						k=0;
						if(t>=IPartida[p] & CPartida[p]>0){
							while(k<NCP[p][t]){
								if((t-k-1)>=0){
									sum_bgn=cplex.sum(sum_bgn,cplex.prod(fes*CPartida[p],B[t-k-1][0]));
								}
								else{
									if(tt<=(Bgni.length-1)){//no sobrepaso tamano de Bgni
										sum_bgn=cplex.sum(sum_bgn,fes*CPartida[p]*Bgni[tt]);
										tt++;
									}
									else{ //si se sobrepasa se rellena con 1
										sum_bgn=cplex.sum(sum_bgn,fes*CPartida[p]);
										tt++;
									}
								}
								k++;
							}
							cplex.addGe(cplex.sum(Costo_AP_VNC1[t],cplex.prod(-fes*CPartida[p],B[t][0]),sum_bgn),0,"costo_partidah_"+ID[t]+"_"+(p+1)+"_"+(t+1));
						}
					}
				}
    		}	
    		catch (IloException e){
    			System.err.println("Concert exception costo partida" + e + "' caught");
    		}
		}
		
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		public void restrtmon2(IloCplex cplex, int t){
			
			try{
			//Ton alternativo
				if(t>=EtpOnIni-1 & t<T-EtpOnFin){
					for(int ta=t;ta<t+EtpOn[t];ta++){
						if(t==0){
							cplex.addGe(cplex.diff(B[ta][0],B[t][0]),-Bgi,"Tonv2_"+ID[t]+"_"+(ta+1)+"_"+(t+1)); 
						}
						else{
							cplex.addGe(cplex.sum(B[ta][0],cplex.diff(B[t-1][0],B[t][0])),0,"Tonv2h_"+ID[t]+"_"+(ta+1)+"_"+(t+1)); 
									
						}
					}
				}
				if(t>=T-EtpOnFin & t<T){
					for(int ta=t;ta<T;ta++){
						if(t==0){
							cplex.addGe(cplex.diff(B[ta][0],B[t][0]),-Bgi,"Tonv2h_"+ID[t]+"_"+(ta+1)+"_"+(t+1)); 
						}
						else{
							cplex.addGe(cplex.sum(B[ta][0],cplex.diff(B[t-1][0],B[t][0])),0,"Tonv2h_"+ID[t]+"_"+(ta+1)+"_"+(t+1)); 
						}
					}
				}
			}
			catch (IloException e) {
				System.err.println("Concert exception: Tiempo minimo operacion alternativo '" + e + "' caught");
			}
			
		}
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		public void restrtmof2(IloCplex cplex, int t){
			
			try{
			//Ton alternativo
				if(t>=EtpOffIni-1 & t<T-EtpOffFin){
					for(int ta=t;ta<t+EtpOff[t];ta++){
						if(t==0){
							cplex.addLe(cplex.diff(B[ta][0],B[t][0]),1-Bgi,"Toffv2h_"+ID[t]+"_"+(ta+1)+"_"+(t+1)); 
						}
						else{
							cplex.addLe(cplex.sum(B[ta][0],cplex.diff(B[t-1][0],B[t][0])),1,"Toffv2h_"+ID[t]+"_"+(ta+1)+"_"+(t+1)); 
						}
					}
				}
				if(t>=T-EtpOffFin & t<T){
					for(int ta=t;ta<T;ta++){
						if(t==0){
							cplex.addLe(cplex.diff(B[ta][0],B[t][0]),1-Bgi,"Toffv2h_"+ID[t]+"_"+(ta+1)+"_"+(t+1)); 
						}
						else{
							cplex.addLe(cplex.sum(B[ta][0],cplex.diff(B[t-1][0],B[t][0])),1,"Toffv2h_"+"_"+(ta+1)+ID[t]+"_"+(t+1)); 
						}
					}
				}
			 }
			 catch (IloException e) {
				System.err.println("Concert exception: Tiempo minimo fuera servicio alternativo '" + e + "' caught");
			}
			
		}
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//Restriccion reserva en giro maximo
		public void restrspinnmax(IloCplex cplex, int t){
			try{
				//System.out.println("restrspinnmax");
				if(Barra[t]>0){
					IloNumExpr p=cplex.constant(0);
					//fijo reserva maxima que puede aportar central
					Rg[t].setUB(SpinningMax[t]);
					
					// Planteo restriccion: Rg+Pt <= Pmaxg
					p	 =cplex.constant(0);
						//si la central esta indisponible no aporta reserva en giro
					if(Unavalaible[t]!=1){
						p=cplex.sum(cplex.prod(B[t][0],Pming[t]),p); 
						p=cplex.sum(P[0][t],p);
					}
					cplex.addLe(cplex.sum(Rg[t],p,cplex.prod(B[t][0],-Pmaxg[t])),0,"reserva_giro_maximah_"+ID[t]+"_"+(t+1));
					
					//para centrales de pasada se limita reserva a afluente
					if(Type[t].equals("P")){
						cplex.addLe(cplex.sum(Rg[t],p),Rend[t]*Aflu[t]-Rend[t]*Qvmin[t],"reserva_giro_maximah_aflu_"+ID[t]+"_"+(t+1));
					}
				}
			}	
			catch (IloException e){
				System.err.println("Concert exception reserva en giro maxima" + e + "' caught");
			}
		}
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//Restriccion reserva en giro maximo 2
		public void restrspinnmax2(IloCplex cplex, int t){
			try{
				//System.out.println("restrspinnmax2");
				IloNumExpr p=cplex.constant(0);
				
				//fijo reserva maxima que puede aportar central
				Rg2[t].setUB(SpinningMax2[t]);
				
				// Planteo restriccion: Rg+Pt <= Pmaxg
				p	 =cplex.constant(0);
					//si la central esta indisponible no aporta reserva en giro
				if(Barra[t]>0){
					if(Unavalaible[t]!=1){
						p=cplex.sum(cplex.prod(B[t][0],Pming[t]),p); 
						p=cplex.sum(P[0][t],p);
					}
					cplex.addLe(cplex.sum(Rg2[t],p,cplex.prod(B[t][0],-Pmaxg[t])),0,"reserva_giro_maxima_2h_"+ID[t]+"_"+(t+1));
					
					//para centrales de pasada se limita reserva a afluente
					if(Type[t].equals("P")){
						cplex.addLe(cplex.sum(Rg2[t],p),Rend[t]*(Aflu[t]-Qvmin[t]),"reserva_giro_maximah_aflu_"+ID[t]+"_"+(t+1));
					}
				}
			}	
			catch (IloException e){
				System.err.println("Concert exception reserva en giro maxima" + e + "' caught");
			}
		}
		//////////////////////////////////////Commitment/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
   		public void restrcommit(IloCplex cplex, int t){
			//System.out.println("restrcommit");
   			try{
				if(Commitment[t]==0 & Pmaxg[t]>0){
					B[t][0].setLB(1);
					B[t][0].setUB(1);
				}
			}
			catch(IloException e){
				System.err.println("Concert exception Commitment " + e + "' caught");
    		}
   		}
		
		//////////////////////////////////////Flujo de red /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
   		public void restrflujo(IloCplex cplex, int t){
			
			//System.out.println("restrflujo");
			
			int k=0;
			int cont_aux=0;
			
   			try{
				IloNumExpr sum_b=cplex.constant(0);
				
				
				//Primera restriccion
				sum_b=cplex.constant(0);
				cont_aux=0;
				if(t>0){
					sum_b=cplex.sum(X[t-1],sum_b);
					if(t-TminOn[t]>=0){
						sum_b=cplex.sum(sum_b, Bga[t-TminOn[t]]);
					}
					else{
						if(TiniN[t]>0 & TminOn[t]-t==TiniN[t]){
							sum_b=cplex.sum(cplex.constant(1),sum_b);
						}
					}
					sum_b=cplex.sum(sum_b,cplex.prod(-1,X[t]),cplex.prod(-1,Bgp[t]));
					cplex.addEq(sum_b,0,"flujo1h_"+ID[t]+"_"+(t+1));
				}
				//condicion inicial
				else{
					if(TiniN[t]>0 & TiniN[t] > TminOn[t]){
						sum_b=cplex.sum(cplex.constant(1),sum_b);
					}					
					if(t-TminOn[t]>=0){
						sum_b=cplex.sum(sum_b, Bga[t-TminOn[t]]);
					}
					else{
						if(TiniN[t]>0 & TminOn[t]-t==TiniN[t]){
							sum_b=cplex.sum(cplex.constant(1),sum_b);
						}
					}
					sum_b=cplex.sum(sum_b,cplex.prod(-1,X[t]),cplex.prod(-1,Bgp[t]));
					cplex.addEq(sum_b,0,"flujo1h_"+ID[t]+"_"+(t+1));
				}
				
				
				//Segunda restriccion
				sum_b=cplex.constant(0);
				cont_aux=0;
				if(t>0){
					sum_b=cplex.sum(Y[t-1],sum_b);
					if(t-TminOff[t]>=0){
						sum_b=cplex.sum(sum_b, Bgp[t-TminOff[t]]);
					}
					else{
						if(TiniN[t]<0 & TminOff[t]-t==-TiniN[t]){
							sum_b=cplex.sum(cplex.constant(1),sum_b);
						}
					}
					sum_b=cplex.sum(sum_b,cplex.prod(-1,Y[t]),cplex.prod(-1,Bga[t]));
					cplex.addEq(sum_b,0,"flujo2h_"+ID[t]+"_"+(t+1));
				
				}
				//condicion inicial
				else{
					if(TiniN[t]<0 & -TiniN[t] > TminOff[t]){
						sum_b=cplex.sum(cplex.constant(1),sum_b);
					}					
					if(t-TminOff[t]>=0){
						sum_b=cplex.sum(sum_b, Bgp[t-TminOff[t]]);
					}
					else{
						if(TiniN[t]<0 & TminOff[t]-t==-TiniN[t]){
							sum_b=cplex.sum(cplex.constant(1),sum_b);
						}
					}
					sum_b=cplex.sum(sum_b,cplex.prod(-1,Y[t]),cplex.prod(-1,Bga[t]));
					cplex.addEq(sum_b,0,"flujo2h_"+ID[t]+"_"+(t+1));
				}
				
				
				
				//Tercera restriccion
				sum_b=cplex.constant(0);
				cplex.addLe(cplex.sum(Bga[t],Bgp[t]),1,"flujo3h_"+ID[t]+"_"+(t+1));
				
				//Cuarta restriccion
				sum_b=cplex.constant(0);
				cont_aux=0;
				for(k=0;k<TminOn[t];k++){
					if(t-k>=0){
						sum_b=cplex.sum(sum_b,Bga[t-k]);
					}
					else{
						if(k-t == TiniN[t] & TiniN[t]>0)
						sum_b=cplex.sum(sum_b,cplex.constant(1));
					}
				}
				cplex.addEq(cplex.sum(X[t],sum_b,cplex.prod(-1,B[t][0])),0,"flujo4h_"+ID[t]+"_"+(t+1));
				
				
			}
			catch(IloException e){
				System.err.println("Concert exception flujos " + e + "' caught");
    		}
   		}
		
		////////////////////////////////Restriccion Volumen ficticio ///////////////////////////////////////////////////////////////////////
		public void restrvfict1(IloCplex cplex, int t){
			
   			try{
				//la restriccion solo se formula para centrales con commitement igual a 0 y potencia minima mayor que cero: Pt=Pmin*B+P>=Pming*B-Vfic   ->  P>=-Vfic
				if(Commitment[t]==0 & Pming[t]>0){
					//agrego restriccion
					cplex.addGe(cplex.sum(P[0][t],cplex.prod(Vfic[t],Rend[t]/fcon/H[t])),0,"restriccion_vfic_"+ID[t]+"_"+(t+1));
					//cambio cota definida anteriormente (si no hay volumen ficticio la cota inferior es igual a 0) -> Vfic <= Max{Pming*H*fcon/red-Aflu}
					P[0][t].setLB(-Math.max(fcon*H[t]*(Pming[t]/Rend[t]-Aflu[t]),0)*Rend[t]/fcon/H[t]);
				}
			}
			catch(IloException e){
				System.err.println("Concert exception restriccion volumen ficticio " + e + "' caught");
				System.err.println("Revise que rendimiento sea mayor a 0");
    		}
		
		}
		
		public void restrvfict2(IloCplex cplex, int t){	
   			try{
				//la restriccion solo de formula para centrales con vertimiento minimo mayor que cero.
				//tampoco se formula para centrales asociadas a embalses ya que la variable vertimiento se define para el objeto embalse
				if(Qvmin[t]>0 & !Type[t].equals("E"))
				cplex.addGe(cplex.sum(Qver[t],cplex.prod(Vfic2[t],Rend[t]/fcon/H[t])),Qvmin[t],"restriccion_vfic2_"+ID[t]+"_"+(t+1));
			}
			catch(IloException e){
				System.err.println("Concert exception restriccion volumen ficticio " + e + "' caught");
				System.err.println("Revise que rendimiento sea mayor a 0");
    		}
		}
		
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public void InitOptimization(IloCplex cplex){
	}
}
	