import org.microemu.device.*;
import org.microemu.device.impl.*;
import org.microemu.device.j2se.*;

/** Real Canvas progression: both opening outcomes lead onward and never offer a rematch. */
public final class OpeningOnceTest {
    static int checks;
    static void check(boolean value,String message){if(!value)throw new RuntimeException(message);checks++;System.out.println("PASS: "+message);}
    static void press(Game game,int key){game.keyPressed(key);game.tick();game.keyReleased(key);}
    static void reach(Game game,int mode){
        int budget=20000;
        while(game.world.mode!=mode){game.tick();if((budget-=Balance.STEP)<0)throw new RuntimeException("Never reached mode "+mode);}
    }
    static void wake(Game game){
        while(game.world.campTime<Balance.CAMP_WAKE_TIME)game.tick();
        check(game.world.mode==World.CAMP&&game.world.hp==100&&game.world.potions==3,"Opening arrival wakes at the safe bonfire with full health and flasks");
    }
    static void outcome(boolean won){
        String label=won?"Victory":"Defeat";Game game=new Game(null,false);press(game,'m');World w=game.world;
        press(game,'j');check(w.mode==World.STORY&&!w.oathkeeperComplete,label+" journey starts with an unresolved opening");
        press(game,'j');reach(game,World.FIGHT);check(w.bossHp==Balance.BOSS_HP,label+" journey contains its initial Oathkeeper encounter");
        if(won){
            w.bossHp=Balance.SWORD_DAMAGE;w.px=160;w.py=147;w.bx=160;w.by=120;w.fx=0;w.fy=-1;
            w.bstate=World.TRANSITION;w.bt=-100000;press(game,'j');reach(game,World.WIN);
        }else{
            w.hp=1;w.bstate=World.ACTIVE;w.attack=0;w.bt=0;w.ox=w.px;w.oy=w.py-20;w.bdx=0;w.bdy=1;w.bossHit=false;
            game.tick();check(w.mode==World.LOSE,"A real opening boss attack starts the defeat branch");
        }
        reach(game,World.CAMP);
        check(w.oathkeeperComplete&&w.oathkeeperDefeated==won&&w.lastBattleWon==won,label+" permanently resolves the opening while preserving the real outcome");
        wake(game);
        int[] keys={'j',13,'5'};
        for(int i=0;i<keys.length;i++){
            w.px=274;w.py=87;press(game,keys[i]);
            check(w.mode==World.CAMP&&w.oathkeeperComplete,label+" cannot replay the first boss through old gate key "+keys[i]);
        }
        for(int visit=0;visit<2;visit++){
            w.px=54;w.py=87;press(game,'j');
            check(w.mode==World.BELFRY&&w.belfry.room==0,label+" enters the next chapter on camp visit "+visit);
            w.px=29;w.py=145;press(game,'j');
            check(w.mode==World.CAMP&&w.oathkeeperComplete&&w.oathkeeperDefeated==won&&w.lastBattleWon==won,label+" keeps the opening resolved after chapter backtracking");
            wake(game);
        }
        w.reset();check(w.mode==World.CAMP&&w.oathkeeperComplete,label+" combat reset cannot reopen the completed first encounter");
        w.finishStory();check(w.mode==World.CAMP,label+" stale introduction completion cannot replay the boss");
        w.mode=World.INTRO;game.tick();check(w.mode==World.CAMP,label+" stale doorway state redirects to the bonfire");wake(game);
        w.px=54;w.py=87;press(game,'j');w.hp=1;w.takeDamage(1);reach(game,World.BELFRY);
        check(w.belfry.room==0&&w.oathkeeperComplete&&w.oathkeeperDefeated==won,label+" death in the next chapter respawns there without returning to Oathkeeper");
        w.belfry.enterRoom(w,4,true);w.belfry.boss.hit(Bellbound.MAX_HP);game.tick();
        check(w.mode==World.DAWN&&w.belfry.fragment==1,label+" reaches the next boss ending with its opening result intact");
        while(w.deathTime<Balance.BELFRY_ENDING_TIME)game.tick();press(game,'j');
        check(w.mode==World.CAMP&&w.oathkeeperComplete&&w.oathkeeperDefeated==won&&w.lastBattleWon==won,label+" later Bellbound victory does not rewrite the first encounter outcome");
        press(game,'q');press(game,'j');
        check(w.mode==World.STORY&&!w.oathkeeperComplete&&!w.oathkeeperDefeated&&w.belfry.fragment==0,label+" explicitly starting a new journey resets the opening and chapter progress");
        press(game,'j');reach(game,World.FIGHT);
        check(w.bossHp==Balance.BOSS_HP,"A new journey gets its own single opening encounter");
    }
    static void runTests()throws Exception{
        RenderTest ctx=new RenderTest();DeviceImpl dev=DeviceImpl.create(ctx,OpeningOnceTest.class.getClassLoader(),"org/microemu/device/resizable/device.xml",J2SEDevice.class);DeviceFactory.setDevice(dev);dev.init();
        outcome(false);outcome(true);System.out.println("ALL "+checks+" ONE-TIME OPENING CHECKS PASSED");System.exit(0);
    }
    public static void main(String[] args){try{runTests();}catch(Throwable error){error.printStackTrace();System.exit(1);}}
}
