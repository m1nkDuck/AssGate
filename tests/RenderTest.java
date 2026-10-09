import org.microemu.*;
import org.microemu.device.*;
import org.microemu.device.impl.*;
import org.microemu.device.j2se.*;
import java.io.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
/** Uses MicroEmulator's real MIDP graphics implementation, no mocked drawing APIs. */
public class RenderTest implements EmulatorContext {
    static int rendered;
    J2SEDeviceDisplay display=new J2SEDeviceDisplay(this);
    J2SEFontManager fonts=new J2SEFontManager();
    J2SEInputMethod input=new J2SEInputMethod();
    public java.io.InputStream getResourceAsStream(String s){return getClass().getResourceAsStream(s);}
    public DisplayComponent getDisplayComponent(){return null;}
    public InputMethod getDeviceInputMethod(){return input;}
    public DeviceDisplay getDeviceDisplay(){return display;}
    public FontManager getDeviceFontManager(){return fonts;}
    public boolean platformRequest(String s){return false;}
    static int[] save(Art art,World w,String name)throws Exception{
        javax.microedition.lcdui.Image im=javax.microedition.lcdui.Image.createImage(320,240);
        art.draw(im.getGraphics(),w,false);int[] rgb=new int[320*240];im.getRGB(rgb,0,320,0,0,320,240);
        BufferedImage out=new BufferedImage(320,240,BufferedImage.TYPE_INT_RGB);out.setRGB(0,0,320,240,rgb,0,320);ImageIO.write(out,"png",new File("preview/"+name+".png"));
        rendered++;return rgb;
    }
    static void black(int[] rgb,String label){for(int c:rgb)if((c&0xffffff)!=0)throw new RuntimeException(label+" did not cover the full frame");System.out.println("PASS: "+label+" is completely black with no HUD or actor leaks.");}
    public static void main(String[] args)throws Exception{
        RenderTest ctx=new RenderTest();DeviceImpl dev=DeviceImpl.create(ctx,RenderTest.class.getClassLoader(),"org/microemu/device/resizable/device.xml",J2SEDevice.class);DeviceFactory.setDevice(dev);dev.init();
        new File("preview").mkdirs();Art art=new Art();World w=new World();save(art,w,"title");w.startStory();for(int i=0;i<3;i++){w.storyTime=i*Balance.STORY_SCENE_TIME+1800;w.clock=w.storyTime;save(art,w,"story-"+i);}w.reset();w.mode=World.FIGHT;w.px=176;w.py=169;w.bx=154;w.by=120;
        for(int i=0;i<4;i++){w.attack=i;w.lockAttack();w.bt=400;save(art,w,"attack-"+i);}
        w.bstate=World.RECOVER;w.pstate=World.HEAL;w.pt=450;w.hp=48;w.potions=2;save(art,w,"heal");
        w.phase=2;w.phaseBanner=1200;w.bossHp=320;w.bstate=World.TRANSITION;save(art,w,"phase-two");
        w.mode=World.WIN;w.deathTime=1000;save(art,w,"victory");w.mode=World.LOSE;save(art,w,"defeat");
        w.reset();w.mode=World.WIN;w.px=184;w.py=185;w.bx=154;w.by=140;w.bossHp=0;w.deathTime=0;save(art,w,"victory-upright");
        w.deathTime=Balance.VICTORY_WAIT-1;save(art,w,"victory-waiting");
        w.deathTime=Balance.VICTORY_WAIT;save(art,w,"victory-collapse-first");
        w.deathTime=Balance.VICTORY_WAIT+Balance.DEATH_TIME;save(art,w,"victory-collapse-last");
        w.deathTime=Balance.VICTORY_WAIT+Balance.DEATH_TIME+Balance.FADE_OUT_TIME/2;save(art,w,"victory-fade");
        w.deathTime=Balance.VICTORY_WAIT+Balance.DEATH_TIME+Balance.FADE_OUT_TIME;black(save(art,w,"victory-black"),"Victory fade-out");
        w.mode=World.LOSE;w.hp=0;w.deathTime=Balance.DEATH_TIME/2;save(art,w,"defeat-collapse");
        w.deathTime=Balance.DEATH_TIME+Balance.FADE_OUT_TIME/2;save(art,w,"defeat-fade");
        w.deathTime=Balance.DEATH_TIME+Balance.FADE_OUT_TIME;black(save(art,w,"defeat-black"),"Defeat fade-out");
        w.enterCamp(false);black(save(art,w,"camp-enter"),"Camp arrival fade-in");
        w.campTime=Balance.FADE_IN_TIME/2;save(art,w,"camp-fade-in");
        w.campTime=Balance.FADE_IN_TIME;save(art,w,"camp-wake-900");
        w.campTime=Balance.FADE_IN_TIME+Balance.DEATH_FRAME_TIME*3;save(art,w,"camp-wake-mid");
        w.campTime=Balance.CAMP_WAKE_TIME;save(art,w,"bonfire-after-defeat");
        w=new World();w.enterCamp(true);w.campTime=Balance.CAMP_WAKE_TIME;save(art,w,"bonfire-after-victory");
        int[] clean=save(art,w,"camp-clean");w.bstate=World.ACTIVE;w.attack=3;w.bossHp=0;w.bx=190;w.by=164;w.phase=2;w.phaseBanner=1500;
        int[] withCombat=save(art,w,"camp-combat-isolated");
        if(!java.util.Arrays.equals(clean,withCombat))throw new RuntimeException("Camp rendered boss, combat HUD or hazard state");
        System.out.println("PASS: Camp composite is independent of boss, hazards and combat HUD state.");
        javax.microedition.lcdui.Image im=javax.microedition.lcdui.Image.createImage(320,240);art.controls(im.getGraphics());int[] rgb=new int[76800];im.getRGB(rgb,0,320,0,0,320,240);BufferedImage out=new BufferedImage(320,240,BufferedImage.TYPE_INT_RGB);out.setRGB(0,0,320,240,rgb,0,320);ImageIO.write(out,"png",new File("preview/controls.png"));
        rendered++;System.out.println("Rendered "+rendered+" real MIDP frames successfully.");System.exit(0);
    }
}
