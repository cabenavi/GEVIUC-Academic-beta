// GEVIUC: Unit Commitment 
// Version 2015-04-12
// Se modelan las perdidas
// Escrito por Carlos Benavides Farias:  cabenavif@gmail.com  2007-2018
// Copyright 2013
// Software desarrollado para el CE-FCFM (www.centroenergia.cl)

// Unit Commitement (UC) group:
// Carlos Benavides Farias		
// Ignacio Alarcon Arias
// Daniel Espinoza
// Frank Lea�ez Grau
// Rodrigo Palma Benhke             
// Alejandro Angulo
// Rodrigo Sepulveda
// Fernando Lanas
// Rigoberto Torres

import ilog.concert.*;
import ilog.cplex.*;
import ilog.cplex.*;
import java.util.Iterator;
import java.awt.*;
import java.io.*;
import java.io.File;
import java.util.Vector;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class MainUC {

public static boolean debMode= true;

    public static void main(String[] args) throws Exception {
    	
		System.out.println("");
		System.out.println("------------------------------------------------------------------------");
		System.out.println("");
		System.out.println("");
		System.out.println("                           G E V I U C                               	");
		System.out.println("");
		System.out.println("");
		System.out.println("                          Centro de Energia                          	");
		System.out.println("");
		System.out.println("                       FCFM-Universidad de Chile                      	");
		System.out.println("");
		System.out.println(" Modelo Hidrotermico Multinodal para Predespacho Unidades de Generacion");
		System.out.println("");
		System.out.println("                         version enero-2016                          	");
		System.out.println("");
		System.out.println("");
		System.out.println("------------------------------------------------------------------------");
		System.out.println("");
		System.out.println("");
		System.out.println("");
		
		//bandera para activar perdidas
		boolean bandera_perdida=true;	
		
    	double time_ini_mip = System.currentTimeMillis();
		double time_fin = 0;
		Thread thisThread = Thread.currentThread();
    	
    	//parametros database
    	String typedb="";           //tipo de base de datos
		String namedb="";           //nombre base de datos
		String userdb="";           //usuario
		String passdb="";           //contrasena 
	  	
		//archivos de salida
	  	String unitfileroot="";
	// original 		
   	if(args.length==0){
	//if(args.length!=0){    		
		System.out.println("ERROR: SIN ARGUMENTOS");
   	}
    	else{
    		System.out.println("Ejecucion con argumentos de entrada");
			namedb=args[0]; 
//	 		namedb="caso1";              
			typedb= "mysql";
			userdb="root";					   
//			passdb="ce-fcfm";					   
//	  		unitfileroot = args[1];
			
 			passdb=args[1];
			
			//passdb="1234";
			
//			if (args.length > 2){
//				unitfileroot = unitfileroot + "\\";
//				if (args[2].equals("debMode=false")){
//					debMode= false;
//					System.out.println("Sin Debug");
//				}
//			}
    	}

			//optimizador
			IloCplex cplex=new IloCplex();
			
			//factor de escala
			double fes = 0.001;
	    	//factor de conversion de m3/s a m3/hora
			double fcon = 3.6;
			//parametros optimizador
	    	double Relative_gap; 			// gap
			double Tmax; 					// tiempo limite
			int pmode;						// cplex Pmode parameter
			int	maxThds;					// cplex max threads parameter
			int Enfasis;					// emphasis MIP
			int printLP;					// print lp problems to file
			int cplexParMode;				// Cplex parameter: Parallel mode
			int cplexMaxThreads;			// Cplex parameter: max threads (CPUs) to use
			double Sbase;
			double Vbase;
			double Zbase;
		    
			
			
			String pOutFName;				// Main Output file Name
						
			//datos simulacion
	        DataSimulation datasimulation;
			datasimulation = new DataSimulation();
	        datasimulation.loaddatafromdatabase(typedb,namedb,userdb,passdb); /*-2-*/
			
			unitfileroot= 	datasimulation.pOutPath[0]+"\\";
			pOutFName=	"SolutionUC.csv";
			
//			unitfileroot= 	args[2];
//			pOutFName=	args[3];
			
			debMode= 	datasimulation.Export_lp[0] > 0;
			System.out.println("");
			System.out.println("pOutPath: "+unitfileroot);
			System.out.println("pOutFName: "+pOutFName);
			PrintWriter pw 		= new PrintWriter( new FileWriter(unitfileroot+pOutFName));			
//			PrintWriter pw 		= new PrintWriter( new FileWriter(unitfileroot+"SolutionUC.csv"));
			//PrintWriter pw2 	= new PrintWriter( new FileWriter(unitfileroot+"SolutionGap.csv"));
			//PrintWriter pw2aux 	= new PrintWriter( new FileWriter(unitfileroot+"SolutionGapAux.csv"));
			//PrintWriter pw3 	= new PrintWriter( new FileWriter(unitfileroot+"Indice.csv"));
			//PrintWriter pw4 	= new PrintWriter( new FileWriter(unitfileroot+"Heuristica.csv"));
			//PrintWriter pw5 	= new PrintWriter( new FileWriter(unitfileroot+"HeuristicaGBase.csv"));
			//PrintWriter pw6 	= new PrintWriter( new FileWriter(unitfileroot+"HeuristicaFinal.csv"));
			//PrintWriter pw7 	= new PrintWriter( new FileWriter(unitfileroot+"PotenciaEmpresa.csv"));
			//PrintWriter pw8 	= new PrintWriter( new FileWriter(unitfileroot+"IngresoEmpresa.csv"));
			PrintWriter pw10 	= new PrintWriter( new FileWriter(unitfileroot+"ResumenSolucion.csv"));
			//PrintWriter pw11 	= new PrintWriter( new FileWriter(unitfileroot+"Demanda.csv"));
			//PrintWriter pw13 	= new PrintWriter( new FileWriter(unitfileroot+"Semaforo.csv"));
			PrintWriter pw14 	= new PrintWriter( new FileWriter(unitfileroot+"MarginalCost.csv"));
			PrintWriter pw15 	= new PrintWriter( new FileWriter(unitfileroot+"LineFlow.csv"));
			
			//id objetos
			int[]			id_tgenerator;		        //id centrales termicas
			int[]			id_hgenerator;			    //id centrales hidraulicas
			int[]			id_reserv;					//id embalses
			int[]			id_hdb_ser;					//id series
			int[]			id_hdb_paspur;				//id centrales de pasada
			int[]			id_hdb_irr;					//id riego
			int[]			id_hdb_affl;				//id afluentes	
			int[]			id_busbar;					//id barras
			int[]			id_line;					//id lineas
			int[]			id_load;					//id cargas
			int[]			id_solar;					//id solar
			int[]			id_eolic;					//id eolicas
			int[]			id_battery;					//id bateria
			int[]			id_pump;					//id bomba
			int[]			id_csp;						//id csp
			int[]			id_banderas;				//id banderas
			
			//demanda
			double[][] 		demanda = new double[1][1];
			double[][]      demanda_original; 			//arreglo de demanda
			
			int[][] 		bus_demanda;				//arreglo de barras de la demanda
			//barras
			int[][] 		bus;						//arreglo de barras
			//Numero de etapas
			int T;										//numero de periodos	
			//arreglo con duracion de cada etapa
			int[] H;
			//Reserva en giro
			double[] Spinning;
			//Reserva en giro v2
			double[] Spinning2;
			//Control primario de frecuencia
			double[] CPF;
			//Limite de generacion ERNC
			double[] ERNCLimit;
			//Unidad de Tiempo
			double UnitT;
			//Penalizacion Volumen ficticio
			double Cpvfic;
			//Heuristica bateria
			String Heurbat;
			//generacion eolica total
			double[] generacion_eolic = new double[1];      
			//generacion solar total
			double[] generacion_solar = new double[1];
			//suma de potencia minima
			double[] sum_pmin = new double[1];
			
			//datos simulacion
	        Tmax = datasimulation.Tmax[0];
	        Relative_gap = datasimulation.Relative_gap[0];
			pmode	= datasimulation.cpxParMod[0];	
			maxThds= datasimulation.cpxMaxThds[0];
			Sbase=datasimulation.Sbase[0];
			Vbase=datasimulation.Vbase[0];
			Zbase=datasimulation.Zbase[0];
			
	        //reserva en giro
			Spinning = datasimulation.Spinning;
			//reserva en giro v2
			Spinning2 = datasimulation.Spinning2;
			
	        //control primario de frecuencia
			CPF		 = datasimulation.CPF;	
			
			//limite reserva
			ERNCLimit = datasimulation.ERNCLimit;
			
			UnitT    = datasimulation.UnitT[0];
			if(UnitT==0){
				System.out.println("Warning: "+"UniT = 0");
				UnitT=1;
			}
			
			Cpvfic   = datasimulation.Cpvfic[0];
			Heurbat  = datasimulation.Heurbat[0];
			Enfasis	 = datasimulation.Enfasis[0];
			
			System.out.println("");
			System.out.println("Parametros optimizador:");
			System.out.println("");
			System.out.println("Tiempo maximo= "+Tmax);
			System.out.println("Gap = "+Relative_gap);
			System.out.println("");
			
			//datos objetos
			DataObject dataobject; 
	        //datos periodos
	        DataPeriod dataperiod;
	        //datos generador termico
	        DataUCTGenerator datauctgenerator;
	        //datos generador hidraulico
	        DataUCHGenerator datauchgenerator;
	        //datos embalse
	        DataUCReserv dataucreserv;
	        //datos costos de centrales termicas
	        DataUCCost2 datauccost;
			//datos costos de centrales hidraulicas
	        DataUCCost2 datauccosth;
			//datos demanda
	        DataUCLoad dataucload;
	        //datos barras
	        DataUCBus dataucbus;
	        //datos solar
	        DataUCSolar dataucsolar;
	        //datos eolico
	        DataUCEolic datauceolic;
	        //datos bateria
	        DataUCBattery dataucbattery;
	        //datos bomba
	        DataUCPump dataucpump;
			 //datos CSP
	        DataUCCSP datauccsp;
	         //datos Banderas
	        DataUCBanderas dataucbanderas;
	        
			
	        System.out.println("");
			System.out.println("Cargando objetos....");
			dataobject = new DataObject();
			dataobject.loaddatafromdatabase(typedb,namedb,userdb,passdb); /*-1-*/
			System.out.println("");
			
			id_tgenerator       = dataobject.id_tgenerator; 			
	        id_hgenerator 	    = dataobject.id_hgenerator;
	        id_reserv 			= dataobject.id_reserv;
	        id_hdb_ser 			= dataobject.id_hdb_ser;
	        id_hdb_paspur 		= dataobject.id_hdb_paspur;
	        id_hdb_irr 			= dataobject.id_hdb_irr;
	        id_hdb_affl 		= dataobject.id_hdb_affl;
	        id_busbar 			= dataobject.id_busbar;
	        id_line 			= dataobject.id_line;
	        id_load 			= dataobject.id_load;
	        id_solar 			= dataobject.id_solar;
	        id_eolic 			= dataobject.id_eolic;
	        id_battery 			= dataobject.id_battery;
	        id_pump 			= dataobject.id_pump;
	 		id_csp 				= dataobject.id_csp;
	 		id_banderas			= dataobject.id_banderas;
			  
	        dataperiod = new DataPeriod();
	        dataperiod.loaddatafromdatabase(typedb,namedb,userdb,passdb,datasimulation.Tini_data[0],datasimulation.Tfin_data[0]); /*-3-*/
			
			H= dataperiod.H;
			T= H.length;
			
			//Vectores de almacenamiento
			Vector vt				=new Vector(1);
			Vector vh				=new Vector(1);
			Vector vsol             =new Vector(1);
			Vector vsoluc           =new Vector(1);
			Vector veol             =new Vector(1);
			Vector veoluc           =new Vector(1);
			Vector vbat             =new Vector(1);
			Vector vbatuc           =new Vector(1);
			Vector vpump            =new Vector(1);
			Vector vpumpuc          =new Vector(1);
			Vector vres             =new Vector(1);
			Vector vresuc           =new Vector(1);
			Vector vload            =new Vector(1);
			Vector vloaduc          =new Vector(1);
			Vector vcsp          	=new Vector(1);
			Vector vcspuc          	=new Vector(1);
			Vector vline          	=new Vector(1);
			Vector vlineuc          =new Vector(1);
			Vector vbus				=new Vector(1);
			Vector vbusuc			=new Vector(1);
			Vector vbanderas		=new Vector(1);
			Vector vbanderasuc		=new Vector(1);
			
			double[][][] Bsol		=null;
			double[][] Bgsol		=null;
			double[][] Bgasol		=null;
			double[][] Bgpsol		=null;
			double[][] Xsol			=null;
			double[][] Ysol			=null;
			
			double[][][] Barrsol	=new double[1][1][1];
			double[][][] Bparsol	=new double[1][1][1];
			double[][] CMedio		=new double[1][1];
			double[][] Psol			=new double[1][1];  
			double[][] Pasol		=new double[1][1];  
			double[][] Ppsol		=new double[1][1];
			double[][] Pt			=new double[1][1];
			double[][] Costsol  	=new double[1][1];
			double[][] Costarrsol 	=new double[1][1];
			double[][] Costparsol 	=new double[1][1];
			double[][] Rgsol		=new double[1][1];
			double[][] Rgsol2		=new double[1][1];
			double[][] CPFsol		=new double[1][1];
			double[][] CPFeolsol	=new double[1][1];
			double[][] Rgeolsol		=new double[1][1];
			double[][] Rgeolsol2	=new double[1][1];
			double[][] CPFsolsol	=new double[1][1];
			double[][] Rgsolsol		=new double[1][1];
			double[][] Rgsolsol2	=new double[1][1];	
			double[][] RPronsol		=new double[1][1];
			double[] Ptotal	 		=new double[1];
			double[][] Ptotal_barra	=new double[1][1];	
			double[] Patotal		=new double[1];
			double[] Pptotal		=new double[1];	
			double[] Dtotal	 		=new double[1];
			double[] Perdidatotal	=new double[1];
			double[] Rtotal	 		=new double[1];
			double[] Rtotal2	 	=new double[1];
			double[] CPFtotal		=new double[1];
			double[] ResPronsol   	=new double[1];	
			double[] Owtotal	 	=new double[1];
			double[] Costtotalsol  	=new double[1];
			double[] Costarrtotalsol 	=new double[1];
			double[] Costpartotalsol 	=new double[1];
			double[] Costparadatotalsol =new double[1];
			double[] Costopertotalsol 	=new double[1];
			double[] Costoens 			=new double[1];
			
			double[][] Vsol	 	    =new double[1][1];
			
			double[][] Vbsol	 	=new double[1][1];
			double[][] Ppossol	 	=new double[1][1];
			double[][] Pnegsol	 	=new double[1][1];
			double[][] Pisol        =new double[1][1];
			double[][] Pbminsol     =new double[1][1];
			double[][][] Ebsol     	=new double[1][1][1];
			double[][][] Bbsol     	=new double[1][1][1];
			double[][] Bpossol	 	=new double[1][1];
			double[][] Bnegsol	 	=new double[1][1];
			
			double[][] Pbombsol     =new double[1][1];
			double[][] Epsol        =new double[1][1];
			double[][] Ensol        =new double[1][1];
			double[][] Bgbombsol    =new double[1][1];
			double[][] Costpartidasol 	=new double[1][1];
			double[][] Costparadasol	=new double[1][1];
			double[]ENSsol 			=new double[1];
			double[][][] Enssol     =new double[1][1][1];
			double[][] Eperdsol 		=new double[1][1];
			double[][] Cdsol		=new double[1][1];
			double[][] Drsol		=new double[1][1];
			double[][] PEmpresa		=new double[1][1];
			double[][] IOEmpresa	=new double[1][1];
			double C0 			= 0;
			double C1 			= 0;
			int[] Semaforo			=new int[T]; 
			double[][] Rgmaxsol		= new double[1][1];
			double[][] Rgmaxsol2    = new double[1][1];	
			
			//CSP
			double[][] Pel1sol		= new double[1][1];
			double[][] Pel2sol		= new double[1][1];
			double[][] Palmsol		= new double[1][1];
			double[][] Vcspsol		= new double[1][1];
			double[][] Bel1sol		= new double[1][1];
			double[][] Balmsol		= new double[1][1];
			double[][] Bgcspsol		= new double[1][1];
			
			double[][] Peolsol		= new double[1][1];
			double[][] Psolsol		= new double[1][1];
			
			//Hidro pasada o serie
			double[][] Phsol				= null;
			double[][] Qhsol				= null;
			double[][] Bhsol			    = null;
			double[][] Qversol				= null;
			double[][] Vficsol				= null;
			double[][] Rgmaxsol_hidro		= null;
			double[][] Rgmaxsol2_hidro    	= null;
			double[][] Rgsol_hidro			= null;
			double[][] Rgsol2_hidro			= null;
			double[][] CPFsol_hidro			= null;
			double[][] RPronsol_hidro		= null;
			
			
			//Embalses
			double[][] Vembsol				= null;
			double[][] Qverembsol			= null;
			double[][] Vficembsol	 		= null;
			double[][] B1sol				= null;
			double[][] B2sol				= null;
			
			//Linea transmision
			double[][] Fsol					= null;
			double[][][] Fpsol				= null;
			double[][][] Fnsol				= null;
			
			//Angulo en barras
			double [][] AnguloSol			= null;
			
			
			double costo_total      		=0;
			double costo_partida_total 		=0;
			double costo_detencion_total	=0;
			double costototalens			=0;
			double gap_solucion				=0;
			double costo_objetivo			=0;
			double costo_diesel				=0;
			double costosolaux				=0;
			double soltiempo1				=0;
			double soltiempo2				=0;
			double soltiempo1_ini			=0;
			double soltiempo2_ini			=0;
			double soltiempo1_fin			=0;
			double soltiempo2_fin			=0;
			double solucion1				=0;
			double cotainfer1				=0;
			double gap1						=0;
			double solucion2				=0;
			double cotainfer2				=0;
			double gap2						=0;		
			int nempresterm					=0;
			double costoaux					=0;
			String nombreaux				="";
			String[] nomempresterm  		= new String[0];
			
			int sum_t=0;
    		double sum_p=0;
			double sum_pa=0;
			double sum_pp=0;
			double sum_costo_b=0;
			double sum_costo_ba=0;
			double sum_costo_bp=0;
			int[][] orden = new int[1][1]; 	
			int Tr=0;
			
			//indices
			int ia=0;
			
				
			//conversiones de variables continuas a binarias
			IloConversion [][] conv_b 			= new IloConversion[1][1];
			IloConversion [][] conv_bh 			= new IloConversion[1][1];
			IloConversion [] conv_bg 			= new IloConversion[1];
			IloConversion [] conv_bga			= new IloConversion[1];
			IloConversion [] conv_bgp			= new IloConversion[1];
			IloConversion [] conv_bgah			= new IloConversion[1];
			IloConversion [] conv_bgph			= new IloConversion[1];
			IloConversion [] conv_bgb   		= new IloConversion[1];
			IloConversion [] conv_bgbomb		= new IloConversion[1]; 
			IloConversion [] conv_bgbatp		= new IloConversion[1]; 
			IloConversion [] conv_bgbatn		= new IloConversion[1]; 
			IloConversion [][] conv_bb 			= new IloConversion[1][1];
			IloConversion [] conv_csp_bel1 		= new IloConversion[1];
			IloConversion [] conv_csp_balm 		= new IloConversion[1];
			IloConversion [] conv_csp_bg 		= new IloConversion[1];
			
			IloConversion [] conv_x 			= new IloConversion[1];
			IloConversion [] conv_y				= new IloConversion[1];
			IloConversion [] conv_xh 			= new IloConversion[1];
			IloConversion [] conv_yh			= new IloConversion[1];
			
			//variables auxiliares embalses
			IloConversion [] conv_b1 			= new IloConversion[1];
			IloConversion [] conv_b2			= new IloConversion[1];
			
			
			IloRange[][] restric_demanda 		= new IloRange[1][1];
			IloNumVar[] Eperd					= new IloNumVar[1];
			
			int nivmax = 0;
			
			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			
			//variables para condiciones iniciales de variables binarias, centrales termicas
			double set_bgh1[][]=new double[id_tgenerator.length][T];
			double set_bh1[][][] =new double[id_tgenerator.length][10][T];
			
			//variables para condiciones iniciales de variables binarias, centrales hidraulicas
			double set_bgh1_hidro[][]=new double[id_hgenerator.length][T];
			double set_bh1_hidro[][][] =new double[id_hgenerator.length][10][T];
			
			double set_bgh1_aux[][]=new double[id_tgenerator.length][T];
			double set_bh1_aux[][][] =new double[id_tgenerator.length][10][T];
			double set_bg0h1[]=new double[id_tgenerator.length];
			boolean bg_off = false;
			inicializa2double(set_bgh1);
			inicializa2double(set_bgh1_aux);
			
			int ii=0;
			int tt=0;
			int sum_hora=0;
			int tini_hora=0;
			for(int t=0;t<T;t++){
				for(int iv=0;iv<10;iv++){
					for(int i=0;i<id_tgenerator.length;i++){
						set_bh1[i][iv][t]=0;
						set_bh1_aux[i][iv][t]=0;
						set_bg0h1[i]=1;	
					}
				}
			}
			demanda_original=new double[id_load.length][T];
			
			
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			generacion_eolic  = new double[T];	
		    generacion_solar  = new double[T];
			
			//generacion renovable
			double[] generacion_renovable = new double[T];
		    inicializa1double(generacion_eolic); 
		    inicializa1double(generacion_solar);
			inicializa1double(generacion_renovable);
			
			
			//suma de potencia minima
			sum_pmin = new double[T];
			inicializa1double(sum_pmin); 
			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////7/////////
			
			costo_total=0;
			cplex=new IloCplex();
			
			//duracion de periodos original
			H= dataperiod.H;
			//Reduccion
			Tr=0;
			
			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			
			int[] Hr=new int[Tr];	
			T=H.length;
			
			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// vector generadores termicos TGUC
			vt= new Vector(id_tgenerator.length);
			for(int i=0;i<id_tgenerator.length;i++){
				vt.add(new TGUC());
			}
			// vector generadores hidraulicos HGUC
			vh= new Vector(id_hgenerator.length);
			for(int i=0;i<id_hgenerator.length;i++){
				vh.add(new HGUC());
			}
			// vector embalses hidraulicos 
			vres= new Vector(id_reserv.length);
			// vector embalses hidraulicos UC
			vresuc= new Vector(id_reserv.length);
			for(int i=0;i<id_reserv.length;i++){
				vres.add(new DataUCReserv());
				vresuc.add(new ReservUC());
			}
			//vector de generadores solares
			vsol= new Vector(id_solar.length);
			vsoluc= new Vector(id_solar.length);
			for(int i=0;i<id_solar.length;i++){
				vsol.add(new DataUCSolar());
				vsoluc.add(new SolarUC());
			}
			//vector de generadores eolicos
			veol= new Vector(id_eolic.length);
			//vector de generadores eolicos UC
			veoluc= new Vector(id_eolic.length);
			for(int i=0;i<id_eolic.length;i++){
				veol.add(new DataUCEolic());
				veoluc.add(new EolicUC());
			}
			//vector de baterias
			vbat= new Vector(id_battery.length);
			//vector de baterias UC
			vbatuc= new Vector(id_battery.length);
			for(int i=0;i<id_battery.length;i++){
				vbat.add(new DataUCBattery());
				vbatuc.add(new BatteryUC());
			}
			//vector de bombas
			vpump= new Vector(id_pump.length);
			//vector de bombas UC
			vpumpuc= new Vector(id_pump.length);
			for(int i=0;i<id_pump.length;i++){
				vpump.add(new DataUCPump());
				vpumpuc.add(new PumpUC());
			}
			//vector de demanda
			vload= new Vector(id_load.length);
			vloaduc= new Vector(id_load.length);
			for(int k=0;k<id_load.length;k++){
				vload.add(new DataUCLoad());
				vloaduc.add(new LoadUC());
			}
			//vector de CSP
			vcsp= new Vector(id_csp.length);
			//vector de CSP UC
			vcspuc= new Vector(id_csp.length);
			for(int i=0;i<id_csp.length;i++){
				vcsp.add(new DataUCCSP());
				vcspuc.add(new CSPUC());
			}
			
			//vector de Lines
			vline= new Vector(id_line.length);
			//vector de Lines UC
			vlineuc= new Vector(id_line.length);
			for(int i=0;i<id_line.length;i++){
				vline.add(new DataUCLine());
				vlineuc.add(new LineUC());
			}
			//vector de Bus
			vbus= new Vector(id_busbar.length);
			//vector de Bus UC
			vbusuc= new Vector(id_busbar.length);
			for(int i=0;i<id_busbar.length;i++){
				vbus.add(new DataUCBus());
				vbusuc.add(new BusUC());
			}
			//vector de banderas
			vbanderas= new Vector(id_banderas.length);
			//vector de Banderas
			vbanderas= new Vector(id_banderas.length);
			for(int i=0;i<id_banderas.length;i++){
				vbanderas.add(new DataUCBanderas());
				vbanderasuc.add(new BanderasUC());
			}
			
				
			//condiciones iniciales y de borde
			InitialConditionUC datos_ini = new InitialConditionUC();
			
			//Chequeo de errores basicos
			System.out.println("");	
			if(id_busbar.length==0){
				System.out.println("WARNING: El sistema no tiene ninguna barra");
			}
			if(id_load.length==0){
				System.out.println("WARNING: El sistema no tiene ninguna carga");
			}
			//Fin Cheque errores basicos
			
			//generadores termicos
			System.out.println("");
			System.out.println("Numero de generadores termicos = "+id_tgenerator.length);
			for(int i=0;i<id_tgenerator.length;i++){
				//cargando atributos
				datauctgenerator = new DataUCTGenerator();
				datauctgenerator.loaddatafromdatabase(typedb,namedb,userdb,passdb,H,dataperiod.Duration,datasimulation.Tini_data,datasimulation.Tfin_data,id_tgenerator[i]); /*-5-*/
				//cargando costos
				datauccost = new DataUCCost2();
				datauccost.loaddatafromdatabase(typedb,namedb,userdb,passdb,H,dataperiod.Duration,datasimulation.Tini_data,datasimulation.Tfin_data,id_tgenerator[i]); /*-6-*/
				//calculando condiciones iniciales y de borde       
				datos_ini.Calculate(datauctgenerator.GradS,datauctgenerator.GradB,datauctgenerator.GradRA,datauctgenerator.GradRP,H,datauctgenerator.TminOn,datauctgenerator.TminOff,datauctgenerator.TiniN,datauctgenerator.PiniN,datauctgenerator.Pming,datauctgenerator.Pmaxg,datauccost.Pmin,datauccost.Pmax,datauccost.Alfa,datauccost.Beta,datauccost.Ns,datauctgenerator.HotStart_upCost,datauctgenerator.WarmStart_upCost,datauctgenerator.ColdStart_upCost,datauctgenerator.Thot,datauctgenerator.Twarm);
				//cargando datos a generador termico
				((TGUC)(vt.elementAt(i))).InitUC(datauctgenerator.ID,datauctgenerator.Nombre,datauctgenerator.Propietario,datauctgenerator.TminOn,datauctgenerator.TminOff,datauctgenerator.PiniN,datauctgenerator.TiniN,datauctgenerator.GradRA,datauctgenerator.GradRP,datauctgenerator.GradS,datauctgenerator.Pming,datauctgenerator.Pmaxg,datauctgenerator.CespMeRA,datauctgenerator.CespMeRP,datauctgenerator.PoderCal,datauctgenerator.OwnConsuption,datauctgenerator.Barra,datauctgenerator.Unavalaible,datauctgenerator.ForcedGenerator,datauctgenerator.Pcomb,datauccost.Pmin,datauccost.Pmax,datauctgenerator.Vini,datauctgenerator.Vmin,datauctgenerator.Vmax,datauctgenerator.KEstanque,datauctgenerator.AEstanque,datauctgenerator.Vfin,datauctgenerator.SpinningMax,datauctgenerator.SpinningMax2,datauctgenerator.CPFMax,datauctgenerator.Reserva_Pronta, datauctgenerator.Commitment, datauctgenerator.StopCost,datauctgenerator.NombreCentral,datauctgenerator.Tecnologia,datauctgenerator.Combustible,datauctgenerator.IDAcoplaTV,datauctgenerator.FactorAcoplaTVTG,datauctgenerator.IsERNC,datauctgenerator.Dependence,datauctgenerator.Fdependence);
				((TGUC)(vt.elementAt(i))).InitDataUC(datos_ini.EtpArrIni,datos_ini.EtpParIni,datos_ini.NetpA,datos_ini.matriz_ia,datos_ini.NetpP,datos_ini.EtpOn,datos_ini.EtpOff,datos_ini.EtpOnFin,datos_ini.EtpOffFin,datos_ini.EtpOnIni,datos_ini.EtpOffIni,datos_ini.Arra,datos_ini.Par,datos_ini.NARR,datos_ini.NPAR,datos_ini.n_ia_max,datos_ini.n_ip_max,datos_ini.n_arraq,datos_ini.n_par,datos_ini.PaFx,datos_ini.PpFx,datos_ini.EtpArrBg,datos_ini.EtpArrBgEq1,datos_ini.Tfs,datos_ini.Bgi,datos_ini.Pminuc,datos_ini.Pmaxuc,datos_ini.Alfauc,datos_ini.Betauc,datos_ini.Niv,datos_ini.M,datos_ini.n_iv,datos_ini.CPartida,datos_ini.TPartida,datos_ini.IPartida,datos_ini.NCP,datos_ini.Bgni, UnitT,set_bh1[i],set_bgh1[i]);
				//cargando variables
				((TGUC)(vt.elementAt(i))).InitVariables(H,1,cplex);
			}
			
			//generadores hidraulicos
			System.out.println("");
			System.out.println("Numero de generadores hidraulicos = "+id_hgenerator.length);
			datos_ini = new InitialConditionUC();
			for(int i=0;i<id_hgenerator.length;i++){	
				//cargando atributos
				datauchgenerator = new DataUCHGenerator();
				datauchgenerator.loaddatafromdatabase(typedb,namedb,userdb,passdb,H,dataperiod.Duration,datasimulation.Tini_data,datasimulation.Tfin_data,id_hgenerator[i]); /*-7-*/
				//cargando costos
				datauccosth = new DataUCCost2();
				datauccosth.loaddatafromdatabase(typedb,namedb,userdb,passdb,H,dataperiod.Duration,datasimulation.Tini_data,datasimulation.Tfin_data,id_hgenerator[i]); /*-6-*/
				//calculando condiciones iniciales y de borde       
				datos_ini.Calculate(datauchgenerator.GradS,datauchgenerator.GradB,datauchgenerator.GradRA,datauchgenerator.GradRP,H,datauchgenerator.TminOn,datauchgenerator.TminOff,datauchgenerator.TiniN,datauchgenerator.PiniN,datauchgenerator.Pming,datauchgenerator.Pmaxg,datauccosth.Pmin,datauccosth.Pmax,datauccosth.Alfa,datauccosth.Beta,datauccosth.Ns,datauchgenerator.HotStart_upCost,datauchgenerator.WarmStart_upCost,datauchgenerator.ColdStart_upCost,datauchgenerator.Thot,datauchgenerator.Twarm);
				//cargando datos a generador hidraulico
				((HGUC)(vh.elementAt(i))).InitUC(datauchgenerator.ID,datauchgenerator.Nombre,datauchgenerator.TminOn,datauchgenerator.TminOff,datauchgenerator.PiniN,datauchgenerator.TiniN, datauchgenerator.GradRA,datauchgenerator.GradRP,datauchgenerator.GradS,datauchgenerator.Pming,datauchgenerator.Pmaxg,datauchgenerator.CespMeRA,datauchgenerator.CespMeRP,datauchgenerator.PoderCal,datauchgenerator.OwnConsuption,datauchgenerator.Barra,datauchgenerator.Unavalaible,datauchgenerator.ForcedGenerator,datauchgenerator.Pcomb,datauccosth.Pmin,datauccosth.Pmax,datauchgenerator.Rend,datauchgenerator.Qvmin, datauchgenerator.Qvmax,datauchgenerator.Ctur,datauchgenerator.Cver,datauchgenerator.SpinningMax,datauchgenerator.SpinningMax2,datauchgenerator.CPFMax,datauchgenerator.Reserva_Pronta,datauchgenerator.Commitment,datauchgenerator.StopCost,datauchgenerator.Aflu,datauchgenerator.Type);
				((HGUC)(vh.elementAt(i))).InitDataUC(datos_ini.EtpArrIni,datos_ini.EtpParIni,datos_ini.NetpA,datos_ini.matriz_ia,datos_ini.NetpP,datos_ini.EtpOn,datos_ini.EtpOff,datos_ini.EtpOnFin,datos_ini.EtpOffFin,datos_ini.EtpOnIni,datos_ini.EtpOffIni,datos_ini.Arra,datos_ini.Par,datos_ini.NARR,datos_ini.NPAR,datos_ini.n_ia_max,datos_ini.n_ip_max,datos_ini.n_arraq,datos_ini.n_par,datos_ini.PaFx,datos_ini.PpFx,datos_ini.EtpArrBg,datos_ini.EtpArrBgEq1,datos_ini.Tfs,datos_ini.Bgi,datos_ini.Pminuc,datos_ini.Pmaxuc,datos_ini.Alfauc,datos_ini.Betauc,datos_ini.Niv,datos_ini.M,datos_ini.n_iv,datos_ini.CPartida,datos_ini.TPartida,datos_ini.IPartida,datos_ini.NCP,datos_ini.Bgni, UnitT,set_bh1_hidro[i],set_bgh1_hidro[i],fes,fcon);
				//cargando variables
				((HGUC)(vh.elementAt(i))).InitVariables(H,1,cplex);
			
			}
			
			//embalses
			System.out.println("");
			System.out.println("Numero de embalses = "+id_reserv.length);
			for(int k=0;k<id_reserv.length;k++){
				//cargando atributos
				((DataUCReserv)(vres.elementAt(k))).loaddatafromdatabase(typedb,namedb,userdb,passdb,H,dataperiod.Duration,datasimulation.Tini_data,datasimulation.Tfin_data,id_reserv[k]); /*-8-*/
				((ReservUC)(vresuc.elementAt(k))).InitUC(((DataUCReserv)(vres.elementAt(k))).Id, ((DataUCReserv)(vres.elementAt(k))).Nombre,((DataUCReserv)(vres.elementAt(k))).Vmin,((DataUCReserv)(vres.elementAt(k))).Vmax,((DataUCReserv)(vres.elementAt(k))).Ctur,((DataUCReserv)(vres.elementAt(k))).Cver,((DataUCReserv)(vres.elementAt(k))).Cfil,((DataUCReserv)(vres.elementAt(k))).Aflu,((DataUCReserv)(vres.elementAt(k))).Ret,((DataUCReserv)(vres.elementAt(k))).Qvmin,((DataUCReserv)(vres.elementAt(k))).Qvmax,((DataUCReserv)(vres.elementAt(k))).Propietario,((DataUCReserv)(vres.elementAt(k))).Vini,((DataUCReserv)(vres.elementAt(k))).Vfin,((DataUCReserv)(vres.elementAt(k))).Vermin,((DataUCReserv)(vres.elementAt(k))).Vermax,((DataUCReserv)(vres.elementAt(k))).ID_Central, ((DataUCReserv)(vres.elementAt(k))).ReserveMinVolume);
				((ReservUC)(vresuc.elementAt(k))).InitDataUC(UnitT);
				((ReservUC)(vresuc.elementAt(k))).InitVariables(H,1,cplex);
			}
			
			//demanda
			System.out.println("");
			System.out.println("Numero de demandas= "+id_load.length);
			demanda		= new double[id_load.length][Hr.length];		
			bus_demanda	= new int[id_load.length][Hr.length];	
			for(int k=0;k<id_load.length;k++){
				((DataUCLoad)(vload.elementAt(k))).loaddatafromdatabase(typedb,namedb,userdb,passdb,H,dataperiod.Duration,datasimulation.Tini_data,datasimulation.Tfin_data,id_load[k]); /*-9-*/
				demanda[k] 			=((DataUCLoad)(vload.elementAt(k))).Load;
				demanda_original[k] = ((DataUCLoad)(vload.elementAt(k))).Load;
				bus_demanda[k]	=((DataUCLoad)(vload.elementAt(k))).Bus;
				((LoadUC)(vloaduc.elementAt(k))).InitUC(((DataUCLoad)(vload.elementAt(k))).Id, ((DataUCLoad)(vload.elementAt(k))).Nombre,((DataUCLoad)(vload.elementAt(k))).Load,((DataUCLoad)(vload.elementAt(k))).Bus,((DataUCLoad)(vload.elementAt(k))).Ensmin,((DataUCLoad)(vload.elementAt(k))).Ensmax,((DataUCLoad)(vload.elementAt(k))).Alfa,((DataUCLoad)(vload.elementAt(k))).Beta,((DataUCLoad)(vload.elementAt(k))).Cdmin,((DataUCLoad)(vload.elementAt(k))).Cdmax,((DataUCLoad)(vload.elementAt(k))).Eperdmin,((DataUCLoad)(vload.elementAt(k))).Eperdmax,((DataUCLoad)(vload.elementAt(k))).Cpperd,((DataUCLoad)(vload.elementAt(k))).Td,((DataUCLoad)(vload.elementAt(k))).Ns, ((DataUCLoad)(vload.elementAt(k))).n_iv);
				((LoadUC)(vloaduc.elementAt(k))).InitDataUC();
				((LoadUC)(vloaduc.elementAt(k))).InitVariables(H,1,cplex);
				
			}
			
			
			//Barra
			System.out.println("Numero de barras = "+id_busbar.length);
			bus	= new int[id_busbar.length][H.length];	
			for(int k=0;k<id_busbar.length;k++){
				((DataUCBus)(vbus.elementAt(k))).loaddatafromdatabase(typedb,namedb,userdb,passdb,H,dataperiod.Duration,datasimulation.Tini_data,datasimulation.Tfin_data,id_busbar[k]); /*-10-*/
				((BusUC)(vbusuc.elementAt(k))).InitUC(((DataUCBus)(vbus.elementAt(k))).Id, ((DataUCBus)(vbus.elementAt(k))).Nombre,((DataUCBus)(vbus.elementAt(k))).Bus);
				((BusUC)(vbusuc.elementAt(k))).InitDataUC(UnitT);
				((BusUC)(vbusuc.elementAt(k))).InitVariables(H,1,cplex);
				bus[k] 		= ((DataUCBus)(vbus.elementAt(k))).Bus; 
			}
			
			//Solar
			System.out.println("");	
			System.out.println("Numero de plantas solares = "+id_solar.length);
			for(int k=0;k<id_solar.length;k++){
				((DataUCSolar)(vsol.elementAt(k))).loaddatafromdatabase(typedb,namedb,userdb,passdb,H,dataperiod.Duration,datasimulation.Tini_data,datasimulation.Tfin_data,id_solar[k]); /*-12-*/
				((SolarUC)(vsoluc.elementAt(k))).InitUC(((DataUCSolar)(vsol.elementAt(k))).Id, ((DataUCSolar)(vsol.elementAt(k))).Nombre,((DataUCSolar)(vsol.elementAt(k))).PotenciaSolar,((DataUCSolar)(vsol.elementAt(k))).Propietario,((DataUCSolar)(vsol.elementAt(k))).CPFmax, ((DataUCSolar)(vsol.elementAt(k))).SpinningMax, ((DataUCSolar)(vsol.elementAt(k))).SpinningMax2, ((DataUCSolar)(vsol.elementAt(k))).IsERNC, ((DataUCSolar)(vsol.elementAt(k))).Barra);
				((SolarUC)(vsoluc.elementAt(k))).InitDataUC(UnitT);
				((SolarUC)(vsoluc.elementAt(k))).InitVariables(H,1,cplex);
			}			
			
			//Eolica
			System.out.println("");
			System.out.println("Numero de centrales eolicas = "+id_eolic.length);
			for(int k=0;k<id_eolic.length;k++){
				((DataUCEolic)(veol.elementAt(k))).loaddatafromdatabase(typedb,namedb,userdb,passdb,H,dataperiod.Duration,datasimulation.Tini_data,datasimulation.Tfin_data,id_eolic[k]); /*-13-*/
				((EolicUC)(veoluc.elementAt(k))).InitUC(((DataUCEolic)(veol.elementAt(k))).Id, ((DataUCEolic)(veol.elementAt(k))).Nombre,((DataUCEolic)(veol.elementAt(k))).Viento,((DataUCEolic)(veol.elementAt(k))).PotenciaEolic,((DataUCEolic)(veol.elementAt(k))).FEolic,((DataUCEolic)(veol.elementAt(k))).Propietario,((DataUCEolic)(veol.elementAt(k))).CPFmax, ((DataUCEolic)(veol.elementAt(k))).SpinningMax, ((DataUCEolic)(veol.elementAt(k))).SpinningMax2, ((DataUCEolic)(veol.elementAt(k))).IsERNC, ((DataUCEolic)(veol.elementAt(k))).Barra);
				((EolicUC)(veoluc.elementAt(k))).InitDataUC(UnitT);
				((EolicUC)(veoluc.elementAt(k))).InitVariables(H,1,cplex);
			}
			
			//Bateria
			System.out.println("");	
			System.out.println("Numero de baterias = "+id_battery.length);
			for(int k=0;k<id_battery.length;k++){
				((DataUCBattery)(vbat.elementAt(k))).loaddatafromdatabase(typedb,namedb,userdb,passdb,H,dataperiod.Duration,datasimulation.Tini_data,datasimulation.Tfin_data,id_battery[k]); /*-14-*/
				((BatteryUC)(vbatuc.elementAt(k))).InitUC(((DataUCBattery)(vbat.elementAt(k))).Id, ((DataUCBattery)(vbat.elementAt(k))).Nombre,((DataUCBattery)(vbat.elementAt(k))).Emin,((DataUCBattery)(vbat.elementAt(k))).Emax,((DataUCBattery)(vbat.elementAt(k))).Pmin,((DataUCBattery)(vbat.elementAt(k))).Pmax,((DataUCBattery)(vbat.elementAt(k))).Nid,((DataUCBattery)(vbat.elementAt(k))).Nic,((DataUCBattery)(vbat.elementAt(k))).Rb,((DataUCBattery)(vbat.elementAt(k))).Eini,((DataUCBattery)(vbat.elementAt(k))).Efin,((DataUCBattery)(vbat.elementAt(k))).Esocmin,((DataUCBattery)(vbat.elementAt(k))).Esocmax,((DataUCBattery)(vbat.elementAt(k))).Alfa,((DataUCBattery)(vbat.elementAt(k))).Sigma,((DataUCBattery)(vbat.elementAt(k))).Pio,((DataUCBattery)(vbat.elementAt(k))).Cb,((DataUCBattery)(vbat.elementAt(k))).Ns, ((DataUCBattery)(vbat.elementAt(k))).n_iv);
				((BatteryUC)(vbatuc.elementAt(k))).InitDataUC(UnitT);
				((BatteryUC)(vbatuc.elementAt(k))).InitVariables(H,1,cplex);
			}
			
			//Bomba
			System.out.println("");	
			System.out.println("Numero de bombas electricas= "+id_pump.length);
			for(int k=0;k<id_pump.length;k++){
				((DataUCPump)(vpump.elementAt(k))).loaddatafromdatabase(typedb,namedb,userdb,passdb,H,dataperiod.Duration,datasimulation.Tini_data,datasimulation.Tfin_data,id_pump[k]); /*-15-*/
				((PumpUC)(vpumpuc.elementAt(k))).InitUC(((DataUCPump)(vpump.elementAt(k))).Id, ((DataUCPump)(vpump.elementAt(k))).Nombre,((DataUCPump)(vpump.elementAt(k))).PBomba,((DataUCPump)(vpump.elementAt(k))).KBomba,((DataUCPump)(vpump.elementAt(k))).EmbBomba,((DataUCPump)(vpump.elementAt(k))).Barra);
				((PumpUC)(vpumpuc.elementAt(k))).InitDataUC(UnitT);
				((PumpUC)(vpumpuc.elementAt(k))).InitVariables(H,1,cplex);
			}
			// CSP
			System.out.println("");
			System.out.println("Numero de plantas CSP= "+id_csp.length);
			for(int k=0;k<id_csp.length;k++){
				((DataUCCSP)(vcsp.elementAt(k))).loaddatafromdatabase(typedb,namedb,userdb,passdb,H,dataperiod.Duration,datasimulation.Tini_data,datasimulation.Tfin_data,id_csp[k]); /*-16-*/
				((CSPUC)(vcspuc.elementAt(k))).InitUC(((DataUCCSP)(vcsp.elementAt(k))).Id, ((DataUCCSP)(vcsp.elementAt(k))).Nombre,((DataUCCSP)(vcsp.elementAt(k))).PotenciaSolar,((DataUCCSP)(vcsp.elementAt(k))).PerdidasAlmacenamiento,((DataUCCSP)(vcsp.elementAt(k))).EficienciaAlmacenamiento,((DataUCCSP)(vcsp.elementAt(k))).EficienciaInyeccion,((DataUCCSP)(vcsp.elementAt(k))).Vmin,((DataUCCSP)(vcsp.elementAt(k))).Vmax,((DataUCCSP)(vcsp.elementAt(k))).Propietario,((DataUCCSP)(vcsp.elementAt(k))).Vini,((DataUCCSP)(vcsp.elementAt(k))).Vfin,((DataUCCSP)(vcsp.elementAt(k))).Pmin,((DataUCCSP)(vcsp.elementAt(k))).Pmax, ((DataUCCSP)(vcsp.elementAt(k))).IsERNC);
				((CSPUC)(vcspuc.elementAt(k))).InitDataUC(UnitT);
				((CSPUC)(vcspuc.elementAt(k))).InitVariables(H,1,cplex);
			} 
			//Lineas
			System.out.println("");
			System.out.println("Numero de lineas = "+id_line.length);
			System.out.println("");
			for(int k=0;k<id_line.length;k++){
				((DataUCLine)(vline.elementAt(k))).loaddatafromdatabase(typedb,namedb,userdb,passdb,H,dataperiod.Duration,datasimulation.Tini_data,datasimulation.Tfin_data,id_line[k]); 
				((LineUC)(vlineuc.elementAt(k))).InitUC(((DataUCLine)(vline.elementAt(k))).ID,((DataUCLine)(vline.elementAt(k))).Nombre,((DataUCLine)(vline.elementAt(k))).BusIni,((DataUCLine)(vline.elementAt(k))).BusFin, ((DataUCLine)(vline.elementAt(k))).Resistencia, ((DataUCLine)(vline.elementAt(k))).Reactancia, ((DataUCLine)(vline.elementAt(k))).Largo, ((DataUCLine)(vline.elementAt(k))).Voltaje, ((DataUCLine)(vline.elementAt(k))).Fmin, ((DataUCLine)(vline.elementAt(k))).Fmax, ((DataUCLine)(vline.elementAt(k))).Propietario, ((DataUCLine)(vline.elementAt(k))).matriz_alpha,((DataUCLine)(vline.elementAt(k))).tramos_maximo,((DataUCLine)(vline.elementAt(k))).Opera);
				((LineUC)(vlineuc.elementAt(k))).InitDataUC(UnitT);
				((LineUC)(vlineuc.elementAt(k))).InitVariables(H,1,cplex);
			}
			
			
			DBObj.closeLink(); /**- Close1 -**/
			
			////////////////////////////////////////////////PREDESPACHO//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
			
			
			//restriccion demanda -> util para extraer costo marginales
			restric_demanda = new IloRange[id_busbar.length][T];
			
			// Formulando problema de optimizacion
			ProblemUC problem = new ProblemUC();
			// El problema de optimizacion se formula en esta clase
			System.out.println("");
			System.out.println("Formulando problema de optimizacion");
			System.out.println("");
			problem.formulate(cplex, vt, vh, vsoluc, veol, veoluc, vbat , vbatuc, vpump, vpumpuc, vres, vresuc, vload, vloaduc, vcsp, vcspuc, vlineuc, vbusuc, restric_demanda, id_tgenerator,  id_hgenerator, id_reserv, id_hdb_ser, id_hdb_paspur, id_hdb_irr, id_hdb_affl, id_busbar, id_line, id_load, id_solar, id_eolic, id_battery, id_pump, id_csp, H, T, UnitT, demanda, Spinning, Spinning2, CPF, ERNCLimit, Cpvfic, generacion_eolic, generacion_solar, generacion_renovable, sum_pmin, fes, fcon,bus,bandera_perdida, Sbase, Vbase,Zbase);
			System.out.println("");
			System.out.println("Fin de formulacion");
			System.out.println("");
	
			//Conversion de variables: de continuas a binarias
			for(int i=0;i<id_tgenerator.length;i++){
				for(int t=0;t<T;t++){
					if(((TGUC)(vt.elementAt(i))).Niv[t]>nivmax){
						nivmax = ((TGUC)(vt.elementAt(i))).Niv[t]; 	
					}
				}
			}
			//B
			conv_b= new IloConversion[id_tgenerator.length][T];
			for(int i=0;i<id_tgenerator.length;i++){
				for(int t=0;t<T;t++){
					conv_b[i][t]=cplex.conversion(((TGUC)(vt.elementAt(i))).B[t], IloNumVarType.Bool);              
					for(int iv=0;iv<((TGUC)(vt.elementAt(i))).Niv[t];iv++){
						if(set_bh1[i][iv][t] != 1){
							cplex.add(conv_b[i][t]);              
						}
					}
				}
			}
			//Bg
			conv_bg= new IloConversion[id_tgenerator.length];
			for(int i=0;i<id_tgenerator.length;i++){
				conv_bg[i]=cplex.conversion(((TGUC)(vt.elementAt(i))).Bg, IloNumVarType.Bool);              
				//if(set_bgh1[t][i] != 1){
					//cplex.add(conv_bg[i]);
				//}	
			}
			
			conv_bga= new IloConversion[id_tgenerator.length];
			conv_bgp= new IloConversion[id_tgenerator.length];
			for(int i=0;i<id_tgenerator.length;i++){
				conv_bga[i]=cplex.conversion(((TGUC)(vt.elementAt(i))).Bga, IloNumVarType.Bool);
				conv_bgp[i]=cplex.conversion(((TGUC)(vt.elementAt(i))).Bgp, IloNumVarType.Bool);               
				cplex.add(conv_bga[i]);
				cplex.add(conv_bgp[i]);
			}
			
			conv_x= new IloConversion[id_tgenerator.length];
			conv_y= new IloConversion[id_tgenerator.length];
			for(int i=0;i<id_tgenerator.length;i++){					
				conv_x[i]=cplex.conversion(((TGUC)(vt.elementAt(i))).X, IloNumVarType.Bool);
				conv_y[i]=cplex.conversion(((TGUC)(vt.elementAt(i))).Y, IloNumVarType.Bool);               
				cplex.add(conv_x[i]);
				cplex.add(conv_y[i]);
			}
			
			//hidro
			//Bh
			conv_bh= new IloConversion[id_hgenerator.length][T];
			for(int i=0;i<id_hgenerator.length;i++){
				for(int t=0;t<T;t++){
					conv_bh[i][t]=cplex.conversion(((HGUC)(vh.elementAt(i))).B[t], IloNumVarType.Bool);              
					for(int iv=0;iv<((HGUC)(vh.elementAt(i))).Niv[t];iv++){
						if(((HGUC)(vh.elementAt(i))).set_bh1[iv][t] != 1){
							cplex.add(conv_bh[i][t]);              
						}
					}
				}
			}
			conv_bgah= new IloConversion[id_hgenerator.length];
			conv_bgph= new IloConversion[id_hgenerator.length];
			for(int i=0;i<id_hgenerator.length;i++){
				conv_bgah[i]=cplex.conversion(((HGUC)(vh.elementAt(i))).Bga, IloNumVarType.Bool);
				conv_bgph[i]=cplex.conversion(((HGUC)(vh.elementAt(i))).Bgp, IloNumVarType.Bool);               
				cplex.add(conv_bgah[i]);
				cplex.add(conv_bgph[i]);
			}
			conv_xh= new IloConversion[id_hgenerator.length]; 
			conv_yh= new IloConversion[id_hgenerator.length];
			for(int i=0;i<id_hgenerator.length;i++){					
				conv_xh[i]=cplex.conversion(((HGUC)(vh.elementAt(i))).X, IloNumVarType.Bool);
				conv_yh[i]=cplex.conversion(((HGUC)(vh.elementAt(i))).Y, IloNumVarType.Bool);               
				cplex.add(conv_xh[i]);
				cplex.add(conv_yh[i]);
			}
			
			//bomba electrica
			conv_bgbomb= new IloConversion[id_pump.length];
			for(int k=0;k<id_pump.length;k++){
				conv_bgbomb[k]=cplex.conversion(((PumpUC)(vpumpuc.elementAt(k))).Bg, IloNumVarType.Bool);              
				cplex.add(conv_bgbomb[k]);              
			}
			//bateria
			conv_bgbatp=  new IloConversion[id_battery.length];
			for(int k=0;k<id_battery.length;k++){
				conv_bgbatp[k]=cplex.conversion(((BatteryUC)(vbatuc.elementAt(k))).Bpos, IloNumVarType.Bool);              
				cplex.add(conv_bgbatp[k]);              
			} 
			
			conv_bgbatn=  new IloConversion[id_battery.length];
			for(int k=0;k<id_battery.length;k++){
				conv_bgbatn[k]=cplex.conversion(((BatteryUC)(vbatuc.elementAt(k))).Bneg, IloNumVarType.Bool);              
				cplex.add(conv_bgbatn[k]);              
			}
			//tramos pmin bateria
			
			conv_bb= new IloConversion[id_battery.length][T];
			for(int k=0;k<id_battery.length;k++){
				for(int s=0;s<((BatteryUC)(vbatuc.elementAt(k))).n_iv;s++){
					conv_bb[k][s]=cplex.conversion(((BatteryUC)(vbatuc.elementAt(k))).B[s], IloNumVarType.Bool);              
					cplex.add(conv_bb[k][s]);              
				}
			}
					
			//CSP
			conv_csp_bel1 	= new IloConversion[id_csp.length];
			conv_csp_balm 	= new IloConversion[id_csp.length];
			for(int k=0;k<id_csp.length;k++){
				conv_csp_bel1[k]=cplex.conversion(((CSPUC)(vcspuc.elementAt(k))).Bel1, IloNumVarType.Bool);
				conv_csp_balm[k]=cplex.conversion(((CSPUC)(vcspuc.elementAt(k))).Balm, IloNumVarType.Bool);          
				conv_csp_bg[k]=cplex.conversion(((CSPUC)(vcspuc.elementAt(k))).Bg, IloNumVarType.Bool); 
				cplex.add(conv_csp_bel1[k]);  
				cplex.add(conv_csp_balm[k]);  
				cplex.add(conv_csp_bg[k]);  	
			}
			
			//variables auxiliares volumen embalse
			conv_b1= new IloConversion[id_reserv.length];
			conv_b2= new IloConversion[id_reserv.length];
			for(int k=0;k<id_reserv.length;k++){
				conv_b1[k]=cplex.conversion(((ReservUC)(vresuc.elementAt(k))).B1, IloNumVarType.Bool);              
				conv_b2[k]=cplex.conversion(((ReservUC)(vresuc.elementAt(k))).B2, IloNumVarType.Bool);              
				cplex.add(conv_b1[k]);              
				cplex.add(conv_b2[k]);
			}
			
			//#### Here to switch lp printing			
			
			///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// System.out.println("");
			if (MainUC.debMode){
				System.out.println("Creando archivo en formato .lp");
				cplex.exportModel(unitfileroot+"UC.lp");	
			}
			
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			//Paremetros CPLEX
			parametres_cplex(cplex, Relative_gap,Tmax, pmode, maxThds);
			
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			System.out.println("");
			System.out.println("-----------------------------SOLVING UC MIP -----------------------------------------------------------------------");
			// Imprimiendo numero de variables y restricciones del problema de optimizacion
			double columnas	= cplex.getNcols();
			double filas	= cplex.getNrows();
			double enteros	= cplex.getNintVars();
			double binarias	= cplex.getNbinVars(); 
			System.out.println("");
			System.out.println("Numero de variables    		 = "+columnas);
			System.out.println("Numero de restricciones		 = "+filas);
			System.out.println("Numero de variables enteras	 = "+enteros);
			System.out.println("Numero de variables binarias = "+binarias);
			System.out.println("Numero de variables continuas= "+(columnas-binarias-enteros));
			System.out.println("");  
			
			boolean impremesol  = true;    
			
			//Llamando optimizador de CPLEX
			if(cplex.solve()){
				double time_fin_mip = System.currentTimeMillis();	
				
				soltiempo1_fin = System.currentTimeMillis();
				soltiempo1=(soltiempo1_fin-soltiempo1_ini)/1000;
				solucion1		=cplex.getObjValue();
				cotainfer1		=cplex.getBestObjValue();
				gap1			=(cplex.getObjValue()-cplex.getBestObjValue())/cplex.getObjValue()*100;
				
				System.out.println("");
				System.out.println("Funcion objetivo de MIP = "+cplex.getObjValue());
				costo_objetivo=cplex.getObjValue();
				System.out.println("Cota inferior           = "+cplex.getBestObjValue());
				System.out.println("Gap absoluto            = "+(cplex.getObjValue()-cplex.getBestObjValue()));
				gap_solucion = (cplex.getObjValue()-cplex.getBestObjValue())/cplex.getObjValue()*100;
				System.out.println("Gap	relativo %          = "+(cplex.getObjValue()-cplex.getBestObjValue())/cplex.getObjValue()*100);
				System.out.println("Tiempo solucion         = "+(time_fin_mip-time_ini_mip)/1000+" segundos");
				System.out.println("");
			
			}
			else{
				System.out.println("PROBLEMA ENTERO MIXTO INFACTIBLE O NO SE ENCUENTRA SOLUCION FACTIBLE");
				impremesol=false;
			//	thisThread.suspend();
				System.out.println("Creando archivo en formato .lp");
				cplex.exportModel(unitfileroot+"UC.lp");	
				System.exit(-1);
			}
			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			//IMPRIMIR SOLUCION	
			if(impremesol){
			
				costo_total      =0;
				
				//Potencia total
				Pt     = new double[id_tgenerator.length][T];
				Ptotal = new double[T];
				Ptotal_barra= new double[id_busbar.length][T];
				Dtotal = new double[T];
				Perdidatotal = new double[T];
				inicializa2double(Pt);
				inicializa1double(Ptotal);
				inicializa2double(Ptotal_barra);
				inicializa1double(Dtotal);
				inicializa1double(Perdidatotal);
				
				//Solucion varibles binarias centrales termicas
				//Solucion de B
				Bsol = new double[id_tgenerator.length][nivmax][T];
				//Solucion Bg
				Bgsol = new double [id_tgenerator.length][T];
				//Solucion Bga
				Bgasol = new double [id_tgenerator.length][T];
				//Solucion Bgp
				Bgpsol = new double [id_tgenerator.length][T];
				//Solucion X
				Xsol = new double [id_tgenerator.length][T];
				//Solucion Y
				Ysol = new double [id_tgenerator.length][T];
				
				
				//Solucion variables binarias bateria
				//Solucion binaria positiva
				Bpossol = new double[id_battery.length][T];
				//Solucion binaria negativa
				Bnegsol = new double[id_battery.length][T];
				//Solucion binaria por tramos positiva
				if(id_battery.length>0){
					Bbsol = new double[id_battery.length][((BatteryUC)(vbatuc.elementAt(0))).n_iv][T];
				}
				else{
					Bbsol = new double[id_battery.length][0][T];
				}
				
				//Solucion variables binarias CSP
				Bel1sol = new double[id_csp.length][T];
				Balmsol = new double[id_csp.length][T];
				Bgcspsol = new double[id_csp.length][T];
				
				//Funcion que calcula solucion de variables binarias
				solucion_binarias_termicas(cplex,vt,id_tgenerator,Bsol,Bgsol,Bgasol,Bgpsol,Xsol,Ysol,set_bgh1,set_bh1,T);
				solucion_binarias_csp(cplex,vcspuc,id_csp,Bel1sol,Balmsol,Bgcspsol,T);
				solucion_binarias_battery(cplex,vbatuc,id_battery,Bpossol,Bnegsol,Bbsol,T);
				
				//Solucion Costo
				System.out.println("solucion costo");
				Costsol = new double[id_tgenerator.length][T];
				Costtotalsol = new double[T];
				Costopertotalsol = new double[T];
				Costoens = new double[T];
				inicializa2double(Costsol);
				inicializa1double(Costtotalsol);
				inicializa1double(Costopertotalsol);
				inicializa1double(Costoens);
				
				for(int t=0;t<T;t++){
					for(int i=0;i<id_tgenerator.length;i++){
						Costsol[i][t]=Costsol[i][t]+(H[t]*UnitT*((TGUC)(vt.elementAt(i))).Pcomb[t]*((TGUC)(vt.elementAt(i))).Alfauc[0][t]*((TGUC)(vt.elementAt(i))).Pming[t])*cplex.getValue(((TGUC)(vt.elementAt(i))).B[t][0]);
						Costsol[i][t]=Costsol[i][t]+ (H[t]*UnitT*((TGUC)(vt.elementAt(i))).Pcomb[t]*((TGUC)(vt.elementAt(i))).Alfauc[0][t])*cplex.getValue(((TGUC)(vt.elementAt(i))).P[0][t]);
						Costtotalsol[t]=Costtotalsol[t]+Costsol[i][t];
						costo_total = costo_total + Costsol[i][t];
						Costopertotalsol[t]=Costopertotalsol[t]+ Costsol[i][t];
					}
				}
				
				//Solucion Costo Rampa Arranque
				Costarrsol = new double[id_tgenerator.length][T];
				inicializa2double(Costarrsol);
				for(int t=0;t<T;t++){
					for(int i=0;i<id_tgenerator.length;i++){
						Costarrsol[i][t]=0;
						if(((TGUC)(vt.elementAt(i))).GradRA[0]>0){
							for(ia=0;ia<((TGUC)(vt.elementAt(i))).NARR[t];ia++){
								Costarrsol[i][t]=Costarrsol[i][t]+Bgasol[i][t+1+ia]*H[t]*UnitT*((TGUC)(vt.elementAt(i))).Pcomb[t]*((TGUC)(vt.elementAt(i))).CespMeRA[t]*((TGUC)(vt.elementAt(i))).PaFx[t+1+ia][ia];
							}
						}
						costo_total = costo_total + Costarrsol[i][t];
						Costtotalsol[t]=Costtotalsol[t]+Costarrsol[i][t];
						Costopertotalsol[t]=Costopertotalsol[t]+Costarrsol[i][t];
					}
				}
				
				//Solucion Costo Rampa Parada
				Costparsol = new double[id_tgenerator.length][T];
				inicializa2double(Costparsol);
				for(int t=0;t<T;t++){
					for(int i=0;i<id_tgenerator.length;i++){
						if(((TGUC)(vt.elementAt(i))).GradRP[0]>0){
							for(int ip=0;ip<((TGUC)(vt.elementAt(i))).NPAR[t];ip++){
								Costparsol[i][t]=Costparsol[i][t]+Bgpsol[i][t-ip]*H[t]*UnitT*((TGUC)(vt.elementAt(i))).Pcomb[t]*((TGUC)(vt.elementAt(i))).CespMeRP[t]*((TGUC)(vt.elementAt(i))).PpFx[t-ip][ip];
							}
						}
						costo_total = costo_total + Costparsol[i][t];
						Costtotalsol[t]=Costtotalsol[t]+Costparsol[i][t];
						Costopertotalsol[t]=Costopertotalsol[t]+Costparsol[i][t];
					}
				}
				
				Costpartidasol = new double[id_tgenerator.length][T];
				inicializa2double(Costpartidasol);
				Costpartotalsol = new double[T];
				inicializa1double(Costpartotalsol);
				
				for(int t=0;t<T;t++){
					for(int i=0;i<id_tgenerator.length;i++){
						Costpartidasol[i][t]=cplex.getValue(((TGUC)(vt.elementAt(i))).Bga[t])*((TGUC)(vt.elementAt(i))).CPartida[0];			        	
						//costo_total = costo_total + Costpartidasol[i][t];
						costo_partida_total	= costo_partida_total+Costpartidasol[i][t];
						costo_total = costo_total + costo_partida_total;
						Costtotalsol[t]=Costtotalsol[t]+Costpartidasol[i][t];
						Costpartotalsol[t]=Costpartotalsol[t]+Costpartidasol[i][t];
						}
					}
				Costparadasol = new double[id_tgenerator.length][T];
				inicializa2double(Costparadasol);
				Costparadatotalsol = new double[T];
				inicializa1double(Costparadatotalsol);
				
				for(int t=0;t<T;t++){
					for(int i=0;i<id_tgenerator.length;i++){
						if(((TGUC)(vt.elementAt(i))).StopCost[0]>0){
							Costparadasol[i][t]=Bgpsol[i][t]*((TGUC)(vt.elementAt(i))).StopCost[t];
							//costo_total = costo_total + Costparadasol[i][t];	
							costo_detencion_total = costo_detencion_total+ Costparadasol[i][t];
							costo_total = costo_total + costo_detencion_total;							
							Costtotalsol[t]=Costtotalsol[t]+Costparadasol[i][t];
							Costparadatotalsol[t]=Costparadatotalsol[t]+Costparadasol[i][t];
						}
					}
				}
				
		
				//Solucion Potencia
				System.out.println("solucion potencia");
				Psol = new double[id_tgenerator.length][T];
				inicializa2double(Psol);
				for(int t=0;t<T;t++){
					for(int i=0;i<id_tgenerator.length;i++){
						Psol[i][t]=Psol[i][t]+((TGUC)(vt.elementAt(i))).Pming[t]*cplex.getValue(((TGUC)(vt.elementAt(i))).B[t][0]);
						Psol[i][t]=Psol[i][t]+cplex.getValue(((TGUC)(vt.elementAt(i))).P[0][t]);
						for(int r=0; r < id_busbar.length;r++){
							if(((DataUCBus)(vbus.elementAt(r))).Bus[t]==((TGUC)(vt.elementAt(i))).Barra[t]){
								Ptotal_barra[r][t]=Ptotal_barra[r][t]+Psol[i][t];
							}
						}
					}
				}
				System.out.println("solucion potencia arranque y parada");
				//Solucion Potencia de Arranque y Parada
				Pasol = new double[id_tgenerator.length][T];
				Ppsol = new double[id_tgenerator.length][T];
				for(int t=0;t<T;t++){
					for(int i=0;i<id_tgenerator.length;i++){
						//Potencia de Arranque
						Pasol[i][t] = 0;
						if(((TGUC)(vt.elementAt(i))).GradRA[0]>0){
							for(ia=0;ia<((TGUC)(vt.elementAt(i))).NARR[t];ia++){
								Pasol[i][t] = Pasol[i][t] + Bgasol[i][t+1+ia]*((TGUC)(vt.elementAt(i))).PaFx[t+1+ia][ia];
							}
						}
						//Potencia de Parada
						Ppsol[i][t] = 0;
						if(((TGUC)(vt.elementAt(i))).GradRP[0]>0){
							for(int ip=0;ip<((TGUC)(vt.elementAt(i))).NPAR[t];ip++){
								Ppsol[i][t] = Ppsol[i][t] + Bgpsol[i][t-ip]*((TGUC)(vt.elementAt(i))).PpFx[t-ip][ip];
							}
						}
					}
				}
				//Solucion volumen estanque combustible
				System.out.println("solucion estanque");
				Vsol = new double[id_tgenerator.length][T];
				inicializa2double(Vsol);
				for(int t=0;t<T;t++){
					for(int i=0;i<id_tgenerator.length;i++){
						if(((TGUC)(vt.elementAt(i))).Vmax[t]>0){
							Vsol[i][t]=cplex.getValue(((TGUC)(vt.elementAt(i))).V[t]);
						}
					}
				}
				
				//Solucion potencia positiva bateria
				Ppossol = new double[id_battery.length][T];
				inicializa2double(Ppossol);
				for(int t=0;t<T;t++){
					for(int i=0;i<id_battery.length;i++){
						Ppossol[i][t]=cplex.getValue(((BatteryUC)(vbatuc.elementAt(i))).Ppos[t]);
					}
				}
				//Solucion potencia negativa bateria
				Pnegsol = new double[id_battery.length][T];
				inicializa2double(Pnegsol);
				for(int t=0;t<T;t++){
					for(int i=0;i<id_battery.length;i++){
						Pnegsol[i][t]=cplex.getValue(((BatteryUC)(vbatuc.elementAt(i))).Pneg[t]);
					}
				}
				//Solucion potencia inversor
				Pisol = new double[id_battery.length][T];
				inicializa2double(Pisol);
				for(int t=0;t<T;t++){
					for(int i=0;i<id_battery.length;i++){
						Dtotal[t]=Dtotal[t]+((BatteryUC)(vbatuc.elementAt(i))).Pio[t];
						Pisol[i][t]=cplex.getValue(((BatteryUC)(vbatuc.elementAt(i))).Pi[t]);
						if((Pisol[i][t]+((BatteryUC)(vbatuc.elementAt(i))).Pio[t])>0){
							Ptotal[t]=Ptotal[t]+Pisol[i][t]+((BatteryUC)(vbatuc.elementAt(i))).Pio[t];	
						}
						else{
							Dtotal[t]=Dtotal[t]-Pisol[i][t]-((BatteryUC)(vbatuc.elementAt(i))).Pio[t];
						}
						
					}
				}
				//Solucion potencia minima
				Pbminsol = new double[id_battery.length][T];
				inicializa2double(Pbminsol);
				for(int t=0;t<T;t++){
					for(int i=0;i<id_battery.length;i++){
						Pbminsol[i][t]=cplex.getValue(((BatteryUC)(vbatuc.elementAt(i))).Pmin[t]);
					}
				}
				
				//Solucion energia bateria
				Vbsol = new double[id_battery.length][T];
				inicializa2double(Vbsol);
				for(int t=0;t<T;t++){
					for(int i=0;i<id_battery.length;i++){
						Vbsol[i][t]=cplex.getValue(((BatteryUC)(vbatuc.elementAt(i))).V[t]);
					}
				}
				//Solucion potencia por tramos positiva
				if(id_battery.length>0){
					Ebsol = new double[id_battery.length][((BatteryUC)(vbatuc.elementAt(0))).n_iv][T];
				}
				else{
					Ebsol = new double[id_battery.length][0][T];
				}
				for(int t=0;t<T;t++){
					for(int i=0;i<id_battery.length;i++){
						for(int s=0;s<((BatteryUC)(vbatuc.elementAt(i))).n_iv;s++){	
							Ebsol[i][s][t]=cplex.getValue(((BatteryUC)(vbatuc.elementAt(i))).E[s][t]);
						}
					}
				}
				//Solucion energia positiva
				Epsol = new double[id_battery.length][T];
				inicializa2double(Epsol);
				for(int t=0;t<T;t++){
					for(int i=0;i<id_battery.length;i++){
						//Epsol[i][t]=cplex.getValue(((BatteryUC)(vbatuc.elementAt(i))).Ep[t]);
					}
				}
				//Solucion energia negativa
				Ensol = new double[id_battery.length][T];
				inicializa2double(Ensol);
				for(int t=0;t<T;t++){
					for(int i=0;i<id_battery.length;i++){
						//Ensol[i][t]=cplex.getValue(((BatteryUC)(vbatuc.elementAt(i))).En[t]);
					}
				}
				System.out.println("fin solucion bateria");
				
				
				//Solucion variable binaria bomba electrica
				Bgbombsol = new double[id_pump.length][T];
				inicializa2double(Bgbombsol);
				for(int t=0;t<T;t++){
					for(int i=0;i<id_pump.length;i++){
						Bgbombsol[i][t]=cplex.getValue(((PumpUC)(vpumpuc.elementAt(i))).Bg[t]);
					}
				}
				//Solucion potencia bomba electrica
				Pbombsol = new double[id_pump.length][T];
				inicializa2double(Pbombsol);
				for(int t=0;t<T;t++){
					for(int i=0;i<id_pump.length;i++){
						Pbombsol[i][t]=((PumpUC)(vpumpuc.elementAt(i))).PBomba[t]*cplex.getValue(((PumpUC)(vpumpuc.elementAt(i))).Bg[t]);
						Dtotal[t]=Dtotal[t]+Pbombsol[i][t];
					}
				}
				System.out.println("fin solucion bomba electrica");
				
				//Solucion Volumen embalse			
				Vembsol 	= new double[id_reserv.length][T];
				Vficembsol 	= new double[id_reserv.length][T];
				Qverembsol 	= new double[id_reserv.length][T];
				B1sol		= new double[id_reserv.length][T];
				B2sol		= new double[id_reserv.length][T];
				
				
				inicializa2double(Vembsol);
				inicializa2double(Vficembsol);
				inicializa2double(Qverembsol);
				inicializa2double(B1sol);
				inicializa2double(B2sol);
				
				for(int t=0;t<T;t++){
					for(int i=0;i<id_reserv.length;i++){
						Vembsol[i][t]	=cplex.getValue(((ReservUC)(vresuc.elementAt(i))).V[t]);
						
						if(((ReservUC)(vresuc.elementAt(i))).Vermin[t]>0)
						Vficembsol[i][t]=cplex.getValue(((ReservUC)(vresuc.elementAt(i))).Vfic[t]);
						
						//if(((ReservUC)(vresuc.elementAt(i))).Vfin[t]>0 & t==(T-1))
						//if(((ReservUC)(vresuc.elementAt(i))).Vfin[t]>0 )
						Vficembsol[i][t]=Vficembsol[i][t]+cplex.getValue(((ReservUC)(vresuc.elementAt(i))).Vfic2[t]);
						
						Qverembsol[i][t]=cplex.getValue(((ReservUC)(vresuc.elementAt(i))).Qver[t]);
						B1sol[i][t]		=cplex.getValue(((ReservUC)(vresuc.elementAt(i))).B1[t]);
						//para evitar errores de redondeo de CPLEX
						if(B1sol[i][t]<0.01){
							B1sol[i][t]=0;
						}
						else if(B1sol[i][t]>0.99){
							B1sol[i][t]=1;
						}
						
						B2sol[i][t]		=cplex.getValue(((ReservUC)(vresuc.elementAt(i))).B2[t]);
						if(B2sol[i][t]<0.01){
							B2sol[i][t]=0;
						}
						else if(B2sol[i][t]>0.99){
							B2sol[i][t]=1;
						}
					}
				}
				System.out.println("fin solucion embalses");
				
				//CSP
				Pel1sol		= new double[id_csp.length][T];
				Pel2sol		= new double[id_csp.length][T];
				Palmsol		= new double[id_csp.length][T];
				Vcspsol		= new double[id_csp.length][T];
				
				for(int t=0;t<T;t++){
					for(int i=0;i<id_csp.length;i++){
						Pel1sol[i][t]=cplex.getValue(((CSPUC)(vcspuc.elementAt(i))).Pel1[t]);
						Pel2sol[i][t]=cplex.getValue(((CSPUC)(vcspuc.elementAt(i))).Pel2[t]);
						Palmsol[i][t]=cplex.getValue(((CSPUC)(vcspuc.elementAt(i))).Palm[t]);
						Vcspsol[i][t]=cplex.getValue(((CSPUC)(vcspuc.elementAt(i))).V[t]);
						Ptotal[t]=Ptotal[t]+Pel1sol[i][t]+Pel2sol[i][t];
					}
				}
				System.out.println("fin CSP");
				
				
				//Solucion potencia eolica
				Peolsol = new double[id_eolic.length][T];
				for(int t=0;t<T;t++){
					for(int i=0;i<id_eolic.length;i++){
						Peolsol[i][t]=cplex.getValue(((EolicUC)(veoluc.elementAt(i))).Peol[t]);
						Ptotal[t]=Ptotal[t]+Peolsol[i][t];
						generacion_eolic[t]=generacion_eolic[t]+Peolsol[i][t];
						for(int r=0; r < id_busbar.length;r++){
							if(((DataUCBus)(vbus.elementAt(r))).Bus[t]==((EolicUC)(veoluc.elementAt(i))).Barra[t]){
								Ptotal_barra[r][t]=Ptotal_barra[r][t]+Peolsol[i][t];
							}
						}
					}
				}
				
				System.out.println("fin solucion eolicas");
				
				//Solucion potencia centrales hidraulicas
				Phsol 	= new double[id_hgenerator.length][T];
				Qhsol	= new double[id_hgenerator.length][T];
				Bhsol	= new double[id_hgenerator.length][T];
				Qversol = new double[id_hgenerator.length][T];
				Vficsol = new double[id_hgenerator.length][T];
				
				inicializa2double(Phsol);
				inicializa2double(Qhsol);
				inicializa2double(Bhsol);
				inicializa2double(Qversol);
				inicializa2double(Vficsol);
				
				for(int t=0;t<T;t++){
					for(int i=0;i<id_hgenerator.length;i++){
						
						Phsol[i][t]=cplex.getValue(((HGUC)(vh.elementAt(i))).B[t][0])*((HGUC)(vh.elementAt(i))).Pminuc[0][t]+cplex.getValue(((HGUC)(vh.elementAt(i))).P[0][t]);
						
						if(((HGUC)(vh.elementAt(i))).Rend[0]>0){
							Qhsol[i][t]=Phsol[i][t]/((HGUC)(vh.elementAt(i))).Rend[0];
						
						}else{
							Qhsol[i][t]=0;
						}
						
						//si la central es del tipo R, la potencia se hace 0 ya que se trata de una unidad ficticia
						if((((HGUC)(vh.elementAt(i))).Type[0]).equals("R") || ((HGUC)(vh.elementAt(i))).Barra[0]==0){
							Phsol[i][t]=0;	
						}
						
						Bhsol[i][t]=(double)cplex.getValue(((HGUC)(vh.elementAt(i))).B[t][0]);
						
						Ptotal[t]=Ptotal[t] +Phsol[i][t];
						
						for(int r=0; r < id_busbar.length;r++){
							if(((DataUCBus)(vbus.elementAt(r))).Bus[t]==((HGUC)(vh.elementAt(i))).Barra[t]){
								Ptotal_barra[r][t]=Ptotal_barra[r][t]+Phsol[i][t];
							}
						}
						
						try{
							Qversol[i][t]=cplex.getValue(((HGUC)(vh.elementAt(i))).Qver[t]);
						}
						catch(IloException e){
							//if(t==0)
							//System.out.println("La solucion de la variable Qversol no esta disponible para: "+ ((HGUC)(vh.elementAt(i))).Nombre[0]);
							//System.err.println("Concert exception '" + e + "' caught");
						}
						try{
							if(((HGUC)(vh.elementAt(i))).Commitment[t]==0 & ((HGUC)(vh.elementAt(i))).Pming[t]>0)
							Vficsol[i][t]=cplex.getValue(((HGUC)(vh.elementAt(i))).Vfic[t]);
							
							if(((HGUC)(vh.elementAt(i))).Qvmin[t]>0)
							Vficsol[i][t]=Vficsol[i][t]+cplex.getValue(((HGUC)(vh.elementAt(i))).Vfic2[t]);
							
						}
						catch(IloException e){
							if(t==0)
							System.out.println("La solucion de la varible Vfic no esta disponible para: "+ ((HGUC)(vh.elementAt(i))).Nombre[0]);
							System.err.println("Concert exception '" + e + "' caught");
						}
					}
				}
				System.out.println("fin solucion potencia hidraulica");
				
				//Solucion potencia solar
				Psolsol = new double[id_solar.length][T];
				for(int t=0;t<T;t++){
					for(int i=0;i<id_solar.length;i++){
						Psolsol[i][t]=cplex.getValue(((SolarUC)(vsoluc.elementAt(i))).Psol[t]);
						Ptotal[t]=Ptotal[t]+Psolsol[i][t];
						generacion_solar[t]=generacion_solar[t]+Psolsol[i][t];
						for(int r=0; r < id_busbar.length;r++){
							if(((DataUCBus)(vbus.elementAt(r))).Bus[t]==((SolarUC)(vsoluc.elementAt(i))).Barra[t]){
								Ptotal_barra[r][t]=Ptotal_barra[r][t]+Psolsol[i][t];
							}
						}
					}
				}
				System.out.println("fin solucion potencia solar");
				
				//Potencia total
				Patotal=new double[T];
				Pptotal=new double[T];
				for(int t=0;t<T;t++){
					for(int i=0;i<id_tgenerator.length;i++){
						Pt[i][t]=Psol[i][t]+Pasol[i][t]+Ppsol[i][t];
						Ptotal[t]=Ptotal[t]+Pt[i][t];
						Patotal[t]=Patotal[t]+Pasol[i][t];
						Pptotal[t]=Pptotal[t]+Ppsol[i][t];
					}
				}
				for(int t=0;t<T;t++){
						for (int i=0;i<id_load.length;i++){
							Dtotal[t]=Dtotal[t]+demanda[i][t];
						}
				}
				for(int t=0;t<T;t++){
						Perdidatotal[t]=Ptotal[t]-Dtotal[t];
				}
				//Lineas transmision
				Fsol 	= new double[id_line.length][T];
				
				if(bandera_perdida==false){
					for(int t=0;t<T;t++){
						for(int i=0;i<id_line.length;i++){
							Fsol[i][t] = cplex.getValue(((LineUC)(vlineuc.elementAt(i))).F[t]);
						}
					}
				}
				else{
					for(int t=0;t<T;t++){
						for(int i=0;i<id_line.length;i++){
							for(int s=0;s<(((LineUC)(vlineuc.elementAt(i))).matriz_alpha).length;s++){
								Fsol[i][t] = Fsol[i][t]+cplex.getValue(((LineUC)(vlineuc.elementAt(i))).Fp[s][t])+cplex.getValue(((LineUC)(vlineuc.elementAt(i))).Fn[s][t]);
							}
						}
					}
				}
				
				//Angulo de las barras
				
				AnguloSol 	= new double[id_busbar.length][T];
				inicializa2double(AnguloSol);
//				for(int t=0;t<T;t++){
//					
//					for(int i=0;i<id_busbar.length;i++){
//						AnguloSol[i][t] = cplex.getValue(((BusUC)(vbusuc.elementAt(i))).Theta[t]);
//						System.out.println(AnguloSol[i][t]);	
//					}
//				}
				
				
				
				//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				//Reserva en giro 1 y reserva en giro 2, solucion:
				
				//Reserva en giro total por periodo
 				Rtotal = new double[T];	
				//Reserva en giro v2 total por periodo 
				Rtotal2 = new double[T];	
				
				//Reserva en giro centrales termicas
				Rgsol = new double[id_tgenerator.length][T];
				//Reserva en giro v2 centrales termicas
				Rgsol2 = new double[id_tgenerator.length][T];
				
				//Reserva en giro centrales hidraulicas
				Rgsol_hidro = new double[id_hgenerator.length][T];
				//Reserva en giro v2 centrales hidraulicas
				Rgsol2_hidro = new double[id_hgenerator.length][T];
				
				// Reserva en giro centrales eolica
				Rgeolsol  = new double[id_eolic.length][T];
				Rgeolsol2 = new double[id_eolic.length][T];
				
				// Reserva en giro centrales solares
				Rgsolsol = new double[id_solar.length][T];
				Rgsolsol2 = new double[id_solar.length][T];
				
				//reserva en giro para centrales termicas limitadas
				Rgmaxsol = new double[id_tgenerator.length][T];
				Rgmaxsol2 = new double[id_tgenerator.length][T];
				
				//reserva en giro para centrales hidraulicas limitadas
				Rgmaxsol_hidro 	= new double[id_hgenerator.length][T];
				Rgmaxsol2_hidro = new double[id_hgenerator.length][T];
				
				inicializa1double(Rtotal);
				inicializa1double(Rtotal2);
				inicializa2double(Rgsol);
				inicializa2double(Rgsol2);
				inicializa2double(Rgsol_hidro);
				inicializa2double(Rgsol2_hidro);
				inicializa2double(Rgeolsol);
				inicializa2double(Rgeolsol2);
				inicializa2double(Rgsolsol);
				inicializa2double(Rgsolsol2);
				
				inicializa2double(Rgmaxsol);
				inicializa2double(Rgmaxsol2);
				inicializa2double(Rgmaxsol_hidro);
				inicializa2double(Rgmaxsol2_hidro);
				
				
				for(int t=0;t<T;t++){
					//centrales termicas
					for(int i=0;i<id_tgenerator.length;i++){
						if(((TGUC)(vt.elementAt(i))).Unavalaible[t]!=1){
							//para unidades sin limite se reserva en giro
							if((((TGUC)(vt.elementAt(i))).SpinningMax[t]-(((TGUC)(vt.elementAt(i))).Pmaxg[t]-((TGUC)(vt.elementAt(i))).Pming[t]))<0){
								Rgsol[i][t] = cplex.getValue(((TGUC)(vt.elementAt(i))).Rg[t]);
							}
							else{
								Rgsol[i][t]=Bsol[i][0][t]*((TGUC)(vt.elementAt(i))).Pmaxg[t]-Psol[i][t];
							}
						
							Rgsol2[i][t] = cplex.getValue(((TGUC)(vt.elementAt(i))).Rg2[t]);			
							
							Rtotal[t] =Rtotal[t]+Rgsol[i][t];
							Rtotal2[t]=Rtotal2[t]+Rgsol2[i][t];
							
						}
					}
					//centrales hidraulicas
					for(int i=0;i<id_hgenerator.length;i++){
						//solo se consideran centrales conectadas a barra real
						if(((HGUC)(vh.elementAt(i))).Barra[t]>0){
							if(((HGUC)(vh.elementAt(i))).Unavalaible[t]!=1){
								//para unidades sin limite se reserva en giro
								//if((((HGUC)(vh.elementAt(i))).SpinningMax[t]-(((HGUC)(vh.elementAt(i))).Pmaxg[t]-((HGUC)(vh.elementAt(i))).Pming[t]))>=0){
								//	Rgsol_hidro[i][t]=Bhsol[i][t]*((HGUC)(vh.elementAt(i))).Pmaxg[t]-Phsol[i][t];
								//}
								//else{
									Rgsol_hidro[i][t] = cplex.getValue(((HGUC)(vh.elementAt(i))).Rg[t]);
								//}
								
								
								Rgsol2_hidro[i][t] = cplex.getValue(((HGUC)(vh.elementAt(i))).Rg2[t]);
								
								Rtotal[t] =Rtotal[t]+Rgsol_hidro[i][t];
								Rtotal2[t]=Rtotal2[t]+Rgsol2_hidro[i][t];
							}
						}
					}
					//centrales eolicas
					for(int i=0;i<id_eolic.length;i++){
						if(((EolicUC)(veoluc.elementAt(i))).SpinningMax[t]>0)
						Rgeolsol[i][t]= cplex.getValue(((EolicUC)(veoluc.elementAt(i))).Rg[t]);
						
						if(((EolicUC)(veoluc.elementAt(i))).SpinningMax2[t]>0)
						Rgeolsol2[i][t]= cplex.getValue(((EolicUC)(veoluc.elementAt(i))).Rg2[t]);
						
						Rtotal[t]=Rtotal[t]+Rgeolsol[i][t];
						Rtotal2[t]=Rtotal2[t]+Rgeolsol2[i][t];
					}
					//centrales solares
					for(int i=0;i<id_solar.length;i++){
						if(((SolarUC)(vsoluc.elementAt(i))).SpinningMax[t]>0)
						Rgsolsol[i][t]= cplex.getValue(((SolarUC)(vsoluc.elementAt(i))).Rg[t]);
						
						if(((SolarUC)(vsoluc.elementAt(i))).SpinningMax2[t]>0)
						Rgsolsol2[i][t]= cplex.getValue(((SolarUC)(vsoluc.elementAt(i))).Rg2[t]);
						
						Rtotal[t]=Rtotal[t]+Rgsolsol2[i][t];
					}
				}
				
				System.out.println("fin solucion reserva en giro total");
				
				//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				//Control primario de frecuencia, solucion:
				
				CPFtotal 		= new double[T];	
				//Contro primario centrales termicas
				CPFsol 			= new double[id_tgenerator.length][T];
				//Contro primario centrales hidraulicas
				CPFsol_hidro 	= new double[id_hgenerator.length][T];
				//Control primario centrales eolica
				CPFeolsol = new double[id_eolic.length][T];
				//Control primario centrales solar
				CPFsolsol = new double[id_solar.length][T];
				
				inicializa1double(CPFtotal);		
				inicializa2double(CPFsol);		
				inicializa2double(CPFsol_hidro);		
				inicializa2double(CPFeolsol);		
				inicializa2double(CPFsolsol);		
				
				for(int t=0;t<T;t++){
					for(int i=0;i<id_tgenerator.length;i++){
						CPFsol[i][t]=Bsol[i][0][t]*((TGUC)(vt.elementAt(i))).CPFMax[t];
						CPFtotal[t]=CPFtotal[t]+CPFsol[i][t];
					}
					for(int i=0;i<id_hgenerator.length;i++){
						CPFsol_hidro[i][t]=Bhsol[i][t]*((HGUC)(vh.elementAt(i))).CPFMax[t];
						CPFtotal[t]=CPFtotal[t]+CPFsol_hidro[i][t];
					}
					for(int i=0;i<id_eolic.length;i++){
						if(((EolicUC)(veoluc.elementAt(i))).CPFmax[t]>0)
						CPFeolsol[i][t]= cplex.getValue(((EolicUC)(veoluc.elementAt(i))).CPF[t]);
						CPFtotal[t]=CPFtotal[t]+CPFeolsol[i][t];
					}
					for(int i=0;i<id_solar.length;i++){
						if(((SolarUC)(vsoluc.elementAt(i))).CPFmax[t]>0)
						CPFsolsol[i][t]= cplex.getValue(((SolarUC)(vsoluc.elementAt(i))).CPF[t]);
						CPFtotal[t]=CPFtotal[t]+CPFsolsol[i][t];
					}
					
				}
				System.out.println("fin solucion CPF");
				///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			
				//Reserva pronta 
				ResPronsol 		= new double[T];
				RPronsol 		= new double[id_tgenerator.length][T];
				RPronsol_hidro 	= new double[id_hgenerator.length][T];
				
				inicializa1double(ResPronsol);		
				inicializa2double(RPronsol);
				
				for(int t=0;t<T;t++){
					ResPronsol[t]=0;
					for(int i=0;i<id_tgenerator.length;i++){
						if(((TGUC)(vt.elementAt(i))).Unavalaible[t]!=1){
							if(((TGUC)(vt.elementAt(i))).Reserva_Pronta[t]==1){ 
								RPronsol[i][t] = (1-Bsol[i][0][t])*((TGUC)(vt.elementAt(i))).Pmaxg[t];
								ResPronsol[t]=ResPronsol[t]+RPronsol[i][t];
							}
						}
					}
					for(int i=0;i<id_hgenerator.length;i++){
						if(((HGUC)(vh.elementAt(i))).Unavalaible[t]!=1){
							if(((HGUC)(vh.elementAt(i))).Reserva_Pronta[t]==1){ 
								RPronsol_hidro[i][t] = (1-Bhsol[i][t])*((HGUC)(vh.elementAt(i))).Pmaxg[t];
								ResPronsol[t]=ResPronsol[t]+RPronsol_hidro[i][t];
							}
						}
					}
				}
				System.out.println("fin solucion Reserva pronta");
				
				//Solucion consumos propios
				Owtotal = new double[T];	
				for(int t=0;t<T;t++){
					Owtotal[t]=0;
					for(int i=0;i<id_tgenerator.length;i++){
						Owtotal[t]=Owtotal[t]+Bsol[i][0][t]*((TGUC)(vt.elementAt(i))).OwnConsuption[t];
						if(((TGUC)(vt.elementAt(i))).GradRA[0]>0){
							for(ia=0;ia<((TGUC)(vt.elementAt(i))).NARR[t];ia++){
								Owtotal[t]=Owtotal[t]+Bgasol[i][t+1+ia]*((TGUC)(vt.elementAt(i))).OwnConsuption[t];
							}
						}
						if(((TGUC)(vt.elementAt(i))).GradRP[0]>0){
							for(int ip=0;ip<((TGUC)(vt.elementAt(i))).NPAR[t];ip++){
								Owtotal[t]=Owtotal[t]+Bgpsol[i][t-ip]*((TGUC)(vt.elementAt(i))).OwnConsuption[t];
							}
						}
					}
					for(int i=0;i<id_hgenerator.length;i++){
						Owtotal[t]=Owtotal[t]+Bhsol[i][t]*((HGUC)(vh.elementAt(i))).OwnConsuption[t];
					}
					
				}
				System.out.println("fin solucion consumos propias");
				
				//Solucion energia no suministrada
				Enssol = new double[id_load.length][((LoadUC)(vloaduc.elementAt(0))).n_iv][T];
				for(int t=0;t<T;t++){
					for(int k=0;k<id_load.length;k++){
						for(int s=0;s<((LoadUC)(vloaduc.elementAt(k))).n_iv;s++){
							Enssol[k][s][t] = cplex.getValue(((LoadUC)(vloaduc.elementAt(k))).Ens[s][t]);
							Costoens[t]=Enssol[k][s][t]*H[t]*UnitT*((LoadUC)(vloaduc.elementAt(k))).Alfa[s][t];
							costototalens=costototalens+Costoens[t];
							//costo_total = costo_total + costototalens;	
						}
					}
				}
				System.out.println("fin solucion energia no suministrado");
				
				//Solucion energia perdida
				Eperdsol = new double[T][id_load.length];
				inicializa2double(Eperdsol);
				for(int t=0;t<T;t++){
					for(int k=0;k<id_load.length;k++){
						if(((LoadUC)(vloaduc.elementAt(k))).Eperdmin[t]<0){
							Eperdsol[t][k]=cplex.getValue(((LoadUC)(vloaduc.elementAt(k))).Eperd[t]);
						}
					}
				}
				System.out.println("fin solucion energia perdida");
				
				//Solucion Cd
				Cdsol = new double[id_load.length][T];
				for(int t=0;t<T;t++){
					for(int k=0;k<id_load.length;k++){
						if(((LoadUC)(vloaduc.elementAt(k))).Cdmin[t] != 1 || ((LoadUC)(vloaduc.elementAt(k))).Cdmax[t] != 1 ){
							Cdsol[k][t] = cplex.getValue(((LoadUC)(vloaduc.elementAt(k))).Cd[t]);
						}
						else{
							Cdsol[k][t] = 1;
						}
					}
				}
				//Solucion semaforo
				for(int k=0;k<id_load.length;k++){
					for(int t=0;t<T;t++){
						if(((LoadUC)(vloaduc.elementAt(k))).Cdmin[t] != 1 || ((LoadUC)(vloaduc.elementAt(k))).Cdmax[t] != 1 ){
							C0 = ((LoadUC)(vloaduc.elementAt(k))).Cdmin[t] + (1-((LoadUC)(vloaduc.elementAt(k))).Cdmin[t])/2; 
							C1 = 1 + (((LoadUC)(vloaduc.elementAt(k))).Cdmax[t]-1)/2;
							
							if(Cdsol[k][t]<C0){
								Semaforo[t]=1;
							}
							else if(Cdsol[k][t]>=C0 & Cdsol[k][t]<C1){
								Semaforo[t]=2;
							}
							else if(Cdsol[k][t]>=C1){
								Semaforo[t]=3;
							}
						}
						else{
							Semaforo[t]=2;
						}
					}
				}
				//Solucion Demanda real Dr
				Drsol = new double[id_load.length][T];
				for(int t=0;t<T;t++){
					for(int k=0;k<id_load.length;k++){
						if(((LoadUC)(vloaduc.elementAt(k))).Cdmin[t] != 1 || ((LoadUC)(vloaduc.elementAt(k))).Cdmax[t] != 1 ){
							Drsol[k][t] = cplex.getValue(((LoadUC)(vloaduc.elementAt(k))).Dr[t]);
						}
						else{
							Drsol[k][t]=0;
						}
					}
				}		
				//Costo Medio
				CMedio = new double[id_tgenerator.length][T];
				for(int t=0;t<T;t++){
					for(int i=0;i<id_tgenerator.length;i++){
						CMedio[i][t] = 0;
						if(Psol[i][t]>0){
							CMedio[i][t] = Costsol[i][t]/(H[t]*Psol[i][t]);
						}
					}
				}
				//Numero de empresas
				int ss=0;
				boolean banderanombre=false;
				nomempresterm = new String[id_tgenerator.length];
				nempresterm=0;
				
				for(int i=0;i<id_tgenerator.length;i++){
					nomempresterm[i]="";
				}
				for(int i=0;i<id_tgenerator.length;i++){
					nombreaux=((TGUC)(vt.elementAt(i))).Propietario[0];
					//System.out.println("PROPIETARIO= "+nombreaux);
					banderanombre=true;
					for(ii=0;ii<id_tgenerator.length;ii++){
						if(nombreaux.equals(nomempresterm[ii])){
							banderanombre=false;
						}
					}
					if(banderanombre){
						nomempresterm[ss]=nombreaux;
						nempresterm++;
						//System.out.println("NOMBRE EMPRESA= "+nomempresterm[ss]);
						ss++;
					}
				}
				System.out.println("Costo Total (costo termico)= "+costo_total);
				//Ordenar
				orden = new int[T][id_tgenerator.length];
				for(int t=0;t<T;t++){
					for(int i=0;i<id_tgenerator.length;i++){
						orden[t][i]=i;
					}
				}
				Ordenar(orden,CMedio,T);
				//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				boolean chequeah1=false;
				boolean bandera_demanda_hora=false;
				double demanda_hora=0;
				double porcentage_demanda=0.9;
				int contaa=0;
				sum_hora=0;
				
				for(int t=0;t<T;t++){
					int iv=0;
					tt=0;
					tini_hora=sum_hora;
					while(tt<H[t]){
						ii=0;
						bandera_demanda_hora=false;
						ii=0;
						demanda_hora=0;
						while(ii<id_tgenerator.length & bandera_demanda_hora==false){
							chequeah1=false;
							demanda_hora=Psol[orden[t][id_tgenerator.length-ii-1]][t]+demanda_hora;
							if(Psol[orden[t][id_tgenerator.length-ii-1]][t]>0.0001 & redondear(demanda_hora,4)<=redondear(Math.min(demanda_original[0][tini_hora+tt]*porcentage_demanda,(demanda[0][t]-Enssol[0][0][t]-Patotal[t]-Pptotal[t])),4)){ //falta corregir el ENS
								bandera_demanda_hora=false;
								//variable Bg: Fijo en 1 
								set_bgh1[orden[t][id_tgenerator.length-ii-1]][tini_hora+tt]=1;
								//busco tramo que se activa para variable B
								contaa=0;
								iv=0;
								while(iv<((TGUC)vt.elementAt(orden[t][id_tgenerator.length-ii-1])).Niv[t] & chequeah1==false){
									//cheque que la potencia este entre pmin y pmax => Pmin<=P<=Pmax
									if(redondear(Psol[orden[t][id_tgenerator.length-ii-1]][t],4)>=redondear(((TGUC)vt.elementAt(orden[t][id_tgenerator.length-ii-1])).Pminuc[((TGUC)vt.elementAt(orden[t][id_tgenerator.length-ii-1])).M[t][iv][0]][t],4) & redondear(Psol[orden[t][id_tgenerator.length-ii-1]][t],4)<=redondear(((TGUC)vt.elementAt(orden[t][id_tgenerator.length-ii-1])).Pmaxuc[((TGUC)vt.elementAt(orden[t][id_tgenerator.length-ii-1])).M[t][iv][1]][t],4)){
										//variable B: Fijo en 1 
										set_bh1[orden[t][id_tgenerator.length-ii-1]][iv][tini_hora+tt]=1;
										chequeah1=true;
										contaa++;
									}
									else{
										set_bh1[orden[t][id_tgenerator.length-ii-1]][iv][tini_hora+tt]=0;
									}
									iv++;										
								}
								if(chequeah1==false){
									System.out.println("NO TIENE ACTIVADO NINGUN TRAMO");
									System.out.println("Central= "+((TGUC)vt.elementAt(orden[t][id_tgenerator.length-ii-1])).Nombre[t] + " ID= "+((TGUC)vt.elementAt(orden[t][id_tgenerator.length-ii-1])).ID[t]);
									System.out.println("Periodo= "+t);
									System.out.println("Potencia= "+Psol[orden[t][id_tgenerator.length-ii-1]][t]);
									thisThread.suspend();
								}
								if(contaa>1){
									System.out.println("ERROR CONTA");
									thisThread.suspend();
								}
							}	
							else{
								bandera_demanda_hora=true;
							}
							ii=ii+1;
						}
						tt=tt+1;
					}
					sum_hora=sum_hora+H[t];
				}
				
				//pw4.close();
				
				
			}
			//FIN IMPRIME SOLUCION
			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			System.out.println("");
			//Eliminando conversion
			System.out.println("Eliminando conversion");
			//centrales termicas
			//B
			for(int i=0;i<id_tgenerator.length;i++){
				for(int t=0;t<T;t++){
					cplex.delete((IloCopyable)conv_b[i][t]);              
				}
			}
			//Bga, Bgp, X, Y
			for(int i=0;i<id_tgenerator.length;i++){          
				cplex.delete(conv_bga[i]);
				cplex.delete(conv_bgp[i]); 
				cplex.delete(conv_x[i]);
				cplex.delete(conv_y[i]);    	
			}
			
			//centrales hidro
			//Bh
			for(int i=0;i<id_hgenerator.length;i++){
				for(int t=0;t<T;t++){
					cplex.delete((IloCopyable)conv_bh[i][t]);              
				}
			}
			//Bgah, Bgph, Xh, Yh
			for(int i=0;i<id_hgenerator.length;i++){
				cplex.delete(conv_bgah[i]);
				cplex.delete(conv_bgph[i]);
				cplex.delete(conv_xh[i]);
				cplex.delete(conv_yh[i]);
			}
			
			//bomba electrica
			for(int k=0;k<id_pump.length;k++){
				cplex.delete(conv_bgbomb[k]);             
			}
			//battery
			for(int k=0;k<id_battery.length;k++){
				cplex.delete(conv_bgbatp[k]);              
			} 	
			for(int k=0;k<id_battery.length;k++){
				cplex.delete(conv_bgbatn[k]);              
			}
			//tramos pmin bateria
			for(int k=0;k<id_battery.length;k++){
				for(int s=0;s<((BatteryUC)(vbatuc.elementAt(k))).n_iv;s++){
					cplex.delete(conv_bb[k][s]);              
				}
			}
			//CSP
			for(int k=0;k<id_csp.length;k++){
				cplex.delete(conv_csp_bel1[k]); 	
				cplex.delete(conv_csp_balm[k]);
				cplex.delete(conv_csp_bg[k]);
			}
			//Embalse
			for(int k=0;k<id_reserv.length;k++){
				cplex.delete(conv_b1[k]);              
				cplex.delete(conv_b2[k]);
			}
			
			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			ia=0;
			System.out.println("Fijando variables binarias para obtener costos marginales");
			//centrales termicas
			//B
			for(int i=0;i<id_tgenerator.length;i++){
				for(int t=0;t<T;t++){
					for(int iv=0;iv<((TGUC)(vt.elementAt(i))).Niv[t];iv++){
						(((TGUC)(vt.elementAt(i))).B[t][iv]).setLB(Bsol[i][iv][t]);
						(((TGUC)(vt.elementAt(i))).B[t][iv]).setUB(Bsol[i][iv][t]);
					}
				}
			}
			//Bga, Bgp
			for(int i=0;i<id_tgenerator.length;i++){
				for(int t=0;t<T;t++){
					(((TGUC)(vt.elementAt(i))).Bga[t]).setLB(Bgasol[i][t]);
					(((TGUC)(vt.elementAt(i))).Bga[t]).setUB(Bgasol[i][t]);
					(((TGUC)(vt.elementAt(i))).Bgp[t]).setLB(Bgpsol[i][t]);
					(((TGUC)(vt.elementAt(i))).Bgp[t]).setUB(Bgpsol[i][t]);
				}
			}
			//centrales hidro
			//Bh
			for(int i=0;i<id_hgenerator.length;i++){
				for(int t=0;t<T;t++){
					//por ahoro solo se admite un tramo para la curva de costos de la centrales hidro -> Corregir para futuras versiones
					(((HGUC)(vh.elementAt(i))).B[t][0]).setLB(Bhsol[i][t]);
					(((HGUC)(vh.elementAt(i))).B[t][0]).setUB(Bhsol[i][t]);
				}
			}
			//Bomba electrica
			for(int k=0;k<id_pump.length;k++){
				for(int t=0;t<T;t++){
					(((PumpUC)(vpumpuc.elementAt(k))).Bg[t]).setLB(Bgbombsol[k][t]);
					(((PumpUC)(vpumpuc.elementAt(k))).Bg[t]).setUB(Bgbombsol[k][t]);
				}  
			}
			//Bateria binaria positiva
			for(int t=0;t<T;t++){
				for(int i=0;i<id_battery.length;i++){
					(((BatteryUC)(vbatuc.elementAt(i))).Bpos[t]).setLB(Bpossol[i][t]);
					(((BatteryUC)(vbatuc.elementAt(i))).Bpos[t]).setUB(Bpossol[i][t]);
				}
			}
			//Bateria binaria negativa
			for(int t=0;t<T;t++){
				for(int i=0;i<id_battery.length;i++){
					(((BatteryUC)(vbatuc.elementAt(i))).Bneg[t]).setLB(Bnegsol[i][t]);
					(((BatteryUC)(vbatuc.elementAt(i))).Bneg[t]).setUB(Bnegsol[i][t]);
				}
			}
			for(int t=0;t<T;t++){
				for(int i=0;i<id_battery.length;i++){
					for(int s=0;s<((BatteryUC)(vbatuc.elementAt(i))).n_iv;s++){	
						(((BatteryUC)(vbatuc.elementAt(i))).B[s][t]).setLB(Bbsol[i][s][t]);
						(((BatteryUC)(vbatuc.elementAt(i))).B[s][t]).setUB(Bbsol[i][s][t]);
					}
				}
			}
			//CSP
			for(int t=0;t<T;t++){
				for(int i=0;i<id_csp.length;i++){
					(((CSPUC)(vcspuc.elementAt(i))).Bel1[t]).setLB(Bel1sol[i][t]);
					(((CSPUC)(vcspuc.elementAt(i))).Bel1[t]).setUB(Bel1sol[i][t]);
					(((CSPUC)(vcspuc.elementAt(i))).Balm[t]).setLB(Balmsol[i][t]);
					(((CSPUC)(vcspuc.elementAt(i))).Balm[t]).setUB(Balmsol[i][t]);
					(((CSPUC)(vcspuc.elementAt(i))).Bg[t]).setLB(Bgcspsol[i][t]);
					(((CSPUC)(vcspuc.elementAt(i))).Bg[t]).setUB(Bgcspsol[i][t]);
				}
			}
			//Embalse
			for(int t=0;t<T;t++){
				for(int i=0;i<id_reserv.length;i++){
					(((ReservUC)(vresuc.elementAt(i))).B1[t]).setLB(B1sol[i][t]);
					(((ReservUC)(vresuc.elementAt(i))).B1[t]).setUB(B1sol[i][t]);
					(((ReservUC)(vresuc.elementAt(i))).B2[t]).setLB(B2sol[i][t]);
					(((ReservUC)(vresuc.elementAt(i))).B2[t]).setUB(B2sol[i][t]);
				}
			}
			
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			System.out.println("");
			System.out.println("Resolviendo con variables enteras fijas");
			System.out.println("");
			if (MainUC.debMode){
					System.out.println("Creando archivo lp con variables binarias fijas");
					cplex.exportModel(unitfileroot+"UC_LP.lp");	
			}
			if(cplex.solve()){
				System.out.println("");
				System.out.println("Funcion objetivo de LP= "+cplex.getObjValue());
				System.out.println("");
				System.out.println("Costo Total (costo termico)= "+costo_total);
				System.out.println("");
			}
			else{
				System.out.println("PROBLEMA CONTINUO INFACTIBLE");
				System.out.println("Creando archivo lp con variables binarias fijas");
				cplex.exportModel(unitfileroot+"UC_LP.lp");	
				//thisThread.suspend();
				System.exit(-1);
			}
			
		try{	
			//Periodo
			sum_t=0;
			pw.print(",,Nodo,Periodo,");
			pw14.print("Periodo,");
			pw15.print("Periodo,");
			
			for(int t=0;t<T;t++){
				if(t<T-1){
					for(int k=1;k<=H[t];k++){
						sum_t=sum_t+1;
						pw.print(sum_t+",");
						pw14.print(sum_t+",");
						pw15.print(sum_t+",");
					}
				}
				else{
					for(int k=1;k<=H[t];k++){
						sum_t=sum_t+1;
						if(k==H[t]){
							pw.println(sum_t);
							pw14.println(sum_t);
							pw15.println(sum_t);
						}
						else{
							pw.print(sum_t+",");			
							pw14.print(sum_t+",");			
							pw15.print(sum_t+",");			
						
						}	
					}
				}
			}
			//Costo Marginal
			for(int r=0; r < id_busbar.length;r++){
				for(int t=0;t<T;t++){
					if(t==0){
						for(int k=1;k<=H[t];k++){
							if(k==1){
								pw.print(",,"+((DataUCBus)(vbus.elementAt(r))).Nombre[0]+",Costo Marginal,"+cplex.getDual(restric_demanda[r][t])/H[t]/fes+",");	
								pw14.print(((DataUCBus)(vbus.elementAt(r))).Nombre[0]+","+cplex.getDual(restric_demanda[r][t])/H[t]/fes+",");	
							}
							else{
								pw.print(cplex.getDual(restric_demanda[r][t])/H[t]/fes+",");
								pw14.print(cplex.getDual(restric_demanda[r][t])/H[t]/fes+",");		
							}
						}
					}
					else if(t>0 & t<T-1){
						for(int k=1;k<=H[t];k++){
							pw.print(cplex.getDual(restric_demanda[r][t])/fes+",");		
							pw14.print(cplex.getDual(restric_demanda[r][t])/fes+",");		
						
						}		
					}
					else{
						for(int k=1;k<=H[t];k++){
							if(k==H[t]){
								pw.println(cplex.getDual(restric_demanda[r][t])/H[t]/fes);		
								pw14.println(cplex.getDual(restric_demanda[r][t])/H[t]/fes);		
							}
							else{
								pw.print(cplex.getDual(restric_demanda[r][t])/H[t]/fes+",");			
								pw14.print(cplex.getDual(restric_demanda[r][t])/H[t]/fes+",");			
							
							}		
						}		
					}	
				}
			}
			
			//POTENCIA POR BARRA
			for(int r=0; r < id_busbar.length;r++){	
				for(int t=0;t<T;t++){
					if(t==0){
						for(int k=1;k<=H[t];k++){
							if(k==1){
								pw.print(",,"+((DataUCBus)(vbus.elementAt(r))).Nombre[0]+",PBARRA,"+Ptotal_barra[r][t]+",");	
								}
							else{
								pw.print(Ptotal_barra[r][t]+",");
							}
						}
					}
					else if(t>0 & t<T-1){
						for(int k=1;k<=H[t];k++){
							pw.print(Ptotal_barra[r][t]+",");		
							}		
					}
					else{
						for(int k=1;k<=H[t];k++){
							if(k==H[t]){
								pw.println(Ptotal_barra[r][t]);		
								}
							else{
								pw.print(Ptotal_barra[r][t]+",");			
							}		
						}		
					}	
				}
			}	
			
			//Angulo POR BARRA
			for(int r=0; r < id_busbar.length;r++){	
				for(int t=0;t<T;t++){
					if(t==0){
						for(int k=1;k<=H[t];k++){
							if(k==1){
								pw.print(",,"+((DataUCBus)(vbus.elementAt(r))).Nombre[0]+",ANGULO,"+AnguloSol[r][t]+",");	
								}
							else{
								pw.print(AnguloSol[r][t]+",");
							}
						}
					}
					else if(t>0 & t<T-1){
						for(int k=1;k<=H[t];k++){
							pw.print(AnguloSol[r][t]+",");		
							}		
					}
					else{
						for(int k=1;k<=H[t];k++){
							if(k==H[t]){
								pw.println(AnguloSol[r][t]);		
								}
							else{
								pw.print(AnguloSol[r][t]+",");			
							}		
						}		
					}	
				}
			}
			
			
			//Potencia Total
			print_solution(pw,Ptotal,"Potencia bruta",H, T);
			//Reserva en giro
			print_solution(pw,Rtotal,"Reserva en giro",H, T);
			//Reserva en giro v2
			print_solution(pw,Rtotal2,"Reserva en giro 2",H, T);
			//Control primario de frecuencia
			print_solution(pw,CPFtotal,"Control primario frecuencia",H, T);
			//Reserva pronta
			print_solution(pw,ResPronsol,"Reserva Pronta",H, T);
			//Demanda real
			for (int i= 0; i<id_load.length;i++){
			
				print_solution(pw,demanda[i],((LoadUC)(vloaduc.elementAt(i))).Nombre[i],H, T);
			}
			//Demanda total
		
			print_solution(pw,Dtotal,"Demanda total",H, T);
			
			print_solution(pw,Perdidatotal,"Perdida",H, T);
			
			//Consumo propio total
			print_solution(pw,Owtotal,"Consumo propio",H, T);
			//Costo total por periodo
			print_solution(pw,Costtotalsol,"Costo total",H, T);
			//Costo total por periodo
			print_solution(pw,Costopertotalsol,"Costo operacion",H, T);
			//Costo partida por periodo
			print_solution(pw,Costpartotalsol,"Costo partida",H, T);
			//Costo parada por periodo
			print_solution(pw,Costparadatotalsol,"Costo parada",H, T);
			
			//Falla
			for(int i=0;i<id_load.length;i++){
				for(int s=0;s<((LoadUC)(vloaduc.elementAt(i))).n_iv;s++){
					for(int t=0;t<T;t++){
						if(t==0){
							for(int k=1;k<=H[t];k++){
								if(k==1){
									int pp=0;
									for(pp=0; pp < (bus.length-1); pp++) // pp < (bus.length-1) to be sure pp at most = bus.length-1
										if (((LoadUC)(vloaduc.elementAt(i))).Bus[0]==bus[pp][0])
											break;									
									//System.out.println("Load: "+((LoadUC)(vloaduc.elementAt(i))).Nombre[0]+ ", BusNo.:"+pp+ "MaxBus: "+vbus.size());
									//System.out.println("Bus Demanda: "+bus_demanda[i][0]);
									pw.print(",,"+((DataUCBus)(vbus.elementAt(pp))).Nombre[0]+",ENS"+(s+1)+","+Enssol[i][s][t]+",");	
								}
								else{
									pw.print(Enssol[i][s][t]+",");	
								}
							}
						}
						else if(t>0 & t<T-1){
							for(int k=1;k<=H[t];k++){
								pw.print(Enssol[i][s][t]+",");		
							}		
						}
						else{
							for(int k=1;k<=H[t];k++){
								if(k==H[t]){
									pw.println(Enssol[i][s][t]);		
								}
								else{
									pw.print(Enssol[i][s][t]+",");			
								}		
							}		
						}	
					}
				}
				
				
				
				
				//
				/*for(int t=0;t<T;t++){
					if(t==0){
						for(int k=1;k<=H[t];k++){
							if(k==1){
								pw.print(",,Cd,"+Cdsol[i][t]+",");	
							}
							else{
								pw.print(Cdsol[i][t]+",");	
							}
						}
					}
					else if(t>0 & t<T-1){
						for(int k=1;k<=H[t];k++){
							pw.print(Cdsol[i][t]+",");		
						}		
					}
					else{
						for(int k=1;k<=H[t];k++){
							if(k==H[t]){
								pw.println(Cdsol[i][t]);		
							}
							else{
								pw.print(Cdsol[i][t]+",");			
							}		
						}		
					}	
				}
				
				for(int t=0;t<T;t++){
					if(t==0){
						for(int k=1;k<=H[t];k++){
							if(k==1){
								pw.print(",,Dr,"+Drsol[i][t]+",");	
							}
							else{
								pw.print(Drsol[i][t]+",");	
							}
						}
					}
					else if(t>0 & t<T-1){
						for(int k=1;k<=H[t];k++){
							pw.print(Drsol[i][t]+",");		
						}		
					}
					else{
						for(int k=1;k<=H[t];k++){
							if(k==H[t]){
								pw.println(Drsol[i][t]);		
							}
							else{
								pw.print(Drsol[i][t]+",");			
							}		
						}		
					}	
				}*/
			
				//Energia perdida
				for(int t=0;t<T;t++){
					if(t==0){
						for(int k=1;k<=H[t];k++){
							if(k==1){
							int pp;
								for(pp=0; pp < (bus.length-1); pp++) // pp < (bus.length-1) to be sure pp at most = bus.length-1
									if (((LoadUC)(vloaduc.elementAt(i))).Bus[0]==bus[pp][0])
										break;	
								pw.print(",,"+((DataUCBus)(vbus.elementAt(pp))).Nombre[0]+",Pperd,"+Eperdsol[t][i]+",");	
							}
							else{
								pw.print(Eperdsol[t][i]+",");	
							}
						}
					}
					else if(t>0 & t<T-1){
						for(int k=1;k<=H[t];k++){
							pw.print(Eperdsol[t][i]+",");		
						}		
					}
					else{
						for(int k=1;k<=H[t];k++){
							if(k==H[t]){
								pw.println(Eperdsol[t][i]);		
							}
							else{
								pw.print(Eperdsol[t][i]+",");			
							}		
						}		
					}	
				}
			
			
			}
			//Falla_LP
			/*for(int t=0;t<T;t++){
				if(t==0){
					for(int k=1;k<=H[t];k++){
						if(k==1){
							pw.print("ENS,Falla,Falla_LP,"+cplex.getValue(ENS[t])+",");	
						}
						else{
							pw.print(cplex.getValue(ENS[t])+",");	
						}
					}
				}
				else if(t>0 & t<T-1){
					for(int k=1;k<=H[t];k++){
						pw.print(cplex.getValue(ENS[t])+",");		
					}		
				}
				else{
					for(int k=1;k<=H[t];k++){
						if(k==H[t]){
							pw.println(cplex.getValue(ENS[t]));		
						}
						else{
							pw.print(cplex.getValue(ENS[t])+",");			
						}		
					}		
				}	
			}*/
			
			//Energia perdida_LP
			/*for(int t=0;t<T;t++){
				if(t==0){
					for(int k=1;k<=H[t];k++){
						if(k==1){
							pw.print("Pperd,Pperd,Pperd_LP,"+cplex.getValue(Eperd[t])+",");	
						}
						else{
							pw.print(cplex.getValue(Eperd[t])+",");	
						}
					}
				}
				else if(t>0 & t<T-1){
					for(int k=1;k<=H[t];k++){
						pw.print(cplex.getValue(Eperd[t])+",");		
					}		
				}
				else{
					for(int k=1;k<=H[t];k++){
						if(k==H[t]){
							pw.println(cplex.getValue(Eperd[t]));		
						}
						else{
							pw.print(cplex.getValue(Eperd[t])+",");			
						}		
					}		
				}	
			}*/
			//generacion eolica
			print_solution(pw,generacion_eolic,"Gen Eolica",H, T);
			//generacion solar
			print_solution(pw,generacion_solar,"Gen Sol",H, T);
			
			
			
			
			String type_aux= "T";
			
			for(int j=0;j<id_tgenerator.length;j++){
				//Solucion P
				int i=j;
				//Potencia operacion normal
				print_solution_gen(pw,(TGUC)(vt.elementAt(i)), Psol,type_aux,"G", "P", H, T, i);
				// Potencia rampa de arranque
				if(((TGUC)(vt.elementAt(i))).GradRA[0]>0){
					print_solution_gen(pw,(TGUC)(vt.elementAt(i)), Pasol,type_aux,"G", "Pa", H, T, i);	
				}
				// Potencia rampa de parada
				if(((TGUC)(vt.elementAt(i))).GradRP[0]>0){
					print_solution_gen(pw,(TGUC)(vt.elementAt(i)), Pasol,type_aux,"G", "Pp", H, T, i);	
				}
				//Potencia total
				print_solution_gen(pw,(TGUC)(vt.elementAt(i)), Pasol,type_aux,"G", "Pt", H, T, i);	
				//Reserva en giro
				print_solution_gen(pw,(TGUC)(vt.elementAt(i)), Rgsol,type_aux,"G", "Rgiro", H, T, i);	
				//Reserva en giro v2
				print_solution_gen(pw,(TGUC)(vt.elementAt(i)), Rgsol2,type_aux,"G", "Rgiro2", H, T, i);	
				//Control primario de frecuencia
				print_solution_gen(pw,(TGUC)(vt.elementAt(i)), CPFsol,type_aux,"G", "CPF", H, T, i);	
				//Reserva pronta
				print_solution_gen(pw,(TGUC)(vt.elementAt(i)), RPronsol,type_aux,"G", "Reserva Pronta", H, T, i);	
				//Bg
				if(((TGUC)(vt.elementAt(i))).Bg!=null){
					print_solution_gen(pw,(TGUC)(vt.elementAt(i)),Bgsol,type_aux,"G", "Bg", H, T, i);	
				}
				//Bga
				if(((TGUC)(vt.elementAt(i))).Bga!=null){
					print_solution_gen(pw,(TGUC)(vt.elementAt(i)),Bgasol,type_aux,"G", "Bga", H, T, i);	
				}
				if(((TGUC)(vt.elementAt(i))).Bgp!=null){
					print_solution_gen(pw,(TGUC)(vt.elementAt(i)),Bgpsol,type_aux,"G", "Bgp", H, T, i);	
				}
				
				
				/*
				//X
				if(((TGUC)(vt.elementAt(i))).X!=null){
					print_solution_gen(pw,(TGUC)(vt.elementAt(i)),Xsol,type_aux,"G", "X", H, T, i);	
				}
				
				//Y
				if(((TGUC)(vt.elementAt(i))).Y!=null){
					print_solution_gen(pw,(TGUC)(vt.elementAt(i)),Ysol,type_aux,"G", "Y", H, T, i);	
				}
				*/
				
				for(int iv=0;iv<((TGUC)(vt.elementAt(i))).Niv[0];iv++){
					for(int t=0;t<T;t++){
						if(set_bgh1[i][t] !=1 || set_bh1[i][iv][t] !=0){
							if(t==0){
								for(int k=1;k<=H[t];k++){
									if(k==1){
										pw.print("G"+((TGUC)(vt.elementAt(i))).ID[t]+","+((TGUC)(vt.elementAt(i))).Nombre[t]+","+type_aux+",B"+(iv+1)+","+Bsol[i][iv][t]+",");	
									}
									else{
										pw.print(Bsol[i][iv][t]+",");	
									}
								}
							}
							else if(t>0 & t<T-1){
								for(int k=1;k<=H[t];k++){
									pw.print(Bsol[i][iv][t]+",");		
								}		
							}
							else{
								for(int k=1;k<=H[t];k++){
									if(k==H[t]){
										pw.println(Bsol[i][iv][t]);		
									}
									else{
										pw.print(Bsol[i][iv][t]+",");			
									}		
								}		
							}	
						}
					}
				}
				//Potencia maxima
				print_solution_gen2(pw,(TGUC)(vt.elementAt(i)),((TGUC)(vt.elementAt(i))).Pmaxg,type_aux,"G", "Pmaxg", H, T);	
				//Potencia minima
				print_solution_gen2(pw,(TGUC)(vt.elementAt(i)),((TGUC)(vt.elementAt(i))).Pming,type_aux,"G", "Pming", H, T);	
				//Volumen estanque
				print_solution_gen(pw,(TGUC)(vt.elementAt(i)),Vsol,type_aux,"G", "V", H, T, i);	
				//Costo arranque
				if(((TGUC)(vt.elementAt(i))).Costo_AP_VNC1!=null){
					print_solution_gen(pw,(TGUC)(vt.elementAt(i)),Costpartidasol,type_aux,"G", "Costo Arranque", H, T, i);	
				}
				//Costo
				print_solution_gen(pw,(TGUC)(vt.elementAt(i)),Costsol,type_aux,"G", "Costo", H, T, i);	
				
				//Costo Rampa Arranque
				if(((TGUC)(vt.elementAt(i))).GradRA[0]>0){
					for(int t=0;t<T;t++){
						if(t==0){
							for(int k=1;k<=H[t];k++){
								if(k==1){
									pw.print("G"+((TGUC)(vt.elementAt(i))).ID[t]+","+((TGUC)(vt.elementAt(i))).Nombre[t]+","+type_aux+",Costo Pa"+","+Costarrsol[i][t]+",");	
									}
								else{
									pw.print(Costarrsol[i][t]+",");	
								}
							}
						}
						else if(t>0 & t<T-1){
							for(int k=1;k<=H[t];k++){
								pw.print(Costarrsol[i][t]+",");		
							}		
						}
						else{
							for(int k=1;k<=H[t];k++){
								if(k==H[t]){
									pw.println(Costarrsol[i][t]);		
								}
								else{
									pw.print(Costarrsol[i][t]+",");			
								}		
							}		
						}
					}
				}
				//Costo Rampa Parada
				if(((TGUC)(vt.elementAt(i))).GradRP[0]>0){
					for(int t=0;t<T;t++){
						if(t==0){
							for(int k=1;k<=H[t];k++){
								if(k==1){
									pw.print("G"+((TGUC)(vt.elementAt(i))).ID[t]+","+((TGUC)(vt.elementAt(i))).Nombre[t]+","+type_aux+",Costo Pp"+","+Costparsol[i][t]+",");	
									}
								else{
									pw.print(Costparsol[i][t]+",");	
								}
							}
						}
						else if(t>0 & t<T-1){
							for(int k=1;k<=H[t];k++){
								pw.print(Costparsol[i][t]+",");		
							}		
						}
						else{
							for(int k=1;k<=H[t];k++){
								if(k==H[t]){
									pw.println(Costparsol[i][t]);		
								}
								else{
									pw.print(Costparsol[i][t]+",");			
								}		
							}		
						}
					}
				}
				//Costo Parada
//**				if(((TGUC)(vt.elementAt(i))).StopCost[0]>0){
					for(int t=0;t<T;t++){
						if(t==0){
							for(int k=1;k<=H[t];k++){
								if(k==1){
									pw.print("G"+((TGUC)(vt.elementAt(i))).ID[t]+","+((TGUC)(vt.elementAt(i))).Nombre[t]+","+type_aux+",Costo Parada"+","+Costparadasol[i][t]+",");	
									}
								else{
									pw.print(Costparadasol[i][t]+",");	
								}
							}
						}
						else if(t>0 & t<T-1){
							for(int k=1;k<=H[t];k++){
								pw.print(Costparadasol[i][t]+",");		
							}		
						}
						else{
							for(int k=1;k<=H[t];k++){
								if(k==H[t]){
									pw.println(Costparadasol[i][t]);		
								}
								else{
									pw.print(Costparadasol[i][t]+",");			
								}		
							}		
						}
					}
//**				}
				//Costo Medio
				for(int t=0;t<T;t++){
					if(t==0){
						for(int k=1;k<=H[t];k++){
							if(k==1){
								pw.print("G"+((TGUC)(vt.elementAt(i))).ID[t]+","+((TGUC)(vt.elementAt(i))).Nombre[t]+","+type_aux+",Costo Medio"+","+CMedio[i][t]/H[t]+",");	
								}
							else{
								pw.print(CMedio[i][t]/H[t]+",");	
							}
						}
					}
					else if(t>0 & t<T-1){
						for(int k=1;k<=H[t];k++){
							pw.print(CMedio[i][t]/H[t]+",");		
						}		
					}
					else{
						for(int k=1;k<=H[t];k++){
							if(k==H[t]){
								pw.println(CMedio[i][t]/H[t]);		
							}
							else{
								pw.print(CMedio[i][t]/H[t]+",");			
							}		
						}		
					}
				}
						
			}
			// Solucion centrales hidraulicas
			for(int j=0;j<id_hgenerator.length;j++){
				type_aux=((HGUC)(vh.elementAt(j))).Type[0];
				//potencia hidraulica
				print_solution_genh(pw,(HGUC)(vh.elementAt(j)), Phsol,type_aux,"G", "P", H, T, j);
				//caudal turbinado
				print_solution_genh(pw,(HGUC)(vh.elementAt(j)), Qhsol,type_aux,"G", "Qh", H, T, j);
				//caudal vertido
				print_solution_genh(pw,(HGUC)(vh.elementAt(j)), Qversol,type_aux,"G", "Qver", H, T, j);
				//volumen ficticio
				print_solution_genh(pw,(HGUC)(vh.elementAt(j)), Vficsol,type_aux,"G", "Vfic", H, T, j);
				//variable binaria
				print_solution_genh(pw,(HGUC)(vh.elementAt(j)), Bhsol,type_aux,"G", "B", H, T, j);
				//reserva en giro 
				print_solution_genh(pw,(HGUC)(vh.elementAt(j)), Rgsol_hidro,type_aux,"G", "Rgiro", H, T, j);
				//reserva en giro v2
				print_solution_genh(pw,(HGUC)(vh.elementAt(j)), Rgsol2_hidro,type_aux,"G", "Rgiro2", H, T, j);
				//CPF
				print_solution_genh(pw,(HGUC)(vh.elementAt(j)), CPFsol_hidro,type_aux,"G", "CPF", H, T, j);
				//Reserva pronta
				print_solution_genh(pw,(HGUC)(vh.elementAt(j)), RPronsol_hidro,type_aux,"G", "Reserva Pronta", H, T, j);
				//pmax
				print_solution_genh2(pw,(HGUC)(vh.elementAt(j)),((HGUC)(vh.elementAt(j))).Pmaxg ,type_aux,"G", "Pmaxg", H, T);
				//pmin
				print_solution_genh2(pw,(HGUC)(vh.elementAt(j)),((HGUC)(vh.elementAt(j))).Pming ,type_aux,"G", "Pming", H, T);
				//afluente
				print_solution_genh2(pw,(HGUC)(vh.elementAt(j)),((HGUC)(vh.elementAt(j))).Aflu ,type_aux,"G", "Aflu", H, T);
			}
			
			
			for(int i=0;i<id_battery.length;i++){
				type_aux="H";
				//Potencia Bateria
				for(int t=0;t<T;t++){
					if(t==0){
						for(int k=1;k<=H[t];k++){
							if(k==1){
								pw.print("BAT"+((BatteryUC)(vbatuc.elementAt(i))).Id[t]+","+((BatteryUC)(vbatuc.elementAt(i))).Nombre[t]+","+type_aux+",Ppos"+","+Ppossol[i][t]+",");	
								}
							else{
								pw.print(Ppossol[i][t]+",");	
							}
						}
					}
					else if(t>0 & t<T-1){
						for(int k=1;k<=H[t];k++){
							pw.print(Ppossol[i][t]+",");		
						}		
					}
					else{
						for(int k=1;k<=H[t];k++){
							if(k==H[t]){
								pw.println(Ppossol[i][t]);		
							}
							else{
								pw.print(Ppossol[i][t]+",");			
							}		
						}		
					}	
				}		
				//Potencia Bateria negativa
				for(int t=0;t<T;t++){
					if(t==0){
						for(int k=1;k<=H[t];k++){
							if(k==1){
								pw.print("BAT"+((BatteryUC)(vbatuc.elementAt(i))).Id[t]+","+((BatteryUC)(vbatuc.elementAt(i))).Nombre[t]+","+type_aux+",Pneg"+","+Pnegsol[i][t]+",");	
								}
							else{
								pw.print(Pnegsol[i][t]+",");	
							}
						}
					}
					else if(t>0 & t<T-1){
						for(int k=1;k<=H[t];k++){
							pw.print(Pnegsol[i][t]+",");		
						}		
					}
					else{
						for(int k=1;k<=H[t];k++){
							if(k==H[t]){
								pw.println(Pnegsol[i][t]);		
							}
							else{
								pw.print(Pnegsol[i][t]+",");			
							}		
						}		
					}	
				}
				//Potencia Inversor
				for(int t=0;t<T;t++){
					if(t==0){
						for(int k=1;k<=H[t];k++){
							if(k==1){
								pw.print("BAT"+((BatteryUC)(vbatuc.elementAt(i))).Id[t]+","+((BatteryUC)(vbatuc.elementAt(i))).Nombre[t]+","+type_aux+",Pinv"+","+Pisol[i][t]+",");	
								}
							else{
								pw.print(Pisol[i][t]+",");	
							}
						}
					}
					else if(t>0 & t<T-1){
						for(int k=1;k<=H[t];k++){
							pw.print(Pisol[i][t]+",");		
						}		
					}
					else{
						for(int k=1;k<=H[t];k++){
							if(k==H[t]){
								pw.println(Pisol[i][t]);		
							}
							else{
								pw.print(Pisol[i][t]+",");			
							}		
						}		
					}	
				}
				//Potencia Minima
				for(int t=0;t<T;t++){
					if(t==0){
						for(int k=1;k<=H[t];k++){
							if(k==1){
								pw.print("BAT"+((BatteryUC)(vbatuc.elementAt(i))).Id[t]+","+((BatteryUC)(vbatuc.elementAt(i))).Nombre[t]+","+type_aux+",Pbmin"+","+Pbminsol[i][t]+",");	
								}
							else{
								pw.print(Pbminsol[i][t]+",");	
							}
						}
					}
					else if(t>0 & t<T-1){
						for(int k=1;k<=H[t];k++){
							pw.print(Pbminsol[i][t]+",");		
						}		
					}
					else{
						for(int k=1;k<=H[t];k++){
							if(k==H[t]){
								pw.println(Pbminsol[i][t]);		
							}
							else{
								pw.print(Pbminsol[i][t]+",");			
							}		
						}		
					}	
				}
				//Binaria Bateria positiva
				for(int t=0;t<T;t++){
					if(t==0){
						for(int k=1;k<=H[t];k++){
							if(k==1){
								pw.print("BAT"+((BatteryUC)(vbatuc.elementAt(i))).Id[t]+","+((BatteryUC)(vbatuc.elementAt(i))).Nombre[t]+","+type_aux+",Bpos"+","+Bpossol[i][t]+",");	
								}
							else{
								pw.print(Bpossol[i][t]+",");	
							}
						}
					}
					else if(t>0 & t<T-1){
						for(int k=1;k<=H[t];k++){
							pw.print(Bpossol[i][t]+",");		
						}		
					}
					else{
						for(int k=1;k<=H[t];k++){
							if(k==H[t]){
								pw.println(Bpossol[i][t]);		
							}
							else{
								pw.print(Bpossol[i][t]+",");			
							}		
						}		
					}	
				}		
				//Binaria Bateria negativa
				for(int t=0;t<T;t++){
					if(t==0){
						for(int k=1;k<=H[t];k++){
							if(k==1){
								pw.print("BAT"+((BatteryUC)(vbatuc.elementAt(i))).Id[t]+","+((BatteryUC)(vbatuc.elementAt(i))).Nombre[t]+","+type_aux+",Bneg"+","+Bnegsol[i][t]+",");	
								}
							else{
								pw.print(Bnegsol[i][t]+",");	
							}
						}
					}
					else if(t>0 & t<T-1){
						for(int k=1;k<=H[t];k++){
							pw.print(Bnegsol[i][t]+",");		
						}		
					}
					else{
						for(int k=1;k<=H[t];k++){
							if(k==H[t]){
								pw.println(Bnegsol[i][t]);		
							}
							else{
								pw.print(Bnegsol[i][t]+",");			
							}		
						}		
					}	
				}
				//tramos pmin
				for(int t=0;t<T;t++){
					if(t==0){
						for(int k=1;k<=H[t];k++){
							if(k==1){
								pw.print("BAT"+((BatteryUC)(vbatuc.elementAt(i))).Id[t]+","+((BatteryUC)(vbatuc.elementAt(i))).Nombre[t]+","+type_aux+",Esocmin"+","+((BatteryUC)(vbatuc.elementAt(i))).Esocmin[0][t]*((BatteryUC)(vbatuc.elementAt(i))).Emax[t]+",");	
								}
							else{
								pw.print(((BatteryUC)(vbatuc.elementAt(i))).Esocmin[0][t]*((BatteryUC)(vbatuc.elementAt(i))).Emax[t]+",");	
							}
						}
					}
					else if(t>0 & t<T-1){
						for(int k=1;k<=H[t];k++){
							pw.print(((BatteryUC)(vbatuc.elementAt(i))).Esocmin[0][t]*((BatteryUC)(vbatuc.elementAt(i))).Emax[t]+",");		
						}		
					}
					else{
						for(int k=1;k<=H[t];k++){
							if(k==H[t]){
								pw.println(((BatteryUC)(vbatuc.elementAt(i))).Esocmin[0][t]*((BatteryUC)(vbatuc.elementAt(i))).Emax[t]);		
							}
							else{
								pw.print(((BatteryUC)(vbatuc.elementAt(i))).Esocmin[0][t]*((BatteryUC)(vbatuc.elementAt(i))).Emax[t]+",");			
							}		
						}		
					}	
				}
				
				for(int s=0;s<((BatteryUC)(vbatuc.elementAt(i))).n_iv;s++){
					for(int t=0;t<T;t++){
						if(t==0){
							for(int k=1;k<=H[t];k++){
								if(k==1){
									pw.print("BAT"+((BatteryUC)(vbatuc.elementAt(i))).Id[t]+","+((BatteryUC)(vbatuc.elementAt(i))).Nombre[t]+","+type_aux+",Ebsol_"+(s+1)+","+Ebsol[i][s][t]+",");	
									}
								else{
									pw.print(Ebsol[i][s][t]+",");	
								}
							}
						}
						else if(t>0 & t<T-1){
							for(int k=1;k<=H[t];k++){
								pw.print(Ebsol[i][s][t]+",");		
							}		
						}
						else{
							for(int k=1;k<=H[t];k++){
								if(k==H[t]){
									pw.println(Ebsol[i][s][t]);		
								}
								else{
									pw.print(Ebsol[i][s][t]+",");			
								}		
							}		
						}	
					}
				}
				for(int s=0;s<((BatteryUC)(vbatuc.elementAt(i))).n_iv;s++){
					for(int t=0;t<T;t++){
						if(t==0){
							for(int k=1;k<=H[t];k++){
								if(k==1){
									pw.print("BAT"+((BatteryUC)(vbatuc.elementAt(i))).Id[t]+","+((BatteryUC)(vbatuc.elementAt(i))).Nombre[t]+","+type_aux+",Bbsol_"+(s+1)+","+Bbsol[i][s][t]+",");	
									}
								else{
									pw.print(Bbsol[i][s][t]+",");	
								}
							}
						}
						else if(t>0 & t<T-1){
							for(int k=1;k<=H[t];k++){
								pw.print(Bbsol[i][s][t]+",");		
							}		
						}
						else{
							for(int k=1;k<=H[t];k++){
								if(k==H[t]){
									pw.println(Bbsol[i][s][t]);		
								}
								else{
									pw.print(Bbsol[i][s][t]+",");			
								}		
							}		
						}	
					}
				}
				//Energia Ep
				/*for(int t=0;t<T;t++){
					if(t==0){
						for(int k=1;k<=H[t];k++){
							if(k==1){
								pw.print("BAT"+((BatteryUC)(vbatuc.elementAt(i))).Id[t]+","+((BatteryUC)(vbatuc.elementAt(i))).Nombre[t]+",Ep"+","+Epsol[i][t]+",");	
								}
							else{
								pw.print(Epsol[i][t]+",");	
							}
						}
					}
					else if(t>0 & t<T-1){
						for(int k=1;k<=H[t];k++){
							pw.print(Epsol[i][t]+",");		
						}		
					}
					else{
						for(int k=1;k<=H[t];k++){
							if(k==H[t]){
								pw.println(Epsol[i][t]);		
							}
							else{
								pw.print(Epsol[i][t]+",");			
							}		
						}		
					}	
				}
				//Energia En
				for(int t=0;t<T;t++){
					if(t==0){
						for(int k=1;k<=H[t];k++){
							if(k==1){
								pw.print("BAT"+((BatteryUC)(vbatuc.elementAt(i))).Id[t]+","+((BatteryUC)(vbatuc.elementAt(i))).Nombre[t]+",En"+","+Ensol[i][t]+",");	
								}
							else{
								pw.print(Ensol[i][t]+",");	
							}
						}
					}
					else if(t>0 & t<T-1){
						for(int k=1;k<=H[t];k++){
							pw.print(Ensol[i][t]+",");		
						}		
					}
					else{
						for(int k=1;k<=H[t];k++){
							if(k==H[t]){
								pw.println(Ensol[i][t]);		
							}
							else{
								pw.print(Ensol[i][t]+",");			
							}		
						}		
					}	
				}
				*/
				//Potencia Bateria_LP
				/*for(int t=0;t<T;t++){
					if(t==0){
						for(int k=1;k<=H[t];k++){
							if(k==1){
								pw.print("BAT"+((BatteryUC)(vbatuc.elementAt(i))).Id[t]+","+((BatteryUC)(vbatuc.elementAt(i))).Nombre[t]+",Pb_LP"+","+cplex.getValue(((BatteryUC)(vbatuc.elementAt(i))).P[t])+",");	
								}
							else{
								pw.print(cplex.getValue(((BatteryUC)(vbatuc.elementAt(i))).P[t])+",");	
							}
						}
					}
					else if(t>0 & t<T-1){
						for(int k=1;k<=H[t];k++){
							pw.print(cplex.getValue(((BatteryUC)(vbatuc.elementAt(i))).P[t])+",");		
						}		
					}
					else{
						for(int k=1;k<=H[t];k++){
							if(k==H[t]){
								pw.println(cplex.getValue(((BatteryUC)(vbatuc.elementAt(i))).P[t]));		
							}
							else{
								pw.print(cplex.getValue(((BatteryUC)(vbatuc.elementAt(i))).P[t])+",");			
							}		
						}		
					}	
				}*/		
				//Energia bateria 
				for(int t=0;t<T;t++){
					if(t==0){
						for(int k=1;k<=H[t];k++){
							if(k==1){
								pw.print("BAT"+((BatteryUC)(vbatuc.elementAt(i))).Id[t]+","+((BatteryUC)(vbatuc.elementAt(i))).Nombre[t]+","+type_aux+",Eb"+","+Vbsol[i][t]+",");	
								}
							else{
								pw.print(Vbsol[i][t]+",");	
							}
						}
					}
					else if(t>0 & t<T-1){
						for(int k=1;k<=H[t];k++){
							pw.print(Vbsol[i][t]+",");		
						}		
					}
					else{
						for(int k=1;k<=H[t];k++){
							if(k==H[t]){
								pw.println(Vbsol[i][t]);		
							}
							else{
								pw.print(Vbsol[i][t]+",");			
							}		
						}		
					}	
				}
			
			}
			
			//CSP
			for(int i=0;i<id_csp.length;i++){
				type_aux="CSP";
				//Potencia Electrica 1
				for(int t=0;t<T;t++){
					if(t==0){
						for(int k=1;k<=H[t];k++){
							if(k==1){
								pw.print("CSP"+((CSPUC)(vcspuc.elementAt(i))).Id[t]+","+((CSPUC)(vcspuc.elementAt(i))).Nombre[t]+","+type_aux+",Pele1"+","+Pel1sol[i][t]+",");	
								}
							else{
								pw.print(Pel1sol[i][t]+",");	
							}
						}
					}
					else if(t>0 & t<T-1){
						for(int k=1;k<=H[t];k++){
							pw.print(Pel1sol[i][t]+",");		
						}		
					}
					else{
						for(int k=1;k<=H[t];k++){
							if(k==H[t]){
								pw.println(Pel1sol[i][t]);		
							}
							else{
								pw.print(Pel1sol[i][t]+",");			
							}		
						}		
					}	
				}		
				
				//Potencia Electrica 2
				for(int t=0;t<T;t++){
					if(t==0){
						for(int k=1;k<=H[t];k++){
							if(k==1){
								pw.print("CSP"+((CSPUC)(vcspuc.elementAt(i))).Id[t]+","+((CSPUC)(vcspuc.elementAt(i))).Nombre[t]+","+type_aux+",Pele2"+","+Pel2sol[i][t]+",");	
								}
							else{
								pw.print(Pel2sol[i][t]+",");	
							}
						}
					}
					else if(t>0 & t<T-1){
						for(int k=1;k<=H[t];k++){
							pw.print(Pel2sol[i][t]+",");		
						}		
					}
					else{
						for(int k=1;k<=H[t];k++){
							if(k==H[t]){
								pw.println(Pel2sol[i][t]);		
							}
							else{
								pw.print(Pel2sol[i][t]+",");			
							}		
						}		
					}	
				}
				//Potencia Almacenamiento
				for(int t=0;t<T;t++){
					if(t==0){
						for(int k=1;k<=H[t];k++){
							if(k==1){
								pw.print("CSP"+((CSPUC)(vcspuc.elementAt(i))).Id[t]+","+((CSPUC)(vcspuc.elementAt(i))).Nombre[t]+","+type_aux+",Palm"+","+Palmsol[i][t]+",");	
								}
							else{
								pw.print(Palmsol[i][t]+",");	
							}
						}
					}
					else if(t>0 & t<T-1){
						for(int k=1;k<=H[t];k++){
							pw.print(Palmsol[i][t]+",");		
						}		
					}
					else{
						for(int k=1;k<=H[t];k++){
							if(k==H[t]){
								pw.println(Palmsol[i][t]);		
							}
							else{
								pw.print(Palmsol[i][t]+",");			
							}		
						}		
					}	
				}
				//Variable binaria Bele1
				for(int t=0;t<T;t++){
					if(t==0){
						for(int k=1;k<=H[t];k++){
							if(k==1){
								pw.print("CSP"+((CSPUC)(vcspuc.elementAt(i))).Id[t]+","+((CSPUC)(vcspuc.elementAt(i))).Nombre[t]+","+type_aux+",Bel1"+","+Bel1sol[i][t]+",");	
								}
							else{
								pw.print(Bel1sol[i][t]+",");	
							}
						}
					}
					else if(t>0 & t<T-1){
						for(int k=1;k<=H[t];k++){
							pw.print(Bel1sol[i][t]+",");		
						}		
					}
					else{
						for(int k=1;k<=H[t];k++){
							if(k==H[t]){
								pw.println(Bel1sol[i][t]);		
							}
							else{
								pw.print(Bel1sol[i][t]+",");			
							}		
						}		
					}	
				}
				//Variable binaria Balm
				for(int t=0;t<T;t++){
					if(t==0){
						for(int k=1;k<=H[t];k++){
							if(k==1){
								pw.print("CSP"+((CSPUC)(vcspuc.elementAt(i))).Id[t]+","+((CSPUC)(vcspuc.elementAt(i))).Nombre[t]+","+type_aux+",Balm"+","+Balmsol[i][t]+",");	
								}
							else{
								pw.print(Balmsol[i][t]+",");	
							}
						}
					}
					else if(t>0 & t<T-1){
						for(int k=1;k<=H[t];k++){
							pw.print(Balmsol[i][t]+",");		
						}		
					}
					else{
						for(int k=1;k<=H[t];k++){
							if(k==H[t]){
								pw.println(Balmsol[i][t]);		
							}
							else{
								pw.print(Balmsol[i][t]+",");			
							}		
						}		
					}	
				}
				//Variable binaria Balm
				for(int t=0;t<T;t++){
					if(t==0){
						for(int k=1;k<=H[t];k++){
							if(k==1){
								pw.print("CSP"+((CSPUC)(vcspuc.elementAt(i))).Id[t]+","+((CSPUC)(vcspuc.elementAt(i))).Nombre[t]+","+type_aux+",Bgcsp"+","+Bgcspsol[i][t]+",");	
								}
							else{
								pw.print(Bgcspsol[i][t]+",");	
							}
						}
					}
					else if(t>0 & t<T-1){
						for(int k=1;k<=H[t];k++){
							pw.print(Bgcspsol[i][t]+",");		
						}		
					}
					else{
						for(int k=1;k<=H[t];k++){
							if(k==H[t]){
								pw.println(Bgcspsol[i][t]);		
							}
							else{
								pw.print(Bgcspsol[i][t]+",");			
							}		
						}		
					}	
				}
				//Almacenamiento
				for(int t=0;t<T;t++){
					if(t==0){
						for(int k=1;k<=H[t];k++){
							if(k==1){
								pw.print("CSP"+((CSPUC)(vcspuc.elementAt(i))).Id[t]+","+((CSPUC)(vcspuc.elementAt(i))).Nombre[t]+","+type_aux+",V"+","+Vcspsol[i][t]+",");	
								}
							else{
								pw.print(Vcspsol[i][t]+",");	
							}
						}
					}
					else if(t>0 & t<T-1){
						for(int k=1;k<=H[t];k++){
							pw.print(Vcspsol[i][t]+",");		
						}		
					}
					else{
						for(int k=1;k<=H[t];k++){
							if(k==H[t]){
								pw.println(Vcspsol[i][t]);		
							}
							else{
								pw.print(Vcspsol[i][t]+",");			
							}		
						}		
					}	
				}						
			
			}
			
			//Solucion eolica
			printsol_eol(pw,veoluc,id_eolic,Peolsol,Rgeolsol,Rgeolsol2,CPFeolsol,H,T);
			
			//Solucion solar
			printsol_sol(pw,vsoluc,id_solar,Psolsol,Rgsolsol,Rgsolsol2,CPFsolsol,H,T);
			
			for(int i=0;i<id_reserv.length;i++){
				type_aux="Emb";
				//Volumen embalse_LP
				/*for(int t=0;t<T;t++){
					if(t==0){
						for(int k=1;k<=H[t];k++){
							if(k==1){
								pw.print("V"+((ReservUC)(vresuc.elementAt(i))).Id[t]+","+((ReservUC)(vresuc.elementAt(i))).Nombre[t]+",Vemb_LP"+","+cplex.getValue(((ReservUC)(vresuc.elementAt(i))).V[t])+",");	
								}
							else{
								pw.print(cplex.getValue(((ReservUC)(vresuc.elementAt(i))).V[t])+",");	
							}
						}
					}
					else if(t>0 & t<T-1){
						for(int k=1;k<=H[t];k++){
							pw.print(cplex.getValue(((ReservUC)(vresuc.elementAt(i))).V[t])+",");		
						}		
					}
					else{
						for(int k=1;k<=H[t];k++){
							if(k==H[t]){
								pw.println(cplex.getValue(((ReservUC)(vresuc.elementAt(i))).V[t]));		
							}
							else{
								pw.print(cplex.getValue(((ReservUC)(vresuc.elementAt(i))).V[t])+",");			
							}		
						}		
					}	
				}*/
				//Retiros
				for(int t=0;t<T;t++){
					if(t==0){
						for(int k=1;k<=H[t];k++){
							if(k==1){
								pw.print("Ret"+((ReservUC)(vresuc.elementAt(i))).Id[t]+","+((ReservUC)(vresuc.elementAt(i))).Nombre[t]+","+type_aux+",Ret"+","+UnitT*((ReservUC)(vresuc.elementAt(i))).Ret[t]+",");	
								}
							else{
								pw.print(UnitT*((ReservUC)(vresuc.elementAt(i))).Ret[t]+",");	
							}
						}
					}
					else if(t>0 & t<T-1){
						for(int k=1;k<=H[t];k++){
							pw.print(UnitT*((ReservUC)(vresuc.elementAt(i))).Ret[t]+",");		
						}		
					}
					else{
						for(int k=1;k<=H[t];k++){
							if(k==H[t]){
								pw.println(UnitT*((ReservUC)(vresuc.elementAt(i))).Ret[t]);		
							}
							else{
								pw.print(UnitT*((ReservUC)(vresuc.elementAt(i))).Ret[t]+",");			
							}		
						}		
					}	
				}
				//Volumen embalse
				for(int t=0;t<T;t++){
					if(t==0){
						for(int k=1;k<=H[t];k++){
							if(k==1){
								pw.print("Vemb"+((ReservUC)(vresuc.elementAt(i))).Id[t]+","+((ReservUC)(vresuc.elementAt(i))).Nombre[t]+","+type_aux+",Vemb"+","+Vembsol[i][t]+",");	
								}
							else{
								pw.print(Vembsol[i][t]+",");	
							}
						}
					}
					else if(t>0 & t<T-1){
						for(int k=1;k<=H[t];k++){
							pw.print(Vembsol[i][t]+",");		
						}		
					}
					else{
						for(int k=1;k<=H[t];k++){
							if(k==H[t]){
								pw.println(Vembsol[i][t]);		
							}
							else{
								pw.print(Vembsol[i][t]+",");			
							}		
						}		
					}	
				}
				//Caudal vertido
				for(int t=0;t<T;t++){
					if(t==0){
						for(int k=1;k<=H[t];k++){
							if(k==1){
								pw.print("Qver"+((ReservUC)(vresuc.elementAt(i))).Id[t]+","+((ReservUC)(vresuc.elementAt(i))).Nombre[t]+","+type_aux+",Qver"+","+Qverembsol[i][t]+",");	
								}
							else{
								pw.print(Qverembsol[i][t]+",");	
							}
						}
					}
					else if(t>0 & t<T-1){
						for(int k=1;k<=H[t];k++){
							pw.print(Qverembsol[i][t]+",");		
						}		
					}
					else{
						for(int k=1;k<=H[t];k++){
							if(k==H[t]){
								pw.println(Qverembsol[i][t]);		
							}
							else{
								pw.print(Qverembsol[i][t]+",");			
							}		
						}		
					}	
				}				
				//Volumen embalse ficticio
				for(int t=0;t<T;t++){
					if(t==0){
						for(int k=1;k<=H[t];k++){
							if(k==1){
								pw.print("Vfic"+((ReservUC)(vresuc.elementAt(i))).Id[t]+","+((ReservUC)(vresuc.elementAt(i))).Nombre[t]+","+type_aux+",Vfic"+","+Vficembsol[i][t]+",");	
								}
							else{
								pw.print(Vficembsol[i][t]+",");	
							}
						}
					}
					else if(t>0 & t<T-1){
						for(int k=1;k<=H[t];k++){
							pw.print(Vficembsol[i][t]+",");		
						}		
					}
					else{
						for(int k=1;k<=H[t];k++){
							if(k==H[t]){
								pw.println(Vficembsol[i][t]);		
							}
							else{
								pw.print(Vficembsol[i][t]+",");			
							}		
						}		
					}	
				}
				//Afluente
				for(int t=0;t<T;t++){
					if(t==0){
						for(int k=1;k<=H[t];k++){
							if(k==1){
								pw.print("Aflu"+((ReservUC)(vresuc.elementAt(i))).Id[t]+","+((ReservUC)(vresuc.elementAt(i))).Nombre[t]+","+type_aux+",Aflu"+","+((ReservUC)(vresuc.elementAt(i))).Aflu[t]+",");	
								}
							else{
								pw.print(((ReservUC)(vresuc.elementAt(i))).Aflu[t]+",");	
							}
						}
					}
					else if(t>0 & t<T-1){
						for(int k=1;k<=H[t];k++){
							pw.print(((ReservUC)(vresuc.elementAt(i))).Aflu[t]+",");		
						}		
					}
					else{
						for(int k=1;k<=H[t];k++){
							if(k==H[t]){
								pw.println(((ReservUC)(vresuc.elementAt(i))).Aflu[t]);		
							}
							else{
								pw.print(((ReservUC)(vresuc.elementAt(i))).Aflu[t]+",");			
							}		
						}		
					}	
				}
				//Volumen minimo
				for(int t=0;t<T;t++){
					if(t==0){
						for(int k=1;k<=H[t];k++){
							if(k==1){
								pw.print("Vemb"+((ReservUC)(vresuc.elementAt(i))).Id[t]+","+((ReservUC)(vresuc.elementAt(i))).Nombre[t]+","+type_aux+",Vmin"+","+((ReservUC)(vresuc.elementAt(i))).Vmin[t]+",");	
								}
							else{
								pw.print(((ReservUC)(vresuc.elementAt(i))).Vmin[t]+",");	
							}
						}
					}
					else if(t>0 & t<T-1){
						for(int k=1;k<=H[t];k++){
							pw.print(((ReservUC)(vresuc.elementAt(i))).Vmin[t]+",");		
						}		
					}
					else{
						for(int k=1;k<=H[t];k++){
							if(k==H[t]){
								pw.println(((ReservUC)(vresuc.elementAt(i))).Vmin[t]);		
							}
							else{
								pw.print(((ReservUC)(vresuc.elementAt(i))).Vmin[t]+",");			
							}		
						}		
					}	
				}
				//Volumen minimo reserva
				for(int t=0;t<T;t++){
					if(t==0){
						for(int k=1;k<=H[t];k++){
							if(k==1){
								pw.print("Vemb"+((ReservUC)(vresuc.elementAt(i))).Id[t]+","+((ReservUC)(vresuc.elementAt(i))).Nombre[t]+","+type_aux+",Vmin_reserva"+","+((ReservUC)(vresuc.elementAt(i))).ReserveMinVolume[t]+",");	
								}
							else{
								pw.print(((ReservUC)(vresuc.elementAt(i))).ReserveMinVolume[t]+",");	
							}
						}
					}
					else if(t>0 & t<T-1){
						for(int k=1;k<=H[t];k++){
							pw.print(((ReservUC)(vresuc.elementAt(i))).ReserveMinVolume[t]+",");		
						}		
					}
					else{
						for(int k=1;k<=H[t];k++){
							if(k==H[t]){
								pw.println(((ReservUC)(vresuc.elementAt(i))).ReserveMinVolume[t]);		
							}
							else{
								pw.print(((ReservUC)(vresuc.elementAt(i))).ReserveMinVolume[t]+",");			
							}		
						}		
					}	
				}
				//B1
				for(int t=0;t<T;t++){
					if(t==0){
						for(int k=1;k<=H[t];k++){
							if(k==1){
								pw.print("Vfic"+((ReservUC)(vresuc.elementAt(i))).Id[t]+","+((ReservUC)(vresuc.elementAt(i))).Nombre[t]+","+type_aux+",B1sol"+","+B1sol[i][t]+",");	
								}
							else{
								pw.print(B1sol[i][t]+",");	
							}
						}
					}
					else if(t>0 & t<T-1){
						for(int k=1;k<=H[t];k++){
							pw.print(B1sol[i][t]+",");		
						}		
					}
					else{
						for(int k=1;k<=H[t];k++){
							if(k==H[t]){
								pw.println(B1sol[i][t]);		
							}
							else{
								pw.print(B1sol[i][t]+",");			
							}		
						}		
					}	
				}
				//B2
				for(int t=0;t<T;t++){
					if(t==0){
						for(int k=1;k<=H[t];k++){
							if(k==1){
								pw.print("Vfic"+((ReservUC)(vresuc.elementAt(i))).Id[t]+","+((ReservUC)(vresuc.elementAt(i))).Nombre[t]+","+type_aux+",B2sol"+","+B2sol[i][t]+",");	
								}
							else{
								pw.print(B2sol[i][t]+",");	
							}
						}
					}
					else if(t>0 & t<T-1){
						for(int k=1;k<=H[t];k++){
							pw.print(B2sol[i][t]+",");		
						}		
					}
					else{
						for(int k=1;k<=H[t];k++){
							if(k==H[t]){
								pw.println(B2sol[i][t]);		
							}
							else{
								pw.print(B2sol[i][t]+",");			
							}		
						}		
					}	
				}	
			}
		
			for(int i=0;i<id_pump.length;i++){
				type_aux="Pump";
				//Potencia bomba electrica
				for(int t=0;t<T;t++){
					if(t==0){
						for(int k=1;k<=H[t];k++){
							if(k==1){
								pw.print("Pbomb"+((PumpUC)(vpumpuc.elementAt(i))).Id[t]+","+((PumpUC)(vpumpuc.elementAt(i))).Nombre[t]+","+type_aux+",Pbomb"+","+Pbombsol[i][t]+",");	
								}
							else{
								pw.print(Pbombsol[i][t]+",");	
							}
						}
					}
					else if(t>0 & t<T-1){
						for(int k=1;k<=H[t];k++){
							pw.print(Pbombsol[i][t]+",");		
						}		
					}
					else{
						for(int k=1;k<=H[t];k++){
							if(k==H[t]){
								pw.println(Pbombsol[i][t]);		
							}
							else{
								pw.print(Pbombsol[i][t]+",");			
							}		
						}		
					}	
				}
				//Potencia bateria_LP
				/*for(int t=0;t<T;t++){
					if(t==0){
						for(int k=1;k<=H[t];k++){
							if(k==1){
								pw.print("Pbomb"+((PumpUC)(vpumpuc.elementAt(i))).Id[t]+","+((PumpUC)(vpumpuc.elementAt(i))).Nombre[t]+",Pbomb_LP"+","+((PumpUC)(vpumpuc.elementAt(i))).PBomba[t]*cplex.getValue(((PumpUC)(vpumpuc.elementAt(i))).Bg[t])+",");	
								}
							else{
								pw.print(((PumpUC)(vpumpuc.elementAt(i))).PBomba[t]*cplex.getValue(((PumpUC)(vpumpuc.elementAt(i))).Bg[t])+",");	
							}
						}
					}
					else if(t>0 & t<T-1){
						for(int k=1;k<=H[t];k++){
							pw.print(((PumpUC)(vpumpuc.elementAt(i))).PBomba[t]*cplex.getValue(((PumpUC)(vpumpuc.elementAt(i))).Bg[t])+",");		
						}		
					}
					else{
						for(int k=1;k<=H[t];k++){
							if(k==H[t]){
								pw.println(((PumpUC)(vpumpuc.elementAt(i))).PBomba[t]*cplex.getValue(((PumpUC)(vpumpuc.elementAt(i))).Bg[t]));		
							}
							else{
								pw.print(((PumpUC)(vpumpuc.elementAt(i))).PBomba[t]*cplex.getValue(((PumpUC)(vpumpuc.elementAt(i))).Bg[t])+",");			
							}		
						}		
					}	
				}*/
				//litros bombeado
				for(int t=0;t<T;t++){
					if(t==0){
						for(int k=1;k<=H[t];k++){
							if(k==1){
								pw.print("Qbomb"+((PumpUC)(vpumpuc.elementAt(i))).Id[t]+","+((PumpUC)(vpumpuc.elementAt(i))).Nombre[t]+","+type_aux+",Qbomb"+","+UnitT*((PumpUC)(vpumpuc.elementAt(i))).KBomba[t]*Pbombsol[i][t]+",");	
								}
							else{
								pw.print(UnitT*((PumpUC)(vpumpuc.elementAt(i))).KBomba[t]*Pbombsol[i][t]+",");	
							}
						}
					}
					else if(t>0 & t<T-1){
						for(int k=1;k<=H[t];k++){
							pw.print(UnitT*((PumpUC)(vpumpuc.elementAt(i))).KBomba[t]*Pbombsol[i][t]+",");		
						}		
					}
					else{
						for(int k=1;k<=H[t];k++){
							if(k==H[t]){
								pw.println(UnitT*((PumpUC)(vpumpuc.elementAt(i))).KBomba[t]*Pbombsol[i][t]);		
							}
							else{
								pw.print(UnitT*((PumpUC)(vpumpuc.elementAt(i))).KBomba[t]*Pbombsol[i][t]+",");			
							}		
						}		
					}	
				}	
			}
			//Lineas	
			for(int i=0;i<id_line.length;i++){
				type_aux="Linea";
				//flujo linea
				print_solution_line(pw,(LineUC)(vlineuc.elementAt(i)), Fsol,type_aux,"Flujo", "Flujo", H, T, i);

			}
			

			//Angulos	
			for(int i=0;i<id_busbar.length;i++){
				print_solution(pw,AnguloSol[i],"Angulo Barra_"+(i+1),H, T);
			}
			
			boolean imprimesolbg=true;
			
		
			pw.println("");
			pw.println("Resumen solucion");	
			pw.println("GAP solucion,"+gap_solucion);
			pw.println("Costo total,"+costo_objetivo/fes);
			pw.println("Costo operacion,"+costo_total);
			pw.println("Costo partida,"+costo_partida_total);
			pw.println("Costo detencion,"+costo_detencion_total);
			pw.println("Costo ENS,"+costototalens);
			pw.println("Solucion,"+solucion1);
			pw.println("Cota Inferior,"+cotainfer1);
			pw.println("Gap agrupa,"+gap1);
			pw.println("Tiempo Solver CPLEX agrupa,"+soltiempo1);
			pw.println("Tiempo Procesamiento datos," + ((time_fin-time_ini_mip)/1000-soltiempo1-soltiempo2));
			pw.println("Tiempo Total,"+(time_fin-time_ini_mip)/1000);
			
			pw.close();
			pw14.close();
			pw15.close();
			
			cplex.end();
			time_fin = System.currentTimeMillis();
			
			//imprimo resumen
			print_resume(pw10, solucion2, cotainfer2,gap2,soltiempo2,solucion1,cotainfer1, gap1,soltiempo1,time_fin,time_ini_mip);
			
		}
    	catch(IloException e) {
    		System.err.println("Concert exception '" + e + "' caught");
    	}
		catch(Exception e){
			e.printStackTrace(System.out);
		}
    	DBObj.closeLink(); /**- Close Link Attempt2 -**/
		System.exit(0);
	}
	
	static void print_resume(PrintWriter pw10, double solucion2, double cotainfer2,double gap2,double soltiempo2,double solucion1,double cotainfer1,double  gap1,double soltiempo1,double time_fin,double time_ini_mip){
	
		//Imprime resumen de resultados
		pw10.println("Solucion,"+solucion1);
		pw10.println("Cota Inferior,"+cotainfer1);
		pw10.println("Gap agrupa,"+gap1);
		pw10.println("Tiempo Solver CPLEX agrupa,"+soltiempo1);
		pw10.println("Tiempo Procesamiento datos," + ((time_fin-time_ini_mip)/1000-soltiempo1-soltiempo2));
		pw10.println("Tiempo Total,"+(time_fin-time_ini_mip)/1000);
		pw10.close();
	}
	// ### Here to set parallel mode use
	static void parametres_cplex(IloCplex cplex, double Relative_gap, double Tmax, int pmode, int maxThds){
		try {
		cplex.setParam(IloCplex.DoubleParam.EpGap,Relative_gap); //Relative_gap
		cplex.setParam(IloCplex.DoubleParam.TiLim,Tmax*60);         //Tmax en minutos
		//cplex.setParam(IloCplex.IntParam.RootAlg,1);  //dual=2
		
		//Cortes para GEVIUC
		//cplex.setParam(IloCplex.IntParam.MIRCuts,-1);
		//cplex.setParam(IloCplex.IntParam.FracCuts,-1);
		//cplex.setParam(IloCplex.IntParam.ImplBd,-1);
		//cplex.setParam(IloCplex.IntParam.Cliques,-1);
		//cplex.setParam(IloCplex.IntParam.Covers,-1);
		//cplex.setParam(IloCplex.IntParam.FlowCovers,-1);
		System.out.println("Cplex Par, ParallelMode: "+pmode);
		System.out.println("Cplex Par, Threads: "+maxThds);		
		cplex.setParam(IloCplex.IntParam.ParallelMode,pmode); //-1 opportunistic, 1: deterministic to max cores
		cplex.setParam(IloCplex.IntParam.Threads,maxThds); // 0: uses the number of cores.
		//cplex.setParam(IloCplex.IntParam.MIPSearch,2);
		cplex.setParam(IloCplex.DoubleParam.TreLim,2048); //1024
		//cplex.setParam(IloCplex.IntParam.HeurFreq,20);
		cplex.setParam(IloCplex.IntParam.RINSHeur,100);
		//cplex.setParam(IloCplex.IntParam.Probe,3);
		//cplex.setParam(IloCplex.IntParam.RelaxPreInd,1);
		cplex.setParam(IloCplex.IntParam.MIPEmphasis,0);
		
		}
		catch(IloException e) {
    		System.err.println("Concert exception '" + e + "' caught");
    	}	
	}
	
	static void print_solution (PrintWriter pw, double[] Solucion, String nombre_solucion, int[] H, int T){
		
		for(int t=0;t<T;t++){
			if(t==0){
				for(int k=1;k<=H[t];k++){
					if(k==1){
						pw.print(",,Sistema,"+nombre_solucion+","+Solucion[t]+",");	
					}
					else{
						pw.print(Solucion[t]+",");	
					}
				}
			}
			else if(t>0 & t<T-1){
				for(int k=1;k<=H[t];k++){
					pw.print(Solucion[t]+",");		
				}		
			}
			else{
				for(int k=1;k<=H[t];k++){
					if(k==H[t]){
						pw.println(Solucion[t]);		
					}
					else{
						pw.print(Solucion[t]+",");			
					}		
				}		
			}	
		}
	}
	
	static void print_solution_gen (PrintWriter pw, TGUC gen, double[][] Solucion, String type, String nombre1, String nombre2, int[] H, int T, int i){
	
		for(int t=0;t<T;t++){
			if(t==0){
				for(int k=1;k<=H[t];k++){
					if(k==1){
						pw.print(nombre1+gen.ID[t]+","+gen.Nombre[t]+","+type+","+nombre2+","+Solucion[i][t]+",");	
						}
					else{
						pw.print(Solucion[i][t]+",");	
					}
				}
			}
			else if(t>0 & t<T-1){
				for(int k=1;k<=H[t];k++){
					pw.print(Solucion[i][t]+",");		
				}		
			}
			else{
				for(int k=1;k<=H[t];k++){
					if(k==H[t]){
						pw.println(Solucion[i][t]);		
					}
					else{
						pw.print(Solucion[i][t]+",");			
					}		
				}		
			}	
		}
	}
	static void print_solution_gen2 (PrintWriter pw, TGUC gen, double[] Solucion, String type, String nombre1, String nombre2, int[] H, int T){
	
		for(int t=0;t<T;t++){
			if(t==0){
				for(int k=1;k<=H[t];k++){
					if(k==1){
						pw.print(nombre1+gen.ID[t]+","+gen.Nombre[t]+","+type+","+nombre2+","+Solucion[t]+",");	
						}
					else{
						pw.print(Solucion[t]+",");	
					}
				}
			}
			else if(t>0 & t<T-1){
				for(int k=1;k<=H[t];k++){
					pw.print(Solucion[t]+",");		
				}		
			}
			else{
				for(int k=1;k<=H[t];k++){
					if(k==H[t]){
						pw.println(Solucion[t]);		
					}
					else{
						pw.print(Solucion[t]+",");			
					}		
				}		
			}	
		}
	}
	static void print_solution_genh (PrintWriter pw, HGUC gen, double[][] Solucion, String type, String nombre1, String nombre2, int[] H, int T, int i){
	
		for(int t=0;t<T;t++){
			if(t==0){
				for(int k=1;k<=H[t];k++){
					if(k==1){
						pw.print(nombre1+gen.ID[t]+","+gen.Nombre[t]+","+type+","+nombre2+","+Solucion[i][t]+",");	
						}
					else{
						pw.print(Solucion[i][t]+",");	
					}
				}
			}
			else if(t>0 & t<T-1){
				for(int k=1;k<=H[t];k++){
					pw.print(Solucion[i][t]+",");		
				}		
			}
			else{
				for(int k=1;k<=H[t];k++){
					if(k==H[t]){
						pw.println(Solucion[i][t]);		
					}
					else{
						pw.print(Solucion[i][t]+",");			
					}		
				}		
			}	
		}
	}
	static void print_solution_genh2 (PrintWriter pw, HGUC gen, double[] Solucion, String type, String nombre1, String nombre2, int[] H, int T){
	
		for(int t=0;t<T;t++){
			if(t==0){
				for(int k=1;k<=H[t];k++){
					if(k==1){
						pw.print(nombre1+gen.ID[t]+","+gen.Nombre[t]+","+type+","+nombre2+","+Solucion[t]+",");	
						}
					else{
						pw.print(Solucion[t]+",");	
					}
				}
			}
			else if(t>0 & t<T-1){
				for(int k=1;k<=H[t];k++){
					pw.print(Solucion[t]+",");		
				}		
			}
			else{
				for(int k=1;k<=H[t];k++){
					if(k==H[t]){
						pw.println(Solucion[t]);		
					}
					else{
						pw.print(Solucion[t]+",");			
					}		
				}		
			}	
		}
	}
	static void print_solution_line (PrintWriter pw, LineUC gen, double[][] Solucion, String type, String nombre1, String nombre2, int[] H, int T, int i){
	
		for(int t=0;t<T;t++){
			if(t==0){
				for(int k=1;k<=H[t];k++){
					if(k==1){
						pw.print(","+","+"Flujo"+","+gen.Nombre[t]+","+Solucion[i][t]+",");	
						}
					else{
						pw.print(Solucion[i][t]+",");	
					}
				}
			}
			else if(t>0 & t<T-1){
				for(int k=1;k<=H[t];k++){
					pw.print(Solucion[i][t]+",");		
				}		
			}
			else{
				for(int k=1;k<=H[t];k++){
					if(k==H[t]){
						pw.println(Solucion[i][t]);		
					}
					else{
						pw.print(Solucion[i][t]+",");			
					}		
				}		
			}	
		}
	}
	
	static void print_solution_angulo (PrintWriter pw, BusUC gen, double[][] Solucion, String type, String nombre1, String nombre2, int[] H, int T, int i){
	
		for(int t=0;t<T;t++){
			if(t==0){
				for(int k=1;k<=H[t];k++){
					if(k==1){
						pw.print(gen.Nombre[t]+","+Solucion[i][t]+",");	
						}
					else{
						pw.print(Solucion[i][t]+",");	
					}
				}
			}
			else if(t>0 & t<T-1){
				for(int k=1;k<=H[t];k++){
					pw.print(Solucion[i][t]+",");		
				}		
			}
			else{
				for(int k=1;k<=H[t];k++){
					if(k==H[t]){
						pw.println(Solucion[i][t]);		
					}
					else{
						pw.print(Solucion[i][t]+",");			
					}		
				}		
			}	
		}
	}
	
	
	//Funcion para obtener solucion de variables binarias
	static void solucion_binarias_termicas(IloCplex cplex,Vector vt, int[] id_tgenerator, double[][][] Bsol,double[][] Bgsol,double[][] Bgasol,double[][] Bgpsol,double[][] Xsol,double[][] Ysol,double[][] set_bgh1, double[][][] set_bh1,int T){
		
		try {
			inicializa3double(Bsol);
			for(int i=0;i<id_tgenerator.length;i++){
				for(int t=0;t<T;t++){
					for(int iv=0;iv<((TGUC)(vt.elementAt(i))).Niv[t];iv++){
						if(set_bgh1[i][t] !=1 || set_bh1[i][iv][t] !=0){
							Bsol[i][iv][t]= cplex.getValue(((TGUC)(vt.elementAt(i))).B[t][iv]);
							//ajustes para evitar errores numericos al momento de resolver el problema LP
							if(Bsol[i][iv][t]<0.1){
								Bsol[i][iv][t]=0;
							}
							else if(Bsol[i][iv][t]>0.9){
								Bsol[i][iv][t]=1;
							}
						}
					}
				}
			}
			inicializa2double(Bgsol);
			
			inicializa2double(Bgasol);
			for(int i=0;i<id_tgenerator.length;i++){
				for(int t=0;t<T;t++){
					Bgasol[i][t]= cplex.getValue(((TGUC)(vt.elementAt(i))).Bga[t]);
					//ajustes para evitar errores numericos al momento de resolver el problema LP
					if(Bgasol[i][t]<0.1){
						Bgasol[i][t]=0;
					}
					else if(Bgasol[i][t]>0.9){
						Bgasol[i][t]=1;
					}
				}
			}
			inicializa2double(Bgpsol);
			for(int i=0;i<id_tgenerator.length;i++){				
				for(int t=0;t<T;t++){
					Bgpsol[i][t]= cplex.getValue(((TGUC)(vt.elementAt(i))).Bgp[t]);
					if(Bgpsol[i][t]<0.1){
						Bgpsol[i][t]=0;
					}
					else if(Bgpsol[i][t]>0.9){
						Bgpsol[i][t]=1;
					}
				}
			}
			
			inicializa2double(Xsol);
			for(int i=0;i<id_tgenerator.length;i++){				
				for(int t=0;t<T;t++){
					Xsol[i][t]= cplex.getValue(((TGUC)(vt.elementAt(i))).X[t]);
				}
			}
			
			inicializa2double(Ysol);
			for(int i=0;i<id_tgenerator.length;i++){				
				for(int t=0;t<T;t++){
					Ysol[i][t]= cplex.getValue(((TGUC)(vt.elementAt(i))).Y[t]);
				}
			}
		
		}
		catch(IloException e) {
    		System.err.println("Concert exception solucion binarias centrales termicas'" + e + "' caught");
    	}
	}
	
	static void solucion_binarias_csp(IloCplex cplex,Vector vcspuc, int[] id_csp,double[][] Bel1sol,double[][] Balmsol, double[][] Bgcspsol,int T){
		try {
			for(int t=0;t<T;t++){
				for(int k=0;k<id_csp.length;k++){
					Bel1sol[k][t]=cplex.getValue(((CSPUC)(vcspuc.elementAt(k))).Bel1[t]);
					Balmsol[k][t]=cplex.getValue(((CSPUC)(vcspuc.elementAt(k))).Balm[t]);
					Bgcspsol[k][t]	 =cplex.getValue(((CSPUC)(vcspuc.elementAt(k))).Bg[t]);
				}
			}
		}
		catch(IloException e) {
    		System.err.println("Concert exception solucion binarias CSP '" + e + "' caught");
    	}
	}
	
	static void solucion_binarias_battery(IloCplex cplex,Vector vbatuc,int[] id_battery, double[][] Bpossol,double[][] Bnegsol,double[][][] Bbsol, int T){
		
		try {
			inicializa2double(Bpossol);
			for(int t=0;t<T;t++){
				for(int i=0;i<id_battery.length;i++){
					Bpossol[i][t]=cplex.getValue(((BatteryUC)(vbatuc.elementAt(i))).Bpos[t]);
				}
			}
			inicializa2double(Bnegsol);
			for(int t=0;t<T;t++){
				for(int i=0;i<id_battery.length;i++){
					Bnegsol[i][t]=cplex.getValue(((BatteryUC)(vbatuc.elementAt(i))).Bneg[t]);
				}
			}
			for(int t=0;t<T;t++){
				for(int i=0;i<id_battery.length;i++){
					for(int s=0;s<((BatteryUC)(vbatuc.elementAt(i))).n_iv;s++){	
						Bbsol[i][s][t]=cplex.getValue(((BatteryUC)(vbatuc.elementAt(i))).B[s][t]);
					}
				}
			}
		}
		catch(IloException e) {
    		System.err.println("Concert exception solucion binarias Battery '" + e + "' caught");
    	}
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
    /*static class MyCutBranch extends IloCplex.CutCallback {
    	
    	//constructor
    	PrintWriter pw;
    	PrintWriter pw2;
    	double time_ini;
    	IloNumVar[] var;
    	
		MyCutBranch(PrintWriter pw2,double time_ini,IloNumVar[] var){
    		//this.pw			= pw;
    		this.pw2		= pw2;
    		this.time_ini	= time_ini;
    		this.var		= var;
    	}
		
		public void main() throws IloException  {

		}
    }*/
	
	static class MyBranch1 extends IloCplex.MIPCallback {
    	
    	//constructor
    	PrintWriter pw;
    	double time_ini;
    	IloNumVar[] var;
    	
		MyBranch1(PrintWriter pw,double time_ini){
    		this.pw		= pw;
    		this.time_ini	= time_ini;
    		this.var		= var;
    	}
		
		public void main() throws IloException  {
  
    		pw.print((System.currentTimeMillis()-time_ini)/1000+",");
			pw.print(getIncumbentObjValue()+",");
			pw.print(getBestObjValue()+",");
    		pw.println((getIncumbentObjValue()-getBestObjValue())/getIncumbentObjValue());
		
		}
    }
  
	static class MyBranch extends IloCplex.BranchCallback {
		IloNumVar[] _vars;
		Vector vt;
		int T;
		double[][] set_bgh1;
		double[][][] set_bh1;
		
		//constructor
		MyBranch(IloNumVar[] vars, Vector vt, int T, double[][] set_bgh1, double[][][] set_bh1) {
			_vars = vars; 
			this.vt=vt;
			this.T=T;
			this.set_bgh1=set_bgh1;
			this.set_bh1=set_bh1;
		}

		public void main() throws IloException {
			if ( !getBranchType().equals(IloCplex.BranchType.BranchOnVariable) )
            return; 
       
			// Branch on var with largest objective coefficient
			// among those with largest infeasibility
		   
			double[] x   = getValues  (_vars);
			double[] obj = getObjCoefs(_vars);
			
			//retorna un arreglo de objetos "infactibilidades (feas)" a partir de las variables _vars 
			IloCplex.IntegerFeasibilityStatus[] feas = getFeasibilities(_vars);
		   
			double maxinf = 0.0;
			double maxobj = 0.0;
			int    bestj  = -1;
			double maxerror= 0;  
			double maxcosto= 0;
			String bestnombre="";
			int ordent=10000;
			int    cols   = _vars.length;
			double errorpmin=0;
			int rr=0;
			for(int t=0;t<T;t++){
				for(int i=0;i<vt.size();i++){
					for(int iv=0;iv<((TGUC)(vt.elementAt(i))).Niv[t];iv++){
						if(set_bgh1[i][t] !=1 || set_bh1[i][iv][t] !=0){
							if ( feas[rr].equals(IloCplex.IntegerFeasibilityStatus.Infeasible) ) {
								if(x[rr]<0.5)
								errorpmin= (((TGUC)vt.elementAt(i)).Pminuc[((TGUC)vt.elementAt(i)).M[t][iv][0]][t])*x[rr];
								else
								errorpmin= (((TGUC)vt.elementAt(i)).Pminuc[((TGUC)vt.elementAt(i)).M[t][iv][0]][t])*(1-x[rr]);
								
								//if(errorpmin > maxerror & ((TGUC)(vt.elementAt(i))).Alfauc[iv][t]> maxcosto){
								//if(errorpmin > maxerror){ //se branchea con la variable que tiene el maximo error de potencia y el menor periodo
								if(getNnodes()<=500){
									if( errorpmin > maxerror & ((TGUC)vt.elementAt(i)).ID[t]==76 & t >= 8){ //se branchea con la variable que tiene el maximo error de potencia y el menor periodo	
										maxerror=errorpmin;
										bestj=rr;
										bestnombre=_vars[rr].getName();
										ordent=t;
										maxcosto=((TGUC)(vt.elementAt(i))).Alfauc[iv][t];	
									}
								}
								//System.out.println("t= "+(t+1)+ " iv= "+(iv+1) + "var= "+ _vars[rr].getName()+ "Pmin= "+((TGUC)vt.elementAt(i)).Pminuc[((TGUC)vt.elementAt(i)).M[t][iv][0]][t] +" INFAC= "+redondear(x[rr],2) + "error= "+errorpmin);
								else if(getNnodes()>500 & getNnodes()<=2000){
									if( errorpmin > maxerror & ((TGUC)vt.elementAt(i)).ID[t]==76 & t >= 72 & t < 161){ //se branchea con la variable que tiene el maximo error de potencia y el menor periodo	
										maxerror=errorpmin;
										bestj=rr;
										bestnombre=_vars[rr].getName();
										ordent=t;
										maxcosto=((TGUC)(vt.elementAt(i))).Alfauc[iv][t];	
									}
								}
								else if(getNnodes()>2000){
									if( errorpmin > maxerror & ((TGUC)vt.elementAt(i)).ID[t]==85 & t < 8){ //se branchea con la variable que tiene el maximo error de potencia y el menor periodo	
										maxerror=errorpmin;
										bestj=rr;
										bestnombre=_vars[rr].getName();
										ordent=t;
										maxcosto=((TGUC)(vt.elementAt(i))).Alfauc[iv][t];	
									}
								}
							}
						rr++;
						}
					}
				}
			}
			//System.out.println("error maximo= "+maxerror+ " perido= "+ordent+ " nombre= "+bestnombre);
							
			//recorro todas las variables
			/*for (int j = 0; j < cols; ++j) {
				
				//chequeo si feas es infactible
				if ( feas[j].equals(IloCplex.IntegerFeasibilityStatus.Infeasible) ) {
					
					//calculo la infactibilidad. Ejemplo   xj= 1.4   Math.floor(1.4)=1.0  >> xj_inf=1.4-1.0=0.4
					double xj_inf = x[j] - Math.floor(x[j]);
					
					//caso en que la infactibililidad sea mayor a 0.5. Ejemplo xj=1.9   >> xj_inf = 1.0-0.9=0.1
					if ( xj_inf > 0.5 )  xj_inf = 1.0 - xj_inf;
					
					
					if ( x[j] < 0.5 && xj_inf >= maxinf) {
						bestj  = j;
						maxinf = xj_inf;
						maxobj = Math.abs(obj[j]);
					}                                             
				}
			}
			*/
			//System.out.println("SUBSTRING=  "+(_vars[bestj].getName()).substring(0,2));
			//if ( bestj >= 0 && ((_vars[bestj].getName()).substring(0,6).equals("Bg(24,") )) {
			if ( bestj >= 0 ) {
				//System.out.println("NOMBRE= "+_vars[bestj].getName());
				makeBranch(_vars[bestj], x[bestj],
				IloCplex.BranchDirection.Up,   getObjValue());
				makeBranch(_vars[bestj], x[bestj],
				IloCplex.BranchDirection.Down, getObjValue());
			}
		}
   }
    
   static void printsol_sol(PrintWriter pw, Vector vsoluc, int id_solar[], double Psolsol[][], double[][] Rgsolsol, double[][] Rgsolsol2, double[][] CPFsolsol, int H[], int T){
	   //Se imprime solucion de plantas solares
	    String type_aux="Solar";
		for(int i=0;i<id_solar.length;i++){
			//Potencia Solar
			for(int t=0;t<T;t++){
				if(t==0){
					for(int k=1;k<=H[t];k++){
						if(k==1){
							pw.print("Solar"+((SolarUC)(vsoluc.elementAt(i))).Id[t]+","+((SolarUC)(vsoluc.elementAt(i))).Nombre[t]+","+type_aux+",P"+","+Psolsol[i][t]+",");	
							}
						else{
							pw.print(Psolsol[i][t]+",");	
						}
					}
				}
				else if(t>0 & t<T-1){
					for(int k=1;k<=H[t];k++){
						pw.print(Psolsol[i][t]+",");		
					}		
				}
				else{
					for(int k=1;k<=H[t];k++){
						if(k==H[t]){
							pw.println(Psolsol[i][t]);		
						}
						else{
							pw.print(Psolsol[i][t]+",");			
						}		
					}		
				}	
			}
			//Reserva en giro 1
			for(int t=0;t<T;t++){
				if(t==0){
					for(int k=1;k<=H[t];k++){
						if(k==1){
							pw.print("Solar"+((SolarUC)(vsoluc.elementAt(i))).Id[t]+","+((SolarUC)(vsoluc.elementAt(i))).Nombre[t]+","+type_aux+",Rgiro"+","+Rgsolsol[i][t]+",");	
							}
						else{
							pw.print(Rgsolsol[i][t]+",");	
						}
					}
				}
				else if(t>0 & t<T-1){
					for(int k=1;k<=H[t];k++){
						pw.print(Rgsolsol[i][t]+",");		
					}		
				}
				else{
					for(int k=1;k<=H[t];k++){
						if(k==H[t]){
							pw.println(Rgsolsol[i][t]);		
						}
						else{
							pw.print(Rgsolsol[i][t]+",");			
						}		
					}		
				}	
			}
			//Reserva en giro 2
			for(int t=0;t<T;t++){
				if(t==0){
					for(int k=1;k<=H[t];k++){
						if(k==1){
							pw.print("Solar"+((SolarUC)(vsoluc.elementAt(i))).Id[t]+","+((SolarUC)(vsoluc.elementAt(i))).Nombre[t]+","+type_aux+",Rgiro2"+","+Rgsolsol2[i][t]+",");	
							}
						else{
							pw.print(Rgsolsol2[i][t]+",");	
						}
					}
				}
				else if(t>0 & t<T-1){
					for(int k=1;k<=H[t];k++){
						pw.print(Rgsolsol2[i][t]+",");		
					}		
				}
				else{
					for(int k=1;k<=H[t];k++){
						if(k==H[t]){
							pw.println(Rgsolsol2[i][t]);		
						}
						else{
							pw.print(Rgsolsol2[i][t]+",");			
						}		
					}		
				}	
			}
			//Control primario
			for(int t=0;t<T;t++){
				if(t==0){
					for(int k=1;k<=H[t];k++){
						if(k==1){
							pw.print("Solar"+((SolarUC)(vsoluc.elementAt(i))).Id[t]+","+((SolarUC)(vsoluc.elementAt(i))).Nombre[t]+","+type_aux+",CPF"+","+CPFsolsol[i][t]+",");	
							}
						else{
							pw.print(CPFsolsol[i][t]+",");	
						}
					}
				}
				else if(t>0 & t<T-1){
					for(int k=1;k<=H[t];k++){
						pw.print(CPFsolsol[i][t]+",");		
					}		
				}
				else{
					for(int k=1;k<=H[t];k++){
						if(k==H[t]){
							pw.println(CPFsolsol[i][t]);		
						}
						else{
							pw.print(CPFsolsol[i][t]+",");			
						}		
					}		
				}	
			}
		}   
	   
   }
	static void printsol_eol(PrintWriter pw, Vector veoluc, int id_eolic[], double Peolsol[][], double[][] Rgeolsol, double[][] Rgeolsol2, double[][] CPFeolsol, int H[], int T){
		String type_aux="Eolica";
		for(int i=0;i<id_eolic.length;i++){
				
			//Potencia Eleolica
			for(int t=0;t<T;t++){
				if(t==0){
					for(int k=1;k<=H[t];k++){
						if(k==1){
							pw.print("Eolic"+((EolicUC)(veoluc.elementAt(i))).Id[t]+","+((EolicUC)(veoluc.elementAt(i))).Nombre[t]+","+type_aux+",P"+","+Peolsol[i][t]+",");	
							}
						else{
							pw.print(Peolsol[i][t]+",");	
						}
					}
				}
				else if(t>0 & t<T-1){
					for(int k=1;k<=H[t];k++){
						pw.print(Peolsol[i][t]+",");		
					}		
				}
				else{
					for(int k=1;k<=H[t];k++){
						if(k==H[t]){
							pw.println(Peolsol[i][t]);		
						}
						else{
							pw.print(Peolsol[i][t]+",");			
						}		
					}		
				}	
			}
			//Reserva en giro 1
			for(int t=0;t<T;t++){
				if(t==0){
					for(int k=1;k<=H[t];k++){
						if(k==1){
							pw.print("Eolic"+((EolicUC)(veoluc.elementAt(i))).Id[t]+","+((EolicUC)(veoluc.elementAt(i))).Nombre[t]+","+type_aux+",Rgiro"+","+Rgeolsol[i][t]+",");	
							}
						else{
							pw.print(Rgeolsol[i][t]+",");	
						}
					}
				}
				else if(t>0 & t<T-1){
					for(int k=1;k<=H[t];k++){
						pw.print(Rgeolsol[i][t]+",");		
					}		
				}
				else{
					for(int k=1;k<=H[t];k++){
						if(k==H[t]){
							pw.println(Rgeolsol[i][t]);		
						}
						else{
							pw.print(Rgeolsol[i][t]+",");			
						}		
					}		
				}	
			}
			//Reserva en giro 2
			for(int t=0;t<T;t++){
				if(t==0){
					for(int k=1;k<=H[t];k++){
						if(k==1){
							pw.print("Eolic"+((EolicUC)(veoluc.elementAt(i))).Id[t]+","+((EolicUC)(veoluc.elementAt(i))).Nombre[t]+","+type_aux+",Rgiro2"+","+Rgeolsol2[i][t]+",");	
							}
						else{
							pw.print(Rgeolsol2[i][t]+",");	
						}
					}
				}
				else if(t>0 & t<T-1){
					for(int k=1;k<=H[t];k++){
						pw.print(Rgeolsol2[i][t]+",");		
					}		
				}
				else{
					for(int k=1;k<=H[t];k++){
						if(k==H[t]){
							pw.println(Rgeolsol2[i][t]);		
						}
						else{
							pw.print(Rgeolsol2[i][t]+",");			
						}		
					}		
				}	
			}
			//Control primario
			for(int t=0;t<T;t++){
				if(t==0){
					for(int k=1;k<=H[t];k++){
						if(k==1){
							pw.print("Eolic"+((EolicUC)(veoluc.elementAt(i))).Id[t]+","+((EolicUC)(veoluc.elementAt(i))).Nombre[t]+","+type_aux+",CPF"+","+CPFeolsol[i][t]+",");	
							}
						else{
							pw.print(CPFeolsol[i][t]+",");	
						}
					}
				}
				else if(t>0 & t<T-1){
					for(int k=1;k<=H[t];k++){
						pw.print(CPFeolsol[i][t]+",");		
					}		
				}
				else{
					for(int k=1;k<=H[t];k++){
						if(k==H[t]){
							pw.println(CPFeolsol[i][t]);		
						}
						else{
							pw.print(CPFeolsol[i][t]+",");			
						}		
					}		
				}	
			}
			
		}
	}   
    static int NTr(int[] H, int paso){
    	//determina el numero de periodos
    	int t=0;
    	int taux=0;
    	int tt=0;
    	int Tr=0;
    	
    	while(t < H.length){
    		taux=0;
    		while(taux<paso & t< H.length){
    			taux=taux+H[t];
    			t=t+1;
    		}
    		tt=tt+1;
    		Tr=tt;
    	}
    	return Tr;
    }
    
    static int[] Reduccion(int[] H, int[] Hr, int paso){
    	//funcion que determina el nuevo vector de duracion de periodos
    	int t=0;
    	int taux=0;
    	int tt=0;
    	
    	while(t < H.length){
    		taux=0;
    		while(taux<paso & t < H.length){
    			taux=taux+H[t];
    			t=t+1;
    		}
    		Hr[tt]=taux;
    		tt=tt+1;
    	}
    	return Hr;
    }
    
    static void Ordenar(int[][] orden, double[][] CMedio, int T){
    	//funcion que ordena centrales por costo medio
    	int temporal;
    	double temporal2;
    	double[][] CMedio_copia=new double[orden[0].length][T];
    	for(int t=0;t<T;t++){
    		for (int i=0;i<orden[0].length;i++){
    			CMedio_copia[i][t]=CMedio[i][t];
    		}
    	}
    	
    	
    	for(int t=0;t<T;t++){
    		temporal=0;
    		temporal2=0;
    		for (int j=orden[0].length-1;j>0;j--){
    			for (int i=0;i<j;i++){
    				if (CMedio_copia[i][t]<CMedio_copia[i+1][t]){
    					temporal = orden[t][i+1];
    					temporal2= CMedio_copia[i+1][t];
    					orden[t][i+1] = orden[t][i];
    					CMedio_copia[i+1][t]= CMedio_copia[i][t];
    					orden[t][i] = temporal;
    					CMedio_copia[i][t]= temporal2;
    				}
    			}
    		}
    	}
   }
	//funcion para redondear
	static double redondear( double numero, int decimales ) {
		return Math.round(numero*Math.pow(10,decimales))/Math.pow(10,decimales);
  	}
   
	//funcion para inicializar arreglos double
	static void inicializa1double( double[] arreglo) {
		for(int t=0;t<arreglo.length;t++){
	   		arreglo[t]=0;
	   	}
	}  
    //funcion para inicializar arreglos double
	static void inicializa2double( double[][] arreglo) {
		for(int i=0;i<arreglo.length;i++){
	   		for(int t=0;t<arreglo[i].length;t++){
	   			arreglo[i][t]=0;
	   		}
	   	}
	}
	//funcion para inicializar arreglos double
	static void inicializa3double(double[][][] arreglo) {
		for(int i=0;i<arreglo.length;i++){
	   		for(int t=0;t<arreglo[i].length;t++){
	   			for(int j=0;j<arreglo[i][t].length;j++){
					arreglo[i][t][j]=0;
				}
			}
	   	}
	}
	//funcion para leer solucion inicial
	static void leesolbg(String archivosolbg, double[][] solinibg) throws Exception{
		File fReader;
		BufferedReader bReader;
		String line;
		String array[];
		fReader=new File(archivosolbg);
		bReader = new BufferedReader(new FileReader(fReader));
		int j=0;
		while ((line = bReader.readLine()) != null){
			array=line.split(",");
			for(int k=0;k<array.length;k++){
				if(k>0){
					solinibg[j][k-1]=Double.valueOf(array[k]).doubleValue();
				}
			}
			j++;
		}
		bReader.close();
	}
	//funcion para leer solucion inicial
	static void leesolb(String archivosolb, double[] solinib) throws Exception{
		File fReader;
		BufferedReader bReader;
		String line;
		String array[];
		fReader=new File(archivosolb);
		bReader = new BufferedReader(new FileReader(fReader));
		int j=0;
		while ((line = bReader.readLine()) != null){
			solinib[j]=Double.valueOf(line).doubleValue();
			j++;
		}
		bReader.close();
	}
	//funcion para obtener demanda reducida
	static void dem_red(double[] demanda, int[] agrupacion_optima) throws Exception{
		int nvar=12; //numero de periodos de demanda diaria agrupada, el maximo es nvar=24
		int[] xazar = new int[nvar];
		int k=0;
		int s=0;
		int i=0;
		boolean flag=false;
		int cont=0;
		int n_verd=0;
		int nmuestras=1000000;
		int indice=0;
		double sum=0;
		double sum_error=0;
		double error=0;
		double demanda_red=0;
		int[] secuencia_valida = new int[nmuestras];
		int[][] secuencia = new int[nmuestras][nvar];
		int[] secuencia_nk = new int[nmuestras];
		double[] dem_red = new double[nmuestras];
		double[] secuencia_errordem = new double[nmuestras];
		double minimo_error=1000000000;
		int indice_minimo=-1;
		
		while(s<nmuestras){
			//inicializo secuencia
			secuencia_valida[s]=0; //0 significa secuencia no valida
			secuencia_nk[s]=0;     //0 
			cont=0;
			//System.out.println("inicio");
			for(k=0;k<nvar;k++){
				//genero numeros al azar entre 1 y 4
				xazar[k]=(int)Math.floor(Math.random()*4+1);
				secuencia[s][k]=xazar[k];
				cont=xazar[k]+cont;
				//System.out.println("azar= "+xazar[k]);
				if(cont==24){
					secuencia_valida[s]=1;
					secuencia_nk[s]=k;
					//System.out.println("verdadero!!!");
					n_verd++;
					//System.out.println("NVERDADERO= "+n_verd);
				}
			}
			s++;
		}
		//calculo demanda reducida
		indice=0;
		sum=0;
		s=0;
		while(s<nmuestras){
			secuencia_errordem[s] = 1000000000;
			sum_error=0;
			indice=0;
			if(secuencia_valida[s]==1){
				k=0;
				//System.out.println("NTRAMOS= "+secuencia_nk[s]);
				while(k<=secuencia_nk[s]){
					sum=0;
					error=0;
					i=1;
					//calculo promedio
					//System.out.println("NK= "+secuencia[s][k]);
					while(i<=secuencia[s][k]){
						sum = sum + demanda[indice+i-1];
						i++;
					}
					demanda_red=sum/secuencia[s][k];
					
					//calculo error de demanda promedio con demanda real
					i=1;
					while(i<=secuencia[s][k]){
						//System.out.println("demanda real=" + demanda[indice+i-1] + "demanda_promedio= "+demanda_red);
						error=error+Math.pow(demanda_red-demanda[indice+i-1],2);
						i++;
					}
					
					sum_error=sum_error + error;		
					//System.out.println("indice= "+indice);
					indice=indice+ secuencia[s][k];
					k++;	
				}
				secuencia_errordem[s] = sum_error/24;
				//guardo secuencia con error minimo
				if(secuencia_errordem[s] < minimo_error){
					minimo_error=secuencia_errordem[s];
					indice_minimo=s;
				}
			}
			
			s++;
		}
		System.out.println("FIN AZAR");
		System.out.println("indice minimo= "+indice_minimo);
		for(k=0;k<=secuencia_nk[indice_minimo];k++){
			System.out.println("SECUENCIA= "+secuencia[indice_minimo][k]);
			agrupacion_optima[k]=secuencia[indice_minimo][k];
		}
	}

}
