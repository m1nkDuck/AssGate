import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/** Separate camp-only sheet; combat atlas dimensions and animation order stay stable. */
public final class RestSprites {
    private Image atlas;
    static int frame(int state,int time){
        if(state==World.REST_SIT)return Math.min(5,Math.max(0,time)*6/Balance.REST_SIT_TIME);
        if(state==World.REST_RISE)return Math.min(5,Math.max(0,Balance.REST_RISE_TIME-time)*6/Balance.REST_RISE_TIME);
        return (Math.max(0,time)/700)%2==0?5:4;
    }
    public void draw(Graphics g,World w,int dir){
        if(atlas==null){
            try{atlas=Image.createImage(getClass().getResourceAsStream("/hero-rest-atlas.png"));}
            catch(java.io.IOException e){throw new RuntimeException("Missing hero rest atlas");}
        }
        int col=frame(w.restState,w.restTime),row=HeroSprites.directionRow(dir);
        g.drawRegion(atlas,col*80,row*64,80,64,0,(int)w.px-40,(int)w.py-52,Graphics.TOP|Graphics.LEFT);
    }
}
