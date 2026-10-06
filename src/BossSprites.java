import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/** Eight isolated boss poses in each of four directions; planted soles share one anchor. */
public final class BossSprites {
    static final int FRAME_WIDTH=224,FRAME_HEIGHT=112,ANCHOR_X=112,ANCHOR_Y=100;
    private Image atlas;
    public BossSprites(){
        java.io.InputStream source=getClass().getResourceAsStream("/boss-atlas.png");
        if(source==null)return;
        try{atlas=Image.createImage(source);if(atlas.getWidth()!=FRAME_WIDTH*8||atlas.getHeight()!=FRAME_HEIGHT*4)atlas=null;}
        catch(java.io.IOException e){atlas=null;}
        finally{try{source.close();}catch(java.io.IOException e){}}
    }
    boolean draw(Graphics g,World w,int dir,int step,boolean walking){
        if(atlas==null)return false;
        int column=w.mode==World.WIN?7:w.bstate==World.PREPARE||w.bstate==World.WARNING?5:w.bstate==World.ACTIVE?6:walking?1+(step/2)%4:0;
        int shift=(w.events&World.BOSS_HIT)!=0&&w.mode!=World.WIN?(dir==2?2:-2):0;
        int row=HeroSprites.directionRow(dir);
        g.drawRegion(atlas,column*FRAME_WIDTH,row*FRAME_HEIGHT,FRAME_WIDTH,FRAME_HEIGHT,0,
                     (int)w.bx-ANCHOR_X+shift,(int)w.by-ANCHOR_Y,Graphics.TOP|Graphics.LEFT);
        if(w.phase==2&&w.mode!=World.WIN){
            // A floor aura signals the second phase without recoloring the bones.
            g.setColor(0xca7853);g.drawArc((int)w.bx-24,(int)w.by-5,48,10,0,360);
        }
        return true;
    }
}
