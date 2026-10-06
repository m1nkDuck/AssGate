public class DeathTest {
    static void check(boolean b,String m){if(!b)throw new RuntimeException(m);}
    public static void main(String[] args){
        for(int i=0;i<6;i++)check(HeroSprites.deathFrame(i*Balance.DEATH_FRAME_TIME)==i,"Frame order");
        check(HeroSprites.deathFrame(720)==5&&HeroSprites.deathFrame(5000)==5,"Death must hold last frame");
        World w=new World();w.mode=World.LOSE;w.hp=0;w.deathTime=0;
        float x=w.px,y=w.py;int potions=w.potions,bhp=w.bossHp;
        w.update(120,1,1,true,true,true);
        check(w.deathTime==120&&w.px==x&&w.py==y&&w.potions==potions&&w.bossHp==bhp,"Death blocks gameplay but advances animation");
        w.reset();check(w.deathTime==0&&w.hp==Balance.PLAYER_HP&&w.potions==3&&w.mode==World.INTRO,"Retry restores animation and gameplay");
        System.out.println("PASS: Death six-frame order, last-frame hold, frozen combat and retry reset.");
    }
}
