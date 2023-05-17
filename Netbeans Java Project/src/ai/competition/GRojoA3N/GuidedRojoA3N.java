/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.competition.GRojoA3N;

import ai.CMAB.GuidedA3NW;
import ai.RandomBiasedAI;
import ai.abstraction.AbstractionLayerAI;
import ai.abstraction.pathfinding.AStarPathFinding;
import ai.abstraction.pathfinding.PathFinding;
import ai.asymmetric.PGS.LightPGSSCriptChoice;
import ai.asymmetric.SSS.LightSSSmRTSScriptChoice;
import ai.asymmetric.SSS.SSSmRTSScriptChoiceRandom;
import ai.competition.capivara.CmabAssymetricMCTS;
import ai.configurablescript.BasicExpandedConfigurableScript;
import ai.configurablescript.ScriptsCreator;
import ai.core.AI;
import ai.core.ParameterSpecification;
import ai.evaluation.EvaluationFunction;
import ai.evaluation.SimpleSqrtEvaluationFunction3;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import rts.GameState;
import rts.PhysicalGameState;
import rts.PlayerAction;
import rts.units.UnitTypeTable;

/**
 *
 * @author Julian, Rubens and Levi
 */
public class GuidedRojoA3N extends AbstractionLayerAI {
    
    GameState gs_to_start_from = null;
    int playerForThisComputation;
    long start_time = 0;
    boolean started = false;
    AI baseAI = null;
    UnitTypeTable utt;    

    public GuidedRojoA3N(UnitTypeTable utt){
        super(new AStarPathFinding(),100, 200);
        this.utt = utt;    
        this.baseAI = new GuidedA3NW(utt);
    }
    
    public GuidedRojoA3N(int time, int max_playouts) {
        super(new AStarPathFinding(),time, max_playouts);
        started = false;
    }

    @Override
    public void reset() {
        this.started = false;
    }

    @Override
    public PlayerAction getAction(int player, GameState gs) throws Exception {   
        return baseAI.getAction(player, gs);
    }

    @Override
    public AI clone() {
        return new GuidedRojoA3N(utt);
    }
    
    @Override
    public AI clone_for_Thread() {
        return this.clone();
    }

    @Override
    public List<ParameterSpecification> getParameters() {
        List<ParameterSpecification> parameters = new ArrayList<>();

        parameters.add(new ParameterSpecification("TimeBudget", int.class, this.TIME_BUDGET));
        parameters.add(new ParameterSpecification("IterationsBudget", int.class, -1));
        parameters.add(new ParameterSpecification("PlayoutLookahead", int.class, this.ITERATIONS_BUDGET));        
        parameters.add(new ParameterSpecification("EvaluationFunction", EvaluationFunction.class, new SimpleSqrtEvaluationFunction3()));
        parameters.add(new ParameterSpecification("PathFinding", PathFinding.class, new AStarPathFinding()));

        return parameters;
    }

    
}
