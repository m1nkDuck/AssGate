import org.microemu.device.*;
import org.microemu.device.impl.*;
import org.microemu.device.j2se.*;

/** Chapter progression uses real sword windows, guard AI, collision and context actions. */
public final class BelfryTest {
    static int checks;
    static void check(boolean b,String s){if(!b)throw new RuntimeException("FAIL: "+s);checks++;System.out.println("PASS: "+s);}
    static World chapter(int room){World w=new World();w.oathkeeperDefeated=w.oathkeeperComplete=true;w.belfry.start(w);if(room!=0)w.belfry.enterRoom(w,room,true);return w;}
    static void press(Game g,int key){g.keyPressed(key);g.tick();g.keyReleased(key);}
    static void ticks(World w,int duration){for(int left=duration;left>0;){int dt=Math.min(left,Balance.STEP);w.update(dt,0,0,false,false,false);left-=dt;}}
    static void settle(World w){int budget=2000;while(w.pstate!=World.IDLE&&w.mode==World.BELFRY){w.update(Balance.STEP,0,0,false,false,false);if((budget-=Balance.STEP)<0)throw new RuntimeException("Hero never recovered");}}
    static void action(World w,float x,float y){settle(w);w.px=x;w.py=y;w.update(1,0,0,true,false,false);}
    static void right(World w){action(w,291,145);}
    static void left(World w){action(w,29,145);}
    static void fire(World w){action(w,w.belfry.torchX(),w.belfry.torchY());}
    static void swordPoint(World w,float x,float y,float dx,float dy){w.px=x-dx*26;w.py=y-dy*26;w.fx=dx;w.fy=dy;}
    static void strikeTwice(World w,Belfry.Enemy e){swordPoint(w,e.x,e.y,0,-1);w.belfry.strike(w);w.belfry.strike(w);}
    static void defeat(World w,Belfry.Enemy e){
        int budget=10000,start=w.hitsLanded;
        while(e.active&&w.mode==World.BELFRY){
            swordPoint(w,e.x,e.y,e.dx,e.dy);
            w.update(Balance.STEP,0,0,w.pstate==World.IDLE,false,false);
            if((budget-=Balance.STEP)<0)throw new RuntimeException("Guard survived the scripted real sword windows");
        }
        check(!e.active&&e.hp==0&&w.mode==World.BELFRY&&w.hitsLanded>start,"Actual sword windows defeat the "+(e.elite?"elite":"guard")+" without changing its health or difficulty");
    }
    static boolean safe(World w,Bellbound b,float x,float y){
        if(x<21||x>299||y<75||y>211||w.belfry.solid(x,y,Balance.PLAYER_RADIUS))return false;
        for(int yy=-5;yy<=5;yy++)for(int xx=-5;xx<=5;xx++)if(xx*xx+yy*yy<=25&&b.inHazard(x+xx,y+yy,false))return false;
        return true;
    }
    static void completeBoss(World w){
        Bellbound b=w.belfry.boss;int budget=120000;
        int[][] directions={{-1,0},{1,0},{0,-1},{0,1},{-1,-1},{1,-1},{-1,1},{1,1}};
        while(w.mode==World.BELFRY&&!b.dead){
            boolean positioned=false;
            for(int i=0;i<directions.length;i++){
                float n=World.length(directions[i][0],directions[i][1]),dx=directions[i][0]/n,dy=directions[i][1]/n;
                float x=b.x+dx*27,y=b.y+dy*27;
                if(safe(w,b,x,y)){w.px=x;w.py=y;w.fx=-dx;w.fy=-dy;positioned=true;break;}
            }
            if(!positioned){w.px=29;w.py=75;}
            w.update(Balance.STEP,0,0,positioned&&w.pstate==World.IDLE,false,false);
            if((budget-=Balance.STEP)<0)throw new RuntimeException("Full chapter boss progression timed out");
        }
        check(b.dead&&w.mode==World.DAWN&&w.hp>0,"Scripted safe attack positions finish the real 600-HP boss with its AI and phase obstacle enabled");
    }
    static void gating(){
        World w=new World();w.mode=World.CAMP;w.campTime=Balance.CAMP_WAKE_TIME;action(w,54,87);
        check(w.mode==World.CAMP&&!w.oathkeeperComplete,"The chapter does not start before the opening encounter resolves");
        w.enterCamp(false);w.campTime=Balance.CAMP_WAKE_TIME;action(w,54,87);
        check(w.mode==World.BELFRY&&w.belfry.room==0&&w.px==40&&w.py==145&&!w.oathkeeperDefeated,"Oathkeeper defeat still unlocks the next chapter through the left camp exit");
        w.belfry.fireMask=3;w.belfry.flaskTaken=true;w.enterCamp(false);w.reset();
        check(w.mode==World.CAMP&&w.oathkeeperComplete&&!w.oathkeeperDefeated&&w.belfry.fireMask==3&&w.belfry.flaskTaken,"Camp returns and reset cannot repeat the first boss or discard chapter progress");
        w.startStory();check(!w.oathkeeperComplete&&!w.oathkeeperDefeated&&w.belfry.fireMask==0&&!w.belfry.flaskTaken&&w.belfry.fragment==0,"A newly chosen journey clears all chapter progress and the opening completion flag");
        w.enterCamp(true);w.campTime=Balance.CAMP_WAKE_TIME;action(w,54,87);
        check(w.mode==World.BELFRY&&w.oathkeeperComplete&&w.oathkeeperDefeated,"Oathkeeper victory opens the same onward route as defeat");
    }
    static void entranceQuote(){
        World w=chapter(0);Belfry f=w.belfry;
        check(f.entranceQuoteSeen&&f.entranceQuoteTime==1800,"The first step into the opening room starts its shorter 1.8-second quote");
        w.update(1799,0,0,false,false,false);check(f.entranceQuoteTime==1,"The entrance quote lasts through its final millisecond");
        w.update(1,0,0,false,false,false);check(f.entranceQuoteTime==0,"The entrance quote expires at 1.8 seconds, before the first guard arrives");
        f.enterRoom(w,1,true);f.enterRoom(w,0,false);
        check(f.entranceQuoteTime==0,"Returning from the corridor does not repeat the entrance quote");
        left(w);w.campTime=Balance.CAMP_WAKE_TIME;action(w,54,87);
        check(w.mode==World.BELFRY&&f.entranceQuoteTime==0,"Returning from the bonfire does not repeat the entrance quote");
        w.hp=1;w.takeDamage(1);w.update(Balance.DEATH_TIME+Balance.FADE_OUT_TIME+Balance.BLACK_HOLD_TIME,0,0,false,false,false);
        w.update(Balance.CAMP_WAKE_TIME,0,0,false,false,false);
        check(w.mode==World.BELFRY&&f.room==0&&f.entranceQuoteTime==0&&f.entranceQuoteSeen,"Respawning and waking in the room do not repeat the quote");
        w.startStory();w.enterCamp(false);f.start(w);
        check(f.entranceQuoteSeen&&f.entranceQuoteTime==1800,"A newly started journey has its own first-visit quote");
    }
    static void roomsAndLight(){
        World w=chapter(0);Belfry f=w.belfry;
        w.update(499,0,0,false,false,false);check(!f.enemies[0].active&&(w.events&World.BELL)==0,"Opening steps initially leave room to read the inscription in safety");
        w.update(1,0,0,false,false,false);check((w.events&World.BELL)!=0,"The opening first distant bell occurs after 500 ms");
        w.update(1299,0,0,false,false,false);check((w.events&World.BELL)==0,"The second distant bell does not arrive early");
        w.update(1,0,0,false,false,false);check((w.events&World.BELL)!=0,"The second distant bell occurs at 1800 ms");
        w.update(1699,0,0,false,false,false);check(f.roomTime==3499&&!f.enemies[0].active,"No guard spawns before the entire 3500-ms grace period");
        w.update(1,0,0,false,false,false);check(f.enemies[0].active&&f.enemies[0].hp==48,"The first guard spawns at the exact grace boundary with normal health");
        right(w);check(f.room==0&&f.notice==Belfry.EXIT_LOCKED,"The first forward exit remains locked while its guard lives");
        strikeTwice(w,f.enemies[0]);check(f.exitReady(),"Two real sword collisions clear the first guard and its exit");
        right(w);check(f.room==1&&f.enemies[0].active&&!f.enemies[1].active,"The corridor begins with one guard rather than overlapping both encounters");
        strikeTwice(w,f.enemies[0]);w.px=40;w.py=145;w.update(699,0,0,false,false,false);
        check(!f.enemies[1].active&&!f.exitReady(),"The second corridor guard waits 699 ms after the first dies");
        w.update(1,0,0,false,false,false);check(f.enemies[1].active&&f.enemies[1].hp==48,"The second corridor guard enters exactly 700 ms after the first death");
        strikeTwice(w,f.enemies[1]);fire(w);check(f.fireMask==1&&f.inLight(f.torchX(),f.torchY()),"The first torch lights through the same J context action as doors");
        check(f.inLight(f.torchX()+78,f.torchY())&&!f.inLight(f.torchX()+79,f.torchY()),"Torch light has a bounded 78-pixel effect radius");
        right(w);check(f.room==2&&f.enemies[0].active==false,"The cleared corridor leads into the fallen bell court");
        fire(w);check(f.fireMask==3,"The courtyard lights the second persistent torch");
        w.potions=1;action(w,72,185);check(f.flaskTaken&&w.potions==2,"The optional courtyard flask is collected once");
        action(w,72,185);check(w.potions==2,"Reusing the pickup location cannot duplicate its flask");
        action(w,60,110);check(f.inscriptionRead&&f.notice==Belfry.INSCRIPTION,"The courtyard inscription uses its own readable context notice");
        left(w);check(f.room==1&&!f.enemies[0].active&&!f.enemies[1].active&&f.fireMask==3,"Backtracking retains both defeated corridor guards and lit torches");
        right(w);right(w);check(f.room==3&&!f.enemies[0].active&&!f.exitReady(),"The chain chamber does not spawn its elite before the final torch");
        fire(w);check(f.fireMask==7&&f.enemies[0].active&&f.enemies[0].elite&&f.enemies[0].hp==96,"Lighting the third torch awakens the 96-HP elite");
        right(w);check(f.room==3&&f.notice==Belfry.EXIT_LOCKED,"Three torches alone cannot open the bridge while the elite remains");
        defeat(w,f.enemies[0]);check(f.guardianDefeated&&f.checkpoint==3&&f.exitReady(),"The bridge requires both all three fires and the defeated elite");
        w.hp=12;w.potions=0;action(w,242,177);check(w.hp==100&&w.potions==3&&f.notice==Belfry.CHECKPOINT,"The pre-boss checkpoint restores all health and flasks");
        right(w);check(f.room==4&&f.boss.hp==600,"The final bridge reaches Bellbound's new arena");
        left(w);check(f.room==3&&f.fireMask==7&&f.guardianDefeated&&!f.enemies[0].active,"Returning from the boss preserves the opened bridge and defeated elite");
        w=chapter(3);f=w.belfry;f.fireMask=3;f.guardianDefeated=true;check(!f.exitReady(),"An elite defeat without the third torch still cannot open the bridge");
        World dark=chapter(1),lit=chapter(1);lit.belfry.fireMask=1;
        Belfry.Enemy de=dark.belfry.enemies[0],le=lit.belfry.enemies[0];de.x=le.x=100;de.y=le.y=115;de.time=le.time=0;dark.px=lit.px=155;dark.py=lit.py=115;
        dark.update(100,0,0,false,false,false);lit.update(100,0,0,false,false,false);
        check(le.lit&&!de.lit&&le.x-100<de.x-100&&le.x>100,"Actual lit guard movement is slower and its visible-lit flag changes inside a torch's light");
    }
    static void collisionAndWarnings(){
        for(int path=0;path<2;path++){
            World w=chapter(2);w.px=40;w.py=path==0?92:194;
            for(int i=0;i<120;i++)w.update(Balance.STEP,1,0,false,false,false);
            check(w.px>280&&!w.belfry.solid(w.px,w.py,5),"The fallen bell leaves the "+(path==0?"upper":"lower")+" courtyard route traversable");
        }
        World w=chapter(2);w.px=198;w.py=142;w.fx=-1;w.fy=0;w.update(33,-1,0,false,true,false);
        for(int i=0;i<12;i++){w.update(33,-1,0,false,false,false);if(w.belfry.solid(w.px,w.py,5))throw new RuntimeException("Courtyard roll entered the solid bell");}
        check(w.px>=195,"A dodge roll cannot pass through the solid courtyard bell");
        w.movePlayer(-150,0);check(w.px>=195,"Collision substeps also prevent a long movement update from tunneling through the bell");
        w=chapter(1);Belfry.Enemy e=w.belfry.enemies[0];e.x=e.ox=160;e.y=e.oy=120;e.dx=0;e.dy=1;e.state=World.WARNING;e.time=0;w.px=210;w.py=175;
        w.update(100,0,0,false,false,false);check(e.dx==0&&e.dy==1&&e.ox==160&&e.oy==120,"Guard warning locks its direction and origin while the hero changes position");
        w.px=160;w.py=144;w.update(599,0,0,false,false,false);check(e.state==World.WARNING&&w.hp==100,"Guard warning cannot damage before all 700 ms have elapsed");
        w.update(1,0,0,false,false,false);w.update(1,0,0,false,false,false);check(w.hp==100-Balance.BELFRY_GUARD_DAMAGE,"Guard live collision damages the hero inside its disclosed wedge");
        w.inv=0;w.pstate=World.IDLE;w.update(1,0,0,false,false,false);check(w.hp==100-Balance.BELFRY_GUARD_DAMAGE,"A guard can damage only once in each attack window");
        w=chapter(1);e=w.belfry.enemies[0];e.x=e.ox=160;e.y=e.oy=120;e.dx=0;e.dy=1;e.state=World.WARNING;e.time=0;w.px=160;w.py=96;
        w.update(700,0,0,false,false,false);w.update(180,0,0,false,false,false);check(w.hp==100&&e.state==World.RECOVER,"The guard wedge has a real safe rear and a recovery opening");
    }
    static void deathAndInput(){
        World w=chapter(2);Belfry f=w.belfry;f.fireMask=3;f.flaskTaken=true;f.inscriptionRead=true;w.hp=1;w.takeDamage(1);
        check(w.mode==World.BELFRY_LOSE&&w.deathTime==0,"A chapter death enters its own scripted collapse state");
        float x=w.px,y=w.py;int end=Balance.DEATH_TIME+Balance.FADE_OUT_TIME+Balance.BLACK_HOLD_TIME;
        w.update(end-1,1,1,true,true,true);check(w.mode==World.BELFRY_LOSE&&w.px==x&&w.py==y&&w.hp==0,"Chapter death freezes combat and inputs through the black hold");
        w.update(1,1,1,true,true,true);check(w.mode==World.BELFRY&&f.room==0&&f.wakeTime==0&&w.hp==100&&w.potions==3,"Before the elite checkpoint, death respawns at the steps at the exact transition boundary");
        check(f.fireMask==3&&f.flaskTaken&&f.inscriptionRead&&w.oathkeeperDefeated,"Death preserves lit fires, pickups, reading and chapter access");
        x=w.px;y=w.py;w.update(1799,1,0,true,true,true);check(f.wakeTime==1799&&w.px==x&&w.py==y&&w.pstate==World.IDLE,"Chapter respawn locks movement and combat for its waking sequence");
        w.update(1,1,0,true,true,true);check(f.wakeTime==1800&&w.px==x&&w.py==y,"The final waking tick completes before accepting gameplay input");
        w.update(33,1,0,false,false,false);check(w.px>x,"The next awake tick restores chapter movement");
        f.guardianDefeated=true;f.fireMask=7;f.enterRoom(w,4,true);w.hp=1;w.takeDamage(1);w.update(end,0,0,false,false,false);
        check(f.room==3&&f.checkpoint==3&&w.px==242&&w.py==177&&f.wakeTime==0&&f.fireMask==7&&f.guardianDefeated,"Boss death returns to the unlocked pre-boss checkpoint with its three fires intact");
        Game g=new Game(null,false);press(g,'m');g.world.oathkeeperDefeated=g.world.oathkeeperComplete=true;g.world.belfry.start(g.world);g.world.hp=1;g.world.takeDamage(1);g.world.deathTime=end-Balance.STEP;
        g.keyPressed('d');g.keyPressed('j');g.tick();while(g.world.belfry.wakeTime<1800)g.tick();g.tick();
        check(g.world.px==40&&g.world.py==145&&g.world.pstate==World.IDLE,"Held inputs across automatic chapter respawn do not become movement or attacks after waking");
        g.keyReleased('d');g.keyReleased('j');int time=g.world.belfry.roomTime;press(g,'p');for(int i=0;i<20;i++)g.tick();check(g.world.belfry.roomTime==time,"Pause freezes chapter guard spawning and room progression");press(g,'p');
        g.world.belfry.enterRoom(g.world,2,true);g.world.px=g.world.belfry.torchX();g.world.py=g.world.belfry.torchY();press(g,'j');
        check((g.world.belfry.fireMask&2)!=0&&g.world.pstate==World.IDLE,"Real Canvas J lights a torch without also starting a sword swing");
        g.world.px=40;g.world.py=145;g.keyPressedAt('d',1000);g.tick();g.keyReleased('d');g.keyPressedAt('d',1100);g.tick();g.keyReleased('d');
        check(g.world.pstate==World.ROLL,"Existing double-tap controls produce a real roll inside the new chapter");
    }
    static void fullChapter(){
        World w=chapter(0);Belfry f=w.belfry;ticks(w,3500);defeat(w,f.enemies[0]);right(w);
        defeat(w,f.enemies[0]);ticks(w,700);defeat(w,f.enemies[1]);fire(w);right(w);fire(w);action(w,60,110);right(w);fire(w);defeat(w,f.enemies[0]);action(w,242,177);right(w);
        check(f.room==4&&f.fireMask==7&&f.guardianDefeated&&w.hp==100&&w.potions==3,"The complete five-screen script reaches the boss through real guard defeats, three fires and checkpoint rest");
        completeBoss(w);check(f.fragment==1&&f.notice==Belfry.CHAPTER_COMPLETE&&w.deathTime==0,"The final real sword hit awards exactly one Dawn Shard and starts the red-light ending");
        float x=w.px,y=w.py;w.update(Balance.BELFRY_ENDING_TIME-1,1,1,true,true,true);
        check(w.mode==World.DAWN&&f.fragment==1&&w.px==x&&w.py==y,"The ending freezes gameplay and cannot be skipped before its minimum duration");
        w.update(1,0,0,false,false,false);w.update(10000,0,0,false,false,false);check(w.mode==World.DAWN&&f.fragment==1,"Continuing the completed ending never duplicates its fragment");
        w.update(1,0,0,true,false,false);check(w.mode==World.CAMP&&f.fragment==1&&w.oathkeeperDefeated,"A fresh ending confirmation returns to camp with the fragment and access preserved");
        w.campTime=1800;action(w,54,87);f.enterRoom(w,4,true);w.update(1,0,0,false,false,false);check(w.mode==World.DAWN&&f.fragment==1,"Revisiting the completed summit cannot award another fragment");
    }
    static void runTests()throws Exception{
        RenderTest ctx=new RenderTest();DeviceImpl dev=DeviceImpl.create(ctx,BelfryTest.class.getClassLoader(),"org/microemu/device/resizable/device.xml",J2SEDevice.class);DeviceFactory.setDevice(dev);dev.init();
        gating();entranceQuote();roomsAndLight();collisionAndWarnings();deathAndInput();fullChapter();
        System.out.println("ALL "+checks+" BELFRY CHECKS PASSED");System.exit(0);
    }
    public static void main(String[] args){try{runTests();}catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
