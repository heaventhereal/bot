package blade.debug.planner;

import blade.planner.score.ScoreAction;
import blade.planner.score.ScorePlanner;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class ScorePlannerDebug {
    private double temperature;
    private ScoreAction actionTaken;
    private Object2ObjectOpenHashMap<ScoreAction, ScorePlanner.Score> scores = new Object2ObjectOpenHashMap<>();

    public Object2ObjectOpenHashMap<ScoreAction, ScorePlanner.Score> getScores() {
        return scores;
    }

    public void setScores(Object2ObjectOpenHashMap<ScoreAction, ScorePlanner.Score> scores) {
        this.scores = scores;
    }

    public void addScore(ScoreAction action, ScorePlanner.Score score) {
        scores.put(action, score);
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public ScoreAction getActionTaken() {
        return actionTaken;
    }

    public void setActionTaken(ScoreAction actionTaken) {
        this.actionTaken = actionTaken;
    }
}
