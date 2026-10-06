import javax.microedition.midlet.MIDlet;
import javax.microedition.lcdui.Display;
public final class AshGate extends MIDlet {
    private Game game;
    public void startApp(){if(game==null)game=new Game(this);Display.getDisplay(this).setCurrent(game);game.resumeApp();}
    public void pauseApp(){if(game!=null)game.suspendApp();}
    public void destroyApp(boolean unconditional){if(game!=null)game.stop();}
    public void quit(){destroyApp(true);notifyDestroyed();}
}
