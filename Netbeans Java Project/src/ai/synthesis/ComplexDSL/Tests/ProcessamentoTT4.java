package ai.synthesis.ComplexDSL.Tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.swing.JFrame;

import Standard.StrategyTactics;
import ai.RandomBiasedAI;

import ai.abstraction.HeavyRush;
import ai.abstraction.RangedRush;
import ai.abstraction.partialobservability.POLightRush;
import ai.abstraction.partialobservability.POWorkerRush;
import ai.asymmetric.PGS.LightPGSSCriptChoice;
import ai.asymmetric.SSS.LightSSSmRTSScriptChoice;
import ai.competition.COAC.CoacAI;

import ai.competition.dropletGNS.Droplet;
import ai.competition.mayariBot.mayari;
import ai.configurablescript.BasicExpandedConfigurableScript;
import ai.configurablescript.ScriptsCreator;
import ai.core.AI;
import ai.evaluation.SimpleSqrtEvaluationFunction3;
import ai.mcts.naivemcts.NaiveMCTS;
import ai.synthesis.ComplexDSL.LS_CFG.FactoryLS;
import ai.synthesis.ComplexDSL.LS_CFG.Node_LS;
import ai.synthesis.ComplexDSL.Synthesis_Base.AIs.Interpreter;
import ai.synthesis.ComplexDSL.Synthesis_Base.CFG.Control;
import gui.PhysicalGameStatePanel;
import rts.GameState;
import rts.PhysicalGameState;
import rts.PlayerAction;
import rts.units.UnitTypeTable;
import util.Pair;

public class ProcessamentoTT4 {
	static int max=7000;
	static FactoryLS f =new FactoryLS();;
	static double vv[] = {0.0,0.0,0.0,0.0,0.0};
	
	

	
	public static AI getAdv(GameState gs,String s,UnitTypeTable utt) throws Exception {
		
		
		if(s.equals("0"))  return new POWorkerRush(utt); //new GuidedRojoA3N(utt);
		if(s.equals("1")) return new POLightRush(utt);
		
		if(s.equals("2")) return new CoacAI(utt);

		
		if(s.equals("3")) return new mayari(utt);
		
		return null;
	}
	
	
	
	
	public static Pair<Node_LS,Node_LS> ler0(String map,String ia,String teste,float limite) throws FileNotFoundException{
		
		
			String entrada = "out_"+map+"_"+ia+"_"+teste+".txt";
			Scanner in = new Scanner(new FileReader("r1/"+entrada));
			String a1="";
			String a2="";
			while (in.hasNextLine()) {
			    String line = in.nextLine();
			    String dados[] = line.split("\t");
			    if(dados[0].equals("Camp")) {
			    	float tempo = Float.parseFloat(dados[1]);
			    	
			    	if(tempo<limite ) {
			    	
				    	a1=dados[dados.length-1];
				    	a2=dados[dados.length-1];
			    	}else {
			    		break;
			    	}
			    }
			}
			
		in.close();
		System.out.println(a1);
		return new Pair<>((Node_LS)Control.load(a1,f),(Node_LS)Control.load(a2,f));
	}
	
public static String getMap(String s) {
		
		
	if(s.equals("4")) return "maps/8x8/basesWorkers8x8A.xml";
	if(s.equals("0")) return "maps/24x24/basesWorkers24x24A.xml";
	if(s.equals("1")) return "maps/32x32/basesWorkers32x32A.xml";
    if(s.equals("2")) { max =15000;;return "maps/BroodWar/(4)BloodBath.scmB.xml";}
		return null;
	}
	
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
                args[0] = "0";
                args[1] = "3";
		String map = args[0];
		String id_IA = args[1];
		int map2 = Integer.parseInt(args[0]) ;
		int id_IA2 = Integer.parseInt(args[1]) ;
		
		
		//String nomes[] = {"SP_logs","FP_logs","DeCS_logs"};
		String nomes[] = {"SP","FP","DeCS", "DO"};
		String nomes2[] = {"enemies_basesWorkers24x24A","enemies_basesWorkers32x32A"};
		UnitTypeTable utt = new UnitTypeTable(2);
		String path_map = getMap(args[0]);
		PhysicalGameState pgs = PhysicalGameState.load(path_map, utt);
		GameState gs2 = new GameState(pgs, utt);
		
		File file = new File("enemies_basesWorkers/"+ nomes2[map2]+"/"+nomes[id_IA2]+"_logs");
		File afile[] = file.listFiles();
		String partidas="250000";
		System.out.println(map+id_IA);
		int cont=0;
		for (int j =0; j <  afile.length; j++) {
			File arquivos = afile[j];
			if(arquivos.getName().indexOf(partidas)>=0){
				
				cont++;
				FileReader fileReader = new FileReader(arquivos);
	            BufferedReader bufferedReader = new BufferedReader(fileReader);
	            String linha = "";
	            while ((linha = bufferedReader.readLine()) != null) {
	            	System.out.println(linha);
                        System.out.println(arquivos);
	            	Pair<Node_LS,Node_LS> ai=new Pair<>((Node_LS)Control.load(linha,f),(Node_LS)Control.load(linha,f));
	            	double r = Avalia(gs2,utt,ai,4);
					System.out.println("Camp\t"+id_IA+"\t"+r);
					System.out.println();
	            	
	            }
			}
		}
		
		System.out.println("n = "+cont);
		for(int i=0;i<4;i++) {
			System.out.println(i+"\t"+vv[i]/cont);
		}
		
		
		

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
	    		try {
	    			pa1 = ai1.getAction(player, gs2);
	    		}catch(Exception e) {
	    			pa1 = new PlayerAction();
	    		}
	    	
	    		PlayerAction pa2=null;
	    		try {
	    			pa2 = ai2.getAction(1-player, gs2);
	    		}catch(Exception e) {
	    			pa2 = new PlayerAction();
	    		}
	            
	    	 gs2.issueSafe(pa1);
	    	 gs2.issueSafe(pa2);
	        
	            if(exibe) {
	            	w.repaint();
	            	Thread.sleep(1);
	            }
	            
	            gameover = gs2.cycle();
	           
	            
	
	    } while (!gameover && (gs2.getTime() < max_cycle));
		
	    if (gs2.winner()==player)return 1.0;
	    if (gs2.winner()==-1)return 0.5;
	    return 0;
	
}
	
	private static double Avalia(GameState gs2, UnitTypeTable utt,Pair<Node_LS, Node_LS> j,int n) throws Exception {
		// TODO Auto-generated method stub 275 525
		double r=0;
		AI j0 = new Interpreter(utt,j.m_a.Clone(f) );
		AI j1 = new Interpreter(utt,j.m_b.Clone(f) );
		for(int i =0;i<n;i++) {
			
		
			double rL=0;
			for(int rep =0;rep<5;rep++) {
				AI adv0 = getAdv(gs2,""+i,utt);
				rL+=partida(gs2,utt,0,max,j0,adv0,false);
			}
			for(int rep =0;rep<5;rep++) {
				AI adv0 = getAdv(gs2,""+i,utt);
				rL+=partida(gs2,utt,1,max,j1,adv0,false);
			}
			System.out.println("VS\t"+i+"\t"+rL);
			vv[i]+=rL/10;
			r+=rL;
		}
		return r;
	}
	
	public static List<AI> decodeScripts(UnitTypeTable utt, String sScripts) {

		//decompõe a tupla
		ArrayList<Integer> iScriptsAi1 = new ArrayList<>();
		String[] itens = sScripts.split(";");

		for (String element : itens) {
			iScriptsAi1.add(Integer.decode(element));
		}

		List<AI> scriptsAI = new ArrayList<>();

		ScriptsCreator sc = new ScriptsCreator(utt, 300);
		ArrayList<BasicExpandedConfigurableScript> scriptsCompleteSet = sc.getScriptsMixReducedSet();

		iScriptsAi1.forEach((idSc) -> {
			scriptsAI.add(scriptsCompleteSet.get(idSc));
		});

		return scriptsAI;
	}

}
