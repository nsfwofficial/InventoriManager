package id.web.dmalvian.invman.util;

public class PauseHelper {
    private boolean paused;

    public PauseHelper(boolean paused) {
        this.paused = paused;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }
}
