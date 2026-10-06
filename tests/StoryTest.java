import org.microemu.device.*;
import org.microemu.device.impl.*;
import org.microemu.device.j2se.*;
public class StoryTest {
    static int checks;
    static void check(boolean b,String s){if(!b)throw new RuntimeException(s);checks++;System.out.println("PASS: "+s);}
    static void press(Game g,int key){g.keyPressed(key);g.tick();g.keyReleased(key);}
    public static void main(String[] args)throws Exception{
        RenderTest ctx=new RenderTest();DeviceImpl dev=DeviceImpl.create(ctx,StoryTest.class.getClassLoader(),"org/microemu/device/resizable/device.xml",J2SEDevice.class);DeviceFactory.setDevice(dev);dev.init();
        Game g=new Game(null,false);press(g,'m');
        press(g,'s');check(g.world.menuIndex==1,"Menu selects controls");press(g,'j');press(g,'j');check(g.world.mode==World.TITLE,"Controls returns to menu");
        press(g,'s');boolean before=g.world.soundOn;press(g,'j');check(g.world.soundOn!=before&&g.world.mode==World.TITLE,"Sound option toggles without starting game");press(g,'j');
        press(g,'w');press(g,'w');press(g,'j');check(g.world.mode==World.STORY,"Confirm journey enters story before arena");
        for(int i=0;i<40;i++)g.tick();check(g.world.mode==World.STORY&&g.world.hp==100&&g.world.bossHp==720,"Combat remains frozen during story");
        int time=g.world.storyTime;press(g,'p');for(int i=0;i<20;i++)g.tick();check(g.world.storyTime==time,"Pause freezes cinematic timer");press(g,'p');
        press(g,'j');check(g.world.mode==World.INTRO&&g.world.py==220,"Skip leads to doorway introduction");for(int i=0;i<50;i++)g.tick();check(g.world.mode==World.FIGHT,"Doorway completes into boss fight");
        press(g,'q');g.world.menuIndex=0;g.suspendApp();g.resumeApp();press(g,'j');check(g.world.mode==World.STORY,"Returning from background does not freeze title");
        press(g,'q');check(g.world.mode==World.TITLE,"Cancel cinematic returns to main menu");press(g,'j');check(g.world.storyTime==0,"New journey restarts story from first scene");
        g.world.storyTime=4490;g.tick();check(g.world.storyTime/4500==1,"First scene advances to second");g.world.storyTime=8990;g.tick();check(g.world.storyTime/4500==2,"Second scene advances to third");
        g.world.storyTime=13490;g.tick();check(g.world.mode==World.INTRO&&g.world.storyTime==0,"Full cinematic auto-completes into doorway");
        g.world.mode=World.LOSE;g.world.deathTime=1000;g.world.hp=0;g.world.potions=0;press(g,'j');check(g.world.mode==World.INTRO&&g.world.hp==100&&g.world.potions==3,"Retry resets fight and avoids repeating cinematic");
        System.out.println("ALL "+checks+" STORY/MENU CHECKS PASSED");System.exit(0);
    }
}
