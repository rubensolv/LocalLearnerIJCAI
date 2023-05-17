package ai.asymmetric.GAB.SandBox;

import ai.RandomBiasedAI;
import ai.abstraction.partialobservability.POLightRush;
import ai.abstraction.pathfinding.AStarPathFinding;
import ai.abstraction.pathfinding.PathFinding;
import ai.asymmetric.common.UnitScriptData;
import ai.configurablescript.BasicExpandedConfigurableScript;
import ai.configurablescript.ScriptsCreator;
import ai.core.AI;
import ai.core.AIWithComputationBudget;
import ai.core.InterruptibleAI;
import ai.core.ParameterSpecification;
import ai.evaluation.EvaluationFunction;
import ai.evaluation.SimpleSqrtEvaluationFunction3;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import rts.GameState;
import rts.PlayerAction;
import rts.UnitAction;
import rts.units.Unit;
import rts.units.UnitTypeTable;
import util.Pair;

public class LightGAS_PGS
extends AIWithComputationBudget
implements InterruptibleAI {
    int LOOKAHEAD = 200;
    int I = 1;
    int R = 0;
    EvaluationFunction evaluation = null;
    List<AI> scripts = null;
    UnitTypeTable utt;
    PathFinding pf;
    int _startTime;
    public AI defaultScript = null;
    private AI enemyScript = null;
    long start_time = 0L;
    int nplayouts = 0;
    GameState gs_to_start_from = null;
    int playerForThisComputation;
    double _bestScore;
    String tuplaInScripts = "";
    AI randAI = null;
    int qtdSumPlayout = 2;
    HashMap<String, PlayerAction> cache;

    public LightGAS_PGS(UnitTypeTable utt) {
        this(100, -1, 200, 1, 1, new SimpleSqrtEvaluationFunction3(), utt, new AStarPathFinding());
    }

    public LightGAS_PGS(int time, int max_playouts, int la, int a_I, int a_R, EvaluationFunction e, UnitTypeTable a_utt, PathFinding a_pf) {
        super(time, max_playouts);
        this.LOOKAHEAD = la;
        this.I = a_I;
        this.R = a_R;
        this.evaluation = e;
        this.utt = a_utt;
        this.pf = a_pf;
        this.defaultScript = new POLightRush(a_utt);
        this.scripts = new ArrayList<AI>();
        this.buildPortfolio();
        this.randAI = new RandomBiasedAI(a_utt);
    }

    public void setNewPortfolio(List<AI> scripts) {
        this.scripts = scripts;
    }

    protected void buildPortfolio() {
        ScriptsCreator sc = new ScriptsCreator(this.utt, 300);
        ArrayList<BasicExpandedConfigurableScript> scriptsCompleteSet = sc.getScriptsMixReducedSet();
        this.scripts.add(0, scriptsCompleteSet.get(0));
        this.scripts.add(1, scriptsCompleteSet.get(1));
        this.scripts.add(2, scriptsCompleteSet.get(2));
        this.scripts.add(3, scriptsCompleteSet.get(3));
    }

    @Override
    public void reset() {
    }

    @Override
    public PlayerAction getAction(int player, GameState gs) throws Exception {
        if (gs.canExecuteAnyAction(player)) {
            this.startNewComputation(player, gs);
            return this.getBestActionSoFar();
        }
        return new PlayerAction();
    }

    @Override
    public PlayerAction getBestActionSoFar() throws Exception {
        AI seedEnemy;
        this.getCache();
        AI seedPlayer = this.getSeedPlayer(this.playerForThisComputation);
        this.enemyScript = seedEnemy = this.getSeedPlayer(1 - this.playerForThisComputation);
        this.defaultScript = seedPlayer;
        UnitScriptData currentScriptData = new UnitScriptData(this.playerForThisComputation);
        currentScriptData.setSeedUnits(seedPlayer);
        this.setAllScripts(this.playerForThisComputation, currentScriptData, seedPlayer);
        if (System.currentTimeMillis() - this.start_time < (long)this.TIME_BUDGET) {
            currentScriptData = this.doPortfolioSearch(this.playerForThisComputation, currentScriptData, seedEnemy);
        }
        return this.getFinalAction(currentScriptData);
    }

    public UnitScriptData getUnitScript(int player, GameState gs) throws Exception {
        this.startNewComputation(player, gs);
        return this.getBestUnitScriptSoFar();
    }

    public UnitScriptData continueImproveUnitScript(int player, GameState gs, UnitScriptData currentScriptData) throws Exception {
        this.startNewComputation(player, gs);
        this.getCache();
        AI seedPlayer = this.getSeedPlayer(this.playerForThisComputation);
        AI seedEnemy = this.getSeedPlayer(1 - this.playerForThisComputation);
        this.defaultScript = seedPlayer;
        this.enemyScript = seedEnemy;
        currentScriptData.setSeedUnits(seedPlayer);
        if (System.currentTimeMillis() - this.start_time < (long)this.TIME_BUDGET) {
            currentScriptData = this.doPortfolioSearch(this.playerForThisComputation, currentScriptData, seedEnemy);
        }
        return currentScriptData;
    }

    public UnitScriptData getBestUnitScriptSoFar() throws Exception {
        this.getCache();
        AI seedPlayer = this.getSeedPlayer(this.playerForThisComputation);
        AI seedEnemy = this.getSeedPlayer(1 - this.playerForThisComputation);
        this.defaultScript = seedPlayer;
        this.enemyScript = seedEnemy;
        UnitScriptData currentScriptData = new UnitScriptData(this.playerForThisComputation);
        currentScriptData.setSeedUnits(seedPlayer);
        this.setAllScripts(this.playerForThisComputation, currentScriptData, seedPlayer);
        if (System.currentTimeMillis() - this.start_time < (long)this.TIME_BUDGET) {
            currentScriptData = this.doPortfolioSearch(this.playerForThisComputation, currentScriptData, seedEnemy);
        }
        return currentScriptData;
    }

    protected AI getSeedPlayer(int player) throws Exception {
        AI seed = null;
        double bestEval = -9999.0;
        AI enemyAI = this.defaultScript.clone();
        for (AI script : this.scripts) {
            double sum = 0.0;
            for (int i = 0; i < this.qtdSumPlayout; ++i) {
                sum += this.eval(player, this.gs_to_start_from, script, enemyAI);
            }
            double tEval = sum / (double)this.qtdSumPlayout;
            if (tEval > bestEval) {
                bestEval = tEval;
                seed = script;
            }
            if (System.currentTimeMillis() - this.start_time <= (long)this.TIME_BUDGET) continue;
            return seed;
        }
        return seed;
    }

    public double eval(int player, GameState gs, AI aiPlayer, AI aiEnemy) throws Exception {
        AI ai1 = aiPlayer.clone();
        AI ai2 = aiEnemy.clone();
        GameState gs2 = gs.clone();
        ai1.reset();
        ai2.reset();
        gs2.issue(ai1.getAction(player, gs2));
        gs2.issue(ai2.getAction(1 - player, gs2));
        int timeLimit = gs2.getTime() + this.LOOKAHEAD;
        boolean gameover = false;
        while (!gameover && gs2.getTime() < timeLimit && this.hasMoreTime()) {
            if (gs2.isComplete()) {
                gameover = gs2.cycle();
                continue;
            }
            gs2.issue(this.randAI.getAction(player, gs2));
            gs2.issue(this.randAI.getAction(1 - player, gs2));
        }
        double e = this.evaluation.evaluate(player, 1 - player, gs2);
        return e;
    }

    public double eval(int player, GameState gs, UnitScriptData uScriptPlayer, AI aiEnemy) throws Exception {
        AI ai2 = aiEnemy.clone();
        ai2.reset();
        GameState gs2 = gs.clone();
        gs2.issue(this.getActionsUScript(player, uScriptPlayer, gs2));
        gs2.issue(ai2.getAction(1 - player, gs2));
        int timeLimit = gs2.getTime() + this.LOOKAHEAD;
        boolean gameover = false;
        while (!gameover && gs2.getTime() < timeLimit && this.hasMoreTime()) {
            if (gs2.isComplete()) {
                gameover = gs2.cycle();
                continue;
            }
            gs2.issue(this.randAI.getAction(player, gs2));
            gs2.issue(this.randAI.getAction(1 - player, gs2));
        }
        return this.evaluation.evaluate(player, 1 - player, gs2);
    }

    @Override
    public AI clone() {
        return new LightGAS_PGS(this.TIME_BUDGET, this.ITERATIONS_BUDGET, this.LOOKAHEAD, this.I, this.R, this.evaluation, this.utt, this.pf);
    }

    @Override
    public List<ParameterSpecification> getParameters() {
        ArrayList<ParameterSpecification> parameters = new ArrayList<ParameterSpecification>();
        parameters.add(new ParameterSpecification("TimeBudget", Integer.TYPE, 100));
        parameters.add(new ParameterSpecification("IterationsBudget", Integer.TYPE, -1));
        parameters.add(new ParameterSpecification("PlayoutLookahead", Integer.TYPE, 100));
        parameters.add(new ParameterSpecification("I", Integer.TYPE, 1));
        parameters.add(new ParameterSpecification("R", Integer.TYPE, 1));
        parameters.add(new ParameterSpecification("EvaluationFunction", EvaluationFunction.class, new SimpleSqrtEvaluationFunction3()));
        parameters.add(new ParameterSpecification("PathFinding", PathFinding.class, new AStarPathFinding()));
        return parameters;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(" + this.TIME_BUDGET + ", " + this.ITERATIONS_BUDGET + ", " + this.LOOKAHEAD + ", " + this.I + ", " + this.R + ", " + this.evaluation + ", " + this.pf + ")";
    }

    public int getPlayoutLookahead() {
        return this.LOOKAHEAD;
    }

    public void setPlayoutLookahead(int a_pola) {
        this.LOOKAHEAD = a_pola;
    }

    public int getI() {
        return this.I;
    }

    public void setI(int a) {
        this.I = a;
    }

    public int getR() {
        return this.R;
    }

    public void setR(int a) {
        this.R = a;
    }

    public EvaluationFunction getEvaluationFunction() {
        return this.evaluation;
    }

    public void setEvaluationFunction(EvaluationFunction a_ef) {
        this.evaluation = a_ef;
    }

    public PathFinding getPathFinding() {
        return this.pf;
    }

    public void setPathFinding(PathFinding a_pf) {
        this.pf = a_pf;
    }

    public double getBestScore() {
        return this._bestScore;
    }

    public AI getDefaultScript() {
        return this.defaultScript;
    }

    public AI getEnemyScript() {
        return this.enemyScript;
    }

    @Override
    public void startNewComputation(int player, GameState gs) throws Exception {
        this.playerForThisComputation = player;
        this.gs_to_start_from = gs;
        this.nplayouts = 0;
        this._startTime = gs.getTime();
        this.start_time = System.currentTimeMillis();
        this._bestScore = 0.0;
        this.cache = new HashMap();
    }

    @Override
    public void computeDuringOneGameFrame() throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void setAllScripts(int playerForThisComputation, UnitScriptData currentScriptData, AI seedPlayer) {
        currentScriptData.reset();
        for (Unit u : this.gs_to_start_from.getUnits()) {
            if (u.getPlayer() != playerForThisComputation) continue;
            currentScriptData.setUnitScript(u, seedPlayer);
        }
    }

    private UnitScriptData doPortfolioSearch(int player, UnitScriptData currentScriptData, AI seedEnemy) throws Exception {
        int enemy = 1 - player;
        UnitScriptData bestScriptData = currentScriptData.clone();
        double bestScore = this.eval(player, this.gs_to_start_from, bestScriptData, seedEnemy);
        ArrayList<Unit> unitsPlayer = this.getUnitsPlayer(player);
        int counterIterations = 0;
        while (System.currentTimeMillis() < this.start_time + (long)this.TIME_BUDGET) {
            boolean hasImproved = false;
            for (Unit unit : unitsPlayer) {
                if (System.currentTimeMillis() >= this.start_time + (long)(this.TIME_BUDGET - 10)) {
                    return currentScriptData;
                }
                for (AI ai : this.scripts) {
                    currentScriptData.setUnitScript(unit, ai);
                    double sum = 0.0;
                    for (int i = 0; i < this.qtdSumPlayout; ++i) {
                        sum += this.eval(player, this.gs_to_start_from, currentScriptData, seedEnemy);
                    }
                    double scoreTemp = sum / (double)this.qtdSumPlayout;
                    if (scoreTemp > bestScore) {
                        bestScriptData = currentScriptData.clone();
                        bestScore = scoreTemp;
                    }
                    if ((counterIterations != 0 || this.scripts.get(0) != ai) && !(scoreTemp > this._bestScore)) continue;
                    this._bestScore = bestScore;
                    hasImproved = true;
                }
                currentScriptData = bestScriptData.clone();
            }
            if (!hasImproved) {
                return currentScriptData;
            }
            ++counterIterations;
        }
        return currentScriptData;
    }

    private ArrayList<Unit> getUnitsPlayer(int player) {
        ArrayList<Unit> unitsPlayer = new ArrayList<Unit>();
        for (Unit u : this.gs_to_start_from.getUnits()) {
            if (u.getPlayer() != player) continue;
            unitsPlayer.add(u);
        }
        return unitsPlayer;
    }

    public PlayerAction getFinalAction(UnitScriptData currentScriptData) throws Exception {
        PlayerAction pAction = new PlayerAction();
        HashMap<String, PlayerAction> actions = new HashMap<String, PlayerAction>();
        for (AI script : this.scripts) {
            actions.put(script.toString(), script.getAction(this.playerForThisComputation, this.gs_to_start_from));
        }
        for (Unit u : currentScriptData.getUnits()) {
            AI ai = currentScriptData.getAIUnit(u);
            UnitAction unt = ((PlayerAction)actions.get(ai.toString())).getAction(u);
            if (unt == null) continue;
            pAction.addUnitAction(u, unt);
        }
        return pAction;
    }

    private void getCache() throws Exception {
        for (AI script : this.scripts) {
            this.cache.put(script.toString(), script.getAction(this.playerForThisComputation, this.gs_to_start_from));
        }
    }

    private PlayerAction getActionsUScript(int player, UnitScriptData uScriptPlayer, GameState gs2) {
        PlayerAction temp = new PlayerAction();
        for (Unit u : gs2.getUnits()) {
            String sAI;
            UnitAction uAt;
            if (u.getPlayer() != player || (uAt = this.getUnitAction(u, this.cache.get(sAI = uScriptPlayer.getAIUnit(u).toString()))) == null) continue;
            temp.addUnitAction(u, uAt);
        }
        return temp;
    }

    private UnitAction getUnitAction(Unit u, PlayerAction get) {
        for (Pair<Unit, UnitAction> tmp : get.getActions()) {
            if (((Unit)tmp.m_a).getID() != u.getID()) continue;
            return (UnitAction)tmp.m_b;
        }
        return null;
    }

    private PlayerAction getCacheActions(int player, GameState gs2, AI aiDefault) {
        PlayerAction temp = new PlayerAction();
        for (Unit u : gs2.getUnits()) {
            String sAI;
            UnitAction uAt;
            if (u.getPlayer() != player || (uAt = this.getUnitAction(u, this.cache.get(sAI = aiDefault.toString()))) == null) continue;
            temp.addUnitAction(u, uAt);
        }
        return temp;
    }

    private boolean hasMoreTime() {
        return System.currentTimeMillis() - this.start_time <= (long)this.TIME_BUDGET;
    }
}