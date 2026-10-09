/** Bellbound tests use its real warning masks, damage, movement and phase transition. */
public final class BellboundTest {
    static int checks;
    static void check(boolean b,String s){if(!b)throw new RuntimeException("FAIL: "+s);checks++;System.out.println("PASS: "+s);}
    static World arena(){
        World w=new World();w.oathkeeperDefeated=w.oathkeeperComplete=true;w.belfry.start(w);w.belfry.enterRoom(w,4,true);
        w.mode=World.BELFRY;w.px=160;w.py=178;w.inv=0;w.pstate=World.IDLE;return w;
    }
    static Bellbound locked(World w,int attack){
        Bellbound b=w.belfry.boss;b.reset();b.x=160;b.y=120;b.attack=attack;b.state=World.PREPARE;b.time=0;
        b.update(w,260);check(b.state==World.WARNING,"Bellbound move "+attack+" enters a disclosed warning before damage");return b;
    }
    static void dangerousPoint(World w,Bellbound b){
        if(b.attack==Bellbound.SWEEP){w.px=b.ox+b.dx*35;w.py=b.oy+b.dy*35;}
        else if(b.attack==Bellbound.SLAM){w.px=b.tx;w.py=b.ty;}
        else{w.px=b.ox+(b.ringIndex+1)*30;w.py=b.oy;}
    }
    static void recovery(Bellbound b,World w){
        w.px=29;w.py=72;
        int budget=10000;
        while(b.state==World.WARNING||b.state==World.ACTIVE){b.update(w,1);if(--budget==0)throw new RuntimeException("Move never reached recovery");}
        check(b.state==World.RECOVER,"Bellbound move "+b.attack+" exposes a recovery opening");
        b.update(w,b.recoveryTime()-1);check(b.state==World.RECOVER,"Bellbound move "+b.attack+" preserves its full recovery window");
        b.update(w,1);check(b.state==World.SEEK,"Bellbound move "+b.attack+" resumes only at the exact recovery boundary");
    }
    public static void main(String[] args){try{runTests();}catch(Throwable e){e.printStackTrace();System.exit(1);}}
    static void runTests(){
        World w=arena();Bellbound b=w.belfry.boss;
        check(b.hp==600&&b.phase==1&&!b.dead&&!b.fallenBell,"Bellbound starts with 600 HP and an unobstructed arena");
        int[] damage={24,34,22};
        for(int attack=0;attack<3;attack++){
            w=arena();b=locked(w,attack);float dx=b.dx,dy=b.dy,tx=b.tx,ty=b.ty,ox=b.ox,oy=b.oy;
            w.px=238;w.py=194;b.update(w,b.warningTime()-1);
            check(b.state==World.WARNING&&w.hp==Balance.PLAYER_HP,"Bellbound move "+attack+" cannot damage during its warning");
            check(b.dx==dx&&b.dy==dy&&b.tx==tx&&b.ty==ty&&b.ox==ox&&b.oy==oy,"Bellbound move "+attack+" locks direction and target instead of tracking the hero");
            dangerousPoint(w,b);check(b.inHazard(w.px,w.py,false)&&!b.inHazard(w.px,w.py,true),"Bellbound move "+attack+" separates the disclosed mask from live damage");
            b.update(w,1);check(b.state==World.ACTIVE&&w.hp==Balance.PLAYER_HP,"Bellbound move "+attack+" activates without an early warning hit");
            b.update(w,1);check(w.hp==Balance.PLAYER_HP-damage[attack],"Bellbound move "+attack+" damages a body inside its marked area");
            w.inv=0;w.pstate=World.IDLE;b.update(w,1);
            check(w.hp==Balance.PLAYER_HP-damage[attack],"Bellbound move "+attack+" cannot hit the same hero twice in one active window");
            recovery(b,w);
        }
        w=arena();b=locked(w,Bellbound.SWEEP);
        check(b.inHazard(b.ox+b.dx*85,b.oy+b.dy*85,false)&&!b.inHazard(b.ox+b.dx*86,b.oy+b.dy*86,false),"Sweep has an exact 85-pixel outer edge");
        w.px=b.ox-b.dx*24;w.py=b.oy-b.dy*24;b.update(w,b.warningTime());b.update(w,280);
        check(w.hp==Balance.PLAYER_HP,"Sweep leaves a real safe space behind the boss including the hero body radius");
        w=arena();b=locked(w,Bellbound.SLAM);float targetX=b.tx,targetY=b.ty;
        w.px=targetX+32;w.py=targetY;b.update(w,b.warningTime());b.update(w,220);
        check(w.hp==Balance.PLAYER_HP,"Locked slam can be avoided by leaving its target circle before it lands");
        w=arena();b=locked(w,Bellbound.SLAM);w.pstate=World.ROLL;dangerousPoint(w,b);b.update(w,b.warningTime());b.update(w,1);
        check(w.hp==Balance.PLAYER_HP,"The existing full-roll immunity also blocks Bellbound damage");
        w.pstate=World.IDLE;b.update(w,1);check(w.hp==Balance.PLAYER_HP-34,"An avoided Bellbound hit remains dangerous after rolling ends");
        w=arena();b=locked(w,Bellbound.TOLL);int previousVersion=b.hazardVersion;
        for(int ring=0;ring<4;ring++){
            check(b.ringIndex==ring&&b.state==World.WARNING,"Toll discloses sequential ring "+ring+" separately");
            int radius=(ring+1)*30;
            check(b.inHazard(Bellbound.CENTER_X+radius,Bellbound.CENTER_Y,false)&&!b.inHazard(Bellbound.CENTER_X,Bellbound.CENTER_Y,false),"Toll ring "+ring+" has a safe hollow center");
            if(ring>0)check(!b.inHazard(Bellbound.CENTER_X+radius-30,Bellbound.CENTER_Y,false)&&b.hazardVersion>previousVersion,"Toll removes the previous ring and refreshes its disclosed mask");
            previousVersion=b.hazardVersion;w.px=Bellbound.CENTER_X;w.py=Bellbound.CENTER_Y;
            b.update(w,b.warningTime()-1);check(b.state==World.WARNING,"Toll ring "+ring+" retains its entire individual warning");
            b.update(w,1);b.update(w,180);check(w.hp==Balance.PLAYER_HP,"Toll ring "+ring+" leaves the central body footprint unharmed");
        }
        check(b.state==World.RECOVER&&b.ringIndex==3,"Toll recovers only after all four outward rings have finished");
        w=arena();b=w.belfry.boss;w.pstate=World.ROLL;int seen=0,selected=0,previous=b.state;
        for(int elapsed=0;elapsed<30000&&selected<6;elapsed+=Balance.STEP){
            b.update(w,Balance.STEP);
            if(b.state==World.PREPARE&&previous!=World.PREPARE){seen|=1<<b.attack;selected++;}
            previous=b.state;
        }
        check(selected==6&&seen==7,"Bellbound's real AI selects all three moves in its attack sequence");
        w=arena();b=locked(w,Bellbound.SWEEP);b.hit(300);b.update(w,1);
        check(b.phase==1&&b.state==World.WARNING,"Crossing half HP never cancels an already disclosed warning");
        w.px=29;w.py=72;int budget=5000;while(b.state==World.WARNING||b.state==World.ACTIVE){b.update(w,1);if(--budget==0)throw new RuntimeException("Phase threshold attack never finished");}
        check(b.state==World.RECOVER&&b.phase==1,"The current attack finishes before the phase transition");
        b.update(w,1);check(b.phase==2&&b.state==World.TRANSITION,"Half HP begins phase two at the next recovery tick");
        w=arena();b=w.belfry.boss;b.x=Bellbound.CENTER_X;b.y=Bellbound.CENTER_Y;w.px=b.x;w.py=b.y;b.hit(300);b.update(w,1);
        check(b.phase==2&&b.state==World.TRANSITION&&!b.fallenBell&&!b.solid(Bellbound.CENTER_X,Bellbound.CENTER_Y,Balance.PLAYER_RADIUS),"Phase two visibly withdraws before introducing the physical fallen bell");
        b.update(w,Bellbound.PHASE_TIME-1);check(!b.fallenBell,"The bell is not solid before the final transition millisecond");
        int hp=w.hp;b.update(w,1);
        check(b.fallenBell&&b.state==World.SEEK&&b.solid(Bellbound.CENTER_X,Bellbound.CENTER_Y,0),"The fallen bell becomes a persistent physical obstacle at the phase boundary");
        check(!b.solid(w.px,w.py,Balance.PLAYER_RADIUS)&&!b.solid(b.x,b.y,Bellbound.BODY_RADIUS)&&w.hp==hp,"The falling bell safely separates both actors without trapping or damaging the hero");
        float oldX=w.px;w.update(Balance.STEP,1,0,false,false,false);
        check(w.px>oldX&&!b.solid(w.px,w.py,Balance.PLAYER_RADIUS),"A hero displaced by the fallen bell can immediately walk away");
        b.state=World.RECOVER;b.time=-100000;b.x=235;b.y=172;w.pstate=World.IDLE;w.inv=0;w.px=198;w.py=Bellbound.CENTER_Y;w.fx=-1;w.fy=0;
        w.update(Balance.STEP,-1,0,false,true,false);
        for(int i=0;i<12;i++){w.update(Balance.STEP,-1,0,false,false,false);if(b.solid(w.px,w.py,Balance.PLAYER_RADIUS))throw new RuntimeException("Roll crossed the fallen bell");}
        check(w.px>=Bellbound.CENTER_X+Bellbound.BELL_RADIUS+Balance.PLAYER_RADIUS,"Rolling obeys the same physical bell collision as walking");
        for(int side=-1;side<=1;side+=2){
            w=arena();b=w.belfry.boss;b.phase=2;b.fallenBell=true;b.x=160+side*31;b.y=111;
            w.px=160-side*45;w.py=111;
            for(int i=0;i<600&&World.length(w.px-b.x,w.py-b.y)>46;i++){
                b.state=World.SEEK;b.time=0;b.update(w,Balance.STEP);
                if(b.solid(b.x,b.y,Bellbound.BODY_RADIUS))throw new RuntimeException("Boss entered the fallen bell while taking a detour");
            }
            check(World.length(w.px-b.x,w.py-b.y)<=46,"Bellbound can detour around the fallen bell from northern side "+side+" without getting stuck");
        }
        b.reset();b.hit(-1);b.hit(0);check(b.hp==600,"Nonpositive hits do not damage Bellbound");
        b.hit(600);int version=b.hazardVersion;b.hit(24);b.update(w,10000);
        check(b.dead&&b.hp==0&&b.hazardVersion==version&&!b.inHazard(160,138,false),"Bellbound dies once, stops simulation and removes all active hazards");
        System.out.println("ALL "+checks+" BELLBOUND CHECKS PASSED");
    }
}
