public class RollImmunityTest {
    static int checks;
    static void check(boolean b,String s){if(!b)throw new RuntimeException("FAIL: "+s);checks++;System.out.println("PASS: "+s);}
    static World arena(){World w=new World();w.mode=World.FIGHT;w.px=160;w.py=120;w.bx=160;w.by=100;w.bstate=World.TRANSITION;w.bt=-100000;return w;}
    public static void main(String[] args){
        int[] times={0,299,300,359};
        for(int t:times){World w=arena();w.pstate=World.ROLL;w.pt=t;w.takeDamage(100);check(w.hp==100&&w.rollingSafe(),"Roll blocks damage at "+t+"ms");}
        for(int attack=0;attack<4;attack++){
            World w=arena();w.attack=attack;w.lockAttack();w.bstate=World.ACTIVE;w.pstate=World.ROLL;w.pt=300;w.rollX=0;w.rollY=1;
            w.update(33,0,0,false,false,false);check(w.hp==100&&!w.bossHit,"Late roll blocks boss attack "+attack);
            w.update(27,0,0,false,false,false);check(w.pstate==World.HURT&&w.hp==100-Balance.DAMAGE[attack],"Boss attack "+attack+" can hit when roll ends");
        }
        World w=arena();w.pstate=World.ROLL;w.pt=359;w.update(1,0,0,false,false,false);check(!w.rollingSafe()&&w.pstate==World.IDLE,"Immunity ends exactly with roll state");w.takeDamage(22);check(w.hp==78,"No extra immunity after the roll");
        System.out.println("ALL "+checks+" FULL-ROLL IMMUNITY CHECKS PASSED");
    }
}
