import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
/** Six-frame animations with a shared foot anchor and room for long sword strikes. */
public final class HeroSprites {
    private final Image atlas;
    private final int frameWidth,frameHeight;
    public HeroSprites(){try{atlas=Image.createImage(getClass().getResourceAsStream("/hero-atlas.png"));frameWidth=atlas.getWidth()/6;frameHeight=atlas.getHeight()/24;}catch(java.io.IOException e){throw new RuntimeException("Missing hero atlas");}}
    static int directionRow(int dir){return dir==1?0:dir==2?1:dir==3?2:3;}
    static int frame(int state,int time,int clock){
        if(state==World.ROLL)return Math.min(5,time*6/Balance.ROLL_TIME);
        if(state==World.HEAL)return Math.min(5,time*6/Balance.HEAL_TIME);
        if(state==World.ATTACK){
            if(time<Balance.SWORD_WINDUP)return Math.min(1,time*2/Balance.SWORD_WINDUP);
            if(time<Balance.SWORD_WINDUP+Balance.SWORD_ACTIVE)return 2+Math.min(1,(time-Balance.SWORD_WINDUP)*2/Balance.SWORD_ACTIVE);
            return 4+Math.min(1,(time-Balance.SWORD_WINDUP-Balance.SWORD_ACTIVE)*2/Balance.SWORD_RECOVERY);
        }
        return clock/170%6;
    }
    static int deathFrame(int time){return Math.min(5,Math.max(0,time)/Balance.DEATH_FRAME_TIME);}
    public void draw(Graphics g,World w,int dir){
        int group=w.mode==World.LOSE?5:w.pstate==World.ROLL?1:w.pstate==World.HEAL?2:w.pstate==World.ATTACK?3:w.moving&&w.pstate==World.IDLE?4:0;
        int row=group*4+directionRow(dir),col=group==5?deathFrame(w.deathTime):group==4?w.clock/95%6:frame(w.pstate,w.pt,w.clock);
        g.drawRegion(atlas,col*frameWidth,row*frameHeight,frameWidth,frameHeight,0,(int)w.px-frameWidth/2,(int)w.py-52,Graphics.TOP|Graphics.LEFT);
    }
}
