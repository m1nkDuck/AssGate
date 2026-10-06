import javax.microedition.lcdui.Graphics;

/** An exposed skeleton inside broken plate. All rig coordinates scale by two. */
public final class SkeletonBossArt {
    static final int HEIGHT=88;
    private static final int OUT=0x101420, STEEL=0x514d72, DARK=0x29263f;
    private static final int EDGE=0x827da4, LIGHT=0xb9bbd2;
    private static final int BONE=0xd8ccb0, BONE_LIGHT=0xf1e5c8, BONE_SHADE=0x94846f;
    private static void r(Graphics g,int c,int x,int y,int a,int b,int w,int h){
        g.setColor(c);g.fillRect(x+a*2,y+b*2,w*2,h*2);
    }
    private static void t(Graphics g,int c,int x,int y,int a,int b,int u,int v,int p,int q){
        g.setColor(c);g.fillTriangle(x+a*2,y+b*2,x+u*2,y+v*2,x+p*2,y+q*2);
    }
    private static void bone(Graphics g,int x,int y,int a,int b,int u,int v){
        int sx=x+a*2,sy=y+b*2,ex=x+u*2,ey=y+v*2;
        g.setColor(OUT);for(int i=-3;i<=3;i++)g.drawLine(sx+i,sy,ex+i,ey);
        g.setColor(BONE_SHADE);for(int i=-2;i<=2;i++)g.drawLine(sx+i,sy,ex+i,ey);
        g.setColor(BONE);for(int i=-1;i<=0;i++)g.drawLine(sx+i,sy,ex+i,ey);
        g.setColor(BONE_LIGHT);g.drawLine(sx-1,sy,ex-1,ey);
    }
    private static int lean(int dir,int pose,int time){
        if(pose==World.HURT)return dir==2?1:-1;
        return pose==World.ATTACK&&time>0?(dir==2?-1:dir==3?1:0):0;
    }
    private static int swing(int step,boolean walking){return walking?(int)(Math.sin(step*Math.PI/4)*2):0;}
    private static int localHandX(int dir,int pose,int time){
        int sign=dir==2?-1:1,arm=dir==1?-1:1;
        if(pose==World.ATTACK)return (dir>=2?sign:arm)*(time==0?10:13);
        return dir>=2?sign*7:arm*12;
    }
    private static int localHandY(int pose,int time,int step,boolean walking){
        return pose==World.ATTACK?(time==0?-32:-24):-21+swing(step,walking)/2;
    }
    static int handX(int x,int dir,int pose,int time,int step,boolean walking){
        return x+2*(localHandX(dir,pose,time)+lean(dir,pose,time));
    }
    static int handY(int y,int dir,int pose,int time,int step,boolean walking){
        return y+2*(localHandY(pose,time,step,walking)+KnightArt.bob(step,walking));
    }
    private static void leg(Graphics g,int x,int y,int hip,int knee,int foot,int lift,int sign){
        bone(g,x,y,hip,-18,knee,-11-lift/2);
        r(g,OUT,x,y,knee-3,-13-lift/2,6,5);
        r(g,STEEL,x,y,knee-2,-12-lift/2,4,3);
        r(g,LIGHT,x,y,knee-2,-12-lift/2,3,1);
        bone(g,x,y,knee,-8-lift/2,foot,-3-lift);
        // A narrow greave leaves the shin bone visible at its inside edge.
        r(g,DARK,x,y,foot+(sign<0?-3:1),-8-lift,2,5);
        r(g,EDGE,x,y,foot+(sign<0?-3:1),-8-lift,1,4);
        r(g,OUT,x,y,foot-3,-3-lift,7,3);
        r(g,STEEL,x,y,foot-2,-3-lift,5,2);
        r(g,LIGHT,x,y,foot-2,-3-lift,3,1);
    }
    private static void pad(Graphics g,int x,int y,int cx){
        r(g,OUT,x,y,cx-2,-33,5,1);r(g,OUT,x,y,cx-3,-32,7,5);
        r(g,STEEL,x,y,cx-2,-32,5,4);r(g,LIGHT,x,y,cx-2,-32,4,1);
        r(g,EDGE,x,y,cx-2,-31,1,2);r(g,DARK,x,y,cx+1,-30,2,2);
        r(g,EDGE,x,y,cx-2,-28,4,1);
    }
    private static void arm(Graphics g,int x,int y,int shoulder,int hx,int hy,boolean weapon){
        int elbow=weapon?(shoulder+hx)/2:shoulder+(shoulder<0?-1:1);
        int ey=weapon&&hy<-27?-28:-24;
        bone(g,x,y,shoulder,-29,elbow,ey);
        r(g,OUT,x,y,elbow-2,ey-1,4,3);r(g,STEEL,x,y,elbow-1,ey-1,2,2);
        r(g,LIGHT,x,y,elbow-1,ey-1,1,1);
        bone(g,x,y,elbow,ey+1,hx,hy);
        r(g,OUT,x,y,hx-2,hy-1,4,3);r(g,BONE,x,y,hx-1,hy-1,2,2);
        r(g,BONE_LIGHT,x,y,hx-1,hy-1,1,1);r(g,BONE_SHADE,x,y,hx,hy+1,1,1);
    }
    private static void chest(Graphics g,int x,int y,boolean side,boolean back,int sign){
        int half=side?5:7;
        r(g,OUT,x,y,-half,-31,half*2+1,13);
        r(g,STEEL,x,y,-half,-30,half*2+1,10);
        r(g,LIGHT,x,y,-half+1,-30,side?3:5,1);
        r(g,EDGE,x,y,-half,-29,2,5);r(g,DARK,x,y,half-1,-29,2,9);
        // The missing middle plate exposes ribs and spine; this is not a visor motif.
        int window=side?sign-3:-4,ww=side?5:9;
        r(g,OUT,x,y,window,-29,ww,10);
        if(back){
            r(g,BONE_SHADE,x,y,-1,-30,3,11);r(g,BONE,x,y,-1,-30,2,11);
            for(int i=0;i<5;i++)r(g,BONE_LIGHT,x,y,-2,-29+i*2,3,1);
            t(g,BONE_SHADE,x,y,-4,-28,-2,-27,-4,-24);
            t(g,BONE,x,y,3,-28,1,-27,3,-25);
        }else if(side){
            r(g,BONE_SHADE,x,y,window,-29,1,9);
            for(int i=0;i<4;i++){
                r(g,BONE,x,y,window+1,-28+i*2,3,1);
                r(g,BONE_LIGHT,x,y,window+2,-28+i*2,1,1);
                r(g,BONE_SHADE,x,y,window+3,-27+i*2,1,1);
            }
        }else{
            r(g,BONE_SHADE,x,y,0,-29,1,10);
            for(int i=0;i<4;i++){
                r(g,BONE,x,y,-3,-28+i*2,3,1);r(g,BONE_LIGHT,x,y,-3,-28+i*2,2,1);
                r(g,BONE,x,y,1,-28+i*2,3,1);
                r(g,BONE_SHADE,x,y,-4,-27+i*2,1,1);r(g,BONE_SHADE,x,y,4,-27+i*2,1,1);
            }
        }
        // Broken side plates and a bronze clasp frame the exposed skeleton.
        t(g,EDGE,x,y,-half,-27,-half+2,-25,-half,-22);
        r(g,OUT,x,y,-half,-20,half*2+1,3);r(g,STEEL,x,y,-half+1,-20,half*2-1,2);
        r(g,EDGE,x,y,-half+1,-20,half*2-1,1);r(g,0xa18b61,x,y,-1,-20,2,2);
        t(g,OUT,x,y,-half,-18,-1,-18,-3,-14);t(g,STEEL,x,y,-half+1,-18,-2,-18,-3,-15);
        t(g,OUT,x,y,2,-18,half,-18,4,-14);t(g,DARK,x,y,2,-18,half-1,-18,4,-15);
    }
    private static void skull(Graphics g,int x,int y,int dir,int phase){
        boolean side=dir>=2,back=dir==0;int sign=dir==2?-1:1;
        // Open helmet: steel crown and cheek pieces surround an ivory skull.
        r(g,OUT,x,y,-4,-44,8,1);r(g,OUT,x,y,-5,-43,10,8);
        r(g,STEEL,x,y,-4,-43,8,2);r(g,LIGHT,x,y,-3,-43,4,1);
        r(g,DARK,x,y,3,-42,2,7);
        r(g,BONE_SHADE,x,y,-3,-41,7,6);r(g,BONE,x,y,-3,-41,6,5);
        r(g,BONE_LIGHT,x,y,-2,-41,4,1);
        if(back){
            r(g,BONE_LIGHT,x,y,-2,-40,2,3);r(g,BONE_SHADE,x,y,2,-39,1,3);
            r(g,OUT,x,y,0,-39,1,1);r(g,OUT,x,y,-2,-36,4,1);
        }else if(side){
            int face=sign>0?1:-3;
            r(g,OUT,x,y,face,-39,2,2);r(g,phase==2?0xe99b66:0xaf6977,x,y,face+(sign>0?1:0),-39,1,1);
            r(g,BONE_LIGHT,x,y,sign>0?3:-5,-38,2,1);
            r(g,OUT,x,y,sign>0?3:-4,-37,1,1);
            r(g,BONE,x,y,sign>0?0:-4,-35,4,2);
            for(int i=0;i<2;i++)r(g,OUT,x,y,(sign>0?1:-3)+i*2,-35,1,1);
            r(g,EDGE,x,y,sign>0?-4:3,-40,1,6);
        }else{
            r(g,OUT,x,y,-3,-39,2,2);r(g,OUT,x,y,1,-39,2,2);
            r(g,phase==2?0xe99b66:0xaf6977,x,y,-2,-39,1,1);
            r(g,phase==2?0xe99b66:0xaf6977,x,y,1,-39,1,1);
            r(g,OUT,x,y,0,-37,1,1);
            r(g,BONE,x,y,-3,-35,6,2);r(g,BONE_LIGHT,x,y,-2,-34,4,1);
            for(int i=-2;i<=2;i+=2)r(g,OUT,x,y,i,-35,1,1);
            r(g,EDGE,x,y,-5,-40,1,5);r(g,DARK,x,y,4,-40,1,5);
        }
        r(g,OUT,x,y,-2,-33,4,2);r(g,BONE,x,y,-1,-33,2,2);
        r(g,BONE_LIGHT,x,y,-1,-33,1,1);
    }
    static void body(Graphics g,int x,int y,int dir,int step,boolean walking,int clock,int pose,int time,int phase){
        boolean side=dir>=2,back=dir==0;int sign=dir==2?-1:1;
        int sway=swing(step,walking),shift=lean(dir,pose,time),tx=x+shift*2;
        int cape=phase==2?0x59313e:0x352b3e;
        // Split remnants keep the back's exposed spine visible.
        t(g,OUT,x,y,-8,-31,-12,-6,-5,-16);t(g,cape,x,y,-8,-30,-11,-8-sway/2,-6,-17);
        t(g,OUT,x,y,8,-31,12,-7,5,-16);t(g,cape,x,y,8,-30,11,-9+sway/2,6,-17);
        int cycle=walking?(int)(Math.sin(step*Math.PI/4)*3):0;
        if(side){
            leg(g,x,y,-2,-2-sign*cycle/2,-2-sign*cycle,cycle>0?2:0,sign);
            leg(g,x,y,2,2+sign*cycle/2,2+sign*cycle,cycle<0?2:0,sign);
        }else{
            leg(g,x,y,-4,-4+cycle/2,-4+cycle,cycle>0?2:0,-1);
            leg(g,x,y,4,4-cycle/2,4-cycle,cycle<0?2:0,1);
        }
        int bodyY=y+2*KnightArt.bob(step,walking);
        int weaponShoulder=side?sign*7:back?9:-9,otherShoulder=side?-sign*5:-weaponShoulder;
        int hx=localHandX(dir,pose,time),hy=localHandY(pose,time,step,walking);
        arm(g,tx,bodyY,otherShoulder,otherShoulder,-21-sway/2,false);
        chest(g,tx,bodyY,side,back,sign);pad(g,tx,bodyY,otherShoulder);
        arm(g,tx,bodyY,weaponShoulder,hx,hy,true);pad(g,tx,bodyY,weaponShoulder);
        skull(g,tx,bodyY,dir,phase);
    }
    static void fallen(Graphics g,int x,int y){
        t(g,0x352b3e,x,y,-22,-2,10,-5,17,0);
        bone(g,x,y,-20,-2,-12,-4);bone(g,x,y,-12,-4,-5,-3);
        r(g,OUT,x,y,-22,-4,5,4);r(g,STEEL,x,y,-21,-3,4,2);
        r(g,OUT,x,y,-14,-6,5,4);r(g,STEEL,x,y,-13,-5,3,2);
        r(g,OUT,x,y,-5,-7,13,7);r(g,STEEL,x,y,-5,-6,13,5);
        r(g,OUT,x,y,-2,-6,6,4);
        for(int i=0;i<3;i++)r(g,BONE,x,y,-1+i*2,-6,1,4);
        bone(g,x,y,3,-3,11,-1);bone(g,x,y,8,-5,15,-3);
        r(g,OUT,x,y,9,-10,11,8);r(g,STEEL,x,y,10,-9,9,6);
        r(g,LIGHT,x,y,10,-9,6,1);r(g,BONE,x,y,12,-8,7,5);
        r(g,BONE_LIGHT,x,y,12,-8,5,1);r(g,OUT,x,y,15,-7,2,2);
        r(g,OUT,x,y,17,-4,2,1);r(g,BONE_SHADE,x,y,13,-3,4,1);
    }
    private SkeletonBossArt(){}
}
