import javax.microedition.lcdui.Graphics;

/** Hunched, chained plate armour and a bell-core flail, drawn directly in MIDP. */
public final class BellboundArt {
    public static final int OUT=0x11131e,STEEL=0x56536d,DARK=0x2d2b40;
    public static final int EDGE=0x85839c,LIGHT=0xb9beca,BRONZE=0x9a8060;
    private static final int RUST=0x594335,CLOTH=0x302d38,CHAIN=0xada48e;
    private static void r(Graphics g,int c,int x,int y,int w,int h){g.setColor(c);g.fillRect(x,y,w,h);}
    private static void t(Graphics g,int c,int x,int y,int a,int b,int u,int v){g.setColor(c);g.fillTriangle(x,y,a,b,u,v);}
    private static void line(Graphics g,int c,int x,int y,int a,int b,int width){
        g.setColor(c);for(int i=-width/2;i<=width/2;i++)g.drawLine(x+i,y,a+i,b);
    }
    private static void plateLimb(Graphics g,int x,int y,int ex,int ey,int width){
        line(g,OUT,x,y,ex,ey,width+2);line(g,STEEL,x,y,ex,ey,width);
        line(g,LIGHT,x-width/2,y+1,ex-width/2,ey-1,0);
        line(g,DARK,x+width/2,y+1,ex+width/2,ey,0);
    }
    private static void chain(Graphics g,int x,int y,int ex,int ey){
        line(g,OUT,x,y,ex,ey,2);
        int length=(int)World.length(ex-x,ey-y),count=Math.max(1,length/5);
        for(int i=0;i<=count;i++){
            int px=x+(ex-x)*i/count,py=y+(ey-y)*i/count;
            if((i&1)==0){r(g,CHAIN,px-1,py-2,3,4);r(g,DARK,px,py-1,1,2);}
            else{r(g,EDGE,px-2,py-1,4,2);r(g,LIGHT,px-1,py-1,2,1);}
        }
    }
    private static void knee(Graphics g,int x,int y){
        r(g,OUT,x-5,y-3,11,7);r(g,STEEL,x-4,y-3,9,6);
        r(g,LIGHT,x-3,y-3,6,1);r(g,EDGE,x-4,y-2,2,3);
        t(g,DARK,x,y-1,x+4,y-2,x+2,y+3);r(g,RUST,x+2,y+1,2,1);
    }
    private static void boot(Graphics g,int x,int y,int sign){
        r(g,OUT,x-5,y-5,12,6);r(g,STEEL,x-4,y-5,10,4);
        r(g,EDGE,x-4,y-4,4,2);r(g,LIGHT,x-3,y-5,5,1);
        r(g,DARK,x-4,y-1,10,1);r(g,OUT,x+(sign<0?-6:5),y-3,2,3);
    }
    private static void leg(Graphics g,int hx,int hy,int kx,int ky,int fx,int fy,int sign){
        plateLimb(g,hx,hy,kx,ky,7);knee(g,kx,ky);
        plateLimb(g,kx,ky+3,fx,fy-5,6);
        r(g,EDGE,fx-2,fy-11,2,4);r(g,RUST,fx+2,fy-8,1,2);boot(g,fx,fy,sign);
    }
    private static void pad(Graphics g,int x,int y,boolean near){
        int width=near?17:14;
        r(g,OUT,x-width/2+3,y-5,width-6,2);r(g,OUT,x-width/2+1,y-3,width-2,2);
        r(g,OUT,x-width/2,y-1,width,7);r(g,STEEL,x-width/2+1,y-3,width-2,8);
        r(g,LIGHT,x-width/2+3,y-3,width-6,1);r(g,EDGE,x-width/2+1,y-1,3,4);
        t(g,DARK,x+2,y-1,x+width/2-1,y,x+width/2-2,y+5);
        r(g,OUT,x-width/2+1,y+5,width-2,2);r(g,EDGE,x-width/2+3,y+5,width-6,1);
        r(g,RUST,x-2,y,2,1);r(g,DARK,x-1,y+1,2,1);
    }
    private static void mace(Graphics g,int x,int y,boolean bright){
        // A flared iron bell shell encloses an ember, with a heavy toothed lip.
        r(g,OUT,x-3,y-14,7,3);r(g,BRONZE,x-2,y-14,5,2);
        r(g,OUT,x-5,y-11,11,3);r(g,STEEL,x-4,y-11,9,4);
        t(g,OUT,x-5,y-10,x-10,y,x+10,y);
        t(g,OUT,x-5,y-10,x+5,y-10,x+10,y);
        t(g,STEEL,x-4,y-10,x-8,y-1,x+8,y-1);
        t(g,STEEL,x-4,y-10,x+4,y-10,x+8,y-1);
        t(g,EDGE,x-3,y-9,x-6,y-2,x-3,y-2);
        r(g,LIGHT,x-3,y-10,3,1);r(g,DARK,x+3,y-7,2,5);
        r(g,OUT,x-2,y-7,5,6);r(g,bright?0xe1a47b:0x826348,x-1,y-6,3,4);
        r(g,bright?0xffd5a0:BRONZE,x,y-5,1,2);
        r(g,OUT,x-10,y-1,21,4);r(g,BRONZE,x-9,y,19,1);r(g,DARK,x-8,y+1,17,1);
        r(g,OUT,x-8,y+2,3,2);r(g,OUT,x+6,y+2,3,2);
        r(g,EDGE,x-8,y-1,2,1);r(g,RUST,x+5,y-2,2,1);
    }
    private static void fallen(Graphics g,int x,int y){
        t(g,CLOTH,x-30,y-3,x+22,y-4,x+31,y+1);
        plateLimb(g,x-25,y-2,x-10,y-5,7);knee(g,x-16,y-3);
        r(g,OUT,x-8,y-15,25,14);r(g,STEEL,x-7,y-14,23,11);
        r(g,EDGE,x-6,y-13,11,2);r(g,DARK,x+6,y-12,8,8);
        r(g,OUT,x+14,y-12,14,10);r(g,STEEL,x+15,y-11,12,8);
        r(g,LIGHT,x+16,y-11,6,1);r(g,OUT,x+21,y-8,5,2);
        chain(g,x-4,y-12,x+18,y-2);mace(g,x-30,y,false);
    }
    private static final BellboundSprites sprites=new BellboundSprites();
    /** Native fallback keeps the same footprint and the same grip as the atlas. */
    private static void body(Graphics g,Bellbound b,int x,int y,int cx,int cy,
                             boolean side,boolean back,int sign,int cycle,int half,int hx,int hy){
        // Bent knees and a raised armoured back establish the heavy, hunched silhouette.
        t(g,OUT,cx-14,cy-50,cx-24,cy-6,cx+11,cy-14);
        t(g,CLOTH,cx-13,cy-48,cx-21,cy-8,cx+8,cy-15);
        r(g,DARK,cx-15,cy-31,2,12);r(g,0x4a3d4b,cx-18,cy-14,3,3);
        leg(g,x-8,y-27,x-12-cycle/2,y-15,x-11-cycle,y-(cycle>0?1:0),-1);
        leg(g,x+7,y-26,x+12+cycle/2,y-14,x+11+cycle,y-(cycle<0?1:0),1);
        // The cuirass has an arched upper back and overlapping, damaged lower plates.
        r(g,OUT,cx-half+5,cy-69,half*2-10,2);
        r(g,OUT,cx-half+2,cy-67,half*2-4,6);
        r(g,OUT,cx-half,cy-61,half*2+1,27);
        r(g,STEEL,cx-half+1,cy-64,half*2-1,28);
        t(g,EDGE,cx-half+2,cy-63,cx-1,cy-66,cx-half+5,cy-42);
        r(g,LIGHT,cx-half+6,cy-66,side?9:17,1);
        t(g,DARK,cx+2,cy-62,cx+half-1,cy-60,cx+half-3,cy-37);
        r(g,EDGE,cx-half+3,cy-52,3,10);r(g,OUT,cx-half+1,cy-39,half*2-2,2);
        r(g,STEEL,cx-half+4,cy-36,half*2-8,7);r(g,EDGE,cx-half+5,cy-36,half*2-11,1);
        r(g,OUT,cx-12,cy-30,25,6);r(g,DARK,cx-11,cy-29,23,4);
        r(g,BRONZE,cx-3,cy-29,6,3);r(g,OUT,cx-1,cy-28,2,1);
        t(g,OUT,cx-11,cy-24,cx-1,cy-24,cx-6,cy-16);
        t(g,STEEL,cx-10,cy-23,cx-2,cy-23,cx-6,cy-18);
        t(g,OUT,cx+1,cy-24,cx+11,cy-24,cx+6,cy-16);
        t(g,DARK,cx+2,cy-23,cx+10,cy-23,cx+6,cy-18);
        r(g,EDGE,cx-9,cy-23,6,1);r(g,EDGE,cx+3,cy-23,6,1);
        // Two chains bind the curved back and breastplate; links are individually lit.
        if(back){chain(g,cx-11,cy-62,cx+7,cy-36);chain(g,cx+10,cy-62,cx-7,cy-37);}
        else{chain(g,cx-half+4,cy-59,cx+6,cy-36);chain(g,cx+half-3,cy-57,cx-5,cy-34);}
        r(g,DARK,cx-half+7,cy-55,3,1);r(g,RUST,cx-half+8,cy-54,2,1);
        r(g,EDGE,cx+half-5,cy-42,2,2);
        int farX=cx-sign*(side?10:19),nearX=cx+sign*(side?12:19),sy=cy-55;
        plateLimb(g,farX,sy,farX-sign*2,cy-40,6);
        plateLimb(g,farX-sign*2,cy-40,farX-sign,cy-30-cycle/2,5);pad(g,farX,sy,false);
        int elbowX=nearX+sign*4,elbowY=hy<cy-40?cy-46:cy-39;
        plateLimb(g,nearX,sy,elbowX,elbowY,7);knee(g,elbowX,elbowY);
        plateLimb(g,elbowX,elbowY+2,hx,hy,6);pad(g,nearX,sy,true);
        r(g,OUT,hx-4,hy-3,9,7);r(g,STEEL,hx-3,hy-3,7,5);r(g,EDGE,hx-3,hy-3,4,1);
        // The head sits low in the gorget, below the domed back, with a sealed visor.
        int he=cx+(side?sign*7:0),hat=cy-63;
        r(g,OUT,he-5,hat,11,2);r(g,OUT,he-7,hat+2,15,11);
        r(g,STEEL,he-6,hat+2,13,10);r(g,LIGHT,he-4,hat+1,8,1);
        r(g,EDGE,he-5,hat+3,2,4);r(g,DARK,he+3,hat+3,3,7);
        if(back){r(g,EDGE,he-1,hat+3,2,7);r(g,OUT,he-4,hat+10,9,1);}
        else{
            int vx=side?(sign>0?he:he-5):he-5;
            r(g,OUT,vx,hat+6,side?6:11,2);r(g,b.phase==2?0xe4b27c:0xa78363,vx+2,hat+6,side?1:2,1);
            if(!side)r(g,b.phase==2?0xe4b27c:0xa78363,he+3,hat+6,1,1);
            r(g,EDGE,he-2,hat+9,4,1);r(g,OUT,he-1,hat+10,2,2);
        }
        r(g,OUT,he-5,hat+12,11,2);r(g,EDGE,he-4,hat+12,8,1);
    }
    public static void draw(Graphics g,Bellbound b,int clock){draw(g,b,clock,0);}
    public static void draw(Graphics g,Bellbound b,int clock,int deathTime){
        int x=(int)b.x,y=(int)b.y;
        if(b.dead){
            if(sprites.draw(g,b,clock,deathTime)){
                chain(g,BellboundSprites.handX(b,clock,deathTime),BellboundSprites.handY(b,clock,deathTime),x-30,y-13);
                mace(g,x-30,y,false);
            }else fallen(g,x,y);
            return;
        }
        boolean side=Math.abs(b.dx)>Math.abs(b.dy),back=!side&&b.dy<0;
        int sign=side?(b.dx<0?-1:1):back?-1:1;
        boolean walking=b.state==World.SEEK&&b.moving;
        int step=b.walkTime/160%4;
        int cycle=walking?(step==0?-3:step==2?3:0):0,bob=walking&&cycle!=0?1:0;
        int lean=(b.state==World.ACTIVE||b.state==World.RECOVER&&b.attack==Bellbound.SLAM)?sign*2:0;
        int cx=x+lean,cy=y+bob,half=side?13:18;
        int recoil=BellboundSprites.recoilX(b),bodyX=cx+recoil;
        int hx=bodyX+sign*19,hy=cy-30;
        if(b.state==World.PREPARE||b.state==World.WARNING){hx=bodyX+sign*20;hy=cy-48;}
        else if(b.state==World.ACTIVE){hx=bodyX+sign*22;hy=cy-34;}
        else if(b.state==World.TRANSITION){hx=bodyX+sign*23;hy=cy-42;}
        if(sprites.draw(g,b,clock,deathTime)){
            hx=BellboundSprites.handX(b,clock,deathTime);hy=BellboundSprites.handY(b,clock,deathTime);
        }else body(g,b,x+recoil,y,bodyX,cy,side,back,sign,cycle,half,hx,hy);
        int mx=cx+sign*31,my=cy-5;
        boolean bright=b.state==World.ACTIVE||b.attack==Bellbound.TOLL&&(b.state==World.WARNING);
        if(b.attack==Bellbound.SLAM&&(b.state==World.ACTIVE||b.state==World.RECOVER)){
            mx=(int)b.tx;my=(int)b.ty-2;
        }else if(b.state==World.PREPARE||b.state==World.WARNING){
            mx=cx+sign*32;my=cy-48;
        }else if(b.state==World.ACTIVE&&b.attack==Bellbound.SWEEP){
            float p=Math.min(1,b.time/280f),angle=(p-0.5f)*2.7f;
            float forward=(float)Math.cos(angle)*46,across=(float)Math.sin(angle)*46;
            mx=cx+(int)(b.dx*forward-b.dy*across);my=cy-6+(int)(b.dy*forward+b.dx*across)*2/3;
        }else if(b.attack==Bellbound.TOLL&&(b.state==World.ACTIVE)){
            mx=cx+sign*27;my=cy-36;
        }
        chain(g,hx,hy,mx,my-13);mace(g,mx,my,bright);
        if(b.hitTime>0){
            int sparkX=bodyX-sign*9,sparkY=cy-48;
            g.setColor(b.hitTime>90?0xe7c9a1:0x9d866b);
            g.drawLine(sparkX-3,sparkY-1,sparkX+3,sparkY+1);
            g.drawLine(sparkX,sparkY-3,sparkX,sparkY+3);
            g.drawLine(sparkX-2,sparkY+2,sparkX+2,sparkY-2);
        }
    }
    /** Draw before actors. During the disclosed phase change the bell visibly falls. */
    public static void fallenBell(Graphics g,Bellbound b){
        boolean falling=b.state==World.TRANSITION&&!b.fallenBell;
        if(!falling&&!b.fallenBell)return;
        int x=Bellbound.CENTER_X,y=Bellbound.CENTER_Y;
        if(falling){
            int progress=Math.min(1100,Math.max(0,b.time));
            y-=74-progress*74/1100;
            chain(g,x-14,37,x-14,y-32);chain(g,x+14,37,x+14,y-32);
        }
        r(g,OUT,x-7,y-39,15,4);r(g,BRONZE,x-6,y-39,13,2);
        r(g,OUT,x-15,y-35,31,6);r(g,0x756956,x-14,y-34,29,7);
        t(g,OUT,x-15,y-31,x-25,y-3,x+25,y-3);
        t(g,OUT,x-15,y-31,x+15,y-31,x+25,y-3);
        t(g,0x68615b,x-14,y-30,x-23,y-4,x+23,y-4);
        t(g,0x68615b,x-14,y-30,x+14,y-30,x+23,y-4);
        t(g,0x9b917c,x-12,y-29,x-19,y-6,x-11,y-6);
        t(g,0x3c3b43,x+8,y-28,x+19,y-6,x+8,y-6);
        r(g,EDGE,x-8,y-31,15,1);r(g,OUT,x-25,y-5,51,7);
        r(g,BRONZE,x-24,y-4,49,2);r(g,DARK,x-23,y-1,47,2);
        // Fractured seams and broken chain ends identify the new solid obstacle.
        line(g,OUT,x+2,y-29,x-2,y-17,1);line(g,OUT,x-2,y-17,x+5,y-8,1);
        r(g,RUST,x-15,y-13,3,2);r(g,BRONZE,x-15,y-14,2,1);
        if(!falling){chain(g,x-14,y-34,x-29,y-10);chain(g,x+14,y-34,x+27,y-15);}
    }
    private BellboundArt(){}
}
