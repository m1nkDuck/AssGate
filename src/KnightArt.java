import javax.microedition.lcdui.Graphics;
/** Articulated pixel armour. Feet anchor the rig; proportions use ~5.5 head heights. */
public final class KnightArt {
    private static final int OUT=0x101420,JOINT=0x252837;
    private static int metal(boolean boss){return boss?0x514d72:0x65588b;}
    private static int shade(boolean boss){return boss?0x29263f:0x322b55;}
    private static int shine(boolean boss){return boss?0xb9bbd2:0xc4bfdf;}
    private static int edge(boolean boss){return boss?0x827da4:0x8d82b4;}
    private static void r(Graphics g,int c,int x,int y,int w,int h){g.setColor(c);g.fillRect(x,y,w,h);}
    private static void t(Graphics g,int c,int x,int y,int a,int b,int u,int v){g.setColor(c);g.fillTriangle(x,y,a,b,u,v);}
    /** A jointed segment with a narrow metal highlight, no antialiasing. */
    private static void limb(Graphics g,int x,int y,int ex,int ey,int width,boolean boss){
        g.setColor(OUT);for(int i=-width/2-1;i<=width/2+1;i++)g.drawLine(x+i,y,ex+i,ey);
        g.setColor(metal(boss));for(int i=-width/2;i<=width/2;i++)g.drawLine(x+i,y,ex+i,ey);
        g.setColor(shine(boss));g.drawLine(x-width/2,y+1,ex-width/2,ey-1);g.setColor(shade(boss));g.drawLine(x+width/2,y,ex+width/2,ey);
    }
    private static void leg(Graphics g,int hipX,int hipY,int kneeX,int kneeY,int footX,int footY,boolean boss){
        limb(g,hipX,hipY,kneeX,kneeY,boss?4:3,boss);r(g,OUT,kneeX-3,kneeY-2,6,4);r(g,metal(boss),kneeX-2,kneeY-2,4,3);r(g,shine(boss),kneeX-2,kneeY-2,2,1);
        limb(g,kneeX,kneeY+2,footX,footY-3,3,boss);r(g,OUT,footX-3,footY-3,8,4);r(g,shade(boss),footX-2,footY-2,6,2);r(g,shine(boss),footX-2,footY-2,3,1);
    }
    static int height(boolean boss){return boss?SkeletonBossArt.HEIGHT:44;}
    static int bob(int step,boolean walking){return walking&&(step%4==1||step%4==2)?1:0;}
    static int handX(int x,int dir,int pose,int time,boolean boss){
        if(boss)return SkeletonBossArt.handX(x,dir,pose,time,0,false);
        int sign=dir==2?-1:1;
        if(pose==World.HEAL)return x+(dir==2?-4:4);
        if(pose==World.ATTACK){float p=Math.min(1,time/(float)(Balance.SWORD_WINDUP+Balance.SWORD_ACTIVE));return x+(int)((dir==2?-1:dir==3?1:1)*(4+4*p));}
        return x+(dir>=2?sign*4:dir==0?-(boss?12:10):(boss?11:9));
    }
    static int handY(int y,int dir,int pose,int time,boolean boss){
        if(boss)return SkeletonBossArt.handY(y,dir,pose,time,0,false);
        if(pose==World.HEAL)return y-height(boss)+9;
        if(pose==World.ATTACK){float p=Math.min(1,time/(float)(Balance.SWORD_WINDUP+Balance.SWORD_ACTIVE));return y-(boss?31:25)-5+(int)(p*9);}
        return y-(boss?24:20);
    }
    public static void body(Graphics g,int x,int y,int dir,int step,boolean walking,int clock){body(g,x,y,dir,step,walking,clock,World.IDLE,0);}
    public static void body(Graphics g,int x,int y,int dir,int step,boolean walking,int clock,int state,int time){rig(g,x,y,dir,step,walking,clock,state,time,false,1);}
    public static void bossBody(Graphics g,int x,int y,int dir,int step,boolean walking,int clock,int state,int time,int phase){SkeletonBossArt.body(g,x,y,dir,step,walking,clock,state,time,phase);}
    private static void rig(Graphics g,int x,int y,int dir,int step,boolean walking,int clock,int pose,int time,boolean boss,int phase){
        if(boss){SkeletonBossArt.body(g,x,y,dir,step,walking,clock,pose,time,phase);return;}
        boolean side=dir>=2,back=dir==0;int sign=dir==2?-1:1;
        int h=height(boss),head=boss?9:8,top=y-h+bob(step,walking),chest=top+head+3,hip=y-(boss?23:19);
        int half=boss?(side?7:10):(side?6:8);
        float cycle=walking?(float)Math.sin(step*Math.PI/4):0;
        int stride=(int)(cycle*(side?4:3)),sway=(int)(cycle*2),lean=pose==World.ATTACK?(dir==2?-1:dir==3?1:0):pose==World.HURT?-1:0;
        int torsoX=x+lean;
        r(g,JOINT,torsoX-1,top+head,3,4);
        // Cape follows the trailing side with a subtle one-pixel flutter.
        int cape=boss?(phase==2?0x592a38:0x352b3e):0x46305e;
        if(side){t(g,OUT,x-sign*2,chest,x-sign*(half+5),y-9,x+sign*2,y-10);t(g,cape,x-sign*3,chest+1,x-sign*(half+3),y-10-sway/2,x+sign*1,y-11);r(g,boss?0x514050:0x7d639b,x-sign*(half+1),chest+5,2,10);}
        else{t(g,OUT,x-half,chest,x+half,chest,x+half+3,y-8);t(g,OUT,x-half,chest,x+half+3,y-8,x-half-3,y-8);t(g,cape,x-half+1,chest+2,x+half-1,chest+2,x+half+1,y-10);t(g,cape,x-half+1,chest+2,x+half+1,y-10,x-half-1,y-9);}
        // Separate thighs and shins, with knee flexion and alternating foot contact.
        int footY1=y-(cycle>0?1:0),footY2=y-(cycle<0?1:0);
        if(side){
            leg(g,x-2,hip,x-2-sign*stride/2,hip+8,x-2-sign*stride,footY1,boss);
            leg(g,x+2,hip+1,x+2+sign*stride/2,hip+9,x+2+sign*stride,footY2,boss);
        }else{
            leg(g,x-4,hip,x-4,hip+8+stride/2,x-4,footY1+stride,boss);
            leg(g,x+4,hip,x+4,hip+8-stride/2,x+4,footY2-stride,boss);
        }
        // Waist is narrower than shoulders. Breastplate uses curved pixel corners.
        int bw=side?(boss?13:11):(boss?19:15),tx=torsoX-bw/2,th=hip-chest;
        r(g,OUT,tx+2,chest-1,bw-4,th+1);r(g,OUT,tx,chest+2,bw,th-2);r(g,metal(boss),tx+1,chest+2,bw-2,th-3);
        t(g,shine(boss),tx+2,chest+2,torsoX-1,chest+3,tx+3,chest+th/2);r(g,shine(boss),tx+2,chest+2,2,4);
        t(g,shade(boss),torsoX+1,chest+4,tx+bw-2,chest+2,tx+bw-2,hip-2);r(g,JOINT,tx+2,hip-4,bw-4,1);r(g,metal(boss),tx+2,hip-3,bw-4,2);
        r(g,OUT,torsoX-(side?4:6),hip-1,side?9:13,5);r(g,edge(boss),torsoX-2,hip,4,1);
        r(g,metal(boss),torsoX-(side?4:6),hip+2,side?4:5,3);r(g,shade(boss),torsoX+1,hip+2,side?4:5,3);
        if(back){r(g,cape,tx+2,chest+2,bw-4,th);r(g,boss?0x68505a:0x8971aa,tx+3,chest+3,2,th-2);r(g,shade(boss),torsoX+2,chest+4,2,th-3);}
        // Near and far arms have elbow bends and counter-swing to the legs.
        int hx=handX(x,dir,pose,time,boss),hy=handY(y,dir,pose,time,boss);
        int farX=side?x-sign*4:x-half-2,nearX=side?x+sign*4:x+half+1;
        int sy=chest+2,farHandY=hip-1+sway,nearHandY=hip-1-sway;
        limb(g,farX,sy,farX-sign*1,sy+6,2,boss);limb(g,farX-sign*1,sy+6,farX,farHandY,2,boss);
        limb(g,nearX,sy,nearX+(side?sign*2:1),sy+7,3,boss);
        if(pose==World.ATTACK||pose==World.HEAL)limb(g,nearX+(side?sign*2:1),sy+7,hx,hy,3,boss);
        else limb(g,nearX+(side?sign*2:1),sy+7,nearX,nearHandY,3,boss);
        int pad=boss?8:6;
        r(g,OUT,nearX-pad/2,sy-2,pad,5);r(g,metal(boss),nearX-pad/2+1,sy-2,pad-2,4);r(g,shine(boss),nearX-pad/2+1,sy-2,pad-3,1);
        if(!side){r(g,OUT,farX-3,sy-2,6,5);r(g,metal(boss),farX-2,sy-2,4,4);r(g,shine(boss),farX-2,sy-2,3,1);}
        if(boss){t(g,edge(true),nearX-3,sy-2,nearX,sy-6,nearX+3,sy-2);if(!side)t(g,edge(true),farX-2,sy-2,farX-3,sy-6,farX+2,sy-2);}
        // Closed pointed helmet, much smaller than the torso and leg span.
        int hw=boss?9:8,he=torsoX+(side?sign:0),hat=top;
        r(g,OUT,he-hw/2+2,hat-1,hw-4,1);r(g,OUT,he-hw/2,hat+1,hw,head-1);r(g,metal(boss),he-hw/2+1,hat+1,hw-2,head-2);
        r(g,shine(boss),he-hw/2+2,hat,hw-4,2);r(g,shine(boss),he-1,hat+2,1,head-3);
        if(back){r(g,shade(boss),he+1,hat+3,2,head-4);r(g,edge(boss),he-hw/2+1,hat+head-2,hw-2,1);}
        else if(side){r(g,OUT,he+(sign>0?0:-hw/2),hat+4,hw/2,1);r(g,edge(boss),he+sign*(hw/2-1),hat+4,1,head-3);r(g,boss?(phase==2?0xffa674:0xd67975):0xbdb7a5,he+(sign>0?2:-3),hat+4,1,1);}
        else{r(g,OUT,he-hw/2+1,hat+4,hw-2,1);r(g,edge(boss),he,hat+3,1,head-2);if(boss){r(g,phase==2?0xffa674:0xd67975,he-2,hat+4,1,1);r(g,0xd67975,he+2,hat+4,1,1);}}
        t(g,metal(boss),he-hw/2+1,hat+head-2,he+hw/2-1,hat+head-2,he,hat+head+1);
        // A compact shield prevents equipment from overwhelming anatomy.
        if(!boss&&pose!=World.ATTACK&&pose!=World.HEAL){int shx=side?x-sign*7:x-11,shy=hip-8;
            r(g,OUT,shx-3,shy,side?4:7,11);r(g,edge(false),shx-2,shy+1,side?2:5,9);r(g,shade(false),shx-1,shy+2,side?1:3,7);
            t(g,OUT,shx-3,shy+10,shx+3,shy+10,shx,shy+15);t(g,edge(false),shx-2,shy+10,shx+2,shy+10,shx,shy+13);
        }
    }
    public static void rolled(Graphics g,int x,int y,int dir,int frame){
        int sign=dir==2?-1:1;r(g,OUT,x-8,y-18,17,15);r(g,0x46305e,x-7,y-16,14,11);
        int hx=x+(frame%2==0?-3:3),hy=y-15+(frame/2)*3;
        limb(g,x-5,y-5,x+4,y-13,3,false);limb(g,x+4,y-13,x+7,y-7,3,false);
        r(g,OUT,hx-3,hy-3,7,8);r(g,metal(false),hx-2,hy-2,5,6);r(g,shine(false),hx-1,hy-2,3,1);r(g,OUT,hx-1,hy+1,3,1);
        r(g,shade(false),x-sign*8,y-6,5,3);
    }
    public static void fallen(Graphics g,int x,int y){fallen(g,x,y,false);}
    public static void fallen(Graphics g,int x,int y,boolean boss){
        if(boss){SkeletonBossArt.fallen(g,x,y);return;}
        r(g,OUT,x-20,y-8,39,9);r(g,boss?0x352b3e:0x46305e,x-17,y-6,28,7);
        limb(g,x-16,y-3,x-5,y-5,3,boss);limb(g,x-5,y-5,x+5,y-4,4,boss);r(g,metal(boss),x+3,y-8,11,7);r(g,shine(boss),x+4,y-8,7,2);
        r(g,OUT,x+13,y-10,8,8);r(g,metal(boss),x+14,y-9,6,6);r(g,shine(boss),x+15,y-9,3,1);r(g,OUT,x+16,y-6,4,1);
    }
    private KnightArt(){}
}
