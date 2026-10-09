import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/** Weapon and body poses share one planted-foot anchor, including their dark variants. */
public final class EnemySprites {
    static final int FRAME_WIDTH=128,FRAME_HEIGHT=80,ANCHOR_X=64,ANCHOR_Y=68;
    private final Image[] atlas=new Image[2],dark=new Image[2];
    private final boolean[] loaded=new boolean[2];

    private void load(int kind){
        if(loaded[kind])return;loaded[kind]=true;
        java.io.InputStream source=getClass().getResourceAsStream(kind==0?"/belfry-guard-atlas.png":"/belfry-spearman-atlas.png");
        if(source==null)return;
        try{
            Image image=Image.createImage(source);
            if(image.getWidth()!=FRAME_WIDTH*8||image.getHeight()!=FRAME_HEIGHT*4)return;
            int[] rgb=new int[image.getWidth()*image.getHeight()];
            image.getRGB(rgb,0,image.getWidth(),0,0,image.getWidth(),image.getHeight());
            for(int i=0;i<rgb.length;i++){
                int c=rgb[i],r=(c>>>16)&255,g=(c>>>8)&255,b=c&255;
                rgb[i]=(c&0xff000000)|((r*42/100)<<16)|((g*45/100)<<8)|(b*52/100);
            }
            dark[kind]=Image.createRGBImage(rgb,image.getWidth(),image.getHeight(),true);atlas[kind]=image;
        }catch(java.io.IOException e){atlas[kind]=dark[kind]=null;}
        finally{try{source.close();}catch(java.io.IOException e){}}
    }
    static int column(Belfry.Enemy e){
        if(e.hp<=0)return e.deathTime<160?0:e.deathTime<300?6:7;
        if(e.state==World.WARNING)return 5;
        if(e.state==World.ACTIVE)return 6;
        return e.moving?1+(e.walkTime/120)%4:0;
    }
    boolean draw(Graphics g,Belfry.Enemy e,int tick){
        int kind=e.elite?1:0;load(kind);if(atlas[kind]==null)return false;
        if(e.hp<=0&&e.deathTime>=Belfry.ENEMY_CORPSE_TIME)return true;
        Image image=e.lit?atlas[kind]:dark[kind];
        int dir=Art.facing(e.dx,e.dy),row=HeroSprites.directionRow(dir),column=column(e);
        int x=(int)e.x-ANCHOR_X,y=(int)e.y-ANCHOR_Y;
        if(e.hp>0&&e.hitTime>0)x+=e.dx<0?2:-2;
        if(e.hp<=0&&e.deathTime<300){
            x+=e.dx<0?2:-2;y+=e.deathTime<160?1:5;
            // The two collapse poses lower the torso while their feet remain on the floor.
            int cx=g.getClipX(),cy=g.getClipY(),cw=g.getClipWidth(),ch=g.getClipHeight();
            g.clipRect(cx,cy,cw,Math.max(0,Math.min(cy+ch,(int)e.y+1)-cy));
            g.drawRegion(image,column*FRAME_WIDTH,row*FRAME_HEIGHT,FRAME_WIDTH,FRAME_HEIGHT,0,x,y,Graphics.TOP|Graphics.LEFT);
            g.setClip(cx,cy,cw,ch);
        }else{
            g.drawRegion(image,column*FRAME_WIDTH,row*FRAME_HEIGHT,FRAME_WIDTH,FRAME_HEIGHT,0,x,y,Graphics.TOP|Graphics.LEFT);
        }
        if(e.hp>0&&e.hitTime>0&&(e.hitTime/45)%2==1){
            int hx=(int)e.x+(e.dx<0?2:-2),hy=(int)e.y-(e.elite?28:25);
            g.setColor(0xf0dfbd);g.drawLine(hx-3,hy,hx+3,hy);g.drawLine(hx,hy-3,hx,hy+3);
            g.setColor(0xc88b59);g.drawLine(hx-2,hy-2,hx+2,hy+2);
        }
        return true;
    }
}
