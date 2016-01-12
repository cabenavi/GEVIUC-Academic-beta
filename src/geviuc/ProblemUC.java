import ilog.concert.*;
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

//clase para formular el problema de optimizacion entero-mixto

	public class ProblemUC{
		
		//constructor
		public ProblemUC(){
		}
			
		//formulacion problema optimizacion
		public void formulate(IloCplex cplex, Vector vt, Vector vh, Vector vsoluc, Vector veol, Vector veoluc, Vector vbat , Vector vbatuc, Vector vpump, Vector vpumpuc, Vector vres, Vector vresuc, Vector vload, Vector vloaduc, Vector vcsp, Vector vcspuc, Vector vlineuc, Vector vbusuc, IloRange[][] restric_demanda, int[] id_tgenerator, int[] id_hgenerator, int[] id_reserv, int[] id_hdb_ser, int[] id_hdb_paspur, int[] id_hdb_irr, int[] id_hdb_affl, int[] id_busbar, int[] id_line, int[] id_load, int[] id_solar, int[] id_eolic, int[] id_battery, int[] id_pump, int[] id_csp, int[] H, int T, double UnitT, double[][] demanda, double[] Spinning, double[] Spinning2, double[] CPF, double[] ERNCLimit, double Cpvfic, double[] generacion_eolic, double[] generacion_solar, double[] generacion_renovable, double[] sum_pmin, double fes, double fcon, int[][] bus, boolean bandera_perdida, double Sbase, double Vbase, double Zbase) throws Exception{
			
			//variables axiliares que se necesitan para formular problema de optimizacion
			IloNumExpr p	 			= cplex.constant(0);   //potencia generada en operacion normal
			IloNumExpr pa				= cplex.constant(0);   //potencia generada en rampa arranque
			IloNumExpr pp	 			= cplex.constant(0);   //potencia generada en rampa parada
			IloNumExpr pa_c	 			= cplex.constant(0);   //potencia generada en rampa arranque
			IloNumExpr pp_c	 			= cplex.constant(0);   //potencia generada en rampa parada
			IloNumExpr pbat	 			= cplex.constant(0);   //potencia bateria		    	
			IloNumExpr pbomb 			= cplex.constant(0);   //potencia bomba
			IloNumExpr pcsp  			= cplex.constant(0);   //potencia csp
			IloNumExpr peol  			= cplex.constant(0);   //potencia eolica
			IloNumExpr paux	 			= cplex.constant(0);   //variable auxiliar para representar potencia
			IloNumExpr flow	 			= cplex.constant(0);   //variable auxiliar para representar flujo por linea
			IloNumExpr dif_theta   		= cplex.constant(0);   //variable auxiliar para representar diferencia de angulo
					
			
			//funcion objetivo
			IloNumExpr costo  			= cplex.constant(0);   //variable auxiliar para definir costo de operacion, partida y parada + penalizaciones
			IloObjective F;
			
			//variables para representar acoplamiento de unidades
			IloNumExpr sum_cc2=cplex.constant(0);				//variables auxiliares para representar restriccion de acoplamiento ciclos combinados
			IloNumExpr p_tv=cplex.constant(0);					//variables auxiliares para representar restriccion de acoplamiento ciclos combinados
			IloNumExpr sum_depen=cplex.constant(0);				
			IloNumExpr sum_depen2=cplex.constant(0);
			
			IloNumExpr balanceh			= cplex.constant(0);   //variable auxiliar para representar restriccion balance hidrico
			IloNumExpr sum_demanda_desp = cplex.constant(0);   //variable auxiliar para representar control de demanda
			
			boolean boolean_depen = true; 
			
			//indices asociados a unidades y etapas
			int i; 
			int j; 
			int t; 
			int k; 
			int r; 
			
			int ia;
			int ip;
			double sum_h=0;
			int cont_desp = 1;
			double demanda_desp = 0;
			boolean varbaux=false;
			int cont_cc=0;
			double max_aux=0;
			
			//********************************************RESTRICCIONES POR OBJETO****************************************///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			System.out.println("");
			System.out.println("restricciones no acopladas entre unidades");
			
			
			//Primero se formulan las restriccion no acopladas de los objetos
			//***************************************RESTRICCIONES CENTRALES TERMICAS*****************************************///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			for(i=0;i<id_tgenerator.length;i++){
				for(t=0;t<T;t++){
					//restriccion potencia minima
					((TGUC)(vt.elementAt(i))).restrpmin(cplex,t);
					//restriccion potencia maxima
					((TGUC)(vt.elementAt(i))).restrpmax(cplex,t);
					//restricciones de formulacion de flujo
					((TGUC)(vt.elementAt(i))).restrflujo(cplex,t);
					//
					((TGUC)(vt.elementAt(i))).restrinic(cplex,t);
					//restriccion generacion forzada
					((TGUC)(vt.elementAt(i))).restrforz(cplex,t);
					//
					((TGUC)(vt.elementAt(i))).restresta(cplex,t);
					//
					if(t-1>0){ 
						if(((TGUC)(vt.elementAt(i))).set_bgh1[t-1]==1 & ((TGUC)(vt.elementAt(i))).set_bgh1[t]==1){
							((TGUC)(vt.elementAt(i))).restrsbga(cplex,t);
							((TGUC)(vt.elementAt(i))).restrsbgp(cplex,t);
						}
					}
					//reserva en giro maxima
					((TGUC)(vt.elementAt(i))).restrspinnmax(cplex,t);						
					//reserva en giro maxima v2
					((TGUC)(vt.elementAt(i))).restrspinnmax2(cplex,t);						
					//commitment
					((TGUC)(vt.elementAt(i))).restrcommit(cplex,t);				
				}
				((TGUC)(vt.elementAt(i))).restrvfin(cplex,T-1);
			}
			//***************************************RESTRICCIONES CENTRALES HIDRAULICAS*****************************************///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			for(j=0;j<id_hgenerator.length;j++){
				for(t=0;t<T;t++){
					for(t=0;t<T;t++){
						//restriccion potencia minima
						((HGUC)(vh.elementAt(j))).restrpmin(cplex,t);
						//restriccion potencia maxima
						((HGUC)(vh.elementAt(j))).restrpmax(cplex,t);
						//restricciones de formulacion de flujo
						((HGUC)(vh.elementAt(j))).restrflujo(cplex,t);
						//
						((HGUC)(vh.elementAt(j))).restrinic(cplex,t);
						//
						((HGUC)(vh.elementAt(j))).restrforz(cplex,t);
						//reserva en giro maxima
						((HGUC)(vh.elementAt(j))).restrspinnmax(cplex,t);						
						//reserva en giro maxima v2
						((HGUC)(vh.elementAt(j))).restrspinnmax2(cplex,t);						
						//commitment
						((HGUC)(vh.elementAt(j))).restrcommit(cplex,t);				
						
						//modificacion para estudio GIZ
						//restriccion de volumen ficticio
						((HGUC)(vh.elementAt(j))).restrvfict1(cplex,t);
						((HGUC)(vh.elementAt(j))).restrvfict2(cplex,t);
					}
				}
			}
			
			 //********************************************RESTRICCIONES BATERIAS*****************************************///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			 for(k=0;k<id_battery.length;k++){
				for(t=0;t<T;t++){
					//((BatteryUC)(vbatuc.elementAt(k))).restrpmin(cplex,t);
					((BatteryUC)(vbatuc.elementAt(k))).restrpmax(cplex,t);
					((BatteryUC)(vbatuc.elementAt(k))).restrppos(cplex,t);
					((BatteryUC)(vbatuc.elementAt(k))).restrpneg(cplex,t);
					((BatteryUC)(vbatuc.elementAt(k))).restrpinv(cplex,t);
					((BatteryUC)(vbatuc.elementAt(k))).restrlogi(cplex,t);
					((BatteryUC)(vbatuc.elementAt(k))).restrbanc(cplex,t);
					//((BatteryUC)(vbatuc.elementAt(k))).restrtram0(cplex,t);
					((BatteryUC)(vbatuc.elementAt(k))).restrtram2(cplex,t);
					((BatteryUC)(vbatuc.elementAt(k))).restrdpmi(cplex,t);
					((BatteryUC)(vbatuc.elementAt(k))).restrpmin2(cplex,t);
					((BatteryUC)(vbatuc.elementAt(k))).restrbala(cplex,t);
					//((BatteryUC)(vbatuc.elementAt(k))).restrcicl(cplex,t);
				}
				((BatteryUC)(vbatuc.elementAt(k))).restrvfin(cplex,T-1);
			}
			 //********************************************RESTRICCIONES Embalses *****************************************///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			
			 for(k=0;k<id_reserv.length;k++){
				//volumen final  V(T) >= Vfinal
				((ReservUC)(vresuc.elementAt(k))).restrvfin(cplex,T-1);
				for(t=0;t<T;t++){
					((ReservUC)(vresuc.elementAt(k))).restrvfic2(cplex,t); //-> al final se decidio incluir la variable vfic al balance hidraulico de los embalses
				}
			 }
			//********************************************RESTRICCIONES Eolicas*****************************************///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			for(k=0;k<id_eolic.length;k++){
				for(t=0;t<T;t++){
					((EolicUC)(veoluc.elementAt(k))).restrpmax(cplex,t);
				}
			}	
			
			//********************************************RESTRICCIONES Solar*****************************************///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			for(k=0;k<id_solar.length;k++){
				for(t=0;t<T;t++){
					((SolarUC)(vsoluc.elementAt(k))).restrpmax(cplex,t);
				}
			}
			
			//********************************************RESTRICCIONES CSP*****************************************///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			for(k=0;k<id_csp.length;k++){
				for(t=0;t<T;t++){
					((CSPUC)(vcspuc.elementAt(k))).restrbala(cplex,t);
					((CSPUC)(vcspuc.elementAt(k))).restrlogi(cplex,t);
					((CSPUC)(vcspuc.elementAt(k))).restrpot(cplex,t);
					((CSPUC)(vcspuc.elementAt(k))).restrpmax(cplex,t);
					((CSPUC)(vcspuc.elementAt(k))).restrpmin(cplex,t);
					//volumen final
					if(t==T-1){
						((CSPUC)(vcspuc.elementAt(k))).restrvfin(cplex,t);
					}
				}
			}	
			
			//********************************************RESTRICCIONES ACOPLADAS****************************************///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			System.out.println("");
			System.out.println("restricciones acopladas entre objetos: ");
			
			//************************************************CONTROL DE DEMANDA****************************************///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			for(k=0;k<id_load.length;k++){
				for(t=0;t<T;t++){
					((LoadUC)(vloaduc.elementAt(k))).restrdrea(cplex,t);
					((LoadUC)(vloaduc.elementAt(k))).restrcmin(cplex,t);
					((LoadUC)(vloaduc.elementAt(k))).restrcmax(cplex,t);
				}
			}
			
			//*************************************************BALANCE DEMANDA****************************************///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			System.out.println("--balance generacion-demanda por barra");
			//recorro barras	
			for(r=0;r<id_busbar.length;r++){
				//recorro periodos
				for(t=0;t<T;t++){
					p	 =cplex.constant(0);
					pa	 =cplex.constant(0);
					pp	 =cplex.constant(0);
					pa_c =cplex.constant(0);
					pp_c =cplex.constant(0);
					
					//recorro generadores termicos
					for(i=0;i<id_tgenerator.length;i++){
						//chequeo barra
						if(((TGUC)(vt.elementAt(i))).Barra[t]==bus[r][t]){
							p=cplex.sum(cplex.prod(((TGUC)(vt.elementAt(i))).B[t][0],((TGUC)(vt.elementAt(i))).Pming[t]),((TGUC)(vt.elementAt(i))).P[0][t],p); 
									
							//Ciclos combinados
							//Busco la turbina a gas a la cual se encuentra acoplada la unidad
							sum_cc2=cplex.constant(0);
							p_tv=cplex.constant(0);
							cont_cc=0;
						
							for(j=0;j<id_tgenerator.length;j++){
								if(((TGUC)(vt.elementAt(j))).IDAcoplaTV[t]==((TGUC)(vt.elementAt(i))).ID[t]  & ((TGUC)(vt.elementAt(i))).ID[t]!=((TGUC)(vt.elementAt(j))).ID[t]){
									cont_cc++;
									sum_cc2		= cplex.sum(cplex.prod(((TGUC)(vt.elementAt(j))).B[t][0],((TGUC)(vt.elementAt(j))).Pming[t]*((TGUC)(vt.elementAt(j))).FactorAcoplaTVTG[t]),sum_cc2);
									sum_cc2     =cplex.sum(cplex.prod(((TGUC)(vt.elementAt(j))).P[0][t],((TGUC)(vt.elementAt(j))).FactorAcoplaTVTG[t]),sum_cc2);
								}
							}							
							//PTV = PTG x Factor Acoplamiento							
							if(cont_cc>0){
								p_tv=cplex.sum(cplex.prod(((TGUC)(vt.elementAt(i))).B[t][0],((TGUC)(vt.elementAt(i))).Pminuc[0][t]),p_tv); 
								p_tv=cplex.sum(((TGUC)(vt.elementAt(i))).P[0][t],p_tv);
								cplex.addLe(cplex.diff(p_tv,sum_cc2),0,"relacion_potencia_cc"+((TGUC)(vt.elementAt(i))).ID[t]+"_"+(t+1));
							}		

							//Otras dependencias
							//dependencia de centrales: Ejemplo, fuego adicional con turbina a gas
							sum_depen=cplex.constant(0);
							sum_depen2=cplex.constant(0);
							boolean_depen=false;
							
							//calculo potencia, sum_depen = p x fdependence
							for(j=0;j<id_tgenerator.length;j++){
								if(((TGUC)(vt.elementAt(i))).Dependence[t]==((TGUC)(vt.elementAt(j))).ID[t]){
									boolean_depen=true;
									sum_depen= cplex.sum(cplex.prod(((TGUC)(vt.elementAt(j))).B[t][0],((TGUC)(vt.elementAt(j))).Pming[t]*((TGUC)(vt.elementAt(i))).Fdependence[t]),sum_depen);
									sum_depen=cplex.sum(cplex.prod(((TGUC)(vt.elementAt(j))).P[0][t],((TGUC)(vt.elementAt(i))).Fdependence[t]),sum_depen);
								}
							}
							
							if(boolean_depen){
								sum_depen2=cplex.sum(cplex.prod(((TGUC)(vt.elementAt(i))).B[t][0],((TGUC)(vt.elementAt(i))).Pming[t]),p_tv); 			
								sum_depen2=cplex.sum(((TGUC)(vt.elementAt(i))).P[0][t],sum_depen2);
								cplex.addLe(cplex.diff(sum_depen2,sum_depen),0,"relacion_dependencia_"+((TGUC)(vt.elementAt(i))).ID[t]+"_"+(t+1));
							}	
						
							
							//arranque
							if(((TGUC)(vt.elementAt(i))).GradRA[0]>0){
								for(ia=0;ia<((TGUC)(vt.elementAt(i))).NARR[t];ia++){
									pa=cplex.sum(cplex.prod(((TGUC)(vt.elementAt(i))).Bga[t+1+ia],((TGUC)(vt.elementAt(i))).PaFx[t+1+ia][ia]),pa);
								}
							}
							//parada		
							if(((TGUC)(vt.elementAt(i))).GradRP[0]>0){
								for(ip=0;ip<((TGUC)(vt.elementAt(i))).NPAR[t];ip++){
									pp=cplex.sum(cplex.prod(((TGUC)(vt.elementAt(i))).Bgp[t-ip],((TGUC)(vt.elementAt(i))).PpFx[t-ip][ip]),pp);
								}
							}
							//consumos propios		
							p=cplex.sum(p,cplex.prod(((TGUC)(vt.elementAt(i))).B[t][0],-1*(((TGUC)(vt.elementAt(i))).OwnConsuption[t])));	
							
							//consumos propios arranque
							if(((TGUC)(vt.elementAt(i))).GradRA[0]>0){
								for(ia=0;ia<((TGUC)(vt.elementAt(i))).NARR[t];ia++){
									pa_c=cplex.sum(cplex.prod(((TGUC)(vt.elementAt(i))).Bga[t+1+ia],-1*((TGUC)(vt.elementAt(i))).OwnConsuption[t]),pa_c);
								}
							}
							//consumos propios parada
							if(((TGUC)(vt.elementAt(i))).GradRP[0]>0){
								for(ip=0;ip<((TGUC)(vt.elementAt(i))).NPAR[t];ip++){
									pp_c=cplex.sum(cplex.prod(((TGUC)(vt.elementAt(i))).Bgp[t-ip],-1*((TGUC)(vt.elementAt(i))).OwnConsuption[t]),pp_c);
								}
							}
						}
					}
					
					//recorro centrales hidraulicas
					for(i=0;i<id_hgenerator.length;i++){
						if(((HGUC)(vh.elementAt(i))).Barra[t]==bus[r][t]){	
						
							//generacion en operacion normal
							p=cplex.sum(cplex.prod(((HGUC)(vh.elementAt(i))).B[t][0],((HGUC)(vh.elementAt(i))).Pming[t]),p); 
							p=cplex.sum(((HGUC)(vh.elementAt(i))).P[0][t],p);
							
							//arranque
							if(((HGUC)(vh.elementAt(i))).GradRA[0]>0){
								for(ia=0;ia<((HGUC)(vh.elementAt(i))).NARR[t];ia++){
									pa=cplex.sum(cplex.prod(((HGUC)(vh.elementAt(i))).Bga[t+1+ia],((HGUC)(vh.elementAt(i))).PaFx[t+1+ia][ia]),pa);
								}
							}
							//parada		
							if(((HGUC)(vh.elementAt(i))).GradRP[0]>0){
								for(ip=0;ip<((HGUC)(vh.elementAt(i))).NPAR[t];ip++){
									pp=cplex.sum(cplex.prod(((HGUC)(vh.elementAt(i))).Bgp[t-ip],((HGUC)(vh.elementAt(i))).PpFx[t-ip][ip]),pp);
								}
							}
							//consumos propios		
							p=cplex.sum(p,cplex.prod(((HGUC)(vh.elementAt(i))).B[t][0],-1*(((HGUC)(vh.elementAt(i))).OwnConsuption[t])));	
						}
					}
					
				
					//recorro baterias --> Ojo que se suma directamente a p
					pbat	 =cplex.constant(0);
					for(k=0;k<id_battery.length;k++){
						//if(((BatteryUC)(vbatuc.elementAt(i))).Barra[t]==bus[r][t]){	
							p=cplex.sum(p,((BatteryUC)(vbatuc.elementAt(k))).Pi[t]);
						//}
					}
					
					//recorro bombas electricas
					pbomb	 =cplex.constant(0);
					for(i=0;i<id_pump.length;i++){
						if(((PumpUC)(vpumpuc.elementAt(i))).Barra[t]==bus[r][t]){
						pbomb=cplex.sum(pbomb,cplex.prod(-((PumpUC)(vpumpuc.elementAt(i))).PBomba[t],((PumpUC)(vpumpuc.elementAt(i))).Bg[t]));
						}
					}
					
					//recorro centrales eolicas
					peol	 =cplex.constant(0);
					for(i=0;i<id_eolic.length;i++){
						if(((EolicUC)(veoluc.elementAt(i))).Barra[t]==bus[r][t]){	
							peol=cplex.sum(peol,((EolicUC)(veoluc.elementAt(i))).Peol[t]);
						}
					}
					
					//recorro centrales solares --> Ojo que se suma directamente a p
					for(i=0;i<id_solar.length;i++){
						if(((SolarUC)(vsoluc.elementAt(i))).Barra[t]==bus[r][t]){	
							p=cplex.sum(p,((SolarUC)(vsoluc.elementAt(i))).Psol[t]);
						}
					}
					
					//recorro CSP
					pcsp = cplex.constant(0);
					for(i=0;i<id_csp.length;i++){
						//if(((CSPUC)(vcspuc.elementAt(i))).Barra[t]==bus[r][t]){	
							pcsp=cplex.sum(pcsp,((CSPUC)(vcspuc.elementAt(i))).Pel1[t],((CSPUC)(vcspuc.elementAt(i))).Pel2[t]);
						//}
					}
					
					//energia no suministrada ens
					for(k=0;k<id_load.length;k++){
						if(((LoadUC)(vloaduc.elementAt(k))).Bus[t]==bus[r][t]){							
							for(int s=0;s<((LoadUC)(vloaduc.elementAt(k))).n_iv;s++){
								p = cplex.sum(((LoadUC)(vloaduc.elementAt(k))).Ens[s][t],p);
							}
						}
					}
					//energia de perdida
					for(k=0;k<id_load.length;k++){
						if(((LoadUC)(vloaduc.elementAt(k))).Bus[t]==bus[r][t]){	
							//se considera energia de perdida solo si cota inferior es menor que 0
							if(((LoadUC)(vloaduc.elementAt(k))).Eperdmin[t]<0){
								p=cplex.sum(p,((LoadUC)(vloaduc.elementAt(k))).Eperd[t]);
								(((LoadUC)(vloaduc.elementAt(k))).Eperd[t]).setLB(-generacion_renovable[t]);
							}
						}
					}
					//recorro lineas
					//***************************************** Pérdidas *************************************************************************************
					if (bandera_perdida==false){
						for(k=0;k<id_line.length;k++){
							//flujo positivo, sale de barra
							if(((LineUC)(vlineuc.elementAt(k))).BusIni[t]==bus[r][t]){	
								p=cplex.diff(p,((LineUC)(vlineuc.elementAt(k))).F[t]);
							}
							//flujo negativo, entra a barra
							else if (((LineUC)(vlineuc.elementAt(k))).BusFin[t]==bus[r][t]){	
								p=cplex.sum(p,((LineUC)(vlineuc.elementAt(k))).F[t]);
							}
						}
					}
					else{
						for(k=0;k<id_line.length;k++){
							//flujo positivo, sale de barra
							if(((LineUC)(vlineuc.elementAt(k))).BusIni[t]==bus[r][t]){
								for(int s=0;s<(((LineUC)(vlineuc.elementAt(k))).matriz_alpha).length;s++){
									p=cplex.sum(p,cplex.prod(-1*(1+((LineUC)(vlineuc.elementAt(k))).matriz_alpha[s][t]),((LineUC)(vlineuc.elementAt(k))).Fp[s][t]));
								}
								for(int s=0;s<(((LineUC)(vlineuc.elementAt(k))).matriz_alpha).length;s++){
									p=cplex.sum(p,cplex.prod((1-((LineUC)(vlineuc.elementAt(k))).matriz_alpha[s][t]),((LineUC)(vlineuc.elementAt(k))).Fn[s][t]));
								}
							}
							//flujo negativo, entra a barra
							else if (((LineUC)(vlineuc.elementAt(k))).BusFin[t]==bus[r][t]){	
								for(int s=0;s<(((LineUC)(vlineuc.elementAt(k))).matriz_alpha).length;s++){
									p=cplex.sum(p,cplex.prod(-1*(1+((LineUC)(vlineuc.elementAt(k))).matriz_alpha[s][t]),((LineUC)(vlineuc.elementAt(k))).Fn[s][t]));
								}
								for(int s=0;s<(((LineUC)(vlineuc.elementAt(k))).matriz_alpha).length;s++){
									p=cplex.sum(p,cplex.prod((1-((LineUC)(vlineuc.elementAt(k))).matriz_alpha[s][t]),((LineUC)(vlineuc.elementAt(k))).Fp[s][t]));
								}
							}
						}
					}
						
					
					
					//control de demanda
					for(k=0;k<id_load.length;k++){
						if (((LoadUC)(vloaduc.elementAt(k))).Bus[t]==bus[r][t]){	
							//solo si Cdmin o Cdmax son distintos de 1
							if(((LoadUC)(vloaduc.elementAt(k))).Cdmin[t] != 1 || ((LoadUC)(vloaduc.elementAt(k))).Cdmax[t] != 1 ){
								p=cplex.diff(p,((LoadUC)(vloaduc.elementAt(k))).Dr[t]);
								//agrego restriccion
								restric_demanda[r][t]=cplex.eq(cplex.sum(p,pa,pp,pa_c,pp_c,pbomb,pcsp,peol),0,"demanda_"+(t+1));
								cplex.add(restric_demanda[r][t]);
							}
							else{
								//agrego restriccion
								restric_demanda[r][t]=cplex.eq(cplex.sum(p,pa,pp,pa_c,pp_c,pbomb,pcsp,peol),((LoadUC)(vloaduc.elementAt(k))).Load[t],"demanda_"+(t+1));
								cplex.add(restric_demanda[r][t]);
							}
						}
					}
					
				}
			}
			//*****************************************Relacion Flujo-Angulo*************************************************************************************
			System.out.println("--relacion flujo-angulo");
			for(k=0;k<id_line.length;k++){
				//recorro periodos
				for(t=0;t<T;t++){
					flow 		= cplex.constant(0);
					dif_theta   = cplex.constant(0);
					
					//escribo flujo por las lineas F=Fp-Fn
					for(int s=0;s<(((LineUC)(vlineuc.elementAt(k))).matriz_alpha).length;s++){
						flow=cplex.sum(flow,((LineUC)(vlineuc.elementAt(k))).Fp[s][t]);
						flow=cplex.sum(flow,cplex.prod(-1,((LineUC)(vlineuc.elementAt(k))).Fn[s][t]));
					}
					//recorro barras
					for(r=0;r<id_busbar.length;r++){
						//flujo positivo, sale de barra
						if(((LineUC)(vlineuc.elementAt(k))).BusIni[t]==((BusUC)(vbusuc.elementAt(r))).Bus[t]){
							dif_theta=cplex.sum(dif_theta,((BusUC)(vbusuc.elementAt(r))).Theta[t]);
						}
						else if (((LineUC)(vlineuc.elementAt(k))).BusFin[t]==((BusUC)(vbusuc.elementAt(r))).Bus[t]){	
							dif_theta=cplex.sum(dif_theta,cplex.prod(-1,((BusUC)(vbusuc.elementAt(r))).Theta[t]));
						}
					}
				//	cplex.addEq(cplex.sum(dif_theta,cplex.prod(-((LineUC)(vlineuc.elementAt(k))).Sbase*Math.pow(((LineUC)(vlineuc.elementAt(k))).Voltaje[t],-2)*((LineUC)(vlineuc.elementAt(k))).Reactancia[t],flow)),0,"restr_diferencia_angulo_"+(k)+"_"+(t));
					cplex.addEq(cplex.sum(dif_theta,cplex.prod(-((1/Zbase)*((LineUC)(vlineuc.elementAt(k))).Reactancia[t]),flow)),0,"restr_diferencia_angulo_"+(k)+"_"+(t));
				
				}
			}
			
			
			//*****************************************BALANCE HIDRAULICO******************************************************************************************
			//Balance hidraulico embalses  V(t) = V(t-1) - H(t)*P/rend + H(t)*Aflu -H(t)*Ret + H(t)*Aguas arriba
			//volumen en miles de m3, caudales en m3/seg, afluentes en m3/seg
			
			System.out.println("--balance hidraulico embalses");
			for(k=0;k<id_reserv.length;k++){
				for(t=0;t<T;t++){
					balanceh=cplex.constant(0);
					//condicion inicial  V(t) = Vini - P/n + Aflu -Ret + Aguas arriba
					if(t==0){
						//modificacion para localizar volumen ficticio
						//version original
						balanceh=cplex.sum(balanceh,cplex.sum(((ReservUC)(vresuc.elementAt(k))).V[t],cplex.prod(-1,((ReservUC)(vresuc.elementAt(k))).Vfic2[t]),cplex.prod(H[t]*UnitT*fcon,((ReservUC)(vresuc.elementAt(k))).Qver[t])));
						//nueva version
						//balanceh=cplex.sum(balanceh,cplex.sum(((ReservUC)(vresuc.elementAt(k))).V[t],cplex.prod(H[t]*UnitT*fcon,((ReservUC)(vresuc.elementAt(k))).Qver[t])));	
					}
					else{
						//modificacion para localizar volumen ficticio
						//version original solo para Tfin para satisfacer restriccion V(T)>=Vfinal
						balanceh=cplex.sum(balanceh,cplex.sum(((ReservUC)(vresuc.elementAt(k))).V[t], cplex.prod(-1,((ReservUC)(vresuc.elementAt(k))).V[t-1]), cplex.prod(-1,((ReservUC)(vresuc.elementAt(k))).Vfic2[t]),cplex.prod(H[t]*UnitT*fcon,((ReservUC)(vresuc.elementAt(k))).Qver[t])));
						//nueva version
						//balanceh=cplex.sum(balanceh,cplex.sum(((ReservUC)(vresuc.elementAt(k))).V[t], cplex.prod(-1,((ReservUC)(vresuc.elementAt(k))).V[t-1]),cplex.prod(H[t]*UnitT*fcon,((ReservUC)(vresuc.elementAt(k))).Qver[t])));
					}
					//---------------------------------------------------------------------------------------
					//busco bombas que bombean agua hacia embalse
					for(i=0;i<id_pump.length;i++){
						if(((PumpUC)(vpumpuc.elementAt(i))).EmbBomba[t].equals(((ReservUC)(vresuc.elementAt(k))).Nombre[t])){
							balanceh=cplex.sum(balanceh,cplex.prod(-H[t]*UnitT*((PumpUC)(vpumpuc.elementAt(i))).KBomba[t]*((PumpUC)(vpumpuc.elementAt(i))).PBomba[t],((PumpUC)(vpumpuc.elementAt(i))).Bg[t]));
						}
					}
					//----------------------------------------------------------------------------------------
					//busco central asociada a embalse (aguas abajo) 
					for(i=0;i<id_hgenerator.length;i++){
						if(((ReservUC)(vresuc.elementAt(k))).ID_Central[t]==((HGUC)(vh.elementAt(i))).ID[t]){
							balanceh=cplex.sum(balanceh,cplex.prod(H[t]*UnitT*fcon/((HGUC)(vh.elementAt(i))).Rend[t],cplex.sum(cplex.prod(((HGUC)(vh.elementAt(i))).B[t][0],((HGUC)(vh.elementAt(i))).Pminuc[0][t]),((HGUC)(vh.elementAt(i))).P[0][t])));
						}
					}
					//----------------------------------------------------------------------------------------
					//busco caudales turbinados por centrales aguas arriba
					for(i=0;i<id_hgenerator.length;i++){
						if(((ReservUC)(vresuc.elementAt(k))).Id[t]==((HGUC)(vh.elementAt(i))).Ctur[t]){
							balanceh=cplex.sum(balanceh,cplex.prod(-H[t]*UnitT*fcon/((HGUC)(vh.elementAt(i))).Rend[t],cplex.sum(cplex.prod(((HGUC)(vh.elementAt(i))).B[t][0],((HGUC)(vh.elementAt(i))).Pminuc[0][t]),((HGUC)(vh.elementAt(i))).P[0][t])));
						}
					}
					//busco vertimientos por centrales aguas arriba 
					for(i=0;i<id_hgenerator.length;i++){
						if(((ReservUC)(vresuc.elementAt(k))).Id[t]==((HGUC)(vh.elementAt(i))).Cver[t]){
							balanceh=cplex.sum(balanceh,cplex.prod(-H[t]*UnitT*fcon,((HGUC)(vh.elementAt(i))).Qver[t]));
						}
					}
					//busco embalses cuyas aguas vertidas llegan a este embalse --> Chequear cast del int
					for(j=0;j<id_reserv.length;j++){
						if(((ReservUC)(vresuc.elementAt(k))).Id[t]==((ReservUC)(vresuc.elementAt(j))).Cver[t]){
							balanceh=cplex.sum(balanceh,cplex.prod(-H[t]*UnitT*fcon,((ReservUC)(vresuc.elementAt(j))).Qver[t]));
						}					
					}
					//falta considerar caudales de embalses que no tienen asociada central

					//-----------------------------------------------------------------------------------------
					if(t==0){
						cplex.addEq(balanceh,H[t]*UnitT*fcon*((ReservUC)(vresuc.elementAt(k))).Aflu[t]+((ReservUC)(vresuc.elementAt(k))).Vini[t]-H[t]*UnitT*fcon*((ReservUC)(vresuc.elementAt(k))).Ret[t],"balance_hidro_emb_"+((ReservUC)(vresuc.elementAt(k))).Id[t]+"_"+(t+1));
					}
					else{
						cplex.addEq(balanceh,H[t]*UnitT*fcon*((ReservUC)(vresuc.elementAt(k))).Aflu[t]-H[t]*UnitT*3.6*((ReservUC)(vresuc.elementAt(k))).Ret[t],"balance_hidro_emb"+((ReservUC)(vresuc.elementAt(k))).Id[t]+"_"+(t+1));
					}
				}
			}
			
			System.out.println("--balance hidraulico serie o pasadas");
			//Balance hidraulico centrales de pasada o serie
			// Q(t) + Qver(t) + Vfic(t)= Aflu + Aguas arriba
			boolean link_emb=false;
			
			for(i=0;i<id_hgenerator.length;i++){
					
				for(t=0;t<T;t++){
					link_emb=false;
					balanceh=cplex.constant(0);
					
					//chequeo si existe embalse asociado a central de hidraulica
					for(j=0;j<id_reserv.length;j++){
						if(((HGUC)(vh.elementAt(i))).ID[i]==((ReservUC)(vresuc.elementAt(j))).ID_Central[t]){
							link_emb=true;
						}					
					}
					if(link_emb==false){
						
						//modificacion para estudio de GIZ
						//version original
						//balanceh=cplex.sum(balanceh,cplex.prod(H[t]*UnitT*fcon/((HGUC)(vh.elementAt(i))).Rend[t],cplex.sum(cplex.prod(((HGUC)(vh.elementAt(i))).B[t][0],((HGUC)(vh.elementAt(i))).Pminuc[0][t]),((HGUC)(vh.elementAt(i))).P[0][t])), cplex.prod(H[t]*UnitT*fcon,((HGUC)(vh.elementAt(i))).Qver[t]),cplex.prod(-1,((HGUC)(vh.elementAt(i))).Vfic[t]));					
						//nueva version -> se elimina Vfic de balance
						balanceh=cplex.sum(balanceh,cplex.prod(H[t]*UnitT*fcon/((HGUC)(vh.elementAt(i))).Rend[t],cplex.sum(cplex.prod(((HGUC)(vh.elementAt(i))).B[t][0],((HGUC)(vh.elementAt(i))).Pminuc[0][t]),((HGUC)(vh.elementAt(i))).P[0][t])), cplex.prod(H[t]*UnitT*fcon,((HGUC)(vh.elementAt(i))).Qver[t]));					
						
						
						//busco caudales turbinados por centrales aguas arriba
						for(j=0;j<id_hgenerator.length;j++){
							if(((HGUC)(vh.elementAt(i))).ID[i]==((HGUC)(vh.elementAt(j))).Ctur[t]){
								balanceh=cplex.sum(balanceh,cplex.prod(-H[t]*UnitT*fcon/((HGUC)(vh.elementAt(j))).Rend[t],cplex.sum(cplex.prod(((HGUC)(vh.elementAt(j))).B[t][0],((HGUC)(vh.elementAt(j))).Pminuc[0][t]),((HGUC)(vh.elementAt(j))).P[0][t])));
							}
						}
						//busco caudales vertidos por centrales aguas arriba
						for(j=0;j<id_hgenerator.length;j++){
							if(((HGUC)(vh.elementAt(i))).ID[i]==((HGUC)(vh.elementAt(j))).Cver[t]){
								balanceh=cplex.sum(balanceh, cplex.prod(-H[t]*UnitT*fcon,((HGUC)(vh.elementAt(j))).Qver[t]));
							}
						}
						//busco embalses cuyas aguas vertidas llegan a esta central 
						for(j=0;j<id_reserv.length;j++){
							if(((HGUC)(vh.elementAt(i))).ID[i]==((ReservUC)(vresuc.elementAt(j))).Cver[t]){
								balanceh=cplex.sum(balanceh,cplex.prod(-H[t]*UnitT*fcon,((ReservUC)(vresuc.elementAt(j))).Qver[t]));
							}					
						}
						cplex.addEq(balanceh,H[t]*UnitT*fcon*((HGUC)(vh.elementAt(i))).Aflu[t],"balance_hidro_"+((HGUC)(vh.elementAt(i))).ID[t]+"_"+(t+1));	
						
					}
				}
			}
			//*************************************************LIMITE GENERACION ERNC****************************************///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			System.out.println("--cota minima de generacion con ERNC");
			//Busco centrales acopladas
			boolean ernc_log=false;
			
			for(t=0;t<T;t++){
				ernc_log=false;
				if(ERNCLimit[t]>=0){ 
					p	 =cplex.constant(0);
					pa	 =cplex.constant(0);
					pp	 =cplex.constant(0);
					
					//recorro generadores termicos
					for(i=0;i<id_tgenerator.length;i++){
						if(((TGUC)(vt.elementAt(i))).IsERNC[t]==1){
							ernc_log=true;
							p=cplex.sum(cplex.prod(((TGUC)(vt.elementAt(i))).B[t][0],((TGUC)(vt.elementAt(i))).Pming[t]),p); 
							p=cplex.sum(((TGUC)(vt.elementAt(i))).P[0][t],p);
								
							//arranque
							if(((TGUC)(vt.elementAt(i))).GradRA[0]>0){
								for(ia=0;ia<((TGUC)(vt.elementAt(i))).NARR[t];ia++){
									pa=cplex.sum(cplex.prod(((TGUC)(vt.elementAt(i))).Bga[t+1+ia],((TGUC)(vt.elementAt(i))).PaFx[t+1+ia][ia]),pa);
								}
							}
							//parada		
							if(((TGUC)(vt.elementAt(i))).GradRP[0]>0){
								for(ip=0;ip<((TGUC)(vt.elementAt(i))).NPAR[t];ip++){
									pp=cplex.sum(cplex.prod(((TGUC)(vt.elementAt(i))).Bgp[t-ip],((TGUC)(vt.elementAt(i))).PpFx[t-ip][ip]),pp);
								}
							}
						}
					}
				
					//recorro CSP
					pcsp = cplex.constant(0);
					for(i=0;i<id_csp.length;i++){
						if(((CSPUC)(vcspuc.elementAt(i))).IsERNC[t]==1){
							ernc_log=true;
							pcsp=cplex.sum(pcsp,((CSPUC)(vcspuc.elementAt(i))).Pel1[t],((CSPUC)(vcspuc.elementAt(i))).Pel2[t]);
						}
					}
					
					//recorro centrales eolicas
					peol	 =cplex.constant(0);
					for(i=0;i<id_eolic.length;i++){
						if(((EolicUC)(veoluc.elementAt(i))).IsERNC[t]==1){
							ernc_log=true;
							peol=cplex.sum(peol,((EolicUC)(veoluc.elementAt(i))).Peol[t]);
						}
					}
					
					//recorro centrales solares --> Ojo que se suma directamente a p
					for(i=0;i<id_solar.length;i++){
						if(((SolarUC)(vsoluc.elementAt(i))).IsERNC[t]==1){
							ernc_log=true;
							p=cplex.sum(p,((SolarUC)(vsoluc.elementAt(i))).Psol[t]);
						}
					}
					//se formula solo si al menos hay una central ERNC para satisfacer restriccion
					if(ernc_log){
						cplex.add(cplex.le(cplex.sum(p,pa,pp,pcsp,peol),ERNCLimit[t],"ERNCLimit_"+(t+1)));
					}
				}
			}
			//***************************************************RESERVA GIRO 1****************************************
			System.out.println("--reserva en giro 1");
			for(t=0;t<T;t++){
				paux	 =cplex.constant(0);
				//reserva en giro centrales termicas
				for(i=0;i<id_tgenerator.length;i++){
					//generacion en operacion normal
					p	 =cplex.constant(0);
					//si la central esta indisponible no aporta reserva en giro
					if(((TGUC)(vt.elementAt(i))).Unavalaible[t]!=1){
						//si reserva maxima supera (potencia maxima - potencia minima), la reserva en giro que aporta central es (Pmax - P)
						if((((TGUC)(vt.elementAt(i))).SpinningMax[t]-(((TGUC)(vt.elementAt(i))).Pmaxg[t]-((TGUC)(vt.elementAt(i))).Pming[t]))>=0){
							p=cplex.sum(cplex.prod(((TGUC)(vt.elementAt(i))).B[t][0],((TGUC)(vt.elementAt(i))).Pming[t]),p); 
							p=cplex.sum(((TGUC)(vt.elementAt(i))).P[0][t],p);
							paux=cplex.sum(paux,cplex.diff(cplex.prod(((TGUC)(vt.elementAt(i))).B[t][0],((TGUC)(vt.elementAt(i))).Pmaxg[t]),p));	
						}
						//si reserva maxima no supera (potencia maxima - potencia minima), se define variable Rg
						else{
							paux=cplex.sum(paux,((TGUC)(vt.elementAt(i))).Rg[t]);
						}
					}
				}
				//reserva en giro centrales hidraulicas
				for(i=0;i<id_hgenerator.length;i++){
					if(((HGUC)(vh.elementAt(i))).Barra[t]>0){
						//si la central esta indisponible no aporta reserva en giro
						if(((HGUC)(vh.elementAt(i))).Unavalaible[t]!=1){
							paux=cplex.sum(paux,((HGUC)(vh.elementAt(i))).Rg[t]);
						}
					}
				}
				
				//centrales eolicas
				for(i=0;i<id_eolic.length;i++){
					if(((EolicUC)(veoluc.elementAt(i))).SpinningMax[t]>0){
						paux=cplex.sum(paux,((EolicUC)(veoluc.elementAt(i))).Rg[t]);
					}
				}
				//centrales solares
				for(i=0;i<id_solar.length;i++){
					if(((SolarUC)(vsoluc.elementAt(i))).SpinningMax[t]>0){
						paux=cplex.sum(paux,((SolarUC)(vsoluc.elementAt(i))).Rg[t]);
					}
				}
				
				cplex.addGe(paux,Spinning[t],"reserva_giro1_sistema_"+(t+1));
				
				/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////7
				//restriccion cota minima de embalses para que aporten reservas
				for(k=0;k<id_reserv.length;k++){
					((ReservUC)(vresuc.elementAt(k))).restrrese(cplex,t);
				}
				
				for(k=0;k<id_reserv.length;k++){
				//busco central asociada a embalse (aguas abajo) 
					for(i=0;i<id_hgenerator.length;i++){
						if(((ReservUC)(vresuc.elementAt(k))).ID_Central[t]==((HGUC)(vh.elementAt(i))).ID[t]){
							cplex.addLe(cplex.diff(((HGUC)(vh.elementAt(i))).Rg[t],cplex.prod(((HGUC)(vh.elementAt(i))).SpinningMax[t],((ReservUC)(vresuc.elementAt(k))).B2[t])),0,"reserva_centrales_embalses_"+((ReservUC)(vresuc.elementAt(k))).ID_Central[t]+"-"+(t+1));
						}
					}
				}
			}
			
			//cota para centrales series que aportan reserva en giro 
			for(i=0;i<id_hgenerator.length;i++){		
				for(t=0;t<T;t++){
					balanceh=cplex.constant(0);
					if((((HGUC)(vh.elementAt(i))).Type[i]).equals("S")){
						
						balanceh=cplex.sum(balanceh,cplex.sum(cplex.prod(((HGUC)(vh.elementAt(i))).B[t][0],((HGUC)(vh.elementAt(i))).Pminuc[0][t]),((HGUC)(vh.elementAt(i))).P[0][t]));					
						
						//busco caudales turbinados por centrales aguas arriba
						for(j=0;j<id_hgenerator.length;j++){
							if(((HGUC)(vh.elementAt(i))).ID[i]==((HGUC)(vh.elementAt(j))).Ctur[t]){
								balanceh=cplex.sum(balanceh,cplex.prod(-((HGUC)(vh.elementAt(i))).Rend[t]/((HGUC)(vh.elementAt(j))).Rend[t],cplex.sum(cplex.prod(((HGUC)(vh.elementAt(j))).B[t][0],((HGUC)(vh.elementAt(j))).Pminuc[0][t]),((HGUC)(vh.elementAt(j))).P[0][t])));
							}
						}
						//busco caudales vertidos por centrales aguas arriba
						for(j=0;j<id_hgenerator.length;j++){
							if(((HGUC)(vh.elementAt(i))).ID[i]==((HGUC)(vh.elementAt(j))).Cver[t]){
								balanceh=cplex.sum(balanceh, cplex.prod(-((HGUC)(vh.elementAt(i))).Rend[t],((HGUC)(vh.elementAt(j))).Qver[t]));
							}
						}
						//busco embalses cuyas aguas vertidas llegan a esta central 
						for(j=0;j<id_reserv.length;j++){
							if(((HGUC)(vh.elementAt(i))).ID[i]==((ReservUC)(vresuc.elementAt(j))).Cver[t]){
								balanceh=cplex.sum(balanceh,cplex.prod(-((HGUC)(vh.elementAt(i))).Rend[t],((ReservUC)(vresuc.elementAt(j))).Qver[t]));
							}					
						}
						
						//reserva giro 1, cota para centrales serie P+Rg <= Rend* (Aflu-Qvermin+Aguas arriba turbinado+Aguar arriba vertido)
						cplex.addLe(cplex.sum(balanceh,((HGUC)(vh.elementAt(i))).Rg[t]),((HGUC)(vh.elementAt(i))).Rend[t]*(((HGUC)(vh.elementAt(i))).Aflu[t]-((HGUC)(vh.elementAt(i))).Qvmin[t]),"reserva_giro1_serie_"+((HGUC)(vh.elementAt(i))).ID[t]+"_"+(t+1));	
						//reserva giro 2
						cplex.addLe(cplex.sum(balanceh,((HGUC)(vh.elementAt(i))).Rg2[t]),((HGUC)(vh.elementAt(i))).Rend[t]*(((HGUC)(vh.elementAt(i))).Aflu[t]-((HGUC)(vh.elementAt(i))).Qvmin[t]),"reserva_giro2_serie_"+((HGUC)(vh.elementAt(i))).ID[t]+"_"+(t+1));	
						
					}
				}
			}
			
			//***************************************************RESERVA GIRO 2****************************************
			System.out.println("--reserva en giro 2");
			for(t=0;t<T;t++){
				if(Spinning2[t]>0){
					paux	 =cplex.constant(0);
					//centrales termicas
					for(i=0;i<id_tgenerator.length;i++){
						//si la central esta indisponible no aporta reserva en giro 2
						if(((TGUC)(vt.elementAt(i))).Unavalaible[t]!=1){
							paux=cplex.sum(paux,((TGUC)(vt.elementAt(i))).Rg2[t]);
						}
					}
					//centrales hidraulicas
					for(i=0;i<id_hgenerator.length;i++){
						if(((HGUC)(vh.elementAt(i))).Barra[t]>0){					
							//si la central esta indisponible no aporta reserva en giro 2
							if(((HGUC)(vh.elementAt(i))).Unavalaible[t]!=1){
								paux=cplex.sum(paux,((HGUC)(vh.elementAt(i))).Rg2[t]);
							}
						}
					}
					
					//reserva en giro centrales eolicas
					for(i=0;i<id_eolic.length;i++){
						if(((EolicUC)(veoluc.elementAt(i))).SpinningMax[t]>0){
							paux=cplex.sum(paux,((EolicUC)(veoluc.elementAt(i))).Rg2[t]);
						}
					}
					
					//reserva en giro centrales solar pv
					for(i=0;i<id_solar.length;i++){
						if(((SolarUC)(vsoluc.elementAt(i))).SpinningMax2[t]>0){
							paux=cplex.sum(paux,((SolarUC)(vsoluc.elementAt(i))).Rg2[t]);
						}
					}
	
					cplex.addGe(paux,Spinning2[t],"reserva_giro2_sistema_"+(t+1));
				}
			}
			//***************************************************CONTROL PRIMARIO DE FRECUENCIA****************************************///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			System.out.println("--control primario de frecuencia");
			for(t=0;t<T;t++){
				if(CPF[t]>0){			
					paux	 =cplex.constant(0);
					//centrales termicas
					for(i=0;i<id_tgenerator.length;i++){
						paux=cplex.sum(paux,cplex.prod(((TGUC)(vt.elementAt(i))).B[t][0],((TGUC)(vt.elementAt(i))).CPFMax[t]));
					}
					//centrales hidraulicas
					for(i=0;i<id_hgenerator.length;i++){
						if(((HGUC)(vh.elementAt(i))).Barra[t]>0){						
							paux=cplex.sum(paux,cplex.prod(((HGUC)(vh.elementAt(i))).B[t][0],((HGUC)(vh.elementAt(i))).CPFMax[t]));
						}
					}
					//centrales eolicas
					for(i=0;i<id_eolic.length;i++){
						if(((EolicUC)(veoluc.elementAt(i))).CPFmax[t]>0){
							paux=cplex.sum(paux,((EolicUC)(veoluc.elementAt(i))).CPF[t]);
						}
					}
					//centrales solares
					for(i=0;i<id_solar.length;i++){
						if(((SolarUC)(vsoluc.elementAt(i))).CPFmax[t]>0){
							paux=cplex.sum(paux,((SolarUC)(vsoluc.elementAt(i))).CPF[t]);
						}
					}			
					cplex.addGe(paux,CPF[t],"control_primario_frecuencia_sistema_ "+(t+1));
				}
			}
			
			//*************************************************DESPLAZAMIENTO DEMANDA***************************************///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			sum_demanda_desp = cplex.constant(0);		
			cont_desp = 1;
			demanda_desp = 0;
			for(t=0;t<T;t++){
				for(k=0;k<id_load.length;k++){
					if(cont_desp < ((LoadUC)(vloaduc.elementAt(k))).Td[t]){
						if(((LoadUC)(vloaduc.elementAt(k))).Cdmin[t] != 1 || ((LoadUC)(vloaduc.elementAt(k))).Cdmax[t] != 1){
							demanda_desp = demanda_desp + ((LoadUC)(vloaduc.elementAt(k))).Load[t];
							sum_demanda_desp = cplex.sum(sum_demanda_desp,((LoadUC)(vloaduc.elementAt(k))).Dr[t]);
						}
						cont_desp=cont_desp + 1;
					}
					else{
						if(((LoadUC)(vloaduc.elementAt(k))).Cdmin[t] != 1 || ((LoadUC)(vloaduc.elementAt(k))).Cdmax[t] != 1){
							sum_demanda_desp = cplex.sum(sum_demanda_desp,((LoadUC)(vloaduc.elementAt(k))).Dr[t]);
							demanda_desp = demanda_desp + ((LoadUC)(vloaduc.elementAt(k))).Load[t];
						}
						//cplex.addGe(sum_demanda_desp,demanda_desp,"desplazamiento_demanda_"+(t+1));
						cont_desp = 1;
						demanda_desp = 0;
						sum_demanda_desp = cplex.constant(0);
					}
				}
			}
			
			//****************************************************FUNCION OBJETIVO***************************************///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			System.out.println("--funcion objetivo");
			//operacion normal			
			for(t=0;t<T;t++){
				for(i=0;i<id_tgenerator.length;i++){
					costo=cplex.sum(cplex.prod(H[t]*fes*UnitT*((TGUC)(vt.elementAt(i))).Pcomb[t]*((TGUC)(vt.elementAt(i))).Alfauc[0][t]*((TGUC)(vt.elementAt(i))).Pminuc[0][t],((TGUC)(vt.elementAt(i))).B[t][0]),costo);
					costo=cplex.sum(cplex.prod(H[t]*fes*UnitT*((TGUC)(vt.elementAt(i))).Pcomb[t]*((TGUC)(vt.elementAt(i))).Alfauc[0][t],((TGUC)(vt.elementAt(i))).P[0][t]),costo);
				}
			}
			for(i=0;i<id_tgenerator.length;i++){
				//rampa de arrranque
				if(((TGUC)(vt.elementAt(i))).GradRA[0]>0){
					for(t=0;t<T;t++){
						for(ia=0;ia<((TGUC)(vt.elementAt(i))).NARR[t];ia++){
							costo=cplex.sum(cplex.prod(((TGUC)(vt.elementAt(i))).Bga[t+1+ia],H[t]*UnitT*fes*((TGUC)(vt.elementAt(i))).Pcomb[t]*((TGUC)(vt.elementAt(i))).CespMeRA[t]*((TGUC)(vt.elementAt(i))).PaFx[t+1+ia][ia]),costo);
						}
					}
				}	
				//rampa parada
				if(((TGUC)(vt.elementAt(i))).GradRP[0]>0){
					for(t=0;t<T;t++){
						for(ip=0;ip<((TGUC)(vt.elementAt(i))).NPAR[t];ip++){
							costo=cplex.sum(cplex.prod(((TGUC)(vt.elementAt(i))).Bgp[t-ip],H[t]*UnitT*fes*((TGUC)(vt.elementAt(i))).Pcomb[t]*((TGUC)(vt.elementAt(i))).CespMeRP[t]*((TGUC)(vt.elementAt(i))).PpFx[t-ip][ip]),costo);
						}
					}
				}
			}
			//costo de partida
			boolean banderacpart=false;
			for(i=0;i<id_tgenerator.length;i++){
				for(t=0;t<T;t++){
					if(((TGUC)(vt.elementAt(i))).StopCost[t]==0){
						banderacpart=false;
						for(int jj=0;jj<((TGUC)(vt.elementAt(i))).CPartida.length;jj++){
							if(t>=((TGUC)(vt.elementAt(i))).IPartida[jj] & ((TGUC)(vt.elementAt(i))).CPartida[jj]>0){
								banderacpart=true;	
							}
						}
						if(banderacpart){
							costo = cplex.sum(((TGUC)(vt.elementAt(i))).Costo_AP_VNC1[t],costo);
						}
					}
					//costo de partida alternativo
					else{
						costo = cplex.sum(cplex.prod(((TGUC)(vt.elementAt(i))).Bga[t],fes*((TGUC)(vt.elementAt(i))).CPartida[0]),costo);
					}
				}
			}

			//costo de parada
			for(i=0;i<id_tgenerator.length;i++){
				for(t=0;t<T;t++){
					if(((TGUC)(vt.elementAt(i))).StopCost[t]>0){
						costo = cplex.sum(cplex.prod(((TGUC)(vt.elementAt(i))).Bgp[t],fes*((TGUC)(vt.elementAt(i))).StopCost[t]),costo);
					}
				}
			}	
			
			//penalizacion volumenes ficticios
			//embalses
			for(t=0;t<T;t++){
				for(k=0;k<id_reserv.length;k++){
					if(((ReservUC)(vresuc.elementAt(k))).Vermin[t]>0)
					costo = cplex.sum(cplex.prod(1.2*fes*Cpvfic,((ReservUC)(vresuc.elementAt(k))).Vfic[t]),costo);
				}
			}
			//volumen ficticio para volumen final
			for(k=0;k<id_reserv.length;k++){
				for(t=0;t<T;t++){
					//if (((ReservUC)(vresuc.elementAt(k))).Vfin[t]>0 & t==(T-1)){ -> caso cuando solo se penalizaba el volumen ficticio al final de periodo
					//if (((ReservUC)(vresuc.elementAt(k))).Vfin[t]>0){
						costo = cplex.sum(cplex.prod(1.5*fes*Cpvfic,((ReservUC)(vresuc.elementAt(k))).Vfic2[t]),costo);
					//}
				}
			}
			
			//hidraulicas de pasada o serie
			for(t=0;t<T;t++){
				for(i=0;i<id_hgenerator.length;i++){
					//volumen ficticio para potencia minima
					if(((HGUC)(vh.elementAt(i))).Commitment[t]==0 & ((HGUC)(vh.elementAt(i))).Pming[t]>0)
					costo = cplex.sum(cplex.prod(fes*Cpvfic,((HGUC)(vh.elementAt(i))).Vfic[t]),costo);
					
					//volumen ficticio para vertimiento minimo
					if(((HGUC)(vh.elementAt(i))).Qvmin[t]>0)
					costo = cplex.sum(cplex.prod(1.2*fes*Cpvfic,((HGUC)(vh.elementAt(i))).Vfic2[t]),costo);
				}
			}
			
			//Energia no suministrada
			for(t=0;t<T;t++){
				for(k=0;k<id_load.length;k++){
					for(int s=0;s<((LoadUC)(vloaduc.elementAt(k))).n_iv;s++){
						costo = cplex.sum(cplex.prod(((LoadUC)(vloaduc.elementAt(k))).Ens[s][t],H[t]*UnitT*fes*((LoadUC)(vloaduc.elementAt(k))).Alfa[s][t]),costo);
					}
				}
			}
			//Penalizacion perdidas 
			for(t=0;t<T;t++){
				for(k=0;k<id_load.length;k++){
					if(((LoadUC)(vloaduc.elementAt(k))).Eperdmin[t]<0){
						//Costo = cplex.sum(cplex.prod(((LoadUC)(vloaduc.elementAt(k))).Eperd[t],H[t]*UnitT*fes*((LoadUC)(vloaduc.elementAt(k))).Cpperd[t]),Costo);
					}
				}
			}
			
			//Descarga-carga bateria
			 for(k=0;k<id_battery.length;k++){
				for(t=0;t<T;t++){
					//Costo = cplex.sum(cplex.prod(fes*((BatteryUC)(vbatuc.elementAt(k))).Cb[t],((BatteryUC)(vbatuc.elementAt(k))).Ep[t]),cplex.prod(-1*fes*(((BatteryUC)(vbatuc.elementAt(k))).Cb[t]),((BatteryUC)(vbatuc.elementAt(k))).En[t]),Costo);
				}
			}
			
			F=cplex.minimize(costo);
			cplex.add(F);
			
		}
	}		