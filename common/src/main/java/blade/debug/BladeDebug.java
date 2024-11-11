package blade.debug;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;


public class BladeDebug {
    // add json from and to for file saving
    private final ObjectArrayList<DebugFrame> frames = new ObjectArrayList<>();

    public void addTick(DebugFrame tick) {
        frames.add(tick);
    }

    public ObjectArrayList<DebugFrame> getFrames() {
        return frames;
    }

    public DebugFrame newFrame() {
        DebugFrame tick = new DebugFrame();
        addTick(tick);
        return tick;
    }
}
