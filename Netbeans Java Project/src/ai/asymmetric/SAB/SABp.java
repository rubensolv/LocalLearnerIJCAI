package ai.asymmetric.SAB;

import ai.RandomAI;
import ai.RandomBiasedAI;
import ai.abstraction.pathfinding.AStarPathFinding;
import ai.abstraction.pathfinding.PathFinding;
import ai.asymmetric.GAB.SandBox.GABpAlphaBetaSearchAbstract;
import ai.asymmetric.ManagerUnits.IManagerAbstraction;
import ai.asymmetric.ManagerUnits.ManagerClosest;
import ai.asymmetric.ManagerUnits.ManagerClosestEnemy;
import ai.asymmetric.ManagerUnits.ManagerFartherEnemy;
import ai.asymmetric.ManagerUnits.ManagerFather;
import ai.asymmetric.ManagerUnits.ManagerLessDPS;
import ai.asymmetric.ManagerUnits.ManagerLessLife;
import ai.asymmetric.ManagerUnits.ManagerMoreDPS;
import ai.asymmetric.ManagerUnits.ManagerMoreLife;
import ai.asymmetric.ManagerUnits.ManagerRandom;
import ai.asymmetric.common.UnitScriptData;
import ai.core.AI;
import ai.core.AIWithComputationBudget;
import ai.core.InterruptibleAI;
import ai.core.ParameterSpecification;
import ai.evaluation.EvaluationFunction;
import ai.evaluation.SimpleSqrtEvaluationFunction3;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import rts.GameState;
import rts.PlayerAction;
import rts.UnitAction;
import rts.units.Unit;
import rts.units.UnitTypeTable;
import util.Pair;

public class SABp
extends AIWithComputationBudget
implements InterruptibleAI {
    EvaluationFunction evaluation = null;
    UnitTypeTable utt;
    PathFinding pf;
    LightSSSLimitScriptChoose _sss = null;
    GABpAlphaBetaSearchAbstract _ab = null;
    GameState gs_to_start_from = null;
    private int playerForThisComputation;
    int _time;
    int _max_playouts;
    HashSet<Unit> _unitsAbsAB;
    int _numUnits;
    int _numManager;
    IManagerAbstraction manager = null;
    boolean firstTime = true;
    UnitScriptData currentScriptData;
    RandomAI rAI;
    AI randAI;
    String name = "";

    public SABp(UnitTypeTable utt) {
        this(100, 200, new SimpleSqrtEvaluationFunction3(), utt, new AStarPathFinding());
    }

    public SABp(UnitTypeTable utt, int numUnits, int numManager) {
        this(100, 200, new SimpleSqrtEvaluationFunction3(), utt, new AStarPathFinding(), numUnits, numManager);
    }

    public SABp(UnitTypeTable utt, int numUnits, int numManager, List<AI> IAsPort, String name) {
        this(100, 200, new SimpleSqrtEvaluationFunction3(), utt, new AStarPathFinding(), numUnits, numManager);
        this.name = name;
        this._sss.setNewPortfolio(IAsPort);
    }

    public SABp(UnitTypeTable utt, int max_playouts, int numUnits, int numManager, List<AI> IAsPort, String name) {
        this(100, max_playouts, new SimpleSqrtEvaluationFunction3(), utt, new AStarPathFinding(), numUnits, numManager);
        this.name = name;
        this._sss.setNewPortfolio(IAsPort);
    }

    public SABp(UnitTypeTable utt, int numUnits, int numManager, List<AI> IAsPort, List<AI> IAsABCD, String name) {
        this(100, 200, new SimpleSqrtEvaluationFunction3(), utt, new AStarPathFinding(), numUnits, numManager);
        this.name = name;
        this._sss.setNewPortfolio(IAsPort);
        this._ab.setOrderedMoveScript(new ArrayList<AI>(IAsABCD));
    }

    public SABp(int time, int max_playouts, EvaluationFunction e, UnitTypeTable a_utt, PathFinding a_pf) {
        super(time, max_playouts);
        this.evaluation = e;
        this.utt = a_utt;
        this.pf = a_pf;
        this._sss = new LightSSSLimitScriptChoose(this.utt);
        this._ab = new GABpAlphaBetaSearchAbstract(this.utt);
        this._time = time;
        this._max_playouts = max_playouts;
        this._unitsAbsAB = new HashSet();
        this._numUnits = 2;
        this._numManager = 2;
        this.rAI = new RandomAI(this.utt);
        this.randAI = new RandomBiasedAI(this.utt);
    }

    public SABp(int time, int max_playouts, EvaluationFunction e, UnitTypeTable a_utt, PathFinding a_pf, int numUnits, int numManager) {
        super(time, max_playouts);
        this.evaluation = e;
        this.utt = a_utt;
        this.pf = a_pf;
        this._sss = new LightSSSLimitScriptChoose(this.utt);
        this._ab = new GABpAlphaBetaSearchAbstract(this.utt);
        this._time = time;
        this._max_playouts = max_playouts;
        this._unitsAbsAB = new HashSet();
        this._numUnits = numUnits;
        this._numManager = numManager;
        this.rAI = new RandomAI(this.utt);
        this.randAI = new RandomBiasedAI(this.utt);
    }

    @Override
    public void reset() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public void reset(UnitTypeTable utt) {
        super.reset(utt); 
        this.utt = utt;
        rAI.reset(utt);
        randAI.reset(utt);                
    }

    @Override
    public PlayerAction getAction(int player, GameState gs) throws Exception {
        if (gs.canExecuteAnyAction(player)) {
            this.startManager(player, this._numUnits);
            this.startNewComputation(player, gs);
            return this.getBestActionSoFar();
        }
        if (gs.getNextChangeTime() - 1 == gs.getTime()) {
            this.startNewComputation(player, gs);
            this._sss.setTimeBudget(100);
            this.currentScriptData = this._sss.continueImproveUnitScript(player, gs, this.currentScriptData);
        }
        return new PlayerAction();
    }

    protected void startManager(int playerID, int numUnits) {
        if (this.manager == null) {
            switch (this._numManager) {
                case 0: {
                    this.manager = new ManagerRandom(playerID, this._numUnits);
                    break;
                }
                case 1: {
                    this.manager = new ManagerClosest(playerID, numUnits);
                    break;
                }
                case 2: {
                    this.manager = new ManagerClosestEnemy(playerID, numUnits);
                    break;
                }
                case 3: {
                    this.manager = new ManagerFather(playerID, numUnits);
                    break;
                }
                case 4: {
                    this.manager = new ManagerFartherEnemy(playerID, numUnits);
                    break;
                }
                case 5: {
                    this.manager = new ManagerLessLife(playerID, numUnits);
                    break;
                }
                case 6: {
                    this.manager = new ManagerMoreLife(playerID, numUnits);
                    break;
                }
                case 7: {
                    this.manager = new ManagerLessDPS(playerID, numUnits);
                }
                case 8: {
                    this.manager = new ManagerMoreDPS(playerID, numUnits);
                    break;
                }
                default: {
                    System.out.println("ai.asymmetric.SAB.SAB.startManager() Erro na escolha!");
                }
            }
        }
    }

    @Override
    public AI clone() {
        return new SABp(this._time, this._max_playouts, this.evaluation, this.utt, this.pf);
    }

    @Override
    public List<ParameterSpecification> getParameters() {
        ArrayList<ParameterSpecification> parameters = new ArrayList<ParameterSpecification>();
        parameters.add(new ParameterSpecification("TimeBudget", Integer.TYPE, 100));
        parameters.add(new ParameterSpecification("IterationsBudget", Integer.TYPE, -1));
        parameters.add(new ParameterSpecification("PlayoutLookahead", Integer.TYPE, 200));
        parameters.add(new ParameterSpecification("EvaluationFunction", EvaluationFunction.class, new SimpleSqrtEvaluationFunction3()));
        parameters.add(new ParameterSpecification("PathFinding", PathFinding.class, new AStarPathFinding()));
        return parameters;
    }

    @Override
    public void startNewComputation(int player, GameState gs) throws Exception {
        this.playerForThisComputation = player;
        this.gs_to_start_from = gs;
    }

    @Override
    public void computeDuringOneGameFrame() throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PlayerAction getBestActionSoFar() throws Exception {
        long start = System.currentTimeMillis();
        if (this.firstTime) {
            this.currentScriptData = this._sss.getUnitScript(this.playerForThisComputation, this.gs_to_start_from);
            this.firstTime = false;
        } else if (this.hasNewUnitToImprove()) {
            this.currentScriptData = this._sss.getUnitScript(this.playerForThisComputation, this.gs_to_start_from);
        }
        PlayerAction paSSS = this._sss.getFinalAction(this.currentScriptData);
        if (this._numUnits == 0) {
            return paSSS;
        }
        if (System.currentTimeMillis() - start < 80L) {
            this.manager.controlUnitsForAB(this.gs_to_start_from, this._unitsAbsAB);
            this._ab.setPlayoutAI(this._sss.getDefaultScript().clone());
            this._ab.setPlayoutAIEnemy(this._sss.getEnemyScript().clone());
            this._ab.setPlayerModel(1 - this.playerForThisComputation, this._sss.getEnemyScript().clone());
            int timeUsed = (int)(System.currentTimeMillis() - start);
            if (timeUsed < 80) {
                this._ab.setTimeBudget(100 - timeUsed);
            } else {
                this._ab.setTimeBudget(10);
            }
            PlayerAction paAB = this._ab.getActionForAssymetric(this.playerForThisComputation, this.gs_to_start_from, this.currentScriptData, this._unitsAbsAB);
            return paAB;
        }
        return paSSS;
    }

    public double runRandomEval(int player, GameState gs, PlayerAction playerAct, AI aiEnemy) throws Exception {
        double sum = 0.0;
        for (int i = 0; i < 2; ++i) {
            sum += this.RandomEval(player, gs, playerAct, aiEnemy);
        }
        double scoreTemp = sum / 2.0;
        return scoreTemp;
    }

    public double RandomEval(int player, GameState gs, PlayerAction playerAct, AI aiEnemy) throws Exception {
        AI ai2 = aiEnemy.clone();
        ai2.reset();
        GameState gs2 = gs.clone();
        gs2.issue(this.changePlayer(player, playerAct, gs2));
        gs2.issue(ai2.getAction(1 - player, gs2));
        int timeLimit = gs2.getTime() + 200;
        boolean gameover = false;
        while (!gameover && gs2.getTime() < timeLimit) {
            if (gs2.isComplete()) {
                gameover = gs2.cycle();
                continue;
            }
            gs2.issue(this.randAI.getAction(player, gs2));
            gs2.issue(this.randAI.getAction(1 - player, gs2));
        }
        return this.evaluation.evaluate(player, 1 - player, gs2);
    }

    protected double playoutAnalise(PlayerAction pa) throws Exception {
        RandomAI ai1 = this.rAI;
        RandomAI ai2 = this.rAI;
        GameState gs2 = this.gs_to_start_from.clone();
        pa = this.changePlayer(this.playerForThisComputation, pa, gs2);
        gs2.issue(pa);
        ((AI)ai1).reset();
        ((AI)ai2).reset();
        int timeLimit = gs2.getTime() + this._max_playouts;
        boolean gameover = false;
        while (!gameover && gs2.getTime() < timeLimit) {
            if (gs2.isComplete()) {
                gameover = gs2.cycle();
                continue;
            }
            PlayerAction pa1 = ((AI)ai1).getAction(this.playerForThisComputation, gs2);
            gs2.issue(pa1);
            PlayerAction pa2 = ((AI)ai2).getAction(1 - this.playerForThisComputation, gs2);
            gs2.issue(pa2);
        }
        double e = this.evaluation.evaluate(this.playerForThisComputation, 1 - this.playerForThisComputation, gs2);
        return e;
    }

    protected PlayerAction changePlayer(int player, PlayerAction pa, GameState gs) {
        PlayerAction paR = new PlayerAction();
        for (Pair<Unit, UnitAction> tmp : pa.getActions()) {
            paR.addUnitAction(gs.getUnit(((Unit)tmp.m_a).getID()), (UnitAction)tmp.m_b);
        }
        return paR;
    }

    protected PlayerAction checkIntegrity(int player, PlayerAction pa) {
        ArrayList<Pair<Unit, UnitAction>> remActions = new ArrayList<Pair<Unit, UnitAction>>();
        for (Pair<Unit, UnitAction> tmp : pa.getActions()) {
            if (((Unit)tmp.m_a).getPlayer() == player) continue;
            remActions.add(tmp);
        }
        for (Pair<Unit, UnitAction> remAction : remActions) {
            pa.removeUnitAction((Unit)remAction.m_a, (UnitAction)remAction.m_b);
        }
        return pa;
    }

    private boolean hasNewUnitToImprove() {
        ArrayList<Unit> unitsPlayer = this.getUnits(this.playerForThisComputation);
        List<Unit> unitsComputed = this.currentScriptData.getUnits();
        for (Unit unit : unitsPlayer) {
            if (unitsComputed.contains(unit)) continue;
            return true;
        }
        return false;
    }

    private ArrayList<Unit> getUnits(int player) {
        ArrayList<Unit> unitsPlayer = new ArrayList<Unit>();
        for (Unit u : this.gs_to_start_from.getUnits()) {
            if (u.getPlayer() != player) continue;
            unitsPlayer.add(u);
        }
        return unitsPlayer;
    }

    private void updateCurrentScriptData() {
        ArrayList<Unit> unitsPlayer = this.getUnits(this.playerForThisComputation);
        List<Unit> unitsComputed = this.currentScriptData.getUnits();
        for (Unit unit : unitsPlayer) {
            if (unitsComputed.contains(unit)) continue;
            this.currentScriptData.setUnitScript(unit, this._sss.getDefaultScript());
        }
    }

    @Override
    public String toString() {
        return "SABp_" + this._numUnits + "_" + this._numManager;
    }
}