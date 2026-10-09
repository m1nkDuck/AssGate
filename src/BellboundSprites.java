import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/** Body-only poses; the native chain and bell retain their disclosed target geometry. */
public final class BellboundSprites {
    static final int FRAME_WIDTH=112,FRAME_HEIGHT=112,ANCHOR_X=56,ANCHOR_Y=100;
    // BEGIN PACKED GRIPS
    private static final int[] GRIP_X={26,24,24,24,25,24,-3,2,30,-35,-22,-15,-19,-23,-16,-21,6,-8,-28,36,22,19,20,22,18,27,11,9,27,-35,-26,-26,-27,-26,-24,-29,-28,-29,-31,-34};
    private static final int[] GRIP_Y={-25,-25,-36,-25,-35,-80,-25,-4,-60,-6,-22,-24,-27,-24,-26,-78,-26,-5,-66,-5,-25,-31,-31,-25,-28,-79,-24,-7,-69,-6,-27,-28,-28,-28,-27,-73,-30,-13,-68,-7};
    // END PACKED GRIPS
    private Image atlas;
    private boolean attempted;
    private void load(){
        if(attempted)return;attempted=true;
        java.io.InputStream source=getClass().getResourceAsStream("/bellbound-atlas.png");
        if(source==null)return;
        try{
            atlas=Image.createImage(source);
            if(atlas.getWidth()!=FRAME_WIDTH*10||atlas.getHeight()!=FRAME_HEIGHT*4)atlas=null;
        }catch(java.io.IOException error){atlas=null;}
        finally{try{source.close();}catch(java.io.IOException error){}}
    }
    static int directionRow(Bellbound b){
        return Math.abs(b.dx)>Math.abs(b.dy)?(b.dx<0?1:2):b.dy<0?3:0;
    }
    static int frame(Bellbound b,int clock,int deathTime){
        if(b.dead)return deathTime<150?8:deathTime<320?7:9;
        if(b.state==World.TRANSITION)return 8;
        if(b.state==World.PREPARE||b.state==World.WARNING)return 5;
        if(b.state==World.ACTIVE)return b.attack==Bellbound.SWEEP?6:b.attack==Bellbound.SLAM?7:8;
        // The weight stays visibly committed before returning to the idle stance.
        if(b.state==World.RECOVER&&b.time<240)return b.attack==Bellbound.SWEEP?6:b.attack==Bellbound.SLAM?7:8;
        return b.state==World.SEEK&&b.moving?1+(Math.max(0,b.walkTime)/160)%4:0;
    }
    static int recoilX(Bellbound b){
        if(b.dead||b.hitTime<=0)return 0;
        int sign=Math.abs(b.dx)>Math.abs(b.dy)?(b.dx<0?-1:1):b.dy<0?-1:1;
        return -sign*(b.hitTime>90?2:1);
    }
    private static int lean(Bellbound b){
        return !b.dead&&(b.state==World.ACTIVE||b.state==World.RECOVER&&b.attack==Bellbound.SLAM)?
            (Math.abs(b.dx)>Math.abs(b.dy)?(b.dx<0?-2:2):b.dy<0?-2:2):0;
    }
    static int handX(Bellbound b,int clock,int deathTime){
        return (int)b.x+lean(b)+recoilX(b)+GRIP_X[directionRow(b)*10+frame(b,clock,deathTime)];
    }
    static int handY(Bellbound b,int clock,int deathTime){
        return (int)b.y+GRIP_Y[directionRow(b)*10+frame(b,clock,deathTime)];
    }
    boolean draw(Graphics g,Bellbound b,int clock,int deathTime){
        load();if(atlas==null)return false;
        int col=frame(b,clock,deathTime),row=directionRow(b);
        g.drawRegion(atlas,col*FRAME_WIDTH,row*FRAME_HEIGHT,FRAME_WIDTH,FRAME_HEIGHT,0,
                     (int)b.x-ANCHOR_X+lean(b)+recoilX(b),(int)b.y-ANCHOR_Y,Graphics.TOP|Graphics.LEFT);
        return true;
    }
}
