import org.microemu.device.*;
import org.microemu.device.impl.*;
import org.microemu.device.j2se.*;
import javax.microedition.lcdui.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
public class DirectionTest {
    static long hash(Image im,int x,int y,int width,int height){int[] rgb=new int[width*height];im.getRGB(rgb,0,width,x,y,width,height);long h=1;for(int c:rgb)h=31*h+c;return h;}
    public static void main(String[] args)throws Exception{
        RenderTest ctx=new RenderTest();DeviceImpl dev=DeviceImpl.create(ctx,DirectionTest.class.getClassLoader(),"org/microemu/device/resizable/device.xml",J2SEDevice.class);DeviceFactory.setDevice(dev);dev.init();
        Art art=new Art();World w=new World();w.mode=World.FIGHT;w.moving=true;w.bstate=World.SEEK;
        int[] dx={0,0,-1,1},dy={-1,1,0,0};String[] names={"UP","DOWN","LEFT","RIGHT"};long[][] hashes=new long[2][4];
        for(int frame=0;frame<8;frame++){
            Image im=Image.createImage(320,240);Graphics g=im.getGraphics();g.setColor(0x171c27);g.fillRect(0,0,320,240);
            Art.text(g,"FOUR DIRECTIONS - WALK CYCLE",40,12,0xe8cb92,1);
            for(int dir=0;dir<4;dir++){
                int x=40+dir*80;Art.text(g,names[dir],x-10,34,0xbcc4ca,1);
                w.px=x;w.py=94;w.fx=dx[dir];w.fy=dy[dir];w.clock=frame*90;art.hero(g,w);
                w.bx=x;w.by=214;w.bdx=dx[dir];w.bdy=dy[dir];w.clock=frame*110;art.boss(g,w);
                if(frame==0){hashes[0][dir]=hash(im,x-30,50,60,55);hashes[1][dir]=hash(im,x-36,115,72,108);}
                else if(frame==2){if(hashes[0][dir]==hash(im,x-30,50,60,55)||hashes[1][dir]==hash(im,x-36,115,72,108))throw new RuntimeException("Walk cycle unchanged for "+names[dir]);}
            }
            Art.text(g,"KNIGHT",9,110,0x9ab0bd,1);Art.text(g,"BLACK OATHKEEPER",9,230,0xb5a4b0,1);
            int[] rgb=new int[76800];im.getRGB(rgb,0,320,0,0,320,240);BufferedImage out=new BufferedImage(320,240,BufferedImage.TYPE_INT_RGB);out.setRGB(0,0,320,240,rgb,0,320);ImageIO.write(out,"png",new File("preview/directions-"+frame+".png"));
        }
        for(int actor=0;actor<2;actor++)for(int i=0;i<4;i++)for(int j=i+1;j<4;j++)if(hashes[actor][i]==hashes[actor][j])throw new RuntimeException("Duplicate directional pose");
        // Exercise sword, drinking and roll for every facing through the real MIDP renderer.
        for(int dir=0;dir<4;dir++)for(int state=1;state<=4;state++){
            Image im=Image.createImage(320,240);w.px=100;w.py=180;w.bx=210;w.by=120;w.fx=dx[dir];w.fy=dy[dir];w.rollX=w.fx;w.rollY=w.fy;w.bdx=w.fx;w.bdy=w.fy;w.pstate=state;w.pt=170;w.bstate=World.ACTIVE;w.attack=dir;art.draw(im.getGraphics(),w,false);
        }
        System.out.println("PASS: Knight and boss have four distinct facings and alternating walk frames.");
        System.out.println("PASS: Sword, roll, healing and hurt render safely in every facing.");System.exit(0);
    }
}
