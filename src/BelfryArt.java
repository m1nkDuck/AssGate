import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/** Five cached, single-screen ruins; scenery never darkens a combat warning. */
public final class BelfryArt {
    private static void r(Graphics g,int c,int x,int y,int w,int h){g.setColor(c);g.fillRect(x,y,w,h);}
    private static void l(Graphics g,int c,int x,int y,int a,int b){g.setColor(c);g.drawLine(x,y,a,b);}
    private static void t(Graphics g,int c,int x,int y,int a,int b,int u,int v){g.setColor(c);g.fillTriangle(x,y,a,b,u,v);}
    public static Image create(int room,int fires){
        return create(room,fires,true);
    }
    public static Image create(int room,int fires,boolean ceilingBell){
        Image image=Image.createImage(320,240);Graphics g=image.getGraphics();r(g,0x0b101b,0,0,320,240);
        sky(g,room);floor(g,room,fires);walls(g,room,fires,ceilingBell);details(g,room,fires);
        if(room<4){door(g,18,145,false);door(g,288,145,true);}
        else{g.setColor(0x5b4b49);g.drawArc(33,66,254,143,0,360);g.setColor(0x2a333c);g.drawArc(38,69,244,137,0,360);}
        return image;
    }
    private static void sky(Graphics g,int room){
        r(g,0x151c2a,0,25,320,59);
        for(int y=27;y<80;y+=4)r(g,0x121b29+((y-27)/13)*0x010101,0,y,320,4);
        for(int i=0;i<9;i++){
            int x=i*39-12,h=13+i*19%35;r(g,0x0b1522,x,82-h,26,h);
            t(g,0x0b1522,x-3,83-h,x+12,63-h,x+30,83-h);l(g,0x29313e,x+3,83-h,x+3,79);
        }
        if(room==0){
            r(g,0x30323e,169,27,24,53);r(g,0x161c29,173,34,16,30);
            t(g,0x30323e,164,29,181,19,198,29);r(g,0x797078,172,29,17,1);
            bell(g,181,46,14);
        }
        if(room==4)dawn(g,0,false);
    }
    private static void floor(Graphics g,int room,int fires){
        r(g,0x151d27,15,79,290,141);
        int[] rows={77,88,101,117,136,158,184,215};
        int fx=room==1?62:room==2?260:73,fy=room==1?115:room==2?105:100;
        boolean lit=room>=1&&room<=3&&(fires&(1<<(room-1)))!=0;
        for(int row=0;row<rows.length-1;row++)for(int col=-1;col<12;col++){
            int tw=25+row*3,x=col*tw+(row%2)*tw/2,y=rows[row],ey=rows[row+1];
            int grain=(col*7+row*11+30)%5;
            int light=lit?Math.max(0,26-(int)World.length(x+tw/2-fx,y+(ey-y)/2-fy)/3):0;
            int color=((22+grain+light*2)<<16)|((27+grain+light)<<8)|(33+grain+light/3);
            r(g,color,x+1,y+1,tw-2,ey-y-2);
            l(g,lit&&light>0?0x675246:0x3b414b,x+3,y+1,x+tw-4,y+1);
            l(g,0x0b131e,x+1,ey-2,x+tw-3,ey-2);
            if((col+row+12)%3==0){l(g,0x0b1420,x+6,y+4,x+13,y+(ey-y)/2);l(g,0x0b1420,x+13,y+(ey-y)/2,x+11,ey-3);}
            r(g,light>0?0x85705b:0x47515a,x+4+grain*3,y+4,1,1);
        }
        if(room==0)for(int i=0;i<5;i++){int y=89+i*24;l(g,0x5b5b60,22,y,298,y);l(g,0x101824,22,y+2,298,y+2);}
        for(int i=0;i<42;i++){
            int x=i%2==0?6+(i*17)%28:287+(i*11)%26,y=82+(i*29)%125;
            r(g,0x0b1320,x-2,y+3,9,3);r(g,0x414550,x,y,6,4);r(g,0x6a6463,x,y,4,1);
            if(i%3==0){l(g,0x314439,x+3,y+3,x+5,y-3);r(g,0x495442,x+5,y-3,2,1);}
        }
    }
    private static void walls(Graphics g,int room,int fires,boolean ceilingBell){
        int h=room==4?34:55;
        for(int row=0;row<h/10;row++)for(int col=-1;col<12;col++){
            int x=col*29+(row%2)*14,y=27+row*10;
            if(room==0&&x>105&&x<213)continue;
            if(room==0&&(row<2&&col%3!=0||row==2&&col%2==0))continue;
            if(room==2&&row<3&&x>25&&x<280)continue;
            if(room==4&&x>24&&x<287)continue;
            r(g,0x252c36+(col+row+16)%4*0x010101,x+1,y+1,27,8);l(g,0x454952,x+2,y+1,x+25,y+1);
            l(g,0x111a25,x+27,y+2,x+27,y+8);
        }
        for(int side=0;side<2;side++){
            int x=side==0?1:304;r(g,0x101a27,x,26,15,187);r(g,0x3a424c,x+2,27,11,183);
            r(g,0x697078,x+2,28,2,181);r(g,0x202b38,x+11,28,2,181);
            for(int y=45;y<211;y+=19){l(g,0x151f2b,x+2,y,x+12,y);l(g,0x505760,x+3,y+1,x+11,y+1);}
        }
        if(room==0||room==1){
            for(int i=0;i<3;i++){
                int x=40+i*98;r(g,0x0b121e,x,32,23,36);r(g,0x6b6567,x-2,32,2,37);
                t(g,0x323d49,x+2,35,x+16,48,x+3,53);t(g,0x323d49,x+18,53,x+20,63,x+8,65);
                l(g,0x889499,x+2,35,x+8,41);l(g,0x53606b,x+16,57,x+19,63);
                r(g,0x3c3d46,x-3,68,30,3);
            }
        }
        if(room==1&&(fires&1)!=0){
            // Scratches only become legible when this brazier burns, all toward the exit.
            for(int i=0;i<13;i++){int x=90+i*13,y=45+(i*11)%23;l(g,0x9b8b70,x,y,x+9,y-3);l(g,0x534b45,x+1,y+1,x+9,y-2);}
            for(int i=0;i<3;i++)l(g,0xb29c75,258+i*3,53,275+i*3,46);
        }
        if(room==3){
            for(int i=0;i<5;i++){int x=47+i*54;chain(g,x,28,x+(i%2==0?3:-3),91,0x5d5a58);r(g,0x75674f,x-4,75,9,6);}
            r(g,0x18212c,118,40,84,37);r(g,0x4d4849,122,42,76,2);r(g,0x3b3e48,133,48,54,20);
            for(int i=0;i<6;i++)l(g,0x777063,138+i*8,52,138+i*8,64);
        }
        if(room==4){
            chain(g,135,26,143,63,0x7c7976);chain(g,186,26,179,63,0x7c7976);
            if(ceilingBell){bell(g,161,64,58);l(g,0x111824,165,34,158,43);l(g,0x111824,158,43,169,56);}
        }
    }
    private static void details(Graphics g,int room,int fires){
        if(room==2){
            // The bell itself is drawn in depth order, so its two paths stay readable.
            l(g,0x7b6b59,119,103,111,96);l(g,0x7b6b59,201,176,210,180);
            for(int i=0;i<6;i++)r(g,0x525459,41+i*7,100,5,17);r(g,0x898174,43,99,41,2);
            for(int i=0;i<3;i++)l(g,0x272d35,48,104+i*4,77,104+i*4);
        }
        if(room>=1&&room<=3){
            int x=room==1?62:room==2?260:73,y=room==1?115:room==2?105:100;
            r(g,0x0c1520,x-10,y-3,21,8);r(g,0x57505a,x-6,y-15,12,17);r(g,0x8c7770,x-5,y-14,2,14);
            r(g,0x222533,x-10,y-18,21,5);l(g,0x797071,x-9,y-18,x+9,y-18);
            r(g,0x3d3030,x-7,y-21,15,4);
        }
        if(room==3){
            g.setColor(0x4e4846);g.drawArc(203,168,34,17,0,360);r(g,0x37302d,212,174,17,5);
            l(g,0x99704a,211,175,229,178);l(g,0x99704a,212,178,227,173);
        }
    }
    private static void door(Graphics g,int x,int y,boolean right){
        r(g,0x080f1b,x-5,y-35,17,38);r(g,0x4c515c,x-7,y-37,3,40);r(g,0x686771,x+12,y-37,2,40);
        r(g,0x75675d,x-7,y-39,21,3);g.setColor(0x8d8372);
        int sign=right?1:-1;g.drawLine(x-sign*3,y-10,x+sign*3,y-14);g.drawLine(x+sign*3,y-14,x-sign*3,y-18);
    }
    public static void chain(Graphics g,int x,int y,int ex,int ey,int color){
        int count=Math.max(Math.abs(ex-x),Math.abs(ey-y))/5;if(count<1)count=1;
        for(int i=0;i<=count;i++){int xx=x+(ex-x)*i/count,yy=y+(ey-y)*i/count;g.setColor(0x111721);g.drawRect(xx-1,yy-2,3,4);g.setColor(color);g.drawRect(xx,yy-1,2,2);}
    }
    public static void bell(Graphics g,int x,int y,int width){
        int h=width*3/5;
        g.setColor(0x0b121c);g.fillArc(x-width/2-2,y-h-2,width+4,h*2,0,180);
        g.setColor(0x4d4544);g.fillArc(x-width/2,y-h,width,h*2,0,180);
        t(g,0x736358,x-width/3,y-h/2,x-width/2,y,x+width/2,y);
        t(g,0x39363c,x+width/6,y-h+3,x+width/2,y,x,y);
        r(g,0x1b1e29,x-width/2-3,y-3,width+6,7);r(g,0x9d866a,x-width/2-2,y-3,width+4,2);
        r(g,0x5d514a,x-width/2-2,y+1,width+4,2);l(g,0xb4a184,x-width/4,y-h+4,x-width/3,y-5);
        r(g,0x282630,x-2,y+3,4,6);
    }
    public static void courtBell(Graphics g){
        g.setColor(0x0b121d);g.fillArc(122,132,76,24,0,360);bell(g,160,146,64);
        l(g,0x181d28,164,111,154,122);l(g,0x181d28,154,122,165,134);l(g,0x181d28,165,134,158,144);
        chain(g,183,122,210,163,0x72675f);
    }
    public static void dawn(Graphics g,int tick,boolean moving){
        int rise=moving?(int)(Math.sin(tick/850.0)*3):0;
        t(g,0x482b3c,36,55+rise,277,29+rise,300,37+rise);
        l(g,0xa25152,64,52+rise,282,34+rise);l(g,0xd17b65,99,49+rise,264,36+rise);
        if(moving){int breath=(int)(Math.sin(tick/600.0)*2);g.setColor(0x613741);g.fillArc(231-breath,40+rise,17+breath*2,12,0,360);r(g,0xc58b78,238,42+rise,2,1);}
    }
    public static void ambient(Graphics g,Belfry b,int tick){
        if(b.room==0||b.room==2||b.room==4)for(int i=0;i<12;i++){
            int x=(i*47+tick/45)%340-10,y=79+(i*29)%121;r(g,i%4==0?0x918988:0x555e69,x,y,2,1);
        }
        if(b.room==0){
            int sway=tick/480%2;r(g,0x080f1b,195,49+sway,4,14);r(g,0x192333,196,50+sway,2,4);
            l(g,0x0c1521,197,57+sway,202,52);l(g,0x53505a,202,37,202,70);
            // A small ember marks the chapter's first return point.
            r(g,0x41352f,49,167,17,4);l(g,0x907052,51,166,64,170);fire(g,58,163,tick);
        }
        if(b.room==3&&b.guardianDefeated){
            for(int i=0;i<4;i++){int x=50+i*54;chain(g,x,28,x+(tick/200%2==0?1:-1),90,0x8a8172);}
        }
        if(b.room==4)dawn(g,tick,b.fragment>0);
        if(b.room>=1&&b.room<=3&&(b.fireMask&(1<<(b.room-1)))!=0){
            int x=b.torchX(),y=b.torchY();fire(g,x,y-17,tick);
            for(int i=0;i<6;i++)r(g,i%2==0?0xd69b5c:0x8d634e,x-5+(i*7+tick/170)%13,y-18-(i*11+tick/90)%31,1,1);
        }
        if(b.room==2&&!b.flaskTaken){r(g,0x0b1521,67,176,11,12);r(g,0xba7845,69,178,7,9);r(g,0xefb65d,70,179,2,5);r(g,0xc4ad83,70,175,5,3);}
        if(b.room==3&&b.guardianDefeated){fire(g,220,175,tick);r(g,0x8cb5a8,284,132,12,2);}
    }
    private static void fire(Graphics g,int x,int y,int tick){
        int f=tick/100%3;t(g,0x97472f,x-6,y+2,x-2,y-15-f*2,x+5,y+2);
        t(g,0xde873e,x-4,y+3,x+2,y-11+f,x+7,y+3);t(g,0xffd779,x-2,y+3,x+1,y-5-f,x+4,y+3);
    }
    private static EnemySprites enemySprites;
    public static void guard(Graphics g,Belfry.Enemy e,int tick){
        int x=(int)e.x,y=(int)e.y,dir=Art.facing(e.dx,e.dy);g.setColor(0x0b1420);g.fillArc(x-11,y-3,22,7,0,360);
        if(enemySprites==null)enemySprites=new EnemySprites();
        if(enemySprites.draw(g,e,tick)){
            if(e.hp>0&&e.lit){r(g,0x3a2834,x-12,y-50,24,2);r(g,0xc18e64,x-12,y-50,24*e.hp/(e.elite?Balance.BELFRY_ELITE_HP:Balance.BELFRY_GUARD_HP),2);}
            return;
        }
        if(e.hp<=0){if(e.deathTime<Belfry.ENEMY_CORPSE_TIME)KnightArt.fallen(g,x,y);return;}
        if(e.lit){KnightArt.body(g,x,y,dir,e.walkTime/60%8,e.moving,tick,e.state==World.ACTIVE?World.ATTACK:World.IDLE,180);}
        else{
            r(g,0x1d2436,x-7,y-31,15,21);t(g,0x111a29,x-9,y-25,x-13,y-7,x+10,y-6);
            r(g,0x35394b,x-4,y-43,8,9);r(g,0x181f30,x-3,y-38,7,2);
            r(g,0x252c3f,x-7,y-10,5,10);r(g,0x252c3f,x+3,y-10,5,10);
            l(g,0x5b596a,x-6,y-30,x-6,y-22);r(g,0x51495c,x-11,y-30,6,3);r(g,0x51495c,x+7,y-30,6,3);
        }
        int sign=e.dx<0?-1:1,hx=x+sign*9,hy=y-21;
        int ex=hx+(int)(e.dx*(e.elite?50:25)),ey=hy+(int)(e.dy*(e.elite?38:24));
        if(e.state==World.WARNING){ex=hx+sign*12;ey=hy-24;}
        l(g,0x0b1320,hx-1,hy,ex-1,ey);l(g,e.lit?0xc5c4cf:0x777586,hx,hy,ex,ey);
        if(e.elite){l(g,0x8b7154,hx+1,hy+3,ex+1,ey+3);t(g,e.lit?0xe3d7c4:0x8c8491,ex,ey,ex-4,ey+7,ex+3,ey+6);}
        if(e.lit){r(g,0x3a2834,x-12,y-50,24,2);r(g,0xc18e64,x-12,y-50,24*e.hp/(e.elite?96:48),2);}
    }
    public static void campExit(Graphics g,boolean open){
        if(open){
            r(g,0x07101c,39,37,26,61);r(g,0x46535c,37,36,3,65);r(g,0x78837f,38,37,1,61);
            r(g,0x48555d,64,37,3,63);l(g,0x9aa193,38,36,65,36);
            for(int i=0;i<5;i++)l(g,0x3f4d52,40,76+i*5,63,76+i*5);
            r(g,0x9c9d83,48,50,7,1);r(g,0x9c9d83,51,47,1,7);
            r(g,0x111c28,34,100,35,4);r(g,0x77796e,34,100,34,1);
        }else{
            for(int i=0;i<7;i++){int x=37+(i*13)%27,y=86+(i*7)%18;r(g,0x3e454c,x,y,9,6);r(g,0x68696a,x,y,7,1);}
        }
    }
    private BelfryArt(){}
}
