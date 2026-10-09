import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/** An original ruined refuge; stone and firelight are cached, embers animate. */
public final class BonfireArt {
    private static void r(Graphics g,int c,int x,int y,int w,int h){g.setColor(c);g.fillRect(x,y,w,h);}
    private static void line(Graphics g,int c,int x,int y,int a,int b){g.setColor(c);g.drawLine(x,y,a,b);}
    private static void tri(Graphics g,int c,int x,int y,int a,int b,int u,int v){g.setColor(c);g.fillTriangle(x,y,a,b,u,v);}
    public static Image create(){
        Image room=Image.createImage(320,240);Graphics g=room.getGraphics();
        r(g,0x0a101a,0,0,320,240);
        // Distant dead spires seen through the collapsed sanctuary wall.
        for(int y=20;y<83;y+=4)r(g,0x151d2a+((y-20)/16)*0x010101,0,y,320,4);
        g.setColor(0x57606b);g.fillArc(77,28,22,22,0,360);
        g.setColor(0x17212d);g.fillArc(84,25,21,23,0,360);
        for(int i=0;i<10;i++){
            int x=7+i*32,h=14+(i*19)%34;
            r(g,0x101925,x,84-h,23,h);tri(g,0x101925,x-3,85-h,x+11,66-h,x+26,85-h);
            r(g,0x23303b,x+3,86-h,1,h-3);
        }
        // The path widens toward the foreground. Warm reflected light surrounds the ember.
        int[] rows={80,89,101,115,132,153,178,206,240};
        for(int row=0;row<rows.length-1;row++){
            int y=rows[row],ey=rows[row+1],tw=24+row*4;
            for(int x=-tw;x<320;x+=tw){
                int xx=x+(row%2)*tw/2,k=(x+400+row*17)%5;
                int warmth=Math.max(0,22-Math.abs(xx+tw/2-148)/4-Math.abs(y-147)/3);
                int color=((25+k+warmth)<<16)|((31+k+warmth/2)<<8)|(37+k);
                r(g,color,xx+1,y+1,tw-2,ey-y-2);
                line(g,0x37434a,xx+2,y+1,xx+tw-4,y+1);
                line(g,0x101923,xx+2,ey-2,xx+tw-3,ey-2);
                if((row+x+400)%3==0){
                    line(g,0x131d27,xx+7,y+4,xx+11,y+(ey-y)/2);
                    line(g,0x131d27,xx+11,y+(ey-y)/2,xx+9,ey-3);
                }
                r(g,0x45505a,xx+4+(k*3),y+3,1,1);
            }
        }
        // Broken ashlar along the back, leaving an opening onto the ruined world.
        for(int side=0;side<2;side++){
            int start=side==0?0:216,end=side==0?72:320;
            for(int y=37;y<87;y+=12)for(int x=start;x<end;x+=25){
                int xx=x+(y/12%2)*7;
                if(side==0&&xx>57&&y<66)continue;
                r(g,0x252e36,xx+1,y+1,23,10);line(g,0x48505a,xx+2,y+1,xx+22,y+1);
                line(g,0x151e28,xx+23,y+2,xx+23,y+10);
            }
        }
        column(g,19,28,91);column(g,63,51,63);column(g,225,25,83);
        tri(g,0x404750,21,28,30,19,38,29);tri(g,0x313c43,65,51,71,43,79,51);
        // The way back to the opening encounter has collapsed permanently.
        r(g,0x080f19,252,42,44,55);g.setColor(0x080f19);g.fillArc(252,28,44,35,0,180);
        g.setColor(0x697079);g.drawArc(247,25,54,42,0,180);
        g.setColor(0x343f4c);g.drawArc(250,28,48,37,0,180);
        r(g,0x4a535f,247,44,5,55);r(g,0x79818a,247,44,1,54);
        r(g,0x3b4652,296,44,5,55);r(g,0x5e6873,297,44,1,54);
        for(int row=0;row<4;row++)for(int col=0;col<3;col++){
            int x=253+col*14+(row%2)*4,y=45+row*12;
            r(g,0x3b4149,x,y,12,10);line(g,0x70716e,x,y,x+10,y);line(g,0x181f2a,x+11,y+1,x+11,y+9);
        }
        for(int i=0;i<7;i++){
            int x=250+i*6,y=87+(i*5)%9;r(g,0x45474b,x,y,10,7);line(g,0x838076,x,y,x+7,y);
        }
        r(g,0x46515b,248,98,52,4);line(g,0x7c7e78,249,98,297,98);
        // Quiet remains of the chapel: a bench, broken urns and grass between stones.
        r(g,0x101822,35,145,48,8);r(g,0x424348,33,138,50,7);line(g,0x77716b,34,138,81,138);
        r(g,0x2e373d,37,144,6,12);r(g,0x2e373d,73,144,6,12);
        r(g,0x262a2c,105,84,10,16);r(g,0x57504c,106,85,3,13);r(g,0x786455,104,82,12,3);
        tri(g,0x0e1621,109,82,112,91,116,85);
        for(int i=0;i<38;i++){
            int x=i%2==0?5+(i*13)%33:287+(i*11)%30,y=88+(i*29)%123;
            r(g,0x111b25,x-2,y+3,9,3);r(g,0x3d4448,x,y,5,4);line(g,0x646769,x,y,x+3,y);
            line(g,0x263c33,x+3,y+3,x+5,y-3);line(g,0x3a4b3c,x+5,y-3,x+6,y-1);
        }
        line(g,0x0e1822,202,170,221,184);line(g,0x0e1822,221,184,214,203);
        line(g,0x40505a,203,171,219,183);
        // Low circular stones hold pale ash, charred wood and the planted nameless blade.
        g.setColor(0x11171e);g.fillArc(124,140,48,27,0,360);
        g.setColor(0x574b43);g.fillArc(127,142,42,21,0,360);
        g.setColor(0x27292b);g.fillArc(132,144,32,17,0,360);
        for(int i=0;i<10;i++){
            double a=i*Math.PI/5;int x=148+(int)(20*Math.cos(a)),y=152+(int)(9*Math.sin(a));
            r(g,0x171d24,x-4,y-2,8,5);r(g,0x79716b,x-3,y-2,6,3);r(g,0xada08b,x-3,y-2,4,1);
        }
        for(int i=0;i<30;i++)r(g,i%3==0?0xa29b87:0x676458,136+(i*7)%25,146+(i*11)%12,1,1);
        line(g,0x2b2223,137,157,159,146);line(g,0x6c4330,137,156,159,145);
        line(g,0x281f21,139,147,158,158);line(g,0x8b5035,139,147,158,157);
        return room;
    }
    private static void column(Graphics g,int x,int y,int h){
        r(g,0x111c27,x-2,y,17,h);r(g,0x38424c,x,y,12,h-5);
        r(g,0x657078,x+1,y+1,2,h-7);r(g,0x202e3a,x+9,y+1,2,h-7);
        for(int yy=y+15;yy<y+h-7;yy+=18){line(g,0x192630,x,yy,x+11,yy);line(g,0x4a565e,x+1,yy+1,x+10,yy+1);}
        r(g,0x46505a,x-4,y+h-6,21,5);line(g,0x818079,x-3,y+h-6,x+15,y+h-6);
    }
    public static void ambient(Graphics g,int tick){
        for(int i=0;i<5;i++){
            int y=116-(tick/180+i*11)%37,x=144+(i*9+tick/330)%21-10;
            line(g,0x394047,x,y,x+4,y);line(g,0x252f3a,x+3,y-2,x+7,y-2);
        }
        for(int i=0;i<9;i++){
            int y=145-(tick/70+i*9)%45,x=144+(i*13+tick/180)%19-7;
            r(g,i%3==0?0xe5a066:0x975c41,x,y,1,i%2+1);
        }
        // Sparse cold mist along the edges, outside the hero's standing area.
        for(int i=0;i<3;i++){int x=(i*109+tick/170)%345-25;line(g,0x34404b,x,191+i*8,x+14,191+i*8);}
    }
    public static void fire(Graphics g,int tick){
        int f=tick/95%4;
        tri(g,0x773d2d,135,151,146,121-f*2,156,152);
        tri(g,0x9e472d,145,153,156,127+f,162,151);
        tri(g,0xd77536,138,151,145,125+f*2,153,153);
        tri(g,0xf5b550,142,152,148,132-f,158,152);
        tri(g,0xffe6a2,144,153,149,141-f*2,154,153);
        r(g,0xfbc471,139,155,17,1);r(g,0xc76836,137,158,3,1);r(g,0xc76836,158,155,2,1);
        // Weathered sword in the ash: black edge, cold blade and warm hilt.
        line(g,0x091321,146,104,146,142);line(g,0x81929b,147,102,147,140);
        line(g,0xc0ced0,148,103,148,139);line(g,0x4a5966,149,104,149,139);
        line(g,0x6d5040,147,99,147,110);r(g,0xa57b4d,146,101,3,2);
        line(g,0x27212a,140,112,156,112);line(g,0xd2a36a,141,111,155,111);
        r(g,0xedcc87,147,110,2,3);
    }
    private BonfireArt(){}
}
