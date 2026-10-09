import org.microemu.device.*;
import org.microemu.device.impl.*;
import org.microemu.device.j2se.*;

/** End-state timing, automatic camp arrival, and real Canvas input behavior. */
public final class BonfireTest {
    static int checks;
    static void check(boolean b,String s){if(!b)throw new RuntimeException("FAIL: "+s);checks++;System.out.println("PASS: "+s);}
    static void press(Game g,int key){g.keyPressed(key);g.tick();g.keyReleased(key);}
    static void ticks(Game g,int n){for(int i=0;i<n;i++)g.tick();}
    static Game quietGame(){Game g=new Game(null,false);press(g,'m');return g;}
    static void finishBattle(World w,boolean won){
        w.reset();w.mode=World.FIGHT;w.bstate=World.TRANSITION;w.bt=-100000;
        w.px=160;w.py=147;w.bx=160;w.by=120;w.fx=0;w.fy=-1;w.potions=0;
        if(won){w.hp=38;w.inv=Balance.HURT_INV;w.bossHp=Balance.SWORD_DAMAGE;w.update(Balance.SWORD_WINDUP,0,0,true,false,false);}
        else{w.hp=1;w.takeDamage(1);}
    }
    static World ending(boolean won){World w=new World();finishBattle(w,won);return w;}
    static int endDuration(boolean won){return (won?Balance.VICTORY_WAIT:0)+Balance.DEATH_TIME+Balance.FADE_OUT_TIME+Balance.BLACK_HOLD_TIME;}
    static void frozenEnd(World w,int dt,String label){
        float px=w.px,py=w.py,bx=w.bx,by=w.by;
        int hp=w.hp,bossHp=w.bossHp,potions=w.potions,pt=w.pt,bt=w.bt,hits=w.hitsTaken,landed=w.hitsLanded;
        w.update(dt,1,-1,true,true,true);
        check(w.px==px&&w.py==py&&w.bx==bx&&w.by==by&&w.hp==hp&&w.bossHp==bossHp&&w.potions==potions&&w.pt==pt&&w.bt==bt&&w.hitsTaken==hits&&w.hitsLanded==landed,label);
    }
    static World awakeCamp(float x,float y){
        World w=new World();w.enterCamp(true);w.campTime=Balance.CAMP_WAKE_TIME;
        w.px=x;w.py=y;w.hp=19;w.potions=0;return w;
    }
    static void fixedRest(World w,float px,float py,float fx,float fy,String label){
        check(w.px==px&&w.py==py&&w.fx==fx&&w.fy==fy&&!w.moving&&w.pstate==World.IDLE,label);
    }
    static void restModel(){
        check(Balance.REST_SIT_TIME==720&&Balance.REST_RISE_TIME==720,"Sitting and rising each use 720 milliseconds");
        World w=awakeCamp(190,164);w.moving=true;float px=w.px,py=w.py;
        w.update(33,1,1,true,true,true);
        check(w.restState==World.REST_SIT&&w.restTime==0&&w.px==px&&w.py==py&&!w.moving,"Rest begins at frame zero before simultaneous movement at an outer seat");
        float distance=World.length(148-px,151-py),fx=(148-px)/distance,fy=(151-py)/distance;
        check(Math.abs(w.fx-fx)<0.0001f&&Math.abs(w.fy-fy)<0.0001f,"Rest turns the hero toward the fire instead of the movement input");
        check(w.hp==Balance.PLAYER_HP&&w.potions==Balance.POTIONS&&w.events==World.DRINK&&w.campNotice==Balance.CAMP_NOTICE_TIME,"Rest restores health and flasks immediately without combat actions");
        w.update(Balance.REST_SIT_TIME-1,0,0,false,true,true);
        check(w.restState==World.REST_SIT&&w.restTime==719,"Sitting retains the last in-progress millisecond");
        fixedRest(w,px,py,fx,fy,"Sitting keeps the foot anchor and fire-facing direction locked");
        w.update(1,0,0,false,false,false);check(w.restState==World.REST_IDLE&&w.restTime==0,"Sitting reaches seated idle at the exact 720-millisecond boundary");
        w.update(937,0,0,false,true,true);check(w.restState==World.REST_IDLE&&w.restTime==937&&w.events==0,"Seated breathing advances its own clock while healing and dodge are ignored");
        fixedRest(w,px,py,fx,fy,"Seated idle never slides or changes facing");
        w.update(1,0,0,true,false,false);check(w.restState==World.REST_RISE&&w.restTime==0&&w.events==0,"A fresh seated J starts rising without repeating the fire reward");
        w.update(Balance.REST_RISE_TIME-1,1,-1,true,true,true);
        check(w.restState==World.REST_RISE&&w.restTime==719&&w.mode==World.CAMP,"Rising ignores further action inputs until the final millisecond");
        fixedRest(w,px,py,fx,fy,"Rising locks position and facing even when movement is held");
        w.update(1,1,0,false,false,false);check(w.restState==World.REST_NONE&&w.restTime==0,"Rising finishes at the exact 720-millisecond boundary");
        fixedRest(w,px,py,fx,fy,"The completion frame remains planted before returning movement control");
        w.update(Balance.STEP,1,0,false,false,false);
        check(w.px>px&&w.py==py&&w.moving&&w.fx==1&&w.fy==0,"Held movement continues on the next standing tick without another press");
        float[][] inner={{148,151},{153,151},{157,139},{186,151}};
        for(int i=0;i<inner.length;i++){
            w=awakeCamp(inner[i][0],inner[i][1]);w.update(1,0,0,true,false,false);
            float radius=World.length(w.px-148,w.py-151);
            check(w.restState==World.REST_SIT&&radius>=Balance.REST_SEAT_RADIUS-0.001f&&radius<=Balance.REST_SEAT_RADIUS+0.001f,"Inner fire seat "+i+" keeps both feet outside the flame radius");
            check(Math.abs(w.fx-(148-w.px)/radius)<0.0001f&&Math.abs(w.fy-(151-w.py)/radius)<0.0001f,"Inner fire seat "+i+" faces the center after its radial correction");
            if(i==0)check(w.px>148&&w.py>151,"A seat at the fire center uses the arrival-side radial direction");
            if(i==1)check(w.py==151&&Math.abs(w.px-186)<0.001f,"A near-center seat preserves its original radial angle");
            if(i==3)check(w.px==186&&w.py==151,"A seat already on the safe inner rim stays at its exact position");
        }
        w=awakeCamp(193,151);w.update(1,0,0,true,false,false);
        check(w.restState==World.REST_SIT&&w.px==193&&w.py==151,"The exact 45-pixel interaction edge rests without moving the hero");
        w=awakeCamp(194,151);w.update(1,0,0,true,false,false);
        check(w.restState==World.REST_NONE&&w.restTime==0&&w.hp==19&&w.potions==0,"A point just outside the fire radius cannot start sitting");
        int[] cancelTimes={0,120,360,719};
        for(int i=0;i<cancelTimes.length;i++){
            int age=cancelTimes[i];w=awakeCamp(190,164);w.update(1,0,0,true,false,false);
            if(age>0)w.update(age,0,0,false,false,false);
            px=w.px;py=w.py;fx=w.fx;fy=w.fy;
            w.update(1,i==1?1:0,0,i!=1,false,false);
            check(w.restState==World.REST_RISE&&w.restTime==Balance.REST_RISE_TIME-age*Balance.REST_RISE_TIME/Balance.REST_SIT_TIME,"Cancel at sitting millisecond "+age+" reverses from the same animation progress");
            fixedRest(w,px,py,fx,fy,"Early cancellation at "+age+" does not jump the foot anchor or facing");
        }
        w=awakeCamp(190,164);w.update(1,0,0,true,false,false);w.update(720,0,0,false,false,false);
        w.update(1,-1,1,false,false,false);check(w.restState==World.REST_RISE&&w.restTime==0&&!w.moving,"Movement alone starts a full rise from seated idle");
        w=awakeCamp(190,164);w.update(1,0,0,true,false,false);w.update(720,0,0,false,false,false);
        w.px=54;w.py=87;w.update(1,0,0,true,false,false);
        check(w.mode==World.CAMP&&w.restState==World.REST_RISE,"A seated interaction cannot bypass rising through the chapter exit");
        w.update(720,0,0,true,false,false);check(w.mode==World.CAMP&&w.restState==World.REST_NONE,"An action during rising is not queued into the gate");
        w.update(1,0,0,true,false,false);check(w.mode==World.BELFRY&&w.restState==World.REST_NONE&&w.restTime==0,"A fresh chapter exit interaction works after the hero has stood up");
        w=awakeCamp(190,164);w.update(1,0,0,true,false,false);w.update(720,0,0,false,false,false);w.update(100,0,0,false,false,false);w.reset();
        check(w.restState==World.REST_NONE&&w.restTime==0,"Reset clears the seated state and its breathing timer");
        w.restState=World.REST_RISE;w.restTime=359;w.enterCamp(false);
        check(w.restState==World.REST_NONE&&w.restTime==0&&w.campTime==0,"Camp arrival clears an interrupted rise before waking");
        w.update(Balance.CAMP_WAKE_TIME-1,0,0,true,true,true);check(w.restState==World.REST_NONE&&w.restTime==0,"Rest cannot start during the camp waking sequence");
    }
    static void restInput(){
        Game g=quietGame();g.world.enterCamp(true);g.world.campTime=Balance.CAMP_WAKE_TIME;g.world.hp=9;g.world.potions=0;
        g.keyPressed('j');g.tick();check(g.world.restState==World.REST_SIT&&g.world.restTime==0,"Real Canvas J starts the sitting animation at the fire");
        int repeats=0;for(int i=0;i<75;i++){g.keyRepeated('j');g.tick();if((g.world.events&World.DRINK)!=0)repeats++;}
        check(g.world.restState==World.REST_IDLE&&g.world.restTime>0&&repeats==0,"Holding or repeating J does not restart rest, stand up, or repeat its reward");
        g.keyPressed('j');g.tick();check(g.world.restState==World.REST_IDLE,"An extra press event for an already held J is ignored");
        g.keyReleased('j');press(g,'j');check(g.world.restState==World.REST_RISE&&g.world.restTime==0,"Releasing and pressing J again starts the seated rise");
        float px=g.world.px,py=g.world.py,fx=g.world.fx,fy=g.world.fy;
        g.keyPressed('d');while(g.world.restState==World.REST_RISE)g.tick();
        fixedRest(g.world,px,py,fx,fy,"Held Canvas movement stays locked through the entire rise");
        g.tick();check(g.world.px>px&&g.world.moving&&g.world.pstate==World.IDLE,"Held Canvas movement resumes after standing without becoming a roll");g.keyReleased('d');
        g=quietGame();g.world.enterCamp(false);g.world.campTime=Balance.CAMP_WAKE_TIME;press(g,13);
        check(g.world.restState==World.REST_SIT,"Center-key input starts the same camp sitting state as J");
        g.world.hp=23;g.world.potions=1;press(g,'l');press(g,'2');
        check(g.world.restState==World.REST_SIT&&g.world.hp==23&&g.world.potions==1&&g.world.pstate==World.IDLE,"Canvas L and 2 cannot drink, cancel sitting, or consume a flask at camp");
        g=quietGame();g.world.enterCamp(false);g.keyPressed('j');
        while(g.world.campTime<Balance.CAMP_WAKE_TIME+Balance.STEP*3)g.tick();
        check(g.world.restState==World.REST_NONE&&g.world.campNotice==0,"J held during waking does not become a delayed rest when controls unlock");
        g.keyReleased('j');press(g,'j');check(g.world.restState==World.REST_SIT,"A fresh post-waking J starts resting normally");
        int[] states={World.REST_SIT,World.REST_IDLE,World.REST_RISE};
        for(int i=0;i<states.length;i++){
            g=quietGame();g.world.enterCamp(true);g.world.campTime=Balance.CAMP_WAKE_TIME;press(g,'j');
            if(states[i]!=World.REST_SIT)g.world.update(720,0,0,false,false,false);
            if(states[i]==World.REST_RISE)press(g,'j');g.world.update(137,0,0,false,false,false);
            int timer=g.world.restTime,camp=g.world.campTime,clock=g.world.clock;px=g.world.px;py=g.world.py;fx=g.world.fx;fy=g.world.fy;
            press(g,'p');press(g,'j');ticks(g,20);
            check(g.world.restState==states[i]&&g.world.restTime==timer&&g.world.campTime==camp&&g.world.clock==clock,"Pause freezes rest state "+states[i]+" and discards paused J intent");
            fixedRest(g.world,px,py,fx,fy,"Pause preserves the seated foot anchor for state "+states[i]);
            press(g,'p');check(g.world.restState==states[i]&&g.world.restTime==timer+Balance.STEP,"Unpause resumes rest state "+states[i]+" by one fixed step");
        }
    }
    static void runTests()throws Exception{
        RenderTest ctx=new RenderTest();DeviceImpl dev=DeviceImpl.create(ctx,BonfireTest.class.getClassLoader(),"org/microemu/device/resizable/device.xml",J2SEDevice.class);DeviceFactory.setDevice(dev);dev.init();
        int[] durations={Balance.VICTORY_WAIT,Balance.DEATH_TIME,Balance.FADE_OUT_TIME,Balance.BLACK_HOLD_TIME,Balance.FADE_IN_TIME,Balance.CAMP_WAKE_TIME};
        check(java.util.Arrays.equals(durations,new int[]{5000,720,900,550,900,1800}),"Bonfire sequence uses the requested waiting, death, fade and waking durations");
        World w=ending(true);check(w.mode==World.WIN&&w.deathTime==0&&w.bossHp==0&&w.inv==0,"Final sword hit starts victory at time zero and clears hurt flashing");
        frozenEnd(w,Balance.VICTORY_WAIT-1,"Victory wait freezes boss, movement, damage, healing and attacks");
        check(w.mode==World.WIN&&w.collapseTime()==0&&w.fadeOutTime()==0,"Victory does not collapse before five seconds");
        w.update(1,0,0,false,false,false);check(w.deathTime==Balance.VICTORY_WAIT&&w.collapseTime()==0,"Five-second boundary starts the collapse clock without skipping a frame");
        w.update(1,0,0,false,false,false);check(w.collapseTime()==1&&w.fadeOutTime()==0,"Victory collapse begins immediately after the waiting period");
        for(int outcome=0;outcome<2;outcome++){
            boolean won=outcome==1;String label=won?"Victory":"Defeat";w=ending(won);
            frozenEnd(w,(won?Balance.VICTORY_WAIT:0)+Balance.DEATH_TIME,label+" death animation freezes the completed battle");
            check(w.collapseTime()==Balance.DEATH_TIME&&w.fadeOutTime()==0,label+" plays the complete death animation before fading");
            frozenEnd(w,Balance.FADE_OUT_TIME-1,label+" fade keeps combat frozen");
            check(w.mode==(won?World.WIN:World.LOSE)&&w.fadeOutTime()==Balance.FADE_OUT_TIME-1,label+" remains in the arena until the fade completes");
            w.update(1,0,0,false,false,false);check(w.fadeOutTime()==Balance.FADE_OUT_TIME,label+" reaches opaque black at the exact fade boundary");
            w.update(Balance.BLACK_HOLD_TIME-1,1,1,true,true,true);check(w.mode==(won?World.WIN:World.LOSE),label+" holds black before entering the camp");
            w.update(1,1,1,true,true,true);check(w.mode==World.CAMP&&w.campTime==0&&w.campNotice==0,label+" automatically enters the bonfire at the exact final boundary");
            check(w.lastBattleWon==won&&w.hp==Balance.PLAYER_HP&&w.potions==Balance.POTIONS,label+" outcome survives arrival with full health and flasks");
            check(w.oathkeeperComplete&&w.oathkeeperDefeated==won,label+" completes the one-time opening without rewriting its outcome");
            check(w.px==190&&w.py==164&&w.pstate==World.IDLE&&w.inv==0&&w.dodgeCd==0,label+" arrival restores the fixed waking position and clears combat locks");
            float px=w.px,py=w.py,bx=w.bx,by=w.by;int bossHp=w.bossHp,bt=w.bt,potions=w.potions;
            w.update(Balance.CAMP_WAKE_TIME-1,1,-1,true,true,true);
            check(w.campTime==Balance.CAMP_WAKE_TIME-1&&w.px==px&&w.py==py&&w.bx==bx&&w.by==by&&w.bossHp==bossHp&&w.bt==bt&&w.potions==potions&&w.campNotice==0,label+" waking locks controls and keeps boss simulation frozen");
            w.update(1,0,0,false,false,false);check(w.campTime==Balance.CAMP_WAKE_TIME,label+" waking reaches the control-unlock boundary");
            w.update(Balance.STEP,1,0,false,false,false);check(w.px>px&&w.lastBattleWon==won,label+" permits movement after waking and retains its outcome");
        }
        w=new World();w.enterCamp(true);w.campTime=Balance.CAMP_WAKE_TIME;w.hp=19;w.potions=0;w.px=194;w.py=151;
        w.update(1,0,0,true,false,false);check(w.hp==19&&w.potions==0&&w.campNotice==0,"J outside the fire interaction radius does not rest");
        w.px=193;w.update(1,0,0,true,false,false);check(w.mode==World.CAMP&&w.hp==Balance.PLAYER_HP&&w.potions==Balance.POTIONS&&w.campNotice>0&&w.lastBattleWon,"J at the 45-pixel fire edge rests without restarting or losing the outcome");
        w.hp=19;w.potions=0;w.px=80;w.py=190;w.update(33,0,0,false,true,true);
        check(w.hp==19&&w.potions==0&&w.pstate==World.IDLE,"Healing and dodge controls do not start combat actions at the camp");
        int bossHp=w.bossHp,bt=w.bt,bstate=w.bstate;float bx=w.bx,by=w.by;
        for(int i=0;i<500;i++)w.update(Balance.STEP,-1,-1,false,false,false);
        check(w.px==28&&w.py==80,"Camp movement clamps to the upper-left walk boundary");
        for(int i=0;i<500;i++)w.update(Balance.STEP,1,1,false,false,false);
        check(w.px==292&&w.py==204,"Camp movement clamps to the lower-right walk boundary");
        check(w.bossHp==bossHp&&w.bt==bt&&w.bstate==bstate&&w.bx==bx&&w.by==by&&w.hp==19,"Camp exploration never advances the boss or damages the hero");
        w.px=245;w.py=87;w.update(1,0,0,true,false,false);check(w.mode==World.CAMP,"J outside the gate interaction radius cannot start a battle");
        w.px=246;w.update(1,0,0,true,false,false);check(w.mode==World.CAMP&&w.hp==19&&w.potions==0&&w.oathkeeperComplete,"J at the old gate edge cannot replay the opening or reset camp resources");
        w=ending(true);w.deathTime=Balance.VICTORY_WAIT-1;check(!HeroSprites.unconscious(w),"Hero stays upright throughout the full victory waiting period");
        w.deathTime=Balance.VICTORY_WAIT;check(HeroSprites.unconscious(w)&&HeroSprites.unconsciousFrame(w)==0,"Victory starts with the first collapse frame at five seconds");
        w=ending(false);check(HeroSprites.unconscious(w)&&HeroSprites.unconsciousFrame(w)==0,"Defeat starts with the first collapse frame immediately");
        for(int frame=0;frame<6;frame++){w.deathTime=frame*Balance.DEATH_FRAME_TIME;check(HeroSprites.unconsciousFrame(w)==frame,"Defeat collapse preserves frame "+frame+" order");}
        w.enterCamp(false);w.campTime=Balance.FADE_IN_TIME-1;check(HeroSprites.unconsciousFrame(w)==5,"Camp fade-in holds the hero fully fallen");
        for(int frame=0;frame<6;frame++){w.campTime=Balance.FADE_IN_TIME+frame*Balance.DEATH_FRAME_TIME;check(HeroSprites.unconscious(w)&&HeroSprites.unconsciousFrame(w)==5-frame,"Camp waking reverses collapse frame "+frame);}
        w.campTime=Balance.CAMP_WAKE_TIME;check(!HeroSprites.unconscious(w),"Camp switches to the living animation when controls unlock");
        for(int outcome=0;outcome<2;outcome++){
            Game g=quietGame();finishBattle(g.world,outcome==1);g.world.deathTime=(outcome==1?Balance.VICTORY_WAIT:0)+Balance.DEATH_TIME+Balance.FADE_OUT_TIME/2;
            int timer=g.world.deathTime;press(g,'p');ticks(g,20);check(g.world.deathTime==timer,"Pause freezes the "+(outcome==1?"victory":"defeat")+" fade timer");
            press(g,'p');check(g.world.deathTime==timer+Balance.STEP,"Unpause advances the end-state timer by one fixed step");
            g.world.enterCamp(outcome==1);g.world.campTime=300;press(g,'p');ticks(g,20);
            check(g.world.campTime==300,"Pause freezes bonfire fade-in and waking timers");
            press(g,'p');check(g.world.campTime==300+Balance.STEP,"Unpause resumes the camp sequence without skipping time");
        }
        Game g=quietGame();finishBattle(g.world,false);g.world.deathTime=endDuration(false)-Balance.STEP;
        g.keyPressed('d');g.keyPressed('j');g.keyPressed('l');g.tick();check(g.world.mode==World.CAMP,"Held inputs cannot interrupt automatic bonfire arrival");
        float px=g.world.px,py=g.world.py;
        while(g.world.campTime<Balance.CAMP_WAKE_TIME+Balance.STEP*3)g.tick();
        check(g.world.px==px&&g.world.py==py&&g.world.campNotice==0,"Held movement and action inputs from defeat do not carry into the awake camp");
        g.keyReleased('d');g.keyReleased('j');g.keyReleased('l');press(g,'d');check(g.world.px>px,"A fresh movement press works after automatic camp arrival");
        g.world.px=148;g.world.py=151;g.world.hp=9;g.world.potions=0;press(g,'j');
        check(g.world.mode==World.CAMP&&g.world.hp==Balance.PLAYER_HP&&g.world.potions==Balance.POTIONS,"Real Canvas J input rests at the fire");
        press(g,'j');ticks(g,23);check(g.world.restState==World.REST_NONE,"Canvas gate interaction waits until the resting hero has stood up");
        g.world.px=274;g.world.py=87;press(g,'j');check(g.world.mode==World.CAMP,"Real Canvas J cannot reopen the old boss gate");
        g.world.px=54;g.world.py=87;press(g,'j');check(g.world.mode==World.BELFRY&&g.world.belfry.room==0,"Real Canvas J at the left exit continues into the next chapter after defeat");
        restModel();restInput();
        System.out.println("ALL "+checks+" BONFIRE CHECKS PASSED");System.exit(0);
    }
    public static void main(String[] args){try{runTests();}catch(Throwable error){error.printStackTrace();System.exit(1);}}
}
