package blade.debug;

import blade.debug.planner.ScorePlannerDebug;
import blade.planner.score.ScoreState;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;

public class DebugFrame {
    private final List<ReportError> errors = new ObjectArrayList<>();

    private ScoreState state;
    private final ScorePlannerDebug planner = new ScorePlannerDebug();

    public List<ReportError> getErrors() {
        return errors;
    }

    public void addError(ReportError error) {
        errors.add(error);
    }

    public ScoreState getState() {
        return state;
    }

    public void setState(ScoreState state) {
        this.state = state;
    }

    public ScorePlannerDebug getPlanner() {
        return planner;
    }
}
