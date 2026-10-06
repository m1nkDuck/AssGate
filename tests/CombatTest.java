public final class CombatTest {
    static int count;
    static void check(boolean ok,String s){if(!ok)throw new RuntimeException("FAIL: "+s);count++;System.out.println("PASS: "+s);}
    static World arena(){World w=new World();w.reset();w.mode=World.FIGHT;w.bx=160;w.by=100;w.px=160;w.py=170;w.bstate=World.TRANSITION;w.bt=-100000;return w;}
    static void ticks(World w,int n){for(int i=0;i<n;i++)w.update(33,0,0,false,false,false);}
    public static void main(String[] a){
        World w=arena();w.px=160;w.py=127;w.fx=0;w.fy=-1;
        w.update(33,0,0,true,false,false);ticks(w,3);check(w.bossHp==720,"Sword windup does no damage");ticks(w,3);check(w.bossHp==696,"Sword active hits once");ticks(w,35);check(w.bossHp==696,"No repeat from the same attack");
        w=arena();w.py=127;w.fy=1;w.update(33,0,0,true,false,false);ticks(w,10);check(w.bossHp==720,"Facing away misses");
        w=arena();w.py=150;w.fx=0;w.fy=-1;w.update(33,0,0,true,false,false);ticks(w,10);check(w.bossHp==720,"Out of sword range misses");
        w=arena();float y=w.py;w.fy=-1;w.update(33,0,0,false,true,false);w.takeDamage(30);check(w.hp==100&&w.rollingSafe(),"Roll immunity starts immediately");ticks(w,10);check(Math.abs((y-w.py)-32.4)<.2,"Short roll travels 32.4 pixels");check(w.dodgeCd>0,"Roll cooldown outlasts roll");w.update(33,0,0,false,true,false);check(w.pstate!=World.ROLL,"Cooldown rejects repeated dodge");
        w=arena();w.update(33,1,-1,false,true,false);check(w.px>160&&w.py<170,"Roll uses diagonal movement direction");
        w=arena();w.takeDamage(22);w.takeDamage(32);check(w.hp==78,"Post-hit immunity prevents stacked damage");ticks(w,21);w.takeDamage(22);check(w.hp==56,"Post-hit immunity expires");
        w=arena();w.hp=35;w.update(33,0,0,false,false,true);check(w.potions==2,"Flask consumed on start");ticks(w,28);check(w.hp==35,"No early healing");w.update(33,0,0,true,true,false);check(w.pstate==World.HEAL,"Cannot attack or dodge while drinking");ticks(w,2);check(w.hp==75,"Heal restores forty percent after one second");
        w=arena();w.hp=80;w.update(33,0,0,false,false,true);ticks(w,31);check(w.hp==100,"Healing capped at max HP");
        w=arena();w.hp=50;w.update(33,0,0,false,false,true);ticks(w,12);w.takeDamage(22);ticks(w,35);check(w.hp==28&&w.potions==2,"Hit interrupts healing without refund");
        w=arena();w.potions=0;w.hp=50;w.update(33,0,0,false,false,true);check(w.pstate==World.IDLE,"Empty flask cannot heal");
        w=arena();w.update(33,0,0,false,false,true);check(w.potions==3,"Full health cannot waste flask");
        for(int attack=0;attack<4;attack++){
            w=arena();w.attack=attack;w.px=160;w.py=120;w.lockAttack();float dx=w.bdx,dy=w.bdy;
            w.px=210;w.py=150;ticks(w,10);check(w.bdx==dx&&w.bdy==dy,"Attack "+attack+" direction stays locked");
            w.px=160;w.py=120;while(w.bstate==World.WARNING)w.update(33,0,0,false,false,false);check(w.hp==100,"Attack "+attack+" warning does not hurt");
            w.update(33,0,0,false,false,false);check(w.hp==100-Balance.DAMAGE[attack],"Attack "+attack+" active hits marked area");ticks(w,15);check(w.hp==100-Balance.DAMAGE[attack],"Attack "+attack+" cannot hit twice");check(w.bstate==World.RECOVER,"Attack "+attack+" has recovery opening");
        }
        w=arena();w.attack=1;w.bdx=0;w.bdy=1;w.ox=160;w.oy=100;w.px=176;w.py=140;check(!w.hazardHitsPlayer(),"Slam outside body footprint is safe");w.px=175;check(w.hazardHitsPlayer(),"Slam edge includes body radius");
        w=arena();w.bossHp=359;w.bstate=World.RECOVER;w.update(33,0,0,false,false,false);check(w.phase==2&&w.bstate==World.TRANSITION,"Phase two starts below half HP");check(w.warningTime()>=600,"Phase two retains readable warnings");
        w=arena();w.bossHp=360;w.bstate=World.RECOVER;w.update(33,0,0,false,false,false);check(w.phase==1,"Exactly half HP does not start phase two");
        w=arena();int last=-1,run=0,max=0;for(int i=0;i<100;i++){w.chooseAttack();run=w.attack==last?run+1:1;last=w.attack;max=Math.max(max,run);}check(max<=2,"Attack sequence avoids excessive repeats");
        w=arena();for(int i=0;i<200;i++)w.update(33,-1,1,false,false,false);check(w.px>=21&&w.py<=211,"Arena boundary clamps player");
        w=arena();w.hp=1;w.takeDamage(22);check(w.mode==World.LOSE,"Zero HP triggers defeat");w.storyTime=4500;w.reset();check(w.hp==100&&w.bossHp==720&&w.potions==3&&w.inv==0&&w.dodgeCd==0&&w.phase==1&&w.bstate==World.SEEK&&w.mode==World.INTRO,"Retry completely resets combat");check(w.storyTime==0,"Retry clears previous cinematic time");
        w=arena();w.bossHp=24;w.py=127;w.fy=-1;w.update(33,0,0,true,false,false);ticks(w,6);check(w.mode==World.WIN&&w.bossHp==0,"Final sword hit triggers victory");
        System.out.println("ALL "+count+" CHECKS PASSED");
    }
}
