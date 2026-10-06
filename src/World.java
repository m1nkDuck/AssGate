/** Platform-independent fixed-step combat model; no rendering or device APIs. */
public final class World {
    public static final int TITLE=0, INTRO=1, FIGHT=2, WIN=3, LOSE=4, STORY=5;
    public static final int IDLE=0, ATTACK=1, ROLL=2, HEAL=3, HURT=4;
    public static final int SEEK=0, PREPARE=1, WARNING=2, ACTIVE=3, RECOVER=4, TRANSITION=5;
    public static final int HIT=1, BOSS_HIT=2, DRINK=4, TELEGRAPH=8, PHASE=16, END=32, SWING=64;
    public int menuIndex, storyTime;
    public boolean soundOn=true;
    public int mode=TITLE, hp, bossHp, potions, pstate, bstate, pt, bt, dodgeCd, inv;
    public int attack, lastAttack=-1, repetitions, sequence, phase=1, events, shake, clock;
    public int phaseBanner, deathTime, hazardVersion, hitsTaken, hitsLanded;
    public float px,py,bx,by,fx,fy,bdx,bdy,ox,oy,rollX,rollY,chargeLength;
    public float dashPrevious,dashNow;
    public boolean playerHit, bossHit, moving;
    public World(){reset();mode=TITLE;}
    public void reset(){
        mode=INTRO;hp=Balance.PLAYER_HP;bossHp=Balance.BOSS_HP;potions=Balance.POTIONS;
        px=160;py=220;bx=160;by=Balance.BOSS_MIN_Y;fx=0;fy=-1;bdx=0;bdy=1;
        pstate=IDLE;bstate=SEEK;pt=bt=inv=dodgeCd=0;lastAttack=-1;repetitions=sequence=0;
        phase=1;events=shake=clock=storyTime=phaseBanner=deathTime=hitsTaken=hitsLanded=0;
        playerHit=bossHit=moving=false;hazardVersion++;dashPrevious=dashNow=0;
    }
    public void startStory(){reset();mode=STORY;storyTime=0;clock=0;}
    public void finishStory(){mode=INTRO;clock=0;storyTime=0;}
    public static float length(float x,float y){return (float)Math.sqrt(x*x+y*y);}
    public static float clamp(float n,float a,float b){return n<a?a:n>b?b:n;}
    public int warningTime(){return Balance.WARN[attack]-(phase==2?120:0);}
    public int recoveryTime(){return Balance.RECOVERY[attack]-(phase==2?100:0);}
    public boolean rollingSafe(){return pstate==ROLL;}
    public boolean vulnerable(){return bstate==RECOVER;}
    public void update(int dt,int mx,int my,boolean slash,boolean dodge,boolean heal){
        events=0;clock+=dt;shake=Math.max(0,shake-dt);phaseBanner=Math.max(0,phaseBanner-dt);
        if(mode==WIN||mode==LOSE){deathTime+=dt;return;}
        if(mode==TITLE)return;
        if(mode==STORY){storyTime+=dt;if(storyTime>=Balance.STORY_SCENE_TIME*Balance.STORY_SCENES)finishStory();return;}
        if(mode==INTRO){moving=true;py-=dt*0.025f;if(py<=187){py=187;mode=FIGHT;moving=false;}return;}
        inv=Math.max(0,inv-dt);dodgeCd=Math.max(0,dodgeCd-dt);
        moving=false;
        float n=length(mx,my), dx=n>0?mx/n:0,dy=n>0?my/n:0;
        if(pstate==IDLE){
            if(n>0){fx=dx;fy=dy;}
            if(dodge&&dodgeCd==0){pstate=ROLL;pt=0;dodgeCd=Balance.ROLL_COOLDOWN;rollX=fx;rollY=fy;}
            else if(heal&&potions>0&&hp<Balance.PLAYER_HP){pstate=HEAL;pt=0;potions--;}
            else if(slash){pstate=ATTACK;pt=0;playerHit=false;events|=SWING;}
        }
        int oldPt=pt;pt+=dt;
        if(pstate==IDLE&&n>0){movePlayer(dx*Balance.WALK_SPEED*dt/1000,dy*Balance.WALK_SPEED*dt/1000);moving=true;}
        else if(pstate==ROLL){
            int amount=Math.min(dt,Math.max(0,Balance.ROLL_TIME-oldPt));
            movePlayer(rollX*Balance.ROLL_SPEED*amount/1000,rollY*Balance.ROLL_SPEED*amount/1000);
            if(pt>=Balance.ROLL_TIME){pstate=IDLE;pt=0;}
        } else if(pstate==ATTACK){
            if(!playerHit&&pt>=Balance.SWORD_WINDUP&&oldPt<Balance.SWORD_WINDUP+Balance.SWORD_ACTIVE&&swordHits()){
                bossHp=Math.max(0,bossHp-Balance.SWORD_DAMAGE);playerHit=true;hitsLanded++;events|=BOSS_HIT;shake=100;
                if(bossHp==0){mode=WIN;deathTime=0;events|=END;return;}
            }
            if(pt>=Balance.SWORD_WINDUP+Balance.SWORD_ACTIVE+Balance.SWORD_RECOVERY){pstate=IDLE;pt=0;}
        } else if(pstate==HURT&&pt>=180){pstate=IDLE;pt=0;}
        updateBoss(dt);
        // Damage is evaluated BEFORE healing completes, so a same-tick hit cancels drinking.
        if(mode==FIGHT&&pstate==HEAL&&pt>=Balance.HEAL_TIME){hp=Math.min(Balance.PLAYER_HP,hp+Balance.HEAL_AMOUNT);pstate=IDLE;pt=0;events|=DRINK;}
    }
    void movePlayer(float dx,float dy){
        float nx=clamp(px+dx,21,299),ny=clamp(py+dy,57,211);
        if(pstate!=ROLL){float vx=nx-bx,vy=ny-by,d=length(vx,vy);if(d<15&&d>0){nx=bx+vx*15/d;ny=by+vy*15/d;}}
        px=clamp(nx,21,299);py=clamp(ny,57,211);
    }
    public boolean swordHits(){
        float x=bx-px,y=by-py, d=length(x,y),forward=x*fx+y*fy;
        return d<=Balance.SWORD_RANGE+Balance.BOSS_RADIUS&&forward>=0&&forward>=d*0.43f-Balance.BOSS_RADIUS;
    }
    void facePlayer(){float x=px-bx,y=py-by,n=length(x,y);if(n>0){bdx=x/n;bdy=y/n;}}
    void chooseAttack(){
        int[] first={0,1,2,0,3,1,2,3};int[] second={2,0,3,1,0,2,1,3};
        attack=(phase==1?first:second)[sequence++%8];
        if(attack==lastAttack){repetitions++;if(repetitions>1){attack=(attack+1)%4;repetitions=0;}}else repetitions=0;
        lastAttack=attack;facePlayer();bstate=PREPARE;bt=0;
    }
    void lockAttack(){
        facePlayer();ox=bx;oy=by;chargeLength=Balance.DASH_LENGTH;
        if(bdx>0)chargeLength=Math.min(chargeLength,(291-bx)/bdx);
        if(bdx<0)chargeLength=Math.min(chargeLength,(29-bx)/bdx);
        if(bdy>0)chargeLength=Math.min(chargeLength,(202-by)/bdy);
        if(bdy<0)chargeLength=Math.min(chargeLength,(Balance.BOSS_MIN_Y-by)/bdy);
        chargeLength=Math.max(0,chargeLength);bossHit=false;dashPrevious=dashNow=0;
        bstate=WARNING;bt=0;hazardVersion++;events|=TELEGRAPH;
    }
    void updateBoss(int dt){
        bt+=dt;
        if(bstate==SEEK||bstate==RECOVER){
            if(phase==1&&bossHp*2<Balance.BOSS_HP){phase=2;bstate=TRANSITION;bt=0;phaseBanner=1800;events|=PHASE;return;}
        }
        if(bstate==TRANSITION){if(bt>=Balance.PHASE_TRANSITION){bstate=SEEK;bt=0;}return;}
        if(bstate==SEEK){
            facePlayer();float d=length(px-bx,py-by);
            if(d>48){float v=(phase==2?29:24)*dt/1000f;bx=clamp(bx+bdx*v,29,291);by=clamp(by+bdy*v,Balance.BOSS_MIN_Y,202);}
            if(bt>=(phase==2?480:700))chooseAttack();
        }else if(bstate==PREPARE){facePlayer();if(bt>=Balance.BOSS_PREPARE)lockAttack();}
        else if(bstate==WARNING){if(bt>=warningTime()){bstate=ACTIVE;bt=0;events|=SWING;shake=attack==3?140:60;}}
        else if(bstate==ACTIVE){
            if(attack==2){dashPrevious=dashNow;dashNow=chargeLength*Math.min(1,bt/(float)Balance.ACTIVE[attack]);bx=ox+bdx*dashNow;by=oy+bdy*dashNow;}
            if(!bossHit&&hazardHitsPlayer()&&!rollingSafe()&&inv==0){takeDamage(Balance.DAMAGE[attack]);bossHit=true;}
            if(bt>=Balance.ACTIVE[attack]){bstate=RECOVER;bt=0;}
        }else if(bstate==RECOVER&&bt>=recoveryTime()){bstate=SEEK;bt=0;}
    }
    /** Exact logical pixel mask used both by the telegraph rasterizer and collision. */
    public boolean inHazard(float x,float y,boolean live){
        float vx=x-ox,vy=y-oy,f=vx*bdx+vy*bdy,s=-vx*bdy+vy*bdx,r=vx*vx+vy*vy;
        if(attack==0)return r<=Balance.SWEEP_RADIUS*Balance.SWEEP_RADIUS&&f>=0&&f*f>=r*0.117f;
        if(attack==1)return f>=0&&f<=Balance.SLAM_LENGTH&&Math.abs(s)<=Balance.SLAM_HALF_WIDTH;
        if(attack==2)return f>=(live?Math.max(0,dashPrevious-12):0)&&f<=(live?Math.min(chargeLength+12,dashNow+12):chargeLength+12)&&Math.abs(s)<=Balance.DASH_HALF_WIDTH;
        return r<=Balance.QUAKE_RADIUS*Balance.QUAKE_RADIUS;
    }
    public boolean hazardHitsPlayer(){
        int r=Balance.PLAYER_RADIUS;
        for(int y=-r;y<=r;y++)for(int x=-r;x<=r;x++)if(x*x+y*y<=r*r&&inHazard(px+x,py+y,true))return true;
        return false;
    }
    public void takeDamage(int damage){
        if(inv>0||rollingSafe()||mode!=FIGHT)return;
        hp=Math.max(0,hp-damage);inv=Balance.HURT_INV;pstate=HURT;pt=0;events|=HIT;shake=150;hitsTaken++;
        if(hp==0){mode=LOSE;deathTime=0;events|=END;}
    }
}
