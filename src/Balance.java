/** All combat tuning; times are milliseconds, distances logical pixels. */
public final class Balance {
    public static final int WIDTH=320, HEIGHT=240, STEP=33;
    public static final int PLAYER_HP=100, BOSS_HP=720, SWORD_DAMAGE=24;
    public static final int PLAYER_RADIUS=5, BOSS_RADIUS=10;
    // Foot baseline keeps the 88-pixel boss below the top HUD.
    public static final int BOSS_MIN_Y=120;
    public static final int WALK_SPEED=66, ROLL_SPEED=90, ROLL_TIME=360;
    public static final int DEATH_FRAME_TIME=120, DEATH_TIME=DEATH_FRAME_TIME*6;
    public static final int VICTORY_WAIT=5000, FADE_OUT_TIME=900, BLACK_HOLD_TIME=550;
    public static final int FADE_IN_TIME=900, CAMP_WAKE_TIME=1800;
    public static final int CAMP_NOTICE_TIME=1600;
    public static final int REST_SIT_TIME=720, REST_RISE_TIME=720, REST_SEAT_RADIUS=38;
    public static final int DOUBLE_TAP_TIME=280;
    public static final int ROLL_COOLDOWN=880;
    public static final int HURT_INV=650, HEAL_TIME=1000, HEAL_AMOUNT=40, POTIONS=3;
    public static final int SWORD_WINDUP=150, SWORD_ACTIVE=100, SWORD_RECOVERY=330;
    public static final int SWORD_RANGE=29;
    public static final int STORY_SCENE_TIME=4500, STORY_SCENES=3;
    public static final int BOSS_PREPARE=130, PHASE_TRANSITION=850;
    public static final int BOSS_SEEK_TIME=300, BOSS_SEEK_TIME_II=210;
    public static final int BOSS_WALK_SPEED=40, BOSS_WALK_SPEED_II=48;
    public static final int PHASE_WARNING_REDUCTION=100, PHASE_RECOVERY_REDUCTION=90;
    public static final int[] WARN={430,500,550,580};
    public static final int[] ACTIVE={150,130,250,180};
    public static final int[] RECOVERY={420,470,450,500};
    public static final int[] DAMAGE={45,62,52,58};
    public static final int SWEEP_RADIUS=82, SLAM_LENGTH=145, SLAM_HALF_WIDTH=18;
    public static final int DASH_LENGTH=175, DASH_HALF_WIDTH=20, QUAKE_RADIUS=96;
    public static final int BELFRY_GUARD_HP=48, BELFRY_ELITE_HP=96;
    public static final int BELFRY_GUARD_SPEED=25, BELFRY_LIT_SPEED=13;
    public static final int BELFRY_GUARD_DAMAGE=18, BELFRY_ELITE_DAMAGE=28;
    public static final int BELFRY_WARNING_TIME=700, BELFRY_ELITE_WARNING_TIME=800;
    public static final int BELFRY_LIGHT_RADIUS=78, BELFRY_FIRST_GUARD_TIME=3500;
    public static final int BELFRY_SECOND_GUARD_DELAY=700, BELFRY_ENDING_TIME=3000;
    public static final int BELFRY_NOTICE_TIME=2600;
    public static final int BELFRY_QUOTE_TIME=1800;
    private Balance(){}
}
