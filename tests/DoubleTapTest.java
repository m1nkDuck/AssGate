import org.microemu.device.*;
import org.microemu.device.impl.*;
import org.microemu.device.j2se.*;
public class DoubleTapTest {
    static int count;
    static void check(boolean b,String s){if(!b)throw new RuntimeException("FAIL: "+s);count++;System.out.println("PASS: "+s);}
    static Game arena(){Game g=new Game(null,false);g.keyPressed('m');g.tick();g.keyReleased('m');World w=g.world;w.mode=World.FIGHT;w.py=160;w.px=160;w.bstate=World.TRANSITION;w.bt=-100000;return g;}
    static void tap(Game g,int key,long time){g.keyPressedAt(key,time);g.keyReleased(key);}
    public static void main(String[] args)throws Exception{
        RenderTest ctx=new RenderTest();DeviceImpl dev=DeviceImpl.create(ctx,DoubleTapTest.class.getClassLoader(),"org/microemu/device/resizable/device.xml",J2SEDevice.class);DeviceFactory.setDevice(dev);dev.init();
        Game g=arena();tap(g,'d',1000);g.tick();check(g.world.pstate==World.IDLE,"One movement tap does not roll");tap(g,'d',1200);g.tick();check(g.world.pstate==World.ROLL&&g.world.rollX==1&&g.world.rollY==0,"Double tap rolls right even if released before tick");
        g=arena();g.keyPressedAt('w',1000);g.tick();for(int i=0;i<20;i++){g.keyRepeated('w');g.keyPressedAt('w',1020+i*10);g.tick();}check(g.world.pstate==World.IDLE&&g.world.py<160,"Holding and repeated presses only move");
        g=arena();tap(g,'a',1000);tap(g,'a',1281);g.tick();check(g.world.pstate==World.IDLE,"Late second tap is rejected");
        g=arena();tap(g,'a',1000);tap(g,'d',1100);g.tick();check(g.world.pstate==World.IDLE,"Different directions do not count as a pair");
        g=arena();tap(g,'a',1000);tap(g,'a',1280);g.tick();check(g.world.pstate==World.ROLL&&g.world.rollX==-1,"Double tap includes 280ms boundary");
        g=arena();tap(g,'1',1000);tap(g,'1',1100);g.tick();check(g.world.pstate==World.ROLL&&g.world.rollX<0&&g.world.rollY<0,"Double tap diagonal key rolls diagonally");
        g=arena();tap(g,'s',1000);tap(g,'s',1100);g.tick();check(g.world.rollY==1,"Down double tap rolls down");
        g=arena();tap(g,'w',1000);tap(g,'w',1100);g.tick();check(g.world.rollY==-1,"Up double tap rolls up");
        for(int i=0;i<12;i++)g.tick();tap(g,'w',1200);tap(g,'w',1300);g.tick();check(g.world.pstate==World.IDLE,"Double tap still obeys roll cooldown");
        g=arena();g.world.hp=50;g.keyPressed('l');g.tick();g.keyReleased('l');tap(g,'w',1000);tap(g,'w',1100);g.tick();check(g.world.pstate==World.HEAL,"Double tap cannot interrupt drinking");
        g=arena();g.keyPressed('j');g.tick();g.keyReleased('j');tap(g,'w',1000);tap(g,'w',1100);g.tick();check(g.world.pstate==World.ATTACK,"Double tap cannot cancel sword recovery");
        g=arena();g.keyPressed('k');g.tick();g.keyReleased('k');g.keyPressed('0');g.tick();g.keyReleased('0');g.keyPressed(' ');g.tick();g.keyReleased(' ');check(g.world.pstate==World.IDLE,"Old roll buttons no longer roll");
        g=arena();tap(g,'w',1000);g.keyPressed('p');g.tick();g.keyReleased('p');g.keyPressed('p');g.tick();g.keyReleased('p');tap(g,'w',1100);g.tick();check(g.world.pstate==World.IDLE,"Pause clears unfinished double tap");
        g=arena();tap(g,'w',1000);g.suspendApp();g.resumeApp();g.keyPressed('p');g.tick();g.keyReleased('p');tap(g,'w',1100);g.tick();check(g.world.pstate==World.IDLE,"Backgrounding clears unfinished double tap");
        g=arena();g.world.mode=World.TITLE;tap(g,'s',1000);tap(g,'s',1100);g.tick();check(g.world.pstate==World.IDLE&&g.world.mode==World.TITLE,"Menu presses do not roll");
        System.out.println("ALL "+count+" DOUBLE-TAP CHECKS PASSED");System.exit(0);
    }
}
