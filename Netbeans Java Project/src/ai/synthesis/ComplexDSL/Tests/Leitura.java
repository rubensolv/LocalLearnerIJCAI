package ai.synthesis.ComplexDSL.Tests;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.swing.JFrame;

import ai.synthesis.ComplexDSL.Synthesis_Base.AIs.Interpreter;
import ai.synthesis.ComplexDSL.Synthesis_Base.CFG.Control;
import ai.synthesis.ComplexDSL.Synthesis_Base.CFG.Node;
import ai.RandomBiasedAI;
import ai.abstraction.partialobservability.POLightRush;
import ai.abstraction.partialobservability.POWorkerRush;
import ai.competition.COAC.CoacAI;
import ai.competition.GRojoA3N.GuidedRojoA3N;
import ai.competition.rojobot.Rojo;
import ai.core.AI;
import ai.evaluation.SimpleSqrtEvaluationFunction3;
import ai.mcts.naivemcts.NaiveMCTS;
import ai.synthesis.ComplexDSL.LS_CFG.FactoryLS;
import gui.PhysicalGameStatePanel;
import rts.GameState;
import rts.PhysicalGameState;
import rts.PlayerAction;
import rts.units.UnitTypeTable;

public class Leitura {

	static int max=6000;
public static AI getAdv(GameState gs,String s,UnitTypeTable utt) throws Exception {
		
		if(s.equals("0")) return new RandomBiasedAI();
		if(s.equals("1")) return new NaiveMCTS(100, -1, 100,10,0.3f, 1.0f, 0.0f, 1.0f, 0.4f, 1.0f, new RandomBiasedAI(), new SimpleSqrtEvaluationFunction3(),false);				
		if(s.equals("2")) return new POWorkerRush(utt);
		if(s.equals("3"))  return new GuidedRojoA3N(utt);
		if(s.equals("4")) return new POLightRush(utt);
		if(s.equals("5")) return new Rojo(utt);
//		if(s.equals("6")) return new UMSBot(utt);
//		if(s.equals("10")) {
//			MentalSeal m = new MentalSeal(utt);
//			m.preGameAnalysis(gs, 100);
//			return m;
//		}
		if(s.equals("7")) return new CoacAI(utt);
		
//		if(s.equals("8")) return new mayari(utt);
		return null;
	}
	

	public static double partida(GameState gs,UnitTypeTable utt, int player, int max_cycle, AI ai1, AI ai2, boolean exibe) throws Exception {
		
		
		
		ai1.reset(utt);
		ai2.reset(utt);
		GameState gs2 = gs.cloneChangingUTT(utt);
		boolean gameover = false;
		JFrame w=null;
		if(exibe) w = PhysicalGameStatePanel.newVisualizer(gs2,640,640,false,PhysicalGameStatePanel.COLORSCHEME_BLACK);
		boolean itbroke=false ;
		
	    do {
	    	PlayerAction pa1=null;
	    	
	            pa1 = ai1.getAction(player, gs2);
	           
	    	
	    	 
	    	
	            PlayerAction pa2 = ai2.getAction(1-player, gs2);
	       
	    	 gs2.issueSafe(pa1);
	    	 gs2.issueSafe(pa2);
	        
	            if(exibe) {
	            	w.repaint();
	            	Thread.sleep(5);
	            }
	            
	            gameover = gs2.cycle();
	           
	            
	
	    } while (!gameover && (gs2.getTime() < max_cycle));
		
	    if (gs2.winner()==player)return 1.0;
	    if (gs2.winner()==-1)return 0.5;
	    return 0;
	
	}


	public static String getMap2(String s) {
		
		if(s.equals("0")) return "maps/8x8/basesWorkers8x8A.xml";
		if(s.equals("1")) return "maps/16x16/basesWorkers16x16A.xml";				
		if(s.equals("2")) return "maps/BWDistantResources32x32.xml";
		if(s.equals("3")) {max=15000; return "maps/BroodWar/(4)BloodBath.scmB.xml";}
		if(s.equals("4")) return "maps/8x8/FourBasesWorkers8x8.xml";
		if(s.equals("5")) return "maps/16x16/TwoBasesBarracks16x16.xml";
		if(s.equals("6")) return "maps/NoWhereToRun9x8.xml";
		if(s.equals("7")) return "maps/DoubleGame24x24.xml";
		if(s.equals("8")) return "maps/24x24/basesWorkers24x24A.xml";
		if(s.equals("9")) return "maps/32x32/basesWorkers32x32A.xml";
		return null;
	}
	
public static String getMap(String s) {
		
		
		if(s.equals("0")) return "maps/16x16/TwoBasesBarracks16x16.xml";
		if(s.equals("1")) return "maps/24x24/basesWorkers24x24A.xml";
		if(s.equals("2")) return "maps/32x32/basesWorkers32x32A.xml";
		if(s.equals("3")) {max=15000; return "maps/BroodWar/(4)BloodBath.scmB.xml";}
		return null;
	}
	
	public static List<String> ler1(String map,String modo) throws FileNotFoundException{
		List<String> list = new ArrayList<>();
		for(int i =0;i<10;i++) {
			String entrada = "out_"+map+"_"+modo+"_"+i+".txt";
			Scanner in = new Scanner(new FileReader("Ava/"+entrada));
			String a1="";
			String a2="";
			while (in.hasNextLine()) {
			    String line = in.nextLine();
			    String dados[] = line.split("\t");
			    if(dados[0].equals("atualizou")) {
			    	a1=dados[3];
			    	a2=dados[4];
			    }
			}
			list.add(a1);
			list.add(a2);
		}
		return list;
	}
	

	public static List<String> ler0(String map,String modo) throws FileNotFoundException{
		List<String> list = new ArrayList<>();
		for(int i =0;i<10;i++) {
			String entrada = "out_"+map+"_"+modo+"_"+i+".txt";
			Scanner in = new Scanner(new FileReader("Ava/"+entrada));
			String a1="";
			String a2="";
			while (in.hasNextLine()) {
			    String line = in.nextLine();
			    String dados[] = line.split("\t");
			    if(dados[0].equals("Camp")) {
			    	a1=dados[2];
			    	a2=dados[3];
			    }
			}
			list.add(a1);
			list.add(a2);
		}
		return list;
	}
	
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
		UnitTypeTable utt = new UnitTypeTable();
		String path_map = getMap(args[0]);
		PhysicalGameState pgs = PhysicalGameState.load(path_map, utt);
		GameState gs2 = new GameState(pgs, utt);
		
		List<String> camps = null;
		if(args[1].equals("1"))camps=ler1(args[0],args[1]);
		else camps=ler0(args[0],args[1]);
		
		int play= Integer.parseInt(args[2]);
		int lado= Integer.parseInt(args[3]);
		
		testa(gs2,utt,camps.get(play),lado,camps);
		
	
		
	}

	public static void testa(GameState gs2, UnitTypeTable utt,String ai0, int i, List<String> camps) throws Exception {
		Node n =Control.load(ai0,new FactoryLS());
		AI j0 = new Interpreter(utt, n);
		double pont=0;
		for(int j=2;j<camps.size();j++) {
			String ai1=camps.get(j);
			AI j1 = new Interpreter(utt, Control.load(ai1,new FactoryLS()));
			long tini0 = System.currentTimeMillis();
			pont+=partida(gs2,utt,i,max,j0,j1,true);
			long paraou0 = System.currentTimeMillis()-tini0;
			System.out.println("parcial "+pont);
		}
		
		
	}
	
	
	private static String melhor(GameState gs2, UnitTypeTable utt, int i, List<String> camps) throws Exception {
		// TODO Auto-generated method stub
		double melhor =-1;
		String camp="";
		long tini = System.currentTimeMillis();
		for(int k=0;k<camps.size();k++) {
			String ai0=camps.get(k);
			double pont=0;
			AI j0 = new Interpreter(utt, Control.load(ai0,new FactoryLS()));
			for(int j=0;j<camps.size();j++) {
				String ai1=camps.get(j);
				AI j1 = new Interpreter(utt, Control.load(ai1,new FactoryLS()));
				long tini0 = System.currentTimeMillis();
				pont+=partida(gs2,utt,i,max,j0,j1,true);
				long paraou0 = System.currentTimeMillis()-tini0;
				System.out.println(k+" "+j+" "+(paraou0*1.0)/1000.0);
			}
			long paraou = System.currentTimeMillis()-tini;
			System.out.println("Camp\t"+((paraou*1.0)/1000.0));
			if(melhor<pont) {
				melhor=pont;
				camp=ai0;
				System.out.print("Atualuzou "+camp);
			}
		}
		return camp;
	}

}
