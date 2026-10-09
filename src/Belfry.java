/** Five connected screens. Chapter progress survives deaths and returns to the camp. */
public final class Belfry {
    public static final int NONE=0, FIRE_LIT=1, FLASK_FOUND=2, INSCRIPTION=3;
    public static final int BRIDGE_OPEN=4, CHECKPOINT=5, EXIT_LOCKED=6, CHAPTER_COMPLETE=7;
    public static final int NOTICE_NONE=NONE, NOTICE_FIRE_LIT=FIRE_LIT, NOTICE_FLASK_FOUND=FLASK_FOUND;
    public static final int NOTICE_INSCRIPTION=INSCRIPTION, NOTICE_BRIDGE_OPEN=BRIDGE_OPEN;
    public static final int NOTICE_CHECKPOINT=CHECKPOINT, NOTICE_EXIT_LOCKED=EXIT_LOCKED, NOTICE_CHAPTER_COMPLETE=CHAPTER_COMPLETE;
    public static final int ENEMY_HIT_TIME=180, ENEMY_DEATH_TIME=500, ENEMY_CORPSE_TIME=720;
    public int room,roomTime,fireMask,notice,noticeTime,checkpoint,fragment,wakeTime,hazardVersion,entranceQuoteTime;
    public boolean guardianDefeated,flaskTaken,inscriptionRead,entranceQuoteSeen;
    public final Bellbound boss=new Bellbound();
    public final Enemy[] enemies={new Enemy(),new Enemy()};
    private int clearedMask,roomOneKills,spawnDelay,bellStage,lastBell,lastBossHazard;

    public static final class Enemy {
        public float x,y,dx,dy,ox,oy;
        public int hp,state,time,walkTime,hitTime,deathTime;
        public boolean active,elite,lit,moving;
        private boolean hitOnce;
    }

    public Belfry(){resetProgress();}
    public void resetProgress(){
        room=roomTime=fireMask=notice=noticeTime=checkpoint=fragment=wakeTime=entranceQuoteTime=0;
        clearedMask=roomOneKills=spawnDelay=bellStage=lastBell=0;
        guardianDefeated=flaskTaken=inscriptionRead=entranceQuoteSeen=false;clearEnemies();boss.reset();
        lastBossHazard=boss.hazardVersion;hazardVersion++;
    }
    private void clearEnemies(){
        for(int i=0;i<enemies.length;i++){
            Enemy e=enemies[i];e.active=e.elite=e.lit=e.hitOnce=false;e.hp=e.time=0;e.state=World.SEEK;
            e.moving=false;e.walkTime=e.hitTime=0;e.deathTime=ENEMY_CORPSE_TIME+1;
            e.x=e.y=e.ox=e.oy=e.dx=0;e.dy=1;
        }
    }
    public int fireIndex(){return room==1?0:room==2?1:room==3?2:-1;}
    public int torchX(){return room==1?62:room==2?260:room==3?73:-100;}
    public int torchY(){return room==1?115:room==2?105:room==3?100:-100;}
    public boolean inLight(float x,float y){
        int index=fireIndex();return index>=0&&(fireMask&(1<<index))!=0&&World.length(x-torchX(),y-torchY())<=Balance.BELFRY_LIGHT_RADIUS;
    }
    public boolean solid(float x,float y,float radius){
        if(room==2)return World.length(x-160,y-142)<30+radius;
        return room==4&&boss.solid(x,y,radius);
    }
    public boolean exitReady(){
        if(room==0)return (clearedMask&1)!=0;
        if(room==1)return roomOneKills>=2;
        if(room==2)return true;
        if(room==3)return (fireMask&7)==7&&guardianDefeated;
        return boss.dead||fragment>0;
    }
    public boolean canLeave(){return exitReady();}
    private void showNotice(int value){notice=value;noticeTime=Balance.BELFRY_NOTICE_TIME;}
    public void start(World w){enterRoom(w,0,true);}
    public void enterRoom(World w,int next,boolean fromLeft){
        room=Math.max(0,Math.min(4,next));roomTime=spawnDelay=bellStage=lastBell=0;wakeTime=Balance.CAMP_WAKE_TIME;
        entranceQuoteTime=0;
        if(room==0&&!entranceQuoteSeen){entranceQuoteSeen=true;entranceQuoteTime=Balance.BELFRY_QUOTE_TIME;}
        notice=NONE;noticeTime=0;clearEnemies();w.mode=World.BELFRY;
        w.placePlayer(fromLeft?40:280,145,fromLeft?1:-1,0);
        if(room==1&&roomOneKills<2)spawn(enemies[roomOneKills],roomOneKills==0?235:245,roomOneKills==0?120:182,false);
        if(room==3&&(fireMask&4)!=0&&!guardianDefeated)spawn(enemies[0],210,126,true);
        if(room==4){
            if(fragment==0)boss.reset();else{boss.hp=0;boss.dead=true;}
            lastBossHazard=boss.hazardVersion;
        }
        hazardVersion++;
    }
    public void respawn(World w){
        int destination=guardianDefeated?3:0;checkpoint=destination;
        w.hp=Balance.PLAYER_HP;w.potions=Balance.POTIONS;enterRoom(w,destination,true);
        if(destination==3)w.placePlayer(242,177,-1,0);
        wakeTime=entranceQuoteTime=0;w.events=0;
    }
    private void spawn(Enemy e,float x,float y,boolean elite){
        e.x=e.ox=x;e.y=e.oy=y;e.dx=-1;e.dy=0;e.hp=elite?Balance.BELFRY_ELITE_HP:Balance.BELFRY_GUARD_HP;
        e.state=World.SEEK;e.time=0;e.active=true;e.elite=elite;e.lit=inLight(x,y);e.hitOnce=false;hazardVersion++;
        e.moving=false;e.walkTime=e.hitTime=e.deathTime=0;
    }
    /** Context actions consume a fresh J press; the same press never also swings. */
    public boolean interact(World w){
        if(w.mode!=World.BELFRY||wakeTime<Balance.CAMP_WAKE_TIME||w.pstate!=World.IDLE)return false;
        int index=fireIndex();
        if(index>=0&&World.length(w.px-torchX(),w.py-torchY())<=26){
            if((fireMask&(1<<index))==0){
                fireMask|=1<<index;hazardVersion++;w.events|=World.DRINK;
                if(room==3&&!guardianDefeated)spawn(enemies[0],210,126,true);
            }
            showNotice(FIRE_LIT);
            return true;
        }
        if(room==2){
            if(!flaskTaken&&World.length(w.px-72,w.py-185)<=22){flaskTaken=true;w.potions=Math.min(Balance.POTIONS,w.potions+1);showNotice(FLASK_FOUND);w.events|=World.DRINK;return true;}
            if(World.length(w.px-60,w.py-110)<=24){inscriptionRead=true;showNotice(INSCRIPTION);return true;}
        }
        if(room==3&&guardianDefeated&&World.length(w.px-242,w.py-177)<=30){
            checkpoint=3;w.hp=Balance.PLAYER_HP;w.potions=Balance.POTIONS;showNotice(CHECKPOINT);w.events|=World.DRINK;return true;
        }
        if(World.length(w.px-29,w.py-145)<=25){
            if(room==0)w.enterCamp(false);else enterRoom(w,room-1,false);
            return true;
        }
        if(World.length(w.px-291,w.py-145)<=25){
            if(exitReady()){if(room<4)enterRoom(w,room+1,true);}
            else showNotice(EXIT_LOCKED);
            return true;
        }
        return false;
    }
    public void update(World w,int dt,int mx,int my,boolean slash,boolean dodge,boolean heal){
        roomTime+=dt;entranceQuoteTime=Math.max(0,entranceQuoteTime-dt);noticeTime=Math.max(0,noticeTime-dt);if(noticeTime==0)notice=NONE;
        if(wakeTime<Balance.CAMP_WAKE_TIME){wakeTime=Math.min(Balance.CAMP_WAKE_TIME,wakeTime+dt);w.moving=false;return;}
        updateBells(w);
        if(room==0&&(clearedMask&1)==0&&!enemies[0].active&&roomTime>=Balance.BELFRY_FIRST_GUARD_TIME)spawn(enemies[0],235,137,false);
        if(room==1&&roomOneKills==1&&!enemies[1].active){
            spawnDelay=Math.max(0,spawnDelay-dt);if(spawnDelay==0)spawn(enemies[1],245,182,false);
        }
        int beforeRoom=room;
        boolean consumed=slash&&interact(w);
        if(w.mode!=World.BELFRY||room!=beforeRoom)return;
        for(int i=0;i<enemies.length;i++){
            Enemy e=enemies[i];e.moving=false;e.hitTime=Math.max(0,e.hitTime-dt);
            if(!e.active&&e.hp==0&&e.deathTime<=ENEMY_CORPSE_TIME)e.deathTime=Math.min(ENEMY_CORPSE_TIME+1,e.deathTime+dt);
        }
        w.updatePlayer(dt,mx,my,slash&&!consumed,dodge,heal);
        if(room==4&&boss.dead){finishChapter(w);return;}
        for(int i=0;i<enemies.length&&w.mode==World.BELFRY;i++)if(enemies[i].active){
            Enemy e=enemies[i];float oldX=e.x,oldY=e.y;updateEnemy(w,e,dt);
            e.moving=e.active&&(e.x!=oldX||e.y!=oldY);if(e.moving)e.walkTime+=dt;
        }
        if(w.mode==World.BELFRY&&room==4){
            boss.update(w,dt);
            if(lastBossHazard!=boss.hazardVersion){lastBossHazard=boss.hazardVersion;hazardVersion++;}
            if(boss.dead){finishChapter(w);return;}
        }
        w.completeHealing();
    }
    private void updateBells(World w){
        if(boss.dead||fragment>0)return;
        if(room==0&&bellStage==0&&roomTime>=500){bellStage=1;lastBell=500;w.events|=World.BELL;}
        if(room==0&&bellStage==1&&roomTime>=1800){bellStage=2;lastBell=1800;w.events|=World.BELL;}
        if(roomTime-lastBell>=5500){lastBell=roomTime;w.events|=World.BELL;}
    }
    private void finishChapter(World w){
        if(w.mode!=World.BELFRY)return;
        fragment=1;showNotice(CHAPTER_COMPLETE);hazardVersion++;lastBossHazard=boss.hazardVersion;w.mode=World.DAWN;w.deathTime=0;
        w.pstate=World.IDLE;w.pt=w.inv=0;w.moving=false;w.events|=World.END;
    }
    boolean strike(World w){
        boolean hit=false;
        for(int i=0;i<enemies.length;i++){
            Enemy e=enemies[i];
            if(e.active&&w.swordHits(e.x,e.y,e.elite?10:8)){
                e.hp=Math.max(0,e.hp-Balance.SWORD_DAMAGE);e.hitTime=ENEMY_HIT_TIME;hit=true;
                if(e.hp==0)defeatEnemy(e);
            }
        }
        if(room==4&&!boss.dead&&w.swordHits(boss.x,boss.y,17)){boss.hit(Balance.SWORD_DAMAGE);hit=true;}
        if(hit){w.hitsLanded++;w.events|=World.BOSS_HIT;w.shake=90;}
        return hit;
    }
    private void defeatEnemy(Enemy e){
        if(!e.active)return;e.active=false;e.hp=0;e.moving=false;e.deathTime=0;hazardVersion++;
        if(room==0)clearedMask|=1;
        else if(room==1){roomOneKills++;if(roomOneKills==1)spawnDelay=Balance.BELFRY_SECOND_GUARD_DELAY;else clearedMask|=2;}
        else if(room==3){guardianDefeated=true;checkpoint=3;clearedMask|=8;if((fireMask&7)==7)showNotice(BRIDGE_OPEN);}
    }
    private void facePlayer(World w,Enemy e){
        float dx=w.px-e.x,dy=w.py-e.y,n=World.length(dx,dy);if(n>0){e.dx=dx/n;e.dy=dy/n;}
    }
    private void changeState(Enemy e,int state){e.state=state;e.time=0;hazardVersion++;}
    private void updateEnemy(World w,Enemy e,int dt){
        if(e.hp<=0){defeatEnemy(e);return;}
        e.lit=inLight(e.x,e.y);e.time+=dt;
        if(e.state==World.SEEK){
            facePlayer(w,e);float distance=World.length(w.px-e.x,w.py-e.y),stop=e.elite?64:28;
            if(distance>stop){float step=Math.min(distance-stop,(e.lit?Balance.BELFRY_LIT_SPEED:Balance.BELFRY_GUARD_SPEED)*dt/1000f);moveEnemy(e,e.dx*step,e.dy*step);}
            if(e.time>=(e.elite?430:500)&&distance<=(e.elite?108:51)){
                facePlayer(w,e);e.ox=e.x;e.oy=e.y;e.hitOnce=false;changeState(e,World.WARNING);w.events|=World.TELEGRAPH;
            }
        }else if(e.state==World.WARNING){
            if(e.time>=(e.elite?Balance.BELFRY_ELITE_WARNING_TIME:Balance.BELFRY_WARNING_TIME)){changeState(e,World.ACTIVE);w.events|=World.SWING;}
        }else if(e.state==World.ACTIVE){
            if(!e.hitOnce&&!w.rollingSafe()&&w.inv==0&&enemyHitsPlayer(w,e)){w.takeDamage(e.elite?Balance.BELFRY_ELITE_DAMAGE:Balance.BELFRY_GUARD_DAMAGE);e.hitOnce=true;}
            if(e.time>=(e.elite?210:180))changeState(e,World.RECOVER);
        }else if(e.state==World.RECOVER&&e.time>=(e.elite?800:650))changeState(e,World.SEEK);
    }
    public boolean enemyHazard(Enemy e,float x,float y){
        if(!e.active)return false;
        float vx=x-e.ox,vy=y-e.oy,f=vx*e.dx+vy*e.dy,s=-vx*e.dy+vy*e.dx,r=vx*vx+vy*vy;
        if(e.elite)return f>=0&&f<=110&&Math.abs(s)<=10;
        return r<=42*42&&f>=0&&f*f>=r*0.16f;
    }
    public boolean hazardAt(float x,float y){
        for(int i=0;i<enemies.length;i++){Enemy e=enemies[i];if((e.state==World.WARNING||e.state==World.ACTIVE)&&enemyHazard(e,x,y))return true;}
        return room==4&&!boss.dead&&(boss.state==World.WARNING||boss.state==World.ACTIVE)&&boss.inHazard(x,y,false);
    }
    private boolean enemyHitsPlayer(World w,Enemy e){
        int radius=Balance.PLAYER_RADIUS;
        for(int y=-radius;y<=radius;y++)for(int x=-radius;x<=radius;x++)if(x*x+y*y<=radius*radius&&enemyHazard(e,w.px+x,w.py+y))return true;
        return false;
    }
    /** Axis separation and <=3px substeps prevent rolls tunneling through a fallen bell. */
    void movePlayer(World w,float dx,float dy){
        int steps=Math.max(1,(int)(Math.max(Math.abs(dx),Math.abs(dy))/3)+1);dx/=steps;dy/=steps;
        for(int i=0;i<steps;i++){
            float nx=World.clamp(w.px+dx,21,299),ny=World.clamp(w.py+dy,75,211);
            if(!solid(nx,w.py,Balance.PLAYER_RADIUS))w.px=nx;
            if(!solid(w.px,ny,Balance.PLAYER_RADIUS))w.py=ny;
        }
    }
    private void moveEnemy(Enemy e,float dx,float dy){
        int steps=Math.max(1,(int)(Math.max(Math.abs(dx),Math.abs(dy))/3)+1);dx/=steps;dy/=steps;
        for(int i=0;i<steps;i++){
            float nx=World.clamp(e.x+dx,29,291),ny=World.clamp(e.y+dy,85,204);
            if(!solid(nx,e.y,8))e.x=nx;
            if(!solid(e.x,ny,8))e.y=ny;
        }
    }
}
