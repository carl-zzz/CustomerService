package com.app.model;

public class Staff {
    private long id;
    private Level level;
    private long handlingId;

    public Staff(long id, Level level) {
        this.id = id;
        this.level = level;
        this.handlingId = -1;
    }

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    public Level getLevel() {
        return level;
    }
    public void setLevel(Level level) {
        this.level = level;
    }

    public long getHandlingId() {
        return handlingId;
    }
    public void setHandlingId(long handlingId) {
        this.handlingId = handlingId;
    }
}
