import javax.microedition.lcdui.*;
import javax.microedition.media.Manager;
/** Input edge detection and a bounded accumulator keep combat timing independent of rendering. */
public final class Game extends Canvas implements Runnable {
    private final AshGate app;
    final World world=new World();
    private final Art art=new Art();
    private final Image frame=Image.createImage(320,240);
    private volatile boolean running=true,hidden=false,paused=false;
    private boolean help=false,sound=true;
    private int helpReturn=0;
    private int held=0,edges=0;
    private int lastTapDirection=0,pendingRoll=0;
    private long lastTapTime=0;
    private static final int U=1,D=2,L=4,R=8,A=16,H=64,P=128,HELP=256,QUIT=512,MUTE=1024;
    private Thread thread;
    public Game(AshGate app){this(app,true);}
    Game(AshGate app,boolean startThread){this.app=app;setFullScreenMode(true);if(startThread){thread=new Thread(this);thread.start();}}
    public synchronized void resumeApp(){hidden=false;held=edges=0;clearRollGesture();}
    public synchronized void suspendApp(){hidden=true;paused=world.mode!=World.TITLE;held=edges=0;clearRollGesture();}
    public void stop(){running=false;}
    protected void hideNotify(){suspendApp();}
    protected synchronized void showNotify(){hidden=false;held=edges=0;clearRollGesture();}
    private int map(int key){
        if(key>='A'&&key<='Z')key+=32;
        switch(key){case 'w':return U;case 's':return D;case 'a':return L;case 'd':return R;
            case '1':return U|L;case '3':return U|R;case '7':return D|L;case '9':return D|R;
            case '4':return L;case '6':return R;case '8':return D;
            case 'j':case '5':case 10:case 13:return A;
            case 'k':case ' ':case '0':return 0;
            case 'l':case '2':return H;
            case 'p':case -6:return P;case 'h':return HELP;case 'q':case -7:return QUIT;case 'm':return MUTE;
        }
        try{int a=getGameAction(key);if(a==UP)return U;if(a==DOWN)return D;if(a==LEFT)return L;if(a==RIGHT)return R;if(a==FIRE)return A;}catch(IllegalArgumentException e){}
        return 0;
    }
    private void clearRollGesture(){lastTapDirection=0;lastTapTime=0;pendingRoll=0;}
    protected synchronized void keyPressed(int code){keyPressedAt(code,System.currentTimeMillis());}
    synchronized void keyPressedAt(int code,long now){
        int b=map(code),fresh=b&~held;
        if((b&(U|D|L|R))!=0&&fresh==b&&world.mode==World.FIGHT&&!paused&&!help){
            long gap=now-lastTapTime;
            if(lastTapDirection==b&&gap>=0&&gap<=Balance.DOUBLE_TAP_TIME){
                pendingRoll=b;lastTapDirection=0;lastTapTime=0;
            }else{lastTapDirection=b;lastTapTime=now;}
        }
        edges|=fresh;held|=b;
    }
    protected synchronized void keyReleased(int code){held&=~map(code);}
    protected void keyRepeated(int code){} // Holding attack never emits another attack edge.
    private synchronized int consume(){int e=edges;edges=0;return e;}
    private synchronized int movement(){return held;}
    synchronized void tick(){
        int e=consume(),h=movement(),rollDirection=pendingRoll;pendingRoll=0;
        if(world.mode!=World.FIGHT){clearRollGesture();rollDirection=0;}
        if((e&MUTE)!=0){sound=!sound;world.soundOn=sound;}
        if(help){if((e&(A|HELP|QUIT))!=0){help=false;world.mode=helpReturn;}return;}
        if((e&HELP)!=0&&world.mode==World.TITLE){helpReturn=world.mode;help=true;return;}
        if((e&QUIT)!=0){if(world.mode==World.TITLE){if(app!=null)app.quit();return;}world.mode=World.TITLE;paused=false;clearRollGesture();return;}
        if((e&P)!=0&&world.mode!=World.TITLE){paused=!paused;clearRollGesture();rollDirection=0;}
        if(paused)return;
        if(world.mode==World.TITLE){
            if((e&U)!=0)world.menuIndex=(world.menuIndex+3)%4;
            if((e&D)!=0)world.menuIndex=(world.menuIndex+1)%4;
            if((e&A)!=0){
                if(world.menuIndex==0){world.startStory();held=edges=0;clearRollGesture();return;}
                if(world.menuIndex==1){helpReturn=World.TITLE;help=true;return;}
                if(world.menuIndex==2){sound=!sound;world.soundOn=sound;}
                if(world.menuIndex==3){if(app!=null)app.quit();return;}
            }
        }
        if((e&A)!=0&&world.mode==World.STORY){world.finishStory();held=edges=0;clearRollGesture();return;}
        if((e&A)!=0&&(world.mode==World.WIN||world.mode==World.LOSE)&&world.deathTime>Balance.DEATH_TIME){world.reset();held=edges=0;clearRollGesture();return;}
        int mx=((h&R)!=0?1:0)-((h&L)!=0?1:0),my=((h&D)!=0?1:0)-((h&U)!=0?1:0);
                if(rollDirection!=0){
            mx=((rollDirection&R)!=0?1:0)-((rollDirection&L)!=0?1:0);
            my=((rollDirection&D)!=0?1:0)-((rollDirection&U)!=0?1:0);
        }
        world.update(Balance.STEP,mx,my,(e&A)!=0,rollDirection!=0,(e&H)!=0);
        if(sound&&world.events!=0){try{
            int note=(world.events&World.HIT)!=0?42:(world.events&World.BOSS_HIT)!=0?68:(world.events&World.DRINK)!=0?81:(world.events&World.PHASE)!=0?38:(world.events&World.END)!=0?60:(world.events&World.TELEGRAPH)!=0?54:48;
            Manager.playTone(note,45,35);
        }catch(Exception ignored){}}
    }
    public void run(){
        long last=System.currentTimeMillis(),acc=0;
        while(running){long now=System.currentTimeMillis(),elapsed=now-last;last=now;
            if(hidden){acc=0;}else{acc+=Math.max(0,Math.min(132,elapsed));int steps=0;while(acc>=Balance.STEP&&steps<4){tick();acc-=Balance.STEP;steps++;}repaint();serviceRepaints();}
            try{Thread.sleep(8);}catch(InterruptedException ignored){}
        }
    }
    protected synchronized void paint(Graphics g){
        Graphics f=frame.getGraphics();if(help)art.controls(f);else art.draw(f,world,paused);
        g.setColor(0x090c12);g.fillRect(0,0,getWidth(),getHeight());
        int sx=0,sy=0;if(world.shake>0&&!paused){sx=(world.clock/33)%3-1;sy=(world.clock/66)%3-1;}
        g.drawImage(frame,(getWidth()-320)/2+sx,(getHeight()-240)/2+sy,Graphics.TOP|Graphics.LEFT);
    }
}
