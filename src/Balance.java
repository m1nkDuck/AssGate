/** All combat tuning; times are milliseconds, distances logical pixels. */
public final class Balance {
    public static final int WIDTH=320, HEIGHT=240, STEP=33;
    public static final int PLAYER_HP=100, BOSS_HP=720, SWORD_DAMAGE=24;
    public static final int PLAYER_RADIUS=5, BOSS_RADIUS=10;
    // Foot baseline keeps the 88-pixel boss below the top HUD.
    public static final int BOSS_MIN_Y=120;
    public static final int WALK_SPEED=66, ROLL_SPEED=90, ROLL_TIME=360;
    public static final int DEATH_FRAME_TIME=120, DEATH_TIME=DEATH_FRAME_TIME*6;
    public static final int DOUBLE_TAP_TIME=280;
    public static final int ROLL_COOLDOWN=880;
    public static final int HURT_INV=650, HEAL_TIME=1000, HEAL_AMOUNT=40, POTIONS=3;
    public static final int SWORD_WINDUP=150, SWORD_ACTIVE=100, SWORD_RECOVERY=330;
    public static final int SWORD_RANGE=29;
    public static final int STORY_SCENE_TIME=4500, STORY_SCENES=3;
    public static final int BOSS_PREPARE=260, PHASE_TRANSITION=1100;
    public static final int[] WARN={800,900,1000,1000};
    public static final int[] ACTIVE={200,170,330,230};
    public static final int[] RECOVERY={850,950,1000,1100};
    public static final int[] DAMAGE={22,32,25,28};
    public static final int SWEEP_RADIUS=53, SLAM_LENGTH=94, SLAM_HALF_WIDTH=10;
    public static final int DASH_LENGTH=112, DASH_HALF_WIDTH=12, QUAKE_RADIUS=61;
    private Balance(){}
}
