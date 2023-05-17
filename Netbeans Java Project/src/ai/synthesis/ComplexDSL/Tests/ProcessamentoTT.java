package ai.synthesis.ComplexDSL.Tests;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.swing.JFrame;

import ai.synthesis.ComplexDSL.Synthesis_Base.AIs.Interpreter;
import ai.synthesis.ComplexDSL.Synthesis_Base.CFG.Control;
import ai.core.AI;
import ai.synthesis.ComplexDSL.LS_CFG.FactoryLS;
import ai.synthesis.ComplexDSL.LS_CFG.Node_LS;
import gui.PhysicalGameStatePanel;
import rts.GameState;
import rts.PhysicalGameState;
import rts.PlayerAction;
import rts.units.UnitTypeTable;
import util.Pair;

public class ProcessamentoTT {
	static int max=7000;
	static FactoryLS f =new FactoryLS();;
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
			    	
			    	if(tempo<limite) {
				    	a1=dados[3];
				    	a2=dados[3];
			    	}else {
			    		break;
			    	}
			    }
			}
			
		in.close();
		
		return new Pair<>((Node_LS)Control.load(a1,f),(Node_LS)Control.load(a2,f));
	}
	
	public static Pair<Node_LS,Node_LS> ler1(String map,String ia,String teste,float limite) throws FileNotFoundException{
		
		
		String entrada = "out_"+map+"_"+ia+"_"+teste+".txt";
		Scanner in = new Scanner(new FileReader("r11/"+entrada));
		String a1="";
		String a2="";
		while (in.hasNextLine()) {
		    String line = in.nextLine();
		    String dados[] = line.split("\t");
		    if(dados[0].equals("Camp")) {
		    	float tempo = Float.parseFloat(dados[1]);
		    	
		    	if(tempo<limite) {
			    	a1=dados[2];
			    	a2=dados[2];
		    	}else {
		    		break;
		    	}
		    }
		}
		
	in.close();
	
	return new Pair<>((Node_LS)Control.load(a1,f),(Node_LS)Control.load(a2,f));
}
	
public static String getMap(String s) {
	
		
	if(s.equals("10")) return "maps/8x8/basesWorkers8x8A.xml";
	if(s.equals("11")) return "maps/NoWhereToRun9x8.xml";
	if(s.equals("12")) return "maps/16x16/basesWorkers16x16A.xml";
	if(s.equals("13")) return "maps/24x24/basesWorkers24x24A.xml";
	if(s.equals("14")) return "maps/DoubleGame24x24.xml";
	if(s.equals("15")) return "maps/32x32/basesWorkers32x32A.xml";
    if(s.equals("16")) { max =15000;;return "maps/BroodWar/(4)BloodBath.scmB.xml";}
	
		
		
		
		if(s.equals("0")) return "./maps/16x16/TwoBasesBarracks16x16.xml";
		if(s.equals("1")) return "maps/24x24/basesWorkers24x24A.xml";
		if(s.equals("2")) return  "maps/32x32/basesWorkers32x32A.xml";
	    if(s.equals("3")) { max =15000;;return "maps/BroodWar/(4)BloodBath.scmB.xml";}
	   
	    
		return null;
	}
	
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		String map = args[0];
		String teste = args[1];
		String diass = args[2];
		
		UnitTypeTable utt = new UnitTypeTable();
		String path_map = getMap(args[0]);
		PhysicalGameState pgs = PhysicalGameState.load(path_map, utt);
		GameState gs2 = new GameState(pgs, utt);
		int n=4;
		String mapeia[] = {"0","1","2","3"};
		int horas = 3600*24*Integer.parseInt(diass);
		System.out.println("N "+n+" "+path_map);
		for(int i =1;i<=12;i++) {
			float limite=  i*2*3600+horas;
			
			List<Pair<Node_LS,Node_LS>> list = new ArrayList<>();
			for(int ia=0;ia<n;ia++) {
				
				list.add(ler0(map,mapeia[ia],teste,limite));
			}
			
			System.out.println("Leitura completa2");
			for(int ia=0;ia<n-1;ia++) {
				double r = Avalia(gs2,utt,list.get(ia),list,limite,ia);
				
			}
			
			
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
	
	private static double Avalia(GameState gs2, UnitTypeTable utt,Pair<Node_LS, Node_LS> j, List<Pair<Node_LS, Node_LS>> adv, float limite, int ini) throws Exception {
		// TODO Auto-generated method stub
		double r=0;
		AI j0 = new Interpreter(utt,j.m_a.Clone(f) );
		AI j1 = new Interpreter(utt,j.m_b.Clone(f) );
		for(int i =ini+1;i<adv.size();i++) {
			AI adv0 = new Interpreter(utt,adv.get(i).m_a.Clone(f) );
			AI adv1 = new Interpreter(utt,adv.get(i).m_b.Clone(f) );
			double rL=0;
			for(int rep =0;rep<1;rep++)rL+=partida(gs2,utt,0,max,j0,adv1,false);
			for(int rep =0;rep<1;rep++)rL+=partida(gs2,utt,1,max,j1,adv0,false);
			System.out.println("VS\t"+limite+"\t"+ini+"\t"+i+"\t"+rL);
			r+=rL;
		}
		return r;
	}

}
