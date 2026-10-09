/** Deliberate chained bell guardian. Geometry is shared by warning and collision. */
public final class Bellbound {
    public static final int MAX_HP=600, HEIGHT=70, BODY_RADIUS=17;
    public static final int CENTER_X=160, CENTER_Y=138, BELL_RADIUS=24;
    public static final int PHASE_TIME=1100;
    public static final int SWEEP=0, SLAM=1, TOLL=2;
    public float x,y,dx,dy,ox,oy,tx,ty;
    public int hp,phase,state,time,attack,ringIndex,hazardVersion,walkTime,hitTime;
    public boolean fallenBell,dead,moving;
    private int sequence,detourTurn;
    private boolean attackHit;
    private float escapeX,escapeY,escapeFromX,escapeFromY;

    public Bellbound(){reset();}
    public void reset(){
        x=160;y=119;dx=0;dy=1;ox=x;oy=y;tx=160;ty=183;
        hp=MAX_HP;phase=1;state=World.SEEK;time=attack=ringIndex=sequence=detourTurn=walkTime=hitTime=0;
        fallenBell=dead=attackHit=moving=false;hazardVersion++;
        escapeX=escapeFromX=x;escapeY=escapeFromY=y;
    }
    public void hit(int amount){
        if(dead||amount<=0)return;
        hp=Math.max(0,hp-amount);
        hitTime=180;
        if(hp==0){dead=true;moving=false;hitTime=0;state=World.RECOVER;time=0;hazardVersion++;}
    }
    public int warningTime(){return attack==SWEEP?900:attack==SLAM?1050:ringIndex==0?650:550;}
    public int recoveryTime(){return (attack==SLAM?1250:1000)-(phase==2?200:0);}
    /** Only the newly fallen central bell is solid; danger rings never are. */
    public boolean solid(float px,float py,float radius){
        if(!fallenBell)return false;
        float vx=px-CENTER_X,vy=py-CENTER_Y,r=BELL_RADIUS+radius;
        return vx*vx+vy*vy<r*r;
    }
    private void face(World w){
        float vx=w.px-x,vy=w.py-y,n=World.length(vx,vy);
        if(n>0.001f){dx=vx/n;dy=vy/n;}
    }
    private boolean valid(float px,float py){
        return px>=34&&px<=286&&py>=111&&py<=204&&!solid(px,py,BODY_RADIUS);
    }
    /** Small, axis-separated steps prevent the guardian crossing the fallen bell. */
    private void move(float vx,float vy){
        int count=(int)(Math.max(Math.abs(vx),Math.abs(vy))/2)+1;
        vx/=count;vy/=count;
        for(int i=0;i<count;i++){
            if(valid(x+vx,y))x+=vx;
            if(valid(x,y+vy))y+=vy;
        }
    }
    private void approach(World w,int dt){
        face(w);if(World.length(w.px-x,w.py-y)<=46)return;
        float amount=(phase==2?25:20)*dt/1000f;
        if(fallenBell){
            float rx=x-CENTER_X,ry=y-CENTER_Y,r=World.length(rx,ry);
            float vx=w.px-x,vy=w.py-y,length2=vx*vx+vy*vy;
            float along=World.clamp(-(rx*vx+ry*vy)/length2,0,1);
            float nearestX=rx+vx*along,nearestY=ry+vy*along;
            int clearance=BELL_RADIUS+BODY_RADIUS+2;
            boolean blocked=nearestX*nearestX+nearestY*nearestY<clearance*clearance;
            if(!blocked)detourTurn=0;
            if(blocked&&r<clearance+5){
                // Hold a radial tangent until the route clears. Retrying the direct
                // vector every frame would oscillate at the bell's northern edge.
                if(detourTurn==0)detourTurn=rx>=0?1:-1;
                float radial=World.clamp((clearance-r)*0.3f,-0.5f,0.5f);
                float sx=-ry/r*detourTurn+rx/r*radial,sy=rx/r*detourTurn+ry/r*radial;
                float scale=amount/World.length(sx,sy);
                move(sx*scale,sy*scale);return;
            }
        }
        move(dx*amount,dy*amount);
    }
    private void lock(World w){
        face(w);ox=x;oy=y;tx=w.px;ty=w.py;
        if(attack==TOLL){ox=CENTER_X;oy=CENTER_Y;ringIndex=0;}
        state=World.WARNING;time=0;attackHit=false;hazardVersion++;w.events|=World.TELEGRAPH;
    }
    private void beginPhase(World w){
        phase=2;state=World.TRANSITION;time=0;hazardVersion++;w.events|=World.PHASE;w.shake=100;
        escapeFromX=x;escapeFromY=y;escapeX=x;escapeY=y;
        float vx=x-CENTER_X,vy=y-CENTER_Y,n=World.length(vx,vy);
        int clearance=BELL_RADIUS+BODY_RADIUS+4;
        if(n<clearance){
            if(n<0.001f){vx=1;vy=0;n=1;}
            escapeX=World.clamp(CENTER_X+vx*clearance/n,34,286);
            escapeY=World.clamp(CENTER_Y+vy*clearance/n,111,204);
            if(World.length(escapeX-CENTER_X,escapeY-CENTER_Y)<clearance){
                escapeX=CENTER_X+(vx<0?-clearance:clearance);escapeY=CENTER_Y;
            }
        }
    }
    private void landBell(World w){
        fallenBell=true;x=escapeX;y=escapeY;hazardVersion++;w.shake=180;
        // The visible falling prop displaces, rather than damages or traps, the hero.
        if(solid(w.px,w.py,Balance.PLAYER_RADIUS)){
            float vx=w.px-CENTER_X,vy=w.py-CENTER_Y,n=World.length(vx,vy);
            if(n<0.001f){vx=0;vy=1;n=1;}
            int clearance=BELL_RADIUS+Balance.PLAYER_RADIUS+2;
            w.px=CENTER_X+vx*clearance/n;w.py=CENTER_Y+vy*clearance/n;
        }
        state=World.SEEK;time=0;
    }
    public void update(World w,int dt){
        if(dead||dt<=0)return;
        moving=false;hitTime=Math.max(0,hitTime-dt);
        time+=dt;
        if(phase==1&&hp<=MAX_HP/2&&(state==World.SEEK||state==World.RECOVER)){
            beginPhase(w);return;
        }
        if(state==World.TRANSITION){
            float p=Math.min(1,time/700f);x=escapeFromX+(escapeX-escapeFromX)*p;y=escapeFromY+(escapeY-escapeFromY)*p;
            if(time>=PHASE_TIME)landBell(w);
            return;
        }
        if(state==World.SEEK){
            float oldX=x,oldY=y;
            approach(w,dt);
            moving=Math.abs(x-oldX)+Math.abs(y-oldY)>0.001f;
            if(moving)walkTime=(walkTime+dt)%640;
            if(time>=(phase==2?420:650)){
                int[] order={SWEEP,SLAM,TOLL,SLAM,SWEEP,TOLL};
                attack=order[sequence++%order.length];state=World.PREPARE;moving=false;time=0;face(w);
            }
        }else if(state==World.PREPARE){
            face(w);if(time>=(phase==2?200:260))lock(w);
        }else if(state==World.WARNING){
            if(time>=warningTime()){
                state=World.ACTIVE;time=0;attackHit=false;w.events|=attack==TOLL?World.BELL:World.SWING;
                w.shake=attack==SLAM?130:65;
            }
        }else if(state==World.ACTIVE){
            if(!attackHit&&!w.rollingSafe()&&w.inv==0&&hits(w)){
                w.takeDamage(attack==SWEEP?24:attack==SLAM?34:22);attackHit=true;
            }
            int duration=attack==SWEEP?280:attack==SLAM?220:180;
            if(time>=duration){
                if(attack==TOLL&&ringIndex<3){
                    ringIndex++;state=World.WARNING;time=0;attackHit=false;hazardVersion++;w.events|=World.TELEGRAPH;
                }else{state=World.RECOVER;time=0;hazardVersion++;}
            }
        }else if(state==World.RECOVER&&time>=recoveryTime()){
            state=World.SEEK;time=0;
        }
    }
    private boolean hits(World w){
        int radius=Balance.PLAYER_RADIUS;
        for(int yy=-radius;yy<=radius;yy++)for(int xx=-radius;xx<=radius;xx++){
            if(xx*xx+yy*yy<=radius*radius&&inHazard(w.px+xx,w.py+yy,true))return true;
        }
        return false;
    }
    /** live=false is exactly the currently disclosed warning geometry. */
    public boolean inHazard(float px,float py,boolean live){
        if(dead||(state!=World.WARNING&&state!=World.ACTIVE)||(live&&state!=World.ACTIVE))return false;
        if(attack==SLAM){
            float vx=px-tx,vy=py-ty,r=phase==2?30:26;
            return vx*vx+vy*vy<=r*r;
        }
        float vx=px-ox,vy=py-oy,distance2=vx*vx+vy*vy;
        if(attack==SWEEP){
            float forward=vx*dx+vy*dy;
            return distance2<=85*85&&forward>=0&&forward*forward>=distance2*0.04f;
        }
        int radius=(ringIndex+1)*30,inner=radius-8,outer=radius+8;
        return distance2>=inner*inner&&distance2<=outer*outer;
    }
}
