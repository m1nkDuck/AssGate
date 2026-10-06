import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/** Damp underground masonry is cached; dim lamps, drips and water ripples animate. */
public final class ArenaArt {
    private static void r(Graphics g,int c,int x,int y,int w,int h){g.setColor(c);g.fillRect(x,y,w,h);}
    private static void line(Graphics g,int c,int x,int y,int a,int b){g.setColor(c);g.drawLine(x,y,a,b);}
    private static void tri(Graphics g,int c,int x,int y,int a,int b,int u,int v){g.setColor(c);g.fillTriangle(x,y,a,b,u,v);}
    private static void quad(Graphics g,int c,int x,int y,int a,int b,int u,int v,int p,int q){tri(g,c,x,y,a,b,u,v);tri(g,c,x,y,u,v,p,q);}
    public static Image create(){
        Image room=Image.createImage(320,240);Graphics g=room.getGraphics();
        r(g,0x0b1215,0,0,320,240);
        paving(g);rearWall(g);supports(g);pipes(g);debris(g);
        puddle(g,49,119,64,16,1);puddle(g,220,162,64,20,2);
        puddle(g,65,191,58,13,3);puddle(g,192,96,43,10,4);
        puddle(g,141,172,39,9,5);
        lamp(g,43,56);lamp(g,277,56);entry(g);
        return room;
    }
    private static void paving(Graphics g){
        r(g,0x0e191b,14,66,292,157);
        int[] rows={64,73,84,97,112,130,150,173,197,223};
        for(int row=0;row<rows.length-1;row++){
            int y=rows[row],ey=rows[row+1],tw=22+row*3,bw=tw+3;
            for(int col=-9;col<9;col++){
                int x=160+col*tw+(row%2)*tw/2,ex=160+col*bw+(row%2)*bw/2;
                int v=(row*13+(col+19)*7)%6;
                int light=Math.max(0,6-Math.abs((x+ex+tw)/2-160)/30);
                int c=((17+v+light)<<16)|((24+v+light)<<8)|(25+v+light);
                quad(g,c,x+1,y+1,x+tw-1,y+1,ex+bw-1,ey-2,ex+1,ey-2);
                line(g,0x303b3b,x+3,y+1,x+tw-3,y+1);
                line(g,0x0a1418,x+1,y+2,ex+1,ey-1);
                line(g,0x081216,ex+2,ey-1,ex+bw-2,ey-1);
                if((col+row+18)%3==0){
                    int cx=x+tw/3,cy=y+3;
                    line(g,0x091315,cx,cy,cx+7,y+(ey-y)/2);
                    line(g,0x091315,cx+7,y+(ey-y)/2,ex+tw/2-2,ey-3);
                    if(row>4)tri(g,0x0c1719,ex+3,ey-4,ex+10,ey-2,ex+1,ey-1);
                }
                if((col+row+18)%4==0)line(g,0x354344,x+tw/2,y+3,x+tw/2+4,y+3);
            }
        }
        // Dark damp seams, chipped corners and branching cracks break up the slabs.
        line(g,0x061113,72,93,89,110);line(g,0x061113,89,110,85,127);
        line(g,0x061113,85,127,99,138);line(g,0x061113,88,112,109,108);
        line(g,0x31403d,90,114,87,127);
        line(g,0x071215,244,178,229,185);line(g,0x071215,229,185,232,204);
        line(g,0x071215,229,185,210,190);
        for(int i=0;i<28;i++){
            int x=i%2==0?19+(i*11)%37:263+(i*13)%38,y=80+(i*29)%133;
            r(g,i%3==0?0x26362b:0x1c2c25,x,y,3+i%4,1);
            r(g,0x26382b,x+1,y-1,2,1);
        }
    }
    private static void rearWall(Graphics g){
        r(g,0x171e1f,13,26,294,51);
        for(int row=0;row<5;row++)for(int col=-1;col<12;col++){
            int x=col*28+(row%2)*14,y=27+row*10;
            int v=(row*7+(col+14)*3)%4;
            r(g,0x1b2423+v*0x010101,x+1,y+1,26,8);
            line(g,0x343c37,x+2,y+1,x+25,y+1);
            line(g,0x0b1417,x+27,y+2,x+27,y+9);
            if((col+row+12)%4==0)line(g,0x0a1517,x+9,y+2,x+16,y+8);
        }
        // A low barrel vault encloses the room, with no daylight openings.
        g.setColor(0x323a35);g.drawArc(16,3,288,116,0,180);
        g.setColor(0x232e2a);g.drawArc(21,8,278,109,0,180);
        g.setColor(0x111c1c);g.drawArc(27,12,266,103,0,180);
        for(int i=0;i<7;i++){
            int x=58+i*34,y=28+Math.abs(i-3)*2;
            line(g,0x0b1518,x,y,x+4,y+10);
            if(i%2==0)line(g,0x3c4338,x+1,y+1,x+3,y+8);
        }
        barredDoor(g);vent(g,55,37);vent(g,239,40);
        // Vertical wet stains and algae collect beneath the ceiling and vents.
        for(int i=0;i<18;i++){
            int x=23+(i*47)%279,y=30+(i*17)%18,len=8+(i*7)%21;
            r(g,0x10201c,x,y,3+i%4,len);
            line(g,0x24392d,x,y,x,y+len-3);
            if(i%3==0)line(g,0x345044,x+2,y+4,x+2,y+len-1);
        }
        line(g,0x080f13,15,76,305,76);line(g,0x313b30,18,74,301,74);
        for(int i=0;i<34;i++){
            int x=17+i*9,h=1+(i*7)%5;
            r(g,0x24392a,x,73-h,3+i%4,h+2);
            if(i%3==0)r(g,0x3a4430,x,72-h,2,1);
        }
        line(g,0x071116,86,31,90,43);line(g,0x071116,90,43,83,61);
        line(g,0x071116,90,43,107,48);line(g,0x071116,226,29,220,44);
        line(g,0x071116,220,44,227,64);line(g,0x38413a,224,31,220,41);
    }
    private static void barredDoor(Graphics g){
        r(g,0x0a1217,121,39,78,36);
        g.setColor(0x090f14);g.fillArc(121,28,78,38,0,180);
        g.setColor(0x45493e);g.drawArc(116,24,88,45,0,180);
        line(g,0x46463a,117,44,117,75);line(g,0x222c27,203,44,203,75);
        line(g,0x101b1b,119,46,119,74);line(g,0x28372e,200,46,200,74);
        for(int i=0;i<8;i++){
            int x=126+i*9,y=35+Math.abs(i-3)*2;
            line(g,0x1e2825,x,y,x,74);line(g,0x65513c,x+1,y+3,x+1,73);
            if(i==5){line(g,0x081216,x,56,x+1,64);line(g,0x68513b,x+1,55,x+4,63);}
            r(g,0x8b6341,x,47+(i*7)%19,2,2);
        }
        line(g,0x4d4436,124,51,196,51);line(g,0x252a25,124,52,196,52);
        line(g,0x584936,124,68,197,68);r(g,0x3a3028,156,55,6,7);
        r(g,0x8b724a,157,56,3,2);r(g,0x0b1517,158,59,1,2);
        r(g,0x2a3129,112,75,96,4);line(g,0x484737,113,75,206,75);
    }
    private static void vent(Graphics g,int x,int y){
        r(g,0x0a1117,x-2,y-2,31,18);r(g,0x343e35,x-2,y-2,31,2);
        r(g,0x0a1518,x,y,27,13);
        for(int i=0;i<5;i++){line(g,0x3d4538,x+3+i*5,y,x+3+i*5,y+12);r(g,0x684d34,x+3+i*5,y+5+i%3,1,2);}
        line(g,0x314638,x-1,y+14,x+28,y+14);
        line(g,0x1b322b,x+6,y+15,x+6,y+29);line(g,0x2f4940,x+7,y+17,x+7,y+27);
    }
    private static void supports(Graphics g){
        for(int side=0;side<2;side++){
            int x=side==0?1:304;
            r(g,0x080f15,x,27,15,195);r(g,0x242d2c,x+2,28,11,192);
            r(g,0x3b4238,x+2,29,2,190);r(g,0x111d1d,x+11,29,2,190);
            for(int i=0;i<10;i++){
                int y=30+i*19;r(g,0x0a1518,x+2,y+16,11,3);line(g,0x404438,x+3,y,x+11,y);
                if(i%3==1){line(g,0x091315,x+5,y+2,x+8,y+9);line(g,0x091315,x+8,y+9,x+6,y+15);}
                if(i%3==2)r(g,0x293c2b,x+3,y+10,4,6);
            }
            r(g,0x1d2c22,x,209,15,12);
        }
    }
    private static void pipes(Graphics g){
        // Corroded plumbing disappears into the side wall; one split joint leaks.
        line(g,0x070e12,19,29,81,29);line(g,0x070e12,19,30,81,30);
        line(g,0x534b3a,20,31,80,31);line(g,0x2c332b,20,32,80,32);
        r(g,0x806447,29,30,3,4);r(g,0x765437,64,30,3,4);
        r(g,0x111c1b,79,29,5,21);r(g,0x4d4634,80,32,2,14);r(g,0x2f4237,78,47,7,2);
        line(g,0x10171b,285,27,285,89);line(g,0x484231,287,29,287,86);
        line(g,0x242e26,289,28,289,88);r(g,0x71553a,286,48,5,3);
        r(g,0x223c2d,284,80,6,13);
        for(int i=0;i<15;i++){
            int y=35+i*3;line(g,i%2==0?0x5c5240:0x212c26,26+i%2,y,27+i%2,y+2);
        }
        r(g,0x342e24,24,80,6,5);r(g,0x746044,25,81,3,1);
    }
    private static void puddle(Graphics g,int x,int y,int w,int h,int seed){
        g.setColor(0x09171b);g.fillArc(x,y+2,w,h,0,360);
        g.setColor(0x0b1b1f);g.fillArc(x+3,y,w*2/3,h,0,360);
        g.setColor(0x102126);g.fillArc(x+w/3,y+3,w*2/3,h-1,0,360);
        tri(g,0x09171b,x+3,y+h/2,x-5,y+h/2+2,x+12,y+h-1);
        tri(g,0x102126,x+w-7,y+h/2,x+w+4,y+h/2+1,x+w-10,y+h-2);
        line(g,0x334843,x+8,y+2,x+w/3,y+2);
        line(g,0x1d3434,x+w/2,y+h,x+w-8,y+h-1);
        line(g,0x3c5050,x+w/2+seed,y+h/2,x+w/2+seed+7,y+h/2);
        line(g,0x223b3a,x+13,y+h/2+2,x+24,y+h/2+2);
    }
    private static void debris(Graphics g){
        for(int i=0;i<29;i++){
            int x=i%2==0?19+(i*17)%27:272+(i*13)%26,y=89+(i*31)%125;
            int w=3+(i*7)%6;
            r(g,0x071315,x-2,y+1,w+4,3);
            tri(g,0x353c32,x,y-3,x+w,y-1,x+1,y+2);
            line(g,0x555446,x,y-3,x+w-1,y-1);
            if(i%4==0)r(g,0x31432b,x,y-2,2,2);
        }
        barrel(g,29,158,false);barrel(g,288,199,true);
        quad(g,0x302f24,19,191,36,188,43,205,25,209);
        line(g,0x60543b,20,192,34,189);line(g,0x151f1a,28,190,34,207);
        line(g,0x48523a,23,195,38,202);line(g,0x1d2b24,282,111,299,117);
        line(g,0x525344,281,112,297,118);
        // Fallen timber and old iron fittings are scattered at the edges.
        quad(g,0x3a3426,273,135,299,128,301,132,276,140);
        line(g,0x66503a,275,135,297,129);r(g,0x201f1b,285,130,2,7);
        line(g,0x223326,23,171,32,175);line(g,0x2f4030,294,144,302,154);
    }
    private static void barrel(Graphics g,int x,int y,boolean broken){
        r(g,0x071215,x-11,y-3,24,5);
        g.setColor(0x18221d);g.fillArc(x-10,y-29,21,31,0,360);
        r(g,0x352f23,x-8,y-25,17,24);
        for(int i=0;i<4;i++){line(g,0x514533,x-7+i*4,y-24,x-7+i*4,y-2);line(g,0x1c211a,x-6+i*4,y-23,x-6+i*4,y-2);}
        r(g,0x27342e,x-9,y-21,20,2);r(g,0x455143,x-9,y-21,18,1);
        r(g,0x28372e,x-9,y-7,20,2);r(g,0x71593b,x-6,y-7,2,2);
        if(broken){tri(g,0x0a1517,x+1,y-27,x+8,y-25,x+4,y-9);line(g,0x6c5438,x+1,y-26,x+2,y-13);}
        else{g.setColor(0x56503c);g.drawArc(x-8,y-29,17,8,0,180);line(g,0x19271f,x-3,y-26,x+4,y-26);}
        r(g,0x2c422d,x-8,y-3,8,2);
    }
    private static void lamp(Graphics g,int x,int y){
        line(g,0x1a221c,x-5,y-6,x+6,y-6);line(g,0x625039,x-4,y-5,x+4,y-5);
        r(g,0x161e1c,x-5,y-3,10,12);r(g,0x554630,x-4,y-3,8,2);
        r(g,0x1e2820,x-4,y+6,8,2);line(g,0x705d3e,x-4,y,x-4,y+6);
        line(g,0x2c3126,x+3,y,x+3,y+6);r(g,0x202b22,x-2,y+8,4,2);
    }
    private static void entry(Graphics g){
        r(g,0x090f15,0,222,320,18);line(g,0x394036,18,221,302,221);
        r(g,0x060c12,141,207,38,33);r(g,0x222c27,134,202,7,38);
        r(g,0x434335,135,203,2,19);r(g,0x1e2924,179,202,7,38);
        r(g,0x363e30,180,203,2,19);r(g,0x2b3027,137,199,46,6);
        line(g,0x56513a,138,199,180,199);
        for(int i=0;i<7;i++)r(g,0x263b28,137+i*7,200+i%2,4,2);
    }
    public static void ambient(Graphics g,int clock){
        // Lantern light is sparse and subdued; hazards retain the brightest floor marks.
        int f=(clock/210)%3;
        r(g,0x695536,40,55,5,5);r(g,f==1?0xb49a68:0x9a8256,41,56,3,3);
        r(g,0x574a31,274,55,5,5);r(g,f==2?0xa48d5e:0x8c7950,275,56,3,3);
        drip(g,81,49,128,clock,0);drip(g,245,58,176,clock,530);
        ripple(g,83,129,clock,0);ripple(g,251,175,clock,420);ripple(g,94,199,clock,790);
        // A thin layer of cold floor mist remains behind actors and warning masks.
        for(int i=0;i<2;i++){
            int x=(i*173+clock/135)%370-35,y=181+i*22;
            line(g,0x243331,x,y,x+18,y);line(g,0x1c2d2c,x+8,y+1,x+31,y+1);
        }
    }
    private static void drip(Graphics g,int x,int start,int ground,int clock,int offset){
        int t=(clock+offset)%1600;
        if(t<1050){
            int y=start+(ground-start)*t*t/(1050*1050);
            r(g,0x405650,x,y,1,2);r(g,0x718077,x,y+2,1,1);
        }
    }
    private static void ripple(Graphics g,int x,int y,int clock,int offset){
        int age=(clock+offset)%1600;
        if(age>1050){
            int size=3+(age-1050)/55;
            g.setColor(age<1320?0x41544e:0x293e3b);
            g.drawArc(x-size/2,y-1,size,3+size/5,15,145);
            g.drawArc(x-size/2,y-1,size,3+size/5,195,115);
        }
    }
    private ArenaArt(){}
}
