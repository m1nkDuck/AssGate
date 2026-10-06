import org.microemu.device.*;
import org.microemu.device.impl.*;
import org.microemu.device.j2se.*;
public class InputTest {
    static int checks;
    static void check(boolean b,String s){if(!b)throw new RuntimeException(s);checks++;System.out.println("PASS: "+s);}
    public static void main(String[] args)throws Exception{
        RenderTest ctx=new RenderTest();DeviceImpl dev=DeviceImpl.create(ctx,InputTest.class.getClassLoader(),"org/microemu/device/resizable/device.xml",J2SEDevice.class);DeviceFactory.setDevice(dev);dev.init();
        Game game=new Game(null,false);game.keyPressed('m');game.tick();game.keyReleased('m');
        game.keyPressed('j');game.tick();game.keyReleased('j');game.keyPressed('j');game.tick();game.keyReleased('j');for(int i=0;i<50;i++)game.tick();check(game.world.mode==World.FIGHT,"Title enters arena through intro");
        World w=game.world;w.bstate=World.TRANSITION;w.bt=-100000;w.px=160;w.py=120;w.bx=160;w.by=90;w.fy=-1;w.fx=0;
        game.keyPressed('j');for(int i=0;i<80;i++){game.keyRepeated('j');game.tick();}check(w.bossHp==696,"Holding and auto-repeat cause only ONE slash");game.keyReleased('j');game.keyPressed('j');for(int i=0;i<20;i++)game.tick();game.keyReleased('j');check(w.bossHp==672,"Release then press permits next slash");
        game.keyPressed('p');game.tick();game.keyReleased('p');float y=w.py;game.keyPressed('s');for(int i=0;i<10;i++)game.tick();check(w.py==y,"Pause freezes gameplay");game.keyReleased('s');game.keyPressed('p');game.tick();game.keyReleased('p');
        game.keyPressed('w');game.keyPressed('a');game.tick();check(w.px<160&&w.py<y,"Simultaneous keys move diagonally");game.keyReleased('w');game.keyReleased('a');
        game.keyPressed('q');game.tick();game.keyReleased('q');check(w.mode==World.TITLE,"Quit during fight returns to title");game.keyPressed('h');game.tick();game.keyReleased('h');game.keyPressed('j');game.tick();game.keyReleased('j');check(w.mode==World.TITLE,"Help returns to title");
        System.out.println("ALL "+checks+" INPUT CHECKS PASSED");System.exit(0);
    }
}
