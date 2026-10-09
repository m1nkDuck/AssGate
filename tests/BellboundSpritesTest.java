import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import org.microemu.device.DeviceFactory;
import org.microemu.device.impl.DeviceImpl;
import org.microemu.device.j2se.J2SEDevice;
import java.io.File;

/** Production body poses and real native flail pixels share the gameplay timeline. */
public final class BellboundSpritesTest {
    static final int BACKGROUND=0x061119,X=120,GROUND=170;
    static int checks;
    static void check(boolean value,String message){
        if(!value)throw new RuntimeException(message);checks++;
    }
    static Image blank(){
        Image image=Image.createImage(320,240);Graphics g=image.getGraphics();
        g.setColor(BACKGROUND);g.fillRect(0,0,320,240);return image;
    }
    static Bellbound boss(int row){
        Bellbound b=new Bellbound();b.x=X;b.y=GROUND;b.state=World.RECOVER;b.time=400;
        b.dx=row==1?-1:row==2?1:0;b.dy=row==3?-1:row==0?1:0;return b;
    }
    static long hash(Image image){return ArenaArtTest.hash(image);}
    static int color(Image image,int x,int y){
        int[] pixel=new int[1];image.getRGB(pixel,0,1,x,y,1,1);return pixel[0]&0xffffff;
    }
    static Image draw(Bellbound b,int clock,int deathTime){
        Image image=blank();BellboundArt.draw(image.getGraphics(),b,clock,deathTime);return image;
    }
    static void selection(BellboundSprites sprites,Image atlas,Bellbound b,int clock,int death,int column){
        Image actual=blank(),expected=blank();
        check(sprites.draw(actual.getGraphics(),b,clock,death),"Bellbound atlas failed to load");
        int sign=Math.abs(b.dx)>Math.abs(b.dy)?(b.dx<0?-1:1):b.dy<0?-1:1;
        int lean=!b.dead&&(b.state==World.ACTIVE||b.state==World.RECOVER&&b.attack==Bellbound.SLAM)?sign*2:0;
        expected.getGraphics().drawRegion(atlas,column*112,BellboundSprites.directionRow(b)*112,112,112,0,X-56+lean+BellboundSprites.recoilX(b),GROUND-100,Graphics.TOP|Graphics.LEFT);
        check(hash(actual)==hash(expected),"Bellbound selected the wrong source body pose");
        int hx=BellboundSprites.handX(b,clock,death),hy=BellboundSprites.handY(b,clock,death),touching=0;
        for(int yy=hy-2;yy<=hy+2;yy++)for(int xx=hx-2;xx<=hx+2;xx++)if(color(actual,xx,yy)!=BACKGROUND)touching++;
        check(touching>=5,"Bellbound chain origin is detached from its drawn gauntlet");
    }
    static void atlas(Image atlas)throws Exception{
        check(atlas.getWidth()==1120&&atlas.getHeight()==448,"Bellbound needs 40 body-only cells");
        int[] pixels=new int[atlas.getWidth()*atlas.getHeight()];atlas.getRGB(pixels,0,atlas.getWidth(),0,0,atlas.getWidth(),atlas.getHeight());
        long[] directions=new long[4];
        for(int row=0;row<4;row++){
            long[] feet=new long[4];
            for(int col=0;col<10;col++){
                int minX=112,minY=112,maxX=-1,maxY=-1,count=0;
                for(int y=0;y<112;y++)for(int x=0;x<112;x++){
                    int alpha=pixels[(row*112+y)*1120+col*112+x]>>>24;
                    check(alpha==0||alpha==255,"Bellbound alpha must be binary for MIDP");
                    if(alpha==255){count++;minX=Math.min(minX,x);minY=Math.min(minY,y);maxX=Math.max(maxX,x);maxY=Math.max(maxY,y);}
                }
                check(count>=450,"A Bellbound body pose is empty");
                check(minX>0&&minY>0&&maxX<111&&maxY<111,"Bellbound body clips its atlas cell");
                check(maxY==100,"Bellbound loses its planted ground anchor");
                if(col==0)check(maxY-minY+1==Bellbound.HEIGHT,"Bellbound idle changes its established 70-pixel height");
                if(col>0&&col<5)check(maxY-minY+1>=64&&maxY-minY+1<=77,"Bellbound walking stretches its body");
                if(col==9)check(maxY-minY+1<46&&maxX-minX+1>45,"Bellbound fallen pose remains upright");
                Image sample=blank();sample.getGraphics().drawRegion(atlas,col*112,row*112,112,112,0,X-56,GROUND-100,Graphics.TOP|Graphics.LEFT);
                if(col==0)directions[row]=hash(sample);
                if(col>0&&col<5)feet[col-1]=DirectionTest.hash(sample,X-40,GROUND-31,80,33);
            }
            int distinct=0;
            for(int i=0;i<4;i++){boolean fresh=true;for(int j=0;j<i;j++)if(feet[j]==feet[i])fresh=false;if(fresh)distinct++;}
            check(distinct>=3,"Bellbound walk does not alternate its feet");
        }
        for(int a=0;a<4;a++)for(int b=a+1;b<4;b++)check(directions[a]!=directions[b],"Bellbound repeats a facing");
    }
    static void timeline(BellboundSprites sprites,Image atlas)throws Exception{
        for(int row=0;row<4;row++){
            Bellbound b=boss(row);check(BellboundSprites.directionRow(b)==row,"Bellbound faces the wrong direction");
            selection(sprites,atlas,b,0,0,0);
            b.state=World.SEEK;selection(sprites,atlas,b,2200,0,0);
            b.moving=true;
            for(int walk=0;walk<4;walk++){b.walkTime=walk*160;selection(sprites,atlas,b,2700,0,walk+1);}
            b.state=World.PREPARE;selection(sprites,atlas,b,0,0,5);
            b.state=World.WARNING;selection(sprites,atlas,b,2000,0,5);
            Image[] actions=new Image[3];
            for(int attack=0;attack<3;attack++){
                b.attack=attack;b.state=World.ACTIVE;b.time=80;selection(sprites,atlas,b,0,0,attack+6);
                actions[attack]=draw(b,0,0);ArenaArtTest.save(actions[attack],"bellbound-active-"+row+"-"+attack);
                b.state=World.RECOVER;b.time=239;selection(sprites,atlas,b,0,0,attack+6);
                b.time=240;selection(sprites,atlas,b,0,0,0);
            }
            for(int a=0;a<3;a++)for(int c=a+1;c<3;c++)check(hash(actions[a])!=hash(actions[c]),"Bellbound attack bodies repeat");
            b.state=World.TRANSITION;selection(sprites,atlas,b,0,0,8);
            b.dead=true;selection(sprites,atlas,b,0,0,8);selection(sprites,atlas,b,0,150,7);selection(sprites,atlas,b,0,320,9);
            ArenaArtTest.save(draw(b,0,320),"bellbound-fallen-"+row);
        }
    }
    static void geometry(){
        Bellbound b=boss(0);b.state=World.ACTIVE;b.attack=Bellbound.SLAM;b.tx=250;b.ty=187;
        Image slam=draw(b,0,0);check(color(slam,250,180)==0xffd5a0,"Native slam bell misses the locked target");
        b.tx=49;b.ty=205;Image moved=draw(b,0,0);
        check(color(moved,49,198)==0xffd5a0&&color(moved,250,180)==BACKGROUND,"Slam bell keeps the previous target after retargeting");
        b.attack=Bellbound.SWEEP;
        for(int row=0;row<4;row++)for(int time=0;time<=280;time+=140){
            b=boss(row);b.state=World.ACTIVE;b.attack=Bellbound.SWEEP;b.time=time;
            int sign=row==1||row==3?-1:1,cx=X+sign*2;
            float angle=(time/280f-0.5f)*2.7f,forward=(float)Math.cos(angle)*46,across=(float)Math.sin(angle)*46;
            int mx=cx+(int)(b.dx*forward-b.dy*across),my=GROUND-6+(int)(b.dy*forward+b.dx*across)*2/3;
            check(color(draw(b,0,0),mx,my-5)==0xffd5a0,"Native sweep bell no longer follows the timed arc");
        }
        b=boss(0);b.state=World.ACTIVE;b.attack=Bellbound.TOLL;
        check(color(draw(b,0,0),X+29,GROUND-41)==0xffd5a0,"Native toll bell is detached from its disclosed pose");
    }
    static void movement(){
        World w=new World();Bellbound b=boss(0);b.state=World.SEEK;b.time=0;
        w.px=X;w.py=GROUND+29;b.update(w,80);
        check(!b.moving&&b.walkTime==0&&BellboundSprites.frame(b,3000,0)==0,"Bellbound walks in place while waiting within attack range");
        check(hash(draw(b,0,0))==hash(draw(b,3300,0)),"Stationary Bellbound moves its body or weapon with the global clock");
        w.px=250;w.py=GROUND;b.time=0;
        long[] steps=new long[4];
        for(int step=0;step<4;step++){
            for(int tick=0;tick<5;tick++)b.update(w,32);
            check(b.moving&&b.walkTime==(step+1)*160%640,"Bellbound walk time must advance only during actual movement");
            check(BellboundSprites.frame(b,9999,0)==1+(step+1)%4,"Actual Bellbound movement selects the wrong step");
            steps[step]=hash(draw(b,5000,0));
        }
        for(int a=0;a<4;a++)for(int c=a+1;c<4;c++)check(steps[a]!=steps[c],"Actual moving Bellbound repeats a walk phase");
        check(b.state==World.SEEK,"Visual walking shortens the original seek window");b.update(w,16);
        check(b.state==World.PREPARE&&!b.moving,"Visual walking changed the original seek-to-prepare timing");
        b.state=World.SEEK;b.time=0;b.walkTime=240;w.px=b.x+20;w.py=b.y;b.update(w,16);
        check(!b.moving&&b.walkTime==240&&BellboundSprites.frame(b,9999,0)==0,"Stopping Bellbound fails to freeze the step clock and return to idle");
        b.x=34;b.y=111;b.time=0;w.px=-50;w.py=111;b.update(w,16);
        check(!b.moving&&b.walkTime==240,"Bellbound animates walking when the arena boundary blocks all movement");
        b.moving=true;b.hitTime=120;int walkTime=b.walkTime,hitTime=b.hitTime,time=b.time;
        long paused=hash(draw(b,100,0));b.update(w,0);
        check(b.moving&&b.walkTime==walkTime&&b.hitTime==hitTime&&b.time==time&&paused==hash(draw(b,9100,0)),"A paused tick advances Bellbound pose, recoil or clocks");
        b.reset();check(!b.moving&&b.walkTime==0&&b.hitTime==0,"Bellbound retry retains movement or hit feedback");
    }
    static void hitFeedback(BellboundSprites sprites,Image atlas)throws Exception{
        World w=new World();Bellbound b=boss(0);b.state=World.WARNING;b.attack=Bellbound.SLAM;b.time=123;b.tx=250;b.ty=187;
        int state=b.state,time=b.time,version=b.hazardVersion;float x=b.x,y=b.y,tx=b.tx,ty=b.ty;
        boolean hazard=b.inHazard(tx,ty,false);Image before=draw(b,0,0);int grip=BellboundSprites.handX(b,0,0);
        b.hit(12);
        check(b.hp==Bellbound.MAX_HP-12&&b.hitTime==180,"A real Bellbound hit does not begin short feedback");
        check(b.state==state&&b.time==time&&b.hazardVersion==version&&b.x==x&&b.y==y&&b.tx==tx&&b.ty==ty&&b.inHazard(tx,ty,false)==hazard,"Hit feedback changes attack timing, world position or disclosed geometry");
        check(hash(before)!=hash(draw(b,0,0))&&BellboundSprites.handX(b,0,0)==grip-2,"Bellbound body and chain grip do not recoil together on hit");
        selection(sprites,atlas,b,0,0,5);
        check(color(before,X+32,GROUND-53)==color(draw(b,0,0),X+32,GROUND-53),"Hit recoil displaces the native warning bell endpoint");
        b.hit(0);b.hit(-1);check(b.hitTime==180&&b.hp==Bellbound.MAX_HP-12,"Nonpositive hits refresh the visual timer or damage the boss");
        b.update(w,0);check(b.hitTime==180,"Paused Bellbound hit feedback expires");
        b.update(w,100);check(b.hitTime==80&&b.state==state&&BellboundSprites.handX(b,0,0)==grip-1,"Bellbound hit recoil does not settle during its existing warning");
        b.update(w,79);check(b.hitTime==1,"Bellbound feedback expires before its final millisecond");
        b.update(w,1);check(b.hitTime==0&&b.state==state&&hash(before)==hash(draw(b,0,0)),"Expired Bellbound feedback leaves a body shift, spark or altered attack");
        b.state=World.ACTIVE;b.time=80;b.hit(1);
        check(color(draw(b,0,0),250,180)==0xffd5a0,"Hit feedback moves the live slam away from its locked target");
        ArenaArtTest.save(draw(b,0,0),"bellbound-hit-feedback");
        b.hit(Bellbound.MAX_HP);check(b.dead&&!b.moving&&b.hitTime==0,"Death retains walking or live hit feedback over the collapse");
        b.hit(1);b.update(w,100);check(b.hitTime==0,"A dead Bellbound restarts hit feedback");
    }
    static void runTests()throws Exception{
        RenderTest context=new RenderTest();DeviceImpl device=DeviceImpl.create(context,BellboundSpritesTest.class.getClassLoader(),"org/microemu/device/resizable/device.xml",J2SEDevice.class);DeviceFactory.setDevice(device);device.init();
        new File("preview").mkdirs();Image atlas=Image.createImage(BellboundSpritesTest.class.getResourceAsStream("/bellbound-atlas.png"));
        atlas(atlas);BellboundSprites sprites=new BellboundSprites();timeline(sprites,atlas);geometry();movement();hitFeedback(sprites,atlas);
        System.out.println("PASS: Bellbound has 40 binary-alpha body poses, four facings, planted 70px idle and articulated walking.");
        System.out.println("PASS: Windup, sweep, slam, toll, phase brace, recovery and collapse select the production atlas.");
        System.out.println("PASS: Native slam target, timed sweep arc and toll bell retain their exact gameplay coordinates.");
        System.out.println("PASS: Actual movement drives walking; stops, blocked movement, pauses and retries preserve the correct visual clocks.");
        System.out.println("PASS: Real hits produce 180ms body/grip recoil and sparks without changing combat state, timing or mace targets.");System.exit(0);
    }
    public static void main(String[] args){try{runTests();}catch(Throwable error){error.printStackTrace();System.exit(1);}}
}
