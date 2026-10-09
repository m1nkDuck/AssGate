import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import org.microemu.device.DeviceFactory;
import org.microemu.device.impl.DeviceImpl;
import org.microemu.device.j2se.J2SEDevice;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

/** Optional GIF frame exporter, using production MIDP scenery and actor renderers. */
public final class BelfryAnimationPreview {
    private static final int FRAME_TIME=120;
    private static final String[] DIRECTIONS={"DOWN","LEFT","RIGHT","UP"};
    private static String number(int value){return value<10?"00"+value:value<100?"0"+value:String.valueOf(value);}
    private static void save(Image image,String prefix,int index)throws Exception{
        int[] rgb=new int[320*240];image.getRGB(rgb,0,320,0,0,320,240);
        BufferedImage out=new BufferedImage(320,240,BufferedImage.TYPE_INT_RGB);out.setRGB(0,0,320,240,rgb,0,320);
        ImageIO.write(out,"png",new File("preview/"+prefix+number(index)+".png"));
    }
    private static String phase(int frame){return frame==0?"IDLE":frame<7?"WALK":frame<9?"WINDUP":frame<11?"STRIKE":frame==11?"HIT":"COLLAPSE";}
    private static void facing(Belfry.Enemy e,int row){e.dx=row==1?-1:row==2?1:0;e.dy=row==0?1:row==3?-1:0;}
    private static void enemy(Belfry.Enemy e,int row,int frame,boolean elite){
        e.x=elite?151:58;e.y=186;e.elite=elite;e.lit=row!=1;e.hp=elite?96:48;e.active=true;
        e.state=World.SEEK;e.time=e.hitTime=e.deathTime=0;e.moving=false;facing(e,row);
        if(frame>=1&&frame<7){e.moving=true;e.walkTime=(frame-1)*FRAME_TIME;}
        else if(frame>=7&&frame<9){e.state=World.WARNING;e.time=(frame-7)*400;}
        else if(frame>=9&&frame<11){e.state=World.ACTIVE;e.time=(frame-9)*140;}
        else if(frame==11){e.state=World.RECOVER;e.hitTime=135;}
        else if(frame>=12){e.hp=0;e.active=false;e.deathTime=(frame-12)*FRAME_TIME;}
    }
    private static void boss(World world,Bellbound b,int row,int frame){
        b.x=251;b.y=162;b.ox=b.x;b.oy=b.y;b.tx=b.x;b.ty=b.y+32;
        b.dx=row==1?-1:row==2?1:0;b.dy=row==0?1:row==3?-1:0;
        b.hp=Bellbound.MAX_HP;b.dead=false;b.phase=1;b.fallenBell=false;b.attack=Bellbound.SWEEP;b.time=400;b.state=World.SEEK;
        b.moving=false;b.walkTime=b.hitTime=b.ringIndex=0;
        if(frame>=1&&frame<7){b.moving=true;b.walkTime=(frame-1)*FRAME_TIME;b.time=b.walkTime;}
        else if(frame>=7&&frame<9){b.state=World.WARNING;b.time=(frame-7)*400;}
        else if(frame>=9&&frame<11){b.state=World.ACTIVE;b.time=(frame-9)*140;}
        else if(frame==11){b.state=World.RECOVER;b.hit(1);b.update(world,45);}
        else if(frame>=12){b.hp=0;b.dead=true;b.time=0;}
    }
    private static void labels(Graphics g,String caption){
        g.setColor(0x0a111c);g.fillRect(0,0,320,24);g.fillRect(0,213,320,27);
        Art.text(g,"ASHEN BELFRY - ACTUAL GAME SCALE",8,6,0xe5c7a5,1);
        Art.text(g,caption,8,16,0xa8b6c9,1);
        Art.text(g,"GUARD 44PX",37,218,0xc5b7a5,1);Art.text(g,"SPEAR 48PX",128,218,0xc5b7a5,1);Art.text(g,"BELLBOUND 70PX",226,218,0xc5b7a5,1);
        Art.text(g,"BODY / WEAPONS / FLOOR - PRODUCTION MIDP",8,230,0x8597aa,1);
    }
    private static void combined()throws Exception{
        Image floor=BelfryArt.create(4,7,false);World world=new World();Belfry b=world.belfry;b.room=4;b.fireMask=7;
        Belfry.Enemy guard=b.enemies[0],spearman=b.enemies[1];Bellbound bellbound=b.boss;
        for(int row=0;row<4;row++)for(int frame=0;frame<18;frame++){
            int index=row*18+frame,clock=index*FRAME_TIME;
            enemy(guard,row,frame,false);enemy(spearman,row,frame,true);boss(world,bellbound,row,frame);
            Image image=Image.createImage(320,240);Graphics g=image.getGraphics();g.drawImage(floor,0,0,Graphics.TOP|Graphics.LEFT);
            BelfryArt.ambient(g,b,clock);
            BellboundArt.draw(g,bellbound,clock,frame>=12?(frame-12)*FRAME_TIME:0);
            BelfryArt.guard(g,guard,clock);BelfryArt.guard(g,spearman,clock);
            labels(g,DIRECTIONS[row]+" / "+phase(frame)+(row==1?" / DARK":" / LIT"));
            save(image,"belfry-animation-",index);
        }
        extraMoves();
        System.out.println("Exported preview/belfry-animation-000.png through -091.png; 320x240, 120ms per frame.");
    }
    private static void extraMoves()throws Exception{
        Art art=new Art();World world=new World();world.belfry.enterRoom(world,4,true);world.px=-100;world.py=280;
        Belfry b=world.belfry;b.fireMask=7;b.entranceQuoteTime=0;
        for(int extra=0;extra<20;extra++){
            int index=72+extra;world.clock=index*FRAME_TIME;
            enemy(b.enemies[0],0,0,false);enemy(b.enemies[1],0,0,true);
            b.enemies[0].x=42;b.enemies[1].x=100;b.enemies[0].y=b.enemies[1].y=207;
            Bellbound bellbound=b.boss;boss(world,bellbound,0,0);String caption;
            if(extra<16){
                int stage=extra%8;bellbound.attack=extra<8?Bellbound.SLAM:Bellbound.TOLL;
                bellbound.tx=270;bellbound.ty=193;
                if(bellbound.attack==Bellbound.TOLL){bellbound.ox=Bellbound.CENTER_X;bellbound.oy=Bellbound.CENTER_Y;}
                bellbound.state=stage<3?World.WARNING:stage<5?World.ACTIVE:World.RECOVER;
                bellbound.time=stage<3?stage*(bellbound.attack==Bellbound.TOLL?300:400):stage<5?(stage-3)*110:(stage-5)*120;
                caption=(extra<8?"SLAM":"TOLL")+" / "+(stage<3?"WINDUP":stage<5?"STRIKE":"RECOVERY");
            }else{
                bellbound.phase=2;bellbound.hp=Bellbound.MAX_HP/2;bellbound.state=World.TRANSITION;bellbound.time=(extra-16)*360;
                caption="PHASE II / BRACE AND FALLING BELL";
            }
            b.hazardVersion++;bellbound.hazardVersion++;
            Image image=Image.createImage(320,240);Graphics g=image.getGraphics();art.draw(g,world,false);labels(g,caption);
            save(image,"belfry-animation-",index);
        }
    }
    private static int campFrame(Art art,World w,int index)throws Exception{
        Image image=Image.createImage(320,240);art.draw(image.getGraphics(),w,false);save(image,"bonfire-rest-animation-",index);return index+1;
    }
    private static void resting()throws Exception{
        Art art=new Art();World w=new World();w.enterCamp(true);w.campTime=Balance.CAMP_WAKE_TIME;w.clock=1800;
        int index=campFrame(art,w,0);w.update(1,0,0,true,false,false);index=campFrame(art,w,index);
        for(int i=0;i<6;i++){w.update(FRAME_TIME,0,0,false,false,false);index=campFrame(art,w,index);}
        if(w.restState!=World.REST_IDLE)throw new RuntimeException("Rest preview did not finish its actual sitting transition");
        for(int i=0;i<20;i++){w.update(FRAME_TIME,0,0,false,false,false);index=campFrame(art,w,index);}
        w.update(1,0,0,true,false,false);index=campFrame(art,w,index);
        for(int i=0;i<6;i++){w.update(FRAME_TIME,0,0,false,false,false);index=campFrame(art,w,index);}
        if(w.restState!=World.REST_NONE)throw new RuntimeException("Rest preview did not finish its actual rising transition");
        w.update(FRAME_TIME,0,0,false,false,false);index=campFrame(art,w,index);
        System.out.println("Exported preview/bonfire-rest-animation-000.png through -"+number(index-1)+".png; World-controlled sitting, breathing and rising.");
    }
    private static void run()throws Exception{
        RenderTest context=new RenderTest();DeviceImpl device=DeviceImpl.create(context,BelfryAnimationPreview.class.getClassLoader(),"org/microemu/device/resizable/device.xml",J2SEDevice.class);DeviceFactory.setDevice(device);device.init();
        new File("preview").mkdirs();combined();resting();System.exit(0);
    }
    public static void main(String[] args){try{run();}catch(Throwable error){error.printStackTrace();System.exit(1);}}
}
