import org.microemu.*;
import org.microemu.device.*;
import org.microemu.device.impl.*;
import org.microemu.device.j2se.*;
import java.io.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
/** Uses MicroEmulator's real MIDP graphics implementation, no mocked drawing APIs. */
public class RenderTest implements EmulatorContext {
    J2SEDeviceDisplay display=new J2SEDeviceDisplay(this);
    J2SEFontManager fonts=new J2SEFontManager();
    J2SEInputMethod input=new J2SEInputMethod();
    public java.io.InputStream getResourceAsStream(String s){return getClass().getResourceAsStream(s);}
    public DisplayComponent getDisplayComponent(){return null;}
    public InputMethod getDeviceInputMethod(){return input;}
    public DeviceDisplay getDeviceDisplay(){return display;}
    public FontManager getDeviceFontManager(){return fonts;}
    public boolean platformRequest(String s){return false;}
    static void save(Art art,World w,String name)throws Exception{
        javax.microedition.lcdui.Image im=javax.microedition.lcdui.Image.createImage(320,240);
        art.draw(im.getGraphics(),w,false);int[] rgb=new int[320*240];im.getRGB(rgb,0,320,0,0,320,240);
        BufferedImage out=new BufferedImage(320,240,BufferedImage.TYPE_INT_RGB);out.setRGB(0,0,320,240,rgb,0,320);ImageIO.write(out,"png",new File("preview/"+name+".png"));
    }
    public static void main(String[] args)throws Exception{
        RenderTest ctx=new RenderTest();DeviceImpl dev=DeviceImpl.create(ctx,RenderTest.class.getClassLoader(),"org/microemu/device/resizable/device.xml",J2SEDevice.class);DeviceFactory.setDevice(dev);dev.init();
        new File("preview").mkdirs();Art art=new Art();World w=new World();save(art,w,"title");w.startStory();for(int i=0;i<3;i++){w.storyTime=i*Balance.STORY_SCENE_TIME+1800;w.clock=w.storyTime;save(art,w,"story-"+i);}w.reset();w.mode=World.FIGHT;w.px=176;w.py=169;w.bx=154;w.by=120;
        for(int i=0;i<4;i++){w.attack=i;w.lockAttack();w.bt=400;save(art,w,"attack-"+i);}
        w.bstate=World.RECOVER;w.pstate=World.HEAL;w.pt=450;w.hp=48;w.potions=2;save(art,w,"heal");
        w.phase=2;w.phaseBanner=1200;w.bossHp=320;w.bstate=World.TRANSITION;save(art,w,"phase-two");
        w.mode=World.WIN;w.deathTime=1000;save(art,w,"victory");w.mode=World.LOSE;save(art,w,"defeat");
        javax.microedition.lcdui.Image im=javax.microedition.lcdui.Image.createImage(320,240);art.controls(im.getGraphics());int[] rgb=new int[76800];im.getRGB(rgb,0,320,0,0,320,240);BufferedImage out=new BufferedImage(320,240,BufferedImage.TYPE_INT_RGB);out.setRGB(0,0,320,240,rgb,0,320);ImageIO.write(out,"png",new File("preview/controls.png"));
        System.out.println("Rendered 13 real MIDP frames successfully.");System.exit(0);
    }
}
