import org.microemu.device.*;
import org.microemu.device.impl.*;
import org.microemu.device.j2se.*;
import javax.microedition.lcdui.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

/** Export the real MIDP renderer for visual review, including the hero size reference. */
public final class BossPreview {
    static void save(Image image,String name)throws Exception{
        int width=image.getWidth(),height=image.getHeight();int[] pixels=new int[width*height];
        image.getRGB(pixels,0,width,0,0,width,height);
        BufferedImage out=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
        out.setRGB(0,0,width,height,pixels,0,width);ImageIO.write(out,"png",new File("preview/"+name+".png"));
    }
    public static void main(String[] args)throws Exception{
        RenderTest context=new RenderTest();DeviceImpl device=DeviceImpl.create(context,BossPreview.class.getClassLoader(),"org/microemu/device/resizable/device.xml",J2SEDevice.class);DeviceFactory.setDevice(device);device.init();
        World w=new World();w.mode=World.FIGHT;w.bstate=World.TRANSITION;
        Image comparison=Image.createImage(320,160);Graphics g=comparison.getGraphics();g.setColor(0x11151f);g.fillRect(0,0,320,160);
        w.px=77;w.py=126;w.fx=0;w.fy=1;w.bx=213;w.by=126;
        new HeroSprites().draw(g,w,1);new BossSprites().draw(g,w,1,0,false);
        Art.text(g,"HERO 44 PX",57,137,0xc7b6a3,1);Art.text(g,"BOSS 88 PX",193,137,0xc7b6a3,1);
        save(comparison,"boss-hero-size");
        Art art=new Art();int[] dx={0,-1,1,0},dy={1,0,0,-1};
        for(int dir=0;dir<4;dir++)for(int pose=0;pose<10;pose++){
            w.reset();w.mode=World.FIGHT;w.px=77;w.py=195;w.fx=0;w.fy=1;w.bx=178;w.by=146;
            w.bdx=dx[dir];w.bdy=dy[dir];w.ox=w.bx;w.oy=w.by;w.attack=1;
            w.bstate=pose==0?World.TRANSITION:pose<=4?World.SEEK:pose==5?World.WARNING:pose==6?World.ACTIVE:pose==7?World.RECOVER:World.TRANSITION;
            w.clock=pose>=1&&pose<=4?(pose-1)*220:0;w.bt=pose==5?400:pose==6?100:0;
            if(pose==8)w.phase=2;if(pose==9){w.mode=World.WIN;w.deathTime=0;}
            Image frame=Image.createImage(320,240);art.draw(frame.getGraphics(),w,false);save(frame,"boss-game-"+dir+"-"+pose);
        }
        // Inspect real spawn and north edge as well as the story reveal.
        w.reset();Image spawn=Image.createImage(320,240);art.draw(spawn.getGraphics(),w,false);save(spawn,"boss-spawn");
        System.out.println("Exported boss/hero comparison, 40 arena poses and initial spawn.");System.exit(0);
    }
}
